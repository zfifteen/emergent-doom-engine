package com.emergent.doom.probe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PURPOSE: Validate the fix for Selection Sort swap counting bug (27x overcounting).
 * 
 * ROOT CAUSE FIXED:
 *   Previously, swap() method invocations (position-seeking loop cycles) were
 *   conflated with actual element exchanges. In Selection Sort where all cells
 *   target position 0, this created massive contention resulting in ~1220 invocations
 *   vs ~45 actual exchanges.
 * 
 * CORRECTION:
 *   - recordSwapInvocation(): Tracks loop cycle attempts (position-seeking effort)
 *   - recordActualSwap(): Tracks element exchanges (algorithmic work)
 *   - markConvergence(): Tracks when array became sorted (convergence time)
 * 
 * IMPACT:
 *   Error tolerance calculations now use convergence time, not loop cycles:
 *   NEW: errorTolerance = (steps_frozen - steps_unfrozen) / steps_unfrozen
 *   OLD (WRONG): errorTolerance = (cycles_frozen - cycles_unfrozen) / cycles_unfrozen
 * 
 * @see Probe
 * @see StepSnapshot
 */
@DisplayName("Swap Counting Correction Test Suite")
public class SwapCountingCorrectionTest {
    
    private Probe probe;
    
    @BeforeEach
    void setUp() {
        probe = new Probe();
    }
    
    /**
     * TEST: shouldDistinguishLoopCyclesFromActualSwaps
     * 
     * PURPOSE:
     *   Verify that loop invocations and actual swaps are tracked separately,
     *   enabling correct efficiency metrics.
     * 
     * HYPOTHESIS:
     *   Probe should maintain independent counters for:
     *   1. recordSwapInvocation() - position-seeking attempts
     *   2. recordActualSwap() - element exchanges that occur
     * 
     * SCENARIO:
     *   Simulate Selection Sort on n=10 with position=0 bottleneck:
     *   - All cells initially target position 0
     *   - Only one cell can occupy position 0
     *   - Other 9 cells retry multiple times
     *   - Result: 1220 invocations vs 45 actual swaps
     * 
     * EXPECTED BEHAVIOR:
     *   getSwapCount() == getActualSwapCount() should be FALSE
     *   This proves we're measuring two different things.
     */
    @Test
    @DisplayName("Should distinguish position-seeking invocations from actual element exchanges")
    void shouldDistinguishLoopCyclesFromActualSwaps() {
        // Simulate ~122 cells × 10 position attempts
        for (int i = 0; i < 1220; i++) {
            probe.recordSwapInvocation();
        }
        
        // Only 45 actual element exchanges occurred
        for (int i = 0; i < 45; i++) {
            probe.recordActualSwap();
        }
        
        // These should be DIFFERENT
        assertEquals(1220, probe.getSwapCount(), 
            "Swap invocation count (loop cycles) should be 1220");
        assertEquals(45, probe.getActualSwapCount(), 
            "Actual swap count (element exchanges) should be 45");
        
        // Verify they're tracked independently
        assertNotEquals(probe.getSwapCount(), probe.getActualSwapCount(),
            "Loop cycles and actual swaps must be tracked separately");
    }
    
    /**
     * TEST: shouldCalculatePositionSeekingEfficiencyCorrectly
     * 
     * PURPOSE:
     *   Verify efficiency metric correctly reflects position-seeking overhead.
     * 
     * HYPOTHESIS:
     *   efficiency = actualSwaps / swapInvocations
     *   For Selection Sort bottleneck: 45 / 1220 ≈ 0.0369
     * 
     * INTERPRETATION:
     *   - 1.0 = Every attempt succeeded (ideal)
     *   - 0.5 = Half the attempts succeeded
     *   - 0.037 = Selection Sort with 27x overhead
     *   - 0.075 = Bubble Sort with 13.5x overhead
     * 
     * EXPECTED BEHAVIOR:
     *   getPositionSeekingEfficiency() should return ratio showing overhead.
     */
    @Test
    @DisplayName("Should calculate position-seeking efficiency as (actual / invocations)")
    void shouldCalculatePositionSeekingEfficiencyCorrectly() {
        // Selection Sort pattern: 1220 invocations, 45 actual
        for (int i = 0; i < 1220; i++) {
            probe.recordSwapInvocation();
        }
        for (int i = 0; i < 45; i++) {
            probe.recordActualSwap();
        }
        
        double efficiency = probe.getPositionSeekingEfficiency();
        double expected = 45.0 / 1220.0;  // ≈ 0.0369
        
        assertEquals(expected, efficiency, 0.0001,
            "Efficiency should be 45/1220 ≈ 0.0369");
        
        assertTrue(efficiency < 0.1,
            "Selection Sort efficiency should be very low (<10%) due to position=0 bottleneck");
    }
    
    /**
     * TEST: shouldTrackConvergenceStepForErrorToleranceCalculation
     * 
     * PURPOSE:
     *   Verify convergence step tracking enables correct error tolerance formula.
     * 
     * HYPOTHESIS:
     *   Old (WRONG): errorTolerance = (cycles_frozen - cycles_unfrozen) / cycles_unfrozen
     *   New (CORRECT): errorTolerance = (steps_frozen - steps_unfrozen) / steps_unfrozen
     * 
     * SCENARIO:
     *   Unfrozen: converges at step 122
     *   Frozen: converges at step 131 (4-8% slower)
     *   Error tolerance = (131 - 122) / 122 ≈ 0.0738 = 7.38%
     * 
     * EXPECTED BEHAVIOR:
     *   markConvergence(step) records when array became sorted.
     *   getConvergenceStep() retrieves that step number.
     */
    @Test
    @DisplayName("Should track convergence step for error tolerance calculations")
    void shouldTrackConvergenceStepForErrorToleranceCalculation() {
        // Unfrozen convergence
        Probe unfrozenProbe = new Probe();
        unfrozenProbe.markConvergence(122);
        
        assertEquals(122, unfrozenProbe.getConvergenceStep(),
            "Unfrozen should converge at step 122");
        assertTrue(unfrozenProbe.hasConverged(),
            "hasConverged() should return true after markConvergence()");
        
        // Frozen convergence (4-8% slower)
        Probe frozenProbe = new Probe();
        frozenProbe.markConvergence(131);
        
        assertEquals(131, frozenProbe.getConvergenceStep(),
            "Frozen should converge at step 131");
        
        // Calculate true error tolerance
        double errorTolerance = (131.0 - 122.0) / 122.0;
        assertEquals(0.0738, errorTolerance, 0.0001,
            "Error tolerance should be (131-122)/122 ≈ 7.38%");
    }
    
    /**
     * TEST: shouldValidateActualSwapsDoNotExceedTheoreticalMaximum
     * 
     * PURPOSE:
     *   Ensure data sanity: actual swaps can't exceed theoretical maximum.
     * 
     * HYPOTHESIS:
     *   For n=10 reverse-sorted array:
     *   - Maximum inversions = n*(n-1)/2 = 10*9/2 = 45
     *   - Any sorting algorithm needs AT MOST 45 swaps
     *   - StepSnapshot constructor should validate this
     * 
     * EXPECTED BEHAVIOR:
     *   Attempting to create snapshot with >45 actual swaps should fail.
     */
    @Test
    @DisplayName("Should validate actual swaps do not exceed theoretical maximum")
    void shouldValidateActualSwapsDoNotExceedTheoreticalMaximum() {
        // For n=10, theoretical max is 45 swaps
        final int N = 10;
        final int THEORETICAL_MAX = N * (N - 1) / 2;  // 45
        
        // Valid: 45 swaps
        try {
            new StepSnapshot<>(
                0,              // stepNumber
                1220,           // loopCycleCount
                THEORETICAL_MAX,// actualSwapCount = 45
                -1,             // convergenceStep
                java.util.List.of(),  // arrayValues
                N               // arraySize
            );
        } catch (IllegalArgumentException e) {
            fail("Should allow 45 swaps for n=10: " + e.getMessage());
        }
        
        // Invalid: 46 swaps (exceeds maximum)
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new StepSnapshot<>(
                0,              // stepNumber
                1220,           // loopCycleCount
                THEORETICAL_MAX + 1,  // actualSwapCount = 46 (INVALID)
                -1,             // convergenceStep
                java.util.List.of(),  // arrayValues
                N               // arraySize
            ),
            "Should reject 46 swaps for n=10"
        );
        
        assertTrue(
            exception.getMessage().contains("exceeds theoretical maximum"),
            "Error message should explain theoretical maximum exceeded"
        );
    }
    
    /**
     * TEST: shouldExportMetricsWithBothOldAndNewValues
     * 
     * PURPOSE:
     *   Verify CSV export contains both legacy and corrected metrics
     *   for backward compatibility and new analysis.
     * 
     * EXPECTED BEHAVIOR:
     *   CSV export should include:
     *   - swapInvocations (legacy, for comparison)
     *   - actualSwaps (correct metric)
     *   - convergenceStep (for error tolerance)
     *   - comparisons (general metric)
     *   - efficiency (ratio)
     */
    @Test
    @DisplayName("Should export metrics in CSV format with both old and new values")
    void shouldExportMetricsWithBothOldAndNewValues() {
        // Set up metrics
        for (int i = 0; i < 607; i++) {
            probe.recordSwapInvocation();
        }
        for (int i = 0; i < 23; i++) {
            probe.recordActualSwap();
        }
        for (int i = 0; i < 100; i++) {
            probe.recordComparison();
        }
        probe.markConvergence(15);
        
        // Export to CSV
        StringWriter writer = new StringWriter();
        try {
            probe.exportMetricsCSV(writer);
        } catch (Exception e) {
            fail("Export failed: " + e.getMessage());
        }
        
        String csv = writer.toString();
        
        // Verify CSV contains all metrics
        assertTrue(csv.contains("607"), "CSV should contain swapInvocations (607)");
        assertTrue(csv.contains("23"), "CSV should contain actualSwaps (23)");
        assertTrue(csv.contains("15"), "CSV should contain convergenceStep (15)");
        assertTrue(csv.contains("100"), "CSV should contain comparisons (100)");
        assertTrue(csv.matches(".*0\\.0.*"), "CSV should contain efficiency ratio");
    }
    
    /**
     * TEST: shouldResetAllMetricsForNextTrial
     * 
     * PURPOSE:
     *   Verify probe can be reset between trials.
     */
    @Test
    @DisplayName("Should reset all metrics for next trial")
    void shouldResetAllMetricsForNextTrial() {
        // Set up metrics
        probe.recordSwapInvocation();
        probe.recordActualSwap();
        probe.recordComparison();
        probe.markConvergence(10);
        
        assertEquals(1, probe.getSwapCount());
        assertEquals(1, probe.getActualSwapCount());
        assertEquals(10, probe.getConvergenceStep());
        
        // Reset
        probe.reset();
        
        // Verify all metrics cleared
        assertEquals(0, probe.getSwapCount());
        assertEquals(0, probe.getActualSwapCount());
        assertEquals(-1, probe.getConvergenceStep());
        assertFalse(probe.hasConverged());
    }
}
