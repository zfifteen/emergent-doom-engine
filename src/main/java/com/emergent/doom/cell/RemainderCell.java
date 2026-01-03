package com.emergent.doom.cell;

import com.emergent.doom.group.CellGroup;
import com.emergent.doom.group.CellStatus;
import com.emergent.doom.group.GroupAwareCell;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Example implementation of Cell for integer factorization domain.
 * 
 * <p>This cell represents a candidate factor by storing the remainder
 * when the target number is divided by the cell's position. Lower
 * remainders are considered "better" - a remainder of zero indicates
 * a perfect factor.</p>
 * 
 * <p><strong>Domain Logic:</strong> For a target N and position p,
 * this cell stores N mod p. The sorting behavior naturally drives
 * better factors to the front when cells are swapped by the engine.</p>
 */
public class RemainderCell implements Cell<RemainderCell>, GroupAwareCell<RemainderCell>, HasAlgotype, HasIdealPosition {
    
    private final BigInteger remainder;
    private final int position;
    private final BigInteger target;
    private final Algotype algotype;
    private CellGroup<RemainderCell> group = null;
    private CellStatus status = CellStatus.ACTIVE;
    private CellStatus previousStatus = CellStatus.ACTIVE;
    private int leftBoundary;
    private int rightBoundary;
    private final AtomicInteger idealPos = new AtomicInteger(0);
    
    /**
     * Construct a RemainderCell with target number and position, defaulting to BUBBLE algotype.
     */
    public RemainderCell(BigInteger target, int position) {
        this(target, position, Algotype.BUBBLE);
    }

    /**
     * Construct a RemainderCell with target number, position, and specified algotype.
     */
    public RemainderCell(BigInteger target, int position, Algotype algotype) {
        if (target == null || target.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Target must be positive");
        }
        if (position < 1) {
            throw new IllegalArgumentException("Position must be >= 1");
        }
        this.target = target;
        this.position = position;
        this.remainder = target.mod(BigInteger.valueOf(position));
        this.algotype = algotype != null ? algotype : Algotype.BUBBLE;
    }
    
    /**
     * IMPLEMENTED: Compare this cell to another based on remainder values.
     * Lower remainder = "better" cell (comes first in sorted order).
     */
     @Override
     public int compareTo(RemainderCell other) {
         // Compare remainders - lower remainder is "better"
         int remainderComparison = this.remainder.compareTo(other.remainder);

         // If remainders are equal, compare by position for stable sorting
         if (remainderComparison == 0) {
             return Integer.compare(this.position, other.position);
         }

         return remainderComparison;
    }

    public Comparable<?> getComparableValue() {
        return remainder; // Returns the full BigInteger
    }

    @Override
    public int getValue() {
        return remainder.intValue();
    }

    @Override
    public Algotype getAlgotype() {
        return algotype;
    }
    
    @Override
    public CellGroup<RemainderCell> getGroup() { return group; }

    @Override
    public void setGroup(CellGroup<RemainderCell> group) { this.group = group; }

    @Override
    public int getLeftBoundary() { return leftBoundary; }

    @Override
    public void setLeftBoundary(int leftBoundary) { this.leftBoundary = leftBoundary; }

    @Override
    public int getRightBoundary() { return rightBoundary; }

    @Override
    public void setRightBoundary(int rightBoundary) { this.rightBoundary = rightBoundary; }

    @Override
    public CellStatus getStatus() { return status; }

    @Override
    public void setStatus(CellStatus status) { 
        this.previousStatus = this.status;
        this.status = status; 
    }

    @Override
    public CellStatus getPreviousStatus() { return previousStatus; }

    @Override
    public void setPreviousStatus(CellStatus previousStatus) { this.previousStatus = previousStatus; }

    @Override
    public int getIdealPos() {
        return idealPos.get();
    }

    @Override
    public int incrementIdealPos() {
        return idealPos.incrementAndGet();
    }

    @Override
    public void setIdealPos(int newIdealPos) {
        this.idealPos.set(newIdealPos);
    }

    @Override
    public boolean compareAndSetIdealPos(int expected, int newValue) {
        return idealPos.compareAndSet(expected, newValue);
    }

    @Override
    public void updateForGroupMerge() {
        setIdealPos(leftBoundary);
    }

    public BigInteger getRemainder() {
        return remainder;
    }
    
    /**
     * IMPLEMENTED: Get the position (candidate factor)
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * IMPLEMENTED: Get the target number being factored
     */
    public BigInteger getTarget() {
        return target;
    }
    
    /**
     * IMPLEMENTED: Check if this cell represents a perfect factor
     */
    public boolean isFactor() {
        return remainder.equals(BigInteger.ZERO);
    }
    
    /**
     * IMPLEMENTED: Check equality based on remainder, position, and target
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        RemainderCell that = (RemainderCell) obj;
        return position == that.position &&
               remainder.equals(that.remainder) &&
               target.equals(that.target);
    }
    
    /**
     * IMPLEMENTED: Generate hash code consistent with equals
     */
    @Override
    public int hashCode() {
        return Objects.hash(remainder, position, target);
    }
    
    /**
     * IMPLEMENTED: Create readable string representation
     */
    @Override
    public String toString() {
        return String.format("Cell[pos=%d, rem=%s, type=%s]", position, remainder, algotype);
    }
}
