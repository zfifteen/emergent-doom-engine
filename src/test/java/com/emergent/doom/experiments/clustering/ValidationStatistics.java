package com.emergent.doom.experiments.clustering;

import com.emergent.doom.statistics.StatisticalTests;
import java.util.List;

public class ValidationStatistics {

    public static class Result {
        public final String name;
        public final double mean;
        public final double stdDev;
        public final double pValue; // One-sample t-test against expected
        
        public Result(String name, double mean, double stdDev, double pValue) {
            this.name = name;
            this.mean = mean;
            this.stdDev = stdDev;
            this.pValue = pValue;
        }
    }

    public static class ComparisonResult {
        public final String name;
        public final double meanDiff;
        public final double pValue; // Two-sample t-test
        public final boolean isSignificant;

        public ComparisonResult(String name, double meanDiff, double pValue, boolean isSignificant) {
            this.name = name;
            this.meanDiff = meanDiff;
            this.pValue = pValue;
            this.isSignificant = isSignificant;
        }
    }

    /**
     * Validates a sample against a benchmark value (from paper).
     * Note: For reproduction, we often want the p-value to be > alpha (failed to reject null hypothesis that they are equal),
     * OR we just care that the mean is within the specific tolerance range requested by the issue.
     */
    public Result validateAgainstBenchmark(String name, List<Double> sample, double expectedMean, double alpha) {
        double mean = StatisticalTests.calculateMean(sample);
        double stdDev = StatisticalTests.calculateStdDev(sample);
        double pValue = StatisticalTests.tTestOneSample(sample, expectedMean);
        
        return new Result(name, mean, stdDev, pValue);
    }

    /**
     * Compares an experiment group against a control group.
     * We expect a significant difference here.
     */
    public ComparisonResult compareWithControl(String name, List<Double> experiment, List<Double> control, double alpha) {
        double meanExp = StatisticalTests.calculateMean(experiment);
        double meanCtrl = StatisticalTests.calculateMean(control);
        double pValue = StatisticalTests.tTestTwoSample(experiment, control);
        boolean significant = StatisticalTests.isSignificant(pValue, alpha);
        
        return new ComparisonResult(name, meanExp - meanCtrl, pValue, significant);
    }
}
