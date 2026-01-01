package com.emergent.doom.cell;

import com.emergent.doom.cell.HasValue;
import com.emergent.doom.cell.HasGroup;
import com.emergent.doom.cell.HasStatus;
import com.emergent.doom.cell.HasAlgotype;
import com.emergent.doom.group.CellGroup;
import com.emergent.doom.group.CellStatus;

import com.emergent.doom.group.GroupAwareCell;

/**
 * Abstract base for Levin cell-view Bubble Sort cells.
 * Baked-in: Algotype.BUBBLE (local adj views/swaps).
 * Domain extends and implements compareTo (value-based).
 *
 * <p>PURPOSE: Provide Bubble policy foundation (bidirectional local).</p>
 * <p>INPUTS: value (int) - fixed sort key, sortDirection - ascending or descending.</p>
 * <p>PROCESS:</p>
 * <ol>
 *   <li>Construct with value and optional sort direction.</li>
 *   <li>Return fixed BUBBLE algotype.</li>
 *   <li>Domain implements compareTo using value.</li>
 * </ol>
 * <p>OUTPUTS: Cell with BUBBLE policy.</p>
 * <p>DEPENDENCIES: Cell interface, HasSortDirection interface.</p>
 *
 * <p>GROUND TRUTH REFERENCE: cell_research/BubbleSortCell.py:54-56</p>
 * <pre>
 * if self.reverse_direction:
 *     return self.value < target if check_right else self.value > target
 * return self.value > target if check_right else self.value < target
 * </pre>
 */
public abstract class BubbleCell<T extends BubbleCell<T>> implements Cell<T>, GroupAwareCell<T>, HasSortDirection, HasValue, HasGroup, HasStatus, HasAlgotype {
    protected final int value;
    protected CellGroup<T> group = null;
    protected CellStatus status = CellStatus.ACTIVE;
    protected CellStatus previousStatus = CellStatus.ACTIVE;
    protected int leftBoundary;
    protected int rightBoundary;
    protected final SortDirection sortDirection;  // Sort direction for cross-purpose experiments

    /**
     * Construct a BubbleCell with the specified value and default ASCENDING direction.
     *
     * @param value the sort key value
     */
    protected BubbleCell(int value) {
        this(value, SortDirection.ASCENDING);
    }

    /**
     * Construct a BubbleCell with the specified value and sort direction.
     *
     * <p>This constructor enables cross-purpose sorting experiments where
     * cells in the same array can sort in opposite directions.</p>
     *
     * @param value the sort key value
     * @param sortDirection the sort direction (ASCENDING or DESCENDING)
     */
    protected BubbleCell(int value, SortDirection sortDirection) {
        this.value = value;
        this.sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Algotype getAlgotype() {
        return Algotype.BUBBLE;  // Baked-in concrete impl
    }

    @Override
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    // Abstract: Domain provides value comparison
    public abstract int compareTo(T other);

    // Implementation of HasGroup, HasStatus for cell instances
    @Override
    public CellGroup<T> getGroup() { return group; }

    @Override
    public CellStatus getStatus() { return status; }

    @Override
    public CellStatus getPreviousStatus() { return previousStatus; }

    @Override
    public void setStatus(CellStatus status) { previousStatus = this.status; this.status = status; }

    @Override
    public void setPreviousStatus(CellStatus previousStatus) { this.previousStatus = previousStatus; }

    @Override
    public void setGroup(CellGroup<T> group) { this.group = group; }

    // Implementation of GroupAwareCell
    @Override
    public int getLeftBoundary() { return leftBoundary; }

    @Override
    public void setLeftBoundary(int leftBoundary) { this.leftBoundary = leftBoundary; }

    @Override
    public int getRightBoundary() { return rightBoundary; }

    @Override
    public void setRightBoundary(int rightBoundary) { this.rightBoundary = rightBoundary; }

    @Override
    public void updateForGroupMerge() {
        // BubbleCell: No action required on merge
    }
}