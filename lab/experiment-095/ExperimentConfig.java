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
        // Implementation pending - Phase Three
        this.changeSeqDatasetPath = null;
        this.nanoporeDatasetPath = null;
        this.syntheticDatasetPath = null;
        this.waveletType = null;
        this.numScales = 0;
        this.windowSize = 0;
        this.samplingRate = 0;
        this.sorterIterations = 0;
        this.distanceMetric = null;
        this.tierThresholds = null;
        this.hiddenLayers = null;
        this.dropoutRate = 0.0;
        this.learningRate = 0.0;
        this.earlyStoppingPatience = 0;
        this.trainRatio = 0.0;
        this.validationRatio = 0.0;
        this.testRatio = 0.0;
        this.bootstrapResamples = 0;
        this.permutationTests = 0;
        this.significanceThreshold = 0.0;
        this.outputDirectory = null;
        this.saveIntermediateResults = false;
        this.randomSeed = 0L;
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
        // Implementation pending - Phase Three
        
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
            // Implementation pending - Phase Three
            return null;
        }
    }
    
    // Getter methods for all fields
    // Implementation pending - Phase Three
}
