package com.emergent.doom.cell;

import com.emergent.doom.group.CellStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract test suite for AbstractCell implementations.
 *
 * <p><strong>PURPOSE:</strong> Verify that AbstractCell subclasses correctly implement
 * the cell contract for domain-agnostic emergent behavior. Tests are framed from end-user
 * perspective.</p>
 *
 * <p><strong>TEST PHILOSOPHY:</strong> These tests define the contract that all AbstractCell
 * subclasses must satisfy. When implementing a new cell type, these tests should pass
 * to ensure compatibility with the engine.</p>
 */
class AbstractCellContractTest {
    
    @Nested
    @DisplayName("Intrinsic Property Tests")
    class IntrinsicPropertyTests {
        
        @Test
        @DisplayName("As a user I want cells to have immutable algotypes so that behavioral identity is stable")
        void algotypeIsImmutable() {
            // PURPOSE: Verify algotype cannot change after construction
            // INPUTS: Mock cell with BUBBLE algotype
            // EXPECTED OUTPUTS: readAlgotype() always returns same value
            // CONSOLE OUTPUT: Test passed - algotype {value} consistent across calls
            
            System.out.println("=== Testing Algotype Immutability ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            
            SortingAlgotype algotype1 = cell.readAlgotype();
            SortingAlgotype algotype2 = cell.readAlgotype();
            
            System.out.println("First read: " + algotype1);
            System.out.println("Second read: " + algotype2);
            System.out.println("Same reference: " + (algotype1 == algotype2));
            
            assertSame(algotype1, algotype2, "Algotype should be same reference");
            assertEquals(SortingAlgotype.BUBBLE, algotype1, "Algotype should be BUBBLE");
            
            System.out.println("Test passed - algotype " + algotype1 + " consistent across calls\n");
        }
        
        @Test
        @DisplayName("As a user I want cells to have immutable values so that domain data is stable")
        void valueIsImmutable() {
            // PURPOSE: Verify value cannot change after construction
            // INPUTS: Mock cell with value 42
            // EXPECTED OUTPUTS: readValue() always returns same value
            // CONSOLE OUTPUT: Test passed - value {value} consistent across calls
            
            System.out.println("=== Testing Value Immutability ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            
            Integer value1 = cell.readValue();
            Integer value2 = cell.readValue();
            
            System.out.println("First read: " + value1);
            System.out.println("Second read: " + value2);
            System.out.println("Equal values: " + value1.equals(value2));
            
            assertEquals(value1, value2, "Value should be equal");
            assertEquals(42, value1, "Value should be 42");
            
            System.out.println("Test passed - value " + value1 + " consistent across calls\n");
        }
        
        @Test
        @DisplayName("As a user I want readAlgotype to never return null so that I can safely use the value")
        void algotypeNeverNull() {
            // PURPOSE: Verify algotype is always non-null
            // INPUTS: Mock cell
            // EXPECTED OUTPUTS: readAlgotype() returns non-null
            // CONSOLE OUTPUT: Test passed - algotype is non-null: {algotype}
            
            System.out.println("=== Testing Algotype Non-Null Guarantee ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.SELECTION, 0);
            
            SortingAlgotype algotype = cell.readAlgotype();
            System.out.println("Algotype: " + algotype);
            System.out.println("Is null: " + (algotype == null));
            
            assertNotNull(algotype, "Algotype should never be null");
            
            System.out.println("Test passed - algotype is non-null: " + algotype + "\n");
        }
        
        @Test
        @DisplayName("As a user I want readValue to never return null so that I can safely use the value")
        void valueNeverNull() {
            // PURPOSE: Verify value is always non-null
            // INPUTS: Mock cell
            // EXPECTED OUTPUTS: readValue() returns non-null
            // CONSOLE OUTPUT: Test passed - value is non-null: {value}
            
            System.out.println("=== Testing Value Non-Null Guarantee ===");
            MockSortingCell cell = new MockSortingCell(99, SortingAlgotype.INSERTION, 0);
            
            Integer value = cell.readValue();
            System.out.println("Value: " + value);
            System.out.println("Is null: " + (value == null));
            
            assertNotNull(value, "Value should never be null");
            
            System.out.println("Test passed - value is non-null: " + value + "\n");
        }
    }
    
    @Nested
    @DisplayName("Mutable State Tests")
    class MutableStateTests {
        
        @Test
        @DisplayName("As a user I want to update cell position so that cells track their array location")
        void positionIsMutable() {
            // PURPOSE: Verify position can be updated
            // INPUTS: Mock cell, update position to 5 then 10
            // EXPECTED OUTPUTS: readCurrentPosition() reflects updates
            // CONSOLE OUTPUT: Test passed - position updated from {old} to {new}
            
            System.out.println("=== Testing Position Mutability ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            
            int initial = cell.readCurrentPosition();
            System.out.println("Initial position: " + initial);
            
            cell.updatePositionTo(5);
            int afterFirst = cell.readCurrentPosition();
            System.out.println("After first update: " + afterFirst);
            
            cell.updatePositionTo(10);
            int afterSecond = cell.readCurrentPosition();
            System.out.println("After second update: " + afterSecond);
            
            assertEquals(0, initial, "Initial position should be 0");
            assertEquals(5, afterFirst, "First update should set position to 5");
            assertEquals(10, afterSecond, "Second update should set position to 10");
            
            System.out.println("Test passed - position updated from " + initial + " to " + afterSecond + "\n");
        }
        
        @Test
        @DisplayName("As a user I want to update cell status so that I can control swap eligibility")
        void statusIsMutable() {
            // PURPOSE: Verify status can be updated
            // INPUTS: Mock cell, update status to FREEZE then ACTIVE
            // EXPECTED OUTPUTS: readStatus() reflects updates
            // CONSOLE OUTPUT: Test passed - status updated from {old} to {new}
            
            System.out.println("=== Testing Status Mutability ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            
            CellStatus initial = cell.readStatus();
            System.out.println("Initial status: " + initial);
            
            cell.updateStatusTo(CellStatus.FREEZE);
            CellStatus afterFreeze = cell.readStatus();
            System.out.println("After freeze: " + afterFreeze);
            
            cell.updateStatusTo(CellStatus.ACTIVE);
            CellStatus afterActive = cell.readStatus();
            System.out.println("After reactivate: " + afterActive);
            
            assertEquals(CellStatus.ACTIVE, initial, "Initial status should be ACTIVE");
            assertEquals(CellStatus.FREEZE, afterFreeze, "Status should be FREEZE");
            assertEquals(CellStatus.ACTIVE, afterActive, "Status should be ACTIVE again");
            
            System.out.println("Test passed - status updated from " + initial + " to " + afterActive + "\n");
        }
    }
    
    @Nested
    @DisplayName("Comparison Tests")
    class ComparisonTests {
        
        @Test
        @DisplayName("As a user I want cells to compare by value so that sorting works correctly")
        void compareByValue() {
            // PURPOSE: Verify compareTo() uses value, not algotype
            // INPUTS: Three cells with different values and algotypes
            // EXPECTED OUTPUTS: Comparison based on value only
            // CONSOLE OUTPUT: Test passed - cells compared correctly by value
            
            System.out.println("=== Testing Value-Based Comparison ===");
            MockSortingCell cell1 = new MockSortingCell(10, SortingAlgotype.BUBBLE, 0);
            MockSortingCell cell2 = new MockSortingCell(20, SortingAlgotype.SELECTION, 1);
            MockSortingCell cell3 = new MockSortingCell(15, SortingAlgotype.INSERTION, 2);
            
            int cmp12 = cell1.compareTo(cell2);
            int cmp21 = cell2.compareTo(cell1);
            int cmp13 = cell1.compareTo(cell3);
            int cmp31 = cell3.compareTo(cell1);
            
            System.out.println("Cell1(10, BUBBLE) vs Cell2(20, SELECTION): " + cmp12);
            System.out.println("Cell2(20, SELECTION) vs Cell1(10, BUBBLE): " + cmp21);
            System.out.println("Cell1(10, BUBBLE) vs Cell3(15, INSERTION): " + cmp13);
            System.out.println("Cell3(15, INSERTION) vs Cell1(10, BUBBLE): " + cmp31);
            
            assertTrue(cmp12 < 0, "10 < 20, should be negative");
            assertTrue(cmp21 > 0, "20 > 10, should be positive");
            assertTrue(cmp13 < 0, "10 < 15, should be negative");
            assertTrue(cmp31 > 0, "15 > 10, should be positive");
            
            System.out.println("Test passed - cells compared correctly by value\n");
        }
        
        @Test
        @DisplayName("As a user I want hasGreaterValueThan to be readable so that swap logic is clear")
        void hasGreaterValueThanReadable() {
            // PURPOSE: Verify hasGreaterValueThan() is more readable than compareTo() > 0
            // INPUTS: Two cells with different values
            // EXPECTED OUTPUTS: Correct boolean result
            // CONSOLE OUTPUT: Test passed - hasGreaterValueThan provides readable comparison
            
            System.out.println("=== Testing Readable Value Comparison ===");
            MockSortingCell larger = new MockSortingCell(50, SortingAlgotype.BUBBLE, 0);
            MockSortingCell smaller = new MockSortingCell(30, SortingAlgotype.BUBBLE, 1);
            
            boolean largerThanSmaller = larger.hasGreaterValueThan(smaller);
            boolean smallerThanLarger = smaller.hasGreaterValueThan(larger);
            
            System.out.println("Larger(50) > Smaller(30): " + largerThanSmaller);
            System.out.println("Smaller(30) > Larger(50): " + smallerThanLarger);
            
            assertTrue(largerThanSmaller, "50 should be greater than 30");
            assertFalse(smallerThanLarger, "30 should not be greater than 50");
            
            System.out.println("Test passed - hasGreaterValueThan provides readable comparison\n");
        }
    }
    
    @Nested
    @DisplayName("Swap Eligibility Tests")
    class SwapEligibilityTests {
        
        @Test
        @DisplayName("As a user I want ACTIVE cells to initiate swaps so that sorting can progress")
        void activeCellsCanInitiate() {
            // PURPOSE: Verify ACTIVE cells can initiate swaps
            // INPUTS: Cell with ACTIVE status
            // EXPECTED OUTPUTS: canInitiateSwap() returns true
            // CONSOLE OUTPUT: Test passed - ACTIVE cell can initiate swaps
            
            System.out.println("=== Testing ACTIVE Cell Swap Initiation ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            cell.updateStatusTo(CellStatus.ACTIVE);
            
            boolean canInitiate = cell.canInitiateSwap();
            System.out.println("Status: " + cell.readStatus());
            System.out.println("Can initiate: " + canInitiate);
            
            assertTrue(canInitiate, "ACTIVE cells should be able to initiate swaps");
            
            System.out.println("Test passed - ACTIVE cell can initiate swaps\n");
        }
        
        @Test
        @DisplayName("As a user I want FREEZE cells to not initiate swaps so that frozen cells stay passive")
        void frozenCellsCannotInitiate() {
            // PURPOSE: Verify FREEZE cells cannot initiate swaps
            // INPUTS: Cell with FREEZE status
            // EXPECTED OUTPUTS: canInitiateSwap() returns false
            // CONSOLE OUTPUT: Test passed - FREEZE cell cannot initiate swaps
            
            System.out.println("=== Testing FREEZE Cell Swap Initiation ===");
            MockSortingCell cell = new MockSortingCell(42, SortingAlgotype.BUBBLE, 0);
            cell.updateStatusTo(CellStatus.FREEZE);
            
            boolean canInitiate = cell.canInitiateSwap();
            System.out.println("Status: " + cell.readStatus());
            System.out.println("Can initiate: " + canInitiate);
            
            assertFalse(canInitiate, "FREEZE cells should not be able to initiate swaps");
            
            System.out.println("Test passed - FREEZE cell cannot initiate swaps\n");
        }
        
        @Test
        @DisplayName("As a user I want FREEZE cells to accept swaps so that other cells can move them")
        void frozenCellsCanAccept() {
            // PURPOSE: Verify FREEZE cells can accept swaps
            // INPUTS: Frozen cell, active initiator
            // EXPECTED OUTPUTS: canAcceptSwapFrom() returns true
            // CONSOLE OUTPUT: Test passed - FREEZE cell can accept swaps from ACTIVE cell
            
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
            
            System.out.println("Test passed - FREEZE cell can accept swaps from ACTIVE cell\n");
        }
    }
    
    // ==================== Mock Implementation for Testing ====================
    
    /**
     * Mock sorting cell for contract testing.
     *
     * <p><strong>PURPOSE:</strong> Minimal AbstractCell implementation to test contract
     * without introducing domain-specific complexity.</p>
     */
    private static class MockSortingCell extends AbstractCell<Integer, SortingAlgotype> {
        
        private final Integer value;
        private final SortingAlgotype algotype;
        private int position;
        private CellStatus status;
        
        public MockSortingCell(Integer value, SortingAlgotype algotype, int position) {
            this.value = value;
            this.algotype = algotype;
            this.position = position;
            this.status = CellStatus.ACTIVE;
        }
        
        @Override
        public SortingAlgotype readAlgotype() {
            return algotype;
        }
        
        @Override
        public Integer readValue() {
            return value;
        }
        
        @Override
        public int readCurrentPosition() {
            return position;
        }
        
        @Override
        public void updatePositionTo(int newPosition) {
            this.position = newPosition;
        }
        
        @Override
        public CellStatus readStatus() {
            return status;
        }
        
        @Override
        public void updateStatusTo(CellStatus newStatus) {
            this.status = newStatus;
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
        
        @Override
        public boolean canInitiateSwap() {
            return status == CellStatus.ACTIVE;
        }
        
        @Override
        public boolean canAcceptSwapFrom(AbstractCell<Integer, SortingAlgotype> initiator) {
            // Can accept if ACTIVE or FREEZE (but not SLEEP/INACTIVE)
            return status == CellStatus.ACTIVE || status == CellStatus.FREEZE;
        }
        
        @Override
        public boolean hasGreaterValueThan(AbstractCell<Integer, SortingAlgotype> other) {
            return this.value > other.readValue();
        }
    }
}
