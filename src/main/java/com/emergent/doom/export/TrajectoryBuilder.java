package com.emergent.doom.export;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.metrics.AlgotypeAggregationIndex;
import com.emergent.doom.metrics.Monotonicity;
import com.emergent.doom.metrics.MonotonicityError;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds ExperimentTrajectory from Probe snapshots.
 * 
 * <p>This utility extracts per-step metrics from probe snapshots to construct
 * a complete trajectory suitable for export and analysis.</p>
 * 
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 * Probe<GenericCell> probe = new Probe<>();
 * // ... run experiment, probe records snapshots ...
 * 
 * ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
 *     probe,
 *     "Bubble",    // algotype
 *     2,           // frozenCells
 *     0,           // trialNumber
 *     50,          // arraySize
 *     System.currentTimeMillis()
 * );
 * }</pre>
 */
public class TrajectoryBuilder {
    
    /**
     * Build trajectory from probe snapshots.
     * 
     * <p>Computes metrics for each snapshot and constructs a complete trajectory
     * with metadata. Automatically detects chimeric experiments by checking if
     * aggregation can be computed from snapshot type data.</p>
     * 
     * @param probe the probe containing snapshots
     * @param algotype algotype name (e.g., "Bubble", "Fib")
     * @param frozenCells number of frozen cells
     * @param trialNumber trial number
     * @param arraySize array size
     * @param timestamp experiment start timestamp (milliseconds)
     * @param <T> cell type
     * @return complete experiment trajectory
     * @throws IllegalArgumentException if probe is null or has no snapshots
     */
    public static <T extends Cell<T>> ExperimentTrajectory fromProbe(
            Probe<T> probe,
            String algotype,
            int frozenCells,
            int trialNumber,
            int arraySize,
            long timestamp) {
        
        if (probe == null) {
            throw new IllegalArgumentException("Probe cannot be null");
        }
        if (probe.getSnapshotCount() == 0) {
            throw new IllegalArgumentException("Probe has no snapshots");
        }
        
        List<StepSnapshot<T>> snapshots = probe.getSnapshots();
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        
        // Create metric instances
        Monotonicity<T> monotonicityMetric = new Monotonicity<>();
        MonotonicityError<T> monotonicityErrorMetric = new MonotonicityError<>();
        AlgotypeAggregationIndex<T> aggregationMetric = new AlgotypeAggregationIndex<>();
        
        // Track cumulative comparisons (initially just use compareAndSwapCount from probe)
        int cumulativeComparisons = 0;
        
        for (StepSnapshot<T> snapshot : snapshots) {
            int stepNumber = snapshot.getStepNumber();
            
            // Compute metrics from snapshot
            double sortedness = monotonicityMetric.compute(snapshot);
            int monotonicityError = (int) monotonicityErrorMetric.compute(snapshot);
            
            // Try to compute aggregation (only for chimeric experiments)
            Double aggregation = null;
            if (isChimeric(snapshot)) {
                aggregation = aggregationMetric.compute(snapshot);
            }
            
            // Get cumulative swaps from snapshot
            int cumulativeSwaps = snapshot.getSwapCount();
            
            // For comparisons, we approximate as swapCount + array_size per step
            // TODO: This is a heuristic since Probe doesn't track per-snapshot comparisons.
            // When Probe is enhanced to record actual comparison counts, replace this
            // approximation with the real values from snapshots.
            cumulativeComparisons = cumulativeSwaps + (stepNumber + 1) * arraySize;
            
            steps.add(new ExperimentTrajectory.TrajectoryStep(
                stepNumber,
                sortedness,
                monotonicityError,
                aggregation,
                cumulativeSwaps,
                cumulativeComparisons
            ));
        }
        
        ExperimentTrajectory.ExperimentMetadata metadata = 
            new ExperimentTrajectory.ExperimentMetadata(
                algotype,
                frozenCells,
                trialNumber,
                arraySize,
                timestamp
            );
        
        return new ExperimentTrajectory(steps, metadata);
    }
    
    /**
     * Check if snapshot represents a chimeric experiment.
     * 
     * <p>A chimeric experiment has cells with different algotypes, which is
     * detectable from the type metadata in snapshots.</p>
     * 
     * @param snapshot the snapshot to check
     * @return true if chimeric (multiple algotypes present)
     */
    private static boolean isChimeric(StepSnapshot<?> snapshot) {
        // Check if types have meaningful algotype data
        // types[i] = [groupId, algotypeLabel, value, isFrozen]
        List<Object[]> types = snapshot.getTypes();
        if (types == null || types.isEmpty()) {
            return false;
        }
        
        // If all algotype labels are -1, it's not chimeric
        // If we see different non-negative labels, it's chimeric
        boolean foundNonNegative = false;
        Integer firstLabel = null;
        
        for (Object[] typeData : types) {
            int label = (Integer) typeData[1];
            if (label >= 0) {
                foundNonNegative = true;
                if (firstLabel == null) {
                    firstLabel = label;
                } else if (!firstLabel.equals(label)) {
                    // Found different algotypes - definitely chimeric
                    return true;
                }
            }
        }
        
        // Not chimeric - either no valid algotype data or all same type
        return false;
    }
}
