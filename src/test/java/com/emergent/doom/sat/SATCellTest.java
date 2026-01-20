package com.emergent.doom.sat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
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
        Map<String, Boolean> assignment = new HashMap<>();
        assignment.put("x1", true);
        Map<String, Boolean> clauseMap = new HashMap<>();
        clauseMap.put("x1", true);
        var formula = new CNFFormula(List.of(new CNFClause(clauseMap)));
        var cell = new SATCell(assignment, formula, SATStrategy.DPLL, 0);
        assertNotNull(cell);
        assertEquals(SATStrategy.DPLL, cell.readAlgotype());
        assertEquals(100, cell.readValue()); // Full satisfaction
    }

    @Test
    void testConstructorIncompleteAssignment() {
        Map<String, Boolean> assignment = new HashMap<>(); // Empty
        Map<String, Boolean> clauseMap = new HashMap<>();
        clauseMap.put("x1", true);
        var formula = new CNFFormula(List.of(new CNFClause(clauseMap)));
        assertThrows(IllegalArgumentException.class, () -> new SATCell(assignment, formula, SATStrategy.DPLL, 0));
    }

    @Test
    void testConstructorNegativePosition() {
        Map<String, Boolean> assignment = new HashMap<>();
        assignment.put("x1", true);
        Map<String, Boolean> clauseMap = new HashMap<>();
        clauseMap.put("x1", true);
        var formula = new CNFFormula(List.of(new CNFClause(clauseMap)));
        assertThrows(IllegalArgumentException.class, () -> new SATCell(assignment, formula, SATStrategy.DPLL, -1));
    }

    @Test
    void testSatisfactionScoreRounding() {
        // 1/3 clauses satisfied = 33%
        Map<String, Boolean> assignment = new HashMap<>();
        assignment.put("x1", true);
        assignment.put("x2", false);
        assignment.put("x3", false);
        
        Map<String, Boolean> clause1 = new HashMap<>();
        clause1.put("x1", true);
        
        Map<String, Boolean> clause2 = new HashMap<>();
        clause2.put("x2", true);
        
        Map<String, Boolean> clause3 = new HashMap<>();
        clause3.put("x3", true);
        
        var clauses = List.of(
            new CNFClause(clause1), // Satisfied
            new CNFClause(clause2), // Unsatisfied
            new CNFClause(clause3)  // Unsatisfied
        );
        var formula = new CNFFormula(clauses);
        var cell = new SATCell(assignment, formula, SATStrategy.DPLL, 0);
        assertEquals(33, cell.readValue()); // Rounded
    }

    @Test
    void testDpllShouldMove() {
        Map<String, Boolean> assignment = new HashMap<>();
        assignment.put("x1", true);
        Map<String, Boolean> clauseMap = new HashMap<>();
        clauseMap.put("x1", true);
        var formula = new CNFFormula(List.of(new CNFClause(clauseMap)));
        var cell = new SATCell(assignment, formula, SATStrategy.DPLL, 5);
        // Mock neighbors - this requires NeighborhoodView mock, stub test for now
        assertTrue(cell.getConfig().getDpllSwapThreshold() == 5); // Config test
    }

    @Test
    void testHybridStagnationUpdate() {
        Map<String, Boolean> assignment = new HashMap<>();
        assignment.put("x1", true);
        Map<String, Boolean> clauseMap = new HashMap<>();
        clauseMap.put("x1", true);
        var formula = new CNFFormula(List.of(new CNFClause(clauseMap)));
        var cell = new SATCell(assignment, formula, SATStrategy.HYBRID, 0);
        cell.updateStagnationTracking(100); // No change
        assertEquals(0, cell.getStepsSinceImprovement());
        cell.updateStagnationTracking(90); // Decrease
        assertEquals(1, cell.getStepsSinceImprovement());
        cell.updateStagnationTracking(95); // Increase
        assertEquals(0, cell.getStepsSinceImprovement());
    }
}