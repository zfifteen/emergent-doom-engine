package com.emergent.doom.sat;

import java.util.*;

/**
 * SAT-specific metrics computation (PHASE THREE ITER 3: Implemented).
 *
 * <p><strong>PURPOSE:</strong> Compute aggregation by strategy proportion, satisfaction
 * trajectories, density correlation for SAT experiments.</p>
 *
 * <p><strong>AGGREGATION:</strong> Max proportion of any single strategy in array.</p>
 * <p><strong>SATISFACTION:</strong> Average satisfaction score across all cells.</p>
 */
public class SATMetrics {

    private final ConstraintDensityAnalyzer densityAnalyzer;

    public SATMetrics() {
        this.densityAnalyzer = new ConstraintDensityAnalyzer();
    }

    /**
     * Record experiment trajectory (PHASE THREE ITER 3).
     *
     * <p><strong>INPUT:</strong> Aggregation, satisfaction, density trajectories.</p>
     * <p><strong>ANALYSIS:</strong> Check peaks >50%, convergence >80%.</p>
     */
    public void recordTrajectory(List<Double> aggregation, List<Double> satisfaction, List<Double> density) {
        if (aggregation.isEmpty()) return;
        
        double maxAgg = Collections.max(aggregation);
        double finalSat = satisfaction.get(satisfaction.size() - 1);
        double avgDensity = density.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        System.out.printf("Trajectory analysis: max_agg=%.2f, final_sat=%.1f%%, avg_density=%.3f%n", 
            maxAgg, finalSat, avgDensity);
        
        if (maxAgg > 0.5) {
            System.out.println("CLUSTERING DETECTED: Max aggregation " + maxAgg + " > 50% threshold");
        } else {
            System.out.println("CLUSTERING WEAK: Max aggregation " + maxAgg + " <= 50%");
        }
        
        if (finalSat > 80.0) {
            System.out.println("STRONG CONVERGENCE: Final satisfaction " + finalSat + "%");
        }
        
        double densityCorr = computeCorrelation(aggregation, density);
        System.out.println("Density-aggregation correlation: " + densityCorr);
    }

    /**
     * Compute correlation between aggregation and density.
     */
    private double computeCorrelation(List<Double> agg, List<Double> dens) {
        if (agg.size() != dens.size() || agg.size() < 2) return 0.0;
        
        double aggMean = agg.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double densMean = dens.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        double aggVar = 0.0, densVar = 0.0, covar = 0.0;
        for (int i = 0; i < agg.size(); i++) {
            double aggDev = agg.get(i) - aggMean;
            double densDev = dens.get(i) - densMean;
            aggVar += aggDev * aggDev;
            densVar += densDev * densVar;
            covar += aggDev * densDev;
        }
        
        aggVar /= agg.size();
        densVar /= agg.size();
        
        if (aggVar == 0 || densVar == 0) return 0.0;
        return covar / Math.sqrt(aggVar * densVar);
    }

    /**
     * Check strong clustering (max agg > 0.5).
     */
    public boolean hasStrongClustering(List<Double> aggregationTrajectory) {
        return Collections.max(aggregationTrajectory) > 0.5;
    }

    /**
     * Check convergence (final sat > 80%).
     */
    public boolean hasConverged(List<Double> satisfactionTrajectory) {
        if (satisfactionTrajectory.isEmpty()) return false;
        return satisfactionTrajectory.get(satisfactionTrajectory.size() - 1) > 80.0;
    }

    /**
     * Generate experiment report.
     */
    public String generateReport(List<Double> aggTraj, List<Double> satTraj, List<Double> densTraj) {
        if (aggTraj.isEmpty()) return "No data";
        
        StringBuilder report = new StringBuilder();
        report.append("SAT Experiment Metrics Report\n");
        report.append("============================\n");
        report.append("Steps: " + aggTraj.size() + "\n");
        report.append("Max Aggregation: " + Collections.max(aggTraj) + "\n");
        report.append("Final Satisfaction: " + satTraj.get(satTraj.size() - 1) + "%\n");
        report.append("Avg Density: " + densTraj.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) + "\n");
        report.append("Clustering: " + (hasStrongClustering(aggTraj) ? "STRONG" : "WEAK") + "\n");
        report.append("Convergence: " + (hasConverged(satTraj) ? "YES" : "NO") + "\n");
        
        return report.toString();
    }

    /**
     * Compute overall density using analyzer.
     */
    public double computeOverallDensity(CNFFormula formula) {
        return densityAnalyzer.computeOverallDensity(formula);
    }
}