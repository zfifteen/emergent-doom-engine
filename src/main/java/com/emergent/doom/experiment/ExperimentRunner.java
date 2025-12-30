package com.emergent.doom.experiment;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.execution.ConvergenceDetector;
import com.emergent.doom.execution.ExecutionEngine;
import com.emergent.doom.execution.NoSwapConvergence;
import com.emergent.doom.metrics.Metric;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.SwapEngine;
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
     * IMPLEMENTED: Execute a single trial
     */
    public TrialResult<T> runTrial(ExperimentConfig config, int trialNumber) {
        // Create fresh instances for this trial
        T[] cells = cellArrayFactory.get();
        FrozenCellStatus frozenStatus = new FrozenCellStatus();
        SwapEngine<T> swapEngine = new SwapEngine<>(frozenStatus);
        Probe<T> probe = new Probe<>();
        probe.setRecordingEnabled(config.isRecordTrajectory());
        ConvergenceDetector<T> convergenceDetector =
                new NoSwapConvergence<>(config.getRequiredStableSteps());

        // Create execution engine (topologies now created internally based on algotype)
        ExecutionEngine<T> engine = new ExecutionEngine<>(
                cells, swapEngine, probe, convergenceDetector);
        
        // Run execution
        long startTime = System.nanoTime();
        int finalStep = engine.runUntilConvergence(config.getMaxSteps());
        long endTime = System.nanoTime();
        
        // Compute metrics
        Map<String, Double> metricValues = new HashMap<>();
        for (Map.Entry<String, Metric<T>> entry : metrics.entrySet()) {
            metricValues.put(entry.getKey(), entry.getValue().compute(cells));
        }
        
        // Get trajectory if recorded
        List<StepSnapshot<T>> trajectory = config.isRecordTrajectory() ? 
                probe.getSnapshots() : null;
        
        // Create and return result
        return new TrialResult<>(
                trialNumber,
                finalStep,
                engine.hasConverged(),
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
