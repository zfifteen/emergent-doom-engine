package lab.experiment095.validation;

/**
 * Container for biological validation results.
 * 
 * PURPOSE:
 * Stores results from orthogonal biological assays.
 * 
 * REQUIREMENTS SATISFIED:
 * - Section 6: Biological Validation results storage
 * 
 * DATA FLOW:
 * Biological assay analysis → BiologicalValidationResults → ExperimentResults
 */
public class BiologicalValidationResults {
    
    /** P-value from Kruskal-Wallis test across tiers */
    private final double kruskalWallisPValue;
    
    /** Post-hoc Dunn test results (tier pair → p-value) */
    private final java.util.Map<String, Double> dunnTestResults;
    
    /** TXTL fold-change (Tier 1 / Tier 3) */
    private final double txtlFoldChange;
    
    /** TXTL Mann-Whitney U test p-value */
    private final double txtlPValue;
    
    /**
     * Constructor for BiologicalValidationResults.
     * 
     * INPUTS:
     * @param kruskalWallisPValue Kruskal-Wallis p-value
     * @param dunnTestResults Dunn test results
     * @param txtlFoldChange TXTL fold-change
     * @param txtlPValue TXTL p-value
     */
    public BiologicalValidationResults(double kruskalWallisPValue,
                                        java.util.Map<String, Double> dunnTestResults,
                                        double txtlFoldChange,
                                        double txtlPValue) {
        // Implementation pending - Phase Three
        this.kruskalWallisPValue = 0.0;
        this.dunnTestResults = null;
        this.txtlFoldChange = 0.0;
        this.txtlPValue = 0.0;
    }
    
    // Getter methods - Implementation pending - Phase Three
}
