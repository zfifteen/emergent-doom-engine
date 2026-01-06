package com.emergent.doom.traditional;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Random;

/**
 * Comprehensive test suite for TraditionalSortEngine and TraditionalSortMetrics.
 *
 * Tests verify:
 * - All three algorithms (Bubble, Insertion, Selection) correctly sort arrays
 * - Metrics tracking (comparisons, swaps, total operations) works correctly
 * - Edge cases (empty, single element, already sorted, reverse sorted)
 * - Algorithm-specific characteristics match expected behavior
 */
class TraditionalSortEngineTest {

    private TraditionalSortEngine<Integer> engine;

    @BeforeEach
    void setUp() {
        engine = new TraditionalSortEngine<>();
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    /**
     * Creates an Integer array from a varargs sequence of primitive integers.
     * This helper simplifies test setup by allowing concise array initialization
     * without explicit boxing (e.g., createArray(5, 2, 8) instead of new Integer[]{5, 2, 8}).
     *
     * @param values Variable number of int values to convert to Integer array
     * @return Integer array containing the boxed values in the same order
     */
    private Integer[] createArray(int... values) {
        Integer[] array = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * Verifies that an Integer array is sorted in ascending order.
     * Used to validate sort algorithm correctness after execution.
     * Returns true for arrays of length 0 or 1 (trivially sorted).
     *
     * @param array The Integer array to check for sorted order
     * @return true if array is sorted in ascending order, false otherwise
     */
    private boolean isSorted(Integer[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
    }

    // ========================================================================
    // Bubble Sort Tests
    // ========================================================================

    @Nested
    @DisplayName("Bubble Sort Tests")
    class BubbleSortTests {

        /**
         * PURPOSE: As a developer, I want to verify bubble sort correctly orders a random array
         * so that I can confirm the algorithm produces correct sorted output.
         *
         * INPUTS: Unsorted array [5, 2, 8, 1, 9]
         * EXPECTED OUTPUT: Array sorted to [1, 2, 5, 8, 9], sort returns successfully
         * TEST DATA: 5 elements with intentional disorder
         * REPRODUCTION: Create array, call engine.sort(array, "BUBBLE"), verify ascending order
         */
        @Test
        @DisplayName("Should sort random array correctly")
        void testBubbleSortRandom() {
            Integer[] array = createArray(5, 2, 8, 1, 9);
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array), "Array should be sorted");
            assertArrayEquals(createArray(1, 2, 5, 8, 9), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify bubble sort is optimized for already-sorted input
         * so that I can confirm the algorithm avoids unnecessary swaps when data is pre-ordered.
         *
         * INPUTS: Already sorted array [1, 2, 3, 4, 5]
         * EXPECTED OUTPUT: Array remains [1, 2, 3, 4, 5], zero swaps recorded
         * TEST DATA: 5 elements in ascending order
         * REPRODUCTION: Create sorted array, call engine.sort(array, "BUBBLE"), verify 0 swaps in metrics
         */
        @Test
        @DisplayName("Should handle already sorted array")
        void testBubbleSortAlreadySorted() {
            Integer[] array = createArray(1, 2, 3, 4, 5);
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array));
            // Already sorted should have minimal swaps
            assertEquals(0, engine.getMetrics().getSwapCount());
        }

        /**
         * PURPOSE: As a developer, I want to verify bubble sort handles worst-case reverse-sorted input
         * so that I can confirm the algorithm correctly reorders elements requiring maximum swaps.
         *
         * INPUTS: Reverse sorted array [5, 4, 3, 2, 1]
         * EXPECTED OUTPUT: Array sorted to [1, 2, 3, 4, 5]
         * TEST DATA: 5 elements in descending order (worst case for bubble sort)
         * REPRODUCTION: Create reversed array, call engine.sort(array, "BUBBLE"), verify ascending order
         */
        @Test
        @DisplayName("Should handle reverse sorted array")
        void testBubbleSortReversed() {
            Integer[] array = createArray(5, 4, 3, 2, 1);
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 3, 4, 5), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify bubble sort handles single-element edge case
         * so that I can confirm the algorithm doesn't fail on trivially sorted input.
         *
         * INPUTS: Single element array [42]
         * EXPECTED OUTPUT: Array unchanged [42], no errors thrown
         * TEST DATA: 1 element (trivially sorted)
         * REPRODUCTION: Create single-element array, call engine.sort(array, "BUBBLE"), verify unchanged
         */
        @Test
        @DisplayName("Should handle single element")
        void testBubbleSortSingleElement() {
            Integer[] array = createArray(42);
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(42), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify bubble sort handles empty array edge case
         * so that I can confirm the algorithm doesn't fail on zero-length input.
         *
         * INPUTS: Empty array []
         * EXPECTED OUTPUT: Array remains empty [], no errors thrown
         * TEST DATA: 0 elements
         * REPRODUCTION: Create empty array, call engine.sort(array, "BUBBLE"), verify length remains 0
         */
        @Test
        @DisplayName("Should handle empty array")
        void testBubbleSortEmpty() {
            Integer[] array = createArray();
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array));
            assertEquals(0, array.length);
        }

        /**
         * PURPOSE: As a developer, I want to verify bubble sort correctly handles duplicate values
         * so that I can confirm the algorithm maintains stability and correct ordering with repeated elements.
         *
         * INPUTS: Array with duplicates [3, 1, 2, 1, 3, 2]
         * EXPECTED OUTPUT: Array sorted to [1, 1, 2, 2, 3, 3] with duplicates grouped
         * TEST DATA: 6 elements with 3 pairs of duplicates
         * REPRODUCTION: Create array with duplicates, call engine.sort(array, "BUBBLE"), verify grouping
         */
        @Test
        @DisplayName("Should handle duplicates")
        void testBubbleSortDuplicates() {
            Integer[] array = createArray(3, 1, 2, 1, 3, 2);
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 1, 2, 2, 3, 3), array);
        }
    }

    // ========================================================================
    // Insertion Sort Tests
    // ========================================================================

    @Nested
    @DisplayName("Insertion Sort Tests")
    class InsertionSortTests {

        /**
         * PURPOSE: As a developer, I want to verify insertion sort correctly orders a random array
         * so that I can confirm the algorithm produces correct sorted output.
         *
         * INPUTS: Unsorted array [5, 2, 8, 1, 9]
         * EXPECTED OUTPUT: Array sorted to [1, 2, 5, 8, 9], sort returns successfully
         * TEST DATA: 5 elements with intentional disorder
         * REPRODUCTION: Create array, call engine.sort(array, "INSERTION"), verify ascending order
         */
        @Test
        @DisplayName("Should sort random array correctly")
        void testInsertionSortRandom() {
            Integer[] array = createArray(5, 2, 8, 1, 9);
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 5, 8, 9), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify insertion sort is optimized for already-sorted input
         * so that I can confirm the algorithm achieves O(n) best-case performance with no swaps.
         *
         * INPUTS: Already sorted array [1, 2, 3, 4, 5]
         * EXPECTED OUTPUT: Array remains [1, 2, 3, 4, 5], zero swaps recorded
         * TEST DATA: 5 elements in ascending order (best case for insertion sort)
         * REPRODUCTION: Create sorted array, call engine.sort(array, "INSERTION"), verify 0 swaps in metrics
         */
        @Test
        @DisplayName("Should handle already sorted array")
        void testInsertionSortAlreadySorted() {
            Integer[] array = createArray(1, 2, 3, 4, 5);
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            // Already sorted should have minimal swaps for insertion sort
            assertEquals(0, engine.getMetrics().getSwapCount());
        }

        /**
         * PURPOSE: As a developer, I want to verify insertion sort handles worst-case reverse-sorted input
         * so that I can confirm the algorithm correctly inserts elements requiring maximum shifts.
         *
         * INPUTS: Reverse sorted array [5, 4, 3, 2, 1]
         * EXPECTED OUTPUT: Array sorted to [1, 2, 3, 4, 5]
         * TEST DATA: 5 elements in descending order (worst case for insertion sort)
         * REPRODUCTION: Create reversed array, call engine.sort(array, "INSERTION"), verify ascending order
         */
        @Test
        @DisplayName("Should handle reverse sorted array")
        void testInsertionSortReversed() {
            Integer[] array = createArray(5, 4, 3, 2, 1);
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 3, 4, 5), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify insertion sort handles single-element edge case
         * so that I can confirm the algorithm doesn't fail on trivially sorted input.
         *
         * INPUTS: Single element array [42]
         * EXPECTED OUTPUT: Array unchanged [42], no errors thrown
         * TEST DATA: 1 element (trivially sorted)
         * REPRODUCTION: Create single-element array, call engine.sort(array, "INSERTION"), verify unchanged
         */
        @Test
        @DisplayName("Should handle single element")
        void testInsertionSortSingleElement() {
            Integer[] array = createArray(42);
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(42), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify insertion sort handles empty array edge case
         * so that I can confirm the algorithm doesn't fail on zero-length input.
         *
         * INPUTS: Empty array []
         * EXPECTED OUTPUT: Array remains empty [], no errors thrown
         * TEST DATA: 0 elements
         * REPRODUCTION: Create empty array, call engine.sort(array, "INSERTION"), verify length remains 0
         */
        @Test
        @DisplayName("Should handle empty array")
        void testInsertionSortEmpty() {
            Integer[] array = createArray();
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            assertEquals(0, array.length);
        }

        /**
         * PURPOSE: As a developer, I want to verify insertion sort correctly handles duplicate values
         * so that I can confirm the algorithm maintains stability with repeated elements in correct order.
         *
         * INPUTS: Array with duplicates [3, 1, 2, 1, 3, 2]
         * EXPECTED OUTPUT: Array sorted to [1, 1, 2, 2, 3, 3] preserving stability
         * TEST DATA: 6 elements with 3 pairs of duplicates
         * REPRODUCTION: Create array with duplicates, call engine.sort(array, "INSERTION"), verify grouping
         */
        @Test
        @DisplayName("Should handle duplicates")
        void testInsertionSortDuplicates() {
            Integer[] array = createArray(3, 1, 2, 1, 3, 2);
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 1, 2, 2, 3, 3), array);
        }
    }

    // ========================================================================
    // Selection Sort Tests
    // ========================================================================

    @Nested
    @DisplayName("Selection Sort Tests")
    class SelectionSortTests {

        /**
         * PURPOSE: As a developer, I want to verify selection sort correctly orders a random array
         * so that I can confirm the algorithm produces correct sorted output.
         *
         * INPUTS: Unsorted array [5, 2, 8, 1, 9]
         * EXPECTED OUTPUT: Array sorted to [1, 2, 5, 8, 9], sort returns successfully
         * TEST DATA: 5 elements with intentional disorder
         * REPRODUCTION: Create array, call engine.sort(array, "SELECTION"), verify ascending order
         */
        @Test
        @DisplayName("Should sort random array correctly")
        void testSelectionSortRandom() {
            Integer[] array = createArray(5, 2, 8, 1, 9);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 5, 8, 9), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify selection sort recognizes already-sorted input
         * so that I can confirm the algorithm avoids unnecessary swaps when data is pre-ordered.
         *
         * INPUTS: Already sorted array [1, 2, 3, 4, 5]
         * EXPECTED OUTPUT: Array remains [1, 2, 3, 4, 5], zero swaps recorded
         * TEST DATA: 5 elements in ascending order (best case for selection sort swap count)
         * REPRODUCTION: Create sorted array, call engine.sort(array, "SELECTION"), verify 0 swaps in metrics
         */
        @Test
        @DisplayName("Should handle already sorted array")
        void testSelectionSortAlreadySorted() {
            Integer[] array = createArray(1, 2, 3, 4, 5);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            // Already sorted should have 0 swaps for selection sort
            assertEquals(0, engine.getMetrics().getSwapCount());
        }

        /**
         * PURPOSE: As a developer, I want to verify selection sort handles reverse-sorted input efficiently
         * so that I can confirm the algorithm maintains minimal swap characteristic even in worst case.
         *
         * INPUTS: Reverse sorted array [5, 4, 3, 2, 1]
         * EXPECTED OUTPUT: Array sorted to [1, 2, 3, 4, 5]
         * TEST DATA: 5 elements in descending order
         * REPRODUCTION: Create reversed array, call engine.sort(array, "SELECTION"), verify ascending order
         */
        @Test
        @DisplayName("Should handle reverse sorted array")
        void testSelectionSortReversed() {
            Integer[] array = createArray(5, 4, 3, 2, 1);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 3, 4, 5), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify selection sort handles single-element edge case
         * so that I can confirm the algorithm doesn't fail on trivially sorted input.
         *
         * INPUTS: Single element array [42]
         * EXPECTED OUTPUT: Array unchanged [42], no errors thrown
         * TEST DATA: 1 element (trivially sorted)
         * REPRODUCTION: Create single-element array, call engine.sort(array, "SELECTION"), verify unchanged
         */
        @Test
        @DisplayName("Should handle single element")
        void testSelectionSortSingleElement() {
            Integer[] array = createArray(42);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(42), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify selection sort handles empty array edge case
         * so that I can confirm the algorithm doesn't fail on zero-length input.
         *
         * INPUTS: Empty array []
         * EXPECTED OUTPUT: Array remains empty [], no errors thrown
         * TEST DATA: 0 elements
         * REPRODUCTION: Create empty array, call engine.sort(array, "SELECTION"), verify length remains 0
         */
        @Test
        @DisplayName("Should handle empty array")
        void testSelectionSortEmpty() {
            Integer[] array = createArray();
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertEquals(0, array.length);
        }

        /**
         * PURPOSE: As a developer, I want to verify selection sort correctly handles duplicate values
         * so that I can confirm the algorithm orders repeated elements correctly.
         *
         * INPUTS: Array with duplicates [3, 1, 2, 1, 3, 2]
         * EXPECTED OUTPUT: Array sorted to [1, 1, 2, 2, 3, 3] with duplicates grouped
         * TEST DATA: 6 elements with 3 pairs of duplicates
         * REPRODUCTION: Create array with duplicates, call engine.sort(array, "SELECTION"), verify grouping
         */
        @Test
        @DisplayName("Should handle duplicates")
        void testSelectionSortDuplicates() {
            Integer[] array = createArray(3, 1, 2, 1, 3, 2);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 1, 2, 2, 3, 3), array);
        }

        /**
         * PURPOSE: As a developer, I want to verify selection sort exhibits its characteristic minimal swap behavior
         * so that I can confirm the algorithm performs at most n-1 swaps regardless of input distribution.
         *
         * INPUTS: Reverse sorted array [5, 4, 3, 2, 1]
         * EXPECTED OUTPUT: Array sorted with ≤4 swaps (n-1 for n=5)
         * TEST DATA: 5 elements in descending order
         * REPRODUCTION: Create reversed array, call engine.sort(array, "SELECTION"), verify swap count ≤ 4
         */
        @Test
        @DisplayName("Should have minimal swaps characteristic")
        void testSelectionSortMinimalSwaps() {
            Integer[] array = createArray(5, 4, 3, 2, 1);
            engine.sort(array, "SELECTION");
            
            // Selection sort should have at most n-1 swaps (one per position)
            assertTrue(engine.getMetrics().getSwapCount() <= array.length - 1,
                "Selection sort should have at most n-1 swaps");
        }
    }

    // ========================================================================
    // Metrics Tests
    // ========================================================================

    @Nested
    @DisplayName("Metrics Tracking Tests")
    class MetricsTests {

        /**
         * PURPOSE: As a developer, I want to verify the engine tracks comparison operations
         * so that I can analyze algorithm efficiency through comparison counts.
         *
         * INPUTS: Unsorted array [3, 1, 2] with bubble sort
         * EXPECTED OUTPUT: Metrics show comparison count > 0
         * TEST DATA: 3 elements requiring comparisons to sort
         * REPRODUCTION: Sort array, call engine.getMetrics().getComparisonCount(), verify > 0
         */
        @Test
        @DisplayName("Should track comparisons")
        void testComparisonTracking() {
            Integer[] array = createArray(3, 1, 2);
            engine.sort(array, "BUBBLE");
            
            assertTrue(engine.getMetrics().getComparisonCount() > 0,
                "Should record comparisons");
        }

        /**
         * PURPOSE: As a developer, I want to verify the engine tracks swap operations
         * so that I can analyze algorithm efficiency through swap counts.
         *
         * INPUTS: Unsorted array [3, 1, 2] with bubble sort
         * EXPECTED OUTPUT: Metrics show swap count > 0
         * TEST DATA: 3 elements requiring swaps to reach sorted state
         * REPRODUCTION: Sort array, call engine.getMetrics().getSwapCount(), verify > 0
         */
        @Test
        @DisplayName("Should track swaps")
        void testSwapTracking() {
            Integer[] array = createArray(3, 1, 2);
            engine.sort(array, "BUBBLE");
            
            assertTrue(engine.getMetrics().getSwapCount() > 0,
                "Should record swaps");
        }

        /**
         * PURPOSE: As a developer, I want to verify total operations equals comparisons plus swaps
         * so that I can validate metrics calculation correctness.
         *
         * INPUTS: Unsorted array [3, 1, 2] with bubble sort
         * EXPECTED OUTPUT: totalOperations == comparisonCount + swapCount
         * TEST DATA: 3 elements generating both comparisons and swaps
         * REPRODUCTION: Sort array, verify metrics.getTotalOperations() matches sum of other metrics
         */
        @Test
        @DisplayName("Should track total operations")
        void testTotalOperationsTracking() {
            Integer[] array = createArray(3, 1, 2);
            engine.sort(array, "BUBBLE");
            
            TraditionalSortMetrics metrics = engine.getMetrics();
            assertEquals(
                metrics.getComparisonCount() + metrics.getSwapCount(),
                metrics.getTotalOperations(),
                "Total operations should equal comparisons + swaps"
            );
        }

        /**
         * PURPOSE: As a developer, I want to verify metrics reset between successive sorts
         * so that I can ensure each sort operation starts with clean metric counters.
         *
         * INPUTS: Two separate sort operations on different arrays
         * EXPECTED OUTPUT: Second sort metrics don't accumulate first sort's counts
         * TEST DATA: First array [3,1,2], second array [5,4]
         * REPRODUCTION: Sort twice, verify second metrics reflect only second sort operation
         */
        @Test
        @DisplayName("Should reset metrics between sorts")
        void testMetricsReset() {
            Integer[] array1 = createArray(3, 1, 2);
            engine.sort(array1, "BUBBLE");
            int firstSwaps = engine.getMetrics().getSwapCount();
            
            Integer[] array2 = createArray(5, 4);
            engine.sort(array2, "BUBBLE");
            int secondSwaps = engine.getMetrics().getSwapCount();
            
            // Second sort should not include first sort's metrics
            assertTrue(secondSwaps < firstSwaps || secondSwaps == 1,
                "Metrics should reset between sorts");
        }

        /**
         * PURPOSE: As a developer, I want to verify selection sort's minimal swap characteristic
         * so that I can confirm it performs fewer swaps than bubble sort for identical input.
         *
         * INPUTS: Identical reverse-sorted arrays [5,4,3,2,1] for both algorithms
         * EXPECTED OUTPUT: Selection sort swap count < bubble sort swap count
         * TEST DATA: 5 elements in descending order (emphasizes swap difference)
         * REPRODUCTION: Sort with both algorithms, compare metrics.getSwapCount() values
         */
        @Test
        @DisplayName("Selection sort should have fewer swaps than bubble sort")
        void testSelectionSortEfficiency() {
            // Create identical unsorted arrays
            Integer[] bubbleArray = createArray(5, 4, 3, 2, 1);
            Integer[] selectionArray = createArray(5, 4, 3, 2, 1);
            
            engine.sort(bubbleArray, "BUBBLE");
            int bubbleSwaps = engine.getMetrics().getSwapCount();
            
            engine.sort(selectionArray, "SELECTION");
            int selectionSwaps = engine.getMetrics().getSwapCount();
            
            assertTrue(selectionSwaps < bubbleSwaps,
                "Selection sort should have fewer swaps than bubble sort for reverse-sorted array");
        }
    }

    // ========================================================================
    // Error Handling Tests
    // ========================================================================

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        /**
         * PURPOSE: As a developer, I want invalid algorithm names to throw exceptions
         * so that I can catch configuration errors early with clear error messages.
         *
         * INPUTS: Array [3,1,2] with invalid algorithm name "INVALID"
         * EXPECTED OUTPUT: IllegalArgumentException thrown
         * TEST DATA: Valid array, invalid algorithm string
         * REPRODUCTION: Call engine.sort(array, "INVALID"), expect exception
         */
        @Test
        @DisplayName("Should throw exception for invalid algorithm name")
        void testInvalidAlgorithm() {
            Integer[] array = createArray(3, 1, 2);
            
            assertThrows(IllegalArgumentException.class, () -> {
                engine.sort(array, "INVALID");
            }, "Should throw exception for invalid algorithm");
        }

        /**
         * PURPOSE: As a developer, I want case-insensitive algorithm name matching
         * so that I can accept user input in any case format without preprocessing.
         *
         * INPUTS: Array [3,1,2] with lowercase algorithm name "bubble"
         * EXPECTED OUTPUT: Sort succeeds, array correctly sorted
         * TEST DATA: Valid array, lowercase algorithm string
         * REPRODUCTION: Call engine.sort(array, "bubble"), verify successful sort
         */
        @Test
        @DisplayName("Should handle case-insensitive algorithm names")
        void testCaseInsensitiveAlgorithm() {
            Integer[] array = createArray(3, 1, 2);
            
            assertDoesNotThrow(() -> {
                engine.sort(array, "bubble");
            }, "Should accept lowercase algorithm name");
            
            assertTrue(isSorted(array));
        }
    }

    // ========================================================================
    // Larger Array Tests
    // ========================================================================

    @Nested
    @DisplayName("Larger Array Tests")
    class LargerArrayTests {

        /**
         * PURPOSE: As a developer, I want to verify bubble sort scales to larger arrays
         * so that I can confirm the algorithm works correctly beyond small test cases.
         *
         * INPUTS: Random array of 100 elements (values 0-99, seed=42)
         * EXPECTED OUTPUT: Array sorted in ascending order
         * TEST DATA: 100 elements with reproducible randomization
         * REPRODUCTION: Create 100-element random array, call engine.sort(array, "BUBBLE"), verify isSorted()
         */
        @Test
        @DisplayName("Should sort array of 100 elements with bubble sort")
        void testBubbleSortLargeArray() {
            Integer[] array = createRandomArray(100);
            engine.sort(array, "BUBBLE");
            assertTrue(isSorted(array));
        }

        /**
         * PURPOSE: As a developer, I want to verify insertion sort scales to larger arrays
         * so that I can confirm the algorithm handles realistic data sizes correctly.
         *
         * INPUTS: Random array of 100 elements (values 0-99, seed=42)
         * EXPECTED OUTPUT: Array sorted in ascending order
         * TEST DATA: 100 elements with reproducible randomization
         * REPRODUCTION: Create 100-element random array, call engine.sort(array, "INSERTION"), verify isSorted()
         */
        @Test
        @DisplayName("Should sort array of 100 elements with insertion sort")
        void testInsertionSortLargeArray() {
            Integer[] array = createRandomArray(100);
            engine.sort(array, "INSERTION");
            assertTrue(isSorted(array));
        }

        /**
         * PURPOSE: As a developer, I want to verify selection sort scales to larger arrays
         * so that I can confirm the minimal-swap characteristic holds at realistic sizes.
         *
         * INPUTS: Random array of 100 elements (values 0-99, seed=42)
         * EXPECTED OUTPUT: Array sorted in ascending order
         * TEST DATA: 100 elements with reproducible randomization
         * REPRODUCTION: Create 100-element random array, call engine.sort(array, "SELECTION"), verify isSorted()
         */
        @Test
        @DisplayName("Should sort array of 100 elements with selection sort")
        void testSelectionSortLargeArray() {
            Integer[] array = createRandomArray(100);
            engine.sort(array, "SELECTION");
            assertTrue(isSorted(array));
        }

        /**
         * Creates a random Integer array for testing sorting algorithms with larger datasets.
         * Uses a fixed seed (42) to ensure reproducible test results across runs.
         * Values range from 0 to 99 to simulate realistic unsorted distributions.
         *
         * @param size The number of elements to generate in the array
         * @return Integer array containing random values from 0 to 99
         */
        private Integer[] createRandomArray(int size) {
            Random random = new Random(42); // Fixed seed for reproducibility
            Integer[] array = new Integer[size];
            for (int i = 0; i < size; i++) {
                array[i] = random.nextInt(100);
            }
            return array;
        }
    }
}
