# Chapter 4: Chimeric Populations

Chimeric populations mix multiple algotypes (sorting strategies) in a single array, enabling the study of **emergent clustering** and **spontaneous organization** patterns. This advanced feature demonstrates basal intelligence in minimal substrates.

## Purpose

Tests in this package validate that chimeric populations:
- Support arbitrary mixes of algotypes (BUBBLE, INSERTION, SELECTION, FIBONACCI)
- Enable percentage-based algotype distribution
- Integrate with lightweight cell architecture via metadata providers
- Facilitate clustering and segregation research
- Work with generic cell factories for flexible population creation

## Concepts Covered

### Emergent Clustering

When different algotypes coexist in one array, unexpected patterns emerge:
- **Algotype segregation**: Cells spontaneously group by strategy type
- **Clustering dynamics**: Like-algotype cells cluster without explicit coordination
- **Spatial organization**: Emergent zones of different behavioral types

From Levin et al. (2024): "Chimeric arrays exhibit clustering behavior where elements organize by algorithm type, revealing that sorting encodes information about both target state and method."

### Chimeric Populations as Research Tool

Chimeric populations enable experiments on:
- **Robustness**: How well systems handle behavioral diversity
- **Competition**: Which algotypes dominate in mixed environments
- **Cooperation**: Whether different strategies can coexist productively
- **Self-organization**: Pattern formation without centralized control

### Algotype Distribution Strategies

1. **Uniform distribution**: Equal percentages of each algotype
2. **Weighted distribution**: Custom percentages (e.g., 70% BUBBLE, 30% INSERTION)
3. **Spatial distribution**: Algotypes assigned by position (e.g., first half BUBBLE, second half INSERTION)
4. **Random seeded distribution**: Reproducible random mixes

## Prerequisites

**Required:**
- [Chapter 1: Cell Foundations](../cell/README.md) - Lightweight cell architecture
- [Chapter 3: Execution Engines](../execution/README.md) - Metadata providers and execution

**Helpful:**
- Levin et al. (2024) Section 3.3 - "Chimeric Arrays"
- Understanding of different algotype behaviors
- Concept of emergent self-organization

## Test Files

### ChimericPopulationTest.java **[Advanced]**

Tests for the chimeric population builder and utilities.

**Test Categories:**

1. **Population Creation**
   - Creating populations with correct sizes
   - 50/50 algotype mixes (disabled - requires metadata refactor)
   - Three-way mixes (Bubble/Insertion/Selection)
   - Custom percentage distributions

2. **Algotype Counting** (Disabled)
   - Note: These tests require metadata tracking, which is now external
   - Need refactoring to count via metadata arrays

3. **Provider Integration**
   - Percentage-based algotype providers
   - Custom factory patterns
   - Deterministic seeding for reproducibility

**Link to source:** [ChimericPopulation.java](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/src/main/java/com/emergent/doom/chimeric/ChimericPopulation.java)

### AlgotypeProvider Interface **[Advanced]**

Tests for algotype assignment strategies:

**Key Implementations:**
- `PercentageAlgotypeProvider`: Distributes algotypes by percentage
- Custom providers for spatial or rule-based assignment

**Link to source:** [AlgotypeProvider.java](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/src/main/java/com/emergent/doom/chimeric/AlgotypeProvider.java)

### CellFactory Interface **[Advanced]**

Tests for flexible cell construction:

**Key Implementations:**
- `GenericCellFactory`: Creates generic wrapper cells
- Custom factories for domain-specific cells

**Link to source:** [CellFactory.java](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/src/main/java/com/emergent/doom/chimeric/CellFactory.java)

## Usage Examples

### Creating a 50/50 Chimeric Population

Mix BUBBLE and INSERTION algotypes equally:

```java
int arraySize = 100;

// Define algotype distribution
Map<Algotype, Double> distribution = Map.of(
    Algotype.BUBBLE, 0.5,
    Algotype.INSERTION, 0.5
);

// Create percentage-based provider
PercentageAlgotypeProvider algotypeProvider = 
    new PercentageAlgotypeProvider(distribution, arraySize, 42L); // Seeded

// Cell factory
CellFactory<GenericCell> cellFactory = (position, algotype) -> 
    new GenericCell(position + 1); // Values 1-100

// Build population
ChimericPopulation<GenericCell> populationBuilder = 
    new ChimericPopulation<>(cellFactory, algotypeProvider);
GenericCell[] cells = populationBuilder.createPopulation(arraySize, GenericCell.class);

// Create metadata provider matching algotype distribution
IntFunction<CellMetadata> metadataProvider = index -> {
    String algotypeStr = algotypeProvider.getAlgotype(index, arraySize);
    Algotype algotype = Algotype.valueOf(algotypeStr);
    return new CellMetadata(algotype, SortDirection.ASCENDING);
};

// Run with chimeric metadata
SynchronousExecutionEngine<GenericCell> engine = 
    new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                     convergence, metadataProvider);
engine.runUntilConvergence(10000);

// Observe emergent clustering!
```

**Key points:**
- Algotype provider creates distribution strategy
- Metadata provider mirrors algotype assignments
- Clustering emerges during execution

### Three-Way Chimeric Mix

Combine the three Levin paper algotypes:

```java
Map<Algotype, Double> threeWayMix = Map.of(
    Algotype.BUBBLE, 0.33,
    Algotype.INSERTION, 0.34,
    Algotype.SELECTION, 0.33
);

PercentageAlgotypeProvider provider = 
    new PercentageAlgotypeProvider(threeWayMix, 99, 123L);

ChimericPopulation<GenericCell> population = 
    new ChimericPopulation<>(cellFactory, provider);
GenericCell[] cells = population.createPopulation(99, GenericCell.class);

// Each algotype gets ~33 cells
```

**Key points:**
- Percentages should sum to ~1.0
- Array size affects exact counts due to rounding
- Use seeded provider for reproducibility

### Spatial Algotype Distribution

Assign algotypes by position (not random):

```java
// Custom provider: first half BUBBLE, second half INSERTION
AlgotypeProvider spatialProvider = (position, arraySize) -> {
    return (position < arraySize / 2) ? "BUBBLE" : "INSERTION";
};

ChimericPopulation<GenericCell> population = 
    new ChimericPopulation<>(cellFactory, spatialProvider);
GenericCell[] cells = population.createPopulation(100, GenericCell.class);

// Initial state: BUBBLE left, INSERTION right
// After sorting: observe how zones interact and reorganize
```

**Use case:** Study boundary interactions between algotype regions.

### Measuring Clustering

Quantify emergent clustering behavior:

```java
// Run chimeric experiment
engine.runUntilConvergence(10000);

// Define clustering metric: count contiguous regions of same algotype
int clusterCount = countClusters(cells, metadataProvider);

System.out.println("Cluster count: " + clusterCount);
// Lower cluster count = stronger clustering (few large zones)
// Higher cluster count = dispersed (many small zones)
```

**Clustering metric implementation:**
```java
private int countClusters(GenericCell[] cells, IntFunction<CellMetadata> metadata) {
    if (cells.length == 0) return 0;
    
    int clusters = 1;
    Algotype prevAlgotype = metadata.apply(0).getAlgotype();
    
    for (int i = 1; i < cells.length; i++) {
        Algotype currAlgotype = metadata.apply(i).getAlgotype();
        if (currAlgotype != prevAlgotype) {
            clusters++;
            prevAlgotype = currAlgotype;
        }
    }
    
    return clusters;
}
```

### Custom Cell Factory

Create domain-specific cells for chimeric populations:

```java
// Factory for factorization cells
CellFactory<FactorCell> factorFactory = (position, algotype) -> {
    int candidate = computeCandidateFactor(position);
    return new FactorCell(candidate);
};

AlgotypeProvider provider = getYourProvider();

ChimericPopulation<FactorCell> population = 
    new ChimericPopulation<>(factorFactory, provider);
FactorCell[] cells = population.createPopulation(100, FactorCell.class);
```

**Key points:**
- Factory receives position and algotype
- Can use position for value initialization
- Algotype used for metadata (not cell state)

## Architecture Insights

### Lightweight Cells + Chimeric Populations

The lightweight cell architecture enables chimeric populations:

**Before (heavyweight cells with embedded algotype):**
```java
// ❌ Cell knows its own algotype - hard to change
class MyCell implements Cell<MyCell> {
    private final Algotype algotype; // Embedded
    
    public Algotype getAlgotype() {
        return algotype;
    }
}
```

**After (lightweight cells with external metadata):**
```java
// ✅ Cell is pure domain data
class MyCell implements Cell<MyCell> {
    // No algotype field!
}

// Algotype lives in metadata
IntFunction<CellMetadata> metadata = index -> 
    new CellMetadata(chimericProvider.getAlgotype(index), ASCENDING);
```

**Why this matters:**
- Same cell array can be used with different algotype distributions
- Algotypes can change dynamically (future feature)
- Chimeric experiments don't require special cell types

### Emergent Clustering Mechanism

Clustering emerges from algotype-specific behaviors:

**BUBBLE cells:**
- Compare with both neighbors
- Swap if out of order
- Create "bubbling" zones

**INSERTION cells:**
- Find position in sorted prefix
- Insert by swapping leftward
- Create "insertion zones"

**SELECTION cells:**
- Find minimum in unsorted suffix
- Swap with ideal position
- Create "selection zones"

When these behaviors coexist, cells with similar behaviors **naturally aggregate** because they reinforce each other's swapping patterns.

### Percentage Distribution Algorithm

`PercentageAlgotypeProvider` uses **shuffled assignment**:

1. Create list with exact counts: [BUBBLE × 50, INSERTION × 50]
2. Shuffle list with seeded random
3. Assign algotypes by shuffled order

**Result:** Exact percentages with random distribution, fully reproducible with seed.

## Common Patterns

### Baseline vs Chimeric Comparison

Compare homogeneous and chimeric populations:

```java
// Baseline: 100% BUBBLE
IntFunction<CellMetadata> homogeneous = index -> 
    new CellMetadata(Algotype.BUBBLE, ASCENDING);
int homogeneousSteps = runExperiment(cells, homogeneous);

// Chimeric: 50% BUBBLE, 50% INSERTION
IntFunction<CellMetadata> chimeric = getChimericMetadata();
int chimericSteps = runExperiment(cells, chimeric);

System.out.printf("Homogeneous: %d steps%n", homogeneousSteps);
System.out.printf("Chimeric: %d steps%n", chimericSteps);
System.out.printf("Chimeric overhead: %.1f%%%n", 
                  100.0 * (chimericSteps - homogeneousSteps) / homogeneousSteps);
```

**Research question:** Does mixing algotypes help or hurt convergence?

### Algotype Dominance Analysis

Which algotype "wins" in competition?

```java
// Run chimeric experiment
engine.runUntilConvergence(10000);

// Measure final clustering
Map<Algotype, Integer> clusterSizes = measureClusterSizes(cells, metadata);

System.out.println("BUBBLE cluster size: " + clusterSizes.get(Algotype.BUBBLE));
System.out.println("INSERTION cluster size: " + clusterSizes.get(Algotype.INSERTION));

// Larger cluster = more successful at organizing space
```

### Delayed Gratification in Chimeric Systems

Do chimeric populations exhibit more DG?

```java
List<Double> sortednessTrajectory = computeSortednessTrajectory(probe);
double dg = new DelayedGratificationCalculator().calculate(sortednessTrajectory);

System.out.println("Chimeric DG: " + dg);

// Hypothesis: Chimeric systems show higher DG due to conflicting strategies
```

## Troubleshooting

### "Algotype percentages don't sum to 1.0"

**Problem:** Distribution map has incorrect total.

**Solution:** Ensure percentages sum to 1.0:
```java
Map<Algotype, Double> mix = Map.of(
    Algotype.BUBBLE, 0.6,
    Algotype.INSERTION, 0.4
); // ✅ Sums to 1.0

// ❌ Wrong
Map<Algotype, Double> badMix = Map.of(
    Algotype.BUBBLE, 0.5,
    Algotype.INSERTION, 0.6
); // Sums to 1.1!
```

### "Algotype counts don't match expected percentages"

**Problem:** Rounding errors with array size.

**Example:** 100 cells, 33% BUBBLE, 33% INSERTION, 34% SELECTION
- 100 × 0.33 = 33 cells
- 100 × 0.33 = 33 cells  
- 100 × 0.34 = 34 cells
- Total = 100 ✅

**Use array sizes divisible by percentages when possible.**

### "No clustering observed"

**Problem:** Clustering may be subtle or require more steps.

**Debug:**
1. Visualize trajectory to see if clustering forms then dissolves
2. Try larger arrays (n > 200) for clearer patterns
3. Measure clustering quantitatively, not just visually
4. Check if convergence happened before clustering could form

### "Test disabled: requires cell metadata"

**Context:** Some older tests assumed cells had `getAlgotype()` methods.

**Status:** Tests are disabled pending refactor to use external metadata.

**Workaround:** Use metadata provider pattern in your own tests.

## Next Steps

Now that you understand chimeric populations, proceed to:

**[Chapter 4: Experiment Framework](../experiment/README.md)** - Learn how to run multi-trial experiments for statistical validation of chimeric phenomena.

**Also see:**
- [Chapter 4: Analysis Tools](../analysis/README.md) - Visualize clustering patterns over time
- [Chapter 2: Metrics](../metrics/README.md) - Quantify emergent clustering behavior

---

**[← Back: Execution Engines](../execution/README.md)** | **[↑ Test Suite Home](../README.md)** | **[Next: Experiment Framework →](../experiment/README.md)**
