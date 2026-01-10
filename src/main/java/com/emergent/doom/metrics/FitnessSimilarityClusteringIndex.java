package com.emergent.doom.metrics;

/**
 * Measures spatial clustering of cells by FITNESS similarity (not strategy labels).
 *
 * <p><strong>SEMANTIC ALIGNMENT:</strong> This metric implements Levin-consistent
 * fitness-field clustering semantics to avoid circular reasoning in experimental design.
 * Unlike {@link AlgotypeAggregationIndex} which measures strategy-label adjacency,
 * this metric quantifies whether cells with SIMILAR FITNESS VALUES tend to be
 * spatially adjacent, independent of their algorithmic strategy.</p>
 *
 * <p><strong>PURPOSE:</strong> Test whether "clustering" (defined as fitness-field
 * spatial aggregation) affects factor localization, without conflating the measurement
 * with the manipulation. This enables substrate-independent pattern formation analysis
 * per Levin's morphogenesis framework.</p>
 *
 * <p><strong>FORMULA:</strong> Fitness-similarity adjacency metric:
 * <pre>
 * For each cell i:
 *   leftSimilar = |fitness[i] - fitness[i-1]| < threshold
 *   rightSimilar = |fitness[i] - fitness[i+1]| < threshold
 *   if (leftSimilar OR rightSimilar): count++
 * return (count / totalCells) × 100
 * </pre></p>
 *
 * <p><strong>INTERPRETATION:</strong></p>
 * <ul>
 *   <li>High value (≥75%) → cells with similar fitness cluster spatially</li>
 *   <li>Low value (≤25%) → fitness landscape is spatially mixed/fragmented</li>
 *   <li>Baseline (~50%) → random fitness distribution</li>
 * </ul>
 *
 * <p><strong>COMPARISON WITH STRATEGY AGGREGATION:</strong></p>
 * <table border="1">
 * <tr><th>Metric</th><th>Measures</th><th>Use Case</th></tr>
 * <tr><td>AlgotypeAggregationIndex</td><td>Strategy-label adjacency</td><td>Chimeric algotype studies (Levin §8)</td></tr>
 * <tr><td>FitnessSimilarityClusteringIndex</td><td>Fitness-field clustering</td><td>Pattern formation, localization dynamics</td></tr>
 * </table>
 *
 * <p><strong>EXPERIMENTAL DESIGN:</strong> Use this metric to measure OUTCOME
 * (fitness clustering), not to define experimental MANIPULATION (strategy grouping).
 * This avoids circular reasoning where clustering hypothesis is tested using
 * clustering-based measurement.</p>
 *
 * <p><strong>THRESHOLD SELECTION:</strong> Default threshold = 0.1 (10% fitness difference).
 * For factorization domain with fitness ∈ [0.0, 1.0], this captures "similar fitness"
 * while distinguishing high-fitness factors from low-fitness non-factors.</p>
 *
 * <p><strong>REFERENCE:</strong> See experiments/clustering_vs_fitness_experiment_2026_01_10/EXPERIMENT_SETUP_AUDIT.md
 * for detailed analysis of circular reasoning in v1 experiment and rationale for
 * fitness-field semantics.</p>
 */
public class FitnessSimilarityClusteringIndex {
    
    /**
     * Default fitness similarity threshold (10% difference).
     * 
     * <p>Two cells are considered "similar" if their fitness difference
     * is less than this threshold. For factorization domain:</p>
     * <ul>
     *   <li>Factors (fitness = 1.0) vs non-factors (fitness < 0.5) → NOT similar</li>
     *   <li>Two near-factors (fitness = 0.9, 0.95) → similar</li>
     *   <li>Two low-fitness cells (fitness = 0.2, 0.25) → similar</li>
     * </ul>
     */
    public static final double DEFAULT_FITNESS_THRESHOLD = 0.1;
    
    private final double fitnessThreshold;
    
    /**
     * Create index with default fitness threshold (0.1).
     */
    public FitnessSimilarityClusteringIndex() {
        this(DEFAULT_FITNESS_THRESHOLD);
    }
    
    /**
     * Create index with custom fitness threshold.
     * 
     * @param fitnessThreshold maximum fitness difference to consider cells "similar"
     */
    public FitnessSimilarityClusteringIndex(double fitnessThreshold) {
        this.fitnessThreshold = fitnessThreshold;
    }
    
    /**
     * Compute fitness-similarity clustering for cell array.
     * 
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>For each cell, check if left or right neighbor has similar fitness</li>
     *   <li>Count cells with at least one similar-fitness neighbor</li>
     *   <li>Return as percentage of total cells</li>
     * </ol>
     * 
     * <p><strong>EDGE CASES:</strong></p>
     * <ul>
     *   <li>Array size ≤ 1 → return 100.0 (trivially clustered)</li>
     *   <li>All fitness equal → return 100.0 (perfect clustering)</li>
     * </ul>
     * 
     * @param fitnessValues array of fitness values (one per cell)
     * @return fitness clustering percentage [0.0, 100.0]
     */
    public double compute(double[] fitnessValues) {
        if (fitnessValues.length <= 1) {
            return 100.0;
        }
        
        int similarNeighborCount = 0;
        
        for (int i = 0; i < fitnessValues.length; i++) {
            double currentFitness = fitnessValues[i];
            
            // Check left neighbor
            boolean hasLeftSimilar = false;
            if (i > 0) {
                double leftFitness = fitnessValues[i - 1];
                hasLeftSimilar = Math.abs(currentFitness - leftFitness) < fitnessThreshold;
            }
            
            // Check right neighbor
            boolean hasRightSimilar = false;
            if (i < fitnessValues.length - 1) {
                double rightFitness = fitnessValues[i + 1];
                hasRightSimilar = Math.abs(currentFitness - rightFitness) < fitnessThreshold;
            }
            
            if (hasLeftSimilar || hasRightSimilar) {
                similarNeighborCount++;
            }
        }
        
        return (similarNeighborCount * 100.0) / fitnessValues.length;
    }
    
    /**
     * Get the fitness threshold used by this index.
     * 
     * @return fitness similarity threshold
     */
    public double getFitnessThreshold() {
        return fitnessThreshold;
    }
}
