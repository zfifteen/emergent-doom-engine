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
public abstract class InsertionCell<T extends InsertionCell<T>> implements Cell<T> {
    protected final int value;

    protected InsertionCell(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public abstract int compareTo(T other);
}
