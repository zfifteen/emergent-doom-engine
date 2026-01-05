package com.emergent.doom.experiments.clustering;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationStatisticsTest {

    @Test
    public void testValidateAgainstBenchmark_Success() {
        // Create a sample with mean close to 72.0
        List<Double> sample = Arrays.asList(71.0, 72.0, 73.0, 71.5, 72.5);
        
        ValidationStatistics stats = new ValidationStatistics();
        ValidationStatistics.Result result = stats.validateAgainstBenchmark(
            "TestBenchmark", 
            sample, 
            72.0, // expected mean
            0.05 // alpha
        );
        
        // Since sample mean is very close to population mean, p-value should be high (fail to reject null hypothesis)
        // Wait, "validate against paper" usually means "is consistent with".
        // If p < 0.05, it means it is DIFFERENT.
        // We want it to be NOT different from the paper value?
        // OR does the issue mean "Demonstrate statistical significance... when comparing experimental results against the CONTROL"?
        // Requirement 4: "Demonstrate statistical significance with a p-value < 0.05 when comparing experimental results against the control."
        // Requirement 1: "Achieve a peak aggregation of 72% ± 5%".
        
        // So for Benchmark (Paper value), we want it to be "within range".
        // T-test against paper value: If p < 0.05, it means we are SIGNIFICANTLY DIFFERENT from paper. That would be bad if we want to reproduce it?
        // Actually, usually "reproduce" means "falls within confidence interval" or "t-test does not reject equality".
        
        // Let's assume for "Benchmark", we check if the mean is within the tolerance range first, 
        // and maybe use t-test to ensure it's not significantly different? 
        // Or just check the mean. Requirement 1 says "72% ± 5%".
        
        assertTrue(result.mean >= 67.0 && result.mean <= 77.0);
    }

    @Test
    public void testCompareWithControl_Significant() {
        // Experiment: High clustering ~70
        List<Double> experiment = Arrays.asList(68.0, 70.0, 72.0, 69.0, 71.0);
        // Control: Low clustering ~50
        List<Double> control = Arrays.asList(48.0, 50.0, 52.0, 49.0, 51.0);
        
        ValidationStatistics stats = new ValidationStatistics();
        ValidationStatistics.ComparisonResult result = stats.compareWithControl(
            "ExperimentVsControl",
            experiment,
            control,
            0.05
        );
        
        // Should be statistically significant (p < 0.05)
        assertTrue(result.isSignificant, "Should be statistically significant");
        assertTrue(result.pValue < 0.05);
    }
}
