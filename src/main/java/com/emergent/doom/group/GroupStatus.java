package com.emergent.doom.group;

/**
 * Enumeration of CellGroup lifecycle states.
 * 
 * <p>Matches cell_research Python implementation (CellGroup.py:6-10)</p>
 * 
 * <p><strong>State Transitions:</strong></p>
 * <ul>
 *   <li>ACTIVE ↔ SLEEP (periodic cycling via change_status())</li>
 *   <li>ACTIVE → MERGED (when absorbed into adjacent group)</li>
 *   <li>MERGED is terminal (thread exits)</li>
 * </ul>
 * 
 * <p><strong>Purpose:</strong> Groups coordinate cell sleep/wake cycles and
 * merge when adjacent groups are both sorted, enabling hierarchical emergence.</p>
 */
public enum GroupStatus {
    /**
     * Group is active - cells can evaluate and propose swaps.
     * Groups in ACTIVE state check if sorted and attempt merges.
     */
    ACTIVE,
    
    /**
     * Group is merging with adjacent group (transitional state).
     * Note: Currently unused in cell_research implementation.
     */
    MERGING,
    
    /**
     * Group is sleeping - cells are put to SLEEP status.
     * Groups periodically sleep to allow other groups to make progress.
     */
    SLEEP,
    
    /**
     * Group has been absorbed into another group (terminal state).
     * Group thread exits when status == MERGED.
     */
    MERGED
}
