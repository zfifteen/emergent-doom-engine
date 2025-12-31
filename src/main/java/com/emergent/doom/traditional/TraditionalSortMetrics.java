package com.emergent.doom.traditional;

/**
 * Metrics tracker for traditional (top-down) sorting algorithms.
 * 
 * <p>This class implements the dual cost model described in the Levin et al. paper (p.10),
 * which analyzes efficiency by counting "both reading (comparison) and writing (swapping)" operations.</p>
 * 
 * <p><strong>Purpose:</strong> Enable comparison between cell-view (emergent) and traditional
 * (top-down) sorting approaches by tracking the same metrics used in the paper.</p>
 * 
 * <p><strong>Design Philosophy:</strong> Traditional algorithms have global visibility and 
 * control, unlike cell-view algorithms where each cell has only local knowledge. This class
 * captures the fundamental difference in computational cost between these paradigms.</p>
 * 
 * @see com.emergent.doom.probe.Probe for cell-view metrics tracking
 */
public class TraditionalSortMetrics {
    
    // PURPOSE: Track total number of element comparisons performed
    // REASONING: Comparison operations represent "reading" cost in the dual cost model.
    //            In traditional algorithms, comparisons are explicit and deterministic.
    //            This differs from cell-view where comparisons are distributed across threads.
    // DATA FLOW: Incremented by compareAndTrack() method during sorting operations
    // INTEGRATION: Used for performance analysis and comparison with cell-view implementations
    private int comparisonCount;
    
    // PURPOSE: Track total number of element swaps (position exchanges) performed
    // REASONING: Swap operations represent "writing" cost in the dual cost model.
    //            Each swap involves reading two elements and writing them to new positions.
    //            This is the primary metric for comparing traditional vs cell-view efficiency.
    // DATA FLOW: Incremented by swapAndTrack() method during sorting operations
    // INTEGRATION: Corresponds to swap_count in cell_research StatusProbe
    private int swapCount;
    
    // PURPOSE: Track total combined operations (comparisons + swaps)
    // REASONING: The paper (p.10) analyzes total computational cost, not just swaps alone.
    //            This provides a more complete picture of algorithm efficiency.
    // DATA FLOW: Derived value = comparisonCount + swapCount
    // INTEGRATION: Used for direct comparison with cell-view "total steps" metric
    private int totalOperations;
    
    /**
     * Constructor - initializes all metrics to zero.
     * 
     * PURPOSE: Create a new metrics tracker for a sorting operation
     * INPUTS: None
     * PROCESS: Initialize all counters to 0
     * OUTPUTS: New TraditionalSortMetrics instance
     * DEPENDENCIES: None
     * INTEGRATION: Called at the start of each traditional sort operation
     * 
     * IMPLEMENTATION NOTE (Phase Two): This is the main entry point for creating
     * a metrics tracker. All counters start at zero to provide a clean baseline
     * for each sorting operation.
     */
    public TraditionalSortMetrics() {
        this.comparisonCount = 0;
        this.swapCount = 0;
        this.totalOperations = 0;
    }
    
    /**
     * Record a comparison operation between two array elements.
     * 
     * PURPOSE: Track a single comparison operation and update total operation count
     * INPUTS: None (just increments counter)
     * PROCESS:
     *   1. Increment comparisonCount by 1
     *   2. Recalculate totalOperations
     * OUTPUTS: None (updates internal state)
     * DEPENDENCIES: None
     * INTEGRATION: Called by TraditionalSortEngine.compareAndTrack()
     * NOTE: This represents the "reading" cost in the dual cost model
     * 
     * IMPLEMENTATION NOTE (Phase Three - recordComparison): This method tracks
     * each comparison operation as it occurs. The totalOperations field is
     * maintained incrementally to avoid repeated recalculation. Each comparison
     * contributes 1 unit to the total operation cost.
     */
    public void recordComparison() {
        comparisonCount++;
        totalOperations++;
    }
    
    /**
     * Record a swap operation between two array elements.
     * 
     * PURPOSE: Track a single swap operation and update total operation count
     * INPUTS: None (just increments counter)
     * PROCESS:
     *   1. Increment swapCount by 1
     *   2. Recalculate totalOperations
     * OUTPUTS: None (updates internal state)
     * DEPENDENCIES: None
     * INTEGRATION: Called by TraditionalSortEngine.swapAndTrack()
     * NOTE: This represents the "writing" cost in the dual cost model
     * 
     * IMPLEMENTATION NOTE (Phase Three - recordSwap): This method tracks each
     * swap operation. Swaps are the primary metric for comparing traditional
     * vs cell-view implementations, as shown in the paper (Table 1, p.10).
     * Each swap contributes 1 unit to the total operation cost.
     */
    public void recordSwap() {
        swapCount++;
        totalOperations++;
    }
    
    /**
     * Get the total number of comparisons performed.
     * 
     * PURPOSE: Retrieve comparison count for analysis and reporting
     * INPUTS: None
     * PROCESS: Return current comparisonCount value
     * OUTPUTS: int - total comparisons performed
     * DEPENDENCIES: recordComparison() must have been called during sorting
     * INTEGRATION: Used by test cases and experiment runners
     * 
     * IMPLEMENTATION NOTE (Phase Two): Simple getter that provides read-only
     * access to the comparison count. This enables analysis without exposing
     * internal mutability.
     */
    public int getComparisonCount() {
        return comparisonCount;
    }
    
    /**
     * Get the total number of swaps performed.
     * 
     * PURPOSE: Retrieve swap count for analysis and reporting
     * INPUTS: None
     * PROCESS: Return current swapCount value
     * OUTPUTS: int - total swaps performed
     * DEPENDENCIES: recordSwap() must have been called during sorting
     * INTEGRATION: Used for direct comparison with cell-view swap counts
     * 
     * IMPLEMENTATION NOTE (Phase Two): This is the primary metric for comparing
     * traditional vs cell-view efficiency, corresponding directly to the swap_count
     * in cell_research's StatusProbe.
     */
    public int getSwapCount() {
        return swapCount;
    }
    
    /**
     * Get the total number of operations (comparisons + swaps).
     * 
     * PURPOSE: Retrieve combined operation count for efficiency analysis
     * INPUTS: None
     * PROCESS: Return totalOperations (= comparisonCount + swapCount)
     * OUTPUTS: int - total operations performed
     * DEPENDENCIES: recordComparison() and recordSwap() must track operations
     * INTEGRATION: Enables dual cost model analysis per Levin et al. paper (p.10)
     * 
     * IMPLEMENTATION NOTE (Phase Two): This getter returns the cached total
     * operations count which is maintained incrementally by recordComparison()
     * and recordSwap() methods.
     */
    public int getTotalOperations() {
        return totalOperations;
    }
    
    /**
     * Reset all metrics to zero.
     * 
     * PURPOSE: Clear metrics for reuse in multiple sorting operations
     * INPUTS: None
     * PROCESS:
     *   1. Set comparisonCount = 0
     *   2. Set swapCount = 0
     *   3. Set totalOperations = 0
     * OUTPUTS: None (modifies internal state)
     * DEPENDENCIES: None
     * INTEGRATION: Called between experiment runs
     * 
     * IMPLEMENTATION NOTE (Phase Three - reset): This method allows the same
     * metrics tracker to be reused across multiple sort operations without
     * creating new objects. This is important for experiment runners that
     * perform many sorting trials.
     */
    public void reset() {
        comparisonCount = 0;
        swapCount = 0;
        totalOperations = 0;
    }
}
