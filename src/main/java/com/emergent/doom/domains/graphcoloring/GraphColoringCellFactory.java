package com.emergent.doom.domains.graphcoloring;

import java.util.*;

/**
 * Factory for creating graph coloring cells with chimeric algotype distributions.
 * 
 * <p><strong>PURPOSE:</strong> Generate populations of GraphColoringCell instances
 * with specified algotype distributions for H1 validation experiments.</p>
 * 
 * <p><strong>ARCHITECTURE:</strong> Follows EDE factory pattern - algotypes are
 * embedded in cells and travel with them during swaps.</p>
 */
public class GraphColoringCellFactory {
    
    private final Random random;
    
    /**
     * Create factory with seeded random for reproducibility.
     * 
     * @param seed random seed
     */
    public GraphColoringCellFactory(long seed) {
        this.random = new Random(seed);
    }
    
    /**
     * Create factory with unseeded random.
     */
    public GraphColoringCellFactory() {
        this.random = new Random();
    }
    
    /**
     * Create array of cells with percentage-based algotype distribution.
     * 
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate distribution sums to ~1.0</li>
     *   <li>Generate random initial colorings for each cell</li>
     *   <li>Calculate count for each algotype based on percentage</li>
     *   <li>Create list of algotype assignments and shuffle</li>
     *   <li>Create cells with shuffled algotypes</li>
     * </ol>
     * 
     * @param distribution map of algotype to percentage (0.0 to 1.0, must sum to ~1.0)
     * @param graph graph instance for coloring
     * @param populationSize number of cells to create
     * @return list of GraphColoringCell with embedded algotypes
     */
    public List<GraphColoringCell> createCells(
            Map<ColoringAlgotype, Double> distribution,
            GraphInstance graph,
            int populationSize) {
        
        if (distribution == null || distribution.isEmpty()) {
            throw new IllegalArgumentException("Distribution cannot be null or empty");
        }
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (populationSize <= 0) {
            throw new IllegalArgumentException("Population size must be positive");
        }
        
        // Validate percentages sum to approximately 1.0
        double sum = distribution.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sum - 1.0) > 0.01) {
            throw new IllegalArgumentException("Distribution percentages must sum to 1.0, got: " + sum);
        }
        
        // Build algotype assignment list
        List<ColoringAlgotype> algotypes = new ArrayList<>(populationSize);
        int remaining = populationSize;
        
        // Sort entries by algotype name for deterministic ordering
        List<Map.Entry<ColoringAlgotype, Double>> entries = new ArrayList<>(distribution.entrySet());
        entries.sort(Comparator.comparing(e -> e.getKey().name()));
        
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<ColoringAlgotype, Double> entry = entries.get(i);
            int count;
            if (i == entries.size() - 1) {
                // Last algotype gets remaining to avoid rounding errors
                count = remaining;
            } else {
                count = (int) Math.round(entry.getValue() * populationSize);
                remaining -= count;
            }
            
            for (int j = 0; j < count; j++) {
                algotypes.add(entry.getKey());
            }
        }
        
        // Shuffle algotype assignments for random spatial distribution
        Collections.shuffle(algotypes, random);
        
        // Create cells with random initial colorings and shuffled algotypes
        List<GraphColoringCell> cells = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            ColoringAlgotype algotype = algotypes.get(i);
            
            // Generate random initial coloring
            int[] colors = new int[graph.getNumVertices()];
            for (int v = 0; v < colors.length; v++) {
                colors[v] = random.nextInt(3);  // Random color in {0, 1, 2}
            }
            
            ColoringState state = new ColoringState(colors, graph);
            GraphColoringCell cell = new GraphColoringCell(state, algotype, graph, i, random);
            cells.add(cell);
        }
        
        return cells;
    }
    
    /**
     * Create uniform algotype distribution (all cells same algotype).
     * 
     * <p>Used for negative control experiments.</p>
     * 
     * @param algotype single algotype for all cells
     * @param graph graph instance
     * @param populationSize number of cells
     * @return list of cells with uniform algotype
     */
    public List<GraphColoringCell> createUniformCells(
            ColoringAlgotype algotype,
            GraphInstance graph,
            int populationSize) {
        
        Map<ColoringAlgotype, Double> distribution = new HashMap<>();
        distribution.put(algotype, 1.0);
        return createCells(distribution, graph, populationSize);
    }
    
    /**
     * Create equal distribution across all algotypes.
     * 
     * <p>Each algotype gets equal percentage (25% for 4 algotypes).</p>
     * 
     * @param graph graph instance
     * @param populationSize number of cells
     * @return list of cells with equal algotype distribution
     */
    public List<GraphColoringCell> createEqualDistributionCells(
            GraphInstance graph,
            int populationSize) {
        
        ColoringAlgotype[] allTypes = ColoringAlgotype.values();
        double percentage = 1.0 / allTypes.length;
        
        Map<ColoringAlgotype, Double> distribution = new HashMap<>();
        for (ColoringAlgotype algotype : allTypes) {
            distribution.put(algotype, percentage);
        }
        
        return createCells(distribution, graph, populationSize);
    }
}
