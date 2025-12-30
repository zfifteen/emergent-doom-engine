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
    
    // PURPOSE: Sum the extracted values from all cells
    // INPUTS: cells (T[]) - the array to analyze
    // PROCESS:
    //   1. Initialize sum = 0.0
    //   2. For each cell, call extractor.extractValue(cell)
    //   3. Add the extracted value to sum
    //   4. Return sum
    // OUTPUTS: double - total aggregated value
    // DEPENDENCIES: CellValueExtractor.extractValue() [FUNCTIONAL INTERFACE]
    @Override
    public double compute(T[] cells) {
        // Implementation will go here
        return 0.0;
    }
    
    @Override
    public String getName() {
        return "Aggregation Value";
    }
}
