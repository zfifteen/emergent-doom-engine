package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;

import java.util.Arrays;

/**
 * Measures the percentage of cells in their correct sorted position.
 *
 * <p>From Levin et al. (2024), p.8:
 * "Sortedness Value is defined as the percentage of cells that strictly follow
 * the designated sort order (either increasing or decreasing). For example, if
 * the array were completely sorted, the Sortedness Value would be 100%."</p>
 *
 * <p>Formula: (cells in correct final position / total cells) × 100</p>
 *
 * <p>Examples:
 * <ul>
 *   <li>[1, 2, 3, 4, 5] → 100.0 (all in correct position)</li>
 *   <li>[5, 4, 3, 2, 1] → 20.0 (only middle element "3" is correct for 5-element array)</li>
 *   <li>[2, 1, 3, 4, 5] → 60.0 (positions 2,3,4 are correct)</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of cell
 */
public class SortednessValue<T extends Cell<T>> implements Metric<T> {

    /**
     * Compute the sortedness value for the given cell array.
     *
     * <p>Creates a sorted reference array and counts how many cells
     * are already in their correct final position.</p>
     *
     * @param cells the array of cells to analyze
     * @return sortedness as a percentage (0.0 to 100.0)
     */
    @Override
    public double compute(T[] cells) {
        // Handle edge cases
        if (cells == null || cells.length == 0) {
            return 100.0; // Empty array is trivially sorted
        }
        if (cells.length == 1) {
            return 100.0; // Single element is trivially sorted
        }

        // Create sorted reference array
        @SuppressWarnings("unchecked")
        T[] sorted = (T[]) Arrays.copyOf(cells, cells.length);
        Arrays.sort(sorted);

        // Count cells in correct position
        int correctCount = 0;
        for (int i = 0; i < cells.length; i++) {
            if (cells[i].compareTo(sorted[i]) == 0) {
                correctCount++;
            }
        }

        return (correctCount * 100.0) / cells.length;
    }

    @Override
    public String getName() {
        return "Sortedness Value";
    }

    @Override
    public boolean isLowerBetter() {
        return false; // Higher sortedness is better (100% = fully sorted)
    }
}
