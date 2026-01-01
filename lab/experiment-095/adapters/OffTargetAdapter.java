package lab.experiment095.adapters;

import lab.experiment095.data.PAMSite;
import java.util.List;

/**
 * Adapter for integrating external CRISPR off-target detection data sources.
 * 
 * PURPOSE:
 * Provides unified interface for loading off-target labels from various sources
 * (CHANGE-seq, GUIDE-seq, Nano-OTS, custom assays). Enables framework to work
 * with any CRISPR dataset with on/off-target labels.
 * 
 * REQUIREMENTS SATISFIED:
 * - Section 3 (Validation/CRISPR context): Adapters for external label sources
 * - Support for CHANGE-seq, GUIDE-seq, Nano-OTS, custom assays
 * - Plug-in interface for dataset integration
 * 
 * ARCHITECTURE:
 * Abstract adapter pattern with concrete implementations for each data source.
 * Normalizes different file formats to common PAMSite representation.
 * Supports lazy loading and caching for large datasets.
 * 
 * DATA FLOW:
 * External file → Parse format → Extract genomic coordinates + activity labels →
 * Normalize to PAMSite objects → Return labeled dataset
 */
public abstract class OffTargetAdapter {
    
    /**
     * Dataset source name (e.g., "CHANGE-seq", "GUIDE-seq").
     */
    protected final String sourceName;
    
    /**
     * Path to dataset file or directory.
     */
    protected final String dataPath;
    
    /**
     * Constructor for OffTargetAdapter.
     * 
     * PURPOSE:
     * Initializes adapter with data source information.
     * 
     * INPUTS:
     * @param sourceName Name of data source
     * @param dataPath Path to data file/directory
     */
    public OffTargetAdapter(String sourceName, String dataPath) {
        // SCAFFOLD ONLY - Implementation pending
        this.sourceName = sourceName;
        this.dataPath = dataPath;
    }
    
    /**
     * Load off-target sites with activity labels.
     * 
     * PURPOSE:
     * Parses external dataset and returns PAM sites with functional labels.
     * Implementations handle format-specific parsing.
     * 
     * OUTPUTS:
     * @return List of PAMSite objects with activity labels
     * 
     * DATA FLOW:
     * dataPath → Read file → Parse format → Extract sites → Label → Return list
     * 
     * @throws java.io.IOException if file cannot be read
     */
    public abstract List<PAMSite> loadOffTargetSites() throws java.io.IOException;
    
    /**
     * Get metadata about dataset.
     * 
     * PURPOSE:
     * Returns information about dataset without loading full data.
     * 
     * OUTPUTS:
     * @return Metadata including site count, sgRNA count, validation method
     */
    public abstract DatasetMetadata getMetadata();
    
    /**
     * Metadata container for off-target datasets.
     */
    public static class DatasetMetadata {
        /** Number of PAM sites in dataset */
        public final int siteCount;
        
        /** Number of unique sgRNAs tested */
        public final int sgRNACount;
        
        /** Validation method used (e.g., "CHANGE-seq", "GUIDE-seq") */
        public final String validationMethod;
        
        /** Species (e.g., "human", "mouse") */
        public final String species;
        
        /**
         * Constructor for DatasetMetadata.
         */
        public DatasetMetadata(int siteCount, int sgRNACount, String validationMethod, String species) {
            // SCAFFOLD ONLY - Implementation pending
            this.siteCount = siteCount;
            this.sgRNACount = sgRNACount;
            this.validationMethod = validationMethod;
            this.species = species;
        }
    }
}

/**
 * Adapter for CHANGE-seq datasets.
 * 
 * PURPOSE:
 * Loads CHANGE-seq data with validated off-target sites and activity levels.
 * Public visibility would require separate file (ChangeSeqAdapter.java).
 * Currently package-private as inner class for scaffold simplicity.
 * 
 * REQUIREMENTS SATISFIED:
 * - CHANGE-seq ground truth integration
 * - Activity-based stratification (on-target, Class A/B/C/D, non-functional)
 * 
 * DATA FLOW:
 * BED/CSV file → Parse columns → Extract chr, start, end, activity →
 * Create PAMSite objects → Return list
 * 
 * NOTE: In production, consider moving to separate file for public access
 * or adding factory method in OffTargetAdapter.
 */
class ChangeSeqAdapter extends OffTargetAdapter {
    
    /**
     * Constructor for ChangeSeqAdapter.
     * 
     * INPUTS:
     * @param dataPath Path to CHANGE-seq BED or CSV file
     */
    public ChangeSeqAdapter(String dataPath) {
        super("CHANGE-seq", dataPath);
        // SCAFFOLD ONLY - Implementation pending
    }
    
    @Override
    public List<PAMSite> loadOffTargetSites() throws java.io.IOException {
        // SCAFFOLD ONLY - Implementation pending
        // Will parse CHANGE-seq format:
        // Column 1: chromosome
        // Column 2: start position
        // Column 3: end position
        // Column 4: sgRNA sequence
        // Column 5: activity score (reads/cell)
        // Column 6: class (on-target, A, B, C, D, non-functional)
        return null;
    }
    
    @Override
    public DatasetMetadata getMetadata() {
        // SCAFFOLD ONLY - Implementation pending
        return null;
    }
}

/**
 * Adapter for GUIDE-seq datasets.
 * 
 * PURPOSE:
 * Loads GUIDE-seq data with off-target sites detected by double-strand break capture.
 * Package-private as inner class for scaffold simplicity.
 * 
 * DATA FLOW:
 * GUIDE-seq output → Parse detected sites → Extract coordinates →
 * Label with read counts → Return PAMSite list
 * 
 * NOTE: In production, consider moving to separate file for public access
 * or adding factory method in OffTargetAdapter.
 */
class GuideSeqAdapter extends OffTargetAdapter {
    
    /**
     * Constructor for GuideSeqAdapter.
     * 
     * INPUTS:
     * @param dataPath Path to GUIDE-seq output file
     */
    public GuideSeqAdapter(String dataPath) {
        super("GUIDE-seq", dataPath);
        // SCAFFOLD ONLY - Implementation pending
    }
    
    @Override
    public List<PAMSite> loadOffTargetSites() throws java.io.IOException {
        // SCAFFOLD ONLY - Implementation pending
        return null;
    }
    
    @Override
    public DatasetMetadata getMetadata() {
        // SCAFFOLD ONLY - Implementation pending
        return null;
    }
}

/**
 * Adapter for Nano-OTS (nanopore-based off-target screening) datasets.
 * 
 * PURPOSE:
 * Loads nanopore-based off-target detection results.
 * Package-private as inner class for scaffold simplicity.
 * 
 * DATA FLOW:
 * Nano-OTS output → Parse nanopore alignments → Extract off-target sites →
 * Return PAMSite list
 * 
 * NOTE: In production, consider moving to separate file for public access
 * or adding factory method in OffTargetAdapter.
 */
class NanoOTSAdapter extends OffTargetAdapter {
    
    /**
     * Constructor for NanoOTSAdapter.
     * 
     * INPUTS:
     * @param dataPath Path to Nano-OTS output
     */
    public NanoOTSAdapter(String dataPath) {
        super("Nano-OTS", dataPath);
        // SCAFFOLD ONLY - Implementation pending
    }
    
    @Override
    public List<PAMSite> loadOffTargetSites() throws java.io.IOException {
        // SCAFFOLD ONLY - Implementation pending
        return null;
    }
    
    @Override
    public DatasetMetadata getMetadata() {
        // SCAFFOLD ONLY - Implementation pending
        return null;
    }
}
