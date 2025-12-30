package com.emergent.doom.experiment;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for running experiments.
 * 
 * <p>Encapsulates all parameters needed to execute a trial, including
 * domain-specific settings, execution limits, and analysis options.</p>
 */
public class ExperimentConfig {
    
    private final int arraySize;
    private final int maxSteps;
    private final int requiredStableSteps;
    private final boolean recordTrajectory;
    private final Map<String, Object> customParameters;
    
    /**
     * IMPLEMENTED: Create an experiment configuration
     */
    public ExperimentConfig(int arraySize, int maxSteps, int requiredStableSteps, boolean recordTrajectory) {
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
    
    public Map<String, Object> getCustomParameters() {
        return new HashMap<>(customParameters); // Defensive copy
    }
}
