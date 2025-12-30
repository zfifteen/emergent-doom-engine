package com.emergent.doom.probe;

import com.emergent.doom.cell.Cell;
import java.util.Arrays;

/**
 * Immutable snapshot of cell states at a specific execution step.
 * 
 * <p>Step snapshots enable trajectory analysis and visualization by
 * capturing the complete state of the cell array at each iteration.</p>
 * 
 * @param <T> the type of cell
 */
public class StepSnapshot<T extends Cell<T>> {
    
    private final int stepNumber;
    private final T[] cellStates;
    private final int swapCount;
    private final long timestamp;
    
    /**
     * IMPLEMENTED: Create an immutable snapshot of current execution state
     */
    public StepSnapshot(int stepNumber, T[] cellStates, int swapCount) {
        this.stepNumber = stepNumber;
        // Create defensive copy to ensure immutability
        this.cellStates = Arrays.copyOf(cellStates, cellStates.length);
        this.swapCount = swapCount;
        this.timestamp = System.nanoTime();
    }
    
    /**
     * IMPLEMENTED: Get the step number for this snapshot
     */
    public int getStepNumber() {
        return stepNumber;
    }
    
    /**
     * IMPLEMENTED: Get a copy of the cell states at this step
     * Returns a defensive copy to preserve immutability
     */
    public T[] getCellStates() {
        return Arrays.copyOf(cellStates, cellStates.length);
    }
    
    /**
     * IMPLEMENTED: Get the number of swaps that occurred in this step
     */
    public int getSwapCount() {
        return swapCount;
    }
    
    /**
     * IMPLEMENTED: Get the timestamp when this snapshot was created
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * IMPLEMENTED: Get the size of the cell array
     */
    public int getArraySize() {
        return cellStates.length;
    }
}
