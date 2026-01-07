package com.emergent.doom.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a complete experimental trajectory with per-step metrics.
 * 
 * <p>This class captures the evolution of an experiment over time, recording metrics
 * at each step including sortedness, monotonicity error, swap counts, and optional
 * aggregation values for chimeric experiments.</p>
 * 
 * <p><strong>Purpose in Emergent Doom Engine:</strong></p>
 * <ul>
 *   <li>Package experiment data for export to CSV/JSON formats</li>
 *   <li>Enable trajectory analysis and visualization</li>
 *   <li>Support delayed gratification and convergence analysis</li>
 *   <li>Facilitate comparison across different algotypes and configurations</li>
 * </ul>
 * 
 * <p><strong>Design:</strong> Immutable after construction. All fields are final and
 * collections are unmodifiable. Use TrajectoryBuilder to construct instances.</p>
 * 
 * @see TrajectoryBuilder
 * @see TrajectoryDataExporter
 */
public class ExperimentTrajectory {
    
    private final String algotype;
    private final int frozenCells;
    private final int trialNumber;
    private final int arraySize;
    private final long timestamp;
    private final List<TrajectoryStep> steps;
    
    /**
     * Immutable class representing metrics at a single experimental step.
     * 
     * <p>Each TrajectoryStep captures the state of the experiment at a specific
     * iteration, including sortedness percentage, monotonicity error, and cumulative
     * operation counts.</p>
     */
    public static class TrajectoryStep {
        private final int stepNumber;
        private final double sortedness;
        private final int monotonicityError;
        private final Double aggregation;
        private final int cumulativeSwaps;
        private final int cumulativeComparisons;
        
        /**
         * Constructs a TrajectoryStep with all metrics.
         * 
         * @param stepNumber Step/iteration number (0-based)
         * @param sortedness Percentage of cells in correct final sorted position (0.0-100.0)
         * @param monotonicityError Number of out-of-order adjacent pairs
         * @param aggregation Optional aggregation percentage for chimeric experiments (null if not applicable)
         * @param cumulativeSwaps Total number of swaps up to this step
         * @param cumulativeComparisons Total number of comparisons up to this step
         */
        public TrajectoryStep(
                int stepNumber,
                double sortedness,
                int monotonicityError,
                Double aggregation,
                int cumulativeSwaps,
                int cumulativeComparisons) {
            this.stepNumber = stepNumber;
            this.sortedness = sortedness;
            this.monotonicityError = monotonicityError;
            this.aggregation = aggregation;
            this.cumulativeSwaps = cumulativeSwaps;
            this.cumulativeComparisons = cumulativeComparisons;
        }
        
        public int stepNumber() { return stepNumber; }
        public double sortedness() { return sortedness; }
        public int monotonicityError() { return monotonicityError; }
        public Double aggregation() { return aggregation; }
        public int cumulativeSwaps() { return cumulativeSwaps; }
        public int cumulativeComparisons() { return cumulativeComparisons; }
    }
    
    /**
     * Constructs an ExperimentTrajectory with metadata and per-step data.
     * 
     * <p>This constructor is package-private. Use TrajectoryBuilder to create instances.</p>
     * 
     * @param algotype Algorithm type identifier (e.g., "Bubble", "Selection", "Chimeric")
     * @param frozenCells Number of frozen cells in the experiment
     * @param trialNumber Trial identifier for this experiment run
     * @param arraySize Size of the cell array being sorted
     * @param timestamp Experiment start time in milliseconds since epoch
     * @param steps List of per-step metrics
     */
    ExperimentTrajectory(
            String algotype,
            int frozenCells,
            int trialNumber,
            int arraySize,
            long timestamp,
            List<TrajectoryStep> steps) {
        this.algotype = algotype;
        this.frozenCells = frozenCells;
        this.trialNumber = trialNumber;
        this.arraySize = arraySize;
        this.timestamp = timestamp;
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }
    
    /**
     * Returns the algorithm type identifier.
     * 
     * @return Algotype string (e.g., "Bubble", "Selection", "Chimeric")
     */
    public String getAlgotype() {
        return algotype;
    }
    
    /**
     * Returns the number of frozen cells in this experiment.
     * 
     * @return Frozen cell count
     */
    public int getFrozenCells() {
        return frozenCells;
    }
    
    /**
     * Returns the trial number identifier.
     * 
     * @return Trial number
     */
    public int getTrialNumber() {
        return trialNumber;
    }
    
    /**
     * Returns the size of the cell array.
     * 
     * @return Array size
     */
    public int getArraySize() {
        return arraySize;
    }
    
    /**
     * Returns the experiment start timestamp.
     * 
     * @return Timestamp in milliseconds since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Returns an unmodifiable list of all trajectory steps.
     * 
     * @return List of TrajectoryStep records
     */
    public List<TrajectoryStep> getSteps() {
        return steps;
    }
    
    /**
     * Returns the number of steps in this trajectory.
     * 
     * @return Step count
     */
    public int getStepCount() {
        return steps.size();
    }
    
    /**
     * Checks if this trajectory includes aggregation data.
     * 
     * <p>Aggregation data is present for chimeric experiments where multiple
     * algotypes coexist in the same cell array.</p>
     * 
     * @return true if any step has non-null aggregation value
     */
    public boolean hasAggregation() {
        return steps.stream().anyMatch(step -> step.aggregation() != null);
    }
}
