package com.emergent.doom.datagen;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.chimeric.ChimericPopulation;
import com.emergent.doom.chimeric.GenericCellFactory;
import com.emergent.doom.chimeric.PercentageAlgotypeProvider;
import com.emergent.doom.experiment.ChimericExperimentConfig;
import com.emergent.doom.experiment.ExperimentResults;
import com.emergent.doom.experiment.ExperimentRunner;
import com.emergent.doom.experiment.TrialResult;
import com.emergent.doom.metrics.AlgotypeAggregationIndex;
import com.emergent.doom.metrics.Monotonicity;
import com.emergent.doom.metrics.SortednessValue;
import com.emergent.doom.probe.StepSnapshot;
import com.emergent.doom.topology.ChimericTopology;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data generation test for Chimeric Population Aggregation Experiment.
 *
 * <p>Produces CSV datasets showing emergent clustering in mixed-algotype populations:
 * - Aggregation percentage (% cells with same-algotype neighbors) over time
 * - Sortedness convergence in chimeric vs homogeneous populations
 * - Impact of frozen cells on clustering dynamics
 * - Comparative performance across different algotype mixes</p>
 *
 * <p>Output CSVs: chimeric_aggregation_timeseries.csv
 * Metadata JSON: chimeric_aggregation_metadata.json</p>
 *
 * <p>This experiment addresses the research question:
 * "How do mixed-algotype populations spontaneously self-organize,
 * and does this clustering impact sorting convergence?"</p>
 */
@Tag("datagen")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ChimericAggregationDataGenTest {

    private static final Path OUTPUT_DIR = 
        Paths.get("docs/findings/chimeric-aggregation-exp-001");
    
    private static final String CSV_FILENAME = "chimeric_aggregation_timeseries.csv";
    private static final String METADATA_FILENAME = "chimeric_aggregation_metadata.json";
    
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
     * PURPOSE: Generate comprehensive time-series dataset for chimeric aggregation analysis.
     *
     * INPUTS: Parameter sweep across array sizes, algotype mixes, frozen cells, and seeds
     * EXPECTED OUTPUT: CSV with ~250k+ rows of step-level metrics
     * TEST DATA: 81 configurations (3 sizes × 3 mixes × 3 frozen × 3 seeds)
     * REPRODUCTION: Run this test class with maven
     */
    @Test
    @Order(1)
    @DisplayName("Generates chimeric aggregation time-series dataset")
    void generatesChimericAggregationDataset() throws IOException {
        System.out.println("\n=== Chimeric Aggregation Experiment 001 ===");
        System.out.println("Generating time-series data for emergent clustering...\n");

        // Step 1: Define parameter sweep
        List<ExperimentConfig> sweepConfigs = buildParameterSweep();
        System.out.printf("Total configurations: %d%n", sweepConfigs.size());
        System.out.printf("Estimated execution time: %.1f minutes%n%n", 
            sweepConfigs.size() * 0.05); // ~3 seconds per config

        // Step 2: Run experiments and collect time-series data
        List<TimeSeriesRecord> allRecords = new ArrayList<>();
        int configNum = 0;
        long startTime = System.currentTimeMillis();
        
        for (ExperimentConfig config : sweepConfigs) {
            configNum++;
            System.out.printf("[%d/%d] Running: size=%d, mix=%s, frozen=%d, seed=%d%n",
                configNum, sweepConfigs.size(),
                config.arraySize, config.algotypeMixLabel,
                config.frozenCount, config.seed);
            
            List<TimeSeriesRecord> configRecords = runExperimentAndCollectMetrics(config);
            allRecords.addAll(configRecords);
            
            System.out.printf("  → Captured %d time-series points%n", configRecords.size());
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("%nExperiment complete! Total time: %.2f seconds%n", elapsed / 1000.0);
        System.out.printf("Total data points collected: %,d%n%n", allRecords.size());

        // Step 3: Write CSV
        Path csvFile = OUTPUT_DIR.resolve(CSV_FILENAME);
        writeToCsv(allRecords, csvFile);
        System.out.printf("✓ CSV exported to: %s%n", csvFile);

        // Step 4: Write metadata JSON
        Path metadataFile = OUTPUT_DIR.resolve(METADATA_FILENAME);
        writeMetadata(metadataFile, sweepConfigs, allRecords.size());
        System.out.printf("✓ Metadata exported to: %s%n%n", metadataFile);

        // Step 5: Structural assertions
        assertTrue(Files.exists(csvFile), "CSV file should exist");
        assertTrue(Files.size(csvFile) > 1000, "CSV should contain substantial data");
        assertTrue(Files.exists(metadataFile), "Metadata JSON should exist");
        assertTrue(allRecords.size() > 1000, 
            "Should have captured thousands of time-series points");
        
        System.out.println("=== Experiment Complete ===\n");
    }

    /**
     * Builds the complete parameter space for the experiment.
     *
     * @return list of all experimental configurations
     */
    private List<ExperimentConfig> buildParameterSweep() {
        List<ExperimentConfig> configs = new ArrayList<>();
        
        // Parameter dimensions
        int[] arraySizes = {30, 50, 100};
        int[] frozenCounts = {0, 1, 3};
        long[] seeds = {42L, 123L, 789L};
        
        // Algotype mixes
        Map<String, Map<Algotype, Double>> algotypeMixes = new LinkedHashMap<>();
        algotypeMixes.put("100%_Bubble", 
            Map.of(Algotype.BUBBLE, 1.0));
        algotypeMixes.put("50/50_Bubble/Selection", 
            Map.of(Algotype.BUBBLE, 0.5, Algotype.SELECTION, 0.5));
        algotypeMixes.put("33/33/33_Bubble/Selection/Insertion",
            Map.of(Algotype.BUBBLE, 0.33, Algotype.SELECTION, 0.33, Algotype.INSERTION, 0.34));
        
        // Generate all combinations
        for (int size : arraySizes) {
            for (Map.Entry<String, Map<Algotype, Double>> mixEntry : algotypeMixes.entrySet()) {
                for (int frozen : frozenCounts) {
                    for (long seed : seeds) {
                        configs.add(new ExperimentConfig(
                            size, mixEntry.getValue(), mixEntry.getKey(), frozen, seed
                        ));
                    }
                }
            }
        }
        
        return configs;
    }

    /**
     * Runs a single experiment configuration and captures per-step metrics.
     *
     * @param config the experiment configuration
     * @return list of time-series records (one per step)
     */
    private List<TimeSeriesRecord> runExperimentAndCollectMetrics(ExperimentConfig config) {
        List<TimeSeriesRecord> records = new ArrayList<>();
        
        // Build ChimericExperimentConfig
        ChimericExperimentConfig chimericConfig = ChimericExperimentConfig.builder()
            .arraySize(config.arraySize)
            .maxSteps(5000)  // Allow sufficient time for clustering
            .requiredStableSteps(3)
            .recordTrajectory(true)
            .algotypeMix(config.algotypeMix)
            .frozenCellCount(config.frozenCount)
            .seed(config.seed)
            .build();
        
        // Create experiment runner
        ExperimentRunner<GenericCell> runner = new ExperimentRunner<>(
            () -> createChimericArray(chimericConfig),
            ChimericTopology::new
        );
        
        // Run single trial (reproducible with seed)
        ExperimentResults<GenericCell> results = runner.runExperiment(chimericConfig, 1);
        
        // Extract trajectory from single trial
        TrialResult<GenericCell> trial = results.getTrials().get(0);
        List<StepSnapshot<GenericCell>> trajectory = trial.getTrajectory();
        
        if (trajectory != null && !trajectory.isEmpty()) {
            // Sample every N steps to keep data manageable
            int sampleInterval = Math.max(1, trajectory.size() / 500); // ~500 samples per config
            
            for (int i = 0; i < trajectory.size(); i += sampleInterval) {
                StepSnapshot<GenericCell> snapshot = trajectory.get(i);
                
                // Compute metrics for this step
                double aggregation = aggregationMetric.compute(snapshot);
                double sortedness = sortednessMetric.compute(snapshot);
                double monotonicity = monotonicityMetric.compute(snapshot);
                
                records.add(new TimeSeriesRecord(
                    snapshot.getStepNumber(),
                    aggregation,
                    sortedness,
                    monotonicity,
                    snapshot.getSwapCount(),
                    config.arraySize,
                    config.algotypeMixLabel,
                    config.frozenCount,
                    config.seed
                ));
            }
            
            // Always include final step
            StepSnapshot<GenericCell> finalSnapshot = trajectory.get(trajectory.size() - 1);
            if ((trajectory.size() - 1) % sampleInterval != 0) {
                double aggregation = aggregationMetric.compute(finalSnapshot);
                double sortedness = sortednessMetric.compute(finalSnapshot);
                double monotonicity = monotonicityMetric.compute(finalSnapshot);
                
                records.add(new TimeSeriesRecord(
                    finalSnapshot.getStepNumber(),
                    aggregation,
                    sortedness,
                    monotonicity,
                    finalSnapshot.getSwapCount(),
                    config.arraySize,
                    config.algotypeMixLabel,
                    config.frozenCount,
                    config.seed
                ));
            }
        }
        
        return records;
    }

    /**
     * Creates a chimeric cell array using the given configuration.
     */
    private GenericCell[] createChimericArray(ChimericExperimentConfig config) {
        Map<Algotype, Double> mix = config.getChimericMix();
        long seed = config.getSeed();
        int size = config.getArraySize();
        
        PercentageAlgotypeProvider provider = new PercentageAlgotypeProvider(mix, size, seed);
        GenericCellFactory factory = GenericCellFactory.shuffled(size, seed + 1);
        ChimericPopulation<GenericCell> population = new ChimericPopulation<>(factory, provider);
        
        return population.createPopulation(size, GenericCell.class);
    }

    /**
     * Writes time-series records to CSV file.
     */
    private void writeToCsv(List<TimeSeriesRecord> records, Path csvFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile)) {
            // CSV header
            writer.write("step_number,aggregation_pct,sortedness_pct,monotonicity_pct," +
                "swap_count,array_size,algotype_mix,frozen_count,seed\n");
            
            // Data rows
            for (TimeSeriesRecord record : records) {
                writer.write(String.format("%d,%.4f,%.4f,%.4f,%d,%d,%s,%d,%d%n",
                    record.stepNumber,
                    record.aggregationPct,
                    record.sortednessPct,
                    record.monotonicityPct,
                    record.swapCount,
                    record.arraySize,
                    record.algotypeMix,
                    record.frozenCount,
                    record.seed
                ));
            }
        }
    }

    /**
     * Writes experiment metadata to JSON file.
     */
    private void writeMetadata(Path metadataFile, List<ExperimentConfig> configs, int totalRecords) 
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(metadataFile)) {
            writer.write("{\n");
            writer.write("  \"experiment_name\": \"Chimeric Aggregation Experiment 001\",\n");
            writer.write("  \"description\": \"Time-series analysis of emergent clustering in mixed-algotype populations\",\n");
            writer.write("  \"generated_at\": \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(new Date()) + "\",\n");
            writer.write("  \"total_configurations\": " + configs.size() + ",\n");
            writer.write("  \"total_data_points\": " + totalRecords + ",\n");
            writer.write("  \"parameters\": {\n");
            writer.write("    \"array_sizes\": [30, 50, 100],\n");
            writer.write("    \"algotype_mixes\": [\n");
            writer.write("      \"100%_Bubble\",\n");
            writer.write("      \"50/50_Bubble/Selection\",\n");
            writer.write("      \"33/33/33_Bubble/Selection/Insertion\"\n");
            writer.write("    ],\n");
            writer.write("    \"frozen_cells\": [0, 1, 3],\n");
            writer.write("    \"seeds\": [42, 123, 789],\n");
            writer.write("    \"max_steps\": 5000\n");
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
            writer.write("    },\n");
            writer.write("    \"swap_count\": {\n");
            writer.write("      \"definition\": \"Cumulative swaps executed\",\n");
            writer.write("      \"unit\": \"count\",\n");
            writer.write("      \"range\": [0, \"infinity\"]\n");
            writer.write("    }\n");
            writer.write("  },\n");
            writer.write("  \"csv_file\": \"" + CSV_FILENAME + "\",\n");
            writer.write("  \"repository\": \"https://github.com/zfifteen/emergent-doom-engine\",\n");
            writer.write("  \"framework_version\": \"0.3.0-alpha\"\n");
            writer.write("}\n");
        }
    }

    /**
     * Immutable record for a single time-series data point.
     */
    private static class TimeSeriesRecord {
        final int stepNumber;
        final double aggregationPct;
        final double sortednessPct;
        final double monotonicityPct;
        final int swapCount;
        final int arraySize;
        final String algotypeMix;
        final int frozenCount;
        final long seed;

        TimeSeriesRecord(int stepNumber, double aggregationPct, double sortednessPct,
                        double monotonicityPct, int swapCount, int arraySize,
                        String algotypeMix, int frozenCount, long seed) {
            this.stepNumber = stepNumber;
            this.aggregationPct = aggregationPct;
            this.sortednessPct = sortednessPct;
            this.monotonicityPct = monotonicityPct;
            this.swapCount = swapCount;
            this.arraySize = arraySize;
            this.algotypeMix = algotypeMix;
            this.frozenCount = frozenCount;
            this.seed = seed;
        }
    }

    /**
     * Experiment configuration holder.
     */
    private static class ExperimentConfig {
        final int arraySize;
        final Map<Algotype, Double> algotypeMix;
        final String algotypeMixLabel;
        final int frozenCount;
        final long seed;

        ExperimentConfig(int arraySize, Map<Algotype, Double> algotypeMix,
                        String algotypeMixLabel, int frozenCount, long seed) {
            this.arraySize = arraySize;
            this.algotypeMix = algotypeMix;
            this.algotypeMixLabel = algotypeMixLabel;
            this.frozenCount = frozenCount;
            this.seed = seed;
        }
    }
}
