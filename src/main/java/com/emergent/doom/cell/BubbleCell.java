package com.emergent.doom.cell;

/**
 * Abstract base for Levin cell-view Bubble Sort cells.
 * Baked-in: Algotype.BUBBLE (local adj views/swaps).
 * Domain extends and implements compareTo (value-based).
 *
 * <p>PURPOSE: Provide Bubble policy foundation (bidirectional local).</p>
 * <p>INPUTS: value (int) - fixed sort key, sortDirection - ascending or descending.</p>
 * <p>PROCESS:</p>
 * <ol>
 *   <li>Construct with value and optional sort direction.</li>
 *   <li>Return fixed BUBBLE algotype.</li>
 *   <li>Domain implements compareTo using value.</li>
 * </ol>
 * <p>OUTPUTS: Cell with BUBBLE policy.</p>
 * <p>DEPENDENCIES: Cell interface, HasSortDirection interface.</p>
 *
 * <p>GROUND TRUTH REFERENCE: cell_research/BubbleSortCell.py:54-56</p>
 * <pre>
 * if self.reverse_direction:
 *     return self.value < target if check_right else self.value > target
 * return self.value > target if check_right else self.value < target
 * </pre>
 */
public abstract class BubbleCell<T extends BubbleCell<T>> implements Cell<T> {
    protected final int value;

    protected BubbleCell(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public abstract int compareTo(T other);
}
