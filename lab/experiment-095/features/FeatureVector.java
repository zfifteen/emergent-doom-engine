package lab.experiment095.features;

/**
 * Represents a 28-dimensional feature vector from wavelet leader extraction.
 * 
 * PURPOSE:
 * Immutable container for the feature vector used in emergent sorting and classification.
 * 
 * REQUIREMENTS SATISFIED:
 * - Section 2.1: 28D feature vector representation
 * 
 * DATA FLOW:
 * WaveletLeaderExtractor → FeatureVector → EmergentSorter/MLPClassifier
 */
public class FeatureVector {
    
    /** 28 feature values */
    private final double[] features;
    
    /** Reference to source signal window */
    private final lab.experiment095.data.SignalWindow sourceWindow;
    
    /**
     * Constructor for FeatureVector.
     * 
     * PURPOSE:
     * Creates immutable feature vector with source window reference.
     * 
     * INPUTS:
     * @param features 28-element feature array
     * @param sourceWindow Source signal window
     * 
     * @throws IllegalArgumentException if features.length != 28
     */
    public FeatureVector(double[] features, lab.experiment095.data.SignalWindow sourceWindow) {
        // Implementation pending - Phase Three
        this.features = null;
        this.sourceWindow = null;
    }
    
    /**
     * Get dimensionality of feature vector.
     * 
     * @return Always 28
     */
    public int getDimension() {
        // Implementation pending - Phase Three
        return 28;
    }
    
    // Getter methods - Implementation pending - Phase Three
}
