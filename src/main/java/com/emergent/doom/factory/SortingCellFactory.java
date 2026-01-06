package com.emergent.doom.factory;

import com.emergent.doom.cell.*;

import java.util.*;

/**
 * Factory for creating sorting cells with embedded algotypes (Levin-aligned semantics).
 *
 * <p><strong>PURPOSE:</strong> Generate arrays of sorting cells where algotype is an intrinsic
 * property that travels with the cell during swaps. Replaces the old PercentageAlgotypeProvider
 * approach which bound algotypes to positions.</p>
 *
 * <p><strong>KEY DIFFERENCE FROM OLD ARCHITECTURE:</strong></p>
 * <ul>
 *   <li>OLD: Algotypes bound to array indices, cells carry only values</li>
 *   <li>NEW: Algotypes embedded in cell objects, travel with cells during swaps</li>
 * </ul>
 *
 * <p><strong>LEVIN ALIGNMENT:</strong> This enables genuine morphogenetic clustering where
 * same-algotype cells physically congregate through collective movement.</p>
 */
public class SortingCellFactory {
    
    private final Random random;
    
    /**
     * Create factory with seeded random for reproducibility.
     *
     * @param seed random seed
     */
    public SortingCellFactory(long seed) {
        this.random = new Random(seed);
    }
    
    /**
     * Create factory with unseeded random.
     */
    public SortingCellFactory() {
        this.random = new Random();
    }
    
    /**
     * Create array of sorting cells with percentage-based algotype distribution.
     *
     * <p><strong>PURPOSE:</strong> Generate cells where algotypes are distributed according
     * to specified percentages, but bound to cells (not positions).</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>distribution - Map of algotype to percentage (0.0 to 1.0, must sum to ~1.0)</li>
     *   <li>values - Array of values to assign to cells (determines array size)</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate distribution sums to ~1.0</li>
     *   <li>Calculate count for each algotype based on percentage</li>
     *   <li>Create list of algotype assignments</li>
     *   <li>Shuffle algotype assignments randomly</li>
     *   <li>Create cells with values and shuffled algotypes</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Array of AbstractSortingCell with embedded algotypes</p>
     *
     * <p><strong>CRITICAL:</strong> Algotypes are assigned to cells ONCE and never change.
     * When cells swap, the algotypes relocate WITH the cells.</p>
     *
     * @param distribution map of algotype to percentage
     * @param values array of integer values for cells
     * @return array of sorting cells with embedded algotypes
     * @throws IllegalArgumentException if percentages don't sum to ~1.0
     */
    public List<AbstractSortingCell> createCells(Map<SortingAlgotype, Double> distribution, int[] values) {
        int arraySize = values.length;
        
        // Validate percentages sum to approximately 1.0
        double sum = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 1.0) > 0.01) {
            throw new IllegalArgumentException("Distribution percentages must sum to 1.0, got: " + sum);
        }
        
        // Build algotype assignment list
        List<SortingAlgotype> algotypes = new ArrayList<>(arraySize);
        int remaining = arraySize;
        
        // Sort entries by algotype name for deterministic ordering
        List<Map.Entry<SortingAlgotype, Double>> entries = new ArrayList<>(distribution.entrySet());
        entries.sort(Comparator.comparing(e -> e.getKey().name()));
        
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<SortingAlgotype, Double> entry = entries.get(i);
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
        
        // Shuffle algotype assignments for random spatial distribution
        Collections.shuffle(algotypes, random);
        
        // Create cells with values and shuffled algotypes
        List<AbstractSortingCell> cells = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            SortingAlgotype algotype = algotypes.get(i);
            int value = values[i];
            
            AbstractSortingCell cell = createCellForAlgotype(algotype, value, i);
            cells.add(cell);
        }
        
        return cells;
    }
    
    /**
     * Create a single cell with specified algotype, value, and position.
     *
     * <p><strong>PURPOSE:</strong> Factory method to create appropriate concrete cell
     * type based on algotype.</p>
     *
     * @param algotype the sorting algotype
     * @param value the cell value
     * @param position the initial position
     * @return concrete cell instance
     */
    private AbstractSortingCell createCellForAlgotype(SortingAlgotype algotype, int value, int position) {
        switch (algotype) {
            case BUBBLE:
                return new BubbleSortingCell(value, position, random);
            case SELECTION:
                return new SelectionSortingCell(value, position, 0);  // Ascending sort
            case INSERTION:
                return new InsertionSortingCell(value, position);
            case FIBONACCI:
                // For now, use BUBBLE behavior for FIBONACCI
                // (Full FIBONACCI implementation would require additional cell type)
                return new BubbleSortingCell(value, position, random);
            default:
                throw new IllegalArgumentException("Unknown algotype: " + algotype);
        }
    }
    
    /**
     * Create array of cells with random values and specified algotype distribution.
     *
     * <p><strong>PURPOSE:</strong> Convenience method that generates both random values
     * and algotype distribution.</p>
     *
     * @param distribution algotype distribution
     * @param size number of cells to create
     * @param maxValue maximum value for random generation
     * @return list of sorting cells
     */
    public List<AbstractSortingCell> createRandomCells(
            Map<SortingAlgotype, Double> distribution, 
            int size, 
            int maxValue) {
        
        int[] values = new int[size];
        for (int i = 0; i < size; i++) {
            values[i] = random.nextInt(maxValue);
        }
        
        return createCells(distribution, values);
    }
}
