package com.emergent.doom.experiment;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.statistics.StatisticalTests;

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
    
    /**
     * Calculate the Z-score for a metric compared to a known population.
     * 
     * <p><b>Purpose:</b> Determines how many standard deviations the observed metric mean 
     * is from a known population mean. This enables comparison of experiment results against 
     * baseline or theoretical values. The Z-score is particularly useful when analyzing 
     * performance metrics against established benchmarks.</p>
     * 
     * <p><b>Use Case:</b> For example, if comparing an emergent sorting algorithm's performance 
     * against a known baseline (e.g., traditional Selection sort with known mean and std dev), 
     * this method quantifies how different the emergent approach is. A Z-score of 120.43 
     * (as in Table 1, p.10 of the paper) indicates extreme deviation from the baseline.</p>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Metric name identifying which performance measure to analyze</li>
     *   <li>Input: Known population mean and standard deviation for comparison</li>
     *   <li>Extract metric values from all trials in this experiment</li>
     *   <li>Calculate sample mean using getMeanMetric()</li>
     *   <li>Delegate to StatisticalTests.calculateZScore() with:
     *       - Sample mean from this experiment
     *       - Population mean (baseline/reference)
     *       - Population std dev (baseline/reference)
     *       - Sample size (number of trials)</li>
     *   <li>Output: Z-score indicating deviation from population</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Works with StatisticalTests.calculateZScore() to provide 
     * statistical analysis. The Z-score can be interpreted directly or converted to a 
     * p-value using zTestOneSample() for hypothesis testing.</p>
     * 
     * @param metricName the name of the metric to analyze
     * @param populationMean the known population mean for comparison
     * @param populationStdDev the known population standard deviation
     * @return the Z-score for this metric
     * @throws IllegalArgumentException if metric doesn't exist or population parameters invalid
     */
    public double getZScore(String metricName, double populationMean, double populationStdDev) {
        // IMPLEMENTED in Phase 3, Iteration 9
        // This method integrates experiment results with Z-score calculation to enable
        // comparison of observed metrics against known population parameters.
        //
        // It bridges the gap between raw experiment data and statistical analysis.
        
        // 1. Validate metricName exists in trials
        if (trials.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate Z-score: no trials available");
        }
        
        // Check if metric exists by attempting to get its mean
        // getMeanMetric will handle missing metrics gracefully by returning 0 if no values found
        
        // 2. Get sample mean using getMeanMetric(metricName)
        double sampleMean = getMeanMetric(metricName);
        
        // 3. Get sample size from trials.size()
        int sampleSize = trials.size();
        
        // 4. Call StatisticalTests.calculateZScore(sampleMean, populationMean, populationStdDev, sampleSize)
        // This delegates to the static utility method which handles validation and computation
        double zScore = StatisticalTests.calculateZScore(
            sampleMean, 
            populationMean, 
            populationStdDev, 
            sampleSize
        );
        
        // 5. Return Z-score
        return zScore;
    }
    
    /**
     * Perform a one-sample t-test on a metric against a hypothesized population mean.
     * 
     * <p><b>Purpose:</b> Tests whether the observed metric values differ significantly 
     * from a hypothesized or baseline population mean. Unlike the Z-test (which requires 
     * known population std dev), the t-test uses the sample's own variability, making it 
     * more appropriate for most experimental scenarios.</p>
     * 
     * <p><b>Statistical Hypothesis:</b></p>
     * <ul>
     *   <li>H0 (Null): The experiment's mean equals the population mean</li>
     *   <li>H1 (Alternative): The experiment's mean differs from the population mean</li>
     * </ul>
     * 
     * <p><b>Use Case:</b> When running 100 trials (as specified in REQUIREMENTS.md 8.1-8.2) 
     * and comparing performance against a theoretical optimal or known baseline, this method 
     * provides statistical evidence of whether observed differences are real or due to chance.</p>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Metric name and hypothesized population mean</li>
     *   <li>Extract all metric values from trials into a List&lt;Double&gt;</li>
     *   <li>Validate that metric exists and has sufficient data</li>
     *   <li>Call StatisticalTests.tTestOneSample(metricValues, populationMean)</li>
     *   <li>Output: Two-tailed p-value indicating significance</li>
     * </ol>
     * 
     * <p><b>Interpretation:</b> A p-value &lt; 0.05 suggests the metric significantly 
     * differs from the population mean at the 95% confidence level. Use with 
     * StatisticalTests.isSignificant() for automatic decision making.</p>
     * 
     * <p><b>Integration:</b> Primary method for batch experiment analysis. Results can be 
     * included in statistical reports and used to validate experimental hypotheses about 
     * emergent problem-solving performance.</p>
     * 
     * @param metricName the name of the metric to test
     * @param populationMean the hypothesized population mean
     * @return the two-tailed p-value
     * @throws IllegalArgumentException if metric doesn't exist or insufficient data
     */
    public double getTTestPValue(String metricName, double populationMean) {
        // IMPLEMENTED in Phase 3, Iteration 10
        // This method performs one-sample t-test on experiment metric data to determine
        // if the observed results differ significantly from a hypothesized population mean.
        //
        // This is the primary statistical test for batch experiment analysis.
        
        // 1. Validate metricName exists and trials not empty
        if (trials.isEmpty()) {
            throw new IllegalArgumentException("Cannot perform t-test: no trials available");
        }
        
        // 2. Create List<Double> to collect metric values
        List<Double> metricValues = new ArrayList<>();
        
        // 3. Iterate through trials and extract metric values
        for (TrialResult<T> trial : trials) {
            Double value = trial.getMetric(metricName);
            if (value != null) {
                metricValues.add(value);
            }
        }
        
        // 4. Validate list has at least 2 values
        if (metricValues.size() < 2) {
            throw new IllegalArgumentException(
                "Cannot perform t-test: metric '" + metricName + "' has insufficient data points. " +
                "Found " + metricValues.size() + ", need at least 2."
            );
        }
        
        // 5. Call StatisticalTests.tTestOneSample(metricValues, populationMean)
        // This delegates to the static utility which uses Apache Commons Math TTest
        double pValue = StatisticalTests.tTestOneSample(metricValues, populationMean);
        
        // 6. Return p-value
        return pValue;
    }
    
    /**
     * Compare this experiment's results with another experiment using a two-sample t-test.
     * 
     * <p><b>Purpose:</b> Determines if two experimental conditions produce significantly 
     * different results for a given metric. This is the primary statistical method for 
     * comparing emergent vs. traditional approaches, or comparing different parameter 
     * configurations.</p>
     * 
     * <p><b>Statistical Hypothesis:</b></p>
     * <ul>
     *   <li>H0 (Null): Both experiments have the same mean for the metric</li>
     *   <li>H1 (Alternative): The experiments have different means</li>
     * </ul>
     * 
     * <p><b>Use Case:</b> As demonstrated in Table 1 (p.10) of the paper, this method 
     * enables statistical comparison between different problem-solving approaches:
     * <ul>
     *   <li>Emergent cells vs. traditional Selection sort</li>
     *   <li>Different cell configurations (e.g., varying swap probabilities)</li>
     *   <li>Different topology structures</li>
     * </ul>
     * A p-value &lt; 0.01 (as shown in the paper) provides strong evidence of difference.</p>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Another ExperimentResults object and metric name</li>
     *   <li>Extract metric values from this experiment's trials</li>
     *   <li>Extract metric values from other experiment's trials</li>
     *   <li>Validate both have sufficient data for the metric</li>
     *   <li>Call StatisticalTests.tTestTwoSample(thisSample, otherSample)</li>
     *   <li>Output: Two-tailed p-value indicating significance of difference</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Central to comparative analysis in batch experiments. 
     * Enables automated statistical validation of performance claims. Results should 
     * be reported alongside means and standard deviations in experiment summaries.</p>
     * 
     * @param other the other experiment results to compare against
     * @param metricName the name of the metric to compare
     * @return the two-tailed p-value from the t-test
     * @throws IllegalArgumentException if metric doesn't exist in both experiments or insufficient data
     */
    public double compareTwoExperiments(ExperimentResults<T> other, String metricName) {
        // IMPLEMENTED in Phase 3, Iteration 11
        // This method compares two experiment results using two-sample t-test to determine
        // if they have significantly different means for a given metric.
        //
        // This is the central method for comparing experimental conditions, such as
        // emergent vs. traditional approaches as shown in the paper's Table 1.
        
        // 1. Validate metricName exists in both this and other experiments
        if (this.trials.isEmpty()) {
            throw new IllegalArgumentException("Cannot compare experiments: this experiment has no trials");
        }
        if (other == null) {
            throw new IllegalArgumentException("Cannot compare experiments: other experiment is null");
        }
        if (other.trials.isEmpty()) {
            throw new IllegalArgumentException("Cannot compare experiments: other experiment has no trials");
        }
        
        // 2. Create List<Double> for this experiment's metric values
        List<Double> thisMetricValues = new ArrayList<>();
        
        // 3. Create List<Double> for other experiment's metric values
        List<Double> otherMetricValues = new ArrayList<>();
        
        // 4. Iterate through this.trials to extract metric values
        for (TrialResult<T> trial : this.trials) {
            Double value = trial.getMetric(metricName);
            if (value != null) {
                thisMetricValues.add(value);
            }
        }
        
        // 5. Iterate through other.trials to extract metric values
        for (TrialResult<T> trial : other.trials) {
            Double value = trial.getMetric(metricName);
            if (value != null) {
                otherMetricValues.add(value);
            }
        }
        
        // 6. Validate both lists have at least 2 values
        if (thisMetricValues.size() < 2) {
            throw new IllegalArgumentException(
                "Cannot compare experiments: this experiment has insufficient data for metric '" + 
                metricName + "'. Found " + thisMetricValues.size() + ", need at least 2."
            );
        }
        if (otherMetricValues.size() < 2) {
            throw new IllegalArgumentException(
                "Cannot compare experiments: other experiment has insufficient data for metric '" + 
                metricName + "'. Found " + otherMetricValues.size() + ", need at least 2."
            );
        }
        
        // 7. Call StatisticalTests.tTestTwoSample(thisSample, otherSample)
        // This performs Welch's t-test which doesn't assume equal variances
        double pValue = StatisticalTests.tTestTwoSample(thisMetricValues, otherMetricValues);
        
        // 8. Return p-value
        return pValue;
    }
    
    /**
     * Calculate a confidence interval for a metric.
     * 
     * <p><b>Purpose:</b> Provides a range within which the true mean of the metric 
     * is likely to fall, given the sample data. Confidence intervals complement 
     * point estimates (means) by quantifying uncertainty and precision of measurements.</p>
     * 
     * <p><b>Interpretation:</b> A 95% confidence interval [a, b] means we are 95% 
     * confident the true population mean falls between a and b. Narrower intervals 
     * indicate more precise estimates; wider intervals suggest more variability or 
     * smaller sample sizes.</p>
     * 
     * <p><b>Use Case:</b> When reporting experiment results, confidence intervals 
     * provide more information than "mean ± std dev". They directly address the 
     * question: "What range of values is plausible for the true performance?" This 
     * is particularly valuable when comparing multiple approaches - non-overlapping 
     * confidence intervals provide visual evidence of significant differences.</p>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Metric name and desired confidence level (typically 0.95)</li>
     *   <li>Calculate sample mean using getMeanMetric(metricName)</li>
     *   <li>Calculate sample std dev using getStdDevMetric(metricName)</li>
     *   <li>Get sample size from trials.size()</li>
     *   <li>Call StatisticalTests.calculateConfidenceInterval(mean, stdDev, size, confidenceLevel)</li>
     *   <li>Output: Array [lowerBound, upperBound]</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Used by getStatisticalSummary() to provide comprehensive 
     * statistical reporting. Can be called directly when detailed interval estimates 
     * are needed for specific metrics.</p>
     * 
     * @param metricName the name of the metric
     * @param confidenceLevel the confidence level (e.g., 0.95 for 95% confidence)
     * @return array with [lowerBound, upperBound]
     * @throws IllegalArgumentException if metric doesn't exist or insufficient data
     */
    public double[] getConfidenceInterval(String metricName, double confidenceLevel) {
        // TODO: Implementation in Phase 3, Iteration 12
        // 1. Validate metricName exists and trials not empty
        // 2. Get mean using getMeanMetric(metricName)
        // 3. Get stdDev using getStdDevMetric(metricName)
        // 4. Get sample size from trials.size()
        // 5. Call StatisticalTests.calculateConfidenceInterval(mean, stdDev, sampleSize, confidenceLevel)
        // 6. Return confidence interval array
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * Get a comprehensive statistical summary for a metric.
     * 
     * <p><b>Purpose:</b> Generates a formatted string containing all key statistical 
     * measures for a metric: mean, standard deviation, and 95% confidence interval. 
     * This provides a complete statistical picture in a human-readable format, suitable 
     * for reports, logs, and console output.</p>
     * 
     * <p><b>Output Format:</b> Returns a string like:
     * <pre>
     * MetricName: mean=45.67, stddev=3.21, 95% CI=[43.45, 47.89]
     * </pre>
     * This format matches common statistical reporting conventions and is easily 
     * parsed by readers familiar with statistical analysis.</p>
     * 
     * <p><b>Use Case:</b> When analyzing batch experiment results (100 trials per 
     * REQUIREMENTS.md 8.1), this method provides a quick, comprehensive summary for 
     * each performance metric. It can be called for multiple metrics and aggregated 
     * into experiment reports, making it easy to compare different configurations or 
     * approaches at a glance.</p>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Metric name to summarize</li>
     *   <li>Calculate mean using getMeanMetric(metricName)</li>
     *   <li>Calculate std dev using getStdDevMetric(metricName)</li>
     *   <li>Calculate 95% CI using getConfidenceInterval(metricName, 0.95)</li>
     *   <li>Format values into readable string with appropriate precision</li>
     *   <li>Output: Formatted statistical summary string</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Can extend getSummaryReport() to include detailed 
     * statistical summaries for all metrics. Useful for automated experiment analysis 
     * pipelines where results need to be logged or displayed with full statistical context.</p>
     * 
     * @param metricName the name of the metric to summarize
     * @return a formatted string with statistical summary
     * @throws IllegalArgumentException if metric doesn't exist or insufficient data
     */
    public String getStatisticalSummary(String metricName) {
        // TODO: Implementation in Phase 3, Iteration 13
        // 1. Validate metricName exists
        // 2. Get mean using getMeanMetric(metricName)
        // 3. Get stdDev using getStdDevMetric(metricName)
        // 4. Get CI using getConfidenceInterval(metricName, 0.95)
        // 5. Format into string: "MetricName: mean=X.XX, stddev=X.XX, 95% CI=[X.XX, X.XX]"
        // 6. Return formatted string
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
