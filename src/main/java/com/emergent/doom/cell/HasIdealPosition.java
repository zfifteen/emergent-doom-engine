package com.emergent.doom.cell;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interface for cells that support ideal position tracking, required for SELECTION algotype.
 *
 * <p>This interface defines the contract for accessing and updating the 'idealPos' state,
 * which is crucial for Levin's selection sort behavior where cells chase dynamic targets.
 * Implementing classes must provide thread-safe operations via AtomicInteger for parallel execution.</p>
 *
 * <p>Purpose: Enables polymorphic access to idealPos in ExecutionEngine without type casts,
 * surfacing errors early for unsupported cell types via explicit exceptions.</p>
 *
 * <p>Expected inputs: Cell instances requiring SELECTION behavior.
 * Expected outputs: Ideal position values and updates.
 * Data flow: ExecutionEngine queries/updates via these methods during swap decisions.</p>
 *
 * @author opencode
 */
public interface HasIdealPosition {
    /**
     * Get the current ideal position.
     *
     * @return the ideal position (0-based index)
     */
    int getIdealPos();

    /**
     * Set the ideal position to a new value.
     *
     * @param newIdealPos the new position
     */
    void setIdealPos(int newIdealPos);

    /**
     * Increment the ideal position atomically and return the new value.
     * Used when a swap is denied (Levin p.9: cell adjusts target rightward).
     *
     * @return the new ideal position after increment
     */
    int incrementIdealPos();

    /**
     * Atomically compare-and-set the ideal position.
     *
     * @param expected the expected current value
     * @param newValue the new value to set
     * @return true if the set was successful
     */
    boolean compareAndSetIdealPos(int expected, int newValue);

    /**
     * Update ideal position based on group boundaries.
     * Matches Python cell_research SelectionSortCell.update() behavior.
     *
     * <p>Python reference (SelectionSortCell.py:77-81):</p>
     * <pre>
     * def update(self):
     *     # Called when group merges - reset ideal position
     *     if self.reverse_direction:
     *         self.ideal_position = self.right_boundary
     *     else:
     *         self.ideal_position = self.left_boundary
     * </pre>
     *
     * @param leftBoundary the left boundary position (0-based)
     * @param rightBoundary the right boundary position (0-based)
     * @param reverseDirection true for descending sort, false for ascending
     */
    default void updateForBoundary(int leftBoundary, int rightBoundary, boolean reverseDirection) {
        if (reverseDirection) {
            setIdealPos(rightBoundary);
        } else {
            setIdealPos(leftBoundary);
        }
    }
}
