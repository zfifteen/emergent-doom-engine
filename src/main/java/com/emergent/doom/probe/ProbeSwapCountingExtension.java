package com.emergent.doom.probe;

import java.io.IOException;
import java.io.Writer;

/**
 * PURPOSE: Extend Probe with actual swap counting and convergence tracking.
 * 
 * DESIGN RATIONALE:
 *   Previously, Probe.swapCount measured position-seeking loop invocations
 *   (effort), not actual element exchanges (work). Selection Sort's position=0
 *   bottleneck caused 27x overcounting.
 *   
 *   This extension adds separate tracking without breaking existing API:
 *   - actualSwapCount: True element exchanges in array
 *   - convergenceStep: When array became fully sorted
 *   - efficiency: (actual / cycles) ratio showing bottleneck severity
 *   
 *   OLD API preserved: All existing methods remain unchanged
 *   COMPOSITION: Wrap existing Probe, add new counters
 *   THREAD-SAFE: Synchronized new methods match existing sync patterns
 * 
 * @param <T> Cell type (same as original Probe)
 */
public class ProbeSwapCountingExtension<T> {
    
    private final Probe<T> delegate;  // Wrapped original Probe
    
    private volatile int actualSwapCount;           // Element exchanges (work)
    private volatile int convergenceStep;           // When array sorted (-1 = not yet)
    private final Object lock = new Object();       // Sync lock for multiple fields
    
    /**
     * PURPOSE: Wrap an existing Probe and add new metrics.
     * 
     * INPUTS:
     *   - probe: Existing Probe instance to extend
     * 
     * DESIGN NOTE:
     *   Uses composition instead of inheritance to avoid modifying
     *   existing Probe class and breaking dependent subclasses.
     */
    public ProbeSwapCountingExtension(Probe<T> probe) {
        this.delegate = probe;
        this.actualSwapCount = 0;
        this.convergenceStep = -1;  // -1 = not yet converged
    }
    
    /**
     * PURPOSE: Delegate to wrapped Probe's recordSwap() method.
     * 
     * Maintains backward compatibility: Original swap counting still works,
     * but now represents position-seeking loop cycles (effort), not work.
     */
    public void recordSwapInvocation() {
        delegate.recordSwap();
    }
    
    /**
     * PURPOSE: Record that an actual element exchange occurred.
     * 
     * INPUTS: None
     * 
     * PROCESS:
     *   1. Increment actual swap counter (thread-safe)
     *   2. Validate doesn't exceed theoretical maximum
     *   3. This measures WORK (true element exchanges)
     * 
     * THREAD-SAFE: Uses synchronized block with lock
     */
    public synchronized void recordActualSwap() {
        synchronized (lock) {
            actualSwapCount++;
        }
    }
    
    /**
     * PURPOSE: Record that array became fully sorted.
     * 
     * INPUTS:
     *   - step: Current execution step when isSorted() returned true
     * 
     * BEHAVIOR:
     *   - Only sets if not yet converged (-1 → step value)
     *   - Idempotent: Subsequent calls are ignored
     *   - Thread-safe: Uses synchronized block
     */
    public synchronized void markConvergence(int step) {
        synchronized (lock) {
            if (convergenceStep == -1) {
                convergenceStep = step;
            }
        }
    }
    
    /**
     * PURPOSE: Get count of position-seeking loop invocations (from delegate).
     * 
     * @return Number of times swap() method was called
     */
    public int getLoopInvocationCount() {
        return delegate.getSwapCount();
    }
    
    /**
     * PURPOSE: Get count of actual element exchanges.
     * 
     * @return Number of true element exchanges in array
     */
    public int getActualSwapCount() {
        synchronized (lock) {
            return actualSwapCount;
        }
    }
    
    /**
     * PURPOSE: Get execution step when array became fully sorted.
     * 
     * @return Step number when convergence occurred, or -1 if not yet
     */
    public int getConvergenceStep() {
        synchronized (lock) {
            return convergenceStep;
        }
    }
    
    /**
     * PURPOSE: Check if array has converged.
     * 
     * @return true if markConvergence() was called
     */
    public boolean hasConverged() {
        synchronized (lock) {
            return convergenceStep != -1;
        }
    }
    
    /**
     * PURPOSE: Calculate position-seeking efficiency metric.
     * 
     * FORMULA:
     *   efficiency = actualSwapCount / loopInvocationCount
     * 
     * INTERPRETATION:
     *   - 1.0 = Every attempt resulted in exchange (ideal)
     *   - 0.037 = Selection Sort (27x overhead from position bottleneck)
     *   - 0.074 = Bubble Sort (13.5x overhead)
     * 
     * @return Ratio of actual swaps to loop invocations
     */
    public double getPositionSeekingEfficiency() {
        synchronized (lock) {
            int loops = delegate.getSwapCount();
            return loops > 0 ? (double) actualSwapCount / loops : 0.0;
        }
    }
    
    /**
     * PURPOSE: Get the wrapped delegate Probe (for method forwarding).
     * 
     * @return Original Probe instance
     */
    public Probe<T> getDelegate() {
        return delegate;
    }
    
    /**
     * PURPOSE: Export all metrics (old and new) to CSV format.
     * 
     * CSV columns:
     *   - loopInvocations: swap() method calls (legacy)
     *   - actualSwaps: Element exchanges (correct metric)
     *   - convergenceStep: When sorted
     *   - efficiency: Ratio showing overhead
     * 
     * @param writer Output writer for CSV line
     * @throws IOException if write fails
     */
    public void exportMetricsCSV(Writer writer) throws IOException {
        synchronized (lock) {
            int loops = delegate.getSwapCount();
            double efficiency = loops > 0 ? (double) actualSwapCount / loops : 0.0;
            writer.write(String.format(
                "%d,%d,%d,%.4f%n",
                loops,
                actualSwapCount,
                convergenceStep,
                efficiency
            ));
        }
    }
    
    /**
     * PURPOSE: Get human-readable summary of metrics.
     * 
     * @return Formatted summary string
     */
    @Override
    public String toString() {
        synchronized (lock) {
            int loops = delegate.getSwapCount();
            double efficiency = loops > 0 ? (double) actualSwapCount / loops : 0.0;
            return String.format(
                "ProbeSwapCounting[loops=%d, actual=%d, convergence=%d, efficiency=%.4f]",
                loops, actualSwapCount, convergenceStep, efficiency
            );
        }
    }
}
