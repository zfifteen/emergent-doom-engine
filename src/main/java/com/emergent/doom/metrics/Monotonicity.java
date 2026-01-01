package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.StepSnapshot;

import java.util.List;

/**
 * Measures the percentage of cells that follow monotonic (sorted) order.
 *
 * <p>From Levin et al. (2024), p.8: "Monotonicity is the measurement of how
 * well the cells followed monotonic order."</p>
 *
 * <p>Formula: (cells in correct relative order with predecessor / total cells) × 100</p>
 *
 * <p>This matches the reference implementation in cell_research/analysis/utils.py:
 * <pre>
 * def get_monotonicity(arr):
 *     monotonicity_value = 1  # Start counting from first element
 *     prev = arr[0]
 *     for i in range(1, len(arr)):
 *         if arr[i] >= prev:
 *             monotonicity_value += 1
 *         prev = arr[i]
 *     return (monotonicity_value / len(arr)) * 100
 * </pre>
 * </p>
 *
 * <p>Examples:
 * <ul>
 *   <li>[1, 2, 3, 4, 5] → 100.0% (all in order: 1 + 4 = 5/5)</li>
 *   <li>[5, 4, 3, 2, 1] → 20.0% (only first element counts: 1/5)</li>
 *   <li>[1, 3, 2, 4, 5] → 80.0% (1, 3→X, 2, 4, 5: 4/5)</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of cell
 * @see MonotonicityError
 */
public class Monotonicity<T extends Cell<T>> implements Metric<T> {

    /**
     * Compute the monotonicity percentage for the given cell array.
     *
     * <p>Counts how many cells are greater than or equal to their predecessor,
     * starting with 1 for the first cell (which has no predecessor to violate).</p>
     *
     * @param cells the array of cells to analyze
     * @return monotonicity as a percentage (0.0 to 100.0)
     */
    @Override
    public double compute(T[] cells) {
        // Handle edge cases
        if (cells == null || cells.length == 0) {
            return 100.0; // Empty array is trivially monotonic
        }
        if (cells.length == 1) {
            return 100.0; // Single element is trivially monotonic
        }

        // Start with 1 for the first element (matches Python implementation)
        int monotonicityCount = 1;
        T prev = cells[0];

        for (int i = 1; i < cells.length; i++) {
            // Cell is in monotonic order if >= predecessor
            if (cells[i].compareTo(prev) >= 0) {
                monotonicityCount++;
            }
            prev = cells[i];
        }

        return (monotonicityCount * 100.0) / cells.length;
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

        int monotonicityCount = 1;
        Integer prev = values.get(0);

        for (int i = 1; i < values.size(); i++) {
            Integer curr = values.get(i);
            if (curr.compareTo(prev) >= 0) {
                monotonicityCount++;
            }
            prev = curr;
        }

        return (monotonicityCount * 100.0) / values.size();
    }

    @Override
    public String getName() {
        return "Monotonicity";
    }

    @Override
    public boolean isLowerBetter() {
        return false; // Higher monotonicity is better (100% = fully monotonic)
    }
}
