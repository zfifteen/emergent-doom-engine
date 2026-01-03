package com.emergent.doom.examples;

import com.emergent.doom.cell.RemainderCell;
import com.emergent.doom.experiment.ExperimentConfig;
import com.emergent.doom.experiment.ExperimentResults;
import com.emergent.doom.experiment.ExperimentRunner;
import com.emergent.doom.metrics.MonotonicityError;
import com.emergent.doom.metrics.SortednessValue;

import com.emergent.doom.util.SemiPrimeGenerator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.emergent.doom.execution.ExecutionMode;

/**
 * Example experiment using the Emergent Doom Engine for integer factorization.
 */
public class FactorizationExperiment {
    
    private static final int DEFAULT_SEMIPRIME_COUNT = 1;
    private static final int DEFAULT_MIN = 99_000;
    private static final int DEFAULT_MAX = 101_000;
    private static final long DEFAULT_SEED = 12345L;
    private static final int DEFAULT_ARRAY_SIZE = 1000;

    public static void main(String[] args) {
        System.out.println("Emergent Doom Engine - Factorization Experiment");
        System.out.println("=".repeat(60));

        int numTrials = 100;
        int arraySize = DEFAULT_ARRAY_SIZE;
        
        // DYNAMIC LIMITS: Increase runway for larger arrays
        int maxSteps = 50_000; 

        List<BigInteger> targets;
        if (args.length == 0) {
            targets = SemiPrimeGenerator.generateSemiPrimes(DEFAULT_SEMIPRIME_COUNT, DEFAULT_MIN, DEFAULT_MAX, DEFAULT_SEED, arraySize);
        } else {
            try {
                targets = Collections.singletonList(new BigInteger(args[0]));
                if (args.length >= 2) numTrials = Integer.parseInt(args[1]);
                if (args.length >= 3) arraySize = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid number format");
                return;
            }
        }
        
        final int finalArraySize = arraySize;
        // DYNAMIC STABILITY: Larger arrays need more "quiet" steps to confirm convergence
        int stableSteps = (finalArraySize >= 2000) ? 10 : 3;

        ExperimentConfig config = new ExperimentConfig(
                finalArraySize,
                maxSteps,
                stableSteps,
                false, // recordTrajectory = false for memory
                ExecutionMode.SEQUENTIAL,
                numTrials);

        System.out.printf("Running %d trials per target using the new batch parallelism model (maxSteps=%d, stableSteps=%d)...%n", 
            numTrials, maxSteps, stableSteps);

        targets.forEach(target -> {
            ExperimentRunner<RemainderCell> runner = new ExperimentRunner<>(
                    () -> createCellArray(target, finalArraySize),
                    () -> new LinearNeighborhood<>(1)
            );
            runner.addMetric("Monotonicity", new MonotonicityError<>());
            runner.addMetric("Sortedness", new SortednessValue<>());

            ExperimentResults<RemainderCell> results = runner.runBatchExperiments(config);
            boolean found = resultsContainFactor(results);
            
            System.out.printf("Target=%s, factorFound=%s, MeanSteps=%.2f, ConvRate=%.1f%%%n", 
                target, found, results.getMeanSteps(), results.getConvergenceRate() * 100);
            System.out.println(results.getSummaryReport());
        });

        System.out.println("\nExperiment complete!");
    }
    
    private static RemainderCell[] createCellArray(BigInteger target, int size) {
        RemainderCell[] cells = new RemainderCell[size];
        for (int i = 0; i < size; i++) {
            cells[i] = new RemainderCell(target, i + 1);
        }
        return cells;
    }

    private static boolean resultsContainFactor(ExperimentResults<RemainderCell> results) {
        return results != null && results.getTrials().stream()
                .flatMap(trial -> java.util.Arrays.stream(trial.getFinalCells()))
                .anyMatch(cell -> cell.getPosition() > 1 && cell.getRemainder().equals(BigInteger.ZERO));
    }
}
