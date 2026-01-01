package com.emergent.doom.cell;

/**
 * Abstract base for Levin cell-view Insertion Sort cells.
 * Baked-in: Algotype.INSERTION (prefix left view, left swaps).
 * Domain extends and implements compareTo.
 *
 * <p>PURPOSE: Provide Insertion policy foundation (prefix conservative).</p>
 * <p>INPUTS: value (int) - fixed sort key, sortDirection - ascending or descending.</p>
 * <p>PROCESS:</p>
 * <ol>
 *   <li>Construct with value and optional sort direction.</li>
 *   <li>Return fixed INSERTION algotype.</li>
 *   <li>Domain implements compareTo.</li>
 * </ol>
 * <p>OUTPUTS: Cell with INSERTION policy.</p>
 * <p>DEPENDENCIES: Cell interface, HasSortDirection interface.</p>
 *
 * <p>GROUND TRUTH REFERENCE: cell_research/InsertionSortCell.py:59-61</p>
 * <pre>
 * if self.reverse_direction:
 *     return self.value > self.cells[int(target_position[0])].value
 * return self.value < self.cells[int(target_position[0])].value
 * </pre>
 */
public abstract class InsertionCell<T extends InsertionCell<T>> implements Cell<T>, HasSortDirection {
    protected final int value;
    protected final SortDirection sortDirection;  // Sort direction for cross-purpose experiments

    /**
     * Construct an InsertionCell with the specified value and default ASCENDING direction.
     *
     * @param value the sort key value
     */
    protected InsertionCell(int value) {
        this(value, SortDirection.ASCENDING);
    }

    /**
     * Construct an InsertionCell with the specified value and sort direction.
     *
     * <p>This constructor enables cross-purpose sorting experiments where
     * cells in the same array can sort in opposite directions.</p>
     *
     * @param value the sort key value
     * @param sortDirection the sort direction (ASCENDING or DESCENDING)
     */
    protected InsertionCell(int value, SortDirection sortDirection) {
        this.value = value;
        this.sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Algotype getAlgotype() {
        return Algotype.INSERTION;  // Baked-in
    }

    @Override
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    @Override
    public abstract int compareTo(T other);
}