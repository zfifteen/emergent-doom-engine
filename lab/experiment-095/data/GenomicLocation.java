package lab.experiment095.data;

/**
 * Represents a genomic location.
 * 
 * PURPOSE:
 * Simple value object for chromosome and position coordinates.
 * 
 * DATA FLOW:
 * Used in PAMSite and SignalWindow for genomic coordinate storage.
 */
public class GenomicLocation {
    
    /** Chromosome identifier */
    private final String chromosome;
    
    /** Position in base pairs */
    private final long position;
    
    /**
     * Constructor for GenomicLocation.
     * 
     * INPUTS:
     * @param chromosome Chromosome identifier
     * @param position Genomic position
     */
    public GenomicLocation(String chromosome, long position) {
        // Implementation pending - Phase Three
        this.chromosome = null;
        this.position = 0;
    }
    
    // Getter methods - Implementation pending - Phase Three
}
