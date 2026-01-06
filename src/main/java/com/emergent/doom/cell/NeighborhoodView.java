package com.emergent.doom.cell;

import java.util.List;
import java.util.Optional;

/**
 * Encapsulates visible neighbors for a cell during swap evaluation.
 *
 * <p><strong>PURPOSE:</strong> Provide cells with "what they can see" without exposing
 * raw array access. Preserves Levin's "local knowledge only" principle while supporting
 * diverse visibility models (adjacent, prefix, ideal target, Fibonacci distances).</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Encapsulation: Hides array access mechanics from cells</li>
 *   <li>Testability: Easy to mock for unit testing cell behavior</li>
 *   <li>Flexibility: Supports different visibility models per algotype</li>
 *   <li>Safety: Prevents cells from seeing beyond their allowed scope</li>
 * </ul>
 *
 * <p><strong>TYPE PARAMETERS:</strong></p>
 * <ul>
 *   <li>V - Value type (Integer for sorting, FactorCandidate for factorization, etc.)</li>
 *   <li>A - Algotype enum (SortingAlgotype, FactorizationAlgotype, etc.)</li>
 * </ul>
 *
 * <p><strong>EXPECTED INPUTS:</strong> Cell array, current position, algotype-specific visibility rules</p>
 * <p><strong>EXPECTED OUTPUTS:</strong> Methods returning visible neighbors and positions</p>
 * <p><strong>DATA FLOW:</strong> Engine builds view → Cell queries neighbors → Cell makes swap decision</p>
 *
 * @param <V> the value type (must be Comparable)
 * @param <A> the algotype enum type
 */
public class NeighborhoodView<V extends Comparable<V>, A extends Enum<A>> {
    
    // PURPOSE: The cell whose neighborhood is being viewed
    // INPUTS: Set during construction
    // PROCESS: Used as reference point for neighbor calculations
    // OUTPUTS: Available via getViewingCell() for context
    // DEPENDENCIES: Must be non-null
    private final AbstractCell<V, A> viewingCell;
    
    // PURPOSE: Current position of the viewing cell in array
    // INPUTS: Set during construction from cell.readCurrentPosition()
    // PROCESS: Used for relative position calculations
    // OUTPUTS: Available via getCurrentPosition()
    // DEPENDENCIES: Must match viewingCell's actual position
    private final int currentPosition;
    
    // PURPOSE: Total size of the cell array
    // INPUTS: Set during construction
    // PROCESS: Used for boundary checking
    // OUTPUTS: Available via getArraySize()
    // DEPENDENCIES: Must match actual array length
    private final int arraySize;
    
    // PURPOSE: Visible neighbor cells based on algotype rules
    // INPUTS: Set during construction from engine's visibility calculation
    // PROCESS: Filtered based on algotype (adjacent, prefix, target, Fibonacci)
    // OUTPUTS: Available via getVisibleNeighbors()
    // DEPENDENCIES: Must not be null (can be empty list)
    private final List<AbstractCell<V, A>> visibleNeighbors;
    
    // PURPOSE: Positions of visible neighbors in array
    // INPUTS: Set during construction, parallel to visibleNeighbors
    // PROCESS: Allows cells to propose swaps by position
    // OUTPUTS: Available via getNeighborPositions()
    // DEPENDENCIES: Must match length of visibleNeighbors
    private final List<Integer> neighborPositions;
    
    /**
     * Create a neighborhood view for a cell.
     *
     * <p><strong>PURPOSE:</strong> Construct view encapsulating what a cell can see
     * based on its algotype's visibility rules.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>viewingCell - the cell whose neighborhood is being viewed (required, non-null)</li>
     *   <li>currentPosition - current position in array (0 to arraySize-1)</li>
     *   <li>arraySize - total size of cell array (must be positive)</li>
     *   <li>visibleNeighbors - list of cells this cell can see (required, non-null, can be empty)</li>
     *   <li>neighborPositions - positions of visible neighbors (required, same length as visibleNeighbors)</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate all inputs are non-null</li>
     *   <li>Validate currentPosition is in bounds</li>
     *   <li>Validate visibleNeighbors and neighborPositions have same length</li>
     *   <li>Store all fields as immutable references</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Fully initialized NeighborhoodView</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @param viewingCell the cell whose neighborhood is being viewed
     * @param currentPosition current position in array
     * @param arraySize total size of cell array
     * @param visibleNeighbors list of cells this cell can see
     * @param neighborPositions positions of visible neighbors
     * @throws NullPointerException if any reference parameter is null
     * @throws IllegalArgumentException if positions/sizes are invalid or lists have different lengths
     */
    public NeighborhoodView(
            AbstractCell<V, A> viewingCell,
            int currentPosition,
            int arraySize,
            List<AbstractCell<V, A>> visibleNeighbors,
            List<Integer> neighborPositions) {
        
        // PURPOSE: Validate inputs and initialize neighborhood view
        // PROCESS:
        //   1. Validate all references are non-null
        //   2. Validate currentPosition is in bounds [0, arraySize)
        //   3. Validate visibleNeighbors and neighborPositions have same length
        //   4. Store all fields
        
        if (viewingCell == null) {
            throw new NullPointerException("viewingCell cannot be null");
        }
        if (visibleNeighbors == null) {
            throw new NullPointerException("visibleNeighbors cannot be null");
        }
        if (neighborPositions == null) {
            throw new NullPointerException("neighborPositions cannot be null");
        }
        if (currentPosition < 0 || currentPosition >= arraySize) {
            throw new IllegalArgumentException(
                "currentPosition " + currentPosition + " out of bounds [0, " + arraySize + ")");
        }
        if (arraySize <= 0) {
            throw new IllegalArgumentException("arraySize must be positive, got: " + arraySize);
        }
        if (visibleNeighbors.size() != neighborPositions.size()) {
            throw new IllegalArgumentException(
                "visibleNeighbors and neighborPositions must have same length: " +
                visibleNeighbors.size() + " vs " + neighborPositions.size());
        }
        
        this.viewingCell = viewingCell;
        this.currentPosition = currentPosition;
        this.arraySize = arraySize;
        this.visibleNeighbors = visibleNeighbors;
        this.neighborPositions = neighborPositions;
    }
    
    /**
     * Get the cell whose neighborhood is being viewed.
     *
     * <p><strong>PURPOSE:</strong> Provide access to viewing cell for context.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>OUTPUTS:</strong> The viewing cell (never null)</p>
     *
     * @return the viewing cell
     */
    public AbstractCell<V, A> getViewingCell() {
        return viewingCell;
    }
    
    /**
     * Get the current position of the viewing cell.
     *
     * <p><strong>PURPOSE:</strong> Provide position for relative calculations.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>OUTPUTS:</strong> Current position (0 to arraySize-1)</p>
     *
     * @return the current position
     */
    public int getCurrentPosition() {
        return currentPosition;
    }
    
    /**
     * Get the total size of the cell array.
     *
     * <p><strong>PURPOSE:</strong> Provide array size for boundary checks.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>OUTPUTS:</strong> Array size (positive integer)</p>
     *
     * @return the array size
     */
    public int getArraySize() {
        return arraySize;
    }
    
    /**
     * Get the list of visible neighbor cells.
     *
     * <p><strong>PURPOSE:</strong> Provide cells for inspection during swap evaluation.
     * List may be empty if no neighbors are visible.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>OUTPUTS:</strong> Unmodifiable list of visible neighbors (never null, may be empty)</p>
     *
     * @return list of visible neighbors
     */
    public List<AbstractCell<V, A>> getVisibleNeighbors() {
        return visibleNeighbors;
    }
    
    /**
     * Get the positions of visible neighbors.
     *
     * <p><strong>PURPOSE:</strong> Provide positions for swap proposals.
     * Parallel to visibleNeighbors list.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>OUTPUTS:</strong> Unmodifiable list of positions (never null, may be empty)</p>
     *
     * @return list of neighbor positions
     */
    public List<Integer> getNeighborPositions() {
        return neighborPositions;
    }
    
    /**
     * Get the number of visible neighbors.
     *
     * <p><strong>PURPOSE:</strong> Convenience method for checking if neighbors exist.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>OUTPUTS:</strong> Number of visible neighbors (non-negative)</p>
     *
     * @return count of visible neighbors
     */
    public int getNeighborCount() {
        return visibleNeighbors.size();
    }
    
    /**
     * Check if any neighbors are visible.
     *
     * <p><strong>PURPOSE:</strong> Convenience method for early-exit checks.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>OUTPUTS:</strong> true if at least one neighbor is visible</p>
     *
     * @return true if neighbors exist
     */
    public boolean hasNeighbors() {
        return !visibleNeighbors.isEmpty();
    }
    
    /**
     * Get a neighbor at a specific index.
     *
     * <p><strong>PURPOSE:</strong> Direct access to specific neighbor by index.</p>
     *
     * <p><strong>INPUTS:</strong> index - position in visibleNeighbors list (0-based)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate index is in bounds</li>
     *   <li>Return visibleNeighbors.get(index)</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> The neighbor cell at specified index</p>
     *
     * @param index the index (0 to neighborCount-1)
     * @return the neighbor at specified index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public AbstractCell<V, A> getNeighborAt(int index) {
        return visibleNeighbors.get(index);
    }
    
    /**
     * Get the position of a neighbor at a specific index.
     *
     * <p><strong>PURPOSE:</strong> Direct access to specific neighbor position.</p>
     *
     * <p><strong>INPUTS:</strong> index - position in neighborPositions list (0-based)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate index is in bounds</li>
     *   <li>Return neighborPositions.get(index)</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> The array position of neighbor at specified index</p>
     *
     * @param index the index (0 to neighborCount-1)
     * @return the position of neighbor at specified index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public int getNeighborPositionAt(int index) {
        return neighborPositions.get(index);
    }
    
    /**
     * Get the left neighbor if it exists.
     *
     * <p><strong>PURPOSE:</strong> Convenience method for BUBBLE and INSERTION algotypes
     * that frequently need left adjacent neighbor.</p>
     *
     * <p><strong>INPUTS:</strong> None</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check if currentPosition > 0 (left neighbor can exist)</li>
     *   <li>Search visibleNeighbors for neighbor at currentPosition - 1</li>
     *   <li>Return Optional.of(neighbor) if found</li>
     *   <li>Return Optional.empty() if not found or at left boundary</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Optional containing left neighbor, or empty</p>
     *
     * @return Optional containing left neighbor, or empty
     */
    public Optional<AbstractCell<V, A>> getLeftNeighbor() {
        if (currentPosition == 0) {
            return Optional.empty();
        }
        int leftPos = currentPosition - 1;
        for (int i = 0; i < neighborPositions.size(); i++) {
            if (neighborPositions.get(i) == leftPos) {
                return Optional.of(visibleNeighbors.get(i));
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get the right neighbor if it exists.
     *
     * <p><strong>PURPOSE:</strong> Convenience method for BUBBLE algotype
     * that frequently needs right adjacent neighbor.</p>
     *
     * <p><strong>INPUTS:</strong> None</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check if currentPosition < arraySize - 1 (right neighbor can exist)</li>
     *   <li>Search visibleNeighbors for neighbor at currentPosition + 1</li>
     *   <li>Return Optional.of(neighbor) if found</li>
     *   <li>Return Optional.empty() if not found or at right boundary</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Optional containing right neighbor, or empty</p>
     *
     * @return Optional containing right neighbor, or empty
     */
    public Optional<AbstractCell<V, A>> getRightNeighbor() {
        if (currentPosition >= arraySize - 1) {
            return Optional.empty();
        }
        int rightPos = currentPosition + 1;
        for (int i = 0; i < neighborPositions.size(); i++) {
            if (neighborPositions.get(i) == rightPos) {
                return Optional.of(visibleNeighbors.get(i));
            }
        }
        return Optional.empty();
    }
}
