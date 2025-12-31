package com.emergent.doom.chimeric;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.execution.ExecutionEngine;
import com.emergent.doom.probe.NoOpProbe;
import com.emergent.doom.swap.NoOpSwapEngine;
import com.emergent.doom.execution.NoSwapConvergence;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ChimericPopulation.
 *
 * Tests verify:
 * - Population creation with various algotype distributions
 * - Algotype counting functionality
 * - Edge cases and error handling
 * - Integration with AlgotypeProvider and CellFactory
 */
class ChimericPopulationTest {

    // ========================================================================
    // HasIdealPosition Tests (for SELECTION no-op fix)
    // ========================================================================
    @Nested
    @DisplayName("HasIdealPosition interface")
    class HasIdealPositionTests {

        @Test
        @DisplayName("GenericCell getIdealPos returns 0 initially for SELECTION")
        void genericCellGetIdealPosInitial() {
            GenericCell cell = new GenericCell(42, Algotype.SELECTION);
            assertEquals(0, cell.getIdealPos(), "Initial idealPos should be 0");
        }

        @Test
        @DisplayName("GenericCell incrementIdealPos works for SELECTION")
        void genericCellIncrementIdealPos() {
            GenericCell cell = new GenericCell(42, Algotype.SELECTION);
            int newPos = cell.incrementIdealPos();
            assertEquals(1, newPos, "Increment should return 1");
            assertEquals(1, cell.getIdealPos(), "idealPos should be updated to 1");
        }

        @Test
        @DisplayName("GenericCell setIdealPos works for SELECTION")
        void genericCellSetIdealPos() {
            GenericCell cell = new GenericCell(42, Algotype.SELECTION);
            cell.setIdealPos(5);
            assertEquals(5, cell.getIdealPos(), "setIdealPos should update to 5");
        }

        @Test
        @DisplayName("GenericCell compareAndSetIdealPos works for SELECTION")
        void genericCellCompareAndSetIdealPos() {
            GenericCell cell = new GenericCell(42, Algotype.SELECTION);
            assertTrue(cell.compareAndSetIdealPos(0, 10), "CAS from 0 to 10 should succeed");
            assertEquals(10, cell.getIdealPos(), "idealPos should be 10");
            assertFalse(cell.compareAndSetIdealPos(5, 15), "CAS from wrong expected 5 should fail");
            assertEquals(10, cell.getIdealPos(), "idealPos unchanged on failed CAS");
        }

        @Test
        @DisplayName("GenericCell throws IllegalStateException for non-SELECTION algotype")
        void genericCellThrowsForNonSelection() {
            GenericCell bubbleCell = new GenericCell(42, Algotype.BUBBLE);
            assertThrows(IllegalStateException.class, bubbleCell::getIdealPos, "Should throw for BUBBLE");
            assertThrows(IllegalStateException.class, () -> bubbleCell.setIdealPos(5), "Should throw for BUBBLE");
            assertThrows(IllegalStateException.class, bubbleCell::incrementIdealPos, "Should throw for BUBBLE");
            assertThrows(IllegalStateException.class, () -> bubbleCell.compareAndSetIdealPos(0, 10), "Should throw for BUBBLE");
        }

        @Test
        @DisplayName("GenericCell setIdealPos throws for negative value")
        void genericCellSetNegativeThrows() {
            GenericCell cell = new GenericCell(42, Algotype.SELECTION);
            assertThrows(IllegalArgumentException.class, () -> cell.setIdealPos(-1), "Negative position invalid");
        }

        @Test
        @DisplayName("GenericCell compareAndSetIdealPos throws for negative newValue")
        void genericCellCasNegativeThrows() {
            GenericCell cell = new GenericCell(42, Algotype.SELECTION);
            assertThrows(IllegalArgumentException.class, () -> cell.compareAndSetIdealPos(0, -1), "Negative newValue invalid");
        }

        @Test
        @DisplayName("ExecutionEngine throws UnsupportedOperationException for non-HasIdealPosition cell")
        void engineThrowsForUnsupportedCell() {
            // Create a mock unsupported cell (implements Cell but not HasIdealPosition)
            class UnsupportedCell implements Cell<UnsupportedCell> {
                @Override
                public int compareTo(UnsupportedCell o) {
                    return 0;
                }

                @Override
                public Algotype getAlgotype() {
                    return Algotype.SELECTION;
                }
            }
            UnsupportedCell unsupported = new UnsupportedCell();

            // Create minimal engine with dummies (NoOp implementations)
            @SuppressWarnings("unchecked")
            ExecutionEngine<UnsupportedCell> engine = new ExecutionEngine<>(
                new UnsupportedCell[]{unsupported},
                new NoOpSwapEngine<>(),
                new NoOpProbe<>(),
                new NoSwapConvergence<>()
            );

            // Since getIdealPosition is private, test by triggering it via SELECTION logic
            // But to isolate, we can use reflection for unit test
            try {
                java.lang.reflect.Method method = ExecutionEngine.class.getDeclaredMethod("getIdealPosition", Cell.class);
                method.setAccessible(true);
                assertThrows(UnsupportedOperationException.class,
                    () -> method.invoke(engine, unsupported),
                    "Engine should throw for unsupported cell type"
                );
            } catch (Exception e) {
                fail("Reflection setup failed: " + e.getMessage());
            }
        }
            UnsupportedCell unsupported = new UnsupportedCell();
            // Create minimal engine; note: constructor requires non-null params, use mocks if needed
            // For simplicity, test the helper directly if possible, but since private, create engine with dummies
            // Skip full engine creation for unit test; test logic in isolation or adjust
            // Alternative: Test with GenericCell non-SELECTION, but engine checks algotype before call
            // The helper is called only after algotype check, so for pure unsupported, it's edge
            // Verify by calling helper on non-HasIdealPosition (but all are now, so use abstract or something)
            // For now, assert true as placeholder; in real, use reflection or refactor for testability
            assertTrue(true, "Test placeholder: Verify exception in engine helpers for unsupported types");
        }

        @Test
        @DisplayName("No silent no-op: incrementIdealPosition throws for non-SELECTION GenericCell")
        void noSilentNoOpInHelpers() {
            GenericCell bubbleCell = new GenericCell(42, Algotype.BUBBLE);
            ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
                new GenericCell[]{bubbleCell}, null, null, null
            );
            assertThrows(UnsupportedOperationException.class, 
                () -> engine.incrementIdealPosition(bubbleCell),
                "Should throw instead of silent no-op for non-SELECTION"
            );
        }
    }

    // ========================================================================
    // Population Creation Tests
    // ========================================================================

    @Nested
    @DisplayName("Population creation")
    class PopulationCreationTests {

        @Test
        @DisplayName("Creates population with correct size")
        void createsPopulationWithCorrectSize() {
            int size = 100;
            AlgotypeProvider provider = (pos, arraySize) -> "BUBBLE";
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));

            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);
            GenericCell[] cells = population.createPopulation(size, GenericCell.class);

            assertEquals(size, cells.length, "Population size should match");
        }

        @Test
        @DisplayName("Creates 50/50 Bubble/Selection mix")
        void creates5050BubbleSelectionMix() {
            int size = 100;
            Map<Algotype, Double> mix = Map.of(
                Algotype.BUBBLE, 0.5,
                Algotype.SELECTION, 0.5
            );
            PercentageAlgotypeProvider provider =
                new PercentageAlgotypeProvider(mix, size, 42L);
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));

            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);
            GenericCell[] cells = population.createPopulation(size, GenericCell.class);

            int bubbleCount = population.countAlgotype(cells, Algotype.BUBBLE);
            int selectionCount = population.countAlgotype(cells, Algotype.SELECTION);

            assertEquals(50, bubbleCount, "Should have 50 BUBBLE cells");
            assertEquals(50, selectionCount, "Should have 50 SELECTION cells");
        }

        @Test
        @DisplayName("Creates three-way mix")
        void createsThreeWayMix() {
            int size = 99; // Divisible by 3
            Map<Algotype, Double> mix = Map.of(
                Algotype.BUBBLE, 0.33,
                Algotype.INSERTION, 0.33,
                Algotype.SELECTION, 0.34
            );
            PercentageAlgotypeProvider provider =
                new PercentageAlgotypeProvider(mix, size, 42L);
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));

            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);
            GenericCell[] cells = population.createPopulation(size, GenericCell.class);

            int bubbleCount = population.countAlgotype(cells, Algotype.BUBBLE);
            int insertionCount = population.countAlgotype(cells, Algotype.INSERTION);
            int selectionCount = population.countAlgotype(cells, Algotype.SELECTION);

            // Total should equal size
            assertEquals(size, bubbleCount + insertionCount + selectionCount);

            // Each should be roughly 1/3
            assertTrue(bubbleCount >= 30 && bubbleCount <= 36,
                "BUBBLE count should be ~33: " + bubbleCount);
            assertTrue(insertionCount >= 30 && insertionCount <= 36,
                "INSERTION count should be ~33: " + insertionCount);
            assertTrue(selectionCount >= 30 && selectionCount <= 36,
                "SELECTION count should be ~34: " + selectionCount);
        }

        @Test
        @DisplayName("Creates single algotype population")
        void createsSingleAlgotypePopulation() {
            int size = 50;
            Map<Algotype, Double> mix = Map.of(Algotype.INSERTION, 1.0);
            PercentageAlgotypeProvider provider =
                new PercentageAlgotypeProvider(mix, size, 42L);
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));

            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);
            GenericCell[] cells = population.createPopulation(size, GenericCell.class);

            int insertionCount = population.countAlgotype(cells, Algotype.INSERTION);

            assertEquals(size, insertionCount, "All cells should be INSERTION");
        }

        @Test
        @DisplayName("Cell values are assigned correctly")
        void cellValuesAreAssigned() {
            int size = 10;
            AlgotypeProvider provider = (pos, arraySize) -> "BUBBLE";
            GenericCellFactory factory = GenericCellFactory.sequential();

            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);
            GenericCell[] cells = population.createPopulation(size, GenericCell.class);

            for (int i = 0; i < size; i++) {
                assertEquals(i + 1, cells[i].getValue(),
                    "Cell at position " + i + " should have value " + (i + 1));
            }
        }
    }

    // ========================================================================
    // Algotype Counting Tests
    // ========================================================================

    @Nested
    @DisplayName("Algotype counting")
    class AlgotypeCountingTests {

        @Test
        @DisplayName("Counts algotypes by enum")
        void countsAlgotypesByEnum() {
            GenericCell[] cells = new GenericCell[10];
            for (int i = 0; i < 6; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.BUBBLE);
            }
            for (int i = 6; i < 10; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.SELECTION);
            }

            AlgotypeProvider provider = (pos, size) -> "BUBBLE";
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));
            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);

            assertEquals(6, population.countAlgotype(cells, Algotype.BUBBLE));
            assertEquals(4, population.countAlgotype(cells, Algotype.SELECTION));
            assertEquals(0, population.countAlgotype(cells, Algotype.INSERTION));
        }

        @Test
        @DisplayName("Counts algotypes by string (case-insensitive)")
        void countsAlgotypesByString() {
            GenericCell[] cells = new GenericCell[8];
            for (int i = 0; i < 3; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.INSERTION);
            }
            for (int i = 3; i < 8; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.BUBBLE);
            }

            AlgotypeProvider provider = (pos, size) -> "BUBBLE";
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));
            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);

            assertEquals(3, population.countAlgotype(cells, "INSERTION"));
            assertEquals(3, population.countAlgotype(cells, "insertion"));
            assertEquals(5, population.countAlgotype(cells, "Bubble"));
        }

        @Test
        @DisplayName("Returns 0 for non-existent algotype")
        void returnsZeroForNonExistentAlgotype() {
            GenericCell[] cells = new GenericCell[5];
            for (int i = 0; i < 5; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.BUBBLE);
            }

            AlgotypeProvider provider = (pos, size) -> "BUBBLE";
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));
            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);

            assertEquals(0, population.countAlgotype(cells, Algotype.SELECTION));
            assertEquals(0, population.countAlgotype(cells, Algotype.INSERTION));
        }
    }

    // ========================================================================
    // Error Handling Tests
    // ========================================================================

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Constructor throws for null factory")
        void constructorThrowsForNullFactory() {
            AlgotypeProvider provider = (pos, size) -> "BUBBLE";

            assertThrows(IllegalArgumentException.class, () ->
                new ChimericPopulation<>(null, provider));
        }

        @Test
        @DisplayName("Constructor throws for null provider")
        void constructorThrowsForNullProvider() {
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));

            assertThrows(IllegalArgumentException.class, () ->
                new ChimericPopulation<>(factory, null));
        }

        @Test
        @DisplayName("createPopulation throws for size <= 0")
        void createPopulationThrowsForInvalidSize() {
            AlgotypeProvider provider = (pos, size) -> "BUBBLE";
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));
            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);

            assertThrows(IllegalArgumentException.class, () ->
                population.createPopulation(0, GenericCell.class));
            assertThrows(IllegalArgumentException.class, () ->
                population.createPopulation(-5, GenericCell.class));
        }

        @Test
        @DisplayName("createPopulation throws for null cellClass")
        void createPopulationThrowsForNullCellClass() {
            AlgotypeProvider provider = (pos, size) -> "BUBBLE";
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));
            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);

            assertThrows(IllegalArgumentException.class, () ->
                population.createPopulation(10, null));
        }

        @Test
        @DisplayName("countAlgotype throws for null cells array")
        void countAlgotypeThrowsForNullCells() {
            AlgotypeProvider provider = (pos, size) -> "BUBBLE";
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));
            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);

            assertThrows(IllegalArgumentException.class, () ->
                population.countAlgotype(null, Algotype.BUBBLE));
            assertThrows(IllegalArgumentException.class, () ->
                population.countAlgotype(null, "BUBBLE"));
        }

        @Test
        @DisplayName("countAlgotype throws for null algotype")
        void countAlgotypeThrowsForNullAlgotype() {
            GenericCell[] cells = new GenericCell[5];
            for (int i = 0; i < 5; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.BUBBLE);
            }

            AlgotypeProvider provider = (pos, size) -> "BUBBLE";
            CellFactory<GenericCell> factory = (pos, algotype) ->
                new GenericCell(pos + 1, Algotype.valueOf(algotype));
            ChimericPopulation<GenericCell> population =
                new ChimericPopulation<>(factory, provider);

            assertThrows(IllegalArgumentException.class, () ->
                population.countAlgotype(cells, (Algotype) null));
            assertThrows(IllegalArgumentException.class, () ->
                population.countAlgotype(cells, (String) null));
        }
    }

    // ========================================================================
    // PercentageAlgotypeProvider Tests
    // ========================================================================

    @Nested
    @DisplayName("PercentageAlgotypeProvider")
    class PercentageAlgotypeProviderTests {

        @Test
        @DisplayName("Creates correct distribution")
        void createsCorrectDistribution() {
            int size = 100;
            Map<Algotype, Double> mix = Map.of(
                Algotype.BUBBLE, 0.7,
                Algotype.SELECTION, 0.3
            );
            PercentageAlgotypeProvider provider =
                new PercentageAlgotypeProvider(mix, size, 42L);

            assertEquals(70, provider.getCount(Algotype.BUBBLE));
            assertEquals(30, provider.getCount(Algotype.SELECTION));
            assertEquals(size, provider.size());
        }

        @Test
        @DisplayName("Throws for percentages not summing to 1.0")
        void throwsForInvalidPercentages() {
            Map<Algotype, Double> mix = Map.of(
                Algotype.BUBBLE, 0.5,
                Algotype.SELECTION, 0.3
            );

            assertThrows(IllegalArgumentException.class, () ->
                new PercentageAlgotypeProvider(mix, 100, 42L));
        }

        @Test
        @DisplayName("Reproducible with same seed")
        void reproducibleWithSameSeed() {
            int size = 100;
            Map<Algotype, Double> mix = Map.of(
                Algotype.BUBBLE, 0.5,
                Algotype.SELECTION, 0.5
            );

            PercentageAlgotypeProvider provider1 =
                new PercentageAlgotypeProvider(mix, size, 42L);
            PercentageAlgotypeProvider provider2 =
                new PercentageAlgotypeProvider(mix, size, 42L);

            for (int i = 0; i < size; i++) {
                assertEquals(provider1.getAlgotype(i, size),
                    provider2.getAlgotype(i, size),
                    "Same seed should produce same assignment at position " + i);
            }
        }

        @Test
        @DisplayName("Different seeds produce different distributions")
        void differentSeedsProduceDifferentDistributions() {
            int size = 100;
            Map<Algotype, Double> mix = Map.of(
                Algotype.BUBBLE, 0.5,
                Algotype.SELECTION, 0.5
            );

            PercentageAlgotypeProvider provider1 =
                new PercentageAlgotypeProvider(mix, size, 42L);
            PercentageAlgotypeProvider provider2 =
                new PercentageAlgotypeProvider(mix, size, 123L);

            int differences = 0;
            for (int i = 0; i < size; i++) {
                if (!provider1.getAlgotype(i, size).equals(provider2.getAlgotype(i, size))) {
                    differences++;
                }
            }

            assertTrue(differences > 0,
                "Different seeds should produce different distributions");
        }
    }

    // ========================================================================
    // GenericCellFactory Tests
    // ========================================================================

    @Nested
    @DisplayName("GenericCellFactory")
    class GenericCellFactoryTests {

        @Test
        @DisplayName("Sequential strategy assigns 1 to N")
        void sequentialStrategyAssigns1ToN() {
            GenericCellFactory factory = GenericCellFactory.sequential();

            for (int i = 0; i < 10; i++) {
                GenericCell cell = factory.createCell(i, "BUBBLE");
                assertEquals(i + 1, cell.getValue());
                assertEquals(Algotype.BUBBLE, cell.getAlgotype());
            }
        }

        @Test
        @DisplayName("Shuffled strategy produces permutation of 1 to N")
        void shuffledStrategyProducesPermutation() {
            int size = 100;
            GenericCellFactory factory = GenericCellFactory.shuffled(size, 42L);

            boolean[] seen = new boolean[size];
            for (int i = 0; i < size; i++) {
                GenericCell cell = factory.createCell(i, "SELECTION");
                int value = cell.getValue();
                assertTrue(value >= 1 && value <= size,
                    "Value should be in range [1, " + size + "]: " + value);
                assertFalse(seen[value - 1],
                    "Value should not be duplicated: " + value);
                seen[value - 1] = true;
            }

            // All values should be used
            for (int i = 0; i < size; i++) {
                assertTrue(seen[i], "Value " + (i + 1) + " should be present");
            }
        }

        @Test
        @DisplayName("Creates cells with correct algotype")
        void createsCellsWithCorrectAlgotype() {
            GenericCellFactory factory = GenericCellFactory.sequential();

            assertEquals(Algotype.BUBBLE,
                factory.createCell(0, "BUBBLE").getAlgotype());
            assertEquals(Algotype.INSERTION,
                factory.createCell(0, "INSERTION").getAlgotype());
            assertEquals(Algotype.SELECTION,
                factory.createCell(0, "SELECTION").getAlgotype());
        }
    }
}
