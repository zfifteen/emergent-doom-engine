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

        /**
         * PURPOSE: As a developer, I want SwapEngine to swap cells when both are NONE
         * so that I can verify basic swap functionality without frozen state interference.
         *
         * INPUTS: Two NONE cells with values [10, 5]
         * EXPECTED OUTPUT: Cells swap positions to [5, 10], returns true
         * TEST DATA: cells=[10, 5], both NONE frozen state
         * REPRODUCTION: Create cells, call attemptSwap(cells, 0, 1), verify values swapped
         */
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

        /**
         * PURPOSE: As a developer, I want SwapEngine to swap cells even when i < j (dumb executor)
         * so that I can verify the engine performs swaps without value checking.
         *
         * INPUTS: Two NONE cells with values [5, 10] where i < j
         * EXPECTED OUTPUT: Cells swap to [10, 5], returns true (dumb swap)
         * TEST DATA: cells=[5, 10], both NONE frozen state
         * REPRODUCTION: Create cells, call attemptSwap(cells, 0, 1), verify swap occurred despite i < j
         */
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

        /**
         * PURPOSE: As a developer, I want MOVABLE cells to be blocked from initiating swaps
         * so that I can verify frozen state correctly prevents swap initiation.
         *
         * INPUTS: MOVABLE cell at index 0, NONE cell at index 1, values [10, 5]
         * EXPECTED OUTPUT: Swap blocked, returns false, values unchanged
         * TEST DATA: cells=[10, 5], frozen[0]=MOVABLE, frozen[1]=NONE
         * REPRODUCTION: Set cell 0 to MOVABLE, attempt swap, verify blocked and swap count is 0
         */
        @Test
        @DisplayName("T04: MOVABLE/NONE - Blocked (MOVABLE cannot initiate)")
        void t04_movableNone_blocked() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(0, FrozenType.MOVABLE);
            // Cell 1 defaults to NONE

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            // MOVABLE cells cannot initiate swaps (matches Python FREEZE)
            assertFalse(result, "Should return false - MOVABLE cannot initiate");
            assertEquals(10, cells[0].getValue(), "cells[0] should remain 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should remain 5");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0");
        }

        /**
         * PURPOSE: As a developer, I want MOVABLE cells to be blocked from initiating swaps even when i < j
         * so that I can verify frozen state enforcement is consistent regardless of values.
         *
         * INPUTS: MOVABLE cell at index 0, NONE cell at index 1, values [5, 10]
         * EXPECTED OUTPUT: Swap blocked, returns false, values unchanged
         * TEST DATA: cells=[5, 10], frozen[0]=MOVABLE, frozen[1]=NONE
         * REPRODUCTION: Set cell 0 to MOVABLE, attempt swap with i < j, verify blocked
         */
        @Test
        @DisplayName("T05: MOVABLE/NONE with i < j - Blocked (MOVABLE cannot initiate)")
        void t05_movableNone_lessThan_blocked() {
            IntCell[] cells = createCells(5, 10);
            frozenStatus.setFrozen(0, FrozenType.MOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            // MOVABLE cells cannot initiate swaps (matches Python FREEZE)
            assertFalse(result, "Should return false - MOVABLE cannot initiate");
            assertEquals(5, cells[0].getValue(), "cells[0] should remain 5");
            assertEquals(10, cells[1].getValue(), "cells[1] should remain 10");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0");
        }
    }

    // ========================================================================
    // T07-T12: Cases where frozen check FAILS (blocked before comparison)
    // ========================================================================

    @Nested
    @DisplayName("Frozen state blocks swap")
    class FrozenBlocksTests {

        /**
         * PURPOSE: As a developer, I want MOVABLE cells to be displaceable by NONE cells
         * so that I can verify MOVABLE allows passive displacement.
         *
         * INPUTS: NONE cell at index 0, MOVABLE cell at index 1, values [10, 5]
         * EXPECTED OUTPUT: Swap succeeds, returns true, values become [5, 10]
         * TEST DATA: cells=[10, 5], frozen[0]=NONE, frozen[1]=MOVABLE
         * REPRODUCTION: Set cell 1 to MOVABLE, attempt swap, verify swap occurs
         */
        @Test
        @DisplayName("T07: NONE/MOVABLE - Swaps (MOVABLE can be displaced)")
        void t07_noneMovable_swaps() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(1, FrozenType.MOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            // MOVABLE cells CAN be displaced (matches Python FREEZE)
            assertTrue(result, "Should return true - MOVABLE can be displaced");
            assertEquals(5, cells[0].getValue(), "cells[0] should now be 5");
            assertEquals(10, cells[1].getValue(), "cells[1] should now be 10");
            assertEquals(1, swapEngine.getSwapCount(), "Swap count should be 1");
        }

        /**
         * PURPOSE: As a developer, I want MOVABLE cells to be displaceable even when i < j
         * so that I can verify MOVABLE displacement works regardless of value ordering.
         *
         * INPUTS: NONE cell at index 0, MOVABLE cell at index 1, values [5, 10]
         * EXPECTED OUTPUT: Swap succeeds, returns true, values become [10, 5]
         * TEST DATA: cells=[5, 10], frozen[0]=NONE, frozen[1]=MOVABLE
         * REPRODUCTION: Set cell 1 to MOVABLE, attempt swap with i < j, verify swap occurs
         */
        @Test
        @DisplayName("T08: NONE/MOVABLE with i < j - Swaps (MOVABLE can be displaced)")
        void t08_noneMovable_lessThan_swaps() {
            IntCell[] cells = createCells(5, 10);
            frozenStatus.setFrozen(1, FrozenType.MOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            // MOVABLE cells CAN be displaced (matches Python FREEZE)
            assertTrue(result, "Should return true - MOVABLE can be displaced");
            assertEquals(10, cells[0].getValue(), "cells[0] should now be 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should now be 5");
            assertEquals(1, swapEngine.getSwapCount(), "Swap count should be 1");
        }

        /**
         * PURPOSE: As a developer, I want MOVABLE/MOVABLE swaps to be blocked
         * so that I can verify MOVABLE cells cannot initiate swaps even against displaceable targets.
         *
         * INPUTS: Both cells MOVABLE, values [10, 5]
         * EXPECTED OUTPUT: Swap blocked, returns false, values unchanged
         * TEST DATA: cells=[10, 5], frozen[0]=MOVABLE, frozen[1]=MOVABLE
         * REPRODUCTION: Set both cells to MOVABLE, attempt swap, verify blocked
         */
        @Test
        @DisplayName("T09: MOVABLE/MOVABLE - Blocked (MOVABLE cannot initiate)")
        void t09_movableMovable_blocked() {
            IntCell[] cells = createCells(10, 5);
            frozenStatus.setFrozen(0, FrozenType.MOVABLE);
            frozenStatus.setFrozen(1, FrozenType.MOVABLE);

            boolean result = swapEngine.attemptSwap(cells, 0, 1);

            // i=MOVABLE cannot initiate (j=MOVABLE could be displaced, but i can't initiate)
            assertFalse(result, "Should return false - MOVABLE cannot initiate");
            assertEquals(10, cells[0].getValue(), "cells[0] should remain 10");
            assertEquals(5, cells[1].getValue(), "cells[1] should remain 5");
            assertEquals(0, swapEngine.getSwapCount(), "Swap count should be 0");
        }

        /**
         * PURPOSE: As a developer, I want IMMOVABLE cells to block swap initiation
         * so that I can verify completely frozen cells cannot move.
         *
         * INPUTS: IMMOVABLE cell at index 0, NONE cell at index 1, values [10, 5]
         * EXPECTED OUTPUT: Swap blocked, returns false, values unchanged
         * TEST DATA: cells=[10, 5], frozen[0]=IMMOVABLE, frozen[1]=NONE
         * REPRODUCTION: Set cell 0 to IMMOVABLE, attempt swap, verify blocked
         */
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

        /**
         * PURPOSE: As a developer, I want IMMOVABLE cells to block displacement
         * so that I can verify completely frozen cells cannot be moved by others.
         *
         * INPUTS: NONE cell at index 0, IMMOVABLE cell at index 1, values [10, 5]
         * EXPECTED OUTPUT: Swap blocked, returns false, values unchanged
         * TEST DATA: cells=[10, 5], frozen[0]=NONE, frozen[1]=IMMOVABLE
         * REPRODUCTION: Set cell 1 to IMMOVABLE, attempt swap, verify blocked
         */
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

        /**
         * PURPOSE: As a developer, I want IMMOVABLE/IMMOVABLE swaps to be blocked
         * so that I can verify completely frozen cells cannot interact.
         *
         * INPUTS: Both cells IMMOVABLE, values [10, 5]
         * EXPECTED OUTPUT: Swap blocked, returns false, values unchanged
         * TEST DATA: cells=[10, 5], frozen[0]=IMMOVABLE, frozen[1]=IMMOVABLE
         * REPRODUCTION: Set both cells to IMMOVABLE, attempt swap, verify blocked
         */
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

        /**
         * PURPOSE: As a developer, I want wouldSwap to return true when swap is allowed
         * so that I can check swap possibility without mutating the array.
         *
         * INPUTS: Two NONE cells with values [10, 5]
         * EXPECTED OUTPUT: wouldSwap returns true, array unchanged, swap count 0
         * TEST DATA: cells=[10, 5], both NONE
         * REPRODUCTION: Call wouldSwap, verify returns true without side effects
         */
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

        /**
         * PURPOSE: As a developer, I want wouldSwap to return false when frozen blocks
         * so that I can check swap possibility without side effects when frozen.
         *
         * INPUTS: NONE cell at index 0, IMMOVABLE cell at index 1, values [10, 5]
         * EXPECTED OUTPUT: wouldSwap returns false
         * TEST DATA: cells=[10, 5], frozen[0]=NONE, frozen[1]=IMMOVABLE
         * REPRODUCTION: Set cell 1 to IMMOVABLE, call wouldSwap, verify returns false
         */
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

        /**
         * PURPOSE: As a developer, I want swap count to accumulate correctly
         * so that I can track the total number of swaps performed.
         *
         * INPUTS: Three successive swaps on array [10, 5, 3]
         * EXPECTED OUTPUT: Swap count equals 3, final array is [3, 5, 10]
         * TEST DATA: cells=[10, 5, 3], 3 attemptSwap calls
         * REPRODUCTION: Perform 3 swaps, verify swap count is 3
         */
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

        /**
         * PURPOSE: As a developer, I want resetSwapCount to clear the counter
         * so that I can reuse the swap engine for multiple trials.
         *
         * INPUTS: SwapEngine with swap count of 1
         * EXPECTED OUTPUT: After reset, swap count is 0
         * TEST DATA: 1 swap then reset
         * REPRODUCTION: Perform swap, call resetSwapCount, verify count is 0
         */
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
