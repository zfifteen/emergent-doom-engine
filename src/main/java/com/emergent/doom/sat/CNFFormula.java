package com.emergent.doom.sat;

import java.util.*;

/**
 * CNF formula representation (PHASE TWO: Implemented evaluation).
 *
 * <p><strong>PURPOSE:</strong> Encapsulate SAT instance with clause collection.</p>
 *
 * <p><strong>REPRESENTATION:</strong> List of CNFClause, each with 3 literals.</p>
 */
public class CNFFormula {

    private final List<CNFClause> clauses;
    private final Set<String> variables;

    public CNFFormula(List<CNFClause> clauses) {
        if (clauses == null || clauses.isEmpty()) {
            throw new IllegalArgumentException("CNF formula must have at least one clause");
        }
        this.clauses = List.copyOf(clauses);
        this.variables = extractVariables(clauses);
    }

    private static Set<String> extractVariables(List<CNFClause> clauses) {
        Set<String> vars = new HashSet<>();
        for (CNFClause clause : clauses) {
            vars.addAll(clause.getVariables());
        }
        return Collections.unmodifiableSet(vars);
    }

    public List<CNFClause> getClauses() {
        return clauses;
    }

    public Set<String> getVariables() {
        return variables;
    }

    public int getClauseCount() {
        return clauses.size();
    }

    public int getVariableCount() {
        return variables.size();
    }

    /**
     * Evaluate formula (PHASE TWO: Implemented short-circuit).
     */
    public boolean evaluate(Map<String, Boolean> assignment) {
        for (CNFClause clause : clauses) {
            if (!clause.evaluate(assignment)) {
                return false; // Short-circuit
            }
        }
        return true;
    }
}

class CNFClause {

    private final Map<String, Boolean> literals; // var -> polarity

    public CNFClause(Map<String, Boolean> literals) {
        if (literals == null || literals.isEmpty()) {
            throw new IllegalArgumentException("Clause cannot be empty");
        }
        this.literals = Map.copyOf(literals);
    }

    public Set<String> getVariables() {
        return literals.keySet();
    }

    public Map<String, Boolean> getLiterals() {
        return literals;
    }

    /**
     * Evaluate clause (PHASE TWO: Implemented OR logic).
     */
    public boolean evaluate(Map<String, Boolean> assignment) {
        for (Map.Entry<String, Boolean> literal : literals.entrySet()) {
            String var = literal.getKey();
            boolean isPositive = literal.getValue();
            Boolean assignedValue = assignment.get(var);
            if (assignedValue == null) {
                continue; // Unassigned, cannot satisfy this literal, but check others
            }
            if (isPositive == assignedValue) {
                return true; // Literal satisfied
            }
        }
        return false;
    }
}