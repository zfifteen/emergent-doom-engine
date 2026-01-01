package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.StepSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Measures the Spearman footrule distance from a sorted array.
 *
 * <p>From Levin et al. (2024), p.8, and REQUIREMENTS.md Section 7.4:
 * "Spearman Distance: Σ |actual_position - expected_position| for all cells"</p>
 *
 * <p>This matches the reference implementation in cell_research/analysis/utils.py:
 * <pre>
 * def get_spearman_distance(arr):
 *     res = 0
 *     for i in range(len(arr)):
 *         res += abs(arr[i] - i)
 *     return res
 * </pre>
 * </p>
 *
 * <p><strong>Note:</strong> The Python implementation assumes arr[i] contains the
 * expected position (0-indexed rank) of the element at position i. In our implementation,
 * we compute ranks based on sorted order of the cell values.</p>
 *
 * <p>Examples (for values [3, 1, 2]):
 * <ul>
 *   <li>Sorted would be [1, 2, 3], so expected positions are: 1→0, 2→1, 3→2</li>
 *   <li>Actual: 3 at pos 0 (expected 2), 1 at pos 1 (expected 0), 2 at pos 2 (expected 1)</li>
 *   <li>Distance: |0-2| + |1-0| + |2-1| = 2 + 1 + 1 = 4</li>
 * </ul>
 * </p>
 *
 * <p>A perfectly sorted array has Spearman distance 0.</p>
 *
 * @param <T> the type of cell
 */
public class SpearmanDistance<T extends Cell<T>> implements Metric<T> {

    /**
     * Compute the Spearman footrule distance for the given cell array.
     *
     * <p>For each cell, computes the absolute difference between its current
     * position and its expected position in a sorted array.</p>
     *
     * @param cells the array of cells to analyze
     * @return total Spearman distance (0 for sorted array, higher for unsorted)
     */
    @Override
    public double compute(T[] cells) {
        // Handle edge cases
        if (cells == null || cells.length == 0) {
            return 0.0; // Empty array has zero distance
        }
        if (cells.length == 1) {
            return 0.0; // Single element is at its correct position
        }

        // Create array of (value, original_position) pairs and sort by value
        @SuppressWarnings("unchecked")
        IndexedCell<T>[] indexed = new IndexedCell[cells.length];
        for (int i = 0; i < cells.length; i++) {
            indexed[i] = new IndexedCell<>(cells[i], i);
        }

        // Sort by cell value to determine expected positions
        Arrays.sort(indexed, (a, b) -> a.cell.compareTo(b.cell));

        // Compute Spearman distance: sum of |actual_position - expected_position|
        double distance = 0.0;
        for (int expectedPos = 0; expectedPos < indexed.length; expectedPos++) {
            int actualPos = indexed[expectedPos].originalIndex;
            distance += Math.abs(actualPos - expectedPos);
        }

        return distance;
    }

    @Override
    public double compute(StepSnapshot<T> snapshot) {
        List<Integer> values = snapshot.getValues();
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        if (values.size() == 1) {
            return 0.0;
        }

        List<IndexedValue> indexed = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            indexed.add(new IndexedValue(values.get(i), i));
        }

        indexed.sort(Comparator.comparingInt(a -> a.value));

        double distance = 0.0;
        for (int expectedPos = 0; expectedPos < indexed.size(); expectedPos++) {
            int actualPos = indexed.get(expectedPos).originalIndex;
            distance += Math.abs(actualPos - expectedPos);
        }
        return distance;
    }

    @Override
    public String getName() {
        return "Spearman Distance";
    }

    @Override
    public boolean isLowerBetter() {
        return true; // Lower distance is better (0 = perfectly sorted)
    }

    /**
     * Helper class to track original position of cells during sorting.
     */
    private static class IndexedCell<T extends Cell<T>> {
        final T cell;
        final int originalIndex;

        IndexedCell(T cell, int originalIndex) {
            this.cell = cell;
            this.originalIndex = originalIndex;
        }
    }

    private static class IndexedValue {
        final int value;
        final int originalIndex;

        IndexedValue(int value, int originalIndex) {
            this.value = value;
            this.originalIndex = originalIndex;
        }
    }
}
