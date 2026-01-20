package com.emergent.doom.sat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

/**
 * Tests for SATInstanceGenerator (PHASE THREE ITER 2).
 *
 * As a developer, I want to verify generation and satisfiability.
 */
public class SATInstanceGeneratorTest {

    @Test
    void testGenerateSatisfiable3SAT() {
        var formula = SATInstanceGenerator.generateSatisfiable3SAT(3, 4, 42L);
        assertEquals(4, formula.getClauseCount());
        assertEquals(3, formula.getVariableCount());
        
        // Verify satisfiability with planted assignment (all true)
        Map<String, Boolean> planted = Map.of("x1", true, "x2", true, "x3", true);
        assertTrue(formula.evaluate(planted));
        
        // At least 60% satisfaction
        int satisfied = 0;
        for (CNFClause clause : formula.getClauses()) {
            if (clause.evaluate(planted)) satisfied++;
        }
        assertTrue(satisfied >= 2); // 60% of 4
    }

    @Test
    void testAppendixB() {
        var formula = SATInstanceGenerator.generateAppendixB();
        assertEquals(4, formula.getClauseCount());
        assertEquals(3, formula.getVariableCount());
    }

    @Test
    void testPilotInstance() {
        var formula = SATInstanceGenerator.generatePilotInstance();
        assertEquals(86, formula.getClauseCount()); // 4.3*20
        assertEquals(20, formula.getVariableCount());
    }
}