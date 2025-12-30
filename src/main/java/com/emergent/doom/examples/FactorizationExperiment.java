package com.emergent.doom.examples;

import com.emergent.doom.cell.RemainderCell;
import com.emergent.doom.experiment.ExperimentConfig;
import com.emergent.doom.experiment.ExperimentResults;
import com.emergent.doom.experiment.ExperimentRunner;
import com.emergent.doom.experiment.TrialResult;
import com.emergent.doom.metrics.MonotonicityError;
import com.emergent.doom.probe.StepSnapshot;

import java.math.BigInteger;
import java.util.List;

/**
 * Example experiment using the Emergent Doom Engine for integer factorization.
 * 
 * <p>This demonstrates how to:
 * <ul>
 *   <li>Set up a domain-specific problem (factorization)</li>
 *   <li>Configure the EDE components</li>
 *   <li>Run experiments</li>
 *   <li>Analyze results</li>
 * </ul>
 * </p>
 * 
 * <p><strong>Factorization Approach:</strong> Each cell represents a candidate
 * factor. The cell stores the remainder when the target is divided by the
 * cell's position. Through emergent sorting dynamics, cells with smaller
 * remainders (better factors) migrate toward the front of the array.</p>
 */
public class FactorizationExperiment {
    
    /**
     * IMPLEMENTED: Main entry point for running the factorization experiment
     */
    public static void main(String[] args) {
        System.out.println("Emergent Doom Engine - Factorization Experiment");
        System.out.println("=".repeat(60));
        
        // Define target number to factor (143 = 11 × 13)
        BigInteger target = new BigInteger("143");
        int arraySize = 20;  // Enough to include both factors
        
        System.out.printf("Target number: %s%n", target);
        System.out.printf("Array size: %d%n%n", arraySize);
        
        // Create experiment configuration
        ExperimentConfig config = new ExperimentConfig(
                arraySize,      // arraySize
                1000,           // maxSteps
                3,              // requiredStableSteps for convergence
                true            // recordTrajectory
        );
        
        // Create experiment runner with factories
        ExperimentRunner<RemainderCell> runner = new ExperimentRunner<>(
                () -> createCellArray(target, arraySize),  // cell array factory
                () -> new LinearNeighborhood<>(1)          // topology factory (radius=1)
        );
        
        // Add metrics
        runner.addMetric("Monotonicity", new MonotonicityError<>());
        
        // Run experiment with 5 trials
        int numTrials = 5;
        System.out.printf("Running experiment with %d trials...%n%n", numTrials);
        
        ExperimentResults<RemainderCell> results = runner.runExperiment(config, numTrials);
        
        // Print results
        System.out.println();
        System.out.println(results.getSummaryReport());
        
        // Display factors found
        displayFactors(results);
        
        System.out.println("\nExperiment complete!");
    }
    
    /**
     * IMPLEMENTED: Create an array of RemainderCell instances
     */
    private static RemainderCell[] createCellArray(BigInteger target, int size) {
        RemainderCell[] cells = new RemainderCell[size];
        for (int i = 0; i < size; i++) {
            cells[i] = new RemainderCell(target, i + 1); // 1-based positions
        }
        return cells;
    }
    
    /**
     * IMPLEMENTED: Display factors found in the results
     */
    private static void displayFactors(ExperimentResults<RemainderCell> results) {
        if (results.getTrials().isEmpty()) {
            System.out.println("No trials to analyze.");
            return;
        }
        
        // Get last trial
        List<TrialResult<RemainderCell>> trials = results.getTrials();
        TrialResult<RemainderCell> lastTrial = trials.get(trials.size() - 1);
        
        // Get final snapshot
        if (lastTrial.getTrajectory() != null && !lastTrial.getTrajectory().isEmpty()) {
            List<StepSnapshot<RemainderCell>> trajectory = lastTrial.getTrajectory();
            StepSnapshot<RemainderCell> finalSnapshot = trajectory.get(trajectory.size() - 1);
            RemainderCell[] finalCells = finalSnapshot.getCellStates();
            
            System.out.println("\nFactors found (remainder = 0):");
            System.out.println("-".repeat(40));
            for (RemainderCell cell : finalCells) {
                if (cell.isFactor()) {
                    System.out.printf("  Position %d is a factor (remainder = %s)%n", 
                            cell.getPosition(), cell.getRemainder());
                }
            }
        }
    }
}
