package com.emergent.doom.cell;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for BubbleSortingCell.
 *
 * <p><strong>PURPOSE:</strong> Verify BUBBLE algotype implementation correctly implements
 * random bidirectional movement with local neighbor comparison. Tests framed from end-user perspective.</p>
 */
class BubbleSortingCellTest {
    
    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("As a user I want to create BUBBLE cells with seeded Random so that I can test deterministically")
        void createWithSeededRandom() {
            // PURPOSE: Verify BubbleSortingCell construction with seeded Random
            // INPUTS: value=42, position=5, seed=12345
            // EXPECTED OUTPUTS: Cell with BUBBLE algotype and deterministic random behavior
            // CONSOLE OUTPUT: Test passed - created BUBBLE cell with seed 12345
            
            System.out.println("=== Testing BubbleSortingCell Construction with Seeded Random ===");
            Random seededRandom = new Random(12345);
            BubbleSortingCell cell = new BubbleSortingCell(42, 5, seededRandom);
            
            System.out.println("Value: " + cell.readValue());
            System.out.println("Algotype: " + cell.readAlgotype());
            System.out.println("Position: " + cell.readCurrentPosition());
            
            assertEquals(42, cell.readValue(), "Value should be 42");
            assertEquals(SortingAlgotype.BUBBLE, cell.readAlgotype(), "Algotype should be BUBBLE");
            assertEquals(5, cell.readCurrentPosition(), "Position should be 5");
            
            System.out.println("Test passed - created BUBBLE cell with seed 12345\n");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cell algotype to be immutably BUBBLE so that behavioral identity is stable")
        void algotypeIsAlwaysBubble() {
            // PURPOSE: Verify algotype is always BUBBLE
            // INPUTS: BubbleSortingCell
            // EXPECTED OUTPUTS: readAlgotype() always returns BUBBLE
            // CONSOLE OUTPUT: Test passed - algotype is immutably BUBBLE
            
            System.out.println("=== Testing BUBBLE Algotype Immutability ===");
            BubbleSortingCell cell = new BubbleSortingCell(42, 0);
            
            SortingAlgotype algotype = cell.readAlgotype();
            System.out.println("Algotype: " + algotype);
            
            assertEquals(SortingAlgotype.BUBBLE, algotype, "Algotype should be BUBBLE");
            
            System.out.println("Test passed - algotype is immutably BUBBLE\n");
        }
    }
    
    @Nested
    @DisplayName("Movement Decision Tests")
    class MovementDecisionTests {
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to move when neighbors exist so that sorting progresses")
        void shouldMoveWhenNeighborsExist() {
            // PURPOSE: Verify shouldMoveGiven() returns true when neighbors exist
            // INPUTS: Cell with neighbors
            // EXPECTED OUTPUTS: shouldMoveGiven() returns true
            // CONSOLE OUTPUT: Test passed - BUBBLE wants to move when neighbors={count}
            
            System.out.println("=== Testing BUBBLE Movement Decision with Neighbors ===");
            BubbleSortingCell cell = new BubbleSortingCell(42, 5);
            
            // Create a neighborhood with one neighbor
            BubbleSortingCell neighbor = new BubbleSortingCell(30, 4);
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(neighbor);
            List<Integer> positions = List.of(4);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 5, 10, neighbors, positions);
            
            boolean shouldMove = cell.shouldMoveGiven(view);
            System.out.println("Neighbor count: " + view.getNeighborCount());
            System.out.println("Should move: " + shouldMove);
            
            assertTrue(shouldMove, "BUBBLE should want to move when neighbors exist");
            
            System.out.println("Test passed - BUBBLE wants to move when neighbors=" + view.getNeighborCount() + "\n");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to not move when isolated so that they don't waste cycles")
        void shouldNotMoveWhenNoNeighbors() {
            // PURPOSE: Verify shouldMoveGiven() returns false when no neighbors
            // INPUTS: Cell with empty neighborhood
            // EXPECTED OUTPUTS: shouldMoveGiven() returns false
            // CONSOLE OUTPUT: Test passed - BUBBLE doesn't move when isolated
            
            System.out.println("=== Testing BUBBLE Movement Decision Without Neighbors ===");
            BubbleSortingCell cell = new BubbleSortingCell(42, 0);
            
            // Create empty neighborhood
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = new ArrayList<>();
            List<Integer> positions = new ArrayList<>();
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 0, 1, neighbors, positions);
            
            boolean shouldMove = cell.shouldMoveGiven(view);
            System.out.println("Neighbor count: " + view.getNeighborCount());
            System.out.println("Should move: " + shouldMove);
            
            assertFalse(shouldMove, "BUBBLE should not want to move when isolated");
            
            System.out.println("Test passed - BUBBLE doesn't move when isolated\n");
        }
    }
    
    @Nested
    @DisplayName("Target Position Calculation Tests")
    class TargetPositionTests {
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to swap left when value is smaller than left neighbor")
        void swapLeftWhenSmallerThanLeft() {
            // PURPOSE: Verify BUBBLE swaps left when this.value < left.value (ascending sort)
            // INPUTS: Cell(value=30) with left neighbor(value=50)
            // EXPECTED OUTPUTS: Target position is left neighbor's position
            // CONSOLE OUTPUT: Test passed - BUBBLE(30) swaps left with neighbor(50)
            
            System.out.println("=== Testing BUBBLE Swap Left (Smaller Value) ===");
            BubbleSortingCell cell = new BubbleSortingCell(30, 5);
            BubbleSortingCell leftNeighbor = new BubbleSortingCell(50, 4);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(leftNeighbor);
            List<Integer> positions = List.of(4);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 5, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Left neighbor value: " + leftNeighbor.readValue());
            System.out.println("Target position: " + target.orElse(-1));
            
            assertTrue(target.isPresent(), "Should have target position");
            assertEquals(4, target.get(), "Should target left neighbor position");
            
            System.out.println("Test passed - BUBBLE(30) swaps left with neighbor(50)\n");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to swap right when value is larger than right neighbor")
        void swapRightWhenLargerThanRight() {
            // PURPOSE: Verify BUBBLE swaps right when this.value > right.value (ascending sort)
            // INPUTS: Cell(value=70) with right neighbor(value=50)
            // EXPECTED OUTPUTS: Target position is right neighbor's position
            // CONSOLE OUTPUT: Test passed - BUBBLE(70) swaps right with neighbor(50)
            
            System.out.println("=== Testing BUBBLE Swap Right (Larger Value) ===");
            BubbleSortingCell cell = new BubbleSortingCell(70, 5);
            BubbleSortingCell rightNeighbor = new BubbleSortingCell(50, 6);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(rightNeighbor);
            List<Integer> positions = List.of(6);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 5, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Right neighbor value: " + rightNeighbor.readValue());
            System.out.println("Target position: " + target.orElse(-1));
            
            assertTrue(target.isPresent(), "Should have target position");
            assertEquals(6, target.get(), "Should target right neighbor position");
            
            System.out.println("Test passed - BUBBLE(70) swaps right with neighbor(50)\n");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to not swap when already in correct order")
        void noSwapWhenInCorrectOrder() {
            // PURPOSE: Verify BUBBLE doesn't swap when ordering is already correct
            // INPUTS: Cell(value=50) with left neighbor(value=30)
            // EXPECTED OUTPUTS: Empty target (no swap)
            // CONSOLE OUTPUT: Test passed - BUBBLE(50) doesn't swap with correctly ordered left(30)
            
            System.out.println("=== Testing BUBBLE No Swap (Correct Order) ===");
            BubbleSortingCell cell = new BubbleSortingCell(50, 5);
            BubbleSortingCell leftNeighbor = new BubbleSortingCell(30, 4);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(leftNeighbor);
            List<Integer> positions = List.of(4);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 5, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Left neighbor value: " + leftNeighbor.readValue());
            System.out.println("Has target: " + target.isPresent());
            
            assertFalse(target.isPresent(), "Should not have target when in correct order");
            
            System.out.println("Test passed - BUBBLE(50) doesn't swap with correctly ordered left(30)\n");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to randomly pick direction when both neighbors need swapping")
        void randomDirectionSelectionWithSeededRandom() {
            // PURPOSE: Verify BUBBLE randomly picks direction when both neighbors need swapping
            // INPUTS: Cell(value=50) with left(70) and right(30), seeded Random
            // EXPECTED OUTPUTS: Deterministic direction selection based on seed
            // CONSOLE OUTPUT: Test passed - BUBBLE randomly picked {direction} with seed
            
            System.out.println("=== Testing BUBBLE Random Direction Selection ===");
            Random seededRandom = new Random(42); // Deterministic seed
            BubbleSortingCell cell = new BubbleSortingCell(50, 5, seededRandom);
            
            BubbleSortingCell leftNeighbor = new BubbleSortingCell(70, 4);
            BubbleSortingCell rightNeighbor = new BubbleSortingCell(30, 6);
            
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(leftNeighbor, rightNeighbor);
            List<Integer> positions = List.of(4, 6);
            
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(
                cell, 5, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            System.out.println("Cell value: " + cell.readValue());
            System.out.println("Left neighbor value: " + leftNeighbor.readValue());
            System.out.println("Right neighbor value: " + rightNeighbor.readValue());
            System.out.println("Target position: " + target.orElse(-1));
            
            assertTrue(target.isPresent(), "Should have target position");
            assertTrue(target.get() == 4 || target.get() == 6, 
                "Should target either left (4) or right (6) neighbor");
            
            String direction = target.get() == 4 ? "left" : "right";
            System.out.println("Test passed - BUBBLE randomly picked " + direction + " with seed 42\n");
        }
    }
}
