package com.emergent.doom.validation;

import java.math.BigInteger;

/**
 * Configuration for a single scaling validation trial.
 * 
 * <p><strong>Purpose:</strong> Encapsulates all parameters needed to run one trial
 * of the linear scaling experiment. This includes the target semiprime, array size,
 * maximum steps, and other execution parameters.</p>
 * 
 * <p><strong>Architecture Role:</strong> Acts as an immutable value object that is passed
 * to the experiment runner. Ensures all trials within a batch have consistent configuration
 * while allowing variation in the critical parameters (target, arraySize).</p>
 * 
 * <p><strong>Data Flow:</strong>
 * <ul>
 *   <li>Input: Created by LinearScalingValidator based on stage configuration</li>
 *   <li>Output: Passed to FactorizationExperiment or ExperimentRunner for execution</li>
 * </ul>
 * </p>
 */
public class ScalingTrialConfig {
    private final BigInteger target;
    private final int arraySize;
    private final int maxSteps;
    private final int requiredStableSteps;
    private final boolean recordTrajectory;
    private final ScalingStage stage;
    
    /**
     * Create a new trial configuration.
     * 
     * <p><strong>Not yet implemented.</strong> Will construct an immutable configuration object.</p>
     * 
     * @param target The semiprime to factor
     * @param arraySize The number of candidate positions (cells) in the array
     * @param maxSteps Maximum steps before declaring non-convergence
     * @param requiredStableSteps Number of consecutive no-swap steps to declare convergence
     * @param recordTrajectory Whether to record full trajectory for analysis
     * @param stage The experimental stage this configuration belongs to
     */
    public ScalingTrialConfig(BigInteger target, int arraySize, int maxSteps,
                              int requiredStableSteps, boolean recordTrajectory,
                              ScalingStage stage) {
        // TODO: Phase 3 - implement constructor with validation
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the target semiprime to factor.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the configured target value.</p>
     * 
     * @return The target semiprime
     */
    public BigInteger getTarget() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the array size for this trial.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the number of cells/positions.</p>
     * 
     * @return The array size
     */
    public int getArraySize() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the maximum steps allowed.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the step limit.</p>
     * 
     * @return Maximum steps
     */
    public int getMaxSteps() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the required stable steps for convergence.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the convergence threshold.</p>
     * 
     * @return Required stable steps
     */
    public int getRequiredStableSteps() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Check if trajectory recording is enabled.
     * 
     * <p><strong>Not yet implemented.</strong> Will return trajectory flag.</p>
     * 
     * @return true if recording trajectory, false otherwise
     */
    public boolean isRecordTrajectory() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the experimental stage.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the stage enum.</p>
     * 
     * @return The experimental stage
     */
    public ScalingStage getStage() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
