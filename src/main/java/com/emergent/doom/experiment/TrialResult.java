package com.emergent.doom.experiment;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.StepSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Results from a single trial execution.
 * 
 * <p>Captures outcome metrics, convergence status, and optional trajectory data.</p>
 * 
 * @param <T> the type of cell
 */
public class TrialResult<T extends Cell<T>> {
    
    private final int trialNumber;
    private final int finalStep;
    private final boolean converged;
    private final List<StepSnapshot<T>> trajectory;
    private final Map<String, Double> metrics;
    private final long executionTimeNanos;
    
    /**
     * IMPLEMENTED: Create a trial result record
     */
    public TrialResult(
            int trialNumber,
            int finalStep,
            boolean converged,
            List<StepSnapshot<T>> trajectory,
            Map<String, Double> metrics,
            long executionTimeNanos) {
        this.trialNumber = trialNumber;
        this.finalStep = finalStep;
        this.converged = converged;
        this.trajectory = trajectory != null ? new ArrayList<>(trajectory) : null;
        this.metrics = new HashMap<>(metrics);
        this.executionTimeNanos = executionTimeNanos;
    }
    
    // Getters
    public int getTrialNumber() {
        return trialNumber;
    }
    
    public int getFinalStep() {
        return finalStep;
    }
    
    public boolean isConverged() {
        return converged;
    }
    
    public List<StepSnapshot<T>> getTrajectory() {
        return trajectory != null ? new ArrayList<>(trajectory) : null;
    }
    
    public Map<String, Double> getMetrics() {
        return new HashMap<>(metrics);
    }
    
    public long getExecutionTimeNanos() {
        return executionTimeNanos;
    }
    
    public double getExecutionTimeMillis() {
        return executionTimeNanos / 1_000_000.0;
    }
    
    /**
     * IMPLEMENTED: Get a specific metric value
     */
    public Double getMetric(String metricName) {
        return metrics.get(metricName);
    }
}
