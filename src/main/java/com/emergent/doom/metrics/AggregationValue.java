package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;

/**
 * Computes an aggregate value over all cells.
 * 
 * <p>For domain-specific analysis, this metric can sum or aggregate
 * cell-specific values (e.g., total remainder sum for factorization).</p>
 * 
 * @param <T> the type of cell
 */
public class AggregationValue<T extends Cell<T>> implements Metric<T> {
    
    private final CellValueExtractor<T> extractor;
    
    /**
     * Functional interface for extracting numeric values from cells.
     */
    @FunctionalInterface
    public interface CellValueExtractor<T extends Cell<T>> {
        double extractValue(T cell);
    }
    
    // PURPOSE: Create an aggregation metric with custom value extractor
    // INPUTS: extractor (CellValueExtractor<T>) - function to get value from cell
    // PROCESS:
    //   1. Store extractor as instance variable
    // OUTPUTS: AggregationValue instance
    // DEPENDENCIES: None
    public AggregationValue(CellValueExtractor<T> extractor) {
        // Implementation will go here
        this.extractor = extractor;
    }
    
    /**
     * Sum the extracted values from all cells.
     *
     * <p>PURPOSE: Compute the aggregate (sum) of cell-specific values.</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Handle null/empty arrays by returning 0.0</li>
     *   <li>Initialize sum = 0.0</li>
     *   <li>For each cell, call extractor.extractValue(cell)</li>
     *   <li>Add the extracted value to sum</li>
     *   <li>Return sum</li>
     * </ol>
     * </p>
     *
     * @param cells the array to analyze
     * @return total aggregated value (sum of all extracted values)
     */
    @Override
    public double compute(T[] cells) {
        if (cells == null || cells.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (T cell : cells) {
            if (cell != null) {
                sum += extractor.extractValue(cell);
            }
        }
        return sum;
    }
    
    @Override
    public String getName() {
        return "Aggregation Value";
    }
}
