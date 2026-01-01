package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.experiment.SortDirection;
import com.emergent.doom.probe.StepSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
 * <p>Examples (INCREASING direction):
 * <ul>
 *   <li>[1, 2, 3, 4, 5] → 100.0 (all in correct position)</li>
 *   <li>[5, 4, 3, 2, 1] → 20.0 (only middle element "3" is correct for 5-element array)</li>
 *   <li>[2, 1, 3, 4, 5] → 60.0 (positions 2,3,4 are correct)</li>
 * </ul>
 * </p>
 *
 * <p>Examples (DECREASING direction):
 * <ul>
 *   <li>[5, 4, 3, 2, 1] → 100.0 (all in correct position for descending)</li>
 *   <li>[1, 2, 3, 4, 5] → 20.0 (only middle element "3" is correct for 5-element array)</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of cell
 */
public class SortednessValue<T extends Cell<T>> implements Metric<T> {

    private final SortDirection direction;

    /**
     * Create a SortednessValue metric with default INCREASING direction.
     */
    public SortednessValue() {
        this(SortDirection.INCREASING);
    }

    /**
     * Create a SortednessValue metric with the specified sort direction.
     *
     * @param direction the target sort direction (INCREASING or DECREASING)
     */
    public SortednessValue(SortDirection direction) {
        this.direction = direction != null ? direction : SortDirection.INCREASING;
    }

    /**
     * Get the sort direction used by this metric.
     *
     * @return the sort direction
     */
    public SortDirection getDirection() {
        return direction;
    }

    /**
     * Compute the sortedness value for the given cell array.
     *
     * <p>Creates a sorted reference array (in the configured direction)
     * and counts how many cells are already in their correct final position.</p>
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

        if (direction == SortDirection.DECREASING) {
            Arrays.sort(sorted, Comparator.reverseOrder());
        } else {
            Arrays.sort(sorted);
        }

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
    public double compute(StepSnapshot<T> snapshot) {
        List<Integer> values = snapshot.getValues();
        if (values == null || values.isEmpty()) {
            return 100.0;
        }
        if (values.size() == 1) {
            return 100.0;
        }

        List<Integer> sorted = new ArrayList<>(values);
        if (direction == SortDirection.DECREASING) {
            sorted.sort(Collections.reverseOrder());
        } else {
            Collections.sort(sorted);
        }

        int correctCount = 0;
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equals(sorted.get(i))) {
                correctCount++;
            }
        }

        return (correctCount * 100.0) / values.size();
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
