package lab.experiment095.classification;

/**
 * Container for MLP model weights and architecture.
 * 
 * PURPOSE:
 * Stores trained neural network parameters for inference.
 * 
 * DATA FLOW:
 * Training → MLPModel (weights saved) → Inference
 */
class MLPModel {
    
    /** Weights for each layer */
    private final double[][][] weights;
    
    /** Biases for each layer */
    private final double[][] biases;
    
    /** Architecture specification */
    private final int[] layerSizes;
    
    /**
     * Constructor for MLPModel.
     * 
     * INPUTS:
     * @param weights Layer weights
     * @param biases Layer biases
     * @param layerSizes Layer dimensions
     */
    MLPModel(double[][][] weights, double[][] biases, int[] layerSizes) {
        // Implementation pending - Phase Three
        this.weights = null;
        this.biases = null;
        this.layerSizes = null;
    }
    
    // Forward pass and getter methods - Implementation pending - Phase Three
}
