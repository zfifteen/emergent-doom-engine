package com.emergent.doom.domains.graphcoloring;

import java.util.*;

/**
 * Analyzes algotype boundaries and mobility gradients in graph coloring populations.
 * 
 * <p><strong>PURPOSE:</strong> Extract boundary locations and mobility gradients
 * for H1 validation experiments.</p>
 * 
 * <p><strong>OBSERVABLES:</strong></p>
 * <ul>
 *   <li>Aggregation: % adjacent pairs sharing same algotype</li>
 *   <li>Boundaries: indices where algotype[i] != algotype[i+1]</li>
 *   <li>Mobility gradient: |meanMob(left) - meanMob(right)| at boundary</li>
 * </ul>
 */
public class BoundaryAnalyzer {
    
    /**
     * Represents a detected boundary between algotype regions.
     */
    public static class Boundary {
        public final int index;  // Index where boundary occurs
        public final ColoringAlgotype leftAlgotype;
        public final ColoringAlgotype rightAlgotype;
        public final double mobilityGradient;
        public final double fitnessGradient;
        
        public Boundary(int index, ColoringAlgotype left, ColoringAlgotype right, 
                       double mobilityGradient, double fitnessGradient) {
            this.index = index;
            this.leftAlgotype = left;
            this.rightAlgotype = right;
            this.mobilityGradient = mobilityGradient;
            this.fitnessGradient = fitnessGradient;
        }
        
        @Override
        public String toString() {
            return String.format("Boundary{idx=%d, %s|%s, mobGrad=%.3f, fitGrad=%.3f}",
                index, leftAlgotype, rightAlgotype, mobilityGradient, fitnessGradient);
        }
    }
    
    private final int mobilityWindow;
    private final int boundaryWindow;
    
    /**
     * Create boundary analyzer.
     * 
     * @param mobilityWindow ticks to average for mobility (default: 10)
     * @param boundaryWindow cells on each side of boundary for gradients (default: 5)
     */
    public BoundaryAnalyzer(int mobilityWindow, int boundaryWindow) {
        if (mobilityWindow <= 0) {
            throw new IllegalArgumentException("Mobility window must be positive");
        }
        if (boundaryWindow <= 0) {
            throw new IllegalArgumentException("Boundary window must be positive");
        }
        
        this.mobilityWindow = mobilityWindow;
        this.boundaryWindow = boundaryWindow;
    }
    
    /**
     * Create analyzer with defaults (mobility=10, boundary=5).
     */
    public BoundaryAnalyzer() {
        this(10, 5);
    }
    
    /**
     * Calculate algotype aggregation index.
     * 
     * <p>Aggregation = (# adjacent same-algotype pairs) / (# total adjacent pairs)</p>
     * 
     * @param algotypes array of algotypes in current order
     * @return aggregation percentage [0, 1]
     */
    public double computeAggregation(ColoringAlgotype[] algotypes) {
        if (algotypes == null || algotypes.length < 2) {
            return 0.0;
        }
        
        int sameCount = 0;
        int totalPairs = algotypes.length - 1;
        
        for (int i = 0; i < totalPairs; i++) {
            if (algotypes[i] == algotypes[i + 1]) {
                sameCount++;
            }
        }
        
        return (double) sameCount / totalPairs;
    }
    
    /**
     * Find all boundaries in population.
     * 
     * <p>Boundary = position where algotype[i] != algotype[i+1].</p>
     * 
     * @param algotypes array of algotypes
     * @param positions position history (last mobilityWindow ticks)
     * @param buckets fitness buckets for gradient calculation
     * @return list of detected boundaries with gradients
     */
    public List<Boundary> findBoundaries(
            ColoringAlgotype[] algotypes,
            List<int[]> positions,  // History of positions [tick][cellIndex]
            int[] buckets) {
        
        if (algotypes == null || algotypes.length < 2) {
            return Collections.emptyList();
        }
        
        List<Boundary> boundaries = new ArrayList<>();
        
        for (int i = 0; i < algotypes.length - 1; i++) {
            if (algotypes[i] != algotypes[i + 1]) {
                // Found boundary at index i
                double mobGrad = computeMobilityGradient(i, positions);
                double fitGrad = computeFitnessGradient(i, buckets);
                
                boundaries.add(new Boundary(i, algotypes[i], algotypes[i + 1], 
                                           mobGrad, fitGrad));
            }
        }
        
        return boundaries;
    }
    
    /**
     * Compute mobility gradient across boundary at index.
     * 
     * <p>Mobility = average position change over last mobilityWindow ticks.
     * Gradient = |meanMob(left) - meanMob(right)|</p>
     * 
     * @param boundaryIndex index of boundary
     * @param positions position history
     * @return mobility gradient
     */
    private double computeMobilityGradient(int boundaryIndex, List<int[]> positions) {
        if (positions == null || positions.size() < 2) {
            return 0.0;
        }
        
        // Get mobility for left and right windows
        double leftMobility = computeWindowMobility(boundaryIndex, true, positions);
        double rightMobility = computeWindowMobility(boundaryIndex, false, positions);
        
        return Math.abs(leftMobility - rightMobility);
    }
    
    /**
     * Compute average mobility for cells in window around boundary.
     * 
     * @param boundaryIndex boundary position
     * @param isLeft if true, compute for left side; else right side
     * @param positions position history
     * @return average mobility
     */
    private double computeWindowMobility(int boundaryIndex, boolean isLeft, 
                                        List<int[]> positions) {
        if (positions.isEmpty()) {
            return 0.0;
        }
        
        int[] currentPos = positions.get(positions.size() - 1);
        int arraySize = currentPos.length;
        
        // Determine window cells
        int start, end;
        if (isLeft) {
            start = Math.max(0, boundaryIndex - boundaryWindow + 1);
            end = boundaryIndex + 1;
        } else {
            start = boundaryIndex + 1;
            end = Math.min(arraySize, boundaryIndex + 1 + boundaryWindow);
        }
        
        // Calculate average mobility for cells in window
        double totalMobility = 0.0;
        int count = 0;
        
        for (int cellIdx = start; cellIdx < end; cellIdx++) {
            double mobility = computeCellMobility(cellIdx, positions);
            totalMobility += mobility;
            count++;
        }
        
        return count > 0 ? totalMobility / count : 0.0;
    }
    
    /**
     * Compute mobility for a single cell.
     * 
     * <p>Mobility = average |pos[t] - pos[t-1]| over last mobilityWindow ticks.</p>
     * 
     * @param cellIdx cell index in current array
     * @param positions position history
     * @return average position change
     */
    private double computeCellMobility(int cellIdx, List<int[]> positions) {
        int ticksToUse = Math.min(mobilityWindow, positions.size());
        if (ticksToUse < 2) {
            return 0.0;
        }
        
        // Track cell across ticks (need to find which position it moved to)
        // For simplicity, use current position and measure variance
        double totalMovement = 0.0;
        int movementCount = 0;
        
        for (int t = positions.size() - ticksToUse; t < positions.size() - 1; t++) {
            int[] pos = positions.get(t);
            int[] nextPos = positions.get(t + 1);
            
            if (cellIdx < pos.length && cellIdx < nextPos.length) {
                totalMovement += Math.abs(nextPos[cellIdx] - pos[cellIdx]);
                movementCount++;
            }
        }
        
        return movementCount > 0 ? totalMovement / movementCount : 0.0;
    }
    
    /**
     * Compute fitness gradient across boundary.
     * 
     * <p>Gradient = |meanBucket(left) - meanBucket(right)|</p>
     * 
     * @param boundaryIndex boundary position
     * @param buckets fitness buckets for all cells
     * @return fitness gradient
     */
    private double computeFitnessGradient(int boundaryIndex, int[] buckets) {
        if (buckets == null || buckets.length == 0) {
            return 0.0;
        }
        
        // Get mean bucket for left and right windows
        double leftMean = computeWindowMeanBucket(boundaryIndex, true, buckets);
        double rightMean = computeWindowMeanBucket(boundaryIndex, false, buckets);
        
        return Math.abs(leftMean - rightMean);
    }
    
    /**
     * Compute mean bucket for window around boundary.
     */
    private double computeWindowMeanBucket(int boundaryIndex, boolean isLeft, int[] buckets) {
        int start, end;
        if (isLeft) {
            start = Math.max(0, boundaryIndex - boundaryWindow + 1);
            end = boundaryIndex + 1;
        } else {
            start = boundaryIndex + 1;
            end = Math.min(buckets.length, boundaryIndex + 1 + boundaryWindow);
        }
        
        double sum = 0.0;
        int count = 0;
        for (int i = start; i < end; i++) {
            sum += buckets[i];
            count++;
        }
        
        return count > 0 ? sum / count : 0.0;
    }
    
    /**
     * Find boundary with highest mobility gradient and low fitness gradient.
     * 
     * <p>For H1 validation: select boundary where strategies differ in mobility
     * but not in fitness (both on same plateau).</p>
     * 
     * @param boundaries list of detected boundaries
     * @param maxFitnessGradient maximum allowed fitness gradient (default: 0.5)
     * @return boundary with highest mobility gradient, or null if none found
     */
    public Boundary selectTopBoundary(List<Boundary> boundaries, double maxFitnessGradient) {
        return boundaries.stream()
            .filter(b -> b.fitnessGradient <= maxFitnessGradient)
            .max(Comparator.comparingDouble(b -> b.mobilityGradient))
            .orElse(null);
    }
    
    /**
     * Select random boundary (control experiment).
     * 
     * @param boundaries list of boundaries
     * @param random Random instance
     * @return random boundary, or null if list empty
     */
    public Boundary selectRandomBoundary(List<Boundary> boundaries, Random random) {
        if (boundaries.isEmpty()) {
            return null;
        }
        return boundaries.get(random.nextInt(boundaries.size()));
    }
}
