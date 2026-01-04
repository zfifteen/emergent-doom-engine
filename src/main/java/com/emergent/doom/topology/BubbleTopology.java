package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.execution.CellMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Bubble topology: Local adjacent bidirectional view per Levin paper.
 * Cells see left and right neighbors only.
 */
public class BubbleTopology<T extends Cell<T>> implements Topology<T> {
    @Override
    public List<Integer> getNeighbors(int position, int arraySize, Algotype algotype) {
        if (algotype != null && algotype != Algotype.BUBBLE) {
            throw new IllegalArgumentException("BubbleTopology only supports BUBBLE algotype");
        }
        List<Integer> neighbors = new ArrayList<>();
        if (position > 0) neighbors.add(position - 1);  // left
        if (position < arraySize - 1) neighbors.add(position + 1);  // right
        return neighbors;
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
     *   <li>Validate algotype is BUBBLE</li>
     *   <li>Return left and right neighbors (if exist)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: List of neighbor indices</p>
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
        // if (algotype != Algotype.BUBBLE) {
        //     throw new IllegalArgumentException("BubbleTopology only supports BUBBLE algotype");
        // }

        // For now, delegate to existing method
        return getNeighbors(position, arraySize, Algotype.BUBBLE);
    }

    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        return IntStream.range(0, arraySize).boxed().collect(Collectors.toList());
    }
}