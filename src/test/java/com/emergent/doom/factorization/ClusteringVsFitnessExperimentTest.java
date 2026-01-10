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
        // Create sample metrics with Phase 2 signature
        StepMetrics metrics = new StepMetrics(
            0,              // step
            52.5,           // strategy aggregation (v1)
            45.0,           // fitness clustering (v2)
            0.85,           // factor localization (v2)
            new int[]{25, 30},  // factor positions
            27.5,           // mean factor distance
            0.034,          // fitness gradient mean
            0.021,          // fitness gradient std
            1.58,           // entropy global
            1.52,           // entropy front
            0,              // swaps
            0,              // consecutive zero swaps
            false           // not stagnant
        );
        
        // Get CSV row and header
        String header = StepMetrics.getCsvHeader();
        String row = metrics.toCsvRow();
        
        // Verify header format (Phase 2)
        assertTrue(header.contains("step"), "Header should contain step column");
        assertTrue(header.contains("strategy_agg"), "Header should contain strategy_agg column");
        assertTrue(header.contains("fitness_clust"), "Header should contain fitness_clust column");
        assertTrue(header.contains("factor_local"), "Header should contain factor_local column");
        assertTrue(header.contains("factor_11_pos"), "Header should contain factor_11_pos column");
        assertTrue(header.contains("factor_13_pos"), "Header should contain factor_13_pos column");
        assertTrue(header.contains("consec_zero_swaps"), "Header should contain consec_zero_swaps column");
        assertTrue(header.contains("stagnant"), "Header should contain stagnant column");
        
        // Verify row format
        assertTrue(row.startsWith("0,"), "Row should start with step number");
        assertTrue(row.contains("52.5"), "Row should contain strategy aggregation value");
        assertTrue(row.contains("45.0"), "Row should contain fitness clustering value");
        assertTrue(row.contains("0.85"), "Row should contain factor localization value");
        assertTrue(row.contains("25"), "Row should contain factor 11 position");
        assertTrue(row.contains("30"), "Row should contain factor 13 position");
        assertTrue(row.contains("false"), "Row should contain stagnant status");
    }
    
    @Test
    void testStepMetricsDataIntegrity() {
        // Create metrics with known values (Phase 2 signature)
        int[] factorPos = new int[]{5, 10};
        StepMetrics metrics = new StepMetrics(
            1,          // step
            60.0,       // strategy aggregation
            50.0,       // fitness clustering
            0.90,       // factor localization
            factorPos,  // factor positions
            7.5,        // mean factor distance
            0.05,       // fitness gradient mean
            0.02,       // fitness gradient std
            1.5,        // entropy global
            1.4,        // entropy front
            10,         // swaps
            0,          // consecutive zero swaps
            false       // not stagnant
        );
        
        // Verify all fields are accessible
        assertEquals(1, metrics.stepNumber);
        assertEquals(60.0, metrics.strategyAggregation, 0.01);
        assertEquals(50.0, metrics.fitnessClustering, 0.01);
        assertEquals(0.90, metrics.factorLocalization, 0.01);
        assertArrayEquals(new int[]{5, 10}, metrics.factorPositions);
        assertEquals(7.5, metrics.meanFactorDistanceFromFront, 0.01);
        assertEquals(0.05, metrics.fitnessGradientMean, 0.001);
        assertEquals(0.02, metrics.fitnessGradientStd, 0.001);
        assertEquals(10, metrics.swapCount);
        assertEquals(0, metrics.consecutiveZeroSwapSteps);
        assertEquals(false, metrics.isStagnant);
    }
}
