package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
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

    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        return IntStream.range(0, arraySize).boxed().collect(Collectors.toList());
    }
}