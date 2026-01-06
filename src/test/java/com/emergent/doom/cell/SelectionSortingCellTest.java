package com.emergent.doom.cell;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SelectionSortingCell.
 *
 * <p><strong>PURPOSE:</strong> Verify SELECTION algotype implementation correctly implements
 * ideal position targeting with incremental convergence. Tests framed from end-user perspective.</p>
 */
class SelectionSortingCellTest {
    
    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("As a user I want to create SELECTION cells with initial ideal position so that I can configure sort direction")
        void createWithInitialIdealPosition() {
            // PURPOSE: Verify SelectionSortingCell construction with initial ideal position
            // INPUTS: value=42, position=5, idealPosition=0
            // EXPECTED OUTPUTS: Cell with SELECTION algotype and ideal position 0
            // CONSOLE OUTPUT: Test passed - created SELECTION cell at position 5, targeting ideal 0
            
            System.out.println("=== Testing SelectionSortingCell Construction ===");
            SelectionSortingCell cell = new SelectionSortingCell(42, 5, 0);
            
            System.out.println("Value: " + cell.readValue());
            System.out.println("Algotype: " + cell.readAlgotype());
            System.out.println("Current position: " + cell.readCurrentPosition());
            System.out.println("Ideal position: " + cell.getIdealPosition());
            
            assertEquals(42, cell.readValue());
            assertEquals(SortingAlgotype.SELECTION, cell.readAlgotype());
            assertEquals(5, cell.readCurrentPosition());
            assertEquals(0, cell.getIdealPosition());
            
            System.out.println("Test passed - created SELECTION cell at position 5, targeting ideal 0\n");
        }
    }
    
    @Nested
    @DisplayName("Ideal Position Management Tests")
    class IdealPositionTests {
        
        @Test
        @DisplayName("As a user I want to increment ideal position so that SELECTION cells adjust when swaps are denied")
        void incrementIdealPosition() {
            // PURPOSE: Verify incrementIdealPosition() updates position correctly
            // INPUTS: Cell with ideal position 0
            // EXPECTED OUTPUTS: Ideal position increments to 1, 2, 3
            // CONSOLE OUTPUT: Test passed - ideal position incremented from 0 to 3
            
            System.out.println("=== Testing Ideal Position Increment ===");
            SelectionSortingCell cell = new SelectionSortingCell(42, 5, 0);
            
            int initial = cell.getIdealPosition();
            System.out.println("Initial ideal: " + initial);
            
            int after1 = cell.incrementIdealPosition();
            System.out.println("After 1st increment: " + after1);
            
            int after2 = cell.incrementIdealPosition();
            System.out.println("After 2nd increment: " + after2);
            
            int after3 = cell.incrementIdealPosition();
            System.out.println("After 3rd increment: " + after3);
            
            assertEquals(0, initial);
            assertEquals(1, after1);
            assertEquals(2, after2);
            assertEquals(3, after3);
            assertEquals(3, cell.getIdealPosition());
            
            System.out.println("Test passed - ideal position incremented from 0 to 3\n");
        }
        
        @Test
        @DisplayName("As a user I want to set ideal position so that I can reset SELECTION cells during group merge")
        void setIdealPosition() {
            // PURPOSE: Verify setIdealPosition() updates position correctly
            // INPUTS: Cell, set ideal to 10
            // EXPECTED OUTPUTS: Ideal position is 10
            // CONSOLE OUTPUT: Test passed - ideal position set to 10
            
            System.out.println("=== Testing Ideal Position Set ===");
            SelectionSortingCell cell = new SelectionSortingCell(42, 5);
            
            cell.setIdealPosition(10);
            int ideal = cell.getIdealPosition();
            System.out.println("Ideal position after set: " + ideal);
            
            assertEquals(10, ideal);
            
            System.out.println("Test passed - ideal position set to 10\n");
        }
    }
    
    @Nested
    @DisplayName("Movement Decision Tests")
    class MovementDecisionTests {
        
        @Test
        @DisplayName("As a user I want SELECTION cells to move when not at ideal position so that they converge")
        void shouldMoveWhenNotAtIdeal() {
            // PURPOSE: Verify shouldMoveGiven() returns true when not at ideal position
            // INPUTS: Cell at position 5, ideal position 0
            // EXPECTED OUTPUTS: shouldMoveGiven() returns true
            // CONSOLE OUTPUT: Test passed - SELECTION at 5 wants to move to ideal 0
            
            System.out.println("=== Testing SELECTION Movement Decision (Not At Ideal) ===");
            SelectionSortingCell cell = new SelectionSortingCell(42, 5, 0);
            
            // Create empty neighborhood (neighbors parameter not used for SELECTION)
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 5, 10, List.of(), List.of());
            
            boolean shouldMove = cell.shouldMoveGiven(view);
            System.out.println("Current position: " + cell.readCurrentPosition());
            System.out.println("Ideal position: " + cell.getIdealPosition());
            System.out.println("Should move: " + shouldMove);
            
            assertTrue(shouldMove);
            
            System.out.println("Test passed - SELECTION at 5 wants to move to ideal 0\n");
        }
        
        @Test
        @DisplayName("As a user I want SELECTION cells to not move when at ideal position so that they don't waste cycles")
        void shouldNotMoveWhenAtIdeal() {
            // PURPOSE: Verify shouldMoveGiven() returns false when at ideal position
            // INPUTS: Cell at position 0, ideal position 0
            // EXPECTED OUTPUTS: shouldMoveGiven() returns false
            // CONSOLE OUTPUT: Test passed - SELECTION at ideal position doesn't move
            
            System.out.println("=== Testing SELECTION Movement Decision (At Ideal) ===");
            SelectionSortingCell cell = new SelectionSortingCell(42, 0, 0);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 0, 10, List.of(), List.of());
            
            boolean shouldMove = cell.shouldMoveGiven(view);
            System.out.println("Current position: " + cell.readCurrentPosition());
            System.out.println("Ideal position: " + cell.getIdealPosition());
            System.out.println("Should move: " + shouldMove);
            
            assertFalse(shouldMove);
            
            System.out.println("Test passed - SELECTION at ideal position doesn't move\n");
        }
    }
    
    @Nested
    @DisplayName("Target Position Calculation Tests")
    class TargetPositionTests {
        
        @Test
        @DisplayName("As a user I want SELECTION cells to swap with ideal position when value is smaller")
        void swapWithIdealWhenSmallerValue() {
            // PURPOSE: Verify SELECTION swaps with ideal position when this.value < target.value
            // INPUTS: Cell(value=30, position=5, ideal=0) with target(value=50, position=0)
            // EXPECTED OUTPUTS: Target position is 0
            // CONSOLE OUTPUT: Test passed - SELECTION(30) swaps with ideal position(0) target(50)
            
            System.out.println("=== Testing SELECTION Swap with Ideal Position (Smaller Value) ===");
            SelectionSortingCell cell = new SelectionSortingCell(30, 5, 0);
            SelectionSortingCell target = new SelectionSortingCell(50, 0);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(target);
            List<Integer> positions = List.of(0);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 5, 10, neighbors, positions);
            
            Optional<Integer> targetPos = cell.calculateTargetPositionGiven(view);
            
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Target value: " + target.readValue());
            System.out.println("Ideal position: " + cell.getIdealPosition());
            System.out.println("Target position: " + targetPos.orElse(-1));
            
            assertTrue(targetPos.isPresent());
            assertEquals(0, targetPos.get());
            
            System.out.println("Test passed - SELECTION(30) swaps with ideal position(0) target(50)\n");
        }
        
        @Test
        @DisplayName("As a user I want SELECTION cells to increment ideal position when swap is denied")
        void incrementIdealWhenSwapDenied() {
            // PURPOSE: Verify SELECTION increments ideal position when this.value > target.value
            // INPUTS: Cell(value=70, position=5, ideal=0) with target(value=50, position=0)
            // EXPECTED OUTPUTS: Empty target, ideal position incremented to 1
            // CONSOLE OUTPUT: Test passed - SELECTION(70) denied swap, ideal incremented to 1
            
            System.out.println("=== Testing SELECTION Ideal Increment (Swap Denied) ===");
            SelectionSortingCell cell = new SelectionSortingCell(70, 5, 0);
            SelectionSortingCell target = new SelectionSortingCell(50, 0);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(target);
            List<Integer> positions = List.of(0);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 5, 10, neighbors, positions);
            
            int idealBefore = cell.getIdealPosition();
            Optional<Integer> targetPos = cell.calculateTargetPositionGiven(view);
            int idealAfter = cell.getIdealPosition();
            
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Target value: " + target.readValue());
            System.out.println("Ideal before: " + idealBefore);
            System.out.println("Ideal after: " + idealAfter);
            System.out.println("Has target: " + targetPos.isPresent());
            
            assertFalse(targetPos.isPresent());
            assertEquals(0, idealBefore);
            assertEquals(1, idealAfter);
            
            System.out.println("Test passed - SELECTION(70) denied swap, ideal incremented to 1\n");
        }
    }
}
