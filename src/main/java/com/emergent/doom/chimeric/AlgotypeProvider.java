package com.emergent.doom.chimeric;

/**
 * Interface for determining algotype assignment for cells.
 * 
 * <p>Algotype providers define strategies for distributing different
 * cell behaviors across the population.</p>
 */
@FunctionalInterface
public interface AlgotypeProvider {
    
    // PURPOSE: Determine the algotype for a cell at given position
    // INPUTS:
    //   - position (int) - the index in the cell array
    //   - arraySize (int) - total number of cells
    // PROCESS:
    //   1. Apply provider-specific logic
    //   2. Return algotype identifier string
    // OUTPUTS: String - the algotype name
    // DEPENDENCIES: None (implementation-specific)
    // NOTE: Common strategies include:
    //       - Uniform: all cells same algotype
    //       - Percentage: X% type A, rest type B
    //       - Positional: algotype based on array position
    String getAlgotype(int position, int arraySize);
}
