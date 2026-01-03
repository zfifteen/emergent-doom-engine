package com.emergent.doom.cell;

public interface HasValue {
    int getValue();

    /**
     * Returns the sortable value of the cell as a Comparable.
     * Replaces getValue() (int) to prevent truncation of large moduli.
     */
    default Comparable<?> getComparableValue() {
        return getValue();
    }
}