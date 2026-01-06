package com.emergent.doom.cell;

import java.util.Optional;

/**
 * Selection sort cell implementation with ideal position targeting.
 *
 * <p><strong>PURPOSE:</strong> Implement SELECTION algotype behavior where cells target
 * a specific ideal position and increment that position when swaps are denied, gradually
 * converging to final sorted position.</p>
 *
 * <p><strong>KEY BEHAVIORAL CHARACTERISTICS:</strong></p>
 * <ul>
 *   <li>Visibility: Sees target position (ideal position field)</li>
 *   <li>Movement: Tries to swap with cell at ideal position</li>
 *   <li>Swap rule: Swap if this.value < target.value (ascending sort)</li>
 *   <li>Denial handling: Increment ideal position rightward when swap denied</li>
 *   <li>Strategy: Targeted - converges toward final position through iterative adjustments</li>
 * </ul>
 *
 * <p><strong>LEVIN REFERENCE:</strong> "Selection sort cells target specific positions
 * and adjust when blocked" (Levin et al., 2024, p. 6).</p>
 *
 * <p><strong>ARCHITECTURE NOTE:</strong> Algotype is immutable (SELECTION), travels with
 * cell during swaps. Ideal position is mutable state updated when swaps are denied.</p>
 *
 * <p><strong>EXPECTED INPUTS:</strong> value (int), initialPosition (int), initialIdealPosition (int)</p>
 * <p><strong>EXPECTED OUTPUTS:</strong> Movement decisions targeting ideal position</p>
 * <p><strong>DATA FLOW:</strong> Engine provides NeighborhoodView → Cell targets idealPosition → Increment if denied</p>
 */
public class SelectionSortingCell extends AbstractSortingCell {
    
    // PURPOSE: Target position this cell is trying to reach
    // INPUTS: Initialized to 0 (ascending) or arraySize-1 (descending)
    // PROCESS: Incremented rightward (ascending) when swap denied
    // OUTPUTS: Used in calculateTargetPositionGiven() to select swap target
    // DEPENDENCIES: Updated via incrementIdealPosition() when swap denied
    // ARCHITECTURE NOTE: Mutable state - allows iterative convergence toward final position
    //                    Matches Levin paper behavior where SELECTION cells adjust target
    private int idealPosition;
    
    /**
     * Create a SelectionSortingCell with specified value, position, and initial ideal position.
     *
     * <p><strong>PURPOSE:</strong> Initialize SELECTION cell with immutable SELECTION algotype
     * and initial ideal position (typically 0 for ascending, arraySize-1 for descending).</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>value - The sort key (immutable)</li>
     *   <li>initialPosition - Starting position in array</li>
     *   <li>initialIdealPosition - Initial target position (typically 0 or arraySize-1)</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Call super constructor with value, SortingAlgotype.SELECTION, initialPosition</li>
     *   <li>Validate initialIdealPosition is non-negative</li>
     *   <li>Store initialIdealPosition as mutable idealPosition field</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Fully initialized SelectionSortingCell</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @param value the sort key value
     * @param initialPosition the starting position in array
     * @param initialIdealPosition the initial target position (non-negative)
     * @throws IllegalArgumentException if initialIdealPosition is negative
     */
    public SelectionSortingCell(int value, int initialPosition, int initialIdealPosition) {
        // PURPOSE: Initialize SELECTION cell with immutable algotype and mutable ideal position
        // PROCESS:
        //   1. Call super constructor with SELECTION algotype (immutable)
        //   2. Validate initialIdealPosition is non-negative
        //   3. Store initialIdealPosition as mutable field
        // GROUND TRUTH: Matches cell_research SelectionSortCell initialization
        
        super(value, SortingAlgotype.SELECTION, initialPosition);
        
        if (initialIdealPosition < 0) {
            throw new IllegalArgumentException("initialIdealPosition must be non-negative, got: " + initialIdealPosition);
        }
        
        this.idealPosition = initialIdealPosition;
    }
    
    /**
     * Create a SelectionSortingCell with specified value and position, defaulting ideal position to 0.
     *
     * <p><strong>PURPOSE:</strong> Convenience constructor for ascending sort (ideal position starts at 0).</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>value - The sort key (immutable)</li>
     *   <li>initialPosition - Starting position in array</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong> Delegate to primary constructor with initialIdealPosition=0</p>
     *
     * <p><strong>OUTPUTS:</strong> Fully initialized SelectionSortingCell with ideal position 0</p>
     *
     * @param value the sort key value
     * @param initialPosition the starting position in array
     */
    public SelectionSortingCell(int value, int initialPosition) {
        this(value, initialPosition, 0);
    }
    
    /**
     * Get the current ideal position.
     *
     * <p><strong>PURPOSE:</strong> Provide read access to ideal position for testing and debugging.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>OUTPUTS:</strong> Current ideal position (non-negative)</p>
     *
     * @return the current ideal position
     */
    public int getIdealPosition() {
        return idealPosition;
    }
    
    /**
     * Set the ideal position to a specific value.
     *
     * <p><strong>PURPOSE:</strong> Allow engine to reset ideal position (e.g., during group merge
     * or initialization for descending sort).</p>
     *
     * <p><strong>INPUTS:</strong> newIdealPosition - the new target position (non-negative)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate newIdealPosition is non-negative</li>
     *   <li>Update idealPosition field</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> None (mutates idealPosition field)</p>
     *
     * @param newIdealPosition the new ideal position
     * @throws IllegalArgumentException if newIdealPosition is negative
     */
    public void setIdealPosition(int newIdealPosition) {
        // PURPOSE: Update ideal position (used for initialization or group merge)
        // PROCESS:
        //   1. Validate newIdealPosition is non-negative
        //   2. Update idealPosition field
        
        if (newIdealPosition < 0) {
            throw new IllegalArgumentException("newIdealPosition must be non-negative, got: " + newIdealPosition);
        }
        this.idealPosition = newIdealPosition;
    }
    
    /**
     * Increment the ideal position by 1.
     *
     * <p><strong>PURPOSE:</strong> Adjust target position when swap is denied. Called by engine
     * or internally when calculateTargetPositionGiven() detects swap should be denied.</p>
     *
     * <p><strong>INPUTS:</strong> None (increment operation)</p>
     *
     * <p><strong>PROCESS:</strong> Increment idealPosition by 1</p>
     *
     * <p><strong>OUTPUTS:</strong> New ideal position after increment</p>
     *
     * <p><strong>GROUND TRUTH REFERENCE:</strong> cell_research/SelectionSortCell.py - increments
     * ideal position when swap denied.</p>
     *
     * @return the new ideal position after increment
     */
    public int incrementIdealPosition() {
        // PURPOSE: Adjust ideal position rightward when swap denied
        // PROCESS: Increment idealPosition by 1
        // GROUND TRUTH: Matches cell_research SelectionSortCell behavior
        return ++idealPosition;
    }
    
    /**
     * Determine if this cell should attempt to move given its neighbors.
     *
     * <p><strong>PURPOSE:</strong> Implement SELECTION movement predicate - move if not already
     * at ideal position.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView (not used for SELECTION, kept for interface)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Compare currentPosition to idealPosition</li>
     *   <li>Return true if different (need to move)</li>
     *   <li>Return false if same (already at target)</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if not at ideal position, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> currentPosition and idealPosition fields</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> SELECTION is goal-directed - wants to move only
     * if not at target position. Contrast with BUBBLE (opportunistic) which always wants
     * to move when neighbors exist.</p>
     *
     * @param neighbors the neighborhood view (not used for SELECTION)
     * @return true if cell should attempt to move
     */
    @Override
    public boolean shouldMoveGiven(NeighborhoodView<Integer, SortingAlgotype> neighbors) {
        // PURPOSE: Check if SELECTION cell wants to move
        // PROCESS:
        //   1. SELECTION cells are goal-directed - want to move only if not at ideal position
        //   2. Return true if currentPosition != idealPosition
        // ARCHITECTURE: Targeted movement decision (contrast with BUBBLE's opportunistic)
        
        return currentPosition != idealPosition;
    }
    
    /**
     * Calculate the target position for this cell's move.
     *
     * <p><strong>PURPOSE:</strong> Implement SELECTION target selection - target the cell
     * at ideal position if swap would move toward sorted order. Increment ideal position
     * if swap should be denied.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView providing cell at ideal position</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>If already at ideal position, return Optional.empty() (no move needed)</li>
     *   <li>If ideal position equals current position (shouldn't happen but defensive), return empty</li>
     *   <li>Find cell at ideal position from neighbors</li>
     *   <li>If cell at ideal position not found, return empty (boundary case)</li>
     *   <li>Check if swap with ideal position cell moves toward sorted order:
     *     <ul>
     *       <li>Ascending: Swap if this.value < target.value (smaller value moves left/toward 0)</li>
     *       <li>Descending: Swap if this.value > target.value (larger value moves right/toward arraySize-1)</li>
     *     </ul>
     *   </li>
     *   <li>If yes: Return Optional.of(idealPosition)</li>
     *   <li>If no: Increment ideal position, return empty</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Optional containing ideal position, or empty if swap denied</p>
     *
     * <p><strong>DEPENDENCIES:</strong> NeighborhoodView must provide cell at ideal position</p>
     *
     * <p><strong>GROUND TRUTH REFERENCE:</strong> cell_research/SelectionSortCell.py - targets
     * ideal position, increments when swap denied.</p>
     *
     * @param neighbors the neighborhood view for this cell
     * @return Optional containing target position, or empty if swap denied
     */
    @Override
    public Optional<Integer> calculateTargetPositionGiven(NeighborhoodView<Integer, SortingAlgotype> neighbors) {
        // PURPOSE: Calculate SELECTION swap target via ideal position targeting
        // PROCESS:
        //   1. If already at ideal position, no move needed
        //   2. Find cell at ideal position from neighbors
        //   3. Check if swap moves toward sorted order (this.value < target.value for ascending)
        //   4. If yes, return ideal position
        //   5. If no, increment ideal position and return empty
        // GROUND TRUTH: Matches cell_research SelectionSortCell behavior
        
        // Already at ideal position - no move needed
        if (currentPosition == idealPosition) {
            return Optional.empty();
        }
        
        // Find cell at ideal position
        AbstractCell<Integer, SortingAlgotype> targetCell = null;
        for (int i = 0; i < neighbors.getNeighborCount(); i++) {
            if (neighbors.getNeighborPositionAt(i) == idealPosition) {
                targetCell = neighbors.getNeighborAt(i);
                break;
            }
        }
        
        // Ideal position not in neighbors (boundary case or engine error)
        if (targetCell == null) {
            return Optional.empty();
        }
        
        // Check if swap with ideal position cell moves toward sorted order
        // Ascending sort (default for this implementation):
        //   - Swap if this.value < target.value (smaller value moves toward 0)
        boolean shouldSwap = this.value < targetCell.readValue();
        
        if (shouldSwap) {
            return Optional.of(idealPosition);
        } else {
            // Swap denied - increment ideal position rightward
            incrementIdealPosition();
            return Optional.empty();
        }
    }
}
