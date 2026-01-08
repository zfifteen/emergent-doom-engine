package com.emergent.doom.factorization;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Runs multiple factorization experiment trials with different seeds.
 *
 * <p><strong>PURPOSE:</strong> Execute batch experiments for statistical validation
 * of emergent clustering hypothesis across multiple runs.</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Parallel execution for performance (optional via executor)</li>
 *   <li>Progress tracking for long-running batches</li>
 *   <li>Individual trial CSV export for reproducibility</li>
 *   <li>Aggregate statistics computed from all trials</li>
 * </ul>
 *
 * <p><strong>USAGE EXAMPLE:</strong></p>
 * <pre>
 * BatchExperimentRunner runner = new BatchExperimentRunner(
 *     143,  // target N
 *     50,   // array size
 *     100,  // number of trials
 *     42L   // base seed
 * );
 * 
 * BatchExperimentRunner.BatchResults results = runner.runBatch();
 * System.out.println("Peak aggregation mean: " + results.getPeakAggregationMean());
 * </pre>
 */
public class BatchExperimentRunner {
    
    private final int target;
    private final int arraySize;
    private final int numTrials;
    private final long baseSeed;
    private final int maxStepsPerTrial;
    private final Map<FactorStrategy, Double> distribution;
    
    /**
     * Create batch runner with default configuration.
     *
     * @param target the semiprime N to factor
     * @param arraySize number of candidates per trial
     * @param numTrials number of trials to run
     * @param baseSeed starting seed (will increment for each trial)
     */
    public BatchExperimentRunner(
            int target,
            int arraySize,
            int numTrials,
            long baseSeed) {
        
        this.target = target;
        this.arraySize = arraySize;
        this.numTrials = numTrials;
        this.baseSeed = baseSeed;
        this.maxStepsPerTrial = 5000;
        
        // Default distribution: 33%/33%/34%
        this.distribution = Map.of(
            FactorStrategy.SMALL_PRIMES, 0.33,
            FactorStrategy.FERMAT_NEAR_SQRT, 0.33,
            FactorStrategy.RANDOM_SAMPLE, 0.34
        );
    }
    
    /**
     * Create batch runner with custom configuration.
     */
    public BatchExperimentRunner(
            int target,
            int arraySize,
            int numTrials,
            long baseSeed,
            int maxStepsPerTrial,
            Map<FactorStrategy, Double> distribution) {
        
        this.target = target;
        this.arraySize = arraySize;
        this.numTrials = numTrials;
        this.baseSeed = baseSeed;
        this.maxStepsPerTrial = maxStepsPerTrial;
        this.distribution = distribution;
    }
    
    /**
     * Run all trials sequentially with progress tracking.
     *
     * <p><strong>PURPOSE:</strong> Execute batch and return aggregated results.</p>
     *
     * <p><strong>PROGRESS:</strong> Prints per-trial progress and overall metrics.</p>
     *
     * @return batch results with all trial data and statistics
     */
    public BatchResults runBatch() {
        System.out.println("Starting batch experiment...");
        System.out.println("  Target: " + target);
        System.out.println("  Array size: " + arraySize);
        System.out.println("  Num trials: " + numTrials);
        System.out.println("  Base seed: " + baseSeed);
        System.out.println();
        
        List<FactorizationExperiment.ExperimentResults> trialResults = new ArrayList<>();
        
        for (int trial = 0; trial < numTrials; trial++) {
            long seed = baseSeed + trial;
            
            // Print progress
            System.out.print("Trial " + (trial + 1) + "/" + numTrials + " (seed=" + seed + ")...");
            System.out.flush();
            
            // Run trial
            FactorizationExperiment experiment = new FactorizationExperiment(
                target, arraySize, distribution, seed, maxStepsPerTrial
            );
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            trialResults.add(results);
            
            // Print result
            System.out.println(" converged at step " + results.getConvergenceStep() +
                ", peak aggregation " + String.format("%.1f%%", results.getPeakAggregation()));
        }
        
        System.out.println();
        return new BatchResults(target, arraySize, numTrials, baseSeed, distribution, trialResults);
    }
    
    /**
     * Batch experiment results with aggregated statistics.
     */
    public static class BatchResults {
        public final int target;
        public final int arraySize;
        public final int numTrials;
        public final long baseSeed;
        public final Map<FactorStrategy, Double> distribution;
        public final List<FactorizationExperiment.ExperimentResults> trialResults;
        
        /**
         * Create batch results from trial data.
         */
        public BatchResults(
                int target,
                int arraySize,
                int numTrials,
                long baseSeed,
                Map<FactorStrategy, Double> distribution,
                List<FactorizationExperiment.ExperimentResults> trialResults) {
            
            this.target = target;
            this.arraySize = arraySize;
            this.numTrials = numTrials;
            this.baseSeed = baseSeed;
            this.distribution = distribution;
            this.trialResults = trialResults;
        }
        
        /**
         * Get mean peak aggregation across all trials.
         */
        public double getPeakAggregationMean() {
            return trialResults.stream()
                .mapToDouble(FactorizationExperiment.ExperimentResults::getPeakAggregation)
                .average()
                .orElse(0.0);
        }
        
        /**
         * Get standard deviation of peak aggregation.
         */
        public double getPeakAggregationStdDev() {
            double mean = getPeakAggregationMean();
            double sumSquaredDiffs = trialResults.stream()
                .mapToDouble(r -> {
                    double peak = r.getPeakAggregation();
                    return Math.pow(peak - mean, 2);
                })
                .sum();
            return Math.sqrt(sumSquaredDiffs / numTrials);
        }
        
        /**
         * Get minimum peak aggregation across trials.
         */
        public double getPeakAggregationMin() {
            return trialResults.stream()
                .mapToDouble(FactorizationExperiment.ExperimentResults::getPeakAggregation)
                .min()
                .orElse(0.0);
        }
        
        /**
         * Get maximum peak aggregation across trials.
         */
        public double getPeakAggregationMax() {
            return trialResults.stream()
                .mapToDouble(FactorizationExperiment.ExperimentResults::getPeakAggregation)
                .max()
                .orElse(0.0);
        }
        
        /**
         * Get mean convergence step across all trials.
         */
        public double getConvergenceStepMean() {
            return trialResults.stream()
                .mapToInt(FactorizationExperiment.ExperimentResults::getConvergenceStep)
                .average()
                .orElse(0.0);
        }
        
        /**
         * Count trials where peak aggregation > threshold.
         */
        public int countTrialsAboveThreshold(double threshold) {
            return (int) trialResults.stream()
                .filter(r -> r.getPeakAggregation() > threshold)
                .count();
        }
        
        /**
         * Export all trial CSVs to directory.
         */
        public void exportAllTrialsToCSV(String outputDir) throws IOException {
            for (int i = 0; i < trialResults.size(); i++) {
                String filename = outputDir + "/trial_" + (i + 1) + "_seed_" + (baseSeed + i) + ".csv";
                trialResults.get(i).exportToCSV(filename);
            }
        }
        
        /**
         * Export aggregate statistics to CSV.
         */
        public void exportStatisticsToCSV(String filename) throws IOException {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                // Header
                writer.println("metric,value");
                
                // Statistics
                writer.printf("target,%d%n", target);
                writer.printf("array_size,%d%n", arraySize);
                writer.printf("num_trials,%d%n", numTrials);
                writer.printf("base_seed,%d%n", baseSeed);
                writer.println();
                
                writer.printf("peak_aggregation_mean,%.2f%n", getPeakAggregationMean());
                writer.printf("peak_aggregation_stddev,%.2f%n", getPeakAggregationStdDev());
                writer.printf("peak_aggregation_min,%.2f%n", getPeakAggregationMin());
                writer.printf("peak_aggregation_max,%.2f%n", getPeakAggregationMax());
                writer.println();
                
                writer.printf("convergence_step_mean,%.1f%n", getConvergenceStepMean());
                writer.println();
                
                writer.printf("trials_above_60_percent,%d%n", countTrialsAboveThreshold(60.0));
                writer.printf("trials_above_baseline,%d%n", countTrialsAboveThreshold(61.0));
            }
        }
        
        /**
         * Print summary statistics to stdout.
         */
        public void printSummary() {
            System.out.println("=== Batch Results Summary ===");
            System.out.println("Trials: " + numTrials);
            System.out.println();
            
            System.out.println("Peak Aggregation:");
            System.out.println("  Mean: " + String.format("%.2f%%", getPeakAggregationMean()));
            System.out.println("  StdDev: " + String.format("%.2f%%", getPeakAggregationStdDev()));
            System.out.println("  Min: " + String.format("%.2f%%", getPeakAggregationMin()));
            System.out.println("  Max: " + String.format("%.2f%%", getPeakAggregationMax()));
            System.out.println();
            
            System.out.println("Convergence:");
            System.out.println("  Mean steps: " + String.format("%.1f", getConvergenceStepMean()));
            System.out.println();
            
            System.out.println("Hypothesis Testing:");
            System.out.println("  Baseline (random): ~50-61%");
            System.out.println("  Trials > 60%: " + countTrialsAboveThreshold(60.0) + "/" + numTrials);
            System.out.println("  Trials > 61% (baseline): " + countTrialsAboveThreshold(61.0) + "/" + numTrials);
            System.out.println();
            
            double mean = getPeakAggregationMean();
            if (mean > 61.0) {
                System.out.println("✓ HYPOTHESIS SUPPORTED: Mean aggregation exceeds baseline!");
            } else {
                System.out.println("✗ HYPOTHESIS NOT SUPPORTED: Mean within baseline range");
            }
        }
    }
}
