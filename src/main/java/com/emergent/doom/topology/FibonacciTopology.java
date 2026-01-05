package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.execution.CellMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Fibonacci topology: Logarithmic viewing distances using Fibonacci sequence.
 * Cells see neighbors at Fibonacci distances left and right.
 */
public class FibonacciTopology<T extends Cell<T>> implements Topology<T> {

    /**
     * Generate Fibonacci numbers up to max_value.
     *
     * @param maxValue the maximum value (exclusive)
     * @return list of Fibonacci numbers in ascending order
     */
    public List<Integer> generateFibonacciUpTo(int maxValue) {
        if (maxValue < 1) {
            return new ArrayList<>();
        }
        List<Integer> fib = new ArrayList<>();
        fib.add(1);
        fib.add(2);
        while (true) {
            int nextFib = fib.get(fib.size() - 1) + fib.get(fib.size() - 2);
            if (nextFib >= maxValue) {
                break;
            }
            fib.add(nextFib);
        }
        return fib;
    }

    @Override
    public List<Integer> getNeighbors(int position, int arraySize, Algotype algotype) {
        if (algotype != null && algotype != Algotype.FIBONACCI) {
            throw new IllegalArgumentException("FibonacciTopology only supports FIBONACCI algotype");
        }
        List<Integer> fibDistances = generateFibonacciUpTo(arraySize);
        List<Integer> neighbors = new ArrayList<>();
        for (int dist : fibDistances) {
            // Left neighbor
            int left = position - dist;
            if (left >= 0) {
                neighbors.add(left);
            }
            // Right neighbor
            int right = position + dist;
            if (right < arraySize) {
                neighbors.add(right);
            }
        }
        neighbors.sort(Integer::compareTo);
        return neighbors;
    }

    /**
     * Get neighbors using metadata array instead of cell algotype query.
     *
     * <p>PURPOSE: Support metadata provider pattern where algotype is stored
     * in metadata array rather than cell object.</p>
     */
    public List<Integer> getNeighborsForMetadata(int position, CellMetadata[] metadata, int arraySize) {
        Algotype algotype = metadata[position].getAlgotype();
        if (algotype != Algotype.FIBONACCI) {
            throw new IllegalArgumentException("FibonacciTopology only supports FIBONACCI algotype, got: " + algotype);
        }
        return getNeighbors(position, arraySize, algotype);
    }

    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        return IntStream.range(0, arraySize).boxed().collect(Collectors.toList());
    }
}