package com.emergent.doom.cell;

/**
 * Abstract base for Levin cell-view Selection Sort cells.
 * Baked-in: Algotype.SELECTION (target chasing, long-range).
 * Domain extends and impls compareTo.
 * Note: Contains mutable idealPos state (starts at 0, increments on swap denial per Levin p.9).
 * This breaks cell immutability but is required for Levin's dynamic position chasing.
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
public abstract class SelectionCell<T extends SelectionCell<T>> implements Cell<T> {
    protected final int value;
    private int idealPos;  // Mutable: starts at 0, increments on swap denial per Levin p.9

    protected SelectionCell(int value) {
        this.value = value;
        this.idealPos = 0;  // Levin: initial ideal position is most left (0)
    }

    public int getValue() {
        return value;
    }

    public int getIdealPos() {
        return idealPos;
    }

    public void incrementIdealPos() {
        idealPos++;
    }

    public void setIdealPos(int idealPos) {
        this.idealPos = idealPos;
    }

    @Override
    public Algotype getAlgotype() {
        return Algotype.SELECTION;  // Baked-in
    }

    @Override
    public abstract int compareTo(T other);
}