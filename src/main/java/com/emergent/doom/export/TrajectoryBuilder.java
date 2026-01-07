package com.emergent.doom.export;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.metrics.MonotonicityError;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory for constructing ExperimentTrajectory instances from Probe data.
 * 
 * <p>This class provides factory methods to build trajectory objects from probe
 * snapshots, calculating per-step metrics including sortedness, monotonicity error,
 * and cumulative operation counts.</p>
 * 
 * <p><strong>Purpose in Emergent Doom Engine:</strong></p>
 * <ul>
 *   <li>Transform raw Probe snapshots into structured trajectory data</li>
 *   <li>Calculate derived metrics (sortedness, monotonicity) from cell states</li>
 *   <li>Support data export pipeline for analysis and visualization</li>
 * </ul>
 * 
 * <p><strong>Design:</strong> All methods are static factory methods. This class
 * cannot be instantiated.</p>
 * 
 * @see ExperimentTrajectory
 * @see Probe
 */
public class TrajectoryBuilder {
    
    /**
     * Private constructor prevents instantiation.
     */
    private TrajectoryBuilder() {
        throw new UnsupportedOperationException("TrajectoryBuilder is a static factory class");
    }
    
    /**
     * Builds an ExperimentTrajectory from Probe snapshots.
     * 
     * <p>This is the primary factory method for converting probe data into
     * a trajectory suitable for export and analysis.</p>
     * 
     * <p><strong>PURPOSE:</strong> Transform probe snapshots into trajectory with metrics</p>
     * 
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>probe: Probe containing recorded snapshots</li>
     *   <li>algotype: Algorithm type identifier (e.g., "Bubble", "Selection")</li>
     *   <li>frozenCells: Number of frozen cells in experiment</li>
     *   <li>trialNumber: Trial identifier</li>
     *   <li>arraySize: Size of cell array</li>
     *   <li>timestamp: Experiment start time in milliseconds</li>
     * </ul>
     * 
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate probe contains snapshots</li>
     *   <li>Determine sorted reference order from final snapshot</li>
     *   <li>For each snapshot:
     *     <ul>
     *       <li>Calculate sortedness percentage</li>
     *       <li>Calculate monotonicity error</li>
     *       <li>Extract cumulative swap count</li>
     *       <li>Estimate cumulative comparisons (heuristic)</li>
     *     </ul>
     *   </li>
     *   <li>Build and return ExperimentTrajectory</li>
     * </ol>
     * 
     * <p><strong>OUTPUTS:</strong> ExperimentTrajectory with per-step metrics</p>
     * 
     * <p><strong>THROWS:</strong> IllegalArgumentException if probe is null or empty</p>
     * 
     * <p><strong>KNOWN LIMITATION:</strong> Comparison count uses heuristic formula:
     * cumulativeSwaps + (stepNumber + 1) * arraySize. For accurate counts, enhance
     * Probe to record actual comparison counts per snapshot.</p>
     * 
     * @param probe Probe containing experiment snapshots
     * @param algotype Algorithm type identifier
     * @param frozenCells Number of frozen cells
     * @param trialNumber Trial identifier
     * @param arraySize Size of cell array
     * @param timestamp Experiment timestamp
     * @param <T> Cell type
     * @return ExperimentTrajectory with calculated metrics
     */
    public static <T extends Cell<T>> ExperimentTrajectory fromProbe(
            Probe<T> probe,
            String algotype,
            int frozenCells,
            int trialNumber,
            int arraySize,
            long timestamp) {
        
        // Step 1: Validate probe
        if (probe == null) {
            throw new IllegalArgumentException("Probe cannot be null");
        }
        
        List<StepSnapshot<T>> snapshots = probe.getSnapshots();
        if (snapshots.isEmpty()) {
            throw new IllegalArgumentException("Probe contains no snapshots");
        }
        
        // Step 2: Determine sorted reference order
        // Use final snapshot as the target sorted order
        StepSnapshot<T> finalSnapshot = snapshots.get(snapshots.size() - 1);
        List<Comparable<?>> sortedReference = new ArrayList<>(finalSnapshot.getComparableValues());
        sortedReference.sort(null); // Natural ordering
        
        // Step 3: Build trajectory steps
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        int cumulativeSwaps = 0;
        
        for (StepSnapshot<T> snapshot : snapshots) {
            int stepNumber = snapshot.getStepNumber();
            cumulativeSwaps += snapshot.getSwapCount();
            
            // Calculate sortedness (percentage in correct position)
            double sortedness = calculateSortedness(
                snapshot.getComparableValues(),
                sortedReference
            );
            
            // Calculate monotonicity error
            int monotonicityError = calculateMonotonicityError(
                snapshot.getComparableValues()
            );
            
            // Heuristic for cumulative comparisons
            // Since Probe doesn't track per-snapshot comparisons, estimate as:
            // cumulativeSwaps + (stepNumber + 1) * arraySize
            int cumulativeComparisons = cumulativeSwaps + (stepNumber + 1) * arraySize;
            
            // Aggregation is null for non-chimeric experiments
            // Can be enhanced later to detect and calculate from snapshot types
            Double aggregation = null;
            
            steps.add(new ExperimentTrajectory.TrajectoryStep(
                stepNumber,
                sortedness,
                monotonicityError,
                aggregation,
                cumulativeSwaps,
                cumulativeComparisons
            ));
        }
        
        // Step 4: Build and return trajectory
        return new ExperimentTrajectory(
            algotype,
            frozenCells,
            trialNumber,
            arraySize,
            timestamp,
            steps
        );
    }
    
    /**
     * Calculates sortedness as percentage of cells in correct final position.
     * 
     * <p><strong>PURPOSE:</strong> Measure how close current state is to target sorted state</p>
     * 
     * <p><strong>ALGORITHM:</strong></p>
     * <ol>
     *   <li>Compare each element in current state with corresponding element in sorted reference</li>
     *   <li>Count matches where current[i] equals sortedReference[i]</li>
     *   <li>Return (matchCount / arraySize) * 100.0</li>
     * </ol>
     * 
     * @param current Current cell values
     * @param sortedReference Target sorted order
     * @return Sortedness percentage (0.0 to 100.0)
     */
    private static double calculateSortedness(
            List<Comparable<?>> current,
            List<Comparable<?>> sortedReference) {
        
        if (current.size() != sortedReference.size()) {
            throw new IllegalArgumentException(
                "Current and sorted arrays must have same size"
            );
        }
        
        int correctPositions = 0;
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).equals(sortedReference.get(i))) {
                correctPositions++;
            }
        }
        
        return (correctPositions / (double) current.size()) * 100.0;
    }
    
    /**
     * Calculates monotonicity error as count of out-of-order adjacent pairs.
     * 
     * <p><strong>PURPOSE:</strong> Measure local disorder in array</p>
     * 
     * <p><strong>ALGORITHM:</strong></p>
     * <ol>
     *   <li>Iterate through adjacent pairs</li>
     *   <li>Count pairs where left > right (out of order)</li>
     *   <li>Return total count</li>
     * </ol>
     * 
     * <p><strong>REFERENCE:</strong> Uses MonotonicityError metric from EDE metrics package</p>
     * 
     * @param values Cell values to analyze
     * @return Count of out-of-order adjacent pairs
     */
    @SuppressWarnings("unchecked")
    private static int calculateMonotonicityError(List<Comparable<?>> values) {
        if (values == null || values.size() < 2) {
            return 0;
        }
        
        int count = 0;
        for (int i = 0; i < values.size() - 1; i++) {
            Comparable left = (Comparable) values.get(i);
            Comparable right = (Comparable) values.get(i + 1);
            if (left.compareTo(right) > 0) {
                count++;
            }
        }
        
        return count;
    }
}
