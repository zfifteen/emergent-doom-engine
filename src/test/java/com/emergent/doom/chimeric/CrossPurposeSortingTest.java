package com.emergent.doom.chimeric;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.cell.SortDirection;
import com.emergent.doom.execution.ExecutionEngine;
import com.emergent.doom.execution.NoSwapConvergence;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.SwapEngine;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for cross-purpose sorting (cells with different sort directions).
 * 
 * <p>From Levin et al. (2024), p.14:
 * "we performed experiments using two mixed Algotypes, where one was made to sort in
 * *decreasing* order while the other sorted in *increasing* order."</p>
 */
public class CrossPurposeSortingTest {

    @Test
    public void testGenericCellWithDirection() {
        GenericCell ascending = new GenericCell(42, Algotype.BUBBLE, SortDirection.ASCENDING);
        GenericCell descending = new GenericCell(99, Algotype.BUBBLE, SortDirection.DESCENDING);

        assertEquals(SortDirection.ASCENDING, ascending.getSortDirection());
        assertEquals(SortDirection.DESCENDING, descending.getSortDirection());
    }

    @Test
    public void testGenericCellDefaultDirection() {
        GenericCell cell = new GenericCell(42);
        assertEquals(SortDirection.ASCENDING, cell.getSortDirection());
    }

    @Test
    public void testSortDirectionEnumMethods() {
        assertTrue(SortDirection.ASCENDING.isAscending());
        assertFalse(SortDirection.ASCENDING.isDescending());
        
        assertFalse(SortDirection.DESCENDING.isAscending());
        assertTrue(SortDirection.DESCENDING.isDescending());
    }

    @Test
    public void testGenericCellFactoryAllAscending() {
        GenericCellFactory factory = new GenericCellFactory(
            GenericCellFactory.ValueStrategy.SEQUENTIAL,
            GenericCellFactory.DirectionStrategy.ALL_ASCENDING,
            10,
            42
        );
        
        GenericCell cell0 = factory.createCell(0, "BUBBLE");
        GenericCell cell1 = factory.createCell(1, "BUBBLE");
        
        assertEquals(SortDirection.ASCENDING, cell0.getSortDirection());
        assertEquals(SortDirection.ASCENDING, cell1.getSortDirection());
    }

    @Test
    public void testGenericCellFactoryAllDescending() {
        GenericCellFactory factory = new GenericCellFactory(
            GenericCellFactory.ValueStrategy.SEQUENTIAL,
            GenericCellFactory.DirectionStrategy.ALL_DESCENDING,
            10,
            42
        );
        
        GenericCell cell0 = factory.createCell(0, "BUBBLE");
        GenericCell cell1 = factory.createCell(1, "BUBBLE");
        
        assertEquals(SortDirection.DESCENDING, cell0.getSortDirection());
        assertEquals(SortDirection.DESCENDING, cell1.getSortDirection());
    }

    @Test
    public void testGenericCellFactoryAlternating() {
        GenericCellFactory factory = new GenericCellFactory(
            GenericCellFactory.ValueStrategy.SEQUENTIAL,
            GenericCellFactory.DirectionStrategy.ALTERNATING,
            10,
            42
        );
        
        GenericCell cell0 = factory.createCell(0, "BUBBLE");
        GenericCell cell1 = factory.createCell(1, "BUBBLE");
        GenericCell cell2 = factory.createCell(2, "BUBBLE");
        
        assertEquals(SortDirection.ASCENDING, cell0.getSortDirection());
        assertEquals(SortDirection.DESCENDING, cell1.getSortDirection());
        assertEquals(SortDirection.ASCENDING, cell2.getSortDirection());
    }

    @Test
    public void testCrossPurposeSortingExecution() {
        // Create a small array with mixed directions
        GenericCell[] cells = new GenericCell[10];
        
        // First half ascending, second half descending
        for (int i = 0; i < 5; i++) {
            cells[i] = new GenericCell(i + 1, Algotype.BUBBLE, SortDirection.ASCENDING);
        }
        for (int i = 5; i < 10; i++) {
            cells[i] = new GenericCell(i + 1, Algotype.BUBBLE, SortDirection.DESCENDING);
        }
        
        // Shuffle the array
        GenericCell temp = cells[2];
        cells[2] = cells[7];
        cells[7] = temp;
        
        // Execute sorting
        FrozenCellStatus frozenStatus = new FrozenCellStatus();
        SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
        Probe<GenericCell> probe = new Probe<>();
        NoSwapConvergence<GenericCell> convergence = new NoSwapConvergence<>(3);
        
        ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
            cells, swapEngine, probe, convergence
        );
        
        // Run for a limited number of steps
        engine.runUntilConvergence(100);
        
        // Verify engine executed without errors
        assertNotNull(engine.getCells());
        assertTrue(engine.getCurrentStep() > 0);
        
        // Note: With conflicting directions, we don't expect full sorting
        // We expect equilibrium where opposing forces balance
    }

    @Test
    public void testChimericPopulationWithDirections() {
        int size = 20;
        
        // Create chimeric population with alternating directions
        GenericCellFactory factory = new GenericCellFactory(
            GenericCellFactory.ValueStrategy.SHUFFLED,
            GenericCellFactory.DirectionStrategy.ALTERNATING,
            size,
            42
        );
        
        Map<Algotype, Double> distribution = Map.of(
            Algotype.BUBBLE, 0.5,
            Algotype.SELECTION, 0.5
        );
        
        PercentageAlgotypeProvider provider = new PercentageAlgotypeProvider(
            distribution,
            size,
            42
        );
        
        ChimericPopulation<GenericCell> population = new ChimericPopulation<>(factory, provider);
        
        GenericCell[] cells = population.createPopulation(size, GenericCell.class);
        
        // Verify we have both directions
        long ascendingCount = 0;
        long descendingCount = 0;
        
        for (GenericCell cell : cells) {
            if (cell.getSortDirection() == SortDirection.ASCENDING) {
                ascendingCount++;
            } else {
                descendingCount++;
            }
        }
        
        // With alternating strategy, should be equal or close
        assertEquals(size / 2, ascendingCount);
        assertEquals(size / 2, descendingCount);
    }
}
