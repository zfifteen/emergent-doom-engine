package com.emergent.doom.domains.graphcoloring;

/**
 * Enum for graph coloring algotypes (behavioral strategies).
 * 
 * <p><strong>PURPOSE:</strong> Define distinct coloring strategies that enable
 * mobility/strategy segregation in H1 validation experiments.</p>
 * 
 * <p><strong>STRATEGIES:</strong></p>
 * <ul>
 *   <li>GREEDY_REPAIR: Repair highest-conflict vertex to best color</li>
 *   <li>MIN_CONFLICT: Random conflicted vertex, recolor to min-conflict</li>
 *   <li>RANDOM_WALK: Random recolor (high mobility)</li>
 *   <li>BACKTRACK_LIGHT: Small-depth lookahead (3-5 steps)</li>
 * </ul>
 * 
 * <p>These are intentionally not "best possible" - the experiment is about
 * boundary utility, not winning graph coloring.</p>
 */
public enum ColoringAlgotype {
    
    /**
     * Greedy repair: always fix the highest-conflict vertex.
     * 
     * <p>Deterministic and focused, but may get stuck in local minima.</p>
     */
    GREEDY_REPAIR("Repair highest-conflict vertex to best color"),
    
    /**
     * Min-conflict: pick random conflicted vertex, recolor to min-conflict.
     * 
     * <p>Stochastic variant of greedy with broader exploration.</p>
     */
    MIN_CONFLICT("Random conflicted vertex, recolor to min-conflict"),
    
    /**
     * Random walk: random vertex, random recolor.
     * 
     * <p>High mobility, useful for escaping plateaus but slow convergence.</p>
     */
    RANDOM_WALK("Random recolor of random vertex"),
    
    /**
     * Backtrack light: small-depth lookahead (3-5 steps).
     * 
     * <p>Deterministic local search with limited horizon.</p>
     */
    BACKTRACK_LIGHT("Small-depth lookahead on few vertices");
    
    private final String description;
    
    ColoringAlgotype(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return name() + ": " + description;
    }
}
