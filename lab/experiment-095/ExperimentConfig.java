package lab.experiment095;

/**
 * Configuration container for Wave-CRISPR-Signal experiment.
 * 
 * PURPOSE:
 * Holds all experimental parameters, dataset paths, and hyperparameters needed
 * to execute the experiment. Acts as a central configuration object passed 
 * between all experimental components.
 * 
 * REQUIREMENTS SATISFIED:
 * - Dataset paths (Section 1 of protocol)
 * - Feature extraction parameters (Section 2)
 * - Emergent sorter parameters (Section 3)
 * - MLP training parameters (Section 4)
 * - Validation parameters (Sections 5-6)
 * 
 * ARCHITECTURE:
 * Immutable configuration object built via builder pattern to ensure
 * thread-safety and prevent accidental parameter modification during experiments.
 * 
 * DATA FLOW:
 * Config file/args → Builder → Validation → Immutable ExperimentConfig → All components
 */
public class ExperimentConfig {
    
    // Dataset paths and parameters
    /**
     * Path to CHANGE-seq ground truth dataset.
     * Contains 110 sgRNAs with 201,934 validated off-target sites.
     * Used for biological ground truth PAM functionality validation.
     */
    private final String changeSeqDatasetPath;
    
    /**
     * Path to Nanopore FAST5 benchmark dataset.
     * Should contain GM24385 or GIAB HG002-4 data from AWS Open Data Registry.
     */
    private final String nanoporeDatasetPath;
    
    /**
     * Path to synthetic FAST5-like dataset.
     * Contains 5,000 synthetic traces with known ground truth PAM positions.
     */
    private final String syntheticDatasetPath;
    
    // Feature extraction parameters
    /**
     * Wavelet type for stationary wavelet transform.
     * Default: "db4" (Daubechies-4) as specified in protocol Section 2.1.
     * Alternatives: "sym4", "coif3" for validation testing.
     */
    private final String waveletType;
    
    /**
     * Number of scales for wavelet decomposition.
     * Default: 8 scales (j = 1, 2, ..., 8) as per protocol.
     */
    private final int numScales;
    
    /**
     * Window size around PAM center for signal extraction.
     * Default: ±250 samples (±62.5ms at 4 kHz sampling) as per Section 2.1.
     */
    private final int windowSize;
    
    /**
     * Sampling rate of nanopore data in Hz.
     * Default: 4000 Hz (4 kHz) for standard MinION data.
     */
    private final int samplingRate;
    
    // Emergent sorter parameters
    /**
     * Number of iterations for emergent sorting algorithm.
     * Default: 2000 iterations as specified in Section 3.1.
     */
    private final int sorterIterations;
    
    /**
     * Distance metric for emergent sorter.
     * Options: "euclidean" (baseline), "mahalanobis" (robustness test).
     */
    private final String distanceMetric;
    
    /**
     * Tier thresholds for PAM classification.
     * Default: [5%, 25%, 70%] → Tier 1 (top 5%), Tier 2 (next 25%), Tier 3 (bottom 70%).
     */
    private final double[] tierThresholds;
    
    // MLP classifier parameters
    /**
     * Hidden layer sizes for MLP.
     * Default: [16, 8] neurons as specified in Section 4.1.
     */
    private final int[] hiddenLayers;
    
    /**
     * Dropout probability for MLP regularization.
     * Default: 0.3 as per Section 4.1.
     */
    private final double dropoutRate;
    
    /**
     * Learning rate for Adam optimizer.
     * Default: 0.001 as per Section 4.1.
     */
    private final double learningRate;
    
    /**
     * Early stopping patience (number of epochs).
     * Default: 10 epochs as per Section 4.1.
     */
    private final int earlyStoppingPatience;
    
    // Train/validation/test split ratios
    /**
     * Training set proportion.
     * Default: 0.70 (70%) as per Section 4.1.
     */
    private final double trainRatio;
    
    /**
     * Validation set proportion.
     * Default: 0.15 (15%) as per Section 4.1.
     */
    private final double validationRatio;
    
    /**
     * Test set proportion.
     * Default: 0.15 (15%) as per Section 4.1.
     */
    private final double testRatio;
    
    // Validation parameters
    /**
     * Number of bootstrap resamples for confidence intervals.
     * Default: 10,000 as per Section 5.2.
     */
    private final int bootstrapResamples;
    
    /**
     * Number of permutations for significance testing.
     * Default: 5,000 as per Section 5.2.
     */
    private final int permutationTests;
    
    /**
     * Significance threshold (Bonferroni-corrected).
     * Default: 0.0125 (0.05/4 for four comparisons) as per Section 5.2.
     */
    private final double significanceThreshold;
    
    // Output parameters
    /**
     * Directory path for saving results and artifacts.
     */
    private final String outputDirectory;
    
    /**
     * Whether to save intermediate results.
     */
    private final boolean saveIntermediateResults;
    
    /**
     * Random seed for reproducibility.
     */
    private final long randomSeed;
    
    /**
     * Private constructor - use Builder to create instances.
     * 
     * PURPOSE:
     * Ensures all configuration objects are created through the builder pattern,
     * allowing for validation and immutability guarantees.
     */
    private ExperimentConfig(Builder builder) {
        // PHASE THREE IMPLEMENTATION - Constructor
        // Copy all values from builder to create immutable config
        this.changeSeqDatasetPath = builder.changeSeqDatasetPath;
        this.nanoporeDatasetPath = builder.nanoporeDatasetPath;
        this.syntheticDatasetPath = builder.syntheticDatasetPath;
        this.waveletType = builder.waveletType;
        this.numScales = builder.numScales;
        this.windowSize = builder.windowSize;
        this.samplingRate = builder.samplingRate;
        this.sorterIterations = builder.sorterIterations;
        this.distanceMetric = builder.distanceMetric;
        this.tierThresholds = builder.tierThresholds.clone();  // Defensive copy
        this.hiddenLayers = builder.hiddenLayers.clone();      // Defensive copy
        this.dropoutRate = builder.dropoutRate;
        this.learningRate = builder.learningRate;
        this.earlyStoppingPatience = builder.earlyStoppingPatience;
        this.trainRatio = builder.trainRatio;
        this.validationRatio = builder.validationRatio;
        this.testRatio = builder.testRatio;
        this.bootstrapResamples = builder.bootstrapResamples;
        this.permutationTests = builder.permutationTests;
        this.significanceThreshold = builder.significanceThreshold;
        this.outputDirectory = builder.outputDirectory;
        this.saveIntermediateResults = builder.saveIntermediateResults;
        this.randomSeed = builder.randomSeed;
    }
    
    /**
     * Builder class for constructing ExperimentConfig instances.
     * 
     * PURPOSE:
     * Provides fluent API for building configuration with validation.
     * Sets sensible defaults from protocol specifications.
     * 
     * DATA FLOW:
     * new Builder() → set parameters → validate() → build() → ExperimentConfig
     */
    public static class Builder {
        // PHASE THREE IMPLEMENTATION - Builder
        // All fields with defaults from protocol
        private String changeSeqDatasetPath = "";
        private String nanoporeDatasetPath = "";
        private String syntheticDatasetPath = "";
        private String waveletType = "db4";
        private int numScales = 8;
        private int windowSize = 250;
        private int samplingRate = 4000;
        private int sorterIterations = 2000;
        private String distanceMetric = "euclidean";
        private double[] tierThresholds = {0.05, 0.25, 0.70};
        private int[] hiddenLayers = {16, 8};
        private double dropoutRate = 0.3;
        private double learningRate = 0.001;
        private int earlyStoppingPatience = 10;
        private double trainRatio = 0.70;
        private double validationRatio = 0.15;
        private double testRatio = 0.15;
        private int bootstrapResamples = 10000;
        private int permutationTests = 5000;
        private double significanceThreshold = 0.0125;
        private String outputDirectory = "results/";
        private boolean saveIntermediateResults = true;
        private long randomSeed = 42L;
        
        public Builder setChangeSeqDatasetPath(String path) { this.changeSeqDatasetPath = path; return this; }
        public Builder setNanoporeDatasetPath(String path) { this.nanoporeDatasetPath = path; return this; }
        public Builder setSyntheticDatasetPath(String path) { this.syntheticDatasetPath = path; return this; }
        public Builder setWaveletType(String type) { this.waveletType = type; return this; }
        public Builder setNumScales(int scales) { this.numScales = scales; return this; }
        public Builder setWindowSize(int size) { this.windowSize = size; return this; }
        public Builder setSamplingRate(int rate) { this.samplingRate = rate; return this; }
        public Builder setSorterIterations(int iterations) { this.sorterIterations = iterations; return this; }
        public Builder setDistanceMetric(String metric) { this.distanceMetric = metric; return this; }
        public Builder setTierThresholds(double[] thresholds) { this.tierThresholds = thresholds; return this; }
        public Builder setHiddenLayers(int[] layers) { this.hiddenLayers = layers; return this; }
        public Builder setDropoutRate(double rate) { this.dropoutRate = rate; return this; }
        public Builder setLearningRate(double rate) { this.learningRate = rate; return this; }
        public Builder setEarlyStoppingPatience(int patience) { this.earlyStoppingPatience = patience; return this; }
        public Builder setTrainRatio(double ratio) { this.trainRatio = ratio; return this; }
        public Builder setValidationRatio(double ratio) { this.validationRatio = ratio; return this; }
        public Builder setTestRatio(double ratio) { this.testRatio = ratio; return this; }
        public Builder setBootstrapResamples(int resamples) { this.bootstrapResamples = resamples; return this; }
        public Builder setPermutationTests(int tests) { this.permutationTests = tests; return this; }
        public Builder setSignificanceThreshold(double threshold) { this.significanceThreshold = threshold; return this; }
        public Builder setOutputDirectory(String dir) { this.outputDirectory = dir; return this; }
        public Builder setSaveIntermediateResults(boolean save) { this.saveIntermediateResults = save; return this; }
        public Builder setRandomSeed(long seed) { this.randomSeed = seed; return this; }
        
        /**
         * Build the final ExperimentConfig instance.
         * 
         * PURPOSE:
         * Validates all parameters and creates immutable configuration object.
         * 
         * INPUTS:
         * Builder state with all parameter values
         * 
         * OUTPUTS:
         * @return Validated, immutable ExperimentConfig
         * 
         * @throws IllegalStateException if required parameters are missing or invalid
         */
        public ExperimentConfig build() {
            // PHASE THREE IMPLEMENTATION - build()
            // Validate that ratios sum to 1.0
            if (Math.abs(trainRatio + validationRatio + testRatio - 1.0) > 0.001) {
                throw new IllegalStateException("Train/validation/test ratios must sum to 1.0");
            }
            
            // Validate tier thresholds sum to 1.0
            double tierSum = 0.0;
            for (double threshold : tierThresholds) {
                tierSum += threshold;
            }
            if (Math.abs(tierSum - 1.0) > 0.001) {
                throw new IllegalStateException("Tier thresholds must sum to 1.0");
            }
            
            // Create and return immutable config
            ExperimentConfig config = new ExperimentConfig(this);
            return config;
        }
    }
    
    // Getter methods for all fields
    public String getWaveletType() { return waveletType; }
    public int getNumScales() { return numScales; }
    public int getSorterIterations() { return sorterIterations; }
    public String getChangeSeqDatasetPath() { return changeSeqDatasetPath; }
    public String getNanoporeDatasetPath() { return nanoporeDatasetPath; }
    public String getSyntheticDatasetPath() { return syntheticDatasetPath; }
    public int getWindowSize() { return windowSize; }
    public int getSamplingRate() { return samplingRate; }
    public String getDistanceMetric() { return distanceMetric; }
    public double[] getTierThresholds() { return tierThresholds; }
    public int[] getHiddenLayers() { return hiddenLayers; }
    public double getDropoutRate() { return dropoutRate; }
    public double getLearningRate() { return learningRate; }
    public int getEarlyStoppingPatience() { return earlyStoppingPatience; }
    public double getTrainRatio() { return trainRatio; }
    public double getValidationRatio() { return validationRatio; }
    public double getTestRatio() { return testRatio; }
    public int getBootstrapResamples() { return bootstrapResamples; }
    public int getPermutationTests() { return permutationTests; }
    public double getSignificanceThreshold() { return significanceThreshold; }
    public String getOutputDirectory() { return outputDirectory; }
    public boolean getSaveIntermediateResults() { return saveIntermediateResults; }
    public long getRandomSeed() { return randomSeed; }
}
