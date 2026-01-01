package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;

/**
 * Measures deviation from perfect monotonic (sorted) order.
 *
 * <p>From Levin et al. (2024), p.8: "Monotonicity Error is defined as the
 * number of cells that violate the monotonic order."</p>
 *
 * <p>Counts the number of <strong>adjacent</strong> inversions: pairs (i, i+1) where
 * cells[i] > cells[i+1]. A perfectly sorted array has zero adjacent inversions.</p>
 *
 * <p>This matches the reference implementation in cell_research/analysis/utils.py
 * which counts adjacent violations (complement of get_monotonicity).</p>
 *
 * <p>Note: This is O(n) complexity, not O(n²). For total inversion count
 * (all pairs, not just adjacent), use {@link TotalInversionCount}.</p>
 *
 * @param <T> the type of cell
 * @see Monotonicity
 */
public class MonotonicityError<T extends Cell<T>> implements Metric<T> {

    /**
     * Count adjacent inversions in the cell array.
     *
     * <p>An adjacent inversion is a pair (i, i+1) where cells[i] > cells[i+1].</p>
     *
     * @param cells the array of cells to analyze
     * @return the count of adjacent inversions (0 to cells.length-1)
     */
    @Override
    public double compute(T[] cells) {
        if (cells == null || cells.length < 2) {
            return 0.0;
        }

        int count = 0;

        // Count adjacent inversions only: pairs (i, i+1) where cells[i] > cells[i+1]
        for (int i = 0; i < cells.length - 1; i++) {
            if (cells[i].compareTo(cells[i + 1]) > 0) {
                count++;
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
