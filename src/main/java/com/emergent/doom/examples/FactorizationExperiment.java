package com.emergent.doom.examples;

import com.emergent.doom.cell.RemainderCell;
import com.emergent.doom.experiment.ExperimentConfig;
import com.emergent.doom.experiment.ExperimentResults;
import com.emergent.doom.experiment.ExperimentRunner;
import com.emergent.doom.experiment.TrialResult;
import com.emergent.doom.metrics.MonotonicityError;
import com.emergent.doom.metrics.SortednessValue;
import com.emergent.doom.probe.StepSnapshot;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
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

    private static class TargetOutcome {
        final BigInteger target;
        final ExperimentResults<RemainderCell> results;

        TargetOutcome(BigInteger target, ExperimentResults<RemainderCell> results) {
            this.target = target;
            this.results = results;
        }
    }

    public static void main(String[] args) {
        System.out.println("Emergent Doom Engine - Factorization Experiment");
        System.out.println("=".repeat(60));

        List<BigInteger> targets;
        long seed = DEFAULT_SEED;
        int numTrials = 30;
        int arraySize = DEFAULT_ARRAY_SIZE;
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
        
        // DYNAMIC LIMITS: Increase runway for larger arrays
        int maxSteps = 50_000; 

        if (args.length == 0) {
            targets = generateSemiPrimes(DEFAULT_SEMIPRIME_COUNT, DEFAULT_MIN, DEFAULT_MAX, seed, arraySize);
        } else {
            try {
                BigInteger target = new BigInteger(args[0]);
                targets = Collections.singletonList(target);
                if (args.length >= 2) numTrials = Integer.parseInt(args[1]);
                if (args.length >= 3) arraySize = Integer.parseInt(args[2]);
                if (args.length >= 4) numThreads = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid number format");
                return;
            }
        }

        final int trialsToRun = numTrials;
        final int finalArraySize = arraySize;

        // DYNAMIC STABILITY: Larger arrays need more "quiet" steps to confirm convergence
        int stableSteps = (finalArraySize >= 2000) ? 10 : 3;

        ExperimentConfig config = new ExperimentConfig(
                finalArraySize,
                maxSteps,
                stableSteps,
                false, // recordTrajectory = false for memory
                ExecutionMode.SEQUENTIAL);

        final int threads = numThreads; 
        System.out.printf("Running %d trials per target using %d threads (maxSteps=%d, stableSteps=%d)...%n", 
            trialsToRun, threads, maxSteps, stableSteps);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        List<Callable<TargetOutcome>> tasks = new ArrayList<>();
        for (BigInteger target : targets) {
            tasks.add(() -> {
                ExperimentRunner<RemainderCell> runner = new ExperimentRunner<>(
                        () -> createCellArray(target, finalArraySize),
                        () -> new LinearNeighborhood<>(1)
                );
                runner.addMetric("Monotonicity", new MonotonicityError<>());
                runner.addMetric("Sortedness", new SortednessValue<>());

                ExperimentResults<RemainderCell> results = runner.runExperiment(config, trialsToRun);
                return new TargetOutcome(target, results);
            });
        }

        List<Future<TargetOutcome>> futures;
        try {
            futures = pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        int successCount = 0;
        for (Future<TargetOutcome> f : futures) {
            try {
                TargetOutcome outcome = f.get();
                boolean found = resultsContainFactor(outcome.results);
                if (found) successCount++;
                
                // LOGGING: Print raw mean steps even if convergence is low
                System.out.printf("Target=%s, factorFound=%s, MeanSteps=%.2f, ConvRate=%.1f%%%n", 
                    outcome.target, found, outcome.results.getMeanSteps(), outcome.results.getConvergenceRate() * 100);

                System.out.println(outcome.results.getSummaryReport());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        pool.shutdown();
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
        if (results == null || results.getTrials().isEmpty()) return false;
        for (TrialResult<RemainderCell> trial : results.getTrials()) {
            RemainderCell[] finalCells = trial.getFinalCells();
            if (finalCells == null) continue;
            for (RemainderCell cell : finalCells) {
                if (cell.getPosition() > 1 && cell.getRemainder().equals(BigInteger.ZERO)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<BigInteger> generateSemiPrimes(int count, int min, int max, long seed, int maxFactorAllowed) {
        Random rng = new Random(seed);
        int sieveLimit = Math.max(max, maxFactorAllowed);
        boolean[] isComposite = new boolean[sieveLimit + 1];
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= sieveLimit; i++) {
            if (!isComposite[i]) {
                primes.add(i);
                if ((long)i * i <= sieveLimit) {
                    for (int j = i * i; j <= sieveLimit; j += i) isComposite[j] = true;
                }
            }
        }
        List<Integer> smallPrimes = primes.stream().filter(p -> p <= maxFactorAllowed).collect(Collectors.toList());
        List<BigInteger> semiprimes = new ArrayList<>();
        while (semiprimes.size() < count) {
            int p = smallPrimes.get(rng.nextInt(smallPrimes.size()));
            int minQ = (int)Math.ceil((double)min / p);
            int maxQ = (int)Math.floor((double)max / p);
            if (minQ > maxQ || maxQ < 2) continue;
            List<Integer> candidates = new ArrayList<>();
            for (int q : primes) {
                if (q >= minQ && q <= maxQ) candidates.add(q);
            }
            if (candidates.isEmpty()) continue;
            int q = candidates.get(rng.nextInt(candidates.size()));
            semiprimes.add(BigInteger.valueOf((long)p * (long)q));
        }
        return semiprimes;
    }
}
