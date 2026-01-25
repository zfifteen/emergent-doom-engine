package com.emergent.doom.domains.graphcoloring;

import java.util.*;

/**
 * Offline boundary-guided recombination operator for H1 validation.
 * 
 * <p><strong>PURPOSE:</strong> Test hypothesis that boundary interfaces carry
 * exploitable structure by performing crossover at algotype boundaries.</p>
 * 
 * <p><strong>APPROACH:</strong> Offline "what-if" analysis - cells never see
 * recombination results. Keeps EDE substrate pure while testing H1.</p>
 * 
 * <p><strong>FALSIFIABILITY:</strong> H1 passes only if boundary-guided
 * recombination dominates all controls on improvement metrics.</p>
 */
public class BoundaryGuidedRecombination {
    
    /**
     * Result of a recombination operation.
     */
    public static class RecombinationResult {
        public final ColoringState child;
        public final ColoringState parentL;
        public final ColoringState parentR;
        public final int deltaViolations;  // min(parent) - child
        public final boolean improved;     // child better by ≥2 violations
        public final boolean solved;       // child has 0 violations
        
        public RecombinationResult(ColoringState child, ColoringState parentL, 
                                  ColoringState parentR) {
            this.child = child;
            this.parentL = parentL;
            this.parentR = parentR;
            
            int minParent = Math.min(parentL.getViolations(), parentR.getViolations());
            this.deltaViolations = minParent - child.getViolations();
            this.improved = deltaViolations >= 2;
            this.solved = child.getViolations() == 0;
        }
        
        @Override
        public String toString() {
            return String.format("RecombinationResult{parentL=%d, parentR=%d, child=%d, delta=%d, improved=%s, solved=%s}",
                parentL.getViolations(), parentR.getViolations(), child.getViolations(),
                deltaViolations, improved, solved);
        }
    }
    
    private final GraphInstance graph;
    private final Random random;
    private final int repairRounds;
    
    /**
     * Create recombination operator.
     * 
     * @param graph graph instance
     * @param random Random for parent selection and repair
     * @param repairRounds number of MIN_CONFLICT repair rounds (default: 2)
     */
    public BoundaryGuidedRecombination(GraphInstance graph, Random random, int repairRounds) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (random == null) {
            throw new IllegalArgumentException("Random cannot be null");
        }
        if (repairRounds < 0) {
            throw new IllegalArgumentException("Repair rounds must be non-negative");
        }
        
        this.graph = graph;
        this.random = random;
        this.repairRounds = repairRounds;
    }
    
    /**
     * Create operator with default repair rounds (2).
     */
    public BoundaryGuidedRecombination(GraphInstance graph, Random random) {
        this(graph, random, 2);
    }
    
    /**
     * Perform boundary-guided recombination.
     * 
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Select parentL = best among 10 cells left of boundary</li>
     *   <li>Select parentR = best among 10 cells right of boundary</li>
     *   <li>Crossover: top k vertices (by degree) from better parent, rest from other</li>
     *   <li>Apply MIN_CONFLICT repair (1-3 rounds)</li>
     *   <li>Return RecombinationResult</li>
     * </ol>
     * 
     * @param boundary boundary to recombine at
     * @param population current population of cells
     * @return recombination result
     */
    public RecombinationResult recombineAtBoundary(
            BoundaryAnalyzer.Boundary boundary,
            List<GraphColoringCell> population) {
        
        if (boundary == null) {
            throw new IllegalArgumentException("Boundary cannot be null");
        }
        if (population == null || population.isEmpty()) {
            throw new IllegalArgumentException("Population cannot be null or empty");
        }
        
        // Select parents from 10 cells on each side
        ColoringState parentL = selectBestInWindow(boundary.index, true, 10, population);
        ColoringState parentR = selectBestInWindow(boundary.index, false, 10, population);
        
        // Perform crossover
        ColoringState child = performCrossover(parentL, parentR);
        
        // Apply repair
        child = applyRepair(child);
        
        return new RecombinationResult(child, parentL, parentR);
    }
    
    /**
     * Perform recombination at random cut point (control).
     * 
     * @param cutPoint random position for crossover
     * @param population current population
     * @return recombination result
     */
    public RecombinationResult recombineAtRandomCut(
            int cutPoint,
            List<GraphColoringCell> population) {
        
        if (cutPoint < 0 || cutPoint >= population.size() - 1) {
            throw new IllegalArgumentException("Invalid cut point");
        }
        
        ColoringState parentL = selectBestInWindow(cutPoint, true, 10, population);
        ColoringState parentR = selectBestInWindow(cutPoint, false, 10, population);
        
        ColoringState child = performCrossover(parentL, parentR);
        child = applyRepair(child);
        
        return new RecombinationResult(child, parentL, parentR);
    }
    
    /**
     * Select best cell in window around position.
     * 
     * @param position center position
     * @param isLeft if true, look left; else look right
     * @param windowSize number of cells to consider
     * @param population current population
     * @return best ColoringState in window
     */
    private ColoringState selectBestInWindow(
            int position, 
            boolean isLeft, 
            int windowSize,
            List<GraphColoringCell> population) {
        
        int start, end;
        if (isLeft) {
            start = Math.max(0, position - windowSize + 1);
            end = position + 1;
        } else {
            start = position + 1;
            end = Math.min(population.size(), position + 1 + windowSize);
        }
        
        ColoringState best = null;
        for (int i = start; i < end; i++) {
            ColoringState state = population.get(i).readValue();
            if (best == null || state.getViolations() < best.getViolations()) {
                best = state;
            }
        }
        
        // Fallback to random if no cells in window
        if (best == null) {
            best = population.get(random.nextInt(population.size())).readValue();
        }
        
        return best;
    }
    
    /**
     * Perform degree-based crossover.
     * 
     * <p>Take top k vertices (by degree desc) from better parent,
     * take rest from other parent.</p>
     * 
     * @param parentL left parent
     * @param parentR right parent
     * @return child coloring
     */
    private ColoringState performCrossover(ColoringState parentL, ColoringState parentR) {
        int[] verticesByDegree = graph.getVerticesByDegreeDescending();
        int k = (int) Math.ceil(graph.getNumVertices() / 3.0);  // Top third
        
        // Determine better parent
        boolean leftBetter = parentL.getViolations() <= parentR.getViolations();
        ColoringState better = leftBetter ? parentL : parentR;
        ColoringState worse = leftBetter ? parentR : parentL;
        
        // Build child
        int[] childColors = new int[graph.getNumVertices()];
        
        // Take top k vertices from better parent
        for (int i = 0; i < k; i++) {
            int v = verticesByDegree[i];
            childColors[v] = better.getColor(v);
        }
        
        // Take rest from worse parent
        for (int i = k; i < verticesByDegree.length; i++) {
            int v = verticesByDegree[i];
            childColors[v] = worse.getColor(v);
        }
        
        return new ColoringState(childColors, graph);
    }
    
    /**
     * Apply MIN_CONFLICT repair to child.
     * 
     * <p>Randomly pick conflicted vertices and recolor to min-conflict color.</p>
     * 
     * @param state initial state
     * @return repaired state
     */
    private ColoringState applyRepair(ColoringState state) {
        int[] colors = state.getColors();
        
        for (int round = 0; round < repairRounds; round++) {
            // Find conflicted vertices
            Set<Integer> conflicted = new HashSet<>();
            for (int[] edge : graph.getEdges()) {
                int u = edge[0];
                int v = edge[1];
                if (colors[u] == colors[v]) {
                    conflicted.add(u);
                    conflicted.add(v);
                }
            }
            
            if (conflicted.isEmpty()) {
                break;  // No conflicts to repair
            }
            
            // Pick random conflicted vertex
            List<Integer> conflictedList = new ArrayList<>(conflicted);
            int vertex = conflictedList.get(random.nextInt(conflictedList.size()));
            
            // Find best color
            int bestColor = colors[vertex];
            int minConflicts = countVertexConflicts(vertex, colors);
            
            for (int c = 0; c < 3; c++) {
                colors[vertex] = c;
                int conflicts = countVertexConflicts(vertex, colors);
                if (conflicts < minConflicts) {
                    minConflicts = conflicts;
                    bestColor = c;
                }
            }
            
            colors[vertex] = bestColor;
        }
        
        return new ColoringState(colors, graph);
    }
    
    /**
     * Count conflicts for a single vertex.
     */
    private int countVertexConflicts(int vertex, int[] colors) {
        int count = 0;
        for (int[] edge : graph.getEdges()) {
            // Count each edge only once
            if ((edge[0] == vertex || edge[1] == vertex) && 
                colors[edge[0]] == colors[edge[1]]) {
                count++;
            }
        }
        return count;
    }
}
