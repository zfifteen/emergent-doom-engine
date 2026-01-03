package com.emergent.doom.examples;

import com.emergent.doom.cell.RemainderCell;
import com.emergent.doom.experiment.ExperimentConfig;
import com.emergent.doom.experiment.ExperimentResults;
import com.emergent.doom.experiment.ExperimentRunner;
import com.emergent.doom.metrics.MonotonicityError;
import com.emergent.doom.metrics.SortednessValue;

import com.emergent.doom.util.SemiPrimeGenerator;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
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
        printHeader();

        ExperimentParameters params = parseArguments(args);
        if (params == null) return;

        int numTrials = calculateNumTrials(params);
        ExperimentConfig config = createConfig(numTrials, params.arraySize);

        System.out.printf("Running %d trials per target using the new batch parallelism model (maxSteps=%d, stableSteps=%d)...%n", 
            numTrials, config.getMaxSteps(), config.getRequiredStableSteps());

        runExperiments(params.targets, config);

        System.out.println("\nExperiment complete!");
    }

    private static void printHeader() {
        System.out.println("Emergent Doom Engine - Factorization Experiment");
        System.out.println("=".repeat(60));
    }

    private static ExperimentParameters parseArguments(String[] args) {
        int numTrials = 100;
        int arraySize = DEFAULT_ARRAY_SIZE;
        boolean autoScale = false;
        List<BigInteger> targets;

        if (args.length == 0) {
            targets = SemiPrimeGenerator.generateSemiPrimes(DEFAULT_SEMIPRIME_COUNT, DEFAULT_MIN, DEFAULT_MAX, DEFAULT_SEED, arraySize);
            autoScale = true;
        } else {
            try {
                targets = Collections.singletonList(new BigInteger(args[0]));
                if (args.length >= 2) {
                    numTrials = Integer.parseInt(args[1]);
                } else {
                    autoScale = true;
                }
                if (args.length >= 3) arraySize = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid number format");
                return null;
            }
        }
        return new ExperimentParameters(targets, numTrials, arraySize, autoScale);
    }

    private static int calculateNumTrials(ExperimentParameters params) {
        int numTrials = params.numTrials;
        if (params.autoScale && !params.targets.isEmpty()) {
            BigInteger target = params.targets.get(0);
            BigInteger sqrt = target.sqrt();
            long trialsNeeded = sqrt.divide(BigInteger.valueOf(params.arraySize)).longValue() + 1;
            
            int trialCap = 10_000;
            numTrials = (int) Math.min(trialCap, trialsNeeded);
            numTrials = Math.max(numTrials, 100);
            
            System.out.printf("Auto-scaling: Target sqrt is %s. Calculated %d trials to cover search space (capped at %d).%n", 
                sqrt, numTrials, trialCap);
        }
        return numTrials;
    }

    private static ExperimentConfig createConfig(int numTrials, int arraySize) {
        int stableSteps = (arraySize >= 2000) ? 10 : 3;
        int maxSteps = 50_000; 

        return new ExperimentConfig(
                arraySize,
                maxSteps,
                stableSteps,
                false, 
                ExecutionMode.SEQUENTIAL,
                numTrials);
    }

    private static void runExperiments(List<BigInteger> targets, ExperimentConfig config) {
        targets.forEach(target -> runSingleTargetExperiment(target, config));
    }

    private static void runSingleTargetExperiment(BigInteger target, ExperimentConfig config) {
        int arraySize = config.getArraySize();
        java.util.function.IntFunction<RemainderCell[]> factory = trialIndex -> 
            createCellArray(target, arraySize, trialIndex * arraySize);
        
        ExperimentRunner<RemainderCell> runner = new ExperimentRunner<>(
                factory,
                () -> new LinearNeighborhood<>(1)
        );
        runner.addMetric("Monotonicity", new MonotonicityError<>());
        runner.addMetric("Sortedness", new SortednessValue<>());

        ExperimentResults<RemainderCell> results = runner.runBatchExperiments(config);
        reportResults(target, results);
    }

    private static void reportResults(BigInteger target, ExperimentResults<RemainderCell> results) {
        boolean found = resultsContainFactor(results);
        
        System.out.printf("Target=%s, factorFound=%s, MeanSteps=%.2f, ConvRate=%.1f%%%n", 
            target, found, results.getMeanSteps(), results.getConvergenceRate() * 100);
        
        if (found) {
            results.getTrials().stream()
                .flatMap(trial -> java.util.Arrays.stream(trial.getFinalCells()))
                .filter(cell -> cell.getPosition() > 1 && cell.isFactor())
                .findFirst()
                .ifPresent(cell -> System.out.printf("Factor discovered at position: %d%n", cell.getPosition()));
        }

        System.out.println(results.getSummaryReport());
    }

    private static class ExperimentParameters {
        final List<BigInteger> targets;
        final int numTrials;
        final int arraySize;
        final boolean autoScale;

        ExperimentParameters(List<BigInteger> targets, int numTrials, int arraySize, boolean autoScale) {
            this.targets = targets;
            this.numTrials = numTrials;
            this.arraySize = arraySize;
            this.autoScale = autoScale;
        }
    }

    private static RemainderCell[] createCellArray(BigInteger target, int size, int startOffset) {
        RemainderCell[] cells = new RemainderCell[size];
        for (int i = 0; i < size; i++) {
            // Position is now relative to the segment offset
            int position = startOffset + i + 1;
            cells[i] = new RemainderCell(target, position);
        }
        return cells;
    }

    private static boolean resultsContainFactor(ExperimentResults<RemainderCell> results) {
        return results != null && results.getTrials().stream()
                .flatMap(trial -> java.util.Arrays.stream(trial.getFinalCells()))
                .anyMatch(cell -> cell.getPosition() > 1 && cell.getRemainder().equals(BigInteger.ZERO));
    }
}
