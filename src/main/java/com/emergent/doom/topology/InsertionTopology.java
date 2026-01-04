package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.execution.CellMetadata;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Insertion topology: Prefix left view per Levin paper.
 * Optimizes by returning only the immediate left neighbor (position-1)
 * since the swap logic currently only acts on that neighbor.
 */
public class InsertionTopology<T extends Cell<T>> implements Topology<T> {
    @Override
    public List<Integer> getNeighbors(int position, int arraySize, Algotype algotype) {
        if (algotype != null && algotype != Algotype.INSERTION) {
            throw new IllegalArgumentException("InsertionTopology only supports INSERTION algotype");
        }
        
        if (position > 0) {
            return Arrays.asList(position - 1);
        }
        return Arrays.asList();
    }

    /**
     * Get neighbors using metadata array instead of cell algotype query.
     *
     * <p>PURPOSE: Support metadata provider pattern where algotype is stored
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
     *   <li>Validate algotype is INSERTION</li>
     *   <li>Return left neighbor (if exists)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: List containing left neighbor, or empty list</p>
     *
     * <p>DEPENDENCIES: metadata[position] must be non-null</p>
     *
     * @param position the cell index
     * @param metadata the metadata array
     * @param arraySize total number of cells
     * @return list of neighbor indices
     */
    public List<Integer> getNeighborsForMetadata(int position, CellMetadata[] metadata, int arraySize) {
        // TODO PHASE THREE: Query algotype from metadata instead of cell
        // Algotype algotype = metadata[position].getAlgotype();
        // if (algotype != Algotype.INSERTION) {
        //     throw new IllegalArgumentException("InsertionTopology only supports INSERTION algotype");
        // }

        // For now, delegate to existing method
        return getNeighbors(position, arraySize, Algotype.INSERTION);
    }

    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        return IntStream.range(0, arraySize).boxed().collect(Collectors.toList());
    }
}