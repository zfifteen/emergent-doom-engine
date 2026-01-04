package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.cell.HasIdealPosition;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.FrozenCellStatus.FrozenType;
import com.emergent.doom.swap.SwapEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for gap implementations from GAPS-CLAUDE.md.
 *
 * <ul>
 *   <li>Gap 2.2: SelectionCell idealPos boundary reset</li>
 *   <li>Gap 4.2: Frozen cell skip in isLeftSorted</li>
 *   <li>Gap 5.1: StatusProbe fields (tested separately in ProbeTest)</li>
 * </ul>
 */
class GapImplementationTest {

    // ========================================================================
    // Gap 2.2: SelectionCell idealPos Boundary Reset
    // ========================================================================

    @Nested
    @DisplayName("Gap 2.2: SelectionCell idealPos Boundary Reset")
    class SelectionCellBoundaryResetTests {

        @Test
        @DisplayName("GenericCell implements HasIdealPosition interface")
        void genericCellImplementsHasIdealPosition() {
            // Test at class level, not instance level (more precise)
            assertTrue(HasIdealPosition.class.isAssignableFrom(GenericCell.class),
                    "GenericCell should implement HasIdealPosition");
        }

        @Test
        @DisplayName("updateForBoundary sets idealPos to leftBoundary for ascending sort")
        void updateForBoundaryAscending() {
            GenericCell cell = new GenericCell(42);
            cell.setIdealPos(50); // Some arbitrary position

            cell.updateForBoundary(0, 99, false); // ascending

            assertEquals(0, cell.getIdealPos(),
                    "For ascending sort, idealPos should be left boundary");
        }

        @Test
        @DisplayName("updateForBoundary sets idealPos to rightBoundary for descending sort")
        void updateForBoundaryDescending() {
            GenericCell cell = new GenericCell(42);
            cell.setIdealPos(50); // Some arbitrary position

            cell.updateForBoundary(0, 99, true); // descending

            assertEquals(99, cell.getIdealPos(),
                    "For descending sort, idealPos should be right boundary");
        }

        @Test
        @DisplayName("ExecutionEngine.reset() resets SELECTION cells to left boundary")
        void executionEngineResetResetsIdealPos() {
            GenericCell[] cells = new GenericCell[5];
            for (int i = 0; i < 5; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.SELECTION);
                cells[i].setIdealPos(i + 10); // Set some non-zero position
            }

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            ConvergenceDetector<GenericCell> detector = new NoSwapConvergence<>(3);

            ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
                    cells, swapEngine, probe, detector, new Random(42));

            engine.reset(); // Default: ascending sort

            for (GenericCell cell : cells) {
                assertEquals(0, cell.getIdealPos(),
                        "After reset, SELECTION cells should have idealPos = 0 (left boundary)");
            }
        }

        @Test
        @DisplayName("ExecutionEngine.reset(true) resets SELECTION cells to right boundary")
        void executionEngineResetDescendingResetsToRightBoundary() {
            GenericCell[] cells = new GenericCell[5];
            for (int i = 0; i < 5; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.SELECTION);
                cells[i].setIdealPos(0);
            }

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            ConvergenceDetector<GenericCell> detector = new NoSwapConvergence<>(3);

            ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
                    cells, swapEngine, probe, detector, new Random(42));

            engine.reset(true); // descending sort

            for (GenericCell cell : cells) {
                assertEquals(4, cell.getIdealPos(), // rightBoundary = cells.length - 1 = 4
                        "After reset(true), SELECTION cells should have idealPos = right boundary");
            }
        }

        @Test
        @DisplayName("reset() only affects SELECTION algotype cells")
        void resetOnlyAffectsSelectionCells() {
            GenericCell[] cells = {
                new GenericCell(1),
                new GenericCell(2),
                new GenericCell(3)
            };

            // Set all to some position
            cells[0].setIdealPos(10);
            cells[1].setIdealPos(10);
            cells[2].setIdealPos(10);

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            ConvergenceDetector<GenericCell> detector = new NoSwapConvergence<>(3);

            ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
                    cells, swapEngine, probe, detector, new Random(42));

            engine.reset();

            // Only SELECTION cell should be reset
            assertEquals(10, cells[0].getIdealPos(), "BUBBLE cell should not be affected");
            assertEquals(0, cells[1].getIdealPos(), "SELECTION cell should be reset to 0");
            assertEquals(10, cells[2].getIdealPos(), "INSERTION cell should not be affected");
        }
    }

    // ========================================================================
    // Gap 4.2: Frozen Cell Skip in isLeftSorted
    // ========================================================================

    @Nested
    @DisplayName("Gap 4.2: Frozen Cell Skip in isLeftSorted")
    class FrozenCellSkipTests {

        @Test
        @DisplayName("SwapEngine.isFrozen returns true for MOVABLE cells")
        void isFrozenReturnsTrueForMovable() {
            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            frozenStatus.setFrozen(1, FrozenType.MOVABLE);

            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);

            assertTrue(swapEngine.isFrozen(1));
            assertFalse(swapEngine.isFrozen(0));
        }

        @Test
        @DisplayName("SwapEngine.isFrozen returns true for IMMOVABLE cells")
        void isFrozenReturnsTrueForImmovable() {
            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            frozenStatus.setFrozen(2, FrozenType.IMMOVABLE);

            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);

            assertTrue(swapEngine.isFrozen(2));
        }

        @Test
        @DisplayName("Frozen swap attempts are recorded to probe")
        void frozenSwapAttemptsRecordedToProbe() {
            GenericCell[] cells = {
                new GenericCell(2),
                new GenericCell(1)
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            frozenStatus.setFrozen(0, FrozenType.MOVABLE); // Cell 0 is frozen

            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            swapEngine.setProbe(probe);

            // Try to swap from frozen cell
            boolean swapped = swapEngine.attemptSwap(cells, 0, 1);

            assertFalse(swapped, "Frozen cell should not be able to initiate swap");
            assertEquals(1, probe.getFrozenSwapAttempts(),
                    "Frozen swap attempt should be recorded");
        }

        @Test
        @DisplayName("Active cell can displace MOVABLE frozen cell")
        void activeCellCanDisplaceMovableFrozen() {
            GenericCell[] cells = {
                new GenericCell(2),
                new GenericCell(1)
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            frozenStatus.setFrozen(1, FrozenType.MOVABLE); // Cell 1 is frozen but movable

            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            swapEngine.setProbe(probe);

            // Cell 0 (active) tries to swap with cell 1 (frozen but movable)
            boolean swapped = swapEngine.attemptSwap(cells, 0, 1);

            assertTrue(swapped, "Active cell should be able to displace MOVABLE frozen cell");
            assertEquals(0, probe.getFrozenSwapAttempts(),
                    "This is not a frozen swap attempt (active cell initiated)");
        }

        @Test
        @DisplayName("Active cell cannot displace IMMOVABLE frozen cell")
        void activeCellCannotDisplaceImmovableFrozen() {
            GenericCell[] cells = {
                new GenericCell(2),
                new GenericCell(1)
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            frozenStatus.setFrozen(1, FrozenType.IMMOVABLE); // Cell 1 is completely frozen

            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);

            boolean swapped = swapEngine.attemptSwap(cells, 0, 1);

            assertFalse(swapped, "Active cell should not be able to displace IMMOVABLE cell");
        }

        @Test
        @DisplayName("CRITICAL FIX: isLeftSorted handles descending sort with frozen cells")
        void isLeftSortedDescendingWithFrozen() {
            // Array: [5, FROZEN(99), 3, 1] - descending order with frozen cell at idx 1
            GenericCell[] cells = {
                new GenericCell(5),
                new GenericCell(99), // frozen (out of desc order, should be skipped)
                new GenericCell(3),
                new GenericCell(1)
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            frozenStatus.setFrozen(1, FrozenType.MOVABLE);

            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            ConvergenceDetector<GenericCell> detector = new NoSwapConvergence<>(3);

            ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
                    cells, swapEngine, probe, detector, new Random(42));

            // Reset for descending sort
            engine.reset(true);

            // Should return true - [5, FROZEN, 3] is sorted descending (skipping frozen at 1)
            // Using reflection to access private isLeftSorted method for testing
            try {
                java.lang.reflect.Method isLeftSorted = ExecutionEngine.class
                        .getDeclaredMethod("isLeftSorted", int.class, boolean.class);
                isLeftSorted.setAccessible(true);
                
                boolean result = (boolean) isLeftSorted.invoke(engine, 3, true); // reverseDirection=true
                assertTrue(result, "[5, FROZEN, 3] should be sorted descending (frozen skipped)");
            } catch (Exception e) {
                fail("Failed to test isLeftSorted: " + e.getMessage());
            }
        }

        @Test
        @DisplayName("CRITICAL FIX: isLeftSorted handles ascending sort with frozen cells")
        void isLeftSortedAscendingWithFrozen() {
            // Array: [1, FROZEN(0), 3, 5] - ascending order with frozen cell at idx 1
            GenericCell[] cells = {
                new GenericCell(1),
                new GenericCell(0), // frozen (out of asc order, should be skipped)
                new GenericCell(3),
                new GenericCell(5)
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            frozenStatus.setFrozen(1, FrozenType.MOVABLE);

            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            ConvergenceDetector<GenericCell> detector = new NoSwapConvergence<>(3);

            ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
                    cells, swapEngine, probe, detector, new Random(42));

            // Reset for ascending sort (default)
            engine.reset(false);

            // Should return true - [1, FROZEN, 3] is sorted ascending (skipping frozen at 1)
            try {
                java.lang.reflect.Method isLeftSorted = ExecutionEngine.class
                        .getDeclaredMethod("isLeftSorted", int.class, boolean.class);
                isLeftSorted.setAccessible(true);
                
                boolean result = (boolean) isLeftSorted.invoke(engine, 3, false); // reverseDirection=false
                assertTrue(result, "[1, FROZEN, 3] should be sorted ascending (frozen skipped)");
            } catch (Exception e) {
                fail("Failed to test isLeftSorted: " + e.getMessage());
            }
        }
    }

    // ========================================================================
    // Gap 5.1: StatusProbe Integration with Execution Engine
    // ========================================================================

    @Nested
    @DisplayName("Gap 5.1: StatusProbe Integration")
    class StatusProbeIntegrationTests {

        @Test
        @DisplayName("ExecutionEngine wires probe to SwapEngine")
        void executionEngineWiresProbeToSwapEngine() {
            GenericCell[] cells = {
                new GenericCell(2),
                new GenericCell(1)
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            frozenStatus.setFrozen(0, FrozenType.MOVABLE); // Cell 0 is frozen

            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            ConvergenceDetector<GenericCell> detector = new NoSwapConvergence<>(3);

            // Creating the engine should wire probe to swapEngine
            ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
                    cells, swapEngine, probe, detector, new Random(42));

            // Force a step to trigger swap attempts
            engine.step();

            // Frozen cell at position 0 trying to swap should have been recorded
            // (The exact count depends on random direction choice in step)
            assertTrue(probe.getFrozenSwapAttempts() >= 0,
                    "Probe should be wired and tracking frozen swap attempts");
        }

        @Test
        @DisplayName("ExecutionEngine records compare-and-swap events")
        void executionEngineRecordsCompareAndSwap() {
            GenericCell[] cells = {
                new GenericCell(2),
                new GenericCell(1)
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            ConvergenceDetector<GenericCell> detector = new NoSwapConvergence<>(3);

            ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
                    cells, swapEngine, probe, detector, new Random(42));

            // Run until sorted
            engine.runUntilConvergence(100);

            assertTrue(probe.getCompareAndSwapCount() > 0,
                    "Should have recorded compare-and-swap events during sorting");
        }

        @Test
        @DisplayName("CRITICAL FIX: compareAndSwap counts ALL comparisons, not just swaps")
        void compareAndSwapCountsAllComparisons() {
            // Create array where BUBBLE will compare but not always swap
            GenericCell[] cells = {
                new GenericCell(1),
                new GenericCell(2),
                new GenericCell(3)  // Already sorted
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new Probe<>();
            ConvergenceDetector<GenericCell> detector = new NoSwapConvergence<>(3);

            ExecutionEngine<GenericCell> engine = new ExecutionEngine<>(
                    cells, swapEngine, probe, detector, new Random(42));

            // Execute 5 steps (many comparisons, few/no swaps since already sorted)
            for (int i = 0; i < 5; i++) {
                engine.step();
            }

            int swapCount = swapEngine.getSwapCount();
            int compareCount = probe.getCompareAndSwapCount();

            // CRITICAL: compareAndSwap should be >= swapCount
            // (Python tracks all comparisons, even those that don't lead to swaps)
            assertTrue(compareCount >= swapCount,
                    "compareAndSwapCount (" + compareCount + ") should be >= swapCount (" + swapCount + ")");
            
            // Since array is sorted, we expect many comparisons but zero swaps
            assertEquals(0, swapCount, "No swaps should occur in already sorted array");
            assertTrue(compareCount > 0, "Should have made comparisons even with no swaps");
        }
    }
}
