package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
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

    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        return IntStream.range(0, arraySize).boxed().collect(Collectors.toList());
    }
}