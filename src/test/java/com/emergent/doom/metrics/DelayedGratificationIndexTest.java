package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.StepSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for DelayedGratificationIndex.
 *
 * PURPOSE: Verify that DelayedGratificationIndex correctly measures the degree
 * to which better cells appear later in the array, capturing adaptive behavior
 * where initially worse solutions must be accepted.
 *
 * [TestWeaver: Generated skeleton - expand with specific test scenarios]
 */
@DisplayName("DelayedGratificationIndex Tests")
class DelayedGratificationIndexTest {

    private DelayedGratificationIndex<TestCell> metric;

    @BeforeEach
    void setUp() {
        metric = new DelayedGratificationIndex<>();
    }

    /**
     * PURPOSE: As a developer, I want to verify position-weighted quality calculation
     * so that I can confirm the metric detects delayed gratification patterns.
     *
     * INPUTS: [TestWeaver: Define test inputs - cell array with quality distribution]
     * EXPECTED OUTPUT: [TestWeaver: Expected index value]
     * TEST DATA: [TestWeaver: Specify cells where better values appear later]
     * REPRODUCTION: [TestWeaver: Manual verification steps]
     *
     * [TestWeaver: Implement test logic based on DelayedGratificationIndex API]
     */
    @Test
    @DisplayName("compute measures position-weighted quality correctly")
    void computeMeasuresPositionWeightedQuality() {
        TestCell[] cells = new TestCell[] {
            new TestCell(1),
            new TestCell(2),
            new TestCell(3),
            new TestCell(10)  // Better value at end
        };
        
        double result = metric.compute(cells);
        
        // Should be positive - better cells appear later
        assertTrue(result > 0.0);
    }

    /**
     * PURPOSE: As a developer, I want to verify normalization
     * so that I can ensure index values are comparable across array sizes.
     *
     * INPUTS: [TestWeaver: Define arrays of different sizes]
     * EXPECTED OUTPUT: [TestWeaver: Normalized index values]
     * TEST DATA: [TestWeaver: Test values]
     * REPRODUCTION: [TestWeaver: Manual steps]
     *
     * [TestWeaver: Implement normalization verification]
     */
    @Test
    @DisplayName("compute normalizes by average position weight")
    void computeNormalizesByAveragePositionWeight() {
        // Create snapshot with values
        List<Integer> values = Arrays.asList(5, 5, 5, 5);
        StepSnapshot<TestCell> snapshot = new StepSnapshot<>(0, 
            Arrays.asList((Comparable<?>) 5, 5, 5, 5),
            Arrays.asList(
                new Object[]{0, 0, 5, 0},
                new Object[]{0, 0, 5, 0},
                new Object[]{0, 0, 5, 0},
                new Object[]{0, 0, 5, 0}
            ),
            0);
        
        // All cells same value - deviation should be 0
        double result = metric.compute(snapshot);
        assertEquals(0.0, result, 0.01);
    }

    /**
     * PURPOSE: As a developer, I want to verify edge case handling
     * so that I can ensure the metric handles empty and null arrays.
     *
     * INPUTS: [TestWeaver: Define edge cases]
     * EXPECTED OUTPUT: [TestWeaver: 0.0 for empty/null]
     * TEST DATA: [TestWeaver: null, []]
     * REPRODUCTION: [TestWeaver: Manual verification]
     *
     * [TestWeaver: Implement edge case tests]
     */
    @Test
    @DisplayName("compute handles edge cases correctly")
    void computeHandlesEdgeCases() {
        assertEquals(0.0, metric.compute((TestCell[]) null));
        assertEquals(0.0, metric.compute(new TestCell[] {}));
        
        // Single cell
        TestCell[] singleCell = new TestCell[] { new TestCell(5) };
        double result = metric.compute(singleCell);
        assertTrue(result >= 0.0);
    }

    // Test helper class
    private static class TestCell implements Cell<TestCell> {
        private final int value;
        
        TestCell(int value) {
            this.value = value;
        }
        
        
        public int getValue() {
            return value;
        }
        
        
        public int compareTo(TestCell other) {
            return Integer.compare(this.value, other.value);
        }
        
        
        public boolean shouldSwapWith(TestCell other) {
            return false;
        }
    }
}
