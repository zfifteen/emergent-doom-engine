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
 * the cell's target position, matching the behavior of SelectionCell. For ascending sort,
 * this field starts at 0 (leftBoundary) and increments when swap attempts are denied.
 * For descending sort, it should be initialized to rightBoundary via
 * {@link HasIdealPosition#updateForBoundary(int, int, boolean)}, per Levin p.9.</p>
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
public class GenericCell implements Cell<GenericCell>, HasAlgotype, HasSortDirection, HasIdealPosition {
    private final int value;
    private final Algotype algotype;
    private final SortDirection sortDirection;

    /**
     * Only used for SELECTION cells. Kept thread-safe for parallel execution.
     */
    private final AtomicInteger idealPos;

    public GenericCell(int value) {
        this(value, Algotype.BUBBLE, SortDirection.ASCENDING);
    }

    public GenericCell(int value, Algotype algotype) {
        this(value, algotype, SortDirection.ASCENDING);
    }

    public GenericCell(int value, Algotype algotype, SortDirection sortDirection) {
        this.value = value;
        this.algotype = (algotype == null) ? Algotype.BUBBLE : algotype;
        this.sortDirection = (sortDirection == null) ? SortDirection.ASCENDING : sortDirection;
        this.idealPos = new AtomicInteger(0);
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public Algotype getAlgotype() {
        return algotype;
    }

    @Override
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    @Override
    public int getIdealPos() {
        return idealPos.get();
    }

    @Override
    public void setIdealPos(int newIdealPos) {
        idealPos.set(newIdealPos);
    }

    @Override
    public int incrementIdealPos() {
        return idealPos.incrementAndGet();
    }

    @Override
    public boolean compareAndSetIdealPos(int expected, int newValue) {
        return idealPos.compareAndSet(expected, newValue);
    }

    @Override
    public int compareTo(GenericCell other) {
        // Comparison is value-based; direction is handled in engine swap logic.
        return Integer.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GenericCell that = (GenericCell) obj;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return String.format(
                "GenericCell{value=%d, algotype=%s, direction=%s, idealPos=%d}",
                value,
                algotype,
                sortDirection,
                idealPos.get()
        );
    }
}
