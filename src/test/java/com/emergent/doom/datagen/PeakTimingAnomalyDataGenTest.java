package com.emergent.doom.datagen;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.chimeric.ChimericPopulation;
import com.emergent.doom.chimeric.GenericCellFactory;
import com.emergent.doom.chimeric.PercentageAlgotypeProvider;
import com.emergent.doom.execution.NoSwapConvergence;
import com.emergent.doom.execution.SynchronousExecutionEngine;
import com.emergent.doom.experiments.clustering.ChimericProbe;
import com.emergent.doom.metrics.AlgotypeAggregationIndex;
import com.emergent.doom.metrics.Monotonicity;
import com.emergent.doom.metrics.SortednessValue;
import com.emergent.doom.probe.StepSnapshot;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.SwapEngine;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data generation test to investigate the Peak Timing Anomaly.
 *
 * <p><strong>PURPOSE:</strong> Generate complete step-by-step trajectories to determine
 * why clustering peaks occur at step 0 instead of mid-sorting as expected from the
 * Levin et al. (2024) paper.</p>
 *
 * <p><strong>ANOMALY DESCRIPTION:</strong>
 * <ul>
 *   <li>Observed: All chimeric pairs show peak aggregation (~74%) at step 0</li>
 *   <li>Expected: Peaks should occur mid-sorting (42%, 21%, 19% of progress)</li>
 *   <li>Expected Values: Bubble-Selection 72%, Bubble-Insertion 65%, Selection-Insertion 69%</li>
 * </ul></p>
 *
 * <p><strong>INVESTIGATION STRATEGY:</strong>
 * <ol>
 *   <li>Use ChimericProbe for proper algotype tracking (like ClusteringValidationExperiment)</li>
 *   <li>Record EVERY step (no sampling) to capture precise peak timing</li>
 *   <li>Export complete trajectories showing aggregation, sortedness, monotonicity over time</li>
 *   <li>Compare initial random state vs. mid-sorting dynamics</li>
 *   <li>Calculate theoretical baseline for 50/50 random mix</li>
 * </ol></p>
 *
 * <p>Output CSVs: peak_timing_trajectories.csv, peak_timing_summary.csv
 * Metadata JSON: peak_timing_metadata.json
 * Documentation: docs/findings/peak-timing-investigation/ANALYSIS.md</p>
 */
@Tag("datagen")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PeakTimingAnomalyDataGenTest {

    private static final Path OUTPUT_DIR = 
        Paths.get("docs/findings/peak-timing-investigation");
    
    private static final String TRAJECTORIES_CSV = "peak_timing_trajectories.csv";
    private static final String SUMMARY_CSV = "peak_timing_summary.csv";
    private static final String METADATA_JSON = "peak_timing_metadata.json";
    
    // Experimental parameters (matching ClusteringValidationExperiment)
    private static final int ARRAY_SIZE = 100;
    private static final int MAX_STEPS = 10000;
    private static final long BASE_SEED = 42L;
    private static final int TRIALS_PER_PAIR = 10;  // Multiple trials for statistical confidence
    
    private AlgotypeAggregationIndex<GenericCell> aggregationMetric;
    private SortednessValue<GenericCell> sortednessMetric;
    private Monotonicity<GenericCell> monotonicityMetric;

    @BeforeAll
    static void ensureOutputDirectoryExists() throws IOException {
        Files.createDirectories(OUTPUT_DIR);
    }
    
    @BeforeEach
    void setUp() {
        aggregationMetric = new AlgotypeAggregationIndex<>();
        sortednessMetric = new SortednessValue<>();
        monotonicityMetric = new Monotonicity<>();
    }

    /**
     * PURPOSE: Generate complete trajectory dataset for peak timing analysis.
     *
     * <p>This test investigates the anomaly where clustering peaks appear at step 0
     * instead of mid-sorting. It generates full step-by-step trajectories showing
     * how aggregation, sortedness, and monotonicity evolve over time.</p>
     *
     * INPUTS: Bubble-Selection, Bubble-Insertion, Selection-Insertion pairs + homogeneous control
     * EXPECTED OUTPUT: CSV with complete trajectories (every step recorded)
     * TEST DATA: 10 trials per algotype pair with fixed seeds
     * REPRODUCTION: Run with mvn test -Dgroups=datagen
     */
    @Test
    @Order(1)
    @DisplayName("Generates complete trajectories for peak timing investigation")
    void generatesCompletePeakTimingTrajectories() throws IOException {
        System.out.println("\n=== Peak Timing Anomaly Investigation ===");
        System.out.println("Generating complete step-by-step trajectories...\n");

        // Step 1: Define algotype pairs to test
        List<AlgotypePairConfig> pairConfigs = buildAlgotypePairConfigs();
        System.out.printf("Testing %d algotype pairs with %d trials each%n", 
            pairConfigs.size(), TRIALS_PER_PAIR);
        System.out.printf("Total experiments: %d%n%n", pairConfigs.size() * TRIALS_PER_PAIR);

        // Step 2: Run experiments and collect complete trajectories
        List<TrajectoryRecord> allTrajectories = new ArrayList<>();
        List<SummaryRecord> summaryRecords = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        
        for (AlgotypePairConfig pairConfig : pairConfigs) {
            System.out.printf("Testing pair: %s%n", pairConfig.pairLabel);
            
            for (int trial = 0; trial < TRIALS_PER_PAIR; trial++) {
                long seed = BASE_SEED + trial;
                System.out.printf("  Trial %d/%d (seed=%d)... ", trial + 1, TRIALS_PER_PAIR, seed);
                
                // Run experiment with ChimericProbe
                ExperimentResult result = runSingleTrial(
                    pairConfig.algotypeA, 
                    pairConfig.algotypeB, 
                    seed
                );
                
                // Extract trajectory records
                List<TrajectoryRecord> trialTrajectories = extractTrajectories(
                    result, pairConfig.pairLabel, trial, seed
                );
                allTrajectories.addAll(trialTrajectories);
                
                // Create summary record
                SummaryRecord summary = createSummaryRecord(
                    result, pairConfig, trial, seed
                );
                summaryRecords.add(summary);
                
                System.out.printf("%d steps, peak=%.2f%% at step %d (%.1f%% progress)%n",
                    result.finalStep,
                    summary.peakAggregationPct,
                    summary.peakStepNumber,
                    summary.peakTimingPct
                );
            }
            System.out.println();
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("Experiment complete! Total time: %.2f seconds%n", elapsed / 1000.0);
        System.out.printf("Total trajectory points: %,d%n", allTrajectories.size());
        System.out.printf("Total trials: %d%n%n", summaryRecords.size());

        // Step 3: Write trajectories CSV (every step from every trial)
        Path trajectoriesFile = OUTPUT_DIR.resolve(TRAJECTORIES_CSV);
        writeTrajectoriesCsv(allTrajectories, trajectoriesFile);
        System.out.printf("✓ Trajectories CSV: %s (%,d rows)%n", 
            trajectoriesFile, allTrajectories.size());

        // Step 4: Write summary CSV (one row per trial with peak info)
        Path summaryFile = OUTPUT_DIR.resolve(SUMMARY_CSV);
        writeSummaryCsv(summaryRecords, summaryFile);
        System.out.printf("✓ Summary CSV: %s (%d trials)%n", 
            summaryFile, summaryRecords.size());

        // Step 5: Write metadata JSON
        Path metadataFile = OUTPUT_DIR.resolve(METADATA_JSON);
        writeMetadata(metadataFile, pairConfigs, allTrajectories.size(), summaryRecords.size());
        System.out.printf("✓ Metadata JSON: %s%n%n", metadataFile);

        // Step 6: Perform analysis and report findings
        analyzeAndReportFindings(summaryRecords);

        // Step 7: Structural assertions
        assertTrue(Files.exists(trajectoriesFile), "Trajectories CSV should exist");
        assertTrue(Files.exists(summaryFile), "Summary CSV should exist");
        assertTrue(Files.exists(metadataFile), "Metadata JSON should exist");
        assertTrue(allTrajectories.size() > 1000, 
            "Should have thousands of trajectory points");
        assertTrue(summaryRecords.size() == pairConfigs.size() * TRIALS_PER_PAIR,
            "Should have one summary per trial");
        
        System.out.println("=== Investigation Complete ===\n");
    }

    /**
     * Builds the list of algotype pair configurations to test.
     *
     * <p>Tests the three pairs from Levin et al. paper plus homogeneous control:</p>
     * <ul>
     *   <li>Bubble-Selection: Expected peak 72% at 42% progress</li>
     *   <li>Bubble-Insertion: Expected peak 65% at 21% progress</li>
     *   <li>Selection-Insertion: Expected peak 69% at 19% progress</li>
     *   <li>Bubble-Bubble (control): Expected < 60% throughout</li>
     * </ul>
     *
     * @return list of algotype pair configurations
     */
    private List<AlgotypePairConfig> buildAlgotypePairConfigs() {
        List<AlgotypePairConfig> configs = new ArrayList<>();
        
        // Chimeric pairs from paper
        configs.add(new AlgotypePairConfig(
            Algotype.BUBBLE, Algotype.SELECTION, "Bubble-Selection",
            72.0, 42.0
        ));
        configs.add(new AlgotypePairConfig(
            Algotype.BUBBLE, Algotype.INSERTION, "Bubble-Insertion",
            65.0, 21.0
        ));
        configs.add(new AlgotypePairConfig(
            Algotype.SELECTION, Algotype.INSERTION, "Selection-Insertion",
            69.0, 19.0
        ));
        
        // Homogeneous control
        configs.add(new AlgotypePairConfig(
            Algotype.BUBBLE, Algotype.BUBBLE, "Bubble-Bubble (control)",
            100.0, 0.0  // Always 100% aggregation (all same type)
        ));
        
        return configs;
    }

    /**
     * Runs a single trial experiment with proper ChimericProbe for algotype tracking.
     *
     * <p><strong>CRITICAL:</strong> Uses ChimericProbe (not standard Probe) to ensure
     * algotype information is tracked in snapshots, enabling AlgotypeAggregationIndex
     * to compute clustering metrics correctly.</p>
     *
     * @param algotypeA first algotype in the mix
     * @param algotypeB second algotype in the mix
     * @param seed random seed for reproducibility
     * @return experiment result with complete trajectory
     */
    private ExperimentResult runSingleTrial(Algotype algotypeA, Algotype algotypeB, long seed) {
        // Create algotype mix
        Map<Algotype, Double> algotypeMix;
        if (algotypeA == algotypeB) {
            // Homogeneous control
            algotypeMix = Map.of(algotypeA, 1.0);
        } else {
            // 50/50 chimeric mix
            algotypeMix = Map.of(algotypeA, 0.5, algotypeB, 0.5);
        }
        
        // Create algotype provider
        PercentageAlgotypeProvider provider = 
            new PercentageAlgotypeProvider(algotypeMix, ARRAY_SIZE, seed);
        
        // Create chimeric array
        GenericCellFactory factory = GenericCellFactory.shuffled(ARRAY_SIZE, seed + 1);
        ChimericPopulation<GenericCell> population = 
            new ChimericPopulation<>(factory, provider);
        GenericCell[] cells = population.createPopulation(ARRAY_SIZE, GenericCell.class);
        
        // Create ChimericProbe for algotype tracking
        ChimericProbe<GenericCell> probe = new ChimericProbe<>(provider, ARRAY_SIZE);
        probe.setRecordingEnabled(true);
        
        // Set up execution components
        FrozenCellStatus frozenStatus = new FrozenCellStatus();
        SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
        NoSwapConvergence<GenericCell> convergenceDetector = new NoSwapConvergence<>(3);
        
        // Create metadata provider
        java.util.function.IntFunction<com.emergent.doom.execution.CellMetadata> metadataProvider = i -> {
            String algotypeName = provider.getAlgotype(i, ARRAY_SIZE);
            Algotype cellAlgotype = Algotype.valueOf(algotypeName.toUpperCase());
            return new com.emergent.doom.execution.CellMetadata(
                cellAlgotype, 
                com.emergent.doom.cell.SortDirection.ASCENDING
            );
        };
        
        // Create and run execution engine
        SynchronousExecutionEngine<GenericCell> engine = new SynchronousExecutionEngine<>(
            cells, swapEngine, probe, convergenceDetector, metadataProvider
        );
        
        int finalStep = engine.runUntilConvergence(MAX_STEPS);
        boolean converged = engine.hasConverged();
        
        // Get trajectory snapshots
        List<StepSnapshot<GenericCell>> trajectory = probe.getSnapshots();
        
        return new ExperimentResult(finalStep, converged, trajectory);
    }

    /**
     * Extracts trajectory records from an experiment result.
     *
     * <p>Converts each snapshot into a TrajectoryRecord with step number, metrics,
     * and experimental parameters for CSV export.</p>
     *
     * @param result experiment result with trajectory
     * @param pairLabel algotype pair label
     * @param trialNumber trial number within this pair
     * @param seed random seed used
     * @return list of trajectory records (one per step)
     */
    private List<TrajectoryRecord> extractTrajectories(
            ExperimentResult result, String pairLabel, int trialNumber, long seed) {
        List<TrajectoryRecord> records = new ArrayList<>();
        
        if (result.trajectory == null || result.trajectory.isEmpty()) {
            return records;
        }
        
        for (StepSnapshot<GenericCell> snapshot : result.trajectory) {
            double aggregation = aggregationMetric.compute(snapshot);
            double sortedness = sortednessMetric.compute(snapshot);
            double monotonicity = monotonicityMetric.compute(snapshot);
            
            records.add(new TrajectoryRecord(
                pairLabel,
                trialNumber,
                seed,
                snapshot.getStepNumber(),
                aggregation,
                sortedness,
                monotonicity,
                snapshot.getSwapCount(),
                result.finalStep
            ));
        }
        
        return records;
    }

    /**
     * Creates a summary record with peak information for one trial.
     *
     * <p>Finds the maximum aggregation value in the trajectory and records
     * when it occurred (both absolute step and percentage of progress).</p>
     *
     * @param result experiment result
     * @param pairConfig algotype pair configuration
     * @param trialNumber trial number
     * @param seed random seed
     * @return summary record with peak info
     */
    private SummaryRecord createSummaryRecord(
            ExperimentResult result, AlgotypePairConfig pairConfig, int trialNumber, long seed) {
        
        // Find peak aggregation
        double peakAggregation = 0.0;
        int peakStep = 0;
        double initialAggregation = 0.0;
        double finalAggregation = 0.0;
        
        if (result.trajectory != null && !result.trajectory.isEmpty()) {
            // Initial aggregation (step 0)
            initialAggregation = aggregationMetric.compute(result.trajectory.get(0));
            
            // Find peak
            for (int i = 0; i < result.trajectory.size(); i++) {
                double current = aggregationMetric.compute(result.trajectory.get(i));
                if (current > peakAggregation) {
                    peakAggregation = current;
                    peakStep = result.trajectory.get(i).getStepNumber();
                }
            }
            
            // Final aggregation
            StepSnapshot<GenericCell> finalSnapshot = 
                result.trajectory.get(result.trajectory.size() - 1);
            finalAggregation = aggregationMetric.compute(finalSnapshot);
        }
        
        // Calculate peak timing as percentage of total progress
        double peakTimingPct = result.finalStep > 0 
            ? (peakStep * 100.0 / result.finalStep) 
            : 0.0;
        
        return new SummaryRecord(
            pairConfig.pairLabel,
            trialNumber,
            seed,
            result.finalStep,
            result.converged,
            initialAggregation,
            peakAggregation,
            peakStep,
            peakTimingPct,
            finalAggregation,
            pairConfig.expectedPeakPct,
            pairConfig.expectedTimingPct
        );
    }

    /**
     * Writes complete trajectory data to CSV.
     *
     * <p>Format: One row per step per trial, showing how metrics evolve over time.</p>
     */
    private void writeTrajectoriesCsv(List<TrajectoryRecord> records, Path csvFile) 
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile)) {
            // CSV header
            writer.write("algotype_pair,trial_number,seed,step_number," +
                "aggregation_pct,sortedness_pct,monotonicity_pct,swap_count,final_step\n");
            
            // Data rows
            for (TrajectoryRecord record : records) {
                writer.write(String.format("%s,%d,%d,%d,%.4f,%.4f,%.4f,%d,%d%n",
                    record.algotypePair,
                    record.trialNumber,
                    record.seed,
                    record.stepNumber,
                    record.aggregationPct,
                    record.sortednessPct,
                    record.monotonicityPct,
                    record.swapCount,
                    record.finalStep
                ));
            }
        }
    }

    /**
     * Writes summary data to CSV.
     *
     * <p>Format: One row per trial, showing peak values and timings.</p>
     */
    private void writeSummaryCsv(List<SummaryRecord> records, Path csvFile) 
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile)) {
            // CSV header
            writer.write("algotype_pair,trial_number,seed,final_step,converged," +
                "initial_aggregation_pct,peak_aggregation_pct,peak_step_number," +
                "peak_timing_pct,final_aggregation_pct," +
                "expected_peak_pct,expected_timing_pct\n");
            
            // Data rows
            for (SummaryRecord record : records) {
                writer.write(String.format("%s,%d,%d,%d,%s,%.4f,%.4f,%d,%.4f,%.4f,%.4f,%.4f%n",
                    record.algotypePair,
                    record.trialNumber,
                    record.seed,
                    record.finalStep,
                    record.converged,
                    record.initialAggregationPct,
                    record.peakAggregationPct,
                    record.peakStepNumber,
                    record.peakTimingPct,
                    record.finalAggregationPct,
                    record.expectedPeakPct,
                    record.expectedTimingPct
                ));
            }
        }
    }

    /**
     * Writes experiment metadata to JSON file.
     */
    private void writeMetadata(Path metadataFile, List<AlgotypePairConfig> pairConfigs,
                              int totalTrajectoryPoints, int totalTrials) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(metadataFile)) {
            writer.write("{\n");
            writer.write("  \"experiment_name\": \"Peak Timing Anomaly Investigation\",\n");
            writer.write("  \"description\": \"Complete step-by-step trajectories to investigate why clustering peaks occur at step 0 instead of mid-sorting\",\n");
            writer.write("  \"generated_at\": \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(new Date()) + "\",\n");
            writer.write("  \"anomaly\": {\n");
            writer.write("    \"observed\": \"All chimeric pairs show peak aggregation (~74%) at step 0\",\n");
            writer.write("    \"expected\": \"Peaks should occur mid-sorting (19-42% of progress) with values 65-72%\",\n");
            writer.write("    \"hypothesis\": \"May be measuring initial random distribution rather than emergent clustering\"\n");
            writer.write("  },\n");
            writer.write("  \"parameters\": {\n");
            writer.write("    \"array_size\": " + ARRAY_SIZE + ",\n");
            writer.write("    \"max_steps\": " + MAX_STEPS + ",\n");
            writer.write("    \"trials_per_pair\": " + TRIALS_PER_PAIR + ",\n");
            writer.write("    \"base_seed\": " + BASE_SEED + ",\n");
            writer.write("    \"algotype_pairs\": [\n");
            for (int i = 0; i < pairConfigs.size(); i++) {
                AlgotypePairConfig config = pairConfigs.get(i);
                writer.write(String.format("      {\"pair\": \"%s\", \"expected_peak\": %.1f, \"expected_timing\": %.1f}%s\n",
                    config.pairLabel, config.expectedPeakPct, config.expectedTimingPct,
                    i < pairConfigs.size() - 1 ? "," : ""
                ));
            }
            writer.write("    ]\n");
            writer.write("  },\n");
            writer.write("  \"metrics\": {\n");
            writer.write("    \"aggregation_pct\": {\n");
            writer.write("      \"definition\": \"Percentage of cells with at least one same-algotype neighbor\",\n");
            writer.write("      \"unit\": \"percent\",\n");
            writer.write("      \"range\": [0, 100]\n");
            writer.write("    },\n");
            writer.write("    \"sortedness_pct\": {\n");
            writer.write("      \"definition\": \"Percentage of cells in correct final sorted position\",\n");
            writer.write("      \"unit\": \"percent\",\n");
            writer.write("      \"range\": [0, 100]\n");
            writer.write("    },\n");
            writer.write("    \"monotonicity_pct\": {\n");
            writer.write("      \"definition\": \"Percentage of cells >= their predecessor\",\n");
            writer.write("      \"unit\": \"percent\",\n");
            writer.write("      \"range\": [0, 100]\n");
            writer.write("    }\n");
            writer.write("  },\n");
            writer.write("  \"outputs\": {\n");
            writer.write("    \"trajectories_csv\": \"" + TRAJECTORIES_CSV + "\",\n");
            writer.write("    \"summary_csv\": \"" + SUMMARY_CSV + "\",\n");
            writer.write("    \"total_trajectory_points\": " + totalTrajectoryPoints + ",\n");
            writer.write("    \"total_trials\": " + totalTrials + "\n");
            writer.write("  },\n");
            writer.write("  \"reference\": \"Levin et al. (2024) - Classical Sorting Algorithms as a Model of Morphogenesis, arXiv:2401.05375v1\",\n");
            writer.write("  \"repository\": \"https://github.com/zfifteen/emergent-doom-engine\",\n");
            writer.write("  \"framework_version\": \"0.3.0-alpha\"\n");
            writer.write("}\n");
        }
    }

    /**
     * Analyzes summary records and reports key findings to console.
     *
     * <p>Calculates statistics across trials to identify patterns in peak timing.</p>
     */
    private void analyzeAndReportFindings(List<SummaryRecord> summaries) {
        System.out.println("=== Analysis Results ===\n");
        
        // Group by algotype pair
        Map<String, List<SummaryRecord>> byPair = new LinkedHashMap<>();
        for (SummaryRecord record : summaries) {
            byPair.computeIfAbsent(record.algotypePair, k -> new ArrayList<>()).add(record);
        }
        
        // Calculate statistics for each pair
        for (Map.Entry<String, List<SummaryRecord>> entry : byPair.entrySet()) {
            String pairLabel = entry.getKey();
            List<SummaryRecord> pairRecords = entry.getValue();
            
            // Calculate means
            double avgInitial = pairRecords.stream()
                .mapToDouble(r -> r.initialAggregationPct).average().orElse(0.0);
            double avgPeak = pairRecords.stream()
                .mapToDouble(r -> r.peakAggregationPct).average().orElse(0.0);
            double avgPeakTiming = pairRecords.stream()
                .mapToDouble(r -> r.peakTimingPct).average().orElse(0.0);
            double avgFinal = pairRecords.stream()
                .mapToDouble(r -> r.finalAggregationPct).average().orElse(0.0);
            
            // Count peaks at step 0
            long peaksAtStepZero = pairRecords.stream()
                .filter(r -> r.peakStepNumber == 0).count();
            
            // Get expected values
            double expectedPeak = pairRecords.get(0).expectedPeakPct;
            double expectedTiming = pairRecords.get(0).expectedTimingPct;
            
            System.out.printf("%s:%n", pairLabel);
            System.out.printf("  Initial aggregation: %.2f%%  (step 0 - random shuffle)%n", avgInitial);
            System.out.printf("  Peak aggregation:    %.2f%%  (expected: %.2f%%)%n", avgPeak, expectedPeak);
            System.out.printf("  Peak timing:         %.2f%%  (expected: %.2f%% of progress)%n", 
                avgPeakTiming, expectedTiming);
            System.out.printf("  Final aggregation:   %.2f%%  (sorted state)%n", avgFinal);
            System.out.printf("  Peaks at step 0:     %d/%d trials%n", 
                peaksAtStepZero, pairRecords.size());
            
            // Diagnosis
            if (peaksAtStepZero == pairRecords.size()) {
                System.out.printf("  ⚠️  ANOMALY CONFIRMED: All peaks occur at initial random state%n");
            } else if (peaksAtStepZero > 0) {
                System.out.printf("  ⚠️  PARTIAL ANOMALY: Some peaks occur at initial state%n");
            } else {
                System.out.printf("  ✓  Peaks occur during sorting (not at step 0)%n");
            }
            System.out.println();
        }
        
        // Calculate theoretical random baseline for 50/50 mix
        double theoreticalBaseline = calculateTheoreticalRandomAggregation(0.5, 0.5);
        System.out.printf("Theoretical random baseline (50/50 mix): %.2f%%%n", theoreticalBaseline);
        System.out.println("(This is the expected aggregation for a randomly shuffled 50/50 algotype mix)");
        System.out.println();
    }

    /**
     * Calculates theoretical aggregation for random 50/50 algotype mix.
     *
     * <p>For a randomly shuffled array with fraction p of type A and (1-p) of type B,
     * the expected percentage of cells with at least one same-type neighbor is
     * approximately: 1 - 2*p*(1-p) for interior cells.</p>
     *
     * <p>For p=0.5: 1 - 2*0.5*0.5 = 1 - 0.5 = 0.5 or 50%... but this is simplified.
     * A more accurate calculation considers all positions including boundaries.</p>
     *
     * @param fractionA fraction of type A cells (0.0 to 1.0)
     * @param fractionB fraction of type B cells (0.0 to 1.0)
     * @return expected aggregation percentage for random shuffle
     */
    private double calculateTheoreticalRandomAggregation(double fractionA, double fractionB) {
        // Probability that a neighbor is the same type
        // For type A cell: probability neighbor is also A = fractionA
        // For type B cell: probability neighbor is also B = fractionB
        
        // Expected fraction with at least one same-type neighbor:
        // For interior cells with 2 neighbors:
        // P(at least one same) = 1 - P(both different)
        
        // Weighted average across cell types
        double probSameForA = 1.0 - Math.pow(1.0 - fractionA, 2);  // At least one A neighbor
        double probSameForB = 1.0 - Math.pow(1.0 - fractionB, 2);  // At least one B neighbor
        
        double expectedAggregation = (fractionA * probSameForA + fractionB * probSameForB) * 100.0;
        
        return expectedAggregation;
    }

    /**
     * Configuration for an algotype pair experiment.
     */
    private static class AlgotypePairConfig {
        final Algotype algotypeA;
        final Algotype algotypeB;
        final String pairLabel;
        final double expectedPeakPct;
        final double expectedTimingPct;

        AlgotypePairConfig(Algotype algotypeA, Algotype algotypeB, String pairLabel,
                          double expectedPeakPct, double expectedTimingPct) {
            this.algotypeA = algotypeA;
            this.algotypeB = algotypeB;
            this.pairLabel = pairLabel;
            this.expectedPeakPct = expectedPeakPct;
            this.expectedTimingPct = expectedTimingPct;
        }
    }

    /**
     * Result from running a single trial.
     */
    private static class ExperimentResult {
        final int finalStep;
        final boolean converged;
        final List<StepSnapshot<GenericCell>> trajectory;

        ExperimentResult(int finalStep, boolean converged, 
                        List<StepSnapshot<GenericCell>> trajectory) {
            this.finalStep = finalStep;
            this.converged = converged;
            this.trajectory = trajectory;
        }
    }

    /**
     * Single trajectory data point (one step in one trial).
     */
    private static class TrajectoryRecord {
        final String algotypePair;
        final int trialNumber;
        final long seed;
        final int stepNumber;
        final double aggregationPct;
        final double sortednessPct;
        final double monotonicityPct;
        final int swapCount;
        final int finalStep;

        TrajectoryRecord(String algotypePair, int trialNumber, long seed, int stepNumber,
                        double aggregationPct, double sortednessPct, double monotonicityPct,
                        int swapCount, int finalStep) {
            this.algotypePair = algotypePair;
            this.trialNumber = trialNumber;
            this.seed = seed;
            this.stepNumber = stepNumber;
            this.aggregationPct = aggregationPct;
            this.sortednessPct = sortednessPct;
            this.monotonicityPct = monotonicityPct;
            this.swapCount = swapCount;
            this.finalStep = finalStep;
        }
    }

    /**
     * Summary record for one trial (with peak information).
     */
    private static class SummaryRecord {
        final String algotypePair;
        final int trialNumber;
        final long seed;
        final int finalStep;
        final boolean converged;
        final double initialAggregationPct;
        final double peakAggregationPct;
        final int peakStepNumber;
        final double peakTimingPct;
        final double finalAggregationPct;
        final double expectedPeakPct;
        final double expectedTimingPct;

        SummaryRecord(String algotypePair, int trialNumber, long seed, int finalStep,
                     boolean converged, double initialAggregationPct, double peakAggregationPct,
                     int peakStepNumber, double peakTimingPct, double finalAggregationPct,
                     double expectedPeakPct, double expectedTimingPct) {
            this.algotypePair = algotypePair;
            this.trialNumber = trialNumber;
            this.seed = seed;
            this.finalStep = finalStep;
            this.converged = converged;
            this.initialAggregationPct = initialAggregationPct;
            this.peakAggregationPct = peakAggregationPct;
            this.peakStepNumber = peakStepNumber;
            this.peakTimingPct = peakTimingPct;
            this.finalAggregationPct = finalAggregationPct;
            this.expectedPeakPct = expectedPeakPct;
            this.expectedTimingPct = expectedTimingPct;
        }
    }
}
