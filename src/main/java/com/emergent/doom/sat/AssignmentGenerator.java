package com.emergent.doom.sat;

import java.util.*;

/**
 * Strategy-specific assignment generation (PHASE THREE ITER 2: Full implementation).
 *
 * <p><strong>PURPOSE:</strong> Create initial assignment candidates biased toward
 * strategy strengths, enabling emergent exploration patterns per §3.2.3.</p>
 *
 * <p><strong>STRATEGY BIASES:</strong></p>
 * <ul>
 *   <li>DPLL: Unit propagation + pure literal assignment</li>
 *   <li>GREEDY_MCV: Assign most-constrained variables first</li>
 *   <li>WALKSAT: Random assignment with local flips</li>
 *   <li>HYBRID: DPLL initialization + WALKSAT noise</li>
 * </ul>
 */
public class AssignmentGenerator {

    private final Random random;

    public AssignmentGenerator(long seed) {
        this.random = new Random(seed);
    }

    /**
     * Generate strategy-biased assignment (PHASE THREE ITER 2: Implemented).
     *
     * <p><strong>OUTPUT:</strong> Complete assignment for all formula variables.</p>
     * <p><strong>BIAS:</strong> Strategy-specific initialization for emergent behavior.</p>
     */
    public Map<String, Boolean> generate(CNFFormula formula, SATStrategy strategy) {
        List<String> variables = new ArrayList<>(formula.getVariables());
        Map<String, Boolean> assignment = new HashMap<>();
        
        if (strategy == SATStrategy.DPLL) {
            return generateDpllAssignment(variables, formula);
        } else if (strategy == SATStrategy.GREEDY_MCV) {
            return generateGreedyAssignment(variables, formula);
        } else if (strategy == SATStrategy.WALKSAT) {
            return generateWalksatAssignment(variables);
        } else if (strategy == SATStrategy.HYBRID) {
            return generateHybridAssignment(variables, formula);
        } else {
            return generateRandomAssignment(variables);
        }
    }

    /**
     * DPLL-style: Unit propagation and pure literal assignment (PHASE THREE ITER 2).
     *
     * <p><strong>ALGORITHM:</strong> Identify unit clauses, assign to satisfy them,
     * then pure literals (appearing only positive/negative).</p>
     */
    private Map<String, Boolean> generateDpllAssignment(List<String> variables, CNFFormula formula) {
        Map<String, Boolean> assignment = new HashMap<>();
        Set<String> unassigned = new HashSet<>(variables);
        
        // Unit propagation
        boolean propagated;
        do {
            propagated = false;
            for (CNFClause clause : formula.getClauses()) {
                if (clause.evaluate(assignment)) continue; // Already satisfied
                
                // Check for unit clause
                List<String> unsatisfiedLiterals = new ArrayList<>();
                for (Map.Entry<String, Boolean> lit : clause.getLiterals().entrySet()) {
                    String var = lit.getKey();
                    Boolean assigned = assignment.get(var);
                    if (assigned == null || (assigned != lit.getValue())) {
                        unsatisfiedLiterals.add(var);
                    }
                }
                
                if (unsatisfiedLiterals.size() == 1) {
                    String unitVar = unsatisfiedLiterals.get(0);
                    Boolean valueToAssign = clause.getLiterals().get(unitVar);
                    assignment.put(unitVar, valueToAssign);
                    unassigned.remove(unitVar);
                    propagated = true;
                }
            }
        } while (propagated);
        
        // Pure literal assignment for remaining
        for (String var : new ArrayList<>(unassigned)) {
            boolean onlyPositive = true, onlyNegative = true;
            for (CNFClause clause : formula.getClauses()) {
                if (clause.getLiterals().containsKey(var)) {
                    boolean polarity = clause.getLiterals().get(var);
                    if (polarity) onlyNegative = false;
                    else onlyPositive = false;
                }
            }
            if (onlyPositive) {
                assignment.put(var, true);
                unassigned.remove(var);
            } else if (onlyNegative) {
                assignment.put(var, false);
                unassigned.remove(var);
            }
        }
        
        // Assign remaining randomly
        for (String var : unassigned) {
            assignment.put(var, random.nextBoolean());
        }
        
        return assignment;
    }

    /**
     * GREEDY_MCV: Assign most-constrained variables first (PHASE THREE ITER 2).
     *
     * <p><strong>ALGORITHM:</strong> Sort variables by degree (appearances in clauses),
     * assign to satisfy most clauses.</p>
     */
    private Map<String, Boolean> generateGreedyAssignment(List<String> variables, CNFFormula formula) {
        Map<String, Boolean> assignment = new HashMap<>();
        
        // Compute variable degrees
        Map<String, Integer> degrees = new HashMap<>();
        for (String var : variables) {
            int degree = 0;
            for (CNFClause clause : formula.getClauses()) {
                if (clause.getVariables().contains(var)) degree++;
            }
            degrees.put(var, degree);
        }
        
        // Sort by descending degree
        variables.sort((a, b) -> degrees.get(b).compareTo(degrees.get(a)));
        
        // Assign greedily
        for (String var : variables) {
            // Try both values, choose one that satisfies more clauses
            int trueSatisfied = countSatisfiedWith(assignment, var, true, formula);
            int falseSatisfied = countSatisfiedWith(assignment, var, false, formula);
            boolean value = trueSatisfied >= falseSatisfied;
            assignment.put(var, value);
        }
        
        return assignment;
    }

    private int countSatisfiedWith(Map<String, Boolean> partial, String var, boolean value, CNFFormula formula) {
        Map<String, Boolean> temp = new HashMap<>(partial);
        temp.put(var, value);
        int count = 0;
        for (CNFClause clause : formula.getClauses()) {
            if (clause.evaluate(temp)) count++;
        }
        return count;
    }

    /**
     * WALKSAT: Random assignment with local flips (PHASE THREE ITER 2).
     *
     * <p><strong>ALGORITHM:</strong> Start random, flip 10% variables randomly.</p>
     */
    private Map<String, Boolean> generateWalksatAssignment(List<String> variables) {
        Map<String, Boolean> assignment = new HashMap<>();
        for (String var : variables) {
            assignment.put(var, random.nextBoolean());
        }
        
        // Add noise: flip 10% randomly
        int flips = variables.size() / 10;
        for (int i = 0; i < flips; i++) {
            if (!variables.isEmpty()) {
                String var = variables.get(random.nextInt(variables.size()));
                boolean current = assignment.get(var);
                assignment.put(var, !current);
            }
        }
        
        return assignment;
    }

    /**
     * HYBRID: DPLL base with WALKSAT noise (PHASE THREE ITER 2).
     */
    private Map<String, Boolean> generateHybridAssignment(List<String> variables, CNFFormula formula) {
        Map<String, Boolean> base = generateDpllAssignment(variables, formula);
        
        // Add WALKSAT noise to 20% of assignments
        int noiseCount = variables.size() / 5; // 20%
        for (int i = 0; i < noiseCount; i++) {
            if (!variables.isEmpty()) {
                String var = variables.get(random.nextInt(variables.size()));
                boolean current = base.get(var);
                base.put(var, !current);
            }
        }
        
        return base;
    }

    /**
     * Fallback random assignment.
     */
    private Map<String, Boolean> generateRandomAssignment(List<String> variables) {
        Map<String, Boolean> assignment = new HashMap<>();
        for (String var : variables) {
            assignment.put(var, random.nextBoolean());
        }
        return assignment;
    }
}