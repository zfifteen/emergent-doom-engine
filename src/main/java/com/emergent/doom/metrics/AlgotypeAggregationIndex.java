package com.emergent.doom.metrics;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.StepSnapshot;

import java.util.List;

/**
 * Measures spatial clustering of cells by algotype in chimeric populations.
 *
 * <p>From Levin et al. (2024), p.8-9 and REQUIREMENTS.md §7.6:
 * "In sorting experiments with mixed Algotypes, we measured the extent to which cells
 * of the same Algotype aggregated together (spatially) within the array. We defined
 * Aggregation Value as the percentage of cells with directly adjacent neighboring
 * cells that were all the same Algotype."</p>
 *
 * <p>Formula: (cells with at least one same-type neighbor / total cells) × 100</p>
 *
 * <p>This matches the cell_research Python implementation:
 * <pre>{@code
 * def get_aggregation_value(cells):
 *     same_type_count = 0
 *     for i in range(len(cells)):
 *         has_left_same = (i > 0 and cells[i-1].algotype == cells[i].algotype)
 *         has_right_same = (i < len(cells)-1 and cells[i+1].algotype == cells[i].algotype)
 *         if has_left_same or has_right_same:
 *             same_type_count += 1
 *     return (same_type_count / len(cells)) * 100
 * }</pre></p>
 *
 * <p>For a random 50/50 mix of two algotypes, expected baseline is ~75%.
 * (Each cell has ~75% chance of having at least one matching neighbor.)</p>
 *
 * <p>Examples (B=Bubble, S=Selection):
 * <ul>
 *   <li>[B, B, B, S, S, S] → 100% (all cells have at least one same-type neighbor)</li>
 *   <li>[B, S, B, S, B, S] → 0% (no cell has a same-type neighbor)</li>
 *   <li>[B, B, B, B, B, B] → 100% (all same type)</li>
 *   <li>[B, S, S, S, B, B] → 83.3% (5/6 cells have same-type neighbor)</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of cell
 */
public class AlgotypeAggregationIndex<T extends Cell<T>> implements Metric<T> {

    /**
     * Compute the aggregation index for the given cell array.
     *
     * <p>Counts cells that have at least one adjacent neighbor of the same algotype
     * and returns as a percentage of total cells.</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Handle edge cases (null, empty, single cell)</li>
     *   <li>For each cell, check left and right neighbors</li>
     *   <li>If either neighbor has same algotype, count the cell</li>
     *   <li>Return (count / total) × 100</li>
     * </ol>
     * </p>
     *
     * @param cells the array of cells to analyze
     * @return aggregation as a percentage (0.0 to 100.0)
     */
    @Override
    public double compute(T[] cells) {
        // Handle edge cases
        if (cells == null || cells.length == 0) {
            return 100.0; // Empty is trivially "aggregated"
        }
        if (cells.length == 1) {
            return 100.0; // Single cell has no neighbors to compare
        }

        int sameTypeNeighborCount = 0;

        for (int i = 0; i < cells.length; i++) {
            // Cast to HasAlgotype for legacy support during Phase 2 migration
            if (!(cells[i] instanceof com.emergent.doom.cell.HasAlgotype)) {
                continue; // Skip cells without algotype
            }
            Algotype current = ((com.emergent.doom.cell.HasAlgotype) cells[i]).getAlgotype();

            // Check left neighbor
            boolean hasLeftSame = (i > 0) 
                && (cells[i - 1] instanceof com.emergent.doom.cell.HasAlgotype)
                && (((com.emergent.doom.cell.HasAlgotype) cells[i - 1]).getAlgotype() == current);

            // Check right neighbor
            boolean hasRightSame = (i < cells.length - 1) 
                && (cells[i + 1] instanceof com.emergent.doom.cell.HasAlgotype)
                && (((com.emergent.doom.cell.HasAlgotype) cells[i + 1]).getAlgotype() == current);

            // Cell is "aggregated" if it has at least one same-type neighbor
            if (hasLeftSame || hasRightSame) {
                sameTypeNeighborCount++;
            }
        }

        return (sameTypeNeighborCount * 100.0) / cells.length;
    }

    @Override
    public double compute(StepSnapshot<T> snapshot) {
        List<Object[]> types = snapshot.getTypes();
        if (types == null || types.isEmpty()) {
            return 100.0;
        }
        if (types.size() == 1) {
            return 100.0;
        }

        int sameTypeNeighborCount = 0;
        for (int i = 0; i < types.size(); i++) {
            // types[i] = [groupId, algotypeLabel, value, isFrozen]
            int currentLabel = (Integer) types.get(i)[1];

            boolean hasLeftSame = (i > 0) && (((Integer) types.get(i - 1)[1]) == currentLabel);
            boolean hasRightSame = (i < types.size() - 1) && (((Integer) types.get(i + 1)[1]) == currentLabel);

            if (hasLeftSame || hasRightSame) {
                sameTypeNeighborCount++;
            }
        }
        return (sameTypeNeighborCount * 100.0) / types.size();
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
