package com.emergent.doom.sat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Tests for ConstraintDensityAnalyzer (PHASE THREE ITER 2).
 *
 * As a developer, I want to verify density metrics for pilot instance.
 */
public class ConstraintDensityAnalyzerTest {

    @Test
    void testVariableDegreesPilot() {
        var formula = SATInstanceGenerator.generatePilotInstance();
        var analyzer = new ConstraintDensityAnalyzer();
        var degrees = analyzer.computeVariableDegrees(formula);
        
        assertEquals(20, degrees.size());
        // Average degree should be ~4.3 (86 clauses / 20 vars, but 3-lit so higher)
        double avgDegree = degrees.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        assertTrue(avgDegree > 10.0); // 3-lit clauses, expect ~12.9
    }

    @Test
    void testClauseOverlapMatrix() {
        var formula = SATInstanceGenerator.generateSatisfiable3SAT(5, 5, 42L);
        var analyzer = new ConstraintDensityAnalyzer();
        double[][] matrix = analyzer.computeClauseOverlapMatrix(formula);
        
        assertEquals(5, matrix.length);
        // Diagonal should be 0 (self-overlap not counted)
        for (int i = 0; i < 5; i++) {
            assertEquals(0.0, matrix[i][i]);
        }
        // Symmetric
        assertEquals(matrix[0][1], matrix[1][0]);
    }

    @Test
    void testIdentifyDenseRegions() {
        var formula = SATInstanceGenerator.generatePilotInstance();
        var analyzer = new ConstraintDensityAnalyzer();
        List<Integer> dense = analyzer.identifyDenseRegions(formula, 0.5);
        
        // Expect some dense regions in 86-clause instance
        assertTrue(dense.size() > 10);
    }

    @Test
    void testOverallDensity() {
        var formula = SATInstanceGenerator.generatePilotInstance();
        var analyzer = new ConstraintDensityAnalyzer();
        double density = analyzer.computeOverallDensity(formula);
        
        // Normalized density >0.5 for dense instance
        assertTrue(density > 0.5);
    }
}