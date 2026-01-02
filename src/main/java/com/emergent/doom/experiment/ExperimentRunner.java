package com.emergent.doom.experiment;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.execution.ConvergenceDetector;
import com.emergent.doom.execution.ExecutionEngine;
import com.emergent.doom.execution.ExecutionMode;
import com.emergent.doom.execution.LockBasedExecutionEngine;
import com.emergent.doom.execution.NoSwapConvergence;
import com.emergent.doom.execution.ParallelExecutionEngine;
import com.emergent.doom.execution.SynchronousExecutionEngine;
import com.emergent.doom.metrics.Metric;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;
import com.emergent.doom.probe.ThreadSafeProbe;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.swap.ThreadSafeFrozenCellStatus;
import com.emergent.doom.topology.Topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Orchestrates experiment execution across multiple trials.
 * 
 * <p>The ExperimentRunner manages:
 * <ul>
 *   <li>Trial initialization</li>
 *   <li>Execution engine setup</li>
 *   <li>Metric computation</li>
 *   <li>Result aggregation</li>
 *   <li>Parallel trial execution via ExecutorService (NEW)</li>
 * </ul>
 * </p>
 * 
 * <p><strong>REFACTORED ARCHITECTURE:</strong> Now supports per-trial parallelism
 * where each trial runs in a single thread (using SynchronousExecutionEngine),
 * and multiple trials execute concurrently across CPU cores. This replaces the
 * wasteful per-cell threading model with embarrassingly parallel trial execution.</p>
 * 
 * @param <T> the type of cell
 */
public class ExperimentRunner<T extends Cell<T>> {
    
    private static final Logger logger = Logger.getLogger(ExperimentRunner.class.getName());
    
    private final Supplier<T[]> cellArrayFactory;
    private final Supplier<Topology<T>> topologyFactory;
    private final Map<String, Metric<T>> metrics;
    
    /**
     * IMPLEMENTED: Create an experiment runner
     */
    public ExperimentRunner(
            Supplier<T[]> cellArrayFactory,
            Supplier<Topology<T>> topologyFactory) {
        this.cellArrayFactory = cellArrayFactory;
        this.topologyFactory = topologyFactory;
        this.metrics = new HashMap<>();
    }
    
    /**
     * IMPLEMENTED: Register a metric to compute for each trial
     */
    public void addMetric(String name, Metric<T> metric) {
        metrics.put(name, metric);
    }
    
    /**
     * Execute a single trial using the configured execution mode.
     *
     * <p>REFACTORED: Now defaults to SynchronousExecutionEngine for sequential mode,
     * which is designed for parallel trial execution (no per-cell threading).</p>
     */
    public TrialResult<T> runTrial(ExperimentConfig config, int trialNumber) {
        // Create fresh instances for this trial
        T[] cells = cellArrayFactory.get();

        // Use thread-safe components for parallel/lock-based execution
        boolean needsThreadSafe = config.isParallelExecution() || config.isLockBasedExecution();
        FrozenCellStatus frozenStatus = needsThreadSafe
                ? new ThreadSafeFrozenCellStatus()
                : new FrozenCellStatus();
        SwapEngine<T> swapEngine = new SwapEngine<>(frozenStatus);
        Probe<T> probe = needsThreadSafe ? new ThreadSafeProbe<>() : new Probe<>();
        probe.setRecordingEnabled(config.isRecordTrajectory());
        ConvergenceDetector<T> convergenceDetector =
                new NoSwapConvergence<>(config.getRequiredStableSteps());

        // Run execution based on mode
        long startTime = System.nanoTime();
        int finalStep;
        boolean converged;

        if (config.isParallelExecution()) {
            // Parallel execution: barrier-based synchronization (DEPRECATED - use batch parallelism instead)
            ParallelExecutionEngine<T> parallelEngine = new ParallelExecutionEngine<>(
                    cells, swapEngine, probe, convergenceDetector);
            try {
                parallelEngine.start();
                finalStep = parallelEngine.runUntilConvergence(config.getMaxSteps());
                converged = parallelEngine.hasConverged();
            } finally {
                parallelEngine.shutdown();
            }
        } else if (config.isLockBasedExecution()) {
            // Lock-based execution: matches Python cell_research behavior
            LockBasedExecutionEngine<T> lockEngine = new LockBasedExecutionEngine<>(
                    cells, swapEngine, probe, convergenceDetector);
            try {
                lockEngine.start();
                finalStep = lockEngine.runUntilConvergence(config.getMaxSteps());
                converged = lockEngine.hasConverged();
            } finally {
                lockEngine.shutdown();
            }
        } else {
            // REFACTORED: Use SynchronousExecutionEngine for sequential mode
            // This is the preferred execution mode for parallel trial batches
            SynchronousExecutionEngine<T> syncEngine = new SynchronousExecutionEngine<>(
                    cells, swapEngine, probe, convergenceDetector);
            finalStep = syncEngine.runUntilConvergence(config.getMaxSteps());
            converged = syncEngine.hasConverged();
        }

        long endTime = System.nanoTime();

        // Compute metrics
        Map<String, Double> metricValues = new HashMap<>();
        for (Map.Entry<String, Metric<T>> entry : metrics.entrySet()) {
            metricValues.put(entry.getKey(), entry.getValue().compute(cells));
        }

        // Get trajectory if recorded
        List<StepSnapshot<T>> trajectory = config.isRecordTrajectory()
                ? probe.getSnapshots()
                : null;

        // Create and return result with final cell array
        return new TrialResult<>(
                trialNumber,
                finalStep,
                converged,
                trajectory,
                metricValues,
                endTime - startTime,
                cells  // Store final cell array for domain-specific analysis
        );
    }
    
    /**
     * Execute multiple trials with the same configuration (SEQUENTIAL execution).
     * Trials are executed one after another in a single thread.
     * 
     * <p>For parallel trial execution, use {@link #runBatchExperiments(ExperimentConfig)} instead,
     * which runs trials concurrently across CPU cores.</p>
     * 
     * @param config experiment configuration
     * @param numTrials number of trials to execute
     * @return aggregated results from all trials
     */
    public ExperimentResults<T> runExperiment(ExperimentConfig config, int numTrials) {
        ExperimentResults<T> results = new ExperimentResults<>(config);
        
        for (int i = 0; i < numTrials; i++) {
            logger.info(String.format("Running trial %d/%d", i + 1, numTrials));
            TrialResult<T> trialResult = runTrial(config, i);
            results.addTrialResult(trialResult);
        }
        
        return results;
    }

    /**
     * Run multiple trials in parallel across CPU cores using ExecutorService.
     *
     * <p>PURPOSE: Maximize throughput by running independent trials concurrently.
     * Each trial runs in a single thread (via SynchronousExecutionEngine), and
     * the trials are parallelized across available CPU cores.</p>
     *
     * <p>ARCHITECTURE: This method implements embarrassingly parallel trial execution:
     * <ul>
     *   <li>No shared state between trials</li>
     *   <li>No synchronization needed between trials</li>
     *   <li>Each trial is an independent Callable task</li>
     *   <li>Thread pool size = min(numTrials, availableProcessors)</li>
     * </ul>
     * </p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>config - ExperimentConfig with trial parameters</li>
     *   <li>Must have numRepetitions field set (number of trials to run)</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Determine worker count = min(numRepetitions, CPU cores)</li>
     *   <li>Create ExecutorService with fixed thread pool</li>
     *   <li>Submit all trials as Future tasks</li>
     *   <li>Collect results as they complete (blocking on Future.get())</li>
     *   <li>Log progress every 10 trials</li>
     *   <li>Aggregate results into ExperimentResults</li>
     *   <li>Shutdown executor (in finally block)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: ExperimentResults containing all trial results</p>
     *
     * <p>DEPENDENCIES:
     * <ul>
     *   <li>runTrial() for single trial execution</li>
     *   <li>ExperimentConfig.getNumRepetitions() for trial count</li>
     *   <li>Java ExecutorService for thread pool management</li>
     * </ul>
     * </p>
     *
     * <p>PERFORMANCE COMPARISON:
     * <pre>
     * Old Model (per-cell threads):
     *   100 trials × 1000 cells = 100,000 thread creations
     *   Barrier sync every step × ~2500 steps = 250,000 barriers
     *   Massive lock contention on swap collector
     *
     * New Model (per-trial threads):
     *   100 trials = 100 thread creations (reused from pool)
     *   No barrier sync (single-threaded per trial)
     *   No lock contention (no shared state per trial)
     * </pre>
     * </p>
     *
     * <p>GROUND TRUTH REFERENCE: This implements the architecture specified in
     * issue "Refactor Threading Model: Per-Trial Parallelism Instead of Per-Cell".</p>
     *
     * @param config experiment configuration with numRepetitions set
     * @return aggregated results from all trials
     * @throws IllegalArgumentException if numRepetitions is less than 1
     */
    public ExperimentResults<T> runBatchExperiments(ExperimentConfig config) {
        int numRepetitions = config.getNumRepetitions();
        
        // Validate input
        if (numRepetitions < 1) {
            throw new IllegalArgumentException("numRepetitions must be at least 1, got: " + numRepetitions);
        }
        
        // Determine worker count: min(trials, CPU cores)
        int numWorkers = Math.min(numRepetitions, Runtime.getRuntime().availableProcessors());
        
        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(numWorkers);
        List<Future<TrialResult<T>>> futures = new ArrayList<>();
        
        try {
            // Submit all trials as tasks
            for (int i = 0; i < numRepetitions; i++) {
                final int trialNum = i;
                futures.add(executor.submit(() -> runTrial(config, trialNum)));
            }
            
            // Collect results as they complete
            List<TrialResult<T>> results = new ArrayList<>();
            boolean failureOccurred = false;
            for (int i = 0; i < futures.size(); i++) {
                if (failureOccurred) {
                    futures.get(i).cancel(true);
                    continue;
                }
                try {
                    TrialResult<T> result = futures.get(i).get();
                    results.add(result);
                    
                    // Progress logging every 10 trials
                    if ((i + 1) % 10 == 0) {
                        logger.info(String.format("Completed %d/%d trials", i + 1, numRepetitions));
                    }
                } catch (InterruptedException e) {
                    failureOccurred = true;
                    // Cancel remaining futures
                    for (int j = i + 1; j < futures.size(); j++) {
                        futures.get(j).cancel(true);
                    }
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Trial " + i + " was interrupted", e);
                } catch (ExecutionException e) {
                    failureOccurred = true;
                    // Cancel remaining futures
                    for (int j = i + 1; j < futures.size(); j++) {
                        futures.get(j).cancel(true);
                    }
                    Throwable cause = e.getCause();
                    String errorMessage = String.format(
                        "Trial %d failed due to %s: %s",
                        i,
                        cause != null ? cause.getClass().getSimpleName() : "unknown error",
                        cause != null ? cause.getMessage() : "no details"
                    );
                    throw new RuntimeException(errorMessage, cause != null ? cause : e);
                }
            }
            
            // Aggregate and return results
            return aggregateStatistics(config, results);
            
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                        logger.severe("Executor did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Aggregate statistics from individual trial results.
     *
     * <p>PURPOSE: Combine results from multiple trials into summary statistics
     * (mean, std dev, min, max for each metric).</p>
     *
     * <p>INPUTS: 
     * <ul>
     *   <li>config - experiment configuration</li>
     *   <li>results - list of TrialResult objects</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Create ExperimentResults container</li>
     *   <li>For each trial result:
     *     <ul>
     *       <li>Add to ExperimentResults</li>
     *       <li>Update running statistics (mean, variance, etc.)</li>
     *     </ul>
     *   </li>
     *   <li>Finalize statistics (compute std dev from variance)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: ExperimentResults with aggregated statistics</p>
     *
     * <p>DEPENDENCIES: ExperimentResults for aggregation logic</p>
     *
     * @param config experiment configuration
     * @param results list of trial results
     * @return aggregated experiment results
     */
    private ExperimentResults<T> aggregateStatistics(ExperimentConfig config, List<TrialResult<T>> results) {
        ExperimentResults<T> experimentResults = new ExperimentResults<>(config);
        
        for (TrialResult<T> result : results) {
            experimentResults.addTrialResult(result);
        }
        
        return experimentResults;
    }
}
