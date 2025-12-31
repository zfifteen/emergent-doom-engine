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

    private Integer[] createArray(int... values) {
        Integer[] array = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = values[i];
        }
        return array;
    }

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

        @Test
        @DisplayName("Should sort random array correctly")
        void testBubbleSortRandom() {
            Integer[] array = createArray(5, 2, 8, 1, 9);
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array), "Array should be sorted");
            assertArrayEquals(createArray(1, 2, 5, 8, 9), array);
        }

        @Test
        @DisplayName("Should handle already sorted array")
        void testBubbleSortAlreadySorted() {
            Integer[] array = createArray(1, 2, 3, 4, 5);
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array));
            // Already sorted should have minimal swaps
            assertEquals(0, engine.getMetrics().getSwapCount());
        }

        @Test
        @DisplayName("Should handle reverse sorted array")
        void testBubbleSortReversed() {
            Integer[] array = createArray(5, 4, 3, 2, 1);
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 3, 4, 5), array);
        }

        @Test
        @DisplayName("Should handle single element")
        void testBubbleSortSingleElement() {
            Integer[] array = createArray(42);
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(42), array);
        }

        @Test
        @DisplayName("Should handle empty array")
        void testBubbleSortEmpty() {
            Integer[] array = createArray();
            engine.sort(array, "BUBBLE");
            
            assertTrue(isSorted(array));
            assertEquals(0, array.length);
        }

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

        @Test
        @DisplayName("Should sort random array correctly")
        void testInsertionSortRandom() {
            Integer[] array = createArray(5, 2, 8, 1, 9);
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 5, 8, 9), array);
        }

        @Test
        @DisplayName("Should handle already sorted array")
        void testInsertionSortAlreadySorted() {
            Integer[] array = createArray(1, 2, 3, 4, 5);
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            // Already sorted should have minimal swaps for insertion sort
            assertEquals(0, engine.getMetrics().getSwapCount());
        }

        @Test
        @DisplayName("Should handle reverse sorted array")
        void testInsertionSortReversed() {
            Integer[] array = createArray(5, 4, 3, 2, 1);
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 3, 4, 5), array);
        }

        @Test
        @DisplayName("Should handle single element")
        void testInsertionSortSingleElement() {
            Integer[] array = createArray(42);
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(42), array);
        }

        @Test
        @DisplayName("Should handle empty array")
        void testInsertionSortEmpty() {
            Integer[] array = createArray();
            engine.sort(array, "INSERTION");
            
            assertTrue(isSorted(array));
            assertEquals(0, array.length);
        }

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

        @Test
        @DisplayName("Should sort random array correctly")
        void testSelectionSortRandom() {
            Integer[] array = createArray(5, 2, 8, 1, 9);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 5, 8, 9), array);
        }

        @Test
        @DisplayName("Should handle already sorted array")
        void testSelectionSortAlreadySorted() {
            Integer[] array = createArray(1, 2, 3, 4, 5);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            // Already sorted should have 0 swaps for selection sort
            assertEquals(0, engine.getMetrics().getSwapCount());
        }

        @Test
        @DisplayName("Should handle reverse sorted array")
        void testSelectionSortReversed() {
            Integer[] array = createArray(5, 4, 3, 2, 1);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 2, 3, 4, 5), array);
        }

        @Test
        @DisplayName("Should handle single element")
        void testSelectionSortSingleElement() {
            Integer[] array = createArray(42);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(42), array);
        }

        @Test
        @DisplayName("Should handle empty array")
        void testSelectionSortEmpty() {
            Integer[] array = createArray();
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertEquals(0, array.length);
        }

        @Test
        @DisplayName("Should handle duplicates")
        void testSelectionSortDuplicates() {
            Integer[] array = createArray(3, 1, 2, 1, 3, 2);
            engine.sort(array, "SELECTION");
            
            assertTrue(isSorted(array));
            assertArrayEquals(createArray(1, 1, 2, 2, 3, 3), array);
        }

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

        @Test
        @DisplayName("Should track comparisons")
        void testComparisonTracking() {
            Integer[] array = createArray(3, 1, 2);
            engine.sort(array, "BUBBLE");
            
            assertTrue(engine.getMetrics().getComparisonCount() > 0,
                "Should record comparisons");
        }

        @Test
        @DisplayName("Should track swaps")
        void testSwapTracking() {
            Integer[] array = createArray(3, 1, 2);
            engine.sort(array, "BUBBLE");
            
            assertTrue(engine.getMetrics().getSwapCount() > 0,
                "Should record swaps");
        }

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

        @Test
        @DisplayName("Should throw exception for invalid algorithm name")
        void testInvalidAlgorithm() {
            Integer[] array = createArray(3, 1, 2);
            
            assertThrows(IllegalArgumentException.class, () -> {
                engine.sort(array, "INVALID");
            }, "Should throw exception for invalid algorithm");
        }

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

        @Test
        @DisplayName("Should sort array of 100 elements with bubble sort")
        void testBubbleSortLargeArray() {
            Integer[] array = createRandomArray(100);
            engine.sort(array, "BUBBLE");
            assertTrue(isSorted(array));
        }

        @Test
        @DisplayName("Should sort array of 100 elements with insertion sort")
        void testInsertionSortLargeArray() {
            Integer[] array = createRandomArray(100);
            engine.sort(array, "INSERTION");
            assertTrue(isSorted(array));
        }

        @Test
        @DisplayName("Should sort array of 100 elements with selection sort")
        void testSelectionSortLargeArray() {
            Integer[] array = createRandomArray(100);
            engine.sort(array, "SELECTION");
            assertTrue(isSorted(array));
        }

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
