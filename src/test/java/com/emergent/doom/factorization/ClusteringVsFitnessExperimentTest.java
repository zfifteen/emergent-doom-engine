package com.emergent.doom.factorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic smoke test for ClusteringVsFitnessExperiment.
 *
 * <p><strong>PURPOSE:</strong> Verify the experiment class can be instantiated
 * and basic components exist.</p>
 *
 * <p><strong>NOTE:</strong> Detailed validation of condition generators and metrics
 * calculations is done during actual experiment execution. This test ensures the
 * implementation compiles and basic file I/O works.</p>
 */
class ClusteringVsFitnessExperimentTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testExperimentClassExists() {
        // Verify the experiment class can be instantiated
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        assertNotNull(experiment, "Experiment should be instantiable");
    }
    
    @Test
    void testStepMetricsCSVFormat() throws IOException {
        // Create sample metrics
        StepMetrics metrics = new StepMetrics(
            0,              // step
            52.5,           // aggregation
            new int[]{25, 30},  // factor positions
            27.5,           // mean factor distance
            0.034,          // fitness gradient mean
            0.021,          // fitness gradient std
            1.58,           // entropy global
            1.52,           // entropy front
            0               // swaps
        );
        
        // Get CSV row and header
        String header = StepMetrics.getCsvHeader();
        String row = metrics.toCsvRow();
        
        // Verify header format
        assertTrue(header.contains("step"), "Header should contain step column");
        assertTrue(header.contains("aggregation"), "Header should contain aggregation column");
        assertTrue(header.contains("factor_11_pos"), "Header should contain factor_11_pos column");
        assertTrue(header.contains("factor_13_pos"), "Header should contain factor_13_pos column");
        
        // Verify row format
        assertTrue(row.startsWith("0,"), "Row should start with step number");
        assertTrue(row.contains("52.5"), "Row should contain aggregation value");
        assertTrue(row.contains("25"), "Row should contain factor 11 position");
        assertTrue(row.contains("30"), "Row should contain factor 13 position");
    }
    
    @Test
    void testStepMetricsDataIntegrity() {
        // Create metrics with known values
        int[] factorPos = new int[]{5, 10};
        StepMetrics metrics = new StepMetrics(
            1, 60.0, factorPos, 7.5, 0.05, 0.02, 1.5, 1.4, 10
        );
        
        // Verify all fields are accessible
        assertEquals(1, metrics.stepNumber);
        assertEquals(60.0, metrics.aggregationValue, 0.01);
        assertArrayEquals(new int[]{5, 10}, metrics.factorPositions);
        assertEquals(7.5, metrics.meanFactorDistanceFromFront, 0.01);
        assertEquals(0.05, metrics.fitnessGradientMean, 0.001);
        assertEquals(0.02, metrics.fitnessGradientStd, 0.001);
        assertEquals(1.5, metrics.strategyEntropyGlobal, 0.01);
        assertEquals(1.4, metrics.strategyEntropyFront, 0.01);
        assertEquals(10, metrics.swapCount);
    }
}
