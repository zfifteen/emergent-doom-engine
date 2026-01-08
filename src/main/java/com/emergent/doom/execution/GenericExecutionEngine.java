package com.emergent.doom.execution;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.cell.NeighborhoodView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generic execution engine for any cell type extending AbstractCell<V, A>.
 *
 * <p><strong>PURPOSE:</strong> Execute steps where cells evaluate neighbors and
 * propose swaps. Domain-agnostic - works with sorting cells, factor cells, or
 * any future cell type.</p>
 *
 * <p><strong>KEY ARCHITECTURAL PRINCIPLE:</strong> Algotypes travel WITH cells
 * during swaps, enabling genuine Levin-style morphogenetic clustering across
 * all problem domains.</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Parameterized types: Works with any Comparable value type and Enum algotype</li>
 *   <li>Simple neighborhood: Currently bubble-like (left/right neighbors)</li>
 *   <li>Convergence detection: No swaps OR user-defined convergence check</li>
 *   <li>Domain-agnostic: No sorting-specific logic</li>
 * </ul>
 *
 * @param <V> the value type (must be Comparable)
 * @param <A> the algotype enum type
 */
public class GenericExecutionEngine<V extends Comparable<V>, A extends Enum<A>> {
    
    /**
     * Execute one step where each ACTIVE cell evaluates and potentially swaps.
     *
     * <p><strong>PURPOSE:</strong> Core execution loop for cell-based problem solving.</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>For each ACTIVE cell in array:</li>
     *   <li>Build NeighborhoodView (currently simple left/right neighbors)</li>
     *   <li>Ask cell if it should move: cell.shouldMoveGiven(neighbors)</li>
     *   <li>If yes, ask where: cell.calculateTargetPositionGiven(neighbors)</li>
     *   <li>If valid target, execute swap (entire cell objects relocate)</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Number of swaps performed</p>
     *
     * @param cells the cell array
     * @return number of swaps performed
     */
    public int executeStep(List<AbstractCell<V, A>> cells) {
        int swapCount = 0;
        
        // Iterate through cells in sequential order
        for (int i = 0; i < cells.size(); i++) {
            AbstractCell<V, A> cell = cells.get(i);
            
            // Skip non-active cells
            if (!cell.canInitiateSwap()) {
                continue;
            }
            
            // Build simple neighborhood view (left and right neighbors)
            NeighborhoodView<V, A> neighbors = buildSimpleNeighborhoodView(cell, cells);
            
            // Ask cell if it wants to move
            if (!cell.shouldMoveGiven(neighbors)) {
                continue;
            }
            
            // Ask cell where it wants to move
            Optional<Integer> targetOpt = cell.calculateTargetPositionGiven(neighbors);
            if (targetOpt.isEmpty()) {
                continue;
            }
            
            int targetPos = targetOpt.get();
            
            // Validate target position
            if (targetPos < 0 || targetPos >= cells.size() || targetPos == i) {
                continue;
            }
            
            AbstractCell<V, A> targetCell = cells.get(targetPos);
            
            // Check if target cell can accept swap
            if (!targetCell.canAcceptSwapFrom(cell)) {
                continue;
            }
            
            // Execute swap: entire cell objects relocate (algotype travels with cell!)
            cells.set(i, targetCell);
            cells.set(targetPos, cell);
            
            // Update cell positions
            cell.updatePositionTo(targetPos);
            targetCell.updatePositionTo(i);
            
            swapCount++;
        }
        
        return swapCount;
    }
    
    /**
     * Build simple neighborhood view with left and right neighbors.
     *
     * <p><strong>PURPOSE:</strong> Provide cell with visibility into adjacent neighbors.
     * Future enhancement: Could accept strategy for different visibility patterns.</p>
     *
     * <p><strong>CURRENT IMPLEMENTATION:</strong> Bubble-like visibility (left + right)</p>
     *
     * @param cell the cell whose neighborhood to build
     * @param cells the full cell array
     * @return neighborhood view for the cell
     */
    private NeighborhoodView<V, A> buildSimpleNeighborhoodView(
            AbstractCell<V, A> cell,
            List<AbstractCell<V, A>> cells) {
        
        int position = cell.readCurrentPosition();
        
        List<AbstractCell<V, A>> visibleNeighbors = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();
        
        // Add left neighbor if exists
        if (position > 0) {
            visibleNeighbors.add(cells.get(position - 1));
            positions.add(position - 1);
        }
        
        // Add right neighbor if exists
        if (position < cells.size() - 1) {
            visibleNeighbors.add(cells.get(position + 1));
            positions.add(position + 1);
        }
        
        return new NeighborhoodView<>(cell, position, cells.size(), visibleNeighbors, positions);
    }
    
    /**
     * Execute until convergence or max steps reached.
     *
     * <p><strong>PURPOSE:</strong> Run problem-solving to completion.</p>
     *
     * <p><strong>CONVERGENCE:</strong> Detected when no swaps occur in a step.
     * Domain-specific convergence (e.g., "sorted") handled by cell comparison logic.</p>
     *
     * @param cells the cell array
     * @param maxSteps maximum number of steps
     * @return number of steps executed
     */
    public int executeUntilConvergence(List<AbstractCell<V, A>> cells, int maxSteps) {
        int steps = 0;
        
        while (steps < maxSteps) {
            int swaps = executeStep(cells);
            steps++;
            
            // Convergence detected: no beneficial swaps remain
            if (swaps == 0) {
                break;
            }
        }
        
        return steps;
    }
    
    /**
     * Execute exact number of steps (for controlled experiments).
     *
     * <p><strong>PURPOSE:</strong> Run fixed number of steps regardless of convergence.
     * Useful for trajectory recording and time-series analysis.</p>
     *
     * @param cells the cell array
     * @param steps number of steps to execute
     * @return total swaps performed
     */
    public int executeFixedSteps(List<AbstractCell<V, A>> cells, int steps) {
        int totalSwaps = 0;
        
        for (int i = 0; i < steps; i++) {
            totalSwaps += executeStep(cells);
        }
        
        return totalSwaps;
    }
}
