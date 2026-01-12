package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.StepSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for MonotonicityError.
 *
 * PURPOSE: Verify that MonotonicityError correctly counts adjacent inversions
 * (pairs where cells[i] > cells[i+1]), matching the reference implementation
 * from cell_research/analysis/utils.py.
 *
 * [TestWeaver: Generated skeleton - expand with specific test scenarios]
 */
@DisplayName("MonotonicityError Tests")
class MonotonicityErrorTest {

    private MonotonicityError<TestCell> metric;

    @BeforeEach
    void setUp() {
        metric = new MonotonicityError<>();
    }

    /**
     * PURPOSE: As a developer, I want to verify adjacent inversion counting
     * so that I can confirm the metric measures deviation from sorted order.
     *
     * INPUTS: [TestWeaver: Define test inputs - cell array with known inversions]
     * EXPECTED OUTPUT: [TestWeaver: Expected inversion count]
     * TEST DATA: [TestWeaver: Specify cell values like [3, 1, 4, 2] = 2 inversions]
     * REPRODUCTION: [TestWeaver: Manual verification steps]
     *
     * [TestWeaver: Implement test logic based on MonotonicityError API]
     */
    @Test
    @DisplayName("compute counts adjacent inversions correctly")
    void computeCountsAdjacentInversions() {
        TestCell[] cells = new TestCell[] {
            new TestCell(3),
            new TestCell(1),
            new TestCell(4),
            new TestCell(2)
        };
        
        // Adjacent inversions: (3>1), (4>2) = 2 inversions
        assertEquals(2.0, metric.compute(cells));
    }

    /**
     * PURPOSE: As a developer, I want to verify zero inversions for sorted arrays
     * so that I can confirm the metric returns 0 for perfect order.
     *
     * INPUTS: [TestWeaver: Define sorted cell array]
     * EXPECTED OUTPUT: [TestWeaver: 0.0]
     * TEST DATA: [TestWeaver: [1, 2, 3, 4, 5]]
     * REPRODUCTION: [TestWeaver: Manual steps]
     *
     * [TestWeaver: Implement sorted array test]
     */
    @Test
    @DisplayName("compute returns zero for sorted arrays")
    void computeReturnsZeroForSortedArrays() {
        TestCell[] cells = new TestCell[] {
            new TestCell(1),
            new TestCell(2),
            new TestCell(3),
            new TestCell(4),
            new TestCell(5)
        };
        
        assertEquals(0.0, metric.compute(cells));
    }

    /**
     * PURPOSE: As a developer, I want to verify edge case handling
     * so that I can ensure the metric handles null, empty, and single-element arrays.
     *
     * INPUTS: [TestWeaver: Define edge cases]
     * EXPECTED OUTPUT: [TestWeaver: 0.0 for all edge cases]
     * TEST DATA: [TestWeaver: null, [], [42]]
     * REPRODUCTION: [TestWeaver: Manual verification]
     *
     * [TestWeaver: Implement edge case tests]
     */
    @Test
    @DisplayName("compute handles edge cases correctly")
    void computeHandlesEdgeCases() {
        assertEquals(0.0, metric.compute((TestCell[]) null));
        assertEquals(0.0, metric.compute(new TestCell[] {}));
        assertEquals(0.0, metric.compute(new TestCell[] { new TestCell(42) }));
    }

    // Test helper class
    private static class TestCell implements Cell<TestCell>, Comparable<TestCell> {
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
