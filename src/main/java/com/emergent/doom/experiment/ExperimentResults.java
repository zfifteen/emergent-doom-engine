package com.emergent.doom.experiment;

import com.emergent.doom.cell.Cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated results from multiple trial executions.
 * 
 * <p>Provides statistical summaries and analysis across trials.</p>
 * 
 * @param <T> the type of cell
 */
public class ExperimentResults<T extends Cell<T>> {
    
    private final List<TrialResult<T>> trials;
    private final ExperimentConfig config;
    
    /**
     * IMPLEMENTED: Create an experiment results container
     */
    public ExperimentResults(ExperimentConfig config) {
        this.config = config;
        this.trials = new ArrayList<>();
    }
    
    /**
     * IMPLEMENTED: Add a trial result to the collection
     */
    public void addTrialResult(TrialResult<T> result) {
        trials.add(result);
    }
    
    /**
     * IMPLEMENTED: Get all trial results
     */
    public List<TrialResult<T>> getTrials() {
        return new ArrayList<>(trials);
    }
    
    /**
     * IMPLEMENTED: Get the experiment configuration
     */
    public ExperimentConfig getConfig() {
        return config;
    }
    
    /**
     * IMPLEMENTED: Compute mean of a specific metric across all trials
     */
    public double getMeanMetric(String metricName) {
        if (trials.isEmpty()) return 0.0;
        
        double sum = 0.0;
        for (TrialResult<T> trial : trials) {
            Double value = trial.getMetric(metricName);
            if (value != null) {
                sum += value;
            }
        }
        return sum / trials.size();
    }
    
    /**
     * IMPLEMENTED: Compute standard deviation of a metric across trials
     */
    public double getStdDevMetric(String metricName) {
        if (trials.size() < 2) return 0.0;
        
        double mean = getMeanMetric(metricName);
        double sumSquaredDiff = 0.0;
        
        for (TrialResult<T> trial : trials) {
            Double value = trial.getMetric(metricName);
            if (value != null) {
                double diff = value - mean;
                sumSquaredDiff += diff * diff;
            }
        }
        
        return Math.sqrt(sumSquaredDiff / trials.size());
    }
    
    /**
     * IMPLEMENTED: Get convergence rate across trials
     */
    public double getConvergenceRate() {
        if (trials.isEmpty()) return 0.0;
        
        long convergedCount = trials.stream()
                .filter(TrialResult::isConverged)
                .count();
        
        return (double) convergedCount / trials.size();
    }
    
    /**
     * IMPLEMENTED: Get mean final step count across trials
     */
    public double getMeanSteps() {
        if (trials.isEmpty()) return 0.0;
        
        double sum = trials.stream()
                .mapToInt(TrialResult::getFinalStep)
                .sum();
        
        return sum / trials.size();
    }
    
    /**
     * IMPLEMENTED: Generate a summary report as a formatted string
     */
    public String getSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Experiment Results Summary\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("Trials:           %d\n", trials.size()));
        sb.append(String.format("Convergence Rate: %.1f%%\n", getConvergenceRate() * 100));
        sb.append(String.format("Mean Steps:       %.2f\n", getMeanSteps()));
        
        // Add metric summaries if any trials have metrics
        if (!trials.isEmpty() && !trials.get(0).getMetrics().isEmpty()) {
            sb.append("\nMetrics:\n");
            for (String metricName : trials.get(0).getMetrics().keySet()) {
                double mean = getMeanMetric(metricName);
                double stdDev = getStdDevMetric(metricName);
                sb.append(String.format("  %s: %.2f ± %.2f\n", metricName, mean, stdDev));
            }
        }
        
        return sb.toString();
    }
}
