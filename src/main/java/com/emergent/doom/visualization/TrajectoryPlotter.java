package com.emergent.doom.visualization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates plot-ready data structures for external visualization tools.
 *
 * <p>This class transforms metric trajectories (computed by {@link TrajectoryAnalyzer})
 * into structured plot data that can be consumed by external visualization libraries
 * such as matplotlib (via Jython), JFreeChart, or web-based charting tools.</p>
 *
 * <p><strong>Purpose in Emergent Doom Engine:</strong></p>
 * <ul>
 *   <li>Prepare data for reproducing Paper Figures 3A, 3B, 3C (sortedness plots)</li>
 *   <li>Prepare data for reproducing Paper Figure 8 (aggregation timeline plots)</li>
 *   <li>Generate comparison plots across multiple algorithms or configurations</li>
 *   <li>Provide metadata for axis labels, titles, and statistical summaries</li>
 * </ul>
 *
 * <p><strong>Reference:</strong> Levin et al. (2024), Figures 3 and 8 show trajectory
 * plots with x-axis as execution steps and y-axis as metric values.</p>
 *
 * <p><strong>Design:</strong> This class does NOT perform actual plotting (Java lacks
 * built-in plotting). Instead, it prepares data structures that external tools can
 * easily consume. Think of this as a "plot data factory".</p>
 *
 * @see PlotData
 * @see MultiSeriesPlotData
 * @see TrajectoryAnalyzer
 */
public class TrajectoryPlotter {
    
    /**
     * PURPOSE: Generate plot data for a single metric trajectory.
     * 
     * INPUTS:
     *   - metricName: Human-readable name of the metric
     *   - trajectory: List of metric values indexed by step number
     * 
     * PROCESS:
     *   1. Validate that metricName is not null/empty
     *   2. Validate that trajectory is not null and not empty
     *   3. Generate x-values as step numbers: [0, 1, 2, ..., trajectory.size()-1]
     *   4. Convert trajectory list to y-values array
     *   5. Compute metadata statistics:
     *      a. min: minimum value in trajectory
     *      b. max: maximum value in trajectory
     *      c. mean: average value across trajectory
     *      d. stddev: standard deviation (optional, may require implementation)
     *   6. Create and return PlotData instance with all data and metadata
     * 
     * OUTPUTS: PlotData instance ready for visualization
     * 
     * THROWS: IllegalArgumentException if inputs are invalid
     * 
     * USAGE:
     *   - Convert sortedness trajectory to plot data for Figure 3
     *   - Convert aggregation trajectory to plot data for Figure 8
     *   - Enable export to CSV/JSON with metadata
     * 
     * DESIGN RATIONALE:
     *   - Encapsulates x-value generation (step numbers are implicit)
     *   - Auto-computes useful statistics to avoid manual calculation
     *   - Returns immutable PlotData to prevent accidental modification
     * 
     * EXAMPLE:
     *   List<Double> sortedness = analyzer.computeSortednessTrajectory(probe, INCREASING);
     *   PlotData plotData = plotter.generatePlotData("Sortedness Value", sortedness);
     * 
     * IMPLEMENTATION NOTES:
     *   This is the MAIN ENTRY POINT for plot data generation. It orchestrates the
     *   complete workflow of converting trajectory data to plot-ready format by:
     *   - Validating inputs
     *   - Generating x-values via generateStepNumbers() (to be implemented)
     *   - Converting list to array via convertToArray() (to be implemented)
     *   - Computing statistics via computeStatistics() (to be implemented)
     *   - Creating the immutable PlotData object
     *   
     *   This method coordinates all the unimplemented helper methods to produce
     *   the final plot-ready data structure.
     */
    public PlotData generatePlotData(String metricName, List<Double> trajectory) {
        // Step 1: Validate metricName
        if (metricName == null || metricName.trim().isEmpty()) {
            throw new IllegalArgumentException("Metric name cannot be null or empty");
        }
        
        // Step 2: Validate trajectory
        if (trajectory == null || trajectory.isEmpty()) {
            throw new IllegalArgumentException("Trajectory cannot be null or empty");
        }
        
        // Step 3: Generate x-values (step numbers)
        double[] xValues = generateStepNumbers(trajectory.size());
        
        // Step 4: Convert trajectory to y-values array
        double[] yValues = convertToArray(trajectory);
        
        // Step 5: Compute metadata statistics
        Map<String, Double> metadata = computeStatistics(trajectory);
        
        // Step 6: Create and return PlotData
        return new PlotData(metricName, xValues, yValues, metadata);
    }
    
    /**
     * PURPOSE: Generate multi-series plot data for comparing multiple trajectories.
     * 
     * INPUTS:
     *   - trajectories: Map of series names to their metric value trajectories
     * 
     * PROCESS:
     *   1. Validate that trajectories map is not null and not empty
     *   2. Validate that all trajectories have the same length (shared x-axis)
     *   3. For each trajectory in the map:
     *      a. Extract series name (map key)
     *      b. Generate PlotData for that series
     *      c. Add to series list
     *   4. Determine if all series share same x-values (they should, by validation)
     *   5. Create plot-level metadata (optional: title, axis labels)
     *   6. Create and return MultiSeriesPlotData instance
     * 
     * OUTPUTS: MultiSeriesPlotData instance ready for multi-series visualization
     * 
     * THROWS: IllegalArgumentException if inputs are invalid or trajectories have mismatched lengths
     * 
     * USAGE:
     *   - Compare sortedness across Bubble vs Selection algorithms
     *   - Compare aggregation across different chimeric ratios
     *   - Overlay multiple metrics on same timeline
     * 
     * DESIGN RATIONALE:
     *   - Enforces shared x-axis by validating trajectory lengths
     *   - Automatically generates PlotData for each series
     *   - Returns immutable multi-series structure for thread-safety
     * 
     * EXAMPLE:
     *   Map<String, List<Double>> trajectories = new HashMap<>();
     *   trajectories.put("Bubble", bubbleSortedness);
     *   trajectories.put("Selection", selectionSortedness);
     *   MultiSeriesPlotData plotData = plotter.generateMultiSeriesPlotData(trajectories);
     * 
     * IMPLEMENTATION:
     *   This section generates multi-series plot data from a map of trajectories.
     *   It integrates with TrajectoryPlotter by reusing generatePlotData for each series.
     *   Validates that all trajectories have the same length to ensure shared x-axis.
     */
    public MultiSeriesPlotData generateMultiSeriesPlotData(Map<String, List<Double>> trajectories) {
        // Step 1: Validate trajectories map
        if (trajectories == null || trajectories.isEmpty()) {
            throw new IllegalArgumentException("Trajectories map cannot be null or empty");
        }
        
        // Step 2: Validate all trajectories have same length
        Integer expectedLength = null;
        String firstSeries = null;
        
        for (Map.Entry<String, List<Double>> entry : trajectories.entrySet()) {
            if (entry.getValue() == null) {
                throw new IllegalArgumentException(
                    "Trajectory for series '" + entry.getKey() + "' is null");
            }
            
            if (expectedLength == null) {
                expectedLength = entry.getValue().size();
                firstSeries = entry.getKey();
            } else {
                if (entry.getValue().size() != expectedLength) {
                    throw new IllegalArgumentException(
                        "Trajectory length mismatch: '" + firstSeries + "' has " + 
                        expectedLength + " values, but '" + entry.getKey() + "' has " + 
                        entry.getValue().size() + " values");
                }
            }
        }
        
        // Step 3: Generate PlotData for each series
        java.util.List<String> seriesNames = new java.util.ArrayList<>();
        java.util.List<PlotData> seriesData = new java.util.ArrayList<>();
        
        for (Map.Entry<String, List<Double>> entry : trajectories.entrySet()) {
            String seriesName = entry.getKey();
            List<Double> trajectory = entry.getValue();
            
            PlotData plotData = generatePlotData(seriesName, trajectory);
            
            seriesNames.add(seriesName);
            seriesData.add(plotData);
        }
        
        // Step 4: All series share x-axis (validated above)
        boolean sharedXAxis = true;
        
        // Step 5: Create plot-level metadata (optional, currently null)
        Map<String, String> plotMetadata = null;
        
        // Step 6: Create and return MultiSeriesPlotData
        return new MultiSeriesPlotData(seriesNames, seriesData, sharedXAxis, plotMetadata);
    }
    
    /**
     * PURPOSE: Compute statistical metadata for a trajectory.
     * 
     * INPUTS:
     *   - trajectory: List of metric values
     * 
     * PROCESS:
     *   1. Validate that trajectory is not null and not empty
     *   2. Compute minimum value
     *   3. Compute maximum value
     *   4. Compute mean (average) value
     *   5. Compute standard deviation (optional: measures spread)
     *   6. Package results into a map with keys: "min", "max", "mean", "stddev"
     *   7. Return metadata map
     * 
     * OUTPUTS: Map of statistic name to value
     * 
     * THROWS: IllegalArgumentException if trajectory is null or empty
     * 
     * USAGE:
     *   - Called internally by generatePlotData to auto-compute metadata
     *   - Can be used externally for custom analysis
     * 
     * DESIGN RATIONALE:
     *   - Provides useful summary statistics without external libraries
     *   - Standard deviation gives insight into trajectory variability
     *   - Map format allows easy extension with additional statistics
     * 
     * IMPLEMENTATION:
     *   This section computes statistical metadata for a trajectory.
     *   It integrates with generatePlotData by providing the metadata parameter
     *   for PlotData construction. The implementation uses standard statistical
     *   formulas: min/max via iteration, mean via sum/count, and standard deviation
     *   via variance calculation.
     */
    private Map<String, Double> computeStatistics(List<Double> trajectory) {
        if (trajectory == null || trajectory.isEmpty()) {
            throw new IllegalArgumentException("Trajectory cannot be null or empty");
        }
        
        // Initialize statistics
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        double sum = 0.0;
        
        // First pass: compute min, max, sum
        for (Double value : trajectory) {
            if (value < min) min = value;
            if (value > max) max = value;
            sum += value;
        }
        
        // Compute mean
        double mean = sum / trajectory.size();
        
        // Second pass: compute variance for standard deviation
        double sumSquaredDiff = 0.0;
        for (Double value : trajectory) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        double variance = sumSquaredDiff / trajectory.size();
        double stddev = Math.sqrt(variance);
        
        // Package into map
        Map<String, Double> metadata = new HashMap<>();
        metadata.put("min", min);
        metadata.put("max", max);
        metadata.put("mean", mean);
        metadata.put("stddev", stddev);
        
        return metadata;
    }
    
    /**
     * PURPOSE: Generate x-values (step numbers) for a trajectory.
     * 
     * INPUTS:
     *   - trajectoryLength: Number of steps in the trajectory
     * 
     * PROCESS:
     *   1. Validate that trajectoryLength is positive
     *   2. Create array of size trajectoryLength
     *   3. Fill with sequential values: [0, 1, 2, ..., trajectoryLength-1]
     *   4. Return x-values array
     * 
     * OUTPUTS: Array of step numbers as doubles
     * 
     * THROWS: IllegalArgumentException if trajectoryLength <= 0
     * 
     * USAGE:
     *   - Called internally by generatePlotData
     *   - Provides x-axis values for plotting
     * 
     * DESIGN RATIONALE:
     *   - Step numbers start at 0 to match array indexing
     *   - Returns doubles for consistency with PlotData (allows future timestamping)
     *   - Encapsulates simple but repetitive logic
     * 
     * IMPLEMENTATION:
     *   This section generates sequential step numbers as x-values.
     *   It integrates with generatePlotData by providing the xValues parameter
     *   for PlotData construction. The implementation creates a simple sequence
     *   starting from 0, matching the standard indexing of trajectory lists.
     */
    private double[] generateStepNumbers(int trajectoryLength) {
        if (trajectoryLength <= 0) {
            throw new IllegalArgumentException("Trajectory length must be positive");
        }
        
        double[] stepNumbers = new double[trajectoryLength];
        for (int i = 0; i < trajectoryLength; i++) {
            stepNumbers[i] = (double) i;
        }
        
        return stepNumbers;
    }
    
    /**
     * PURPOSE: Convert List<Double> to double[] for PlotData.
     * 
     * INPUTS:
     *   - trajectory: List of metric values
     * 
     * PROCESS:
     *   1. Validate that trajectory is not null
     *   2. Create array of size trajectory.size()
     *   3. Copy values from list to array
     *   4. Return array
     * 
     * OUTPUTS: Array of metric values
     * 
     * THROWS: IllegalArgumentException if trajectory is null
     * 
     * USAGE:
     *   - Called internally by generatePlotData
     *   - Converts trajectory to PlotData-compatible format
     * 
     * DESIGN RATIONALE:
     *   - PlotData uses arrays for efficient storage and iteration
     *   - Trajectory computations return lists for ease of building
     *   - This method bridges the two representations
     * 
     * IMPLEMENTATION:
     *   This section converts a List<Double> to a primitive double[] array.
     *   It integrates with generatePlotData by providing the yValues parameter
     *   for PlotData construction. The implementation performs a simple list-to-array
     *   conversion, handling the boxing/unboxing of Double objects.
     */
    private double[] convertToArray(List<Double> trajectory) {
        if (trajectory == null) {
            throw new IllegalArgumentException("Trajectory cannot be null");
        }
        
        double[] array = new double[trajectory.size()];
        for (int i = 0; i < trajectory.size(); i++) {
            array[i] = trajectory.get(i);
        }
        
        return array;
    }
}
