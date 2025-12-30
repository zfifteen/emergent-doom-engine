package com.emergent.doom.swap;

import com.emergent.doom.swap.FrozenCellStatus.FrozenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for SwapEngine.
 *
 * Tests are organized according to SwapEngineTestSpec.md and cover:
 * - All frozen state combinations (T01-T12)
 * - wouldSwap parity tests (T13-T14)
 * - Swap count tracking (T15-T16)
 */
class SwapEngineTest {

    private FrozenCellStatus frozenStatus;
    private SwapEngine<IntCell> swapEngine;

    @BeforeEach
    void setUp() {
        frozenStatus = new FrozenCellStatus();
        swapEngine = new SwapEngine<>(frozenStatus);
    }

    private IntCell[] createCells(int... values) {
        IntCell[] cells = new IntCell[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = new IntCell(values[i]);
        }
        return cells;
    }

    // ========================================================================
    // T01-T06: Cases where frozen check PASSES (proceeds to comparison)
    // ========================================================================

    @Nested
    @DisplayName("NONE/NONE frozen state")
    class NoneNoneTests {

        @Test
        @DisplayName("T01: NONE/NONE - Swaps regardless of value")
        void t01_noneNone_swaps() {
            IntCell[] cells = createCells(10, 5);
            // Both cells default to NONE

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertTrue(result, "Should return true when not frozen");
            assertEquals(5, cells[0].getValue(), "cells[0] should now be 5");
            assertEquals(10, cells[1].getValue(), "cells[1] should now be 10");
        }

        @Test
        @DisplayName("T02: NONE/NONE - Swaps even if i < j (dumb executor)")
        void t02_noneNone_lessThan_swaps() {
            IntCell[] cells = createCells(5, 10);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertTrue(result, "Should return true (dumb executor)");
            assertEquals(10, cells[0].getValue(), "cells[0] should now be 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should now be 5");
        }
    }

    @Nested
    @DisplayName("MOVABLE/NONE frozen state")
    class MovableNoneTests {

        @Test
        @DisplayName("T04: MOVABLE/NONE - Swaps regardless of value")
        void t04_movableNone_swaps() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(0, FrozenType.MOVABLE);
            // Cell 1 defaults to NONE

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertTrue(result, "Should return true when not frozen");
            assertEquals(5, cells[0].getValue(), "cells[0] should now be 5");
            assertEquals(10, cells[1].getValue(), "cells[1] should now be 10");
        }

        @Test
        @DisplayName("T05: MOVABLE/NONE - Swaps even if i < j")
        void t05_movableNone_lessThan_swaps() {
            IntCell[] cells = createCells(5, 10);
            frozenStatus.setFrozen(0, FrozenType.MOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertTrue(result, "Should return true (dumb executor)");
            assertEquals(10, cells[0].getValue(), "cells[0] should now be 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should now be 5");
        }
    }

    // ========================================================================
    // T07-T12: Cases where frozen check FAILS (blocked before comparison)
    // ========================================================================

    @Nested
    @DisplayName("Frozen state blocks swap")
    class FrozenBlocksTests {

        @Test
        @DisplayName("T07: NONE/MOVABLE with i > j - Blocked by frozen")
        void t07_noneMovable_blocked() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(1, FrozenType.MOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertFalse(result, "Should return false - j cannot be displaced");
            assertEquals(10, cells[0].getValue(), "cells[0] should remain 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should remain 5");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0");
        }

        @Test
        @DisplayName("T08: NONE/MOVABLE with i < j - Blocked by frozen")
        void t08_noneMovable_lessThan_blocked() {
            IntCell[] cells = createCells(5, 10);
            frozenStatus.setFrozen(1, FrozenType.MOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertFalse(result, "Should return false - j cannot be displaced");
            assertEquals(5, cells[0].getValue(), "cells[0] should remain 5");
            assertEquals(10, cells[1].getValue(), "cells[1] should remain 10");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0");
        }

        @Test
        @DisplayName("T09: MOVABLE/MOVABLE - Blocked by frozen")
        void t09_movableMovable_blocked() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(0, FrozenType.MOVABLE);
            frozenStatus.setFrozen(1, FrozenType.MOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertFalse(result, "Should return false - j cannot be displaced");
            assertEquals(10, cells[0].getValue(), "cells[0] should remain 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should remain 5");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0");
        }

        @Test
        @DisplayName("T10: IMMOVABLE/NONE - Blocked by frozen")
        void t10_immovableNone_blocked() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(0, FrozenType.IMMOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertFalse(result, "Should return false - i cannot move");
            assertEquals(10, cells[0].getValue(), "cells[0] should remain 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should remain 5");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0");
        }

        @Test
        @DisplayName("T11: NONE/IMMOVABLE - Blocked by frozen")
        void t11_noneImmovable_blocked() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(1, FrozenType.IMMOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertFalse(result, "Should return false - j cannot be displaced");
            assertEquals(10, cells[0].getValue(), "cells[0] should remain 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should remain 5");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0");
        }

        @Test
        @DisplayName("T12: IMMOVABLE/IMMOVABLE - Blocked by frozen")
        void t12_immovableImmovable_blocked() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(0, FrozenType.IMMOVABLE);
            frozenStatus.setFrozen(1, FrozenType.IMMOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            assertFalse(result, "Should return false - both frozen");
            assertEquals(10, cells[0].getValue(), "cells[0] should remain 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should remain 5");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0");
        }
    }

    // ========================================================================
    // T13-T14: wouldSwap parity tests
    // ========================================================================

    @Nested
    @DisplayName("wouldSwap parity")
    class WouldSwapTests {

        @Test
        @DisplayName("T13: wouldSwap returns true when not frozen")
        void t13_wouldSwap_positive() {
            IntCell[] cells = createCells(10, 5);

            boolean wouldResult = swapEngine.wouldSwap(cells, 0, 1);

            assertTrue(wouldResult, "wouldSwap should return true when not frozen");
            assertEquals(10, cells[0].getValue(), "cells[0] should remain 10 (no mutation)");
            assertEquals(5, cells[1].getValue(), "cells[1] should remain 5 (no mutation)");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should remain 0");
        }

        @Test
        @DisplayName("T14: wouldSwap returns false when frozen blocks")
        void t14_wouldSwap_negative_frozen() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(1, FrozenType.IMMOVABLE);

            boolean wouldResult = swapEngine.wouldSwap(cells, 0, 1);

            assertFalse(wouldResult, "wouldSwap should return false");
        }
    }

    // ========================================================================
    // T15-T16: Swap count tracking
    // ========================================================================

    @Nested
    @DisplayName("Swap count tracking")
    class SwapCountTests {

        @Test
        @DisplayName("T15: Swap count accumulates correctly")
        void t15_swapCount_accumulates() {
            IntCell[] cells = createCells(10, 5, 3);

            swapEngine.attemptSwap(cells, 0, 1);  // 10 > 5, swaps -> [5, 10, 3]
            swapEngine.attemptSwap(cells, 1, 2);  // 10 > 3, swaps -> [5, 3, 10]
            swapEngine.attemptSwap(cells, 0, 1);  // 5 > 3, swaps -> [3, 5, 10]

            assertEquals(3, swapEngine.getSwapCount(), "Should have 3 swaps");
            assertEquals(3, cells[0].getValue(), "cells[0] should be 3");
            assertEquals(5, cells[1].getValue(), "cells[1] should be 5");
            assertEquals(10, cells[2].getValue(), "cells[2] should be 10");
        }

        @Test
        @DisplayName("T16: resetSwapCount clears counter")
        void t16_resetSwapCount() {
            IntCell[] cells = createCells(10, 5);
            swapEngine.attemptSwap(cells, 0, 1);
            assertEquals(1, swapEngine.getSwapCount(), "Should have 1 swap before reset");

            swapEngine.resetSwapCount();

            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0 after reset");
        }
    }
}
