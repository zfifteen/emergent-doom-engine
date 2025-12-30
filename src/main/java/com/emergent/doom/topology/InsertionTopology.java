package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
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

    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        return IntStream.range(0, arraySize).boxed().collect(Collectors.toList());
    }
}