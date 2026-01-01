package com.emergent.doom.visualization;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.experiment.SortDirection;
import com.emergent.doom.metrics.AlgotypeAggregationIndex;
import com.emergent.doom.metrics.MonotonicityError;
import com.emergent.doom.metrics.SortednessValue;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;

import java.util.List;

/**
 * Analyzes Probe snapshots to compute metric trajectories over execution time.
 *
 * <p>This class bridges the gap between raw execution data (captured by {@link Probe})
 * and trajectory visualization (enabled by {@link TrajectoryPlotter} and export utilities).
 * It computes time-series data for various metrics by applying metric computations to
 * each snapshot in a probe's history.</p>
 *
 * <p><strong>Purpose in Emergent Doom Engine:</strong></p>
 * <ul>
 *   <li>Enables Paper Figure 3 (sortedness vs steps) by computing sortedness trajectories</li>
 *   <li>Enables Paper Figure 8 (aggregation vs time) by computing aggregation trajectories</li>
 *   <li>Supports general metric trajectory analysis for any custom metric</li>
 *   <li>Provides foundation for data export to external visualization tools</li>
 * </ul>
 *
 * <p><strong>Reference:</strong> Levin et al. (2024), p.6: "information collected by the
 * Probe is stored as a .npy file for downstream analysis". This class performs the
 * "downstream analysis" by computing metrics over the probe's snapshot history.</p>
 *
 * <p><strong>Design:</strong> This class is stateless and thread-safe. All methods are
 * static or instance methods that operate only on input parameters.</p>
 *
 * @param <T> the type of cell
 * @see Probe
 * @see StepSnapshot
 * @see TrajectoryPlotter
 * @deprecated Use {@link com.emergent.doom.analysis.TrajectoryAnalyzer} instead.
 *             This class will be removed in a future release. The analysis package
 *             version provides both Probe-based convenience methods (matching this class)
 *             and flexible low-level APIs with custom Metric support.
 */
@Deprecated
public class TrajectoryAnalyzer<T extends Cell<T>> {
    
    /**
     * PURPOSE: Compute the sortedness value trajectory over execution steps.
     * 
     * INPUTS:
     *   - probe: The probe containing snapshot history
     *   - direction: The target sort direction (INCREASING or DECREASING)
     * 
     * PROCESS:
     *   1. Validate that probe is not null and contains snapshots
     *   2. Create a SortednessValue metric instance with the specified direction
     *   3. Iterate through all snapshots in chronological order
     *   4. For each snapshot:
     *      a. Extract the cell array state
     *      b. Compute sortedness using the metric
     *      c. Append the sortedness value to the result list
     *   5. Return the complete trajectory as a list indexed by step number
     * 
     * OUTPUTS: List of sortedness values (0.0 to 100.0) indexed by step
     * 
     * THROWS: IllegalArgumentException if probe is null or empty
     * 
     * USAGE:
     *   - Generate data for Paper Figure 3A, 3B, 3C (sortedness trajectories)
     *   - Analyze convergence rate of sorting algorithms
     *   - Compare performance across different configurations
     * 
     * DESIGN RATIONALE:
     *   - Uses existing SortednessValue metric for consistency
     *   - Returns list (not array) for easier manipulation and export
     *   - Each index corresponds to the step number for direct plotting
     * 
     * IMPLEMENTATION NOTES:
     *   This is the MAIN ENTRY POINT for trajectory analysis. It orchestrates the
     *   complete workflow of extracting snapshots and computing metrics. This method
     *   satisfies the role of the program's primary analysis function by:
     *   - Validating input data via isProbeValid() (to be implemented)
     *   - Creating appropriate metric instances
     *   - Delegating metric computation to existing SortednessValue class
     *   - Building and returning the trajectory list
     *   
     *   Other unimplemented trajectory methods follow this same pattern.
     */
    public List<Double> computeSortednessTrajectory(Probe<T> probe, SortDirection direction) {
        // Step 1: Validate inputs
        if (probe == null) {
            throw new IllegalArgumentException("Probe cannot be null");
        }
        if (probe.getSnapshotCount() == 0) {
            throw new IllegalArgumentException("Probe contains no snapshots");
        }
        if (direction == null) {
            direction = SortDirection.INCREASING; // Default direction
        }
        
        // Step 2: Create metric instance with specified direction
        SortednessValue<T> metric = new SortednessValue<>(direction);
        
        // Step 3: Build trajectory by computing metric at each snapshot
        java.util.ArrayList<Double> trajectory = new java.util.ArrayList<>();
        List<StepSnapshot<T>> snapshots = probe.getSnapshots();
        
        // Step 4: Iterate through snapshots and compute sortedness
        for (StepSnapshot<T> snapshot : snapshots) {
            T[] cellStates = snapshot.getCellStates();
            double sortedness = metric.compute(cellStates);
            trajectory.add(sortedness);
        }
        
        // Step 5: Return complete trajectory
        return trajectory;
    }
    
    /**
     * PURPOSE: Compute the algotype aggregation trajectory over execution steps.
     * 
     * INPUTS:
     *   - probe: The probe containing snapshot history
     * 
     * PROCESS:
     *   1. Validate that probe is not null and contains snapshots
     *   2. Create an AlgotypeAggregationIndex metric instance
     *   3. Iterate through all snapshots in chronological order
     *   4. For each snapshot:
     *      a. Extract the cell array state
     *      b. Compute aggregation using the metric
     *      c. Append the aggregation value to the result list
     *   5. Return the complete trajectory as a list indexed by step number
     * 
     * OUTPUTS: List of aggregation values (0.0 to 100.0) indexed by step
     * 
     * THROWS: IllegalArgumentException if probe is null or empty
     * 
     * USAGE:
     *   - Generate data for Paper Figure 8 (algotype aggregation timelines)
     *   - Analyze spatial clustering emergence in chimeric populations
     *   - Compare aggregation patterns across different algotype ratios
     * 
     * DESIGN RATIONALE:
     *   - Uses existing AlgotypeAggregationIndex metric for consistency
     *   - Returns list for easy integration with plotting and export utilities
     *   - Critical for understanding emergent self-sorting in chimeric arrays
     * 
     * IMPLEMENTATION:
     *   This section computes aggregation trajectory using the same pattern as
     *   computeSortednessTrajectory. It integrates with the TrajectoryAnalyzer by
     *   following the established pattern of validation, metric instantiation,
     *   iteration, and list building. Uses AlgotypeAggregationIndex to measure
     *   spatial clustering of cells by algotype.
     */
    public List<Double> computeAggregationTrajectory(Probe<T> probe) {
        // Step 1: Validate inputs
        if (probe == null) {
            throw new IllegalArgumentException("Probe cannot be null");
        }
        if (probe.getSnapshotCount() == 0) {
            throw new IllegalArgumentException("Probe contains no snapshots");
        }
        
        // Step 2: Create metric instance
        AlgotypeAggregationIndex<T> metric = new AlgotypeAggregationIndex<>();
        
        // Step 3: Build trajectory by computing metric at each snapshot
        java.util.ArrayList<Double> trajectory = new java.util.ArrayList<>();
        List<StepSnapshot<T>> snapshots = probe.getSnapshots();
        
        // Step 4: Iterate through snapshots and compute aggregation
        for (StepSnapshot<T> snapshot : snapshots) {
            T[] cellStates = snapshot.getCellStates();
            double aggregation = metric.compute(cellStates);
            trajectory.add(aggregation);
        }
        
        // Step 5: Return complete trajectory
        return trajectory;
    }
    
    /**
     * PURPOSE: Compute the monotonicity error trajectory over execution steps.
     * 
     * INPUTS:
     *   - probe: The probe containing snapshot history
     *   - direction: The target sort direction (currently unused but reserved for future)
     * 
     * PROCESS:
     *   1. Validate that probe is not null and contains snapshots
     *   2. Create a MonotonicityError metric instance
     *   3. Iterate through all snapshots in chronological order
     *   4. For each snapshot:
     *      a. Extract the cell array state
     *      b. Compute inversion count using the metric
     *      c. Append the error value to the result list
     *   5. Return the complete trajectory as a list indexed by step number
     * 
     * OUTPUTS: List of inversion counts indexed by step
     * 
     * THROWS: IllegalArgumentException if probe is null or empty
     * 
     * USAGE:
     *   - Analyze convergence behavior using inversion counts
     *   - Compare to traditional sorting algorithm complexity
     *   - Validate that arrays are monotonically improving
     * 
     * DESIGN RATIONALE:
     *   - Uses existing MonotonicityError metric for consistency
     *   - Inversion count is a classical measure in sorting analysis
     *   - Decreasing inversions indicate progress toward sorted state
     * 
     * IMPLEMENTATION:
     *   This section computes monotonicity error trajectory using the same pattern.
     *   It integrates with the TrajectoryAnalyzer by following the established pattern.
     *   Uses MonotonicityError to count inversions (pairs out of order).
     *   The direction parameter is included for API consistency but not currently used
     *   by MonotonicityError (reserved for future directional inversion counting).
     */
    public List<Double> computeMonotonicityTrajectory(Probe<T> probe, SortDirection direction) {
        // Step 1: Validate inputs
        if (probe == null) {
            throw new IllegalArgumentException("Probe cannot be null");
        }
        if (probe.getSnapshotCount() == 0) {
            throw new IllegalArgumentException("Probe contains no snapshots");
        }
        
        // Step 2: Create metric instance
        MonotonicityError<T> metric = new MonotonicityError<>();
        
        // Step 3: Build trajectory by computing metric at each snapshot
        java.util.ArrayList<Double> trajectory = new java.util.ArrayList<>();
        List<StepSnapshot<T>> snapshots = probe.getSnapshots();
        
        // Step 4: Iterate through snapshots and compute monotonicity error
        for (StepSnapshot<T> snapshot : snapshots) {
            T[] cellStates = snapshot.getCellStates();
            double error = metric.compute(cellStates);
            trajectory.add(error);
        }
        
        // Step 5: Return complete trajectory
        return trajectory;
    }
    
    /**
     * PURPOSE: Extract the swap count trajectory from probe snapshots.
     * 
     * INPUTS:
     *   - probe: The probe containing snapshot history
     * 
     * PROCESS:
     *   1. Validate that probe is not null and contains snapshots
     *   2. Iterate through all snapshots in chronological order
     *   3. For each snapshot:
     *      a. Extract the swap count for that step
     *      b. Append the swap count to the result list
     *   4. Return the complete trajectory as a list indexed by step number
     * 
     * OUTPUTS: List of swap counts indexed by step
     * 
     * THROWS: IllegalArgumentException if probe is null or empty
     * 
     * USAGE:
     *   - Analyze swap activity over time
     *   - Compare swap efficiency across algorithms
     *   - Correlate swap counts with sortedness improvements
     * 
     * DESIGN RATIONALE:
     *   - Swap count is already recorded in snapshots, just needs extraction
     *   - No metric computation required, just data access
     *   - Useful for understanding algorithm behavior and efficiency
     * 
     * IMPLEMENTATION:
     *   This section extracts swap counts from snapshots without metric computation.
     *   It integrates with the TrajectoryAnalyzer by following a simplified version
     *   of the established pattern (no metric instantiation needed).
     *   Simply extracts the swap count field from each snapshot.
     */
    public List<Integer> computeSwapCountTrajectory(Probe<T> probe) {
        // Step 1: Validate inputs
        if (probe == null) {
            throw new IllegalArgumentException("Probe cannot be null");
        }
        if (probe.getSnapshotCount() == 0) {
            throw new IllegalArgumentException("Probe contains no snapshots");
        }
        
        // Step 2: Build trajectory by extracting swap counts
        java.util.ArrayList<Integer> trajectory = new java.util.ArrayList<>();
        List<StepSnapshot<T>> snapshots = probe.getSnapshots();
        
        // Step 3: Iterate through snapshots and extract swap counts
        for (StepSnapshot<T> snapshot : snapshots) {
            int swapCount = snapshot.getSwapCount();
            trajectory.add(swapCount);
        }
        
        // Step 4: Return complete trajectory
        return trajectory;
    }
    
    /**
     * PURPOSE: Validate that a probe contains usable snapshot data.
     * 
     * INPUTS:
     *   - probe: The probe to validate
     * 
     * PROCESS:
     *   1. Check if probe is null
     *   2. Check if probe has zero snapshots
     *   3. Return validation result
     * 
     * OUTPUTS: true if probe is valid and has snapshots, false otherwise
     * 
     * USAGE:
     *   - Guard clause for all trajectory computation methods
     *   - Prevents null pointer exceptions and empty list results
     * 
     * DESIGN RATIONALE:
     *   - Centralizes validation logic to reduce code duplication
     *   - Enables consistent error handling across all methods
     * 
     * IMPLEMENTATION:
     *   This section validates probe data before computation.
     *   It integrates with all trajectory methods by providing a centralized
     *   validation check. Note: This method is currently not used by the
     *   implemented trajectory methods (they perform inline validation),
     *   but is kept for potential future refactoring or external use.
     */
    private boolean isProbeValid(Probe<T> probe) {
        return probe != null && probe.getSnapshotCount() > 0;
    }
}
