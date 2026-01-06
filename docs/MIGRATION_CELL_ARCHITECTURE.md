# Migration Guide: Cell Architecture Refactor (PR #113)

## Breaking Changes: Position-Based to Cell-Based Algotype Binding

### What Changed

PR [#113](https://github.com/zfifteen/emergent-doom-engine/pull/113) implemented a clean break refactoring that fundamentally changed how algotypes are assigned and managed in the Emergent Doom Engine.

**Before (Position-Based)**: Algotypes were bound to array positions via external metadata providers. When cells swapped, only values moved—algotypes stayed frozen at their positions.

**After (Cell-Based)**: Algotypes are intrinsic properties embedded within cell objects. When cells swap, entire objects (value + algotype) relocate together.

### Why This Change?

This architectural shift achieves **Levin-aligned morphogenetic clustering semantics**:

- **Position-based binding** produced constant 71.20% aggregation (0% variance) throughout sorting
- **Cell-based binding** produces dynamic aggregation with ~18.30% variance (72.20% → 90.50% peak → 71.50%)
- The mid-sorting aggregation peak is the **quantitative signature** that algotypes genuinely cluster through physical cell movement

This aligns the implementation with Levin et al. (2024) research on morphogenetic clustering where behavioral identities physically relocate during collective problem-solving.

## Classes Removed (Clean Break)

### Deleted Permanently
- `PercentageAlgotypeProvider` - Position-based algotype distribution
- `AlgotypeProvider` - Interface for position-based providers
- `CellMetadata` - Parallel metadata arrays
- `BubbleTopology`, `InsertionTopology`, `SelectionTopology`, `FibonacciTopology`, `ChimericTopology` - Old topology classes
- `SynchronousExecutionEngine` - Old execution engine (moved to `.old`)
- Various old experiments and examples (moved to `.old`)

### Classes Marked `.old`
Files ending in `.old` are preserved for reference only and are not compiled. They can be safely deleted.

## New Architecture Components

### Core Abstractions

**`AbstractCell<V, A>`** - Domain-agnostic base class parameterized by value type and algotype enum:

```java
public abstract class AbstractCell<V extends Comparable<V>, A extends Enum<A>> {
    // Intrinsic immutable properties (travel with cell during swaps)
    public abstract A readAlgotype();
    public abstract V readValue();
    
    // Mutable positional state (updated by engine after swaps)
    public abstract int readCurrentPosition();
    public abstract void updatePositionTo(int newPosition);
    public abstract CellStatus readStatus();
    public abstract void updateStatusTo(CellStatus newStatus);
    
    // Behavioral policy (algotype-specific logic)
    public abstract boolean shouldMoveGiven(NeighborhoodView<V, A> neighbors);
    public abstract Optional<Integer> calculateTargetPositionGiven(NeighborhoodView<V, A> neighbors);
}
```

**`NeighborhoodView<V, A>`** - Encapsulates neighbor visibility, hiding array mechanics from cells:

```java
public class NeighborhoodView<V, A> {
    public Optional<AbstractCell<V, A>> getLeftNeighbor();
    public Optional<AbstractCell<V, A>> getRightNeighbor();
    public List<AbstractCell<V, A>> getVisibleNeighbors();
    // ... additional methods
}
```

**`SortingAlgotype`** - Enum defining behavioral policies for sorting domain:

```java
public enum SortingAlgotype {
    BUBBLE,      // Local adjacent bidirectional movement
    INSERTION,   // Prefix left view with conservative swaps
    SELECTION,   // Ideal position targeting with convergence
    FIBONACCI    // Logarithmic neighbor coverage
}
```

### Sorting Domain Implementation

**`AbstractSortingCell`** - Main entry point for sorting domain, fixing type parameters:

```java
public abstract class AbstractSortingCell 
    extends AbstractCell<Integer, SortingAlgotype> {
    protected final int value;              // Immutable sort key
    protected final SortingAlgotype algotype;  // Immutable behavioral policy
    protected int currentPosition;          // Mutable position tracking
    protected CellStatus status;            // Mutable execution status
}
```

**Concrete Implementations:**
- **`BubbleSortingCell`** - Random bidirectional movement (50/50 left/right)
- **`SelectionSortingCell`** - Ideal position targeting, increments on swap denial
- **`InsertionSortingCell`** - Conservative left-only movement, waits for sorted prefix

### Infrastructure

**`SortingCellFactory`** - Creates cells with embedded algotypes:

```java
SortingCellFactory factory = new SortingCellFactory(seed);
Map<SortingAlgotype, Double> distribution = Map.of(
    SortingAlgotype.BUBBLE, 0.4,
    SortingAlgotype.SELECTION, 0.3,
    SortingAlgotype.INSERTION, 0.3
);
List<AbstractSortingCell> cells = factory.createRandomCells(distribution, size, maxValue);
```

**`CellBasedExecutionEngine`** - Simple execution engine (~200 LOC) that swaps entire cell objects:

```java
CellBasedExecutionEngine engine = new CellBasedExecutionEngine();
int steps = engine.executeSorting(cells, maxSteps);
```

## Migration Steps

### Step 1: Replace Cell Creation

**Before (Position-Based):**
```java
// Old: External metadata provider with position-based algotypes
AlgotypeProvider provider = new PercentageAlgotypeProvider(
    Map.of(Algotype.BUBBLE, 0.5, Algotype.INSERTION, 0.5),
    arraySize,
    seed
);

IntFunction<CellMetadata> metadata = index -> {
    Algotype algotype = Algotype.valueOf(provider.getAlgotype(index, arraySize));
    return new CellMetadata(algotype, SortDirection.ASCENDING);
};

GenericCell[] cells = new GenericCell[arraySize];
for (int i = 0; i < arraySize; i++) {
    cells[i] = new GenericCell(values[i]);
}
```

**After (Cell-Based):**
```java
// New: Factory creates cells with embedded algotypes
SortingCellFactory factory = new SortingCellFactory(seed);

Map<SortingAlgotype, Double> distribution = Map.of(
    SortingAlgotype.BUBBLE, 0.5,
    SortingAlgotype.INSERTION, 0.5
);

List<AbstractSortingCell> cells = factory.createRandomCells(
    distribution, 
    arraySize, 
    maxValue
);
```

### Step 2: Replace Execution Engine

**Before (Position-Based):**
```java
SynchronousExecutionEngine<GenericCell> engine = 
    new SynchronousExecutionEngine<>(
        cells,
        swapEngine,
        probe,
        convergenceDetector,
        metadata  // External metadata provider
    );

int steps = engine.runUntilConvergence(maxSteps);
```

**After (Cell-Based):**
```java
CellBasedExecutionEngine engine = new CellBasedExecutionEngine();
int steps = engine.executeSorting(cells, maxSteps);
```

### Step 3: Update Cell Inspection

**Before (Position-Based):**
```java
// Query metadata separately from cell value
CellMetadata meta = metadata.apply(position);
Algotype algotype = meta.getAlgotype();
int value = cells[position].getValue();
```

**After (Cell-Based):**
```java
// Query cell directly - algotype is intrinsic property
AbstractSortingCell cell = cells.get(position);
SortingAlgotype algotype = cell.readAlgotype();
int value = cell.readValue();
CellStatus status = cell.readStatus();
int pos = cell.readCurrentPosition();
```

### Step 4: Update Chimeric Experiments

**Before (Position-Based):**
```java
Map<Algotype, Double> mix = Map.of(
    Algotype.BUBBLE, 0.5,
    Algotype.SELECTION, 0.5
);

AlgotypeProvider provider = new PercentageAlgotypeProvider(mix, size, seed);
IntFunction<CellMetadata> metadata = index -> 
    new CellMetadata(
        Algotype.valueOf(provider.getAlgotype(index, size)),
        SortDirection.ASCENDING
    );
```

**After (Cell-Based):**
```java
Map<SortingAlgotype, Double> mix = Map.of(
    SortingAlgotype.BUBBLE, 0.5,
    SortingAlgotype.SELECTION, 0.5
);

SortingCellFactory factory = new SortingCellFactory(seed);
List<AbstractSortingCell> cells = factory.createRandomCells(mix, size, maxValue);

// Algotypes are now embedded in cells and travel WITH cells during swaps!
```

## Key Architectural Differences

### Swap Semantics

**Before (Position-Based - INCORRECT for Levin alignment):**
```java
// Only values swap, algotypes frozen at positions
int temp = cells[i].getValue();
cells[i] = new GenericCell(cells[j].getValue());
cells[j] = new GenericCell(temp);

// Algotypes at positions i and j UNCHANGED!
// Result: Constant aggregation, no clustering
```

**After (Cell-Based - LEVIN-ALIGNED):**
```java
// Entire cell objects swap (value + algotype together)
AbstractSortingCell temp = cells.get(i);
cells.set(i, cells.get(j));
cells.set(j, temp);

// Update position tracking
cells.get(i).updatePositionTo(i);
cells.get(j).updatePositionTo(j);

// Algotypes TRAVEL WITH CELLS!
// Result: Dynamic aggregation with characteristic variance
```

### Algotype Query

**Before:** `String algotype = metadata.apply(position).getAlgotype().name()`  
**After:** `SortingAlgotype algotype = cell.readAlgotype()`

### Neighbor Visibility

**Before:** Topology classes (`BubbleTopology`, `InsertionTopology`, etc.)  
**After:** `NeighborhoodView` encapsulation built by engine based on algotype rules

### Metadata Management

**Before:** External `IntFunction<CellMetadata>` provider  
**After:** Embedded in cells as immutable properties

## Expected Behavioral Changes

### Clustering Dynamics

**Position-Based (Old):**
- Constant 71.20% aggregation throughout sorting
- 0% variance in aggregation values
- No mid-sorting peak
- Algotypes don't cluster because they don't move

**Cell-Based (New - Levin-Aligned):**
- Dynamic aggregation: 72.20% → 90.50% (peak) → 71.50%
- 18.30% variance (characteristic signature)
- Mid-sorting aggregation peak at ~50% progress
- Algotypes cluster as cells physically relocate

### Performance

The new `CellBasedExecutionEngine` is simpler (~200 LOC vs 1000+ LOC in old engine) and focuses on core cell swapping mechanics. Future integration with probe system and batch-level parallelism planned.

## Demo Application

Run the demonstration to see algotypes traveling with cells during swaps:

```bash
mvn compile
java -cp target/classes com.emergent.doom.examples.NewCellArchitectureDemo
```

Expected output shows:
- Initial algotype distribution
- Swaps relocating entire cell objects
- Aggregation percentage changing as algotypes cluster
- Clear contrast with position-based architecture

## Troubleshooting

### Compile Errors

If you see errors about missing classes:
- `PercentageAlgotypeProvider` → Use `SortingCellFactory`
- `CellMetadata` → Algotype is now embedded in cells
- `AlgotypeProvider` → Use `SortingCellFactory` with distribution map
- `SynchronousExecutionEngine` → Use `CellBasedExecutionEngine`

### Runtime Errors

**"Cannot cast GenericCell to AbstractSortingCell"**:  
Old `GenericCell` type not compatible with new architecture. Use `AbstractSortingCell` and concrete implementations (`BubbleSortingCell`, etc.) created by `SortingCellFactory`.

**"No such method getAlgotype()"**:  
Old metadata query pattern. Use `cell.readAlgotype()` instead of `metadata.apply(index).getAlgotype()`.

## Future Domain Extensions

The new architecture supports extensibility to non-sorting domains:

```java
// Example: Factorization domain (future)
public abstract class AbstractFactorizationCell 
    extends AbstractCell<FactorCandidate, FactorizationAlgotype> {
    // Domain-specific implementation
}

public enum FactorizationAlgotype {
    TRIAL_DIVISION,
    WHEEL_FACTORIZATION,
    POLLARD_RHO
}
```

Same cell architecture, different domain. Type parameters (`V`, `A`) enable domain-agnostic substrate.

## References

- **PR #113**: [Cell Architecture Refactor](https://github.com/zfifteen/emergent-doom-engine/pull/113)
- **REFACTOR_COMPLETE.md**: Detailed refactor completion summary
- **CELL_REFACTOR_COMPLETE.md**: Implementation guide
- **Levin et al. (2024)**: "Classical Sorting Algorithms as a Model of Morphogenesis" - Research foundation for Levin-aligned semantics

---

**Last Updated:** January 6, 2026  
**Migration Difficulty:** High (clean break, no backward compatibility)  
**Rationale:** Achieve Levin-aligned morphogenetic clustering semantics with characteristic 18.30% aggregation variance signature
