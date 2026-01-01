package com.emergent.doom.cell;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base for Levin cell-view Selection Sort cells.
 * Baked-in: Algotype.SELECTION (target chasing, incremental convergence).
 * Domain extends and implements compareTo.
 *
 * <p>Note: Contains mutable idealPos state (starts at 0 for ascending, or rightBoundary
 * for descending). Increments on swap denial per Levin p.9. This breaks cell immutability
 * but is required for Levin's dynamic position chasing.</p>
 *
 * <p><strong>Thread Safety:</strong> The idealPos field uses {@link AtomicInteger}
 * for thread-safe access in parallel execution mode.</p>
 *
 * <p>PURPOSE: Provide Selection policy foundation (goal-directed).</p>
 * <p>INPUTS: value (int) - fixed sort key, sortDirection - ascending or descending.</p>
 * <p>PROCESS:</p>
 * <ol>
 *   <li>Construct with value and optional sort direction.</li>
 *   <li>Return fixed SELECTION algotype.</li>
 *   <li>Domain implements compareTo.</li>
 * </ol>
 * <p>OUTPUTS: Cell with SELECTION policy.</p>
 * <p>DEPENDENCIES: Cell interface, HasIdealPosition, HasSortDirection interfaces.</p>
 *
 * <p>GROUND TRUTH REFERENCE: cell_research/SelectionSortCell.py:11-14</p>
 * <pre>
 * if self.reverse_direction:
 *     self.ideal_position = right_boundary
 * else:
 *     self.ideal_position = left_boundary
 * </pre>
 */
public abstract class SelectionCell<T extends SelectionCell<T>> implements Cell<T>, HasIdealPosition, HasSortDirection {
    protected final int value;
    protected final SortDirection sortDirection;  // Sort direction for cross-purpose experiments
    private final AtomicInteger idealPos;  // Thread-safe: starts at 0 for ascending, increments on swap denial

    /**
     * Construct a SelectionCell with the specified value and default ASCENDING direction.
     *
     * @param value the sort key value
     */
    protected SelectionCell(int value) {
        this(value, SortDirection.ASCENDING);
    }

    /**
     * Construct a SelectionCell with the specified value and sort direction.
     *
     * <p>Note: The idealPos is initialized to 0. For descending sort, call
     * {@link #updateForBoundary(int, int, boolean)} to set it to rightBoundary.</p>
     *
     * @param value the sort key value
     * @param sortDirection the sort direction (ASCENDING or DESCENDING)
     */
    protected SelectionCell(int value, SortDirection sortDirection) {
        this.value = value;
        this.sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
        this.idealPos = new AtomicInteger(0);  // Levin: initial ideal position is most left (0) for ascending
    }

    public int getValue() {
        return value;
    }

    public int getIdealPos() {
        return idealPos.get();
    }

    /**
     * Atomically increment the ideal position and return the new value.
     * Thread-safe for parallel execution.
     *
     * @return the new ideal position after increment
     */
    public int incrementIdealPos() {
        return idealPos.incrementAndGet();
    }

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

    @Override
    public Algotype getAlgotype() {
        return Algotype.SELECTION;  // Baked-in
    }

    @Override
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    @Override
    public abstract int compareTo(T other);

    // HasIdealPosition already implemented via existing methods; no additional stubs needed
    // Existing: getIdealPos(), incrementIdealPos(), setIdealPos(), compareAndSetIdealPos()
}