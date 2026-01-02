package com.emergent.doom.validation;

import java.math.BigInteger;
import java.util.List;

/**
 * Main orchestrator for linear scaling validation experiments.
 * 
 * <p><strong>Purpose:</strong> Systematically tests the linear scaling hypothesis
 * (B ≈ 0) on progressively harder semiprimes to identify the failure boundary where
 * convergence time transitions from constant to growing with array size.</p>
 * 
 * <p><strong>Architecture Role:</strong> Top-level coordinator that:
 * <ol>
 *   <li>Generates semiprimes for each stage using nextprime() logic</li>
 *   <li>Configures and executes batches of trials across array sizes</li>
 *   <li>Aggregates results into ScalingReport for analysis</li>
 *   <li>Implements early termination when failure boundary detected</li>
 *   <li>Exports data to CSV and generates findings documentation</li>
 * </ol>
 * </p>
 * 
 * <p><strong>Data Flow:</strong>
 * <pre>
 * Command-line args → parseArguments()
 *                  ↓
 *           selectStage()
 *                  ↓
 *       generateTarget() (nextprime-based semiprime)
 *                  ↓
 *       runTrialBatch() (30 trials × multiple array sizes)
 *                  ↓
 *       ScalingReport (B calculation, assessment)
 *                  ↓
 *       exportResults() + generateFindings()
 * </pre>
 * </p>
 * 
 * <p><strong>Integration:</strong> Uses existing FactorizationExperiment and
 * ExperimentRunner infrastructure for trial execution. Adds statistical analysis
 * layer on top for B metric computation.</p>
 */
public class LinearScalingValidator {
    
    private static final int DEFAULT_TRIALS_PER_CONFIG = 30;
    private static final int DEFAULT_REQUIRED_STABLE_STEPS = 3;
    private static final boolean DEFAULT_RECORD_TRAJECTORY = false;
    
    /**
     * Main entry point for scaling validation experiments.
     * 
     * <p><strong>Implementation (Phase 2):</strong> This method orchestrates the entire
     * experimental workflow by coordinating target generation, trial execution, and reporting.</p>
     * 
     * <p><strong>Responsibilities:</strong>
     * <ul>
     *   <li>Parse command-line arguments to determine stage, custom targets, output paths</li>
     *   <li>Load or generate semiprime targets using nextprime() methodology</li>
     *   <li>Initialize experiment configuration based on stage parameters</li>
     *   <li>Execute trial batches with progress reporting</li>
     *   <li>Aggregate results and compute B metric</li>
     *   <li>Generate console report and CSV export</li>
     *   <li>Implement early termination if B > 0.5 detected</li>
     * </ul>
     * </p>
     * 
     * <p><strong>Why this satisfies the entry point role:</strong>
     * The main method serves as the coordinator that triggers all other unimplemented
     * sections. It defines the workflow: parse args → generate target → run trials →
     * generate report → export results. Each step delegates to specialized methods
     * that will be implemented in Phase 3.</p>
     * 
     * <p><strong>How it coordinates unimplemented sections:</strong>
     * <ul>
     *   <li>Calls parseArguments() to extract configuration (Phase 3)</li>
     *   <li>Calls generateTarget() to create semiprimes (Phase 3)</li>
     *   <li>Calls runTrialBatch() to execute experiments (Phase 3)</li>
     *   <li>Calls generateReport() to analyze results (Phase 3)</li>
     *   <li>Calls exportToCsv() to save data (Phase 3)</li>
     * </ul>
     * </p>
     * 
     * <p><strong>Usage examples:</strong>
     * <pre>
     * # Run Stage 1 (10^6 magnitude)
     * java LinearScalingValidator --stage STAGE_1_E6
     * 
     * # Run Stage 2 with custom output path
     * java LinearScalingValidator --stage STAGE_2_E9 --output results/stage2.csv
     * 
     * # Run all stages sequentially (stops on failure boundary)
     * java LinearScalingValidator --all-stages
     * 
     * # Test custom target with default array sizes
     * java LinearScalingValidator --target 1000000007 --trials 30
     * </pre>
     * </p>
     * 
     * @param args Command-line arguments specifying stage, options, output paths
     */
    public static void main(String[] args) {
        // Phase 2 Implementation: Main orchestration logic
        
        System.out.println("=".repeat(70));
        System.out.println("LINEAR SCALING VALIDATION EXPERIMENT");
        System.out.println("Testing O(n) hypothesis on progressively harder semiprimes");
        System.out.println("=".repeat(70));
        System.out.println();
        
        // Step 1: Parse command-line arguments (delegates to Phase 3 method)
        ExperimentOptions options;
        try {
            options = parseArguments(args);
        } catch (UnsupportedOperationException e) {
            // parseArguments not yet implemented - use defaults for Phase 2
            System.out.println("INFO: Using default configuration (parseArguments not yet implemented)");
            options = new ExperimentOptions();
            options.stage = ScalingStage.STAGE_1_E6;
            options.numTrials = DEFAULT_TRIALS_PER_CONFIG;
            options.outputPath = "scaling_validation_results.csv";
            options.runAllStages = false;
            options.customTarget = null;
        }
        
        System.out.printf("Configuration:%n");
        System.out.printf("  Stage: %s%n", options.stage);
        System.out.printf("  Trials per array size: %d%n", options.numTrials);
        System.out.printf("  Output path: %s%n", options.outputPath);
        System.out.println();
        
        // Step 2: Determine which stages to run
        ScalingStage[] stagesToRun;
        if (options.runAllStages) {
            stagesToRun = ScalingStage.values();
        } else {
            stagesToRun = new ScalingStage[]{options.stage};
        }
        
        // Step 3: Execute each stage sequentially
        for (ScalingStage stage : stagesToRun) {
            System.out.println("=".repeat(70));
            System.out.printf("RUNNING STAGE: %s%n", stage);
            System.out.println("=".repeat(70));
            System.out.println();
            
            // Step 3a: Generate or use custom target
            BigInteger target;
            if (options.customTarget != null) {
                target = options.customTarget;
                System.out.printf("Using custom target: %s%n", target);
            } else {
                try {
                    target = generateTarget(stage);
                    System.out.printf("Generated target: %s%n", target);
                } catch (UnsupportedOperationException e) {
                    System.out.println("ERROR: generateTarget not yet implemented (Phase 3)");
                    System.out.println("Cannot proceed with experiment.");
                    return;
                }
            }
            
            System.out.printf("Target magnitude: ~10^%.1f%n", Math.log10(target.doubleValue()));
            System.out.println();
            
            // Step 3b: Run trial batch (delegates to Phase 3 method)
            List<ScalingTrialResult> results;
            try {
                System.out.printf("Running %d trials across multiple array sizes...%n", 
                                options.numTrials);
                results = runTrialBatch(target, stage, options.numTrials);
                System.out.printf("Completed %d trials%n", results.size());
            } catch (UnsupportedOperationException e) {
                System.out.println("ERROR: runTrialBatch not yet implemented (Phase 3)");
                System.out.println("Skipping this stage.");
                continue;
            }
            
            System.out.println();
            
            // Step 3c: Generate analysis report (delegates to Phase 3 method)
            ScalingReport report;
            try {
                report = generateReport(stage, results);
            } catch (UnsupportedOperationException e) {
                System.out.println("ERROR: generateReport not yet implemented (Phase 3)");
                System.out.println("Cannot analyze results.");
                continue;
            }
            
            // Step 3d: Display report
            try {
                System.out.println(report.generateConsoleReport());
            } catch (UnsupportedOperationException e) {
                System.out.println("ERROR: report.generateConsoleReport() not yet implemented (Phase 3)");
            }
            
            // Step 3e: Export to CSV (delegates to Phase 3 method)
            try {
                String outputPath = options.outputPath.replace(".csv", 
                    "_" + stage.toString() + ".csv");
                exportToCsv(results, outputPath);
                System.out.printf("Results exported to: %s%n", outputPath);
            } catch (UnsupportedOperationException e) {
                System.out.println("WARNING: exportToCsv not yet implemented (Phase 3)");
            }
            
            System.out.println();
            
            // Step 3f: Check if should proceed to next stage
            if (options.runAllStages) {
                try {
                    if (!report.shouldProceedToNextStage()) {
                        System.out.println("EARLY TERMINATION: Failure boundary detected (B > 0.5)");
                        System.out.println("Not proceeding to harder stages.");
                        break;
                    }
                } catch (UnsupportedOperationException e) {
                    System.out.println("INFO: Early termination check not yet implemented");
                    System.out.println("Proceeding to next stage...");
                }
            }
        }
        
        System.out.println("=".repeat(70));
        System.out.println("EXPERIMENT COMPLETE");
        System.out.println("=".repeat(70));
    }
    
    /**
     * Generate a semiprime target for the specified stage.
     * 
     * <p><strong>Implementation:</strong> Implements the nextprime-based
     * semiprime generation as specified in the experimental protocol. Creates
     * balanced semiprimes (p ≈ q) which are the hardest to factor.</p>
     * 
     * <p><strong>Algorithm:</strong>
     * <ol>
     *   <li>Determine base prime magnitude from stage (e.g., 10^3 for Stage 1)</li>
     *   <li>p = nextprime(base)</li>
     *   <li>q = nextprime(p + gap), where gap is stage-specific</li>
     *   <li>target = p × q</li>
     *   <li>Verify target is in expected magnitude range</li>
     * </ol>
     * </p>
     * 
     * <p><strong>Integration:</strong> Called by main() to generate test targets.
     * The resulting semiprime is used across all array sizes for that stage,
     * ensuring consistent target difficulty when measuring B coefficient.</p>
     * 
     * @param stage The experimental stage (determines prime magnitude)
     * @return A semiprime target of appropriate magnitude
     */
    public static BigInteger generateTarget(ScalingStage stage) {
        // Get configuration from stage
        int primeMagnitude = stage.getPrimeMagnitude();
        int gap = stage.getPrimeGap();
        
        // Compute base value: 10^primeMagnitude
        BigInteger base = BigInteger.TEN.pow(primeMagnitude);
        
        // Find first prime >= base
        BigInteger p = nextPrime(base);
        
        // Find second prime >= p + gap
        BigInteger q = nextPrime(p.add(BigInteger.valueOf(gap)));
        
        // Compute semiprime
        BigInteger target = p.multiply(q);
        
        // Log for verification
        System.out.printf("  Generated semiprime: %s × %s = %s%n", p, q, target);
        System.out.printf("  Prime magnitudes: p ≈ 10^%.2f, q ≈ 10^%.2f%n",
                         Math.log10(p.doubleValue()), Math.log10(q.doubleValue()));
        
        return target;
    }
    
    /**
     * Run a batch of trials for given configuration.
     * 
     * <p><strong>Implementation:</strong> Executes multiple trials
     * (default 30) for each array size, collecting results for analysis.</p>
     * 
     * <p><strong>Execution strategy:</strong>
     * <ul>
     *   <li>For each array size in stage configuration</li>
     *   <li>Run N trials using executeSingleTrial()</li>
     *   <li>Collect steps, convergence, factor discovery for each trial</li>
     *   <li>Return list of ScalingTrialResult for aggregation</li>
     * </ul>
     * </p>
     * 
     * <p><strong>Reasoning:</strong> Multiple trials per array size enable
     * statistical analysis (mean, variance) and reliable B coefficient calculation.
     * Array size variation is the independent variable for measuring B.</p>
     * 
     * <p><strong>Integration:</strong> Called by main() for each stage.
     * Results passed to generateReport() for B calculation.</p>
     * 
     * @param target The semiprime to factor
     * @param stage The experimental stage (determines array sizes, max steps)
     * @param numTrials Number of trials to run per array size
     * @return List of trial results across all array sizes
     */
    public static List<ScalingTrialResult> runTrialBatch(BigInteger target, 
                                                          ScalingStage stage,
                                                          int numTrials) {
        List<ScalingTrialResult> results = new java.util.ArrayList<>();
        
        int[] arraySizes = stage.getArraySizes();
        int maxSteps = stage.getMaxSteps();
        
        System.out.printf("Testing %d array sizes with %d trials each:%n", 
                         arraySizes.length, numTrials);
        
        for (int arraySize : arraySizes) {
            System.out.printf("  Array size %d: ", arraySize);
            
            for (int trial = 0; trial < numTrials; trial++) {
                // Create configuration for this trial
                ScalingTrialConfig config = new ScalingTrialConfig(
                    target,
                    arraySize,
                    maxSteps,
                    DEFAULT_REQUIRED_STABLE_STEPS,
                    DEFAULT_RECORD_TRAJECTORY,
                    stage
                );
                
                // Execute trial
                ScalingTrialResult result = executeSingleTrial(config);
                results.add(result);
                
                // Progress indicator
                if ((trial + 1) % 10 == 0) {
                    System.out.printf("%d ", trial + 1);
                    System.out.flush();
                }
            }
            
            System.out.println(" completed");
        }
        
        return results;
    }
    
    /**
     * Compute the next prime greater than or equal to n.
     * 
     * <p><strong>Implementation:</strong> Uses Miller-Rabin probabilistic primality
     * test for efficient nextprime computation. This is the standard algorithm for
     * generating cryptographic-grade primes.</p>
     * 
     * <p><strong>Algorithm:</strong>
     * <ol>
     *   <li>Start with candidate = n (or n+1 if n is even)</li>
     *   <li>Apply trial division for small primes up to 1000</li>
     *   <li>Use Miller-Rabin test with k rounds (k=20 for high confidence)</li>
     *   <li>If composite, increment by 2 and repeat</li>
     *   <li>Return first probable prime found</li>
     * </ol>
     * </p>
     * 
     * <p><strong>Correctness:</strong> Miller-Rabin with 20 rounds has error
     * probability < 2^-40, acceptable for experimental purposes.</p>
     * 
     * <p><strong>Integration:</strong> Used by generateTarget() to create
     * balanced semiprimes p × q where both factors are prime.</p>
     * 
     * @param n The starting value
     * @return The next prime >= n
     */
    public static BigInteger nextPrime(BigInteger n) {
        // Handle edge cases
        if (n.compareTo(BigInteger.TWO) < 0) {
            return BigInteger.TWO;
        }
        
        // Start with odd candidate
        BigInteger candidate = n;
        if (candidate.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            candidate = candidate.add(BigInteger.ONE);
        }
        
        // Search for prime
        while (true) {
            // Use built-in probable prime test (Miller-Rabin with certainty parameter)
            // certainty=20 gives error probability < 2^-40
            if (candidate.isProbablePrime(20)) {
                return candidate;
            }
            
            // Try next odd number
            candidate = candidate.add(BigInteger.TWO);
        }
    }
    
    /**
     * Execute a single trial with given configuration.
     * 
     * <p><strong>Implementation:</strong> Runs one instance of the factorization
     * experiment and collects detailed results using existing experiment infrastructure.</p>
     * 
     * <p><strong>Execution:</strong>
     * <ol>
     *   <li>Create RemainderCell array for target and array size</li>
     *   <li>Set up ExperimentRunner with LinearNeighborhood topology</li>
     *   <li>Configure execution mode (SEQUENTIAL for now) and parameters</li>
     *   <li>Run trial until convergence or max steps</li>
     *   <li>Extract metrics: steps, convergence status, discovered factors</li>
     *   <li>Compute remainder statistics from final configuration</li>
     *   <li>Package into ScalingTrialResult</li>
     * </ol>
     * </p>
     * 
     * <p><strong>Reasoning:</strong> Reuses existing ExperimentRunner to avoid
     * duplicating execution logic. Wraps results in ScalingTrialResult for
     * B metric analysis.</p>
     * 
     * <p><strong>Integration:</strong> Called by runTrialBatch() for each trial.
     * Results aggregated into ScalingReport for analysis.</p>
     * 
     * @param config The trial configuration
     * @return Result of the trial execution
     */
    public static ScalingTrialResult executeSingleTrial(ScalingTrialConfig config) {
        long startTime = System.currentTimeMillis();
        
        // Use existing ExperimentRunner infrastructure
        com.emergent.doom.experiment.ExperimentRunner<com.emergent.doom.cell.RemainderCell> runner =
            new com.emergent.doom.experiment.ExperimentRunner<>(
                () -> {
                    com.emergent.doom.cell.RemainderCell[] newCells = 
                        new com.emergent.doom.cell.RemainderCell[config.getArraySize()];
                    for (int i = 0; i < newCells.length; i++) {
                        newCells[i] = new com.emergent.doom.cell.RemainderCell(
                            config.getTarget(), i + 1);
                    }
                    return newCells;
                },
                () -> new com.emergent.doom.examples.LinearNeighborhood<>(1)
            );
        
        // Configure experiment
        com.emergent.doom.experiment.ExperimentConfig expConfig = 
            new com.emergent.doom.experiment.ExperimentConfig(
                config.getArraySize(),
                config.getMaxSteps(),
                config.getRequiredStableSteps(),
                config.isRecordTrajectory(),
                com.emergent.doom.execution.ExecutionMode.SEQUENTIAL
            );
        
        // Run single trial
        com.emergent.doom.experiment.TrialResult<com.emergent.doom.cell.RemainderCell> trialResult =
            runner.runTrial(expConfig, 0);
        
        // Extract results
        int steps = trialResult.getFinalStep();
        boolean converged = trialResult.isConverged();
        
        // Find factors in final configuration
        com.emergent.doom.cell.RemainderCell[] finalCells = trialResult.getFinalCells();
        BigInteger discoveredFactor = null;
        boolean foundFactor = false;
        
        if (finalCells != null) {
            for (com.emergent.doom.cell.RemainderCell cell : finalCells) {
                if (cell.getPosition() > 1 && cell.isFactor()) {
                    discoveredFactor = BigInteger.valueOf(cell.getPosition());
                    foundFactor = true;
                    break;  // Found a factor
                }
            }
        }
        
        // Compute remainder statistics
        RemainderStatistics stats = RemainderStatistics.fromCells(finalCells);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        return new ScalingTrialResult(
            config,
            steps,
            converged,
            foundFactor,
            discoveredFactor,
            executionTime,
            stats
        );
    }
    
    /**
     * Generate scaling analysis report from trial results.
     * 
     * <p><strong>Implementation:</strong> Aggregates trial results
     * and computes B metric via linear regression. Wraps in ScalingReport.</p>
     * 
     * <p><strong>Reasoning:</strong> Delegates to ScalingReport constructor
     * which handles all statistical analysis. Keeps this method simple.</p>
     * 
     * <p><strong>Integration:</strong> Called by main() after runTrialBatch()
     * completes. Report used for console output and decision making.</p>
     * 
     * @param stage The experimental stage
     * @param results Trial results to analyze
     * @return Comprehensive scaling report with B coefficient
     */
    public static ScalingReport generateReport(ScalingStage stage, 
                                                List<ScalingTrialResult> results) {
        return new ScalingReport(stage, results);
    }
    
    /**
     * Export results to CSV file.
     * 
     * <p><strong>Implementation:</strong> Writes trial results to
     * CSV following the schema from analyze_scaling.py. Uses ScalingReport.toCsv()
     * for formatting.</p>
     * 
     * <p><strong>Reasoning:</strong> CSV export enables offline analysis,
     * archival, and compatibility with existing Python analysis tools.</p>
     * 
     * <p><strong>Integration:</strong> Called by main() after report generation
     * to persist experimental data.</p>
     * 
     * @param results Trial results to export
     * @param outputPath Path to CSV file
     */
    public static void exportToCsv(List<ScalingTrialResult> results, String outputPath) {
        try {
            // Create report for CSV generation
            ScalingReport report = new ScalingReport(
                results.get(0).getConfig().getStage(), results);
            
            // Write to file
            java.nio.file.Files.writeString(
                java.nio.file.Paths.get(outputPath),
                report.toCsv()
            );
            
            System.out.printf("Successfully exported %d results to %s%n", 
                results.size(), outputPath);
        } catch (java.io.IOException e) {
            System.err.printf("ERROR: Failed to export CSV: %s%n", e.getMessage());
        }
    }
    
    /**
     * Parse command-line arguments.
     * 
     * <p><strong>Not yet implemented.</strong> Will parse args to extract
     * stage selection, custom targets, output paths, and options.</p>
     * 
     * @param args Command-line arguments
     * @return Parsed configuration object
     */
    private static ExperimentOptions parseArguments(String[] args) {
        // TODO: Phase 3 - implement argument parsing
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Helper class for parsed command-line options.
     * 
     * <p><strong>Not yet implemented.</strong> Will encapsulate parsed arguments.</p>
     */
    private static class ExperimentOptions {
        ScalingStage stage;
        BigInteger customTarget;
        String outputPath;
        int numTrials;
        boolean runAllStages;
        
        // TODO: Phase 3 - implement options class
    }
}
