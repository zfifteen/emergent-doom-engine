package com.emergent.doom.execution;

import com.emergent.doom.cell.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Simple execution engine using new cell-based architecture (Levin-aligned semantics).
 *
 * <p><strong>PURPOSE:</strong> Execute sorting steps where cells evaluate neighbors and
 * propose swaps. Engine builds NeighborhoodView for each cell and executes swaps by
 * relocating entire cell objects (value + algotype together).</p>
 *
 * <p><strong>KEY ARCHITECTURAL CHANGE:</strong> Algotypes travel WITH cells during swaps,
 * enabling genuine Levin-style morphogenetic clustering with 18.30% variance signature.</p>
 */
public class CellBasedExecutionEngine {
    
    /**
     * Execute one step of sorting where each ACTIVE cell evaluates and potentially swaps.
     *
     * <p><strong>PURPOSE:</strong> Core execution loop for cell-based sorting.</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>For each ACTIVE cell in array:</li>
     *   <li>Build NeighborhoodView based on cell's algotype</li>
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
    public int executeStep(List<AbstractSortingCell> cells) {
        int swapCount = 0;
        
        // Iterate through cells in sequential order
        for (int i = 0; i < cells.size(); i++) {
            AbstractSortingCell cell = cells.get(i);
            
            // Skip non-active cells
            if (!cell.canInitiateSwap()) {
                continue;
            }
            
            // Build neighborhood view for this cell
            NeighborhoodView<Integer, SortingAlgotype> neighbors = buildNeighborhoodView(cell, cells);
            
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
            
            AbstractSortingCell targetCell = cells.get(targetPos);
            
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
     * Build neighborhood view for a cell based on its algotype.
     *
     * <p><strong>PURPOSE:</strong> Provide cell with visibility into its neighbors
     * according to algotype-specific rules.</p>
     *
     * <p><strong>VISIBILITY RULES:</strong></p>
     * <ul>
     *   <li>BUBBLE: Left and right adjacent neighbors</li>
     *   <li>INSERTION: All cells to the left (prefix view)</li>
     *   <li>SELECTION: Cell at ideal position</li>
     *   <li>FIBONACCI: Fibonacci-distance neighbors (for future implementation)</li>
     * </ul>
     *
     * @param cell the cell whose neighborhood to build
     * @param cells the full cell array
     * @return neighborhood view for the cell
     */
    private NeighborhoodView<Integer, SortingAlgotype> buildNeighborhoodView(
            AbstractSortingCell cell, 
            List<AbstractSortingCell> cells) {
        
        int position = cell.readCurrentPosition();
        SortingAlgotype algotype = cell.readAlgotype();
        
        List<AbstractCell<Integer, SortingAlgotype>> visibleNeighbors = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();
        
        switch (algotype) {
            case BUBBLE:
                // BUBBLE sees left and right adjacent neighbors
                if (position > 0) {
                    visibleNeighbors.add(cells.get(position - 1));
                    positions.add(position - 1);
                }
                if (position < cells.size() - 1) {
                    visibleNeighbors.add(cells.get(position + 1));
                    positions.add(position + 1);
                }
                break;
                
            case INSERTION:
                // INSERTION sees all cells to the left (prefix view)
                for (int i = 0; i < position; i++) {
                    visibleNeighbors.add(cells.get(i));
                    positions.add(i);
                }
                break;
                
            case SELECTION:
                // SELECTION sees cell at ideal position
                SelectionSortingCell selectionCell = (SelectionSortingCell) cell;
                int idealPos = selectionCell.getIdealPosition();
                if (idealPos >= 0 && idealPos < cells.size()) {
                    visibleNeighbors.add(cells.get(idealPos));
                    positions.add(idealPos);
                }
                break;
                
            case FIBONACCI:
                // For now, use BUBBLE visibility (can be extended later)
                if (position > 0) {
                    visibleNeighbors.add(cells.get(position - 1));
                    positions.add(position - 1);
                }
                if (position < cells.size() - 1) {
                    visibleNeighbors.add(cells.get(position + 1));
                    positions.add(position + 1);
                }
                break;
        }
        
        return new NeighborhoodView<>(cell, position, cells.size(), visibleNeighbors, positions);
    }
    
    /**
     * Execute sorting until array is sorted or max steps reached.
     *
     * <p><strong>PURPOSE:</strong> Run sorting to completion.</p>
     *
     * @param cells the cell array
     * @param maxSteps maximum number of steps
     * @return number of steps executed
     */
    public int executeSorting(List<AbstractSortingCell> cells, int maxSteps) {
        int steps = 0;
        
        while (steps < maxSteps && !isSorted(cells)) {
            int swaps = executeStep(cells);
            steps++;
            
            // If no swaps occurred, array is stable (though may not be sorted)
            if (swaps == 0) {
                break;
            }
        }
        
        return steps;
    }
    
    /**
     * Check if cell array is sorted by values.
     *
     * @param cells the cell array
     * @return true if sorted
     */
    private boolean isSorted(List<AbstractSortingCell> cells) {
        for (int i = 0; i < cells.size() - 1; i++) {
            if (cells.get(i).readValue() > cells.get(i + 1).readValue()) {
                return false;
            }
        }
        return true;
    }
}
