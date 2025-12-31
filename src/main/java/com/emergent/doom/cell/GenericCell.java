package com.emergent.doom.cell;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A flexible cell implementation that can represent any algotype and sort direction.
 *
 * <p>GenericCell enables chimeric population experiments where cells of different
 * algotypes (BUBBLE, INSERTION, SELECTION) coexist in the same array. Each cell
 * carries a fixed value for comparison and an assigned algotype for behavioral policy.</p>
 *
 * <p>From Levin et al. (2024), p.11-12:
 * "At the beginning of these experiments, we randomly assigned one of the three
 * different Algotypes to each of the cells, and began the sort as previously,
 * allowing all the cells to move based on their Algotype."</p>
 *
 * <p>From Levin et al. (2024), p.14 (Cross-Purpose Sorting):
 * "we performed experiments using two mixed Algotypes, where one was made to sort in
 * *decreasing* order while the other sorted in *increasing* order."</p>
 *
 * <p>For SELECTION algotype cells, GenericCell maintains an idealPos field that tracks
 * the cell's target position, matching the behavior of SelectionCell. This field starts
 * at 0 and increments when swap attempts are denied, per Levin p.9.</p>
 *
 * <p>Usage:
 * <pre>{@code
 * // Create a cell with value 42 and Bubble algotype
 * GenericCell cell = new GenericCell(42, Algotype.BUBBLE);
 *
 * // Create 50/50 Bubble/Selection mix
 * GenericCell[] cells = new GenericCell[100];
 * for (int i = 0; i < 100; i++) {
 *     Algotype type = (i % 2 == 0) ? Algotype.BUBBLE : Algotype.SELECTION;
 *     cells[i] = new GenericCell(randomValue(), type);
 * }
 * 
 * // Create cross-purpose sorting population (ascending vs descending)
 * GenericCell[] crossPurpose = new GenericCell[100];
 * for (int i = 0; i < 100; i++) {
 *     SortDirection dir = (i % 2 == 0) ? SortDirection.ASCENDING : SortDirection.DESCENDING;
 *     crossPurpose[i] = new GenericCell(randomValue(), Algotype.BUBBLE, dir);
 * }
 * }</pre></p>
 */
public class GenericCell implements Cell<GenericCell>, HasIdealPosition, HasSortDirection {

    private final int value;
    private final Algotype algotype;
    private final SortDirection sortDirection;  // Direction preference (ascending or descending)
    private final AtomicInteger idealPos;  // Thread-safe: used only for SELECTION algotype

    /**
     * Create a GenericCell with the specified value and algotype (default ascending direction).
     *
     * <p>PURPOSE: Backward-compatible constructor for existing code that doesn't use
     * cross-purpose sorting. Defaults to ASCENDING direction.</p>
     *
     * @param value the sort key value (typically 1 to N)
     * @param algotype the behavioral algotype (BUBBLE, INSERTION, or SELECTION)
     * @throws IllegalArgumentException if algotype is null
     */
    public GenericCell(int value, Algotype algotype) {
        this(value, algotype, SortDirection.ASCENDING);
    }

    /**
     * Create a GenericCell with the specified value, algotype, and sort direction.
     *
     * <p>PURPOSE: Primary constructor for cross-purpose sorting experiments where cells
     * can have different sort directions (ascending vs descending).</p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>value - Sort key value for comparison (immutable)</li>
     *   <li>algotype - Behavioral policy (BUBBLE, INSERTION, or SELECTION)</li>
     *   <li>sortDirection - Direction preference (ASCENDING or DESCENDING)</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Validate algotype is not null (throw IllegalArgumentException if null)</li>
     *   <li>Validate sortDirection is not null (throw IllegalArgumentException if null)</li>
     *   <li>Store value as immutable field</li>
     *   <li>Store algotype as immutable field</li>
     *   <li>Store sortDirection as immutable field</li>
     *   <li>Initialize idealPos to 0 for SELECTION algotype compatibility</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Fully initialized GenericCell instance</p>
     *
     * <p>GROUND TRUTH REFERENCE: cell_research/MultiThreadCell.py:
     * <pre>
     * def __init__(self, ..., reverse_direction=False):
     *     self.reverse_direction = reverse_direction
     * </pre>
     * </p>
     *
     * @param value the sort key value (typically 1 to N)
     * @param algotype the behavioral algotype (BUBBLE, INSERTION, or SELECTION)
     * @param sortDirection the sort direction (ASCENDING or DESCENDING)
     * @throws IllegalArgumentException if algotype or sortDirection is null
     */
    public GenericCell(int value, Algotype algotype, SortDirection sortDirection) {
        if (algotype == null) {
            throw new IllegalArgumentException("Algotype cannot be null");
        }
        if (sortDirection == null) {
            throw new IllegalArgumentException("SortDirection cannot be null");
        }
        this.value = value;
        this.algotype = algotype;
        this.sortDirection = sortDirection;
        this.idealPos = new AtomicInteger(0);  // Levin: initial ideal position is leftmost (0)
    }

    /**
     * Get the sort key value of this cell.
     *
     * @return the cell's value
     */
    public int getValue() {
        return value;
    }

    @Override
    public Algotype getAlgotype() {
        return algotype;
    }

    /**
     * Get the ideal position for SELECTION algotype cells.
     * Thread-safe for parallel execution.
     *
     * @return the current ideal position (0-based index)
     */
    public int getIdealPos() {
        return idealPos.get();
    }

    /**
     * Atomically increment the ideal position and return the new value.
     * Used by SELECTION algotype when swap is denied.
     * Thread-safe for parallel execution.
     *
     * @return the new ideal position after increment
     */
    public int incrementIdealPos() {
        return idealPos.incrementAndGet();
    }

    /**
     * Set the ideal position to a specific value.
     * Used for SELECTION algotype state management.
     *
     * @param newIdealPos the new ideal position (0-based index)
     */
    public void setIdealPos(int newIdealPos) {
        this.idealPos.set(newIdealPos);
    }

    /**
     * Atomically compare-and-set the ideal position.
     * Useful for concurrent updates when exact coordination is needed.
     *
     * @param expected the expected current value
     * @param newValue the new value to set
     * @return true if successful (current value matched expected)
     */
    public boolean compareAndSetIdealPos(int expected, int newValue) {
        return idealPos.compareAndSet(expected, newValue);
    }

    /**
     * Compare this cell to another based on their values.
     *
     * @param other the cell to compare against
     * @return negative if this < other, zero if equal, positive if this > other
     */
    @Override
    public int compareTo(GenericCell other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GenericCell that = (GenericCell) obj;
        return value == that.value && algotype == that.algotype;
    }

    @Override
    public int hashCode() {
        return 31 * value + algotype.hashCode();
    }

    @Override
    public String toString() {
        return String.format("GenericCell{value=%d, algotype=%s}", value, algotype.name());
    }

    /**
     * Get the sort direction preference of this cell.
     *
     * <p>PURPOSE: Implements HasSortDirection interface to support cross-purpose sorting
     * where cells in the same array can sort in opposite directions.</p>
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
     * <p>DEPENDENCIES: sortDirection field must be set during construction</p>
     *
     * <p>ARCHITECTURE NOTE: This method is called frequently by execution engines
     * during swap evaluation, so it simply returns a stored field with no computation.</p>
     *
     * <p>GROUND TRUTH REFERENCE: cell_research/MultiThreadCell.py:
     * <pre>
     * # Accessing reverse_direction field
     * if self.reverse_direction:
     *     # Descending sort logic
     * else:
     *     # Ascending sort logic
     * </pre>
     * </p>
     *
     * @return the sort direction of this cell (ASCENDING or DESCENDING)
     */
    @Override
    public SortDirection getSortDirection() {
        return this.sortDirection;
    }
}
