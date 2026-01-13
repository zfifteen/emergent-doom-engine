package com.emergent.doom.sat;

import java.util.*;

/**
 * Analyze constraint density for clustering boundary detection (PHASE THREE ITER 2: New class).
 *
 * <p><strong>PURPOSE:</strong> Identify "constraint-dense regions" per §4.3.2.
 * Compute variable degrees and clause overlap to define clustering boundaries.</p>
 *
 * <p><strong>METRICS:</strong></p>
 * <ul>
 *   <li>Variable degree: Number of clauses containing variable</li>
 *   <li>Clause overlap: Shared variables between clauses</li>
 *   <li>Density threshold: >50% overlap defines dense regions</li>
 * </ul>
 */
public class ConstraintDensityAnalyzer {

    /**
     * Compute variable degrees (PHASE THREE ITER 2: Implemented).
     *
     * <p><strong>OUTPUT:</strong> Map of variable to degree (clause count).</p>
     * <p><strong>USAGE:</strong> High-degree variables expected in clustering centers.</p>
     */
    public Map<String, Integer> computeVariableDegrees(CNFFormula formula) {
        Map<String, Integer> degrees = new HashMap<>();
        
        for (CNFClause clause : formula.getClauses()) {
            for (String var : clause.getVariables()) {
                degrees.put(var, degrees.getOrDefault(var, 0) + 1);
            }
        }
        
        return degrees;
    }

    /**
     * Compute pairwise clause overlap (PHASE THREE ITER 2: Implemented).
     *
     * <p><strong>OUTPUT:</strong> Matrix of overlap counts between clauses i,j.</p>
     * <p><strong>DEFINITION:</strong> Number of shared variables between clauses.</p>
     * <p><strong>CLUSTER BOUNDARY:</strong> Regions with >50% average overlap are dense.</p>
     */
    public double[][] computeClauseOverlapMatrix(CNFFormula formula) {
        List<CNFClause> clauses = formula.getClauses();
        int numClauses = clauses.size();
        double[][] overlap = new double[numClauses][numClauses];
        
        for (int i = 0; i < numClauses; i++) {
            for (int j = i + 1; j < numClauses; j++) {
                Set<String> shared = new HashSet<>(clauses.get(i).getVariables());
                shared.retainAll(clauses.get(j).getVariables());
                double overlapCount = shared.size();
                double minSize = Math.min(clauses.get(i).getVariables().size(), 
                                        clauses.get(j).getVariables().size());
                double overlapPct = minSize > 0 ? overlapCount / minSize : 0.0;
                
                overlap[i][j] = overlap[j][i] = overlapPct;
            }
        }
        
        return overlap;
    }

    /**
     * Identify dense regions (PHASE THREE ITER 2: Threshold-based).
     *
     * <p><strong>CRITERIA:</strong> Average overlap > 0.5 defines dense region.</p>
     * <p><strong>OUTPUT:</strong> List of clause indices in dense regions.</p>
     */
    public List<Integer> identifyDenseRegions(CNFFormula formula, double densityThreshold) {
        double[][] overlapMatrix = computeClauseOverlapMatrix(formula);
        int numClauses = formula.getClauseCount();
        List<Integer> denseClauses = new ArrayList<>();
        
        for (int i = 0; i < numClauses; i++) {
            double avgOverlap = 0.0;
            int neighborCount = 0;
            for (int j = 0; j < numClauses; j++) {
                if (i != j) {
                    avgOverlap += overlapMatrix[i][j];
                    neighborCount++;
                }
            }
            avgOverlap /= neighborCount;
            
            if (avgOverlap > densityThreshold) {
                denseClauses.add(i);
            }
        }
        
        return denseClauses;
    }

    /**
     * Compute constraint density score (0-1) for entire formula.
     */
    public double computeOverallDensity(CNFFormula formula) {
        Map<String, Integer> degrees = computeVariableDegrees(formula);
        double avgDegree = degrees.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        int numVars = formula.getVariables().size();
        return avgDegree / numVars; // Normalized density
    }

    /**
     * Pilot analysis for 20-var instance (PHASE THREE ITER 2: Validation).
     */
    public static void analyzePilotInstance() {
        CNFFormula pilot = SATInstanceGenerator.generatePilotInstance();
        ConstraintDensityAnalyzer analyzer = new ConstraintDensityAnalyzer();
        
        Map<String, Integer> degrees = analyzer.computeVariableDegrees(pilot);
        System.out.println("Pilot degrees: " + degrees);
        
        List<Integer> dense = analyzer.identifyDenseRegions(pilot, 0.5);
        System.out.println("Dense clauses: " + dense.size() + "/" + pilot.getClauseCount());
        
        double density = analyzer.computeOverallDensity(pilot);
        System.out.println("Overall density: " + density);
        
        // Verify: expect ~4.3 density, some dense regions
        // Pseudo-code, actual test in JUnit
    }
}