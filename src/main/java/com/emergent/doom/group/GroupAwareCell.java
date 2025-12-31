package com.emergent.doom.group;

import com.emergent.doom.cell.Cell;

/**
 * Interface for cells that are aware of their parent CellGroup.
 * 
 * <p>Enables CellGroup to manage cell boundaries, sleep/wake cycles,
 * and coordinate merging operations.</p>
 * 
 * <p><strong>cell_research Reference:</strong> MultiThreadCell.py properties:
 * <ul>
 *   <li>group: CellGroup - parent group reference</li>
 *   <li>left_boundary: tuple - leftmost position in group</li>
 *   <li>right_boundary: tuple - rightmost position in group</li>
 *   <li>status: CellStatus - current operational status</li>
 *   <li>previous_status: CellStatus - status before MOVING</li>
 * </ul>
 * </p>
 * 
 * @param <T> the type of cell
 */
public interface GroupAwareCell<T extends Cell<T>> extends Cell<T> {
    
    // ========== GROUP MEMBERSHIP ==========
    
    /**
     * Get the parent CellGroup managing this cell.
     * 
     * <p><strong>Purpose:</strong> Cells check group.status to determine
     * if they should sleep during move() operations.</p>
     * 
     * @return the parent CellGroup, or null if not assigned
     */
    CellGroup<T> getGroup();
    
    /**
     * Set the parent CellGroup for this cell.
     * 
     * <p><strong>Purpose:</strong> Called during group initialization
     * and when groups merge (cell.group = new_group).</p>
     * 
     * @param group the parent CellGroup to assign
     */
    void setGroup(CellGroup<T> group);
    
    // ========== BOUNDARY MANAGEMENT ==========
    
    /**
     * Get the left boundary index of the parent group.
     * 
     * <p><strong>Purpose:</strong> Cells use boundaries to determine
     * valid swap targets (must be within [left, right]).</p>
     * 
     * <p><strong>Implementation Note:</strong> In cell_research, boundaries
     * are (x,y) tuples. In EDE, we use simple integer indices for 1D arrays.</p>
     * 
     * @return the leftmost index in the group
     */
    int getLeftBoundary();
    
    /**
     * Set the left boundary index.
     * 
     * <p><strong>Purpose:</strong> Updated when groups merge to reflect
     * new combined group boundaries.</p>
     * 
     * @param leftBoundary the leftmost index in the group
     */
    void setLeftBoundary(int leftBoundary);
    
    /**
     * Get the right boundary index of the parent group.
     * 
     * @return the rightmost index in the group
     */
    int getRightBoundary();
    
    /**
     * Set the right boundary index.
     * 
     * @param rightBoundary the rightmost index in the group
     */
    void setRightBoundary(int rightBoundary);
    
    // ========== STATUS MANAGEMENT ==========
    
    /**
     * Get the current operational status of this cell.
     * 
     * <p><strong>Purpose:</strong> Controls whether cell can evaluate/swap.
     * Group threads set cells to SLEEP during sleep phase.</p>
     * 
     * @return the current CellStatus
     */
    CellStatus getStatus();
    
    /**
     * Set the current operational status.
     * 
     * <p><strong>Purpose:</strong> Called by group to sleep/wake cells,
     * and by swap engine to mark MOVING state.</p>
     * 
     * @param status the new CellStatus
     */
    void setStatus(CellStatus status);
    
    /**
     * Get the previous status before MOVING.
     * 
     * <p><strong>Purpose:</strong> When swap animation completes,
     * cell restores previous_status (either ACTIVE or SLEEP).</p>
     * 
     * @return the previous CellStatus
     */
    CellStatus getPreviousStatus();
    
    /**
     * Set the previous status.
     * 
     * @param previousStatus the status to restore after MOVING
     */
    void setPreviousStatus(CellStatus previousStatus);
    
    // ========== UPDATE CALLBACK ==========
    
    /**
     * Update cell state after group merge.
     * 
     * <p><strong>Purpose:</strong> Called by CellGroup.merge_with_group()
     * to notify cells that boundaries have changed.</p>
     * 
     * <p><strong>Implementation-specific behavior:</strong></p>
     * <ul>
     *   <li>SelectionCell: Reset idealPos to new left_boundary</li>
     *   <li>InsertionCell: Set enable_to_move = false (except first cell)</li>
     *   <li>BubbleCell: No action required</li>
     * </ul>
     * 
     * <p><strong>cell_research Reference:</strong> SelectionSortCell.py:77-81</p>
     */
    void updateForGroupMerge();
}
