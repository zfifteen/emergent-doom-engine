package com.emergent.doom.chimeric;

import com.emergent.doom.cell.Algotype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Assigns algotypes based on percentage distribution with random shuffling.
 *
 * <p>Creates a shuffled assignment list based on specified percentages,
 * ensuring exact proportions while randomizing positions.</p>
 *
 * <p>From Levin et al. (2024), experiments used:
 * <ul>
 *   <li>50/50 Bubble/Selection</li>
 *   <li>50/50 Bubble/Insertion</li>
 *   <li>33/33/34 three-way mix</li>
 * </ul></p>
 *
 * <p>Usage:
 * <pre>{@code
 * // 50/50 Bubble/Selection mix
 * Map<Algotype, Double> mix = Map.of(
 *     Algotype.BUBBLE, 0.5,
 *     Algotype.SELECTION, 0.5
 * );
 * AlgotypeProvider provider = new PercentageAlgotypeProvider(mix, 100, seed);
 * }</pre></p>
 */
public class PercentageAlgotypeProvider implements AlgotypeProvider {

    private final List<String> assignments;

    /**
     * Create a percentage-based algotype provider.
     *
     * @param distribution map of algotype to percentage (0.0 to 1.0)
     * @param arraySize the total number of cells
     * @param seed random seed for reproducibility
     * @throws IllegalArgumentException if percentages don't sum to ~1.0
     */
    public PercentageAlgotypeProvider(Map<Algotype, Double> distribution, int arraySize, long seed) {
        if (distribution == null || distribution.isEmpty()) {
            throw new IllegalArgumentException("Distribution cannot be null or empty");
        }
        if (arraySize <= 0) {
            throw new IllegalArgumentException("Array size must be positive");
        }

        // Validate percentages sum to approximately 1.0
        double sum = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 1.0) > 0.01) {
            throw new IllegalArgumentException("Distribution percentages must sum to 1.0, got: " + sum);
        }

        // Build assignment list
        assignments = new ArrayList<>(arraySize);
        int remaining = arraySize;

        // Sort entries by algotype name for deterministic ordering regardless of Map implementation
        List<Map.Entry<Algotype, Double>> entries = new ArrayList<>(distribution.entrySet());
        entries.sort(Comparator.comparing(e -> e.getKey().name()));

        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Algotype, Double> entry = entries.get(i);
            int count;
            if (i == entries.size() - 1) {
                // Last algotype gets remaining to avoid rounding errors
                count = remaining;
            } else {
                count = (int) Math.round(entry.getValue() * arraySize);
                remaining -= count;
            }

            for (int j = 0; j < count; j++) {
                assignments.add(entry.getKey().name());
            }
        }

        // Shuffle for random distribution
        Collections.shuffle(assignments, new Random(seed));
    }

    /**
     * Create a percentage-based algotype provider with random seed.
     *
     * @param distribution map of algotype to percentage (0.0 to 1.0)
     * @param arraySize the total number of cells
     */
    public PercentageAlgotypeProvider(Map<Algotype, Double> distribution, int arraySize) {
        this(distribution, arraySize, System.nanoTime());
    }

    @Override
    public String getAlgotype(int position, int arraySize) {
        if (position < 0 || position >= assignments.size()) {
            throw new IndexOutOfBoundsException("Position " + position + " out of bounds for size " + assignments.size());
        }
        return assignments.get(position);
    }

    /**
     * Get the actual count of a specific algotype in this distribution.
     *
     * @param algotype the algotype to count
     * @return number of positions assigned to this algotype
     */
    public int getCount(Algotype algotype) {
        String name = algotype.name();
        return (int) assignments.stream().filter(a -> a.equals(name)).count();
    }

    /**
     * Get the total size of this distribution.
     *
     * @return total number of positions
     */
    public int size() {
        return assignments.size();
    }
}
