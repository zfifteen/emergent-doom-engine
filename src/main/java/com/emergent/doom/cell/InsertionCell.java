package com.emergent.doom.cell;

/**
 * Abstract base for Levin cell-view Insertion Sort cells.
 * Baked-in: Algotype.INSERTION (prefix left view, left swaps).
 * Domain extends and impls compareTo.
 * TEMPLATE_BEGIN
 * PURPOSE: Provide Insertion policy foundation (prefix conservative).
 * INPUTS: value (int).
 * PROCESS:
 *   STEP[1]: Construct with value.
 *   STEP[2]: Return fixed INSERTION algotype.
 *   STEP[3]: Domain impls compareTo.
 * OUTPUTS: Cell<T> with INSERTION policy.
 * DEPENDENCIES: Cell interface.
 * TEMPLATE_END
 */
public abstract class InsertionCell<T extends InsertionCell<T>> implements Cell<T> {
    protected final int value;

    protected InsertionCell(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Algotype getAlgotype() {
        return Algotype.INSERTION;  // Baked-in
    }

    @Override
    public abstract int compareTo(T other);
}