package com.emergent.doom.experiment;

import com.emergent.doom.execution.ExecutionMode;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for running experiments.
 *
 * <p>Encapsulates all parameters needed to execute a trial, including
 * domain-specific settings, execution limits, and analysis options.</p>
 *
 * <p><strong>Execution Mode:</strong> Supports both sequential (default) and
 * parallel execution modes. Parallel mode matches the Levin paper's specification
 * of one thread per cell with barrier synchronization.</p>
 */
public class ExperimentConfig {

    private final int arraySize;
    private final int maxSteps;
    private final int requiredStableSteps;
    private final boolean recordTrajectory;
    private final ExecutionMode executionMode;
    private final Map<String, Object> customParameters;

    /**
     * Create an experiment configuration with default sequential execution.
     */
    public ExperimentConfig(int arraySize, int maxSteps, int requiredStableSteps, boolean recordTrajectory) {
        this(arraySize, maxSteps, requiredStableSteps, recordTrajectory, ExecutionMode.SEQUENTIAL);
    }

    /**
     * Create an experiment configuration with specified execution mode.
     *
     * @param arraySize array size for the experiment
     * @param maxSteps maximum steps before timeout
     * @param requiredStableSteps steps with no swaps required for convergence
     * @param recordTrajectory whether to record full trajectory
     * @param executionMode SEQUENTIAL or PARALLEL execution
     */
    public ExperimentConfig(int arraySize, int maxSteps, int requiredStableSteps,
                           boolean recordTrajectory, ExecutionMode executionMode) {
        if (arraySize <= 0) {
            throw new IllegalArgumentException("Array size must be positive");
        }
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("Max steps must be positive");
        }
        if (requiredStableSteps <= 0) {
            throw new IllegalArgumentException("Required stable steps must be positive");
        }

        this.arraySize = arraySize;
        this.maxSteps = maxSteps;
        this.requiredStableSteps = requiredStableSteps;
        this.recordTrajectory = recordTrajectory;
        this.executionMode = executionMode != null ? executionMode : ExecutionMode.SEQUENTIAL;
        this.customParameters = new HashMap<>();
    }
    
    /**
     * IMPLEMENTED: Add a custom parameter to the configuration
     */
    public void setCustomParameter(String key, Object value) {
        customParameters.put(key, value);
    }
    
    /**
     * IMPLEMENTED: Get a custom parameter value
     */
    public Object getCustomParameter(String key) {
        return customParameters.get(key);
    }
    
    // Getters
    public int getArraySize() {
        return arraySize;
    }
    
    public int getMaxSteps() {
        return maxSteps;
    }
    
    public int getRequiredStableSteps() {
        return requiredStableSteps;
    }
    
    public boolean isRecordTrajectory() {
        return recordTrajectory;
    }

    /**
     * Get the execution mode for this experiment.
     *
     * @return SEQUENTIAL or PARALLEL execution mode
     */
    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    /**
     * Check if parallel execution mode is enabled.
     *
     * @return true if execution mode is PARALLEL
     */
    public boolean isParallelExecution() {
        return executionMode == ExecutionMode.PARALLEL;
    }

    /**
     * Check if lock-based execution mode is enabled.
     *
     * @return true if execution mode is LOCK_BASED
     */
    public boolean isLockBasedExecution() {
        return executionMode == ExecutionMode.LOCK_BASED;
    }

    public Map<String, Object> getCustomParameters() {
        return new HashMap<>(customParameters); // Defensive copy
    }
}
