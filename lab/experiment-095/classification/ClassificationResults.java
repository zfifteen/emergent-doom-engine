package lab.experiment095.classification;

import java.util.List;

/**
 * Container for classification predictions.
 * 
 * PURPOSE:
 * Stores predicted labels and probabilities from classifier.
 * 
 * DATA FLOW:
 * MLPClassifier.predict() → ClassificationResults → Evaluation
 */
public class ClassificationResults {
    
    /** Binary predictions (PAM / non-PAM) */
    private final List<Boolean> predictions;
    
    /** Prediction probabilities (0.0 to 1.0) */
    private final List<Double> probabilities;
    
    /**
     * Constructor for ClassificationResults.
     * 
     * INPUTS:
     * @param predictions Binary predictions
     * @param probabilities Prediction probabilities
     */
    public ClassificationResults(List<Boolean> predictions,
                                  List<Double> probabilities) {
        // Implementation pending - Phase Three
        this.predictions = null;
        this.probabilities = null;
    }
    
    // Getter methods - Implementation pending - Phase Three
}
