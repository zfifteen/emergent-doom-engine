package lab.experiment095;

import java.util.Map;
import java.util.List;

/**
 * Container for all experimental results and metrics.
 * 
 * PURPOSE:
 * Aggregates results from all phases of the experiment including performance metrics,
 * statistical validation, tier assignments, and biological validation outcomes.
 * 
 * REQUIREMENTS SATISFIED:
 * - Performance metrics collection (Section 5.1)
 * - Statistical validation results (Section 5.2)
 * - Generalization test results (Section 5.3)
 * - Biological validation outcomes (Section 6)
 * - Integration results with φ-geometry (Section 7)
 * - Scalability benchmarks (Section 8)
 * - Failure mode analysis (Section 9)
 * 
 * ARCHITECTURE:
 * Immutable results object that accumulates data throughout the experimental pipeline.
 * Uses builder pattern to allow incremental result addition during execution.
 * 
 * DATA FLOW:
 * Experiment phases → Builder.add*() → build() → ExperimentResults → Report generation
 */
public class ExperimentResults {
    
    // Primary performance metrics (Section 5.1)
    /**
     * Overall accuracy on test set.
     * Target: 92% as per protocol claim validation.
     */
    private final double accuracy;
    
    /**
     * Precision metric on test set.
     */
    private final double precision;
    
    /**
     * Recall metric on test set.
     */
    private final double recall;
    
    /**
     * F1 score on test set.
     */
    private final double f1Score;
    
    /**
     * Area under ROC curve (AUROC).
     */
    private final double auroc;
    
    /**
     * Area under Precision-Recall curve (AUPRC).
     */
    private final double auprc;
    
    /**
     * Spearman correlation with CHANGE-seq cleavage activity.
     * Target: ρ > 0.6 as per Section 12.
     */
    private final double spearmanCorrelation;
    
    // Statistical validation results (Section 5.2)
    /**
     * Bootstrap confidence interval for accuracy [lower, upper].
     * Based on 10,000 resamples.
     */
    private final double[] accuracyConfidenceInterval;
    
    /**
     * P-value from permutation test comparing to ridgelet+SVM baseline.
     * Null hypothesis: emergent editing ≤ ridgelet+SVM.
     */
    private final double permutationTestPValue;
    
    /**
     * DeLong test results for AUROC comparisons against baselines.
     * Maps baseline name → p-value.
     */
    private final Map<String, Double> delongTestResults;
    
    // Baseline comparison results (Section 4.2)
    /**
     * Performance metrics for baseline methods.
     * Maps method name → metrics map (accuracy, precision, etc.).
     */
    private final Map<String, Map<String, Double>> baselinePerformance;
    
    // Tier assignment results (Section 3)
    /**
     * Number of PAM candidates assigned to each tier.
     * Index 0 = Tier 1, Index 1 = Tier 2, Index 2 = Tier 3.
     */
    private final int[] tierCounts;
    
    /**
     * Accuracy of each tier on validation set.
     * Validates that Tier 1 > Tier 2 > Tier 3 in quality.
     */
    private final double[] tierAccuracies;
    
    /**
     * Cohen's κ for tier assignment stability across random seeds.
     * Target: κ > 0.8 (substantial agreement) as per Section 3.2a.
     */
    private final double tierStabilityKappa;
    
    // Biological validation results (Section 6)
    /**
     * Indel frequencies from targeted amplicon sequencing.
     * Maps tier number → list of indel frequencies.
     */
    private final Map<Integer, List<Double>> indelFrequencies;
    
    /**
     * P-value from Kruskal-Wallis test across tiers.
     * Tests whether tier indel rates are significantly different.
     */
    private final double kruskalWallisPValue;
    
    /**
     * TXTL-based cleavage efficiency ratios.
     * Tier 1 vs Tier 3 fold-change.
     * Target: ≥2-fold as per Section 6.2.
     */
    private final double txtlCleavageFoldChange;
    
    // φ-Geometry integration results (Section 7)
    /**
     * AUPRC of hybrid model (wavelet + φ-geometry).
     */
    private final double hybridModelAUPRC;
    
    /**
     * AUPRC of wavelet-only model.
     */
    private final double waveletOnlyAUPRC;
    
    /**
     * AUPRC of φ-only model.
     */
    private final double phiOnlyAUPRC;
    
    /**
     * Whether hybrid model outperforms individual models by >3%.
     * Acceptance criterion from Section 7.2.
     */
    private final boolean hybridSynergyAchieved;
    
    // Generalization test results (Section 5.3)
    /**
     * Accuracy drop when testing on different chemistry.
     * Target: <15% drop as per Section 5.3.
     */
    private final double crossChemistryAccuracyDrop;
    
    /**
     * Accuracy drop when testing on different species.
     */
    private final double crossSpeciesAccuracyDrop;
    
    /**
     * Accuracy at SNR = 1 on synthetic data.
     */
    private final double lowSNRAccuracy;
    
    // Scalability benchmarks (Section 8)
    /**
     * Median latency per PAM site on consumer laptop (ms).
     * Target: <5 ms as per Section 8.1.
     */
    private final double laptopLatency;
    
    /**
     * Median latency per PAM site on Jetson GPU (ms).
     * Target: <2 ms as per Section 8.1.
     */
    private final double jetsonLatency;
    
    /**
     * Memory footprint in MB.
     */
    private final double memoryFootprintMB;
    
    /**
     * Read-until enrichment fold-change.
     * Target: ≥5× as per Section 8.2.
     */
    private final double readUntilEnrichment;
    
    // Failure mode analysis (Section 9)
    /**
     * Accuracy drop in challenging contexts.
     * Maps context type → accuracy drop percentage.
     */
    private final Map<String, Double> challengingContextAccuracyDrop;
    
    /**
     * Adversarial robustness: accuracy under moderate noise.
     * Target: >75% as per Section 9.2.
     */
    private final double adversarialRobustnessAccuracy;
    
    // Success criteria evaluation
    /**
     * Whether all success criteria from Section 12 are met.
     */
    private final boolean allCriteriaMet;
    
    /**
     * List of failed criteria with recommended actions.
     */
    private final List<String> failedCriteria;
    
    /**
     * Private constructor - use Builder to create instances.
     */
    private ExperimentResults(Builder builder) {
        // PHASE THREE IMPLEMENTATION - Constructor
        // Copy all values from builder
        this.accuracy = builder.accuracy;
        this.precision = builder.precision;
        this.recall = builder.recall;
        this.f1Score = builder.f1Score;
        this.auroc = builder.auroc;
        this.auprc = builder.auprc;
        this.spearmanCorrelation = builder.spearmanCorrelation;
        this.accuracyConfidenceInterval = builder.accuracyConfidenceInterval;
        this.permutationTestPValue = builder.permutationTestPValue;
        this.delongTestResults = new java.util.HashMap<>(builder.delongTestResults);
        this.baselinePerformance = new java.util.HashMap<>(builder.baselinePerformance);
        this.tierCounts = builder.tierCounts.clone();
        this.tierAccuracies = builder.tierAccuracies.clone();
        this.tierStabilityKappa = builder.tierStabilityKappa;
        this.indelFrequencies = new java.util.HashMap<>(builder.indelFrequencies);
        this.kruskalWallisPValue = builder.kruskalWallisPValue;
        this.txtlCleavageFoldChange = builder.txtlCleavageFoldChange;
        this.hybridModelAUPRC = builder.hybridModelAUPRC;
        this.waveletOnlyAUPRC = builder.waveletOnlyAUPRC;
        this.phiOnlyAUPRC = builder.phiOnlyAUPRC;
        this.hybridSynergyAchieved = builder.hybridSynergyAchieved;
        this.crossChemistryAccuracyDrop = builder.crossChemistryAccuracyDrop;
        this.crossSpeciesAccuracyDrop = builder.crossSpeciesAccuracyDrop;
        this.lowSNRAccuracy = builder.lowSNRAccuracy;
        this.laptopLatency = builder.laptopLatency;
        this.jetsonLatency = builder.jetsonLatency;
        this.memoryFootprintMB = builder.memoryFootprintMB;
        this.readUntilEnrichment = builder.readUntilEnrichment;
        this.challengingContextAccuracyDrop = new java.util.HashMap<>(builder.challengingContextAccuracyDrop);
        this.adversarialRobustnessAccuracy = builder.adversarialRobustnessAccuracy;
        this.allCriteriaMet = builder.allCriteriaMet;
        this.failedCriteria = new java.util.ArrayList<>(builder.failedCriteria);
    }
    
    /**
     * Builder class for constructing ExperimentResults instances.
     * 
     * PURPOSE:
     * Allows incremental accumulation of results during experiment execution.
     * Provides fluent API for setting different result categories.
     * 
     * DATA FLOW:
     * new Builder() → add metrics from each phase → build() → ExperimentResults
     */
    public static class Builder {
        // PHASE THREE IMPLEMENTATION - Builder
        // All fields initialized to sensible defaults
        private double accuracy = 0.0;
        private double precision = 0.0;
        private double recall = 0.0;
        private double f1Score = 0.0;
        private double auroc = 0.0;
        private double auprc = 0.0;
        private double spearmanCorrelation = 0.0;
        private double[] accuracyConfidenceInterval = new double[2];
        private double permutationTestPValue = 1.0;
        private Map<String, Double> delongTestResults = new java.util.HashMap<>();
        private Map<String, Map<String, Double>> baselinePerformance = new java.util.HashMap<>();
        private int[] tierCounts = new int[3];
        private double[] tierAccuracies = new double[3];
        private double tierStabilityKappa = 0.0;
        private Map<Integer, List<Double>> indelFrequencies = new java.util.HashMap<>();
        private double kruskalWallisPValue = 1.0;
        private double txtlCleavageFoldChange = 0.0;
        private double hybridModelAUPRC = 0.0;
        private double waveletOnlyAUPRC = 0.0;
        private double phiOnlyAUPRC = 0.0;
        private boolean hybridSynergyAchieved = false;
        private double crossChemistryAccuracyDrop = 0.0;
        private double crossSpeciesAccuracyDrop = 0.0;
        private double lowSNRAccuracy = 0.0;
        private double laptopLatency = 0.0;
        private double jetsonLatency = 0.0;
        private double memoryFootprintMB = 0.0;
        private double readUntilEnrichment = 0.0;
        private Map<String, Double> challengingContextAccuracyDrop = new java.util.HashMap<>();
        private double adversarialRobustnessAccuracy = 0.0;
        private boolean allCriteriaMet = false;
        private List<String> failedCriteria = new java.util.ArrayList<>();
        
        public Builder setAccuracy(double val) { this.accuracy = val; return this; }
        public Builder setPrecision(double val) { this.precision = val; return this; }
        public Builder setRecall(double val) { this.recall = val; return this; }
        public Builder setF1Score(double val) { this.f1Score = val; return this; }
        public Builder setAuroc(double val) { this.auroc = val; return this; }
        public Builder setAuprc(double val) { this.auprc = val; return this; }
        public Builder setSpearmanCorrelation(double val) { this.spearmanCorrelation = val; return this; }
        public Builder setAccuracyConfidenceInterval(double[] val) { this.accuracyConfidenceInterval = val; return this; }
        public Builder setPermutationTestPValue(double val) { this.permutationTestPValue = val; return this; }
        public Builder setDelongTestResults(Map<String, Double> val) { this.delongTestResults = val; return this; }
        public Builder setBaselinePerformance(Map<String, Map<String, Double>> val) { this.baselinePerformance = val; return this; }
        public Builder setTierCounts(int[] val) { this.tierCounts = val; return this; }
        public Builder setTierAccuracies(double[] val) { this.tierAccuracies = val; return this; }
        public Builder setTierStabilityKappa(double val) { this.tierStabilityKappa = val; return this; }
        public Builder setIndelFrequencies(Map<Integer, List<Double>> val) { this.indelFrequencies = val; return this; }
        public Builder setKruskalWallisPValue(double val) { this.kruskalWallisPValue = val; return this; }
        public Builder setTxtlCleavageFoldChange(double val) { this.txtlCleavageFoldChange = val; return this; }
        public Builder setHybridModelAUPRC(double val) { this.hybridModelAUPRC = val; return this; }
        public Builder setWaveletOnlyAUPRC(double val) { this.waveletOnlyAUPRC = val; return this; }
        public Builder setPhiOnlyAUPRC(double val) { this.phiOnlyAUPRC = val; return this; }
        public Builder setHybridSynergyAchieved(boolean val) { this.hybridSynergyAchieved = val; return this; }
        public Builder setCrossChemistryAccuracyDrop(double val) { this.crossChemistryAccuracyDrop = val; return this; }
        public Builder setCrossSpeciesAccuracyDrop(double val) { this.crossSpeciesAccuracyDrop = val; return this; }
        public Builder setLowSNRAccuracy(double val) { this.lowSNRAccuracy = val; return this; }
        public Builder setLaptopLatency(double val) { this.laptopLatency = val; return this; }
        public Builder setJetsonLatency(double val) { this.jetsonLatency = val; return this; }
        public Builder setMemoryFootprintMB(double val) { this.memoryFootprintMB = val; return this; }
        public Builder setReadUntilEnrichment(double val) { this.readUntilEnrichment = val; return this; }
        public Builder setChallengingContextAccuracyDrop(Map<String, Double> val) { this.challengingContextAccuracyDrop = val; return this; }
        public Builder setAdversarialRobustnessAccuracy(double val) { this.adversarialRobustnessAccuracy = val; return this; }
        public Builder setAllCriteriaMet(boolean val) { this.allCriteriaMet = val; return this; }
        public Builder setFailedCriteria(List<String> val) { this.failedCriteria = val; return this; }
        
        /**
         * Build the final ExperimentResults instance.
         * 
         * PURPOSE:
         * Creates immutable results object after all metrics have been collected.
         * 
         * INPUTS:
         * Builder state with all collected metrics
         * 
         * OUTPUTS:
         * @return Immutable ExperimentResults
         */
        public ExperimentResults build() {
            // PHASE THREE IMPLEMENTATION - build()
            return new ExperimentResults(this);
        }
    }
    
    // Getter methods for all fields
    public double getAccuracy() { return accuracy; }
    public double getPrecision() { return precision; }
    public double getRecall() { return recall; }
    public double getF1Score() { return f1Score; }
    public double getAuroc() { return auroc; }
    public double getAuprc() { return auprc; }
    public double getSpearmanCorrelation() { return spearmanCorrelation; }
    public double[] getAccuracyConfidenceInterval() { return accuracyConfidenceInterval; }
    public double getPermutationTestPValue() { return permutationTestPValue; }
    public Map<String, Double> getDelongTestResults() { return delongTestResults; }
    public Map<String, Map<String, Double>> getBaselinePerformance() { return baselinePerformance; }
    public int[] getTierCounts() { return tierCounts; }
    public double[] getTierAccuracies() { return tierAccuracies; }
    public double getTierStabilityKappa() { return tierStabilityKappa; }
    public Map<Integer, List<Double>> getIndelFrequencies() { return indelFrequencies; }
    public double getKruskalWallisPValue() { return kruskalWallisPValue; }
    public double getTxtlCleavageFoldChange() { return txtlCleavageFoldChange; }
    public double getHybridModelAUPRC() { return hybridModelAUPRC; }
    public double getWaveletOnlyAUPRC() { return waveletOnlyAUPRC; }
    public double getPhiOnlyAUPRC() { return phiOnlyAUPRC; }
    public boolean isHybridSynergyAchieved() { return hybridSynergyAchieved; }
    public double getCrossChemistryAccuracyDrop() { return crossChemistryAccuracyDrop; }
    public double getCrossSpeciesAccuracyDrop() { return crossSpeciesAccuracyDrop; }
    public double getLowSNRAccuracy() { return lowSNRAccuracy; }
    public double getLaptopLatency() { return laptopLatency; }
    public double getJetsonLatency() { return jetsonLatency; }
    public double getMemoryFootprintMB() { return memoryFootprintMB; }
    public double getReadUntilEnrichment() { return readUntilEnrichment; }
    public Map<String, Double> getChallengingContextAccuracyDrop() { return challengingContextAccuracyDrop; }
    public double getAdversarialRobustnessAccuracy() { return adversarialRobustnessAccuracy; }
    public boolean isAllCriteriaMet() { return allCriteriaMet; }
    public List<String> getFailedCriteria() { return failedCriteria; }
}
