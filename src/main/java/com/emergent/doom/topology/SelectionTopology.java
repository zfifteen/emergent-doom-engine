package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.execution.CellMetadata;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Selection topology: Ideal target position per Levin paper.
 * Note: Actual target calculation handled in ExecutionEngine due to cell state requirements.
 * This topology returns a placeholder; decisions are made externally.
 */
public class SelectionTopology<T extends Cell<T>> implements Topology<T> {
    @Override
    public List<Integer> getNeighbors(int position, int arraySize, Algotype algotype) {
        if (algotype != null && algotype != Algotype.SELECTION) {
            throw new IllegalArgumentException("SelectionTopology only supports SELECTION algotype");
        }
        // Selection topology requires cell state (idealPos), handled in ExecutionEngine
        // Return empty list as neighbors are determined externally for this algotype
        return Arrays.asList();
    }

    /**
     * Get neighbors using metadata array for ideal position tracking.
     *
     * <p>PURPOSE: Support metadata provider pattern where ideal position is stored
     * in metadata array rather than cell object.</p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>position - cell index to find neighbors for</li>
     *   <li>metadata - metadata array (indexed by position)</li>
     *   <li>arraySize - total number of cells</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Get algotype from metadata[position]</li>
     *   <li>Validate algotype is SELECTION</li>
     *   <li>Get ideal position from metadata[position]</li>
     *   <li>Clamp to valid range [0, arraySize-1]</li>
     *   <li>Return as singleton list</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: List containing ideal target index</p>
     *
     * <p>DEPENDENCIES: metadata[position] must be non-null with valid idealPos</p>
     *
     * @param position the cell index
     * @param metadata the metadata array
     * @param arraySize total number of cells
     * @return list containing ideal target index
     */
    public List<Integer> getNeighborsForMetadata(int position, CellMetadata[] metadata, int arraySize) {
        // TODO PHASE THREE: Query algotype and ideal position from metadata
        // Algotype algotype = metadata[position].getAlgotype();
        // if (algotype != Algotype.SELECTION) {
        //     throw new IllegalArgumentException("SelectionTopology only supports SELECTION algotype");
        // }
        // int idealPos = metadata[position].getIdealPos();
        // int target = Math.min(idealPos, arraySize - 1);
        // return Arrays.asList(target);

        // For now, return empty list (handled externally in engines)
        return Arrays.asList();
    }

    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        return IntStream.range(0, arraySize).boxed().collect(Collectors.toList());
    }
}