package com.emergent.doom.cell;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base for Levin cell-view Selection Sort cells.
 * Baked-in: Algotype.SELECTION (target chasing, long-range).
 * Domain extends and impls compareTo.
 * Note: Contains mutable idealPos state (starts at 0, increments on swap denial per Levin p.9).
 * This breaks cell immutability but is required for Levin's dynamic position chasing.
 *
 * <p><strong>Thread Safety:</strong> The idealPos field uses {@link AtomicInteger}
 * for thread-safe access in parallel execution mode.</p>
 *
 * TEMPLATE_BEGIN
 * PURPOSE: Provide Selection policy foundation (goal-directed).
 * INPUTS: value (int).
 * PROCESS:
 *   STEP[1]: Construct with value.
 *   STEP[2]: Return fixed SELECTION algotype.
 *   STEP[3]: Domain impls compareTo.
 * OUTPUTS: Cell<T> with SELECTION policy.
 * DEPENDENCIES: Cell interface; internal idealPos state and accessors.
 * TEMPLATE_END
 */
public abstract class SelectionCell<T extends SelectionCell<T>> implements Cell<T>, HasIdealPosition {
    protected final int value;
    private final AtomicInteger idealPos;  // Thread-safe: starts at 0, increments on swap denial per Levin p.9

    protected SelectionCell(int value) {
        this.value = value;
        this.idealPos = new AtomicInteger(0);  // Levin: initial ideal position is most left (0)
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
    public abstract int compareTo(T other);

    // HasIdealPosition already implemented via existing methods; no additional stubs needed
    // Existing: getIdealPos(), incrementIdealPos(), setIdealPos(), compareAndSetIdealPos()
}