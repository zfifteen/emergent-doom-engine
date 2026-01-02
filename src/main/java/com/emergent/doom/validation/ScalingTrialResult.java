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
     * <p><strong>Not yet implemented.</strong> Will construct an immutable result object
     * capturing all trial outcomes.</p>
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
        // TODO: Phase 3 - implement constructor
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the trial configuration.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the config used.</p>
     * 
     * @return The trial configuration
     */
    public ScalingTrialConfig getConfig() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the number of steps to convergence.
     * 
     * <p><strong>Not yet implemented.</strong> Will return step count. If non-convergent,
     * returns maxSteps from config.</p>
     * 
     * @return Steps to convergence
     */
    public int getStepsToConvergence() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Check if trial converged.
     * 
     * <p><strong>Not yet implemented.</strong> Will return convergence status.</p>
     * 
     * @return true if converged, false otherwise
     */
    public boolean isConverged() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Check if a non-trivial factor was found.
     * 
     * <p><strong>Not yet implemented.</strong> Will return factor discovery status.</p>
     * 
     * @return true if factor found, false otherwise
     */
    public boolean isFoundFactor() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the discovered factor.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the factor if found, null otherwise.</p>
     * 
     * @return The discovered factor or null
     */
    public BigInteger getDiscoveredFactor() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get execution time in milliseconds.
     * 
     * <p><strong>Not yet implemented.</strong> Will return wall-clock time.</p>
     * 
     * @return Execution time in ms
     */
    public long getExecutionTimeMs() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get remainder landscape statistics.
     * 
     * <p><strong>Not yet implemented.</strong> Will return remainder stats object.</p>
     * 
     * @return Remainder statistics
     */
    public RemainderStatistics getRemainderStats() {
        // TODO: Phase 3 - implement getter
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Convert to CSV row format.
     * 
     * <p><strong>Not yet implemented.</strong> Will generate a CSV-formatted string
     * with all trial metrics for export.</p>
     * 
     * <p><strong>Expected format:</strong>
     * stage,target,arraySize,trial,steps,converged,foundFactor,factor,timeMs,
     * remainderMean,remainderVariance,remainderAutocorr</p>
     * 
     * @return CSV row string
     */
    public String toCsvRow() {
        // TODO: Phase 3 - implement CSV serialization
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
