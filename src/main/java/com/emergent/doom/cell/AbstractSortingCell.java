package com.emergent.doom.cell;

import com.emergent.doom.group.CellStatus;

/**
 * Abstract base for sorting domain cells.
 *
 * <p><strong>PURPOSE:</strong> Provide sorting-specific base implementation that extends
 * AbstractCell&lt;Integer, SortingAlgotype&gt; with common sorting functionality. All concrete
 * sorting cells (Bubble, Selection, Insertion) extend this class.</p>
 *
 * <p><strong>KEY ARCHITECTURAL ROLE:</strong> This is the "main entry point" for the sorting
 * domain, implementing the AbstractCell contract with Integer values and SortingAlgotype enums.
 * Subclasses override behavioral methods (shouldMoveGiven, calculateTargetPositionGiven) to
 * implement algotype-specific logic.</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Domain-specific: Fixes value type to Integer and algotype type to SortingAlgotype</li>
 *   <li>Shared state: Provides common mutable state (position, status) for all sorting cells</li>
 *   <li>Template pattern: Subclasses override behavioral methods while inheriting state management</li>
 *   <li>Levin-aligned: Algotype is immutable and travels with cell during swaps</li>
 * </ul>
 *
 * <p><strong>EXPECTED INPUTS:</strong> value (Integer), algotype (SortingAlgotype), position (int)</p>
 * <p><strong>EXPECTED OUTPUTS:</strong> Methods implementing AbstractCell contract</p>
 * <p><strong>DATA FLOW:</strong> Engine queries cell → Cell returns decisions → Engine executes swaps</p>
 */
public abstract class AbstractSortingCell extends AbstractCell<Integer, SortingAlgotype> {
    
    // ==================== IMMUTABLE INTRINSIC PROPERTIES ====================
    
    // PURPOSE: The sort key value for this cell
    // INPUTS: Set during construction, never changes
    // PROCESS: Used for comparisons and swap decisions
    // OUTPUTS: Available via readValue()
    // DEPENDENCIES: None
    // ARCHITECTURE NOTE: Immutability ensures value is stable during sorting
    protected final int value;
    
    // PURPOSE: The sorting algorithm behavioral policy
    // INPUTS: Set during construction, never changes
    // PROCESS: Determines neighbor visibility and swap rules
    // OUTPUTS: Available via readAlgotype()
    // DEPENDENCIES: None
    // ARCHITECTURE NOTE: Immutability ensures algotype clustering reflects genuine
    //                    strategic compatibility, not behavioral drift
    protected final SortingAlgotype algotype;
    
    // ==================== MUTABLE POSITIONAL STATE ====================
    
    // PURPOSE: Current position in the cell array
    // INPUTS: Set during construction, updated by engine after swaps
    // PROCESS: Used for neighbor calculations and swap proposals
    // OUTPUTS: Available via readCurrentPosition()
    // DEPENDENCIES: Engine must call updatePositionTo() after swaps
    // ARCHITECTURE NOTE: Position is mutable because cells physically relocate
    protected int currentPosition;
    
    // PURPOSE: Current execution status controlling swap eligibility
    // INPUTS: Initialized to ACTIVE, updated by engine or external controller
    // PROCESS: ACTIVE = can initiate/accept swaps
    //          FREEZE = cannot initiate but can accept swaps
    //          SLEEP/INACTIVE = cannot participate in swaps
    // OUTPUTS: Available via readStatus()
    // DEPENDENCIES: None
    // ARCHITECTURE NOTE: Enables dynamic control over cell participation
    protected CellStatus status;
    
    /**
     * Create an AbstractSortingCell with specified value, algotype, and position.
     *
     * <p><strong>PURPOSE:</strong> Initialize sorting cell with immutable intrinsic properties
     * and mutable positional state. This is the main constructor called by all subclasses.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>value - The sort key (immutable)</li>
     *   <li>algotype - The behavioral policy (immutable, never null)</li>
     *   <li>initialPosition - Starting position in array (mutable)</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate algotype is not null</li>
     *   <li>Validate initialPosition is non-negative</li>
     *   <li>Store value and algotype as final fields</li>
     *   <li>Store initialPosition as mutable field</li>
     *   <li>Initialize status to ACTIVE</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Fully initialized AbstractSortingCell</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> This constructor establishes the cell's immutable
     * identity (value + algotype) and mutable state (position + status). Subclasses add
     * algotype-specific fields (e.g., idealPosition for SELECTION).</p>
     *
     * @param value the sort key value
     * @param algotype the sorting algotype (never null)
     * @param initialPosition the starting position in array (non-negative)
     * @throws NullPointerException if algotype is null
     * @throws IllegalArgumentException if initialPosition is negative
     */
    protected AbstractSortingCell(int value, SortingAlgotype algotype, int initialPosition) {
        // PURPOSE: Initialize immutable intrinsic properties and mutable positional state
        // PROCESS:
        //   1. Validate inputs (algotype non-null, initialPosition non-negative)
        //   2. Store value and algotype as final fields (immutable identity)
        //   3. Store initialPosition as mutable field
        //   4. Initialize status to ACTIVE (default state)
        
        if (algotype == null) {
            throw new NullPointerException("algotype cannot be null");
        }
        if (initialPosition < 0) {
            throw new IllegalArgumentException("initialPosition must be non-negative, got: " + initialPosition);
        }
        
        this.value = value;
        this.algotype = algotype;
        this.currentPosition = initialPosition;
        this.status = CellStatus.ACTIVE;
    }
    
    // ==================== INTRINSIC PROPERTY ACCESSORS ====================
    
    /**
     * Read the algotype of this cell.
     *
     * <p><strong>PURPOSE:</strong> Implement AbstractCell contract for algotype access.
     * Returns the immutable algotype that travels with this cell during swaps.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>PROCESS:</strong> Return the immutable algotype field</p>
     *
     * <p><strong>OUTPUTS:</strong> SortingAlgotype (never null)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @return the sorting algotype
     */
    @Override
    public SortingAlgotype readAlgotype() {
        // PURPOSE: Return immutable algotype field
        // ARCHITECTURE: Algotype is bound to cell object, travels with cell during swaps
        return algotype;
    }
    
    /**
     * Read the value of this cell.
     *
     * <p><strong>PURPOSE:</strong> Implement AbstractCell contract for value access.
     * Returns the immutable integer value used for sorting.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>PROCESS:</strong> Return the immutable value field</p>
     *
     * <p><strong>OUTPUTS:</strong> Integer value (never null)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @return the sort key value
     */
    @Override
    public Integer readValue() {
        // PURPOSE: Return immutable value field
        // ARCHITECTURE: Value is stable during sorting, used for comparisons
        return value;
    }
    
    // ==================== MUTABLE STATE ACCESSORS ====================
    
    /**
     * Read the current position of this cell in the array.
     *
     * <p><strong>PURPOSE:</strong> Implement AbstractCell contract for position tracking.
     * Returns the current array index where this cell resides.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>PROCESS:</strong> Return the mutable currentPosition field</p>
     *
     * <p><strong>OUTPUTS:</strong> Current position (0-based index)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Engine must keep position synchronized via updatePositionTo()</p>
     *
     * @return the current position
     */
    @Override
    public int readCurrentPosition() {
        // PURPOSE: Return current position in array
        // ARCHITECTURE: Position is mutable, updated by engine after swaps
        return currentPosition;
    }
    
    /**
     * Update the position of this cell after a swap.
     *
     * <p><strong>PURPOSE:</strong> Implement AbstractCell contract for position updates.
     * Called by engine after executing a swap to keep position synchronized.</p>
     *
     * <p><strong>INPUTS:</strong> newPosition - the new array index (non-negative)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate newPosition is non-negative</li>
     *   <li>Update currentPosition field</li>
     *   <li>Subclasses may override to trigger position-dependent state updates</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> None (mutates currentPosition field)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Must be called by engine after swap execution</p>
     *
     * @param newPosition the new position in the array
     * @throws IllegalArgumentException if newPosition is negative
     */
    @Override
    public void updatePositionTo(int newPosition) {
        // PURPOSE: Update position after swap
        // PROCESS:
        //   1. Validate newPosition is non-negative
        //   2. Update currentPosition field
        // ARCHITECTURE: Subclasses can override to trigger position-dependent updates
        
        if (newPosition < 0) {
            throw new IllegalArgumentException("newPosition must be non-negative, got: " + newPosition);
        }
        this.currentPosition = newPosition;
    }
    
    /**
     * Read the current execution status of this cell.
     *
     * <p><strong>PURPOSE:</strong> Implement AbstractCell contract for status access.
     * Returns the current status controlling swap eligibility.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>PROCESS:</strong> Return the mutable status field</p>
     *
     * <p><strong>OUTPUTS:</strong> CellStatus (never null)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @return the current status
     */
    @Override
    public CellStatus readStatus() {
        // PURPOSE: Return current execution status
        // ARCHITECTURE: Status controls swap eligibility (ACTIVE, FREEZE, SLEEP, INACTIVE)
        return status;
    }
    
    /**
     * Update the execution status of this cell.
     *
     * <p><strong>PURPOSE:</strong> Implement AbstractCell contract for status updates.
     * Allows engine or external controller to change cell state.</p>
     *
     * <p><strong>INPUTS:</strong> newStatus - the new status (never null)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate newStatus is not null</li>
     *   <li>Update status field</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> None (mutates status field)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @param newStatus the new status to set
     * @throws NullPointerException if newStatus is null
     */
    @Override
    public void updateStatusTo(CellStatus newStatus) {
        // PURPOSE: Update execution status
        // PROCESS:
        //   1. Validate newStatus is not null
        //   2. Update status field
        
        if (newStatus == null) {
            throw new NullPointerException("newStatus cannot be null");
        }
        this.status = newStatus;
    }
    
    // ==================== SWAP ELIGIBILITY ====================
    
    /**
     * Check if this cell can initiate a swap.
     *
     * <p><strong>PURPOSE:</strong> Implement AbstractCell contract for swap initiation check.
     * Returns true only if status is ACTIVE.</p>
     *
     * <p><strong>INPUTS:</strong> None (uses status field)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check if status == ACTIVE</li>
     *   <li>Return true if ACTIVE, false otherwise</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if can initiate swaps, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> FREEZE cells can be swapped but cannot
     * initiate swaps (one-way asymmetry).</p>
     *
     * @return true if cell can initiate swaps
     */
    @Override
    public boolean canInitiateSwap() {
        // PURPOSE: Check if cell can propose swaps
        // ARCHITECTURE: Only ACTIVE cells can initiate (FREEZE cannot initiate but can accept)
        return status == CellStatus.ACTIVE;
    }
    
    /**
     * Check if this cell can accept a swap from an initiating cell.
     *
     * <p><strong>PURPOSE:</strong> Implement AbstractCell contract for swap acceptance check.
     * Returns true if status is ACTIVE or FREEZE (but not SLEEP/INACTIVE).</p>
     *
     * <p><strong>INPUTS:</strong> initiator - the cell proposing the swap (not currently used)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check if status is ACTIVE or FREEZE</li>
     *   <li>Return true if either, false otherwise</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if can accept swap, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> FREEZE cells can be moved by others,
     * enabling asymmetric swap eligibility.</p>
     *
     * @param initiator the cell proposing the swap
     * @return true if cell can accept swap
     */
    @Override
    public boolean canAcceptSwapFrom(AbstractCell<Integer, SortingAlgotype> initiator) {
        // PURPOSE: Check if cell can participate in swap proposed by another cell
        // ARCHITECTURE: ACTIVE and FREEZE can accept (SLEEP/INACTIVE cannot)
        return status == CellStatus.ACTIVE || status == CellStatus.FREEZE;
    }
    
    // ==================== VALUE COMPARISON ====================
    
    /**
     * Check if this cell's value is greater than another cell's value.
     *
     * <p><strong>PURPOSE:</strong> Implement AbstractCell contract for readable value comparison.
     * More readable than "compareTo(other) > 0" in swap logic.</p>
     *
     * <p><strong>INPUTS:</strong> other - the cell to compare against</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Extract value from this cell</li>
     *   <li>Extract value from other cell</li>
     *   <li>Return this.value > other.readValue()</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if this cell has greater value</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @param other the cell to compare against
     * @return true if this cell's value is greater
     */
    @Override
    public boolean hasGreaterValueThan(AbstractCell<Integer, SortingAlgotype> other) {
        // PURPOSE: Readable value comparison for swap logic
        // ARCHITECTURE: Delegates to Integer comparison
        return this.value > other.readValue();
    }
    
    // ==================== BEHAVIORAL POLICY (Abstract - Subclass Override) ====================
    
    /**
     * Determine if this cell should attempt to move given its neighbors.
     *
     * <p><strong>PURPOSE:</strong> Algotype-specific movement decision logic.
     * Subclasses override to implement BUBBLE/INSERTION/SELECTION/FIBONACCI logic.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView providing visible cells</p>
     *
     * <p><strong>PROCESS:</strong> Implemented by subclasses based on algotype rules</p>
     *
     * <p><strong>OUTPUTS:</strong> true if cell wants to move, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Subclass implementation</p>
     *
     * @param neighbors the neighborhood view for this cell
     * @return true if cell should attempt to move
     */
    @Override
    public abstract boolean shouldMoveGiven(NeighborhoodView<Integer, SortingAlgotype> neighbors);
    
    /**
     * Calculate the target position for this cell's move.
     *
     * <p><strong>PURPOSE:</strong> Algotype-specific target selection logic.
     * Subclasses override to implement BUBBLE/INSERTION/SELECTION/FIBONACCI logic.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView providing visible cells</p>
     *
     * <p><strong>PROCESS:</strong> Implemented by subclasses based on algotype rules</p>
     *
     * <p><strong>OUTPUTS:</strong> Optional containing target position, or empty</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Subclass implementation</p>
     *
     * @param neighbors the neighborhood view for this cell
     * @return Optional containing target position, or empty if no valid target
     */
    @Override
    public abstract java.util.Optional<Integer> calculateTargetPositionGiven(
        NeighborhoodView<Integer, SortingAlgotype> neighbors);
}
