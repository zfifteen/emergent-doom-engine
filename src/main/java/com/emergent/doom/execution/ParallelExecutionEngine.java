package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.SelectionCell;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.ConcurrentSwapCollector;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.swap.SwapProposal;
import com.emergent.doom.topology.BubbleTopology;
import com.emergent.doom.topology.InsertionTopology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Parallel execution engine implementing one-thread-per-cell model.
 *
 * <p>This implementation matches the Levin paper specification:
 * "We used multi-thread programming to implement the cell-view sorting algorithms.
 * 2 types of threads were involved during the sorting process: cell threads are
 * used to represent all cells, with each cell represented by a single thread;
 * a main thread is used to activate all the threads and monitor the sorting process."</p>
 *
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>One {@link Thread} per cell in the array</li>
 *   <li>{@link CyclicBarrier} with N+1 parties for synchronization</li>
 *   <li>Main thread coordinates phases and resolves conflicts</li>
 * </ul>
 *
 * <p><strong>Execution Flow per Step:</strong></p>
 * <ol>
 *   <li><strong>Phase 1 (Parallel):</strong> All cells evaluate neighbors and propose swaps</li>
 *   <li><strong>Phase 2 (Main Thread):</strong> Resolve conflicts using leftmost priority</li>
 *   <li><strong>Phase 3 (Main Thread):</strong> Execute non-conflicting swaps</li>
 *   <li><strong>Phase 4 (Main Thread):</strong> Update metrics and check convergence</li>
 * </ol>
 *
 * @param <T> the type of cell
 */
public class ParallelExecutionEngine<T extends Cell<T>> {

    private final T[] cells;
    private final Thread[] cellThreads;
    private final CellThread<T>[] cellWorkers;
    private final CyclicBarrier barrier;
    private final ConcurrentSwapCollector swapCollector;
    private final SwapEngine<T> swapEngine;
    private final Probe<T> probe;
    private final ConvergenceDetector<T> convergenceDetector;

    // Topology helpers for evaluation
    private final BubbleTopology<T> bubbleTopology;
    private final InsertionTopology<T> insertionTopology;

    private volatile boolean running = false;
    private volatile boolean converged = false;
    private int currentStep = 0;

    // Barrier timeout to prevent infinite waits during shutdown
    private static final long BARRIER_TIMEOUT_MS = 5000;

    /**
     * Create a new parallel execution engine.
     *
     * @param cells the cell array to sort
     * @param swapEngine the swap engine for executing swaps
     * @param probe the probe for recording snapshots
     * @param convergenceDetector the convergence detector
     */
    @SuppressWarnings("unchecked")
    public ParallelExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector) {

        this.cells = cells;
        this.swapEngine = swapEngine;
        this.probe = probe;
        this.convergenceDetector = convergenceDetector;

        // Initialize topology helpers
        this.bubbleTopology = new BubbleTopology<>();
        this.insertionTopology = new InsertionTopology<>();

        // Create barrier with N+1 parties (N cells + 1 main thread)
        this.barrier = new CyclicBarrier(cells.length + 1);
        this.swapCollector = new ConcurrentSwapCollector();

        // Create cell threads
        this.cellThreads = new Thread[cells.length];
        this.cellWorkers = (CellThread<T>[]) new CellThread[cells.length];

        // Create evaluator function
        CellEvaluator<T> evaluator = this::evaluateCell;

        for (int i = 0; i < cells.length; i++) {
            cellWorkers[i] = new CellThread<>(i, cells, barrier, swapCollector, evaluator);
            cellThreads[i] = new Thread(cellWorkers[i], "Cell-" + i);
            cellThreads[i].setDaemon(true);
        }

        // Record initial state
        probe.recordSnapshot(0, cells, 0);
    }

    /**
     * Start all cell threads. Must be called before step().
     */
    public void start() {
        if (running) {
            throw new IllegalStateException("Engine already running");
        }
        running = true;
        for (Thread t : cellThreads) {
            t.start();
        }
    }

    /**
     * Execute a single step of parallel sorting.
     *
     * @return the number of swaps performed in this step
     */
    public int step() {
        if (!running) {
            throw new IllegalStateException("Engine not started. Call start() first.");
        }

        try {
            // Reset swap collector and counter
            swapCollector.clear();
            swapEngine.resetSwapCount();

            // Barrier 1: Release cell threads to evaluate
            barrier.await(BARRIER_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            // Barrier 2: Wait for all cells to finish evaluating
            barrier.await(BARRIER_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            // Phase 2: Resolve conflicts (main thread only)
            List<SwapProposal> resolved = resolveConflicts();

            // Phase 3: Execute approved swaps (main thread only)
            int swapCount = executeSwaps(resolved);

            // Barrier 3: Release cell threads for next step
            barrier.await(BARRIER_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            // Phase 4: Update state
            currentStep++;
            probe.recordSnapshot(currentStep, cells, swapCount);
            converged = convergenceDetector.hasConverged(probe, currentStep);

            return swapCount;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Step interrupted", e);
        } catch (BrokenBarrierException e) {
            throw new RuntimeException("Barrier broken during step", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Barrier timeout during step", e);
        }
    }

    /**
     * Run until convergence or max steps reached.
     *
     * @param maxSteps maximum number of steps to execute
     * @return total number of steps executed
     */
    public int runUntilConvergence(int maxSteps) {
        if (!running) {
            start();
        }

        while (!converged && currentStep < maxSteps) {
            step();
        }

        return currentStep;
    }

    /**
     * Shutdown the engine and stop all cell threads.
     */
    public void shutdown() {
        running = false;

        // Signal all workers to stop
        for (CellThread<T> worker : cellWorkers) {
            worker.stop();
        }

        // Reset barrier to unblock waiting threads
        barrier.reset();

        // Interrupt all threads
        for (Thread t : cellThreads) {
            t.interrupt();
        }

        // Wait for threads to terminate
        for (Thread t : cellThreads) {
            try {
                t.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Evaluate a cell and return a swap proposal if appropriate.
     * This is called by each cell thread in parallel.
     */
    private Optional<SwapProposal> evaluateCell(int cellIndex, T[] cellArray) {
        T cell = cellArray[cellIndex];
        Algotype algotype = cell.getAlgotype();

        List<Integer> neighbors = getNeighborsForAlgotype(cellIndex, algotype, cellArray);

        for (int neighborIndex : neighbors) {
            if (shouldSwapForAlgotype(cellIndex, neighborIndex, algotype, cellArray)) {
                // Create proposal with initiator's position as priority (leftmost first)
                return Optional.of(new SwapProposal(cellIndex, neighborIndex));
            }
        }

        return Optional.empty();
    }

    /**
     * Get neighbors for a cell based on its algotype.
     */
    private List<Integer> getNeighborsForAlgotype(int i, Algotype algotype, T[] cellArray) {
        switch (algotype) {
            case BUBBLE:
                return bubbleTopology.getNeighbors(i, cellArray.length, algotype);
            case INSERTION:
                return insertionTopology.getNeighbors(i, cellArray.length, algotype);
            case SELECTION:
                if (cellArray[i] instanceof SelectionCell) {
                    SelectionCell<?> selCell = (SelectionCell<?>) cellArray[i];
                    int target = Math.min(selCell.getIdealPos(), cellArray.length - 1);
                    return Arrays.asList(target);
                }
                return Arrays.asList();
            default:
                throw new IllegalStateException("Unknown algotype: " + algotype);
        }
    }

    /**
     * Determine if a swap should occur based on Levin algotype rules.
     */
    private boolean shouldSwapForAlgotype(int i, int j, Algotype algotype, T[] cellArray) {
        switch (algotype) {
            case BUBBLE:
                if (j == i - 1 && cellArray[i].compareTo(cellArray[j]) < 0) {
                    return true;
                } else if (j == i + 1 && cellArray[i].compareTo(cellArray[j]) > 0) {
                    return true;
                }
                return false;

            case INSERTION:
                if (j == i - 1 && isLeftSorted(i, cellArray) && cellArray[i].compareTo(cellArray[j]) < 0) {
                    return true;
                }
                return false;

            case SELECTION:
                if (i == j) {
                    return false;
                }
                if (cellArray[i].compareTo(cellArray[j]) < 0) {
                    return true;
                } else {
                    // Swap denied: increment ideal position
                    if (cellArray[i] instanceof SelectionCell) {
                        SelectionCell<?> selCell = (SelectionCell<?>) cellArray[i];
                        if (selCell.getIdealPos() < cellArray.length - 1) {
                            selCell.incrementIdealPos();
                        }
                    }
                    return false;
                }

            default:
                return false;
        }
    }

    /**
     * Check if cells 0 to i-1 are sorted in ascending order.
     */
    private boolean isLeftSorted(int i, T[] cellArray) {
        for (int k = 0; k < i - 1; k++) {
            if (cellArray[k].compareTo(cellArray[k + 1]) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resolve swap conflicts using leftmost-priority rule.
     * Per paper: "Sort by priority (e.g., leftmost cell first)"
     */
    private List<SwapProposal> resolveConflicts() {
        List<SwapProposal> proposals = swapCollector.drainAndSort();
        List<SwapProposal> resolved = new ArrayList<>();
        Set<Integer> involvedCells = new HashSet<>();

        for (SwapProposal proposal : proposals) {
            int initiator = proposal.getInitiatorIndex();
            int target = proposal.getTargetIndex();

            // Skip if either cell is already involved in a swap this step
            if (involvedCells.contains(initiator) || involvedCells.contains(target)) {
                continue;
            }

            // Accept this proposal
            involvedCells.add(initiator);
            involvedCells.add(target);
            resolved.add(proposal);
        }

        return resolved;
    }

    /**
     * Execute the resolved swaps.
     */
    private int executeSwaps(List<SwapProposal> resolved) {
        int count = 0;
        for (SwapProposal proposal : resolved) {
            if (swapEngine.attemptSwap(cells, proposal.getInitiatorIndex(), proposal.getTargetIndex())) {
                count++;
            }
        }
        return count;
    }

    // ========== Accessors ==========

    public T[] getCells() {
        return cells;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public boolean hasConverged() {
        return converged;
    }

    public boolean isRunning() {
        return running;
    }

    public Probe<T> getProbe() {
        return probe;
    }

    /**
     * Reset execution state to initial conditions.
     */
    public void reset() {
        if (running) {
            shutdown();
        }

        currentStep = 0;
        converged = false;
        running = false;
        probe.clear();
        swapEngine.resetSwapCount();
        swapCollector.clear();
        bubbleTopology.reset();
        insertionTopology.reset();
        convergenceDetector.reset();

        // Reset SelectionCell ideal positions
        for (T cell : cells) {
            if (cell instanceof SelectionCell) {
                ((SelectionCell<?>) cell).setIdealPos(0);
            }
        }

        // Recreate cell workers (threads are single-use)
        CellEvaluator<T> evaluator = this::evaluateCell;
        for (int i = 0; i < cells.length; i++) {
            cellWorkers[i] = new CellThread<>(i, cells, barrier, swapCollector, evaluator);
            cellThreads[i] = new Thread(cellWorkers[i], "Cell-" + i);
            cellThreads[i].setDaemon(true);
        }

        probe.recordSnapshot(0, cells, 0);
    }
}
