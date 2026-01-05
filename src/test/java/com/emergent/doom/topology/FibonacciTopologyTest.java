package com.emergent.doom.topology;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FibonacciTopology")
class FibonacciTopologyTest {

    private final FibonacciTopology<GenericCell> topology = new FibonacciTopology<>();

    @Nested
    @DisplayName("generateFibonacciUpTo")
    class GenerateFibonacciUpToTests {

    @Test
    @DisplayName("returns empty list for maxValue <= 1")
    void returnsEmptyListForMaxValueLessThanOrEqualToOne() {
        assertTrue(topology.generateFibonacciUpTo(0).isEmpty());
        assertTrue(topology.generateFibonacciUpTo(1).isEmpty());
        assertTrue(topology.generateFibonacciUpTo(-1).isEmpty());
    }

        @Test
        @DisplayName("generates correct Fibonacci sequence")
        void generatesCorrectSequence() {
            List<Integer> fib = topology.generateFibonacciUpTo(10);
            assertEquals(List.of(1, 2, 3, 5, 8), fib);

            fib = topology.generateFibonacciUpTo(20);
            assertEquals(List.of(1, 2, 3, 5, 8, 13), fib);
        }
    }

    @Nested
    @DisplayName("getNeighbors")
    class GetNeighborsTests {

        @Test
        @DisplayName("throws for non-FIBONACCI algotype")
        void throwsForNonFibonacciAlgotype() {
            assertThrows(IllegalArgumentException.class,
                () -> topology.getNeighbors(0, 10, Algotype.BUBBLE));
        }

        @Test
        @DisplayName("returns Fibonacci neighbors for FIBONACCI algotype")
        void returnsFibonacciNeighbors() {
            List<Integer> neighbors = topology.getNeighbors(0, 10, Algotype.FIBONACCI);
            // For position 0: right neighbors at 1,2,3,5,8
            assertEquals(List.of(1, 2, 3, 5, 8), neighbors);

            neighbors = topology.getNeighbors(5, 10, Algotype.FIBONACCI);
            // Position 5: left at 5-1=4, 5-2=3, 5-3=2, 5-5=0; right at 5+1=6, 5+2=7, 5+3=8, 5+5=10(out)
            assertEquals(List.of(0, 2, 3, 4, 6, 7, 8), neighbors);
        }

        @Test
        @DisplayName("respects array bounds")
        void respectsArrayBounds() {
            List<Integer> neighbors = topology.getNeighbors(9, 10, Algotype.FIBONACCI);
            // Position 9: left at 9-1=8, 9-2=7, 9-3=6, 9-5=4, 9-8=1; right at 9+1=10(out)
            assertEquals(List.of(1, 4, 6, 7, 8), neighbors);
        }
    }

    @Nested
    @DisplayName("getIterationOrder")
    class GetIterationOrderTests {

        @Test
        @DisplayName("returns sequential order")
        void returnsSequentialOrder() {
            List<Integer> order = topology.getIterationOrder(5);
            assertEquals(List.of(0, 1, 2, 3, 4), order);
        }
    }
}