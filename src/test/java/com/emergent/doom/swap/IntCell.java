package com.emergent.doom.swap;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;

/**
 * Simple integer-based cell for testing purposes.
 */
public class IntCell implements Cell<IntCell> {

    private final int value;

    public IntCell(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Algotype getAlgotype() {
        return Algotype.BUBBLE;
    }

    @Override
    public int compareTo(IntCell other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "IntCell(" + value + ")";
    }
}
