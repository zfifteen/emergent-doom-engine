package com.emergent.doom.traditional;

/**
 * Traditional (top-down) sorting algorithm engine for comparison studies.
 * 
 * <p>This class implements standard bubble sort, insertion sort, and selection sort
 * algorithms as described in the Levin et al. paper (p.6-7, p.10). These traditional
 * implementations serve as a baseline for comparing against cell-view (emergent) approaches.</p>
 * 
 * <p><strong>Key Differences from Cell-View:</strong></p>
 * <ul>
 *   <li><strong>Global Knowledge:</strong> Traditional algorithms have full array visibility,
 *       while cell-view agents only see local neighbors</li>
 *   <li><strong>Centralized Control:</strong> Single controller directs all operations,
 *       vs. distributed decision-making in cell-view</li>
 *   <li><strong>Deterministic:</strong> Traditional algorithms produce identical results,
 *       while cell-view has emergent, non-deterministic behavior</li>
 *   <li><strong>Sequential:</strong> Operations happen in strict order,
 *       vs. parallel cell threads in cell-view</li>
 * </ul>
 * 
 * <p><strong>Paper Reference:</strong> Levin et al. (2024) compares traditional vs cell-view
 * efficiency (Table 1, p.10), showing Selection sort is ~10x slower in cell-view due to
 * lack of global knowledge, while Bubble and Insertion maintain comparable performance.</p>
 * 
 * @param <T> the type of comparable elements to sort
 */
public class TraditionalSortEngine<T extends Comparable<T>> {
    
    // PURPOSE: Track metrics (comparisons, swaps, total operations) during sorting
    // REASONING: Enables dual cost model analysis per paper (p.10).
    //            Must be accessible across all algorithm methods to record operations.
    // DATA FLOW: Initialized in constructor, updated by compareAndTrack() and swapAndTrack(),
    //            read by getMetrics()
    // INTEGRATION: Provides same metrics as cell-view Probe for fair comparison
    private final TraditionalSortMetrics metrics;
    
    /**
     * Constructor - creates engine with new metrics tracker.
     * 
     * PURPOSE: Initialize traditional sort engine with fresh metrics
     * INPUTS: None
     * PROCESS: Create new TraditionalSortMetrics instance
     * OUTPUTS: New TraditionalSortEngine instance
     * DEPENDENCIES: TraditionalSortMetrics class
     * INTEGRATION: Called by test cases and experiment runners
     * 
     * IMPLEMENTATION NOTE (Phase Two): This is the main entry point for creating
     * a traditional sort engine. Each engine instance has its own metrics tracker
     * to ensure thread-safety and isolated metric collection across experiments.
     */
    public TraditionalSortEngine() {
        this.metrics = new TraditionalSortMetrics();
    }
    
    /**
     * Main sorting dispatcher - routes to specific algorithm implementation.
     * 
     * PURPOSE: Provide unified interface for all three traditional sorting algorithms
     * INPUTS:
     *   - array: T[] - array of comparable elements to sort (modified in-place)
     *   - algorithm: String - "BUBBLE", "INSERTION", or "SELECTION"
     * PROCESS:
     *   1. Reset metrics to zero
     *   2. Validate algorithm name
     *   3. Route to bubbleSort(), insertionSort(), or selectionSort()
     *   4. Return when array is fully sorted
     * OUTPUTS: void (array is modified in-place, metrics updated)
     * DEPENDENCIES: bubbleSort(), insertionSort(), selectionSort() methods
     * INTEGRATION: Main entry point for experiments comparing traditional vs cell-view
     * THROWS: IllegalArgumentException if algorithm name is invalid
     * 
     * IMPLEMENTATION NOTE (Phase Two): This dispatcher method coordinates the sorting
     * operation. It ensures metrics are reset before each sort and routes to the
     * appropriate algorithm implementation. The algorithm name is case-insensitive
     * for convenience.
     */
    public void sort(T[] array, String algorithm) {
        // Reset metrics before starting new sort
        metrics.reset();
        
        // Normalize algorithm name to uppercase for matching
        String algoUpper = algorithm.toUpperCase();
        
        // Route to appropriate algorithm implementation
        switch (algoUpper) {
            case "BUBBLE":
                bubbleSort(array);
                break;
            case "INSERTION":
                insertionSort(array);
                break;
            case "SELECTION":
                selectionSort(array);
                break;
            default:
                throw new IllegalArgumentException(
                    "Unknown algorithm: " + algorithm + 
                    ". Valid options are: BUBBLE, INSERTION, SELECTION"
                );
        }
    }
    
    /**
     * Traditional bubble sort implementation.
     * 
     * PURPOSE: Implement standard bubble sort with comparison/swap tracking
     * INPUTS: array - T[] - array to sort in-place (ascending order)
     * PROCESS:
     *   1. Repeat until no swaps occur in a full pass:
     *      a. For each adjacent pair (i, i+1):
     *         - Call compareAndTrack(array, i, i+1)
     *         - If array[i] > array[i+1], call swapAndTrack(array, i, i+1)
     *   2. Algorithm terminates when pass completes with zero swaps
     * OUTPUTS: void (array sorted, metrics updated)
     * DEPENDENCIES: compareAndTrack(), swapAndTrack()
     * INTEGRATION: Matches cell-view BubbleSortCell behavior for comparison
     * NOTE: Paper (p.10, Table 1) shows traditional bubble ~2500 swaps vs cell-view ~2500
     * 
     * IMPLEMENTATION NOTE (Phase Three - bubbleSort): This is the classic bubble
     * sort algorithm where larger elements "bubble up" to the right through
     * successive adjacent comparisons and swaps. The algorithm continues until
     * a complete pass is made with no swaps, indicating the array is sorted.
     * Unlike cell-view bubble sort which has randomized direction choice, this
     * traditional implementation always scans left-to-right deterministically.
     */
    private void bubbleSort(T[] array) {
        int n = array.length;
        boolean swapped;
        
        // Continue until no swaps occur in a full pass
        do {
            swapped = false;
            
            // Compare and potentially swap each adjacent pair
            for (int i = 0; i < n - 1; i++) {
                // Compare array[i] with array[i+1]
                if (compareAndTrack(array, i, i + 1) > 0) {
                    // array[i] > array[i+1], so swap them
                    swapAndTrack(array, i, i + 1);
                    swapped = true;
                }
            }
            
            // Optimization: after each pass, the largest unsorted element
            // is in its final position, so we can reduce the scan range
            n--;
            
        } while (swapped);
    }
    
    /**
     * Traditional insertion sort implementation.
     * 
     * PURPOSE: Implement standard insertion sort with comparison/swap tracking
     * INPUTS: array - T[] - array to sort in-place (ascending order)
     * PROCESS:
     *   1. For i from 1 to array.length-1:
     *      a. Set key = array[i]
     *      b. Set j = i - 1
     *      c. While j >= 0 AND compareAndTrack(key, array[j]) shows key < array[j]:
     *         - Shift array[j] one position right (counts as swap)
     *         - Decrement j
     *      d. Insert key at position j+1
     *   2. Left portion always sorted, right portion gets inserted one by one
     * OUTPUTS: void (array sorted, metrics updated)
     * DEPENDENCIES: compareAndTrack(), swapAndTrack()
     * INTEGRATION: Matches cell-view InsertionSortCell behavior for comparison
     * NOTE: Paper (p.10, Table 1) shows traditional insertion ~2500 swaps vs cell-view ~2500
     * 
     * IMPLEMENTATION NOTE (Phase Three - insertionSort): This is the classic
     * insertion sort algorithm where elements from the unsorted portion are
     * inserted into their correct position in the sorted portion. The sorted
     * portion grows from left to right. Unlike cell-view insertion sort which
     * checks if left portion is sorted before moving, this traditional
     * implementation maintains the sorted invariant by construction.
     */
    private void insertionSort(T[] array) {
        int n = array.length;
        
        // Iterate through unsorted portion (starting at index 1)
        for (int i = 1; i < n; i++) {
            T key = array[i];
            int j = i - 1;
            
            // Shift elements of sorted portion that are greater than key
            // to one position ahead of their current position
            while (j >= 0) {
                metrics.recordComparison();
                if (array[j].compareTo(key) > 0) {
                    // array[j] > key, so shift array[j] right
                    array[j + 1] = array[j];
                    metrics.recordSwap(); // Count the shift as a swap
                    j--;
                } else {
                    // Found correct position
                    break;
                }
            }
            
            // Insert key at its correct position
            array[j + 1] = key;
            // Note: If key didn't move (j == i-1), no swap is counted
            // If key moved, the shifts already counted as swaps
        }
    }
    
    /**
     * Traditional selection sort implementation.
     * 
     * PURPOSE: Implement standard selection sort with comparison/swap tracking
     * INPUTS: array - T[] - array to sort in-place (ascending order)
     * PROCESS:
     *   1. For i from 0 to array.length-2:
     *      a. Set minIndex = i
     *      b. For j from i+1 to array.length-1:
     *         - Call compareAndTrack(array, j, minIndex)
     *         - If array[j] < array[minIndex], set minIndex = j
     *      c. If minIndex != i, call swapAndTrack(array, i, minIndex)
     *   2. Left portion always sorted, builds sorted region from left to right
     * OUTPUTS: void (array sorted, metrics updated)
     * DEPENDENCIES: compareAndTrack(), swapAndTrack()
     * INTEGRATION: Matches cell-view SelectionSortCell behavior for comparison
     * NOTE: Paper (p.10, Table 1) shows traditional selection ~100 swaps vs cell-view ~1100
     *       This 10x difference is key finding - cell-view lacks global minimum knowledge
     * 
     * IMPLEMENTATION NOTE (Phase Three - selectionSort): This is the classic
     * selection sort algorithm where the minimum element from the unsorted
     * portion is selected and placed at the beginning of the unsorted portion.
     * Traditional selection sort has a key advantage: it uses global knowledge
     * to find the minimum in O(n) comparisons with only 1 swap per pass.
     * Cell-view selection sort lacks this global view, leading to ~10x more
     * swaps as shown in the paper. This dramatically illustrates the cost of
     * distributed decision-making without global coordination.
     */
    private void selectionSort(T[] array) {
        int n = array.length;
        
        // Build sorted portion from left to right
        for (int i = 0; i < n - 1; i++) {
            // Find minimum element in unsorted portion
            int minIndex = i;
            
            for (int j = i + 1; j < n; j++) {
                // Compare array[j] with current minimum
                if (compareAndTrack(array, j, minIndex) < 0) {
                    // Found new minimum
                    minIndex = j;
                }
            }
            
            // Swap minimum element to its correct position (if needed)
            if (minIndex != i) {
                swapAndTrack(array, i, minIndex);
            }
        }
    }
    
    /**
     * Compare two array elements and record the comparison in metrics.
     * 
     * PURPOSE: Perform comparison while tracking "reading" cost in dual cost model
     * INPUTS:
     *   - array: T[] - the array containing elements
     *   - i: int - index of first element
     *   - j: int - index of second element
     * PROCESS:
     *   1. Record comparison in metrics
     *   2. Perform array[i].compareTo(array[j])
     *   3. Return comparison result
     * OUTPUTS: int - negative if array[i] < array[j], 0 if equal, positive if array[i] > array[j]
     * DEPENDENCIES: TraditionalSortMetrics.recordComparison()
     * INTEGRATION: Called by all sorting algorithms to track comparison cost
     * NOTE: Every comparison is explicit in traditional algorithms, unlike cell-view
     * 
     * IMPLEMENTATION NOTE (Phase Three - compareAndTrack): This is a critical
     * helper method that wraps the standard compareTo() operation with metrics
     * tracking. By recording every comparison, we capture the "reading" cost
     * in the dual cost model. This enables fair comparison with cell-view
     * implementations where comparisons are also tracked.
     */
    private int compareAndTrack(T[] array, int i, int j) {
        metrics.recordComparison();
        return array[i].compareTo(array[j]);
    }
    
    /**
     * Swap two array elements and record the swap in metrics.
     * 
     * PURPOSE: Perform swap while tracking "writing" cost in dual cost model
     * INPUTS:
     *   - array: T[] - the array containing elements (modified in-place)
     *   - i: int - index of first element
     *   - j: int - index of second element
     * PROCESS:
     *   1. Record swap in metrics
     *   2. Exchange array[i] and array[j]
     * OUTPUTS: void (array modified, metrics updated)
     * DEPENDENCIES: TraditionalSortMetrics.recordSwap()
     * INTEGRATION: Called by all sorting algorithms to track swap cost
     * NOTE: Traditional swaps are atomic and immediate, unlike cell-view's threaded swaps
     * 
     * IMPLEMENTATION NOTE (Phase Three - swapAndTrack): This helper method wraps
     * the standard array element exchange with metrics tracking. By recording
     * every swap, we capture the "writing" cost in the dual cost model. Swaps
     * are the primary metric for efficiency comparisons (see paper Table 1, p.10).
     * The swap is performed using a temporary variable for clarity.
     */
    private void swapAndTrack(T[] array, int i, int j) {
        metrics.recordSwap();
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    
    /**
     * Get current metrics (comparisons, swaps, total operations).
     * 
     * PURPOSE: Retrieve metrics for analysis and reporting
     * INPUTS: None
     * PROCESS: Return reference to metrics object
     * OUTPUTS: TraditionalSortMetrics - current metrics state
     * DEPENDENCIES: metrics must be initialized
     * INTEGRATION: Used by test cases and experiment runners to compare with cell-view
     * 
     * IMPLEMENTATION NOTE (Phase Two): Returns a reference to the internal metrics
     * object, allowing callers to query comparison count, swap count, and total
     * operations after a sort completes.
     */
    public TraditionalSortMetrics getMetrics() {
        return metrics;
    }
}
