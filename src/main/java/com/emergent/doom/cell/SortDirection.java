package com.emergent.doom.cell;

/**
 * Enumeration representing the sort direction for cells in cross-purpose sorting experiments.
 * 
 * <p>PURPOSE: Enables chimeric populations where different cells sort in opposite directions,
 * creating competitive dynamics as described in Levin et al. (2024), p.14:</p>
 * 
 * <blockquote>
 * "we performed experiments using two mixed Algotypes, where one was made to sort in
 * *decreasing* order while the other sorted in *increasing* order."
 * </blockquote>
 * 
 * <p>ARCHITECTURE ROLE: This enum is a fundamental property of cells that influences
 * how they compare values and make swap decisions. When combined with different algotypes,
 * it allows studying equilibrium states where cells with conflicting goals interact.</p>
 * 
 * <p>IMPLEMENTATION FLOW:
 * <ol>
 *   <li>Cell is created with a SortDirection (ASCENDING or DESCENDING)</li>
 *   <li>During swap evaluation, the cell's direction determines comparison logic</li>
 *   <li>ASCENDING cells prefer smaller values to their left, larger to their right</li>
 *   <li>DESCENDING cells prefer larger values to their left, smaller to their right</li>
 *   <li>In cross-purpose scenarios, cells reach equilibrium when opposing forces balance</li>
 * </ol>
 * </p>
 * 
 * <p>EXPECTED INPUTS: None (enum constants)</p>
 * <p>EXPECTED OUTPUTS: Direction policy for swap decisions</p>
 * <p>DATA FLOW: Cell construction → Swap evaluation → Direction-aware comparison</p>
 * 
 * <p>GROUND TRUTH REFERENCE: cell_research/MultiThreadCell.py:
 * <pre>
 * class MultiThreadCell:
 *     reverse_direction: bool  # True = descending, False = ascending
 * </pre>
 * </p>
 * 
 * @see HasSortDirection
 */
public enum SortDirection {
    /**
     * Ascending sort direction (smallest to largest, left to right).
     * 
     * <p>PURPOSE: Default sort direction matching natural ordering.</p>
     * <p>BEHAVIOR: Cell prefers to move left if its value is smaller than its neighbor,
     * or right if larger, ultimately placing smallest values at left end.</p>
     * <p>CORRESPONDS TO: reverse_direction = false in cell_research Python code</p>
     */
    ASCENDING,
    
    /**
     * Descending sort direction (largest to smallest, left to right).
     * 
     * <p>PURPOSE: Reverse sort direction for cross-purpose experiments.</p>
     * <p>BEHAVIOR: Cell prefers to move left if its value is larger than its neighbor,
     * or right if smaller, ultimately placing largest values at left end.</p>
     * <p>CORRESPONDS TO: reverse_direction = true in cell_research Python code</p>
     */
    DESCENDING;
    
    /**
     * Check if this direction is ascending.
     * 
     * <p>PURPOSE: Convenience method for direction-aware comparison logic.</p>
     * <p>INPUTS: None (instance method)</p>
     * <p>OUTPUTS: true if ASCENDING, false if DESCENDING</p>
     * <p>USAGE: Simplifies conditional logic in swap evaluation methods</p>
     * 
     * @return true if this direction is ASCENDING, false otherwise
     */
    public boolean isAscending() {
        return this == ASCENDING;
    }
    
    /**
     * Check if this direction is descending.
     * 
     * <p>PURPOSE: Convenience method for direction-aware comparison logic.</p>
     * <p>INPUTS: None (instance method)</p>
     * <p>OUTPUTS: true if DESCENDING, false if ASCENDING</p>
     * <p>USAGE: Simplifies conditional logic in swap evaluation methods</p>
     * 
     * @return true if this direction is DESCENDING, false otherwise
     */
    public boolean isDescending() {
        return this == DESCENDING;
    }
}
