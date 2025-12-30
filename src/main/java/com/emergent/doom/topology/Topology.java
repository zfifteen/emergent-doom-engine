package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import java.util.List;

/**
 * Defines neighborhood relationships and iteration strategies for cells.
 * 
 * <p>The topology determines:
 * <ul>
 *   <li>Which cells are neighbors (for swap candidate selection)</li>
 *   <li>The order in which cells are processed each step</li>
 *   <li>The structure of the search space</li>
 * </ul>
 * </p>
 * 
 * <p>Different topologies enable different emergent behaviors and search
 * characteristics.</p>
 * 
 * @param <T> the type of cell
 */
public interface Topology<T extends Cell<T>> {
    
    // PURPOSE: Get the indices of cells neighboring the given position
    // INPUTS:
    //   - position (int) - the index of the cell
    //   - arraySize (int) - the total number of cells in the array
    //   - algotype (Algotype) - the cell's sorting algorithm policy
    // PROCESS:
    //   1. Apply topology-specific rules to determine neighbors
    //   2. Dispatch based on algotype per Levin paper behaviors
    //   3. Return list of valid neighbor indices
    //   4. Indices must be in range [0, arraySize)
    // OUTPUTS: List<Integer> - indices of neighboring cells
    // DEPENDENCIES: Algotype enum
    // NOTE: The neighborhood definition is critical to emergent behavior.
    //       Algotype determines view scope: adj (Bubble), prefix (Insertion), target (Selection).
    List<Integer> getNeighbors(int position, int arraySize, Algotype algotype);
    
    // PURPOSE: Get the iteration order for processing cells in a step
    // INPUTS: arraySize (int) - the total number of cells
    // PROCESS:
    //   1. Define the order in which cells attempt swaps
    //   2. Can be sequential, shuffled, or custom
    //   3. All indices [0, arraySize) should appear exactly once
    // OUTPUTS: List<Integer> - ordered list of cell indices to process
    // DEPENDENCIES: None (implementation-specific)
    // NOTE: Iteration order can significantly affect convergence speed.
    //       Forward sweep vs reverse sweep vs random can yield different paths.
    List<Integer> getIterationOrder(int arraySize);
    
    // PURPOSE: Optional hook to reset any stateful topology elements
    // INPUTS: None
    // PROCESS:
    //   1. Reset any internal state (e.g., random seeds, caches)
    //   2. Default implementation does nothing
    // OUTPUTS: void
    // DEPENDENCIES: None
    default void reset() {
        // Default: no-op for stateless topologies
    }
}
