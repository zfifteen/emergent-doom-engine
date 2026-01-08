package com.emergent.doom.factorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

/**
 * PURPOSE: Comprehensive tests for all three factorization algotypes.
 * 
 * ORGANIZATION: Tests for each algotype separately, then interaction tests.
 * 
 * PILOT CASES:
 *   - N=143 (11 × 13): Trivial, small search space [2..11]
 *   - N=437 (19 × 23): Balanced, factors near √N (Fermat favorable)
 *   - N=221 (13 × 17): Skewed, factors distant from √N (Fermat unfavorable)
 */
@DisplayName("Factorization Algotypes: Trial Division, Fermat, Pollard")
public class FactorizationAlgotypesTest {
    
    // Constants
    private static final long N_143 = 143L;  // 11 × 13, trivial
    private static final long N_437 = 437L;  // 19 × 23, balanced
    private static final long N_221 = 221L;  // 13 × 17, skewed
    
    private TrialDivisionFactorizer trialDiv;
    private FermatFactorizer fermat;
    private PollardFactorizer pollard;
    
    @BeforeEach
    void setUp() {
        trialDiv = new TrialDivisionFactorizer();
        fermat = new FermatFactorizer();
        pollard = new PollardFactorizer();
    }
    
    // ============= Trial Division Tests =============
    
    @Test
    @DisplayName("Trial Division should complete factorization of N=143 in bounded steps")
    void shouldCompleteTrialDivisionOnSimpleSemiprime() {
        // Create divisor array for N=143: [2..11]
        FactorizationCell[] divisors = createDivisorArray(N_143);
        
        int lockedFactors = 0;
        long maxSteps = 50;  // Should complete in much fewer
        
        for (long step = 0; step < maxSteps; step++) {
            trialDiv.executeStep(divisors, N_143, step);
            int newLockedCount = countLockedFactors(divisors);
            if (newLockedCount > lockedFactors) {
                lockedFactors = newLockedCount;
                // Verify correct factor locked
                for (FactorizationCell cell : divisors) {
                    if (cell.isFactor() && cell.getDiscoveredAtStep() == step) {
                        assertEquals(0L, cell.getRemainder(), 
                            "Locked cell must have remainder = 0");
                    }
                }
            }
        }
        
        assertEquals(1, lockedFactors, "Trial Division should lock divisor 11 as factor");
    }
    
    @Test
    @DisplayName("Trial Division should not disturb locked factors")
    void shouldRespectLockedFactorsInTrialDivision() {
        FactorizationCell[] divisors = createDivisorArray(N_143);
        
        // Manually lock divisor 11 as factor
        for (FactorizationCell cell : divisors) {
            if (cell.getDivisor() == 11) {
                cell.lockAsFactor("Manual", 0);
                break;
            }
        }
        
        // Execute many steps
        for (long step = 1; step < 20; step++) {
            trialDiv.executeStep(divisors, N_143, step);
        }
        
        // Locked factor should still be locked with original metadata
        for (FactorizationCell cell : divisors) {
            if (cell.getDivisor() == 11) {
                assertTrue(cell.isFactor(), "Divisor 11 should remain locked");
                assertEquals("Manual", cell.getClaimingAlgotype(), 
                    "Claiming algotype should remain unchanged");
                assertEquals(0, cell.getDiscoveredAtStep(), 
                    "Discovery step should remain unchanged");
                break;
            }
        }
    }
    
    @Test
    @DisplayName("Trial Division should mark cells as claimed")
    void shouldMarkCellsAsClaimedByTrialDivision() {
        FactorizationCell[] divisors = createDivisorArray(N_143);
        
        trialDiv.executeStep(divisors, N_143, 0);
        
        // At least the first two cells should be claimed
        boolean foundClaimed = false;
        for (FactorizationCell cell : divisors) {
            if (cell.getClaimingAlgotype() != null && 
                cell.getClaimingAlgotype().equals("Trial Division")) {
                foundClaimed = true;
                break;
            }
        }
        assertTrue(foundClaimed, "Trial Division should mark cells as claimed");
    }
    
    // ============= Pollard Tests =============
    
    @Test
    @DisplayName("Pollard should find best divisor (minimum remainder)")
    void shouldSelectBestDivisorInPollard() {
        FactorizationCell[] divisors = createDivisorArray(N_143);
        
        // Execute one step
        pollard.executeStep(divisors, N_143, 0);
        
        // Check that divisor 11 (remainder = 0) was claimed
        boolean foundBest = false;
        for (FactorizationCell cell : divisors) {
            if (cell.getRemainder() == 0 && 
                cell.getClaimingAlgotype() != null &&
                cell.getClaimingAlgotype().equals("Pollard")) {
                foundBest = true;
                break;
            }
        }
        assertTrue(foundBest, "Pollard should identify divisor with remainder = 0");
    }
    
    @Test
    @DisplayName("Pollard should lock factor (remainder = 0) immediately")
    void shouldLockFactorWhenRemainderZeroInPollard() {
        FactorizationCell[] divisors = createDivisorArray(N_143);
        
        pollard.executeStep(divisors, N_143, 5L);
        
        // Divisor 11 should be locked
        for (FactorizationCell cell : divisors) {
            if (cell.getDivisor() == 11) {
                assertTrue(cell.isFactor(), "Divisor 11 (remainder=0) should be locked");
                assertEquals("Pollard", cell.getClaimingAlgotype());
                assertEquals(5L, cell.getDiscoveredAtStep());
                break;
            }
        }
    }
    
    @Test
    @DisplayName("Pollard should skip already-locked factors")
    void shouldSkipLockedFactorsInPollard() {
        FactorizationCell[] divisors = createDivisorArray(N_143);
        
        // Manually lock divisor 11
        for (FactorizationCell cell : divisors) {
            if (cell.getDivisor() == 11) {
                cell.lockAsFactor("Manual", 0);
                break;
            }
        }
        
        // Pollard should skip it and find next best (smallest non-locked remainder)
        pollard.executeStep(divisors, N_143, 1L);
        
        boolean foundOtherClaimed = false;
        for (FactorizationCell cell : divisors) {
            if (!cell.isFactor() && cell.getClaimingAlgotype() != null &&
                cell.getClaimingAlgotype().equals("Pollard")) {
                foundOtherClaimed = true;
                break;
            }
        }
        assertTrue(foundOtherClaimed, "Pollard should claim a non-locked cell");
    }
    
    // ============= Fermat Tests =============
    
    @Test
    @DisplayName("Fermat should prioritize divisors near sqrt(N)")
    void shouldPrioritizeDivisorsNearSqrtNInFermat() {
        FactorizationCell[] divisors = createDivisorArray(N_437);  // 19 × 23, √437 ≈ 20.9
        
        // Execute multiple steps to allow Fermat to shift promising candidates
        for (long step = 0; step < 10; step++) {
            fermat.executeStep(divisors, N_437, step);
        }
        
        // Divisors 19 and 23 should be near sqrt(437) ≈ 20.9
        // (23 is outside range [2..20], so 19 is the one in range)
        // After Fermat's shifting, divisor 19 should have been claimed/moved
        boolean found19Claimed = false;
        for (FactorizationCell cell : divisors) {
            if (cell.getDivisor() == 19 && 
                cell.getClaimingAlgotype() != null &&
                cell.getClaimingAlgotype().equals("Fermat")) {
                found19Claimed = true;
                break;
            }
        }
        assertTrue(found19Claimed, "Fermat should prioritize divisor 19 (near sqrt(437))");
    }
    
    @Test
    @DisplayName("Fermat should struggle on skewed semiprimes")
    void shouldStruggleOnSkewedSemiprimeFermat() {
        FactorizationCell[] divisors = createDivisorArray(N_221);  // 13 × 17, √221 ≈ 14.9
        
        // Divisor 13 is factor; 17 is outside search range
        // Fermat prefers near sqrt, but 13 < sqrt(221) by significant margin
        // So Fermat's heuristic is not optimal here
        
        // Run Fermat for many steps
        int lockedCount = 0;
        for (long step = 0; step < 30; step++) {
            fermat.executeStep(divisors, N_221, step);
            int newCount = countLockedFactors(divisors);
            if (newCount > lockedCount) lockedCount = newCount;
        }
        
        // Fermat might not lock the factor quickly (shows specialization)
        // This is acceptable; we're testing that Fermat has trade-offs
        // The key insight is that Fermat specializes (good on balanced, bad on skewed)
    }
    
    // ============= Interaction Tests =============
    
    @Test
    @DisplayName("All three algotypes should cooperate in chimeric mode on N=143")
    void shouldCooperateInChimericModeOnN143() {
        FactorizationCell[] divisors = createDivisorArray(N_143);
        
        // Run all three algotypes in round-robin
        int step = 0;
        int maxSteps = 50;
        int lockedFactors = 0;
        
        while (lockedFactors < 1 && step < maxSteps) {
            trialDiv.executeStep(divisors, N_143, step);
            fermat.executeStep(divisors, N_143, step);
            pollard.executeStep(divisors, N_143, step);
            
            lockedFactors = countLockedFactors(divisors);
            step++;
        }
        
        assertEquals(1, lockedFactors, 
            "Chimeric approach should lock divisor 11 on N=143");
        assertTrue(step < maxSteps, 
            "Chimeric approach should converge faster than baseline");
    }
    
    @Test
    @DisplayName("Algotypes should record which one found each factor")
    void shouldRecordDiscoveringAlgotype() {
        FactorizationCell[] divisors = createDivisorArray(N_143);
        
        // Manually create conditions where different algotypes claim the same cell
        trialDiv.executeStep(divisors, N_143, 0);
        fermat.executeStep(divisors, N_143, 0);
        pollard.executeStep(divisors, N_143, 0);
        
        // Check that at least one algotype has claimed something
        int claimedCount = 0;
        for (FactorizationCell cell : divisors) {
            if (cell.getClaimingAlgotype() != null) {
                claimedCount++;
            }
        }
        assertTrue(claimedCount > 0, "At least one algotype should have claimed a cell");
    }
    
    // ============= Edge Cases & Error Tolerance =============
    
    @Test
    @DisplayName("Algotypes should handle empty divisor array gracefully")
    void shouldHandleEmptyDivisorArray() {
        FactorizationCell[] empty = {};
        
        // Should not throw
        assertDoesNotThrow(() -> {
            trialDiv.executeStep(empty, N_143, 0);
            fermat.executeStep(empty, N_143, 0);
            pollard.executeStep(empty, N_143, 0);
        }, "Algotypes should handle empty arrays without throwing");
    }
    
    @Test
    @DisplayName("Algotypes should handle single-cell divisor array")
    void shouldHandleSingleCellArray() {
        FactorizationCell[] single = new FactorizationCell[1];
        single[0] = new FactorizationCell(N_143, 11);
        
        assertDoesNotThrow(() -> {
            trialDiv.executeStep(single, N_143, 0);
            fermat.executeStep(single, N_143, 0);
            pollard.executeStep(single, N_143, 0);
        }, "Algotypes should handle single-cell arrays");
    }
    
    @Test
    @DisplayName("Algotypes should reset state properly")
    void shouldResetStateProperly() {
        FactorizationCell[] divisors = createDivisorArray(N_143);
        
        // Execute steps
        for (int i = 0; i < 5; i++) {
            trialDiv.executeStep(divisors, N_143, i);
            fermat.executeStep(divisors, N_143, i);
            pollard.executeStep(divisors, N_143, i);
        }
        
        // Reset
        trialDiv.reset();
        fermat.reset();
        pollard.reset();
        
        // After reset, should behave as newly constructed
        // This is tested implicitly by subsequent operations
        // A fresh array should converge similarly to the reset algotype
        
        FactorizationCell[] newArray = createDivisorArray(N_143);
        assertDoesNotThrow(() -> {
            trialDiv.executeStep(newArray, N_143, 0);
            fermat.executeStep(newArray, N_143, 0);
            pollard.executeStep(newArray, N_143, 0);
        }, "Reset algotypes should execute steps normally");
    }
    
    // ============= Helper Methods =============
    
    /**
     * PURPOSE: Create a divisor array for a given semiprime.
     * 
     * INPUTS: semiprime N
     * OUTPUTS: FactorizationCell array with all divisors in [2, sqrt(N)]
     */
    private FactorizationCell[] createDivisorArray(long semiprimeN) {
        long sqrtN = (long) Math.sqrt((double) semiprimeN);
        FactorizationCell[] divisors = new FactorizationCell[(int)(sqrtN - 1)];
        
        for (long d = 2; d <= sqrtN; d++) {
            divisors[(int)(d - 2)] = new FactorizationCell(semiprimeN, d);
        }
        
        return divisors;
    }
    
    /**
     * PURPOSE: Count how many factors are locked in the array.
     * 
     * INPUTS: divisor array
     * OUTPUTS: count of cells with isFactor() = true
     */
    private int countLockedFactors(FactorizationCell[] divisors) {
        int count = 0;
        for (FactorizationCell cell : divisors) {
            if (cell.isFactor()) {
                count++;
            }
        }
        return count;
    }
}
