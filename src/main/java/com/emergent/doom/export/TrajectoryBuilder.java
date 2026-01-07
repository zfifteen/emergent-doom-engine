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
     * with metadata. Detects chimeric experiments by checking if snapshot type
     * metadata contains valid algotype labels.</p>
     * 
     * <p><strong>Important:</strong> Chimeric detection only works with AlgotypedProbe
     * or other probe implementations that populate algotype labels in snapshot metadata.
     * Standard Probe always sets algotypeLabel to -1, so aggregation metrics will not
     * be computed when using standard Probe.</p>
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
            
            // WARNING: Comparison count is a ROUGH HEURISTIC and likely inaccurate
            // 
            // Formula: cumulativeSwaps + (stepNumber + 1) * arraySize
            // 
            // This assumes arraySize comparisons per step plus accumulated swaps,
            // which does NOT match real sorting behavior. For example:
            // - Bubble sort does O(n) comparisons per pass, decreasing as sorting progresses
            // - Selection sort does exactly O(n²) comparisons regardless of swaps
            // - The formula treats swaps and comparisons as independent, which is incorrect
            // 
            // The actual comparison count depends on:
            // 1. The specific sorting algorithm implementation
            // 2. The current state of the array (partially sorted vs random)
            // 3. The termination condition (e.g., no swaps in a pass)
            // 
            // TODO: Enhance Probe to track actual comparison counts per snapshot.
            // Until then, treat this value as an order-of-magnitude estimate only.
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
        Integer firstLabel = null;
        
        for (Object[] typeData : types) {
            int label = (Integer) typeData[1];
            if (label >= 0) {
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
