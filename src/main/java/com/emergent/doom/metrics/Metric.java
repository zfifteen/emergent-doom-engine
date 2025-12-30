package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;

/**
 * Interface for computing metrics over cell arrays.
 * 
 * <p>Metrics provide quantitative measures of cell arrangement quality,
 * ordering characteristics, and emergent properties.</p>
 * 
 * @param <T> the type of cell
 */
public interface Metric<T extends Cell<T>> {
    
    // PURPOSE: Compute the metric value for the given cell array
    // INPUTS: cells (T[]) - the array of cells to analyze
    // PROCESS:
    //   1. Apply metric-specific computation
    //   2. Return a double value representing the metric
    // OUTPUTS: double - the computed metric value
    // DEPENDENCIES: Cell.compareTo() [DEFINED IN INTERFACE]
    // NOTE: Higher or lower values may be better depending on the metric
    double compute(T[] cells);
    
    // PURPOSE: Get a human-readable name for this metric
    // INPUTS: None
    // OUTPUTS: String - metric name (e.g., "Monotonicity Error")
    // DEPENDENCIES: None
    String getName();
    
    // PURPOSE: Check if lower values are better for this metric
    // INPUTS: None
    // OUTPUTS: boolean - true if minimization metric, false if maximization
    // DEPENDENCIES: None
    default boolean isLowerBetter() {
        return true; // Most metrics are error/distance measures
    }
}
