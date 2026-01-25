package com.emergent.doom.domains.graphcoloring;

import java.util.*;

/**
 * Represents an undirected graph instance for 3-coloring experiments.
 * 
 * <p><strong>PURPOSE:</strong> Generate and store graph structure for H1 validation
 * experiments using Erdős-Rényi random graph model.</p>
 * 
 * <p><strong>GRAPH SIZES:</strong> Recommended n=20 (primary), n=15 (easier), n=25 (harder)
 * with edge probability p ≈ 0.22-0.28 to create wide fitness plateaus.</p>
 */
public class GraphInstance {
    
    private final int numVertices;
    private final List<int[]> edges;  // Each edge is [u, v]
    private final int[] degrees;
    private final long seed;
    
    /**
     * Create a graph instance from explicit edge list.
     * 
     * @param numVertices number of vertices
     * @param edges list of edges (each edge is [u, v])
     * @param seed random seed used for generation
     */
    public GraphInstance(int numVertices, List<int[]> edges, long seed) {
        if (numVertices <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive");
        }
        if (edges == null) {
            throw new IllegalArgumentException("Edges cannot be null");
        }
        
        this.numVertices = numVertices;
        this.edges = new ArrayList<>(edges);
        this.seed = seed;
        this.degrees = new int[numVertices];
        
        // Calculate degrees
        for (int[] edge : edges) {
            if (edge.length != 2) {
                throw new IllegalArgumentException("Each edge must have exactly 2 vertices");
            }
            int u = edge[0];
            int v = edge[1];
            if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
                throw new IllegalArgumentException("Invalid vertex index in edge");
            }
            degrees[u]++;
            degrees[v]++;
        }
    }
    
    /**
     * Generate Erdős-Rényi random graph G(n, p).
     * 
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>For each pair of vertices (u, v) where u < v</li>
     *   <li>Add edge with probability p using seeded Random</li>
     *   <li>Store as undirected edge list</li>
     * </ol>
     * 
     * @param numVertices number of vertices (n)
     * @param edgeProbability probability of each edge (p), must be in [0, 1]
     * @param seed random seed for reproducibility
     * @return GraphInstance with generated edges
     */
    public static GraphInstance generateErdosRenyi(int numVertices, double edgeProbability, long seed) {
        if (numVertices <= 0) {
            throw new IllegalArgumentException("Number of vertices must be positive");
        }
        if (edgeProbability < 0.0 || edgeProbability > 1.0) {
            throw new IllegalArgumentException("Edge probability must be in [0, 1]");
        }
        
        Random random = new Random(seed);
        List<int[]> edges = new ArrayList<>();
        
        // Generate edges for all pairs (u, v) where u < v
        for (int u = 0; u < numVertices; u++) {
            for (int v = u + 1; v < numVertices; v++) {
                if (random.nextDouble() < edgeProbability) {
                    edges.add(new int[]{u, v});
                }
            }
        }
        
        return new GraphInstance(numVertices, edges, seed);
    }
    
    /**
     * Get number of vertices in the graph.
     * 
     * @return number of vertices
     */
    public int getNumVertices() {
        return numVertices;
    }
    
    /**
     * Get list of edges.
     * 
     * @return unmodifiable list of edges
     */
    public List<int[]> getEdges() {
        return Collections.unmodifiableList(edges);
    }
    
    /**
     * Get degree of a vertex.
     * 
     * @param vertex vertex index
     * @return degree of vertex
     */
    public int getDegree(int vertex) {
        if (vertex < 0 || vertex >= numVertices) {
            throw new IllegalArgumentException("Invalid vertex index: " + vertex);
        }
        return degrees[vertex];
    }
    
    /**
     * Get number of edges in the graph.
     * 
     * @return number of edges
     */
    public int getNumEdges() {
        return edges.size();
    }
    
    /**
     * Get seed used to generate this graph.
     * 
     * @return random seed
     */
    public long getSeed() {
        return seed;
    }
    
    /**
     * Get vertices sorted by degree (descending).
     * 
     * <p>Used for degree-based crossover in recombination operator.</p>
     * 
     * @return array of vertex indices sorted by descending degree
     */
    public int[] getVerticesByDegreeDescending() {
        Integer[] vertices = new Integer[numVertices];
        for (int i = 0; i < numVertices; i++) {
            vertices[i] = i;
        }
        
        // Sort by degree descending
        Arrays.sort(vertices, (a, b) -> Integer.compare(degrees[b], degrees[a]));
        
        // Convert to int[]
        int[] result = new int[numVertices];
        for (int i = 0; i < numVertices; i++) {
            result[i] = vertices[i];
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "GraphInstance{n=" + numVertices + ", m=" + edges.size() + ", seed=" + seed + "}";
    }
}
