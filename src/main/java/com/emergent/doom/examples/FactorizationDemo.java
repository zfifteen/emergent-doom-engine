package com.emergent.doom.examples;

import com.emergent.doom.factorization.FactorStrategy;
import com.emergent.doom.factorization.FactorizationExperiment;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(FactorizationDemo.class);
    
    public static void main(String[] args) {
        logger.info("=== Factorization Experiment Demo ===");
        logger.info("");
        
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
        
        logger.info("Configuration:");
        logger.info("  Target (N): {} (11 × 13)", target);
        logger.info("  Array size: {} candidates", arraySize);
        logger.info("  Strategy distribution:");
        logger.info("    - SMALL_PRIMES: 33%");
        logger.info("    - FERMAT_NEAR_SQRT: 33%");
        logger.info("    - RANDOM_SAMPLE: 34%");
        logger.info("  Random seed: {}", seed);
        logger.info("  Max steps: {}", maxSteps);
        logger.info("");
        
        // Create and run experiment
        FactorizationExperiment experiment = new FactorizationExperiment(
            target, arraySize, distribution, seed, maxSteps
        );
        
        logger.info("Running experiment...");
        FactorizationExperiment.ExperimentResults results = experiment.runTrial();
        
        // Print results
        logger.info("");
        logger.info("=== Results ===");
        logger.info("Convergence step: {}", results.getConvergenceStep());
        logger.info("Peak aggregation: {:.2f}%", results.getPeakAggregation());
        logger.info("Peak aggregation step: {}", results.getPeakAggregationStep());
        
        // Print initial and final states
        FactorizationExperiment.StepData initial = results.trajectory.get(0);
        FactorizationExperiment.StepData finalState = results.trajectory.get(results.trajectory.size() - 1);
        
        logger.info("");
        logger.info("Initial state:");
        logger.info("  Aggregation: {:.2f}%", initial.aggregation);
        logger.info("  Avg fitness: {:.4f}", initial.avgFitness);
        logger.info("  Max fitness: {:.4f}", initial.maxFitness);
        logger.info("  Perfect factors: {}", initial.perfectFactorCount);
        logger.info("  Perfect factor positions: {}", initial.perfectFactorPositions);
        
        logger.info("");
        logger.info("Final state:");
        logger.info("  Aggregation: {:.2f}%", finalState.aggregation);
        logger.info("  Avg fitness: {:.4f}", finalState.avgFitness);
        logger.info("  Max fitness: {:.4f}", finalState.maxFitness);
        logger.info("  Perfect factors: {}", finalState.perfectFactorCount);
        logger.info("  Perfect factor positions: {}", finalState.perfectFactorPositions);
        
        // Export to CSV
        String filename = "factorization_experiment_" + seed + ".csv";
        try {
            results.exportToCSV(filename);
            logger.info("");
            logger.info("Trajectory exported to: {}", filename);
        } catch (IOException e) {
            logger.error("Failed to export CSV: {}", e.getMessage());
        }
        
        logger.info("");
        logger.info("=== Interpretation ===");
        logger.info("Baseline aggregation (random): ~50-61%");
        logger.info("Hypothesis: Peak aggregation > 60% indicates meaningful clustering");
        
        if (results.getPeakAggregation() > 60.0) {
            logger.info("✓ HYPOTHESIS SUPPORTED: Peak aggregation exceeds baseline!");
        } else {
            logger.info("✗ HYPOTHESIS NOT SUPPORTED: Peak aggregation within baseline range");
        }
        
        if (finalState.perfectFactorPositions.size() > 0) {
            logger.info("✓ True factors detected in candidate set");
            logger.info("✓ Factors migrated to front (positions {})", finalState.perfectFactorPositions);
        } else {
            logger.info("✗ No perfect factors in final arrangement (may have been excluded during generation)");
        }
    }
}