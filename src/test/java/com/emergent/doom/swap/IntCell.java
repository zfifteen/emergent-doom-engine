package com.emergent.doom.swap;

import com.emergent.doom.cell.Cell;

/**
 * Simple integer-based cell for testing purposes.
 * 
 * <p>IntCell is a lightweight test implementation of the Cell interface,
 * demonstrating the minimal contract for domain-agnostic sorting. This class
 * is used throughout the test suite to validate swap engine behavior, metadata
 * coordination, and execution engine functionality.</p>
 * 
 * <p>As a pure Comparable data carrier, IntCell contains zero engine-specific
 * state. All sorting metadata (algotype, sort direction, ideal position) is
 * managed externally by execution engines via CellMetadata arrays.</p>
 * 
 * <p><strong>Usage in Tests:</strong></p>
 * <ul>
 *   <li>SwapEngine tests - verifying swap mechanics and frozen cell handling</li>
 *   <li>ExecutionEngine tests - validating metadata coordination during swaps</li>
 *   <li>Topology tests - testing neighbor evaluation logic</li>
 * </ul>
 */
public class IntCell implements Cell<IntCell> {

    private final int value;

    /**
     * Create an IntCell wrapping the specified integer value.
     * 
     * <p>PURPOSE: Construct a lightweight test cell carrying only domain data.
     * All execution metadata is managed externally by test fixtures.</p>
     * 
     * @param value the integer value to wrap (immutable)
     */
    public IntCell(int value) {
        this.value = value;
    }

    /**
     * Get the wrapped integer value.
     * 
     * <p>PURPOSE: Provide access to the domain value for test assertions
     * and verification.</p>
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
     * @param other the cell to compare against
     * @return negative if this < other, zero if equal, positive if this > other
     */
    @Override
    public int compareTo(IntCell other) {
        return Integer.compare(this.value, other.value);
    }

    /**
     * Convert cell to string representation.
     * 
     * <p>PURPOSE: Support test debugging and assertion failure messages.
     * Format: "IntCell(value)"</p>
     * 
     * @return string representation in format "IntCell(value)"
     */
    @Override
    public String toString() {
        return "IntCell(" + value + ")";
    }
}
