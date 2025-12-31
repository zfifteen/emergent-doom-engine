package com.emergent.doom.examples;

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
import com.emergent.doom.metrics.SortednessValue;
import com.emergent.doom.probe.StepSnapshot;
import com.emergent.doom.topology.ChimericTopology;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

/**
 * Experiment to validate emergent clustering in chimeric populations.
 *
 * <p>From Levin et al. (2024), p.11-12:
 * "At the beginning of these experiments, we randomly assigned one of the three
 * different Algotypes to each of the cells... The extent of the emergent local
 * grouping (aggregation) was measured... at the beginning the Aggregation Value
 * for a 50/50 mix was 50% (expected for random distribution). Then as sorting
 * proceeds, this value rises as cells of the same Algotype tend to cluster."</p>
 *
 * <p>This experiment:
 * <ul>
 *   <li>Creates 50/50 Bubble/Selection chimeric populations</li>
 *   <li>Tracks AlgotypeAggregationIndex over sorting process</li>
 *   <li>Validates transient clustering (peak ~60-70%)</li>
 *   <li>Exports trajectory data for analysis</li>
 * </ul></p>
 *
 * <p>Expected results (per paper Figure 8):
 * <ul>
 *   <li>Initial aggregation: ~50% (random baseline)</li>
 *   <li>Peak aggregation: ~60-72% (mid-process clustering)</li>
 *   <li>Final aggregation: ~50% (returns to baseline)</li>
 * </ul></p>
 */
public class ChimericClusteringExperiment {

    private static final int ARRAY_SIZE = 100;
    private static final int MAX_STEPS = 5000;
    private static final int NUM_TRIALS = 100;
    private static final long BASE_SEED = 42L;

    public static void main(String[] args) {
        System.out.println("Emergent Doom Engine - Chimeric Clustering Experiment");
        System.out.println("=".repeat(70));
        System.out.println();

        // Run main experiment: 50/50 Bubble/Selection
        System.out.println("Experiment 1: 50/50 Bubble/Selection Mix");
        System.out.println("-".repeat(70));
        runChimericExperiment(
                Map.of(Algotype.BUBBLE, 0.5, Algotype.SELECTION, 0.5),
                "bubble_selection"
        );

        // Run control: 100% Bubble (non-chimeric)
        System.out.println("\nExperiment 2: Control - 100% Bubble (Non-chimeric)");
        System.out.println("-".repeat(70));
        runChimericExperiment(
                Map.of(Algotype.BUBBLE, 1.0),
                "bubble_only"
        );

        // Run additional mix: 50/50 Bubble/Insertion
        System.out.println("\nExperiment 3: 50/50 Bubble/Insertion Mix");
        System.out.println("-".repeat(70));
        runChimericExperiment(
                Map.of(Algotype.BUBBLE, 0.5, Algotype.INSERTION, 0.5),
                "bubble_insertion"
        );

        System.out.println("\n" + "=".repeat(70));
        System.out.println("Experiment complete!");
    }

    private static void runChimericExperiment(Map<Algotype, Double> mix, String experimentName) {
        // Build configuration
        ChimericExperimentConfig config = ChimericExperimentConfig.builder()
                .arraySize(ARRAY_SIZE)
                .maxSteps(MAX_STEPS)
                .requiredStableSteps(3)
                .recordTrajectory(true)
                .algotypeMix(mix)
                .seed(BASE_SEED)
                .build();

        System.out.printf("Array size: %d, Max steps: %d, Trials: %d%n",
                ARRAY_SIZE, MAX_STEPS, NUM_TRIALS);
        System.out.printf("Algotype mix: %s%n", mix);
        System.out.printf("Seed: %d%n%n", BASE_SEED);

        // Create experiment runner with chimeric population
        ExperimentRunner<GenericCell> runner = new ExperimentRunner<>(
                () -> createChimericArray(config),
                ChimericTopology::new
        );

        // Add metrics
        runner.addMetric("Aggregation", new AlgotypeAggregationIndex<>());
        runner.addMetric("Sortedness", new SortednessValue<>());

        // Run experiment
        System.out.printf("Running %d trials...%n", NUM_TRIALS);
        long startTime = System.currentTimeMillis();
        ExperimentResults<GenericCell> results = runner.runExperiment(config, NUM_TRIALS);
        long elapsed = System.currentTimeMillis() - startTime;

        // Analyze aggregation trajectories
        AggregationAnalysis analysis = analyzeAggregationTrajectories(results);

        // Print results
        System.out.println();
        System.out.println(results.getSummaryReport());
        System.out.println("Aggregation Trajectory Analysis:");
        System.out.println("-".repeat(40));
        System.out.printf("  Initial aggregation: %.2f%% +/- %.2f%%%n",
                analysis.meanInitial, analysis.stdInitial);
        System.out.printf("  Peak aggregation:    %.2f%% +/- %.2f%%%n",
                analysis.meanPeak, analysis.stdPeak);
        System.out.printf("  Final aggregation:   %.2f%% +/- %.2f%%%n",
                analysis.meanFinal, analysis.stdFinal);
        System.out.printf("  Peak step (mean):    %.1f%n", analysis.meanPeakStep);
        System.out.println();
        System.out.printf("Execution time: %.2f seconds%n", elapsed / 1000.0);

        // Validate against paper expectations
        boolean isChimeric = mix.size() > 1;
        validateResults(analysis, isChimeric);

        // Export trajectory data
        exportTrajectoryCSV(results, experimentName);
    }

    private static GenericCell[] createChimericArray(ChimericExperimentConfig config) {
        Map<Algotype, Double> mix = config.getChimericMix();
        long seed = config.getSeed();
        int size = config.getArraySize();

        PercentageAlgotypeProvider provider = new PercentageAlgotypeProvider(mix, size, seed);
        GenericCellFactory factory = GenericCellFactory.shuffled(size, seed + 1);
        ChimericPopulation<GenericCell> population = new ChimericPopulation<>(factory, provider);

        return population.createPopulation(size, GenericCell.class);
    }

    private static AggregationAnalysis analyzeAggregationTrajectories(
            ExperimentResults<GenericCell> results) {

        List<Double> initialValues = new ArrayList<>();
        List<Double> peakValues = new ArrayList<>();
        List<Double> finalValues = new ArrayList<>();
        List<Double> peakSteps = new ArrayList<>();

        AlgotypeAggregationIndex<GenericCell> aggregationMetric = new AlgotypeAggregationIndex<>();

        for (TrialResult<GenericCell> trial : results.getTrials()) {
            List<StepSnapshot<GenericCell>> trajectory = trial.getTrajectory();
            if (trajectory == null || trajectory.isEmpty()) continue;

            // Compute aggregation at each step
            double peak = 0.0;
            int peakStep = 0;
            double initial = aggregationMetric.compute(trajectory.get(0).getCellStates());
            double finalVal = aggregationMetric.compute(
                    trajectory.get(trajectory.size() - 1).getCellStates());

            for (StepSnapshot<GenericCell> snapshot : trajectory) {
                double agg = aggregationMetric.compute(snapshot.getCellStates());
                if (agg > peak) {
                    peak = agg;
                    peakStep = snapshot.getStepNumber();
                }
            }

            initialValues.add(initial);
            peakValues.add(peak);
            finalValues.add(finalVal);
            peakSteps.add((double) peakStep);
        }

        return new AggregationAnalysis(
                mean(initialValues), stdDev(initialValues),
                mean(peakValues), stdDev(peakValues),
                mean(finalValues), stdDev(finalValues),
                mean(peakSteps)
        );
    }

    private static void validateResults(AggregationAnalysis analysis, boolean isChimeric) {
        System.out.println("Validation:");
        System.out.println("-".repeat(40));

        if (isChimeric) {
            // For chimeric: expect peak > initial
            boolean peakAboveInitial = analysis.meanPeak > analysis.meanInitial + 5.0;
            System.out.printf("  Peak above initial (+5%%): %s (%.2f > %.2f)%n",
                    peakAboveInitial ? "PASS" : "FAIL",
                    analysis.meanPeak, analysis.meanInitial + 5.0);

            // Paper target: peak 60-72%
            boolean peakInRange = analysis.meanPeak >= 55.0 && analysis.meanPeak <= 80.0;
            System.out.printf("  Peak in expected range [55-80%%]: %s (%.2f%%)%n",
                    peakInRange ? "PASS" : "WARN",
                    analysis.meanPeak);

            // Transient behavior: peak should be above final
            boolean transient_ = analysis.meanPeak > analysis.meanFinal;
            System.out.printf("  Transient clustering (peak > final): %s%n",
                    transient_ ? "PASS" : "FAIL");
        } else {
            // For non-chimeric: should always be 100% (homogeneous)
            boolean stable = analysis.meanInitial > 95.0 && analysis.meanFinal > 95.0;
            System.out.printf("  Homogeneous array (>95%% aggregation): %s%n",
                    stable ? "PASS" : "FAIL");
        }
    }

    private static void exportTrajectoryCSV(ExperimentResults<GenericCell> results,
                                             String experimentName) {
        String filename = experimentName + "_trajectories.csv";
        AlgotypeAggregationIndex<GenericCell> aggregationMetric = new AlgotypeAggregationIndex<>();
        SortednessValue<GenericCell> sortednessMetric = new SortednessValue<>();

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("trial,step,aggregation,sortedness");

            int trialNum = 0;
            for (TrialResult<GenericCell> trial : results.getTrials()) {
                List<StepSnapshot<GenericCell>> trajectory = trial.getTrajectory();
                if (trajectory == null) continue;

                // Sample every 10 steps to keep file size manageable
                for (int i = 0; i < trajectory.size(); i += 10) {
                    StepSnapshot<GenericCell> snapshot = trajectory.get(i);
                    GenericCell[] cells = snapshot.getCellStates();
                    double agg = aggregationMetric.compute(cells);
                    double sort = sortednessMetric.compute(cells);
                    writer.printf("%d,%d,%.4f,%.4f%n",
                            trialNum, snapshot.getStepNumber(), agg, sort);
                }

                // Always include final step
                StepSnapshot<GenericCell> finalSnapshot = trajectory.get(trajectory.size() - 1);
                GenericCell[] cells = finalSnapshot.getCellStates();
                double agg = aggregationMetric.compute(cells);
                double sort = sortednessMetric.compute(cells);
                writer.printf("%d,%d,%.4f,%.4f%n",
                        trialNum, finalSnapshot.getStepNumber(), agg, sort);

                trialNum++;
            }

            System.out.printf("Exported trajectory data to: %s%n", filename);
        } catch (IOException e) {
            System.err.println("Failed to export trajectory data: " + e.getMessage());
        }
    }

    private static double mean(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static double stdDev(List<Double> values) {
        if (values.size() < 2) return 0.0;
        double mean = mean(values);
        double sumSquaredDiff = values.stream()
                .mapToDouble(v -> (v - mean) * (v - mean))
                .sum();
        return Math.sqrt(sumSquaredDiff / values.size());
    }

    /**
     * Container for aggregation trajectory statistics.
     */
    private static class AggregationAnalysis {
        final double meanInitial, stdInitial;
        final double meanPeak, stdPeak;
        final double meanFinal, stdFinal;
        final double meanPeakStep;

        AggregationAnalysis(double meanInitial, double stdInitial,
                           double meanPeak, double stdPeak,
                           double meanFinal, double stdFinal,
                           double meanPeakStep) {
            this.meanInitial = meanInitial;
            this.stdInitial = stdInitial;
            this.meanPeak = meanPeak;
            this.stdPeak = stdPeak;
            this.meanFinal = meanFinal;
            this.stdFinal = stdFinal;
            this.meanPeakStep = meanPeakStep;
        }
    }
}
