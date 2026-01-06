package com.emergent.doom.cell;

/**
 * Cell implementation that carries its algotype as an intrinsic property.
 *
 * <p><strong>PURPOSE:</strong> Model cells where algotype is bound to the cell object itself,
 * not to its position in the array. When cells swap during sorting, their algotypes move WITH them,
 * enabling dynamic aggregation patterns that can change during sorting.</p>
 *
 * <p><strong>ARCHITECTURAL DIFFERENCE FROM GENERICCELL:</strong></p>
 * <ul>
 *   <li><strong>GenericCell:</strong> Algotype determined by position index (via AlgotypeProvider).
 *       When cells swap, algotypes stay at their positions. Result: constant aggregation.</li>
 *   <li><strong>AlgotypedCell:</strong> Algotype is a property of the cell object.
 *       When cells swap, algotypes move with them. Result: dynamic aggregation.</li>
 * </ul>
 *
 * <p><strong>USE CASE:</strong> Reproducing experimental results from Levin et al. (2024) reference
 * implementation (cell_research repository), where algotypes are object properties that travel
 * with cells during sorting, enabling mid-sorting clustering peaks.</p>
 *
 * <p><strong>EXAMPLE:</strong></p>
 * <pre>{@code
 * // Create cells with embedded algotypes
 * AlgotypedCell[] cells = new AlgotypedCell[100];
 * cells[0] = new AlgotypedCell(45, Algotype.BUBBLE);     // value=45, algotype=Bubble
 * cells[1] = new AlgotypedCell(23, Algotype.SELECTION);  // value=23, algotype=Selection
 * 
 * // After swap (by value), algotypes move WITH cells:
 * // Position 0: AlgotypedCell(23, SELECTION) ← cell object moved here
 * // Position 1: AlgotypedCell(45, BUBBLE)    ← cell object moved here
 * 
 * // Contrast with GenericCell + PercentageAlgotypeProvider:
 * // Position 0: GenericCell(23) with algotype lookup → still returns BUBBLE (position-based)
 * // Position 1: GenericCell(45) with algotype lookup → still returns SELECTION (position-based)
 * }</pre>
 *
 * <p><strong>METRICS COMPATIBILITY:</strong> Use with AlgotypeAggregationIndex by providing
 * snapshots where type information reflects cell algotypes (not position algotypes).</p>
 *
 * @see GenericCell
 * @see Algotype
 */
public class AlgotypedCell implements Cell<AlgotypedCell> {

    private final int value;
    private final Algotype algotype;

    /**
     * Create an AlgotypedCell with specified value and algotype.
     *
     * <p><strong>PURPOSE:</strong> Construct a cell where algotype is an intrinsic, immutable
     * property that travels with the cell during swaps.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>value - The numeric value for sorting (domain data)</li>
     *   <li>algotype - The sorting algorithm type (intrinsic metadata)</li>
     * </ul>
     *
     * <p><strong>OUTPUTS:</strong> Fully initialized AlgotypedCell instance</p>
     *
     * <p><strong>IMMUTABILITY:</strong> Both value and algotype are final and cannot change
     * after construction. Swapping occurs at the array level (cell objects move between indices).</p>
     *
     * @param value the sort key value
     * @param algotype the algorithm type for this cell
     * @throws IllegalArgumentException if algotype is null
     */
    public AlgotypedCell(int value, Algotype algotype) {
        if (algotype == null) {
            throw new IllegalArgumentException("Algotype cannot be null");
        }
        this.value = value;
        this.algotype = algotype;
    }

    /**
     * Get the wrapped integer value.
     *
     * <p><strong>PURPOSE:</strong> Provide access to the domain value for sorting and metrics.</p>
     *
     * @return the cell's value
     */
    public int getValue() {
        return value;
    }

    /**
     * Get the algotype of this cell.
     *
     * <p><strong>PURPOSE:</strong> Provide access to the intrinsic algotype property.
     * This algotype travels WITH the cell during swaps, enabling metrics to track
     * dynamic spatial aggregation patterns.</p>
     *
     * <p><strong>KEY DIFFERENCE:</strong> Unlike GenericCell where algotype is determined
     * by position lookup, AlgotypedCell's algotype is bound to the cell object itself.</p>
     *
     * @return the cell's algotype (never null)
     */
    public Algotype getAlgotype() {
        return algotype;
    }

    /**
     * Compare this cell to another based on their values.
     *
     * <p><strong>PURPOSE:</strong> Implement Comparable contract for sorting.
     * Comparison is based ONLY on value, NOT on algotype. This allows cells
     * of different algotypes to sort together correctly.</p>
     *
     * <p><strong>INPUTS:</strong> other - Another AlgotypedCell to compare against</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Extract value from this cell</li>
     *   <li>Extract value from other cell</li>
     *   <li>Return Integer.compare(this.value, other.value)</li>
     *   <li>Algotype is ignored in comparison</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Negative if this < other, zero if equal, positive if this > other</p>
     *
     * @param other the cell to compare against
     * @return negative if this < other, zero if equal, positive if this > other
     */
    @Override
    public int compareTo(AlgotypedCell other) {
        return Integer.compare(this.value, other.value);
    }

    /**
     * Check equality based on both value AND algotype.
     *
     * <p><strong>PURPOSE:</strong> Support collections and proper equality semantics.
     * AlgotypedCells are equal if they have the same value AND algotype.</p>
     *
     * <p><strong>DESIGN DECISION:</strong> Including algotype in equality distinguishes
     * AlgotypedCell from GenericCell. Two cells with same value but different algotypes
     * are considered different objects (which is correct for tracking cell identities).</p>
     *
     * @param obj the object to compare
     * @return true if both value and algotype are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AlgotypedCell)) return false;
        AlgotypedCell that = (AlgotypedCell) obj;
        return value == that.value && algotype == that.algotype;
    }

    /**
     * Compute hash code based on both value and algotype.
     *
     * <p><strong>PURPOSE:</strong> Support hash-based collections (HashMap, HashSet).
     * Consistent with equals() - equal cells have equal hash codes.</p>
     *
     * @return hash code derived from value and algotype
     */
    @Override
    public int hashCode() {
        int result = Integer.hashCode(value);
        result = 31 * result + algotype.hashCode();
        return result;
    }

    /**
     * Convert cell to string representation.
     *
     * <p><strong>PURPOSE:</strong> Support debugging and logging. Shows both value and algotype
     * to distinguish AlgotypedCell from GenericCell in logs.</p>
     *
     * <p><strong>FORMAT:</strong> "AlgotypedCell(value=23, algotype=BUBBLE)"</p>
     *
     * @return string representation including value and algotype
     */
    @Override
    public String toString() {
        return String.format("AlgotypedCell(value=%d, algotype=%s)", value, algotype);
    }
}
