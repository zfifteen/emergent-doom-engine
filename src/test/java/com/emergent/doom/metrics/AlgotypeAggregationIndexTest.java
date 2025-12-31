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
            double result = metric.compute(null);
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

        // TODO: Phase Three - Fix test name and docs to 80.0% (scaffold comment only)
        @Test
        @DisplayName("[B,B,B,S,S,S] returns 80.0% (scaffold: update from 66.67%)")
        void perfectlyClustered_returns80() {
            // [B,B,B,S,S,S] has pairs: BB, BB, BS, SS, SS
            // Matching: BB, BB, SS, SS = 4/5 = 80%
            GenericCell[] cells = createCells(
                Algotype.BUBBLE, Algotype.BUBBLE, Algotype.BUBBLE,
                Algotype.SELECTION, Algotype.SELECTION, Algotype.SELECTION
            );
            double result = metric.compute(cells);
            assertEquals(80.0, result, 0.001, "4/5 matching pairs = 80% aggregation");
        }

        @Test
        @DisplayName("[B,S,S,S,B,B] returns 60.0%")
        void partialClustering_returns60() {
            // [B,S,S,S,B,B] has pairs: BS, SS, SS, SB, BB
            // Matching: SS, SS, BB = 3/5 = 60%
            GenericCell[] cells = createCells(
                Algotype.BUBBLE, Algotype.SELECTION, Algotype.SELECTION,
                Algotype.SELECTION, Algotype.BUBBLE, Algotype.BUBBLE
            );
            double result = metric.compute(cells);
            assertEquals(60.0, result, 0.001, "3/5 matching pairs = 60%");
        }

        @Test
        @DisplayName("[B,B,S,B,B] returns 50.0%")
        void singleIntrusion_returns50() {
            // [B,B,S,B,B] has pairs: BB, BS, SB, BB
            // Matching: BB, BB = 2/4 = 50%
            GenericCell[] cells = createCells(
                Algotype.BUBBLE, Algotype.BUBBLE, Algotype.SELECTION,
                Algotype.BUBBLE, Algotype.BUBBLE
            );
            double result = metric.compute(cells);
            assertEquals(50.0, result, 0.001, "2/4 matching pairs = 50%");
        }

        @Test
        @DisplayName("[I,I,I,B,S,S] three algotypes")
        void threeAlgotypesClustered() {
            // [I,I,I,B,S,S] has pairs: II, II, IB, BS, SS
            // Matching: II, II, SS = 3/5 = 60%
            GenericCell[] cells = createCells(
                Algotype.INSERTION, Algotype.INSERTION, Algotype.INSERTION,
                Algotype.BUBBLE, Algotype.SELECTION, Algotype.SELECTION
            );
            double result = metric.compute(cells);
            assertEquals(60.0, result, 0.001, "3/5 matching pairs = 60%");
        }
    }

    // ========================================================================
    // Statistical Baseline Tests
    // ========================================================================

    @Nested
    @DisplayName("Statistical baseline")
    class StatisticalTests {

        @Test
        @DisplayName("Random 50/50 mix averages around 50%")
        void random5050Mix_averagesAround50() {
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

            // For random 50/50 mix, expected aggregation ~50%
            // (each pair has 50% chance of matching)
            assertTrue(average > 45.0, "Average should be above 45%: " + average);
            assertTrue(average < 55.0, "Average should be below 55%: " + average);
        }

        @Test
        @DisplayName("Random 33/33/34 three-way mix averages around 33%")
        void randomThreeWayMix_averagesAround33() {
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

            // For random three-way mix, expected aggregation ~33%
            // (each pair has 1/3 chance of matching)
            assertTrue(average > 28.0, "Average should be above 28%: " + average);
            assertTrue(average < 38.0, "Average should be below 38%: " + average);
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
        @DisplayName("Initial random 50/50 mix should be ~50%")
        void initialRandom5050_shouldBe50() {
            // From Levin paper: "at the beginning the Aggregation Value for a 50/50 mix was 50%"
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

            // Should be close to 50% for random distribution
            assertTrue(result > 40.0 && result < 60.0,
                "Random 50/50 should yield ~50% aggregation, got: " + result);
        }

        @Test
        @DisplayName("Perfect clustering should exceed 50%")
        void perfectClustering_shouldExceed50() {
            // Simulates mid-sort state where cells have clustered
            GenericCell[] cells = new GenericCell[100];

            // First 50 are BUBBLE, last 50 are SELECTION (perfect clustering)
            for (int i = 0; i < 50; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.BUBBLE);
            }
            for (int i = 50; i < 100; i++) {
                cells[i] = new GenericCell(i + 1, Algotype.SELECTION);
            }

            double result = metric.compute(cells);

            // Only 1 non-matching pair (B-S at position 49-50)
            // 98/99 = 98.99%
            assertTrue(result > 95.0, "Perfect clustering should yield >95%, got: " + result);
        }
    }
}
