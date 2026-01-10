package com.emergent.doom.metrics;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.StepSnapshot;

import java.util.List;

/**
 * Measures spatial clustering of cells by ALGOTYPE (strategy label) in chimeric populations.
 *
 * <p><strong>SEMANTIC CLARIFICATION:</strong> This metric measures STRATEGY-LABEL adjacency,
 * NOT fitness-field clustering. Use {@link FitnessSimilarityClusteringIndex} for
 * fitness-based clustering analysis to avoid circular reasoning in experimental design.</p>
 *
 * <p><strong>USE CASE:</strong> Chimeric algotype studies (Levin §8) where the goal
 * is to measure emergent aggregation of cells with the SAME ALGORITHM (algotype),
 * independent of their fitness values. This is the original Levin metric for
 * studying algorithmic "personality" clustering.</p>
 *
 * <p><strong>NOT FOR:</strong> Pattern formation or localization experiments where
 * "clustering" refers to fitness-field structure. Using this metric to both define
 * experimental conditions AND measure outcomes creates circular reasoning (begging
 * the question).</p>
 *
 * <p><strong>REFERENCE:</strong> From Levin et al. (2024), p.8-9 and REQUIREMENTS.md §7.6:
 * "In sorting experiments with mixed Algotypes, we measured the extent to which cells
 * of the same Algotype aggregated together (spatially) within the array. We defined
 * Aggregation Value as the percentage of cells with directly adjacent neighboring
 * cells that were all the same Algotype."</p>
 *
 * <p><strong>FORMULA:</strong> (cells with at least one same-algotype neighbor / total cells) × 100</p>
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
 * <p><strong>EXAMPLES</strong> (B=Bubble, S=Selection):
 * <ul>
 *   <li>[B, B, B, S, S, S] → 100% (all cells have at least one same-type neighbor)</li>
 *   <li>[B, S, B, S, B, S] → 0% (no cell has a same-type neighbor)</li>
 *   <li>[B, B, B, B, B, B] → 100% (all same type)</li>
 *   <li>[B, S, S, S, B, B] → 83.3% (5/6 cells have same-type neighbor)</li>
 * </ul>
 * </p>
 *
 * <p><strong>COMPARISON WITH FITNESS CLUSTERING:</strong></p>
 * <table border="1">
 * <tr><th>Metric</th><th>Measures</th><th>Use Case</th></tr>
 * <tr><td>AlgotypeAggregationIndex</td><td>Strategy-label adjacency</td><td>Chimeric algotype studies</td></tr>
 * <tr><td>FitnessSimilarityClusteringIndex</td><td>Fitness-field clustering</td><td>Pattern formation, localization</td></tr>
 * </table>
 *
 * @param <T> the type of cell
 * @see FitnessSimilarityClusteringIndex for fitness-based clustering metric
 */
public class AlgotypeAggregationIndex<T extends Cell<T>> implements Metric<T> {

    /**
     * Compute the aggregation index for the given cell array.
     *
     * <p><strong>DEPRECATED:</strong> This method requires cells to implement HasAlgotype,
     * which has been removed. Use {@link #compute(StepSnapshot)} instead, which works
     * with snapshot data from Probe recordings.</p>
     *
     * <p>Counts cells that have at least one adjacent neighbor of the same algotype
     * and returns as a percentage of total cells.</p>
     *
     * @param cells the array of cells to analyze
     * @return aggregation as a percentage (0.0 to 100.0)
     * @throws UnsupportedOperationException always thrown - use compute(StepSnapshot) instead
     * @deprecated Cells no longer implement HasAlgotype. Use compute(StepSnapshot) instead.
     */
    @Override
    @Deprecated
    public double compute(T[] cells) {
        throw new UnsupportedOperationException(
            "AlgotypeAggregationIndex.compute(T[] cells) is no longer supported. " +
            "Cells do not implement HasAlgotype interface. " +
            "Use compute(StepSnapshot) instead, which works with probe snapshot data."
        );
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
