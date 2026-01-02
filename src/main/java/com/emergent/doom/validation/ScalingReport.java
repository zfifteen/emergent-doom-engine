package com.emergent.doom.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * Aggregated analysis report for scaling validation experiments.
 * 
 * <p><strong>Purpose:</strong> Computes the critical B metric (∂steps/∂array_size)
 * from trial results. B ≈ 0 indicates linear scaling (O(n)), while B > 0.5 indicates
 * the failure boundary where convergence time grows with array size.</p>
 * 
 * <p><strong>Architecture Role:</strong> Central analysis component that determines
 * whether linear scaling hypothesis holds for a given stage/target. Produces both
 * quantitative metrics (B, Z-normalization, R²) and qualitative assessment.</p>
 * 
 * <p><strong>Data Flow:</strong>
 * <ul>
 *   <li>Input: List of ScalingTrialResult from multiple array sizes at same target</li>
 *   <li>Processing: Linear regression on (array_size, mean_steps) pairs</li>
 *   <li>Output: B coefficient, statistical significance, success rates, recommendations</li>
 * </ul>
 * </p>
 */
public class ScalingReport {
    private final ScalingStage stage;
    private final List<ScalingTrialResult> results;
    private final double bCoefficient;
    private final double rSquared;
    private final double zNormalization;
    private final double successRate;
    private final String assessment;
    
    /**
     * Generate scaling analysis report from trial results.
     * 
     * <p><strong>Implementation:</strong> Performs statistical analysis
     * and generates comprehensive report with B coefficient calculation.</p>
     * 
     * <p><strong>Reasoning:</strong> B metric is the key indicator of scaling
     * behavior. Linear regression provides both slope (B) and fit quality (R²).
     * Success rate indicates whether the method works at all for this difficulty.</p>
     * 
     * <p><strong>Integration:</strong> Created by generateReport() in main workflow,
     * used to generate console output and determine early termination.</p>
     * 
     * @param stage The experimental stage
     * @param results Trial results to analyze (must span multiple array sizes)
     */
    public ScalingReport(ScalingStage stage, List<ScalingTrialResult> results) {
        this.stage = stage;
        this.results = new ArrayList<>(results);
        
        // Compute aggregated metrics per array size
        Map<Integer, List<Integer>> stepsByArraySize = new HashMap<>();
        Map<Integer, List<Boolean>> successByArraySize = new HashMap<>();
        
        for (ScalingTrialResult result : results) {
            int arraySize = result.getConfig().getArraySize();
            stepsByArraySize.putIfAbsent(arraySize, new ArrayList<>());
            successByArraySize.putIfAbsent(arraySize, new ArrayList<>());
            
            stepsByArraySize.get(arraySize).add(result.getStepsToConvergence());
            successByArraySize.get(arraySize).add(result.isFoundFactor());
        }
        
        // Perform linear regression: steps = B * arraySize + intercept
        SimpleRegression regression = new SimpleRegression();
        for (Map.Entry<Integer, List<Integer>> entry : stepsByArraySize.entrySet()) {
            int arraySize = entry.getKey();
            double meanSteps = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0);
            regression.addData(arraySize, meanSteps);
        }
        
        this.bCoefficient = regression.getSlope();
        this.rSquared = regression.getRSquare();
        
        // Compute Z-normalization (simplified version)
        // Z measures how much step variation exists relative to what we'd expect if scaling with array size
        double meanStepsOverall = results.stream()
            .mapToInt(ScalingTrialResult::getStepsToConvergence)
            .average().orElse(0);
        double stdStepsOverall = Math.sqrt(results.stream()
            .mapToDouble(r -> Math.pow(r.getStepsToConvergence() - meanStepsOverall, 2))
            .average().orElse(0));
        this.zNormalization = stdStepsOverall / Math.max(meanStepsOverall, 1.0);
        
        // Compute success rate
        long successes = results.stream().filter(ScalingTrialResult::isFoundFactor).count();
        this.successRate = (double) successes / results.size();
        
        // Generate assessment
        this.assessment = generateAssessment();
    }
    
    private String generateAssessment() {
        // Decision logic for assessment
        if (successRate < 0.5) {
            return "NON-CONVERGENT (success rate < 50%)";
        } else if (Math.abs(bCoefficient) < 0.01 && rSquared > 0.90 && successRate > 0.9) {
            return "LINEAR SCALING CONFIRMED (B ≈ 0, high R², high success rate)";
        } else if (bCoefficient > 0.5) {
            return "FAILURE BOUNDARY FOUND (B > 0.5, super-linear growth detected)";
        } else if (rSquared < 0.70) {
            return "INCONCLUSIVE (poor linear fit, high variance)";
        } else {
            return "MODERATE LINEAR SCALING (B small but non-zero)";
        }
    }
    
    /**
     * Get the B coefficient (slope of steps vs array_size).
     * 
     * <p><strong>Implementation:</strong> Returns the regression slope
     * B = ∂(mean_steps)/∂(array_size).</p>
     * 
     * <p><strong>Interpretation:</strong>
     * <ul>
     *   <li>B ≈ 0: Steps invariant to array size → O(n) scaling confirmed</li>
     *   <li>B > 0.5: Steps grow with array size → failure boundary found</li>
     *   <li>B < 0: Steps decrease with array size → artifact, investigate</li>
     * </ul>
     * </p>
     * 
     * @return The B coefficient
     */
    public double getBCoefficient() {
        return bCoefficient;
    }
    
    /**
     * Get the R² goodness-of-fit for linear regression.
     * 
     * <p><strong>Implementation:</strong> Returns R² ∈ [0,1].</p>
     * 
     * <p><strong>Interpretation:</strong>
     * R² close to 1 means data fits linear model well (either B≈0 or B>0 consistently).
     * R² << 1 means high variance, inconclusive results.</p>
     * 
     * @return R-squared value
     */
    public double getRSquared() {
        return rSquared;
    }
    
    /**
     * Get the Z-normalization metric.
     * 
     * <p><strong>Implementation:</strong> Returns coefficient of variation
     * (std/mean) for step counts across all trials.</p>
     * 
     * <p><strong>From LINEAR_SCALING_ANALYSIS.md:</strong>
     * Z ≈ 0.08 means 99.92% invariance, confirming constant convergence time.</p>
     * 
     * @return Z-normalization score
     */
    public double getZNormalization() {
        return zNormalization;
    }
    
    /**
     * Get the overall success rate across all trials.
     * 
     * <p><strong>Implementation:</strong> Returns fraction of trials
     * that found factors within step limit.</p>
     * 
     * @return Success rate [0,1]
     */
    public double getSuccessRate() {
        return successRate;
    }
    
    /**
     * Get the qualitative assessment.
     * 
     * <p><strong>Implementation:</strong> Returns human-readable verdict based on metrics.</p>
     * 
     * @return Assessment string
     */
    public String getAssessment() {
        return assessment;
    }
    
    /**
     * Generate detailed console report.
     * 
     * <p><strong>Implementation:</strong> Formats a multi-line report
     * with all metrics, per-array-size breakdown, and interpretation.</p>
     * 
     * <p><strong>Reasoning:</strong> Console output provides immediate feedback
     * during experiments. Structured format makes it easy to spot issues.</p>
     * 
     * @return Formatted report string
     */
    public String generateConsoleReport() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=" .repeat(70)).append("\n");
        sb.append("SCALING ANALYSIS REPORT\n");
        sb.append("=".repeat(70)).append("\n\n");
        
        sb.append(String.format("Stage: %s\n", stage));
        sb.append(String.format("Total trials: %d\n", results.size()));
        sb.append("\n");
        
        sb.append("KEY METRICS:\n");
        sb.append(String.format("  B coefficient (∂steps/∂array_size): %.6f\n", bCoefficient));
        sb.append(String.format("  R² (goodness of fit): %.4f\n", rSquared));
        sb.append(String.format("  Z-normalization (CV): %.4f\n", zNormalization));
        sb.append(String.format("  Success rate: %.1f%%\n", successRate * 100));
        sb.append("\n");
        
        sb.append("ASSESSMENT: ").append(assessment).append("\n");
        sb.append("\n");
        
        // Per-array-size breakdown
        sb.append("PER-ARRAY-SIZE BREAKDOWN:\n");
        Map<Integer, List<Integer>> stepsBySize = new HashMap<>();
        Map<Integer, Integer> successesBySize = new HashMap<>();
        
        for (ScalingTrialResult result : results) {
            int size = result.getConfig().getArraySize();
            stepsBySize.putIfAbsent(size, new ArrayList<>());
            stepsBySize.get(size).add(result.getStepsToConvergence());
            successesBySize.put(size, successesBySize.getOrDefault(size, 0) + 
                              (result.isFoundFactor() ? 1 : 0));
        }
        
        for (Integer size : stepsBySize.keySet().stream().sorted().toArray(Integer[]::new)) {
            List<Integer> steps = stepsBySize.get(size);
            double mean = steps.stream().mapToInt(Integer::intValue).average().orElse(0);
            double stdDev = Math.sqrt(steps.stream()
                .mapToDouble(s -> Math.pow(s - mean, 2)).average().orElse(0));
            int successes = successesBySize.getOrDefault(size, 0);
            
            sb.append(String.format("  Array size %d: mean=%.1f, std=%.1f, successes=%d/%d (%.0f%%)\n",
                size, mean, stdDev, successes, steps.size(), 
                100.0 * successes / steps.size()));
        }
        
        sb.append("\n");
        sb.append("=".repeat(70)).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Export results to CSV format.
     * 
     * <p><strong>Implementation:</strong> Generates CSV rows for all trials
     * following the schema compatible with analyze_scaling.py.</p>
     * 
     * <p><strong>Reasoning:</strong> CSV export enables offline analysis
     * and archival of experimental data.</p>
     * 
     * @return CSV content string with header
     */
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("stage,target,arraySize,steps,converged,foundFactor,factor,timeMs,");
        sb.append("remainderMean,remainderVariance,remainderAutocorr\n");
        
        // Data rows
        for (ScalingTrialResult result : results) {
            sb.append(result.toCsvRow()).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Determine if should proceed to next stage.
     * 
     * <p><strong>Implementation:</strong> Applies decision logic based on
     * B coefficient and success rate.</p>
     * 
     * <p><strong>Reasoning:</strong>
     * <ul>
     *   <li>Proceed if B ≈ 0 and success rate high (linear scaling holds)</li>
     *   <li>Stop if B > 0.5 (found failure boundary)</li>
     *   <li>Stop if non-convergence detected (success rate low)</li>
     * </ul>
     * </p>
     * 
     * @return true if should continue to next stage, false otherwise
     */
    public boolean shouldProceedToNextStage() {
        // Don't proceed if success rate too low
        if (successRate < 0.7) {
            return false;
        }
        
        // Don't proceed if B > 0.5 (failure boundary detected)
        if (bCoefficient > 0.5) {
            return false;
        }
        
        // Proceed if B ≈ 0 and good fit
        return Math.abs(bCoefficient) < 0.1 && rSquared > 0.70;
    }
}
