package com.emergent.doom.cell;

import java.util.Optional;

/**
 * Insertion sort cell implementation with conservative left-only movement.
 *
 * <p><strong>PURPOSE:</strong> Implement INSERTION algotype behavior where cells check if
 * the left side is sorted before attempting to insert into the sorted region. This conservative
 * approach prevents disrupting the sorted prefix.</p>
 *
 * <p><strong>KEY BEHAVIORAL CHARACTERISTICS:</strong></p>
 * <ul>
 *   <li>Visibility: Sees all cells to the left (prefix view)</li>
 *   <li>Movement: Only moves left, and only if left side is already sorted</li>
 *   <li>Swap rule: Swap left if this.value < left.value AND left side is sorted</li>
 *   <li>Strategy: Defensive - waits for left side to sort before inserting</li>
 * </ul>
 *
 * <p><strong>LEVIN REFERENCE:</strong> "Insertion sort cells check if the left segment
 * is sorted before attempting to insert" (Levin et al., 2024, p. 5).</p>
 *
 * <p><strong>ARCHITECTURE NOTE:</strong> Algotype is immutable (INSERTION), travels with
 * cell during swaps. No additional mutable state beyond AbstractSortingCell.</p>
 *
 * <p><strong>EXPECTED INPUTS:</strong> value (int), initialPosition (int)</p>
 * <p><strong>EXPECTED OUTPUTS:</strong> Movement decisions based on left-sorted check</p>
 * <p><strong>DATA FLOW:</strong> Engine provides NeighborhoodView → Cell checks left sorted → Swap left if true</p>
 */
public class InsertionSortingCell extends AbstractSortingCell {
    
    /**
     * Create an InsertionSortingCell with specified value and position.
     *
     * <p><strong>PURPOSE:</strong> Initialize INSERTION cell with immutable INSERTION algotype.
     * No additional state beyond AbstractSortingCell.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>value - The sort key (immutable)</li>
     *   <li>initialPosition - Starting position in array</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong> Call super constructor with value, SortingAlgotype.INSERTION, initialPosition</p>
     *
     * <p><strong>OUTPUTS:</strong> Fully initialized InsertionSortingCell</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @param value the sort key value
     * @param initialPosition the starting position in array
     */
    public InsertionSortingCell(int value, int initialPosition) {
        // PURPOSE: Initialize INSERTION cell with immutable algotype
        // PROCESS: Call super constructor with INSERTION algotype (immutable)
        
        super(value, SortingAlgotype.INSERTION, initialPosition);
    }
    
    /**
     * Determine if this cell should attempt to move given its neighbors.
     *
     * <p><strong>PURPOSE:</strong> Implement INSERTION movement predicate - move if left neighbor
     * exists with wrong value ordering AND left side is sorted.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView with left prefix cells</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Get left neighbor from view</li>
     *   <li>If no left neighbor exists, return false (at left boundary)</li>
     *   <li>Check if left side is sorted via isLeftSorted()</li>
     *   <li>If not sorted, return false (wait for left side to sort)</li>
     *   <li>If sorted, check if this.value < left.value (wrong ordering)</li>
     *   <li>Return true if wrong ordering, false otherwise</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if should move, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> NeighborhoodView must provide left prefix, isLeftSorted() helper</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> INSERTION is defensive - waits for left side
     * to sort before inserting. This prevents disrupting the sorted prefix.</p>
     *
     * @param neighbors the neighborhood view for this cell
     * @return true if cell should attempt to move
     */
    @Override
    public boolean shouldMoveGiven(NeighborhoodView<Integer, SortingAlgotype> neighbors) {
        // PURPOSE: Check if INSERTION cell wants to move
        // PROCESS:
        //   1. INSERTION cells are defensive - only move if left side is sorted
        //   2. Get left neighbor
        //   3. If no left neighbor, return false (at left boundary)
        //   4. Check if left side is sorted
        //   5. If sorted AND this.value < left.value, return true
        // ARCHITECTURE: Conservative movement decision (contrast with BUBBLE's opportunistic)
        
        Optional<AbstractCell<Integer, SortingAlgotype>> leftOpt = neighbors.getLeftNeighbor();
        
        // No left neighbor - at left boundary
        if (leftOpt.isEmpty()) {
            return false;
        }
        
        // Check if left side is sorted
        if (!isLeftSorted(neighbors)) {
            return false; // Wait for left side to sort
        }
        
        // Left side is sorted - check if we should insert
        AbstractCell<Integer, SortingAlgotype> leftNeighbor = leftOpt.get();
        return this.value < leftNeighbor.readValue();
    }
    
    /**
     * Calculate the target position for this cell's move.
     *
     * <p><strong>PURPOSE:</strong> Implement INSERTION target selection - target left neighbor
     * if left side is sorted and swap would move toward sorted order.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView with left prefix cells</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Get left neighbor from view</li>
     *   <li>If no left neighbor exists, return Optional.empty()</li>
     *   <li>Check if left side is sorted via isLeftSorted()</li>
     *   <li>If not sorted, return Optional.empty() (wait)</li>
     *   <li>If sorted, check if this.value < left.value (should insert)</li>
     *   <li>If yes, return Optional.of(leftNeighborPosition)</li>
     *   <li>If no, return Optional.empty()</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Optional containing left neighbor position, or empty</p>
     *
     * <p><strong>DEPENDENCIES:</strong> NeighborhoodView, isLeftSorted() helper</p>
     *
     * <p><strong>GROUND TRUTH REFERENCE:</strong> cell_research/InsertionSortCell.py - checks
     * isLeftSorted() before swapping left.</p>
     *
     * @param neighbors the neighborhood view for this cell
     * @return Optional containing target position, or empty if no valid swap
     */
    @Override
    public Optional<Integer> calculateTargetPositionGiven(NeighborhoodView<Integer, SortingAlgotype> neighbors) {
        // PURPOSE: Calculate INSERTION swap target via left-sorted check
        // PROCESS:
        //   1. Get left neighbor
        //   2. If no left neighbor, return empty
        //   3. Check if left side is sorted
        //   4. If not sorted, return empty (wait)
        //   5. If sorted AND this.value < left.value, return left position
        // GROUND TRUTH: Matches cell_research InsertionSortCell behavior
        
        Optional<AbstractCell<Integer, SortingAlgotype>> leftOpt = neighbors.getLeftNeighbor();
        
        // No left neighbor - at left boundary
        if (leftOpt.isEmpty()) {
            return Optional.empty();
        }
        
        // Check if left side is sorted
        if (!isLeftSorted(neighbors)) {
            return Optional.empty(); // Wait for left side to sort
        }
        
        // Left side is sorted - check if we should insert
        AbstractCell<Integer, SortingAlgotype> leftNeighbor = leftOpt.get();
        if (this.value < leftNeighbor.readValue()) {
            return Optional.of(leftNeighbor.readCurrentPosition());
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Check if the left side of the array (positions 0 to currentPosition-1) is sorted.
     *
     * <p><strong>PURPOSE:</strong> Verify that the left prefix is sorted before attempting
     * to insert this cell into the sorted region. Matches Levin paper behavior.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView with left prefix cells</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Get all neighbors (should be left prefix for INSERTION)</li>
     *   <li>For each consecutive pair of neighbors:
     *     <ul>
     *       <li>Check if neighbor[i].value <= neighbor[i+1].value (ascending sort)</li>
     *       <li>If out of order, return false</li>
     *     </ul>
     *   </li>
     *   <li>If all pairs are in order, return true</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if left side is sorted, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> NeighborhoodView must provide left prefix in order</p>
     *
     * <p><strong>GROUND TRUTH REFERENCE:</strong> cell_research/InsertionSortCell.py checks
     * if left segment is sorted before inserting.</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> This is the key defensive check that distinguishes
     * INSERTION from BUBBLE. INSERTION waits for stability before moving.</p>
     *
     * @param neighbors the neighborhood view
     * @return true if left side is sorted
     */
    private boolean isLeftSorted(NeighborhoodView<Integer, SortingAlgotype> neighbors) {
        // PURPOSE: Check if left prefix is sorted
        // PROCESS:
        //   1. For each consecutive pair of left neighbors
        //   2. Check if neighbor[i].value <= neighbor[i+1].value
        //   3. If out of order, return false
        //   4. If all in order, return true
        // GROUND TRUTH: Matches cell_research InsertionSortCell.isLeftSorted()
        
        // If at position 0 or 1, left side is trivially sorted
        if (currentPosition <= 1) {
            return true;
        }
        
        // Get all visible neighbors (should be left prefix for INSERTION)
        // We need to check if they are in sorted order
        // For simplicity, we'll check consecutive pairs from the view
        
        // Since NeighborhoodView provides left prefix, we check consecutive pairs
        for (int i = 0; i < neighbors.getNeighborCount() - 1; i++) {
            AbstractCell<Integer, SortingAlgotype> current = neighbors.getNeighborAt(i);
            AbstractCell<Integer, SortingAlgotype> next = neighbors.getNeighborAt(i + 1);
            
            // Ascending sort: current.value should be <= next.value
            if (current.readValue() > next.readValue()) {
                return false; // Out of order
            }
        }
        
        return true; // All pairs in order
    }
}
