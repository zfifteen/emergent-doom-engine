package com.emergent.doom.analysis;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.metrics.Metric;
import com.emergent.doom.probe.StepSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzes and visualizes execution trajectories.
 * 
 * <p>The TrajectoryAnalyzer processes snapshot sequences to extract
 * patterns, trends, and visualizable data.</p>
 * 
 * @param <T> the type of cell
 */
public class TrajectoryAnalyzer<T extends Cell<T>> {
    
    // PURPOSE: Compute a metric over the entire trajectory
    // INPUTS:
    //   - snapshots (List<StepSnapshot<T>>) - execution history
    //   - metric (Metric<T>) - the metric to compute
    // PROCESS:
    //   1. For each snapshot in the list:
    //      - Get cell states from snapshot
    //      - Compute metric value
    //      - Store in results list
    //   2. Return list of metric values over time
    // OUTPUTS: List<Double> - metric values at each step
    // DEPENDENCIES:
    //   - StepSnapshot.getCellStates()
    //   - Metric.compute()
    public List<Double> computeMetricTrajectory(List<StepSnapshot<T>> snapshots, Metric<T> metric) {
        if (snapshots == null || snapshots.isEmpty()) {
            return new ArrayList<>();
        }
        List<Double> values = new ArrayList<>(snapshots.size());
        for (StepSnapshot<T> snapshot : snapshots) {
            values.add(metric.compute(snapshot.getCellStates()));
        }
        return values;
    }
    
    // PURPOSE: Extract swap counts over time
    // INPUTS: snapshots (List<StepSnapshot<T>>) - execution history
    // PROCESS:
    //   1. For each snapshot:
    //      - Extract swap count
    //      - Add to results list
    //   2. Return list of swap counts
    // OUTPUTS: List<Integer> - swap counts at each step
    // DEPENDENCIES: StepSnapshot.getSwapCount()
    public List<Integer> extractSwapCounts(List<StepSnapshot<T>> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> counts = new ArrayList<>(snapshots.size());
        for (StepSnapshot<T> snapshot : snapshots) {
            counts.add(snapshot.getSwapCount());
        }
        return counts;
    }
    
    // PURPOSE: Detect convergence point in trajectory
    // INPUTS:
    //   - snapshots (List<StepSnapshot<T>>) - execution history
    //   - consecutiveZeroSwaps (int) - required stable steps
    // PROCESS:
    //   1. Iterate through snapshots
    //   2. Track consecutive zero-swap steps
    //   3. When threshold reached, return that step number
    //   4. Return -1 if never converged
    // OUTPUTS: int - step number where convergence occurred, or -1
    // DEPENDENCIES: StepSnapshot.getSwapCount()
    public int findConvergenceStep(List<StepSnapshot<T>> snapshots, int consecutiveZeroSwaps) {
        if (snapshots == null || snapshots.isEmpty() || consecutiveZeroSwaps <= 0) {
            return -1;
        }

        int zeroCount = 0;
        for (int i = 0; i < snapshots.size(); i++) {
            if (snapshots.get(i).getSwapCount() == 0) {
                zeroCount++;
                if (zeroCount >= consecutiveZeroSwaps) {
                    // Return the step where convergence was first detected
                    return snapshots.get(i - consecutiveZeroSwaps + 1).getStepNumber();
                }
            } else {
                zeroCount = 0;
            }
        }
        return -1; // Never converged
    }
    
    // PURPOSE: Generate a text-based visualization of trajectory
    // INPUTS:
    //   - snapshots (List<StepSnapshot<T>>) - execution history
    //   - maxSnapshotsToShow (int) - limit on output size
    // PROCESS:
    //   1. Format header with column names
    //   2. For each snapshot (up to max):
    //      - Format step number, swap count, and key metrics
    //      - Add to output string
    //   3. Return formatted string
    // OUTPUTS: String - multi-line text visualization
    // DEPENDENCIES: StepSnapshot methods
    public String visualizeTrajectory(List<StepSnapshot<T>> snapshots, int maxSnapshotsToShow) {
        if (snapshots == null || snapshots.isEmpty()) {
            return "No trajectory data available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s %-12s %-12s%n", "Step", "Swaps", "Array Size"));
        sb.append("-".repeat(35)).append("\n");

        int limit = Math.min(snapshots.size(), maxSnapshotsToShow);
        for (int i = 0; i < limit; i++) {
            StepSnapshot<T> snapshot = snapshots.get(i);
            sb.append(String.format("%-8d %-12d %-12d%n",
                    snapshot.getStepNumber(),
                    snapshot.getSwapCount(),
                    snapshot.getArraySize()));
        }

        if (snapshots.size() > maxSnapshotsToShow) {
            sb.append(String.format("... and %d more steps%n", snapshots.size() - maxSnapshotsToShow));
        }

        return sb.toString();
    }
    
    // PURPOSE: Calculate time elapsed between first and last snapshot
    // INPUTS: snapshots (List<StepSnapshot<T>>) - execution history
    // PROCESS:
    //   1. Get timestamp from first snapshot
    //   2. Get timestamp from last snapshot
    //   3. Return difference in nanoseconds
    // OUTPUTS: long - elapsed time in nanoseconds
    // DEPENDENCIES: StepSnapshot.getTimestamp()
    public long getTotalExecutionTime(List<StepSnapshot<T>> snapshots) {
        if (snapshots == null || snapshots.size() < 2) {
            return 0L;
        }
        long firstTimestamp = snapshots.get(0).getTimestamp();
        long lastTimestamp = snapshots.get(snapshots.size() - 1).getTimestamp();
        return lastTimestamp - firstTimestamp;
    }
}
