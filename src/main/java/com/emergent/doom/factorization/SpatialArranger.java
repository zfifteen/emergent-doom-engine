package com.emergent.doom.factorization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Utility for controlling the spatial distribution of strategies in the cell array.
 *
 * <p><strong>PURPOSE:</strong> Decouples candidate generation from spatial arrangement,
 * allowing specific aggregation patterns (like Zero Aggregation) to be applied
 * systematically to any cell population.</p>
 *
 * <p><strong>MODES:</strong></p>
 * <ul>
 *   <li><strong>RANDOM:</strong> Standard shuffle (Baseline C1)</li>
 *   <li><strong>CLUSTERED:</strong> Group by strategy (High Aggregation C2)</li>
 *   <li><strong>MAXIMAL_MIXING:</strong> Round-robin interleaving (Zero Aggregation C3)</li>
 * </ul>
 */
public class SpatialArranger {

    public enum LayoutMode {
        RANDOM,
        CLUSTERED,
        MAXIMAL_MIXING
    }

    /**
     * Re-arrange the given list of cells according to the specified mode.
     * Updates the 'position' field of each cell to match its new index.
     *
     * @param cells input list of cells (will be reordered)
     * @param mode desired layout mode
     * @param rand random source (for shuffling)
     * @return the reordered list (modified in-place or new list)
     */
    public static List<FactorCell> arrange(List<FactorCell> cells, LayoutMode mode, Random rand) {
        List<FactorCell> arranged;

        switch (mode) {
            case CLUSTERED:
                arranged = arrangeClustered(cells);
                break;
            case MAXIMAL_MIXING:
                arranged = arrangeMaximallyMixed(cells);
                break;
            case RANDOM:
            default:
                arranged = new ArrayList<>(cells);
                Collections.shuffle(arranged, rand);
                break;
        }

        // Update position fields to match new array order
        for (int i = 0; i < arranged.size(); i++) {
            arranged.get(i).updatePositionTo(i);
        }

        return arranged;
    }

    /**
     * Group cells by strategy to maximize aggregation.
     */
    private static List<FactorCell> arrangeClustered(List<FactorCell> cells) {
        List<FactorCell> result = new ArrayList<>(cells);
        // Sort by strategy ordinal/name to group them
        result.sort((c1, c2) -> c1.readAlgotype().compareTo(c2.readAlgotype()));
        return result;
    }

    /**
     * Interleave cells to minimize aggregation (Zero Aggregation / C3 style).
     *
     * <p><strong>ALGORITHM:</strong></p>
     * 1. Bucket cells by strategy.
     * 2. Round-robin through buckets, taking one cell from each until all are exhausted.
     * 3. This ensures that (size permitting) no two same-strategy cells are adjacent.
     */
    private static List<FactorCell> arrangeMaximallyMixed(List<FactorCell> cells) {
        // Bucket cells by strategy
        Map<FactorStrategy, List<FactorCell>> buckets = new HashMap<>();
        for (FactorCell cell : cells) {
            buckets.computeIfAbsent(cell.readAlgotype(), k -> new ArrayList<>()).add(cell);
        }

        List<FactorCell> result = new ArrayList<>(cells.size());
        List<FactorStrategy> strategies = new ArrayList<>(buckets.keySet());
        
        // Sort strategies to ensure deterministic interleaving order
        Collections.sort(strategies);

        boolean remaining = true;
        while (remaining) {
            remaining = false;
            for (FactorStrategy strategy : strategies) {
                List<FactorCell> bucket = buckets.get(strategy);
                if (!bucket.isEmpty()) {
                    result.add(bucket.remove(0));
                    remaining = true; // Still found something, keep looping
                }
            }
        }
        
        return result;
    }
}
