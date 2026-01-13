package com.emergent.doom.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for InsertionSortingCell.
 *
 * <p><strong>PURPOSE:</strong> This narrative explores INSERTION cells' conservative strategy: waiting for left-sorted prefixes
 * before shifting into position, simulating insertion sort's build-from-left progression. End-user stories highlight initialization,
 * boundary checks, and precise leftward targeting.</p>
 */
class InsertionSortingCellTest {
    
    private InsertionSortingCell cell;
    
    @BeforeEach
    void setUp() {
        // Shared setup: Initialize a base cell for reuse in movement and target tests.
        cell = new InsertionSortingCell(30, 3);
    }
    
    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("As a user I want to create INSERTION cells so that I can initialize the sorting array")
        void createInsertionCell() {
            // PURPOSE: Confirm construction sets up INSERTION identity, ready for left-prefix integration in array building.
            // INPUTS: value=42, position=5
            // EXPECTED OUTPUTS: Cell with INSERTION algotype, value, and position intact.
            // REPRODUCTION: Instantiate and verify core properties.
            
            InsertionSortingCell constructedCell = new InsertionSortingCell(42, 5);
            
            assertEquals(42, constructedCell.readValue(), "Value should match input");
            assertEquals(SortingAlgotype.INSERTION, constructedCell.readAlgotype(), "Algotype should be INSERTION");
            assertEquals(5, constructedCell.readCurrentPosition(), "Position should match input");
        }
    }
    
    @Nested
    @DisplayName("Movement Decision Tests")
    class MovementDecisionTests {
        
        private List<AbstractCell<Integer, SortingAlgotype>> sortedLeftNeighbors;
        private List<AbstractCell<Integer, SortingAlgotype>> unsortedLeftNeighbors;
        private List<Integer> leftPositions;
        private NeighborhoodView<Integer, SortingAlgotype> sortedView;
        private NeighborhoodView<Integer, SortingAlgotype> unsortedView;
        private NeighborhoodView<Integer, SortingAlgotype> boundaryView;
        
        @BeforeEach
        void setUpNeighborhoods() {
            // Shared helper: Prepare sorted/unsorted left neighborhoods and boundary view to streamline decision testing.
            
            // Sorted left: [10, 20, 40]
            sortedLeftNeighbors = List.of(
                new InsertionSortingCell(10, 0),
                new InsertionSortingCell(20, 1),
                new InsertionSortingCell(40, 2)
            );
            leftPositions = List.of(0, 1, 2);
            sortedView = new NeighborhoodView<>(cell, 3, 10, sortedLeftNeighbors, leftPositions);
            
            // Unsorted left: [20, 10, 40]
            unsortedLeftNeighbors = List.of(
                new InsertionSortingCell(20, 0),
                new InsertionSortingCell(10, 1),
                new InsertionSortingCell(40, 2)
            );
            unsortedView = new NeighborhoodView<>(cell, 3, 10, unsortedLeftNeighbors, leftPositions);
            
            // Boundary (no left)
            boundaryView = new NeighborhoodView<>(new InsertionSortingCell(42, 0), 0, 10, List.of(), List.of());
        }
        
        @Test
        @DisplayName("As a user I want INSERTION cells to move when left is sorted and value is smaller")
        void shouldMoveWhenLeftSortedAndSmallerValue() {
            // PURPOSE: Illustrate activation when left prefix is ordered and cell value fits insertion point, advancing the sort front.
            // INPUTS: Cell(value=30, position=3) with sorted left: [10, 20, 40]
            // EXPECTED OUTPUTS: shouldMoveGiven() returns true
            // REPRODUCTION: Use pre-built sortedView; assert movement for smaller value.
            
            boolean shouldMove = cell.shouldMoveGiven(sortedView);
            
            assertTrue(shouldMove, "INSERTION should move into sorted left when value is smaller");
        }
        
        @Test
        @DisplayName("As a user I want INSERTION cells to not move when left is unsorted")
        void shouldNotMoveWhenLeftUnsorted() {
            // PURPOSE: Enforce patience: no movement until left stabilizes, preventing premature shifts in building the sorted prefix.
            // INPUTS: Cell(value=30, position=3) with unsorted left: [20, 10, 40]
            // EXPECTED OUTPUTS: shouldMoveGiven() returns false
            // REPRODUCTION: Use pre-built unsortedView; assert no movement.
            
            boolean shouldMove = cell.shouldMoveGiven(unsortedView);
            
            assertFalse(shouldMove, "INSERTION should wait until left is sorted");
        }
        
        @Test
        @DisplayName("As a user I want INSERTION cells to not move when at left boundary")
        void shouldNotMoveAtLeftBoundary() {
            // PURPOSE: Boundary cells anchor the sort; no leftward movement possible, maintaining array integrity.
            // INPUTS: Cell at position 0 (no left neighbors)
            // EXPECTED OUTPUTS: shouldMoveGiven() returns false
            // REPRODUCTION: Use pre-built boundaryView; assert immobility.
            
            InsertionSortingCell boundaryCell = new InsertionSortingCell(42, 0);
            boolean shouldMove = boundaryCell.shouldMoveGiven(boundaryView);
            
            assertFalse(shouldMove, "INSERTION at left boundary remains stationary");
        }
    }
    
    @Nested
    @DisplayName("Target Position Calculation Tests")
    class TargetPositionTests {
        
        private List<AbstractCell<Integer, SortingAlgotype>> sortedLeftForTarget;
        private List<Integer> leftPositionsForTarget;
        private NeighborhoodView<Integer, SortingAlgotype> sortedTargetView;
        private List<AbstractCell<Integer, SortingAlgotype>> unsortedLeftForTarget;
        private NeighborhoodView<Integer, SortingAlgotype> unsortedTargetView;
        
        @BeforeEach
        void setUpTargetNeighborhoods() {
            // Shared helper: Configure neighborhoods for target computation, focusing on left insertion points.
            
            InsertionSortingCell testCell = new InsertionSortingCell(25, 3);
            
            // Sorted left for valid target: [10, 20, 30]
            sortedLeftForTarget = List.of(
                new InsertionSortingCell(10, 0),
                new InsertionSortingCell(20, 1),
                new InsertionSortingCell(30, 2)
            );
            leftPositionsForTarget = List.of(0, 1, 2);
            sortedTargetView = new NeighborhoodView<>(testCell, 3, 10, sortedLeftForTarget, leftPositionsForTarget);
            
            // Unsorted for no target
            unsortedLeftForTarget = List.of(
                new InsertionSortingCell(20, 0),
                new InsertionSortingCell(10, 1),
                new InsertionSortingCell(30, 2)
            );
            unsortedTargetView = new NeighborhoodView<>(testCell, 3, 10, unsortedLeftForTarget, leftPositionsForTarget);
        }
        
        @Test
        @DisplayName("As a user I want INSERTION cells to swap left when left sorted and value smaller")
        void swapLeftWhenLeftSortedAndSmaller() {
            // PURPOSE: Pinpoint insertion: target the immediate left when prefix is ready and value requires shift.
            // INPUTS: Cell(value=25, position=3) with sorted left: [10, 20, 30]
            // EXPECTED OUTPUTS: Target position is 2 (adjacent left)
            // REPRODUCTION: Use sortedTargetView; assert target at 2.
            
            Optional<Integer> target = new InsertionSortingCell(25, 3).calculateTargetPositionGiven(sortedTargetView);
            
            assertTrue(target.isPresent(), "Valid sorted left yields insertion target");
            assertEquals(2, target.get(), "Target immediate left neighbor for shift");
        }
        
        @Test
        @DisplayName("As a user I want INSERTION cells to return empty when left unsorted")
        void noSwapWhenLeftUnsorted() {
            // PURPOSE: Defer action until prefix readiness: no target until left order is confirmed.
            // INPUTS: Cell(value=25, position=3) with unsorted left: [20, 10, 30]
            // EXPECTED OUTPUTS: Empty target
            // REPRODUCTION: Use unsortedTargetView; assert no target.
            
            Optional<Integer> target = new InsertionSortingCell(25, 3).calculateTargetPositionGiven(unsortedTargetView);
            
            assertFalse(target.isPresent(), "Unsorted left yields no insertion target");
        }
    }
}
