package com.emergent.doom.cell;

/**
 * Abstract base for Levin cell-view Bubble Sort cells.
 * Baked-in: Algotype.BUBBLE (local adj views/swaps).
 * Domain extends and impls compareTo (value-based).
 * TEMPLATE_BEGIN
 * PURPOSE: Provide Bubble policy foundation (bidirectional local).
 * INPUTS: value (int) - fixed sort key.
 * PROCESS:
 *   STEP[1]: Construct with value.
 *   STEP[2]: Return fixed BUBBLE algotype.
 *   STEP[3]: Domain impls compareTo using value.
 * OUTPUTS: Cell<T> with BUBBLE policy.
 * DEPENDENCIES: Cell interface.
 * TEMPLATE_END
 */
public abstract class BubbleCell<T extends BubbleCell<T>> implements Cell<T> {
    protected final int value;  // Fixed Levin value (1-N or domain)

    protected BubbleCell(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Algotype getAlgotype() {
        return Algotype.BUBBLE;  // Baked-in concrete impl
    }

    // Abstract: Domain provides value comparison
    @Override
    public abstract int compareTo(T other);
}