package com.emergent.doom.swap;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.group.GroupAwareCell;

/**
 * Simple integer-based cell for testing purposes.
 */
public class IntCell implements Cell<IntCell>, GroupAwareCell<IntCell> {

    private final int value;

    public IntCell(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Algotype getAlgotype() {
        return Algotype.BUBBLE;
    }

    @Override
    public com.emergent.doom.group.CellGroup<IntCell> getGroup() { return null; }
    @Override
    public com.emergent.doom.group.CellStatus getStatus() { return com.emergent.doom.group.CellStatus.ACTIVE; }
    @Override
    public com.emergent.doom.group.CellStatus getPreviousStatus() { return com.emergent.doom.group.CellStatus.ACTIVE; }
    @Override
    public void setStatus(com.emergent.doom.group.CellStatus status) {}
    @Override
    public void setPreviousStatus(com.emergent.doom.group.CellStatus status) {}
    @Override
    public void setGroup(com.emergent.doom.group.CellGroup<IntCell> group) {}
    @Override
    public int getLeftBoundary() { return 0; }
    @Override
    public void setLeftBoundary(int leftBoundary) {}
    @Override
    public int getRightBoundary() { return 0; }
    @Override
    public void setRightBoundary(int rightBoundary) {}
    @Override
    public void updateForGroupMerge() {}

    @Override
    public int compareTo(IntCell other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public String toString() {
        return "IntCell(" + value + ")";
    }
}
