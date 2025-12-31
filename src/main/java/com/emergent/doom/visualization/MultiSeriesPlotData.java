package com.emergent.doom.visualization;

import java.util.List;
import java.util.Map;

/**
 * Immutable data structure representing a multi-series plot.
 *
 * <p>This class encapsulates data needed for visualizing multiple metric trajectories
 * on the same axes, enabling comparison of different algorithms, configurations, or
 * chimeric population behaviors.</p>
 *
 * <p><strong>Use Cases in Emergent Doom Engine:</strong></p>
 * <ul>
 *   <li>Compare sortedness trajectories across different algotypes (Bubble vs Selection)</li>
 *   <li>Compare aggregation over time in different chimeric ratios</li>
 *   <li>Overlay multiple metrics on same timeline (sortedness + aggregation)</li>
 * </ul>
 *
 * <p><strong>Reference:</strong> Levin et al. (2024), Figure 8 shows multiple aggregation
 * trajectories for different chimeric population compositions.</p>
 *
 * @see PlotData
 * @see TrajectoryPlotter
 */
public final class MultiSeriesPlotData {
    
    /**
     * PURPOSE: Store the names of each data series in the multi-series plot.
     * USAGE: Used for legend labels and identifying individual series.
     * EXAMPLE: ["Bubble-only", "Selection-only", "50/50 Chimeric"]
     * INVARIANT: Must have same length as seriesData list
     */
    private final List<String> seriesNames;
    
    /**
     * PURPOSE: Store the PlotData for each series in the multi-series plot.
     * USAGE: Contains the actual data points for each series.
     * EXAMPLE: [PlotData(bubble trajectory), PlotData(selection trajectory), ...]
     * INVARIANT: Must have same length as seriesNames list
     */
    private final List<PlotData> seriesData;
    
    /**
     * PURPOSE: Indicate whether all series share the same x-axis values.
     * USAGE: Optimization flag for plotting - if true, x-values need only be stored once.
     * EXAMPLE: true when comparing algorithms over same step range [0, 1000]
     * INVARIANT: If true, all PlotData objects must have identical xValues arrays
     */
    private final boolean sharedXAxis;
    
    /**
     * PURPOSE: Store optional metadata about the entire multi-series plot.
     * USAGE: Provides plot-level information (title, axis labels, date generated, etc.).
     * EXAMPLE: {"title": "Chimeric Aggregation Comparison", "xLabel": "Steps", "yLabel": "Aggregation %"}
     * INVARIANT: May be null; if present, keys and values must be non-null
     */
    private final Map<String, String> plotMetadata;
    
    /**
     * PURPOSE: Construct an immutable MultiSeriesPlotData instance.
     * 
     * INPUTS:
     *   - seriesNames: List of series names (for legend)
     *   - seriesData: List of PlotData objects (one per series)
     *   - sharedXAxis: Whether all series use same x-values
     *   - plotMetadata: Optional plot-level metadata
     * 
     * PROCESS:
     *   1. Validate that seriesNames and seriesData are not null
     *   2. Validate that both lists have same non-zero length
     *   3. If sharedXAxis is true, validate that all PlotData have identical x-values
     *   4. Create defensive copies of lists to ensure immutability
     *   5. Create defensive copy of metadata map if provided
     *   6. Store all data as final fields
     * 
     * OUTPUTS: Immutable MultiSeriesPlotData instance
     * 
     * THROWS: IllegalArgumentException if validation fails
     * 
     * DESIGN RATIONALE:
     *   - Immutability ensures thread-safety for concurrent analysis
     *   - Validation ensures data consistency across all series
     *   - sharedXAxis flag enables efficient storage when appropriate
     * 
     * IMPLEMENTATION:
     *   This section constructs an immutable, validated MultiSeriesPlotData object.
     *   It integrates with the plotting system by enforcing strict validation and
     *   creating defensive copies to ensure immutability.
     */
    public MultiSeriesPlotData(List<String> seriesNames, List<PlotData> seriesData,
                               boolean sharedXAxis, Map<String, String> plotMetadata) {
        // Validate inputs
        if (seriesNames == null || seriesData == null) {
            throw new IllegalArgumentException("Series names and data cannot be null");
        }
        
        if (seriesNames.size() != seriesData.size()) {
            throw new IllegalArgumentException(
                "Series names and data must have same size. Got: " + 
                seriesNames.size() + " names vs " + seriesData.size() + " data");
        }
        
        if (seriesNames.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one series");
        }
        
        // Validate sharedXAxis if claimed
        if (sharedXAxis && seriesData.size() > 1) {
            double[] firstXValues = seriesData.get(0).getXValues();
            
            for (int i = 1; i < seriesData.size(); i++) {
                double[] xValues = seriesData.get(i).getXValues();
                
                if (!java.util.Arrays.equals(firstXValues, xValues)) {
                    throw new IllegalArgumentException(
                        "sharedXAxis is true but series have different x-values");
                }
            }
        }
        
        // Store fields with defensive copies
        this.seriesNames = java.util.Collections.unmodifiableList(
            new java.util.ArrayList<>(seriesNames));
        this.seriesData = java.util.Collections.unmodifiableList(
            new java.util.ArrayList<>(seriesData));
        this.sharedXAxis = sharedXAxis;
        
        // Create defensive copy of metadata if provided
        if (plotMetadata != null) {
            this.plotMetadata = java.util.Collections.unmodifiableMap(
                new java.util.HashMap<>(plotMetadata));
        } else {
            this.plotMetadata = null;
        }
    }
    
    /**
     * PURPOSE: Get the list of series names.
     * 
     * OUTPUTS: Unmodifiable list of series names
     * 
     * DESIGN RATIONALE:
     *   - Returns unmodifiable view to preserve immutability
     *   - Prevents external modification while allowing iteration
     * 
     * IMPLEMENTATION:
     *   Returns the unmodifiable list created during construction.
     */
    public List<String> getSeriesNames() {
        return seriesNames;
    }
    
    /**
     * PURPOSE: Get the list of PlotData for all series.
     * 
     * OUTPUTS: Unmodifiable list of PlotData objects
     * 
     * DESIGN RATIONALE:
     *   - Returns unmodifiable view to preserve immutability
     *   - Each PlotData is itself immutable, ensuring deep immutability
     * 
     * IMPLEMENTATION:
     *   Returns the unmodifiable list created during construction.
     */
    public List<PlotData> getSeriesData() {
        return seriesData;
    }
    
    /**
     * PURPOSE: Check if all series share the same x-axis values.
     * 
     * OUTPUTS: true if all series have identical x-values
     * 
     * USAGE: Determines whether plotting can optimize by sharing x-axis
     * 
     * IMPLEMENTATION:
     *   Returns the sharedXAxis flag validated during construction.
     */
    public boolean hasSharedXAxis() {
        return sharedXAxis;
    }
    
    /**
     * PURPOSE: Get the plot-level metadata.
     * 
     * OUTPUTS: Unmodifiable map of metadata, or null if no metadata
     * 
     * USAGE: Access plot title, axis labels, and other plot-wide settings
     * 
     * IMPLEMENTATION:
     *   Returns the unmodifiable map created during construction.
     */
    public Map<String, String> getPlotMetadata() {
        return plotMetadata;
    }
    
    /**
     * PURPOSE: Get the number of series in this multi-series plot.
     * 
     * OUTPUTS: Number of series (length of seriesNames/seriesData)
     * 
     * USAGE: Useful for validation and iteration
     * 
     * IMPLEMENTATION:
     *   Returns the size of seriesNames (same as seriesData per constructor validation).
     */
    public int getSeriesCount() {
        return seriesNames.size();
    }
    
    /**
     * PURPOSE: Get PlotData for a specific series by name.
     * 
     * INPUTS: seriesName - The name of the series to retrieve
     * 
     * OUTPUTS: PlotData for the named series, or null if not found
     * 
     * USAGE: Direct access to specific series without iterating through lists
     * 
     * IMPLEMENTATION:
     *   Searches through seriesNames to find matching name, returns corresponding PlotData.
     */
    public PlotData getSeriesByName(String seriesName) {
        if (seriesName == null) {
            return null;
        }
        
        for (int i = 0; i < seriesNames.size(); i++) {
            if (seriesNames.get(i).equals(seriesName)) {
                return seriesData.get(i);
            }
        }
        
        return null;
    }
    
    /**
     * PURPOSE: Check if this plot has plot-level metadata.
     * 
     * OUTPUTS: true if metadata map is non-null and non-empty
     * 
     * USAGE: Guards against null pointer exceptions when accessing metadata
     * 
     * IMPLEMENTATION:
     *   Checks if plotMetadata is non-null and non-empty.
     */
    public boolean hasPlotMetadata() {
        return plotMetadata != null && !plotMetadata.isEmpty();
    }
}
