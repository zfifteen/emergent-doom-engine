package com.emergent.doom.cell;

import com.emergent.doom.group.CellStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for AbstractSortingCell.
 *
 * <p><strong>PURPOSE:</strong> Verify the main entry point for sorting domain correctly
 * implements AbstractCell contract with Integer values and SortingAlgotype enums. Tests
 * framed from end-user perspective.</p>
 */
class AbstractSortingCellTest {
    
    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("As a user I want to create sorting cells with value, algotype, and position so that I can initialize the sorting array")
        void createSortingCell() {
            // PURPOSE: Verify AbstractSortingCell construction
            // INPUTS: value=42, algotype=BUBBLE, position=5
            // EXPECTED OUTPUTS: Cell with correct immutable and mutable state
            // CONSOLE OUTPUT: Test passed - created cell with value {value}, algotype {algotype}, position {position}
            
            System.out.println("=== Testing AbstractSortingCell Construction ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 5);
            
            System.out.println("Value: " + cell.readValue());
            System.out.println("Algotype: " + cell.readAlgotype());
            System.out.println("Position: " + cell.readCurrentPosition());
            System.out.println("Status: " + cell.readStatus());
            
            assertEquals(42, cell.readValue(), "Value should be 42");
            assertEquals(SortingAlgotype.BUBBLE, cell.readAlgotype(), "Algotype should be BUBBLE");
            assertEquals(5, cell.readCurrentPosition(), "Position should be 5");
            assertEquals(CellStatus.ACTIVE, cell.readStatus(), "Status should be ACTIVE by default");
            
            System.out.println("Test passed - created cell with value " + cell.readValue() + 
                ", algotype " + cell.readAlgotype() + ", position " + cell.readCurrentPosition() + "\n");
        }
        
        @Test
        @DisplayName("As a user I want construction to fail with null algotype so that I catch configuration errors early")
        void rejectNullAlgotype() {
            // PURPOSE: Verify null algotype validation
            // INPUTS: value=42, algotype=null, position=0
            // EXPECTED OUTPUTS: NullPointerException
            // CONSOLE OUTPUT: Test passed - null algotype rejected with exception
            
            System.out.println("=== Testing Null Algotype Rejection ===");
            
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new MockSortingCell(42, null, 0),
                "Should throw NullPointerException for null algotype"
            );
            
            System.out.println("Exception message: " + exception.getMessage());
            assertTrue(exception.getMessage().contains("algotype"),
                "Exception message should mention algotype");
            
            System.out.println("Test passed - null algotype rejected with exception\n");
        }
        
        @Test
        @DisplayName("As a user I want construction to fail with negative position so that I catch initialization errors early")
        void rejectNegativePosition() {
            // PURPOSE: Verify negative position validation
            // INPUTS: value=42, algotype=BUBBLE, position=-1
            // EXPECTED OUTPUTS: IllegalArgumentException
            // CONSOLE OUTPUT: Test passed - negative position rejected with exception
            
            System.out.println("=== Testing Negative Position Rejection ===");
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new MockSortingCell(42, SortingAlgotype.BUBBLE, -1),
                "Should throw IllegalArgumentException for negative position"
            );
            
            System.out.println("Exception message: " + exception.getMessage());
            assertTrue(exception.getMessage().toLowerCase().contains("position"),
                "Exception message should mention position");
            
            System.out.println("Test passed - negative position rejected with exception\n");
        }
    }
    
    @Nested
    @DisplayName("Intrinsic Property Tests")
    class IntrinsicPropertyTests {
        
        @Test
        @DisplayName("As a user I want value to be immutable so that cell identity is stable during sorting")
        void valueIsImmutable() {
            // PURPOSE: Verify value cannot change after construction
            // INPUTS: Cell with value 99
            // EXPECTED OUTPUTS: readValue() always returns 99
            // CONSOLE OUTPUT: Test passed - value immutable at {value}
            
            System.out.println("=== Testing Value Immutability ===");
            MockSortingCell cell = new MockSortingCell(99, SortingAlgotype.INSERTION, 0);
            
            Integer value1 = cell.readValue();
            Integer value2 = cell.readValue();
            
            System.out.println("First read: " + value1);
            System.out.println("Second read: " + value2);
            
            assertEquals(value1, value2, "Value should be consistent");
            assertEquals(99, value1, "Value should be 99");
            
            System.out.println("Test passed - value immutable at " + value1 + "\n");
        }
        
        @Test
        @DisplayName("As a user I want algotype to be immutable so that behavioral identity is stable")
        void algotypeIsImmutable() {
            // PURPOSE: Verify algotype cannot change after construction
            // INPUTS: Cell with SELECTION algotype
            // EXPECTED OUTPUTS: readAlgotype() always returns SELECTION
            // CONSOLE OUTPUT: Test passed - algotype immutable at {algotype}
            
            System.out.println("=== Testing Algotype Immutability ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.SELECTION, 0);
            
            SortingAlgotype algotype1 = cell.readAlgotype();
            SortingAlgotype algotype2 = cell.readAlgotype();
            
            System.out.println("First read: " + algotype1);
            System.out.println("Second read: " + algotype2);
            
            assertSame(algotype1, algotype2, "Algotype should be same reference");
            assertEquals(SortingAlgotype.SELECTION, algotype1, "Algotype should be SELECTION");
            
            System.out.println("Test passed - algotype immutable at " + algotype1 + "\n");
        }
    }
    
    @Nested
    @DisplayName("Mutable State Tests")
    class MutableStateTests {
        
        @Test
        @DisplayName("As a user I want to update cell position so that cells track their location after swaps")
        void positionIsMutable() {
            // PURPOSE: Verify position can be updated
            // INPUTS: Cell at position 0, update to 10
            // EXPECTED OUTPUTS: readCurrentPosition() reflects update
            // CONSOLE OUTPUT: Test passed - position updated from {old} to {new}
            
            System.out.println("=== Testing Position Mutability ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            
            int initial = cell.readCurrentPosition();
            System.out.println("Initial position: " + initial);
            
            cell.updatePositionTo(10);
            int updated = cell.readCurrentPosition();
            System.out.println("Updated position: " + updated);
            
            assertEquals(0, initial, "Initial position should be 0");
            assertEquals(10, updated, "Updated position should be 10");
            
            System.out.println("Test passed - position updated from " + initial + " to " + updated + "\n");
        }
        
        @Test
        @DisplayName("As a user I want position update to reject negative values so that I catch errors early")
        void rejectNegativePositionUpdate() {
            // PURPOSE: Verify updatePositionTo() validates input
            // INPUTS: Cell, attempt to set position to -5
            // EXPECTED OUTPUTS: IllegalArgumentException
            // CONSOLE OUTPUT: Test passed - negative position update rejected
            
            System.out.println("=== Testing Negative Position Update Rejection ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cell.updatePositionTo(-5),
                "Should throw IllegalArgumentException for negative position"
            );
            
            System.out.println("Exception message: " + exception.getMessage());
            assertTrue(exception.getMessage().toLowerCase().contains("position"),
                "Exception message should mention position");
            
            System.out.println("Test passed - negative position update rejected\n");
        }
        
        @Test
        @DisplayName("As a user I want to update cell status so that I can control swap eligibility")
        void statusIsMutable() {
            // PURPOSE: Verify status can be updated
            // INPUTS: Cell, update status to FREEZE then ACTIVE
            // EXPECTED OUTPUTS: readStatus() reflects updates
            // CONSOLE OUTPUT: Test passed - status updated from {old} to {new}
            
            System.out.println("=== Testing Status Mutability ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            
            CellStatus initial = cell.readStatus();
            System.out.println("Initial status: " + initial);
            
            cell.updateStatusTo(CellStatus.FREEZE);
            CellStatus frozen = cell.readStatus();
            System.out.println("After freeze: " + frozen);
            
            cell.updateStatusTo(CellStatus.ACTIVE);
            CellStatus active = cell.readStatus();
            System.out.println("After reactivate: " + active);
            
            assertEquals(CellStatus.ACTIVE, initial, "Initial status should be ACTIVE");
            assertEquals(CellStatus.FREEZE, frozen, "Status should be FREEZE");
            assertEquals(CellStatus.ACTIVE, active, "Status should be ACTIVE again");
            
            System.out.println("Test passed - status updated from " + initial + " to " + active + "\n");
        }
    }
    
    @Nested
    @DisplayName("Value Comparison Tests")
    class ValueComparisonTests {
        
        @Test
        @DisplayName("As a user I want cells to compare by value so that sorting works correctly")
        void compareByValue() {
            // PURPOSE: Verify compareTo() uses value
            // INPUTS: Three cells with different values
            // EXPECTED OUTPUTS: Comparison based on value
            // CONSOLE OUTPUT: Test passed - cells compare correctly by value
            
            System.out.println("=== Testing Value-Based Comparison ===");
            MockSortingCell cell1 = new MockSortingCell(10, SortingAlgotype.BUBBLE, 0);
            MockSortingCell cell2 = new MockSortingCell(20, SortingAlgotype.SELECTION, 1);
            MockSortingCell cell3 = new MockSortingCell(15, SortingAlgotype.INSERTION, 2);
            
            int cmp12 = cell1.compareTo(cell2);
            int cmp21 = cell2.compareTo(cell1);
            int cmp13 = cell1.compareTo(cell3);
            
            System.out.println("10 vs 20: " + cmp12);
            System.out.println("20 vs 10: " + cmp21);
            System.out.println("10 vs 15: " + cmp13);
            
            assertTrue(cmp12 < 0, "10 < 20");
            assertTrue(cmp21 > 0, "20 > 10");
            assertTrue(cmp13 < 0, "10 < 15");
            
            System.out.println("Test passed - cells compare correctly by value\n");
        }
        
        @Test
        @DisplayName("As a user I want hasGreaterValueThan to be readable so that swap logic is clear")
        void hasGreaterValueThanReadable() {
            // PURPOSE: Verify hasGreaterValueThan() is readable
            // INPUTS: Two cells with different values
            // EXPECTED OUTPUTS: Correct boolean result
            // CONSOLE OUTPUT: Test passed - hasGreaterValueThan is readable
            
            System.out.println("=== Testing Readable Value Comparison ===");
            MockSortingCell larger = new MockSortingCell(50, SortingAlgotype.BUBBLE, 0);
            MockSortingCell smaller = new MockSortingCell(30, SortingAlgotype.BUBBLE, 1);
            
            boolean largerThanSmaller = larger.hasGreaterValueThan(smaller);
            boolean smallerThanLarger = smaller.hasGreaterValueThan(larger);
            
            System.out.println("50 > 30: " + largerThanSmaller);
            System.out.println("30 > 50: " + smallerThanLarger);
            
            assertTrue(largerThanSmaller, "50 should be greater than 30");
            assertFalse(smallerThanLarger, "30 should not be greater than 50");
            
            System.out.println("Test passed - hasGreaterValueThan is readable\n");
        }
    }
    
    @Nested
    @DisplayName("Swap Eligibility Tests")
    class SwapEligibilityTests {
        
        @Test
        @DisplayName("As a user I want ACTIVE cells to initiate swaps so that sorting progresses")
        void activeCellsCanInitiate() {
            // PURPOSE: Verify ACTIVE cells can initiate swaps
            // INPUTS: Cell with ACTIVE status
            // EXPECTED OUTPUTS: canInitiateSwap() returns true
            // CONSOLE OUTPUT: Test passed - ACTIVE cell can initiate
            
            System.out.println("=== Testing ACTIVE Cell Swap Initiation ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            cell.updateStatusTo(CellStatus.ACTIVE);
            
            boolean canInitiate = cell.canInitiateSwap();
            System.out.println("Status: " + cell.readStatus());
            System.out.println("Can initiate: " + canInitiate);
            
            assertTrue(canInitiate, "ACTIVE cells should be able to initiate swaps");
            
            System.out.println("Test passed - ACTIVE cell can initiate\n");
        }
        
        @Test
        @DisplayName("As a user I want FREEZE cells to not initiate swaps so that frozen cells stay passive")
        void frozenCellsCannotInitiate() {
            // PURPOSE: Verify FREEZE cells cannot initiate swaps
            // INPUTS: Cell with FREEZE status
            // EXPECTED OUTPUTS: canInitiateSwap() returns false
            // CONSOLE OUTPUT: Test passed - FREEZE cell cannot initiate
            
            System.out.println("=== Testing FREEZE Cell Swap Initiation ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            cell.updateStatusTo(CellStatus.FREEZE);
            
            boolean canInitiate = cell.canInitiateSwap();
            System.out.println("Status: " + cell.readStatus());
            System.out.println("Can initiate: " + canInitiate);
            
            assertFalse(canInitiate, "FREEZE cells should not be able to initiate swaps");
            
            System.out.println("Test passed - FREEZE cell cannot initiate\n");
        }
        
        @Test
        @DisplayName("As a user I want FREEZE cells to accept swaps so that other cells can move them")
        void frozenCellsCanAccept() {
            // PURPOSE: Verify FREEZE cells can accept swaps
            // INPUTS: Frozen cell, active initiator
            // EXPECTED OUTPUTS: canAcceptSwapFrom() returns true
            // CONSOLE OUTPUT: Test passed - FREEZE cell can accept swaps
            
            System.out.println("=== Testing FREEZE Cell Swap Acceptance ===");
            MockSortingCell frozenCell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            MockSortingCell activeCell = new MockSortingCell(30, SortingAlgotype.BUBBLE, 1);
            
            frozenCell.updateStatusTo(CellStatus.FREEZE);
            activeCell.updateStatusTo(CellStatus.ACTIVE);
            
            boolean canAccept = frozenCell.canAcceptSwapFrom(activeCell);
            System.out.println("Frozen cell status: " + frozenCell.readStatus());
            System.out.println("Active cell status: " + activeCell.readStatus());
            System.out.println("Can accept swap: " + canAccept);
            
            assertTrue(canAccept, "FREEZE cells should be able to accept swaps");
            
            System.out.println("Test passed - FREEZE cell can accept swaps\n");
        }
    }
    
    // ==================== Mock Implementation for Testing ====================
    
    /**
     * Mock sorting cell for testing AbstractSortingCell.
     */
    private static class MockSortingCell extends AbstractSortingCell {
        
        public MockSortingCell(int value, SortingAlgotype algotype, int initialPosition) {
            super(value, algotype, initialPosition);
        }
        
        @Override
        public boolean shouldMoveGiven(NeighborhoodView<Integer, SortingAlgotype> neighbors) {
            // Mock implementation: always return true if has neighbors
            return neighbors.hasNeighbors();
        }
        
        @Override
        public Optional<Integer> calculateTargetPositionGiven(NeighborhoodView<Integer, SortingAlgotype> neighbors) {
            // Mock implementation: return first neighbor's position if available
            if (neighbors.hasNeighbors()) {
                return Optional.of(neighbors.getNeighborPositionAt(0));
            }
            return Optional.empty();
        }
    }
}
