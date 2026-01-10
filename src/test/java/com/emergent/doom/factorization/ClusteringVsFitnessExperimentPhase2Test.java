package com.emergent.doom.factorization;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Unit tests for Phase 2 correctness fixes in ClusteringVsFitnessExperiment.
 *
 * <p><strong>PURPOSE:</strong> Validate critical fixes for experimental validity:</p>
 * <ul>
 *   <li>Factor presence guarantee (Issue #2)</li>
 *   <li>Stagnation detection (Issue #5.2)</li>
 *   <li>Convergence position consistency (Issue #5.1)</li>
 * </ul>
 *
 * <p><strong>REFERENCE:</strong> EXPERIMENT_SETUP_AUDIT.md</p>
 */
public class ClusteringVsFitnessExperimentPhase2Test {
    
    @Test
    public void testFactorPresenceInC1() throws Exception {
        // Test that C1 (baseline) always has factors 11 and 13
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        
        // Use reflection to call private generateC1Baseline method
        Method method = ClusteringVsFitnessExperiment.class.getDeclaredMethod(
            "generateC1Baseline", long.class
        );
        method.setAccessible(true);
        
        // Test across multiple seeds
        for (long seed = 1; seed <= 30; seed++) {
            @SuppressWarnings("unchecked")
            List<FactorCell> cells = (List<FactorCell>) method.invoke(experiment, seed);
            
            boolean has11 = false;
            boolean has13 = false;
            
            for (FactorCell cell : cells) {
                if (cell.readValue() == 11) has11 = true;
                if (cell.readValue() == 13) has13 = true;
            }
            
            assertTrue(has11, "C1 with seed " + seed + " should have factor 11");
            assertTrue(has13, "C1 with seed " + seed + " should have factor 13");
        }
    }
    
    @Test
    public void testFactorPresenceInC2() throws Exception {
        // Test that C2 (high aggregation) always has factors 11 and 13
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        
        Method method = ClusteringVsFitnessExperiment.class.getDeclaredMethod(
            "generateC2HighAggregation", long.class
        );
        method.setAccessible(true);
        
        for (long seed = 1; seed <= 30; seed++) {
            @SuppressWarnings("unchecked")
            List<FactorCell> cells = (List<FactorCell>) method.invoke(experiment, seed);
            
            boolean has11 = false;
            boolean has13 = false;
            
            for (FactorCell cell : cells) {
                if (cell.readValue() == 11) has11 = true;
                if (cell.readValue() == 13) has13 = true;
            }
            
            assertTrue(has11, "C2 with seed " + seed + " should have factor 11");
            assertTrue(has13, "C2 with seed " + seed + " should have factor 13");
        }
    }
    
    @Test
    public void testFactorPresenceInC3() throws Exception {
        // Test that C3 (zero aggregation) always has factors 11 and 13
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        
        Method method = ClusteringVsFitnessExperiment.class.getDeclaredMethod(
            "generateC3ZeroAggregation", long.class
        );
        method.setAccessible(true);
        
        for (long seed = 1; seed <= 30; seed++) {
            @SuppressWarnings("unchecked")
            List<FactorCell> cells = (List<FactorCell>) method.invoke(experiment, seed);
            
            boolean has11 = false;
            boolean has13 = false;
            
            for (FactorCell cell : cells) {
                if (cell.readValue() == 11) has11 = true;
                if (cell.readValue() == 13) has13 = true;
            }
            
            assertTrue(has11, "C3 with seed " + seed + " should have factor 11");
            assertTrue(has13, "C3 with seed " + seed + " should have factor 13");
        }
    }
    
    @Test
    public void testFactorAbsenceInC4() throws Exception {
        // Test that C4 (fitness control) NEVER has factors 11 or 13
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        
        Method method = ClusteringVsFitnessExperiment.class.getDeclaredMethod(
            "generateC4FitnessControl", long.class
        );
        method.setAccessible(true);
        
        for (long seed = 1; seed <= 30; seed++) {
            @SuppressWarnings("unchecked")
            List<FactorCell> cells = (List<FactorCell>) method.invoke(experiment, seed);
            
            for (FactorCell cell : cells) {
                int value = cell.readValue();
                assertNotEquals(11, value, "C4 with seed " + seed + " should NOT have factor 11");
                assertNotEquals(13, value, "C4 with seed " + seed + " should NOT have factor 13");
            }
        }
    }
    
    @Test
    public void testFactorPresenceInC5() throws Exception {
        // Test that C5 (homogeneous) always has factors 11 and 13
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        
        Method method = ClusteringVsFitnessExperiment.class.getDeclaredMethod(
            "generateC5Homogeneous", long.class
        );
        method.setAccessible(true);
        
        for (long seed = 1; seed <= 30; seed++) {
            @SuppressWarnings("unchecked")
            List<FactorCell> cells = (List<FactorCell>) method.invoke(experiment, seed);
            
            boolean has11 = false;
            boolean has13 = false;
            
            for (FactorCell cell : cells) {
                if (cell.readValue() == 11) has11 = true;
                if (cell.readValue() == 13) has13 = true;
            }
            
            assertTrue(has11, "C5 with seed " + seed + " should have factor 11");
            assertTrue(has13, "C5 with seed " + seed + " should have factor 13");
        }
    }
    
    @Test
    public void testConvergencePositionConstant() throws Exception {
        // Test that CONVERGENCE_POSITION constant is accessible and set correctly
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        
        java.lang.reflect.Field field = ClusteringVsFitnessExperiment.class.getDeclaredField("CONVERGENCE_POSITION");
        field.setAccessible(true);
        int convergencePos = (int) field.get(experiment);
        
        assertEquals(4, convergencePos, "CONVERGENCE_POSITION should be 4 (positions [0,4])");
    }
    
    @Test
    public void testStagnationThresholdConstant() throws Exception {
        // Test that STAGNATION_THRESHOLD constant is accessible and set correctly
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        
        java.lang.reflect.Field field = ClusteringVsFitnessExperiment.class.getDeclaredField("STAGNATION_THRESHOLD");
        field.setAccessible(true);
        int stagnationThreshold = (int) field.get(experiment);
        
        assertEquals(20, stagnationThreshold, "STAGNATION_THRESHOLD should be 20 steps");
    }
}
