package com.emergent.doom.metrics;

import com.emergent.doom.cell.HasValue;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.StepSnapshot;

import java.util.List;

/**
 * Measures the degree to which better cells appear later in the array.
 * 
 * <p>This metric captures "delayed gratification" where initially worse
 * solutions must be accepted before finding better ones. It computes the
 * position-weighted quality difference.</p>
 * 
 * @param <T> the type of cell
 */
public class DelayedGratificationIndex<T extends Cell<T>> implements Metric<T> {
    
    /**
     * Computes a position-weighted quality index as a proxy for delayed gratification.
     *
     * <p>For single-array analysis, weights deviation from average by position.
     * Higher values suggest better cells later (delayed discovery).</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Compute average cell value</li>
     *   <li>For each position i, quality = |cell.value - avg|</li>
     *   <li>Weighted sum: sum((i+1) * quality)</li>
     *   <li>Normalize: sum / (n*(n+1)/2)</li>
     * </ol>
     * </p>
     *
     * @param cells the array to analyze
     * @return normalized index (higher = more delayed)
     */
    @Override
    public double compute(T[] cells) {
        if (cells == null || cells.length == 0) {
            return 0.0;
        }
        double totalQuality = 0.0;
        int n = cells.length;
        double avgValue = computeAverageValue(cells);
        for (int i = 0; i < n; i++) {
            T cell = cells[i];
            double quality = Math.abs(cell.getValue() - avgValue); // Deviation as proxy for "quality difference"
            totalQuality += (i + 1) * quality; // Weight by position (1-based for later emphasis)
        }
        return totalQuality / (n * (n + 1) / 2.0); // Normalize by avg position weight
    }

    @Override
    public double compute(StepSnapshot<T> snapshot) {
        List<Integer> values = snapshot.getValues();
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double totalQuality = 0.0;
        int n = values.size();
        
        double sum = 0.0;
        for (Integer val : values) {
            sum += val;
        }
        double avgValue = n > 0 ? sum / n : 0.0;

        for (int i = 0; i < n; i++) {
            double quality = Math.abs(values.get(i) - avgValue);
            totalQuality += (i + 1) * quality;
        }
        return totalQuality / (n * (n + 1) / 2.0);
    }

    private double computeAverageValue(T[] cells) {
        double sum = 0.0;
        int count = 0;
        for (T cell : cells) {
            if (cell != null) {
                sum += cell.getValue();
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }
    
    @Override
    public String getName() {
        return "Delayed Gratification Index";
    }
    
    @Override
    public boolean isLowerBetter() {
        return false; // Higher DGI can indicate more complex search paths
    }
}
