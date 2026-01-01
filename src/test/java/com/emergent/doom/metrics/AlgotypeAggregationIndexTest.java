package com.emergent.doom.metrics;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for AlgotypeAggregationIndex metric.
 *
 * Tests verify:
 * - Homogeneous arrays return 100%
 * - Alternating arrays return 0%
 * - Partially clustered arrays return expected values
 * - Edge cases (null, empty, single element)
 * - Statistical baseline for random 50/50 mix
 */
class AlgotypeAggregationIndexTest {

    private AlgotypeAggregationIndex<GenericCell> metric;

    @BeforeEach
    void setUp() {
        metric = new AlgotypeAggregationIndex<>();
    }

    private GenericCell[] createCells(Algotype... types) {
        GenericCell[] cells = new GenericCell[types.length];
        for (int i = 0; i < types.length; i++) {
            cells[i] = new GenericCell(i + 1, types[i]);
        }
        return cells;
    }

    // ========================================================================
    // Edge Cases
    // ========================================================================

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Null array returns 100.0")
        void nullArray_returns100() {
            double result = metric.compute((GenericCell[]) null);
            assertEquals(100.0, result, 0.001, "Null array trivially aggregated");
        }

        @Test
        @DisplayName("Empty array returns 100.0")
        void emptyArray_returns100() {
            GenericCell[] cells = new GenericCell[0];
            double result = metric.compute(cells);
            assertEquals(100.0, result, 0.001, "Empty array trivially aggregated");
        }

        @Test
        @DisplayName("Single element returns 100.0")
        void singleElement_returns100() {
            GenericCell[] cells = createCells(Algotype.BUBBLE);
            double result = metric.compute(cells);
            assertEquals(100.0, result, 0.001, "Single element trivially aggregated");
        }

        @Test
        @DisplayName("Two same algotypes returns 100.0")
        void twoSameAlgotypes_returns100() {
            GenericCell[] cells = createCells(Algotype.BUBBLE, Algotype.BUBBLE);
            double result = metric.compute(cells);
            assertEquals(100.0, result, 0.001, "Two same algotypes = 1/1 matching pairs");
        }

        @Test
        @DisplayName("Two different algotypes returns 0.0")
        void twoDifferentAlgotypes_returns0() {
            GenericCell[] cells = createCells(Algotype.BUBBLE, Algotype.SELECTION);
            double result = metric.compute(cells);
            assertEquals(0.0, result, 0.001, "Two different algotypes = 0/1 matching pairs");
        }
    }

    // ========================================================================
    // Homogeneous Arrays (Single Algotype)
    // ========================================================================

    @Nested
    @DisplayName("Homogeneous arrays")
    class HomogeneousArrayTests {

        @Test
        @DisplayName("All BUBBLE returns 100.0")
        void allBubble_returns100() {
            GenericCell[] cells = createCells(
                Algotype.BUBBLE, Algotype.BUBBLE, Algotype.BUBBLE,
                Algotype.BUBBLE, Algotype.BUBBLE, Algotype.BUBBLE
            );
            double result = metric.compute(cells);
            assertEquals(100.0, result, 0.001, "All same = 100% aggregation");
        }

        @Test
        @DisplayName("All SELECTION returns 100.0")
        void allSelection_returns100() {
            GenericCell[] cells = createCells(
                Algotype.SELECTION, Algotype.SELECTION, Algotype.SELECTION
            );
            double result = metric.compute(cells);
            assertEquals(100.0, result, 0.001, "All same = 100% aggregation");
        }

        @Test
        @DisplayName("All INSERTION returns 100.0")
        void allInsertion_returns100() {
            GenericCell[] cells = createCells(
                Algotype.INSERTION, Algotype.INSERTION, Algotype.INSERTION,
                Algotype.INSERTION
            );
            double result = metric.compute(cells);
            assertEquals(100.0, result, 0.001, "All same = 100% aggregation");
        }
    }

    // ========================================================================
    // Alternating Arrays (Maximum Dispersion)
    // ========================================================================

    @Nested
    @DisplayName("Alternating arrays (minimum aggregation)")
    class AlternatingArrayTests {

        @Test
        @DisplayName("[B,S,B,S,B,S] returns 0.0")
        void alternatingBubbleSelection_returns0() {
            GenericCell[] cells = createCells(
                Algotype.BUBBLE, Algotype.SELECTION,
                Algotype.BUBBLE, Algotype.SELECTION,
                Algotype.BUBBLE, Algotype.SELECTION
            );
            double result = metric.compute(cells);
            assertEquals(0.0, result, 0.001, "Perfect alternation = 0/5 matching pairs");
        }

        @Test
        @DisplayName("[B,I,S,B,I,S] returns 0.0")
        void alternatingThreeWay_returns0() {
            GenericCell[] cells = createCells(
                Algotype.BUBBLE, Algotype.INSERTION, Algotype.SELECTION,
                Algotype.BUBBLE, Algotype.INSERTION, Algotype.SELECTION
            );
            double result = metric.compute(cells);
            assertEquals(0.0, result, 0.001, "Three-way alternation = 0/5 matching pairs");
        }
    }

    // ========================================================================
    // Clustered Arrays (Partial Aggregation)
    // ========================================================================

    @Nested
    @DisplayName("Clustered arrays")
    class ClusteredArrayTests {

        @Test
        @DisplayName("[B,B,B,S,S,S] returns 100.0% (all cells have same-type neighbor)")
        void perfectlyClustered_returns100() {
            // [B,B,B,S,S,S]: Every cell has at least one same-type neighbor
            // B0: right=B ✓, B1: left=B ✓, B2: left=B ✓
            // S3: right=S ✓, S4: left=S ✓, S5: left=S ✓
            // 6/6 = 100%
            GenericCell[] cells = createCells(
                Algotype.BUBBLE, Algotype.BUBBLE, Algotype.BUBBLE,
                Algotype.SELECTION, Algotype.SELECTION, Algotype.SELECTION
            );
            double result = metric.compute(cells);
            assertEquals(100.0, result, 0.001, "All cells have same-type neighbor = 100%");
        }

        @Test
        @DisplayName("[B,S,S,S,B,B] returns 83.3% (5/6 cells have same-type neighbor)")
        void partialClustering_returns83() {
            // [B,S,S,S,B,B]:
            // B0: right=S ✗ → not counted
            // S1: left=B ✗, right=S ✓ → counted
            // S2: left=S ✓ → counted
            // S3: left=S ✓ → counted
            // B4: left=S ✗, right=B ✓ → counted
            // B5: left=B ✓ → counted
            // 5/6 = 83.33%
            GenericCell[] cells = createCells(
                Algotype.BUBBLE, Algotype.SELECTION, Algotype.SELECTION,
                Algotype.SELECTION, Algotype.BUBBLE, Algotype.BUBBLE
            );
            double result = metric.compute(cells);
            assertEquals(83.333, result, 0.01, "5/6 cells have same-type neighbor = 83.33%");
        }

        @Test
        @DisplayName("[B,B,S,B,B] returns 80.0% (4/5 cells have same-type neighbor)")
        void singleIntrusion_returns80() {
            // [B,B,S,B,B]:
            // B0: right=B ✓ → counted
            // B1: left=B ✓ → counted
            // S2: left=B ✗, right=B ✗ → not counted (isolated)
            // B3: right=B ✓ → counted
            // B4: left=B ✓ → counted
            // 4/5 = 80%
            GenericCell[] cells = createCells(
                Algotype.BUBBLE, Algotype.BUBBLE, Algotype.SELECTION,
                Algotype.BUBBLE, Algotype.BUBBLE
            );
            double result = metric.compute(cells);
            assertEquals(80.0, result, 0.001, "4/5 cells have same-type neighbor = 80%");
        }

        @Test
        @DisplayName("[I,I,I,B,S,S] three algotypes returns 83.3%")
        void threeAlgotypesClustered() {
            // [I,I,I,B,S,S]:
            // I0: right=I ✓ → counted
            // I1: left=I ✓ → counted
            // I2: left=I ✓ → counted
            // B3: left=I ✗, right=S ✗ → not counted (isolated)
            // S4: right=S ✓ → counted
            // S5: left=S ✓ → counted
            // 5/6 = 83.33%
            GenericCell[] cells = createCells(
                Algotype.INSERTION, Algotype.INSERTION, Algotype.INSERTION,
                Algotype.BUBBLE, Algotype.SELECTION, Algotype.SELECTION
            );
            double result = metric.compute(cells);
            assertEquals(83.333, result, 0.01, "5/6 cells have same-type neighbor = 83.33%");
        }
    }

    // ========================================================================
    // Statistical Baseline Tests
    // ========================================================================

    @Nested
    @DisplayName("Statistical baseline")
    class StatisticalTests {

        @Test
        @DisplayName("Random 50/50 mix averages around 75%")
        void random5050Mix_averagesAround75() {
            Random rng = new Random(42);
            int trials = 1000;
            int arraySize = 100;
            double sum = 0.0;

            for (int t = 0; t < trials; t++) {
                GenericCell[] cells = new GenericCell[arraySize];
                for (int i = 0; i < arraySize; i++) {
                    Algotype type = rng.nextBoolean() ? Algotype.BUBBLE : Algotype.SELECTION;
                    cells[i] = new GenericCell(i + 1, type);
                }
                sum += metric.compute(cells);
            }

            double average = sum / trials;

            // For random 50/50 mix, expected aggregation ~75%
            // Each cell has: P(at least one matching neighbor) = 1 - P(both different)
            // = 1 - 0.5 * 0.5 = 0.75 for interior cells
            // Edge cells have ~50% chance, so average is slightly below 75%
            assertTrue(average > 70.0, "Average should be above 70%: " + average);
            assertTrue(average < 80.0, "Average should be below 80%: " + average);
        }

        @Test
        @DisplayName("Random 33/33/34 three-way mix averages around 55%")
        void randomThreeWayMix_averagesAround55() {
            Random rng = new Random(42);
            int trials = 1000;
            int arraySize = 99; // Divisible by 3
            double sum = 0.0;

            Algotype[] types = Algotype.values();

            for (int t = 0; t < trials; t++) {
                GenericCell[] cells = new GenericCell[arraySize];
                for (int i = 0; i < arraySize; i++) {
                    Algotype type = types[rng.nextInt(3)];
                    cells[i] = new GenericCell(i + 1, type);
                }
                sum += metric.compute(cells);
            }

            double average = sum / trials;

            // For random three-way mix, expected aggregation ~55%
            // Each cell has: P(at least one matching neighbor) = 1 - P(both different)
            // = 1 - (2/3) * (2/3) = 1 - 4/9 = 5/9 ≈ 55.5% for interior cells
            assertTrue(average > 50.0, "Average should be above 50%: " + average);
            assertTrue(average < 60.0, "Average should be below 60%: " + average);
        }
    }

    // ========================================================================
    // Metric Interface Contract
    // ========================================================================

    @Nested
    @DisplayName("Metric interface contract")
    class MetricContractTests {

        @Test
        @DisplayName("getName returns correct name")
        void getName_returnsCorrectName() {
            assertEquals("Algotype Aggregation Index", metric.getName());
        }

        @Test
        @DisplayName("isLowerBetter returns false")
        void isLowerBetter_returnsFalse() {
            assertFalse(metric.isLowerBetter(), "Higher aggregation is better");
        }
    }

    // ========================================================================
    // Paper-Referenced Scenarios
    // ========================================================================

    @Nested
    @DisplayName("Paper-referenced scenarios")
    class PaperScenarioTests {

        @Test
        @DisplayName("Initial random 50/50 mix should be ~75%")
        void initialRandom5050_shouldBe75() {
            // For a random 50/50 mix, each interior cell has ~75% chance of having
            // at least one same-type neighbor (1 - 0.5 * 0.5)
            Random rng = new Random(42);
            GenericCell[] cells = new GenericCell[100];

            // Create strict 50/50 mix and shuffle
            for (int i = 0; i < 50; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.BUBBLE);
            }
            for (int i = 50; i < 100; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.SELECTION);
            }

            // Shuffle
            for (int i = 99; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                GenericCell temp = cells[i];
                cells[i] = cells[j];
                cells[j] = temp;
            }

            double result = metric.compute(cells);

            // Should be around 75% for random distribution
            assertTrue(result > 65.0 && result < 85.0,
                "Random 50/50 should yield ~75% aggregation, got: " + result);
        }

        @Test
        @DisplayName("Perfect clustering should be 100%")
        void perfectClustering_shouldBe100() {
            // Simulates mid-sort state where cells have perfectly clustered
            GenericCell[] cells = new GenericCell[100];

            // First 50 are BUBBLE, last 50 are SELECTION (perfect clustering)
            for (int i = 0; i < 50; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.BUBBLE);
            }
            for (int i = 50; i < 100; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.SELECTION);
            }

            double result = metric.compute(cells);

            // Every cell has at least one same-type neighbor except boundary cells
            // But in a 50/50 split, even the boundary cells (49 and 50) have same-type neighbors
            // So all 100 cells have at least one same-type neighbor = 100%
            assertEquals(100.0, result, 0.001, "Perfect clustering should yield 100%");
        }
    }
}
