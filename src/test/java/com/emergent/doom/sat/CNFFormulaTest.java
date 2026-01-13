package com.emergent.doom.sat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Tests for CNFFormula (SCAFFOLD).
 *
 * As a developer, I want to test formula creation.
 */
public class CNFFormulaTest {

    @Test
    void testConstructorValid() {
        var clause = new CNFClause(Map.of("x1", true));
        var formula = new CNFFormula(List.of(clause));
        assertEquals(1, formula.getClauseCount());
        assertEquals(1, formula.getVariableCount());
        assertTrue(formula.evaluate(Map.of("x1", true)));
    }

    @Test
    void testConstructorEmptyClauses() {
        assertThrows(IllegalArgumentException.class, () -> new CNFFormula(List.of()));
    }

    @Test
    void testEvaluateUnsatisfied() {
        var clause = new CNFClause(Map.of("x1", true));
        var formula = new CNFFormula(List.of(clause));
        assertFalse(formula.evaluate(Map.of("x1", false)));
    }
}
}