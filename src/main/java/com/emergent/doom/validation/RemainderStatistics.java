package com.emergent.doom.validation;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Statistical analysis of the remainder landscape during factorization.
 * 
 * <p><strong>Purpose:</strong> Characterizes the fitness landscape that guides
 * emergent self-organization. Key insight from LINEAR_SCALING_ANALYSIS.md is that
 * convergence depends on remainder gradient strength, not array size. This class
 * captures metrics to test that hypothesis.</p>
 * 
 * <p><strong>Architecture Role:</strong> Provides quantitative measures of landscape
 * structure that correlate with convergence behavior. Used to predict when emergent
 * method will fail (flat landscape, high variance, low autocorrelation).</p>
 * 
 * <p><strong>Data Flow:</strong>
 * <ul>
 *   <li>Input: Array of RemainderCell values from final or intermediate configuration</li>
 *   <li>Output: Statistical summaries (mean, variance, autocorrelation) for analysis</li>
 *   <li>Integration: Embedded in ScalingTrialResult for correlation with convergence</li>
 * </ul>
 * </p>
 */
public class RemainderStatistics {
    private final double mean;
    private final double variance;
    private final double standardDeviation;
    private final double autocorrelation;
    private final int sampleSize;
    
    /**
     * Compute remainder statistics from precomputed values.
     * 
     * <p><strong>Implementation:</strong> Constructor for creating statistics
     * object from already-computed values.</p>
     * 
     * <p><strong>Reasoning:</strong> Separates computation (in fromCells) from
     * storage, allowing flexible creation from different sources.</p>
     * 
     * @param mean Mean remainder value
     * @param variance Variance of remainder values
     * @param standardDeviation Standard deviation of remainder values
     * @param autocorrelation Lag-1 autocorrelation of sorted remainders
     * @param sampleSize Number of cells in sample
     */
    public RemainderStatistics(double mean, double variance, double standardDeviation,
                               double autocorrelation, int sampleSize) {
        this.mean = mean;
        this.variance = variance;
        this.standardDeviation = standardDeviation;
        this.autocorrelation = autocorrelation;
        this.sampleSize = sampleSize;
    }
    
    /**
     * Get the mean remainder value.
     * 
     * <p><strong>Implementation:</strong> Returns average remainder.</p>
     * 
     * @return Mean remainder
     */
    public double getMean() {
        return mean;
    }
    
    /**
     * Get the variance of remainder values.
     * 
     * <p><strong>Implementation:</strong> Returns variance.
     * High variance suggests flat landscape where cells can't distinguish good positions.</p>
     * 
     * @return Variance
     */
    public double getVariance() {
        return variance;
    }
    
    /**
     * Get the standard deviation.
     * 
     * <p><strong>Implementation:</strong> Returns sqrt(variance).</p>
     * 
     * @return Standard deviation
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }
    
    /**
     * Get the lag-1 autocorrelation of sorted remainders.
     * 
     * <p><strong>Implementation:</strong> Returns autocorrelation coefficient.
     * Low autocorrelation suggests noisy gradient that hampers convergence.</p>
     * 
     * @return Autocorrelation coefficient
     */
    public double getAutocorrelation() {
        return autocorrelation;
    }
    
    /**
     * Get the sample size.
     * 
     * <p><strong>Implementation:</strong> Returns number of cells analyzed.</p>
     * 
     * @return Sample size
     */
    public int getSampleSize() {
        return sampleSize;
    }
    
    /**
     * Create statistics from cell array.
     * 
     * <p><strong>Implementation:</strong> Extracts remainder values from
     * RemainderCell array and computes statistical measures.</p>
     * 
     * <p><strong>Algorithm:</strong>
     * <ol>
     *   <li>Extract remainder values from each cell (convert BigInteger to double)</li>
     *   <li>Compute mean: average of all remainders</li>
     *   <li>Compute variance: average of squared deviations from mean</li>
     *   <li>Sort remainders for autocorrelation calculation</li>
     *   <li>Compute lag-1 autocorrelation: correlation between r[i] and r[i+1]</li>
     * </ol>
     * </p>
     * 
     * <p><strong>Reasoning:</strong> These statistics characterize the fitness
     * landscape structure. High variance + low autocorrelation = flat, noisy landscape
     * that won't guide convergence effectively.</p>
     * 
     * <p><strong>Integration:</strong> Called by executeSingleTrial() to analyze
     * final cell configuration and embed stats in result.</p>
     * 
     * @param cells Array of RemainderCell objects to analyze
     * @return RemainderStatistics object with computed metrics
     */
    public static RemainderStatistics fromCells(com.emergent.doom.cell.RemainderCell[] cells) {
        if (cells == null || cells.length == 0) {
            return new RemainderStatistics(0, 0, 0, 0, 0);
        }
        
        int n = cells.length;
        
        // Extract remainder values as double array for numerical stability
        double[] remainders = new double[n];
        for (int i = 0; i < n; i++) {
            remainders[i] = cells[i].getRemainder().doubleValue();
        }
        
        // Compute mean
        double sum = 0;
        for (double r : remainders) {
            sum += r;
        }
        double mean = sum / n;
        
        // Compute variance
        double sumSquaredDiff = 0;
        for (double r : remainders) {
            double diff = r - mean;
            sumSquaredDiff += diff * diff;
        }
        double variance = sumSquaredDiff / n;
        double stdDev = Math.sqrt(variance);
        
        // Sort remainders for autocorrelation
        double[] sortedRemainders = Arrays.copyOf(remainders, n);
        Arrays.sort(sortedRemainders);
        
        // Compute lag-1 autocorrelation
        double autocorr = 0;
        if (n > 1) {
            // Compute mean of sorted values
            double sortedMean = 0;
            for (double r : sortedRemainders) {
                sortedMean += r;
            }
            sortedMean /= n;
            
            // Compute autocorrelation: corr(r[i], r[i+1])
            double numerator = 0;
            double denominator = 0;
            for (int i = 0; i < n - 1; i++) {
                numerator += (sortedRemainders[i] - sortedMean) * 
                            (sortedRemainders[i+1] - sortedMean);
            }
            for (int i = 0; i < n; i++) {
                double diff = sortedRemainders[i] - sortedMean;
                denominator += diff * diff;
            }
            
            if (denominator > 0) {
                autocorr = numerator / denominator;
            }
        }
        
        return new RemainderStatistics(mean, variance, stdDev, autocorr, n);
    }
}
