package com.emergent.doom.sat;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.cell.NeighborhoodView;
import com.emergent.doom.group.CellStatus;
import java.util.*;

/**
 * Full SAT clustering experiment runner (PHASE THREE ITER 3: Implemented).
 *
 * <p><strong>PURPOSE:</strong> Execute SAT clustering experiments with probes for trajectory analysis
 * per §4.4. Integrates SATCell behavioral policies with simple execution loop.</p>
 *
 * <p><strong>EXECUTION FLOW:</strong></p>
 * <ul>
 *   <li>Generate formula and chimeric array</li>
 *   <li>Run steps with SATCell swap decisions</li>
 *   <li>Probe metrics at each step (aggregation, satisfaction, density)</li>
 *   <li>Stop at solution or max steps</li>
 * </ul>
 */
public class SATExperiment {

    private final List<AbstractCell<Integer, SATStrategy>> cells;
    private final CNFFormula formula;
    private final SATMetrics metrics;
    private int stepCount;
    private int maxSteps;
    private boolean solved;
    private List<Double> aggregationTrajectory;
    private List<Double> satisfactionTrajectory;

    public SATExperiment(CNFFormula formula, int arraySize, long seed) {
        this.formula = formula;
        this.cells = new ArrayList<>();
        this.metrics = new SATMetrics();
        this.stepCount = 0;
        this.maxSteps = 1000; // Default
        this.solved = false;
        this.aggregationTrajectory = new ArrayList<>();
        this.satisfactionTrajectory = new ArrayList<>();
        initializeArray(arraySize, seed);
    }

    private void initializeArray(int size, long seed) {
        AssignmentGenerator gen = new AssignmentGenerator(seed);
        SATCellFactory factory = new SATCellFactory(gen);
        
        // Generate cells with 30/30/40 distribution
        Map<SATStrategy, Double> dist = factory.getDefaultDistribution();
        int dpllCount = (int) (size * dist.get(SATStrategy.DPLL));
        int greedyCount = (int) (size * dist.get(SATStrategy.GREEDY_MCV));
        int walksatCount = (int) (size * dist.get(SATStrategy.WALKSAT));
        int hybridCount = size - (dpllCount + greedyCount + walksatCount);
        
        // Create cells
        for (int i = 0; i < dpllCount; i++) {
            var assignment = gen.generate(formula, SATStrategy.DPLL);
            cells.add(new SATCell(assignment, formula, SATStrategy.DPLL, i));
        }
        for (int i = 0; i < greedyCount; i++) {
            var assignment = gen.generate(formula, SATStrategy.GREEDY_MCV);
            cells.add(new SATCell(assignment, formula, SATStrategy.GREEDY_MCV, i));
        }
        for (int i = 0; i < walksatCount; i++) {
            var assignment = gen.generate(formula, SATStrategy.WALKSAT);
            cells.add(new SATCell(assignment, formula, SATStrategy.WALKSAT, i));
        }
        for (int i = 0; i < hybridCount; i++) {
            var assignment = gen.generate(formula, SATStrategy.HYBRID);
            cells.add(new SATCell(assignment, formula, SATStrategy.HYBRID, i));
        }
        
        // Shuffle positions for initial randomness
        Collections.shuffle(cells, new Random(seed));
        // Update positions after shuffle
        for (int i = 0; i < cells.size(); i++) {
            cells.get(i).updatePositionTo(i);
        }
        
        System.out.println("Initialized " + size + " cells: DPLL=" + dpllCount + ", GREEDY=" + greedyCount + 
                          ", WALKSAT=" + walksatCount + ", HYBRID=" + hybridCount);
    }

    public void run(int maxSteps) {
        this.maxSteps = maxSteps;
        List<Double> densityTrajectory = new ArrayList<>();
        
        while (stepCount < maxSteps && !isSolved()) {
            int swaps = executeStep();
            stepCount++;
            
            // Probe metrics
            double agg = computeAggregation();
            double sat = computeAverageSatisfaction();
            double dens = metrics.computeOverallDensity(formula);
            
            aggregationTrajectory.add(agg);
            satisfactionTrajectory.add(sat);
            densityTrajectory.add(dens);
            
            System.out.printf("Step %d: swaps=%d, agg=%.2f, sat=%.1f%%, dens=%.3f%n", 
                stepCount, swaps, agg, sat, dens);
            
            if (swaps == 0) break; // Stable
        }
        
        solved = isSolved();
        System.out.println("Experiment complete: " + stepCount + " steps, solved=" + solved);
        System.out.println("Final aggregation: " + aggregationTrajectory.get(aggregationTrajectory.size() - 1));
        System.out.println("Max satisfaction: " + Collections.max(satisfactionTrajectory));
        
        // Output trajectories for analysis
        metrics.recordTrajectory(aggregationTrajectory, satisfactionTrajectory, densityTrajectory);
    }

    private int executeStep() {
        int swaps = 0;
        for (int i = 0; i < cells.size(); i++) {
            SATCell cell = (SATCell) cells.get(i);
            if (!cell.canInitiateSwap()) continue;
            
            // Build neighborhood view (simple adjacent for SAT)
            List<AbstractCell<Integer, SATStrategy>> visible = new ArrayList<>();
            List<Integer> positions = new ArrayList<>();
            if (i > 0) {
                visible.add(cells.get(i - 1));
                positions.add(i - 1);
            }
            if (i < cells.size() - 1) {
                visible.add(cells.get(i + 1));
                positions.add(i + 1);
            }
            NeighborhoodView<Integer, SATStrategy> neighbors = new SimpleSATNeighborhoodView(cell, i, cells.size(), visible, positions);
            
            if (!cell.shouldMoveGiven(neighbors)) continue;
            
            Optional<Integer> targetOpt = cell.calculateTargetPositionGiven(neighbors);
            if (targetOpt.isEmpty()) continue;
            
            int targetPos = targetOpt.get();
            if (targetPos < 0 || targetPos >= cells.size() || targetPos == i) continue;
            
            AbstractCell<Integer, SATStrategy> targetCell = cells.get(targetPos);
            if (!targetCell.canAcceptSwapFrom(cell)) continue;
            
            // Execute swap
            Collections.swap(cells, i, targetPos);
            cell.updatePositionTo(targetPos);
            ((SATCell) targetCell).updatePositionTo(i);
            swaps++;
        }
        return swaps;
    }



    private double computeAverageSatisfaction() {
        double total = 0.0;
        for (AbstractCell<Integer, SATStrategy> cell : cells) {
            total += cell.readValue();
        }
        return total / cells.size();
    }

    private double computeAggregation() {
        if (cells.isEmpty()) return 0.0;
        
        long totalCells = cells.size();
        Map<SATStrategy, Integer> counts = new HashMap<>();
        for (AbstractCell<Integer, SATStrategy> cell : cells) {
            SATStrategy strat = cell.readAlgotype();
            counts.put(strat, counts.getOrDefault(strat, 0) + 1);
        }
        
        // Simple aggregation: max strategy proportion
        double maxProp = 0.0;
        for (int count : counts.values()) {
            double prop = (double) count / totalCells;
            if (prop > maxProp) maxProp = prop;
        }
        return maxProp;
    }

    public int getStepCount() { return stepCount; }
    public boolean isSolved() { return solved; }
    public List<AbstractCell<Integer, SATStrategy>> getCells() { return cells; }
    public double getMaxAggregation() {
        if (aggregationTrajectory.isEmpty()) return 0.0;
        return Collections.max(aggregationTrajectory);
    }
    public double getFinalSatisfaction() {
        if (satisfactionTrajectory.isEmpty()) return 0.0;
        return satisfactionTrajectory.get(satisfactionTrajectory.size() - 1);
    }
}

/**
 * Simple neighborhood view for SAT cells (PHASE THREE ITER 3: Adapter for adjacent neighbors).
 */
class SimpleSATNeighborhoodView extends NeighborhoodView<Integer, SATStrategy> {

    public SimpleSATNeighborhoodView(AbstractCell<Integer, SATStrategy> self, int position, int arraySize,
                                   List<AbstractCell<Integer, SATStrategy>> visibleNeighbors, List<Integer> visiblePositions) {
        super(self, position, arraySize, visibleNeighbors, visiblePositions);
    }
}
