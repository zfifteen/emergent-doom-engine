package com.emergent.doom.examples;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.topology.Topology;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Simple linear neighborhood topology.
 * 
 * <p>Cells can swap with their immediate neighbors in a 1D array.
 * This creates local interaction dynamics where improvements propagate
 * gradually through the population.</p>
 * 
 * @param <T> the type of cell
 */
public class LinearNeighborhood<T extends Cell<T>> implements Topology<T> {
    
    private final int neighborhoodRadius;
    
    /**
     * IMPLEMENTED: Create a linear neighborhood topology
     * @param neighborhoodRadius how many positions away neighbors can be (radius=1 means immediate neighbors)
     */
    public LinearNeighborhood(int neighborhoodRadius) {
        if (neighborhoodRadius < 1) {
            throw new IllegalArgumentException("Neighborhood radius must be >= 1");
        }
        this.neighborhoodRadius = neighborhoodRadius;
    }
    
    /**
     * IMPLEMENTED: Get neighboring indices within the radius (ignores algotype for baseline)
     */
    @Override
    public List<Integer> getNeighbors(int position, int arraySize, Algotype algotype) {
        // Ignore algotype - return adjacent neighbors as baseline
        int minNeighbor = Math.max(0, position - neighborhoodRadius);
        int maxNeighbor = Math.min(arraySize - 1, position + neighborhoodRadius);

        return IntStream.rangeClosed(minNeighbor, maxNeighbor)
                .filter(i -> i != position)  // Exclude the position itself
                .boxed()
                .collect(Collectors.toList());
    }
    
    /**
     * IMPLEMENTED: Get sequential iteration order (left to right)
     */
    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        return IntStream.range(0, arraySize)
                .boxed()
                .collect(Collectors.toList());
    }
}
