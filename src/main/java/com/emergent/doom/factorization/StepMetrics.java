package com.emergent.doom.factorization;

/**
 * Data record for per-step metrics in clustering vs fitness experiment.
 *
 * <p><strong>PURPOSE:</strong> Capture all relevant metrics at each step to enable
 * analysis of factor localization dynamics under different aggregation conditions.</p>
 *
 * <p><strong>KEY METRICS:</strong></p>
 * <ul>
 *   <li>aggregationValue - percentage of cells with same-strategy neighbor (clustering measure)</li>
 *   <li>factorPositions - array positions of true factors (candidates 11 and 13)</li>
 *   <li>meanFactorDistanceFromFront - average distance of factors from front of array</li>
 *   <li>fitnessGradientMean - mean absolute fitness difference between adjacent cells</li>
 *   <li>fitnessGradientStd - standard deviation of fitness gradient</li>
 *   <li>strategyEntropyGlobal - Shannon entropy of strategy distribution across entire array</li>
 *   <li>strategyEntropyFront - Shannon entropy in positions 0-9 (top 20%)</li>
 *   <li>swapCount - number of swaps executed in this step</li>
 * </ul>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Immutable: All fields final, set in constructor</li>
 *   <li>Self-describing: Field names match CSV column headers</li>
 *   <li>Scientific: Captures both clustering (aggregation, entropy) and sorting (fitness gradient, factor positions)</li>
 *   <li>Compact: No derived values—all raw measurements for downstream analysis</li>
 * </ul>
 *
 * <p><strong>USAGE:</strong> Created once per step during experiment execution,
 * then serialized to CSV for time-series analysis.</p>
 *
 * <p><strong>REFERENCE:</strong> Section "Required Metrics Per Step" in experiment specification</p>
 */
public class StepMetrics {
    
    // ==================== TIME ====================
    
    /** Step number (0 = initial state, 1 = after first execution step) */
    public final int stepNumber;
    
    // ==================== CLUSTERING METRICS ====================
    
    /**
     * Strategy aggregation: percentage of cells with at least one same-strategy neighbor.
     *
     * <p><strong>SEMANTIC NOTE:</strong> This measures STRATEGY-LABEL adjacency, not
     * fitness-field clustering. Renamed from "aggregationValue" to "strategyAggregation"
     * for semantic clarity (v2 experiment). See {@link #fitnessClustering} for
     * fitness-based clustering metric.</p>
     *
     * <p><strong>FORMULA:</strong> (cells with same-strategy neighbor / total cells) × 100</p>
     *
     * <p><strong>INTERPRETATION:</strong></p>
     * <ul>
     *   <li>100% = complete strategy grouping (all cells grouped by strategy)</li>
     *   <li>~50-60% = random baseline for 3-strategy chimeric population</li>
     *   <li>0% = maximally mixed strategies (no adjacent same-strategy cells)</li>
     * </ul>
     *
     * <p><strong>EXPERIMENTAL NOTE:</strong> Used for MANIPULATION (C2 pre-groups strategies),
     * not for MEASUREMENT. Using this as outcome metric creates circular reasoning.</p>
     *
     * @deprecated Use {@link #fitnessClustering} for hypothesis testing to avoid circular reasoning
     */
    @Deprecated
    public final double strategyAggregation;
    
    /**
     * Fitness clustering: percentage of cells with at least one similar-fitness neighbor.
     *
     * <p><strong>SEMANTIC ALIGNMENT:</strong> This measures FITNESS-FIELD spatial aggregation,
     * independent of strategy labels. This is the correct metric for testing whether
     * "clustering" (fitness-similarity adjacency) affects factor localization.</p>
     *
     * <p><strong>FORMULA:</strong> (cells with fitness-similar neighbor / total cells) × 100,
     * where "similar" means |fitness[i] - fitness[neighbor]| < 0.1</p>
     *
     * <p><strong>INTERPRETATION:</strong></p>
     * <ul>
     *   <li>High (≥75%) = fitness landscape is spatially clustered (similar fitness grouped)</li>
     *   <li>~50% = random fitness distribution</li>
     *   <li>Low (≤25%) = fitness landscape is fragmented (dissimilar fitness mixed)</li>
     * </ul>
     *
     * <p><strong>HYPOTHESIS TEST:</strong> If fitness clustering drives localization,
     * conditions with higher fitnessClustering should show faster factor movement.</p>
     */
    public final double fitnessClustering;
    
    /**
     * Factor localization: normalized proximity of high-fitness factors.
     *
     * <p><strong>SEMANTIC ALIGNMENT:</strong> Per Levin, "localization" is concentration
     * of high-fitness configurations in morphospace. This metric captures pattern formation
     * independent of task-specific convergence criteria.</p>
     *
     * <p><strong>FORMULA:</strong> 1.0 - (interFactorDistance / maxDistance)</p>
     *
     * <p><strong>INTERPRETATION:</strong></p>
     * <ul>
     *   <li>1.0 = perfect localization (factors adjacent)</li>
     *   <li>~0.5 = moderate dispersion (factors mid-array apart)</li>
     *   <li>0.0 = maximum dispersion (factors at opposite ends)</li>
     * </ul>
     *
     * <p><strong>COMPARISON:</strong> Factors at [2,3] have high localization (0.98)
     * AND convergence. Factors at [0,49] have convergence criteria met but LOW localization (0.02).
     * This metric separates pattern formation from task success.</p>
     */
    public final double factorLocalization;
    
    /**
     * Shannon entropy of strategy distribution across entire array.
     *
     * <p><strong>FORMULA:</strong> H = -Σ(p_i × log₂(p_i)) where p_i = proportion of strategy i</p>
     *
     * <p><strong>INTERPRETATION:</strong></p>
     * <ul>
     *   <li>log₂(3) ≈ 1.585 = maximum entropy for 3 strategies (uniform distribution)</li>
     *   <li>0 = minimum entropy (single strategy dominates)</li>
     * </ul>
     *
     * <p><strong>PURPOSE:</strong> Measure strategy diversity globally. Complements aggregation
     * by capturing mixing at the distribution level rather than spatial level.</p>
     */
    public final double strategyEntropyGlobal;
    
    /**
     * Shannon entropy of strategy distribution in front 20% of array (positions 0-9).
     *
     * <p><strong>PURPOSE:</strong> Test if factors localize in strategy-homogeneous regions.
     * If clustering drives localization, entropy in front should drop as factors move forward.</p>
     *
     * <p><strong>HYPOTHESIS:</strong> If fitness-driven, front entropy should remain high
     * (strategies mix as all high-fitness cells move forward, regardless of strategy).</p>
     */
    public final double strategyEntropyFront;
    
    // ==================== FACTOR LOCALIZATION METRICS ====================
    
    /**
     * Positions of cells containing true factors (candidates 11 and 13).
     *
     * <p><strong>FORMAT:</strong> Two-element array: [position of 11, position of 13]</p>
     *
     * <p><strong>SPECIAL CASES:</strong></p>
     * <ul>
     *   <li>If factor not present (C4 control), position = -1</li>
     *   <li>Array always length 2 for consistent CSV output</li>
     * </ul>
     *
     * <p><strong>SUCCESS CRITERION:</strong> Both factors in positions 0-4 indicates localization.</p>
     */
    public final int[] factorPositions;
    
    /**
     * Mean distance of true factors from front of array.
     *
     * <p><strong>FORMULA:</strong> average(position of 11, position of 13)</p>
     *
     * <p><strong>INTERPRETATION:</strong></p>
     * <ul>
     *   <li>0.0 = both factors at front (perfect localization)</li>
     *   <li>~25.0 = factors at mid-array (random positions for 50-cell array)</li>
     *   <li>49.0 = both factors at rear (inverse localization, should not occur)</li>
     * </ul>
     *
     * <p><strong>CONVERGENCE INDICATOR:</strong> Decreasing trend indicates factors
     * moving toward front. Rate of decrease reveals localization speed.</p>
     */
    public final double meanFactorDistanceFromFront;
    
    // ==================== FITNESS LANDSCAPE METRICS ====================
    
    /**
     * Mean absolute fitness difference between adjacent cells.
     *
     * <p><strong>FORMULA:</strong> mean(|fitness[i] - fitness[i+1]|) for i = 0..N-2</p>
     *
     * <p><strong>INTERPRETATION:</strong></p>
     * <ul>
     *   <li>High gradient = rough fitness landscape (many adjacent cells with different fitness)</li>
     *   <li>Low gradient = smooth landscape (fitness changes gradually)</li>
     *   <li>~0.0 = flat landscape (all cells have similar fitness, as in C4 control)</li>
     * </ul>
     *
     * <p><strong>PURPOSE:</strong> Measure sorting progress. As bubble sort proceeds,
     * fitness gradient should decrease (array becomes more sorted by fitness).</p>
     */
    public final double fitnessGradientMean;
    
    /**
     * Standard deviation of fitness gradient across array.
     *
     * <p><strong>INTERPRETATION:</strong></p>
     * <ul>
     *   <li>High std = non-uniform gradient (some regions have steep changes, others flat)</li>
     *   <li>Low std = uniform gradient (fitness changes consistently across array)</li>
     * </ul>
     *
     * <p><strong>HYPOTHESIS:</strong> If factors form a plateau at front, gradient std
     * should increase (steep drop at plateau boundary, flat elsewhere).</p>
     */
    public final double fitnessGradientStd;
    
    // ==================== EXECUTION METRICS ====================
    
    /**
     * Number of swaps executed in this step.
     *
     * <p><strong>CONVERGENCE:</strong> swapCount = 0 indicates no beneficial swaps remain.
     * For factor localization, this typically occurs when both factors reach front and
     * form a fitness plateau.</p>
     *
     * <p><strong>USAGE:</strong> Track sorting activity. High swap count early, declining
     * to zero indicates convergence.</p>
     */
    public final int swapCount;
    
    // ==================== CONSTRUCTOR ====================
    
    /**
     * Create a StepMetrics record with all measurements.
     *
     * <p><strong>PURPOSE:</strong> Capture complete state at a single step for CSV export.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>stepNumber - execution step (0 = initial, 1+ = after execution)</li>
     *   <li>strategyAggregation - strategy-label clustering percentage [0.0, 100.0] (v1 metric)</li>
     *   <li>fitnessClustering - fitness-similarity clustering percentage [0.0, 100.0] (v2 metric)</li>
     *   <li>factorLocalization - inter-factor proximity [0.0, 1.0] (v2 metric)</li>
     *   <li>factorPositions - positions of factors 11 and 13 (length 2 array)</li>
     *   <li>meanFactorDistanceFromFront - average factor distance from position 0</li>
     *   <li>fitnessGradientMean - mean |fitness[i] - fitness[i+1]|</li>
     *   <li>fitnessGradientStd - standard deviation of fitness gradient</li>
     *   <li>strategyEntropyGlobal - Shannon entropy across entire array</li>
     *   <li>strategyEntropyFront - Shannon entropy in positions 0-9</li>
     *   <li>swapCount - swaps executed in this step</li>
     * </ul>
     *
     * <p><strong>VALIDATION:</strong> Minimal validation—assumes caller provides valid data.
     * Only checks factorPositions array length for safety.</p>
     *
     * @param stepNumber the step number
     * @param strategyAggregation the strategy-label clustering percentage (v1)
     * @param fitnessClustering the fitness-similarity clustering percentage (v2)
     * @param factorLocalization the inter-factor proximity (v2)
     * @param factorPositions the positions of true factors
     * @param meanFactorDistanceFromFront the mean factor distance from front
     * @param fitnessGradientMean the mean fitness gradient
     * @param fitnessGradientStd the fitness gradient standard deviation
     * @param strategyEntropyGlobal the global strategy entropy
     * @param strategyEntropyFront the front region strategy entropy
     * @param swapCount the number of swaps
     * @throws IllegalArgumentException if factorPositions is not length 2
     */
    public StepMetrics(
            int stepNumber,
            double strategyAggregation,
            double fitnessClustering,
            double factorLocalization,
            int[] factorPositions,
            double meanFactorDistanceFromFront,
            double fitnessGradientMean,
            double fitnessGradientStd,
            double strategyEntropyGlobal,
            double strategyEntropyFront,
            int swapCount) {
        
        if (factorPositions.length != 2) {
            throw new IllegalArgumentException(
                "factorPositions must have length 2, got: " + factorPositions.length
            );
        }
        
        this.stepNumber = stepNumber;
        this.strategyAggregation = strategyAggregation;
        this.fitnessClustering = fitnessClustering;
        this.factorLocalization = factorLocalization;
        this.factorPositions = factorPositions;
        this.meanFactorDistanceFromFront = meanFactorDistanceFromFront;
        this.fitnessGradientMean = fitnessGradientMean;
        this.fitnessGradientStd = fitnessGradientStd;
        this.strategyEntropyGlobal = strategyEntropyGlobal;
        this.strategyEntropyFront = strategyEntropyFront;
        this.swapCount = swapCount;
    }
    
    // ==================== CSV EXPORT ====================
    
    /**
     * Convert to CSV row format.
     *
     * <p><strong>PURPOSE:</strong> Generate CSV-compatible string for time-series export.</p>
     *
     * <p><strong>FORMAT:</strong> 
     * step,strategy_agg,fitness_clust,factor_local,factor_11_pos,factor_13_pos,mean_factor_dist,
     * fitness_grad_mean,fitness_grad_std,entropy_global,entropy_front,swaps</p>
     *
     * <p><strong>OUTPUTS:</strong> Comma-separated string (no trailing newline)</p>
     *
     * @return CSV row string
     */
    public String toCsvRow() {
        return String.format("%d,%.2f,%.2f,%.4f,%d,%d,%.2f,%.4f,%.4f,%.4f,%.4f,%d",
            stepNumber,
            strategyAggregation,
            fitnessClustering,
            factorLocalization,
            factorPositions[0],
            factorPositions[1],
            meanFactorDistanceFromFront,
            fitnessGradientMean,
            fitnessGradientStd,
            strategyEntropyGlobal,
            strategyEntropyFront,
            swapCount
        );
    }
    
    /**
     * Get CSV header row.
     *
     * <p><strong>PURPOSE:</strong> Provide column names for CSV file header.</p>
     *
     * <p><strong>OUTPUTS:</strong> Comma-separated header string matching toCsvRow() format</p>
     *
     * @return CSV header string
     */
    public static String getCsvHeader() {
        return "step,strategy_agg,fitness_clust,factor_local,factor_11_pos,factor_13_pos,mean_factor_dist," +
               "fitness_grad_mean,fitness_grad_std,entropy_global,entropy_front,swaps";
    }
    
    // ==================== DISPLAY ====================
    
    @Override
    public String toString() {
        return String.format(
            "StepMetrics[step=%d, stratAgg=%.1f%%, fitClust=%.1f%%, local=%.3f, factors=[%d,%d], dist=%.1f, swaps=%d]",
            stepNumber,
            strategyAggregation,
            fitnessClustering,
            factorLocalization,
            factorPositions[0],
            factorPositions[1],
            meanFactorDistanceFromFront,
            swapCount
        );
    }
}
