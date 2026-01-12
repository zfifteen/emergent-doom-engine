package com.emergent.doom.metrics;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.StepSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for AlgotypeAggregationIndex.
 *
 * PURPOSE: Verify that AlgotypeAggregationIndex correctly measures spatial clustering
 * of cells by algotype in chimeric populations, matching the reference implementation
 * from cell_research Python codebase and Levin et al. (2024).
 *
 * [TestWeaver: Generated skeleton - expand with specific test scenarios]
 */
@DisplayName("AlgotypeAggregationIndex Tests")
class AlgotypeAggregationIndexTest {

    private AlgotypeAggregationIndex<TestCell> metric;

    @BeforeEach
    void setUp() {
        metric = new AlgotypeAggregationIndex<>();
    }

    /**
     * PURPOSE: As a developer, I want to verify aggregation calculation for perfect clustering
     * so that I can confirm the metric returns 100% when all same-type cells are adjacent.
     *
     * INPUTS: [TestWeaver: Define test inputs - cell array [B,B,B,S,S,S]]
     * EXPECTED OUTPUT: [TestWeaver: 100.0]
     * TEST DATA: [TestWeaver: Fully clustered algotype distribution]
     * REPRODUCTION: [TestWeaver: Manual verification steps]
     *
     * [TestWeaver: Implement test logic based on AlgotypeAggregationIndex API]
     */
    @Test
    @DisplayName("compute returns 100% for perfectly clustered algotypes")
    void computeReturns100ForPerfectlyClustered() {
        // Create snapshot with perfectly clustered algotypes: [B,B,B,S,S,S]
        // types[i] = [groupId, algotypeLabel, value, isFrozen]
        List<Object[]> types = Arrays.asList(
            new Object[]{0, 1, 1, 0},  // Bubble (label 1)
            new Object[]{0, 1, 2, 0},  // Bubble
            new Object[]{0, 1, 3, 0},  // Bubble
            new Object[]{0, 2, 4, 0},  // Selection (label 2)
            new Object[]{0, 2, 5, 0},  // Selection
            new Object[]{0, 2, 6, 0}   // Selection
        );
        
        StepSnapshot<TestCell> snapshot = new StepSnapshot<>(0,
            Arrays.asList((Comparable<?>) 1, 2, 3, 4, 5, 6),
            types,
            0);
        
        // All cells have at least one same-type neighbor
        assertEquals(100.0, metric.compute(snapshot), 0.01);
    }

    /**
     * PURPOSE: As a developer, I want to verify zero aggregation for alternating pattern
     * so that I can confirm the metric returns 0% when no cells have same-type neighbors.
     *
     * INPUTS: [TestWeaver: Define alternating pattern [B,S,B,S,B,S]]
     * EXPECTED OUTPUT: [TestWeaver: 0.0]
     * TEST DATA: [TestWeaver: Fully dispersed algotype distribution]
     * REPRODUCTION: [TestWeaver: Manual steps]
     *
     * [TestWeaver: Implement alternating pattern test]
     */
    @Test
    @DisplayName("compute returns 0% for alternating algotype pattern")
    void computeReturns0ForAlternatingPattern() {
        // Alternating pattern: [B,S,B,S,B,S]
        List<Object[]> types = Arrays.asList(
            new Object[]{0, 1, 1, 0},  // Bubble
            new Object[]{0, 2, 2, 0},  // Selection
            new Object[]{0, 1, 3, 0},  // Bubble
            new Object[]{0, 2, 4, 0},  // Selection
            new Object[]{0, 1, 5, 0},  // Bubble
            new Object[]{0, 2, 6, 0}   // Selection
        );
        
        StepSnapshot<TestCell> snapshot = new StepSnapshot<>(0,
            Arrays.asList((Comparable<?>) 1, 2, 3, 4, 5, 6),
            types,
            0);
        
        // No cell has a same-type neighbor
        assertEquals(0.0, metric.compute(snapshot), 0.01);
    }

    /**
     * PURPOSE: As a developer, I want to verify random baseline calculation
     * so that I can confirm expected ~75% aggregation for 50/50 random mix.
     *
     * INPUTS: [TestWeaver: Define random 50/50 mix]
     * EXPECTED OUTPUT: [TestWeaver: ~75% (statistical expectation)]
     * TEST DATA: [TestWeaver: Multiple random trials]
     * REPRODUCTION: [TestWeaver: Manual verification]
     *
     * [TestWeaver: Implement random baseline test]
     */
    @Test
    @DisplayName("compute approximates 75% for random 50/50 mix")
    void computeApproximates75ForRandom5050Mix() {
        // Pattern: [B,B,S,B,B,S] = 4/6 cells have same-type neighbor = 66.67%
        // Cell 0 (B): has right neighbor B -> yes
        // Cell 1 (B): has left B, right S -> yes  
        // Cell 2 (S): has left B, right B -> no
        // Cell 3 (B): has left S, right B -> yes
        // Cell 4 (B): has left B, right S -> yes
        // Cell 5 (S): has left B -> no
        // Total: 4/6 = 66.67%
        List<Object[]> types = Arrays.asList(
            new Object[]{0, 1, 1, 0},  // Bubble
            new Object[]{0, 1, 2, 0},  // Bubble
            new Object[]{0, 2, 3, 0},  // Selection
            new Object[]{0, 1, 4, 0},  // Bubble
            new Object[]{0, 1, 5, 0},  // Bubble
            new Object[]{0, 2, 6, 0}   // Selection
        );
        
        StepSnapshot<TestCell> snapshot = new StepSnapshot<>(0,
            Arrays.asList((Comparable<?>) 1, 2, 3, 4, 5, 6),
            types,
            0);
        
        double result = metric.compute(snapshot);
        
        // 4 out of 6 cells have same-type neighbor
        assertEquals(66.67, result, 0.1);
    }

    // Test helper class
    private static class TestCell implements Cell<TestCell> {
        
        public int getValue() {
            return 0;
        }
        
        
        public int compareTo(TestCell other) {
            return 0;
        }
        
        
        public boolean shouldSwapWith(TestCell other) {
            return false;
        }
    }
}
