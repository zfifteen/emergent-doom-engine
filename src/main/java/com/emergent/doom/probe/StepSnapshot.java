package com.emergent.doom.probe;

import java.util.ArrayList;
import java.util.List;

/**
 * Captures a single step's state within the sorting execution.
 * 
 * <p><strong>Distinction Between Loop Cycles and Actual Swaps:</strong></p>
 * <ul>
 *   <li><code>loopCycleCount</code>: Number of times cell.move() was called,
 *       which increments for each position-seeking attempt. In Selection Sort
 *       where all cells target position 0, this creates massive contention and
 *       retry loops, producing 27× overcounting vs actual exchanges.</li>
 *   <li><code>actualSwapCount</code>: Number of actual element exchanges that
 *       occurred in the array. For n=10 reverse-sorted, this should be ~45
 *       regardless of algorithm.</li>
 * </ul>
 * 
 * <p><strong>Why This Matters:</strong></p>
 * Previously, only loop cycles were tracked (swapCount), producing invalid
 * metrics where error tolerance was calculated as:
 * <pre>
 * errorTolerance = (loopCycles_frozen - loopCycles_unfrozen) / loopCycles_unfrozen
 * </pre>
 * 
 * This measured position-seeking retry overhead, not algorithmic cost.
 * Corrected error tolerance now uses convergence steps:
 * <pre>
 * errorTolerance = (steps_frozen - steps_unfrozen) / steps_unfrozen
 * </pre>
 * 
 * @see com.emergent.doom.probe.Probe
 */
public class StepSnapshot<T> {
    
    private final int stepNumber;
    private final int loopCycleCount;       // swap() method invocations (position-seeking loops)
    private final int actualSwapCount;      // Actual element exchanges in array
    private final int convergenceStep;      // Step number when array became sorted (-1 if not)
    private final List<Integer> arrayValues;
    private final int arraySize;
    
    /**
     * PURPOSE: Construct a step snapshot with loop cycle and actual swap tracking.
     * 
     * INPUTS:
     *   - stepNumber: Current execution step
     *   - loopCycleCount: Times swap() method was called (position-seeking attempts)
     *   - actualSwapCount: Actual element exchanges that occurred
     *   - convergenceStep: Step when array became fully sorted
     *   - arrayValues: Current state of array values
     *   - arraySize: Number of elements in array
     * 
     * PROCESS:
     *   1. Store all parameters for state capture
     *   2. Validate that actual swaps ≤ theoretical maximum (n*(n-1)/2)
     *   3. Snapshot array values for reproducibility
     * 
     * OUTPUTS: New StepSnapshot instance
     * 
     * THROWS: IllegalArgumentException if validation fails
     * 
     * DESIGN RATIONALE:
     *   Previous implementations only tracked loopCycleCount, conflating
     *   position-seeking retry loops with actual element exchanges. This
     *   constructor enables separate tracking of both metrics, enabling
     *   correct error tolerance and efficiency calculations.
     */
    public StepSnapshot(
            int stepNumber,
            int loopCycleCount,
            int actualSwapCount,
            int convergenceStep,
            List<Integer> arrayValues,
            int arraySize
    ) {
        this.stepNumber = stepNumber;
        this.loopCycleCount = loopCycleCount;
        this.actualSwapCount = actualSwapCount;
        this.convergenceStep = convergenceStep;
        this.arrayValues = new ArrayList<>(arrayValues);
        this.arraySize = arraySize;
        
        // Validate that actual swaps don't exceed theoretical maximum
        int theoreticalMax = arraySize * (arraySize - 1) / 2;
        if (actualSwapCount > theoreticalMax) {
            throw new IllegalArgumentException(
                String.format(
                    "Actual swap count (%d) exceeds theoretical maximum (%d) for array size %d",
                    actualSwapCount, theoreticalMax, arraySize
                )
            );
        }
    }
    
    /**
     * PURPOSE: Get the step number for this snapshot.
     * 
     * @return Step number in execution sequence
     */
    public int getStepNumber() {
        return stepNumber;
    }
    
    /**
     * PURPOSE: Get count of swap() method invocations (position-seeking loop cycles).
     * 
     * IMPORTANT: This is NOT the number of actual element exchanges. In Selection Sort
     * where all cells target position 0, this can be 27× higher than actualSwapCount
     * due to contention and retry loops.
     * 
     * @return Number of times cells attempted position-seeking swaps
     * 
     * @deprecated Use {@link #getActualSwapCount()} for algorithm analysis
     */
    @Deprecated(since = "2026-01-08", forRemoval = false)
    public int getSwapCount() {
        return loopCycleCount;
    }
    
    /**
     * PURPOSE: Get count of position-seeking loop cycles (swap() method calls).
     * 
     * <p>This measures the EFFORT of position-seeking in the algorithm:
     * cells call move() repeatedly trying to reach their ideal positions.
     * Higher values indicate more contention and retry loops.
     * 
     * <p>For algorithm efficiency analysis, use {@link #getActualSwapCount()}
     * instead, which counts actual element exchanges.
     * 
     * @return Number of swap() method invocations during this step
     */
    public int getLoopCycleCount() {
        return loopCycleCount;
    }
    
    /**
     * PURPOSE: Get count of actual element exchanges that occurred.
     * 
     * <p>This is the TRUE measure of algorithmic work: how many elements
     * actually changed positions. For a completely reversed n=10 array,
     * this should be ~45 for any correct sorting algorithm.
     * 
     * @return Number of actual element exchanges in the array
     */
    public int getActualSwapCount() {
        return actualSwapCount;
    }
    
    /**
     * PURPOSE: Get the step number when array became fully sorted.
     * 
     * <p>This enables calculation of convergence time, which is the TRUE
     * measure of algorithmic overhead from frozen cells:
     * <pre>
     * errorTolerance = (convergence_frozen - convergence_unfrozen) / convergence_unfrozen
     * </pre>
     * 
     * @return Step number when convergence occurred, or -1 if not yet sorted
     */
    public int getConvergenceStep() {
        return convergenceStep;
    }
    
    /**
     * PURPOSE: Get the array size for this snapshot.
     * 
     * @return Number of elements in sorted array
     */
    public int getArraySize() {
        return arraySize;
    }
    
    /**
     * PURPOSE: Get the current array values.
     * 
     * @return Immutable list of current values
     */
    public List<Integer> getValues() {
        return new ArrayList<>(arrayValues);
    }
    
    /**
     * PURPOSE: Calculate efficiency metric (actual swaps per loop cycle).
     * 
     * <p>Useful for understanding position-seeking overhead:
     * <pre>
     * efficiency = actualSwapCount / loopCycleCount
     * </pre>
     * 
     * Higher values indicate more efficient position-seeking (fewer retries).
     * Selection Sort with position=0 bottleneck has low efficiency (~0.037).
     * 
     * @return Ratio of actual swaps to loop cycles
     */
    public double getEfficiency() {
        return loopCycleCount > 0 ? (double) actualSwapCount / loopCycleCount : 0.0;
    }
}
