package lab.experiment095.classification;

/**
 * Container for performance metrics from classification evaluation.
 * 
 * PURPOSE:
 * Aggregates all performance metrics for model evaluation and comparison.
 * 
 * REQUIREMENTS SATISFIED:
 * - Section 5.1: Primary metrics (accuracy, precision, recall, F1, AUROC, AUPRC)
 * 
 * DATA FLOW:
 * Predictions + True labels → Metric computation → PerformanceMetrics
 */
public class PerformanceMetrics {
    
    /** Overall accuracy */
    private final double accuracy;
    
    /** Precision metric */
    private final double precision;
    
    /** Recall metric */
    private final double recall;
    
    /** F1 score */
    private final double f1Score;
    
    /** Area under ROC curve */
    private final double auroc;
    
    /** Area under Precision-Recall curve */
    private final double auprc;
    
    /**
     * Constructor for PerformanceMetrics.
     * 
     * PURPOSE:
     * Creates immutable metrics container.
     * 
     * INPUTS:
     * @param accuracy Overall accuracy
     * @param precision Precision
     * @param recall Recall
     * @param f1Score F1 score
     * @param auroc AUROC
     * @param auprc AUPRC
     */
    public PerformanceMetrics(double accuracy, double precision, double recall,
                              double f1Score, double auroc, double auprc) {
        // Implementation pending - Phase Three
        this.accuracy = 0.0;
        this.precision = 0.0;
        this.recall = 0.0;
        this.f1Score = 0.0;
        this.auroc = 0.0;
        this.auprc = 0.0;
    }
    
    // Getter methods - Implementation pending - Phase Three
}
