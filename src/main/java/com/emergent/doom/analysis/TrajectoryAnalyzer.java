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
 * <p><strong>Input Assumptions:</strong> All methods assume snapshots are provided
 * in chronological order (sorted by step number / timestamp). Out-of-order snapshots
 * will produce incorrect results for time-based calculations.</p>
 *
 * @param <T> the type of cell
 */
public class TrajectoryAnalyzer<T extends Cell<T>> {

    /**
     * Compute a metric value at each step of the trajectory.
     *
     * <p>Iterates through all snapshots and computes the given metric
     * on the cell states at each step, producing a time series of metric values.</p>
     *
     * @param snapshots the execution history as a list of step snapshots (chronologically ordered)
     * @param metric the metric to compute at each step
     * @return list of metric values, one per snapshot, in the same order as input;
     *         empty list if snapshots is null or empty
     */
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
    
    /**
     * Extract swap counts from each step of the trajectory.
     *
     * <p>Produces a time series of swap counts, useful for analyzing
     * sorting progress and detecting convergence patterns.</p>
     *
     * @param snapshots the execution history as a list of step snapshots (chronologically ordered)
     * @return list of swap counts, one per snapshot, in the same order as input;
     *         empty list if snapshots is null or empty
     */
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
    
    /**
     * Find the step at which the sorting process converged.
     *
     * <p>Convergence is defined as the <em>first</em> step of a sequence of
     * {@code consecutiveZeroSwaps} steps with zero swaps. This represents
     * the "time of entry into the attractor" - when the system first reaches
     * and stays in a stable sorted state.</p>
     *
     * <p><strong>Semantics:</strong> Returns the step number where the stable
     * sequence begins, not the step where it was confirmed. For example, if
     * steps [5,6,7] all have zero swaps and consecutiveZeroSwaps=3, this
     * returns 5 (start of stable sequence), not 7 (confirmation step).</p>
     *
     * @param snapshots the execution history as a list of step snapshots (chronologically ordered)
     * @param consecutiveZeroSwaps number of consecutive zero-swap steps required to declare convergence
     * @return the step number where convergence began, or -1 if never converged
     *         or if inputs are invalid (null, empty, or consecutiveZeroSwaps &lt;= 0)
     */
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
    
    /**
     * Generate a text-based visualization of the trajectory.
     *
     * <p>Produces a formatted table showing step number, swap count, and
     * array size for each snapshot, suitable for console output or logging.</p>
     *
     * @param snapshots the execution history as a list of step snapshots (chronologically ordered)
     * @param maxSnapshotsToShow maximum number of snapshots to include in output;
     *                           remaining snapshots are summarized with a count
     * @return formatted multi-line string visualization, or informative message
     *         if snapshots is null or empty
     */
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
    
    /**
     * Calculate the total wall-clock execution time of the trajectory.
     *
     * <p>Computes the difference between the timestamp of the last snapshot
     * and the first snapshot. This represents the actual elapsed time for
     * the sorting process.</p>
     *
     * <p><strong>Assumption:</strong> Snapshots must be in chronological order
     * with monotonically increasing timestamps. Out-of-order snapshots will
     * produce incorrect (possibly negative) results.</p>
     *
     * @param snapshots the execution history as a list of step snapshots (chronologically ordered)
     * @return elapsed time in nanoseconds between first and last snapshot;
     *         returns 0 if snapshots is null, empty, or contains only one snapshot
     */
    public long getTotalExecutionTime(List<StepSnapshot<T>> snapshots) {
        if (snapshots == null || snapshots.size() < 2) {
            return 0L;
        }
        long firstTimestamp = snapshots.get(0).getTimestamp();
        long lastTimestamp = snapshots.get(snapshots.size() - 1).getTimestamp();
        return lastTimestamp - firstTimestamp;
    }
}
