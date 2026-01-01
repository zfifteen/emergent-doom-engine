package com.emergent.doom.analysis;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.experiment.SortDirection;
import com.emergent.doom.metrics.AlgotypeAggregationIndex;
import com.emergent.doom.metrics.Metric;
import com.emergent.doom.metrics.Monotonicity;
import com.emergent.doom.metrics.MonotonicityError;
import com.emergent.doom.metrics.SortednessValue;
import com.emergent.doom.metrics.SpearmanDistance;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Analyzes execution trajectories by computing metrics over snapshot sequences.
 *
 * <p>The TrajectoryAnalyzer provides both low-level flexible APIs (using raw snapshot
 * lists and custom metrics) and high-level convenience methods (using Probe objects
 * and predefined metrics).</p>
 *
 * <p><strong>Usage patterns:</strong></p>
 * <ul>
 *   <li>Low-level: {@link #computeMetricTrajectory(List, Metric)} - any metric on snapshot list</li>
 *   <li>High-level: {@link #computeSortednessTrajectory(Probe, SortDirection)} - sortedness from Probe</li>
 * </ul>
 *
 * <p><strong>Input Assumptions:</strong> All methods assume snapshots are provided
 * in chronological order (sorted by step number / timestamp). Out-of-order snapshots
 * will produce incorrect results for time-based calculations.</p>
 *
 * <p><strong>Reference:</strong> Levin et al. (2024), p.6: "information collected by the
 * Probe is stored as a .npy file for downstream analysis". This class performs the
 * "downstream analysis" by computing metrics over the probe's snapshot history.</p>
 *
 * @param <T> the type of cell
 */
public class TrajectoryAnalyzer<T extends Cell<T>> {

    // ========== Low-Level API (Snapshot Lists + Custom Metrics) ==========

    /**
     * Compute a metric value at each step of the trajectory.
     *
     * <p>Iterates through all snapshots and computes the given metric
     * on the cell states at each step, producing a time series of metric values.</p>
     *
     * @param snapshots the execution history as a list of step snapshots (chronologically ordered)
     * @param metric the metric to compute at each step (must not be null if snapshots is non-empty)
     * @return list of metric values, one per snapshot, in the same order as input;
     *         empty list if snapshots is null or empty
     * @throws NullPointerException if metric is null and snapshots is non-empty
     */
    public List<Double> computeMetricTrajectory(List<StepSnapshot<T>> snapshots, Metric<T> metric) {
        if (snapshots == null || snapshots.isEmpty()) {
            return new ArrayList<>();
        }
        Objects.requireNonNull(metric, "Metric cannot be null when computing trajectory");
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

    // ========== High-Level API (Probe-based Convenience Methods) ==========

    /**
     * Compute the sortedness value trajectory from a Probe.
     *
     * <p>Enables Paper Figure 3A, 3B, 3C (sortedness vs steps) by computing
     * sortedness values for each recorded snapshot.</p>
     *
     * @param probe the probe containing snapshot history
     * @param direction the target sort direction (INCREASING or DECREASING);
     *                  defaults to INCREASING if null
     * @return list of sortedness values (0.0 to 100.0) indexed by step
     * @throws IllegalArgumentException if probe is null or empty
     * @see SortednessValue
     */
    public List<Double> computeSortednessTrajectory(Probe<T> probe, SortDirection direction) {
        validateProbe(probe);
        if (direction == null) {
            direction = SortDirection.INCREASING;
        }
        return computeMetricTrajectory(probe.getSnapshots(), new SortednessValue<>(direction));
    }

    /**
     * Compute the algotype aggregation trajectory from a Probe.
     *
     * <p>Enables Paper Figure 8 (aggregation timelines) by computing
     * aggregation values for chimeric populations.</p>
     *
     * @param probe the probe containing snapshot history
     * @return list of aggregation values (0.0 to 100.0) indexed by step
     * @throws IllegalArgumentException if probe is null or empty
     * @see AlgotypeAggregationIndex
     */
    public List<Double> computeAggregationTrajectory(Probe<T> probe) {
        validateProbe(probe);
        return computeMetricTrajectory(probe.getSnapshots(), new AlgotypeAggregationIndex<>());
    }

    /**
     * Compute the monotonicity trajectory from a Probe.
     *
     * <p>Measures the percentage of cells that follow monotonic order at each step.</p>
     *
     * @param probe the probe containing snapshot history
     * @return list of monotonicity values (0.0 to 100.0) indexed by step
     * @throws IllegalArgumentException if probe is null or empty
     * @see Monotonicity
     */
    public List<Double> computeMonotonicityTrajectory(Probe<T> probe) {
        validateProbe(probe);
        return computeMetricTrajectory(probe.getSnapshots(), new Monotonicity<>());
    }

    /**
     * Compute the monotonicity error trajectory from a Probe.
     *
     * <p>Counts adjacent inversions (pairs out of order) at each step.</p>
     *
     * @param probe the probe containing snapshot history
     * @return list of inversion counts indexed by step
     * @throws IllegalArgumentException if probe is null or empty
     * @see MonotonicityError
     */
    public List<Double> computeMonotonicityErrorTrajectory(Probe<T> probe) {
        validateProbe(probe);
        return computeMetricTrajectory(probe.getSnapshots(), new MonotonicityError<>());
    }

    /**
     * Compute the Spearman distance trajectory from a Probe.
     *
     * <p>Measures total displacement from sorted positions at each step.</p>
     *
     * @param probe the probe containing snapshot history
     * @return list of Spearman distances indexed by step
     * @throws IllegalArgumentException if probe is null or empty
     * @see SpearmanDistance
     */
    public List<Double> computeSpearmanDistanceTrajectory(Probe<T> probe) {
        validateProbe(probe);
        return computeMetricTrajectory(probe.getSnapshots(), new SpearmanDistance<>());
    }

    /**
     * Extract the swap count trajectory from a Probe.
     *
     * <p>Returns the number of swaps at each step, useful for analyzing
     * convergence patterns.</p>
     *
     * @param probe the probe containing snapshot history
     * @return list of swap counts indexed by step
     * @throws IllegalArgumentException if probe is null or empty
     */
    public List<Integer> computeSwapCountTrajectory(Probe<T> probe) {
        validateProbe(probe);
        return extractSwapCounts(probe.getSnapshots());
    }

    /**
     * Find the convergence step from a Probe.
     *
     * @param probe the probe containing snapshot history
     * @param consecutiveZeroSwaps number of consecutive zero-swap steps required
     * @return the step number where convergence began, or -1 if never converged
     * @throws IllegalArgumentException if probe is null or empty
     */
    public int findConvergenceStep(Probe<T> probe, int consecutiveZeroSwaps) {
        validateProbe(probe);
        return findConvergenceStep(probe.getSnapshots(), consecutiveZeroSwaps);
    }

    /**
     * Validate that a probe contains usable snapshot data.
     *
     * @param probe the probe to validate
     * @throws IllegalArgumentException if probe is null or contains no snapshots
     */
    private void validateProbe(Probe<T> probe) {
        if (probe == null) {
            throw new IllegalArgumentException("Probe cannot be null");
        }
        if (probe.getSnapshotCount() == 0) {
            throw new IllegalArgumentException("Probe contains no snapshots");
        }
    }
}
