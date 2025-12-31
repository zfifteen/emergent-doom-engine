package com.emergent.doom.cell;

/**
 * Interface for cells that support configurable sort direction (ascending or descending).
 * 
 * <p>PURPOSE: Enables cross-purpose sorting experiments where cells in the same array
 * can sort in opposite directions, creating competitive dynamics and equilibrium states.</p>
 * 
 * <p>From Levin et al. (2024), p.14:</p>
 * <blockquote>
 * "we performed experiments using two mixed Algotypes, where one was made to sort in
 * *decreasing* order while the other sorted in *increasing* order."
 * </blockquote>
 * 
 * <p>ARCHITECTURE ROLE: This interface extends the Cell contract to support per-cell
 * directionality. It allows execution engines to query each cell's preferred sort
 * direction and adjust swap logic accordingly, enabling competitive scenarios.</p>
 * 
 * <p>IMPLEMENTATION FLOW:
 * <ol>
 *   <li>Cell implements HasSortDirection and stores a SortDirection field</li>
 *   <li>Execution engine calls getSortDirection() before evaluating swaps</li>
 *   <li>Engine uses direction to determine comparison polarity (< vs >)</li>
 *   <li>Cells with different directions create opposing forces in the array</li>
 *   <li>System reaches equilibrium when no more beneficial swaps exist</li>
 * </ol>
 * </p>
 * 
 * <p>EXPECTED INPUTS: None (getter interface)</p>
 * <p>EXPECTED OUTPUTS: SortDirection indicating cell's sorting preference</p>
 * <p>DATA FLOW: Cell state → getSortDirection() → Engine swap logic → Direction-aware comparison</p>
 * 
 * <p>GROUND TRUTH REFERENCE: cell_research/MultiThreadCell.py:
 * <pre>
 * class MultiThreadCell:
 *     reverse_direction: bool  # True = descending, False = ascending
 *     
 *     def __init__(self, ..., reverse_direction=False):
 *         self.reverse_direction = reverse_direction
 * </pre>
 * </p>
 * 
 * <p>USAGE EXAMPLE (conceptual):
 * <pre>{@code
 * // Create cells with opposing directions
 * GenericCell ascending = new GenericCell(42, Algotype.BUBBLE, SortDirection.ASCENDING);
 * GenericCell descending = new GenericCell(99, Algotype.SELECTION, SortDirection.DESCENDING);
 * 
 * // In execution engine:
 * SortDirection direction = cell.getSortDirection();
 * boolean shouldSwap = direction.isAscending() 
 *     ? (cell.compareTo(neighbor) < 0)  // Move left if smaller
 *     : (cell.compareTo(neighbor) > 0); // Move left if larger
 * }</pre>
 * </p>
 * 
 * @see SortDirection
 * @see GenericCell
 */
public interface HasSortDirection {
    
    /**
     * Get the sort direction preference of this cell.
     * 
     * <p>PURPOSE: Provides the cell's sorting policy to execution engines for
     * direction-aware swap evaluation.</p>
     * 
     * <p>INPUTS: None (getter method)</p>
     * 
     * <p>PROCESS:
     * <ol>
     *   <li>Retrieve the SortDirection field from cell's internal state</li>
     *   <li>Return immutable enum value (ASCENDING or DESCENDING)</li>
     *   <li>Must be thread-safe for concurrent execution engines</li>
     *   <li>Must be deterministic (same cell always returns same direction)</li>
     * </ol>
     * </p>
     * 
     * <p>OUTPUTS: SortDirection enum - ASCENDING or DESCENDING</p>
     * 
     * <p>DEPENDENCIES: Cell must maintain a SortDirection field set during construction</p>
     * 
     * <p>ARCHITECTURE NOTE: This method is called frequently during execution,
     * so implementations should avoid computation and return a stored field.</p>
     * 
     * <p>CROSS-REFERENCE: Corresponds to checking reverse_direction field in
     * cell_research Python implementation (MultiThreadCell.py:reverse_direction)</p>
     * 
     * @return the sort direction of this cell (never null)
     */
    // UNIMPLEMENTED: To be implemented by GenericCell in Phase Two/Three
    SortDirection getSortDirection();
}
