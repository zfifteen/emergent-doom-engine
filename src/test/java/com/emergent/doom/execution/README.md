# Chapter 3: Execution Engines

Execution engines orchestrate emergent computation by coordinating cell swaps, managing metadata, and detecting convergence. They transform local pairwise interactions into system-level sorting through repeated iteration without global control.

## Purpose

Tests in this package validate that execution engines correctly:
- Execute sorting algorithms via bottom-up cell interactions
- Manage external metadata separate from cell data
- Detect convergence using stable-step heuristics
- Support both synchronous (single-threaded) and parallel (multi-threaded) execution
- Provide deterministic results with fixed random seeds
- Track execution state (step count, running status, convergence)

## Concepts Covered

### Doom (Inevitability Toward Target State)

"Doom" in the EDE context means **inevitable convergence toward a goal state**, not catastrophe:
- System progresses inexorably toward sorted state
- Convergence is inevitable given sufficient steps
- Doom is the engine's purpose, not a failure mode

### Decentralized Orchestration

Engines coordinate without centralized decision-making:
- Each step, cells autonomously decide swaps based on local comparisons
- No global "plan" - order emerges from repetition
- Engine provides iteration framework, not sorting intelligence

### External Metadata Management

Metadata (algotype, sort direction, ideal position) is managed separately from cells:
- **Metadata providers**: `IntFunction<CellMetadata>` maps position → metadata
- **Metadata swapping**: Metadata follows cells during swaps (preserves agent identity)
- **Lightweight cells**: Pure `Comparable` data carriers with zero engine state

### Convergence Detection

Engines use **stable-step heuristics** to detect when sorting completes:
- **No-swap convergence**: N consecutive steps with zero swaps
- **Metric-based convergence**: Sortedness reaches threshold (e.g., 99%)
- **Max steps**: Safety timeout to prevent infinite loops

### Execution Modes

1. **Synchronous**: Single-threaded, deterministic, fast for small arrays
2. **Parallel**: Multi-threaded, scales with core count, non-deterministic without seed
3. **Lock-based**: Thread-safe variant with explicit locking (experimental)

## Prerequisites

**Required:**
- [Chapter 1: Cell Foundations](../cell/README.md) - Cell interface and comparison
- [Chapter 2: Swap Mechanics](../swap/README.md) - Swap engine and frozen cells
- [Chapter 2: Probe Recording](../probe/README.md) - Snapshot capture
- [Chapter 2: Metrics](../metrics/README.md) - Convergence measurement

**Helpful:**
- Java concurrency basics (`Random`, threading, atomicity)
- Algotype behaviors (Bubble, Insertion, Selection)

## Test Files

### SynchronousExecutionEngineTest.java

Comprehensive tests for single-threaded execution.

**Test Categories:**

1. **Basic Sorting Functionality**
   - Small array sorting
   - Already-sorted arrays (fast convergence)
   - Reverse-sorted arrays (worst case)
   - Edge cases: single element, two elements

2. **Lifecycle Management**
   - Reset between runs
   - Stop during execution
   - State verification (step count, running flag)

3. **Step Execution**
   - Step returns swap count
   - Multiple steps progress sorting incrementally

4. **Probe Integration**
   - Initial snapshot recording
   - Snapshot after each step
   - Trajectory completeness

5. **Convergence Detection**
   - No-swap heuristic triggers convergence
   - Max steps prevents infinite loops

6. **Determinism**
   - Same seed → same results
   - Multiple runs produce identical outcomes

7. **Metadata Provider Pattern**
   - Engine accepts `IntFunction<CellMetadata>` constructor
   - Metadata swaps with cells during execution
   - Sorting works with external metadata (no cell.getAlgotype())

**Link to source:** [SynchronousExecutionEngine.java](../../../../../../main/java/com/emergent/doom/execution/SynchronousExecutionEngine.java)

### ParallelExecutionEngineTest.java

Tests multi-threaded execution with thread pool.

**Key Differences from Synchronous:**
- Uses `ExecutorService` for concurrent swap execution
- Non-deterministic swap order without seed
- Higher overhead for small arrays, better scaling for large arrays
- Thread safety requirements

**Key Tests:**
- Parallel sorting correctness
- Thread pool lifecycle management
- Determinism with seeded `Random`
- Performance characteristics vs synchronous

**Link to source:** [ParallelExecutionEngine.java](../../../../../../main/java/com/emergent/doom/execution/ParallelExecutionEngine.java)

### LockBasedExecutionEngineTest.java

Tests experimental lock-based parallel execution.

**Key Features:**
- Explicit locking for swap coordination
- Reduced contention compared to coarse-grained locking
- Experimental - may be deprecated

**Link to source:** [LockBasedExecutionEngine.java](../../../../../../main/java/com/emergent/doom/execution/LockBasedExecutionEngine.java)

### CellMetadataTest.java

Tests external metadata management system.

**Key Tests:**
- Metadata construction and validation
- Algotype and sort direction storage
- Ideal position tracking (for SELECTION algotype)
- Thread-safe atomic operations
- Boundary updates for group merging

**Why it matters:** Validates the lightweight cell architecture's metadata separation.

**Link to source:** [CellMetadata.java](../../../../../../main/java/com/emergent/doom/execution/CellMetadata.java)

## Usage Examples

### Basic Synchronous Execution

Run sorting with minimal setup:

```java
// Create cells (lightweight - just Comparable)
IntCell[] cells = { new IntCell(5), new IntCell(3), new IntCell(1) };

// Create infrastructure
FrozenCellStatus frozenStatus = new FrozenCellStatus();
SwapEngine<IntCell> swapEngine = new SwapEngine<>(frozenStatus);
Probe<IntCell> probe = new Probe<>();
probe.setRecordingEnabled(true);
ConvergenceDetector<IntCell> convergence = new NoSwapConvergence<>(10);

// Provide external metadata (cells don't know their algotype)
IntFunction<CellMetadata> metadataProvider = index -> 
    new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);

// Create engine
SynchronousExecutionEngine<IntCell> engine = 
    new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                     convergence, metadataProvider);

// Run until converged or max 1000 steps
int finalStep = engine.runUntilConvergence(1000);

System.out.println("Converged at step: " + finalStep);
System.out.println("Final state: " + Arrays.toString(cells));
System.out.println("Is sorted: " + engine.hasConverged());
```

**Key points:**
- Cells are pure data (no algotype field)
- Metadata provided externally via function
- Engine handles all orchestration

### Deterministic Execution with Seed

Ensure reproducible results:

```java
long seed = 42L;
Random random = new Random(seed);

SynchronousExecutionEngine<IntCell> engine = 
    new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                     convergence, metadataProvider, random);

// Run 1
engine.runUntilConvergence(1000);
int steps1 = engine.getCurrentStep();

// Reset and run again with same seed
engine.reset();
random = new Random(seed); // Re-seed
engine = new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                          convergence, metadataProvider, random);
engine.runUntilConvergence(1000);
int steps2 = engine.getCurrentStep();

assert steps1 == steps2; // Deterministic!
```

**Key points:**
- Same seed → identical execution
- Useful for debugging and reproducibility
- Required for scientific experiments

### Parallel Execution for Large Arrays

Scale to multiple cores:

```java
// Large array
IntCell[] cells = new IntCell[10000];
for (int i = 0; i < cells.length; i++) {
    cells[i] = new IntCell(10000 - i); // Reverse sorted
}

// Create infrastructure (same as synchronous)
// ... swapEngine, probe, convergence, metadataProvider ...

// Parallel engine with thread pool
ParallelExecutionEngine<IntCell> engine = 
    new ParallelExecutionEngine<>(cells, swapEngine, probe, 
                                  convergence, metadataProvider);

// Run with parallelism
int finalStep = engine.runUntilConvergence(50000);

System.out.println("Sorted " + cells.length + " cells in " + 
                   finalStep + " steps");

// Don't forget cleanup!
engine.stop(); // Shuts down thread pool
```

**Key points:**
- Better performance for large arrays (n > 1000)
- Non-deterministic without seed
- Must call `stop()` to release thread pool

### Using Metadata Providers for Chimeric Populations

Mix multiple algotypes in one array:

```java
IntCell[] cells = new IntCell[100];
// ... initialize cells ...

// First 50 cells use BUBBLE, last 50 use INSERTION
IntFunction<CellMetadata> chimericMetadata = index -> {
    Algotype algotype = (index < 50) ? Algotype.BUBBLE : Algotype.INSERTION;
    return new CellMetadata(algotype, SortDirection.ASCENDING);
};

SynchronousExecutionEngine<IntCell> engine = 
    new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                     convergence, chimericMetadata);

engine.runUntilConvergence(10000);

// Observe emergent clustering - cells segregate by algotype!
```

**Key points:**
- Metadata provider can return different algotypes per cell
- Enables chimeric population experiments
- See [Chapter 4: Chimeric Populations](../chimeric/README.md) for details

### Step-by-Step Execution

Manual control for debugging:

```java
SynchronousExecutionEngine<IntCell> engine = 
    new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                     convergence, metadataProvider);

// Execute steps manually
for (int step = 0; step < 10; step++) {
    int swaps = engine.step();
    System.out.printf("Step %d: %d swaps, State: %s%n", 
                      step, swaps, Arrays.toString(cells));
    
    if (engine.hasConverged()) {
        System.out.println("Converged early!");
        break;
    }
}
```

**Key points:**
- `step()` returns number of swaps performed
- Inspect state between steps
- Useful for understanding emergent dynamics

### Custom Convergence Detection

Implement domain-specific convergence logic:

```java
// Converge when Sortedness reaches 95%
ConvergenceDetector<IntCell> customDetector = new ConvergenceDetector<IntCell>() {
    private SortednessValue<IntCell> metric = new SortednessValue<>();
    
    @Override
    public boolean hasConverged(IntCell[] cells, int stepsSinceLastSwap) {
        double sortedness = metric.compute(cells);
        return sortedness >= 95.0;
    }
    
    @Override
    public void reset() {
        // No state to reset
    }
};

SynchronousExecutionEngine<IntCell> engine = 
    new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                     customDetector, metadataProvider);

engine.runUntilConvergence(5000);
```

**Key points:**
- Convergence detection is pluggable
- Can use metrics for domain-specific termination
- Balance precision vs computation cost

## Architecture Insights

### Metadata Swapping Preserves Identity

When cells swap positions, their metadata swaps too, preserving agent identity throughout execution:

#### Concrete Example: Metadata Follows Cells During Swaps

```
Execution Step: Swap cells at positions 2 and 5

Before:
  Position:  0    1    2    3    4    5
  Cells:    [10] [25] [30] [15] [20] [5]
  Metadata: [B]  [B]  [I]  [B]  [S]  [I]
            ↑                         ↑
         BUBBLE                   INSERTION

After swap(2, 5):
  Position:  0    1    2    3    4    5  
  Cells:    [10] [25] [5]  [15] [20] [30]
  Metadata: [B]  [B]  [I]  [B]  [S]  [I]
                      ↑                ↑
                   INSERTION      INSERTION
                      
❗ Metadata swaps WITH cells - agent identity preserved!
   Cell value 5 was INSERTION before, still INSERTION after.
   Cell value 30 was INSERTION before, still INSERTION after.

Legend:
  B = BUBBLE algotype (bidirectional movement)
  I = INSERTION algotype (left-only movement)  
  S = SELECTION algotype (targets ideal position)
```

#### Simple Example

```
Before swap:
  cells:    [10, 5]
  metadata: [BUBBLE, INSERTION]
  
After swap at (0, 1):
  cells:    [5, 10]
  metadata: [INSERTION, BUBBLE]  ← Metadata follows cells!
```

**Why this matters:**
- Metadata represents agent identity, not position
- BUBBLE cell at position 0 becomes BUBBLE cell at position 1
- Enables chimeric populations with persistent agent types
- Cell value 5 maintains its INSERTION identity throughout execution

### Doom as Architectural Principle

The engine implements "doom" through:
1. **Bounded iteration**: Max steps prevents infinite loops
2. **Convergence detection**: System recognizes goal state
3. **Monotonic progress**: Swaps always move toward sorted state (on average)

**Doom is NOT:**
- A failure condition
- Catastrophic breakdown
- Something to avoid

**Doom IS:**
- The intended outcome (convergence)
- Inevitability encoded in comparison logic
- The engine's purpose

### Synchronous vs Parallel Trade-offs

**Synchronous Engine:**
- ✅ Deterministic (same input → same output)
- ✅ Fast for small arrays (no threading overhead)
- ✅ Simple debugging (linear execution)
- ❌ Doesn't scale to multiple cores
- ❌ Slower for large arrays (n > 10,000)

**Parallel Engine:**
- ✅ Scales with core count
- ✅ Fast for large arrays
- ✅ Models biological parallelism
- ❌ Non-deterministic without seed
- ❌ Threading overhead hurts small arrays
- ❌ Complex debugging (race conditions)

**Rule of thumb:** Use synchronous for n < 1000, parallel for n > 1000.

### No Global Sorting Intelligence

Engines do NOT:
- Know what "sorted" means
- Plan optimal swap sequences
- Make strategic decisions

Engines DO:
- Iterate over cell pairs
- Ask cells to compare themselves
- Swap if comparison indicates disorder
- Repeat until convergence

**Sorting emerges from:**
- Cell comparison logic (`compareTo()`)
- Repeated local interactions
- Algotype behavioral policies

## Common Patterns

### Multi-Trial Experiments

Run same configuration multiple times:

```java
int trials = 100;
int[] convergenceSteps = new int[trials];

for (int trial = 0; trial < trials; trial++) {
    // Reset cells and engine
    IntCell[] cells = createRandomCells(100);
    SynchronousExecutionEngine<IntCell> engine = 
        createEngine(cells, new Random(trial)); // Different seed per trial
    
    convergenceSteps[trial] = engine.runUntilConvergence(5000);
}

// Analyze distribution
double avgSteps = Arrays.stream(convergenceSteps).average().orElse(0.0);
System.out.println("Average convergence: " + avgSteps + " steps");
```

### Progressive Freezing

Freeze cells as they reach final positions:

```java
while (!engine.hasConverged()) {
    engine.step();
    
    // Freeze cells in correct final positions
    for (int i = 0; i < cells.length; i++) {
        if (isInFinalPosition(cells[i], i)) {
            frozenStatus.setFrozen(i, FrozenType.IMMUTABLE);
        }
    }
}
```

**Use case:** Optimize convergence by crystallizing solved regions.

### Timeout Handling

Detect non-convergence gracefully:

```java
int maxSteps = 10000;
int finalStep = engine.runUntilConvergence(maxSteps);

if (finalStep >= maxSteps && !engine.hasConverged()) {
    System.err.println("WARNING: Did not converge within " + maxSteps + " steps");
    System.err.println("Final sortedness: " + 
                       new SortednessValue<>().compute(cells) + "%");
    // Handle non-convergence (retry, adjust parameters, etc.)
}
```

## Troubleshooting

### "Engine doesn't converge"

**Problem:** `runUntilConvergence()` hits max steps without converging.

**Check:**
1. Is your array sortable? (Valid `compareTo()` implementation)
2. Is convergence detector too strict? (Try increasing stable steps threshold)
3. Are too many cells frozen? (Prevents swaps needed for sorting)

**Debug:**
```java
// Print state every 100 steps
for (int step = 0; step < maxSteps; step += 100) {
    for (int i = 0; i < 100; i++) engine.step();
    System.out.println("Step " + step + ": " + 
                       new SortednessValue<>().compute(cells) + "% sorted");
}
```

### "Parallel engine produces different results each run"

**Problem:** Non-deterministic behavior.

**Solution:** Provide seeded `Random`:
```java
// ✅ Deterministic
ParallelExecutionEngine<IntCell> engine = 
    new ParallelExecutionEngine<>(cells, swapEngine, probe, 
                                  convergence, metadataProvider, new Random(42L));
```

### "Metadata not swapping with cells"

**Problem:** Metadata stays at original positions after swaps.

**This is a bug** - metadata should always swap with cells. File an issue with:
- Engine type (Synchronous/Parallel)
- Reproduction code
- Expected vs actual metadata positions

### "OutOfMemoryError with probe enabled"

**Problem:** Probe storing too many snapshots.

**Solution:** Disable recording for large experiments:
```java
probe.setRecordingEnabled(false);
// Counters still work, but no snapshots stored
```

## Next Steps

Now that you understand execution orchestration, proceed to:

**[Chapter 4: Chimeric Populations](../chimeric/README.md)** - Explore emergent clustering when mixing multiple algotypes in one population.

**Also see:**
- [Chapter 4: Experiment Framework](../experiment/README.md) - Multi-trial experiment utilities
- [Chapter 5: Validation](../validation/README.md) - End-to-end system testing

---

**[← Back: Metrics](../metrics/README.md)** | **[↑ Test Suite Home](../README.md)** | **[Next: Chimeric Populations →](../chimeric/README.md)**
