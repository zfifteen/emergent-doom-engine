package com.emergent.doom.factorization;

import java.util.Objects;

/**
 * PURPOSE: Represent a single divisor candidate in emergent semi-prime factorization.
 * SEMANTICS: A FactorizationCell is a "cell" in the EDE framework where the value
 * is the remainder when the target semiprime N is divided by the candidate divisor.
 * SORTEDNESS: A FactorizationCell is sorted when its remainder equals zero (factor found)
 * or is locked as a frozen cell (factor already discovered and locked).
 * 
 * DESIGN PRINCIPLE: Just as Levin et al. (2024) map array positions → cells and integer
 * keys → values in a sorting context, we map divisor candidates → cells and remainders → values
 * in a factorization context. Multiple "algotypes" (Trial Division, Fermat, Pollard)
 * navigate this remainder-space, competing to lock factors via frozen cells.
 * 
 * USER STORY:
 * As a factorization researcher, I want to represent divisor candidates as cells
 * so that I can apply EDE's clustering and delayed gratification metrics to understand
 * how emergent multi-strategy approaches discover semi-prime factors faster and with
 * better error tolerance than sequential factorization.
 * 
 * INPUTS: semiprime N, candidate divisor d
 * OUTPUTS: FactorizationCell with remainder value, comparability, and state tracking
 * LOGIC: Compute and cache N % d; provide Comparable interface for remainder-based sorting;
 * track discovery state (which algotype found this factor, at which step).
 */
public class FactorizationCell implements Comparable<FactorizationCell> {
    
    /** The semiprime being factored (constant for all cells in a given experiment). */
    private final long semiprimeN;
    
    /** The candidate divisor d ∈ [2, √N]. This is immutable after construction. */
    private final long divisor;
    
    /** 
     * The remainder r = N % d. This is the "value" in EDE terms—the key by which
     * sorting algorithms order cells. Smaller remainders indicate candidates closer
     * to being actual factors (remainder ≈ 0 = factor found).
     */
    private final long remainder;
    
    /** 
     * Which algotype (e.g., TrialDivision, Fermat, Pollard) "claimed" this cell
     * by moving it, comparing it, or swapping it. Null if unclaimed yet.
     */
    private String claimingAlgotype;
    
    /** 
     * True if this cell has been locked as a factor (remainder ≈ 0) and is now frozen.
     * Frozen cells represent discovered factors and are no longer candidate for swaps.
     */
    private boolean isFactor;
    
    /** 
     * The step number (iteration count) at which this factor was discovered and locked.
     * Used for metrics like "time to first factor" and "time between factor discoveries".
     */
    private long discoveredAtStep;
    
    /**
     * Constructs a FactorizationCell representing a divisor candidate in the search
     * for factors of semiprime N.
     * 
     * PURPOSE: Initialize a divisor cell with precomputed remainder value.
     * INPUTS:
     *   - semiprimeN: the semiprime being factored (e.g., 143 = 11 × 13)
     *   - divisor: candidate divisor d ∈ [2, √N] (e.g., 11)
     * OUTPUTS: FactorizationCell with remainder, comparability, and state tracking
     * LOGIC:
     *   1. Store semiprime N and divisor as final fields (immutable)
     *   2. Compute remainder r = N % d immediately (cache it)
     *   3. Initialize state: not a factor yet, unclaimed, no discovery step
     * 
     * @param semiprimeN the semiprime being factored
     * @param divisor the candidate divisor
     * @throws IllegalArgumentException if divisor < 2 or divisor > √N
     */
    public FactorizationCell(long semiprimeN, long divisor) {
        if (divisor < 2) {
            throw new IllegalArgumentException(
                String.format("Divisor must be >= 2; got %d", divisor)
            );
        }
        long sqrtN = (long) Math.sqrt(semiprimeN);
        if (divisor > sqrtN) {
            throw new IllegalArgumentException(
                String.format("Divisor %d exceeds √%d ≈ %d; only search up to √N",
                    divisor, semiprimeN, sqrtN)
            );
        }
        
        this.semiprimeN = semiprimeN;
        this.divisor = divisor;
        this.remainder = semiprimeN % divisor;  // Cache remainder immediately
        this.claimingAlgotype = null;
        this.isFactor = false;
        this.discoveredAtStep = -1L;  // -1 indicates "not yet discovered"
    }
    
    /**
     * PURPOSE: Compare two FactorizationCells by their remainder values (sortedness metric).
     * SEMANTICS: Following EDE framework, smaller remainders indicate higher sortedness
     * (closer to being a factor, i.e., remainder ≈ 0). This Comparable interface enables
     * sorting algorithms to navigate divisor space by comparing remainders.
     * 
     * INPUTS: other FactorizationCell to compare with
     * OUTPUTS: negative if this.remainder < other.remainder (this is "more sorted")
     *          zero if remainders equal
     *          positive if this.remainder > other.remainder (other is "more sorted")
     * LOGIC: Standard numeric comparison; Java's Long.compare handles edge cases.
     * 
     * MATHEMATICAL NOTE: When a Bubble sort or Insertion sort "compares" two cells,
     * it is asking "which has a smaller remainder?" A factor (remainder = 0) will
     * naturally sort toward the beginning via standard comparison-swap algorithms.
     * 
     * @param other the FactorizationCell to compare
     * @return comparison result (-1, 0, or +1)
     */
    @Override
    public int compareTo(FactorizationCell other) {
        Objects.requireNonNull(other, "Cannot compare with null FactorizationCell");
        // Smaller remainder = higher sortedness = should come first
        return Long.compare(this.remainder, other.remainder);
    }
    
    /**
     * PURPOSE: Lock this cell as a discovered factor and mark the discovery step.
     * SEMANTICS: Once a factor is discovered (remainder ≈ 0), the cell is frozen in place.
     * In EDE terms, frozen cells are immovable and represent "solved" positions.
     * 
     * INPUTS:
     *   - algotypeName: name of the algotype that discovered this factor (e.g., "TrialDivision")
     *   - stepNumber: iteration count when factor was locked
     * OUTPUTS: Cell state updated; isFactor = true, claimingAlgotype set, discoveredAtStep recorded
     * LOGIC:
     *   1. Verify remainder is near-zero (tolerance for floating-point remainder = 0)
     *   2. Mark cell as discovered factor
     *   3. Record which algotype found it and when
     *   4. This cell now behaves as frozen (immutable in sorting operations)
     * 
     * @param algotypeName the name of the factorization algotype that discovered this factor
     * @param stepNumber the iteration step at which discovery occurred
     * @throws IllegalStateException if remainder is not zero (not a valid factor)
     */
    public void lockAsFactor(String algotypeName, long stepNumber) {
        if (this.remainder != 0) {
            throw new IllegalStateException(
                String.format("Cannot lock divisor %d as factor; remainder = %d (not zero)",
                    this.divisor, this.remainder)
            );
        }
        this.isFactor = true;
        this.claimingAlgotype = algotypeName;
        this.discoveredAtStep = stepNumber;
    }
    
    /**
     * PURPOSE: Mark this cell as claimed by an algotype during a comparison or swap.
     * SEMANTICS: Enables tracking which factorization strategy has been active on
     * this divisor candidate. Useful for post-experiment analysis and visualizations.
     * 
     * INPUTS: algotypeName (e.g., "Fermat", "Pollard")
     * OUTPUTS: Cell's claimingAlgotype field updated
     * LOGIC: Simple setter; replaces previous algotype if this cell was previously claimed.
     * 
     * @param algotypeName the name of the algotype claiming this cell
     */
    public void setClaimingAlgotype(String algotypeName) {
        this.claimingAlgotype = algotypeName;
    }
    
    // ============= Accessors (Immutable Fields) =============
    
    /** @return the semiprime N being factored */
    public long getSemiprimeN() {
        return semiprimeN;
    }
    
    /** @return the divisor candidate d */
    public long getDivisor() {
        return divisor;
    }
    
    /** @return the remainder N % d (the "value" in EDE terms) */
    public long getRemainder() {
        return remainder;
    }
    
    /** @return true if this cell has been locked as a discovered factor */
    public boolean isFactor() {
        return isFactor;
    }
    
    /** @return the step number at which this factor was discovered, or -1 if not yet discovered */
    public long getDiscoveredAtStep() {
        return discoveredAtStep;
    }
    
    /** @return the name of the algotype that discovered this factor, or null if not yet discovered */
    public String getClaimingAlgotype() {
        return claimingAlgotype;
    }
    
    // ============= Sortedness Metric =============
    
    /**
     * PURPOSE: Calculate the "sortedness" contribution of this cell (0.0 to 1.0).
     * SEMANTICS: Following EDE framework, sortedness is the fraction of positions that are
     * "correctly sorted." For factorization, a position is correctly sorted if:
     *   1. It is a factor (remainder ≈ 0) OR
     *   2. It is frozen (isFactor = true)
     * 
     * OUTPUTS: 1.0 if this cell represents a discovered factor; 0.0 otherwise
     * LOGIC: Factors are "sorted" in the sense that they've been discovered and locked.
     * 
     * @return sortedness contribution: 1.0 if factor, 0.0 otherwise
     */
    public double getSortednessContribution() {
        return isFactor ? 1.0 : 0.0;
    }
    
    // ============= Object Methods =============
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FactorizationCell)) return false;
        FactorizationCell other = (FactorizationCell) obj;
        // Two cells are equal if they represent the same (semiprime, divisor) pair
        return this.semiprimeN == other.semiprimeN && this.divisor == other.divisor;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(semiprimeN, divisor);
    }
    
    @Override
    public String toString() {
        return String.format(
            "FactorizationCell{N=%d, d=%d, r=%d, %s, locked=%s, step=%d}",
            semiprimeN, divisor, remainder,
            claimingAlgotype != null ? "claimed by " + claimingAlgotype : "unclaimed",
            isFactor, discoveredAtStep
        );
    }
}
