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

        /**
         * PURPOSE: As a developer, I want null array to return 0.0
         * so that I can handle null input safely without NullPointerException.
         *
         * INPUTS: null array
         * EXPECTED OUTPUT: compute() returns 0.0
         * TEST DATA: null
         * REPRODUCTION: metric.compute((IntCell[]) null)
         */
        @Test
        @DisplayName("null array returns 0.0")
        void nullArray() {
            assertEquals(0.0, metric.compute((IntCell[]) null));
        }

        /**
         * PURPOSE: As a developer, I want empty array to return 0.0
         * so that I can handle edge cases with zero-length arrays.
         *
         * INPUTS: Empty array
         * EXPECTED OUTPUT: compute() returns 0.0
         * TEST DATA: new IntCell[0]
         * REPRODUCTION: metric.compute(new IntCell[0])
         */
        @Test
        @DisplayName("empty array returns 0.0")
        void emptyArray() {
            assertEquals(0.0, metric.compute(new IntCell[0]));
        }

        /**
         * PURPOSE: As a developer, I want single element array to return 0.0
         * so that I can verify trivially sorted arrays have zero distance.
         *
         * INPUTS: Array with single element [42]
         * EXPECTED OUTPUT: compute() returns 0.0
         * TEST DATA: [42]
         * REPRODUCTION: metric.compute(createCells(42))
         */
        @Test
        @DisplayName("single element returns 0.0")
        void singleElement() {
            assertEquals(0.0, metric.compute(createCells(42)));
        }
    }

    @Nested
    @DisplayName("Reference Implementation Tests (cell_research/analysis/utils.py)")
    class ReferenceTests {

        /**
         * PURPOSE: As a developer, I want sorted array to return 0.0
         * so that I can verify perfectly sorted data has zero Spearman distance.
         *
         * INPUTS: Sorted array [0,1,2,3,4]
         * EXPECTED OUTPUT: compute() returns 0.0 (all elements at expected positions)
         * TEST DATA: [0,1,2,3,4]
         * REPRODUCTION: metric.compute(createCells(0, 1, 2, 3, 4))
         */
        @Test
        @DisplayName("sorted array [0,1,2,3,4] returns 0.0")
        void sortedArray() {
            // For values [0,1,2,3,4], sorted order places each at its index
            // All elements are already at expected positions → distance = 0
            IntCell[] cells = createCells(0, 1, 2, 3, 4);
            assertEquals(0.0, metric.compute(cells), 0.01);
        }

        /**
         * PURPOSE: As a developer, I want reverse sorted array to have maximum distance
         * so that I can verify the metric correctly identifies worst-case unsorted data.
         *
         * INPUTS: Reverse sorted array [4,3,2,1,0]
         * EXPECTED OUTPUT: compute() returns 12.0 (sum of position displacements)
         * TEST DATA: [4,3,2,1,0]
         * REPRODUCTION: Spearman distance = Σ|actual_pos - expected_pos| = 4+2+0+2+4 = 12
         */
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

        /**
         * PURPOSE: As a developer, I want three-element unsorted array to calculate distance correctly
         * so that I can verify the metric works on small arrays.
         *
         * INPUTS: Unsorted array [3,1,2]
         * EXPECTED OUTPUT: compute() returns 4.0 (sum of position displacements)
         * TEST DATA: [3,1,2]
         * REPRODUCTION: Spearman distance = |0-2| + |1-0| + |2-1| = 2+1+1 = 4
         */
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

        /**
         * PURPOSE: As a developer, I want sorted three-element array to return 0.0
         * so that I can verify small sorted arrays have zero distance.
         *
         * INPUTS: Sorted array [1,2,3]
         * EXPECTED OUTPUT: compute() returns 0.0
         * TEST DATA: [1,2,3]
         * REPRODUCTION: metric.compute(createCells(1, 2, 3))
         */
        @Test
        @DisplayName("[1,2,3] has distance 0")
        void threeElementsSorted() {
            IntCell[] cells = createCells(1, 2, 3);
            assertEquals(0.0, metric.compute(cells), 0.01);
        }

        /**
         * PURPOSE: As a developer, I want two swapped elements to calculate distance correctly
         * so that I can verify the metric handles minimal unsorted arrays.
         *
         * INPUTS: Swapped two-element array [2,1]
         * EXPECTED OUTPUT: compute() returns 2.0
         * TEST DATA: [2,1]
         * REPRODUCTION: Spearman distance = |0-1| + |1-0| = 1+1 = 2
         */
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

        /**
         * PURPOSE: As a developer, I want equal elements to return 0.0
         * so that I can verify arrays with all equal values have zero distance.
         *
         * INPUTS: Array with all equal elements [5,5,5]
         * EXPECTED OUTPUT: compute() returns 0.0
         * TEST DATA: [5,5,5]
         * REPRODUCTION: metric.compute(createCells(5, 5, 5))
         */
        @Test
        @DisplayName("equal elements [5,5,5] returns 0.0")
        void equalElements() {
            // All elements equal → all in their "expected" position → distance = 0
            IntCell[] cells = createCells(5, 5, 5);
            assertEquals(0.0, metric.compute(cells), 0.01);
        }

        /**
         * PURPOSE: As a developer, I want duplicates in order to return 0.0
         * so that I can verify sorted arrays with duplicates have zero distance.
         *
         * INPUTS: Sorted array with duplicates [1,1,2]
         * EXPECTED OUTPUT: compute() returns 0.0
         * TEST DATA: [1,1,2]
         * REPRODUCTION: metric.compute(createCells(1, 1, 2))
         */
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

        /**
         * PURPOSE: As a developer, I want to verify the metric name is 'Spearman Distance'
         * so that I can identify the metric correctly in reports.
         *
         * INPUTS: SpearmanDistance metric instance
         * EXPECTED OUTPUT: getName() returns "Spearman Distance"
         * TEST DATA: metric instance
         * REPRODUCTION: metric.getName()
         */
        @Test
        @DisplayName("metric name is 'Spearman Distance'")
        void metricName() {
            assertEquals("Spearman Distance", metric.getName());
        }

        /**
         * PURPOSE: As a developer, I want to verify lower values are better for Spearman Distance
         * so that I can correctly interpret metric results in experiments.
         *
         * INPUTS: SpearmanDistance metric instance
         * EXPECTED OUTPUT: isLowerBetter() returns true (lower distance is better)
         * TEST DATA: metric instance
         * REPRODUCTION: metric.isLowerBetter()
         */
        @Test
        @DisplayName("lower is better")
        void lowerIsBetter() {
            assertTrue(metric.isLowerBetter());
        }
    }
}
