package com.emergent.doom.examples;

import com.emergent.doom.factorization.*;

import java.io.IOException;
import java.util.Map;

/**
 * Demonstration of factorization experiment.
 *
 * <p><strong>PURPOSE:</strong> Show single-run execution of factorization
 * experiment with trajectory recording and CSV export.</p>
 *
 * <p><strong>EXPERIMENT CONFIGURATION:</strong></p>
 * <ul>
 *   <li>Target: N=143 (11×13 semiprime)</li>
 *   <li>Array size: 50 candidates</li>
 *   <li>Strategy distribution: 33% SMALL_PRIMES, 33% FERMAT_NEAR_SQRT, 34% RANDOM_SAMPLE</li>
 *   <li>Seed: 42 (reproducible)</li>
 *   <li>Max steps: 5000</li>
 * </ul>
 */
public class FactorizationDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Factorization Experiment Demo ===");
        System.out.println();
        
        // Configure experiment
        int target = 143;  // 11 × 13
        int arraySize = 50;
        long seed = 42L;
        int maxSteps = 5000;
        
        Map<FactorStrategy, Double> distribution = Map.of(
            FactorStrategy.SMALL_PRIMES, 0.33,
            FactorStrategy.FERMAT_NEAR_SQRT, 0.33,
            FactorStrategy.RANDOM_SAMPLE, 0.34
        );
        
        System.out.println("Configuration:");
        System.out.println("  Target (N): " + target + " (11 × 13)");
        System.out.println("  Array size: " + arraySize + " candidates");
        System.out.println("  Strategy distribution:");
        System.out.println("    - SMALL_PRIMES: 33%");
        System.out.println("    - FERMAT_NEAR_SQRT: 33%");
        System.out.println("    - RANDOM_SAMPLE: 34%");
        System.out.println("  Random seed: " + seed);
        System.out.println("  Max steps: " + maxSteps);
        System.out.println();
        
        // Create and run experiment
        FactorizationExperiment experiment = new FactorizationExperiment(
            target, arraySize, distribution, seed, maxSteps
        );
        
        System.out.println("Running experiment...");
        FactorizationExperiment.ExperimentResults results = experiment.runTrial();
        
        // Print results
        System.out.println();
        System.out.println("=== Results ===");
        System.out.println("Convergence step: " + results.getConvergenceStep());
        System.out.println("Peak aggregation: " + String.format("%.2f%%", results.getPeakAggregation()));
        System.out.println("Peak aggregation step: " + results.getPeakAggregationStep());
        
        // Print initial and final states
        FactorizationExperiment.StepData initial = results.trajectory.get(0);
        FactorizationExperiment.StepData finalState = results.trajectory.get(results.trajectory.size() - 1);
        
        System.out.println();
        System.out.println("Initial state:");
        System.out.println("  Aggregation: " + String.format("%.2f%%", initial.aggregation));
        System.out.println("  Avg fitness: " + String.format("%.4f", initial.avgFitness));
        System.out.println("  Max fitness: " + String.format("%.4f", initial.maxFitness));
        System.out.println("  Perfect factors: " + initial.perfectFactorCount);
        System.out.println("  Perfect factor positions: " + initial.perfectFactorPositions);
        
        System.out.println();
        System.out.println("Final state:");
        System.out.println("  Aggregation: " + String.format("%.2f%%", finalState.aggregation));
        System.out.println("  Avg fitness: " + String.format("%.4f", finalState.avgFitness));
        System.out.println("  Max fitness: " + String.format("%.4f", finalState.maxFitness));
        System.out.println("  Perfect factors: " + finalState.perfectFactorCount);
        System.out.println("  Perfect factor positions: " + finalState.perfectFactorPositions);
        
        // Export to CSV
        String filename = "factorization_experiment_" + seed + ".csv";
        try {
            results.exportToCSV(filename);
            System.out.println();
            System.out.println("Trajectory exported to: " + filename);
        } catch (IOException e) {
            System.err.println("Failed to export CSV: " + e.getMessage());
        }
        
        System.out.println();
        System.out.println("=== Interpretation ===");
        System.out.println("Baseline aggregation (random): ~50-61%");
        System.out.println("Hypothesis: Peak aggregation > 60% indicates meaningful clustering");
        
        if (results.getPeakAggregation() > 60.0) {
            System.out.println("✓ HYPOTHESIS SUPPORTED: Peak aggregation exceeds baseline!");
        } else {
            System.out.println("✗ HYPOTHESIS NOT SUPPORTED: Peak aggregation within baseline range");
        }
        
        if (finalState.perfectFactorPositions.size() > 0) {
            System.out.println("✓ True factors detected in candidate set");
            System.out.println("✓ Factors migrated to front (positions " + finalState.perfectFactorPositions + ")");
        } else {
            System.out.println("✗ No perfect factors in final arrangement (may have been excluded during generation)");
        }
    }
}
