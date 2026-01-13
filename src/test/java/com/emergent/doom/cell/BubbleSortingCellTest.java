package com.emergent.doom.cell;

import org.junit.jupiter.api.BeforeEach;
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
 * <p><strong>PURPOSE:</strong> This suite narrates the life of a BUBBLE cell: from creation with deterministic randomness,
 * through immutable identity, to local movement decisions and bidirectional swaps based on neighbor comparisons.
 * Tests frame end-user scenarios, ensuring reliable emergent behavior in sorting arrays.</p>
 */
class BubbleSortingCellTest {
    
    private Random seededRandom;
    
    @BeforeEach
    void setUp() {
        // Shared setup: Initialize seeded Random for deterministic testing across methods
        seededRandom = new Random(12345);
    }
    
    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("As a user I want to create BUBBLE cells with seeded Random so that I can test deterministically")
        void createWithSeededRandom() {
            // PURPOSE: Verify BubbleSortingCell construction with seeded Random ensures reproducible randomness in movement.
            // INPUTS: value=42, position=5, seed=12345
            // EXPECTED OUTPUTS: Cell with BUBBLE algotype and deterministic random behavior
            // REPRODUCTION: Construct cell and verify value, algotype, position remain unchanged.
            
            BubbleSortingCell cell = new BubbleSortingCell(42, 5, seededRandom);
            
            assertEquals(42, cell.readValue(), "Value should be 42");
            assertEquals(SortingAlgotype.BUBBLE, cell.readAlgotype(), "Algotype should be BUBBLE");
            assertEquals(5, cell.readCurrentPosition(), "Position should be 5");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cell algotype to be immutably BUBBLE so that behavioral identity is stable")
        void algotypeIsAlwaysBubble() {
            // PURPOSE: Confirm algotype immutability guarantees consistent bubble-like local swaps throughout the cell's lifecycle.
            // INPUTS: BubbleSortingCell
            // EXPECTED OUTPUTS: readAlgotype() always returns BUBBLE, unchangeable post-construction.
            // REPRODUCTION: Read algotype multiple times to verify stability.
            
            BubbleSortingCell cell = new BubbleSortingCell(42, 0);
            
            SortingAlgotype algotype = cell.readAlgotype();
            
            assertEquals(SortingAlgotype.BUBBLE, algotype, "Algotype should be BUBBLE");
        }
    }
    
    @Nested
    @DisplayName("Movement Decision Tests")
    class MovementDecisionTests {
        
        private BubbleSortingCell cell;
        private NeighborhoodView<Integer, SortingAlgotype> viewWithNeighbor;
        private NeighborhoodView<Integer, SortingAlgotype> emptyView;
        
        @BeforeEach
        void setUpNeighborhoods() {
            // Shared helper: Prepare common neighborhood views to reduce boilerplate in movement tests.
            cell = new BubbleSortingCell(42, 5);
            
            // View with one neighbor
            BubbleSortingCell neighbor = new BubbleSortingCell(30, 4);
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(neighbor);
            List<Integer> positions = List.of(4);
            viewWithNeighbor = new NeighborhoodView<>(cell, 5, 10, neighbors, positions);
            
            // Empty view
            List<AbstractCell<Integer, SortingAlgotype>> emptyNeighbors = new ArrayList<>();
            List<Integer> emptyPositions = new ArrayList<>();
            emptyView = new NeighborhoodView<>(new BubbleSortingCell(42, 0), 0, 1, emptyNeighbors, emptyPositions);
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to move when neighbors exist so that sorting progresses")
        void shouldMoveWhenNeighborsExist() {
            // PURPOSE: Ensure BUBBLE cells activate movement in populated neighborhoods, enabling local bubble propagation.
            // INPUTS: Cell with neighbors
            // EXPECTED OUTPUTS: shouldMoveGiven() returns true when neighbor count > 0
            // REPRODUCTION: Use pre-built viewWithNeighbor and assert movement intent.
            
            boolean shouldMove = cell.shouldMoveGiven(viewWithNeighbor);
            
            assertTrue(shouldMove, "BUBBLE should want to move when neighbors exist");
            assertEquals(1, viewWithNeighbor.getNeighborCount(), "Neighborhood has one neighbor");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to not move when isolated so that they don't waste cycles")
        void shouldNotMoveWhenNoNeighbors() {
            // PURPOSE: Confirm isolated BUBBLE cells remain passive, conserving computation in sparse arrays.
            // INPUTS: Cell with empty neighborhood
            // EXPECTED OUTPUTS: shouldMoveGiven() returns false when neighbor count == 0
            // REPRODUCTION: Use pre-built emptyView and assert no movement.
            
            BubbleSortingCell isolatedCell = new BubbleSortingCell(42, 0);
            boolean shouldMove = isolatedCell.shouldMoveGiven(emptyView);
            
            assertFalse(shouldMove, "BUBBLE should not want to move when isolated");
            assertEquals(0, emptyView.getNeighborCount(), "Neighborhood is empty");
        }
    }
    
    @Nested
    @DisplayName("Target Position Calculation Tests")
    class TargetPositionTests {
        
        private BubbleSortingCell cell;
        
        @BeforeEach
        void setUpCell() {
            // Shared helper: Initialize cell for target calculations, reusing seeded random.
            cell = new BubbleSortingCell(30, 5);
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to swap left when value is smaller than left neighbor")
        void swapLeftWhenSmallerThanLeft() {
            // PURPOSE: Validate leftward bubble in ascending sort: smaller value targets left neighbor for inversion fix.
            // INPUTS: Cell(value=30) with left neighbor(value=50)
            // EXPECTED OUTPUTS: Target position is left neighbor's position (4)
            // REPRODUCTION: Build view and assert target presence and value.
            
            BubbleSortingCell leftNeighbor = new BubbleSortingCell(50, 4);
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(leftNeighbor);
            List<Integer> positions = List.of(4);
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(cell, 5, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            assertTrue(target.isPresent(), "Should have target position");
            assertEquals(4, target.get(), "Should target left neighbor position");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to swap right when value is larger than right neighbor")
        void swapRightWhenLargerThanRight() {
            // PURPOSE: Confirm rightward bubble: larger value targets right neighbor to resolve local disorder.
            // INPUTS: Cell(value=70) with right neighbor(value=50)
            // EXPECTED OUTPUTS: Target position is right neighbor's position (6)
            // REPRODUCTION: Build view and assert target presence and value.
            
            cell = new BubbleSortingCell(70, 5, seededRandom); // Re-init for different value
            BubbleSortingCell rightNeighbor = new BubbleSortingCell(50, 6);
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(rightNeighbor);
            List<Integer> positions = List.of(6);
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(cell, 5, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            assertTrue(target.isPresent(), "Should have target position");
            assertEquals(6, target.get(), "Should target right neighbor position");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to not swap when already in correct order")
        void noSwapWhenInCorrectOrder() {
            // PURPOSE: Ensure no unnecessary movement when local order is maintained, preserving stability.
            // INPUTS: Cell(value=50) with left neighbor(value=30)
            // EXPECTED OUTPUTS: Empty target (no swap needed)
            // REPRODUCTION: Build view and assert absent target.
            
            cell = new BubbleSortingCell(50, 5, seededRandom); // Re-init for value=50
            BubbleSortingCell leftNeighbor = new BubbleSortingCell(30, 4);
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(leftNeighbor);
            List<Integer> positions = List.of(4);
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(cell, 5, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            assertFalse(target.isPresent(), "Should not have target when in correct order");
        }
        
        @Test
        @DisplayName("As a user I want BUBBLE cells to randomly pick direction when both neighbors need swapping")
        void randomDirectionSelectionWithSeededRandom() {
            // PURPOSE: Demonstrate seeded randomness ensures reproducible choice in dual-disorder scenarios, balancing exploration.
            // INPUTS: Cell(value=50) with left(70) and right(30), seeded Random(42)
            // EXPECTED OUTPUTS: Target is either left (4) or right (6), deterministic per seed
            // REPRODUCTION: Use seed 42; expected target=6 (right, based on Random.nextBoolean()).
            
            Random testRandom = new Random(42); // Specific seed for this test
            cell = new BubbleSortingCell(50, 5, testRandom);
            BubbleSortingCell leftNeighbor = new BubbleSortingCell(70, 4);
            BubbleSortingCell rightNeighbor = new BubbleSortingCell(30, 6);
            List<AbstractCell<Integer, SortingAlgotype>> neighbors = List.of(leftNeighbor, rightNeighbor);
            List<Integer> positions = List.of(4, 6);
            NeighborhoodView<Integer, SortingAlgotype> view = new NeighborhoodView<>(cell, 5, 10, neighbors, positions);
            
            Optional<Integer> target = cell.calculateTargetPositionGiven(view);
            
            assertTrue(target.isPresent(), "Should have target position");
            assertTrue(target.get() == 4 || target.get() == 6, "Should target either left (4) or right (6) neighbor");
        }
    }
}
