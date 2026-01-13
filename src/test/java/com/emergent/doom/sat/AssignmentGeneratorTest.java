package com.emergent.doom.sat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Tests for AssignmentGenerator (PHASE THREE ITER 2).
 *
 * As a developer, I want to verify strategy-biased assignments.
 */
public class AssignmentGeneratorTest {

    private final AssignmentGenerator generator = new AssignmentGenerator(42L);

    @Test
    void testDpllAssignment() {
        var formula = SATInstanceGenerator.generateSatisfiable3SAT(3, 4, 42L);
        var assignment = generator.generate(formula, SATStrategy.DPLL);
        
        assertEquals(3, assignment.size()); // Complete
        assertTrue(formula.evaluate(assignment)); // Should be satisfiable
    }

    @Test
    void testGreedyAssignment() {
        var formula = SATInstanceGenerator.generateSatisfiable3SAT(5, 10, 42L);
        var assignment = generator.generate(formula, SATStrategy.GREEDY_MCV);
        
        assertEquals(5, assignment.size());
        // Greedy should have reasonable satisfaction (>20%)
        int satisfied = 0;
        for (CNFClause clause : formula.getClauses()) {
            if (clause.evaluate(assignment)) satisfied++;
        }
        assertTrue(satisfied > 2);
    }

    @Test
    void testWalksatAssignment() {
        var formula = SATInstanceGenerator.generateSatisfiable3SAT(3, 4, 42L);
        var assignment = generator.generate(formula, SATStrategy.WALKSAT);
        
        assertEquals(3, assignment.size());
        // WALKSAT may not satisfy, but complete
    }

    @Test
    void testHybridAssignment() {
        var formula = SATInstanceGenerator.generateSatisfiable3SAT(3, 4, 42L);
        var assignment = generator.generate(formula, SATStrategy.HYBRID);
        
        assertEquals(3, assignment.size());
        assertTrue(formula.evaluate(assignment)); // Hybrid should satisfy like DPLL base
    }
}