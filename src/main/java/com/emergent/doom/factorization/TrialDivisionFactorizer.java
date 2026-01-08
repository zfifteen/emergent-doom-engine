package com.emergent.doom.factorization;

import java.util.Objects;

/**
 * PURPOSE: Implement Trial Division factorization as a Bubble sort analogue.
 * 
 * SEMANTICS: Trial Division is the exhaustive method: test every candidate divisor
 * in order. In EDE terms, this translates to Bubble sort: repeatedly scan the array,
 * compare adjacent elements by value (remainder), and swap if out of order.
 * Smaller remainders (closer to 0 = factors) naturally bubble toward the front.
 * 
 * STRATEGY:
 *   1. Scan divisor array from beginning to end
 *   2. For each adjacent pair (d_i, d_{i+1}), compare remainders
 *   3. If remainder(d_i) > remainder(d_{i+1}), swap them
 *   4. When remainder = 0 is detected, lock that cell as a factor
 *   5. Repeat until all factors found or timeout
 * 
 * EMERGENT PROPERTIES:
 *   - SLOW: Every step compares only one pair; requires O(n²) steps in worst case
 *   - GUARANTEED: Will eventually find all factors (exhaustive coverage)
 *   - SIMPLE: No heuristics; straightforward logic
 *   - COOPERATES: Cooperates with Fermat/Pollard in chimeric configurations
 * 
 * EDE ANALOGUE: Bubble sort
 *   - Cell = divisor candidate
 *   - Value = remainder (sortedness = remainder ≈ 0)
 *   - Operation = compare adjacent, swap if left > right
 *   - Result = factors (remainder=0) bubble to front
 * 
 * USER STORY:
 * As a factorization researcher, I want Trial Division as a baseline algotype
 * so that I can measure its convergence speed, verify it finds factors reliably,
 * and observe how it cooperates (or interferes) with Fermat and Pollard in
 * chimeric configurations.
 * 
 * INPUTS: FactorizationCell array, semiprime N, step count
 * OUTPUTS: Array with one compare-swap operation executed
 * LOGIC: Maintain a position pointer; scan array from 0 to end; compare adjacent;
 * swap if needed; lock factors; move pointer forward (wrapping or resetting)
 */
public class TrialDivisionFactorizer implements FactorizationAlgotype {
    
    /** Algorithm name for logging and metrics. */
    private static final String NAME = "Trial Division";
    
    /**
     * Current position in the divisor array where the next compare-swap will occur.
     * Maintains state across multiple calls to executeStep().
     * After reaching end of array, resets to 0.
     */
    private int currentPosition;
    
    /**
     * CONSTRUCTOR.
     * 
     * PURPOSE: Initialize Trial Division factorizer.
     * LOGIC: Set position to 0 (start of array)
     */
    public TrialDivisionFactorizer() {
        this.currentPosition = 0;
    }
    
    /**
     * PURPOSE: Execute one step of Trial Division (Bubble sort analogue).
     * 
     * INPUTS:
     *   - divisors: array of FactorizationCell objects to search
     *   - semiprimeN: semiprime being factored (for logging)
     *   - stepCount: iteration number
     * 
     * OUTPUTS: divisors array potentially modified by:
     *   - Swap of two adjacent cells (if remainders out of order)
     *   - Locking of a factor (if remainder = 0)
     *   - Claiming cells (marking which algotype touched them)
     * 
     * LOGIC:
     *   1. Get current position i in array
     *   2. If i >= array.length-1, reset to 0 (start new pass)
     *   3. Get cells at i and i+1
     *   4. If either is already locked (factor), skip (don't disturb frozen cells)
     *   5. Compare remainders: if remainder(i) > remainder(i+1), swap
     *   6. Check if either cell has remainder = 0; if so, lock as factor
     *   7. Mark both cells as claimed by Trial Division
     *   8. Increment position for next step
     * 
     * INVARIANTS:
     *   - No locked cells are swapped (frozen cells are immovable)
     *   - Each locked factor is recorded with step number and algotype name
     *   - currentPosition always valid [0, array.length)
     */
    @Override
    public void executeStep(FactorizationCell[] divisors, long semiprimeN, long stepCount) {
        Objects.requireNonNull(divisors, "Divisor array cannot be null");
        
        if (divisors.length < 2) {
            return;  // Nothing to compare in array of size 0 or 1
        }
        
        // Wrap position around when reaching end of array (start new pass)
        if (currentPosition >= divisors.length - 1) {
            currentPosition = 0;
        }
        
        int i = currentPosition;
        FactorizationCell left = divisors[i];
        FactorizationCell right = divisors[i + 1];
        
        // Don't disturb frozen cells (already-locked factors)
        if (left.isFactor() || right.isFactor()) {
            currentPosition++;
            return;
        }
        
        // Compare remainders: if left > right, swap (bubble smaller to front)
        if (left.getRemainder() > right.getRemainder()) {
            divisors[i] = right;
            divisors[i + 1] = left;
        }
        
        // Mark both cells as claimed by this algotype
        left.setClaimingAlgotype(NAME);
        right.setClaimingAlgotype(NAME);
        
        // Lock any factor (remainder = 0) found
        if (left.getRemainder() == 0 && !left.isFactor()) {
            left.lockAsFactor(NAME, stepCount);
        }
        if (right.getRemainder() == 0 && !right.isFactor()) {
            right.lockAsFactor(NAME, stepCount);
        }
        
        // Move to next position for next step
        currentPosition++;
    }
    
    /**
     * PURPOSE: Reset internal state for a new factorization trial.
     * 
     * LOGIC: Reset position pointer to 0, allowing fresh scan from beginning
     */
    @Override
    public void reset() {
        this.currentPosition = 0;
    }
    
    /**
     * PURPOSE: Get algorithm name.
     * 
     * OUTPUTS: "Trial Division"
     */
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String toString() {
        return String.format("TrialDivisionFactorizer{position=%d}", currentPosition);
    }
}
