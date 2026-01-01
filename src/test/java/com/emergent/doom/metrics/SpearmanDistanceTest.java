package com.emergent.doom.metrics;

import com.emergent.doom.swap.IntCell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SpearmanDistance} metric.
 *
 * <p>Validates against cell_research/analysis/utils.py get_spearman_distance() reference.</p>
 */
class SpearmanDistanceTest {

    private SpearmanDistance<IntCell> metric;

    @BeforeEach
    void setUp() {
        metric = new SpearmanDistance<>();
    }

    private IntCell[] createCells(int... values) {
        IntCell[] cells = new IntCell[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = new IntCell(values[i]);
        }
        return cells;
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("null array returns 0.0")
        void nullArray() {
            assertEquals(0.0, metric.compute(null));
        }

        @Test
        @DisplayName("empty array returns 0.0")
        void emptyArray() {
            assertEquals(0.0, metric.compute(new IntCell[0]));
        }

        @Test
        @DisplayName("single element returns 0.0")
        void singleElement() {
            assertEquals(0.0, metric.compute(createCells(42)));
        }
    }

    @Nested
    @DisplayName("Reference Implementation Tests (cell_research/analysis/utils.py)")
    class ReferenceTests {

        @Test
        @DisplayName("sorted array [0,1,2,3,4] returns 0.0")
        void sortedArray() {
            // For values [0,1,2,3,4], sorted order places each at its index
            // All elements are already at expected positions → distance = 0
            IntCell[] cells = createCells(0, 1, 2, 3, 4);
            assertEquals(0.0, metric.compute(cells), 0.01);
        }

        @Test
        @DisplayName("reverse sorted array [4,3,2,1,0] has maximum distance")
        void reverseSortedArray() {
            // Values [4,3,2,1,0]
            // Expected positions: 0→pos0, 1→pos1, 2→pos2, 3→pos3, 4→pos4
            // Actual: 4 at pos0 (expected 4), 3 at pos1 (expected 3), etc.
            // Wait - need to think about this more carefully.
            //
            // Values are [4,3,2,1,0], indices are [0,1,2,3,4]
            // Sorted would be [0,1,2,3,4], so:
            // - 4 at index 0 should be at index 4 → |0-4| = 4
            // - 3 at index 1 should be at index 3 → |1-3| = 2
            // - 2 at index 2 should be at index 2 → |2-2| = 0
            // - 1 at index 3 should be at index 1 → |3-1| = 2
            // - 0 at index 4 should be at index 0 → |4-0| = 4
            // Total: 4+2+0+2+4 = 12
            IntCell[] cells = createCells(4, 3, 2, 1, 0);
            assertEquals(12.0, metric.compute(cells), 0.01);
        }

        @Test
        @DisplayName("[3,1,2] has distance 4")
        void threeElementsUnsorted() {
            // Values [3,1,2], indices [0,1,2]
            // Sorted would be [1,2,3], so:
            // - 3 at index 0 should be at index 2 → |0-2| = 2
            // - 1 at index 1 should be at index 0 → |1-0| = 1
            // - 2 at index 2 should be at index 1 → |2-1| = 1
            // Total: 2+1+1 = 4
            IntCell[] cells = createCells(3, 1, 2);
            assertEquals(4.0, metric.compute(cells), 0.01);
        }

        @Test
        @DisplayName("[1,2,3] has distance 0")
        void threeElementsSorted() {
            IntCell[] cells = createCells(1, 2, 3);
            assertEquals(0.0, metric.compute(cells), 0.01);
        }

        @Test
        @DisplayName("[2,1] has distance 2")
        void twoElementsSwapped() {
            // Values [2,1], indices [0,1]
            // Sorted would be [1,2], so:
            // - 2 at index 0 should be at index 1 → |0-1| = 1
            // - 1 at index 1 should be at index 0 → |1-0| = 1
            // Total: 1+1 = 2
            IntCell[] cells = createCells(2, 1);
            assertEquals(2.0, metric.compute(cells), 0.01);
        }
    }

    @Nested
    @DisplayName("Equal Elements")
    class EqualElements {

        @Test
        @DisplayName("equal elements [5,5,5] returns 0.0")
        void equalElements() {
            // All elements equal → all in their "expected" position → distance = 0
            IntCell[] cells = createCells(5, 5, 5);
            assertEquals(0.0, metric.compute(cells), 0.01);
        }

        @Test
        @DisplayName("[1,1,2] returns 0.0")
        void duplicatesInOrder() {
            // Already sorted → distance = 0
            IntCell[] cells = createCells(1, 1, 2);
            assertEquals(0.0, metric.compute(cells), 0.01);
        }
    }

    @Nested
    @DisplayName("Metric Properties")
    class MetricProperties {

        @Test
        @DisplayName("metric name is 'Spearman Distance'")
        void metricName() {
            assertEquals("Spearman Distance", metric.getName());
        }

        @Test
        @DisplayName("lower is better")
        void lowerIsBetter() {
            assertTrue(metric.isLowerBetter());
        }
    }
}
