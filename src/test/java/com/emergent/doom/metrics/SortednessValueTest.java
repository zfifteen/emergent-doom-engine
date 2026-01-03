package com.emergent.doom.metrics;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.group.GroupAwareCell;
import com.emergent.doom.experiment.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for SortednessValue metric.
 *
 * Tests verify:
 * - Fully sorted arrays return 100.0
 * - Reverse sorted arrays return appropriate low values
 * - Partially sorted arrays return intermediate values
 * - Edge cases (empty, single element, duplicates)
 * - Statistical baseline for random arrays
 * - Both INCREASING and DECREASING sort directions
 */
class SortednessValueTest {

    private SortednessValue<IntCell> metric;

    @BeforeEach
    void setUp() {
        metric = new SortednessValue<>();
    }

    private IntCell[] createCells(int... values) {
        IntCell[] cells = new IntCell[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = new IntCell(values[i]);
        }
        return cells;
    }

    // ========================================================================
    // Edge Cases
    // ========================================================================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty array returns 100.0")
        void emptyArray_returns100() {
            IntCell[] cells = new IntCell[0];

            double result = metric.compute(cells);

            assertEquals(100.0, result, 0.001, "Empty array is trivially sorted");
        }

        @Test
        @DisplayName("Null array returns 100.0")
        void nullArray_returns100() {
            double result = metric.compute((IntCell[]) null);

            assertEquals(100.0, result, 0.001, "Null array is trivially sorted");
        }

        @Test
        @DisplayName("Single element returns 100.0")
        void singleElement_returns100() {
            IntCell[] cells = createCells(42);

            double result = metric.compute(cells);

            assertEquals(100.0, result, 0.001, "Single element is trivially sorted");
        }

        @Test
        @DisplayName("Two elements sorted returns 100.0")
        void twoElementsSorted_returns100() {
            IntCell[] cells = createCells(1, 2);

            double result = metric.compute(cells);

            assertEquals(100.0, result, 0.001, "Sorted pair should be 100%");
        }

        @Test
        @DisplayName("Two elements reversed returns 0.0")
        void twoElementsReversed_returns0() {
            IntCell[] cells = createCells(2, 1);

            double result = metric.compute(cells);

            assertEquals(0.0, result, 0.001, "Reversed pair has no correct positions");
        }
    }

    // ========================================================================
    // Fully Sorted Arrays
    // ========================================================================

    @Nested
    @DisplayName("Fully sorted arrays")
    class FullySortedTests {

        @Test
        @DisplayName("Sorted array of 5 returns 100.0")
        void sortedArray5_returns100() {
            IntCell[] cells = createCells(1, 2, 3, 4, 5);

            double result = metric.compute(cells);

            assertEquals(100.0, result, 0.001, "All elements in correct position");
        }

        @Test
        @DisplayName("Sorted array of 10 returns 100.0")
        void sortedArray10_returns100() {
            IntCell[] cells = createCells(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

            double result = metric.compute(cells);

            assertEquals(100.0, result, 0.001, "All elements in correct position");
        }

        @Test
        @DisplayName("Sorted array of 100 returns 100.0")
        void sortedArray100_returns100() {
            IntCell[] cells = new IntCell[100];
            for (int i = 0; i < 100; i++) {
                cells[i] = new IntCell(i + 1);
            }

            double result = metric.compute(cells);

            assertEquals(100.0, result, 0.001, "All elements in correct position");
        }
    }

    // ========================================================================
    // Reverse Sorted Arrays
    // ========================================================================

    @Nested
    @DisplayName("Reverse sorted arrays")
    class ReverseSortedTests {

        @Test
        @DisplayName("Reverse sorted [5,4,3,2,1] returns 20.0")
        void reverseSorted5_returns20() {
            // Reverse: [5,4,3,2,1]
            // Sorted:  [1,2,3,4,5]
            // Only position 2 (value 3) is correct
            IntCell[] cells = createCells(5, 4, 3, 2, 1);

            double result = metric.compute(cells);

            assertEquals(20.0, result, 0.001, "Only middle element '3' is in correct position");
        }

        @Test
        @DisplayName("Reverse sorted [4,3,2,1] returns 0.0")
        void reverseSorted4_returns0() {
            // Reverse: [4,3,2,1]
            // Sorted:  [1,2,3,4]
            // No elements in correct position
            IntCell[] cells = createCells(4, 3, 2, 1);

            double result = metric.compute(cells);

            assertEquals(0.0, result, 0.001, "No elements in correct position for even-length reverse");
        }

        @Test
        @DisplayName("Reverse sorted [3,2,1] returns 33.33")
        void reverseSorted3_returns33() {
            // Reverse: [3,2,1]
            // Sorted:  [1,2,3]
            // Only position 1 (value 2) is correct
            IntCell[] cells = createCells(3, 2, 1);

            double result = metric.compute(cells);

            assertEquals(33.33, result, 0.01, "Only middle element '2' is in correct position");
        }
    }

    // ========================================================================
    // Partially Sorted Arrays
    // ========================================================================

    @Nested
    @DisplayName("Partially sorted arrays")
    class PartiallySortedTests {

        @Test
        @DisplayName("[2,1,3,4,5] returns 60.0")
        void partialSort_2swapped_returns60() {
            // Array:  [2,1,3,4,5]
            // Sorted: [1,2,3,4,5]
            // Correct positions: 2(3), 3(4), 4(5) = 3/5 = 60%
            IntCell[] cells = createCells(2, 1, 3, 4, 5);

            double result = metric.compute(cells);

            assertEquals(60.0, result, 0.001, "3 out of 5 in correct position");
        }

        @Test
        @DisplayName("[1,3,2,4,5] returns 60.0")
        void partialSort_middleSwapped_returns60() {
            // Array:  [1,3,2,4,5]
            // Sorted: [1,2,3,4,5]
            // Correct positions: 0(1), 3(4), 4(5) = 3/5 = 60%
            IntCell[] cells = createCells(1, 3, 2, 4, 5);

            double result = metric.compute(cells);

            assertEquals(60.0, result, 0.001, "3 out of 5 in correct position");
        }

        @Test
        @DisplayName("[1,2,3,5,4] returns 60.0")
        void partialSort_endSwapped_returns60() {
            // Array:  [1,2,3,5,4]
            // Sorted: [1,2,3,4,5]
            // Correct positions: 0(1), 1(2), 2(3) = 3/5 = 60%
            IntCell[] cells = createCells(1, 2, 3, 5, 4);

            double result = metric.compute(cells);

            assertEquals(60.0, result, 0.001, "3 out of 5 in correct position");
        }
    }

    // ========================================================================
    // Duplicate Values
    // ========================================================================

    @Nested
    @DisplayName("Duplicate values")
    class DuplicateTests {

        @Test
        @DisplayName("All same values returns 100.0")
        void allSameValues_returns100() {
            IntCell[] cells = createCells(5, 5, 5, 5, 5);

            double result = metric.compute(cells);

            assertEquals(100.0, result, 0.001, "All same values are trivially sorted");
        }

        @Test
        @DisplayName("[1,1,2,2,3] returns 100.0")
        void sortedWithDuplicates_returns100() {
            IntCell[] cells = createCells(1, 1, 2, 2, 3);

            double result = metric.compute(cells);

            assertEquals(100.0, result, 0.001, "Sorted array with duplicates");
        }

        @Test
        @DisplayName("[2,1,1,2,3] with duplicates")
        void partialWithDuplicates() {
            // Array:  [2,1,1,2,3]
            // Sorted: [1,1,2,2,3]
            // Position 4 (value 3) is correct, others depend on stable sort
            IntCell[] cells = createCells(2, 1, 1, 2, 3);

            double result = metric.compute(cells);

            // At minimum, position 4 (value 3) is correct
            assertTrue(result >= 20.0, "At least 1 element in correct position");
            assertTrue(result <= 100.0, "At most all elements correct");
        }
    }

    // ========================================================================
    // Statistical Baseline for Random Arrays
    // ========================================================================

    @Nested
    @DisplayName("Statistical baseline")
    class StatisticalTests {

        @Test
        @DisplayName("Random arrays with duplicates average less than 50%")
        void randomArrays_averageAround50() {
            Random rng = new Random(42); // Fixed seed for reproducibility
            int trials = 1000;
            double sum = 0.0;

            for (int t = 0; t < trials; t++) {
                IntCell[] cells = new IntCell[100];
                for (int i = 0; i < 100; i++) {
                    cells[i] = new IntCell(rng.nextInt(100) + 1);
                }
                sum += metric.compute(cells);
            }

            double average = sum / trials;

            // Random permutations should have ~1% of elements in correct position
            // (each has 1/n chance), but with duplicates the expected value is higher
            // For 100 elements with values 1-100, we expect roughly 1-10% correct
            assertTrue(average > 0.0, "Average should be positive");
            assertTrue(average < 50.0, "Average should be less than 50% for random");
        }

        @Test
        @DisplayName("Shuffled unique array baseline")
        void shuffledUniqueArray_baseline() {
            Random rng = new Random(42);
            int trials = 100;
            double sum = 0.0;

            for (int t = 0; t < trials; t++) {
                // Create unique values 1-100 and shuffle
                int[] values = new int[100];
                for (int i = 0; i < 100; i++) {
                    values[i] = i + 1;
                }
                // Fisher-Yates shuffle
                for (int i = 99; i > 0; i--) {
                    int j = rng.nextInt(i + 1);
                    int temp = values[i];
                    values[i] = values[j];
                    values[j] = temp;
                }

                IntCell[] cells = new IntCell[100];
                for (int i = 0; i < 100; i++) {
                    cells[i] = new IntCell(values[i]);
                }
                sum += metric.compute(cells);
            }

            double average = sum / trials;

            // For unique values, expected value is ~1% (1/n chance per position)
            assertTrue(average > 0.0, "Average should be positive");
            assertTrue(average < 10.0, "Average should be very low for shuffled unique");
        }
    }

    // ========================================================================
    // Metric Interface Contract
    // ========================================================================

    @Nested
    @DisplayName("Metric interface contract")
    class MetricContractTests {

        @Test
        @DisplayName("getName returns 'Sortedness Value'")
        void getName_returnsCorrectName() {
            assertEquals("Sortedness Value", metric.getName());
        }

        @Test
        @DisplayName("isLowerBetter returns false")
        void isLowerBetter_returnsFalse() {
            assertFalse(metric.isLowerBetter(), "Higher sortedness is better");
        }
    }

    // ========================================================================
    // Sort Direction Tests (DECREASING)
    // ========================================================================

    @Nested
    @DisplayName("Decreasing sort direction")
    class DecreasingDirectionTests {

        private SortednessValue<IntCell> decreasingMetric;

        @BeforeEach
        void setUp() {
            decreasingMetric = new SortednessValue<>(SortDirection.DECREASING);
        }

        @Test
        @DisplayName("getDirection returns DECREASING")
        void getDirection_returnsDecreasing() {
            assertEquals(SortDirection.DECREASING, decreasingMetric.getDirection());
        }

        @Test
        @DisplayName("Descending [5,4,3,2,1] returns 100.0 for DECREASING")
        void descendingArray_returns100() {
            // Array:  [5,4,3,2,1]
            // Target: [5,4,3,2,1] (descending)
            // All elements in correct position
            IntCell[] cells = createCells(5, 4, 3, 2, 1);

            double result = decreasingMetric.compute(cells);

            assertEquals(100.0, result, 0.001, "All elements in correct position for descending");
        }

        @Test
        @DisplayName("Ascending [1,2,3,4,5] returns 20.0 for DECREASING")
        void ascendingArray_returns20_forDecreasing() {
            // Array:  [1,2,3,4,5]
            // Target: [5,4,3,2,1] (descending)
            // Only position 2 (value 3) is correct
            IntCell[] cells = createCells(1, 2, 3, 4, 5);

            double result = decreasingMetric.compute(cells);

            assertEquals(20.0, result, 0.001, "Only middle element '3' is in correct position");
        }

        @Test
        @DisplayName("Two elements descending returns 100.0")
        void twoElementsDescending_returns100() {
            IntCell[] cells = createCells(2, 1);

            double result = decreasingMetric.compute(cells);

            assertEquals(100.0, result, 0.001, "Descending pair should be 100%");
        }

        @Test
        @DisplayName("Two elements ascending returns 0.0 for DECREASING")
        void twoElementsAscending_returns0_forDecreasing() {
            IntCell[] cells = createCells(1, 2);

            double result = decreasingMetric.compute(cells);

            assertEquals(0.0, result, 0.001, "Ascending pair has no correct positions for descending");
        }

        @Test
        @DisplayName("[4,3,2,1] returns 100.0 for DECREASING")
        void descendingArray4_returns100() {
            IntCell[] cells = createCells(4, 3, 2, 1);

            double result = decreasingMetric.compute(cells);

            assertEquals(100.0, result, 0.001, "All elements in correct position for descending");
        }

        @Test
        @DisplayName("[5,4,2,3,1] returns 60.0 for DECREASING")
        void partialDescending_returns60() {
            // Array:  [5,4,2,3,1]
            // Target: [5,4,3,2,1] (descending)
            // Correct positions: 0(5), 1(4), 4(1) = 3/5 = 60%
            IntCell[] cells = createCells(5, 4, 2, 3, 1);

            double result = decreasingMetric.compute(cells);

            assertEquals(60.0, result, 0.001, "3 out of 5 in correct position for descending");
        }

        @Test
        @DisplayName("Empty array returns 100.0 for DECREASING")
        void emptyArray_returns100_forDecreasing() {
            IntCell[] cells = new IntCell[0];

            double result = decreasingMetric.compute(cells);

            assertEquals(100.0, result, 0.001, "Empty array is trivially sorted");
        }

        @Test
        @DisplayName("Single element returns 100.0 for DECREASING")
        void singleElement_returns100_forDecreasing() {
            IntCell[] cells = createCells(42);

            double result = decreasingMetric.compute(cells);

            assertEquals(100.0, result, 0.001, "Single element is trivially sorted");
        }

        @Test
        @DisplayName("All same values returns 100.0 for DECREASING")
        void allSameValues_returns100_forDecreasing() {
            IntCell[] cells = createCells(5, 5, 5, 5, 5);

            double result = decreasingMetric.compute(cells);

            assertEquals(100.0, result, 0.001, "All same values are trivially sorted");
        }
    }

    // ========================================================================
    // Default Direction Tests
    // ========================================================================

    @Nested
    @DisplayName("Default direction behavior")
    class DefaultDirectionTests {

        @Test
        @DisplayName("Default constructor uses INCREASING")
        void defaultConstructor_usesIncreasing() {
            SortednessValue<IntCell> defaultMetric = new SortednessValue<>();
            assertEquals(SortDirection.INCREASING, defaultMetric.getDirection());
        }

        @Test
        @DisplayName("Null direction defaults to INCREASING")
        void nullDirection_defaultsToIncreasing() {
            SortednessValue<IntCell> nullDirectionMetric = new SortednessValue<>(null);
            assertEquals(SortDirection.INCREASING, nullDirectionMetric.getDirection());
        }

        @Test
        @DisplayName("Explicit INCREASING matches default behavior")
        void explicitIncreasing_matchesDefault() {
            SortednessValue<IntCell> increasingMetric = new SortednessValue<>(SortDirection.INCREASING);
            IntCell[] cells = createCells(1, 2, 3, 4, 5);

            double defaultResult = metric.compute(cells);
            double increasingResult = increasingMetric.compute(cells);

            assertEquals(defaultResult, increasingResult, 0.001, "Explicit INCREASING should match default");
        }
    }

    // ========================================================================
    // Test Cell Implementation
    // ========================================================================

    /**
     * Simple integer-based cell for testing purposes.
     */
    static class IntCell implements Cell<IntCell>, GroupAwareCell<IntCell> {
        private final int value;

        IntCell(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public Algotype getAlgotype() {
            return Algotype.BUBBLE;
        }

        @Override
        public com.emergent.doom.group.CellGroup<IntCell> getGroup() { return null; }
        @Override
        public com.emergent.doom.group.CellStatus getStatus() { return com.emergent.doom.group.CellStatus.ACTIVE; }
        @Override
        public com.emergent.doom.group.CellStatus getPreviousStatus() { return com.emergent.doom.group.CellStatus.ACTIVE; }
        @Override
        public void setStatus(com.emergent.doom.group.CellStatus status) {}
        @Override
        public void setPreviousStatus(com.emergent.doom.group.CellStatus status) {}
        @Override
        public void setGroup(com.emergent.doom.group.CellGroup<IntCell> group) {}
        @Override
        public int getLeftBoundary() { return 0; }
        @Override
        public void setLeftBoundary(int leftBoundary) {}
        @Override
        public int getRightBoundary() { return 0; }
        @Override
        public void setRightBoundary(int rightBoundary) {}
        @Override
        public void updateForGroupMerge() {}

        @Override
        public int compareTo(IntCell other) {
            return Integer.compare(this.value, other.value);
        }

        @Override
        public String toString() {
            return "IntCell(" + value + ")";
        }
    }
}
