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
    
    // Run a single semiprime near 1e5
    private static final int DEFAULT_SEMIPRIME_COUNT = 1;
    private static final int DEFAULT_MIN = 99_000;
    private static final int DEFAULT_MAX = 101_000;
    private static final long DEFAULT_SEED = 12345L;
    private static final int DEFAULT_ARRAY_SIZE = 1000; // user requested

    private static class TargetOutcome {
        final BigInteger target;
        final ExperimentResults<RemainderCell> results;

        TargetOutcome(BigInteger target, ExperimentResults<RemainderCell> results) {
            this.target = target;
            this.results = results;
        }
    }

    /**
     * UPDATED: Batch runner for 100 seeded semiprimes, parallelized over targets.
     */
    public static void main(String[] args) {
        System.out.println("Emergent Doom Engine - Factorization Experiment (batch semiprimes)");
        System.out.println("=".repeat(60));

        // CLI / configuration (simple, with defaults)
        final int semiprimeCount = DEFAULT_SEMIPRIME_COUNT;
        final int min = DEFAULT_MIN;
        final int max = DEFAULT_MAX;
        final long seed = DEFAULT_SEED;
        final int arraySize = DEFAULT_ARRAY_SIZE;
        final int numTrials = 5; // per-target trials

        System.out.printf("Generating %d semiprimes in [%d, %d] with seed=%d...%n",
                semiprimeCount, min, max, seed);

        List<BigInteger> targets = generateSemiPrimes(semiprimeCount, min, max, seed, arraySize);
        System.out.printf("Generated %d targets.%n", targets.size());

        // Shared experiment configuration (sequential execution to avoid nested threading)
        ExperimentConfig config = new ExperimentConfig(
                arraySize,      // arraySize
                10_000,         // maxSteps (increased per request)
                3,              // requiredStableSteps for convergence
                true            // recordTrajectory
        , ExecutionMode.SEQUENTIAL);  // Use SEQUENTIAL temporarily for verification

        // Option B: run tasks in a thread pool sized to available processors
        final int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
        System.out.printf("Running experiments in parallel using %d threads (one task per target)...%n", threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        List<Callable<TargetOutcome>> tasks = new ArrayList<>();
        for (BigInteger target : targets) {
            tasks.add(() -> {
                ExperimentRunner<RemainderCell> runner = new ExperimentRunner<>(
                        () -> createCellArray(target, arraySize),
                        () -> new LinearNeighborhood<>(1)
                );
                runner.addMetric("Monotonicity", new MonotonicityError<>());
                runner.addMetric("Sortedness", new SortednessValue<>());

                ExperimentResults<RemainderCell> results = runner.runExperiment(config, numTrials);
                return new TargetOutcome(target, results);
            });
        }

        List<Future<TargetOutcome>> futures;
        try {
            futures = pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execution interrupted while submitting tasks.");
            pool.shutdownNow();
            return;
        }

        // Collect and analyze
        int successCount = 0;
        List<String> perTargetReports = new ArrayList<>();

        for (Future<TargetOutcome> f : futures) {
            try {
                TargetOutcome outcome = f.get();
                boolean found = resultsContainFactor(outcome.results);
                if (found) successCount++;
                String report = String.format("Target=%s, factorFound=%s\n%s",
                        outcome.target, found, outcome.results.getSummaryReport());
                perTargetReports.add(report);
                // Print discovered non-trivial factor positions for this target
                displayFactors(outcome.results);
                // Verify by brute force what the actual factors are
                verifyFactorsByBruteForce(outcome.target, arraySize);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted while waiting for task result.");
            } catch (ExecutionException e) {
                System.err.println("Task execution failed: " + e.getCause());
            }
        }

        pool.shutdown();

        // Print aggregate summary
        System.out.println();
        System.out.println("Batch Factorization Summary");
        System.out.println("-".repeat(60));
        System.out.printf("Total targets: %d, Successes (found factor in any trial): %d%n", targets.size(), successCount);
        System.out.println();

        // Print per-target summaries (only one target in this run)
        perTargetReports.forEach(System.out::println);

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
     * Display factors found in the results - FIXED to use final cell array.
     */
    private static void displayFactors(ExperimentResults<RemainderCell> results) {
        if (results.getTrials().isEmpty()) {
            System.out.println("No trials to analyze.");
            return;
        }
        
        // Get last trial
        List<TrialResult<RemainderCell>> trials = results.getTrials();
        TrialResult<RemainderCell> lastTrial = trials.get(trials.size() - 1);
        
        // Get final cell array
        RemainderCell[] finalCells = lastTrial.getFinalCells();

        if (finalCells == null) {
            System.out.println("Final cell array not available.");
            return;
        }

        System.out.println("\nFactors found (remainder = 0), excluding trivial position 1:");
        System.out.println("-".repeat(60));

        // Collect factors by examining each cell's position and remainder
        List<Integer> factorPositions = new ArrayList<>();
        List<String> factorDetails = new ArrayList<>();

        for (int idx = 0; idx < finalCells.length; idx++) {
            RemainderCell cell = finalCells[idx];
            int position = cell.getPosition();  // Original candidate factor
            BigInteger remainder = cell.getRemainder();

            // Skip trivial factor 1
            if (position == 1) continue;

            // Check if this is a factor (remainder = 0)
            if (remainder.equals(BigInteger.ZERO)) {
                factorPositions.add(position);
                BigInteger cofactor = cell.getTarget().divide(BigInteger.valueOf(position));
                factorDetails.add(String.format("  Position %d (array index %d): %d × %s = %s",
                    position, idx, position, cofactor, cell.getTarget()));
            }
        }

        if (factorPositions.isEmpty()) {
            System.out.println("  No non-trivial factors found in final configuration.");
        } else {
            System.out.printf("  Found %d non-trivial factor(s): %s%n",
                factorPositions.size(), factorPositions);
            System.out.println("\n  Factor details:");
            for (String detail : factorDetails) {
                System.out.println(detail);
            }
        }

        // Show first few cells for debugging
        System.out.println("\n  First 10 cells in sorted array:");
        for (int i = 0; i < Math.min(10, finalCells.length); i++) {
            RemainderCell cell = finalCells[i];
            System.out.printf("    Index %d: position=%d, remainder=%s%n",
                i, cell.getPosition(), cell.getRemainder());
        }
    }

    /**
     * Verify factors by brute force - create cells and check which positions have remainder=0
     */
    private static void verifyFactorsByBruteForce(BigInteger target, int arraySize) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("VERIFICATION: Checking all positions 1-" + arraySize + " for target=" + target);
        System.out.println("=".repeat(60));

        List<Integer> actualFactors = new ArrayList<>();
        for (int pos = 1; pos <= arraySize; pos++) {
            BigInteger remainder = target.mod(BigInteger.valueOf(pos));
            if (remainder.equals(BigInteger.ZERO)) {
                actualFactors.add(pos);
            }
        }

        if (actualFactors.isEmpty()) {
            System.out.println("No factors found in range [1, " + arraySize + "]");
        } else {
            System.out.println("Actual factors in range [1, " + arraySize + "]:");
            for (int factor : actualFactors) {
                BigInteger cofactor = target.divide(BigInteger.valueOf(factor));
                System.out.printf("  Position %d: %d × %s = %s%n",
                    factor, factor, cofactor, target);
            }

            // Exclude trivial factor 1
            List<Integer> nonTrivial = actualFactors.stream()
                .filter(f -> f > 1)
                .collect(Collectors.toList());

            if (nonTrivial.isEmpty()) {
                System.out.println("\nNo non-trivial factors found (only 1 divides the target).");
            } else {
                System.out.println("\nNon-trivial factors: " + nonTrivial);
            }
        }
    }

    /**
     * Check if any trial contains a non-trivial factor (remainder=0, position > 1).
     */
    private static boolean resultsContainFactor(ExperimentResults<RemainderCell> results) {
        if (results == null || results.getTrials().isEmpty()) return false;

        for (TrialResult<RemainderCell> trial : results.getTrials()) {
            RemainderCell[] finalCells = trial.getFinalCells();
            if (finalCells == null) continue;

            // Check each cell for remainder=0 and position > 1
            for (RemainderCell cell : finalCells) {
                if (cell.getPosition() > 1 && cell.getRemainder().equals(BigInteger.ZERO)) {
                    return true;  // Found a non-trivial factor
                }
            }
        }
        return false;
    }

    /**
     * Deterministic seeded semiprime generator constrained so that at least one
     * factor is <= maxFactorAllowed (so small-factor semiprimes that can be
     * discovered with arraySize positions).
     *
     * Strategy: pick a small prime p (<= maxFactorAllowed) and select a partner
     * prime q such that p*q is within [min, max]. Use a seeded Random for
     * reproducibility.
     */
    private static List<BigInteger> generateSemiPrimes(int count, int min, int max, long seed, int maxFactorAllowed) {
        if (min >= max) return Collections.emptyList();
        Random rng = new Random(seed);

        // Generate primes up to max (simple sieve)
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

        // Small primes (<= maxFactorAllowed)
        List<Integer> smallPrimes = primes.stream().filter(p -> p <= maxFactorAllowed).collect(Collectors.toList());
        if (smallPrimes.isEmpty()) throw new IllegalStateException("No small primes found for factorization helper");

        List<BigInteger> semiprimes = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = count * 1000; // safeguard

        while (semiprimes.size() < count && attempts < maxAttempts) {
            attempts++;
            int p = smallPrimes.get(rng.nextInt(smallPrimes.size()));
            int minQ = (int)Math.ceil((double)min / p);
            int maxQ = (int)Math.floor((double)max / p);
            if (minQ > maxQ || maxQ < 2) continue;

            // Find primes in [minQ, maxQ]
            List<Integer> candidates = new ArrayList<>();
            for (int q : primes) {
                if (q < minQ) continue;
                if (q > maxQ) break;
                candidates.add(q);
            }
            if (candidates.isEmpty()) continue;

            int q = candidates.get(rng.nextInt(candidates.size()));
            long product = (long)p * (long)q;
            if (product < min || product > max) continue;

            BigInteger bi = BigInteger.valueOf(product);
            if (!semiprimes.contains(bi)) semiprimes.add(bi);
        }

        if (semiprimes.size() < count) {
            System.err.printf("Warning: only generated %d semiprimes (requested %d)%n", semiprimes.size(), count);
        }

        return semiprimes;
    }
}
