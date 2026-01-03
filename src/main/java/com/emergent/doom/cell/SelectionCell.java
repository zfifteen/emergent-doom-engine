package com.emergent.doom.cell;

/**
 * Abstract base for Levin cell-view Selection Sort cells.
 * Baked-in: Algotype.SELECTION (target chasing, incremental convergence).
 * Domain extends and implements compareTo.
 *
 * <p>Note: Contains mutable idealPos state (starts at 0 for ascending, or rightBoundary
 * for descending). Increments on swap denial per Levin p.9. This breaks cell immutability
 * but is required for Levin's dynamic position chasing.</p>
 *
 * <p><strong>Thread Safety:</strong> The idealPos field uses {@link java.util.concurrent.atomic.AtomicInteger}
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
public abstract class SelectionCell<T extends SelectionCell<T>> implements Cell<T> {
    protected final int value;

    protected SelectionCell(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }

    public abstract int compareTo(T other);
}
