package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.SelectionCell;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.topology.BubbleTopology;
import com.emergent.doom.topology.InsertionTopology;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock-based execution engine matching Python cell_research behavior.
 *
 * <p>This implementation matches the Python cell_research codebase exactly:
 * Each cell thread acquires a single global lock, evaluates, potentially swaps,
 * and releases the lock. There is no phase synchronization - cells operate
 * asynchronously as they acquire the lock.</p>
 *
 * <p><strong>Python Reference:</strong> (BubbleSortCell.py:58-74)</p>
 * <pre>
 * def move(self):
 *     self.lock.acquire()    # Single global lock
 *     # ... evaluation and swap logic ...
 *     self.lock.release()
 * </pre>
 *
 * <p><strong>Key Differences from ParallelExecutionEngine:</strong></p>
 * <ul>
 *   <li>Uses ReentrantLock instead of CyclicBarrier</li>
 *   <li>No phase synchronization - cells swap as they acquire lock</li>
 *   <li>Non-deterministic execution order</li>
 *   <li>More closely matches cell_research Python behavior</li>
 * </ul>
 *
 * @param <T> the type of cell
 */
public class LockBasedExecutionEngine<T extends Cell<T>> {

    private final T[] cells;
    private final Thread[] cellThreads;
    private final LockBasedCellWorker<T>[] cellWorkers;
    private final ReentrantLock globalLock;
    private final SwapEngine<T> swapEngine;
    private final Probe<T> probe;
    private final ConvergenceDetector<T> convergenceDetector;
    private final Random random;

    // Topology helpers for evaluation
    private final BubbleTopology<T> bubbleTopology;
    private final InsertionTopology<T> insertionTopology;

    private volatile boolean running = false;
    private volatile boolean converged = false;
    private final AtomicInteger currentStep = new AtomicInteger(0);
    private final AtomicInteger totalSwaps = new AtomicInteger(0);

    // Snapshot interval for recording (every N swaps)
    private static final int SNAPSHOT_INTERVAL = 1;

    /**
     * Create a new lock-based execution engine.
     *
     * @param cells the cell array to sort
     * @param swapEngine the swap engine for executing swaps
     * @param probe the probe for recording snapshots
     * @param convergenceDetector the convergence detector
     */
    @SuppressWarnings("unchecked")
    public LockBasedExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector) {

        this.cells = cells;
        this.swapEngine = swapEngine;
        this.probe = probe;
        this.convergenceDetector = convergenceDetector;
        this.random = new Random();

        // Single global lock (matches Python cell_research)
        this.globalLock = new ReentrantLock();

        // Initialize topology helpers
        this.bubbleTopology = new BubbleTopology<>();
        this.insertionTopology = new InsertionTopology<>();

        // Create cell threads
        this.cellThreads = new Thread[cells.length];
        this.cellWorkers = (LockBasedCellWorker<T>[]) new LockBasedCellWorker[cells.length];

        for (int i = 0; i < cells.length; i++) {
            cellWorkers[i] = new LockBasedCellWorker<>(i);
            cellThreads[i] = new Thread(cellWorkers[i], "LockCell-" + i);
            cellThreads[i].setDaemon(true);
        }

        // Record initial state
        probe.recordSnapshot(0, cells, 0);
    }

    /**
     * Start all cell threads. Must be called before waiting for convergence.
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
     * Run until convergence or max steps reached.
     *
     * @param maxSteps maximum number of swap operations
     * @return total number of swap operations
     */
    public int runUntilConvergence(int maxSteps) {
        if (!running) {
            start();
        }

        // Poll for convergence
        int lastSwapCount = 0;
        int stableCount = 0;
        int requiredStable = 3; // Require 3 consecutive checks with no swaps

        while (!converged && totalSwaps.get() < maxSteps) {
            try {
                Thread.sleep(10); // Poll interval
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            int currentSwaps = totalSwaps.get();
            if (currentSwaps == lastSwapCount) {
                stableCount++;
                if (stableCount >= requiredStable * 10) { // 10ms * 30 = 300ms of no swaps
                    converged = true;
                }
            } else {
                stableCount = 0;
            }
            lastSwapCount = currentSwaps;
        }

        return totalSwaps.get();
    }

    /**
     * Shutdown the engine and stop all cell threads.
     */
    public void shutdown() {
        running = false;

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
     * Inner class representing a cell worker thread.
     * Each worker continuously acquires the lock, evaluates, swaps if needed, and releases.
     */
    private class LockBasedCellWorker<C extends Cell<C>> implements Runnable {
        private final int cellIndex;

        LockBasedCellWorker(int cellIndex) {
            this.cellIndex = cellIndex;
        }

        @Override
        public void run() {
            while (running && !converged) {
                globalLock.lock();
                try {
                    evaluateAndSwap();
                } finally {
                    globalLock.unlock();
                }

                // Small yield to allow other threads to acquire lock
                Thread.yield();

                // Check for interruption
                if (Thread.interrupted()) {
                    break;
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void evaluateAndSwap() {
            T cell = cells[cellIndex];
            Algotype algotype = cell.getAlgotype();

            List<Integer> neighbors;
            if (algotype == Algotype.BUBBLE) {
                // Random 50/50 direction choice - matches cell_research Python
                List<Integer> allNeighbors = getNeighborsForAlgotype(cellIndex, algotype);
                if (allNeighbors.isEmpty()) {
                    return;
                }
                int randomIndex = random.nextInt(allNeighbors.size());
                neighbors = Arrays.asList(allNeighbors.get(randomIndex));
            } else {
                neighbors = getNeighborsForAlgotype(cellIndex, algotype);
            }

            for (int neighborIndex : neighbors) {
                if (shouldSwapForAlgotype(cellIndex, neighborIndex, algotype)) {
                    if (swapEngine.attemptSwap(cells, cellIndex, neighborIndex)) {
                        int swaps = totalSwaps.incrementAndGet();

                        // Record snapshot periodically
                        if (swaps % SNAPSHOT_INTERVAL == 0) {
                            int step = currentStep.incrementAndGet();
                            probe.recordSnapshot(step, cells, 1);
                        }
                    }
                    break; // Only one swap per evaluation (matches Python)
                }
            }
        }
    }

    // ========== Helper Methods ==========

    private int getIdealPosition(T cell) {
        if (cell instanceof SelectionCell) {
            return ((SelectionCell<?>) cell).getIdealPos();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            return ((com.emergent.doom.cell.GenericCell) cell).getIdealPos();
        }
        return 0;
    }

    private void incrementIdealPosition(T cell) {
        if (cell instanceof SelectionCell) {
            ((SelectionCell<?>) cell).incrementIdealPos();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            ((com.emergent.doom.cell.GenericCell) cell).incrementIdealPos();
        }
    }

    private List<Integer> getNeighborsForAlgotype(int i, Algotype algotype) {
        switch (algotype) {
            case BUBBLE:
                return bubbleTopology.getNeighbors(i, cells.length, algotype);
            case INSERTION:
                return insertionTopology.getNeighbors(i, cells.length, algotype);
            case SELECTION:
                int idealPos = getIdealPosition(cells[i]);
                int target = Math.min(idealPos, cells.length - 1);
                return Arrays.asList(target);
            default:
                throw new IllegalStateException("Unknown algotype: " + algotype);
        }
    }

    private boolean shouldSwapForAlgotype(int i, int j, Algotype algotype) {
        switch (algotype) {
            case BUBBLE:
                if (j == i - 1 && cells[i].compareTo(cells[j]) < 0) {
                    return true;
                } else if (j == i + 1 && cells[i].compareTo(cells[j]) > 0) {
                    return true;
                }
                return false;

            case INSERTION:
                if (j == i - 1 && isLeftSorted(i) && cells[i].compareTo(cells[j]) < 0) {
                    return true;
                }
                return false;

            case SELECTION:
                if (i == j) {
                    return false;
                }
                if (cells[i].compareTo(cells[j]) < 0) {
                    return true;
                } else {
                    int currentIdealPos = getIdealPosition(cells[i]);
                    if (currentIdealPos < cells.length - 1) {
                        incrementIdealPosition(cells[i]);
                    }
                    return false;
                }

            default:
                return false;
        }
    }

    private boolean isLeftSorted(int i) {
        for (int k = 0; k < i - 1; k++) {
            if (cells[k].compareTo(cells[k + 1]) > 0) {
                return false;
            }
        }
        return true;
    }

    // ========== Accessors ==========

    public T[] getCells() {
        return cells;
    }

    public int getCurrentStep() {
        return currentStep.get();
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

    public int getTotalSwaps() {
        return totalSwaps.get();
    }

    /**
     * Reset execution state to initial conditions.
     */
    @SuppressWarnings("unchecked")
    public void reset() {
        if (running) {
            shutdown();
        }

        currentStep.set(0);
        totalSwaps.set(0);
        converged = false;
        running = false;
        probe.clear();
        swapEngine.resetSwapCount();
        bubbleTopology.reset();
        insertionTopology.reset();
        convergenceDetector.reset();

        // Reset SelectionCell ideal positions
        for (T cell : cells) {
            if (cell.getAlgotype() == Algotype.SELECTION) {
                if (cell instanceof SelectionCell) {
                    ((SelectionCell<?>) cell).setIdealPos(0);
                } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
                    ((com.emergent.doom.cell.GenericCell) cell).setIdealPos(0);
                }
            }
        }

        // Recreate cell workers (threads are single-use)
        for (int i = 0; i < cells.length; i++) {
            cellWorkers[i] = new LockBasedCellWorker<>(i);
            cellThreads[i] = new Thread(cellWorkers[i], "LockCell-" + i);
            cellThreads[i].setDaemon(true);
        }

        probe.recordSnapshot(0, cells, 0);
    }
}
