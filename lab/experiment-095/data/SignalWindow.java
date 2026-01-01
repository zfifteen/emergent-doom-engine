package lab.experiment095.data;

/**
 * Represents a nanopore signal window centered on a PAM candidate.
 * 
 * PURPOSE:
 * Stores raw current trace and associated metadata for a PAM-centered signal window.
 * This is the primary data structure passed through the experimental pipeline.
 * 
 * REQUIREMENTS SATISFIED:
 * - Dataset B (Section 1.1): Nanopore FAST5 signal window representation
 * - ±50bp (±250 samples at 4kHz) window around PAM center
 * - Labels: functional, ambiguous, non-functional
 * 
 * DATA FLOW:
 * FAST5 file → Extract window → SignalWindow → Feature extraction → Classification
 */
public class SignalWindow {
    
    /** Raw current trace I(t) in picoamperes */
    private final double[] currentTrace;
    
    /** Sampling rate in Hz */
    private final int samplingRate;
    
    /** Genomic location of window center */
    private final GenomicLocation location;
    
    /** Functional label (if known from CHANGE-seq cross-reference) */
    private FunctionalLabel label;
    
    /** Cleavage activity score (if available from CHANGE-seq) */
    private Double cleavageActivity;
    
    /** Flowcell chemistry version (e.g., "R9.4.1", "R10.4.1") */
    private final String chemistry;
    
    /** Whether this is synthetic data */
    private final boolean isSynthetic;
    
    /** Signal-to-noise ratio (for synthetic data) */
    private Double snr;
    
    /**
     * Constructor for SignalWindow.
     * 
     * PURPOSE:
     * Creates signal window object with raw trace and metadata.
     * Labels can be set later via cross-referencing.
     * 
     * INPUTS:
     * @param currentTrace Raw current measurements
     * @param samplingRate Sampling frequency in Hz
     * @param location Genomic location
     * @param chemistry Flowcell chemistry version
     * @param isSynthetic Whether data is synthetic
     */
    public SignalWindow(double[] currentTrace, int samplingRate,
                        GenomicLocation location, String chemistry,
                        boolean isSynthetic) {
        // Implementation pending - Phase Three
        this.currentTrace = null;
        this.samplingRate = 0;
        this.location = null;
        this.chemistry = null;
        this.isSynthetic = false;
        this.label = null;
        this.cleavageActivity = null;
        this.snr = null;
    }
    
    /**
     * Functional label for signal windows based on CHANGE-seq validation.
     */
    public enum FunctionalLabel {
        /** Validated functional PAM with cleavage activity */
        FUNCTIONAL,
        /** NGG motif but no CHANGE-seq data available */
        AMBIGUOUS,
        /** NGG motif with zero cleavage activity in CHANGE-seq */
        NON_FUNCTIONAL
    }
    
    // Getter and setter methods - Implementation pending - Phase Three
}
