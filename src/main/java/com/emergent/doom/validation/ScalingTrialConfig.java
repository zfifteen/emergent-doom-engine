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
     * <p><strong>Implementation:</strong> Constructs an immutable configuration object
     * with validation of critical parameters.</p>
     * 
     * <p><strong>Reasoning:</strong> Immutability ensures trial configurations cannot
     * be accidentally modified during execution, preventing hard-to-debug issues.
     * Validation catches configuration errors early.</p>
     * 
     * <p><strong>Integration:</strong> Created in runTrialBatch(), passed to
     * executeSingleTrial() for each trial execution.</p>
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
        // Validate parameters
        if (target == null) throw new IllegalArgumentException("target cannot be null");
        if (arraySize <= 0) throw new IllegalArgumentException("arraySize must be positive");
        if (maxSteps <= 0) throw new IllegalArgumentException("maxSteps must be positive");
        if (requiredStableSteps < 0) throw new IllegalArgumentException("requiredStableSteps cannot be negative");
        if (stage == null) throw new IllegalArgumentException("stage cannot be null");
        
        this.target = target;
        this.arraySize = arraySize;
        this.maxSteps = maxSteps;
        this.requiredStableSteps = requiredStableSteps;
        this.recordTrajectory = recordTrajectory;
        this.stage = stage;
    }
    
    /**
     * Get the target semiprime to factor.
     * 
     * <p><strong>Implementation:</strong> Returns the configured target value.</p>
     * 
     * @return The target semiprime
     */
    public BigInteger getTarget() {
        return target;
    }
    
    /**
     * Get the array size for this trial.
     * 
     * <p><strong>Implementation:</strong> Returns the number of cells/positions.</p>
     * 
     * @return The array size
     */
    public int getArraySize() {
        return arraySize;
    }
    
    /**
     * Get the maximum steps allowed.
     * 
     * <p><strong>Implementation:</strong> Returns the step limit.</p>
     * 
     * @return Maximum steps
     */
    public int getMaxSteps() {
        return maxSteps;
    }
    
    /**
     * Get the required stable steps for convergence.
     * 
     * <p><strong>Implementation:</strong> Returns the convergence threshold.</p>
     * 
     * @return Required stable steps
     */
    public int getRequiredStableSteps() {
        return requiredStableSteps;
    }
    
    /**
     * Check if trajectory recording is enabled.
     * 
     * <p><strong>Implementation:</strong> Returns trajectory flag.</p>
     * 
     * @return true if recording trajectory, false otherwise
     */
    public boolean isRecordTrajectory() {
        return recordTrajectory;
    }
    
    /**
     * Get the experimental stage.
     * 
     * <p><strong>Implementation:</strong> Returns the stage enum.</p>
     * 
     * @return The experimental stage
     */
    public ScalingStage getStage() {
        return stage;
    }
}
