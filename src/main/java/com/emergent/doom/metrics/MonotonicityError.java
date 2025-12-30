package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;

/**
 * Measures deviation from perfect monotonic (sorted) order.
 * 
 * <p>Counts the number of inversions: pairs (i, j) where i < j but
 * cells[i] > cells[j]. A perfectly sorted array has zero inversions.</p>
 * 
 * @param <T> the type of cell
 */
public class MonotonicityError<T extends Cell<T>> implements Metric<T> {
    
    /**
     * IMPLEMENTED: Count inversions in the cell array
     * O(n²) complexity - suitable for small-to-medium arrays
     */
    @Override
    public double compute(T[] cells) {
        int count = 0;
        
        // Count inversions: pairs (i, j) where i < j but cells[i] > cells[j]
        for (int i = 0; i < cells.length - 1; i++) {
            for (int j = i + 1; j < cells.length; j++) {
                if (cells[i].compareTo(cells[j]) > 0) {
                    count++;
                }
            }
        }
        
        return (double) count;
    }
    
    @Override
    public String getName() {
        return "Monotonicity Error";
    }
    
    @Override
    public boolean isLowerBetter() {
        return true; // Fewer inversions is better
    }
}
