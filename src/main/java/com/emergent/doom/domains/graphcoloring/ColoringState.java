package com.emergent.doom.domains.graphcoloring;

import java.util.Arrays;

/**
 * Represents a candidate 3-coloring solution with violation tracking.
 * 
 * <p><strong>PURPOSE:</strong> Store vertex color assignments and compute
 * violations for H1 validation experiments.</p>
 * 
 * <p><strong>COLORS:</strong> {0, 1, 2} representing three colors.</p>
 * 
 * <p><strong>FITNESS:</strong> Violations = number of edges (u,v) where
 * color[u] == color[v]. Lower is better, 0 = valid coloring.</p>
 */
public class ColoringState implements Comparable<ColoringState> {
    
    private final int[] colors;
    private final int violations;
    private final int bucket;  // violations / bucketSize for plateau engineering
    
    private static final int DEFAULT_BUCKET_SIZE = 2;
    
    /**
     * Create a coloring state with explicit color assignments.
     * 
     * @param colors array of color assignments (values in {0, 1, 2})
     * @param graph graph instance to compute violations
     * @param bucketSize bucket size for plateau engineering (default: 2)
     */
    public ColoringState(int[] colors, GraphInstance graph, int bucketSize) {
        if (colors == null) {
            throw new IllegalArgumentException("Colors cannot be null");
        }
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (colors.length != graph.getNumVertices()) {
            throw new IllegalArgumentException(
                "Colors array length must match number of vertices");
        }
        
        this.colors = Arrays.copyOf(colors, colors.length);
        this.violations = computeViolations(colors, graph);
        this.bucket = violations / bucketSize;
    }
    
    /**
     * Create a coloring state with default bucket size.
     * 
     * @param colors array of color assignments
     * @param graph graph instance
     */
    public ColoringState(int[] colors, GraphInstance graph) {
        this(colors, graph, DEFAULT_BUCKET_SIZE);
    }
    
    /**
     * Compute number of violations for a coloring.
     * 
     * <p>Violation = edge (u,v) where color[u] == color[v].</p>
     * 
     * @param colors color assignments
     * @param graph graph instance
     * @return number of violations
     */
    private static int computeViolations(int[] colors, GraphInstance graph) {
        int count = 0;
        for (int[] edge : graph.getEdges()) {
            int u = edge[0];
            int v = edge[1];
            if (colors[u] == colors[v]) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get color assignment for a vertex.
     * 
     * @param vertex vertex index
     * @return color (0, 1, or 2)
     */
    public int getColor(int vertex) {
        if (vertex < 0 || vertex >= colors.length) {
            throw new IllegalArgumentException("Invalid vertex index: " + vertex);
        }
        return colors[vertex];
    }
    
    /**
     * Get all color assignments.
     * 
     * @return copy of color array
     */
    public int[] getColors() {
        return Arrays.copyOf(colors, colors.length);
    }
    
    /**
     * Get number of violations.
     * 
     * @return violation count
     */
    public int getViolations() {
        return violations;
    }
    
    /**
     * Get bucket for plateau comparison.
     * 
     * @return bucket index (violations / bucketSize)
     */
    public int getBucket() {
        return bucket;
    }
    
    /**
     * Check if this is a valid coloring (zero violations).
     * 
     * @return true if violations == 0
     */
    public boolean isValid() {
        return violations == 0;
    }
    
    /**
     * Get compact string representation of coloring.
     * 
     * <p>Format: "012201..." - one digit per vertex.</p>
     * 
     * @return compact string
     */
    public String toCompactString() {
        StringBuilder sb = new StringBuilder(colors.length);
        for (int color : colors) {
            sb.append(color);
        }
        return sb.toString();
    }
    
    /**
     * Compare colorings using bucketed violations for plateaus.
     * 
     * <p>Comparison order:</p>
     * <ol>
     *   <li>By bucket (lower bucket = better)</li>
     *   <li>By violations within bucket (lower = better)</li>
     * </ol>
     * 
     * @param other other coloring state
     * @return negative if this < other, zero if equal, positive if this > other
     */
    @Override
    public int compareTo(ColoringState other) {
        // First compare by bucket
        int bucketCmp = Integer.compare(this.bucket, other.bucket);
        if (bucketCmp != 0) {
            return bucketCmp;
        }
        // Then by violations within bucket
        return Integer.compare(this.violations, other.violations);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ColoringState)) return false;
        ColoringState other = (ColoringState) obj;
        return Arrays.equals(this.colors, other.colors);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(colors);
    }
    
    @Override
    public String toString() {
        return "ColoringState{violations=" + violations + 
               ", bucket=" + bucket + 
               ", colors=" + toCompactString() + "}";
    }
}
