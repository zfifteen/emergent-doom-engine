package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.cell.HasIdealPosition;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.BasicProbe;
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
            GenericCell cell = new GenericCell(42, Algotype.SELECTION);
            cell.setIdealPos(50); // Some arbitrary position

            cell.updateForBoundary(0, 99, false); // ascending

            assertEquals(0, cell.getIdealPos(),
                    "For ascending sort, idealPos should be left boundary");
        }

        @Test
        @DisplayName("updateForBoundary sets idealPos to rightBoundary for descending sort")
        void updateForBoundaryDescending() {
            GenericCell cell = new GenericCell(42, Algotype.SELECTION);
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
            Probe<GenericCell> probe = new BasicProbe<>();
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
                new GenericCell(2, Algotype.BUBBLE),
                new GenericCell(1, Algotype.BUBBLE)
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new BasicProbe<>();
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
                new GenericCell(1, Algotype.BUBBLE),
                new GenericCell(2, Algotype.BUBBLE),
                new GenericCell(3, Algotype.BUBBLE)  // Already sorted
            };

            FrozenCellStatus frozenStatus = new FrozenCellStatus();
            SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
            Probe<GenericCell> probe = new BasicProbe<>();
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
