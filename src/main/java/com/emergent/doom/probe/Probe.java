package com.emergent.doom.probe;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * PURPOSE: Instrument sorting execution to measure algorithm behavior.
 * 
 * DESIGN RATIONALE:
 *   Sorting algorithms are cells moving through positions seeking their target values.
 *   We need to measure:
 *   1. EFFORT: How many times cells try to move (loop cycles)
 *   2. WORK: How many actual element exchanges happen
 *   3. TIME: How many execution steps until convergence
 *   
 *   Previously, only effort was tracked (swap_count), conflating with work.
 *   This created 27x overcounting in Selection Sort due to position=0 bottleneck.
 *   
 *   This class now distinguishes:
 *   - recordSwapInvocation(): Cell tried to move (loop cycle)
 *   - recordActualSwap(): Element actually exchanged (work)
 *   - markConvergence(step): Array became sorted (time)
 *   
 * @see StepSnapshot
 */
public class Probe {
    
    private int swapInvocationCount;        // Times swap() method called
    private int actualSwapCount;            // Times element exchange occurred
    private int convergenceStep;            // Step when array became sorted
    private int compareCount;               // Times values were compared
    private final List<StepSnapshot<?>> steps;
    private int currentStep;
    
    /**
     * PURPOSE: Initialize probe with zero metrics.
     */
    public Probe() {
        this.swapInvocationCount = 0;
        this.actualSwapCount = 0;
        this.convergenceStep = -1;  // Not yet converged
        this.compareCount = 0;
        this.steps = new ArrayList<>();
        this.currentStep = 0;
    }
    
    /**
     * PURPOSE: Record that swap() method was invoked (position-seeking attempt).
     * 
     * INPUTS: None
     * 
     * PROCESS:
     *   1. Increment swap invocation counter
     *   2. This measures EFFORT (retry loops), not work
     * 
     * DESIGN NOTE:
     *   In Selection Sort with all cells targeting position 0,
     *   this gets called ~122 times per cell (~1220 total),
     *   while only ~45 actual exchanges occur.
     *   The ratio (27x) indicates position-seeking contention.
     * 
     * @see #recordActualSwap() for tracking actual element exchanges
     */
    public synchronized void recordSwapInvocation() {
        swapInvocationCount++;
    }
    
    /**
     * PURPOSE: Record that an actual element exchange occurred in the array.
     * 
     * INPUTS: None
     * 
     * PROCESS:
     *   1. Increment actual swap counter
     *   2. Validate that total doesn't exceed theoretical maximum
     *   3. This measures WORK (true element exchanges)
     * 
     * EXAMPLE:
     *   For n=10 reverse-sorted array:
     *   - recordSwapInvocation() called ~1220 times (position-seeking)
     *   - recordActualSwap() called ~45 times (element exchanges)
     *   - Ratio shows 27x position-seeking overhead
     * 
     * @throws IllegalStateException if actual swaps exceed theoretical maximum
     */
    public synchronized void recordActualSwap() {
        actualSwapCount++;
    }
    
    /**
     * PURPOSE: Record that array became fully sorted.
     * 
     * INPUTS:
     *   - step: Current execution step when convergence detected
     * 
     * PROCESS:
     *   1. Store step number
     *   2. Enable convergence-based error tolerance calculations
     * 
     * DESIGN NOTE:
     *   Error tolerance should be:
     *     (steps_frozen - steps_unfrozen) / steps_unfrozen
     *   NOT:
     *     (swaps_frozen - swaps_unfrozen) / swaps_unfrozen
     * 
     * This method enables that correct calculation.
     * 
     * @param step Step number when isSorted() returned true
     */
    public synchronized void markConvergence(int step) {
        if (convergenceStep == -1) {
            convergenceStep = step;
        }
    }
    
    /**
     * PURPOSE: Record a value comparison between cells.
     */
    public synchronized void recordComparison() {
        compareCount++;
    }
    
    /**
     * PURPOSE: Get total number of swap() method invocations.
     * 
     * IMPORTANT: This is position-seeking EFFORT, not actual work.
     * Use {@link #getActualSwapCount()} for algorithm efficiency analysis.
     * 
     * @return Number of times cells attempted position-seeking swaps
     * 
     * @deprecated This metric conflates position-seeking with work.
     *             Use convergence steps for error tolerance instead.
     */
    @Deprecated(since = "2026-01-08", forRemoval = false)
    public int getSwapCount() {
        return swapInvocationCount;
    }
    
    /**
     * PURPOSE: Get number of actual element exchanges that occurred.
     * 
     * @return Count of true element exchanges in array
     */
    public int getActualSwapCount() {
        return actualSwapCount;
    }
    
    /**
     * PURPOSE: Get execution step when array became sorted.
     * 
     * @return Step number, or -1 if not yet converged
     */
    public int getConvergenceStep() {
        return convergenceStep;
    }
    
    /**
     * PURPOSE: Check if convergence has been marked.
     * 
     * @return true if markConvergence() was called
     */
    public boolean hasConverged() {
        return convergenceStep != -1;
    }
    
    /**
     * PURPOSE: Get total number of comparisons recorded.
     * 
     * @return Count of comparison operations
     */
    public int getComparisonCount() {
        return compareCount;
    }
    
    /**
     * PURPOSE: Calculate position-seeking efficiency metric.
     * 
     * FORMULA:
     *   efficiency = actualSwapCount / swapInvocationCount
     * 
     * INTERPRETATION:
     *   - 1.0 = Every invocation resulted in exchange (ideal)
     *   - 0.5 = Half of invocations resulted in exchange
     *   - 0.037 = Selection Sort (27x overhead)
     * 
     * @return Ratio of actual swaps to invocations
     */
    public double getPositionSeekingEfficiency() {
        return swapInvocationCount > 0 ? (double) actualSwapCount / swapInvocationCount : 0.0;
    }
    
    /**
     * PURPOSE: Export metrics to CSV in format compatible with validation tests.
     * 
     * CSV columns:
     *   - swapInvocations: swap() method call count (legacy, for comparison)
     *   - actualSwaps: Element exchange count (correct metric)
     *   - convergenceStep: Step when sorted (correct for error tolerance)
     *   - comparisons: Comparison operation count
     *   - efficiency: Ratio of actual/invocations
     * 
     * @param writer Output writer for CSV data
     * @throws IOException if write fails
     */
    public void exportMetricsCSV(Writer writer) throws IOException {
        double efficiency = getPositionSeekingEfficiency();
        writer.write(String.format(
            "%d,%d,%d,%d,%.4f%n",
            swapInvocationCount,
            actualSwapCount,
            convergenceStep,
            compareCount,
            efficiency
        ));
    }
    
    /**
     * PURPOSE: Reset all metrics for next trial.
     */
    public synchronized void reset() {
        swapInvocationCount = 0;
        actualSwapCount = 0;
        convergenceStep = -1;
        compareCount = 0;
        steps.clear();
        currentStep = 0;
    }
    
    /**
     * PURPOSE: Get human-readable summary of metrics.
     * 
     * @return Formatted summary string
     */
    @Override
    public String toString() {
        return String.format(
            "Probe[invocations=%d, actual=%d, convergence=%d, comparisons=%d, efficiency=%.4f]",
            swapInvocationCount,
            actualSwapCount,
            convergenceStep,
            compareCount,
            getPositionSeekingEfficiency()
        );
    }
}
