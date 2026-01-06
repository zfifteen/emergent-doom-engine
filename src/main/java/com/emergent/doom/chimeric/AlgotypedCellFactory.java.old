package com.emergent.doom.chimeric;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.AlgotypedCell;

import java.util.*;

/**
 * Factory for creating shuffled AlgotypedCell arrays with specified algotype distribution.
 *
 * <p><strong>PURPOSE:</strong> Generate cell arrays where each cell carries its algotype
 * as an intrinsic property (Levin-style architecture), in contrast to position-based
 * algotype assignment via PercentageAlgotypeProvider (EDE default architecture).</p>
 *
 * <p><strong>USAGE:</strong></p>
 * <pre>{@code
 * // Create 50/50 Bubble/Selection mix
 * Map<Algotype, Double> mix = Map.of(
 *     Algotype.BUBBLE, 0.5,
 *     Algotype.SELECTION, 0.5
 * );
 * 
 * AlgotypedCellFactory factory = new AlgotypedCellFactory(100, mix, 42L);
 * AlgotypedCell[] cells = factory.createShuffledArray();
 * 
 * // Each cell knows its own algotype
 * System.out.println(cells[0].getAlgotype());  // e.g., BUBBLE
 * System.out.println(cells[1].getAlgotype());  // e.g., SELECTION
 * 
 * // After swapping, algotypes move WITH cells
 * // (contrast with GenericCell where algotypes stay at positions)
 * }</pre>
 */
public class AlgotypedCellFactory {

    private final int arraySize;
    private final Map<Algotype, Double> distribution;
    private final long seed;

    /**
     * Create a factory for AlgotypedCell arrays.
     *
     * <p><strong>PURPOSE:</strong> Configure factory with array size, algotype distribution,
     * and random seed for reproducible cell generation.</p>
     *
     * @param arraySize number of cells to generate
     * @param distribution map of algotype to percentage (must sum to 1.0)
     * @param seed random seed for reproducibility
     * @throws IllegalArgumentException if arraySize <= 0, distribution is null/empty,
     *                                  or percentages don't sum to ~1.0
     */
    public AlgotypedCellFactory(int arraySize, Map<Algotype, Double> distribution, long seed) {
        if (arraySize <= 0) {
            throw new IllegalArgumentException("Array size must be positive");
        }
        if (distribution == null || distribution.isEmpty()) {
            throw new IllegalArgumentException("Distribution cannot be null or empty");
        }

        // Validate percentages sum to approximately 1.0
        double sum = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 1.0) > 0.01) {
            throw new IllegalArgumentException("Distribution percentages must sum to 1.0, got: " + sum);
        }

        this.arraySize = arraySize;
        this.distribution = new LinkedHashMap<>(distribution);
        this.seed = seed;
    }

    /**
     * Create a shuffled array of AlgotypedCells with random values.
     *
     * <p><strong>PURPOSE:</strong> Generate a cell array ready for sorting experiments
     * where algotypes are embedded in cell objects (Levin-style architecture).</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Generate shuffled value sequence (0 to arraySize-1)</li>
     *   <li>Generate algotype assignments according to distribution</li>
     *   <li>Shuffle algotype assignments</li>
     *   <li>Create AlgotypedCell objects pairing values with algotypes</li>
     *   <li>Shuffle final array for random initial state</li>
     * </ol>
     *
     * <p><strong>KEY PROPERTY:</strong> Each cell has a unique value (0 to arraySize-1)
     * and an algotype selected from the distribution. When sorted, cells will end up
     * in value order (0, 1, 2, ..., arraySize-1), but algotypes will be spatially
     * distributed according to sorting dynamics (not frozen at positions).</p>
     *
     * @return shuffled array of AlgotypedCells with random initial ordering
     */
    public AlgotypedCell[] createShuffledArray() {
        Random random = new Random(seed);

        // Step 1: Create sequential values (will be shuffled later)
        List<Integer> values = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            values.add(i);
        }

        // Step 2: Create algotype assignments according to distribution
        List<Algotype> algotypes = new ArrayList<>(arraySize);
        int remaining = arraySize;

        // Sort entries by algotype name for deterministic ordering
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
                algotypes.add(entry.getKey());
            }
        }

        // Step 3: Shuffle algotypes for random distribution
        Collections.shuffle(algotypes, random);

        // Step 4: Create AlgotypedCell objects pairing values with algotypes
        AlgotypedCell[] cells = new AlgotypedCell[arraySize];
        for (int i = 0; i < arraySize; i++) {
            cells[i] = new AlgotypedCell(values.get(i), algotypes.get(i));
        }

        // Step 5: Shuffle final array for random initial state
        // (This mimics random.shuffle(sorting_list) from Levin implementation)
        List<AlgotypedCell> cellList = Arrays.asList(cells);
        Collections.shuffle(cellList, random);

        return cellList.toArray(new AlgotypedCell[0]);
    }

    /**
     * Create multiple shuffled arrays with different seeds.
     *
     * <p><strong>PURPOSE:</strong> Generate multiple trial arrays for statistical experiments,
     * each with a different random configuration but same algotype distribution.</p>
     *
     * @param count number of arrays to generate
     * @return list of shuffled arrays, each with different random seed
     */
    public List<AlgotypedCell[]> createMultipleArrays(int count) {
        List<AlgotypedCell[]> arrays = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            // Use base seed + trial number for deterministic but varied results
            AlgotypedCellFactory factory = new AlgotypedCellFactory(
                arraySize, distribution, seed + i
            );
            arrays.add(factory.createShuffledArray());
        }
        return arrays;
    }

    /**
     * Get the configured array size.
     *
     * @return number of cells per array
     */
    public int getArraySize() {
        return arraySize;
    }

    /**
     * Get the configured algotype distribution.
     *
     * @return unmodifiable map of algotype to percentage
     */
    public Map<Algotype, Double> getDistribution() {
        return Collections.unmodifiableMap(distribution);
    }

    /**
     * Get the configured seed.
     *
     * @return random seed
     */
    public long getSeed() {
        return seed;
    }
}
