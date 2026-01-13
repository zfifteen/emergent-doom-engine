package com.emergent.doom.sat;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Pilot/Full experiment runner for validation (PHASE FOUR: New runner).
 *
 * <p><strong>PURPOSE:</strong> Run multiple trials, compute statistics per §4.4.</p>
 */
public class PilotRunner {

    public static void main(String[] args) {
        // Pilot: 10 trials, 40 cells, small instance
        runPilot(10, 40);
        
        // Full: 100 trials, 100 cells, standard instance
        runFull(100, 100);
    }

    private static void runPilot(int numTrials, int arraySize) {
        System.out.println("\n=== PILOT VALIDATION (10 trials, 40 cells) ===");
        List<Double> maxAggs = new ArrayList<>();
        List<Double> finalSats = new ArrayList<>();
        long totalSteps = 0;
        int solvedCount = 0;
        
        for (int trial = 0; trial < numTrials; trial++) {
            var formula = SATInstanceGenerator.generatePilotInstance();
            var experiment = new SATExperiment(formula, arraySize, trial * 100L);
            experiment.run(500); // Pilot timeout 500 steps
            
            maxAggs.add(experiment.getMaxAggregation());
            finalSats.add(experiment.getFinalSatisfaction());
            totalSteps += experiment.getStepCount();
            if (experiment.isSolved()) solvedCount++;
            
            System.out.printf("Trial %d: steps=%d, max_agg=%.2f, final_sat=%.1f%%, solved=%b%n", 
                trial, experiment.getStepCount(), experiment.getMaxAggregation(), experiment.getFinalSatisfaction(), experiment.isSolved());
        }
        
        double avgMaxAgg = maxAggs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double avgFinalSat = finalSats.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double solveRate = (double) solvedCount / numTrials * 100.0;
        
        System.out.printf("Pilot summary: avg_max_agg=%.2f, avg_final_sat=%.1f%%, solve_rate=%.1f%%, avg_steps=%.1f%n", 
            avgMaxAgg, avgFinalSat, solveRate, (double) totalSteps / numTrials);
    }

    private static void runFull(int numTrials, int arraySize) {
        System.out.println("\n=== FULL VALIDATION (100 trials, 100 cells) ===");
        List<Double> maxAggs = new ArrayList<>();
        List<Double> finalSats = new ArrayList<>();
        long totalSteps = 0;
        int solvedCount = 0;
        
        for (int trial = 0; trial < numTrials; trial++) {
            var formula = SATInstanceGenerator.generateSatisfiable3SAT(50, 215, trial * 100L); // Standard 4.3 density
            var experiment = new SATExperiment(formula, arraySize, trial * 100L);
            experiment.run(2000); // Full timeout 2000 steps
            
            maxAggs.add(experiment.getMaxAggregation());
            finalSats.add(experiment.getFinalSatisfaction());
            totalSteps += experiment.getStepCount();
            if (experiment.isSolved()) solvedCount++;
            
            if (trial % 10 == 0) {
                System.out.printf("Trial %d complete%n", trial);
            }
        }
        
        double avgMaxAgg = maxAggs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double avgFinalSat = finalSats.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double solveRate = (double) solvedCount / numTrials * 100.0;
        
        System.out.printf("Full summary: avg_max_agg=%.2f, avg_final_sat=%.1f%%, solve_rate=%.1f%%, avg_steps=%.1f%n", 
            avgMaxAgg, avgFinalSat, solveRate, (double) totalSteps / numTrials);
        
        // Hypothesis check
        if (avgMaxAgg > 0.6) {
            System.out.println("SUCCESS: Strong clustering observed (avg_max_agg > 0.6)");
        } else {
            System.out.println("WARNING: Clustering weaker than expected (avg_max_agg = " + avgMaxAgg + ")");
        }
    }
}