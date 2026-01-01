package lab.experiment095.features;

/**
 * Container for wavelet leaders at each scale.
 * 
 * PURPOSE:
 * Stores computed wavelet leaders (local suprema of coefficients).
 * Provides access for statistical feature computation.
 * 
 * DATA FLOW:
 * WaveletCoefficients → WaveletLeaders → Statistical feature computation
 */
public class WaveletLeaders {
    
    /** Leaders at each scale [scale][position] */
    private final double[][] leaders;
    
    /** Number of scales */
    private final int numScales;
    
    /**
     * Constructor for WaveletLeaders.
     * 
     * PURPOSE:
     * Stores wavelet leader values computed from detail coefficients.
     * 
     * INPUTS:
     * @param leaders 2D array [scale][position] of leader values
     */
    public WaveletLeaders(double[][] leaders) {
        this.leaders = leaders;
        this.numScales = leaders.length;
    }
    
    /**
     * Get leader values at all scales.
     * 
     * PURPOSE:
     * Provides access to leader values for feature extraction.
     * 
     * OUTPUTS:
     * @return 2D array [scale][position] of leader values
     */
    public double[][] getLeaders() {
        return leaders;
    }
    
    /**
     * Get leader values at specific scale.
     * 
     * PURPOSE:
     * Access leader values for a single scale.
     * 
     * INPUTS:
     * @param scale Scale index (0-based)
     * 
     * OUTPUTS:
     * @return Array of leader values at specified scale
     */
    public double[] getLeadersAtScale(int scale) {
        if (scale < 0 || scale >= numScales) {
            throw new IllegalArgumentException("Scale must be between 0 and " + (numScales - 1));
        }
        return leaders[scale];
    }
    
    /**
     * Get number of scales.
     * 
     * OUTPUTS:
     * @return Number of scales
     */
    public int getNumScales() {
        return numScales;
    }
}
