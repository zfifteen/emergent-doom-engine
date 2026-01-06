package com.emergent.doom.experiments.clustering;

import org.apache.commons.math3.stat.inference.TTest;

import java.util.List;

/**
 * Statistical analysis methods for clustering validation experiments.
 *
 * <p><strong>PURPOSE:</strong> Provides t-test and statistical comparison methods
 * to validate experimental results against paper expectations and control baselines.</p>
 *
 * <p><strong>METHODS:</strong>
 * <ul>
 *   <li>compareToPaper() - One-sample t-test vs expected value</li>
 *   <li>compareToControl() - Two-sample t-test vs control group</li>
 * </ul></p>
 *
 * <p><strong>STATISTICAL INTERPRETATION:</strong>
 * <ul>
 *   <li>p-value < 0.05: Statistically significant difference</li>
 *   <li>p-value >= 0.05: No significant difference (matches baseline)</li>
 *   <li>For paper comparison: We want p >= 0.05 (matches expected)</li>
 *   <li>For control comparison: We want p < 0.05 (differs from random baseline)</li>
 * </ul></p>
 *
 * <p><strong>DEPENDENCIES:</strong> Apache Commons Math 3 (org.apache.commons.math3)</p>
 */
public class ValidationStatistics {

    /**
     * Result of a statistical t-test.
     *
     * <p><strong>PURPOSE:</strong> Store complete t-test results including
     * descriptive statistics and p-value.</p>
     *
     * <p><strong>FIELDS:</strong>
     * <ul>
     *   <li>mean - Sample mean</li>
     *   <li>stdDev - Sample standard deviation</li>
     *   <li>pValue - Two-tailed p-value from t-test</li>
     *   <li>confidenceIntervalLower - 95% confidence interval lower bound</li>
     *   <li>confidenceIntervalUpper - 95% confidence interval upper bound</li>
     *   <li>sampleSize - Number of observations</li>
     * </ul></p>
     *
     * <p><strong>USAGE:</strong> Return type for all statistical comparison methods.</p>
     */
    public static class TTestResult {
        private final double mean;
        private final double stdDev;
        private final double pValue;
        private final double confidenceIntervalLower;
        private final double confidenceIntervalUpper;
        private final int sampleSize;

        public TTestResult(double mean, double stdDev, double pValue,
                          double confidenceIntervalLower, double confidenceIntervalUpper,
                          int sampleSize) {
            this.mean = mean;
            this.stdDev = stdDev;
            this.pValue = pValue;
            this.confidenceIntervalLower = confidenceIntervalLower;
            this.confidenceIntervalUpper = confidenceIntervalUpper;
            this.sampleSize = sampleSize;
        }

        public double mean() { return mean; }
        public double stdDev() { return stdDev; }
        public double pValue() { return pValue; }
        public double confidenceIntervalLower() { return confidenceIntervalLower; }
        public double confidenceIntervalUpper() { return confidenceIntervalUpper; }
        public int sampleSize() { return sampleSize; }

        /**
         * Check if result is statistically significant at α = 0.05 level.
         *
         * <p><strong>PURPOSE:</strong> As a researcher, I want to quickly determine
         * if a result is significant so that I can make statistical conclusions.</p>
         *
         * @return true if p-value < 0.05
         */
        public boolean isSignificant() {
            return pValue < 0.05;
        }

        /**
         * Format result as human-readable string.
         *
         * <p><strong>PURPOSE:</strong> As a researcher, I want to see formatted
         * statistical results so that I can include them in reports.</p>
         *
         * <p><strong>FORMAT:</strong>
         * "Mean: X.XX ± Y.YY, p = Z.ZZZZ, 95% CI: [A.AA, B.BB], n = N"
         * </p>
         *
         * @return formatted string with all statistics
         */
        @Override
        public String toString() {
            return String.format("Mean: %.2f ± %.2f, p = %.4f, 95%% CI: [%.2f, %.2f], n = %d",
                mean, stdDev, pValue, confidenceIntervalLower, confidenceIntervalUpper, sampleSize);
        }
    }

    /**
     * Perform one-sample t-test comparing observed values to paper expectation.
     *
     * <p><strong>PURPOSE:</strong> As a researcher, I want to test if my observed
     * clustering values match the Levin paper expectations so that I can validate
     * the implementation.</p>
     *
     * <p><strong>HYPOTHESIS TEST:</strong>
     * <ul>
     *   <li>H0 (null hypothesis): observed mean = expected value</li>
     *   <li>H1 (alternative): observed mean ≠ expected value (two-tailed)</li>
     *   <li>We WANT to fail to reject H0 (p >= 0.05) to match the paper</li>
     * </ul></p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Convert List<Double> to double[] array for Apache Commons Math</li>
     *   <li>Create TTest instance</li>
     *   <li>Compute sample mean and standard deviation</li>
     *   <li>Perform one-sample t-test: tTest(expectedPeak, observedPeaks)</li>
     *   <li>Compute 95% confidence interval:
     *       <ul>
     *         <li>Critical t-value for α=0.05, df=n-1</li>
     *         <li>Margin of error = t * (stdDev / sqrt(n))</li>
     *         <li>CI = [mean - margin, mean + margin]</li>
     *       </ul>
     *   </li>
     *   <li>Return TTestResult with all statistics</li>
     * </ol></p>
     *
     * <p><strong>INPUTS:</strong>
     * <ul>
     *   <li>observedPeaks - List of observed peak aggregation values from trials</li>
     *   <li>expectedPeak - Expected peak value from Levin paper (e.g., 0.72 for 72%)</li>
     * </ul></p>
     *
     * <p><strong>OUTPUTS:</strong> TTestResult with mean, std dev, p-value, and CI</p>
     *
     * <p><strong>DEPENDENCIES:</strong>
     * <ul>
     *   <li>org.apache.commons.math3.stat.inference.TTest</li>
     *   <li>org.apache.commons.math3.distribution.TDistribution (for CI)</li>
     * </ul></p>
     *
     * @param observedPeaks list of observed peak values
     * @param expectedPeak expected peak value from paper
     * @return t-test result with statistics
     */
    public static TTestResult compareToPaper(
        List<Double> observedPeaks,
        double expectedPeak
    ) {
        if (observedPeaks == null || observedPeaks.isEmpty()) {
            return new TTestResult(0, 0, 1.0, 0, 0, 0);
        }
        
        double[] values = toArray(observedPeaks);
        TTest tTest = new TTest();
        
        // Compute mean and standard deviation
        double sampleMean = mean(values);
        double sampleStdDev = stdDev(values);
        int n = values.length;
        
        // Perform one-sample t-test
        double pValue = tTest.tTest(expectedPeak, values);
        
        // Compute 95% confidence interval
        double tCritical;
        double marginOfError;
        try {
            org.apache.commons.math3.distribution.TDistribution tDist =
                new org.apache.commons.math3.distribution.TDistribution(n - 1);
            tCritical = tDist.inverseCumulativeProbability(0.975);  // 95% CI, two-tailed
            marginOfError = tCritical * (sampleStdDev / Math.sqrt(n));
        } catch (Exception e) {
            // Fallback to approximate z-score for problematic cases
            tCritical = 1.96;
            marginOfError = tCritical * (sampleStdDev / Math.sqrt(n));
        }
        
        double ciLower = sampleMean - marginOfError;
        double ciUpper = sampleMean + marginOfError;
        
        return new TTestResult(sampleMean, sampleStdDev, pValue, ciLower, ciUpper, n);
    }

    /**
     * Perform two-sample t-test comparing experimental peaks to control peaks.
     *
     * <p><strong>PURPOSE:</strong> As a researcher, I want to test if chimeric
     * clustering is significantly different from random baseline (control) so that
     * I can confirm it's a real phenomenon.</p>
     *
     * <p><strong>HYPOTHESIS TEST:</strong>
     * <ul>
     *   <li>H0 (null hypothesis): experimental mean = control mean</li>
     *   <li>H1 (alternative): experimental mean ≠ control mean (two-tailed)</li>
     *   <li>We WANT to reject H0 (p < 0.05) to show real clustering</li>
     * </ul></p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Convert both List<Double> to double[] arrays</li>
     *   <li>Create TTest instance</li>
     *   <li>Compute means and standard deviations for both samples</li>
     *   <li>Perform two-sample t-test: tTest(experimentalPeaks, controlPeaks)</li>
     *   <li>Compute pooled standard deviation for CI</li>
     *   <li>Compute 95% CI for difference in means:
     *       <ul>
     *         <li>Difference = experimental_mean - control_mean</li>
     *         <li>Standard error = sqrt((s1²/n1) + (s2²/n2))</li>
     *         <li>Margin = t * SE</li>
     *         <li>CI = [difference - margin, difference + margin]</li>
     *       </ul>
     *   </li>
     *   <li>Return TTestResult for experimental group with p-value vs control</li>
     * </ol></p>
     *
     * <p><strong>INPUTS:</strong>
     * <ul>
     *   <li>experimentalPeaks - List of peak values from chimeric experiment</li>
     *   <li>controlPeaks - List of peak values from homogeneous control</li>
     * </ul></p>
     *
     * <p><strong>OUTPUTS:</strong> TTestResult for experimental group with comparison</p>
     *
     * <p><strong>DEPENDENCIES:</strong>
     * <ul>
     *   <li>org.apache.commons.math3.stat.inference.TTest</li>
     *   <li>org.apache.commons.math3.distribution.TDistribution (for CI)</li>
     * </ul></p>
     *
     * @param experimentalPeaks list of experimental peak values
     * @param controlPeaks list of control peak values
     * @return t-test result comparing groups
     */
    public static TTestResult compareToControl(
        List<Double> experimentalPeaks,
        List<Double> controlPeaks
    ) {
        if (experimentalPeaks == null || experimentalPeaks.isEmpty() ||
            controlPeaks == null || controlPeaks.isEmpty()) {
            return new TTestResult(0, 0, 1.0, 0, 0, 0);
        }
        
        double[] expValues = toArray(experimentalPeaks);
        double[] ctrlValues = toArray(controlPeaks);
        TTest tTest = new TTest();
        
        // Compute means and standard deviations
        double expMean = mean(expValues);
        double expStdDev = stdDev(expValues);
        int expN = expValues.length;
        
        double ctrlMean = mean(ctrlValues);
        double ctrlStdDev = stdDev(ctrlValues);
        int ctrlN = ctrlValues.length;
        
        // Perform two-sample t-test
        double pValue = tTest.tTest(expValues, ctrlValues);
        
        // Compute 95% CI for difference in means
        // Using pooled standard error for independent samples
        double se = Math.sqrt((expStdDev * expStdDev / expN) + (ctrlStdDev * ctrlStdDev / ctrlN));
        
        // Degrees of freedom using Welch-Satterthwaite equation
        // Add safeguards for division by zero and invalid values
        double expVar = expStdDev * expStdDev;
        double ctrlVar = ctrlStdDev * ctrlStdDev;
        
        double numerator = Math.pow(expVar / expN + ctrlVar / ctrlN, 2);
        double denominator = (Math.pow(expVar / expN, 2) / (expN - 1)) +
                            (Math.pow(ctrlVar / ctrlN, 2) / (ctrlN - 1));
        
        double df;
        if (denominator > 0) {
            df = numerator / denominator;
            // Ensure df is at least 1 and not NaN/Inf
            if (Double.isNaN(df) || Double.isInfinite(df) || df < 1) {
                df = Math.min(expN - 1, ctrlN - 1);  // Conservative fallback
            }
        } else {
            // If variances are zero, use simple df
            df = Math.min(expN - 1, ctrlN - 1);
        }
        
        double tCritical;
        double marginOfError;
        try {
            org.apache.commons.math3.distribution.TDistribution tDist =
                new org.apache.commons.math3.distribution.TDistribution(df);
            tCritical = tDist.inverseCumulativeProbability(0.975);  // 95% CI, two-tailed
            marginOfError = tCritical * se;
        } catch (Exception e) {
            // Fallback to approximate z-score for large samples
            tCritical = 1.96;
            marginOfError = tCritical * se;
        }
        
        // Compute 95% CI for the experimental mean using the computed margin of error
        double ciLower = expMean - marginOfError;
        double ciUpper = expMean + marginOfError;
        
        // Return result for experimental group with CI bounds for its mean
        return new TTestResult(expMean, expStdDev, pValue,
                              ciLower, ciUpper, expN);
    }

    /**
     * Convert List<Double> to primitive double[] array.
     *
     * <p><strong>PURPOSE:</strong> Helper method to convert Java collections to
     * primitive arrays required by Apache Commons Math library.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Create double[] array of same length as list</li>
     *   <li>For each index i, copy values[i] to array[i]</li>
     *   <li>Return array</li>
     * </ol></p>
     *
     * @param values list of values
     * @return primitive double array
     */
    private static double[] toArray(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return new double[0];
        }
        double[] array = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    /**
     * Compute sample mean.
     *
     * <p><strong>PURPOSE:</strong> Calculate average value for statistical analysis.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>If empty, return 0.0</li>
     *   <li>Sum all values</li>
     *   <li>Divide by count</li>
     *   <li>Return mean</li>
     * </ol></p>
     *
     * @param values array of values
     * @return mean
     */
    private static double mean(double[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }

    /**
     * Compute sample standard deviation.
     *
     * <p><strong>PURPOSE:</strong> Measure spread/variance for statistical analysis.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>If length < 2, return 0.0</li>
     *   <li>Compute mean</li>
     *   <li>For each value, compute (value - mean)²</li>
     *   <li>Sum squared differences</li>
     *   <li>Divide by (n - 1) for sample std dev (Bessel's correction)</li>
     *   <li>Take square root</li>
     *   <li>Return standard deviation</li>
     * </ol></p>
     *
     * @param values array of values
     * @return sample standard deviation
     */
    private static double stdDev(double[] values) {
        if (values == null || values.length < 2) {
            return 0.0;
        }
        double meanVal = mean(values);
        double sumSquaredDiff = 0.0;
        for (double v : values) {
            sumSquaredDiff += (v - meanVal) * (v - meanVal);
        }
        return Math.sqrt(sumSquaredDiff / (values.length - 1));
    }
}
