package com.emergent.doom.execution;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;

import java.util.List;

/**
 * Convergence detector that triggers when no swaps occur for N consecutive steps.
 * 
 * <p>This is the simplest and most common convergence criterion - the system
 * has reached a local equilibrium where no cell can improve by swapping with
 * its neighbors.</p>
 * 
 * @param <T> the type of cell
 */
public class NoSwapConvergence<T extends Cell<T>> implements ConvergenceDetector<T> {
    
    private final int requiredStableSteps;
    
    /**
     * IMPLEMENTED: Create a no-swap convergence detector
     */
    public NoSwapConvergence(int requiredStableSteps) {
        if (requiredStableSteps < 1) {
            throw new IllegalArgumentException("Required stable steps must be >= 1");
        }
        this.requiredStableSteps = requiredStableSteps;
    }
    
    /**
     * IMPLEMENTED: Check if N consecutive steps had zero swaps
     */
    @Override
    public boolean hasConverged(Probe<T> probe, int currentStep) {
        List<StepSnapshot<T>> snapshots = probe.getSnapshots();
        
        // Need at least requiredStableSteps snapshots
        if (snapshots.size() < requiredStableSteps) {
            return false;
        }
        
        // Check last requiredStableSteps snapshots for zero swaps
        int startIndex = snapshots.size() - requiredStableSteps;
        for (int i = startIndex; i < snapshots.size(); i++) {
            if (snapshots.get(i).getSwapCount() > 0) {
                return false;
            }
        }
        
        return true;
    }
}
