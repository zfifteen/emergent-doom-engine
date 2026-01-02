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
     * Main experiment runner with command-line argument support.
     *
     * Usage:
     *   java FactorizationExperiment                    # Default: test ~1e5 semiprime
     *   java FactorizationExperiment <target>           # Test specific target number
     *   java FactorizationExperiment <target> <trials>  # Custom target with trial count
     *   java FactorizationExperiment <target> <trials> <arraySize>  # Custom target, trials, and array size
     *   java FactorizationExperiment <target> <trials> <arraySize> <threads> # Custom target, trials, array size, and threads
     *
     * Examples:
     *   java FactorizationExperiment                                # FACT-EXP-001 (default)
     *   java FactorizationExperiment 1000000000000000000            # FACT-EXP-002 (1e18)
     *   java FactorizationExperiment 1000000000000000091 100        # FACT-EXP-003 (100 trials)
     *   java FactorizationExperiment 1000000000000000091 100 2000    # FACT-EXP-004 (custom array size)
     *   java FactorizationExperiment 1000000000000000091 100 2000 16 # FACT-EXP-005 (custom threads)
     */
    public static void main(String[] args) {
        System.out.println("Emergent Doom Engine - Factorization Experiment");
        System.out.println("=".repeat(60));

        // Parse command-line arguments
        List<BigInteger> targets;
        long seed = DEFAULT_SEED;
        int numTrials = 30; // Default increased from 5 to 30
        int arraySize = DEFAULT_ARRAY_SIZE;
        int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors());

        if (args.length == 0) {
            // Default mode: generate semiprime in 1e5 range (FACT-EXP-001 behavior)
            System.out.println("Mode: DEFAULT (generating semiprime near 1e5)");
            targets = generateSemiPrimes(DEFAULT_SEMIPRIME_COUNT, DEFAULT_MIN, DEFAULT_MAX, seed, arraySize);
            System.out.printf("Generated %d targets in [%d, %d] with seed=%d%n",
                targets.size(), DEFAULT_MIN, DEFAULT_MAX, seed);
        } else if (args.length == 1) {
            // Single target number provided (for EXP-002 and beyond)
            try {
                BigInteger target = new BigInteger(args[0]);
                targets = Collections.singletonList(target);
                System.out.println("Mode: CUSTOM TARGET");
                System.out.printf("Target: %s%n", target);
                System.out.printf("Target magnitude: ~1e%.0f%n", Math.log10(target.doubleValue()));
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid number format: " + args[0]);
                printUsage();
                return;
            }
        } else if (args.length == 2) {
            // Target and trial count provided
            try {
                BigInteger target = new BigInteger(args[0]);
                numTrials = Integer.parseInt(args[1]);
                targets = Collections.singletonList(target);
                System.out.println("Mode: CUSTOM TARGET WITH TRIALS");
                System.out.printf("Target: %s%n", target);
                System.out.printf("Target magnitude: ~1e%.0f%n", Math.log10(target.doubleValue()));
                System.out.printf("Trials: %d%n", numTrials);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid number format in arguments");
                printUsage();
                return;
            }
        } else if (args.length == 3) {
            // Target, trial count, and array size provided
            try {
                BigInteger target = new BigInteger(args[0]);
                numTrials = Integer.parseInt(args[1]);
                arraySize = Integer.parseInt(args[2]);
                targets = Collections.singletonList(target);
                System.out.println("Mode: CUSTOM TARGET WITH TRIALS AND ARRAY SIZE");
                System.out.printf("Target: %s%n", target);
                System.out.printf("Target magnitude: ~1e%.0f%n", Math.log10(target.doubleValue()));
                System.out.printf("Trials: %d%n", numTrials);
                System.out.printf("Array size: %d%n", arraySize);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid number format in arguments");
                printUsage();
                return;
            }
        } else if (args.length == 4) {
            // Target, trial count, array size, and thread count provided
            try {
                BigInteger target = new BigInteger(args[0]);
                numTrials = Integer.parseInt(args[1]);
                arraySize = Integer.parseInt(args[2]);
                numThreads = Integer.parseInt(args[3]);
                targets = Collections.singletonList(target);
                System.out.println("Mode: CUSTOM TARGET WITH TRIALS, ARRAY SIZE, AND THREADS");
                System.out.printf("Target: %s%n", target);
                System.out.printf("Target magnitude: ~1e%.0f%n", Math.log10(target.doubleValue()));
                System.out.printf("Trials: %d%n", numTrials);
                System.out.printf("Array size: %d%n", arraySize);
                System.out.printf("Threads: %d%n", numThreads);
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid number format in arguments");
                printUsage();
                return;
            }
        } else {
            System.err.println("Error: Invalid number of arguments");
            printUsage();
            return;
        }

        if (targets.isEmpty()) {
            System.err.println("Error: No targets generated or parsed");
            return;
        }

        final int trialsToRun = numTrials; // Make effectively final for lambda
        final int finalArraySize = arraySize; // Make effectively final for lambda

        // Shared experiment configuration (sequential execution to avoid nested threading)
        ExperimentConfig config = new ExperimentConfig(
                finalArraySize,      // arraySize
                10_000,         // maxSteps (increased per request)
                3,              // requiredStableSteps for convergence
                true            // recordTrajectory
        , ExecutionMode.SEQUENTIAL);  // Use SEQUENTIAL temporarily for verification

        // Option B: run tasks in a thread pool sized to available processors
        final int threads = numThreads; 
        System.out.printf("Running %d trials per target using %d threads...%n", trialsToRun, threads);
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
                verifyFactorsByBruteForce(outcome.target, finalArraySize);
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

    /**
     * Print usage information for command-line arguments.
     */
    private static void printUsage() {
        System.err.println("\nUsage:");
        System.err.println("  java FactorizationExperiment              # Default: test ~1e5 semiprime");
        System.err.println("  java FactorizationExperiment <target>     # Test specific target number");
        System.err.println("  java FactorizationExperiment <target> <trials>  # Custom target with trial count");
        System.err.println("  java FactorizationExperiment <target> <trials> <arraySize>  # Custom target, trials, and array size");
        System.err.println("  java FactorizationExperiment <target> <trials> <arraySize> <threads> # Custom target, trials, array size, and threads");
        System.err.println();
        System.err.println("Examples:");
        System.err.println("  java FactorizationExperiment                        # FACT-EXP-001 (default)");
        System.err.println("  java FactorizationExperiment 1000000000000000000    # FACT-EXP-002 (1e18)");
        System.err.println("  java FactorizationExperiment 100039                 # Test specific number");
        System.err.println("  java FactorizationExperiment 1000000000000000091 100  # Custom target and trials");
        System.err.println("  java FactorizationExperiment 1000000000000000091 100 2000  # Custom target, trials, and array size");
        System.err.println("  java FactorizationExperiment 1000000000000000091 100 2000 16 # Custom target, trials, array size, and threads");
    }
}
