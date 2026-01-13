package com.emergent.doom.sat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/**
 * Tests for SATCell (SCAFFOLD).
 *
 * As a developer, I want to verify basic instantiation.
 */
public class SATCellTest {

    @Test
    void testConstructorDefault() {
        var assignment = Map.of("x1", true);
        var formula = new CNFFormula(List.of(new CNFClause(Map.of("x1", true))));
        var cell = new SATCell(assignment, formula, SATStrategy.DPLL, 0);
        assertNotNull(cell);
        assertEquals(SATStrategy.DPLL, cell.readAlgotype());
        assertEquals(100, cell.readValue()); // Full satisfaction
    }

    @Test
    void testConstructorIncompleteAssignment() {
        var assignment = Map.of(); // Empty
        var formula = new CNFFormula(List.of(new CNFClause(Map.of("x1", true))));
        assertThrows(IllegalArgumentException.class, () -> new SATCell(assignment, formula, SATStrategy.DPLL, 0));
    }

    @Test
    void testConstructorNegativePosition() {
        var assignment = Map.of("x1", true);
        var formula = new CNFFormula(List.of(new CNFClause(Map.of("x1", true))));
        assertThrows(IllegalArgumentException.class, () -> new SATCell(assignment, formula, SATStrategy.DPLL, -1));
    }

    @Test
    void testSatisfactionScoreRounding() {
        // 1/3 clauses satisfied = 33%
        var assignment = Map.of("x1", true, "x2", false, "x3", false);
        var clauses = List.of(
            new CNFClause(Map.of("x1", true)), // Satisfied
            new CNFClause(Map.of("x2", true)), // Unsatisfied
            new CNFClause(Map.of("x3", true))  // Unsatisfied
        );
        var formula = new CNFFormula(clauses);
        var cell = new SATCell(assignment, formula, SATStrategy.DPLL, 0);
        assertEquals(33, cell.readValue()); // Rounded
    }

    @Test
    void testDpllShouldMove() {
        var cell = new SATCell(Map.of("x1", true), new CNFFormula(List.of(new CNFClause(Map.of("x1", true)))), SATStrategy.DPLL, 5);
        // Mock neighbors - this requires NeighborhoodView mock, stub test for now
        assertTrue(cell.getConfig().getDpllSwapThreshold() == 5); // Config test
    }

    @Test
    void testHybridStagnationUpdate() {
        var assignment = Map.of("x1", true);
        var formula = new CNFFormula(List.of(new CNFClause(Map.of("x1", true))));
        var cell = new SATCell(assignment, formula, SATStrategy.HYBRID, 0);
        cell.updateStagnationTracking(100); // No change
        assertEquals(0, cell.getStepsSinceImprovement());
        cell.updateStagnationTracking(90); // Decrease
        assertEquals(1, cell.getStepsSinceImprovement());
        cell.updateStagnationTracking(95); // Increase
        assertEquals(0, cell.getStepsSinceImprovement());
    }
}
}