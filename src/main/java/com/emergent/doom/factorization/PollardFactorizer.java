package com.emergent.doom.factorization;

import java.util.Objects;

/**
 * PURPOSE: Implement Pollard-inspired factorization as a Selection sort analogue.
 * 
 * SEMANTICS: Pollard's method conceptually finds the best divisor (smallest remainder)
 * and reduces the problem. In EDE terms, this is Selection sort: repeatedly scan to
 * find the best (minimum) element, lock it (freeze it in place), and move on.
 * Each step identifies the divisor with the smallest remainder and locks it if zero.
 * 
 * STRATEGY:
 *   1. Scan divisor array to find cell with minimum remainder
 *   2. Lock it as a factor (if remainder = 0)
 *   3. Mark it as processed; move to next iteration
 *   4. Repeat until all factors found
 * 
 * EMERGENT PROPERTIES:
 *   - FAST: In best case (factor with r=0), finds in O(n) scans
 *   - EFFICIENT: Prioritizes most promising divisors (smallest remainders)
 *   - REDUCES PROBLEM: Finding first factor p enables N/p for next phase
 *   - SKEW-TOLERANT: Works well whether p ≈ q or p << q
 * 
 * EDE ANALOGUE: Selection sort
 *   - Cell = divisor candidate
 *   - Value = remainder (sortedness = remainder ≈ 0)
 *   - Operation = find minimum remainder, lock it
 *   - Result = best candidates (smallest remainders) locked first
 * 
 * USER STORY:
 * As a factorization researcher, I want Pollard-inspired factorization
 * so that I can test whether selecting optimal divisors outperforms exhaustive search,
 * and observe how Pollard cooperates with Trial Division and Fermat in chimeric mode.
 * 
 * INPUTS: FactorizationCell array, semiprime N, step count
 * OUTPUTS: Array with best divisor identified, potentially locked as factor
 * LOGIC: Scan full array each step; find argmin(remainder); lock if r=0
 */
public class PollardFactorizer implements FactorizationAlgotype {
    
    /** Algorithm name for logging and metrics. */
    private static final String NAME = "Pollard";
    
    /**
     * Index of the last locked factor, used to skip already-processed divisors.
     * Allows us to focus on remaining (unlocked) candidates.
     */
    private int lastProcessedIndex;
    
    /**
     * CONSTRUCTOR.
     * 
     * PURPOSE: Initialize Pollard factorizer.
     * LOGIC: Set lastProcessedIndex to -1 (no factors locked yet)
     */
    public PollardFactorizer() {
        this.lastProcessedIndex = -1;
    }
    
    /**
     * PURPOSE: Execute one step of Pollard factorization (Selection sort analogue).
     * 
     * INPUTS:
     *   - divisors: array of FactorizationCell objects to search
     *   - semiprimeN: semiprime being factored (for logging)
     *   - stepCount: iteration number
     * 
     * OUTPUTS: divisors array potentially modified by:
     *   - Locking of best divisor as a factor (if remainder = 0)
     *   - Claiming cells (marking which algotype touched them)
     * 
     * LOGIC:
     *   1. Scan entire array to find cell with minimum remainder
     *   2. Skip cells that are already locked (frozen factors)
     *   3. Mark best cell as claimed by Pollard
     *   4. If remainder = 0, lock as factor; else mark for future attempts
     *   5. Record discovery step and algotype
     * 
     * INVARIANTS:
     *   - Locked cells are never processed (frozen)
     *   - Each step identifies exactly one minimum remainder cell
     *   - Pollard prioritizes smallest remainders (most likely factors)
     */
    @Override
    public void executeStep(FactorizationCell[] divisors, long semiprimeN, long stepCount) {
        Objects.requireNonNull(divisors, "Divisor array cannot be null");
        
        if (divisors.length == 0) {
            return;  // Nothing to process
        }
        
        // Find divisor with minimum remainder (that isn't already locked)
        int minIndex = -1;
        long minRemainder = Long.MAX_VALUE;
        
        for (int i = 0; i < divisors.length; i++) {
            FactorizationCell cell = divisors[i];
            
            // Skip locked factors (already processed)
            if (cell.isFactor()) {
                continue;
            }
            
            // Track minimum
            if (cell.getRemainder() < minRemainder) {
                minRemainder = cell.getRemainder();
                minIndex = i;
            }
        }
        
        // If all cells are locked, nothing to do
        if (minIndex == -1) {
            return;
        }
        
        FactorizationCell bestCell = divisors[minIndex];
        
        // Mark as claimed by Pollard
        bestCell.setClaimingAlgotype(NAME);
        
        // Lock if factor (remainder = 0)
        if (bestCell.getRemainder() == 0) {
            bestCell.lockAsFactor(NAME, stepCount);
            lastProcessedIndex = minIndex;
        }
    }
    
    /**
     * PURPOSE: Reset internal state for a new factorization trial.
     * 
     * LOGIC: Reset lastProcessedIndex to -1, allowing fresh scan
     */
    @Override
    public void reset() {
        this.lastProcessedIndex = -1;
    }
    
    /**
     * PURPOSE: Get algorithm name.
     * 
     * OUTPUTS: "Pollard"
     */
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String toString() {
        return String.format("PollardFactorizer{lastProcessedIndex=%d}", lastProcessedIndex);
    }
}
