package com.emergent.doom.metrics;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;

/**
 * Measures spatial clustering of cells by algotype in chimeric populations.
 *
 * <p>From Levin et al. (2024), p.8-9:
 * "In sorting experiments with mixed Algotypes, we measured the extent to which cells
 * of the same Algotype aggregated together (spatially) within the array. We defined
 * Aggregation Value as the percentage of cells with directly adjacent neighboring
 * cells that were all the same Algotype."</p>
 *
 * <p>Formula: (matching adjacent pairs / total adjacent pairs) × 100</p>
 *
 * <p>For a random 50/50 mix of two algotypes, expected baseline is ~50%.
 * Values above baseline indicate clustering; below indicates repulsion.</p>
 *
 * <p>Examples (B=Bubble, S=Selection):
 * <ul>
    *   <li>[B, B, B, S, S, S] → 80.0% (4/5 pairs match: BB, BB, BS, SS, SS)</li>
 *   <li>[B, S, B, S, B, S] → 0% (0/5 pairs match: alternating)</li>
 *   <li>[B, B, B, B, B, B] → 100% (5/5 pairs match: homogeneous)</li>
 *   <li>[B, S, S, S, B, B] → 60% (3/5 pairs match)</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of cell
 */
public class AlgotypeAggregationIndex<T extends Cell<T>> implements Metric<T> {

    /**
     * Compute the aggregation index for the given cell array.
     *
     * <p>Counts adjacent pairs where both cells have the same algotype
     * and returns as a percentage of total adjacent pairs.</p>
     *
     * @param cells the array of cells to analyze
     * @return aggregation as a percentage (0.0 to 100.0)
     */
    @Override
    public double compute(T[] cells) {
        // Handle edge cases
        if (cells == null || cells.length < 2) {
            return 100.0; // Single cell or empty is trivially "aggregated"
        }

        int matchingPairs = 0;
        int totalPairs = cells.length - 1;

        for (int i = 0; i < totalPairs; i++) {
            Algotype current = cells[i].getAlgotype();
            Algotype next = cells[i + 1].getAlgotype();
            if (current == next) {
                matchingPairs++;
            }
        }

        return (matchingPairs * 100.0) / totalPairs;
    }

    @Override
    public String getName() {
        return "Algotype Aggregation Index";
    }

    @Override
    public boolean isLowerBetter() {
        return false; // Higher aggregation indicates more clustering
    }
}
