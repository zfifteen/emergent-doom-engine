# EDE Integration Guide for Experiment-095

## Purpose

This document provides detailed guidance for refactoring experiment-095 from a standalone implementation to a fully-integrated **Emergent Doom Engine (EDE)** client. Use this guide during the code refactoring phase.

## Integration Strategy

### Step 1: Implement WaveletFeatureCell

Create a new file: `lab/experiment-095/cell/WaveletFeatureCell.java`

```java
package lab.experiment095.cell;

import com.emergent.doom.cell.Cell;

/**
 * Cell implementation for 28-dimensional wavelet-leader PAM features.
 * 
 * Implements EDE's Cell interface to enable emergent sorting of
 * PAM candidates based on similarity to mean PAM pattern.
 */
public class WaveletFeatureCell implements Cell<WaveletFeatureCell> {
    
    // Immutable intrinsic properties
    private final double[] features;      // 28D wavelet-leader signature
    private final String sourceId;        // FAST5 trace identifier
    private final double distanceToMean;  // Precomputed similarity metric
    
    /**
     * Create a WaveletFeatureCell with computed distance to mean pattern.
     * 
     * @param features 28-dimensional wavelet-leader feature vector
     * @param sourceId Identifier for source FAST5 trace
     * @param meanPattern Mean PAM pattern for distance computation
     */
    public WaveletFeatureCell(double[] features, String sourceId, double[] meanPattern) {
        if (features.length != 28) {
            throw new IllegalArgumentException("Expected 28D features, got " + features.length);
        }
        
        this.features = features.clone();  // Defensive copy
        this.sourceId = sourceId;
        this.distanceToMean = computeEuclideanDistance(features, meanPattern);
    }
    
    @Override
    public int compareTo(WaveletFeatureCell other) {
        // PAM-likeness comparison: smaller distance = more PAM-like = comes first
        return Double.compare(this.distanceToMean, other.distanceToMean);
    }
    
    @Override
    public int getValue() {
        // For metrics: return quantized distance as integer
        double scaled = distanceToMean * 1000.0;
        if (scaled > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (scaled < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) Math.round(scaled);
    }
    
    // Domain-specific accessors
    
    public double[] getFeatures() {
        return features.clone();
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public double getDistanceToMean() {
        return distanceToMean;
    }
    
    // Helper methods
    
    private static double computeEuclideanDistance(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}
```

### Step 2: Choose Execution Approach

Before refactoring, decide which execution pattern to use. **Both approaches have trade-offs:**

**Recommended: Option B** - Create `GenericCellExecutionEngine` adapter
- Works with any `Cell` implementation
- No need to modify `WaveletFeatureCell` to extend a base class
- No algotype definitions required
- Simplest integration in terms of cell design
- **Trade-off**: Requires implementing new execution engine from scratch
- **Status**: This adapter does not yet exist in EDE and must be created

**Alternative: Option A** - Adapt `WaveletFeatureCell` to extend `AbstractSortingCell`
- Reuses existing `CellBasedExecutionEngine`
- No new implementations needed
- **Trade-off**: Must convert Double distance values to Integer for sorting
- **Limitation**: Loses precision; not ideal for all domain applications

Both are valid. Choose based on your constraints and timeline.

### Step 3: Refactor EmergentSorter to Use EDE

Modify `lab/experiment-095/sorting/EmergentSorter.java`:

**Before** (standalone implementation):
```java
public class EmergentSorter {
    public TierAssignment sort(List<FeatureVector> features) {
        // Custom sorting implementation
        // ...custom iteration logic...
        // ...custom swap logic...
    }
}
```

**After** (EDE client - Option B):
```java
package lab.experiment095.sorting;

import lab.experiment095.cell.WaveletFeatureCell;
import java.util.List;
import java.util.ArrayList;

/**
 * EDE-based emergent sorting adapter for PAM tiering.
 * 
 * Wraps EDE's execution engine to provide experiment-specific
 * tier assignment functionality.
 */
public class EmergentSorter {
    
    private final int iterations;
    private final double[] tierThresholds;
    
    public EmergentSorter(int iterations, String distanceMetric,
                          double[] tierThresholds, long randomSeed) {
        this.iterations = iterations;
        this.tierThresholds = tierThresholds;
    }
    
    /**
     * Execute emergent sorting via EDE framework.
     * 
     * @param features Raw 28D feature vectors
     * @return Tier assignments based on sorted order
     */
    public TierAssignment sort(List<FeatureVector> features) {
        // Step 1: Compute mean PAM pattern (unsupervised)
        double[] meanPattern = computeMeanPattern(features);
        
        // Step 2: Create WaveletFeatureCells
        List<WaveletFeatureCell> cells = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            FeatureVector fv = features.get(i);
            cells.add(new WaveletFeatureCell(
                fv.getData(),
                fv.getId(),
                meanPattern
            ));
        }
        
        // Step 3: Execute emergent sorting via EDE
        // Option B: Use GenericCellExecutionEngine (must be created - see Step 4)
        GenericCellExecutionEngine<WaveletFeatureCell> engine = 
            new GenericCellExecutionEngine<>();
        
        int stepsTaken = engine.executeSorting(cells, iterations);
        
        // Step 4: Extract tier assignments from sorted order
        return assignTiers(cells, tierThresholds);
    }
    
    private double[] computeMeanPattern(List<FeatureVector> features) {
        double[] mean = new double[28];
        for (FeatureVector fv : features) {
            double[] data = fv.getData();
            for (int i = 0; i < 28; i++) {
                mean[i] += data[i];
            }
        }
        for (int i = 0; i < 28; i++) {
            mean[i] /= features.size();
        }
        return mean;
    }
    
    private TierAssignment assignTiers(List<WaveletFeatureCell> sortedCells,
                                       double[] thresholds) {
        TierAssignment assignment = new TierAssignment();
        
        int n = sortedCells.size();
        int tier1Cutoff = (int) (n * thresholds[0]);
        int tier2Cutoff = (int) (n * (thresholds[0] + thresholds[1]));
        
        for (int i = 0; i < sortedCells.size(); i++) {
            WaveletFeatureCell cell = sortedCells.get(i);
            int tier;
            
            if (i < tier1Cutoff) {
                tier = 1;  // Top 5%
            } else if (i < tier2Cutoff) {
                tier = 2;  // Next 25%
            } else {
                tier = 3;  // Bottom 70%
            }
            
            assignment.assignTier(cell.getSourceId(), tier);
        }
        
        return assignment;
    }
}
```

### Step 4: Implementation Details for Each Approach

**Option A: Extend AbstractSortingCell**

This approach adapts `WaveletFeatureCell` to work with the existing `CellBasedExecutionEngine`. Key constraint: `AbstractSortingCell` is defined as `AbstractCell<Integer, SortingAlgotype>`, so you must map Double distances to Integer values:

```java
package lab.experiment095.cell;

import com.emergent.doom.cell.AbstractSortingCell;

public class WaveletFeatureCell extends AbstractSortingCell {
    private final double[] features;      // 28D signature
    private final String sourceId;
    private final double distanceToMean;
    
    public WaveletFeatureCell(double[] features, String sourceId, double[] meanPattern) {
        this.features = features.clone();
        this.sourceId = sourceId;
        this.distanceToMean = euclidean(features, meanPattern);
    }
    
    @Override
    public Integer readValue() {
        // Map Double distance to Integer (with bounds checking)
        double scaled = distanceToMean * 1000.0;
        if (scaled > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (scaled < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) Math.round(scaled);
    }
    
    @Override
    public int compareTo(AbstractSortingCell other) {
        return Integer.compare(this.readValue(), other.readValue());
    }
    
    public double[] getFeatures() { return features.clone(); }
    public String getSourceId() { return sourceId; }
}
```

Then use with existing `CellBasedExecutionEngine`:

```java
CellBasedExecutionEngine engine = new CellBasedExecutionEngine();
int steps = engine.executeSorting(cells, 2000);
```

**Option B: Create Generic Execution Engine Adapter**

**IMPORTANT**: This is a **simplified sorting algorithm**, not a full EDE framework adapter.

It provides basic sorting functionality for the Cell interface without implementing full EDE patterns like neighborhood views and algotype-driven behaviors.

```java
package lab.experiment095.execution;

import com.emergent.doom.cell.Cell;
import java.util.List;

/**
 * Generic cell execution engine for any Cell implementation.
 * 
 * ⚠️ WARNING: SIMPLIFIED SORTING ALGORITHM (not a full EDE adapter)
 * 
 * This class provides basic emergent sorting functionality for the Cell interface.
 * While it performs effective sorting, it does NOT implement the full EDE
 * architecture patterns such as:
 *   - Neighborhood views based on algotypes
 *   - Cell-driven decision making via shouldMoveGiven()/calculateTargetPositionGiven()
 *   - Trajectory recording and convergence analysis
 *   - Algotype-based behavioral policies
 * 
 * Use this when:
 * - You want to work with minimal Cell interface (no AbstractCell required)
 * - You don't need full EDE feature set
 * - Speed of integration is more important than leveraging advanced patterns
 * 
 * For production systems requiring neighborhoods and advanced behaviors,
 * consider implementing a more complete EDE-aligned engine that adapts
 * EDE's core architectural patterns rather than just implementing sorting.
 */
public class GenericCellExecutionEngine<T extends Cell<T>> {
    
    public int executeSorting(List<T> cells, int maxSteps) {
        int totalSwaps = 0;
        
        for (int step = 0; step < maxSteps; step++) {
            int swaps = executeStep(cells);
            totalSwaps += swaps;
            
            if (swaps == 0) {
                break;  // Converged - no swaps needed
            }
        }
        
        return totalSwaps;
    }
    
    private int executeStep(List<T> cells) {
        int swapCount = 0;
        
        // Single pass of comparisons and swaps
        for (int i = 0; i < cells.size() - 1; i++) {
            T current = cells.get(i);
            T next = cells.get(i + 1);
            
            // If current > next, they're out of order
            if (current.compareTo(next) > 0) {
                // Swap
                cells.set(i, next);
                cells.set(i + 1, current);
                swapCount++;
            }
        }
        
        return swapCount;
    }
}
```

### Step 5: External Metadata Management

Create `lab/experiment-095/metadata/ExperimentMetadata.java`:

```java
package lab.experiment095.metadata;

/**
 * External metadata for experiment tracking (EDE pattern).
 * 
 * Keeps experimental metadata separate from cell intrinsic properties,
 * following EDE's external metadata provider pattern. This allows cells
 * to remain immutable and focused on sorting, while metadata is managed
 * in external data structures.
 */
public class ExperimentMetadata {
    
    private final Boolean groundTruthLabel;  // CHANGE-seq validated PAM
    private final String datasetSource;      // Dataset identifier
    private final double qualityScore;       // Nanopore signal quality
    
    public ExperimentMetadata(Boolean groundTruthLabel,
                              String datasetSource,
                              double qualityScore) {
        this.groundTruthLabel = groundTruthLabel;
        this.datasetSource = datasetSource;
        this.qualityScore = qualityScore;
    }
    
    public Boolean getGroundTruthLabel() { return groundTruthLabel; }
    public String getDatasetSource() { return datasetSource; }
    public double getQualityScore() { return qualityScore; }
}
```

Use with cells via external maps (EDE pattern) - metadata is NOT stored on cells:

```java
// Create metadata map keyed by sourceId (external to cells)
Map<String, ExperimentMetadata> metadataMap = new HashMap<>();
for (WaveletFeatureCell cell : cells) {
    // Lookup metadata from external maps using cell's sourceId
    metadataMap.put(cell.getSourceId(), new ExperimentMetadata(
        groundTruth.get(cell.getSourceId()),           // External map
        datasetSource.get(cell.getSourceId()),         // External map
        qualityScore.get(cell.getSourceId())           // External map
    ));
}

// Access during validation WITHOUT storing in cell (EDE pattern)
for (WaveletFeatureCell cell : cells) {
    ExperimentMetadata metadata = metadataMap.get(cell.getSourceId());
    if (metadata.getGroundTruthLabel() != null && metadata.getGroundTruthLabel()) {
        // Validate using external metadata, not cell-stored data
        validateAgainstGroundTruth(cell, metadata);
    }
}
```

### Step 6: Update Main Experiment Runner

Modify `WaveCrisprSignalExperiment.java` to use refactored components:

```java
private ExperimentResults executeExperiment(ExperimentConfig config) {
    System.out.println("  [3.1] Loading datasets...");
    // Keep existing DatasetManager
    
    System.out.println("  [3.2] Extracting wavelet-leader features...");
    // Keep existing WaveletLeaderExtractor
    List<FeatureVector> rawFeatures = extractor.extractFeatures(data);
    
    System.out.println("  [3.3] Running emergent sorting via EDE...");
    // NOW USING EDE!
    EmergentSorter sorter = new EmergentSorter(
        config.getSorterIterations(),
        config.getDistanceMetric(),
        config.getTierThresholds(),
        config.getRandomSeed()
    );
    TierAssignment tiers = sorter.sort(rawFeatures);
    
    System.out.println("  [3.4] Training MLP classifier...");
    // Keep existing MLPClassifier
    
    // Continue with validation...
}
```

## Migration Checklist

- [ ] Create `lab/experiment-095/cell/WaveletFeatureCell.java`
- [ ] Decide: Extend `AbstractSortingCell` (Option A) or use `Cell` interface (Option B)
- [ ] If Option A: Adapt cell to map Double distances to Integer values
- [ ] If Option B: Create `GenericCellExecutionEngine` in `lab/experiment-095/execution/`
- [ ] Refactor `EmergentSorter` to use EDE execution
- [ ] Create `ExperimentMetadata` and external metadata maps
- [ ] Update `WaveCrisprSignalExperiment.executeExperiment()`
- [ ] Add integration tests (see Testing Integration below)
- [ ] Update documentation to reflect completed integration

## Testing Integration

```java
// Test cell comparison
WaveletFeatureCell cell1 = new WaveletFeatureCell(features1, "trace1", meanPattern);
WaveletFeatureCell cell2 = new WaveletFeatureCell(features2, "trace2", meanPattern);
assertTrue(cell1.compareTo(cell2) < 0);  // cell1 more PAM-like

// Test sorting convergence
List<WaveletFeatureCell> cells = createCells(...);
GenericCellExecutionEngine<WaveletFeatureCell> engine = new GenericCellExecutionEngine<>();
int steps = engine.executeSorting(cells, 2000);
assertTrue(isSorted(cells));  // Verify sorted order

// Helper method for verifying sort order in tests
private static boolean isSorted(List<WaveletFeatureCell> cells) {
    for (int i = 0; i < cells.size() - 1; i++) {
        if (cells.get(i).compareTo(cells.get(i + 1)) > 0) {
            return false;
        }
    }
    return true;
}
```

## Benefits of EDE Integration

1. **Leverage proven execution patterns** - No need to debug custom sorting logic
2. **Frozen cell support** - Handle corrupted signals automatically (full EDE only)
3. **Trajectory recording** - Analyze convergence dynamics (full EDE only)
4. **Metrics integration** - Use EDE's probe system (full EDE only)
5. **Domain-agnostic patterns** - Follow established Cell architecture
6. **Future extensions** - Easy to add clustering analysis, advanced features

## Next Steps

After completing integration:
1. Validate that tier assignments match standalone implementation
2. Add trajectory recording (if using Option A with full EDE)
3. Implement frozen cell handling for corrupted signals
4. Add EDE metrics probes for disorder tracking
5. Update documentation with actual code examples
6. Create tutorial showing EDE usage in bioinformatics context
