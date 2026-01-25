package com.emergent.doom.domains.graphcoloring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GraphInstance graph generation and properties.
 */
public class GraphInstanceTest {
    
    @Test
    public void testErdosRenyiGeneration() {
        // Generate deterministic graph
        long seed = 42L;
        int n = 20;
        double p = 0.25;
        
        GraphInstance graph = GraphInstance.generateErdosRenyi(n, p, seed);
        
        assertNotNull(graph);
        assertEquals(n, graph.getNumVertices());
        assertEquals(seed, graph.getSeed());
        assertTrue(graph.getNumEdges() >= 0);
        
        // Same seed should produce same graph
        GraphInstance graph2 = GraphInstance.generateErdosRenyi(n, p, seed);
        assertEquals(graph.getNumEdges(), graph2.getNumEdges());
    }
    
    @Test
    public void testDegreeCalculation() {
        // Create simple graph: 0-1, 1-2, 2-0 (triangle)
        java.util.List<int[]> edges = new java.util.ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{1, 2});
        edges.add(new int[]{2, 0});
        
        GraphInstance graph = new GraphInstance(3, edges, 0L);
        
        assertEquals(2, graph.getDegree(0));  // Connected to 1 and 2
        assertEquals(2, graph.getDegree(1));  // Connected to 0 and 2
        assertEquals(2, graph.getDegree(2));  // Connected to 0 and 1
    }
    
    @Test
    public void testVerticesByDegree() {
        // Create graph with different degrees
        java.util.List<int[]> edges = new java.util.ArrayList<>();
        edges.add(new int[]{0, 1});
        edges.add(new int[]{0, 2});
        edges.add(new int[]{0, 3});  // Vertex 0 has degree 3
        edges.add(new int[]{1, 2});  // Vertex 1 has degree 2
        
        GraphInstance graph = new GraphInstance(4, edges, 0L);
        
        int[] sorted = graph.getVerticesByDegreeDescending();
        
        assertEquals(4, sorted.length);
        assertEquals(0, sorted[0]);  // Highest degree (3)
        assertTrue(sorted[1] == 1 || sorted[1] == 2);  // Degree 2
    }
    
    @Test
    public void testInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> {
            GraphInstance.generateErdosRenyi(-1, 0.5, 0L);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            GraphInstance.generateErdosRenyi(10, 1.5, 0L);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new GraphInstance(3, null, 0L);
        });
    }
}
