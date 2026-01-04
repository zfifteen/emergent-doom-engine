package com.emergent.doom.cell;

/**
 * Lightweight base class for Selection Sort cells.
 * 
 * <p>SelectionCell is a pure Comparable data carrier. All Selection algotype behavior
 * (ideal position tracking, target chasing) is managed externally by execution engines
 * via CellMetadata arrays.</p>
 *
 * <p>Domain subclasses implement only compareTo() for value-based comparison.</p>
 */
public abstract class SelectionCell<T extends SelectionCell<T>> implements Cell<T> {
    protected final int value;

    /**
     * Construct a SelectionCell wrapping the specified integer value.
     *
     * <p>PURPOSE: Construct a lightweight cell carrying only domain data.
     * All Selection-specific metadata (ideal position, sort direction) is managed
     * externally by execution engines via CellMetadata.</p>
     *
     * <p>INPUTS: value - The integer value to wrap (immutable)</p>
     *
     * <p>OUTPUTS: Fully initialized SelectionCell instance</p>
     *
     * @param value the sort key value
     */
    protected SelectionCell(int value) {
        this.value = value;
    }

    /**
     * Get the wrapped integer value.
     *
     * <p>PURPOSE: Provide access to the domain value for metrics and logging.</p>
     *
     * <p>INPUTS: None</p>
     *
     * <p>OUTPUTS: The wrapped integer value</p>
     *
     * @return the cell's value
     */
    public int getValue() {
        return value;
    }

    /**
     * Compare this cell to another for ordering.
     *
     * <p>PURPOSE: Abstract method for domain-specific comparison logic.
     * Subclasses must implement to define how cells are ordered.</p>
     *
     * <p>INPUTS: other - Another SelectionCell to compare against</p>
     *
     * <p>OUTPUTS: Negative if this < other, zero if equal, positive if this > other</p>
     *
     * @param other the cell to compare against
     * @return comparison result
     */
    public abstract int compareTo(T other);
}
