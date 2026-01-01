package lab.experiment095.data;

/**
 * Represents a PAM site from CHANGE-seq data.
 * 
 * PURPOSE:
 * Stores genomic coordinates and biological activity information for a validated PAM site.
 * Used as ground truth for training and validation.
 * 
 * REQUIREMENTS SATISFIED:
 * - Dataset A (Section 1.1): CHANGE-seq ground truth representation
 * - Activity classification: on-target, Class A/B/C/D, non-functional
 * 
 * DATA FLOW:
 * CHANGE-seq file → Parse → PAMSite objects → Cross-reference with signal data
 */
public class PAMSite {
    
    /** Chromosome identifier (e.g., "chr1", "chr2") */
    private final String chromosome;
    
    /** Genomic position (base pair) */
    private final long position;
    
    /** PAM sequence (e.g., "NGG") */
    private final String pamSequence;
    
    /** Activity class: ON_TARGET, CLASS_A, CLASS_B, CLASS_C, CLASS_D, NON_FUNCTIONAL */
    private final ActivityClass activityClass;
    
    /** Quantified cleavage activity (0.0 to 1.0) */
    private final double cleavageActivity;
    
    /** sgRNA identifier */
    private final String sgRNAId;
    
    /**
     * Constructor for PAMSite.
     * 
     * PURPOSE:
     * Creates immutable PAMSite object with validated genomic and activity data.
     * 
     * INPUTS:
     * @param chromosome Chromosome identifier
     * @param position Genomic position
     * @param pamSequence PAM sequence
     * @param activityClass Activity classification
     * @param cleavageActivity Quantified cleavage activity
     * @param sgRNAId Associated sgRNA identifier
     */
    public PAMSite(String chromosome, long position, String pamSequence,
                   ActivityClass activityClass, double cleavageActivity,
                   String sgRNAId) {
        // Implementation pending - Phase Three
        this.chromosome = null;
        this.position = 0;
        this.pamSequence = null;
        this.activityClass = null;
        this.cleavageActivity = 0.0;
        this.sgRNAId = null;
    }
    
    /**
     * Activity classification for PAM sites based on CHANGE-seq validation.
     */
    public enum ActivityClass {
        /** On-target site with high cleavage activity */
        ON_TARGET,
        /** High-activity off-target (Class A/B in protocol) */
        HIGH_ACTIVITY_OFF_TARGET,
        /** Medium-activity off-target (Class C) */
        MEDIUM_ACTIVITY_OFF_TARGET,
        /** Low-activity off-target (Class D) */
        LOW_ACTIVITY_OFF_TARGET,
        /** NGG motif with zero cleavage activity */
        NON_FUNCTIONAL
    }
    
    // Getter methods - Implementation pending - Phase Three
}
