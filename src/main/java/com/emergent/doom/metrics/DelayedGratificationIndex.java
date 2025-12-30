package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;

/**
 * Measures the degree to which better cells appear later in the array.
 * 
 * <p>This metric captures "delayed gratification" where initially worse
 * solutions must be accepted before finding better ones. It computes the
 * position-weighted quality difference.</p>
 * 
 * @param <T> the type of cell
 */
public class DelayedGratificationIndex<T extends Cell<T>> implements Metric<T> {
    
    // PURPOSE: Compute weighted position-quality correlation
    // INPUTS: cells (T[]) - the array to analyze
    // PROCESS:
    //   1. For each position i, compute a quality score based on cell comparisons
    //   2. Weight the quality by position (later positions get higher weight)
    //   3. Sum the weighted qualities
    //   4. Normalize by array size
    // OUTPUTS: double - the delayed gratification index
    // DEPENDENCIES: Cell.compareTo() [DEFINED IN INTERFACE]
    // NOTE: Higher values indicate more delayed gratification
    //       (better cells concentrated at end)
    @Override
    public double compute(T[] cells) {
        // Implementation will go here
        return 0.0;
    }
    
    @Override
    public String getName() {
        return "Delayed Gratification Index";
    }
    
    @Override
    public boolean isLowerBetter() {
        return false; // Higher DGI can indicate more complex search paths
    }
}
