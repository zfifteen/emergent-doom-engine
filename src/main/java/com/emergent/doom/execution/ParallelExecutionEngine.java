package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.HasIdealPosition;
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
import java.util.concurrent.ThreadLocalRandom;
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
    private volatile boolean reverseDirection = false;  // Track sort direction for isLeftSorted
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

        // Wire up probe to swap engine for frozen swap attempt tracking
        swapEngine.setProbe(probe);

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
     *
     * <p><strong>Python Reference:</strong> Each algotype has specific evaluation rules:
     * <ul>
     *   <li>BUBBLE: Random 50/50 direction choice (BubbleSortCell.py:66)</li>
     *   <li>SELECTION: Target ideal position</li>
     *   <li>INSERTION: Move left if left side sorted</li>
     * </ul>
     * </p>
     */
    private Optional<SwapProposal> evaluateCell(int cellIndex, T[] cellArray) {
        T cell = cellArray[cellIndex];
        Algotype algotype = cell.getAlgotype();
        com.emergent.doom.cell.SortDirection direction = getCellDirection(cell);

        List<Integer> neighbors;

        if (algotype == Algotype.BUBBLE) {
            // Random 50/50 direction choice - matches cell_research Python behavior
            // Each evaluation, cell randomly picks ONE direction (left or right), not both
            List<Integer> allNeighbors = getNeighborsForAlgotype(cellIndex, algotype, cellArray);
            if (allNeighbors.isEmpty()) {
                return Optional.empty();
            }
            // Pick ONE random neighbor (50% left, 50% right if both exist)
            int randomIndex = ThreadLocalRandom.current().nextInt(allNeighbors.size());
            neighbors = Arrays.asList(allNeighbors.get(randomIndex));
        } else {
            neighbors = getNeighborsForAlgotype(cellIndex, algotype, cellArray);
        }

        for (int neighborIndex : neighbors) {
            // Record comparison (matches Python should_move() -> record_compare_and_swap())
            probe.recordCompareAndSwap();

            if (shouldSwapWithDirection(cellIndex, neighborIndex, algotype, direction, cellArray)) {
                // Create proposal with initiator's position as priority (leftmost first)
                return Optional.of(new SwapProposal(cellIndex, neighborIndex));
            }
        }

        return Optional.empty();
    }

    /**
     * Helper: Get ideal position from a SELECTION cell (supports both SelectionCell and GenericCell)
     */
    private int getIdealPosition(T cell) {
        if (cell instanceof SelectionCell) {
            return ((SelectionCell<?>) cell).getIdealPos();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            return ((com.emergent.doom.cell.GenericCell) cell).getIdealPos();
        }
        return 0;  // Default for other cell types
    }

    /**
     * Helper: Increment ideal position for a SELECTION cell (supports both SelectionCell and GenericCell)
     */
    private void incrementIdealPosition(T cell) {
        if (cell instanceof SelectionCell) {
            ((SelectionCell<?>) cell).incrementIdealPos();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            ((com.emergent.doom.cell.GenericCell) cell).incrementIdealPos();
        }
    }

    /**
     * Helper: Set ideal position for a SELECTION cell (supports both SelectionCell and GenericCell)
     */
    private void setIdealPosition(T cell, int newIdealPos) {
        if (cell instanceof SelectionCell) {
            ((SelectionCell<?>) cell).setIdealPos(newIdealPos);
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            ((com.emergent.doom.cell.GenericCell) cell).setIdealPos(newIdealPos);
        }
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
                int idealPos = getIdealPosition(cellArray[i]);
                int target = Math.min(idealPos, cellArray.length - 1);
                return Arrays.asList(target);
            default:
                throw new IllegalStateException("Unknown algotype: " + algotype);
        }
    }

    /**
     * Helper: Get the sort direction of a cell, if it implements HasSortDirection.
     * 
     * <p>PURPOSE: Provides safe access to cell sort direction for cross-purpose
     * sorting support. Returns ASCENDING as default for cells without direction.</p>
     * 
     * @param cell the cell to query
     * @return the cell's sort direction, or ASCENDING if not direction-aware
     */
    private com.emergent.doom.cell.SortDirection getCellDirection(T cell) {
        // Check if cell implements HasSortDirection interface
        if (cell instanceof com.emergent.doom.cell.HasSortDirection) {
            // Cell supports direction - return its preference
            return ((com.emergent.doom.cell.HasSortDirection) cell).getSortDirection();
        }
        // Cell doesn't support direction - default to ascending
        return com.emergent.doom.cell.SortDirection.ASCENDING;
    }

    /**
     * Determine if a swap should occur using direction-aware comparison.
     * 
     * <p>PURPOSE: Enables cross-purpose sorting by respecting each cell's individual
     * sort direction preference during swap evaluation.</p>
     * 
     * @param i index of cell initiating swap
     * @param j index of target neighbor
     * @param algotype algorithm policy
     * @param direction sort direction preference
     * @param cellArray the cell array
     * @return true if swap should occur
     */
    private boolean shouldSwapWithDirection(int i, int j, Algotype algotype, 
                                           com.emergent.doom.cell.SortDirection direction, T[] cellArray) {
        // Get comparison result: negative if cells[i] < cells[j], positive if cells[i] > cells[j]
        int cmp = cellArray[i].compareTo(cellArray[j]);
        boolean isAscending = direction.isAscending();
        
        switch (algotype) {
            case BUBBLE:
                // BUBBLE: Move based on value comparison and direction
                // For ascending: move left if smaller, right if larger
                // For descending: move left if larger, right if smaller
                
                if (j == i - 1) { // Left neighbor
                    // Ascending: swap if i < j (cmp < 0), Descending: swap if i > j (cmp > 0)
                    return isAscending ? (cmp < 0) : (cmp > 0);
                } else if (j == i + 1) { // Right neighbor
                    // Ascending: swap if i > j (cmp > 0), Descending: swap if i < j (cmp < 0)
                    return isAscending ? (cmp > 0) : (cmp < 0);
                }
                return false;
                
            case INSERTION:
                // INSERTION: Only move left, and only if left side is sorted
                if (j == i - 1 && isLeftSorted(i, !isAscending, cellArray)) {
                    // Ascending: swap if i < j (cmp < 0), Descending: swap if i > j (cmp > 0)
                    return isAscending ? (cmp < 0) : (cmp > 0);
                }
                return false;
                
            case SELECTION:
                // Guard: Skip if targeting self
                if (i == j) {
                    return false;
                }
                
                // SELECTION: Swap with ideal target if in correct order
                // Ascending: swap if i < j (cmp < 0), Descending: swap if i > j (cmp > 0)
                boolean shouldSwap = isAscending ? (cmp < 0) : (cmp > 0);
                
                if (shouldSwap) {
                    return true;
                } else {
                    // Swap denied: increment ideal position if not at end
                    int currentIdealPos = getIdealPosition(cellArray[i]);
                    if (currentIdealPos < cellArray.length - 1) {
                        incrementIdealPosition(cellArray[i]);
                    }
                    return false;
                }
                
            default:
                return false;
        }
    }

    /**
     * Check if cells 0 to i-1 are sorted in correct order (ascending or descending).
     * Matches Python cell_research behavior: frozen cells are skipped and
     * reset the comparison chain.
     *
     * <p><strong>Python Reference:</strong> (InsertionSortCell.py:74-76)</p>
     * <pre>
     * if self.cells[i].status == CellStatus.FREEZE:
     *     prev = -1  # Reset comparison, skip frozen
     *     continue
     * </pre>
     *
     * @param i the position to check (checks cells 0 to i-1)
     * @param reverseDirection true for descending sort, false for ascending
     * @param cellArray the cell array
     * @return true if cells 0 to i-1 are sorted in the current direction
     */
    private boolean isLeftSorted(int i, boolean reverseDirection, T[] cellArray) {
        // Start with sentinel: MIN for ascending (any value >= MIN), MAX for descending (any value <= MAX)
        int prevValue = reverseDirection ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        for (int k = 0; k < i; k++) {
            // Skip frozen cells - reset comparison chain (matches Python)
            if (swapEngine.isFrozen(k)) {
                // Reset sentinel after frozen cell
                prevValue = reverseDirection ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                continue;
            }

            // Get cell value for comparison
            int currentValue = getCellValue(cellArray[k]);

            // Check if out of order based on direction
            boolean outOfOrder = reverseDirection
                ? (currentValue > prevValue)  // Descending: next should be <= prev
                : (currentValue < prevValue); // Ascending: next should be >= prev

            if (outOfOrder) {
                return false; // Out of order
            }
            prevValue = currentValue;
        }
        return true;
    }

    /**
     * Helper: Extract comparable value from cell for isLeftSorted comparison.
     *
     * <p>Throws UnsupportedOperationException for unsupported cell types
     * since hashCode() is unreliable for sorting comparisons.</p>
     */
    private int getCellValue(T cell) {
        if (cell instanceof SelectionCell) {
            return ((SelectionCell<?>) cell).getValue();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            return ((com.emergent.doom.cell.GenericCell) cell).getValue();
        } else if (cell instanceof com.emergent.doom.cell.InsertionCell) {
            return ((com.emergent.doom.cell.InsertionCell<?>) cell).getValue();
        } else if (cell instanceof com.emergent.doom.cell.BubbleCell) {
            return ((com.emergent.doom.cell.BubbleCell<?>) cell).getValue();
        }
        throw new UnsupportedOperationException(
            "Cell type " + cell.getClass().getName() + " does not support getValue(). " +
            "All Cell implementations must extend SelectionCell, GenericCell, InsertionCell, or BubbleCell."
        );
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
        reset(false); // ascending sort by default
    }

    /**
     * Reset execution state with explicit sort direction for SELECTION cells.
     *
     * @param reverseDirection true for descending sort, false for ascending
     */
    public void reset(boolean reverseDirection) {
        if (running) {
            shutdown();
        }

        currentStep = 0;
        converged = false;
        running = false;
        this.reverseDirection = reverseDirection;  // Store for isLeftSorted
        probe.clear();
        swapEngine.resetSwapCount();
        swapCollector.clear();
        bubbleTopology.reset();
        insertionTopology.reset();
        convergenceDetector.reset();

        // Reset SELECTION cell ideal positions to boundary (matches Python cell_research)
        resetSelectionCellIdealPositions(reverseDirection);

        // Recreate cell workers (threads are single-use)
        CellEvaluator<T> evaluator = this::evaluateCell;
        for (int i = 0; i < cells.length; i++) {
            cellWorkers[i] = new CellThread<>(i, cells, barrier, swapCollector, evaluator);
            cellThreads[i] = new Thread(cellWorkers[i], "Cell-" + i);
            cellThreads[i].setDaemon(true);
        }

        probe.recordSnapshot(0, cells, 0);
    }

    /**
     * Reset ideal positions for SELECTION algotype cells.
     * Uses updateForBoundary matching Python cell_research SelectionSortCell.update() behavior.
     *
     * @param reverseDirection true for descending sort (ideal = right boundary),
     *                         false for ascending (ideal = left boundary)
     */
    private void resetSelectionCellIdealPositions(boolean reverseDirection) {
        int leftBoundary = 0;
        int rightBoundary = cells.length - 1;

        for (T cell : cells) {
            if (cell.getAlgotype() == Algotype.SELECTION) {
                if (cell instanceof HasIdealPosition) {
                    ((HasIdealPosition) cell).updateForBoundary(leftBoundary, rightBoundary, reverseDirection);
                }
            }
        }
    }
}
