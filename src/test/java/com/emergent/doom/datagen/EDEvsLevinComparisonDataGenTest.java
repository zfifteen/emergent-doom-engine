package com.emergent.doom.datagen;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.AlgotypedCell;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.chimeric.AlgotypedCellFactory;
import com.emergent.doom.chimeric.GenericCellFactory;
import com.emergent.doom.chimeric.PercentageAlgotypeProvider;
import com.emergent.doom.execution.NoSwapConvergence;
import com.emergent.doom.execution.SynchronousExecutionEngine;
import com.emergent.doom.experiments.clustering.ChimericProbe;
import com.emergent.doom.metrics.AlgotypeAggregationIndex;
import com.emergent.doom.metrics.SortednessValue;
import com.emergent.doom.probe.AlgotypedProbe;
import com.emergent.doom.probe.StepSnapshot;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.SwapEngine;
import org.junit.jupiter.api.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comparative data generation test: EDE architecture vs. Levin architecture.
 *
 * <p><strong>PURPOSE:</strong> Provide definitive experimental evidence for the architectural
 * difference between EDE and Levin implementations by running BOTH approaches side-by-side
 * and comparing aggregation dynamics.</p>
 *
 * <p><strong>HYPOTHESIS:</strong></p>
 * <ul>
 *   <li><strong>EDE Approach (GenericCell + ChimericProbe):</strong> Algotypes tied to positions.
 *       Swaps move values only. Expected: Constant aggregation (~71% for 50/50 mix).</li>
 *   <li><strong>Levin Approach (AlgotypedCell + AlgotypedProbe):</strong> Algotypes bound to cells.
 *       Swaps move cells (with algotypes). Expected: Dynamic aggregation with potential peaks.</li>
 * </ul>
 *
 * <p><strong>EXPERIMENTAL DESIGN:</strong></p>
 * <ol>
 *   <li>Create identical value sequences for both approaches (same random seed)</li>
 *   <li>Assign algotypes using same distribution (50/50 Bubble/Selection)</li>
 *   <li>Run sorting experiments with identical execution parameters</li>
 *   <li>Record complete trajectories (aggregation + sortedness at each step)</li>
 *   <li>Compare results in side-by-side CSV output</li>
 * </ol>
 *
 * <p><strong>OUTPUT FILES:</strong></p>
 * <ul>
 *   <li>{@code ede_vs_levin_comparison.csv}: Interleaved trajectories from both approaches</li>
 *   <li>{@code ede_vs_levin_summary.csv}: Per-trial statistics and peak information</li>
 *   <li>{@code ede_vs_levin_metadata.json}: Experiment parameters and metric definitions</li>
 * </ul>
 */
@Tag("datagen")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EDEvsLevinComparisonDataGenTest {

    private static final Path OUTPUT_DIR =
        Paths.get("docs/findings/peak-timing-investigation");

    private static final String COMPARISON_CSV = "ede_vs_levin_comparison.csv";
    private static final String SUMMARY_CSV = "ede_vs_levin_summary.csv";
    private static final String METADATA_JSON = "ede_vs_levin_metadata.json";

    // Experimental parameters
    private static final int ARRAY_SIZE = 100;
    private static final int MAX_STEPS = 10000;
    private static final long BASE_SEED = 42L;
    private static final int TRIALS = 10;

    @BeforeAll
    static void ensureOutputDirectoryExists() throws IOException {
        Files.createDirectories(OUTPUT_DIR);
    }

    /**
     * PURPOSE: Generate side-by-side comparison of EDE and Levin architectures.
     *
     * <p>This test runs the SAME sorting problem using both architectural approaches:
     * <ul>
     *   <li>EDE: Position-based algotypes (constant aggregation expected)</li>
     *   <li>Levin: Cell-based algotypes (dynamic aggregation expected)</li>
     * </ul>
     * </p>
     *
     * INPUTS: 50/50 Bubble/Selection mix, array size 100, 10 trials
     * EXPECTED OUTPUT: CSV showing EDE with flat aggregation, Levin with dynamic aggregation
     * TEST DATA: Seeds 42-51 for reproducibility
     * REPRODUCTION: Run with mvn test -Dgroups=datagen
     */
    @Test
    @Order(1)
    @DisplayName("Generates comparative dataset: EDE architecture vs. Levin architecture")
    void generatesEDEvsLevinComparison() throws IOException {
        System.out.println("\n=== EDE vs. Levin Architecture Comparison ===");
        System.out.println("Testing hypothesis: Position-based vs. Cell-based algotypes\n");

        // Algotype distribution: 50/50 Bubble/Selection (matching Levin paper)
        Map<Algotype, Double> distribution = Map.of(
            Algotype.BUBBLE, 0.5,
            Algotype.SELECTION, 0.5
        );

        List<ComparisonTrajectory> allTrajectories = new ArrayList<>();
        List<ComparisonSummary> summaries = new ArrayList<>();

        for (int trial = 0; trial < TRIALS; trial++) {
            long seed = BASE_SEED + trial;
            System.out.printf("Trial %d/%d (seed=%d)...%n", trial + 1, TRIALS, seed);

            // Run EDE approach
            System.out.print("  Running EDE approach (position-based algotypes)... ");
            EDEResult edeResult = runEDEApproach(distribution, seed);
            System.out.printf("completed in %d steps%n", edeResult.finalStep);

            // Run Levin approach
            System.out.print("  Running Levin approach (cell-based algotypes)... ");
            LevinResult levinResult = runLevinApproach(distribution, seed);
            System.out.printf("completed in %d steps%n", levinResult.finalStep);

            // Extract and combine trajectories
            List<ComparisonTrajectory> trialTrajectories =
                combineTrajectories(edeResult, levinResult, trial, seed);
            allTrajectories.addAll(trialTrajectories);

            // Create summary
            ComparisonSummary summary = createSummary(edeResult, levinResult, trial, seed);
            summaries.add(summary);

            // Report findings
            System.out.printf("  EDE: Aggregation %.2f%% (initial) → %.2f%% (peak) → %.2f%% (final)%n",
                summary.edeInitialAgg, summary.edePeakAgg, summary.edeFinalAgg);
            System.out.printf("  Levin: Aggregation %.2f%% (initial) → %.2f%% (peak) → %.2f%% (final)%n",
                summary.levinInitialAgg, summary.levinPeakAgg, summary.levinFinalAgg);
            System.out.printf("  Delta: %.2f%% (initial) → %.2f%% (peak) → %.2f%% (final)%n",
                Math.abs(summary.levinInitialAgg - summary.edeInitialAgg),
                Math.abs(summary.levinPeakAgg - summary.edePeakAgg),
                Math.abs(summary.levinFinalAgg - summary.edeFinalAgg));
            System.out.println();
        }

        // Write outputs
        System.out.println("Writing output files...");
        Path comparisonFile = OUTPUT_DIR.resolve(COMPARISON_CSV);
        writeComparisonCSV(allTrajectories, comparisonFile);
        System.out.printf("✓ Comparison CSV: %s (%,d rows)%n", comparisonFile, allTrajectories.size());

        Path summaryFile = OUTPUT_DIR.resolve(SUMMARY_CSV);
        writeSummaryCSV(summaries, summaryFile);
        System.out.printf("✓ Summary CSV: %s (%d trials)%n", summaryFile, summaries.size());

        Path metadataFile = OUTPUT_DIR.resolve(METADATA_JSON);
        writeMetadata(metadataFile, distribution);
        System.out.printf("✓ Metadata JSON: %s%n", metadataFile);

        // Analyze results
        System.out.println("\n=== Analysis ===");
        analyzeResults(summaries);

        // Assertions
        assertTrue(Files.exists(comparisonFile), "Comparison CSV should exist");
        assertTrue(Files.exists(summaryFile), "Summary CSV should exist");
        assertTrue(Files.exists(metadataFile), "Metadata JSON should exist");
        assertEquals(TRIALS, summaries.size(), "Should have one summary per trial");

        System.out.println("\n=== Comparison Complete ===\n");
    }

    /**
     * Run sorting experiment using EDE architecture (position-based algotypes).
     */
    private EDEResult runEDEApproach(Map<Algotype, Double> distribution, long seed) {
        // Create position-based algotype provider
        PercentageAlgotypeProvider algotypeProvider =
            new PercentageAlgotypeProvider(distribution, ARRAY_SIZE, seed);

        // Create GenericCell array with shuffled values
        GenericCellFactory factory = GenericCellFactory.shuffled(ARRAY_SIZE, seed);
        GenericCell[] cells = new GenericCell[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; i++) {
            cells[i] = factory.createCell(i, algotypeProvider.getAlgotype(i, ARRAY_SIZE));
        }

        // Create ChimericProbe (reads algotypes by position)
        ChimericProbe<GenericCell> probe = new ChimericProbe<>(algotypeProvider, ARRAY_SIZE);
        probe.setRecordingEnabled(true);

        // Set up execution engine
        FrozenCellStatus frozenStatus = new FrozenCellStatus();
        SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
        NoSwapConvergence<GenericCell> convergenceDetector = new NoSwapConvergence<>(3);

        java.util.function.IntFunction<com.emergent.doom.execution.CellMetadata> metadataProvider =
            index -> new com.emergent.doom.execution.CellMetadata(
                Algotype.valueOf(algotypeProvider.getAlgotype(index, ARRAY_SIZE)),
                com.emergent.doom.cell.SortDirection.ASCENDING
            );

        SynchronousExecutionEngine<GenericCell> engine =
            new SynchronousExecutionEngine<>(
                cells, swapEngine, probe, convergenceDetector, metadataProvider
            );

        int finalStep = engine.runUntilConvergence(MAX_STEPS);
        List<StepSnapshot<GenericCell>> trajectory = probe.getSnapshots();

        return new EDEResult(finalStep, trajectory);
    }

    /**
     * Run sorting experiment using Levin architecture (cell-based algotypes).
     */
    private LevinResult runLevinApproach(Map<Algotype, Double> distribution, long seed) {
        // Create AlgotypedCell array (algotypes embedded in cells)
        AlgotypedCellFactory factory = new AlgotypedCellFactory(ARRAY_SIZE, distribution, seed);
        AlgotypedCell[] cells = factory.createShuffledArray();

        // Create AlgotypedProbe (reads algotypes from cells)
        AlgotypedProbe probe = new AlgotypedProbe();
        probe.setRecordingEnabled(true);

        // Set up execution engine
        FrozenCellStatus frozenStatus = new FrozenCellStatus();
        SwapEngine<AlgotypedCell> swapEngine = new SwapEngine<>(frozenStatus);
        NoSwapConvergence<AlgotypedCell> convergenceDetector = new NoSwapConvergence<>(3);

        java.util.function.IntFunction<com.emergent.doom.execution.CellMetadata> metadataProvider =
            index -> new com.emergent.doom.execution.CellMetadata(
                cells[index].getAlgotype(),
                com.emergent.doom.cell.SortDirection.ASCENDING
            );

        SynchronousExecutionEngine<AlgotypedCell> engine =
            new SynchronousExecutionEngine<>(
                cells, swapEngine, probe, convergenceDetector, metadataProvider
            );

        int finalStep = engine.runUntilConvergence(MAX_STEPS);
        List<StepSnapshot<AlgotypedCell>> trajectory = probe.getSnapshots();

        return new LevinResult(finalStep, trajectory);
    }

    /**
     * Combine EDE and Levin trajectories for side-by-side comparison.
     */
    private List<ComparisonTrajectory> combineTrajectories(
            EDEResult ede, LevinResult levin, int trial, long seed) {

        List<ComparisonTrajectory> records = new ArrayList<>();

        AlgotypeAggregationIndex<GenericCell> edeAggMetric = new AlgotypeAggregationIndex<>();
        SortednessValue<GenericCell> edeSortMetric = new SortednessValue<>();

        AlgotypeAggregationIndex<AlgotypedCell> levinAggMetric = new AlgotypeAggregationIndex<>();
        SortednessValue<AlgotypedCell> levinSortMetric = new SortednessValue<>();

        int maxSteps = Math.max(
            ede.trajectory.isEmpty() ? 0 : ede.trajectory.get(ede.trajectory.size() - 1).getStepNumber(),
            levin.trajectory.isEmpty() ? 0 : levin.trajectory.get(levin.trajectory.size() - 1).getStepNumber()
        );

        int edeIndex = 0;
        int levinIndex = 0;

        for (int step = 0; step <= maxSteps; step++) {
            Double edeAgg = null;
            Double edeSort = null;
            if (edeIndex < ede.trajectory.size() &&
                ede.trajectory.get(edeIndex).getStepNumber() == step) {
                edeAgg = edeAggMetric.compute(ede.trajectory.get(edeIndex));
                edeSort = edeSortMetric.compute(ede.trajectory.get(edeIndex));
                edeIndex++;
            }

            Double levinAgg = null;
            Double levinSort = null;
            if (levinIndex < levin.trajectory.size() &&
                levin.trajectory.get(levinIndex).getStepNumber() == step) {
                levinAgg = levinAggMetric.compute(levin.trajectory.get(levinIndex));
                levinSort = levinSortMetric.compute(levin.trajectory.get(levinIndex));
                levinIndex++;
            }

            if (edeAgg != null || levinAgg != null) {
                records.add(new ComparisonTrajectory(
                    trial, seed, step,
                    edeAgg, edeSort,
                    levinAgg, levinSort
                ));
            }
        }

        return records;
    }

    /**
     * Create summary statistics for one trial.
     */
    private ComparisonSummary createSummary(
            EDEResult ede, LevinResult levin, int trial, long seed) {

        AlgotypeAggregationIndex<GenericCell> edeMetric = new AlgotypeAggregationIndex<>();
        AlgotypeAggregationIndex<AlgotypedCell> levinMetric = new AlgotypeAggregationIndex<>();

        // EDE stats
        double edeInitial = ede.trajectory.isEmpty() ? 0 : edeMetric.compute(ede.trajectory.get(0));
        double edePeak = edeInitial;
        int edePeakStep = 0;
        double edeFinal = edeInitial;

        for (int i = 0; i < ede.trajectory.size(); i++) {
            double current = edeMetric.compute(ede.trajectory.get(i));
            if (current > edePeak) {
                edePeak = current;
                edePeakStep = ede.trajectory.get(i).getStepNumber();
            }
            if (i == ede.trajectory.size() - 1) {
                edeFinal = current;
            }
        }

        // Levin stats
        double levinInitial = levin.trajectory.isEmpty() ? 0 : levinMetric.compute(levin.trajectory.get(0));
        double levinPeak = levinInitial;
        int levinPeakStep = 0;
        double levinFinal = levinInitial;

        for (int i = 0; i < levin.trajectory.size(); i++) {
            double current = levinMetric.compute(levin.trajectory.get(i));
            if (current > levinPeak) {
                levinPeak = current;
                levinPeakStep = levin.trajectory.get(i).getStepNumber();
            }
            if (i == levin.trajectory.size() - 1) {
                levinFinal = current;
            }
        }

        return new ComparisonSummary(
            trial, seed,
            ede.finalStep, levin.finalStep,
            edeInitial, edePeak, edePeakStep, edeFinal,
            levinInitial, levinPeak, levinPeakStep, levinFinal
        );
    }

    /**
     * Write comparison trajectories to CSV.
     */
    private void writeComparisonCSV(List<ComparisonTrajectory> records, Path file)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("trial,seed,step," +
                "ede_aggregation_pct,ede_sortedness_pct," +
                "levin_aggregation_pct,levin_sortedness_pct," +
                "aggregation_delta,architecture_differs\n");

            for (ComparisonTrajectory rec : records) {
                double aggDelta = 0.0;
                boolean differs = false;
                if (rec.edeAgg != null && rec.levinAgg != null) {
                    aggDelta = Math.abs(rec.levinAgg - rec.edeAgg);
                    differs = aggDelta > 1.0;  // Consider > 1% difference as significant
                }

                writer.write(String.format("%d,%d,%d,%s,%s,%s,%s,%.4f,%s\n",
                    rec.trial, rec.seed, rec.step,
                    rec.edeAgg != null ? String.format("%.4f", rec.edeAgg) : "",
                    rec.edeSort != null ? String.format("%.4f", rec.edeSort) : "",
                    rec.levinAgg != null ? String.format("%.4f", rec.levinAgg) : "",
                    rec.levinSort != null ? String.format("%.4f", rec.levinSort) : "",
                    aggDelta,
                    differs ? "true" : "false"
                ));
            }
        }
    }

    /**
     * Write summary statistics to CSV.
     */
    private void writeSummaryCSV(List<ComparisonSummary> summaries, Path file)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("trial,seed," +
                "ede_final_step,levin_final_step," +
                "ede_initial_agg,ede_peak_agg,ede_peak_step,ede_final_agg," +
                "levin_initial_agg,levin_peak_agg,levin_peak_step,levin_final_agg," +
                "initial_delta,peak_delta,final_delta\n");

            for (ComparisonSummary sum : summaries) {
                writer.write(String.format("%d,%d,%d,%d," +
                    "%.4f,%.4f,%d,%.4f," +
                    "%.4f,%.4f,%d,%.4f," +
                    "%.4f,%.4f,%.4f\n",
                    sum.trial, sum.seed,
                    sum.edeFinalStep, sum.levinFinalStep,
                    sum.edeInitialAgg, sum.edePeakAgg, sum.edePeakStep, sum.edeFinalAgg,
                    sum.levinInitialAgg, sum.levinPeakAgg, sum.levinPeakStep, sum.levinFinalAgg,
                    Math.abs(sum.levinInitialAgg - sum.edeInitialAgg),
                    Math.abs(sum.levinPeakAgg - sum.edePeakAgg),
                    Math.abs(sum.levinFinalAgg - sum.edeFinalAgg)
                ));
            }
        }
    }

    /**
     * Write experiment metadata to JSON.
     */
    private void writeMetadata(Path file, Map<Algotype, Double> distribution)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("{\n");
            writer.write("  \"experiment_name\": \"EDE vs. Levin Architecture Comparison\",\n");
            writer.write("  \"description\": \"Side-by-side comparison of position-based (EDE) vs. cell-based (Levin) algotype binding\",\n");
            writer.write("  \"generated_at\": \"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(new Date()) + "\",\n");
            writer.write("  \"hypothesis\": {\n");
            writer.write("    \"ede\": \"Algotypes tied to positions → constant aggregation during sorting\",\n");
            writer.write("    \"levin\": \"Algotypes bound to cells → dynamic aggregation during sorting\"\n");
            writer.write("  },\n");
            writer.write("  \"parameters\": {\n");
            writer.write("    \"array_size\": " + ARRAY_SIZE + ",\n");
            writer.write("    \"max_steps\": " + MAX_STEPS + ",\n");
            writer.write("    \"trials\": " + TRIALS + ",\n");
            writer.write("    \"base_seed\": " + BASE_SEED + ",\n");
            writer.write("    \"distribution\": \"50/50 Bubble/Selection\"\n");
            writer.write("  },\n");
            writer.write("  \"outputs\": {\n");
            writer.write("    \"comparison_csv\": \"" + COMPARISON_CSV + "\",\n");
            writer.write("    \"summary_csv\": \"" + SUMMARY_CSV + "\"\n");
            writer.write("  },\n");
            writer.write("  \"metrics\": {\n");
            writer.write("    \"aggregation_pct\": \"Percentage of cells with ≥1 same-algotype neighbor\",\n");
            writer.write("    \"sortedness_pct\": \"Percentage of cells in correct final position\",\n");
            writer.write("    \"aggregation_delta\": \"Absolute difference between EDE and Levin aggregation\"\n");
            writer.write("  },\n");
            writer.write("  \"reference\": \"Levin et al. (2024), arXiv:2401.05375v1\",\n");
            writer.write("  \"repository\": \"https://github.com/zfifteen/emergent-doom-engine\"\n");
            writer.write("}\n");
        }
    }

    /**
     * Analyze and report key findings.
     */
    private void analyzeResults(List<ComparisonSummary> summaries) {
        double avgEdeInitial = summaries.stream().mapToDouble(s -> s.edeInitialAgg).average().orElse(0);
        double avgEdePeak = summaries.stream().mapToDouble(s -> s.edePeakAgg).average().orElse(0);
        double avgEdeFinal = summaries.stream().mapToDouble(s -> s.edeFinalAgg).average().orElse(0);

        double avgLevinInitial = summaries.stream().mapToDouble(s -> s.levinInitialAgg).average().orElse(0);
        double avgLevinPeak = summaries.stream().mapToDouble(s -> s.levinPeakAgg).average().orElse(0);
        double avgLevinFinal = summaries.stream().mapToDouble(s -> s.levinFinalAgg).average().orElse(0);

        System.out.printf("EDE Approach (Position-Based Algotypes):%n");
        System.out.printf("  Initial: %.2f%% → Peak: %.2f%% → Final: %.2f%%%n",
            avgEdeInitial, avgEdePeak, avgEdeFinal);
        System.out.printf("  Variance: %.2f%% (peak - initial), %.2f%% (final - initial)%n",
            avgEdePeak - avgEdeInitial, avgEdeFinal - avgEdeInitial);

        System.out.printf("Levin Approach (Cell-Based Algotypes):%n");
        System.out.printf("  Initial: %.2f%% → Peak: %.2f%% → Final: %.2f%%%n",
            avgLevinInitial, avgLevinPeak, avgLevinFinal);
        System.out.printf("  Variance: %.2f%% (peak - initial), %.2f%% (final - initial)%n",
            avgLevinPeak - avgLevinInitial, avgLevinFinal - avgLevinInitial);

        System.out.printf("Difference (Levin - EDE):%n");
        System.out.printf("  Initial: %.2f%%, Peak: %.2f%%, Final: %.2f%%%n",
            avgLevinInitial - avgEdeInitial,
            avgLevinPeak - avgEdePeak,
            avgLevinFinal - avgEdeFinal);

        // Determine if hypothesis is supported
        double edeVariance = Math.abs(avgEdeFinal - avgEdeInitial);
        double levinVariance = Math.abs(avgLevinFinal - avgLevinInitial);

        System.out.println();
        if (edeVariance < 5.0 && levinVariance > 5.0) {
            System.out.println("✓ HYPOTHESIS SUPPORTED:");
            System.out.println("  - EDE shows minimal aggregation variance (constant pattern)");
            System.out.println("  - Levin shows significant aggregation variance (dynamic pattern)");
        } else if (edeVariance < 5.0 && levinVariance < 5.0) {
            System.out.println("⚠ HYPOTHESIS PARTIALLY SUPPORTED:");
            System.out.println("  - Both approaches show constant aggregation");
            System.out.println("  - Levin approach may need different initial conditions for dynamic behavior");
        } else {
            System.out.println("✗ HYPOTHESIS NOT SUPPORTED:");
            System.out.println("  - Unexpected aggregation patterns observed");
        }
    }

    // Data structures

    private static class EDEResult {
        final int finalStep;
        final List<StepSnapshot<GenericCell>> trajectory;

        EDEResult(int finalStep, List<StepSnapshot<GenericCell>> trajectory) {
            this.finalStep = finalStep;
            this.trajectory = trajectory;
        }
    }

    private static class LevinResult {
        final int finalStep;
        final List<StepSnapshot<AlgotypedCell>> trajectory;

        LevinResult(int finalStep, List<StepSnapshot<AlgotypedCell>> trajectory) {
            this.finalStep = finalStep;
            this.trajectory = trajectory;
        }
    }

    private static class ComparisonTrajectory {
        final int trial;
        final long seed;
        final int step;
        final Double edeAgg;
        final Double edeSort;
        final Double levinAgg;
        final Double levinSort;

        ComparisonTrajectory(int trial, long seed, int step,
                           Double edeAgg, Double edeSort,
                           Double levinAgg, Double levinSort) {
            this.trial = trial;
            this.seed = seed;
            this.step = step;
            this.edeAgg = edeAgg;
            this.edeSort = edeSort;
            this.levinAgg = levinAgg;
            this.levinSort = levinSort;
        }
    }

    private static class ComparisonSummary {
        final int trial;
        final long seed;
        final int edeFinalStep;
        final int levinFinalStep;
        final double edeInitialAgg;
        final double edePeakAgg;
        final int edePeakStep;
        final double edeFinalAgg;
        final double levinInitialAgg;
        final double levinPeakAgg;
        final int levinPeakStep;
        final double levinFinalAgg;

        ComparisonSummary(int trial, long seed,
                         int edeFinalStep, int levinFinalStep,
                         double edeInitialAgg, double edePeakAgg, int edePeakStep, double edeFinalAgg,
                         double levinInitialAgg, double levinPeakAgg, int levinPeakStep, double levinFinalAgg) {
            this.trial = trial;
            this.seed = seed;
            this.edeFinalStep = edeFinalStep;
            this.levinFinalStep = levinFinalStep;
            this.edeInitialAgg = edeInitialAgg;
            this.edePeakAgg = edePeakAgg;
            this.edePeakStep = edePeakStep;
            this.edeFinalAgg = edeFinalAgg;
            this.levinInitialAgg = levinInitialAgg;
            this.levinPeakAgg = levinPeakAgg;
            this.levinPeakStep = levinPeakStep;
            this.levinFinalAgg = levinFinalAgg;
        }
    }
}
