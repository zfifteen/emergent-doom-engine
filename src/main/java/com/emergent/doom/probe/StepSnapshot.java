package com.emergent.doom.probe;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable snapshot of cell states at a specific execution step.
 *
 * <p>Step snapshots enable trajectory analysis and visualization by
 * capturing the complete state of the cell array at each iteration.</p>
 *
 * <p><strong>Cell Type Distribution:</strong> Optionally tracks the
 * distribution of cell algotypes at each step, matching the cell_research
 * Python implementation's cell_types[] tracking.</p>
 *
 * @param <T> the type of cell
 */
public class StepSnapshot<T extends Cell<T>> {

    private final int stepNumber;
    private final T[] cellStates;
    private final int swapCount;
    private final long timestamp;
    private final Map<Algotype, Integer> cellTypeDistribution;
    
    /**
     * IMPLEMENTED: Create an immutable snapshot of current execution state
     */
    public StepSnapshot(int stepNumber, T[] cellStates, int swapCount) {
        this(stepNumber, cellStates, swapCount, null);
    }

    /**
     * Create an immutable snapshot with cell type distribution tracking.
     *
     * @param stepNumber the step number
     * @param cellStates the cell array state
     * @param swapCount swaps in this step
     * @param cellTypeDistribution map of algotype to count (may be null)
     */
    public StepSnapshot(int stepNumber, T[] cellStates, int swapCount,
                        Map<Algotype, Integer> cellTypeDistribution) {
        this.stepNumber = stepNumber;
        // Create defensive copy to ensure immutability
        this.cellStates = Arrays.copyOf(cellStates, cellStates.length);
        this.swapCount = swapCount;
        this.timestamp = System.nanoTime();
        // Defensive copy of distribution map
        this.cellTypeDistribution = cellTypeDistribution != null
                ? Collections.unmodifiableMap(new HashMap<>(cellTypeDistribution))
                : null;
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

    /**
     * Get the cell type distribution at this step.
     *
     * @return unmodifiable map of algotype to count, or null if not tracked
     */
    public Map<Algotype, Integer> getCellTypeDistribution() {
        return cellTypeDistribution;
    }

    /**
     * Check if cell type distribution was recorded for this snapshot.
     *
     * @return true if cell type distribution is available
     */
    public boolean hasCellTypeDistribution() {
        return cellTypeDistribution != null;
    }
}
