package com.emergent.doom.swap;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.swap.FrozenCellStatus.FrozenType;

/**
 * Core swap engine that handles cell exchange mechanics.
 * 
 * <p>The swap engine is responsible for:
 * <ul>
 *   <li>Evaluating whether a swap should occur based on cell comparison</li>
 *   <li>Respecting frozen cell constraints</li>
 *   <li>Performing the actual swap operation</li>
 *   <li>Tracking swap statistics</li>
 * </ul>
 * </p>
 * 
 * <p><strong>Swap Logic:</strong> A swap occurs when both cells satisfy
 * frozen constraints. The comparison decision is handled externally
 * by the ExecutionEngine per algotype rules.</p>
 * 
 * @param <T> the type of cell
 */
public class SwapEngine<T extends Cell<T>> {
    
    private final FrozenCellStatus frozenStatus;
    private int swapCount;
    
    // PURPOSE: Initialize the swap engine with frozen cell tracking
    // INPUTS: frozenStatus (FrozenCellStatus) - tracks which cells are frozen
    // PROCESS:
    //   1. Store reference to frozenStatus
    //   2. Initialize swapCount to 0
    // OUTPUTS: SwapEngine instance
    // DEPENDENCIES: FrozenCellStatus [IMPLEMENTED ✓]
    public SwapEngine(FrozenCellStatus frozenStatus) {
        this.frozenStatus = frozenStatus;
        this.swapCount = 0;
    }
    
    /**
     * IMPLEMENTED: Attempt to swap cells at positions i and j
     * The swap decision is made externally; this method only checks frozen constraints.
     * @return true if swap occurred, false otherwise
     */
    public boolean attemptSwap(T[] cells, int i, int j) {
        // Check frozen constraints
        if (!frozenStatus.canMove(i) || !frozenStatus.canBeDisplaced(j)) {
            return false;
        }

        // Perform the swap (decision made externally)
        T temp = cells[i];
        cells[i] = cells[j];
        cells[j] = temp;
        swapCount++;
        return true;
    }
    
    /**
     * IMPLEMENTED: Get the total number of swaps performed
     */
    public int getSwapCount() {
        return swapCount;
    }
    
    /**
     * IMPLEMENTED: Reset the swap counter to zero
     */
    public void resetSwapCount() {
        swapCount = 0;
    }
    
    /**
     * IMPLEMENTED: Check if a swap between i and j would be valid
     * This only checks frozen constraints; decision is external.
     */
    public boolean wouldSwap(T[] cells, int i, int j) {
        // Check frozen constraints (same as attemptSwap)
        return frozenStatus.canMove(i) && frozenStatus.canBeDisplaced(j);
    }
}
