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
        double scaled = distanceToMean * 1000.0;
        if (scaled > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (scaled < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) Math.round(scaled);
    }
    
    public double[] getFeatures() { return features.clone(); }
    public String getSourceId() { return sourceId; }
    public double getDistanceToMean() { return distanceToMean; }
    
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

### Using the Cell - Two Execution Options

#### Option A: Using CellBasedExecutionEngine (Existing EDE Framework)

**Note**: This requires `WaveletFeatureCell` to extend `AbstractSortingCell` (Integer-based values). If using the `Cell` interface directly with Double values, this approach will NOT compile.

If you adapt `WaveletFeatureCell` to extend `AbstractSortingCell`:

```java
// 1. Extract features
List<FeatureVector> rawFeatures = extractor.extractWaveletLeaders(fast5Data);

// 2. Compute mean pattern
double[] meanPattern = computeMeanPattern(rawFeatures);

// 3. Create cells (as AbstractSortingCell subclass)
List<WaveletFeatureCell> cells = rawFeatures.stream()
    .map(fv -> new WaveletFeatureCell(fv.getData(), fv.getId(), meanPattern))
    .collect(Collectors.toList());

// 4. Execute using existing CellBasedExecutionEngine
import com.emergent.doom.execution.CellBasedExecutionEngine;

CellBasedExecutionEngine engine = new CellBasedExecutionEngine();
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

#### Option B: Using GenericCellExecutionEngine (Must Be Implemented First)

⚠️ **CRITICAL**: `GenericCellExecutionEngine` does NOT currently exist in the EDE framework. This code will NOT compile until you implement it (see EDE_INTEGRATION_GUIDE.md Step 4).

```java
// 1. Extract features
List<FeatureVector> rawFeatures = extractor.extractWaveletLeaders(fast5Data);

// 2. Compute mean pattern
double[] meanPattern = computeMeanPattern(rawFeatures);

// 3. Create cells
List<WaveletFeatureCell> cells = rawFeatures.stream()
    .map(fv -> new WaveletFeatureCell(fv.getData(), fv.getId(), meanPattern))
    .collect(Collectors.toList());

// 4. Execute emergent sorting
// ⚠️ GenericCellExecutionEngine must be implemented first (see EDE_INTEGRATION_GUIDE.md Step 4)
GenericCellExecutionEngine<WaveletFeatureCell> engine = 
    new GenericCellExecutionEngine<>();
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
   - Implement as shown above (implements `Cell<WaveletFeatureCell>`)
   - For Option A, extend `AbstractSortingCell` instead

2. **Modify**: `lab/experiment-095/sorting/EmergentSorter.java`
   - Import `CellBasedExecutionEngine` (Option A) or `GenericCellExecutionEngine` (Option B)
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
    // Option B - requires GenericCellExecutionEngine implementation
    List<WaveletFeatureCell> cells = createRandomCells(100);
    GenericCellExecutionEngine<WaveletFeatureCell> engine = 
        new GenericCellExecutionEngine<>();
    
    int steps = engine.executeSorting(cells, 2000);
    
    assertTrue(isSortedByDistance(cells));
    assertTrue(steps < 2000);  // Should converge early
}

private boolean isSortedByDistance(List<WaveletFeatureCell> cells) {
    for (int i = 0; i < cells.size() - 1; i++) {
        if (cells.get(i).compareTo(cells.get(i + 1)) > 0) {
            return false;
        }
    }
    return true;
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

4. **Type mismatch with CellBasedExecutionEngine**
   - `CellBasedExecutionEngine` expects `List<AbstractSortingCell>` (Integer values)
   - If using `Cell<WaveletFeatureCell>` directly with Double values, won't compile
   - Choose Option A (extend AbstractSortingCell) or Option B (implement GenericCellExecutionEngine)

### Build and Run

```bash
# Build EDE core
mvn clean install

# Compile experiment using find (works on all platforms)
find lab/experiment-095 -name "*.java" -exec javac -cp target/classes -d build {} +

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
    → Sorting 4000 cells with execution engine
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

### Decision Guide: Which Option?

| Factor | Option A (AbstractSortingCell) | Option B (GenericCellExecutionEngine) |
|--------|-------------------------------|----------------------------------------|
| **Uses existing framework** | ✓ Yes | ✗ No (must implement) |
| **Implementation effort** | Medium (adapt cell type) | High (implement new engine) |
| **Type compatibility** | Integer values only | Any Cell implementation |
| **Full EDE features** | ✗ Limited | ✓ Can use neighborhoods |
| **Recommended for quick integration** | ✓ Yes | ✗ No |
| **Recommended for full EDE features** | ✗ No | ✓ Yes |

### Questions?

See:
- [README.md](README.md) - Full documentation with integration options
- [EDE_INTEGRATION_GUIDE.md](EDE_INTEGRATION_GUIDE.md) - Detailed step-by-step implementation
- [Main EDE README](../../README.md) - Framework overview
