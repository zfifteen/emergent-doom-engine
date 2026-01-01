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
public abstract class BubbleCell<T extends BubbleCell<T>> implements Cell<T>, HasSortDirection {
    protected final int value;  // Fixed Levin value (1-N or domain)
    protected final SortDirection sortDirection;  // Sort direction for cross-purpose experiments

    /**
     * Construct a BubbleCell with the specified value and default ASCENDING direction.
     *
     * @param value the sort key value
     */
    protected BubbleCell(int value) {
        this(value, SortDirection.ASCENDING);
    }

    /**
     * Construct a BubbleCell with the specified value and sort direction.
     *
     * <p>This constructor enables cross-purpose sorting experiments where
     * cells in the same array can sort in opposite directions.</p>
     *
     * @param value the sort key value
     * @param sortDirection the sort direction (ASCENDING or DESCENDING)
     */
    protected BubbleCell(int value, SortDirection sortDirection) {
        this.value = value;
        this.sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Algotype getAlgotype() {
        return Algotype.BUBBLE;  // Baked-in concrete impl
    }

    @Override
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    // Abstract: Domain provides value comparison
    @Override
    public abstract int compareTo(T other);
}