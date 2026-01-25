package com.emergent.doom.domains.graphcoloring;

import java.util.*;

/**
 * Demonstration of H1 validation experiment for graph 3-coloring.
 * 
 * <p><strong>PURPOSE:</strong> Minimal, falsifiable test of hypothesis H1:
 * "Boundary interfaces between algotypes are computational primitives that
 * carry exploitable structure."</p>
 * 
 * <p><strong>EXPERIMENT DESIGN:</strong></p>
 * <ul>
 *   <li>Domain: 3-coloring on Erdős-Rényi graphs (n=20, p≈0.25)</li>
 *   <li>Fitness plateaus: Bucketed violations (bucket size = 2)</li>
 *   <li>4 algotypes: GREEDY_REPAIR, MIN_CONFLICT, RANDOM_WALK, BACKTRACK_LIGHT</li>
 *   <li>Offline recombination at plateaus</li>
 * </ul>
 * 
 * <p><strong>SUCCESS CRITERIA:</strong> Boundary-guided recombination must
 * dominate random controls on improvement metrics, otherwise H1 is falsified.</p>
 */
public class H1ValidationDemo {
    
    private final long seed;
    private final Random random;
    
    /**
     * Create demo experiment with seed.
     * 
     * @param seed random seed for reproducibility
     */
    public H1ValidationDemo(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }
    
    /**
     * Run a simple demonstration of the H1 validation components.
     * 
     * <p>This demonstrates:</p>
     * <ol>
     *   <li>Graph generation with Erdős-Rényi</li>
     *   <li>Population creation with chimeric algotypes</li>
     *   <li>Plateau detection</li>
     *   <li>Boundary analysis</li>
     *   <li>Offline boundary-guided recombination</li>
     * </ol>
     * 
     * @return summary of experiment results
     */
    public String runDemonstration() {
        StringBuilder report = new StringBuilder();
        report.append("=== H1 Validation Demo ===\n");
        report.append("Seed: ").append(seed).append("\n\n");
        
        // 1. Generate graph
        int n = 20;
        double p = 0.25;
        GraphInstance graph = GraphInstance.generateErdosRenyi(n, p, seed);
        report.append("Graph: n=").append(n).append(", m=").append(graph.getNumEdges()).append("\n\n");
        
        // 2. Create chimeric population
        int popSize = 50;
        Map<ColoringAlgotype, Double> distribution = new HashMap<>();
        distribution.put(ColoringAlgotype.GREEDY_REPAIR, 0.25);
        distribution.put(ColoringAlgotype.MIN_CONFLICT, 0.25);
        distribution.put(ColoringAlgotype.RANDOM_WALK, 0.25);
        distribution.put(ColoringAlgotype.BACKTRACK_LIGHT, 0.25);
        
        GraphColoringCellFactory factory = new GraphColoringCellFactory(seed);
        List<GraphColoringCell> population = factory.createCells(distribution, graph, popSize);
        
        report.append("Population: ").append(popSize).append(" cells\n");
        report.append("Distribution: equal (25% each algotype)\n\n");
        
        // 3. Simulate a few improvement steps
        report.append("--- Simulating improvement ---\n");
        for (int step = 0; step < 10; step++) {
            // Apply algotype-specific improvements
            for (int i = 0; i < population.size(); i++) {
                GraphColoringCell cell = population.get(i);
                ColoringState improved = cell.applyImprovementStep();
                
                // Create new cell with improved state (simulating in-place update)
                GraphColoringCell newCell = new GraphColoringCell(
                    improved, cell.readAlgotype(), graph, cell.readCurrentPosition(), random);
                population.set(i, newCell);
            }
            
            // Report current state
            int best = Integer.MAX_VALUE;
            int worst = 0;
            int total = 0;
            
            for (GraphColoringCell cell : population) {
                int v = cell.readValue().getViolations();
                best = Math.min(best, v);
                worst = Math.max(worst, v);
                total += v;
            }
            
            int avg = total / population.size();
            report.append(String.format("Step %d: best=%d, avg=%d, worst=%d\n", 
                step, best, avg, worst));
            
            if (best == 0) {
                report.append("  *** Found valid coloring! ***\n");
                break;
            }
        }
        
        report.append("\n--- Plateau Detection Demo ---\n");
        PlateauDetector detector = new PlateauDetector(5, false);
        
        // Simulate plateau
        for (int i = 0; i < 7; i++) {
            detector.recordTick(10, 12);
        }
        
        report.append("Simulated 7 ticks with violations=10\n");
        report.append("On plateau: ").append(detector.isOnPlateau()).append("\n");
        report.append("Plateau duration: ").append(detector.getPlateauDuration()).append("\n\n");
        
        // 4. Boundary analysis
        report.append("--- Boundary Analysis Demo ---\n");
        ColoringAlgotype[] algotypes = new ColoringAlgotype[population.size()];
        int[] buckets = new int[population.size()];
        
        for (int i = 0; i < population.size(); i++) {
            algotypes[i] = population.get(i).readAlgotype();
            buckets[i] = population.get(i).readValue().getBucket();
        }
        
        BoundaryAnalyzer analyzer = new BoundaryAnalyzer();
        
        // Create simple position history (cells haven't moved much)
        List<int[]> positionHistory = new ArrayList<>();
        for (int t = 0; t < 10; t++) {
            int[] positions = new int[population.size()];
            for (int i = 0; i < positions.length; i++) {
                positions[i] = i;  // Simplified: no movement
            }
            positionHistory.add(positions);
        }
        
        List<BoundaryAnalyzer.Boundary> boundaries = 
            analyzer.findBoundaries(algotypes, positionHistory, buckets);
        
        double aggregation = analyzer.computeAggregation(algotypes);
        
        report.append("Aggregation index: ").append(String.format("%.3f", aggregation)).append("\n");
        report.append("Boundaries found: ").append(boundaries.size()).append("\n");
        
        if (!boundaries.isEmpty()) {
            BoundaryAnalyzer.Boundary top = analyzer.selectTopBoundary(boundaries, 1.0);
            if (top != null) {
                report.append("Top boundary: ").append(top).append("\n");
            }
        }
        
        report.append("\n--- Recombination Demo ---\n");
        if (!boundaries.isEmpty()) {
            BoundaryGuidedRecombination recombinator = 
                new BoundaryGuidedRecombination(graph, random);
            
            BoundaryAnalyzer.Boundary testBoundary = boundaries.get(0);
            BoundaryGuidedRecombination.RecombinationResult result = 
                recombinator.recombineAtBoundary(testBoundary, population);
            
            report.append("Boundary-guided result: ").append(result).append("\n");
            
            // Compare with random cut
            int randomCut = 10 + random.nextInt(30);
            BoundaryGuidedRecombination.RecombinationResult randomResult = 
                recombinator.recombineAtRandomCut(randomCut, population);
            
            report.append("Random cut result: ").append(randomResult).append("\n");
        }
        
        report.append("\n=== Demo Complete ===\n");
        return report.toString();
    }
    
    /**
     * Main entry point for running the demo.
     */
    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 42L;
        
        H1ValidationDemo demo = new H1ValidationDemo(seed);
        String result = demo.runDemonstration();
        
        System.out.println(result);
    }
}
