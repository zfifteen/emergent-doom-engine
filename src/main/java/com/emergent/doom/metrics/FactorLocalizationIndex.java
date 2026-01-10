package com.emergent.doom.metrics;

/**
 * Measures LOCALIZATION of high-fitness configurations in morphospace.
 *
 * <p><strong>SEMANTIC ALIGNMENT:</strong> Per Levin's framework, "localization"
 * refers to the concentration of high-fitness configurations in a specific region
 * of morphospace. This differs from "convergence" (reaching target positions) and
 * "clustering" (fitness-field spatial aggregation).</p>
 *
 * <p><strong>PURPOSE:</strong> Quantify how concentrated high-fitness cells
 * (factors) are within the array, independent of their absolute positions.
 * This metric captures pattern formation dynamics without conflating with
 * task-specific convergence criteria.</p>
 *
 * <p><strong>FORMULA:</strong> Factor localization index:
 * <pre>
 * 1. Find positions of all factors (fitness = 1.0)
 * 2. If factors present:
 *      interFactorDistance = |pos[factor1] - pos[factor2]|
 *      localizationIndex = 1.0 - (interFactorDistance / maxDistance)
 *    Else:
 *      localizationIndex = 0.0 (no factors to localize)
 * 
 * Range: [0.0, 1.0]
 *   - 1.0 = perfect localization (factors adjacent)
 *   - 0.0 = maximum dispersion (factors at opposite ends)
 * </pre></p>
 *
 * <p><strong>INTERPRETATION:</strong></p>
 * <ul>
 *   <li>Localization ≥ 0.9 → factors tightly clustered (within ~5 positions)</li>
 *   <li>Localization ~ 0.5 → factors moderately separated</li>
 *   <li>Localization ≤ 0.1 → factors maximally dispersed</li>
 * </ul>
 *
 * <p><strong>COMPARISON WITH CONVERGENCE:</strong></p>
 * <table border="1">
 * <tr><th>Metric</th><th>Measures</th><th>Example</th></tr>
 * <tr><td>Convergence (positions [0,4])</td><td>Task-specific success</td><td>Factors at [2,3] → converged</td></tr>
 * <tr><td>Localization Index</td><td>Pattern formation</td><td>Factors at [2,3] → high localization (0.98)</td></tr>
 * <tr><td></td><td></td><td>Factors at [0,49] → low localization (0.02)</td></tr>
 * </table>
 *
 * <p><strong>EXPERIMENTAL DESIGN:</strong> Use this metric to test Levin's
 * hypothesis that pattern formation (localization) emerges from fitness-driven
 * sorting, not from pre-existing clustering. Measure localization as OUTCOME,
 * not as part of convergence criterion.</p>
 *
 * <p><strong>MULTI-FACTOR EXTENSION:</strong> For N factors, compute pairwise
 * distances and return mean localization. Current implementation assumes 2 factors
 * (11, 13) for factorization domain.</p>
 *
 * <p><strong>REFERENCE:</strong> See experiments/clustering_vs_fitness_experiment_2026_01_10/EXPERIMENT_SETUP_AUDIT.md
 * §5 for discussion of convergence criterion mismatch and need for separate
 * localization metric.</p>
 */
public class FactorLocalizationIndex {
    
    private final int arraySize;
    
    /**
     * Create localization index for given array size.
     * 
     * @param arraySize the size of the cell array (needed for normalization)
     */
    public FactorLocalizationIndex(int arraySize) {
        this.arraySize = arraySize;
    }
    
    /**
     * Compute localization index for two factors.
     * 
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check if both factors present (pos ≥ 0)</li>
     *   <li>Compute inter-factor distance</li>
     *   <li>Normalize by maximum possible distance (arraySize - 1)</li>
     *   <li>Invert to get localization (1.0 - normalized distance)</li>
     * </ol>
     * 
     * <p><strong>EDGE CASES:</strong></p>
     * <ul>
     *   <li>One or both factors missing → return 0.0 (no localization possible)</li>
     *   <li>Factors adjacent (distance = 1) → return 1.0 - 1/(arraySize-1) ≈ 0.98 for size=50</li>
     *   <li>Factors at opposite ends (distance = arraySize-1) → return 0.0</li>
     * </ul>
     * 
     * @param factorPositions array of [pos_factor1, pos_factor2], -1 if absent
     * @return localization index [0.0, 1.0]
     */
    public double compute(int[] factorPositions) {
        if (factorPositions.length < 2) {
            throw new IllegalArgumentException("Need at least 2 factor positions");
        }
        
        int pos1 = factorPositions[0];
        int pos2 = factorPositions[1];
        
        // If either factor missing, no localization possible
        if (pos1 < 0 || pos2 < 0) {
            return 0.0;
        }
        
        // Compute inter-factor distance
        int distance = Math.abs(pos1 - pos2);
        
        // Normalize by maximum possible distance
        int maxDistance = arraySize - 1;
        double normalizedDistance = (double) distance / maxDistance;
        
        // Invert to get localization (close = high, far = low)
        return 1.0 - normalizedDistance;
    }
    
    /**
     * Compute localization with neighborhood fitness bonus.
     * 
     * <p><strong>EXTENDED METRIC:</strong> Combines inter-factor distance with
     * neighborhood fitness quality to capture both spatial proximity and
     * local fitness landscape structure.</p>
     * 
     * <p><strong>FORMULA:</strong>
     * <pre>
     * baseLocalization = 1.0 - (interFactorDistance / maxDistance)
     * neighborhoodBonus = meanFitness(neighborhood around factors)
     * localizationIndex = 0.7 × baseLocalization + 0.3 × neighborhoodBonus
     * </pre></p>
     * 
     * <p><strong>RATIONALE:</strong> Factors at [2,3] with high-fitness neighbors
     * represent stronger pattern formation than factors at [2,3] surrounded by
     * low-fitness cells. The neighborhood context captures emergent fitness-field
     * structure.</p>
     * 
     * @param factorPositions array of [pos_factor1, pos_factor2]
     * @param fitnessValues fitness array for entire cell array
     * @param neighborhoodRadius radius around factors to sample (e.g., 2 positions)
     * @return localization index with neighborhood bonus [0.0, 1.0]
     */
    public double computeWithNeighborhood(
            int[] factorPositions,
            double[] fitnessValues,
            int neighborhoodRadius) {
        
        // Base localization from inter-factor distance
        double baseLocalization = compute(factorPositions);
        
        // If no factors, no neighborhood to evaluate
        if (baseLocalization == 0.0) {
            return 0.0;
        }
        
        int pos1 = factorPositions[0];
        int pos2 = factorPositions[1];
        
        // Compute mean fitness in neighborhood around factors
        double neighborhoodFitness = computeNeighborhoodFitness(
            fitnessValues, pos1, pos2, neighborhoodRadius
        );
        
        // Weighted combination: 70% distance, 30% neighborhood quality
        return 0.7 * baseLocalization + 0.3 * neighborhoodFitness;
    }
    
    /**
     * Compute mean fitness in neighborhood around factors.
     * 
     * @param fitnessValues fitness array
     * @param pos1 position of factor 1
     * @param pos2 position of factor 2
     * @param radius neighborhood radius
     * @return mean fitness in neighborhood [0.0, 1.0]
     */
    private double computeNeighborhoodFitness(
            double[] fitnessValues,
            int pos1,
            int pos2,
            int radius) {
        
        // Determine neighborhood bounds (union of both factors' neighborhoods)
        int minPos = Math.max(0, Math.min(pos1, pos2) - radius);
        int maxPos = Math.min(fitnessValues.length - 1, Math.max(pos1, pos2) + radius);
        
        // Compute mean fitness in neighborhood
        double sum = 0.0;
        int count = 0;
        
        for (int i = minPos; i <= maxPos; i++) {
            sum += fitnessValues[i];
            count++;
        }
        
        return count > 0 ? sum / count : 0.0;
    }
}
