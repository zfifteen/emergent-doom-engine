package lab.experiment095.data;

import java.util.List;

/**
 * Container for stratified train/validation/test dataset splits.
 * 
 * PURPOSE:
 * Holds the three dataset partitions with stratified sampling to ensure
 * balanced representation of PAM classes and contexts.
 * 
 * REQUIREMENTS SATISFIED:
 * - Section 4.1: 70% train, 15% validation, 15% test split
 * - Stratified by tier and functional class
 * 
 * DATA FLOW:
 * All windows → Stratification → DatasetSplit → Train/Val/Test access
 */
public class DatasetSplit {
    
    /** Training set (70% of data) */
    private final List<SignalWindow> trainSet;
    
    /** Validation set (15% of data) */
    private final List<SignalWindow> validationSet;
    
    /** Test set (15% of data) */
    private final List<SignalWindow> testSet;
    
    /**
     * Constructor for DatasetSplit.
     * 
     * PURPOSE:
     * Creates immutable split container ensuring no data leakage between sets.
     * 
     * INPUTS:
     * @param trainSet Training windows
     * @param validationSet Validation windows
     * @param testSet Test windows
     */
    public DatasetSplit(List<SignalWindow> trainSet,
                        List<SignalWindow> validationSet,
                        List<SignalWindow> testSet) {
        // Implementation pending - Phase Three
        this.trainSet = null;
        this.validationSet = null;
        this.testSet = null;
    }
    
    // Getter methods - Implementation pending - Phase Three
}
