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
     *
     * <p>Note: The value passed to BubbleCell is derived from the remainder for
     * compatibility with getValue() calls in execution engines (e.g., isLeftSorted).</p>
     */
    public RemainderCell(BigInteger target, int position) {
        // Pass remainder as value for getValue() compatibility with execution engines
        // Validation and calculation done in static helper to ensure super() is called first
        super(computeRemainderValue(target, position));

        this.target = target;
        this.position = position;
        this.remainder = target.mod(BigInteger.valueOf(position));
    }

    /**
     * Helper method to compute remainder value with validation.
     * Called before super() to ensure proper initialization order.
     */
    private static int computeRemainderValue(BigInteger target, int position) {
        if (target == null || target.compareTo(BigInteger.ZERO) <= 0) {
            throw new IllegalArgumentException("Target must be positive");
        }
        if (position < 1) {
            throw new IllegalArgumentException("Position must be >= 1");
        }
        // Using intValue() is safe since remainders are bounded by position (< Integer.MAX_VALUE for typical use)
        return target.mod(BigInteger.valueOf(position)).intValue();
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
