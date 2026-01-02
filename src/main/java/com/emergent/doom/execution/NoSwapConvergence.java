package com.emergent.doom.execution;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.Probe;

/**
 * Convergence detector that triggers when no swaps occur for N consecutive steps.
 */
public class NoSwapConvergence<T extends Cell<T>> implements ConvergenceDetector<T> {
    
    private final int requiredStableSteps;
    
    public NoSwapConvergence(int requiredStableSteps) {
        if (requiredStableSteps < 1) {
            throw new IllegalArgumentException("Required stable steps must be >= 1");
        }
        this.requiredStableSteps = requiredStableSteps;
    }
    
    /**
     * Check if N consecutive steps had zero swaps.
     * FIX: Uses probe.getStepsSinceLastSwap() which works even if recording is disabled.
     */
    @Override
    public boolean hasConverged(Probe<T> probe, int currentStep) {
        return probe.getStepsSinceLastSwap() >= requiredStableSteps;
    }
}
