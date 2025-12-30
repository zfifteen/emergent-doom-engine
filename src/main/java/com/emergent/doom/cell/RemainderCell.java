package com.emergent.doom.cell;

import java.math.BigInteger;
import java.util.Objects;

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
public class RemainderCell extends BubbleCell<RemainderCell> {
    
    private final BigInteger remainder;
    private final int position;
    private final BigInteger target;
    
    /**
     * IMPLEMENTED: Construct a RemainderCell with target number and position
     */
    public RemainderCell(BigInteger target, int position) {
        super(0);  // Dummy value; compareTo overridden to use remainder
        if (target == null || target.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Target must be positive");
        }
        if (position < 1) {
            throw new IllegalArgumentException("Position must be >= 1");
        }

        this.target = target;
        this.position = position;
        // Calculate remainder = target mod position
        this.remainder = target.mod(BigInteger.valueOf(position));
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
    
    /**
     * IMPLEMENTED: Get the remainder value for metrics/analysis
     */
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
        return String.format("Cell[pos=%d, rem=%s]", position, remainder);
    }
}
