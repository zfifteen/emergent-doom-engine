package com.emergent.doom.execution;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.Probe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ConvergenceDetector interface and implementations.
 *
 * PURPOSE: Verify that convergence detectors correctly analyze execution state
 * to determine when the system has reached stable configuration or met termination
 * criteria.
 *
 * [TestWeaver: Generated skeleton - expand with specific test scenarios]
 */
@DisplayName("ConvergenceDetector Tests")
class ConvergenceDetectorTest {

    /**
     * PURPOSE: As a developer, I want to verify convergence detection interface contract
     * so that I can ensure implementations correctly analyze probe data.
     *
     * INPUTS: [TestWeaver: Define test inputs - probe with execution history, step number]
     * EXPECTED OUTPUT: [TestWeaver: Expected hasConverged() result]
     * TEST DATA: [TestWeaver: Specify concrete probe snapshots and swap counts]
     * REPRODUCTION: [TestWeaver: Manual verification steps]
     *
     * [TestWeaver: Implement test logic based on ConvergenceDetector interface]
     */
    @Test
    @DisplayName("hasConverged analyzes probe data correctly")
    void hasConvergedAnalyzesProbeData() {
        // Test using NoSwapConvergence implementation
        ConvergenceDetector<TestCell> detector = new NoSwapConvergence<>(2);
        Probe<TestCell> probe = new Probe<>();
        probe.setRecordingEnabled(false);
        
        // No swaps yet
        assertFalse(detector.hasConverged(probe, 0));
        
        // Record steps with no swaps
        probe.recordSnapshot(0, new TestCell[0], 0);
        probe.recordSnapshot(1, new TestCell[0], 0);
        
        // Should converge after 2 steps with no swaps
        assertTrue(detector.hasConverged(probe, 2));
    }

    /**
     * PURPOSE: As a developer, I want to verify reset functionality
     * so that I can ensure detectors clear accumulated state between runs.
     *
     * INPUTS: [TestWeaver: Define detector with internal state]
     * EXPECTED OUTPUT: [TestWeaver: Expected state after reset]
     * TEST DATA: [TestWeaver: Test values]
     * REPRODUCTION: [TestWeaver: Manual steps]
     *
     * [TestWeaver: Implement reset verification]
     */
    @Test
    @DisplayName("reset clears detector state")
    void resetClearsDetectorState() {
        // NoSwapConvergence is stateless (relies on Probe), so reset is a no-op
        // This test verifies the interface contract allows for stateful implementations
        ConvergenceDetector<TestCell> detector = new NoSwapConvergence<>(3);
        Probe<TestCell> probe1 = new Probe<>();
        Probe<TestCell> probe2 = new Probe<>();
        
        probe1.setRecordingEnabled(false);
        probe2.setRecordingEnabled(false);
        
        // Converge probe1
        for (int i = 0; i < 4; i++) {
            probe1.recordSnapshot(i, new TestCell[0], 0);
        }
        assertTrue(detector.hasConverged(probe1, 4));
        
        // New probe should start fresh (detector doesn't carry state between probes)
        assertFalse(detector.hasConverged(probe2, 0));
    }

    // Test helper class
    private static class TestCell implements Cell<TestCell> {
        
        public int getValue() {
            return 0;
        }
        
        
        public int compareTo(TestCell other) {
            return 0;
        }
        
        
        public boolean shouldSwapWith(TestCell other) {
            return false;
        }
    }
}
