package com.emergent.doom.factorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FactorCellFactory.
 *
 * <p><strong>PURPOSE:</strong> Validate Step 2 success criteria:
 * <ul>
 *   <li>Factory creates chimeric populations correctly</li>
 *   <li>Strategy distribution matches specification</li>
 *   <li>Candidates generated appropriately for each strategy</li>
 *   <li>Cells initialized with correct positions and fitness</li>
 * </ul>
 */
@DisplayName("FactorCellFactory Tests")
class FactorCellFactoryTest {
    
    private static final int TARGET_N = 143;  // 11 × 13
    
    @Nested
    @DisplayName("Chimeric Population Creation")
    class PopulationTests {
        
        @Test
        @DisplayName("Creates correct number of cells")
        void correctCellCount() {
            // GIVEN: Factory with distribution
            FactorCellFactory factory = new FactorCellFactory(42L);
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.RANDOM_SAMPLE, 0.5
            );
            
            // WHEN: Creating 50 cells
            List<FactorCell> cells = factory.createCells(TARGET_N, 50, distribution);
            
            // THEN: Exactly 50 cells created
            assertEquals(50, cells.size());
        }
        
        @Test
        @DisplayName("Strategy distribution matches specification")
        void strategyDistribution() {
            // GIVEN: Factory with 33/33/34 distribution
            FactorCellFactory factory = new FactorCellFactory(42L);
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.33,
                FactorStrategy.FERMAT_NEAR_SQRT, 0.33,
                FactorStrategy.RANDOM_SAMPLE, 0.34
            );
            
            // WHEN: Creating 50 cells
            List<FactorCell> cells = factory.createCells(TARGET_N, 50, distribution);
            
            // THEN: Strategy counts match
            long smallPrimesCount = cells.stream()
                .filter(c -> c.readAlgotype() == FactorStrategy.SMALL_PRIMES)
                .count();
            long fermatCount = cells.stream()
                .filter(c -> c.readAlgotype() == FactorStrategy.FERMAT_NEAR_SQRT)
                .count();
            long randomCount = cells.stream()
                .filter(c -> c.readAlgotype() == FactorStrategy.RANDOM_SAMPLE)
                .count();
            
            // Allow ±1 for rounding
            assertEquals(17, smallPrimesCount, 1);
            assertEquals(17, fermatCount, 1);
            assertEquals(17, randomCount, 1);
            assertEquals(50, smallPrimesCount + fermatCount + randomCount);
        }
        
        @Test
        @DisplayName("All cells have valid positions")
        void validPositions() {
            // GIVEN: Created cells
            FactorCellFactory factory = new FactorCellFactory(42L);
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.RANDOM_SAMPLE, 0.5
            );
            List<FactorCell> cells = factory.createCells(TARGET_N, 20, distribution);
            
            // THEN: All positions valid and unique
            for (int i = 0; i < cells.size(); i++) {
                assertEquals(i, cells.get(i).readCurrentPosition(),
                    "Cell at index " + i + " should have position " + i);
            }
        }
        
        @Test
        @DisplayName("All candidates in valid range [2, sqrt(N)]")
        void candidatesInValidRange() {
            // GIVEN: Created cells for N=143
            FactorCellFactory factory = new FactorCellFactory(42L);
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.33,
                FactorStrategy.FERMAT_NEAR_SQRT, 0.33,
                FactorStrategy.RANDOM_SAMPLE, 0.34
            );
            List<FactorCell> cells = factory.createCells(TARGET_N, 50, distribution);
            
            // THEN: All candidates in [2, 11]
            int sqrtN = (int) Math.sqrt(TARGET_N);
            for (FactorCell cell : cells) {
                int candidate = cell.readValue();
                assertTrue(candidate >= 2, "Candidate " + candidate + " should be >= 2");
                assertTrue(candidate <= sqrtN, "Candidate " + candidate + " should be <= sqrt(N)");
            }
        }
        
        @Test
        @DisplayName("Throws on invalid distribution (doesn't sum to 1.0)")
        void invalidDistribution() {
            // GIVEN: Invalid distribution
            FactorCellFactory factory = new FactorCellFactory(42L);
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.RANDOM_SAMPLE, 0.3  // Sums to 0.8
            );
            
            // WHEN/THEN: Throws IllegalArgumentException
            assertThrows(IllegalArgumentException.class, () -> {
                factory.createCells(TARGET_N, 50, distribution);
            });
        }
    }
    
    @Nested
    @DisplayName("Reproducibility")
    class ReproducibilityTests {
        
        @Test
        @DisplayName("Same seed produces identical populations")
        void sameSeedIdentical() {
            // GIVEN: Two factories with same seed
            FactorCellFactory factory1 = new FactorCellFactory(12345L);
            FactorCellFactory factory2 = new FactorCellFactory(12345L);
            
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.RANDOM_SAMPLE, 0.5
            );
            
            // WHEN: Creating populations
            List<FactorCell> cells1 = factory1.createCells(TARGET_N, 30, distribution);
            List<FactorCell> cells2 = factory2.createCells(TARGET_N, 30, distribution);
            
            // THEN: Identical cell values and strategies
            assertEquals(cells1.size(), cells2.size());
            for (int i = 0; i < cells1.size(); i++) {
                assertEquals(cells1.get(i).readValue(), cells2.get(i).readValue());
                assertEquals(cells1.get(i).readAlgotype(), cells2.get(i).readAlgotype());
            }
        }
        
        @Test
        @DisplayName("Different seeds produce different populations")
        void differentSeedsDifferent() {
            // GIVEN: Two factories with different seeds
            FactorCellFactory factory1 = new FactorCellFactory(111L);
            FactorCellFactory factory2 = new FactorCellFactory(222L);
            
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.RANDOM_SAMPLE, 1.0
            );
            
            // WHEN: Creating populations
            List<FactorCell> cells1 = factory1.createCells(TARGET_N, 30, distribution);
            List<FactorCell> cells2 = factory2.createCells(TARGET_N, 30, distribution);
            
            // THEN: Different (with very high probability)
            boolean anyDifferent = false;
            for (int i = 0; i < cells1.size(); i++) {
                if (!cells1.get(i).readValue().equals(cells2.get(i).readValue())) {
                    anyDifferent = true;
                    break;
                }
            }
            assertTrue(anyDifferent, "Different seeds should produce different populations");
        }
    }
    
    @Nested
    @DisplayName("Perfect Factor Inclusion")
    class PerfectFactorTests {
        
        @Test
        @DisplayName("SMALL_PRIMES strategy includes factor 11")
        void smallPrimesIncludesFactor11() {
            // GIVEN: Population with SMALL_PRIMES strategy
            FactorCellFactory factory = new FactorCellFactory(42L);
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 1.0
            );
            
            // WHEN: Creating cells
            List<FactorCell> cells = factory.createCells(TARGET_N, 20, distribution);
            
            // THEN: Contains factor 11
            boolean hasFactor11 = cells.stream()
                .anyMatch(c -> c.readValue() == 11 && c.isPerfectFactor());
            assertTrue(hasFactor11, "SMALL_PRIMES should include factor 11");
        }
        
        @Test
        @DisplayName("FERMAT_NEAR_SQRT strategy likely includes factor 11")
        void fermatLikelyIncludesFactor11() {
            // GIVEN: Population with FERMAT_NEAR_SQRT strategy
            FactorCellFactory factory = new FactorCellFactory(42L);
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.FERMAT_NEAR_SQRT, 1.0
            );
            
            // WHEN: Creating cells
            List<FactorCell> cells = factory.createCells(TARGET_N, 20, distribution);
            
            // THEN: Should include 11 (near sqrt(143) ≈ 11.96)
            boolean hasFactor11 = cells.stream()
                .anyMatch(c -> c.readValue() == 11);
            assertTrue(hasFactor11, "FERMAT_NEAR_SQRT should include 11 (near sqrt)");
        }
    }
}
