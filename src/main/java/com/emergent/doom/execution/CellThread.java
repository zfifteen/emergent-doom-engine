package com.emergent.doom.execution;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.swap.ConcurrentSwapCollector;
import com.emergent.doom.swap.SwapProposal;

import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Runnable representing a single cell's execution thread.
 *
 * <p>Each cell in the array runs in its own thread, evaluating its
 * neighbors and proposing swaps in parallel with all other cells.
 * Synchronization is achieved via {@link CyclicBarrier}.</p>
 *
 * <p><strong>Execution Flow per Step:</strong></p>
 * <ol>
 *   <li>Wait at barrier for step start</li>
 *   <li>Evaluate neighbors and propose swap (if any)</li>
 *   <li>Wait at barrier for all cells to finish evaluating</li>
 *   <li>Wait at barrier for main thread to execute swaps</li>
 *   <li>Repeat until running=false</li>
 * </ol>
 *
 * <p><strong>DEPRECATED:</strong> This class is part of the per-cell threading model
 * which creates one thread per cell in the array. This approach is wasteful for batch
 * trial execution. Use {@link SynchronousExecutionEngine} with per-trial parallelism
 * via {@link com.emergent.doom.experiment.ExperimentRunner#runBatchExperiments(com.emergent.doom.experiment.ExperimentConfig)}
 * instead. This class is retained for comparison and compatibility.</p>
 *
 * @param <T> the type of cell
 * @deprecated Use SynchronousExecutionEngine with per-trial parallelism instead
 */
@Deprecated
public class CellThread<T extends Cell<T>> implements Runnable {

    private final int cellIndex;
    private final T[] cells;
    private final CyclicBarrier barrier;
    private final ConcurrentSwapCollector swapCollector;
    private final CellEvaluator<T> evaluator;
    private volatile boolean running = true;

    /**
     * Create a new cell thread.
     *
     * @param cellIndex the index of the cell this thread represents
     * @param cells the shared cell array (read during evaluation)
     * @param barrier the synchronization barrier (N+1 parties)
     * @param swapCollector the collector for swap proposals
     * @param evaluator the evaluator function for this cell
     */
    public CellThread(int cellIndex,
                      T[] cells,
                      CyclicBarrier barrier,
                      ConcurrentSwapCollector swapCollector,
                      CellEvaluator<T> evaluator) {
        this.cellIndex = cellIndex;
        this.cells = cells;
        this.barrier = barrier;
        this.swapCollector = swapCollector;
        this.evaluator = evaluator;
    }

    @Override
    public void run() {
        try {
            while (running) {
                // Barrier 1: Wait for step start signal from main thread
                barrier.await();

                // Check if we should terminate
                if (!running) {
                    break;
                }

                // Evaluate this cell and propose swap if appropriate
                Optional<SwapProposal> proposal = evaluator.evaluate(cellIndex, cells);
                proposal.ifPresent(swapCollector::propose);

                // Barrier 2: Wait for all cells to finish evaluating
                barrier.await();

                // Barrier 3: Wait for main thread to resolve conflicts and execute swaps
                barrier.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // Normal shutdown path when interrupted
        } catch (BrokenBarrierException e) {
            // Barrier was reset or broken, indicates shutdown
            if (running) {
                // Unexpected break - log if logging available
            }
        }
    }

    /**
     * Signal this thread to stop after the current step completes.
     * The thread will exit at the next barrier wait.
     */
    public void stop() {
        running = false;
    }

    /**
     * Check if this thread is still running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get the cell index this thread represents.
     */
    public int getCellIndex() {
        return cellIndex;
    }
}
