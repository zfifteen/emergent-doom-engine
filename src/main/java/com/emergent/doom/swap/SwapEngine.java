package com.emergent.doom.swap;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.FrozenCellStatus.FrozenType;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core swap engine that handles cell exchange mechanics.
 *
 * <p>The swap engine is responsible for:
 * <ul>
 *   <li>Evaluating whether a swap should occur based on cell comparison</li>
 *   <li>Respecting frozen cell constraints</li>
 *   <li>Performing the actual swap operation</li>
 *   <li>Tracking swap statistics</li>
 *   <li>Recording frozen swap attempts to Probe (StatusProbe compatibility)</li>
 * </ul>
 * </p>
 *
 * <p><strong>Swap Logic:</strong> A swap occurs when both cells satisfy
 * frozen constraints. The comparison decision is handled externally
 * by the ExecutionEngine per algotype rules.</p>
 *
 * <p><strong>Thread Safety:</strong> The swap count uses {@link AtomicInteger}
 * for thread-safe access. However, actual swap operations on the cell array
 * must be externally synchronized (typically by the main thread after
 * conflict resolution).</p>
 *
 * @param <T> the type of cell
 */
public class SwapEngine<T extends Cell<T>> {

    private final FrozenCellStatus frozenStatus;
    private final AtomicInteger swapCount;
    private Probe<T> probe; // Optional reference for frozen swap attempt tracking
    
    /**
     * Initialize the swap engine with frozen cell tracking.
     *
     * @param frozenStatus tracks which cells are frozen
     */
    public SwapEngine(FrozenCellStatus frozenStatus) {
        this.frozenStatus = frozenStatus;
        this.swapCount = new AtomicInteger(0);
        this.probe = null;
    }

    /**
     * Set the probe reference for StatusProbe-compatible tracking.
     * When set, frozen swap attempts will be recorded to the probe.
     *
     * @param probe the probe to record frozen swap attempts to
     */
    public void setProbe(Probe<T> probe) {
        this.probe = probe;
    }
    
    /**
     * IMPLEMENTED: Attempt to swap cells at positions i and j
     * The swap decision is made externally; this method only checks frozen constraints.
     * Records frozen swap attempts to the probe if one is set.
     *
     * @return true if swap occurred, false otherwise
     */
    public boolean attemptSwap(T[] cells, int i, int j) {
        // Check frozen constraints
        if (!frozenStatus.canMove(i)) {
            // Cell at i is frozen and tried to initiate swap
            if (probe != null) {
                probe.countFrozenSwapAttempt();
            }
            return false;
        }
        if (!frozenStatus.canBeDisplaced(j)) {
            // Target is immovable (IMMOVABLE type)
            return false;
        }

        // Perform the swap (decision made externally)
        T temp = cells[i];
        cells[i] = cells[j];
        cells[j] = temp;
        swapCount.incrementAndGet();
        return true;
    }

    /**
     * IMPLEMENTED: Get the total number of swaps performed.
     * Thread-safe read.
     */
    public int getSwapCount() {
        return swapCount.get();
    }

    /**
     * IMPLEMENTED: Reset the swap counter to zero.
     * Thread-safe write.
     */
    public void resetSwapCount() {
        swapCount.set(0);
    }

    /**
     * Atomically add to the swap count.
     * Useful when executing multiple swaps in parallel execution mode.
     *
     * @param delta the number of swaps to add
     * @return the new total swap count
     */
    public int addSwapCount(int delta) {
        return swapCount.addAndGet(delta);
    }
    
    /**
     * IMPLEMENTED: Check if a swap between i and j would be valid
     * This only checks frozen constraints; decision is external.
     */
    public boolean wouldSwap(T[] cells, int i, int j) {
        // Check frozen constraints (same as attemptSwap)
        return frozenStatus.canMove(i) && frozenStatus.canBeDisplaced(j);
    }

    /**
     * Check if a cell at the given position is frozen (MOVABLE or IMMOVABLE).
     * Used for isLeftSorted skip logic matching Python cell_research behavior.
     *
     * @param position the cell position to check
     * @return true if cell is frozen (cannot initiate swaps)
     */
    public boolean isFrozen(int position) {
        return !frozenStatus.canMove(position);
    }

    /**
     * Get the frozen status tracker.
     *
     * @return the FrozenCellStatus instance
     */
    public FrozenCellStatus getFrozenStatus() {
        return frozenStatus;
    }
}
