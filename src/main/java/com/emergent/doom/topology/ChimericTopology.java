package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Composite topology for chimeric populations with mixed algotypes.
 *
 * <p>Dispatches neighborhood queries based on each cell's algotype:
 * <ul>
 *   <li>BUBBLE: Adjacent left and right neighbors (bidirectional local)</li>
 *   <li>INSERTION: Left neighbor only (prefix view)</li>
 *   <li>SELECTION: Returns empty (handled by ExecutionEngine via idealPos)</li>
 * </ul></p>
 *
 * <p>From Levin et al. (2024), p.7:
 * "In cell-view sorting, each cell has its own limited view of the other cells,
 * and it follows its own algorithm policy to decide swapping."</p>
 *
 * @param <T> the type of cell
 */
public class ChimericTopology<T extends Cell<T>> implements Topology<T> {

    /**
     * Get neighbors based on the cell's algotype.
     *
     * @param position the cell's current position
     * @param arraySize the total array size
     * @param algotype the cell's algotype (determines view scope)
     * @return list of neighbor indices visible to this cell
     */
    @Override
    public List<Integer> getNeighbors(int position, int arraySize, Algotype algotype) {
        if (algotype == null) {
            // Default to Bubble behavior if algotype unknown
            return getBubbleNeighbors(position, arraySize);
        }

        switch (algotype) {
            case BUBBLE:
                return getBubbleNeighbors(position, arraySize);
            case INSERTION:
                return getInsertionNeighbors(position);
            case SELECTION:
                // Selection uses idealPos tracking in ExecutionEngine
                return Collections.emptyList();
            default:
                return getBubbleNeighbors(position, arraySize);
        }
    }

    /**
     * Bubble: bidirectional adjacent neighbors.
     */
    private List<Integer> getBubbleNeighbors(int position, int arraySize) {
        List<Integer> neighbors = new ArrayList<>(2);
        if (position > 0) {
            neighbors.add(position - 1);
        }
        if (position < arraySize - 1) {
            neighbors.add(position + 1);
        }
        return neighbors;
    }

    /**
     * Insertion: left neighbor only (prefix view).
     */
    private List<Integer> getInsertionNeighbors(int position) {
        if (position > 0) {
            return Collections.singletonList(position - 1);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        return IntStream.range(0, arraySize)
                .boxed()
                .collect(Collectors.toList());
    }
}
