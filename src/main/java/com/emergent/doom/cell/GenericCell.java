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
    private final AtomicInteger idealPos = new AtomicInteger(0);  // For SELECTION compatibility; initialized to 0 per Levin

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
        // idealPos initialized to 0 for SELECTION compatibility (Levin p.9)
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
    public int getIdealPos() {
        if (algotype != Algotype.SELECTION) {
            throw new IllegalStateException("idealPos is only supported for SELECTION algotype; current: " + algotype);
        }
        return idealPos.get();
    }

    @Override
    public void setIdealPos(int newIdealPos) {
        if (algotype != Algotype.SELECTION) {
            throw new IllegalStateException("idealPos is only supported for SELECTION algotype; current: " + algotype);
        }
        if (newIdealPos < 0) {
            throw new IllegalArgumentException("Ideal position cannot be negative: " + newIdealPos);
        }
        idealPos.set(newIdealPos);
    }

    @Override
    public int incrementIdealPos() {
        if (algotype != Algotype.SELECTION) {
            throw new IllegalStateException("idealPos is only supported for SELECTION algotype; current: " + algotype);
        }
        return idealPos.incrementAndGet();
    }

    @Override
    public boolean compareAndSetIdealPos(int expected, int newValue) {
        if (algotype != Algotype.SELECTION) {
            throw new IllegalStateException("idealPos is only supported for SELECTION algotype; current: " + algotype);
        }
        if (newValue < 0) {
            throw new IllegalArgumentException("Ideal position cannot be negative: " + newValue);
        }
        return idealPos.compareAndSet(expected, newValue);
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
