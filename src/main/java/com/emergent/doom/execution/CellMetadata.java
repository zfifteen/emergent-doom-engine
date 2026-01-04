package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.SortDirection;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Metadata for cell execution behavior, decoupled from cell domain data.
 *
 * <p>PURPOSE: Store algotype, sort direction, and position tracking state that was previously
 * embedded in cell implementations. This enables true domain-agnostic cells that are pure
 * Comparable data carriers.</p>
 *
 * <p>ARCHITECTURE ROLE: Execution engines maintain a parallel metadata array indexed by cell
 * position. When evaluating cell[i], the engine consults metadata[i] for behavioral policy
 * without interrogating the cell object itself.</p>
 *
 * <p>EXPECTED INPUTS: Algotype, SortDirection (required), idealPos (for SELECTION algotype)</p>
 * <p>EXPECTED OUTPUTS: Getters/setters for metadata fields, thread-safe ideal position ops</p>
 * <p>DATA FLOW: MetadataProvider (index → metadata) → Engine evaluation logic</p>
 *
 * <p>DESIGN RATIONALE: Per lightweight-cell-plan.md Phase 3, this achieves "true generality"
 * by separating domain concerns (cell comparison) from execution concerns (sorting policy).</p>
 *
 * @see <a href="https://github.com/zfifteen/emergent-doom-engine/pull/55">PR #55</a>
 */
public class CellMetadata {
    
    // PURPOSE: Sorting algorithm policy for this cell position
    // INPUTS: Set during construction or via setAlgotype()
    // PROCESS: Determines neighbor visibility and swap rules
    // OUTPUTS: BUBBLE, INSERTION, or SELECTION
    // DEPENDENCIES: Must be non-null
    private final Algotype algotype;
    
    // PURPOSE: Sort direction preference (ascending or descending)
    // INPUTS: Set during construction or via setSortDirection()
    // PROCESS: Affects comparison polarity in swap evaluation
    // OUTPUTS: ASCENDING or DESCENDING
    // DEPENDENCIES: Must be non-null
    private final SortDirection sortDirection;
    
    // PURPOSE: Target position for SELECTION algotype cells
    // INPUTS: Initialized to 0 (ascending) or rightBoundary (descending)
    // PROCESS: Increments when swap denied, updated on group merge
    // OUTPUTS: Current ideal position (0-based index)
    // DEPENDENCIES: AtomicInteger for thread-safe access
    // NOTE: Only used for SELECTION algotype
    private final AtomicInteger idealPos;
    
    /**
     * Create metadata with specified algotype and sort direction.
     *
     * <p>PURPOSE: Primary constructor for creating cell metadata with explicit policies.</p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>algotype - Behavioral policy (BUBBLE, INSERTION, or SELECTION)</li>
     *   <li>sortDirection - Direction preference (ASCENDING or DESCENDING)</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Validate algotype is not null (throw IllegalArgumentException if null)</li>
     *   <li>Validate sortDirection is not null (throw IllegalArgumentException if null)</li>
     *   <li>Store algotype as immutable field</li>
     *   <li>Store sortDirection as immutable field</li>
     *   <li>Initialize idealPos to 0 (will be updated for DESCENDING via updateForBoundary)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Fully initialized CellMetadata instance</p>
     *
     * @param algotype the sorting algorithm policy (required, non-null)
     * @param sortDirection the sort direction (required, non-null)
     * @throws IllegalArgumentException if algotype or sortDirection is null
     */
    public CellMetadata(Algotype algotype, SortDirection sortDirection) {
        // PURPOSE: Validate inputs and initialize metadata fields
        // PROCESS:
        //   1. Validate algotype is not null
        //   2. Validate sortDirection is not null
        //   3. Store algotype and sortDirection as immutable fields
        //   4. Initialize idealPos to 0 (will be updated via updateForBoundary if needed)
        
        if (algotype == null) {
            throw new IllegalArgumentException("Algotype cannot be null");
        }
        if (sortDirection == null) {
            throw new IllegalArgumentException("SortDirection cannot be null");
        }
        
        this.algotype = algotype;
        this.sortDirection = sortDirection;
        this.idealPos = new AtomicInteger(0);  // Levin: initial ideal position is leftmost (0)
    }
    
    /**
     * Get the algotype of this metadata.
     *
     * <p>PURPOSE: Provides the cell's sorting policy to execution engines for
     * neighbor selection and swap evaluation.</p>
     *
     * <p>INPUTS: None (getter method)</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Retrieve immutable algotype field</li>
     *   <li>Return Algotype enum value</li>
     *   <li>Thread-safe (field is final and enum is immutable)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Algotype - BUBBLE, INSERTION, or SELECTION</p>
     *
     * @return the algotype (never null)
     */
    public Algotype getAlgotype() {
        // PURPOSE: Return the stored algotype for engine evaluation
        // PROCESS: Return immutable field (thread-safe due to final + enum immutability)
        return algotype;
    }
    
    /**
     * Get the sort direction of this metadata.
     *
     * <p>PURPOSE: Provides the cell's sorting direction to execution engines for
     * direction-aware swap evaluation.</p>
     *
     * <p>INPUTS: None (getter method)</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Retrieve immutable sortDirection field</li>
     *   <li>Return SortDirection enum value</li>
     *   <li>Thread-safe (field is final and enum is immutable)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: SortDirection - ASCENDING or DESCENDING</p>
     *
     * @return the sort direction (never null)
     */
    public SortDirection getSortDirection() {
        // PURPOSE: Return the stored sort direction for engine evaluation
        // PROCESS: Return immutable field (thread-safe due to final + enum immutability)
        return sortDirection;
    }
    
    /**
     * Get the ideal position for SELECTION algotype.
     *
     * <p>PURPOSE: Provides the target position for SELECTION cells during swap evaluation.
     * Thread-safe for parallel execution.</p>
     *
     * <p>INPUTS: None (getter method)</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Call AtomicInteger.get() for thread-safe read</li>
     *   <li>Return current ideal position value</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Current ideal position (0-based index)</p>
     *
     * <p>NOTE: Only meaningful for SELECTION algotype. For other algotypes, this value
     * is unused but maintained for consistency.</p>
     *
     * @return the current ideal position
     */
    public int getIdealPos() {
        // PURPOSE: Return the current ideal position for SELECTION algotype
        // PROCESS: Thread-safe read via AtomicInteger.get()
        return idealPos.get();
    }
    
    /**
     * Set the ideal position to a specific value.
     *
     * <p>PURPOSE: Update the target position for SELECTION cells, typically during
     * group merge operations or initialization.</p>
     *
     * <p>INPUTS: newIdealPos - the new ideal position (0-based index)</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Call AtomicInteger.set() for thread-safe write</li>
     *   <li>Update idealPos field to new value</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: None (mutates internal state)</p>
     *
     * <p>NOTE: Only meaningful for SELECTION algotype.</p>
     *
     * @param newIdealPos the new ideal position
     */
    public void setIdealPos(int newIdealPos) {
        // PURPOSE: Update the ideal position for SELECTION algotype
        // PROCESS: Thread-safe write via AtomicInteger.set()
        idealPos.set(newIdealPos);
    }
    
    /**
     * Atomically increment the ideal position and return the new value.
     *
     * <p>PURPOSE: Update target position when swap is denied (Levin p.9: cell adjusts
     * target rightward). Thread-safe for parallel execution.</p>
     *
     * <p>INPUTS: None (increment operation)</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Call AtomicInteger.incrementAndGet() for thread-safe increment</li>
     *   <li>Return new value after increment</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: New ideal position after increment</p>
     *
     * <p>NOTE: Only used for SELECTION algotype during swap denial.</p>
     *
     * @return the new ideal position after increment
     */
    public int incrementIdealPos() {
        // PURPOSE: Atomically increment ideal position when swap is denied
        // PROCESS: Thread-safe increment via AtomicInteger.incrementAndGet()
        // OUTPUTS: New value after increment
        return idealPos.incrementAndGet();
    }
    
    /**
     * Atomically compare-and-set the ideal position.
     *
     * <p>PURPOSE: Enable atomic conditional updates for concurrent coordination when
     * exact synchronization is needed.</p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>expected - the expected current value</li>
     *   <li>newValue - the new value to set</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Call AtomicInteger.compareAndSet() with expected and newValue</li>
     *   <li>If current value matches expected, update to newValue</li>
     *   <li>Return true if successful, false otherwise</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: true if update succeeded, false if current value didn't match expected</p>
     *
     * @param expected the expected current value
     * @param newValue the new value to set
     * @return true if successful
     */
    public boolean compareAndSetIdealPos(int expected, int newValue) {
        // PURPOSE: Atomically update ideal position only if current value matches expected
        // PROCESS: Thread-safe CAS via AtomicInteger.compareAndSet()
        // OUTPUTS: true if update succeeded, false if current value didn't match expected
        return idealPos.compareAndSet(expected, newValue);
    }
    
    /**
     * Update ideal position based on group boundaries.
     *
     * <p>PURPOSE: Reset ideal position when groups merge, matching Python cell_research
     * SelectionSortCell.update() behavior.</p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>leftBoundary - the left boundary position (0-based)</li>
     *   <li>rightBoundary - the right boundary position (0-based)</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>If sortDirection is DESCENDING, set idealPos to rightBoundary</li>
     *   <li>If sortDirection is ASCENDING, set idealPos to leftBoundary</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: None (mutates idealPos)</p>
     *
     * <p>GROUND TRUTH REFERENCE: cell_research/SelectionSortCell.py:77-81:
     * <pre>
     * def update(self):
     *     # Called when group merges - reset ideal position
     *     if self.reverse_direction:
     *         self.ideal_position = self.right_boundary
     *     else:
     *         self.ideal_position = self.left_boundary
     * </pre>
     * </p>
     *
     * <p>NOTE: Only meaningful for SELECTION algotype.</p>
     *
     * @param leftBoundary the left boundary position
     * @param rightBoundary the right boundary position
     */
    public void updateForBoundary(int leftBoundary, int rightBoundary) {
        // PURPOSE: Reset ideal position based on sort direction after group merge
        // PROCESS:
        //   1. If DESCENDING, set idealPos to rightBoundary (cells start at right end)
        //   2. If ASCENDING, set idealPos to leftBoundary (cells start at left end)
        // GROUND TRUTH: Matches SelectionSortCell.py:77-81 update() behavior
        
        if (sortDirection == SortDirection.DESCENDING) {
            setIdealPos(rightBoundary);
        } else {
            setIdealPos(leftBoundary);
        }
    }
}
