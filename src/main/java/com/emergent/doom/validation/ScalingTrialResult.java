package com.emergent.doom.validation;

import java.math.BigInteger;

/**
 * Results from a single scaling validation trial.
 * 
 * <p><strong>Purpose:</strong> Captures all measurements from one trial execution,
 * including convergence metrics, factor discovery, and remainder statistics.
 * This data is used to calculate the B metric (∂steps/∂array_size) and assess
 * linear scaling behavior.</p>
 * 
 * <p><strong>Architecture Role:</strong> Serves as the primary output data structure
 * from trial execution. Multiple ScalingTrialResult instances are aggregated to
 * produce ScalingReport with statistical analysis.</p>
 * 
 * <p><strong>Data Flow:</strong>
 * <ul>
 *   <li>Input: Created after trial execution with measurements from Probe and ExecutionEngine</li>
 *   <li>Output: Aggregated into ScalingReport for B calculation and CSV export</li>
 * </ul>
 * </p>
 */
public class ScalingTrialResult {
    private final ScalingTrialConfig config;
    private final int stepsToConvergence;
    private final boolean converged;
    private final boolean foundFactor;
    private final BigInteger discoveredFactor;
    private final long executionTimeMs;
    private final RemainderStatistics remainderStats;
    
    /**
     * Create a trial result record.
     * 
     * <p><strong>Implementation:</strong> Constructs an immutable result object
     * capturing all trial outcomes for later analysis.</p>
     * 
     * <p><strong>Reasoning:</strong> Immutability prevents accidental modification
     * of experimental data. All fields are captured at construction time to ensure
     * consistency.</p>
     * 
     * <p><strong>Integration:</strong> Created by executeSingleTrial(), aggregated
     * by generateReport() for statistical analysis.</p>
     * 
     * @param config The configuration used for this trial
     * @param stepsToConvergence Number of steps until convergence (or maxSteps if non-convergent)
     * @param converged Whether the trial reached convergence criterion
     * @param foundFactor Whether a non-trivial factor was discovered
     * @param discoveredFactor The factor found (null if not found)
     * @param executionTimeMs Wall-clock execution time in milliseconds
     * @param remainderStats Statistics about the remainder landscape
     */
    public ScalingTrialResult(ScalingTrialConfig config, int stepsToConvergence,
                              boolean converged, boolean foundFactor,
                              BigInteger discoveredFactor, long executionTimeMs,
                              RemainderStatistics remainderStats) {
        if (config == null) throw new IllegalArgumentException("config cannot be null");
        if (stepsToConvergence < 0) throw new IllegalArgumentException("stepsToConvergence cannot be negative");
        if (executionTimeMs < 0) throw new IllegalArgumentException("executionTimeMs cannot be negative");
        
        this.config = config;
        this.stepsToConvergence = stepsToConvergence;
        this.converged = converged;
        this.foundFactor = foundFactor;
        this.discoveredFactor = discoveredFactor;
        this.executionTimeMs = executionTimeMs;
        this.remainderStats = remainderStats;
    }
    
    /**
     * Get the trial configuration.
     * 
     * <p><strong>Implementation:</strong> Returns the config used.</p>
     * 
     * @return The trial configuration
     */
    public ScalingTrialConfig getConfig() {
        return config;
    }
    
    /**
     * Get the number of steps to convergence.
     * 
     * <p><strong>Implementation:</strong> Returns step count. If non-convergent,
     * returns maxSteps from config.</p>
     * 
     * @return Steps to convergence
     */
    public int getStepsToConvergence() {
        return stepsToConvergence;
    }
    
    /**
     * Check if trial converged.
     * 
     * <p><strong>Implementation:</strong> Returns convergence status.</p>
     * 
     * @return true if converged, false otherwise
     */
    public boolean isConverged() {
        return converged;
    }
    
    /**
     * Check if a non-trivial factor was found.
     * 
     * <p><strong>Implementation:</strong> Returns factor discovery status.</p>
     * 
     * @return true if factor found, false otherwise
     */
    public boolean isFoundFactor() {
        return foundFactor;
    }
    
    /**
     * Get the discovered factor.
     * 
     * <p><strong>Implementation:</strong> Returns the factor if found, null otherwise.</p>
     * 
     * @return The discovered factor or null
     */
    public BigInteger getDiscoveredFactor() {
        return discoveredFactor;
    }
    
    /**
     * Get execution time in milliseconds.
     * 
     * <p><strong>Implementation:</strong> Returns wall-clock time.</p>
     * 
     * @return Execution time in ms
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * Get remainder landscape statistics.
     * 
     * <p><strong>Implementation:</strong> Returns remainder stats object.</p>
     * 
     * @return Remainder statistics
     */
    public RemainderStatistics getRemainderStats() {
        return remainderStats;
    }
    
    /**
     * Convert to CSV row format.
     * 
     * <p><strong>Implementation:</strong> Generates a CSV-formatted string
     * with all trial metrics for export. Compatible with analyze_scaling.py schema.</p>
     * 
     * <p><strong>Expected format:</strong>
     * stage,target,arraySize,steps,converged,foundFactor,factor,timeMs,
     * remainderMean,remainderVariance,remainderAutocorr</p>
     * 
     * <p><strong>Reasoning:</strong> CSV format enables analysis in Python/R and
     * allows long-term archival of experimental data.</p>
     * 
     * @return CSV row string
     */
    public String toCsvRow() {
        StringBuilder sb = new StringBuilder();
        
        // Stage, target, arraySize
        sb.append(config.getStage()).append(",");
        sb.append(config.getTarget()).append(",");
        sb.append(config.getArraySize()).append(",");
        
        // Steps, converged, foundFactor
        sb.append(stepsToConvergence).append(",");
        sb.append(converged ? "true" : "false").append(",");
        sb.append(foundFactor ? "true" : "false").append(",");
        
        // Discovered factor (or empty)
        sb.append(discoveredFactor != null ? discoveredFactor : "").append(",");
        
        // Execution time
        sb.append(executionTimeMs).append(",");
        
        // Remainder statistics (or empty if not available)
        if (remainderStats != null) {
            sb.append(remainderStats.getMean()).append(",");
            sb.append(remainderStats.getVariance()).append(",");
            sb.append(remainderStats.getAutocorrelation());
        } else {
            sb.append(",,");  // Empty fields for missing stats
        }
        
        return sb.toString();
    }
}
