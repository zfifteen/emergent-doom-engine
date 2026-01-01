package lab.experiment095.features;

/**
 * Container for wavelet coefficients from stationary wavelet transform.
 * 
 * PURPOSE:
 * Stores multi-scale wavelet detail coefficients and final approximation.
 * Provides access to coefficients for leader computation and feature extraction.
 * 
 * DATA FLOW:
 * SWT → WaveletCoefficients → Wavelet leader computation → Feature extraction
 */
public class WaveletCoefficients {
    
    /** Detail coefficients at each scale [scale][position] */
    private final double[][] detailCoefficients;
    
    /** Final approximation coefficients after all scales */
    private final double[] approximation;
    
    /** Number of scales */
    private final int numScales;
    
    /**
     * Constructor for WaveletCoefficients.
     * 
     * PURPOSE:
     * Stores detail coefficients from SWT decomposition.
     * 
     * INPUTS:
     * @param detailCoefficients 2D array [scale][position] of detail coefficients
     * @param approximation Final approximation after decomposition
     */
    public WaveletCoefficients(double[][] detailCoefficients, double[] approximation) {
        this.detailCoefficients = detailCoefficients;
        this.approximation = approximation;
        this.numScales = detailCoefficients.length;
    }
    
    /**
     * Get detail coefficients at all scales.
     * 
     * PURPOSE:
     * Provides access to detail coefficients for leader computation.
     * 
     * OUTPUTS:
     * @return 2D array [scale][position] of detail coefficients
     */
    public double[][] getDetailCoefficients() {
        return detailCoefficients;
    }
    
    /**
     * Get final approximation coefficients.
     * 
     * PURPOSE:
     * Provides access to low-frequency approximation after all scales.
     * 
     * OUTPUTS:
     * @return Array of approximation coefficients
     */
    public double[] getApproximation() {
        return approximation;
    }
    
    /**
     * Get number of scales.
     * 
     * OUTPUTS:
     * @return Number of decomposition scales
     */
    public int getNumScales() {
        return numScales;
    }
    
    /**
     * Get coefficients at specific scale.
     * 
     * PURPOSE:
     * Access detail coefficients for a single scale.
     * 
     * INPUTS:
     * @param scale Scale index (0-based)
     * 
     * OUTPUTS:
     * @return Array of detail coefficients at specified scale
     */
    public double[] getCoefficientsAtScale(int scale) {
        if (scale < 0 || scale >= numScales) {
            throw new IllegalArgumentException("Scale must be between 0 and " + (numScales - 1));
        }
        return detailCoefficients[scale];
    }
}
