package com.emergent.doom.metrics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FactorLocalizationIndex.
 *
 * <p><strong>PURPOSE:</strong> Validate factor localization (pattern formation)
 * metric calculations for various factor positions.</p>
 *
 * <p><strong>TEST STRATEGY:</strong></p>
 * <ul>
 *   <li>Edge cases: missing factors, single factor</li>
 *   <li>Extreme cases: adjacent factors, opposite ends</li>
 *   <li>Real-world cases: typical factorization scenarios</li>
 * </ul>
 */
public class FactorLocalizationIndexTest {
    
    private static final double DELTA = 0.001;
    private static final int ARRAY_SIZE = 50;
    
    @Test
    public void testBothFactorsMissing() {
        FactorLocalizationIndex index = new FactorLocalizationIndex(ARRAY_SIZE);
        double result = index.compute(new int[]{-1, -1});
        assertEquals(0.0, result, DELTA, "No factors → no localization");
    }
    
    @Test
    public void testOneFactorMissing() {
        FactorLocalizationIndex index = new FactorLocalizationIndex(ARRAY_SIZE);
        double result1 = index.compute(new int[]{5, -1});
        double result2 = index.compute(new int[]{-1, 10});
        assertEquals(0.0, result1, DELTA, "One factor missing → no localization");
        assertEquals(0.0, result2, DELTA, "One factor missing → no localization");
    }
    
    @Test
    public void testAdjacentFactors() {
        // Factors at positions 2 and 3 → distance = 1
        FactorLocalizationIndex index = new FactorLocalizationIndex(ARRAY_SIZE);
        double result = index.compute(new int[]{2, 3});
        double expected = 1.0 - (1.0 / 49.0); // 1.0 - 0.0204 = 0.9796
        assertEquals(expected, result, DELTA, "Adjacent factors → high localization");
        assertTrue(result > 0.97, "Adjacent factors should be > 0.97");
    }
    
    @Test
    public void testOppositeEnds() {
        // Factors at positions 0 and 49 → distance = 49 (maximum)
        FactorLocalizationIndex index = new FactorLocalizationIndex(ARRAY_SIZE);
        double result = index.compute(new int[]{0, 49});
        assertEquals(0.0, result, DELTA, "Opposite ends → zero localization");
    }
    
    @Test
    public void testMidArray() {
        // Factors at positions 20 and 30 → distance = 10
        FactorLocalizationIndex index = new FactorLocalizationIndex(ARRAY_SIZE);
        double result = index.compute(new int[]{20, 30});
        double expected = 1.0 - (10.0 / 49.0); // 1.0 - 0.204 = 0.796
        assertEquals(expected, result, DELTA, "Mid-array factors → moderate localization");
    }
    
    @Test
    public void testOrderIndependence() {
        // Result should be same regardless of factor order
        FactorLocalizationIndex index = new FactorLocalizationIndex(ARRAY_SIZE);
        double result1 = index.compute(new int[]{10, 20});
        double result2 = index.compute(new int[]{20, 10});
        assertEquals(result1, result2, DELTA, "Localization should be order-independent");
    }
    
    @Test
    public void testFrontPositions() {
        // Convergence criterion: both factors in [0, 4]
        // Test various front configurations
        FactorLocalizationIndex index = new FactorLocalizationIndex(ARRAY_SIZE);
        
        double loc_0_4 = index.compute(new int[]{0, 4}); // distance = 4
        double loc_1_3 = index.compute(new int[]{1, 3}); // distance = 2
        double loc_2_2 = index.compute(new int[]{2, 2}); // distance = 0 (same position, should not happen)
        
        assertTrue(loc_0_4 > 0.9, "Factors at [0,4] should have high localization");
        assertTrue(loc_1_3 > loc_0_4, "Factors at [1,3] should have higher localization than [0,4]");
        assertEquals(1.0, loc_2_2, DELTA, "Same position → perfect localization");
    }
    
    @Test
    public void testSmallArray() {
        // Test with smaller array size
        FactorLocalizationIndex index = new FactorLocalizationIndex(10);
        double result = index.compute(new int[]{2, 7}); // distance = 5, max = 9
        double expected = 1.0 - (5.0 / 9.0); // = 0.444
        assertEquals(expected, result, DELTA, "Small array localization");
    }
    
    @Test
    public void testLargeArray() {
        // Test with larger array size
        FactorLocalizationIndex index = new FactorLocalizationIndex(100);
        double result = index.compute(new int[]{45, 55}); // distance = 10, max = 99
        double expected = 1.0 - (10.0 / 99.0); // = 0.899
        assertEquals(expected, result, DELTA, "Large array localization");
    }
    
    @Test
    public void testValidRange() {
        // All valid inputs should produce results in [0.0, 1.0]
        FactorLocalizationIndex index = new FactorLocalizationIndex(ARRAY_SIZE);
        
        for (int i = 0; i < ARRAY_SIZE; i++) {
            for (int j = i; j < ARRAY_SIZE; j++) {
                double result = index.compute(new int[]{i, j});
                assertTrue(result >= 0.0 && result <= 1.0, 
                    String.format("Localization for [%d,%d] should be in [0,1]", i, j));
            }
        }
    }
    
    @Test
    public void testInvalidInputTooFewPositions() {
        FactorLocalizationIndex index = new FactorLocalizationIndex(ARRAY_SIZE);
        assertThrows(IllegalArgumentException.class, () -> {
            index.compute(new int[]{5}); // Only one position
        }, "Should throw exception for fewer than 2 positions");
    }
}
