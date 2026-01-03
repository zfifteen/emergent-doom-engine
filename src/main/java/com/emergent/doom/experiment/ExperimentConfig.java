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
 *
 * <p><strong>REFACTORED:</strong> Added numRepetitions field to support batch
 * trial execution via runBatchExperiments().</p>
 */
public class ExperimentConfig {

    private final int arraySize;
    private final int maxSteps;
    private final int requiredStableSteps;
    private final boolean recordTrajectory;
    private final ExecutionMode executionMode;
    private final int numRepetitions;  // NEW: Number of trials to run in batch mode
    private final Map<String, Object> customParameters;

    /**
     * Create an experiment configuration with default sequential execution.
     */
    public ExperimentConfig(int arraySize, int maxSteps, int requiredStableSteps, boolean recordTrajectory) {
        this(arraySize, maxSteps, requiredStableSteps, recordTrajectory, ExecutionMode.SEQUENTIAL, 100);
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
        this(arraySize, maxSteps, requiredStableSteps, recordTrajectory, executionMode, 100);
    }

    /**
     * Create an experiment configuration with batch trial support.
     *
     * <p>PURPOSE: Support parallel batch execution via runBatchExperiments().</p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>arraySize - size of cell array per trial</li>
     *   <li>maxSteps - maximum steps before timeout</li>
     *   <li>requiredStableSteps - steps with no swaps for convergence</li>
     *   <li>recordTrajectory - whether to record full trajectory</li>
     *   <li>executionMode - SEQUENTIAL, PARALLEL, or LOCK_BASED</li>
     *   <li>numRepetitions - number of trials to run in batch</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Validate all parameters</li>
     *   <li>Store fields</li>
     *   <li>Initialize customParameters map</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Fully initialized configuration</p>
     *
     * <p>GROUND TRUTH REFERENCE: Supports architecture from issue
     * "Refactor Threading Model: Per-Trial Parallelism Instead of Per-Cell".</p>
     *
     * @param arraySize array size for the experiment
     * @param maxSteps maximum steps before timeout
     * @param requiredStableSteps steps with no swaps required for convergence
     * @param recordTrajectory whether to record full trajectory
     * @param executionMode SEQUENTIAL, PARALLEL, or LOCK_BASED execution
     * @param numRepetitions number of trials for batch execution
     */
    public ExperimentConfig(int arraySize, int maxSteps, int requiredStableSteps,
                           boolean recordTrajectory, ExecutionMode executionMode, int numRepetitions) {
        if (arraySize <= 0) {
            throw new IllegalArgumentException("Array size must be positive");
        }
        if (maxSteps <= 0) {
            throw new IllegalArgumentException("Max steps must be positive");
        }
        if (requiredStableSteps <= 0) {
            throw new IllegalArgumentException("Required stable steps must be positive");
        }
        if (numRepetitions <= 0) {
            throw new IllegalArgumentException("Number of repetitions must be positive");
        }

        this.arraySize = arraySize;
        this.maxSteps = maxSteps;
        this.requiredStableSteps = requiredStableSteps;
        this.recordTrajectory = recordTrajectory;
        this.executionMode = executionMode != null ? executionMode : ExecutionMode.SEQUENTIAL;
        this.numRepetitions = numRepetitions;
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

    /**
     * Get the number of trial repetitions for batch execution.
     *
     * <p>PURPOSE: Provide trial count for runBatchExperiments().</p>
     *
     * @return number of trials to execute in batch mode
     */
    public int getNumRepetitions() {
        return numRepetitions;
    }
}
