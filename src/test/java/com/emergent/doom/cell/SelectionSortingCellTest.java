package com.emergent.doom.cell;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SelectionSortingCell.
 *
 * <strong>PURPOSE:</strong> This story follows SELECTION cells' pursuit of ideal positions: starting with targeted construction,
 * managing incremental adjustments for denied swaps, and deciding movement toward convergence. End-user narratives emphasize
 * configuration flexibility and adaptive targeting in selection sort dynamics.
 */
class SelectionSortingCellTest {
    
    private SelectionSortingCell cell;
    
    @BeforeEach
    void setUp() {
        // Shared setup: Base cell with initial ideal position 0 for convergence tests.
        cell = new SelectionSortingCell(42, 5, 0);
    }
    
    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("As a user I want to create SELECTION cells with initial ideal position so that I can configure sort direction")
        void createWithInitialIdealPosition() {
            // PURPOSE: Establish SELECTION cells with predefined targets, setting the stage for position-driven convergence.
            // INPUTS: value=42, position=5, idealPosition=0
            // EXPECTED OUTPUTS: Cell with SELECTION algotype and exact ideal position.
            // REPRODUCTION: Instantiate and verify all initial properties.
            
            SelectionSortingCell constructedCell = new SelectionSortingCell(42, 5, 0);
            
            assertEquals(42, constructedCell.readValue(), "Value should match input");
            assertEquals(SortingAlgotype.SELECTION, constructedCell.readAlgotype(), "Algotype should be SELECTION");
            assertEquals(5, constructedCell.readCurrentPosition(), "Current position should match input");
            assertEquals(0, constructedCell.getIdealPosition(), "Ideal position should be 0");
        }
    }
    
    @Nested
    @DisplayName("Ideal Position Management Tests")
    class IdealPositionTests {
        
        @Test
        @DisplayName("As a user I want to increment ideal position so that SELECTION cells adjust when swaps are denied")
        void incrementIdealPosition() {
            // PURPOSE: Simulate convergence adaptation: repeated denials shift the target rightward, exploring viable slots.
            // INPUTS: Cell with ideal position 0
            // EXPECTED OUTPUTS: Sequential increments: 1, 2, 3
            // REPRODUCTION: Call increment three times; assert final value and returns.
            
            SelectionSortingCell incrementCell = new SelectionSortingCell(42, 5, 0);
            
            int initial = incrementCell.getIdealPosition();
            int after1 = incrementCell.incrementIdealPosition();
            int after2 = incrementCell.incrementIdealPosition();
            int after3 = incrementCell.incrementIdealPosition();
            int finalIdeal = incrementCell.getIdealPosition();
            
            assertEquals(0, initial, "Initial ideal should be 0");
            assertEquals(1, after1, "First increment returns and sets 1");
            assertEquals(2, after2, "Second increment returns and sets 2");
            assertEquals(3, after3, "Third increment returns and sets 3");
            assertEquals(3, finalIdeal, "Final getter confirms 3");
        }
        
        @Test
        @DisplayName("As a user I want to set ideal position so that I can reset SELECTION cells during group merge")
        void setIdealPosition() {
            // PURPOSE: Enable dynamic reconfiguration, such as merging sorted subgroups by resetting targets.
            // INPUTS: Cell, set ideal to 10
            // EXPECTED OUTPUTS: Ideal position updated to 10
            // REPRODUCTION: Set and verify via getter.
            
            SelectionSortingCell setCell = new SelectionSortingCell(42, 5);
            setCell.setIdealPosition(10);
            int ideal = setCell.getIdealPosition();
            
            assertEquals(10, ideal, "Ideal should be set to 10");
        }
    }
    
    @Nested
    @DisplayName("Movement Decision Tests")
    class MovementDecisionTests {
        
        private NeighborhoodView<Integer, SortingAlgotype> emptyViewNotAtIdeal;
        private NeighborhoodView<Integer, SortingAlgotype> emptyViewAtIdeal;
        
        @BeforeEach
        void setUpViews() {
            // Shared helper: Empty views for SELECTION (neighbors irrelevant); one off-ideal, one at-ideal.
            emptyViewNotAtIdeal = new NeighborhoodView<>(new SelectionSortingCell(42, 5, 0), 5, 10, List.of(), List.of());
            emptyViewAtIdeal = new NeighborhoodView<>(new SelectionSortingCell(42, 0, 0), 0, 10, List.of(), List.of());
        }
        
        @Test
        @DisplayName("As a user I want SELECTION cells to move when not at ideal position so that they converge")
        void shouldMoveWhenNotAtIdeal() {
            // PURPOSE: Drive progression: cells distant from ideal activate to close the gap toward sorted order.
            // INPUTS: Cell at position 5, ideal 0
            // EXPECTED OUTPUTS: shouldMoveGiven() returns true
            // REPRODUCTION: Use emptyViewNotAtIdeal; assert movement.
            
            SelectionSortingCell offIdealCell = new SelectionSortingCell(42, 5, 0);
            boolean shouldMove = offIdealCell.shouldMoveGiven(emptyViewNotAtIdeal);
            
            assertTrue(shouldMove, "SELECTION should move when not at ideal");
        }
        
        @Test
        @DisplayName("As a user I want SELECTION cells to not move when at ideal position so that they don't waste cycles")
        void shouldNotMoveWhenAtIdeal() {
            // PURPOSE: Achieve stability: cells at target halt, signaling local convergence.
            // INPUTS: Cell at position 0, ideal 0
            // EXPECTED OUTPUTS: shouldMoveGiven() returns false
            // REPRODUCTION: Use emptyViewAtIdeal; assert no movement.
            
            SelectionSortingCell atIdealCell = new SelectionSortingCell(42, 0, 0);
            boolean shouldMove = atIdealCell.shouldMoveGiven(emptyViewAtIdeal);
            
            assertFalse(shouldMove, "SELECTION should not move when at ideal");
        }
    }
    
    @Nested
    @DisplayName("Target Position Calculation Tests")
    class TargetPositionTests {
        
        private SelectionSortingCell smallerCell;
        private SelectionSortingCell largerCell;
        private SelectionSortingCell targetCell;
        private List<AbstractCell<Integer, SortingAlgotype>> neighborList;
        private List<Integer> positionList;
        private NeighborhoodView<Integer, SortingAlgotype> swapView;
        private NeighborhoodView<Integer, SortingAlgotype> denyView;
        
        @BeforeEach
        void setUpTargets() {
            // Shared helper: Cells and views for swap/deny scenarios at ideal position 0.
            smallerCell = new SelectionSortingCell(30, 5, 0);
            largerCell = new SelectionSortingCell(70, 5, 0);
            targetCell = new SelectionSortingCell(50, 0);
            neighborList = List.of(targetCell);
            positionList = List.of(0);
            swapView = new NeighborhoodView<>(smallerCell, 5, 10, neighborList, positionList);
            denyView = new NeighborhoodView<>(largerCell, 5, 10, neighborList, positionList);
        }
        
        @Test
        @DisplayName("As a user I want SELECTION cells to swap with ideal position when value is smaller")
        void swapWithIdealWhenSmallerValue() {
            // PURPOSE: Facilitate minimum placement: smaller value claims ideal slot via direct swap.
            // INPUTS: Cell(value=30, position=5, ideal=0) with target(value=50, position=0)
            // EXPECTED OUTPUTS: Target position is 0
            // REPRODUCTION: Use swapView; assert target at ideal.
            
            Optional<Integer> targetPos = smallerCell.calculateTargetPositionGiven(swapView);
            
            assertTrue(targetPos.isPresent(), "Smaller value targets ideal for swap");
            assertEquals(0, targetPos.get(), "Target is ideal position 0");
        }
        
        @Test
        @DisplayName("As a user I want SELECTION cells to increment ideal position when swap is denied")
        void incrementIdealWhenSwapDenied() {
            // PURPOSE: Adapt to rejection: larger value skips current ideal, incrementing target for next candidate.
            // INPUTS: Cell(value=70, position=5, ideal=0) with target(value=50, position=0)
            // EXPECTED OUTPUTS: Empty target, ideal incremented to 1
            // REPRODUCTION: Use denyView; check pre/post ideal and absent target.
            
            int idealBefore = largerCell.getIdealPosition();
            Optional<Integer> targetPos = largerCell.calculateTargetPositionGiven(denyView);
            int idealAfter = largerCell.getIdealPosition();
            
            assertFalse(targetPos.isPresent(), "Larger value denied swap at ideal");
            assertEquals(0, idealBefore, "Ideal starts at 0");
            assertEquals(1, idealAfter, "Ideal increments to 1 post-denial");
        }
    }
}
