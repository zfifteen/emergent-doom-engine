package com.emergent.doom.metrics;

import com.emergent.doom.swap.IntCell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Monotonicity} metric.
 *
 * <p>Validates against cell_research/analysis/utils.py get_monotonicity() reference.</p>
 */
class MonotonicityTest {

    private Monotonicity<IntCell> metric;

    @BeforeEach
    void setUp() {
        metric = new Monotonicity<>();
    }

    /**
     * Creates an IntCell array from a varargs sequence of primitive integers.
     * This helper simplifies test data construction for monotonicity metric tests,
     * allowing concise specification of cell sequences to verify ordering error calculations.
     *
     * @param values Variable number of int values to wrap in IntCell instances
     * @return IntCell array containing cells with the specified values in order
     */
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
         * PURPOSE: As a developer, I want null array to return 100.0
         * so that I can handle edge cases safely without NullPointerException.
         *
         * INPUTS: null array
         * EXPECTED OUTPUT: compute() returns 100.0
         * TEST DATA: null
         * REPRODUCTION: metric.compute((IntCell[]) null)
         */
        @Test
        @DisplayName("null array returns 100.0")
        void nullArray() {
            assertEquals(100.0, metric.compute((IntCell[]) null));
        }

        /**
         * PURPOSE: As a developer, I want empty array to return 100.0
         * so that I can handle edge cases with zero-length arrays.
         *
         * INPUTS: Empty array
         * EXPECTED OUTPUT: compute() returns 100.0
         * TEST DATA: new IntCell[0]
         * REPRODUCTION: metric.compute(new IntCell[0])
         */
        @Test
        @DisplayName("empty array returns 100.0")
        void emptyArray() {
            assertEquals(100.0, metric.compute(new IntCell[0]));
        }

        /**
         * PURPOSE: As a developer, I want single element array to return 100.0
         * so that I can verify trivially sorted arrays score perfectly.
         *
         * INPUTS: Array with single element [42]
         * EXPECTED OUTPUT: compute() returns 100.0
         * TEST DATA: [42]
         * REPRODUCTION: metric.compute(createCells(42))
         */
        @Test
        @DisplayName("single element returns 100.0")
        void singleElement() {
            assertEquals(100.0, metric.compute(createCells(42)));
        }
    }

    @Nested
    @DisplayName("Reference Implementation Tests (cell_research/analysis/utils.py)")
    class ReferenceTests {

        /**
         * PURPOSE: As a developer, I want sorted array to return 100.0%
         * so that I can verify the metric correctly identifies perfectly sorted data.
         *
         * INPUTS: Sorted array [1,2,3,4,5]
         * EXPECTED OUTPUT: compute() returns 100.0 (all elements >= predecessor)
         * TEST DATA: [1,2,3,4,5]
         * REPRODUCTION: Python get_monotonicity([1,2,3,4,5]) → 5/5 = 100%
         */
        @Test
        @DisplayName("sorted array [1,2,3,4,5] returns 100.0%")
        void sortedArray() {
            // Python: get_monotonicity([1,2,3,4,5])
            // monotonicity_value starts at 1, then 2>=1, 3>=2, 4>=3, 5>=4 → all pass → 5/5 = 100%
            IntCell[] cells = createCells(1, 2, 3, 4, 5);
            assertEquals(100.0, metric.compute(cells), 0.01);
        }

        /**
         * PURPOSE: As a developer, I want reverse sorted array to return 20.0%
         * so that I can verify the metric correctly identifies worst-case unsorted data.
         *
         * INPUTS: Reverse sorted array [5,4,3,2,1]
         * EXPECTED OUTPUT: compute() returns 20.0 (only first element counts)
         * TEST DATA: [5,4,3,2,1]
         * REPRODUCTION: Python get_monotonicity([5,4,3,2,1]) → 1/5 = 20%
         */
        @Test
        @DisplayName("reverse sorted array [5,4,3,2,1] returns 20.0%")
        void reverseSortedArray() {
            // Python: get_monotonicity([5,4,3,2,1])
            // monotonicity_value starts at 1, then 4<5, 3<4, 2<3, 1<2 → none pass → 1/5 = 20%
            IntCell[] cells = createCells(5, 4, 3, 2, 1);
            assertEquals(20.0, metric.compute(cells), 0.01);
        }

        /**
         * PURPOSE: As a developer, I want partially monotonic array to return correct percentage
         * so that I can verify the metric handles mixed ordering correctly.
         *
         * INPUTS: Partially monotonic array [1,3,2,4,5]
         * EXPECTED OUTPUT: compute() returns 80.0 (4 out of 5 elements satisfy monotonicity)
         * TEST DATA: [1,3,2,4,5]
         * REPRODUCTION: Python get_monotonicity([1,3,2,4,5]) → 4/5 = 80%
         */
        @Test
        @DisplayName("[1,3,2,4,5] returns 80.0%")
        void partiallyMonotonic() {
            // Python: get_monotonicity([1,3,2,4,5])
            // Start: 1 (count=1)
            // 3>=1? Yes (count=2)
            // 2>=3? No (count stays 2)
            // 4>=2? Yes (count=3)
            // 5>=4? Yes (count=4)
            // Result: 4/5 = 80%
            IntCell[] cells = createCells(1, 3, 2, 4, 5);
            assertEquals(80.0, metric.compute(cells), 0.01);
        }

        /**
         * PURPOSE: As a developer, I want three-element array to compute correctly
         * so that I can verify the metric works on small arrays.
         *
         * INPUTS: Three-element array [3,1,2]
         * EXPECTED OUTPUT: compute() returns 66.67 (2 out of 3 elements satisfy monotonicity)
         * TEST DATA: [3,1,2]
         * REPRODUCTION: Python get_monotonicity([3,1,2]) → 2/3 = 66.67%
         */
        @Test
        @DisplayName("[3,1,2] returns 66.67%")
        void threeElements() {
            // Python: get_monotonicity([3,1,2])
            // Start: 3 (count=1)
            // 1>=3? No (count stays 1)
            // 2>=1? Yes (count=2)
            // Result: 2/3 = 66.67%
            IntCell[] cells = createCells(3, 1, 2);
            assertEquals(66.67, metric.compute(cells), 0.1);
        }

        /**
         * PURPOSE: As a developer, I want equal elements to return 100.0%
         * so that I can verify the metric treats equal values as monotonic.
         *
         * INPUTS: Array with all equal elements [5,5,5]
         * EXPECTED OUTPUT: compute() returns 100.0 (all elements >= predecessor)
         * TEST DATA: [5,5,5]
         * REPRODUCTION: metric.compute(createCells(5, 5, 5))
         */
        @Test
        @DisplayName("equal elements [5,5,5] returns 100.0%")
        void equalElements() {
            // All elements equal → all >= predecessor → 100%
            IntCell[] cells = createCells(5, 5, 5);
            assertEquals(100.0, metric.compute(cells), 0.01);
        }
    }

    @Nested
    @DisplayName("Metric Properties")
    class MetricProperties {

        /**
         * PURPOSE: As a developer, I want to verify the metric name is 'Monotonicity'
         * so that I can identify the metric correctly in reports.
         *
         * INPUTS: Monotonicity metric instance
         * EXPECTED OUTPUT: getName() returns "Monotonicity"
         * TEST DATA: metric instance
         * REPRODUCTION: metric.getName()
         */
        @Test
        @DisplayName("metric name is 'Monotonicity'")
        void metricName() {
            assertEquals("Monotonicity", metric.getName());
        }

        /**
         * PURPOSE: As a developer, I want to verify higher values are better for Monotonicity
         * so that I can correctly interpret metric results in experiments.
         *
         * INPUTS: Monotonicity metric instance
         * EXPECTED OUTPUT: isLowerBetter() returns false (higher is better)
         * TEST DATA: metric instance
         * REPRODUCTION: metric.isLowerBetter()
         */
        @Test
        @DisplayName("higher is better (not lower)")
        void higherIsBetter() {
            assertFalse(metric.isLowerBetter());
        }
    }
}
