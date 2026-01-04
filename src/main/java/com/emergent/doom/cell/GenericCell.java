package com.emergent.doom.cell;

/**
 * Lightweight cell implementation wrapping an integer value.
 *
 * <p>GenericCell is a pure Comparable data carrier with zero engine-specific state.
 * All sorting metadata (algotype, sort direction, ideal position) is managed externally
 * by execution engines via CellMetadata arrays.</p>
 *
 * <p>Usage:
 * <pre>{@code
 * // Create cells with domain values only
 * GenericCell[] cells = new GenericCell[100];
 * for (int i = 0; i < 100; i++) {
 *     cells[i] = new GenericCell(randomValue());
 * }
 * 
 * // Metadata is provided via engine constructor
 * IntFunction<CellMetadata> provider = index -> 
 *     new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);
 * 
 * ParallelExecutionEngine<GenericCell> engine = 
 *     new ParallelExecutionEngine<>(cells, swapEngine, probe, detector, provider);
 * }</pre></p>
 */
public class GenericCell implements Cell<GenericCell> {

    private final int value;

    /**
     * Create a GenericCell wrapping the specified integer value.
     *
     * <p>PURPOSE: Construct a lightweight cell carrying only domain data.
     * All execution metadata (algotype, sort direction, ideal position) is managed
     * externally by execution engines via CellMetadata.</p>
     *
     * <p>INPUTS: value - The integer value to wrap (immutable)</p>
     *
     * <p>OUTPUTS: Fully initialized GenericCell instance</p>
     *
     * @param value the sort key value
     */
    public GenericCell(int value) {
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
     * Compare this cell to another based on their values.
     *
     * <p>PURPOSE: Implement Comparable contract for sorting. This is the only
     * method required by the execution engine for ordering cells.</p>
     *
     * <p>INPUTS: other - Another GenericCell to compare against</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Extract value from this cell</li>
     *   <li>Extract value from other cell</li>
     *   <li>Return Integer.compare(this.value, other.value)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Negative if this < other, zero if equal, positive if this > other</p>
     *
     * @param other the cell to compare against
     * @return negative if this < other, zero if equal, positive if this > other
     */
    @Override
    public int compareTo(GenericCell other) {
        return Integer.compare(this.value, other.value);
    }

    /**
     * Check equality based solely on value.
     *
     * <p>PURPOSE: Support collections and proper equality semantics.
     * GenericCells are equal if they wrap the same value.</p>
     *
     * <p>INPUTS: obj - Object to compare for equality</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Check reference equality first</li>
     *   <li>Check if obj is instance of GenericCell</li>
     *   <li>Compare values for equality</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: true if equal, false otherwise</p>
     *
     * @param obj the object to compare
     * @return true if objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GenericCell)) return false;
        GenericCell that = (GenericCell) obj;
        return value == that.value;
    }

    /**
     * Compute hash code based solely on value.
     *
     * <p>PURPOSE: Support hash-based collections (HashMap, HashSet).
     * Consistent with equals() - equal cells have equal hash codes.</p>
     *
     * <p>INPUTS: None</p>
     *
     * <p>OUTPUTS: Hash code derived from value</p>
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    /**
     * Convert cell to string representation.
     *
     * <p>PURPOSE: Support debugging and logging. Shows only the wrapped value.</p>
     *
     * <p>INPUTS: None</p>
     *
     * <p>OUTPUTS: String representation of value</p>
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}