# Quick Reference: Using EDE for Experiment-095

## For Developers Implementing the Refactoring

### Minimal EDE Cell Implementation

```java
package lab.experiment095.cell;

import com.emergent.doom.cell.Cell;

public class WaveletFeatureCell implements Cell<WaveletFeatureCell> {
    private final double[] features;      // 28D signature
    private final String sourceId;        // FAST5 trace ID
    private final double distanceToMean;  // Similarity metric
    
    public WaveletFeatureCell(double[] features, String sourceId, double[] meanPattern) {
        this.features = features.clone();
        this.sourceId = sourceId;
        this.distanceToMean = euclidean(features, meanPattern);
    }
    
    @Override
    public int compareTo(WaveletFeatureCell other) {
        return Double.compare(this.distanceToMean, other.distanceToMean);
    }
    
    @Override
    public int getValue() {
        return (int) Math.round(distanceToMean * 1000);
    }
    
    public double[] getFeatures() { return features.clone(); }
    public String getSourceId() { return sourceId; }
    
    private static double euclidean(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
}
```

### Using the Cell

```java
// 1. Extract features
List<FeatureVector> rawFeatures = extractor.extractWaveletLeaders(fast5Data);

// 2. Compute mean pattern
double[] meanPattern = computeMeanPattern(rawFeatures);

// 3. Create cells
List<WaveletFeatureCell> cells = rawFeatures.stream()
    .map(fv -> new WaveletFeatureCell(fv.getData(), fv.getId(), meanPattern))
    .collect(Collectors.toList());

// 4. Execute emergent sorting (EDE)
// Note: GenericCellExecutionEngine must be created (see EDE_INTEGRATION_GUIDE.md)
// Alternatively, extend WaveletFeatureCell from AbstractCell to use CellBasedExecutionEngine
GenericCellExecutionEngine<WaveletFeatureCell> engine = new GenericCellExecutionEngine<>();
int steps = engine.executeSorting(cells, 2000);

// 5. Extract tiers from sorted order
int n = cells.size();
int tier1Cutoff = (int) (n * 0.05);
int tier2Cutoff = (int) (n * 0.30);

for (int i = 0; i < cells.size(); i++) {
    int tier = (i < tier1Cutoff) ? 1 : (i < tier2Cutoff) ? 2 : 3;
    tierMap.put(cells.get(i).getSourceId(), tier);
}
```

### Key Files to Modify

1. **Create**: `lab/experiment-095/cell/WaveletFeatureCell.java`
2. **Modify**: `lab/experiment-095/sorting/EmergentSorter.java`
   - Import `CellBasedExecutionEngine`
   - Replace custom iteration with `engine.executeSorting()`
3. **Keep unchanged**: 
   - `features/WaveletLeaderExtractor.java`
   - `data/DatasetManager.java`
   - `validation/StatisticalValidator.java`
   - `classification/MLPClassifier.java`

### What NOT to Change

- Feature extraction logic (domain-specific)
- Dataset loading (experiment-specific)
- MLP classifier (supervised learning component)
- Validation suite (statistical tests)

Only replace the **emergent sorting** component with EDE.

### Testing the Integration

```java
@Test
public void testCellComparison() {
    double[] meanPattern = {1.0, 2.0, ...};  // 28D
    double[] close = {1.1, 2.1, ...};        // Similar to mean
    double[] far = {5.0, 8.0, ...};          // Different from mean
    
    WaveletFeatureCell closeCell = new WaveletFeatureCell(close, "id1", meanPattern);
    WaveletFeatureCell farCell = new WaveletFeatureCell(far, "id2", meanPattern);
    
    assertTrue(closeCell.compareTo(farCell) < 0);  // Closer comes first
}

@Test
public void testSortingConvergence() {
    List<WaveletFeatureCell> cells = createRandomCells(100);
    CellBasedExecutionEngine engine = new CellBasedExecutionEngine();
    
    int steps = engine.executeSorting(cells, 2000);
    
    assertTrue(isSortedByDistance(cells));
    assertTrue(steps < 2000);  // Should converge early
}
```

### Common Pitfalls

1. **Don't modify cell features during sorting**
   - Features are immutable
   - Distance is precomputed at construction

2. **Don't forget to clone arrays**
   - Use `features.clone()` in constructor
   - Use `features.clone()` in getter

3. **Handle edge cases**
   - Empty feature lists
   - Single-cell arrays
   - All-identical features

### Build and Run

```bash
# Build EDE core
mvn clean install

# Compile experiment
javac -cp target/classes -d build lab/experiment-095/**/*.java

# Run experiment
java -cp build:target/classes lab.experiment095.WaveCrisprSignalExperiment
```

### Expected Output

```
=== Wave-CRISPR-Signal Experiment (Experiment-095) ===
Validating emergent PAM detection via wavelet-leader tiering

[1/8] Initializing configuration...
  ✓ Configuration loaded successfully

[2/8] Creating experiment instance...
  ✓ Experiment initialized

[3/8] Executing experimental pipeline...
  [3.1] Loading datasets...
  [3.2] Extracting wavelet-leader features...
  [3.3] Running emergent sorting via EDE...
    → Sorting 4000 cells with CellBasedExecutionEngine
    → Converged in 1247 steps
  [3.4] Training MLP classifier...
  ✓ Experiment completed successfully

=== Experiment Summary ===
Accuracy: 92.00%
AUROC: 0.9500
Tier assignments:
  Tier 1: 200 PAMs
  Tier 2: 1000 PAMs
  Tier 3: 2800 PAMs
```

### Questions?

See:
- [README.md](README.md) - Full documentation
- [EDE_INTEGRATION_GUIDE.md](EDE_INTEGRATION_GUIDE.md) - Detailed integration steps
- [Main EDE README](../../README.md) - Framework overview
