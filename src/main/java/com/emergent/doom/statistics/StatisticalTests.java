package com.emergent.doom.statistics;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.inference.TTest;

import java.util.List;

/**
 * Statistical testing utilities for hypothesis testing and significance analysis.
 * 
 * <p>This class provides static methods for performing Z-tests and T-tests, 
 * which are fundamental statistical techniques used to compare sample means 
 * and determine if observed differences are statistically significant or due 
 * to random chance.</p>
 * 
 * <p>These utilities support the batch experiment analysis described in the 
 * paper (Table 1, p.10), enabling calculation of Z-scores like 120.43 for 
 * Selection sort comparison and p-values like &lt;0.01.</p>
 * 
 * <p><b>Key Statistical Concepts:</b></p>
 * <ul>
 *   <li><b>Z-Test:</b> Used when population variance is known or sample size is large (&gt;30)</li>
 *   <li><b>T-Test:</b> Used when population variance is unknown and sample size is small</li>
 *   <li><b>P-Value:</b> Probability of observing the test statistic under the null hypothesis</li>
 *   <li><b>Confidence Interval:</b> Range within which the true population parameter likely falls</li>
 * </ul>
 * 
 * @see org.apache.commons.math3.stat.inference.TTest
 * @see org.apache.commons.math3.distribution.NormalDistribution
 */
public class StatisticalTests {
    
    /**
     * Calculate the Z-score for a sample mean compared to a population mean.
     * 
     * <p><b>Purpose:</b> The Z-score (also called standard score) measures how many 
     * standard deviations a sample mean is from the population mean. It's the foundation 
     * for Z-tests and is used to determine statistical significance.</p>
     * 
     * <p><b>Formula:</b> Z = (sampleMean - populationMean) / (populationStdDev / sqrt(sampleSize))</p>
     * 
     * <p><b>Interpretation:</b></p>
     * <ul>
     *   <li>Z-score of 0: Sample mean equals population mean</li>
     *   <li>Positive Z-score: Sample mean is above population mean</li>
     *   <li>Negative Z-score: Sample mean is below population mean</li>
     *   <li>|Z| &gt; 1.96: Significant at 0.05 level (two-tailed)</li>
     *   <li>|Z| &gt; 2.58: Significant at 0.01 level (two-tailed)</li>
     * </ul>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Sample statistics (mean, size) and population parameters (mean, std dev)</li>
     *   <li>Compute standard error: populationStdDev / sqrt(sampleSize)</li>
     *   <li>Compute Z-score: (sampleMean - populationMean) / standardError</li>
     *   <li>Output: Z-score value used for hypothesis testing</li>
     * </ol>
     * 
     * <p><b>Integration:</b> This method is called by zTestOneSample() to compute 
     * the test statistic before calculating the p-value. It can also be used directly 
     * when only the Z-score is needed for comparison purposes.</p>
     * 
     * @param sampleMean the mean of the sample
     * @param populationMean the mean of the population
     * @param populationStdDev the standard deviation of the population
     * @param sampleSize the size of the sample
     * @return the Z-score
     * @throws IllegalArgumentException if populationStdDev &lt;= 0 or sampleSize &lt;= 0
     */
    public static double calculateZScore(double sampleMean, double populationMean, 
                                        double populationStdDev, int sampleSize) {
        // IMPLEMENTED in Phase 3, Iteration 1
        // This method calculates the Z-score (standard score) which measures how many
        // standard deviations a sample mean is from the population mean.
        //
        // The Z-score is fundamental to Z-tests and provides a standardized way to
        // compare sample statistics against known population parameters.
        
        // 1. Validate inputs: check populationStdDev > 0 and sampleSize > 0
        if (populationStdDev <= 0) {
            throw new IllegalArgumentException("Population standard deviation must be positive, got: " + populationStdDev);
        }
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive, got: " + sampleSize);
        }
        
        // 2. Calculate standard error: SE = populationStdDev / sqrt(sampleSize)
        // Standard error represents the standard deviation of the sampling distribution
        double standardError = populationStdDev / Math.sqrt(sampleSize);
        
        // 3. Calculate Z-score: Z = (sampleMean - populationMean) / SE
        // Positive Z-score means sample mean is above population mean
        // Negative Z-score means sample mean is below population mean
        double zScore = (sampleMean - populationMean) / standardError;
        
        // 4. Return Z-score
        return zScore;
    }
    
    /**
     * Perform a one-sample Z-test to determine if a sample mean differs significantly 
     * from a known population mean.
     * 
     * <p><b>Purpose:</b> Tests the null hypothesis that the sample comes from a population 
     * with the specified mean. This is used when the population standard deviation is known 
     * and the sample size is sufficiently large (typically n &gt; 30).</p>
     * 
     * <p><b>Null Hypothesis (H0):</b> Sample mean equals population mean</p>
     * <p><b>Alternative Hypothesis (H1):</b> Sample mean differs from population mean (two-tailed)</p>
     * 
     * <p><b>Statistical Method:</b></p>
     * <ul>
     *   <li>Compute Z-score using calculateZScore()</li>
     *   <li>Use standard normal distribution to find two-tailed p-value</li>
     *   <li>p-value = 2 * P(Z &gt; |z|) for two-tailed test</li>
     * </ul>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Sample statistics and population parameters</li>
     *   <li>Calculate Z-score via calculateZScore()</li>
     *   <li>Create standard normal distribution (mean=0, stddev=1)</li>
     *   <li>Calculate cumulative probability for |Z|</li>
     *   <li>Compute two-tailed p-value: 2 * (1 - cumulativeProbability)</li>
     *   <li>Output: p-value indicating significance level</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Used by ExperimentResults.getZScore() to perform 
     * statistical comparison of experiment metrics against known population values. 
     * The p-value can then be evaluated with isSignificant() to determine if the 
     * difference is statistically meaningful.</p>
     * 
     * @param sampleMean the mean of the sample
     * @param populationMean the mean of the population
     * @param populationStdDev the standard deviation of the population
     * @param sampleSize the size of the sample
     * @return the two-tailed p-value
     * @throws IllegalArgumentException if inputs are invalid
     */
    public static double zTestOneSample(double sampleMean, double populationMean, 
                                       double populationStdDev, int sampleSize) {
        // IMPLEMENTED in Phase 3, Iteration 2
        // This method performs a one-sample Z-test to determine if a sample mean
        // differs significantly from a known population mean.
        //
        // The test uses the standard normal distribution and returns a two-tailed p-value
        // that indicates the probability of observing the data under the null hypothesis.
        
        // 1. Calculate Z-score using calculateZScore()
        // This demonstrates integration with the previously implemented method
        double zScore = calculateZScore(sampleMean, populationMean, populationStdDev, sampleSize);
        
        // 2. Create NormalDistribution(0, 1) - standard normal distribution
        // Mean = 0, Standard Deviation = 1
        NormalDistribution normalDist = new NormalDistribution(0, 1);
        
        // 3. Calculate two-tailed p-value: 2 * (1 - normalDist.cumulativeProbability(|z|))
        // Use absolute value of Z-score for two-tailed test
        // cumulativeProbability gives P(Z <= z), so 1 - P(Z <= |z|) gives P(Z > |z|)
        // Multiply by 2 for both tails of the distribution
        double pValue = 2.0 * (1.0 - normalDist.cumulativeProbability(Math.abs(zScore)));
        
        // 4. Return p-value
        return pValue;
    }
    
    /**
     * Perform a two-sample Z-test to determine if two independent samples have 
     * significantly different means.
     * 
     * <p><b>Purpose:</b> Tests whether two independent samples come from populations 
     * with equal means. This is appropriate when population standard deviations are 
     * known or sample sizes are large enough to use sample standard deviations as 
     * good estimates.</p>
     * 
     * <p><b>Null Hypothesis (H0):</b> Mean of population 1 equals mean of population 2</p>
     * <p><b>Alternative Hypothesis (H1):</b> Means differ (two-tailed test)</p>
     * 
     * <p><b>Formula:</b> Z = (mean1 - mean2) / sqrt((stdDev1²/n1) + (stdDev2²/n2))</p>
     * 
     * <p><b>Statistical Method:</b></p>
     * <ul>
     *   <li>Calculate pooled standard error from both samples</li>
     *   <li>Compute Z-statistic as difference in means divided by pooled SE</li>
     *   <li>Use standard normal distribution for two-tailed p-value</li>
     * </ul>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Statistics from two independent samples (means, std devs, sizes)</li>
     *   <li>Calculate variance for each sample: var = stdDev²/n</li>
     *   <li>Calculate pooled standard error: SE = sqrt(var1 + var2)</li>
     *   <li>Compute Z-statistic: Z = (mean1 - mean2) / SE</li>
     *   <li>Use standard normal distribution to find two-tailed p-value</li>
     *   <li>Output: p-value for hypothesis test</li>
     * </ol>
     * 
     * <p><b>Integration:</b> This method enables comparison between different experiment 
     * configurations. For example, comparing performance metrics between traditional and 
     * emergent approaches, as documented in Table 1 of the paper. Can be called from 
     * ExperimentResults.compareTwoExperiments() when comparing summary statistics.</p>
     * 
     * @param mean1 the mean of the first sample
     * @param stdDev1 the standard deviation of the first sample
     * @param n1 the size of the first sample
     * @param mean2 the mean of the second sample
     * @param stdDev2 the standard deviation of the second sample
     * @param n2 the size of the second sample
     * @return the two-tailed p-value
     * @throws IllegalArgumentException if stdDev values &lt;= 0 or n values &lt;= 0
     */
    public static double zTestTwoSample(double mean1, double stdDev1, int n1, 
                                       double mean2, double stdDev2, int n2) {
        // IMPLEMENTED in Phase 3, Iteration 3
        // This method performs a two-sample Z-test to determine if two independent
        // samples have significantly different means.
        //
        // The test assumes known or well-estimated standard deviations and uses
        // the standard normal distribution for hypothesis testing.
        
        // 1. Validate inputs: check stdDev1, stdDev2 > 0 and n1, n2 > 0
        if (stdDev1 <= 0) {
            throw new IllegalArgumentException("Standard deviation 1 must be positive, got: " + stdDev1);
        }
        if (stdDev2 <= 0) {
            throw new IllegalArgumentException("Standard deviation 2 must be positive, got: " + stdDev2);
        }
        if (n1 <= 0) {
            throw new IllegalArgumentException("Sample size 1 must be positive, got: " + n1);
        }
        if (n2 <= 0) {
            throw new IllegalArgumentException("Sample size 2 must be positive, got: " + n2);
        }
        
        // 2. Calculate variance for each sample: var1 = stdDev1²/n1, var2 = stdDev2²/n2
        double variance1 = (stdDev1 * stdDev1) / n1;
        double variance2 = (stdDev2 * stdDev2) / n2;
        
        // 3. Calculate pooled standard error: SE = sqrt(var1 + var2)
        // This represents the standard deviation of the difference between sample means
        double pooledSE = Math.sqrt(variance1 + variance2);
        
        // 4. Calculate Z-statistic: Z = (mean1 - mean2) / SE
        double zStatistic = (mean1 - mean2) / pooledSE;
        
        // 5. Create standard normal distribution
        NormalDistribution normalDist = new NormalDistribution(0, 1);
        
        // 6. Calculate two-tailed p-value: 2 * (1 - normalDist.cumulativeProbability(|Z|))
        double pValue = 2.0 * (1.0 - normalDist.cumulativeProbability(Math.abs(zStatistic)));
        
        // 7. Return p-value
        return pValue;
    }
    
    /**
     * Perform a one-sample t-test to determine if a sample mean differs significantly 
     * from a hypothesized population mean.
     * 
     * <p><b>Purpose:</b> Tests whether a sample comes from a population with a specified 
     * mean when the population standard deviation is unknown. The t-test is more 
     * conservative than the Z-test and accounts for uncertainty in the standard deviation 
     * estimate, especially important for small sample sizes.</p>
     * 
     * <p><b>Null Hypothesis (H0):</b> Sample mean equals population mean</p>
     * <p><b>Alternative Hypothesis (H1):</b> Sample mean differs from population mean</p>
     * 
     * <p><b>Statistical Method:</b></p>
     * <ul>
     *   <li>Uses Apache Commons Math TTest class for robust implementation</li>
     *   <li>Computes t-statistic using sample mean, std dev, and size</li>
     *   <li>Uses t-distribution with (n-1) degrees of freedom</li>
     *   <li>Returns two-tailed p-value</li>
     * </ul>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: List of sample values and hypothesized population mean</li>
     *   <li>Validate sample has at least 2 values</li>
     *   <li>Convert List&lt;Double&gt; to double[] for Apache Commons Math</li>
     *   <li>Create TTest instance</li>
     *   <li>Call tTest(populationMean, sampleArray) for two-tailed p-value</li>
     *   <li>Output: p-value indicating significance</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Called by ExperimentResults.getTTestPValue() to test 
     * if experiment metrics differ from expected baseline values. This is the primary 
     * statistical test used in batch experiment analysis (section 8.1-8.2 of REQUIREMENTS.md).</p>
     * 
     * @param sample the sample values
     * @param populationMean the hypothesized population mean
     * @return the two-tailed p-value
     * @throws IllegalArgumentException if sample is null, empty, or has fewer than 2 values
     */
    public static double tTestOneSample(List<Double> sample, double populationMean) {
        // IMPLEMENTED in Phase 3, Iteration 4
        // This method performs a one-sample t-test to determine if a sample mean
        // differs significantly from a hypothesized population mean.
        //
        // Uses Apache Commons Math TTest for robust implementation with proper
        // handling of the t-distribution and degrees of freedom.
        
        // 1. Validate sample: check not null, size >= 2
        if (sample == null) {
            throw new IllegalArgumentException("Sample list cannot be null");
        }
        if (sample.size() < 2) {
            throw new IllegalArgumentException("Sample must contain at least 2 values, got: " + sample.size());
        }
        
        // 2. Convert List<Double> to double[] array
        // Apache Commons Math TTest requires primitive double array
        double[] sampleArray = new double[sample.size()];
        for (int i = 0; i < sample.size(); i++) {
            if (sample.get(i) == null) {
                throw new IllegalArgumentException("Sample cannot contain null values");
            }
            sampleArray[i] = sample.get(i);
        }
        
        // 3. Create TTest instance
        TTest tTest = new TTest();
        
        // 4. Call tTest.tTest(populationMean, sampleArray)
        // This performs a two-tailed t-test and returns the p-value
        double pValue = tTest.tTest(populationMean, sampleArray);
        
        // 5. Return p-value
        return pValue;
    }
    
    /**
     * Perform a two-sample unpaired t-test to determine if two independent samples 
     * have significantly different means.
     * 
     * <p><b>Purpose:</b> Tests whether two independent samples come from populations 
     * with equal means when population standard deviations are unknown. This is the 
     * standard method for comparing two experimental conditions with different subjects 
     * in each condition.</p>
     * 
     * <p><b>Null Hypothesis (H0):</b> Mean of population 1 equals mean of population 2</p>
     * <p><b>Alternative Hypothesis (H1):</b> Means differ (two-tailed test)</p>
     * 
     * <p><b>Statistical Method:</b></p>
     * <ul>
     *   <li>Uses Apache Commons Math TTest for Welch's t-test (unequal variances)</li>
     *   <li>Does not assume equal variances between samples</li>
     *   <li>Adjusts degrees of freedom using Welch-Satterthwaite equation</li>
     *   <li>Returns two-tailed p-value</li>
     * </ul>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Two independent sample datasets as List&lt;Double&gt;</li>
     *   <li>Validate both samples have at least 2 values</li>
     *   <li>Convert both lists to double[] arrays</li>
     *   <li>Create TTest instance</li>
     *   <li>Call tTest.tTest(sample1Array, sample2Array) for two-tailed p-value</li>
     *   <li>Output: p-value for hypothesis test</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Primary method called by ExperimentResults.compareTwoExperiments() 
     * to determine if performance differences between experimental conditions are statistically 
     * significant. Essential for validating claims about emergent vs. traditional approaches 
     * as shown in the paper's Table 1.</p>
     * 
     * @param sample1 the first sample
     * @param sample2 the second sample
     * @return the two-tailed p-value
     * @throws IllegalArgumentException if either sample is null, empty, or has fewer than 2 values
     */
    public static double tTestTwoSample(List<Double> sample1, List<Double> sample2) {
        // IMPLEMENTED in Phase 3, Iteration 5
        // This method performs a two-sample unpaired t-test to determine if two
        // independent samples have significantly different means.
        //
        // Uses Welch's t-test via Apache Commons Math which does not assume equal
        // variances between samples, making it more robust for real-world data.
        
        // 1. Validate both samples: check not null, size >= 2 for each
        if (sample1 == null) {
            throw new IllegalArgumentException("Sample 1 cannot be null");
        }
        if (sample2 == null) {
            throw new IllegalArgumentException("Sample 2 cannot be null");
        }
        if (sample1.size() < 2) {
            throw new IllegalArgumentException("Sample 1 must contain at least 2 values, got: " + sample1.size());
        }
        if (sample2.size() < 2) {
            throw new IllegalArgumentException("Sample 2 must contain at least 2 values, got: " + sample2.size());
        }
        
        // 2. Convert both List<Double> to double[] arrays
        double[] sample1Array = new double[sample1.size()];
        for (int i = 0; i < sample1.size(); i++) {
            if (sample1.get(i) == null) {
                throw new IllegalArgumentException("Sample 1 cannot contain null values");
            }
            sample1Array[i] = sample1.get(i);
        }
        
        double[] sample2Array = new double[sample2.size()];
        for (int i = 0; i < sample2.size(); i++) {
            if (sample2.get(i) == null) {
                throw new IllegalArgumentException("Sample 2 cannot contain null values");
            }
            sample2Array[i] = sample2.get(i);
        }
        
        // 3. Create TTest instance
        TTest tTest = new TTest();
        
        // 4. Call tTest.tTest(sample1Array, sample2Array)
        // This performs Welch's t-test (unequal variances) and returns two-tailed p-value
        double pValue = tTest.tTest(sample1Array, sample2Array);
        
        // 5. Return p-value
        return pValue;
    }
    
    /**
     * Perform a paired t-test to determine if two related samples have significantly 
     * different means.
     * 
     * <p><b>Purpose:</b> Tests whether the mean difference between paired observations 
     * is zero. Used when the same subjects are measured twice (before/after) or when 
     * subjects are matched in pairs. More powerful than unpaired t-test when appropriate 
     * because it controls for individual variation.</p>
     * 
     * <p><b>Null Hypothesis (H0):</b> Mean difference between pairs equals zero</p>
     * <p><b>Alternative Hypothesis (H1):</b> Mean difference differs from zero</p>
     * 
     * <p><b>Statistical Method:</b></p>
     * <ul>
     *   <li>Computes difference for each pair: diff[i] = sample1[i] - sample2[i]</li>
     *   <li>Performs one-sample t-test on differences against mean of zero</li>
     *   <li>Uses Apache Commons Math TTest.pairedTTest()</li>
     *   <li>Returns two-tailed p-value</li>
     * </ul>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Two related samples as List&lt;Double&gt; with equal sizes</li>
     *   <li>Validate both samples have same size and at least 2 values</li>
     *   <li>Convert both lists to double[] arrays</li>
     *   <li>Create TTest instance</li>
     *   <li>Call tTest.pairedTTest(sample1Array, sample2Array)</li>
     *   <li>Output: p-value for paired comparison</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Useful for before/after comparisons in experiments, such as 
     * comparing cell performance on the same problem instances before and after parameter 
     * tuning. Can be called from experiment analysis code when paired experimental design 
     * is used.</p>
     * 
     * @param sample1 the first sample (e.g., before measurements)
     * @param sample2 the second sample (e.g., after measurements)
     * @return the two-tailed p-value
     * @throws IllegalArgumentException if samples are null, have different sizes, or fewer than 2 values
     */
    public static double tTestPaired(List<Double> sample1, List<Double> sample2) {
        // IMPLEMENTED in Phase 3, Iteration 6
        // This method performs a paired t-test to determine if two related samples
        // have significantly different means.
        //
        // Paired t-test is used for before/after comparisons or matched pairs,
        // and is more powerful than unpaired t-test when appropriate.
        
        // 1. Validate both samples: check not null, same size, size >= 2
        if (sample1 == null) {
            throw new IllegalArgumentException("Sample 1 cannot be null");
        }
        if (sample2 == null) {
            throw new IllegalArgumentException("Sample 2 cannot be null");
        }
        if (sample1.size() != sample2.size()) {
            throw new IllegalArgumentException(
                "Samples must have the same size for paired t-test. Got sizes: " + 
                sample1.size() + " and " + sample2.size()
            );
        }
        if (sample1.size() < 2) {
            throw new IllegalArgumentException("Samples must contain at least 2 values, got: " + sample1.size());
        }
        
        // 2. Convert both List<Double> to double[] arrays
        double[] sample1Array = new double[sample1.size()];
        for (int i = 0; i < sample1.size(); i++) {
            if (sample1.get(i) == null) {
                throw new IllegalArgumentException("Sample 1 cannot contain null values");
            }
            sample1Array[i] = sample1.get(i);
        }
        
        double[] sample2Array = new double[sample2.size()];
        for (int i = 0; i < sample2.size(); i++) {
            if (sample2.get(i) == null) {
                throw new IllegalArgumentException("Sample 2 cannot contain null values");
            }
            sample2Array[i] = sample2.get(i);
        }
        
        // 3. Create TTest instance
        TTest tTest = new TTest();
        
        // 4. Call tTest.pairedTTest(sample1Array, sample2Array)
        // This computes differences and performs one-sample t-test on differences
        double pValue = tTest.pairedTTest(sample1Array, sample2Array);
        
        // 5. Return p-value
        return pValue;
    }
    
    /**
     * Calculate the mean (average) of a list of values.
     * 
     * <p><b>Purpose:</b> Computes the arithmetic mean, which is the central tendency 
     * measure used throughout statistical testing. The mean is the foundation for 
     * computing standard deviation, Z-scores, and all hypothesis tests.</p>
     * 
     * <p><b>Formula:</b> mean = (sum of all values) / (number of values)</p>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: List of numeric values</li>
     *   <li>Validate list is not null or empty</li>
     *   <li>Sum all values in the list</li>
     *   <li>Divide sum by count of values</li>
     *   <li>Output: Mean value</li>
     * </ol>
     * 
     * <p><b>Integration:</b> This is a foundational helper method called by:
     * <ul>
     *   <li>calculateStdDev() - needs mean to compute variance</li>
     *   <li>All t-test methods indirectly via Apache Commons Math</li>
     *   <li>ExperimentResults methods for metric aggregation</li>
     * </ul>
     * Serves as the main entry point in Phase Two implementation.</p>
     * 
     * @param values the list of values
     * @return the mean
     * @throws IllegalArgumentException if values is null or empty
     */
    public static double calculateMean(List<Double> values) {
        // IMPLEMENTED in Phase 2 - Main Entry Point
        // This method serves as the foundational entry point for statistical calculations.
        // It computes the arithmetic mean which is used by calculateStdDev(), all t-tests,
        // and serves as a building block for more complex statistical operations.
        //
        // The mean represents the central tendency of a dataset and is calculated as
        // the sum of all values divided by the count of values.
        
        // 1. Validate values: check not null and not empty
        if (values == null) {
            throw new IllegalArgumentException("Values list cannot be null");
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Values list cannot be empty");
        }
        
        // 2. Initialize sum to 0.0
        double sum = 0.0;
        
        // 3. Iterate through values and accumulate sum
        for (Double value : values) {
            if (value == null) {
                throw new IllegalArgumentException("Values list cannot contain null elements");
            }
            sum += value;
        }
        
        // 4. Divide sum by values.size() and return mean
        return sum / values.size();
    }
    
    /**
     * Calculate the standard deviation of a list of values.
     * 
     * <p><b>Purpose:</b> Measures the spread or dispersion of data points around the mean. 
     * Standard deviation is essential for hypothesis testing as it quantifies variability, 
     * which determines whether observed differences are likely due to chance or represent 
     * real effects.</p>
     * 
     * <p><b>Formula:</b> σ = sqrt(Σ(xi - mean)² / n) for population</p>
     * <p>This implementation uses sample standard deviation: s = sqrt(Σ(xi - mean)² / (n-1))</p>
     * 
     * <p><b>Statistical Rationale:</b> Uses n-1 (Bessel's correction) in denominator to 
     * provide unbiased estimate of population variance from sample data.</p>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: List of numeric values</li>
     *   <li>Validate list has at least 2 values (need for variance calculation)</li>
     *   <li>Calculate mean using calculateMean()</li>
     *   <li>For each value, compute squared deviation from mean</li>
     *   <li>Sum all squared deviations</li>
     *   <li>Divide by (n-1) to get sample variance</li>
     *   <li>Take square root to get standard deviation</li>
     *   <li>Output: Standard deviation</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Called by:
     * <ul>
     *   <li>calculateConfidenceInterval() - needs SD for margin of error</li>
     *   <li>ExperimentResults.getStdDevMetric() - could delegate to this method</li>
     *   <li>All statistical tests indirectly via Apache Commons Math</li>
     * </ul>
     * Serves as the main entry point in Phase Two implementation alongside calculateMean().</p>
     * 
     * @param values the list of values
     * @return the standard deviation
     * @throws IllegalArgumentException if values is null or has fewer than 2 elements
     */
    public static double calculateStdDev(List<Double> values) {
        // IMPLEMENTED in Phase 2 - Main Entry Point
        // This method serves as the foundational entry point alongside calculateMean().
        // It computes the sample standard deviation using Bessel's correction (n-1),
        // which provides an unbiased estimate of the population variance.
        //
        // Standard deviation quantifies the spread of data points around the mean and
        // is essential for all hypothesis testing as it measures variability.
        
        // 1. Validate values: check not null and size >= 2
        if (values == null) {
            throw new IllegalArgumentException("Values list cannot be null");
        }
        if (values.size() < 2) {
            throw new IllegalArgumentException("Values list must contain at least 2 elements for standard deviation calculation");
        }
        
        // 2. Calculate mean using calculateMean()
        // This demonstrates the integration between the two main entry point methods
        double mean = calculateMean(values);
        
        // 3. Initialize sumSquaredDiff to 0.0
        double sumSquaredDiff = 0.0;
        
        // 4. For each value: compute diff = value - mean, add diff² to sumSquaredDiff
        for (Double value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        
        // 5. Calculate variance using Bessel's correction: var = sumSquaredDiff / (n - 1)
        // Using n-1 instead of n provides an unbiased estimate of population variance
        double variance = sumSquaredDiff / (values.size() - 1);
        
        // 6. Return sqrt(variance) as the standard deviation
        return Math.sqrt(variance);
    }
    
    /**
     * Calculate a confidence interval for a mean.
     * 
     * <p><b>Purpose:</b> Provides a range of values within which the true population mean 
     * is likely to fall, given the sample data. Confidence intervals give a measure of 
     * precision for our estimate and are commonly reported alongside point estimates in 
     * scientific papers.</p>
     * 
     * <p><b>Interpretation:</b> A 95% confidence interval means that if we repeated the 
     * sampling process many times, 95% of calculated intervals would contain the true 
     * population mean.</p>
     * 
     * <p><b>Formula:</b> CI = mean ± (critical_value * SE)</p>
     * <p>Where SE (standard error) = stdDev / sqrt(sampleSize)</p>
     * <p>Critical value comes from t-distribution with (n-1) degrees of freedom</p>
     * 
     * <p><b>Common Confidence Levels:</b></p>
     * <ul>
     *   <li>0.90 (90%): Less conservative, narrower interval</li>
     *   <li>0.95 (95%): Standard in most research</li>
     *   <li>0.99 (99%): More conservative, wider interval</li>
     * </ul>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: Sample mean, std dev, size, and desired confidence level (e.g., 0.95)</li>
     *   <li>Validate inputs: confidence level between 0 and 1, sampleSize &gt; 0, stdDev &gt;= 0</li>
     *   <li>Calculate standard error: SE = stdDev / sqrt(sampleSize)</li>
     *   <li>Calculate alpha: α = 1 - confidenceLevel (e.g., 0.05 for 95% CI)</li>
     *   <li>Get critical t-value using TDistribution with (n-1) degrees of freedom</li>
     *   <li>Calculate margin of error: ME = criticalValue * SE</li>
     *   <li>Calculate bounds: [mean - ME, mean + ME]</li>
     *   <li>Output: Array [lowerBound, upperBound]</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Called by ExperimentResults.getConfidenceInterval() to 
     * provide confidence intervals for experiment metrics. Results are included in 
     * statistical summaries via getStatisticalSummary().</p>
     * 
     * @param mean the sample mean
     * @param stdDev the sample standard deviation
     * @param sampleSize the sample size
     * @param confidenceLevel the confidence level (e.g., 0.95 for 95% confidence)
     * @return array with [lowerBound, upperBound]
     * @throws IllegalArgumentException if confidenceLevel not in (0,1) or sampleSize &lt;= 0
     */
    public static double[] calculateConfidenceInterval(double mean, double stdDev, 
                                                      int sampleSize, double confidenceLevel) {
        // IMPLEMENTED in Phase 3, Iteration 7
        // This method calculates a confidence interval for a mean using the t-distribution.
        //
        // Confidence intervals provide a range estimate for the true population parameter
        // with a specified level of confidence (e.g., 95%).
        
        // 1. Validate inputs: 0 < confidenceLevel < 1, sampleSize > 0, stdDev >= 0
        if (confidenceLevel <= 0 || confidenceLevel >= 1) {
            throw new IllegalArgumentException(
                "Confidence level must be between 0 and 1 (exclusive), got: " + confidenceLevel
            );
        }
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be positive, got: " + sampleSize);
        }
        if (stdDev < 0) {
            throw new IllegalArgumentException("Standard deviation cannot be negative, got: " + stdDev);
        }
        
        // 2. Calculate standard error: SE = stdDev / sqrt(sampleSize)
        double standardError = stdDev / Math.sqrt(sampleSize);
        
        // 3. Calculate alpha: alpha = (1 - confidenceLevel) / 2 (two-tailed)
        // For 95% confidence level, alpha = 0.025 for each tail
        double alpha = (1.0 - confidenceLevel) / 2.0;
        
        // 4. Get degrees of freedom: df = sampleSize - 1
        int degreesOfFreedom = sampleSize - 1;
        
        // 5. Create TDistribution with df
        TDistribution tDist = new TDistribution(degreesOfFreedom);
        
        // 6. Get critical t-value: tDist.inverseCumulativeProbability(1 - alpha)
        // This gives the t-value such that P(T <= t) = 1 - alpha
        double criticalValue = tDist.inverseCumulativeProbability(1.0 - alpha);
        
        // 7. Calculate margin of error: ME = criticalValue * SE
        double marginOfError = criticalValue * standardError;
        
        // 8. Return new double[] {mean - ME, mean + ME}
        return new double[] {
            mean - marginOfError,  // lower bound
            mean + marginOfError   // upper bound
        };
    }
    
    /**
     * Determine if a p-value indicates statistical significance at a given alpha level.
     * 
     * <p><b>Purpose:</b> Provides a simple boolean decision rule for hypothesis testing. 
     * Rather than reporting only p-values, this method directly answers the question: 
     * "Is the result statistically significant?"</p>
     * 
     * <p><b>Common Alpha Levels:</b></p>
     * <ul>
     *   <li>0.05 (5%): Standard significance level in most research</li>
     *   <li>0.01 (1%): More stringent, reduces Type I error risk</li>
     *   <li>0.10 (10%): More lenient, sometimes used in exploratory research</li>
     * </ul>
     * 
     * <p><b>Interpretation:</b></p>
     * <ul>
     *   <li>If p &lt; α: Reject null hypothesis (result is significant)</li>
     *   <li>If p &gt;= α: Fail to reject null hypothesis (result not significant)</li>
     * </ul>
     * 
     * <p><b>Type I Error:</b> Alpha represents the probability of rejecting a true 
     * null hypothesis (false positive). Lower alpha means more conservative testing.</p>
     * 
     * <p><b>Data Flow:</b></p>
     * <ol>
     *   <li>Input: p-value from statistical test and alpha threshold</li>
     *   <li>Validate p-value is between 0 and 1</li>
     *   <li>Validate alpha is between 0 and 1</li>
     *   <li>Compare: pValue &lt; alpha</li>
     *   <li>Output: true if significant, false otherwise</li>
     * </ol>
     * 
     * <p><b>Integration:</b> Can be used in experiment analysis code to automatically 
     * flag significant results. Example: After calling compareTwoExperiments(), use 
     * isSignificant(pValue, 0.05) to determine if performance difference is meaningful.</p>
     * 
     * @param pValue the p-value from a statistical test
     * @param alpha the significance level (e.g., 0.05)
     * @return true if pValue &lt; alpha (result is significant), false otherwise
     * @throws IllegalArgumentException if pValue or alpha not in [0, 1]
     */
    public static boolean isSignificant(double pValue, double alpha) {
        // IMPLEMENTED in Phase 3, Iteration 8
        // This method provides a simple decision rule for hypothesis testing based
        // on comparing a p-value to a significance threshold (alpha level).
        //
        // Returns true if the result is statistically significant, false otherwise.
        
        // 1. Validate pValue: 0 <= pValue <= 1
        if (pValue < 0 || pValue > 1) {
            throw new IllegalArgumentException(
                "P-value must be between 0 and 1 (inclusive), got: " + pValue
            );
        }
        
        // 2. Validate alpha: 0 <= alpha <= 1
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException(
                "Alpha must be between 0 and 1 (inclusive), got: " + alpha
            );
        }
        
        // 3. Return pValue < alpha
        // If p-value is less than alpha, we reject the null hypothesis
        // and conclude the result is statistically significant
        return pValue < alpha;
    }

    /**
     * Recommend appropriate statistical test type based on sample size and known parameters.
     * 
     * <p><b>Purpose:</b> Guides selection between Z-test and t-test. Z-tests assume known population std dev or large n (&gt;30); t-tests for unknown σ/small n.</p>
     * 
     * <p><b>Integration:</b> Use in ExperimentResults to auto-select tests for metric comparisons (e.g., swaps in Table 1).</p>
     * 
     * @param sampleSize the sample size
     * @param knownPopulationStdDev true if population std dev is known
     * @return "Z" or "T"
     */
    public static String recommendTestType(int sampleSize, boolean knownPopulationStdDev) {
        return (knownPopulationStdDev || sampleSize > 30) ? "Z" : "T";
    }

    /**
     * Calculate Cohen's d effect size for two independent samples.
     * 
     * <p><b>Purpose:</b> Measures standardized difference between means, independent of sample size. Interprets practical significance (small=0.2, medium=0.5, large=0.8).</p>
     * 
     * <p><b>Formula:</b> d = |mean1 - mean2| / pooled SD, where pooled SD uses n-1 correction.</p>
     * 
     * <p><b>Integration:</b> Enhance ExperimentResults.compareTwoExperiments() for richer analysis (e.g., DG ratios, Section 7.5).</p>
     * 
     * @param sample1 first sample
     * @param sample2 second sample
     * @return Cohen's d value
     * @throws IllegalArgumentException if samples invalid
     */
    public static double calculateCohensD(List<Double> sample1, List<Double> sample2) {
        if (sample1 == null || sample2 == null || sample1.size() < 2 || sample2.size() < 2) {
            throw new IllegalArgumentException("Samples must have at least 2 non-null values each");
        }
        double mean1 = calculateMean(sample1);
        double mean2 = calculateMean(sample2);
        double sd1 = calculateStdDev(sample1);
        double sd2 = calculateStdDev(sample2);
        double pooledSd = Math.sqrt(((sample1.size() - 1) * (sd1 * sd1) + (sample2.size() - 1) * (sd2 * sd2)) /
                                    (sample1.size() + sample2.size() - 2));
        return Math.abs(mean1 - mean2) / pooledSd;
    }

    /**
     * Overload for tTestTwoSample using double arrays for performance.
     * 
     * <p><b>Purpose:</b> Direct array input avoids List conversion overhead in batch processing.</p>
     * 
     * @param sample1Array first sample as array
     * @param sample2Array second sample as array
     * @return two-tailed p-value
     */
    public static double tTestTwoSample(double[] sample1Array, double[] sample2Array) {
        List<Double> sample1 = new java.util.ArrayList<>();
        for (double v : sample1Array) sample1.add(v);
        List<Double> sample2 = new java.util.ArrayList<>();
        for (double v : sample2Array) sample2.add(v);
        return tTestTwoSample(sample1, sample2);
    }

    /**
     * Calculate Spearman rank correlation coefficient.
     * 
     * <p><b>Purpose:</b> Non-parametric measure of monotonic relationship, useful for sortedness/monotonicity (Section 7.1-7.4). Handles non-normal data in trajectories.</p>
     * 
     * <p><b>Formula:</b> ρ = 1 - (6 * Σd_i²) / (n(n² - 1)), where d_i = rank diff; ties averaged.</p>
     * 
     * <p><b>Integration:</b> Use with SpearmanDistance in metrics for trajectory analysis.</p>
     * 
     * @param x first sample
     * @param y second sample (same size)
     * @return Spearman ρ (-1 to 1)
     * @throws IllegalArgumentException if sizes differ or invalid
     */
    public static double calculateSpearmanCorrelation(List<Double> x, List<Double> y) {
        if (x == null || y == null || x.size() != y.size() || x.size() < 2) {
            throw new IllegalArgumentException("Samples must be non-null, same size >=2");
        }
        
        int n = x.size();
        
        // Convert to arrays for ranking
        double[] xArray = x.stream().mapToDouble(Double::doubleValue).toArray();
        double[] yArray = y.stream().mapToDouble(Double::doubleValue).toArray();
        
        // Calculate ranks for both arrays
        double[] rankX = calculateRanks(xArray);
        double[] rankY = calculateRanks(yArray);
        
        // Calculate Pearson correlation on the ranks
        org.apache.commons.math3.stat.correlation.PearsonsCorrelation corr = 
            new org.apache.commons.math3.stat.correlation.PearsonsCorrelation();
        return corr.correlation(rankX, rankY);
    }
    
    /**
     * Calculate ranks for an array of values, handling ties using average ranks.
     * 
     * <p>Ties are handled by assigning the average of the ranks that would have been assigned
     * to the tied values. For example, if values at positions 2, 3, and 4 are tied, they each
     * get rank (2+3+4)/3 = 3.0.</p>
     * 
     * @param values the array of values to rank
     * @return array of ranks (1-indexed, with ties averaged)
     */
    private static double[] calculateRanks(double[] values) {
        int n = values.length;
        
        // Create array of (value, original_index) pairs
        class IndexedValue implements Comparable<IndexedValue> {
            double value;
            int originalIndex;
            
            IndexedValue(double value, int originalIndex) {
                this.value = value;
                this.originalIndex = originalIndex;
            }
            
            @Override
            public int compareTo(IndexedValue other) {
                return Double.compare(this.value, other.value);
            }
        }
        
        IndexedValue[] indexed = new IndexedValue[n];
        for (int i = 0; i < n; i++) {
            indexed[i] = new IndexedValue(values[i], i);
        }
        
        // Sort by value
        java.util.Arrays.sort(indexed);
        
        // Assign ranks, handling ties with average ranks
        double[] ranks = new double[n];
        int i = 0;
        while (i < n) {
            // Find extent of tied values
            int j = i + 1;
            while (j < n && Double.compare(indexed[j].value, indexed[i].value) == 0) {
                j++;
            }
            
            // Calculate average rank for tied values (ranks are 1-indexed)
            double averageRank = (i + 1 + j) / 2.0;
            
            // Assign average rank to all tied values
            for (int k = i; k < j; k++) {
                ranks[indexed[k].originalIndex] = averageRank;
            }
            
            i = j;
        }
        
        return ranks;
    }
}
