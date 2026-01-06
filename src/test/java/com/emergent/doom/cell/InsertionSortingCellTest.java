package com.emergent.doom.cell;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for InsertionSortingCell.
 *
 * <p><strong>PURPOSE:</strong> Verify INSERTION algotype implementation correctly implements
 * conservative left-only movement with left-sorted check. Tests framed from end-user perspective.</p>
 */
class InsertionSortingCellTest {
    
    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("As a user I want to create INSERTION cells so that I can initialize the sorting array")
        void createInsertionCell() {
            // PURPOSE: Verify InsertionSortingCell construction
            // INPUTS: value=42, position=5
            // EXPECTED OUTPUTS: Cell with INSERTION algotype
            // CONSOLE OUTPUT: Test passed - created INSERTION cell at position 5
            
            System.out.println("=== Testing InsertionSortingCell Construction ===");
            InsertionSortingCell cell = new InsertionSortingCell(42, 5);
            
            System.out.println("Value: " + cell.readValue());
            System.out.println("Algotype: " + cell.readAlgotype());
            System.out.println("Position: " + cell.readCurrentPosition());
            
            assertEquals(42, cell.readValue());
            assertEquals(SortingAlgotype.INSERTION, cell.readAlgotype());
            assertEquals(5, cell.readCurrentPosition());
            
            System.out.println("Test passed - created INSERTION cell at position 5\n");
        }
    }
    
    @Nested
    @DisplayName("Movement Decision Tests")
    class MovementDecisionTests {
        
        @Test
        @DisplayName("As a user I want INSERTION cells to move when left is sorted and value is smaller")
        void shouldMoveWhenLeftSortedAndSmallerValue() {
            // PURPOSE: Verify shouldMoveGiven() returns true when left sorted and this.value < left.value
            // INPUTS: Cell(value=30, position=3) with sorted left: [10, 20, 40]
            // EXPECTED OUTPUTS: shouldMoveGiven() returns true
            // CONSOLE OUTPUT: Test passed - INSERTION(30) wants to move into sorted left
            
            System.out.println("=== Testing INSERTION Movement Decision (Left Sorted, Smaller Value) ===");
            InsertionSortingCell cell = new InsertionSortingCell(30, 3);
            
            // Create sorted left prefix: [10, 20, 40]
            InsertionSortingCell left0 = new InsertionSortingCell(10, 0);
            InsertionSortingCell left1 = new InsertionSortingCell(20, 1);
            InsertionSortingCell left2 = new InsertionSortingCell(40, 2);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(left0, left1, left2);
            List<Integer> positions = List.of(0, 1, 2);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 3, 10, neighbors, positions);
            
            boolean shouldMove = cell.shouldMoveGiven(view);
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Left values: [10, 20, 40]");
            System.out.println("Should move: " + shouldMove);
            
            assertTrue(shouldMove);
            
            System.out.println("Test passed - INSERTION(30) wants to move into sorted left\n");
        }
        
        @Test
        @DisplayName("As a user I want INSERTION cells to not move when left is unsorted")
        void shouldNotMoveWhenLeftUnsorted() {
            // PURPOSE: Verify shouldMoveGiven() returns false when left is unsorted
            // INPUTS: Cell(value=30, position=3) with unsorted left: [20, 10, 40]
            // EXPECTED OUTPUTS: shouldMoveGiven() returns false
            // CONSOLE OUTPUT: Test passed - INSERTION(30) waits for left to sort
            
            System.out.println("=== Testing INSERTION Movement Decision (Left Unsorted) ===");
            InsertionSortingCell cell = new InsertionSortingCell(30, 3);
            
            // Create unsorted left prefix: [20, 10, 40] - out of order
            InsertionSortingCell left0 = new InsertionSortingCell(20, 0);
            InsertionSortingCell left1 = new InsertionSortingCell(10, 1);
            InsertionSortingCell left2 = new InsertionSortingCell(40, 2);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(left0, left1, left2);
            List<Integer> positions = List.of(0, 1, 2);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 3, 10, neighbors, positions);
            
            boolean shouldMove = cell.shouldMoveGiven(view);
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Left values: [20, 10, 40] (unsorted)");
            System.out.println("Should move: " + shouldMove);
            
            assertFalse(shouldMove);
            
            System.out.println("Test passed - INSERTION(30) waits for left to sort\n");
        }
        
        @Test
        @DisplayName("As a user I want INSERTION cells to not move when at left boundary")
        void shouldNotMoveAtLeftBoundary() {
            // PURPOSE: Verify shouldMoveGiven() returns false at left boundary
            // INPUTS: Cell at position 0 (no left neighbors)
            // EXPECTED OUTPUTS: shouldMoveGiven() returns false
            // CONSOLE OUTPUT: Test passed - INSERTION at boundary doesn't move
            
            System.out.println("=== Testing INSERTION Movement Decision (At Boundary) ===");
            InsertionSortingCell cell = new InsertionSortingCell(42, 0);
            
            // No left neighbors
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 0, 10, List.of(), List.of());
            
            boolean shouldMove = cell.shouldMoveGiven(view);
            System.out.println("Position: " + cell.readCurrentPosition());
            System.out.println("Should move: " + shouldMove);
            
            assertFalse(shouldMove);
            
            System.out.println("Test passed - INSERTION at boundary doesn't move\n");
        }
    }
    
    @Nested
    @DisplayName("Target Position Calculation Tests")
    class TargetPositionTests {
        
        @Test
        @DisplayName("As a user I want INSERTION cells to swap left when left sorted and value smaller")
        void swapLeftWhenLeftSortedAndSmaller() {
            // PURPOSE: Verify calculateTargetPositionGiven() returns left position when valid
            // INPUTS: Cell(value=25, position=3) with sorted left: [10, 20, 30]
            // EXPECTED OUTPUTS: Target position is 2 (left neighbor)
            // CONSOLE OUTPUT: Test passed - INSERTION(25) targets left position 2
            
            System.out.println("=== Testing INSERTION Target Calculation (Valid Swap) ===");
            InsertionSortingCell cell = new InsertionSortingCell(25, 3);
            
            // Create sorted left prefix: [10, 20, 30]
            InsertionSortingCell left0 = new InsertionSortingCell(10, 0);
            InsertionSortingCell left1 = new InsertionSortingCell(20, 1);
            InsertionSortingCell left2 = new InsertionSortingCell(30, 2);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(left0, left1, left2);
            List<Integer> positions = List.of(0, 1, 2);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 3, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Left neighbor value: 30");
            System.out.println("Target position: " + target.orElse(-1));
            
            assertTrue(target.isPresent());
            assertEquals(2, target.get());
            
            System.out.println("Test passed - INSERTION(25) targets left position 2\n");
        }
        
        @Test
        @DisplayName("As a user I want INSERTION cells to return empty when left unsorted")
        void noSwapWhenLeftUnsorted() {
            // PURPOSE: Verify calculateTargetPositionGiven() returns empty when left unsorted
            // INPUTS: Cell(value=25, position=3) with unsorted left: [20, 10, 30]
            // EXPECTED OUTPUTS: Empty target
            // CONSOLE OUTPUT: Test passed - INSERTION(25) returns empty for unsorted left
            
            System.out.println("=== Testing INSERTION Target Calculation (Left Unsorted) ===");
            InsertionSortingCell cell = new InsertionSortingCell(25, 3);
            
            // Create unsorted left prefix: [20, 10, 30]
            InsertionSortingCell left0 = new InsertionSortingCell(20, 0);
            InsertionSortingCell left1 = new InsertionSortingCell(10, 1);
            InsertionSortingCell left2 = new InsertionSortingCell(30, 2);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(left0, left1, left2);
            List<Integer> positions = List.of(0, 1, 2);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 3, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Left values: [20, 10, 30] (unsorted)");
            System.out.println("Has target: " + target.isPresent());
            
            assertFalse(target.isPresent());
            
            System.out.println("Test passed - INSERTION(25) returns empty for unsorted left\n");
        }
    }
}
