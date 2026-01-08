package com.emergent.doom.factorization;

import java.util.Objects;

/**
 * PURPOSE: Implement Fermat-inspired factorization as an Insertion sort analogue.
 * 
 * SEMANTICS: Fermat's method conceptually assumes N = a² - b² = (a-b)(a+b).
 * For balanced semiprimes (p ≈ q), factors are near √N. This translates to
 * EDE's Insertion sort: scan for candidates with special properties (close to √N)
 * and shift them forward (give them higher priority).
 * 
 * STRATEGY:
 *   1. Calculate √N (the pivot)
 *   2. For each divisor, calculate distance from √N
 *   3. Prioritize divisors close to √N (small distance)
 *   4. Move promising divisors toward front of array
 *   5. When remainder = 0, lock as factor
 * 
 * EMERGENT PROPERTIES:
 *   - HEURISTIC-BASED: Uses mathematical insight (factors near √N for balanced N)
 *   - FAST for balanced: Excellent when p ≈ q (e.g., N = 437 = 19×23)
 *   - SLOW for skewed: Poor when p << q (e.g., N = 221 = 13×17)
 *   - SPECIALIZATION: Shows algorithmic trade-off (good for one case, bad for another)
 * 
 * EDE ANALOGUE: Insertion sort
 *   - Cell = divisor candidate
 *   - Value = remainder (sortedness = remainder ≈ 0)
 *   - Operation = shift promising candidates (near √N) forward
 *   - Result = best heuristic candidates get early priority
 * 
 * MATHEMATICAL INSIGHT:
 * For semiprime N = p × q with p < q:
 *   - If factors are balanced (p ≈ √N ≈ q), both are near √N
 *   - Example: N = 437 = 19 × 23, √437 ≈ 20.9
 *     Divisors 19 and 23 are very close to √N
 *   - Example: N = 221 = 13 × 17, √221 ≈ 14.9
 *     Divisor 13 is close, but divisor 17 > √221, out of range
 * 
 * USER STORY:
 * As a factorization researcher, I want Fermat-inspired factorization
 * so that I can test whether heuristic-based strategies outperform exhaustive search
 * on balanced semiprimes, and observe specialization effects in chimeric mode.
 * 
 * INPUTS: FactorizationCell array, semiprime N, step count
 * OUTPUTS: Array with promising cells shifted toward front, potentially locked factors
 * LOGIC: Track current position; shift cells with distance < threshold toward front;
 * lock factors when found
 */
public class FermatFactorizer implements FactorizationAlgotype {
    
    /** Algorithm name for logging and metrics. */
    private static final String NAME = "Fermat";
    
    /**
     * Distance threshold from √N. Divisors closer than this are considered "promising".
     * Can be tuned for different problem classes.
     */
    private static final long FERMAT_THRESHOLD = 5L;
    
    /**
     * Current position for rotation through the array.
     * Maintains state across multiple calls to executeStep().
     */
    private int currentPosition;
    
    /**
     * CONSTRUCTOR.
     * 
     * PURPOSE: Initialize Fermat factorizer.
     * LOGIC: Set position to 0
     */
    public FermatFactorizer() {
        this.currentPosition = 0;
    }
    
    /**
     * PURPOSE: Calculate distance of divisor from √N (for Fermat heuristic).
     * 
     * INPUTS: divisor d, semiprime N
     * OUTPUTS: |d - √N| (absolute distance from divisor to square root)
     * LOGIC: Math.sqrt() followed by absolute difference
     * 
     * @param divisor the candidate divisor
     * @param semiprimeN the semiprime
     * @return absolute distance from √N
     */
    private long distanceFromSqrtN(long divisor, long semiprimeN) {
        long sqrtN = (long) Math.sqrt((double) semiprimeN);
        return Math.abs(divisor - sqrtN);
    }
    
    /**
     * PURPOSE: Check if divisor is "promising" for Fermat's method.
     * 
     * LOGIC: A divisor is promising if it is close to √N (distance < threshold).
     * This heuristic works well for balanced semiprimes.
     * 
     * @param divisor the candidate divisor
     * @param semiprimeN the semiprime
     * @return true if divisor is within FERMAT_THRESHOLD of √N
     */
    private boolean isPromising(long divisor, long semiprimeN) {
        return distanceFromSqrtN(divisor, semiprimeN) < FERMAT_THRESHOLD;
    }
    
    /**
     * PURPOSE: Execute one step of Fermat factorization (Insertion sort analogue).
     * 
     * INPUTS:
     *   - divisors: array of FactorizationCell objects to search
     *   - semiprimeN: semiprime being factored
     *   - stepCount: iteration number
     * 
     * OUTPUTS: divisors array potentially modified by:
     *   - Shifting a promising cell toward front (higher priority)
     *   - Locking a factor (if remainder = 0)
     *   - Claiming cells (marking which algotype touched them)
     * 
     * LOGIC:
     *   1. Get current position i in array
     *   2. If i >= array.length, reset to 0 (wrap around)
     *   3. Check if cell at i is promising (near √N)
     *   4. If promising and not locked, shift it toward front (swap with earlier position)
     *   5. Mark both cells as claimed by Fermat
     *   6. Lock any factor (remainder = 0) found
     *   7. Increment position for next step
     * 
     * SHIFT MECHANISM:
     * When a promising divisor is found, bubble it backward (toward position 0)
     * repeatedly until it reaches its appropriate position by distance from √N.
     * This gives Fermat candidates early opportunities to be tested.
     * 
     * INVARIANTS:
     *   - No locked cells are disturbed
     *   - Promising cells naturally move toward front
     *   - Factors are locked with step number and algotype recorded
     */
    @Override
    public void executeStep(FactorizationCell[] divisors, long semiprimeN, long stepCount) {
        Objects.requireNonNull(divisors, "Divisor array cannot be null");
        
        if (divisors.length == 0) {
            return;  // Nothing to process
        }
        
        // Wrap position around
        if (currentPosition >= divisors.length) {
            currentPosition = 0;
        }
        
        int i = currentPosition;
        FactorizationCell cell = divisors[i];
        
        // Don't disturb locked factors
        if (cell.isFactor()) {
            currentPosition++;
            return;
        }
        
        // Check if this cell is promising (near √N)
        if (isPromising(cell.getDivisor(), semiprimeN)) {
            // Shift promising cell toward front (bubble backward)
            // Find first position where a non-locked, non-promising cell sits
            int insertPos = i;
            while (insertPos > 0 && !divisors[insertPos - 1].isFactor()) {
                FactorizationCell prevCell = divisors[insertPos - 1];
                // Only swap if previous is not as promising
                if (!isPromising(prevCell.getDivisor(), semiprimeN)) {
                    // Swap
                    divisors[insertPos] = prevCell;
                    divisors[insertPos - 1] = cell;
                    insertPos--;
                } else {
                    break;  // Previous is also promising; stop shifting
                }
            }
            
            // Mark cells as claimed
            cell.setClaimingAlgotype(NAME);
            divisors[insertPos].setClaimingAlgotype(NAME);
        } else {
            // Not promising; mark as claimed for tracking
            cell.setClaimingAlgotype(NAME);
        }
        
        // Lock any factor found
        if (cell.getRemainder() == 0 && !cell.isFactor()) {
            cell.lockAsFactor(NAME, stepCount);
        }
        
        // Move to next position
        currentPosition++;
    }
    
    /**
     * PURPOSE: Reset internal state for a new factorization trial.
     * 
     * LOGIC: Reset position to 0
     */
    @Override
    public void reset() {
        this.currentPosition = 0;
    }
    
    /**
     * PURPOSE: Get algorithm name.
     * 
     * OUTPUTS: "Fermat"
     */
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String toString() {
        return String.format("FermatFactorizer{position=%d, threshold=%d}", 
            currentPosition, FERMAT_THRESHOLD);
    }
}
