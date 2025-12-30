package com.emergent.doom.chimeric;

import com.emergent.doom.cell.Cell;

/**
 * Interface for creating cells of specific types.
 * 
 * <p>Used by chimeric populations to instantiate cells with different
 * algotypes or behaviors.</p>
 * 
 * @param <T> the type of cell
 */
@FunctionalInterface
public interface CellFactory<T extends Cell<T>> {
    
    // PURPOSE: Create a cell for the given position
    // INPUTS:
    //   - position (int) - the index in the cell array
    //   - algotype (String) - the algotype identifier (optional, can be null)
    // PROCESS:
    //   1. Apply factory-specific creation logic
    //   2. Return a new cell instance
    // OUTPUTS: T - the created cell
    // DEPENDENCIES: None (implementation-specific)
    T createCell(int position, String algotype);
}
