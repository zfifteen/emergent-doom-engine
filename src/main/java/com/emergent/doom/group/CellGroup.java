package com.emergent.doom.group;

import com.emergent.doom.cell.Cell;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Hierarchical group manager for coordinating cell sleep/wake cycles and merging.
 * 
 * <p><strong>cell_research Reference:</strong> CellGroup.py:13-122</p>
 * 
 * <p><strong>Purpose:</strong> CellGroups manage collections of cells with shared
 * boundaries, coordinating their lifecycle through periodic sleep/wake cycles.
 * When adjacent groups are both sorted, they merge into a single larger group,
 * enabling hierarchical emergence from small sorted regions to full array.</p>
 * 
 * <p><strong>Threading Model:</strong> Each CellGroup runs in its own thread,
 * periodically checking if the group is sorted and attempting to merge with
 * adjacent groups. Uses shared lock for synchronization with cell threads.</p>
 * 
 * <p><strong>Lifecycle:</strong></p>
 * <ol>
 *   <li>Initialized with cells, boundaries, and phase timing parameters</li>
 *   <li>Thread alternates between ACTIVE (countdown) and SLEEP (countdown) phases</li>
 *   <li>During ACTIVE: checks is_group_sorted(), attempts merge if sorted</li>
 *   <li>During SLEEP: puts cells to sleep, prevents evaluation</li>
 *   <li>When merged into another group: status → MERGED, thread exits</li>
 * </ol>
 * 
 * @param <T> the type of cell (must implement GroupAwareCell)
 */
public class CellGroup<T extends Cell<T> & GroupAwareCell<T>> extends Thread {
    
    // ========== CORE PROPERTIES ==========
    
    private final int groupId;
    private final List<T> cellsInGroup;
    private final T[] globalCells;
    private int leftBoundaryPosition;
    private int rightBoundaryPosition;
    private volatile GroupStatus status;
    private final Lock lock;
    private int phasePeriod;
    private int countDown;
    
    // ========== CONSTRUCTOR ==========
    
    public CellGroup(
        List<T> cellsInGroup,
        T[] globalCells,
        int groupId,
        int leftBoundary,
        int rightBoundary,
        GroupStatus status,
        Lock lock,
        int countDown,
        int phasePeriod
    ) {
        // Validate inputs
        if (cellsInGroup == null || cellsInGroup.isEmpty()) {
            throw new IllegalArgumentException("cellsInGroup must not be null or empty");
        }
        if (globalCells == null || globalCells.length == 0) {
            throw new IllegalArgumentException("globalCells must not be null or empty");
        }
        if (leftBoundary > rightBoundary) {
            throw new IllegalArgumentException("leftBoundary must be <= rightBoundary");
        }
        if (phasePeriod <= 0) {
            throw new IllegalArgumentException("phasePeriod must be > 0");
        }
        if (lock == null) {
            throw new IllegalArgumentException("lock must not be null");
        }
        
        // Initialize all fields
        this.cellsInGroup = cellsInGroup;
        this.globalCells = globalCells;
        this.groupId = groupId;
        this.leftBoundaryPosition = leftBoundary;
        this.rightBoundaryPosition = rightBoundary;
        this.status = status;
        this.lock = lock;
        this.countDown = countDown;
        this.phasePeriod = phasePeriod;
        
        // Set thread name for debugging (matches Python threading.Thread.__init__)
        setName("CellGroup-" + groupId);
    }
    
    // ========== SORTED DETECTION ==========
    
    public boolean isGroupSorted() {
        // Check if all cells in group are in sorted order
        // Returns false if any cell is SLEEP/MOVING (indeterminate state)
        // Matches cell_research CellGroup.py:37-44
        
        T firstCell = globalCells[leftBoundaryPosition];
        CellStatus firstStatus = firstCell.getStatus();
        if (firstStatus == CellStatus.SLEEP || firstStatus == CellStatus.MOVING) {
            return false;
        }
        T prevCell = firstCell;
        for (int i = leftBoundaryPosition + 1; i <= rightBoundaryPosition; i++) {
            T cell = globalCells[i];
            CellStatus cellStatus = cell.getStatus();
            if (cellStatus == CellStatus.SLEEP || cellStatus == CellStatus.MOVING) {
                return false;
            }
            if (cell.compareTo(prevCell) < 0) {
                return false;
            }
            prevCell = cell;
        }
        return true;
    }
    
    // ========== ADJACENT GROUP FINDING ==========
    
    public CellGroup<T> findNextGroup() {
        // Find the adjacent group to the right
        // Returns null if this is the rightmost group
        // Matches cell_research CellGroup.py:50-52
        
        int nextIndex = rightBoundaryPosition + 1;
        
        // Check if we're at the array boundary
        if (nextIndex >= globalCells.length) {
            return null; // No group to the right
        }
        
        // Get the cell immediately to our right and return its group
        T nextCell = globalCells[nextIndex];
        return nextCell.getGroup();
    }
    
    // ========== GROUP MERGING ==========
    
    public void mergeWithGroup(CellGroup<T> nextGroup) {
        // Absorb adjacent sorted group into this group
        // Expands boundaries, updates cells, handles algorithm-specific resets
        // Matches cell_research CellGroup.py:55-73
        
        // Mark next group as merged (terminates its thread)
        nextGroup.status = GroupStatus.MERGED;
        
        // Take minimum timing parameters (faster pace wins)
        this.countDown = Math.min(this.countDown, nextGroup.countDown);
        this.phasePeriod = Math.min(this.phasePeriod, nextGroup.phasePeriod);
        
        // Expand our right boundary to include next group
        this.rightBoundaryPosition = nextGroup.rightBoundaryPosition;
        
        // Absorb next group's cells into our group
        this.cellsInGroup.addAll(nextGroup.cellsInGroup);
        
        // Update all cells in merged group with new boundaries and parent
        for (T cell : this.cellsInGroup) {
            cell.setGroup(this);
            cell.setLeftBoundary(this.leftBoundaryPosition);
            cell.setRightBoundary(this.rightBoundaryPosition);
            cell.updateForGroupMerge(); // Algorithm-specific updates (e.g., SelectionCell resets idealPos)
        }
        
        // After merge, this group manages expanded region [leftBoundary, nextGroup.rightBoundary]
        // Next group's thread exits because status == MERGED
    }
    
    // ========== SLEEP/WAKE CYCLE ==========
    
    public void changeStatus() {
        // Toggle group between ACTIVE and SLEEP phases
        // Resets countdown and updates all cell statuses
        // Matches cell_research CellGroup.py:81-90
        
        countDown = phasePeriod; // Reset phase timer
        
        if (status == GroupStatus.ACTIVE) {
            status = GroupStatus.SLEEP;
            putCellsToSleep();
        } else if (status == GroupStatus.SLEEP) {
            status = GroupStatus.ACTIVE;
            awakeCells();
        }
        
        // MERGING and MERGED statuses don't change
    }
    
    public void putCellsToSleep() {
        // Set all active cells to SLEEP status
        // Preserves MOVING and INACTIVE states
        // Matches cell_research CellGroup.py:93-95
        
        for (T cell : cellsInGroup) {
            CellStatus cellStatus = cell.getStatus();
            
            // Don't interrupt MOVING (animation) or INACTIVE (terminated) cells
            if (cellStatus != CellStatus.MOVING && cellStatus != CellStatus.INACTIVE) {
                cell.setPreviousStatus(cellStatus);
                cell.setStatus(CellStatus.SLEEP);
            }
        }
    }
    
    public void awakeCells() {
        // Restore cells to previous status (typically ACTIVE)
        // Called when group transitions from SLEEP to ACTIVE
        // Matches cell_research CellGroup.py:97-100
        
        for (T cell : cellsInGroup) {
            // Don't wake terminated cells
            if (cell.getStatus() != CellStatus.INACTIVE) {
                CellStatus previousStatus = cell.getPreviousStatus();
                cell.setStatus(previousStatus);
            }
        }
    }
    
    public boolean allCellsInactive() {
        // Check if all cells in this group have terminated
        // Used in run() loop termination condition
        // Matches cell_research CellGroup.py:75-78
        
        for (T cell : cellsInGroup) {
            if (cell.getStatus() != CellStatus.INACTIVE) {
                return false; // At least one cell still active
            }
        }
        
        return true; // All cells terminated
    }
    
    // ========== THREAD EXECUTION ==========
    
    @Override
    public void run() {
        // Main execution loop coordinating group lifecycle, sleep/wake cycles, and merging
        // Matches cell_research CellGroup.py:104-122
        
        while (status != GroupStatus.MERGED && !allCellsInactive()) {
            
            // Phase change: toggle ACTIVE ↔ SLEEP when countdown expires
            if (countDown == 0) {
                changeStatus();
            }
            
            // SLEEP phase: put cells to sleep, countdown, wait
            if (status == GroupStatus.SLEEP) {
                putCellsToSleep();
                countDown--;
                try {
                    Thread.sleep(50); // 50ms matches Python time.sleep(0.05)
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // Exit on interruption
                }
            }
            
            // ACTIVE phase: check sorted status, attempt merge, countdown, wait
            if (status == GroupStatus.ACTIVE) {
                lock.lock();
                try {
                    // Recheck status under lock (may have changed)
                    if (status == GroupStatus.ACTIVE && isGroupSorted()) {
                        CellGroup<T> nextGroup = findNextGroup();
                        
                        // Merge if next group exists, is ACTIVE, and sorted
                        if (nextGroup != null 
                            && nextGroup.status == GroupStatus.ACTIVE 
                            && nextGroup.isGroupSorted()) {
                            mergeWithGroup(nextGroup);
                        }
                    }
                } finally {
                    lock.unlock();
                }
                
                countDown--;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // Thread exits when status == MERGED or all cells INACTIVE
        // This implements hierarchical emergence: small groups merge into larger ones
    }
    
    // ========== ACCESSORS ==========
    
    public int getGroupId() {
        return groupId;
    }
    
    public List<T> getCellsInGroup() {
        return cellsInGroup;
    }
    
    public int getLeftBoundaryPosition() {
        return leftBoundaryPosition;
    }
    
    public int getRightBoundaryPosition() {
        return rightBoundaryPosition;
    }
    
    public GroupStatus getStatus() {
        return status;
    }
    
    public void setStatus(GroupStatus status) {
        this.status = status;
    }
    
    public int getPhasePeriod() {
        return phasePeriod;
    }
    
    public int getCountDown() {
        return countDown;
    }
}
