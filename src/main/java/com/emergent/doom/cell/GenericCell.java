package com.emergent.doom.cell;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A flexible cell implementation that can represent any algotype.
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
 * }</pre></p>
 */
public class GenericCell implements Cell<GenericCell>, HasIdealPosition {

    private final int value;
    private final Algotype algotype;
    private final AtomicInteger idealPos;  // Thread-safe: used only for SELECTION algotype

    /**
     * Create a GenericCell with the specified value and algotype.
     *
     * @param value the sort key value (typically 1 to N)
     * @param algotype the behavioral algotype (BUBBLE, INSERTION, or SELECTION)
     * @throws IllegalArgumentException if algotype is null
     */
    public GenericCell(int value, Algotype algotype) {
        if (algotype == null) {
            throw new IllegalArgumentException("Algotype cannot be null");
        }
        this.value = value;
        this.algotype = algotype;
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
}
