package com.emergent.doom.execution;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.Probe;

/**
 * Interface for detecting when execution should terminate.
 * 
 * <p>Convergence detectors analyze execution state to determine when
 * the system has reached a stable configuration or met termination
 * criteria.</p>
 * 
 * @param <T> the type of cell
 */
public interface ConvergenceDetector<T extends Cell<T>> {
    
    // PURPOSE: Check if convergence has been reached
    // INPUTS:
    //   - probe (Probe<T>) - execution history and current state
    //   - currentStep (int) - the current step number
    // PROCESS:
    //   1. Analyze probe data (recent snapshots, swap counts, etc.)
    //   2. Apply detector-specific convergence criteria
    //   3. Return true if criteria met, false otherwise
    // OUTPUTS: boolean - true if execution should terminate
    // DEPENDENCIES: Probe methods
    // NOTE: Different detectors can implement various stopping criteria:
    //       - No swaps in last N steps
    //       - Metric threshold reached
    //       - Maximum iterations exceeded
    boolean hasConverged(Probe<T> probe, int currentStep);
    
    // PURPOSE: Reset any internal state of the detector
    // INPUTS: None
    // PROCESS:
    //   1. Clear any accumulated state
    //   2. Default implementation does nothing
    // OUTPUTS: void
    // DEPENDENCIES: None
    default void reset() {
        // Default: no-op for stateless detectors
    }
}
