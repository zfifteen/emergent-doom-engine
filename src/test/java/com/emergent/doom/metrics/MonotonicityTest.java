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
        @DisplayName("null array returns 100.0")
        void nullArray() {
            assertEquals(100.0, metric.compute(null));
        }

        @Test
        @DisplayName("empty array returns 100.0")
        void emptyArray() {
            assertEquals(100.0, metric.compute(new IntCell[0]));
        }

        @Test
        @DisplayName("single element returns 100.0")
        void singleElement() {
            assertEquals(100.0, metric.compute(createCells(42)));
        }
    }

    @Nested
    @DisplayName("Reference Implementation Tests (cell_research/analysis/utils.py)")
    class ReferenceTests {

        @Test
        @DisplayName("sorted array [1,2,3,4,5] returns 100.0%")
        void sortedArray() {
            // Python: get_monotonicity([1,2,3,4,5])
            // monotonicity_value starts at 1, then 2>=1, 3>=2, 4>=3, 5>=4 → all pass → 5/5 = 100%
            IntCell[] cells = createCells(1, 2, 3, 4, 5);
            assertEquals(100.0, metric.compute(cells), 0.01);
        }

        @Test
        @DisplayName("reverse sorted array [5,4,3,2,1] returns 20.0%")
        void reverseSortedArray() {
            // Python: get_monotonicity([5,4,3,2,1])
            // monotonicity_value starts at 1, then 4<5, 3<4, 2<3, 1<2 → none pass → 1/5 = 20%
            IntCell[] cells = createCells(5, 4, 3, 2, 1);
            assertEquals(20.0, metric.compute(cells), 0.01);
        }

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

        @Test
        @DisplayName("metric name is 'Monotonicity'")
        void metricName() {
            assertEquals("Monotonicity", metric.getName());
        }

        @Test
        @DisplayName("higher is better (not lower)")
        void higherIsBetter() {
            assertFalse(metric.isLowerBetter());
        }
    }
}
