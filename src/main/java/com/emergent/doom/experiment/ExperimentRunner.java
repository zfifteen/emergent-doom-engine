package com.emergent.doom.experiment;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.execution.ConvergenceDetector;
import com.emergent.doom.execution.ExecutionEngine;
import com.emergent.doom.execution.ExecutionMode;
import com.emergent.doom.execution.LockBasedExecutionEngine;
import com.emergent.doom.execution.NoSwapConvergence;
import com.emergent.doom.execution.ParallelExecutionEngine;
import com.emergent.doom.metrics.Metric;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;
import com.emergent.doom.probe.ThreadSafeProbe;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.swap.ThreadSafeFrozenCellStatus;
import com.emergent.doom.topology.Topology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Orchestrates experiment execution across multiple trials.
 * 
 * <p>The ExperimentRunner manages:
 * <ul>
 *   <li>Trial initialization</li>
 *   <li>Execution engine setup</li>
 *   <li>Metric computation</li>
 *   <li>Result aggregation</li>
 * </ul>
 * </p>
 * 
 * @param <T> the type of cell
 */
public class ExperimentRunner<T extends Cell<T>> {
    
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
            // Parallel execution: barrier-based synchronization
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
            // Sequential execution: original behavior
            ExecutionEngine<T> engine = new ExecutionEngine<>(
                    cells, swapEngine, probe, convergenceDetector);
            finalStep = engine.runUntilConvergence(config.getMaxSteps());
            converged = engine.hasConverged();
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

        // Create and return result
        return new TrialResult<>(
                trialNumber,
                finalStep,
                converged,
                trajectory,
                metricValues,
                endTime - startTime
        );
    }
    
    /**
     * IMPLEMENTED: Execute multiple trials with the same configuration
     */
    public ExperimentResults<T> runExperiment(ExperimentConfig config, int numTrials) {
        ExperimentResults<T> results = new ExperimentResults<>(config);
        
        for (int i = 0; i < numTrials; i++) {
            System.out.printf("Running trial %d/%d...%n", i + 1, numTrials);
            TrialResult<T> trialResult = runTrial(config, i);
            results.addTrialResult(trialResult);
        }
        
        return results;
    }
}
