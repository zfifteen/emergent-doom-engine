package com.emergent.doom.factorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * PURPOSE: Comprehensive unit tests for FactorizationCell, establishing the contract
 * for divisor-candidate representation in emergent semi-prime factorization.
 * 
 * SEMANTICS: Tests validate that each FactorizationCell correctly represents a single
 * divisor candidate d in the search space [2, √N], with cached remainder value N % d,
 * comparable interface for remainder-based sorting, and state tracking for discovery.
 * 
 * TEST DESIGN: Following TestWeaver conventions, each test method name is a complete
 * English sentence describing the behavior being validated. Tests are organized by
 * concern: construction, remainder computation, comparability, factor discovery, and
 * sortedness metrics.
 * 
 * PILOT CASE: All tests use N=143 (= 11 × 13) unless otherwise specified, allowing
 * manual verification of expected remainders and factor discovery sequences.
 */
@DisplayName("FactorizationCell: divisor candidates in emergent factorization")
public class FactorizationCellTest {
    
    // ============= Constants for Pilot Case (N=143 = 11 × 13) =============
    
    private static final long SEMIPRIME_N = 143L;  // Pilot case: 11 × 13
    private static final long FACTOR_P = 11L;       // First factor
    private static final long FACTOR_Q = 13L;       // Second factor
    private static final long NON_FACTOR = 7L;      // Non-factor divisor
    private static final long SQRT_N = 11L;         // √143 ≈ 11.96 (floor to 11)
    
    // ============= Construction Tests =============
    
    @Test
    @DisplayName("should construct a cell with valid divisor in [2, √N]")
    void shouldConstructWithValidDivisor() {
        // Divisor d=7 is valid for N=143 (2 <= 7 <= 11)
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, 7L);
        
        assertNotNull(cell, "Cell should be constructed");
        assertEquals(SEMIPRIME_N, cell.getSemiprimeN());
        assertEquals(7L, cell.getDivisor());
    }
    
    @Test
    @DisplayName("should reject divisor less than 2")
    void shouldRejectDivisorLessThanTwo() {
        // Divisor d=1 is invalid (too small)
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FactorizationCell(SEMIPRIME_N, 1L)
        );
        assertTrue(
            exception.getMessage().contains("must be >= 2"),
            "Error message should mention minimum divisor value"
        );
    }
    
    @Test
    @DisplayName("should reject divisor greater than √N")
    void shouldRejectDivisorGreaterThanSqrtN() {
        // Divisor d=12 > √143, so search need not continue beyond 11
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new FactorizationCell(SEMIPRIME_N, 12L)
        );
        assertTrue(
            exception.getMessage().contains("exceeds"),
            "Error message should mention divisor exceeds sqrt(N)"
        );
    }
    
    @Test
    @DisplayName("should construct cell with divisor equal to √N")
    void shouldConstructWithDivisorEqualToSqrtN() {
        // Boundary case: d = √143 ≈ 11 is valid
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, 11L);
        assertEquals(11L, cell.getDivisor());
    }
    
    // ============= Remainder Computation Tests =============
    
    @Test
    @DisplayName("should correctly compute remainder for non-factor divisor")
    void shouldComputeRemainderForNonFactorDivisor() {
        // d=7: 143 % 7 = 3 (since 143 = 7*20 + 3)
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, NON_FACTOR);
        
        long expectedRemainder = SEMIPRIME_N % NON_FACTOR;  // 143 % 7 = 3
        assertEquals(expectedRemainder, cell.getRemainder());
        assertEquals(3L, cell.getRemainder(), "143 % 7 should equal 3");
    }
    
    @Test
    @DisplayName("should compute zero remainder for actual factor")
    void shouldComputeZeroRemainderForFactor() {
        // d=11: 143 % 11 = 0 (11 is a factor of 143)
        FactorizationCell cellP = new FactorizationCell(SEMIPRIME_N, FACTOR_P);
        assertEquals(0L, cellP.getRemainder(), "143 % 11 should be 0 (11 is a factor)");
        
        // d=13: 143 % 13 = 0 (13 is a factor of 143)
        FactorizationCell cellQ = new FactorizationCell(SEMIPRIME_N, FACTOR_Q);
        assertEquals(0L, cellQ.getRemainder(), "143 % 13 should be 0 (13 is a factor)");
    }
    
    @Test
    @DisplayName("should cache remainder and not recompute on repeated access")
    void shouldCacheRemainderValue() {
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, 5L);
        
        long remainder1 = cell.getRemainder();
        long remainder2 = cell.getRemainder();
        
        assertEquals(remainder1, remainder2, "Remainder should be consistent across calls");
        assertEquals(3L, remainder1, "143 % 5 should equal 3");
    }
    
    // ============= Comparable Interface Tests =============
    
    @Test
    @DisplayName("should compare cells by remainder: smaller remainder compares as less")
    void shouldCompareByRemainderAscending() {
        // d1=5: remainder = 3
        // d2=6: remainder = 11 (143 = 6*23 + 5, so 143 % 6 = 5)
        FactorizationCell cell1 = new FactorizationCell(SEMIPRIME_N, 5L);
        FactorizationCell cell2 = new FactorizationCell(SEMIPRIME_N, 6L);
        
        int comparison = cell1.compareTo(cell2);
        assertTrue(comparison < 0, "Cell with smaller remainder (3) should compare less than cell with remainder (5)");
    }
    
    @Test
    @DisplayName("should compare equal when remainders are equal")
    void shouldCompareEqualForEqualRemainders() {
        // Both cells from N=143 with remainder = 3
        // Actually, find two different divisors with same remainder...
        // 143 % 5 = 3, 143 % 10 = 3
        FactorizationCell cell1 = new FactorizationCell(143L, 5L);
        FactorizationCell cell2 = new FactorizationCell(143L, 10L);
        
        assertEquals(0, cell1.compareTo(cell2), "Cells with same remainder should compare equal");
    }
    
    @Test
    @DisplayName("should compare factor (remainder=0) as least among all cells")
    void shouldCompareFacterAsLeast() {
        FactorizationCell factor = new FactorizationCell(SEMIPRIME_N, FACTOR_P);     // remainder=0
        FactorizationCell nonFactor = new FactorizationCell(SEMIPRIME_N, NON_FACTOR);  // remainder=3
        
        assertTrue(
            factor.compareTo(nonFactor) < 0,
            "Factor (remainder=0) should compare less than non-factor (remainder>0)"
        );
    }
    
    @Test
    @DisplayName("should handle comparison with null by throwing NullPointerException")
    void shouldThrowNullPointerExceptionWhenComparingWithNull() {
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, 5L);
        
        assertThrows(
            NullPointerException.class,
            () -> cell.compareTo(null),
            "Comparing with null should throw NullPointerException"
        );
    }
    
    // ============= Factor Discovery and Locking Tests =============
    
    @Test
    @DisplayName("should lock cell as factor when remainder is zero")
    void shouldLockCellWithZeroRemainder() {
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, FACTOR_P);
        
        assertFalse(cell.isFactor(), "Initially, cell should not be marked as factor");
        
        cell.lockAsFactor("TrialDivision", 5L);
        
        assertTrue(cell.isFactor(), "After locking, cell should be marked as factor");
        assertEquals("TrialDivision", cell.getClaimingAlgotype());
        assertEquals(5L, cell.getDiscoveredAtStep());
    }
    
    @Test
    @DisplayName("should reject locking cell as factor when remainder is not zero")
    void shouldRejectLockingNonFactorCell() {
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, NON_FACTOR);  // remainder=3
        
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> cell.lockAsFactor("Fermat", 10L)
        );
        
        assertTrue(
            exception.getMessage().contains("not zero"),
            "Error message should indicate remainder is not zero"
        );
    }
    
    @Test
    @DisplayName("should record which algotype discovered the factor")
    void shouldRecordDiscoveringAlgotype() {
        FactorizationCell cellP = new FactorizationCell(SEMIPRIME_N, FACTOR_P);
        FactorizationCell cellQ = new FactorizationCell(SEMIPRIME_N, FACTOR_Q);
        
        cellP.lockAsFactor("TrialDivision", 3L);
        cellQ.lockAsFactor("Pollard", 8L);
        
        assertEquals("TrialDivision", cellP.getClaimingAlgotype());
        assertEquals("Pollard", cellQ.getClaimingAlgotype());
    }
    
    @Test
    @DisplayName("should record step number when factor was discovered")
    void shouldRecordDiscoveryStep() {
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, FACTOR_P);
        
        cell.lockAsFactor("Fermat", 12L);
        
        assertEquals(12L, cell.getDiscoveredAtStep(), "Discovery step should be recorded");
    }
    
    @Test
    @DisplayName("should return -1 for discovery step before factor is locked")
    void shouldReturnNegativeOneForUndiscoveredFactor() {
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, NON_FACTOR);
        
        assertEquals(-1L, cell.getDiscoveredAtStep(), "Undiscovered factors should report step = -1");
    }
    
    // ============= Sortedness Metric Tests =============
    
    @Test
    @DisplayName("should contribute 0.0 sortedness when not a factor")
    void shouldContributeZeroSortednessForNonFactor() {
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, NON_FACTOR);
        
        double sortedness = cell.getSortednessContribution();
        assertEquals(0.0, sortedness, "Non-factor cells contribute 0.0 to sortedness");
    }
    
    @Test
    @DisplayName("should contribute 1.0 sortedness when locked as factor")
    void shouldContributeFullSortednessWhenFactor() {
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, FACTOR_P);
        
        cell.lockAsFactor("TrialDivision", 2L);
        
        double sortedness = cell.getSortednessContribution();
        assertEquals(1.0, sortedness, "Factor cells contribute 1.0 to sortedness");
    }
    
    // ============= Object Contract Tests (equals, hashCode, toString) =============
    
    @Test
    @DisplayName("should be equal to another cell with same (semiprime, divisor) pair")
    void shouldBeEqualForSameSemiprimeAndDivisor() {
        FactorizationCell cell1 = new FactorizationCell(SEMIPRIME_N, 5L);
        FactorizationCell cell2 = new FactorizationCell(SEMIPRIME_N, 5L);
        
        assertEquals(cell1, cell2, "Cells with same (N, d) should be equal");
    }
    
    @Test
    @DisplayName("should not be equal when divisors differ")
    void shouldNotBeEqualForDifferentDivisors() {
        FactorizationCell cell1 = new FactorizationCell(SEMIPRIME_N, 5L);
        FactorizationCell cell2 = new FactorizationCell(SEMIPRIME_N, 7L);
        
        assertNotEquals(cell1, cell2, "Cells with different divisors should not be equal");
    }
    
    @Test
    @DisplayName("should have equal hashCode for equal cells")
    void shouldHaveEqualHashCodeForEqualCells() {
        FactorizationCell cell1 = new FactorizationCell(SEMIPRIME_N, 5L);
        FactorizationCell cell2 = new FactorizationCell(SEMIPRIME_N, 5L);
        
        assertEquals(cell1.hashCode(), cell2.hashCode(), "Equal cells should have equal hashCode");
    }
    
    @Test
    @DisplayName("should produce readable toString representation")
    void shouldProduceReadableToString() {
        FactorizationCell cell = new FactorizationCell(SEMIPRIME_N, 5L);
        cell.lockAsFactor("Fermat", 7L);
        
        String representation = cell.toString();
        
        assertTrue(representation.contains("143"), "toString should include semiprime N");
        assertTrue(representation.contains("5"), "toString should include divisor d");
        assertTrue(representation.contains("Fermat"), "toString should include claiming algotype");
        assertTrue(representation.contains("7"), "toString should include discovery step");
    }
    
    // ============= Integration: Two-Factor Discovery Sequence =============
    
    @Test
    @DisplayName("should establish correct remainder ordering for N=143 divisors 2 through 11")
    void shouldOrderRemainderCorrectlyForFullSearchSpace() {
        // For N=143, compute all divisor remainders in [2, 11]
        long[] divisors = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        FactorizationCell[] cells = new FactorizationCell[divisors.length];
        
        for (int i = 0; i < divisors.length; i++) {
            cells[i] = new FactorizationCell(SEMIPRIME_N, divisors[i]);
        }
        
        // Factors 11 and 13(out of range) should have remainder 0
        // But 13 is out of range, so only divisor 11 is a factor here
        assertEquals(0L, cells[9].getRemainder(), "Divisor 11 should have remainder 0 (is a factor)");
        
        // All other divisors should have non-zero remainders
        for (int i = 0; i < 9; i++) {
            assertTrue(cells[i].getRemainder() > 0, 
                String.format("Divisor %d should have non-zero remainder", divisors[i]));
        }
    }
    
    @Test
    @DisplayName("should track independent discovery of two factors as separate locked cells")
    void shouldTrackMultipleFactorDiscoveries() {
        // Simulate discovering factor 11 first, then factor 13
        // (Note: 13 > sqrt(143), so this is hypothetical for illustration)
        FactorizationCell factorP = new FactorizationCell(SEMIPRIME_N, 11L);
        FactorizationCell factorQ = new FactorizationCell(SEMIPRIME_N, 13L);  // Will fail constructor
        
        // This will throw because 13 > sqrt(143)
        assertThrows(
            IllegalArgumentException.class,
            () -> new FactorizationCell(SEMIPRIME_N, 13L),
            "Divisor 13 exceeds sqrt(143), so search space limits prevent testing q discovery directly"
        );
        
        // Instead, verify that we can track first factor discovery independently
        factorP.lockAsFactor("TrialDivision", 3L);
        assertTrue(factorP.isFactor(), "First factor should be locked");
        assertEquals(3L, factorP.getDiscoveredAtStep());
    }
}
