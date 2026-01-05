package com.emergent.doom.experiments.clustering;

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
import com.emergent.doom.probe.StepSnapshot;
import com.emergent.doom.topology.ChimericTopology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates that chimeric clustering reproduces the foundational results from Levin et al. (2024).
 *
 * <p><strong>PURPOSE:</strong> This experiment validates the baseline clustering behavior
 * observed in the Levin paper before extracting ClusteringPrimitive API. It runs controlled
 * chimeric experiments to empirically confirm aggregation patterns match paper expectations.</p>
 *
 * <p><strong>EXPERIMENTS:</strong>
 * <ul>
 *   <li>Bubble-Selection: Expected peak 72% ± 5% at 42% ± 5% progress</li>
 *   <li>Bubble-Insertion: Expected peak 65% ± 5% at 21% ± 5% progress</li>
 *   <li>Selection-Insertion: Expected peak 69% ± 5% at 19% ± 5% progress</li>
 *   <li>Negative Control (Bubble-Bubble): Expected < 60% throughout (random baseline)</li>
 * </ul></p>
 *
 * <p><strong>SUCCESS CRITERIA:</strong>
 * <ul>
 *   <li>Peak aggregation matches paper values (within tolerance)</li>
 *   <li>Peak timing matches paper values (within tolerance)</li>
 *   <li>Statistical significance: p < 0.05 vs control</li>
 *   <li>Negative control stays below random baseline</li>
 * </ul></p>
 *
 * <p><strong>REFERENCE:</strong> Zhang, Goldstein, Levin (2024) - Classical Sorting Algorithms
 * as a Model of Morphogenesis, arXiv:2401.05375v1</p>
 */
public class ClusteringValidationExperiment {

    // PURPOSE: Store expected peak aggregation and timing for an algotype pair
    // FIELDS:
    //   - peakAggregation: Expected peak value (0.0 to 1.0, e.g., 0.72 for 72%)
    //   - peakTiming: Expected timing as fraction of sorting progress (0.0 to 1.0, e.g., 0.42 for 42%)
    // USAGE: Used to validate experimental results against paper baselines
    public static class ExpectedResult {
        private final double peakAggregation;
        private final double peakTiming;
        
        public ExpectedResult(double peakAggregation, double peakTiming) {
            this.peakAggregation = peakAggregation;
            this.peakTiming = peakTiming;
        }
        
        public double peakAggregation() { return peakAggregation; }
        public double peakTiming() { return peakTiming; }
    }

    // PURPOSE: Store complete validation report for all experiments
    // FIELDS:
    //   - pairResults: Map from algotype pair to their validation results
    //   - controlResult: Validation result for negative control (homogeneous array)
    //   - timestamp: When the validation was performed
    //   - totalTrialsRun: Total number of trials executed across all experiments
    //   - hardwareInfo: CPU, RAM, Java version for reproducibility
    // USAGE: Comprehensive report of validation experiment outcomes
    public static class ValidationReport {
        private final Map<AlgotypePair, PairValidationResult> pairResults;
        private final PairValidationResult controlResult;
        private final long timestamp;
        private final int totalTrialsRun;
        private final String hardwareInfo;
        
        public ValidationReport(Map<AlgotypePair, PairValidationResult> pairResults,
                               PairValidationResult controlResult,
                               long timestamp,
                               int totalTrialsRun,
                               String hardwareInfo) {
            this.pairResults = pairResults;
            this.controlResult = controlResult;
            this.timestamp = timestamp;
            this.totalTrialsRun = totalTrialsRun;
            this.hardwareInfo = hardwareInfo;
        }
        
        public Map<AlgotypePair, PairValidationResult> pairResults() { return pairResults; }
        public PairValidationResult controlResult() { return controlResult; }
        public long timestamp() { return timestamp; }
        public int totalTrialsRun() { return totalTrialsRun; }
        public String hardwareInfo() { return hardwareInfo; }
    }

    // PURPOSE: Store aggregation pair identifier (e.g., "Bubble-Selection")
    // FIELDS:
    //   - first: First algotype in the pair
    //   - second: Second algotype in the pair
    // USAGE: Key for storing and retrieving validation results
    public static class AlgotypePair {
        private final Algotype first;
        private final Algotype second;
        
        public AlgotypePair(Algotype first, Algotype second) {
            this.first = first;
            this.second = second;
        }
        
        public Algotype first() { return first; }
        public Algotype second() { return second; }
        
        @Override
        public String toString() {
            return first.name() + "-" + second.name();
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AlgotypePair that = (AlgotypePair) o;
            return first == that.first && second == that.second;
        }
        
        @Override
        public int hashCode() {
            return 31 * first.hashCode() + second.hashCode();
        }
    }

    // PURPOSE: Store validation results for a single algotype pair
    // FIELDS:
    //   - pair: The algotype pair tested
    //   - meanPeakAggregation: Mean peak aggregation across all trials
    //   - stdPeakAggregation: Standard deviation of peak aggregation
    //   - meanPeakTiming: Mean timing of peak (as fraction of total steps)
    //   - stdPeakTiming: Standard deviation of peak timing
    //   - pValueVsPaper: p-value from t-test comparing to paper expectation
    //   - pValueVsControl: p-value from t-test comparing to negative control
    //   - allPeakValues: Raw peak values from all trials (for statistical analysis)
    //   - allPeakTimings: Raw peak timings from all trials (as fraction of convergence step)
    // USAGE: Complete statistical summary of one algotype pair's clustering behavior
    public static class PairValidationResult {
        private final AlgotypePair pair;
        private final double meanPeakAggregation;
        private final double stdPeakAggregation;
        private final double meanPeakTiming;
        private final double stdPeakTiming;
        private final double pValueVsPaper;
        private final double pValueVsControl;
        private final List<Double> allPeakValues;
        private final List<Double> allPeakTimings;
        
        public PairValidationResult(AlgotypePair pair,
                                   double meanPeakAggregation,
                                   double stdPeakAggregation,
                                   double meanPeakTiming,
                                   double stdPeakTiming,
                                   double pValueVsPaper,
                                   double pValueVsControl,
                                   List<Double> allPeakValues,
                                   List<Double> allPeakTimings) {
            this.pair = pair;
            this.meanPeakAggregation = meanPeakAggregation;
            this.stdPeakAggregation = stdPeakAggregation;
            this.meanPeakTiming = meanPeakTiming;
            this.stdPeakTiming = stdPeakTiming;
            this.pValueVsPaper = pValueVsPaper;
            this.pValueVsControl = pValueVsControl;
            this.allPeakValues = allPeakValues;
            this.allPeakTimings = allPeakTimings;
        }
        
        public AlgotypePair pair() { return pair; }
        public double meanPeakAggregation() { return meanPeakAggregation; }
        public double stdPeakAggregation() { return stdPeakAggregation; }
        public double meanPeakTiming() { return meanPeakTiming; }
        public double stdPeakTiming() { return stdPeakTiming; }
        public double pValueVsPaper() { return pValueVsPaper; }
        public double pValueVsControl() { return pValueVsControl; }
        public List<Double> allPeakValues() { return allPeakValues; }
        public List<Double> allPeakTimings() { return allPeakTimings; }
    }

    // Levin paper baselines from Table/Figure data
    private static final Map<AlgotypePair, ExpectedResult> LEVIN_BASELINES = Map.of(
        new AlgotypePair(Algotype.BUBBLE, Algotype.SELECTION), new ExpectedResult(0.72, 0.42),
        new AlgotypePair(Algotype.BUBBLE, Algotype.INSERTION), new ExpectedResult(0.65, 0.21),
        new AlgotypePair(Algotype.SELECTION, Algotype.INSERTION), new ExpectedResult(0.69, 0.19)
    );

    // Experiment configuration
    private static final int ARRAY_SIZE = 100;
    private static final int MAX_STEPS = 5000;
    private static final int TRIALS_PER_PAIR = 100;
    private static final long BASE_SEED = 42L;

    /**
     * Run full validation suite: all algotype pairs + negative control.
     *
     * <p><strong>PURPOSE:</strong> As an experimenter, I want to run the complete clustering
     * validation suite so that I can verify the EDE reproduces Levin paper results.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Collect system hardware information for reproducibility</li>
     *   <li>For each algotype pair in LEVIN_BASELINES:
     *       <ul>
     *         <li>Run TRIALS_PER_PAIR experiments with 50/50 algotype mix</li>
     *         <li>Extract aggregation trajectories from each trial</li>
     *         <li>Compute peak aggregation and timing statistics</li>
     *         <li>Perform t-tests vs paper expectations and control</li>
     *         <li>Store results in PairValidationResult</li>
     *       </ul>
     *   </li>
     *   <li>Run negative control: homogeneous array (Bubble-Bubble)</li>
     *   <li>Assemble complete ValidationReport with all results</li>
     *   <li>Return report for analysis and documentation</li>
     * </ol></p>
     *
     * <p><strong>INPUTS:</strong> None (uses class constants for configuration)</p>
     *
     * <p><strong>OUTPUTS:</strong> ValidationReport containing all experimental results</p>
     *
     * <p><strong>DEPENDENCIES:</strong>
     * <ul>
     *   <li>validatePair() - runs experiments for one algotype pair</li>
     *   <li>ValidationStatistics.compareToPaper() - statistical comparison to paper</li>
     *   <li>ValidationStatistics.compareToControl() - statistical comparison to control</li>
     * </ul></p>
     *
     * @return complete validation report with all experimental results
     */
    public ValidationReport runFullValidation() {
        // PURPOSE: This main entry point orchestrates the entire clustering validation workflow
        // by running experiments for all algotype pairs plus a negative control, then
        // assembling a comprehensive report with statistical analysis.
        
        System.out.println("=".repeat(70));
        System.out.println("Clustering Validation Experiment - Levin Paper Baseline Check");
        System.out.println("=".repeat(70));
        System.out.println();
        
        // Step 1: Collect hardware info for reproducibility
        String hardwareInfo = collectHardwareInfo();
        System.out.println("Hardware Configuration:");
        System.out.println(hardwareInfo);
        System.out.println();
        
        // Step 2: Run negative control first (homogeneous Bubble-Bubble array)
        // This establishes the random baseline that chimeric experiments should exceed
        System.out.println("Running Negative Control (Bubble-Bubble homogeneous)...");
        AlgotypePair controlPair = new AlgotypePair(Algotype.BUBBLE, Algotype.BUBBLE);
        PairValidationResult controlResult = validatePair(
            Algotype.BUBBLE, 
            Algotype.BUBBLE,
            new ExpectedResult(0.50, 0.50),  // Expect random baseline
            TRIALS_PER_PAIR,
            null  // No control comparison for the control itself
        );
        System.out.printf("  Control peak: %.2f%% ± %.2f%%%n%n", 
            controlResult.meanPeakAggregation(), controlResult.stdPeakAggregation());
        
        // Step 3: Run all chimeric algotype pairs from Levin baselines
        Map<AlgotypePair, PairValidationResult> pairResults = new HashMap<>();
        int totalTrials = TRIALS_PER_PAIR;  // Already ran control trials
        
        for (Map.Entry<AlgotypePair, ExpectedResult> entry : LEVIN_BASELINES.entrySet()) {
            AlgotypePair pair = entry.getKey();
            ExpectedResult expected = entry.getValue();
            
            System.out.printf("Running %s (50/50 mix)...%n", pair);
            System.out.printf("  Expected: %.0f%% peak at %.0f%% progress%n",
                expected.peakAggregation() * 100, expected.peakTiming() * 100);
            
            // Run experiments for this pair, comparing to control
            PairValidationResult result = validatePair(
                pair.first(),
                pair.second(),
                expected,
                TRIALS_PER_PAIR,
                controlResult.allPeakValues()  // Pass control peaks for statistical comparison
            );
            
            pairResults.put(pair, result);
            totalTrials += TRIALS_PER_PAIR;
            
            // Print immediate results for this pair
            System.out.printf("  Observed: %.2f%% ± %.2f%% at %.2f%% ± %.2f%% progress%n",
                result.meanPeakAggregation(), result.stdPeakAggregation(),
                result.meanPeakTiming() * 100, result.stdPeakTiming() * 100);
            System.out.printf("  p-value vs paper: %.4f %s%n",
                result.pValueVsPaper(),
                result.pValueVsPaper() >= 0.05 ? "✓ (matches)" : "✗ (differs)");
            System.out.printf("  p-value vs control: %.4f %s%n%n",
                result.pValueVsControl(),
                result.pValueVsControl() < 0.05 ? "✓ (real clustering)" : "✗ (no clustering)");
        }
        
        // Step 4: Assemble complete validation report
        ValidationReport report = new ValidationReport(
            pairResults,
            controlResult,
            System.currentTimeMillis(),
            totalTrials,
            hardwareInfo
        );
        
        System.out.println("=".repeat(70));
        System.out.println("Validation Complete!");
        System.out.printf("Total trials run: %d%n", totalTrials);
        System.out.println("=".repeat(70));
        
        return report;
    }

    /**
     * Validate one algotype pair against expected baseline.
     *
     * <p><strong>PURPOSE:</strong> Run experiments for a specific algotype pair and
     * collect statistical data on clustering behavior.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Create ChimericExperimentConfig with 50/50 mix of the two algotypes</li>
     *   <li>Set up ExperimentRunner with GenericCellFactory and ChimericTopology</li>
     *   <li>Add AlgotypeAggregationIndex metric to track clustering</li>
     *   <li>Run TRIALS_PER_PAIR trials with full trajectory recording</li>
     *   <li>For each trial:
     *       <ul>
     *         <li>Extract aggregation trajectory from recorded snapshots</li>
     *         <li>Identify peak aggregation value</li>
     *         <li>Identify timing of peak (as fraction of total steps to convergence)</li>
     *         <li>Store both values for statistical analysis</li>
     *       </ul>
     *   </li>
     *   <li>Compute mean and standard deviation of peak values and timings</li>
     *   <li>Perform t-test comparing observed peaks to expected baseline</li>
     *   <li>Return PairValidationResult with all statistics</li>
     * </ol></p>
     *
     * <p><strong>INPUTS:</strong>
     * <ul>
     *   <li>a - First algotype in the pair</li>
     *   <li>b - Second algotype in the pair</li>
     *   <li>expected - Expected peak aggregation and timing from Levin paper</li>
     *   <li>trials - Number of trials to run (typically 100)</li>
     *   <li>controlPeaks - Peak values from negative control for comparison (null if not available yet)</li>
     * </ul></p>
     *
     * <p><strong>OUTPUTS:</strong> PairValidationResult with complete statistical summary</p>
     *
     * <p><strong>DEPENDENCIES:</strong>
     * <ul>
     *   <li>ChimericExperimentConfig - configuration builder</li>
     *   <li>ExperimentRunner - batch execution framework</li>
     *   <li>AlgotypeAggregationIndex - clustering metric</li>
     *   <li>extractAggregationTrajectory() - extracts trajectory from trial results</li>
     *   <li>ValidationStatistics - statistical analysis methods</li>
     * </ul></p>
     *
     * @param a first algotype
     * @param b second algotype
     * @param expected expected results from Levin paper
     * @param trials number of trials to run
     * @param controlPeaks peak values from negative control (for t-test), or null
     * @return validation result with statistics
     */
    private PairValidationResult validatePair(
        Algotype a,
        Algotype b,
        ExpectedResult expected,
        int trials,
        List<Double> controlPeaks
    ) {
        // Step 1: Build experiment configuration with 50/50 algotype mix
        ChimericExperimentConfig config = ChimericExperimentConfig.builder()
            .arraySize(ARRAY_SIZE)
            .maxSteps(MAX_STEPS)
            .requiredStableSteps(3)
            .recordTrajectory(true)
            .algotypeMix(Map.of(a, 0.5, b, 0.5))
            .seed(BASE_SEED)
            .build();
        
        // Step 2: Create experiment runner
        ExperimentRunner<GenericCell> runner = new ExperimentRunner<>(
            () -> createChimericArray(config),
            ChimericTopology::new
        );
        
        // Step 3: Add aggregation metric
        runner.addMetric("Aggregation", new AlgotypeAggregationIndex<>());
        
        // Step 4: Run trials
        ExperimentResults<GenericCell> results = runner.runExperiment(config, trials);
        
        // Step 5: Extract peak values and timings from all trials
        List<Double> peakValues = new ArrayList<>();
        List<Double> peakTimings = new ArrayList<>();
        AlgotypeAggregationIndex<GenericCell> metric = new AlgotypeAggregationIndex<>();
        
        for (TrialResult<GenericCell> trial : results.getTrials()) {
            // Extract aggregation trajectory
            List<Double> trajectory = extractAggregationTrajectory(trial, metric);
            
            // Find peak in this trial
            int totalSteps = trial.getFinalStep();
            PeakInfo peak = findPeak(trajectory, totalSteps);
            
            peakValues.add(peak.peakValue());
            peakTimings.add(peak.timing());
        }
        
        // Step 6: Compute statistics
        double meanPeak = mean(peakValues);
        double stdPeak = stdDev(peakValues);
        double meanTiming = mean(peakTimings);
        double stdTiming = stdDev(peakTimings);
        
        // Step 7: Perform statistical tests
        ValidationStatistics.TTestResult paperComparison = 
            ValidationStatistics.compareToPaper(peakValues, expected.peakAggregation());
        double pValueVsPaper = paperComparison.pValue();
        
        double pValueVsControl = 0.0;
        if (controlPeaks != null && !controlPeaks.isEmpty()) {
            ValidationStatistics.TTestResult controlComparison =
                ValidationStatistics.compareToControl(peakValues, controlPeaks);
            pValueVsControl = controlComparison.pValue();
        }
        
        // Step 8: Return complete validation result
        return new PairValidationResult(
            new AlgotypePair(a, b),
            meanPeak,
            stdPeak,
            meanTiming,
            stdTiming,
            pValueVsPaper,
            pValueVsControl,
            peakValues,
            peakTimings
        );
    }

    /**
     * Extract aggregation trajectory from a single trial result.
     *
     * <p><strong>PURPOSE:</strong> Convert raw trial snapshots into aggregation values
     * over time for peak detection and analysis.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Get trajectory snapshots from TrialResult</li>
     *   <li>For each snapshot:
     *       <ul>
     *         <li>Compute aggregation value using AlgotypeAggregationIndex</li>
     *         <li>Store value with corresponding step number</li>
     *       </ul>
     *   </li>
     *   <li>Return list of aggregation values in temporal order</li>
     * </ol></p>
     *
     * <p><strong>INPUTS:</strong>
     * <ul>
     *   <li>trial - Single trial result with recorded trajectory</li>
     *   <li>metric - AlgotypeAggregationIndex for computing aggregation at each step</li>
     * </ul></p>
     *
     * <p><strong>OUTPUTS:</strong> List of aggregation values (as percentages 0-100)</p>
     *
     * <p><strong>DEPENDENCIES:</strong>
     * <ul>
     *   <li>TrialResult.getTrajectory() - accesses recorded snapshots</li>
     *   <li>AlgotypeAggregationIndex.compute(snapshot) - computes aggregation</li>
     * </ul></p>
     *
     * @param trial trial result with trajectory
     * @param metric aggregation metric
     * @return list of aggregation values over time
     */
    private List<Double> extractAggregationTrajectory(
        TrialResult<GenericCell> trial,
        AlgotypeAggregationIndex<GenericCell> metric
    ) {
        List<StepSnapshot<GenericCell>> trajectory = trial.getTrajectory();
        if (trajectory == null || trajectory.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Double> aggregationValues = new ArrayList<>();
        for (StepSnapshot<GenericCell> snapshot : trajectory) {
            double aggregation = metric.compute(snapshot);
            aggregationValues.add(aggregation);
        }
        
        return aggregationValues;
    }

    /**
     * Find peak aggregation and its timing in a trajectory.
     *
     * <p><strong>PURPOSE:</strong> Identify the maximum aggregation value and when
     * it occurred during the sorting process.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Initialize peak = 0.0, peakStep = 0</li>
     *   <li>For each step in trajectory:
     *       <ul>
     *         <li>If current aggregation > peak:
     *             <ul>
     *               <li>Update peak = current aggregation</li>
     *               <li>Update peakStep = current step number</li>
     *             </ul>
     *         </li>
     *       </ul>
     *   </li>
     *   <li>Compute timing as fraction: peakStep / totalSteps</li>
     *   <li>Return (peak value, timing fraction)</li>
     * </ol></p>
     *
     * <p><strong>INPUTS:</strong>
     * <ul>
     *   <li>trajectory - List of aggregation values over time</li>
     *   <li>totalSteps - Total steps to convergence (for normalizing timing)</li>
     * </ul></p>
     *
     * <p><strong>OUTPUTS:</strong> PeakInfo record with peak value and timing</p>
     *
     * @param trajectory aggregation values over time
     * @param totalSteps total steps to convergence
     * @return peak information (value and timing)
     */
    private PeakInfo findPeak(List<Double> trajectory, int totalSteps) {
        if (trajectory == null || trajectory.isEmpty()) {
            return new PeakInfo(0.0, 0.0);
        }
        
        double peak = 0.0;
        int peakStep = 0;
        
        for (int i = 0; i < trajectory.size(); i++) {
            double current = trajectory.get(i);
            if (current > peak) {
                peak = current;
                peakStep = i;
            }
        }
        
        // Normalize timing as fraction of total sorting progress
        double timing = totalSteps > 0 ? (double) peakStep / totalSteps : 0.0;
        
        return new PeakInfo(peak, timing);
    }

    // PURPOSE: Store peak aggregation value and its timing
    // FIELDS:
    //   - peakValue: Maximum aggregation percentage achieved (0-100)
    //   - timing: When peak occurred as fraction of total sorting time (0.0-1.0)
    // USAGE: Return type for findPeak() method
    private static class PeakInfo {
        private final double peakValue;
        private final double timing;
        
        public PeakInfo(double peakValue, double timing) {
            this.peakValue = peakValue;
            this.timing = timing;
        }
        
        public double peakValue() { return peakValue; }
        public double timing() { return timing; }
    }

    /**
     * Create chimeric cell array for experiments.
     *
     * <p><strong>PURPOSE:</strong> Factory method to create arrays with specified
     * algotype mix for experimental trials.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Create PercentageAlgotypeProvider with specified mix and seed</li>
     *   <li>Create GenericCellFactory with shuffled values</li>
     *   <li>Combine into ChimericPopulation</li>
     *   <li>Generate array of specified size</li>
     *   <li>Return cell array ready for execution</li>
     * </ol></p>
     *
     * <p><strong>INPUTS:</strong>
     * <ul>
     *   <li>config - ChimericExperimentConfig with algotype mix and parameters</li>
     * </ul></p>
     *
     * <p><strong>OUTPUTS:</strong> Array of GenericCell with mixed algotypes</p>
     *
     * @param config experiment configuration
     * @return chimeric cell array
     */
    private static GenericCell[] createChimericArray(ChimericExperimentConfig config) {
        Map<Algotype, Double> mix = config.getChimericMix();
        long seed = config.getSeed();
        int size = config.getArraySize();
        
        PercentageAlgotypeProvider provider = new PercentageAlgotypeProvider(mix, size, seed);
        GenericCellFactory factory = GenericCellFactory.shuffled(size, seed + 1);
        ChimericPopulation<GenericCell> population = new ChimericPopulation<>(factory, provider);
        
        return population.createPopulation(size, GenericCell.class);
    }

    /**
     * Collect system hardware information for reproducibility.
     *
     * <p><strong>PURPOSE:</strong> Record execution environment details so results
     * can be properly contextualized and reproduced.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Get Java version from System.getProperty("java.version")</li>
     *   <li>Get OS information from System.getProperty("os.name", "os.version")</li>
     *   <li>Get available processors from Runtime.getRuntime().availableProcessors()</li>
     *   <li>Get max memory from Runtime.getRuntime().maxMemory()</li>
     *   <li>Format into human-readable string</li>
     *   <li>Return hardware info string</li>
     * </ol></p>
     *
     * <p><strong>INPUTS:</strong> None (reads from system properties)</p>
     *
     * <p><strong>OUTPUTS:</strong> String with hardware/environment information</p>
     *
     * @return hardware information string
     */
    private static String collectHardwareInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMB = runtime.maxMemory() / (1024 * 1024);
        
        return String.format(
            "Java %s, %s %s, %d processors, %d MB max memory",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version"),
            runtime.availableProcessors(),
            maxMemoryMB
        );
    }

    /**
     * Compute mean of a list of values.
     *
     * <p><strong>PURPOSE:</strong> Calculate average value for statistical analysis.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>If list is empty, return 0.0</li>
     *   <li>Sum all values</li>
     *   <li>Divide by count</li>
     *   <li>Return mean</li>
     * </ol></p>
     *
     * @param values list of numeric values
     * @return mean (average) value
     */
    private static double mean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Compute standard deviation of a list of values.
     *
     * <p><strong>PURPOSE:</strong> Measure spread/variance for statistical analysis.</p>
     *
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>If list has < 2 values, return 0.0</li>
     *   <li>Compute mean using mean() method</li>
     *   <li>For each value, compute (value - mean)²</li>
     *   <li>Sum all squared differences</li>
     *   <li>Divide by (n - 1) for sample standard deviation (Bessel's correction)</li>
     *   <li>Take square root</li>
     *   <li>Return standard deviation</li>
     * </ol></p>
     *
     * @param values list of numeric values
     * @return sample standard deviation
     */
    private static double stdDev(List<Double> values) {
        if (values == null || values.size() < 2) {
            return 0.0;
        }
        double meanVal = mean(values);
        double sumSquaredDiff = values.stream()
            .mapToDouble(v -> (v - meanVal) * (v - meanVal))
            .sum();
        return Math.sqrt(sumSquaredDiff / (values.size() - 1));
    }
}
