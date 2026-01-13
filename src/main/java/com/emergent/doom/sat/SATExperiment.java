package com.emergent.doom.sat;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.cell.NeighborhoodView;
import com.emergent.doom.group.CellStatus;
import com.emergent.doom.metrics.AlgotypeAggregationIndex;
import com.emergent.doom.probe.StepSnapshot;
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

    public SATExperiment(CNFFormula formula, int arraySize, long seed) {
        this.formula = formula;
        this.cells = new ArrayList<>();
        this.metrics = new SATMetrics();
        this.stepCount = 0;
        this.maxSteps = 1000; // Default
        this.solved = false;
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
        List<Double> aggregationTrajectory = new ArrayList<>();
        List<Double> satisfactionTrajectory = new ArrayList<>();
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
            NeighborhoodView<Integer, SATStrategy> neighbors = 
                new SimpleSATNeighborhoodView(cell, i, cells.size(), visible, positions);
            
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

    private boolean isSolved() {
        for (AbstractCell<Integer, SATStrategy> cell : cells) {
            if (((SATCell) cell).isSolution()) {
                return true;
            }
        }
        return false;
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
}

/**
 * Simple neighborhood view for SAT cells (PHASE THREE ITER 3: Adapter for adjacent neighbors).
 */
class SimpleSATNeighborhoodView implements NeighborhoodView<Integer, SATStrategy> {

    private final AbstractCell<Integer, SATStrategy> self;
    private final int position;
    private final int arraySize;
    private final List<AbstractCell<Integer, SATStrategy>> visibleNeighbors;
    private final List<Integer> visiblePositions;

    public SimpleSATNeighborhoodView(AbstractCell<Integer, SATStrategy> self, int position, int arraySize,
                                   List<AbstractCell<Integer, SATStrategy>> visibleNeighbors, List<Integer> visiblePositions) {
        this.self = self;
        this.position = position;
        this.arraySize = arraySize;
        this.visibleNeighbors = visibleNeighbors;
        this.visiblePositions = visiblePositions;
    }

    @Override
    public AbstractCell<Integer, SATStrategy> getSelf() { return self; }
    
    @Override
    public int getCurrentPosition() { return position; }
    
    @Override
    public int getArraySize() { return arraySize; }
    
    @Override
    public List<AbstractCell<Integer, SATStrategy>> getVisibleNeighbors() { return visibleNeighbors; }
    
    @Override
    public List<Integer> getVisiblePositions() { return visiblePositions; }
    
    @Override
    public Optional<AbstractCell<Integer, SATStrategy>> getLeftNeighbor() {
        if (position > 0) {
            return Optional.ofNullable(visibleNeighbors.get(0)); // Assume first is left
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<AbstractCell<Integer, SATStrategy>> getRightNeighbor() {
        if (position < arraySize - 1) {
            return Optional.ofNullable(visibleNeighbors.get(1)); // Assume second is right
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<AbstractCell<Integer, SATStrategy>> getCellAtPosition(int pos) {
        if (pos < 0 || pos >= arraySize) return Optional.empty();
        // Simple: return if in visible
        for (int i = 0; i < visiblePositions.size(); i++) {
            if (visiblePositions.get(i) == pos) {
                return Optional.of(visibleNeighbors.get(i));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<AbstractCell<Integer, SATStrategy>> getNeighborAtDistance(int distance) {
        int target = position + distance;
        return getCellAtPosition(target);
    }
}

    /**
     * Initialize chimeric array with strategy distribution (PHASE THREE ITER 3).
     */
    private void initializeArray(int size, SATStrategyConfig config, long seed) {
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
            cells.add(new SATCell(assignment, formula, SATStrategy.DPLL, i, config));
        }
        for (int i = 0; i < greedyCount; i++) {
            var assignment = gen.generate(formula, SATStrategy.GREEDY_MCV);
            cells.add(new SATCell(assignment, formula, SATStrategy.GREEDY_MCV, i, config));
        }
        for (int i = 0; i < walksatCount; i++) {
            var assignment = gen.generate(formula, SATStrategy.WALKSAT);
            cells.add(new SATCell(assignment, formula, SATStrategy.WALKSAT, i, config));
        }
        for (int i = 0; i < hybridCount; i++) {
            var assignment = gen.generate(formula, SATStrategy.HYBRID);
            cells.add(new SATCell(assignment, formula, SATStrategy.HYBRID, i, config));
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

    /**
     * Run experiment until solution or max steps (PHASE THREE ITER 3).
     *
     * <p><strong>PROBING:</strong> Record aggregation, satisfaction, density at each step.</p>
     */
    public void run(int maxSteps) {
        this.maxSteps = maxSteps;
        List<Double> aggregationTrajectory = new ArrayList<>();
        List<Double> satisfactionTrajectory = new ArrayList<>();
        List<Double> densityTrajectory = new ArrayList<>();
        
        while (stepCount < maxSteps && !isSolved()) {
            int swaps = executeStep();
            stepCount++;
            
            // Probe metrics
            double agg = aggIndex.compute(cells.toArray(new AbstractCell[0]));
            double sat = computeAverageSatisfaction();
            double dens = densityAnalyzer.computeOverallDensity(formula);
            
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

    /**
     * Execute one step: simple sequential swap execution (PHASE THREE ITER 3).
     */
    private int executeStep() {
        int swaps = 0;
        for (int i = 0; i < cells.size(); i++) {
            SATCell cell = (SATCell) cells.get(i);
            if (!cell.canInitiateSwap()) continue;
            
            // Build neighborhood view (simple all-cells for now)
            NeighborhoodView<Integer, SATStrategy> neighbors = 
                new SimpleNeighborhoodView(cell, i, cells);
            
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

    /**
     * Check if experiment solved (any cell has 100% satisfaction).
     */
    private boolean isSolved() {
        for (AbstractCell<Integer, SATStrategy> cell : cells) {
            if (((SATCell) cell).isSolution()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compute average satisfaction across all cells.
     */
    private double computeAverageSatisfaction() {
        double total = 0.0;
        for (AbstractCell<Integer, SATStrategy> cell : cells) {
            total += cell.readValue();
        }
        return total / cells.size();
    }

    public int getStepCount() { return stepCount; }
    public boolean isSolved() { return solved; }
    public List<AbstractCell<Integer, SATStrategy>> getCells() { return cells; }
}

/**
 * Simple neighborhood view for SAT cells (PHASE THREE ITER 3: Adapter).
 */
class SimpleNeighborhoodView implements NeighborhoodView<Integer, SATStrategy> {

    private final AbstractCell<Integer, SATStrategy> self;
    private final int position;
    private final int arraySize;
    private final List<AbstractCell<Integer, SATStrategy>> allCells;

    public SimpleNeighborhoodView(AbstractCell<Integer, SATStrategy> self, int position, List<AbstractCell<Integer, SATStrategy>> cells) {
        this.self = self;
        this.position = position;
        this.arraySize = cells.size();
        this.allCells = cells;
    }

    @Override
    public AbstractCell<Integer, SATStrategy> getSelf() { return self; }
    @Override
    public int getCurrentPosition() { return position; }
    @Override
    public int getArraySize() { return arraySize; }

    @Override
    public Optional<AbstractCell<Integer, SATStrategy>> getLeftNeighbor() {
        if (position > 0) {
            return Optional.of(allCells.get(position - 1));
        }
        return Optional.empty();
    }

    @Override
    public Optional<AbstractCell<Integer, SATStrategy>> getRightNeighbor() {
        if (position < arraySize - 1) {
            return Optional.of(allCells.get(position + 1));
        }
        return Optional.empty();
    }

    @Override
    public Optional<AbstractCell<Integer, SATStrategy>> getCellAtPosition(int pos) {
        if (pos < 0 || pos >= arraySize) return Optional.empty();
        return Optional.of(allCells.get(pos));
    }

    @Override
    public List<AbstractCell<Integer, SATStrategy>> getVisibleNeighbors() { return allCells; }
    @Override
    public List<Integer> getVisiblePositions() {
        List<Integer> pos = new ArrayList<>();
        for (int i = 0; i < arraySize; i++) pos.add(i);
        return pos;
    }
}