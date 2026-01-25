package com.emergent.doom.domains.graphcoloring;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.cell.NeighborhoodView;
import com.emergent.doom.group.CellStatus;

import java.util.Optional;
import java.util.Random;

/**
 * Cell implementation for graph 3-coloring domain.
 * 
 * <p><strong>PURPOSE:</strong> Implement cell-based graph coloring with algotype-specific
 * behavior for H1 validation experiments.</p>
 * 
 * <p><strong>ARCHITECTURE:</strong> Extends AbstractCell with ColoringState as value type
 * and ColoringAlgotype as algotype type. Algotype travels with cell during swaps.</p>
 */
public class GraphColoringCell extends AbstractCell<ColoringState, ColoringAlgotype> {
    
    // Immutable intrinsic properties
    private final ColoringState state;
    private final ColoringAlgotype algotype;
    private final GraphInstance graph;
    private final Random random;
    
    // Mutable positional state
    private int currentPosition;
    private CellStatus status;
    
    /**
     * Create a graph coloring cell.
     * 
     * @param state coloring state (candidate solution)
     * @param algotype behavioral strategy
     * @param graph graph instance
     * @param initialPosition starting position
     * @param random Random instance for stochastic algotypes
     */
    public GraphColoringCell(
            ColoringState state, 
            ColoringAlgotype algotype, 
            GraphInstance graph,
            int initialPosition,
            Random random) {
        
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        if (algotype == null) {
            throw new IllegalArgumentException("Algotype cannot be null");
        }
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (random == null) {
            throw new IllegalArgumentException("Random cannot be null");
        }
        if (initialPosition < 0) {
            throw new IllegalArgumentException("Initial position must be non-negative");
        }
        
        this.state = state;
        this.algotype = algotype;
        this.graph = graph;
        this.currentPosition = initialPosition;
        this.status = CellStatus.ACTIVE;
        this.random = random;
    }
    
    @Override
    public ColoringAlgotype readAlgotype() {
        return algotype;
    }
    
    @Override
    public ColoringState readValue() {
        return state;
    }
    
    @Override
    public int readCurrentPosition() {
        return currentPosition;
    }
    
    @Override
    public void updatePositionTo(int newPosition) {
        if (newPosition < 0) {
            throw new IllegalArgumentException("Position must be non-negative");
        }
        this.currentPosition = newPosition;
    }
    
    @Override
    public CellStatus readStatus() {
        return status;
    }
    
    @Override
    public void updateStatusTo(CellStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
    }
    
    /**
     * Determine if cell should move based on algotype-specific logic.
     * 
     * <p>For graph coloring, cells always want to move if they have violations
     * or if algotype allows exploration (e.g., RANDOM_WALK).</p>
     */
    @Override
    public boolean shouldMoveGiven(NeighborhoodView<ColoringState, ColoringAlgotype> neighbors) {
        switch (algotype) {
            case GREEDY_REPAIR:
            case MIN_CONFLICT:
            case BACKTRACK_LIGHT:
                // Move if have violations
                return state.getViolations() > 0 && neighbors.hasNeighbors();
            
            case RANDOM_WALK:
                // Always move (high mobility)
                return neighbors.hasNeighbors();
            
            default:
                return false;
        }
    }
    
    /**
     * Calculate target position for swap.
     * 
     * <p>Graph coloring cells use simple random neighbor selection since the actual
     * optimization happens through recoloring, not position-based swaps.</p>
     */
    @Override
    public Optional<Integer> calculateTargetPositionGiven(
            NeighborhoodView<ColoringState, ColoringAlgotype> neighbors) {
        
        // For graph coloring, swap with random neighbor
        // (optimization happens through state changes, not positional swaps)
        Optional<AbstractCell<ColoringState, ColoringAlgotype>> leftOpt = neighbors.getLeftNeighbor();
        Optional<AbstractCell<ColoringState, ColoringAlgotype>> rightOpt = neighbors.getRightNeighbor();
        
        if (leftOpt.isEmpty() && rightOpt.isEmpty()) {
            return Optional.empty();
        }
        
        // Pick random neighbor
        AbstractCell<ColoringState, ColoringAlgotype> chosen;
        if (leftOpt.isPresent() && rightOpt.isPresent()) {
            chosen = random.nextBoolean() ? leftOpt.get() : rightOpt.get();
        } else if (leftOpt.isPresent()) {
            chosen = leftOpt.get();
        } else {
            chosen = rightOpt.get();
        }
        
        // Swap if chosen neighbor has better fitness (lower violations)
        if (chosen.readValue().getViolations() < this.state.getViolations()) {
            return Optional.of(chosen.readCurrentPosition());
        }
        
        return Optional.empty();
    }
    
    @Override
    public boolean canInitiateSwap() {
        return status == CellStatus.ACTIVE;
    }
    
    @Override
    public boolean canAcceptSwapFrom(AbstractCell<ColoringState, ColoringAlgotype> initiator) {
        return status == CellStatus.ACTIVE || status == CellStatus.FREEZE;
    }
    
    @Override
    public boolean hasGreaterValueThan(AbstractCell<ColoringState, ColoringAlgotype> other) {
        return this.state.compareTo(other.readValue()) > 0;
    }
    
    /**
     * Apply algotype-specific improvement to coloring.
     * 
     * <p>This modifies the coloring in-place based on the algotype's strategy.</p>
     * 
     * @return new ColoringState after improvement step
     */
    public ColoringState applyImprovementStep() {
        int[] colors = state.getColors();
        
        switch (algotype) {
            case GREEDY_REPAIR:
                return applyGreedyRepair(colors);
            
            case MIN_CONFLICT:
                return applyMinConflict(colors);
            
            case RANDOM_WALK:
                return applyRandomWalk(colors);
            
            case BACKTRACK_LIGHT:
                return applyBacktrackLight(colors);
            
            default:
                return state;
        }
    }
    
    /**
     * GREEDY_REPAIR: Fix highest-conflict vertex to best color.
     */
    private ColoringState applyGreedyRepair(int[] colors) {
        int[] conflicts = new int[graph.getNumVertices()];
        
        // Count conflicts for each vertex
        for (int[] edge : graph.getEdges()) {
            int u = edge[0];
            int v = edge[1];
            if (colors[u] == colors[v]) {
                conflicts[u]++;
                conflicts[v]++;
            }
        }
        
        // Find highest-conflict vertex
        int maxVertex = 0;
        for (int i = 1; i < conflicts.length; i++) {
            if (conflicts[i] > conflicts[maxVertex]) {
                maxVertex = i;
            }
        }
        
        // Try all 3 colors, pick best
        if (conflicts[maxVertex] > 0) {
            int bestColor = colors[maxVertex];
            int minConflicts = conflicts[maxVertex];
            
            for (int c = 0; c < 3; c++) {
                colors[maxVertex] = c;
                int newConflicts = countVertexConflicts(maxVertex, colors);
                if (newConflicts < minConflicts) {
                    minConflicts = newConflicts;
                    bestColor = c;
                }
            }
            colors[maxVertex] = bestColor;
        }
        
        return new ColoringState(colors, graph);
    }
    
    /**
     * MIN_CONFLICT: Random conflicted vertex, recolor to min-conflict.
     */
    private ColoringState applyMinConflict(int[] colors) {
        // Find conflicted vertices
        java.util.List<Integer> conflicted = new java.util.ArrayList<>();
        for (int[] edge : graph.getEdges()) {
            int u = edge[0];
            int v = edge[1];
            if (colors[u] == colors[v]) {
                if (!conflicted.contains(u)) conflicted.add(u);
                if (!conflicted.contains(v)) conflicted.add(v);
            }
        }
        
        if (conflicted.isEmpty()) {
            return state;
        }
        
        // Pick random conflicted vertex
        int vertex = conflicted.get(random.nextInt(conflicted.size()));
        
        // Try all colors, pick min-conflict
        int bestColor = colors[vertex];
        int minConflicts = countVertexConflicts(vertex, colors);
        
        for (int c = 0; c < 3; c++) {
            colors[vertex] = c;
            int newConflicts = countVertexConflicts(vertex, colors);
            if (newConflicts < minConflicts) {
                minConflicts = newConflicts;
                bestColor = c;
            }
        }
        colors[vertex] = bestColor;
        
        return new ColoringState(colors, graph);
    }
    
    /**
     * RANDOM_WALK: Random vertex, random recolor.
     */
    private ColoringState applyRandomWalk(int[] colors) {
        int vertex = random.nextInt(colors.length);
        colors[vertex] = random.nextInt(3);
        return new ColoringState(colors, graph);
    }
    
    /**
     * BACKTRACK_LIGHT: Small-depth lookahead (3-5 steps).
     */
    private ColoringState applyBacktrackLight(int[] colors) {
        // Simple implementation: try improving 3 random vertices in sequence
        int depth = 3 + random.nextInt(3);  // 3-5 steps
        
        for (int i = 0; i < depth; i++) {
            int vertex = random.nextInt(colors.length);
            int bestColor = colors[vertex];
            int minConflicts = countVertexConflicts(vertex, colors);
            
            for (int c = 0; c < 3; c++) {
                colors[vertex] = c;
                int newConflicts = countVertexConflicts(vertex, colors);
                if (newConflicts < minConflicts) {
                    minConflicts = newConflicts;
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
            if (edge[0] == vertex && colors[edge[0]] == colors[edge[1]]) {
                count++;
            }
            if (edge[1] == vertex && colors[edge[0]] == colors[edge[1]]) {
                count++;
            }
        }
        return count;
    }
}
