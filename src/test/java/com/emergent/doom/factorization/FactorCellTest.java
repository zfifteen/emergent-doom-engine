package com.emergent.doom.factorization;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.cell.NeighborhoodView;
import com.emergent.doom.group.CellStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for FactorCell.
 *
 * <p><strong>PURPOSE:</strong> Validate Step 1 success criteria:
 * <ul>
 *   <li>All classes compile</li>
 *   <li>Fitness function validated (remainder=0 yields 1.0)</li>
 *   <li>Cell comparisons work correctly (ordered by fitness score)</li>
 *   <li>Swap eligibility and behavioral policies work as designed</li>
 * </ul>
 */
@DisplayName("FactorCell Tests")
class FactorCellTest {
    
    private static final int TARGET_N = 143;  // 11 × 13
    
    @Nested
    @DisplayName("Construction and Validation")
    class ConstructionTests {
        
        @Test
        @DisplayName("Valid FactorCell construction succeeds")
        void validConstruction() {
            // GIVEN: Valid candidate and target
            int candidate = 11;
            int target = 143;
            FactorStrategy strategy = FactorStrategy.SMALL_PRIMES;
            int position = 0;
            
            // WHEN: Creating FactorCell
            FactorCell cell = new FactorCell(candidate, target, strategy, position);
            
            // THEN: Cell initialized correctly
            assertEquals(candidate, cell.readValue());
            assertEquals(strategy, cell.readAlgotype());
            assertEquals(position, cell.readCurrentPosition());
            assertEquals(CellStatus.ACTIVE, cell.readStatus());
        }
        
        @Test
        @DisplayName("Candidate <= 1 throws IllegalArgumentException")
        void candidateTooSmall() {
            // GIVEN: Invalid candidate (0 or 1)
            int candidate = 1;
            int target = 143;
            
            // WHEN/THEN: Construction throws
            assertThrows(IllegalArgumentException.class, () -> {
                new FactorCell(candidate, target, FactorStrategy.SMALL_PRIMES, 0);
            });
        }
        
        @Test
        @DisplayName("Candidate >= target throws IllegalArgumentException")
        void candidateTooLarge() {
            // GIVEN: Candidate equal to target
            int candidate = 143;
            int target = 143;
            
            // WHEN/THEN: Construction throws
            assertThrows(IllegalArgumentException.class, () -> {
                new FactorCell(candidate, target, FactorStrategy.SMALL_PRIMES, 0);
            });
        }
    }
    
    @Nested
    @DisplayName("Fitness Computation")
    class FitnessTests {
        
        @Test
        @DisplayName("Perfect factor (11) has fitness 1.0")
        void perfectFactorFitness() {
            // GIVEN: True factor of 143
            FactorCell cell = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            
            // WHEN: Checking fitness
            double fitness = cell.getFitness();
            
            // THEN: Fitness is exactly 1.0
            assertEquals(1.0, fitness, 0.0001);
            assertTrue(cell.isPerfectFactor());
        }
        
        @Test
        @DisplayName("Perfect factor (13) has fitness 1.0")
        void perfectFactorFitness13() {
            // GIVEN: Other true factor of 143
            // NOTE: 13 > sqrt(143) ≈ 11.96, so normally excluded from candidates
            // But we can construct it directly for testing
            FactorCell cell = new FactorCell(13, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            
            // WHEN: Checking fitness
            double fitness = cell.getFitness();
            
            // THEN: Fitness is exactly 1.0
            assertEquals(1.0, fitness, 0.0001);
            assertTrue(cell.isPerfectFactor());
        }
        
        @Test
        @DisplayName("Non-factor has fitness < 1.0")
        void nonFactorFitness() {
            // GIVEN: Non-factor candidate
            FactorCell cell = new FactorCell(7, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 0);
            
            // WHEN: Checking fitness
            // 143 % 7 = 3 (remainder)
            // distanceToMultiple = min(3, 7-3) = min(3, 4) = 3
            // fitness = 1.0 - (3/7) ≈ 0.571
            double fitness = cell.getFitness();
            
            // THEN: Fitness is less than 1.0
            assertTrue(fitness < 1.0);
            assertFalse(cell.isPerfectFactor());
            assertEquals(0.571, fitness, 0.01);  // Allow small tolerance
        }
        
        @Test
        @DisplayName("Fitness decreases with distance from factors")
        void fitnessOrdering() {
            // GIVEN: Candidates at different distances from factors
            FactorCell factor11 = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            FactorCell near11 = new FactorCell(10, TARGET_N, FactorStrategy.FERMAT_NEAR_SQRT, 1);
            FactorCell far = new FactorCell(2, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 2);
            
            // WHEN: Comparing fitness
            double fitnessFactor = factor11.getFitness();
            double fitnessNear = near11.getFitness();
            double fitnessFar = far.getFitness();
            
            // THEN: Perfect factor > near factor > far from factors
            assertTrue(fitnessFactor > fitnessNear);
            assertTrue(fitnessNear > fitnessFar);
        }
    }
    
    @Nested
    @DisplayName("Comparison Logic")
    class ComparisonTests {
        
        @Test
        @DisplayName("Cells compare by FITNESS, not candidate value")
        void comparisonByFitness() {
            // GIVEN: Cell with higher candidate but lower fitness
            FactorCell lowCandidate = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            FactorCell highCandidate = new FactorCell(12, TARGET_N, FactorStrategy.FERMAT_NEAR_SQRT, 1);
            
            // Fitness: 11 is perfect factor (1.0), 12 is not
            // So lowCandidate has HIGHER fitness despite LOWER value
            
            // WHEN: Comparing cells
            int comparison = lowCandidate.compareTo(highCandidate);
            
            // THEN: Cell with higher fitness compares as "less" (sorts to front)
            assertTrue(comparison < 0, "Higher fitness cell should sort to front");
        }
        
        @Test
        @DisplayName("hasGreaterValueThan checks fitness, not value")
        void hasGreaterValueThanByFitness() {
            // GIVEN: Same cells as above
            FactorCell perfectFactor = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            FactorCell nonFactor = new FactorCell(12, TARGET_N, FactorStrategy.FERMAT_NEAR_SQRT, 1);
            
            // WHEN: Checking hasGreaterValueThan
            boolean perfectGreater = perfectFactor.hasGreaterValueThan(nonFactor);
            boolean nonFactorGreater = nonFactor.hasGreaterValueThan(perfectFactor);
            
            // THEN: Perfect factor has "greater value" (higher fitness)
            assertTrue(perfectGreater);
            assertFalse(nonFactorGreater);
        }
        
        @Test
        @DisplayName("Cells with same fitness are equal in comparison")
        void equalFitness() {
            // GIVEN: Two different candidates with coincidentally same fitness
            // This is rare but possible - we'll construct two cells with same remainder
            FactorCell cell1 = new FactorCell(5, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 0);
            FactorCell cell2 = new FactorCell(5, TARGET_N, FactorStrategy.SMALL_PRIMES, 1);
            
            // WHEN: Comparing
            int comparison = cell1.compareTo(cell2);
            
            // THEN: Equal (comparison = 0)
            assertEquals(0, comparison);
        }
    }
    
    @Nested
    @DisplayName("Behavioral Policies")
    class BehavioralTests {
        
        @Test
        @DisplayName("ACTIVE cells should always want to move")
        void activeWantsToMove() {
            // GIVEN: Active cell
            FactorCell cell = new FactorCell(7, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 0);
            cell.updateStatusTo(CellStatus.ACTIVE);
            
            // WHEN: Checking shouldMoveGiven (neighbors irrelevant for current policy)
            boolean shouldMove = cell.shouldMoveGiven(null);
            
            // THEN: Always wants to move if active
            assertTrue(shouldMove);
        }
        
        @Test
        @DisplayName("FREEZE cells should not want to move")
        void freezeDoesNotMove() {
            // GIVEN: Frozen cell
            FactorCell cell = new FactorCell(7, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 0);
            cell.updateStatusTo(CellStatus.FREEZE);
            
            // WHEN: Checking shouldMoveGiven
            boolean shouldMove = cell.shouldMoveGiven(null);
            
            // THEN: Does not want to move
            assertFalse(shouldMove);
        }
        
        @Test
        @DisplayName("Cell swaps left with lower-fitness neighbor")
        void swapsLeftWhenBeneficial() {
            // GIVEN: Cell with higher fitness than left neighbor
            FactorCell highFitness = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 1);
            FactorCell lowFitness = new FactorCell(7, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 0);
            
            List<AbstractCell<Integer, FactorStrategy>> array = new ArrayList<>();
            array.add(lowFitness);   // Position 0
            array.add(highFitness);  // Position 1
            
            // Build neighborhood view
            NeighborhoodView<Integer, FactorStrategy> neighbors = 
                new NeighborhoodView<>(highFitness, 1, 2, array, List.of(0, 1));
            
            // WHEN: Calculating target position
            Optional<Integer> target = highFitness.calculateTargetPositionGiven(neighbors);
            
            // THEN: Wants to swap left
            assertTrue(target.isPresent());
            assertEquals(0, target.get(), "Should target left neighbor's position");
        }
        
        @Test
        @DisplayName("Cell swaps right with higher-fitness neighbor")
        void swapsRightWhenBeneficial() {
            // GIVEN: Cell with lower fitness than right neighbor
            FactorCell lowFitness = new FactorCell(7, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 0);
            FactorCell highFitness = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 1);
            
            List<AbstractCell<Integer, FactorStrategy>> array = new ArrayList<>();
            array.add(lowFitness);   // Position 0
            array.add(highFitness);  // Position 1
            
            // Build neighborhood view
            NeighborhoodView<Integer, FactorStrategy> neighbors = 
                new NeighborhoodView<>(lowFitness, 0, 2, array, List.of(0, 1));
            
            // WHEN: Calculating target position
            Optional<Integer> target = lowFitness.calculateTargetPositionGiven(neighbors);
            
            // THEN: Wants to swap right
            assertTrue(target.isPresent());
            assertEquals(1, target.get(), "Should target right neighbor's position");
        }
        
        @Test
        @DisplayName("Cell returns empty when no beneficial swap exists")
        void noBeneficialSwap() {
            // GIVEN: Cell already at front with highest fitness
            FactorCell highFitness = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            FactorCell lowRight = new FactorCell(7, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 1);
            
            List<AbstractCell<Integer, FactorStrategy>> array = new ArrayList<>();
            array.add(highFitness);  // Position 0
            array.add(lowRight);     // Position 1
            
            // Build neighborhood view - only sees right neighbor
            NeighborhoodView<Integer, FactorStrategy> neighbors = 
                new NeighborhoodView<>(highFitness, 0, 2, array, List.of(0, 1));
            
            // WHEN: Calculating target position
            Optional<Integer> target = highFitness.calculateTargetPositionGiven(neighbors);
            
            // THEN: No beneficial swap (already at front with highest fitness)
            assertFalse(target.isPresent());
        }
    }
    
    @Nested
    @DisplayName("Swap Eligibility")
    class SwapEligibilityTests {
        
        @Test
        @DisplayName("ACTIVE cells can initiate swaps")
        void activeCanInitiate() {
            FactorCell cell = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            cell.updateStatusTo(CellStatus.ACTIVE);
            
            assertTrue(cell.canInitiateSwap());
        }
        
        @Test
        @DisplayName("FREEZE cells cannot initiate swaps")
        void freezeCannotInitiate() {
            FactorCell cell = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            cell.updateStatusTo(CellStatus.FREEZE);
            
            assertFalse(cell.canInitiateSwap());
        }
        
        @Test
        @DisplayName("ACTIVE cells can accept swaps")
        void activeCanAccept() {
            FactorCell cell = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            FactorCell initiator = new FactorCell(7, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 1);
            
            cell.updateStatusTo(CellStatus.ACTIVE);
            
            assertTrue(cell.canAcceptSwapFrom(initiator));
        }
        
        @Test
        @DisplayName("FREEZE cells can accept swaps (one-way asymmetry)")
        void freezeCanAccept() {
            FactorCell cell = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            FactorCell initiator = new FactorCell(7, TARGET_N, FactorStrategy.RANDOM_SAMPLE, 1);
            
            cell.updateStatusTo(CellStatus.FREEZE);
            
            // FREEZE cannot initiate
            assertFalse(cell.canInitiateSwap());
            
            // But FREEZE can accept (Levin-style asymmetry)
            assertTrue(cell.canAcceptSwapFrom(initiator));
        }
    }
    
    @Nested
    @DisplayName("Position and Status Updates")
    class MutabilityTests {
        
        @Test
        @DisplayName("Position can be updated")
        void positionUpdate() {
            FactorCell cell = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            
            cell.updatePositionTo(5);
            assertEquals(5, cell.readCurrentPosition());
            
            cell.updatePositionTo(10);
            assertEquals(10, cell.readCurrentPosition());
        }
        
        @Test
        @DisplayName("Negative position throws IllegalArgumentException")
        void negativePositionThrows() {
            FactorCell cell = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            
            assertThrows(IllegalArgumentException.class, () -> {
                cell.updatePositionTo(-1);
            });
        }
        
        @Test
        @DisplayName("Status can be updated")
        void statusUpdate() {
            FactorCell cell = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            
            assertEquals(CellStatus.ACTIVE, cell.readStatus());
            
            cell.updateStatusTo(CellStatus.FREEZE);
            assertEquals(CellStatus.FREEZE, cell.readStatus());
            
            cell.updateStatusTo(CellStatus.INACTIVE);
            assertEquals(CellStatus.INACTIVE, cell.readStatus());
        }
        
        @Test
        @DisplayName("Null status throws IllegalArgumentException")
        void nullStatusThrows() {
            FactorCell cell = new FactorCell(11, TARGET_N, FactorStrategy.SMALL_PRIMES, 0);
            
            assertThrows(IllegalArgumentException.class, () -> {
                cell.updateStatusTo(null);
            });
        }
    }
}
