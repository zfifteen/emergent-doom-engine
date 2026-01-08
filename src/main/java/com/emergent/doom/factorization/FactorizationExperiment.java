package com.emergent.doom.factorization;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.execution.GenericExecutionEngine;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Orchestrates factorization experiments using emergent clustering.
 *
 * <p><strong>PURPOSE:</strong> Run controlled experiments to test whether
 * emergent clustering can partition factor candidates and reveal structure
 * in the factorization problem space.</p>
 *
 * <p><strong>EXPERIMENT PROTOCOL:</strong></p>
 * <ol>
 *   <li>Create 50-cell chimeric population for N=143 (11×13)</li>
 *   <li>Distribute strategies: 33% SMALL_PRIMES, 33% FERMAT_NEAR_SQRT, 34% RANDOM_SAMPLE</li>
 *   <li>Execute until convergence (max 5000 steps)</li>
 *   <li>Record trajectory: step number, aggregation, fitness metrics</li>
 *   <li>Export results to CSV for analysis</li>
 * </ol>
 *
 * <p><strong>SUCCESS CRITERIA (from FIRST_NON_SORTING_EXPERIMENT.md):</strong></p>
 * <ul>
 *   <li>Peak aggregation > 60% (baseline ~50-61%)</li>
 *   <li>True factors (11, 13) co-locate in clusters > 70% of runs</li>
 *   <li>At least one strategy shows consistent dominance near factors</li>
 * </ul>
 *
 * <p><strong>REFERENCE:</strong> FIRST_NON_SORTING_EXPERIMENT.md for complete
 * experimental design, controls, and analysis plan.</p>
 */
public class FactorizationExperiment {
    
    /** Target semiprime to factor */
    private final int target;
    
    /** Array size (number of candidates) */
    private final int arraySize;
    
    /** Strategy distribution (percentages) */
    private final Map<FactorStrategy, Double> distribution;
    
    /** Random seed for reproducibility */
    private final long seed;
    
    /** Maximum execution steps */
    private final int maxSteps;
    
    /**
     * Create factorization experiment with configuration.
     *
     * @param target the semiprime to factor
     * @param arraySize number of candidate cells
     * @param distribution strategy distribution
     * @param seed random seed for reproducibility
     * @param maxSteps maximum execution steps
     */
    public FactorizationExperiment(
            int target,
            int arraySize,
            Map<FactorStrategy, Double> distribution,
            long seed,
            int maxSteps) {
        
        this.target = target;
        this.arraySize = arraySize;
        this.distribution = distribution;
        this.seed = seed;
        this.maxSteps = maxSteps;
    }
    
    /**
     * Run single experiment trial.
     *
     * <p><strong>PURPOSE:</strong> Execute one complete run and return results.</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Create chimeric cell population</li>
     *   <li>Record initial state</li>
     *   <li>Execute steps with trajectory recording</li>
     *   <li>Record final state</li>
     *   <li>Return experiment results</li>
     * </ol>
     *
     * @return experiment results with trajectory data
     */
    public ExperimentResults runTrial() {
        // Create cell population
        FactorCellFactory factory = new FactorCellFactory(seed);
        List<FactorCell> cells = factory.createCells(target, arraySize, distribution);
        
        // Create execution engine
        GenericExecutionEngine<Integer, FactorStrategy> engine = new GenericExecutionEngine<>();
        
        // Record trajectory
        List<StepData> trajectory = new ArrayList<>();
        
        // Record initial state
        StepData initialState = recordState(0, cells);
        trajectory.add(initialState);
        
        // Execute with trajectory recording
        int step = 0;
        while (step < maxSteps) {
            int swaps = engine.executeStep(castToAbstractCells(cells));
            step++;
            
            // Record state after each step
            StepData stepData = recordState(step, cells);
            trajectory.add(stepData);
            
            // Convergence detected
            if (swaps == 0) {
                break;
            }
        }
        
        return new ExperimentResults(target, arraySize, distribution, seed, trajectory);
    }
    
    /**
     * Record state at current step.
     *
     * <p><strong>METRICS RECORDED:</strong></p>
     * <ul>
     *   <li>Step number</li>
     *   <li>Aggregation value (clustering)</li>
     *   <li>Average fitness</li>
     *   <li>Max fitness</li>
     *   <li>Perfect factor count (fitness = 1.0)</li>
     *   <li>Perfect factor positions (if any)</li>
     * </ul>
     *
     * @param step the step number
     * @param cells the cell array
     * @return step data record
     */
    private StepData recordState(int step, List<FactorCell> cells) {
        double aggregation = computeAggregation(cells);
        double avgFitness = computeAverageFitness(cells);
        double maxFitness = computeMaxFitness(cells);
        int perfectFactorCount = countPerfectFactors(cells);
        List<Integer> perfectFactorPositions = findPerfectFactorPositions(cells);
        
        return new StepData(
            step,
            aggregation,
            avgFitness,
            maxFitness,
            perfectFactorCount,
            perfectFactorPositions
        );
    }
    
    /**
     * Compute aggregation value (percentage of cells with same-strategy neighbor).
     *
     * <p><strong>FORMULA:</strong> (cells with >= 1 same-strategy neighbor / total cells) × 100</p>
     *
     * <p><strong>REFERENCE:</strong> AlgotypeAggregationIndex.java for Levin formula</p>
     *
     * @param cells the cell array
     * @return aggregation percentage [0.0, 100.0]
     */
    private double computeAggregation(List<FactorCell> cells) {
        if (cells.size() <= 1) {
            return 100.0;
        }
        
        int sameStrategyNeighborCount = 0;
        for (int i = 0; i < cells.size(); i++) {
            FactorStrategy currentStrategy = cells.get(i).readAlgotype();
            
            boolean hasLeftSame = (i > 0) && 
                (cells.get(i - 1).readAlgotype() == currentStrategy);
            boolean hasRightSame = (i < cells.size() - 1) && 
                (cells.get(i + 1).readAlgotype() == currentStrategy);
            
            if (hasLeftSame || hasRightSame) {
                sameStrategyNeighborCount++;
            }
        }
        
        return (sameStrategyNeighborCount * 100.0) / cells.size();
    }
    
    /**
     * Compute average fitness across all cells.
     */
    private double computeAverageFitness(List<FactorCell> cells) {
        return cells.stream()
            .mapToDouble(FactorCell::getFitness)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Find maximum fitness in array.
     */
    private double computeMaxFitness(List<FactorCell> cells) {
        return cells.stream()
            .mapToDouble(FactorCell::getFitness)
            .max()
            .orElse(0.0);
    }
    
    /**
     * Count cells with perfect fitness (true factors).
     */
    private int countPerfectFactors(List<FactorCell> cells) {
        return (int) cells.stream()
            .filter(FactorCell::isPerfectFactor)
            .count();
    }
    
    /**
     * Find positions of perfect factors in array.
     */
    private List<Integer> findPerfectFactorPositions(List<FactorCell> cells) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).isPerfectFactor()) {
                positions.add(i);
            }
        }
        return positions;
    }
    
    /**
     * Cast List<FactorCell> to List<AbstractCell> for generic engine.
     *
     * <p><strong>NOTE:</strong> Safe because FactorCell extends AbstractCell.</p>
     */
    @SuppressWarnings("unchecked")
    private List<AbstractCell<Integer, FactorStrategy>> castToAbstractCells(List<FactorCell> cells) {
        return (List<AbstractCell<Integer, FactorStrategy>>) (List<?>) cells;
    }
    
    /**
     * Data recorded at each step.
     */
    public static class StepData {
        public final int step;
        public final double aggregation;
        public final double avgFitness;
        public final double maxFitness;
        public final int perfectFactorCount;
        public final List<Integer> perfectFactorPositions;
        
        public StepData(
                int step,
                double aggregation,
                double avgFitness,
                double maxFitness,
                int perfectFactorCount,
                List<Integer> perfectFactorPositions) {
            
            this.step = step;
            this.aggregation = aggregation;
            this.avgFitness = avgFitness;
            this.maxFitness = maxFitness;
            this.perfectFactorCount = perfectFactorCount;
            this.perfectFactorPositions = perfectFactorPositions;
        }
    }
    
    /**
     * Experiment results with full trajectory.
     */
    public static class ExperimentResults {
        public final int target;
        public final int arraySize;
        public final Map<FactorStrategy, Double> distribution;
        public final long seed;
        public final List<StepData> trajectory;
        
        public ExperimentResults(
                int target,
                int arraySize,
                Map<FactorStrategy, Double> distribution,
                long seed,
                List<StepData> trajectory) {
            
            this.target = target;
            this.arraySize = arraySize;
            this.distribution = distribution;
            this.seed = seed;
            this.trajectory = trajectory;
        }
        
        /**
         * Export results to CSV file.
         *
         * @param filename output CSV filename
         * @throws IOException if file write fails
         */
        public void exportToCSV(String filename) throws IOException {
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                // Header
                writer.println("step,aggregation,avg_fitness,max_fitness,perfect_factor_count,perfect_factor_positions");
                
                // Data rows
                for (StepData data : trajectory) {
                    writer.printf("%d,%.2f,%.4f,%.4f,%d,\"%s\"\n",
                        data.step,
                        data.aggregation,
                        data.avgFitness,
                        data.maxFitness,
                        data.perfectFactorCount,
                        formatPositions(data.perfectFactorPositions)
                    );
                }
            }
        }
        
        /**
         * Format positions as bracketed list string.
         */
        private String formatPositions(List<Integer> positions) {
            if (positions.isEmpty()) {
                return "[]";
            }
            return positions.toString();
        }
        
        /**
         * Get peak aggregation value from trajectory.
         */
        public double getPeakAggregation() {
            return trajectory.stream()
                .mapToDouble(d -> d.aggregation)
                .max()
                .orElse(0.0);
        }
        
        /**
         * Get step number where peak aggregation occurred.
         */
        public int getPeakAggregationStep() {
            double peak = getPeakAggregation();
            for (StepData data : trajectory) {
                if (Math.abs(data.aggregation - peak) < 0.01) {
                    return data.step;
                }
            }
            return -1;
        }
        
        /**
         * Get final convergence step.
         */
        public int getConvergenceStep() {
            return trajectory.isEmpty() ? 0 : trajectory.get(trajectory.size() - 1).step;
        }
    }
}
