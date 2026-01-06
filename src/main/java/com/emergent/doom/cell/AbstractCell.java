package com.emergent.doom.cell;

import com.emergent.doom.group.CellStatus;

import java.util.Optional;

/**
 * Abstract base class for all cell implementations in the Emergent Doom Engine.
 *
 * <p><strong>PURPOSE:</strong> Provide domain-agnostic substrate for emergent cell-based
 * problem solving where algotype is an intrinsic property that travels with the cell
 * during swaps, enabling Levin-style morphogenetic clustering.</p>
 *
 * <p><strong>KEY ARCHITECTURAL PRINCIPLE:</strong> Algotype is bound to the cell object itself,
 * NOT to array position. When cells swap, their algotypes move WITH them, enabling dynamic
 * spatial aggregation patterns that can change during execution.</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Levin-aligned semantics: Algotype fixed per cell, carried through swaps, clustering meaningful</li>
 *   <li>Domain-agnostic substrate: Engine only cares about cells exposing common interface</li>
 *   <li>Lightweight inheritance: Domains plug in via minimal method overrides</li>
 *   <li>Type safety: Parameterized types prevent mixing incompatible value/algotype types</li>
 * </ul>
 *
 * <p><strong>TYPE PARAMETERS:</strong></p>
 * <ul>
 *   <li>V - Value type (Integer for sorting, FactorCandidate for factorization, etc.)</li>
 *   <li>A - Algotype enum (SortingAlgotype, FactorizationAlgotype, etc.)</li>
 * </ul>
 *
 * <p><strong>EXPECTED INPUTS:</strong> Subclasses provide domain-specific value and algotype</p>
 * <p><strong>EXPECTED OUTPUTS:</strong> Methods returning intrinsic properties and behavioral decisions</p>
 * <p><strong>DATA FLOW:</strong> Engine queries cells → Cells return properties/decisions → Engine executes swaps</p>
 *
 * @param <V> the value type (must be Comparable for ordering)
 * @param <A> the algotype enum type
 */
public abstract class AbstractCell<V extends Comparable<V>, A extends Enum<A>> implements Comparable<AbstractCell<V, A>> {
    
    // ==================== INTRINSIC PROPERTIES (Immutable) ====================
    
    /**
     * Read the algotype of this cell.
     *
     * <p><strong>PURPOSE:</strong> Provide access to the cell's behavioral policy.
     * This algotype is an intrinsic property that travels WITH the cell during swaps.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Return the immutable algotype field from subclass implementation</li>
     *   <li>Must be consistent throughout cell's lifetime</li>
     *   <li>Used by engine for neighbor selection and swap rules</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> The cell's algotype (never null)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> Immutability ensures algotype clustering
     * reflects genuine strategic compatibility, not behavioral drift.</p>
     *
     * @return the algotype (immutable, never null)
     */
    public abstract A readAlgotype();
    
    /**
     * Read the value of this cell.
     *
     * <p><strong>PURPOSE:</strong> Provide access to the domain-specific value for
     * comparison and metrics.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Return the immutable value field from subclass implementation</li>
     *   <li>Value type is domain-specific (Integer for sorting, etc.)</li>
     *   <li>Used for compareTo() implementation and metrics</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> The cell's value (never null)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @return the value (immutable, never null)
     */
    public abstract V readValue();
    
    // ==================== MUTABLE STATE (Positional/Operational) ====================
    
    /**
     * Read the current array position of this cell.
     *
     * <p><strong>PURPOSE:</strong> Track where the cell currently resides in the array.
     * Updated by engine after swaps.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Return the current position field (0-based index)</li>
     *   <li>Position is mutable and changes during swaps</li>
     *   <li>Used for neighbor calculations and swap proposals</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Current position (0 to arraySize-1)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Engine must call updatePositionTo() after swaps</p>
     *
     * @return the current position in the array
     */
    public abstract int readCurrentPosition();
    
    /**
     * Update the position of this cell after a swap.
     *
     * <p><strong>PURPOSE:</strong> Notify cell of new position after engine executes swap.
     * Called by engine swap logic.</p>
     *
     * <p><strong>INPUTS:</strong> newPosition - the new array index (0-based)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate newPosition is non-negative</li>
     *   <li>Update internal position field</li>
     *   <li>Subclasses may trigger position-dependent state updates</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> None (mutates cell state)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Must be called by engine after swap execution</p>
     *
     * @param newPosition the new position in the array
     */
    public abstract void updatePositionTo(int newPosition);
    
    /**
     * Read the current execution status of this cell.
     *
     * <p><strong>PURPOSE:</strong> Determine if cell can participate in swaps.
     * Used by engine to skip FREEZE/SLEEP/INACTIVE cells.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Return current CellStatus enum value</li>
     *   <li>ACTIVE = can initiate and accept swaps</li>
     *   <li>FREEZE = cannot initiate but can accept swaps</li>
     *   <li>SLEEP/INACTIVE = cannot participate in swaps</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> CellStatus enum (never null)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @return the current status
     */
    public abstract CellStatus readStatus();
    
    /**
     * Update the execution status of this cell.
     *
     * <p><strong>PURPOSE:</strong> Allow engine or external controller to change cell state.
     * Used for freezing cells, putting groups to sleep, etc.</p>
     *
     * <p><strong>INPUTS:</strong> newStatus - the new CellStatus (never null)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate newStatus is not null</li>
     *   <li>Store previous status for potential rollback</li>
     *   <li>Update current status field</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> None (mutates cell state)</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @param newStatus the new status to set
     */
    public abstract void updateStatusTo(CellStatus newStatus);
    
    // ==================== BEHAVIORAL POLICY (Algotype-Specific) ====================
    
    /**
     * Determine if this cell should attempt to move given its neighbors.
     *
     * <p><strong>PURPOSE:</strong> Implement algotype-specific movement predicate.
     * Separates "do I want to move?" from "where should I move?"</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView providing visible cells</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Inspect neighbors based on algotype visibility rules</li>
     *   <li>Apply algotype-specific movement logic</li>
     *   <li>Return boolean decision</li>
     *   <li>Examples:
     *     <ul>
     *       <li>BUBBLE: true if neighbor exists with wrong value ordering</li>
     *       <li>INSERTION: true if left side is sorted and can insert</li>
     *       <li>SELECTION: true if not at ideal position</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if cell wants to move, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> NeighborhoodView must provide correct visibility</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> Mirrors Levin's asymmetry between Bubble
     * (opportunistic) and Selection (targeted).</p>
     *
     * @param neighbors the neighborhood view for this cell
     * @return true if cell should attempt to move
     */
    public abstract boolean shouldMoveGiven(NeighborhoodView<V, A> neighbors);
    
    /**
     * Calculate the target position for this cell's move.
     *
     * <p><strong>PURPOSE:</strong> Implement algotype-specific target selection.
     * Returns Optional.empty() if no valid target exists.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView providing visible cells</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Inspect neighbors based on algotype rules</li>
     *   <li>Calculate optimal target position</li>
     *   <li>Return Optional.of(position) if valid target exists</li>
     *   <li>Return Optional.empty() if no valid target</li>
     *   <li>Examples:
     *     <ul>
     *       <li>BUBBLE: return position of randomly selected neighbor</li>
     *       <li>INSERTION: return position of left neighbor if sorted</li>
     *       <li>SELECTION: return ideal position field</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Optional containing target position, or empty</p>
     *
     * <p><strong>DEPENDENCIES:</strong> NeighborhoodView must provide correct visibility</p>
     *
     * <p><strong>DESIGN RATIONALE:</strong> Optional cleanly handles "no valid target"
     * without magic numbers or exceptions.</p>
     *
     * @param neighbors the neighborhood view for this cell
     * @return Optional containing target position, or empty if no valid target
     */
    public abstract Optional<Integer> calculateTargetPositionGiven(NeighborhoodView<V, A> neighbors);
    
    // ==================== SWAP ELIGIBILITY ====================
    
    /**
     * Check if this cell can initiate a swap.
     *
     * <p><strong>PURPOSE:</strong> Determine if cell is allowed to propose swaps.
     * Used by engine to skip frozen/inactive cells.</p>
     *
     * <p><strong>INPUTS:</strong> None (uses internal status)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check if status is ACTIVE</li>
     *   <li>Return false if FREEZE, SLEEP, INACTIVE, etc.</li>
     *   <li>Return true if ACTIVE</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if can initiate swaps, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Requires readStatus() implementation</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> FREEZE cells can be swapped but cannot
     * initiate swaps (one-way asymmetry).</p>
     *
     * @return true if cell can initiate swaps
     */
    public abstract boolean canInitiateSwap();
    
    /**
     * Check if this cell can accept a swap from an initiating cell.
     *
     * <p><strong>PURPOSE:</strong> Determine if cell is allowed to participate in
     * a swap proposed by another cell.</p>
     *
     * <p><strong>INPUTS:</strong> initiator - the cell proposing the swap</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check if this cell's status allows swaps</li>
     *   <li>ACTIVE and FREEZE can accept swaps</li>
     *   <li>SLEEP and INACTIVE cannot accept swaps</li>
     *   <li>Optionally check compatibility with initiator</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if can accept swap, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Requires readStatus() implementation</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> Enables asymmetric swap eligibility
     * (frozen cells can be moved but can't initiate).</p>
     *
     * @param initiator the cell proposing the swap
     * @return true if cell can accept swap from initiator
     */
    public abstract boolean canAcceptSwapFrom(AbstractCell<V, A> initiator);
    
    // ==================== VALUE COMPARISON (Domain-Specific) ====================
    
    /**
     * Check if this cell's value is greater than another cell's value.
     *
     * <p><strong>PURPOSE:</strong> Provide domain-specific value comparison for
     * swap decisions. More readable than compareTo() in swap logic.</p>
     *
     * <p><strong>INPUTS:</strong> other - the cell to compare against</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Extract value from this cell</li>
     *   <li>Extract value from other cell</li>
     *   <li>Compare using domain-specific comparison logic</li>
     *   <li>Return true if this.value > other.value</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if this cell has greater value</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Requires readValue() and Comparable implementation</p>
     *
     * <p><strong>DESIGN RATIONALE:</strong> Named method more readable than
     * "compareTo(other) > 0" in swap logic.</p>
     *
     * @param other the cell to compare against
     * @return true if this cell's value is greater
     */
    public abstract boolean hasGreaterValueThan(AbstractCell<V, A> other);
    
    /**
     * Compare this cell to another based on their values.
     *
     * <p><strong>PURPOSE:</strong> Implement Comparable contract for sorting.
     * Comparison is based ONLY on value, NOT on algotype.</p>
     *
     * <p><strong>INPUTS:</strong> other - Another AbstractCell to compare against</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Extract value from this cell via readValue()</li>
     *   <li>Extract value from other cell via readValue()</li>
     *   <li>Delegate to value's compareTo() method</li>
     *   <li>Return comparison result</li>
     *   <li>Algotype is ignored in comparison</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Negative if this < other, zero if equal, positive if this > other</p>
     *
     * <p><strong>DEPENDENCIES:</strong> Requires V to implement Comparable</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> Cells with different algotypes can
     * sort together correctly since comparison ignores algotype.</p>
     *
     * @param other the cell to compare against
     * @return negative if this < other, zero if equal, positive if this > other
     */
    @Override
    public int compareTo(AbstractCell<V, A> other) {
        // PURPOSE: Delegate to value comparison (domain-specific)
        // PROCESS: Extract values and compare using Comparable contract
        // ARCHITECTURE: Algotype is ignored - cells sort by value only
        return this.readValue().compareTo(other.readValue());
    }
}
