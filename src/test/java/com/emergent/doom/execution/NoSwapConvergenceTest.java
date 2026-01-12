package com.emergent.doom.execution;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.Probe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for NoSwapConvergence.
 *
 * PURPOSE: Verify that NoSwapConvergence correctly detects convergence when
 * no swaps occur for N consecutive steps, using probe.getStepsSinceLastSwap().
 *
 * [TestWeaver: Generated skeleton - expand with specific test scenarios]
 */
@DisplayName("NoSwapConvergence Tests")
class NoSwapConvergenceTest {

    private NoSwapConvergence<TestCell> detector;

    @BeforeEach
    void setUp() {
        detector = new NoSwapConvergence<>(3); // Require 3 stable steps
    }

    /**
     * PURPOSE: As a developer, I want to verify convergence after N stable steps
     * so that I can confirm the detector correctly identifies execution completion.
     *
     * INPUTS: [TestWeaver: Define test inputs - probe with N steps since last swap]
     * EXPECTED OUTPUT: [TestWeaver: hasConverged() returns true]
     * TEST DATA: [TestWeaver: Specify required stable steps = 3, actual = 3 or more]
     * REPRODUCTION: [TestWeaver: Manual verification steps]
     *
     * [TestWeaver: Implement test logic based on NoSwapConvergence API]
     */
    @Test
    @DisplayName("hasConverged returns true after N stable steps")
    void hasConvergedReturnsTrueAfterNStableSteps() {
        Probe<TestCell> probe = new Probe<>();
        probe.setRecordingEnabled(false);
        
        // Simulate 5 steps with no swaps
        for (int i = 0; i < 5; i++) {
            probe.recordSnapshot(i, new TestCell[0], 0);
        }
        
        // Should converge after 3+ steps with no swaps
        assertTrue(detector.hasConverged(probe, 5));
    }

    /**
     * PURPOSE: As a developer, I want to verify non-convergence before N steps
     * so that I can ensure detector doesn't terminate prematurely.
     *
     * INPUTS: [TestWeaver: Define probe with fewer than N steps since last swap]
     * EXPECTED OUTPUT: [TestWeaver: hasConverged() returns false]
     * TEST DATA: [TestWeaver: Required = 3, actual = 1 or 2]
     * REPRODUCTION: [TestWeaver: Manual steps]
     *
     * [TestWeaver: Implement premature termination prevention test]
     */
    @Test
    @DisplayName("hasConverged returns false before N stable steps")
    void hasConvergedReturnsFalseBeforeNStableSteps() {
        Probe<TestCell> probe = new Probe<>();
        probe.setRecordingEnabled(false);
        
        // Only 2 steps with no swaps
        probe.recordSnapshot(0, new TestCell[0], 0);
        probe.recordSnapshot(1, new TestCell[0], 0);
        
        // Should not converge with only 2 stable steps
        assertFalse(detector.hasConverged(probe, 2));
    }

    /**
     * PURPOSE: As a developer, I want to verify constructor validation
     * so that I can ensure invalid stable step counts are rejected.
     *
     * INPUTS: [TestWeaver: Define invalid requiredStableSteps values]
     * EXPECTED OUTPUT: [TestWeaver: IllegalArgumentException thrown]
     * TEST DATA: [TestWeaver: Test with 0, -1]
     * REPRODUCTION: [TestWeaver: Manual verification]
     *
     * [TestWeaver: Implement constructor validation test]
     */
    @Test
    @DisplayName("constructor rejects invalid stable step counts")
    void constructorRejectsInvalidStableStepCounts() {
        assertThrows(IllegalArgumentException.class, () -> new NoSwapConvergence<>(0));
        assertThrows(IllegalArgumentException.class, () -> new NoSwapConvergence<>(-1));
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
