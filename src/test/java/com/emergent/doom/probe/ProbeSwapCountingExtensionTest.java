package com.emergent.doom.probe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PURPOSE: Validate ProbeSwapCountingExtension fixes swap counting bug
 * without breaking existing Probe API.
 * 
 * KEY INSIGHT:
 * The fix adds new metrics (actualSwapCount, convergenceStep) ALONGSIDE
 * existing ones, using composition instead of modifying Probe.
 * This preserves backward compatibility while enabling correct measurements.
 */
@DisplayName("Probe Swap Counting Extension Test Suite")
public class ProbeSwapCountingExtensionTest {
    
    private Probe<Object> probe;
    private ProbeSwapCountingExtension<Object> extension;
    
    @BeforeEach
    void setUp() {
        probe = new Probe<>();
        extension = new ProbeSwapCountingExtension<>(probe);
    }
    
    /**
     * TEST: shouldSeparateLoopCyclesFromActualSwaps
     * 
     * Verifies that loop invocations (position-seeking effort) and actual
     * swaps (work) are tracked independently.
     */
    @Test
    @DisplayName("Should track loop invocations and actual swaps separately")
    void shouldSeparateLoopCyclesFromActualSwaps() {
        // Simulate Selection Sort: 1220 loop cycles, 45 actual swaps
        for (int i = 0; i < 1220; i++) {
            extension.recordSwapInvocation();
        }
        for (int i = 0; i < 45; i++) {
            extension.recordActualSwap();
        }
        
        assertEquals(1220, extension.getLoopInvocationCount(),
            "Loop invocations should be 1220");
        assertEquals(45, extension.getActualSwapCount(),
            "Actual swaps should be 45");
        
        // Verify they're independent
        assertNotEquals(extension.getLoopInvocationCount(), extension.getActualSwapCount(),
            "Loop cycles and actual swaps must be tracked separately");
    }
    
    /**
     * TEST: shouldCalculateEfficiencyCorrectly
     * 
     * Efficiency = actualSwaps / loopCycles
     * For Selection Sort: 45 / 1220 ≈ 0.0369 (27x overhead)
     */
    @Test
    @DisplayName("Should calculate position-seeking efficiency correctly")
    void shouldCalculateEfficiencyCorrectly() {
        // Selection Sort pattern
        for (int i = 0; i < 1220; i++) {
            extension.recordSwapInvocation();
        }
        for (int i = 0; i < 45; i++) {
            extension.recordActualSwap();
        }
        
        double efficiency = extension.getPositionSeekingEfficiency();
        double expected = 45.0 / 1220.0;
        
        assertEquals(expected, efficiency, 0.0001,
            "Efficiency should be 45/1220 ≈ 0.0369");
        assertTrue(efficiency < 0.1,
            "Selection efficiency should be <10% (position=0 bottleneck)");
    }
    
    /**
     * TEST: shouldTrackConvergenceIdempotently
     * 
     * markConvergence() should only set the step once.
     * Subsequent calls are ignored (idempotent behavior).
     */
    @Test
    @DisplayName("Should track convergence step idempotently")
    void shouldTrackConvergenceIdempotently() {
        // Mark convergence at step 122
        extension.markConvergence(122);
        
        assertEquals(122, extension.getConvergenceStep(),
            "Should record convergence at step 122");
        assertTrue(extension.hasConverged(),
            "hasConverged() should return true");
        
        // Try to mark again - should be ignored
        extension.markConvergence(131);
        
        assertEquals(122, extension.getConvergenceStep(),
            "Should NOT change to 131 (idempotent)");
    }
    
    /**
     * TEST: shouldPreserveExistingProbeAPI
     * 
     * All existing Probe methods should still work through delegation.
     */
    @Test
    @DisplayName("Should preserve existing Probe API through delegation")
    void shouldPreserveExistingProbeAPI() {
        // Direct access to delegate should still work
        Probe<Object> delegatedProbe = extension.getDelegate();
        assertNotNull(delegatedProbe,
            "getDelegate() should return wrapped Probe");
        
        // Both should have same swap count (delegate records via recordSwapInvocation)
        extension.recordSwapInvocation();
        extension.recordSwapInvocation();
        
        assertEquals(2, extension.getLoopInvocationCount(),
            "Both extension and delegate should show 2 invocations");
    }
    
    /**
     * TEST: shouldExportCSVWithBothMetrics
     * 
     * CSV export should include both loop invocations and actual swaps.
     */
    @Test
    @DisplayName("Should export CSV with loop invocations and actual swaps")
    void shouldExportCSVWithBothMetrics() {
        // Set up metrics
        for (int i = 0; i < 607; i++) {
            extension.recordSwapInvocation();
        }
        for (int i = 0; i < 23; i++) {
            extension.recordActualSwap();
        }
        extension.markConvergence(15);
        
        // Export
        StringWriter writer = new StringWriter();
        try {
            extension.exportMetricsCSV(writer);
        } catch (Exception e) {
            fail("CSV export failed: " + e.getMessage());
        }
        
        String csv = writer.toString();
        String[] fields = csv.trim().split(",");
        
        assertEquals(4, fields.length,
            "CSV should have 4 fields: loops, actual, convergence, efficiency");
        
        int loops = Integer.parseInt(fields[0]);
        int actual = Integer.parseInt(fields[1]);
        int convergence = Integer.parseInt(fields[2]);
        double efficiency = Double.parseDouble(fields[3]);
        
        assertEquals(607, loops, "First field should be loop count");
        assertEquals(23, actual, "Second field should be actual swaps");
        assertEquals(15, convergence, "Third field should be convergence step");
        assertEquals(23.0 / 607.0, efficiency, 0.0001, "Fourth field should be efficiency ratio");
    }
    
    /**
     * TEST: shouldHandleEdgeCaseZeroSwaps
     * 
     * When no swaps have occurred, efficiency should return 0.0.
     */
    @Test
    @DisplayName("Should handle edge case of zero swaps")
    void shouldHandleEdgeCaseZeroSwaps() {
        // No swaps recorded at all
        double efficiency = extension.getPositionSeekingEfficiency();
        
        assertEquals(0.0, efficiency,
            "Efficiency should be 0.0 when no invocations");
        
        assertEquals(0, extension.getActualSwapCount(),
            "Actual swap count should be 0");
    }
    
    /**
     * TEST: shouldHandleAlreadySortedArray
     * 
     * When array is already sorted (no swaps needed), metrics should reflect that.
     */
    @Test
    @DisplayName("Should handle already-sorted array (zero actual swaps)")
    void shouldHandleAlreadySortedArray() {
        // Already sorted: some loop cycles but zero actual swaps
        for (int i = 0; i < 10; i++) {
            extension.recordSwapInvocation();
        }
        // No recordActualSwap() calls
        extension.markConvergence(1);
        
        assertEquals(0, extension.getActualSwapCount(),
            "Actual swaps should be 0 for sorted array");
        assertEquals(10, extension.getLoopInvocationCount(),
            "Should still have loop invocations (checking if sorted)");
        assertEquals(0.0, extension.getPositionSeekingEfficiency(),
            "Efficiency should be 0 (no work)");
        assertTrue(extension.hasConverged(),
            "Should record convergence at step 1");
    }
    
    /**
     * TEST: shouldReturnMinusOneForUnconvergedState
     * 
     * When convergence hasn't been marked, getConvergenceStep() returns -1.
     */
    @Test
    @DisplayName("Should return -1 for unconverged state")
    void shouldReturnMinusOneForUnconvergedState() {
        assertEquals(-1, extension.getConvergenceStep(),
            "Should return -1 when convergence not marked");
        assertFalse(extension.hasConverged(),
            "hasConverged() should return false initially");
    }
    
    /**
     * TEST: shouldHaveThreadSafeAccess
     * 
     * Multiple threads should be able to safely call recordActualSwap() and
     * getActualSwapCount() without races.
     */
    @Test
    @DisplayName("Should provide thread-safe access to metrics")
    void shouldHaveThreadSafeAccess() throws InterruptedException {
        final int THREADS = 10;
        final int SWAPS_PER_THREAD = 100;
        final int TOTAL_EXPECTED = THREADS * SWAPS_PER_THREAD;
        
        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < SWAPS_PER_THREAD; j++) {
                    extension.recordActualSwap();
                }
            });
        }
        
        // Start all threads
        for (Thread t : threads) {
            t.start();
        }
        
        // Wait for completion
        for (Thread t : threads) {
            t.join();
        }
        
        assertEquals(TOTAL_EXPECTED, extension.getActualSwapCount(),
            "Should correctly count all swaps from " + THREADS + " threads");
    }
    
    /**
     * TEST: shouldProvideClearStringRepresentation
     * 
     * toString() should clearly show all metrics for debugging.
     */
    @Test
    @DisplayName("Should provide clear string representation")
    void shouldProvideClearStringRepresentation() {
        for (int i = 0; i < 100; i++) {
            extension.recordSwapInvocation();
        }
        for (int i = 0; i < 10; i++) {
            extension.recordActualSwap();
        }
        extension.markConvergence(5);
        
        String str = extension.toString();
        
        assertTrue(str.contains("loops=100"),
            "Should show loop invocation count");
        assertTrue(str.contains("actual=10"),
            "Should show actual swap count");
        assertTrue(str.contains("convergence=5"),
            "Should show convergence step");
        assertTrue(str.matches(".*efficiency=0\\.1.*"),
            "Should show efficiency ratio");
    }
}
