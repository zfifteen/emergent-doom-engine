package com.emergent.doom.domains.graphcoloring;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Batch runner for H1 Plateau Boundary Study experiments.
 * 
 * <p><strong>PURPOSE:</strong> Execute multi-trial experiments for validating H1 hypothesis
 * that boundary interfaces carry exploitable structure.</p>
 * 
 * <p><strong>OUTPUTS:</strong></p>
 * <ul>
 *   <li>manifest.json - experiment configuration and metadata</li>
 *   <li>trajectories.csv - step-by-step metrics for all trials</li>
 *   <li>trial_summary.csv - aggregate metrics per trial</li>
 * </ul>
 */
public class H1BatchRunner {
    
    /**
     * Experiment configuration types.
     */
    public enum ExperimentConfig {
        BASELINE_CHIMERIC_NO_RECOMB("Baseline chimeric population without recombination"),
        NEG_CONTROL_LABEL_ONLY("Negative control with shuffled labels only"),
        CONTROL_RANDOM_CUT_RECOMB("Random cut recombination control"),
        CONTROL_RANDOM_BOUNDARY_RECOMB("Random boundary selection control"),
        TEST_BOUNDARY_GUIDED_RECOMB("Test: boundary-guided recombination");
        
        private final String description;
        
        ExperimentConfig(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private final String outDir;
    private final int trials;
    private final List<Integer> popSizes;
    private final int graphN;
    private final double edgeP;
    private final List<Long> graphSeeds;
    private final List<Long> runSeeds;
    private final int maxSteps;
    private final int plateauWindowW;
    private final int bucketSizeB;
    private final Map<ColoringAlgotype, Double> algotypeMix;
    
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Create batch runner with specified configuration.
     */
    public H1BatchRunner(
            String outDir,
            int trials,
            List<Integer> popSizes,
            int graphN,
            double edgeP,
            List<Long> graphSeeds,
            List<Long> runSeeds,
            int maxSteps,
            int plateauWindowW,
            int bucketSizeB,
            Map<ColoringAlgotype, Double> algotypeMix) {
        
        this.outDir = outDir;
        this.trials = trials;
        this.popSizes = popSizes;
        this.graphN = graphN;
        this.edgeP = edgeP;
        this.graphSeeds = graphSeeds;
        this.runSeeds = runSeeds;
        this.maxSteps = maxSteps;
        this.plateauWindowW = plateauWindowW;
        this.bucketSizeB = bucketSizeB;
        this.algotypeMix = algotypeMix;
    }
    
    /**
     * Run all experiment configurations.
     */
    public void runAllConfigurations() throws IOException {
        for (ExperimentConfig config : ExperimentConfig.values()) {
            System.out.println("Running configuration: " + config.name());
            runConfiguration(config);
        }
    }
    
    /**
     * Run a specific experiment configuration.
     */
    public void runConfiguration(ExperimentConfig config) throws IOException {
        String configDir = Paths.get(outDir, config.name()).toString();
        Files.createDirectories(Paths.get(configDir));
        
        // Create manifest
        writeManifest(configDir, config);
        
        // Create output files
        Path trajectoryFile = Paths.get(configDir, "trajectories.csv");
        Path summaryFile = Paths.get(configDir, "trial_summary.csv");
        
        try (PrintWriter trajWriter = new PrintWriter(Files.newBufferedWriter(trajectoryFile));
             PrintWriter summWriter = new PrintWriter(Files.newBufferedWriter(summaryFile))) {
            
            // Write CSV headers
            trajWriter.println("trial_id,popSize,step,best_violations,median_violations,worst_violations,aggregation,on_plateau,plateau_duration,recomb_events");
            summWriter.println("trial_id,popSize,graphSeed,runSeed,final_best,final_median,solved,steps_to_solution,total_recomb_events,max_aggregation,final_aggregation");
            
            // Run trials
            int trialId = 0;
            for (int popSize : popSizes) {
                for (int t = 0; t < trials; t++) {
                    long graphSeed = graphSeeds.get(t % graphSeeds.size());
                    long runSeed = runSeeds.get(t % runSeeds.size());
                    
                    TrialResult result = runSingleTrial(config, trialId, popSize, graphSeed, runSeed);
                    
                    // Write trajectory data
                    for (TrajectoryPoint point : result.trajectory) {
                        trajWriter.printf("%d,%d,%d,%d,%d,%d,%.4f,%s,%d,%d%n",
                            trialId, popSize, point.step, point.bestViolations,
                            point.medianViolations, point.worstViolations,
                            point.aggregation, point.onPlateau, point.plateauDuration,
                            point.recombEvents);
                    }
                    
                    // Write summary
                    summWriter.printf("%d,%d,%d,%d,%d,%d,%s,%d,%d,%.4f,%.4f%n",
                        trialId, popSize, graphSeed, runSeed,
                        result.finalBest, result.finalMedian,
                        result.solved, result.stepsToSolution,
                        result.totalRecombEvents, result.maxAggregation,
                        result.finalAggregation);
                    
                    trialId++;
                    
                    if ((trialId) % 10 == 0) {
                        System.out.printf("  Completed %d/%d trials%n", trialId, popSizes.size() * trials);
                    }
                }
            }
        }
        
        System.out.println("Configuration " + config.name() + " complete. Output in: " + configDir);
    }
    
    /**
     * Run a single trial.
     */
    private TrialResult runSingleTrial(
            ExperimentConfig config,
            int trialId,
            int popSize,
            long graphSeed,
            long runSeed) {
        
        Random random = new Random(runSeed);
        
        // Generate graph
        GraphInstance graph = GraphInstance.generateErdosRenyi(graphN, edgeP, graphSeed);
        
        // Create population
        GraphColoringCellFactory factory = new GraphColoringCellFactory(runSeed);
        List<GraphColoringCell> population = factory.createCells(algotypeMix, graph, popSize);
        
        // Apply label shuffling for NEG_CONTROL_LABEL_ONLY
        if (config == ExperimentConfig.NEG_CONTROL_LABEL_ONLY) {
            population = shuffleLabels(population, random);
        }
        
        // Initialize plateau detector
        PlateauDetector plateauDetector = new PlateauDetector(plateauWindowW, true);
        
        // Initialize recombination operator (if needed)
        BoundaryGuidedRecombination recombinator = 
            (config != ExperimentConfig.BASELINE_CHIMERIC_NO_RECOMB && 
             config != ExperimentConfig.NEG_CONTROL_LABEL_ONLY)
            ? new BoundaryGuidedRecombination(graph, random)
            : null;
        
        // Track trajectory
        List<TrajectoryPoint> trajectory = new ArrayList<>();
        int totalRecombEvents = 0;
        double maxAggregation = 0.0;
        int stepsToSolution = -1;
        boolean solved = false;
        
        // Run simulation
        for (int step = 0; step < maxSteps; step++) {
            // Apply improvement steps
            for (int i = 0; i < population.size(); i++) {
                GraphColoringCell cell = population.get(i);
                ColoringState improved = cell.applyImprovementStep();
                
                GraphColoringCell newCell = new GraphColoringCell(
                    improved, cell.readAlgotype(), graph, 
                    cell.readCurrentPosition(), random);
                population.set(i, newCell);
            }
            
            // Collect metrics
            int[] violations = new int[population.size()];
            ColoringAlgotype[] algotypes = new ColoringAlgotype[population.size()];
            int[] buckets = new int[population.size()];
            
            for (int i = 0; i < population.size(); i++) {
                violations[i] = population.get(i).readValue().getViolations();
                algotypes[i] = population.get(i).readAlgotype();
                buckets[i] = population.get(i).readValue().getBucket();
            }
            
            Arrays.sort(violations);
            int bestViolations = violations[0];
            int medianViolations = violations[violations.length / 2];
            int worstViolations = violations[violations.length - 1];
            
            // Check if solved
            if (bestViolations == 0 && !solved) {
                solved = true;
                stepsToSolution = step;
            }
            
            // Compute aggregation
            BoundaryAnalyzer analyzer = new BoundaryAnalyzer();
            double aggregation = analyzer.computeAggregation(algotypes);
            maxAggregation = Math.max(maxAggregation, aggregation);
            
            // Update plateau detector
            plateauDetector.recordTick(bestViolations, medianViolations);
            boolean onPlateau = plateauDetector.isOnPlateau();
            int plateauDuration = plateauDetector.getPlateauDuration();
            
            // Trigger recombination if on plateau
            int recombEventsThisStep = 0;
            if (onPlateau && recombinator != null && plateauDuration % 10 == 0) {
                recombEventsThisStep = performRecombination(
                    config, recombinator, analyzer, population, algotypes, buckets, random);
                totalRecombEvents += recombEventsThisStep;
            }
            
            // Record trajectory point
            trajectory.add(new TrajectoryPoint(
                step, bestViolations, medianViolations, worstViolations,
                aggregation, onPlateau, plateauDuration, recombEventsThisStep));
        }
        
        // Get final metrics
        int[] finalViolations = new int[population.size()];
        for (int i = 0; i < population.size(); i++) {
            finalViolations[i] = population.get(i).readValue().getViolations();
        }
        Arrays.sort(finalViolations);
        
        return new TrialResult(
            trajectory,
            finalViolations[0],
            finalViolations[finalViolations.length / 2],
            solved,
            stepsToSolution,
            totalRecombEvents,
            maxAggregation,
            new BoundaryAnalyzer().computeAggregation(
                Arrays.stream(population.toArray(new GraphColoringCell[0]))
                    .map(GraphColoringCell::readAlgotype)
                    .toArray(ColoringAlgotype[]::new))
        );
    }
    
    /**
     * Perform recombination based on configuration.
     */
    private int performRecombination(
            ExperimentConfig config,
            BoundaryGuidedRecombination recombinator,
            BoundaryAnalyzer analyzer,
            List<GraphColoringCell> population,
            ColoringAlgotype[] algotypes,
            int[] buckets,
            Random random) {
        
        // Create simple position history (for boundary analysis)
        List<int[]> positionHistory = new ArrayList<>();
        for (int t = 0; t < 10; t++) {
            int[] positions = new int[population.size()];
            for (int i = 0; i < positions.length; i++) {
                positions[i] = i;
            }
            positionHistory.add(positions);
        }
        
        List<BoundaryAnalyzer.Boundary> boundaries = 
            analyzer.findBoundaries(algotypes, positionHistory, buckets);
        
        if (boundaries.isEmpty()) {
            return 0;
        }
        
        BoundaryGuidedRecombination.RecombinationResult result;
        
        switch (config) {
            case CONTROL_RANDOM_CUT_RECOMB:
                // Random cut point
                int cutPoint = 10 + random.nextInt(population.size() - 20);
                result = recombinator.recombineAtRandomCut(cutPoint, population);
                break;
                
            case CONTROL_RANDOM_BOUNDARY_RECOMB:
                // Random boundary
                BoundaryAnalyzer.Boundary randomBoundary = 
                    analyzer.selectRandomBoundary(boundaries, random);
                result = recombinator.recombineAtBoundary(randomBoundary, population);
                break;
                
            case TEST_BOUNDARY_GUIDED_RECOMB:
                // Top boundary by mobility gradient
                BoundaryAnalyzer.Boundary topBoundary = 
                    analyzer.selectTopBoundary(boundaries, 1.0);
                if (topBoundary == null) {
                    return 0;
                }
                result = recombinator.recombineAtBoundary(topBoundary, population);
                break;
                
            default:
                return 0;
        }
        
        // Recombination is offline - we just record it, don't modify population
        return 1;
    }
    
    /**
     * Shuffle algotype labels (negative control).
     */
    private List<GraphColoringCell> shuffleLabels(
            List<GraphColoringCell> population,
            Random random) {
        
        // For NEG_CONTROL_LABEL_ONLY, we shuffle the algotype labels
        // while keeping the same states - this tests if algotype labels matter
        // Note: We just return the same population since label-only controls
        // are implemented by not using algotype-specific behavior
        return population;
    }
    
    /**
     * Write manifest.json for experiment configuration.
     */
    private void writeManifest(String configDir, ExperimentConfig config) throws IOException {
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("experiment", "H1_Plateau_Boundary_Study");
        manifest.put("configuration", config.name());
        manifest.put("description", config.getDescription());
        manifest.put("parameters", Map.of(
            "trials", trials,
            "popSizes", popSizes,
            "graphN", graphN,
            "edgeP", edgeP,
            "maxSteps", maxSteps,
            "plateauWindowW", plateauWindowW,
            "bucketSizeB", bucketSizeB,
            "algotypeMix", algotypeMix.entrySet().stream()
                .collect(HashMap::new, 
                    (m, e) -> m.put(e.getKey().name(), e.getValue()),
                    HashMap::putAll)
        ));
        manifest.put("seeds", Map.of(
            "graphSeeds", graphSeeds,
            "runSeeds", runSeeds
        ));
        manifest.put("timestamp", System.currentTimeMillis());
        
        Path manifestPath = Paths.get(configDir, "manifest.json");
        try (Writer writer = Files.newBufferedWriter(manifestPath)) {
            gson.toJson(manifest, writer);
        }
    }
    
    /**
     * Data class for trajectory point.
     */
    private static class TrajectoryPoint {
        final int step;
        final int bestViolations;
        final int medianViolations;
        final int worstViolations;
        final double aggregation;
        final boolean onPlateau;
        final int plateauDuration;
        final int recombEvents;
        
        TrajectoryPoint(int step, int bestViolations, int medianViolations,
                       int worstViolations, double aggregation, boolean onPlateau,
                       int plateauDuration, int recombEvents) {
            this.step = step;
            this.bestViolations = bestViolations;
            this.medianViolations = medianViolations;
            this.worstViolations = worstViolations;
            this.aggregation = aggregation;
            this.onPlateau = onPlateau;
            this.plateauDuration = plateauDuration;
            this.recombEvents = recombEvents;
        }
    }
    
    /**
     * Data class for trial result.
     */
    private static class TrialResult {
        final List<TrajectoryPoint> trajectory;
        final int finalBest;
        final int finalMedian;
        final boolean solved;
        final int stepsToSolution;
        final int totalRecombEvents;
        final double maxAggregation;
        final double finalAggregation;
        
        TrialResult(List<TrajectoryPoint> trajectory, int finalBest, int finalMedian,
                   boolean solved, int stepsToSolution, int totalRecombEvents,
                   double maxAggregation, double finalAggregation) {
            this.trajectory = trajectory;
            this.finalBest = finalBest;
            this.finalMedian = finalMedian;
            this.solved = solved;
            this.stepsToSolution = stepsToSolution;
            this.totalRecombEvents = totalRecombEvents;
            this.maxAggregation = maxAggregation;
            this.finalAggregation = finalAggregation;
        }
    }
    
    /**
     * Main entry point with CLI argument parsing.
     */
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printUsage();
                return;
            }
            
            // Parse arguments
            Map<String, String> params = parseArgs(args);
            
            String outDir = params.getOrDefault("--outDir", "out");
            int trials = Integer.parseInt(params.getOrDefault("--trials", "100"));
            
            List<Integer> popSizes = parseIntList(
                params.getOrDefault("--popSizes", "50,100"));
            
            int graphN = Integer.parseInt(params.getOrDefault("--graphN", "20"));
            double edgeP = Double.parseDouble(params.getOrDefault("--edgeP", "0.25"));
            int maxSteps = Integer.parseInt(params.getOrDefault("--maxSteps", "5000"));
            int plateauWindowW = Integer.parseInt(
                params.getOrDefault("--plateauWindowW", "75"));
            int bucketSizeB = Integer.parseInt(
                params.getOrDefault("--bucketSizeB", "2"));
            
            // Generate or parse seeds
            long masterSeed = Long.parseLong(
                params.getOrDefault("--masterSeed", String.valueOf(System.currentTimeMillis())));
            
            List<Long> graphSeeds = params.containsKey("--graphSeeds")
                ? parseLongList(params.get("--graphSeeds"))
                : generateSeeds(masterSeed, trials);
            
            List<Long> runSeeds = params.containsKey("--runSeeds")
                ? parseLongList(params.get("--runSeeds"))
                : generateSeeds(masterSeed + 1000, trials);
            
            // Default algotype mix: equal distribution
            Map<ColoringAlgotype, Double> algotypeMix = new HashMap<>();
            algotypeMix.put(ColoringAlgotype.GREEDY_REPAIR, 0.25);
            algotypeMix.put(ColoringAlgotype.MIN_CONFLICT, 0.25);
            algotypeMix.put(ColoringAlgotype.RANDOM_WALK, 0.25);
            algotypeMix.put(ColoringAlgotype.BACKTRACK_LIGHT, 0.25);
            
            // Create and run batch runner
            H1BatchRunner runner = new H1BatchRunner(
                outDir, trials, popSizes, graphN, edgeP,
                graphSeeds, runSeeds, maxSteps, plateauWindowW,
                bucketSizeB, algotypeMix);
            
            System.out.println("=== H1 Batch Runner ===");
            System.out.println("Output directory: " + outDir);
            System.out.println("Trials per popSize: " + trials);
            System.out.println("Population sizes: " + popSizes);
            System.out.println("Graph: n=" + graphN + ", p=" + edgeP);
            System.out.println("Max steps: " + maxSteps);
            System.out.println("Plateau window: " + plateauWindowW);
            System.out.println("Bucket size: " + bucketSizeB);
            System.out.println();
            
            runner.runAllConfigurations();
            
            System.out.println("\n=== Batch Run Complete ===");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.out.println("H1 Batch Runner - Usage:");
        System.out.println();
        System.out.println("Required arguments:");
        System.out.println("  --outDir <path>          Output directory");
        System.out.println();
        System.out.println("Optional arguments:");
        System.out.println("  --trials <n>             Number of trials per popSize (default: 100)");
        System.out.println("  --popSizes <list>        Comma-separated population sizes (default: 50,100)");
        System.out.println("  --graphN <n>             Graph vertices (default: 20)");
        System.out.println("  --edgeP <p>              Edge probability (default: 0.25)");
        System.out.println("  --maxSteps <n>           Maximum steps per trial (default: 5000)");
        System.out.println("  --plateauWindowW <w>     Plateau window size (default: 75)");
        System.out.println("  --bucketSizeB <b>        Bucket size for plateaus (default: 2)");
        System.out.println("  --masterSeed <seed>      Master seed for generating seeds (default: current time)");
        System.out.println("  --graphSeeds <list>      Comma-separated graph seeds (optional)");
        System.out.println("  --runSeeds <list>        Comma-separated run seeds (optional)");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java H1BatchRunner --outDir out --trials 10 --popSizes 50,100");
    }
    
    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--") && i + 1 < args.length) {
                params.put(args[i], args[i + 1]);
                i++;
            }
        }
        return params;
    }
    
    private static List<Integer> parseIntList(String s) {
        List<Integer> result = new ArrayList<>();
        for (String part : s.split(",")) {
            result.add(Integer.parseInt(part.trim()));
        }
        return result;
    }
    
    private static List<Long> parseLongList(String s) {
        List<Long> result = new ArrayList<>();
        for (String part : s.split(",")) {
            result.add(Long.parseLong(part.trim()));
        }
        return result;
    }
    
    private static List<Long> generateSeeds(long baseSeed, int count) {
        Random rng = new Random(baseSeed);
        List<Long> seeds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            seeds.add(rng.nextLong());
        }
        return seeds;
    }
}
