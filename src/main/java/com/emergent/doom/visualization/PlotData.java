package com.emergent.doom.visualization;

import java.util.Map;

/**
 * Immutable data structure representing a single-series plot.
 *
 * <p>This class encapsulates data needed for visualizing a single metric trajectory
 * over execution steps. It provides a bridge between the Java computation engine and
 * external visualization tools (matplotlib via Jython, JFreeChart, web-based charting, etc.).</p>
 *
 * <p><strong>Purpose in Emergent Doom Engine:</strong></p>
 * <ul>
 *   <li>Enables reproduction of Paper Figures 3A, 3B, 3C (sortedness trajectories)</li>
 *   <li>Enables reproduction of Paper Figure 8 (aggregation timelines)</li>
 *   <li>Provides plot-ready data structures for external tools</li>
 * </ul>
 *
 * <p><strong>Reference:</strong> Levin et al. (2024), Figures 3 and 8 show trajectory plots
 * of metrics over execution steps.</p>
 *
 * @see TrajectoryPlotter
 * @see TrajectoryAnalyzer
 */
public final class PlotData {
    
    /**
     * PURPOSE: Store the human-readable name of the metric being plotted.
     * USAGE: Used as the plot title or series label in visualization tools.
     * EXAMPLE: "Sortedness Value", "Algotype Aggregation Index"
     */
    private final String metricName;
    
    /**
     * PURPOSE: Store the x-axis values (typically step numbers or timestamps).
     * USAGE: Provides the independent variable for plotting.
     * EXAMPLE: [0, 1, 2, 3, 4, ...] for step numbers
     * INVARIANT: Must have same length as yValues
     */
    private final double[] xValues;
    
    /**
     * PURPOSE: Store the y-axis values (metric measurements at each step).
     * USAGE: Provides the dependent variable (the metric value trajectory).
     * EXAMPLE: [45.2, 52.1, 68.7, 85.3, 92.1] for sortedness percentages
     * INVARIANT: Must have same length as xValues
     */
    private final double[] yValues;
    
    /**
     * PURPOSE: Store optional metadata about the metric trajectory.
     * USAGE: Provides statistical summaries (min, max, mean, stddev) for analysis.
     * EXAMPLE: {"min": 45.2, "max": 92.1, "mean": 68.68, "stddev": 17.3}
     * INVARIANT: May be null; if present, values must be valid doubles
     */
    private final Map<String, Double> metadata;
    
    /**
     * PURPOSE: Construct an immutable PlotData instance with all required data.
     * 
     * INPUTS:
     *   - metricName: Human-readable metric name
     *   - xValues: Array of x-axis values (steps/time)
     *   - yValues: Array of y-axis values (metric measurements)
     *   - metadata: Optional map of statistical metadata
     * 
     * PROCESS:
     *   1. Validate that metricName is not null/empty
     *   2. Validate that xValues and yValues are not null and have same length
     *   3. Create defensive copies of arrays to ensure immutability
     *   4. Create defensive copy of metadata map if provided
     *   5. Store all data as final fields
     * 
     * OUTPUTS: Immutable PlotData instance
     * 
     * THROWS: IllegalArgumentException if validation fails
     * 
     * DESIGN RATIONALE:
     *   - Immutability ensures thread-safety and prevents accidental modification
     *   - Defensive copying prevents external modification of internal state
     *   - Validation ensures data consistency for downstream visualization tools
     * 
     * IMPLEMENTATION:
     *   This section constructs an immutable, validated PlotData object.
     *   It integrates with the PlotData class by:
     *   - Enforcing strict validation on all inputs
     *   - Creating defensive copies to ensure immutability
     *   - Storing data in final fields for thread-safety
     *   
     *   This satisfies the requirement of creating a robust, immutable data structure
     *   that can be safely shared across threads and passed to external tools.
     */
    public PlotData(String metricName, double[] xValues, double[] yValues, 
                    Map<String, Double> metadata) {
        // Validate metricName
        if (metricName == null || metricName.trim().isEmpty()) {
            throw new IllegalArgumentException("Metric name cannot be null or empty");
        }
        
        // Validate arrays
        if (xValues == null || yValues == null) {
            throw new IllegalArgumentException("xValues and yValues cannot be null");
        }
        
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException(
                "xValues and yValues must have same length. Got: " + 
                xValues.length + " vs " + yValues.length);
        }
        
        if (xValues.length == 0) {
            throw new IllegalArgumentException("Arrays cannot be empty");
        }
        
        // Store fields with defensive copies
        this.metricName = metricName;
        this.xValues = java.util.Arrays.copyOf(xValues, xValues.length);
        this.yValues = java.util.Arrays.copyOf(yValues, yValues.length);
        
        // Create defensive copy of metadata if provided
        if (metadata != null) {
            this.metadata = java.util.Collections.unmodifiableMap(
                new java.util.HashMap<>(metadata));
        } else {
            this.metadata = null;
        }
    }
    
    /**
     * PURPOSE: Get the metric name for this plot.
     * 
     * OUTPUTS: The metric name string
     * 
     * USAGE: Used for plot titles, legends, and identifying the data series
     * 
     * IMPLEMENTATION:
     *   Simple getter that returns the immutable metric name.
     *   No defensive copy needed since String is immutable.
     */
    public String getMetricName() {
        return metricName;
    }
    
    /**
     * PURPOSE: Get a copy of the x-axis values.
     * 
     * OUTPUTS: Defensive copy of x-values array
     * 
     * DESIGN RATIONALE:
     *   - Returns copy to preserve immutability
     *   - Prevents external code from modifying internal state
     * 
     * IMPLEMENTATION:
     *   Returns a defensive copy to prevent external modification.
     *   Ensures PlotData remains immutable after construction.
     */
    public double[] getXValues() {
        return java.util.Arrays.copyOf(xValues, xValues.length);
    }
    
    /**
     * PURPOSE: Get a copy of the y-axis values.
     * 
     * OUTPUTS: Defensive copy of y-values array
     * 
     * DESIGN RATIONALE:
     *   - Returns copy to preserve immutability
     *   - Prevents external code from modifying internal state
     * 
     * IMPLEMENTATION:
     *   Returns a defensive copy to prevent external modification.
     *   Ensures PlotData remains immutable after construction.
     */
    public double[] getYValues() {
        return java.util.Arrays.copyOf(yValues, yValues.length);
    }
    
    /**
     * PURPOSE: Get the metadata map for this plot.
     * 
     * OUTPUTS: Unmodifiable view of metadata, or null if no metadata
     * 
     * DESIGN RATIONALE:
     *   - Returns unmodifiable map to preserve immutability
     *   - Allows read access without risking modification
     * 
     * IMPLEMENTATION:
     *   Returns the unmodifiable map created during construction.
     *   No additional wrapping needed since map was already made unmodifiable.
     */
    public Map<String, Double> getMetadata() {
        return metadata;
    }
    
    /**
     * PURPOSE: Get the number of data points in this plot.
     * 
     * OUTPUTS: Length of x/y value arrays
     * 
     * USAGE: Useful for validation and iteration
     * 
     * IMPLEMENTATION:
     *   Returns the length of xValues (same as yValues per constructor validation).
     */
    public int getDataPointCount() {
        return xValues.length;
    }
    
    /**
     * PURPOSE: Check if this plot has metadata.
     * 
     * OUTPUTS: true if metadata map is non-null and non-empty
     * 
     * USAGE: Guards against null pointer exceptions when accessing metadata
     * 
     * IMPLEMENTATION:
     *   Checks if metadata is non-null and non-empty.
     */
    public boolean hasMetadata() {
        return metadata != null && !metadata.isEmpty();
    }
}
