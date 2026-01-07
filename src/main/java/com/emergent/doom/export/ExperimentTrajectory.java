package com.emergent.doom.export;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates complete trajectory data for a single experiment run.
 * 
 * <p>This structure captures step-by-step evolution of metrics required for
 * dashboard validation as specified in the Levin et al. (2024) paper:
 * <ul>
 *   <li><strong>Delayed Gratification (DG):</strong> Requires sortedness history to detect
 *       temporary decreases that enable later gains</li>
 *   <li><strong>Aggregation Peaks:</strong> Requires aggregation history to identify
 *       maximum clustering and timing</li>
 *   <li><strong>Monotonicity Error:</strong> Requires full trajectory to track error
 *       tolerance dynamics</li>
 *   <li><strong>Error Tolerance Analysis:</strong> Requires comparing trajectories across
 *       frozen cell counts (0, 1, 2, 3)</li>
 * </ul>
 * </p>
 * 
 * <p><strong>Design:</strong> Immutable value object containing metadata and step-by-step
 * trajectory data. Supports export to CSV, JSON, or other formats via TrajectoryDataExporter.</p>
 * 
 * @see TrajectoryDataExporter
 */
public class ExperimentTrajectory {
    
    private final List<TrajectoryStep> steps;
    private final ExperimentMetadata metadata;
    
    /**
     * Create an experiment trajectory with metadata and step data.
     * 
     * @param steps list of trajectory steps (will be copied to ensure immutability)
     * @param metadata experiment metadata
     * @throws IllegalArgumentException if steps or metadata is null
     */
    public ExperimentTrajectory(List<TrajectoryStep> steps, ExperimentMetadata metadata) {
        if (steps == null) {
            throw new IllegalArgumentException("Steps cannot be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Metadata cannot be null");
        }
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        this.metadata = metadata;
    }
    
    public List<TrajectoryStep> getSteps() {
        return steps;
    }
    
    public ExperimentMetadata getMetadata() {
        return metadata;
    }
    
    public int getStepCount() {
        return steps.size();
    }
    
    /**
     * Immutable class representing a single step in the execution trajectory.
     * 
     * <p>Each step captures the state of key metrics at that point in execution,
     * enabling detailed analysis of emergent behavior over time.</p>
     */
    public static class TrajectoryStep {
        private final int stepNumber;
        private final double sortedness;
        private final int monotonicityError;
        private final Double aggregation;
        private final int cumulativeSwaps;
        private final int cumulativeComparisons;
        
        /**
         * Create a trajectory step with validation.
         * 
         * @param stepNumber the execution step number (0-based)
         * @param sortedness percentage of cells in correct relative order (0-100%)
         * @param monotonicityError count of adjacent inversions
         * @param aggregation percentage of cells with same-type neighbors (0-100%, null for non-chimeric)
         * @param cumulativeSwaps total swaps executed up to this step
         * @param cumulativeComparisons total comparisons executed up to this step
         */
        public TrajectoryStep(
                int stepNumber,
                double sortedness,
                int monotonicityError,
                Double aggregation,
                int cumulativeSwaps,
                int cumulativeComparisons) {
            if (stepNumber < 0) {
                throw new IllegalArgumentException("Step number cannot be negative");
            }
            if (sortedness < 0.0 || sortedness > 100.0) {
                throw new IllegalArgumentException("Sortedness must be between 0 and 100");
            }
            if (monotonicityError < 0) {
                throw new IllegalArgumentException("Monotonicity error cannot be negative");
            }
            if (aggregation != null && (aggregation < 0.0 || aggregation > 100.0)) {
                throw new IllegalArgumentException("Aggregation must be between 0 and 100");
            }
            if (cumulativeSwaps < 0) {
                throw new IllegalArgumentException("Cumulative swaps cannot be negative");
            }
            if (cumulativeComparisons < 0) {
                throw new IllegalArgumentException("Cumulative comparisons cannot be negative");
            }
            
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
        
        /**
         * Check if this is a chimeric experiment step (has aggregation data).
         */
        public boolean isChimeric() {
            return aggregation != null;
        }
    }
    
    /**
     * Immutable class representing experiment metadata.
     * 
     * <p>Metadata provides context for interpreting trajectory data, enabling
     * filtering and grouping during analysis.</p>
     */
    public static class ExperimentMetadata {
        private final String algotype;
        private final int frozenCells;
        private final int trialNumber;
        private final int arraySize;
        private final long timestamp;
        
        /**
         * Create experiment metadata with validation.
         * 
         * @param algotype the algotype used in this experiment (e.g., "Bubble", "Fib")
         * @param frozenCells number of frozen (immovable) cells
         * @param trialNumber trial number within the experiment batch
         * @param arraySize size of the cell array
         * @param timestamp Unix timestamp (milliseconds since epoch, typically positive for modern dates)
         */
        public ExperimentMetadata(
                String algotype,
                int frozenCells,
                int trialNumber,
                int arraySize,
                long timestamp) {
            if (algotype == null || algotype.trim().isEmpty()) {
                throw new IllegalArgumentException("Algotype cannot be null or empty");
            }
            if (frozenCells < 0) {
                throw new IllegalArgumentException("Frozen cells cannot be negative");
            }
            if (trialNumber < 0) {
                throw new IllegalArgumentException("Trial number cannot be negative");
            }
            if (arraySize <= 0) {
                throw new IllegalArgumentException("Array size must be positive");
            }
            // Note: Timestamp validation removed - negative timestamps are valid for pre-1970 dates
            
            this.algotype = algotype.trim();
            this.frozenCells = frozenCells;
            this.trialNumber = trialNumber;
            this.arraySize = arraySize;
            this.timestamp = timestamp;
        }
        
        public String algotype() { return algotype; }
        public int frozenCells() { return frozenCells; }
        public int trialNumber() { return trialNumber; }
        public int arraySize() { return arraySize; }
        public long timestamp() { return timestamp; }
    }
    
    @Override
    public String toString() {
        return String.format(
            "ExperimentTrajectory[algotype=%s, frozen=%d, trial=%d, arraySize=%d, steps=%d]",
            metadata.algotype(), metadata.frozenCells(), metadata.trialNumber(),
            metadata.arraySize(), steps.size()
        );
    }
}
