package com.emergent.doom.group;

/**
 * Enumeration of individual cell execution states.
 * 
 * <p>Matches cell_research Python implementation (MultiThreadCell.py:7-14)</p>
 * 
 * <p><strong>Cell Lifecycle:</strong></p>
 * <ol>
 *   <li>Cells start as ACTIVE</li>
 *   <li>Transition to SLEEP when group sleeps</li>
 *   <li>Transition to MOVING during swap animation</li>
 *   <li>Transition to INACTIVE to terminate thread</li>
 *   <li>Can be set to FREEZE to simulate frozen cells</li>
 * </ol>
 * 
 * <p><strong>Purpose:</strong> Controls whether a cell can initiate swaps
 * and participate in sorting. Managed by parent CellGroup.</p>
 */
public enum CellStatus {
    /**
     * Cell is active and can evaluate/swap.
     * This is the normal operational state.
     */
    ACTIVE,
    
    /**
     * Cell is sleeping (group is in SLEEP phase).
     * Cell cannot evaluate or propose swaps while asleep.
     */
    SLEEP,
    
    /**
     * Cell is merging (transitional state).
     * Note: Currently unused in cell_research implementation.
     */
    MERGE,
    
    /**
     * Cell is animating a swap (visualization mode).
     * Cell moves from current_position to target_position.
     */
    MOVING,
    
    /**
     * Cell thread should terminate.
     * Thread exits when status == INACTIVE.
     */
    INACTIVE,
    
    /**
     * Cell encountered an error (unused).
     * Reserved for error handling.
     */
    ERROR,
    
    /**
     * Cell is frozen - cannot initiate swaps but CAN be moved by others.
     * Matches cell_research FREEZE semantics.
     */
    FREEZE
}
