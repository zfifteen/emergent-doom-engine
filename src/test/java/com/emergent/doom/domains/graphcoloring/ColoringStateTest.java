package com.emergent.doom.domains.graphcoloring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ColoringState violations and comparisons.
 */
public class ColoringStateTest {
    
    @Test
    public void testValidColoring() {
        // Create triangle graph
        java.util.List<int[]> edges = new java.util.ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{1, 2});
        edges.add(new int[]{2, 0});
        
        GraphInstance graph = new GraphInstance(3, edges, 0L);
        
        // Valid 3-coloring: all different colors
        int[] validColors = {0, 1, 2};
        ColoringState valid = new ColoringState(validColors, graph);
        
        assertEquals(0, valid.getViolations());
        assertTrue(valid.isValid());
    }
    
    @Test
    public void testInvalidColoring() {
        // Create triangle graph
        java.util.List<int[]> edges = new java.util.ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{1, 2});
        edges.add(new int[]{2, 0});
        
        GraphInstance graph = new GraphInstance(3, edges, 0L);
        
        // Invalid: all same color
        int[] invalidColors = {0, 0, 0};
        ColoringState invalid = new ColoringState(invalidColors, graph);
        
        assertEquals(3, invalid.getViolations());  // All 3 edges violated
        assertFalse(invalid.isValid());
    }
    
    @Test
    public void testBucketComparison() {
        GraphInstance graph = GraphInstance.generateErdosRenyi(10, 0.3, 0L);
        
        // Create colorings with different violations
        int[] colors1 = new int[10];
        int[] colors2 = new int[10];
        int[] colors3 = new int[10];
        
        // Fill with patterns to get different violation counts
        for (int i = 0; i < 10; i++) {
            colors1[i] = 0;  // All same - max violations
            colors2[i] = i % 3;  // More varied
            colors3[i] = i % 2;  // Some violations
        }
        
        ColoringState state1 = new ColoringState(colors1, graph);
        ColoringState state2 = new ColoringState(colors2, graph);
        ColoringState state3 = new ColoringState(colors3, graph);
        
        // Bucket comparison should work
        assertTrue(state1.getBucket() >= 0);
        assertTrue(state2.getBucket() >= 0);
    }
    
    @Test
    public void testCompactString() {
        GraphInstance graph = GraphInstance.generateErdosRenyi(5, 0.3, 0L);
        int[] colors = {0, 1, 2, 0, 1};
        
        ColoringState state = new ColoringState(colors, graph);
        String compact = state.toCompactString();
        
        assertEquals("01201", compact);
        assertEquals(5, compact.length());
    }
    
    @Test
    public void testCompareTo() {
        GraphInstance graph = GraphInstance.generateErdosRenyi(10, 0.3, 42L);
        
        int[] betterColors = new int[10];
        int[] worseColors = new int[10];
        
        for (int i = 0; i < 10; i++) {
            betterColors[i] = i % 3;  // More varied
            worseColors[i] = 0;  // All same
        }
        
        ColoringState better = new ColoringState(betterColors, graph);
        ColoringState worse = new ColoringState(worseColors, graph);
        
        assertTrue(better.compareTo(worse) < 0);  // Better has lower violations
        assertTrue(worse.compareTo(better) > 0);
    }
}
