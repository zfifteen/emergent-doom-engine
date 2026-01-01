package lab.experiment095.features;

/**
 * Container for wavelet leaders at each scale.
 * 
 * PURPOSE:
 * Stores computed wavelet leaders (local suprema of coefficients).
 * 
 * DATA FLOW:
 * WaveletCoefficients → WaveletLeaders → Statistical feature computation
 */
class WaveletLeaders {
    
    /** Leaders at each scale [scale][position] */
    private final double[][] leaders;
    
    /** Number of scales */
    private final int numScales;
    
    /**
     * Constructor for WaveletLeaders.
     * 
     * INPUTS:
     * @param leaders 2D array [scale][position]
     */
    WaveletLeaders(double[][] leaders) {
        // Implementation pending - Phase Three
        this.leaders = null;
        this.numScales = 0;
    }
    
    // Getter methods - Implementation pending - Phase Three
}
