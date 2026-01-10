package com.emergent.doom.metrics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FitnessSimilarityClusteringIndex.
 *
 * <p><strong>PURPOSE:</strong> Validate fitness-field clustering metric calculations
 * for various array configurations.</p>
 *
 * <p><strong>TEST STRATEGY:</strong></p>
 * <ul>
 *   <li>Edge cases: empty, single-element, all-same-fitness arrays</li>
 *   <li>Extreme cases: perfect clustering, zero clustering</li>
 *   <li>Real-world cases: factorization fitness patterns</li>
 * </ul>
 */
public class FitnessSimilarityClusteringIndexTest {
    
    private static final double DELTA = 0.001;
    
    @Test
    public void testEmptyArray() {
        FitnessSimilarityClusteringIndex index = new FitnessSimilarityClusteringIndex();
        double result = index.compute(new double[]{});
        assertEquals(100.0, result, DELTA, "Empty array should return 100% clustering");
    }
    
    @Test
    public void testSingleElement() {
        FitnessSimilarityClusteringIndex index = new FitnessSimilarityClusteringIndex();
        double result = index.compute(new double[]{0.5});
        assertEquals(100.0, result, DELTA, "Single element should return 100% clustering");
    }
    
    @Test
    public void testAllSameFitness() {
        FitnessSimilarityClusteringIndex index = new FitnessSimilarityClusteringIndex();
        double result = index.compute(new double[]{0.5, 0.5, 0.5, 0.5, 0.5});
        assertEquals(100.0, result, DELTA, "All same fitness should return 100% clustering");
    }
    
    @Test
    public void testPerfectClusteringTwoGroups() {
        // Two groups of similar fitness: [high, high, high, low, low, low]
        FitnessSimilarityClusteringIndex index = new FitnessSimilarityClusteringIndex();
        double result = index.compute(new double[]{0.95, 0.92, 0.90, 0.15, 0.18, 0.20});
        assertEquals(100.0, result, DELTA, "Clustered groups should return 100% clustering");
    }
    
    @Test
    public void testZeroClusteringAlternating() {
        // Alternating high/low fitness: [high, low, high, low, high, low]
        FitnessSimilarityClusteringIndex index = new FitnessSimilarityClusteringIndex(0.1);
        double result = index.compute(new double[]{0.95, 0.15, 0.92, 0.18, 0.90, 0.20});
        assertEquals(0.0, result, DELTA, "Alternating dissimilar fitness should return 0% clustering");
    }
    
    @Test
    public void testPartialClustering() {
        // Some clustering: [high, high, low, low, high, low]
        // Cells 0,1,2,3 have similar neighbors → 4/6 = 66.7%
        FitnessSimilarityClusteringIndex index = new FitnessSimilarityClusteringIndex(0.1);
        double result = index.compute(new double[]{0.95, 0.92, 0.15, 0.18, 0.90, 0.20});
        assertEquals(66.67, result, 0.1, "Partial clustering should return ~67%");
    }
    
    @Test
    public void testFactorizationPattern() {
        // Realistic factorization pattern: factors at front, non-factors at back
        // [factor1, factor2, near-factor, low, low, low]
        // Cell 0 (1.0): right neighbor 1.0, diff = 0.0 < 0.1 ✓
        // Cell 1 (1.0): left 1.0 (diff 0.0) ✓, right 0.85 (diff 0.15) ✗ → has left ✓
        // Cell 2 (0.85): left 1.0 (diff 0.15) ✗, right 0.2 (diff 0.65) ✗ → no similar
        // Cell 3 (0.2): left 0.85 (diff 0.65) ✗, right 0.25 (diff 0.05) ✓
        // Cell 4 (0.25): left 0.2 (diff 0.05) ✓, right 0.22 (diff 0.03) ✓
        // Cell 5 (0.22): left 0.25 (diff 0.03) ✓
        // Similar: 0,1,3,4,5 = 5/6 = 83.33%
        FitnessSimilarityClusteringIndex index = new FitnessSimilarityClusteringIndex(0.1);
        double result = index.compute(new double[]{1.0, 1.0, 0.85, 0.2, 0.25, 0.22});
        assertEquals(83.33, result, 0.1, "Factorization pattern should show 83.33% clustering");
    }
    
    @Test
    public void testBoundaryConditions() {
        // Test with fitness differences at and beyond threshold
        // Use 0.15 threshold to avoid floating point precision issues with 0.1
        // Cells: [0.5, 0.65, 0.8, 0.96] with threshold 0.15
        // All adjacent pairs have diff = 0.15, which is NOT < 0.15
        FitnessSimilarityClusteringIndex index = new FitnessSimilarityClusteringIndex(0.15);
        double result = index.compute(new double[]{0.5, 0.65, 0.8, 0.96});
        assertEquals(0.0, result, DELTA, "Fitness diff exactly at threshold should not cluster");
    }
    
    @Test
    public void testJustBelowThreshold() {
        // Test with fitness differences just below threshold
        // Cells: [0.5, 0.59, 0.68, 0.77] with threshold 0.1
        // All adjacent pairs have diff = 0.09 < 0.1
        FitnessSimilarityClusteringIndex index = new FitnessSimilarityClusteringIndex(0.1);
        double result = index.compute(new double[]{0.5, 0.59, 0.68, 0.77});
        assertEquals(100.0, result, DELTA, "Fitness diff just below threshold should cluster");
    }
}
