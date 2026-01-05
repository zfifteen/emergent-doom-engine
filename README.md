[![Java CI with Maven](https://github.com/zfifteen/emergent-doom-engine/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/zfifteen/emergent-doom-engine/actions/workflows/maven.yml)  [![Documentation Link Validation](https://github.com/zfifteen/emergent-doom-engine/actions/workflows/validate-docs-links.yml/badge.svg)](https://github.com/zfifteen/emergent-doom-engine/actions/workflows/validate-docs-links.yml)
# Emergent Doom Engine (EDE)

The Emergent Doom Engine (EDE) is a general-purpose, domain-agnostic API for simulating emergent phenomena. It provides a clean, extensible framework that is not tied to any specific application domain, including prime factorization or game development. The engine is designed around sortable Cell primitives and configurable sorting dynamics to enable modular composition of complex emergent systems. [1]

## 🚨 Breaking Changes in v2.0

**Per-cell threading modes removed** in favor of per-trial parallelism:
- `ExecutionMode.PARALLEL` and `LOCK_BASED` deleted
- Use `ExecutionMode.SEQUENTIAL` + `runBatchExperiments()` instead
- **Performance**: Expect 5-10× speedup due to eliminated synchronization overhead
- **Migration guide**: See [`docs/MIGRATION_v2.0.md`](docs/MIGRATION_v2.0.md)

## Why Emergence?

### The Honest Trade-off

If you just need to sort an array of integers, **use `Arrays.sort()`**. It's faster, simpler, and battle-tested.

The Emergent Doom Engine trades runtime efficiency for unique capabilities that traditional algorithms cannot provide:

### What Emergence Enables

#### 1. Robustness on Unreliable Substrates

Traditional sorting fails when parts of the array are locked or corrupted. EDE continues working:

```java
// Freeze 30% of array positions (simulate hardware defects)
FrozenCellStatus frozenStatus = new FrozenCellStatus();
frozenStatus.setFrozen(10, FrozenType.IMMOVABLE);
frozenStatus.setFrozen(25, FrozenType.IMMOVABLE);

// Sorting still converges around frozen cells!
SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
engine.runUntilConvergence(5000);
```

**Real-world applications:**
- Fault-tolerant distributed systems
- Self-repairing data structures
- Computation on degraded hardware

#### 2. Emergent Organization Patterns

Mix multiple sorting strategies in one array and watch spontaneous segregation emerge:

```java
// 50% bubble sort cells, 50% insertion sort cells (chimeric population)
Map<Algotype, Double> distribution = Map.of(
    Algotype.BUBBLE, 0.5,
    Algotype.INSERTION, 0.5
);

AlgotypeProvider algotypeProvider = 
    new PercentageAlgotypeProvider(distribution, arraySize, 42L);

IntFunction<CellMetadata> chimeric = index -> {
    Algotype algotype = Algotype.valueOf(algotypeProvider.getAlgotype(index, arraySize));
    return new CellMetadata(algotype, SortDirection.ASCENDING);
};

engine.runUntilConvergence(10000);

// Result: Cells cluster by algotype WITHOUT explicit clustering code!
// Bubble cells migrate to one region, insertion cells to another.
```

**Real-world applications:**
- Agent-based modeling
- Swarm intelligence research
- Self-organizing systems

#### 3. Observable Delayed Gratification

Some problems require temporary setbacks for long-term progress. EDE makes this visible:

```java
// Track disorder over time
Probe<GenericCell> probe = new Probe<>();
MonotonicityError<GenericCell> monotonicity = new MonotonicityError<>();

for (int step = 0; step < 100; step++) {
    engine.step();
    double disorder = monotonicity.compute(cells);
    System.out.println("Step " + step + ": " + disorder);
}

// Output shows disorder INCREASES before final convergence!
// Step 20: 45.0 inversions
// Step 40: 52.0 inversions (worse!)
// Step 80: 12.0 inversions (rapid improvement)
```

**Real-world applications:**
- Understanding biological development
- Multi-objective optimization
- Strategy evolution research

#### 4. Domain-Agnostic Framework

Implement `Comparable<T>` and EDE works for ANY problem:

```java
// Integer factorization (beyond sorting!)
public class FactorCell implements Cell<FactorCell> {
    private final int candidate;
    private final int remainder;
    
    @Override
    public int compareTo(FactorCell other) {
        return Integer.compare(this.remainder, other.remainder);
    }
}

// Same engine, different problem domain!
```

**Real-world applications:**
- Prototyping emergent algorithms for new domains
- Cross-domain research (biology ↔ computation)
- Educational tool for emergence concepts

### The Biological Inspiration

EDE models how biological systems achieve goals through **decentralized coordination**:

- Embryonic cells organize into organs without blueprints
- Ant colonies solve complex problems through simple local rules
- Slime molds navigate mazes via chemical signaling

These systems are **robust, adaptive, and emergent**. EDE brings these properties to computational problems.

### When to Use EDE

✅ **Use EDE when you need:**
- Fault tolerance (unreliable substrates)
- Emergent pattern detection (clustering, segregation)
- Observable dynamics (delayed gratification, trajectories)
- Cross-domain algorithm research
- Educational demonstrations of emergence

❌ **Don't use EDE when you need:**
- Maximum runtime performance (`Arrays.sort()` wins)
- Simple, one-off sorting tasks
- Guaranteed O(n log n) complexity
- Production-critical code without experimental tolerance

### Learn More

- **[Quickstart Tutorial](#quickstart-your-first-emergent-sort)** - See emergence in action (5 minutes)
- **[Test Suite Documentation](src/test/java/com/emergent/doom/README.md)** - Deep dive into capabilities
- **[Levin et al. (2024)](docs/theory/2401.05375v1.md)** - Theoretical foundations

## Quickstart: Your First Emergent Sort

See emergent sorting in action with a simple 6-integer example. Copy and run this code to watch local cell interactions produce global order:

```java
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.execution.*;
import com.emergent.doom.swap.*;
import com.emergent.doom.probe.*;
import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.SortDirection;
import java.util.Arrays;
import java.util.function.IntFunction;

public class QuickStart {
    public static void main(String[] args) {
        // 1. Create cells - lightweight Comparable wrappers with zero engine state
        GenericCell[] cells = {
            new GenericCell(5), new GenericCell(2), new GenericCell(9),
            new GenericCell(1), new GenericCell(7), new GenericCell(3)
        };
        
        // 2. Set up infrastructure
        SwapEngine<GenericCell> swapEngine = new SwapEngine<>(new FrozenCellStatus());
        Probe<GenericCell> probe = new Probe<>();
        ConvergenceDetector<GenericCell> convergence = new NoSwapConvergence<>(10);
        
        // 3. Provide metadata - cells don't know their own algotype
        IntFunction<CellMetadata> metadata = i -> 
            new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);
        
        // 4. Create engine and run
        SynchronousExecutionEngine<GenericCell> engine = 
            new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                             convergence, metadata);
        
        int steps = engine.runUntilConvergence(1000);
        
        // 5. Emergent sorting complete!
        System.out.println("Sorted in " + steps + " steps: " + 
                           Arrays.toString(cells));
    }
}
```

**Expected Output:**
```
Sorted in 47 steps: [1, 2, 3, 5, 7, 9]
```

### How It Works

**Cells are pure data carriers** - `GenericCell` implements only `compareTo()`. No sorting metadata lives in the cell objects.

**Infrastructure manages mechanics** - `SwapEngine` handles swap execution, `Probe` records the trajectory, `ConvergenceDetector` signals completion.

**Metadata providers enable domain-agnostic design** - The `IntFunction<CellMetadata>` supplies each cell position with its algotype and sort direction externally, keeping cells lightweight and reusable across domains.

**Engine orchestrates emergence** - `SynchronousExecutionEngine` evaluates cells sequentially, collecting swap proposals based on local comparisons. Global order emerges from these local interactions without centralized control.

### Next Steps

**Explore the test suite** - See [Test Suite Documentation](src/test/java/com/emergent/doom/README.md) for progressively complex examples, including chimeric populations, delayed gratification, and clustering behaviors.

**Try different algotypes** - Change `Algotype.BUBBLE` to `SELECTION`, `INSERTION`, or `FIBONACCI` to see how different sorting strategies affect convergence.

**Build your own cells** - Implement `Cell<YourType>` with custom `compareTo()` logic to sort domain-specific data using emergent dynamics.

## Glossary

<details>
<summary><strong>Core Concepts</strong></summary>

**Doom**  
Inevitable convergence toward a target state. In EDE, "doom" means the system inexorably progresses toward a solution through repeated local interactions. This is the engine's *purpose*, not a failure mode. The term emphasizes inevitability rather than catastrophe. See: [`SynchronousExecutionEngine`](src/main/java/com/emergent/doom/execution/SynchronousExecutionEngine.java)

**Emergent Computation**  
Solutions arise from collective dynamics of simple local agents, not from centralized algorithms. Order emerges from repeated pairwise comparisons without global planning or coordination. Each cell makes local swap decisions based only on comparisons with neighbors, yet global sorting emerges from these interactions.

**Cell**  
A lightweight `Comparable<T>` data carrier representing a single agent. Cells encapsulate domain-specific comparison logic but carry zero engine-specific metadata. The only required method is `compareTo()` - all domain logic is self-contained. See: [`Cell<T>`](src/main/java/com/emergent/doom/cell/Cell.java) interface.

**Algotype**  
The behavioral policy a cell follows when deciding swaps. Available algotypes: **BUBBLE** (bidirectional movement based on immediate neighbors), **INSERTION** (left-only swaps, requires sorted left side), **SELECTION** (targets ideal position with incremental convergence), and **FIBONACCI** (logarithmic neighbor coverage via Fibonacci-distance viewing). Algotypes are stored externally via metadata providers, not inside cells. See: [`Algotype`](src/main/java/com/emergent/doom/cell/Algotype.java) enum.

**Metadata Provider**  
A function (`IntFunction<CellMetadata>`) that supplies engine-specific metadata (algotype, sort direction, ideal position) for each cell position. Enables lightweight, domain-agnostic cell design by externalizing engine concerns. Cells remain pure `Comparable` objects without any sorting state. See: [`CellMetadata`](src/main/java/com/emergent/doom/execution/CellMetadata.java)

**Probe**  
Observability infrastructure that records execution trajectories. Captures snapshots of cell state at each step, plus counters for comparisons, swaps, and frozen attempts. Enables post-hoc analysis without affecting execution performance. See: [`Probe`](src/main/java/com/emergent/doom/probe/Probe.java)

**Convergence**  
The state where no beneficial swaps remain and the system has reached equilibrium. Detected via heuristics like "N consecutive steps with zero swaps" or "sortedness metric exceeds threshold." The engine terminates when convergence is achieved. See: [`ConvergenceDetector`](src/main/java/com/emergent/doom/execution/ConvergenceDetector.java)

**Frozen Cell**  
A cell constrained from moving (simulates unreliable substrate). **IMMOVABLE** cells reject all swaps and cannot participate in any movement. **MOVABLE** cells cannot initiate swaps but can be displaced by other cells. **NONE** cells are fully mobile. Simulates biological defects and tests robustness. See: [`FrozenCellStatus`](src/main/java/com/emergent/doom/swap/FrozenCellStatus.java)

</details>

<details>
<summary><strong>Advanced Concepts</strong></summary>

**Chimeric Population**  
An array mixing multiple algotypes (e.g., 50% BUBBLE, 50% INSERTION). Enables study of emergent clustering, where cells spontaneously segregate by strategy type without explicit clustering logic. Reveals how sorting dynamics encode both value ordering and algorithmic identity. See: [Test Suite - Chimeric Package](src/test/java/com/emergent/doom/chimeric/README.md)

**Delayed Gratification**  
Temporary increases in disorder (monotonicity) that enable long-term progress. Insertion and selection cells may make situations temporarily worse to achieve better final outcomes. For example, a SELECTION cell might swap with a worse neighbor to reach its ideal position, temporarily increasing disorder but moving closer to the solution. Measured via monotonicity metric over time.

**Substrate**  
The computational environment cells operate on. EDE explores "unreliable substrates" with frozen cells and stochastic ordering to model biological constraints. Unlike traditional algorithms that assume perfect execution, EDE tests how sorting dynamics adapt to defects and failures in the underlying system.

**Spearman Distance**  
Metric measuring rank correlation between current and target orderings. Reaches 0 when fully sorted, 1 when reverse sorted. Tracks progress independent of cell values - two arrays with different numbers but identical relative ordering have the same Spearman distance. See: [Metrics Package](src/test/java/com/emergent/doom/metrics/README.md)

**Topology**  
The neighborhood structure defining which cells can interact. **BUBBLE** uses immediate neighbors (i±1), **INSERTION** views all left cells, **SELECTION** uses dynamic ideal position, **FIBONACCI** uses Fibonacci-sequence distances for logarithmic coverage. Topology shapes emergent behavior without changing cell logic.

**Monotonicity Error**  
The number of inversions (disorder) remaining in the array. An inversion is a pair of cells (i, j) where i < j but cell[i] > cell[j]. Monotonicity error decreases as sorting progresses, though delayed gratification strategies may temporarily increase it. Reaches zero when fully sorted.

**Sort Direction**  
Whether cells prefer ascending (smaller values toward left) or descending (larger values toward left) order. Part of external metadata - the same cell can sort ascending or descending based on configuration. Chimeric populations can mix both directions in a single array. See: [`SortDirection`](src/main/java/com/emergent/doom/cell/SortDirection.java)

</details>

## Theoretical Background

The EDE concept is inspired by research on sorting algorithms as models of emergent behavior and decentralized intelligence. Zhang, Goldstein, and Levin (2024) demonstrated that classical sorting algorithms can serve as minimal models of basal cognition and morphogenesis, revealing unexpected competencies in self-organizing systems.

This work extends those ideas into a general-purpose engine framework, where sorting dynamics become the fundamental computational primitive for simulating emergent phenomena across diverse application domains.

## Key Concepts from the Levin et al. Research

### Decentralized Intelligence and Bottom-Up Control
The Levin paper breaks the traditional assumption of top-down control in sorting algorithms. Instead of treating sorting as a centralized process controlled by an external executor, the research reconceptualizes each array element as an autonomous agent with minimal agency.

Each element implements sorting policies from the bottom up through local interactions with neighbors. This demonstrates that complex collective behaviors can emerge from simple, decentralized rules without centralized coordination.

### Robustness Through Autonomous Element Agency
A key finding is that arrays of autonomous elements sort themselves more reliably and robustly than traditional implementations, particularly in the presence of errors or "damaged" elements. This robustness emerges from the distributed nature of the sorting process.

When individual elements possess agency, the system can adapt to failures without catastrophic breakdown. This demonstrates that basal forms of intelligence can provide unexpected resilience in computational systems.

### Delayed Gratification and Problem-Space Navigation
The research quantitatively characterizes sorting activity as traversal through a problem space, revealing that autonomous sorting systems exhibit delayed gratification behavior. Elements can temporarily increase disorder (reduce progress toward sorted state) to navigate around defects or obstacles, then resume progress toward the goal.

This capacity to accept temporary setbacks for long-term gain represents a form of minimal goal-directed behavior. The behavior emerges from the sorting dynamics themselves without explicit programming.

### Emergent Clustering in Chimeric Systems
When arrays contain elements following different sorting algorithms ("chimeric arrays"), unexpected clustering behavior emerges. Elements sort themselves not only by value but also spontaneously organize by algorithm type.

This reveals that the sorting process encodes information about both the target state and the method being used. The emergent pattern formation demonstrates that simple systems can exhibit multiple simultaneous organizational principles.

### Basal Cognition Without Explicit Encoding
The most significant insight is that problem-solving capacities emerge in simple, familiar algorithms without being explicitly encoded in their underlying mechanics. The sorting algorithms, when viewed through the lens of autonomous elements navigating problem spaces, reveal memory-like persistence, decision-making at interaction points, and adaptive responses to perturbations.

This demonstrates that basal forms of intelligence can exist in minimal computational substrates. The finding provides a new perspective on the field of Diverse Intelligence, suggesting that cognitive-like competencies may be far more widespread in simple systems than previously recognized.

## Design Concept

The EDE is implemented in Java and built around the standard java.lang.Comparable interface. Users define domain-specific Cells that implement Comparable, allowing the engine to order and process them using well-understood Java sorting contracts.

The engine provides four built-in sorting algorithm implementations:

- Selection Sort (SELECTION algotype)
- Bubble Sort (BUBBLE algotype)
- Insertion Sort (INSERTION algotype)
- Fibonacci Jump Sort (FIBONACCI algotype) - Novel algotype using Fibonacci-distance viewing for logarithmic neighbor coverage

The Engine API allows users to configure which sorting algorithm is applied to their Cells, making the sorting strategy a tunable parameter of the emergent system.

## User-Facing Components

The EDE exposes two major user-facing components:

### Engine API
The Engine API allows implementors to customize engine parameters and behavior, including selection of the sorting algorithm (SELECTION, BUBBLE, INSERTION, or FIBONACCI algotypes) to be applied during Cell processing. This provides flexibility to adapt the engine to specific emergent phenomena and experimental setups.

### Implementation API
The Implementation API facilitates the creation of domain-specific Cells that implement the java.lang.Comparable interface. This ensures modularity, interoperability, and the ability to compose complex systems from well-defined, sortable components.

For more information and to contribute, visit the project repository at https://github.com/zfifteen/emergent-doom-engine.

## Java Architecture

The EDE translates the theoretical concepts from the Levin et al. research into a practical Java implementation built around autonomous, sortable Cells and configurable sorting dynamics. The architecture is designed to enable emergent behaviors through decentralized bottom-up interactions while providing clean APIs for extensibility.

### Core Components

#### Cell Interface
The foundation of the EDE is the Cell interface, which extends `java.lang.Comparable<Cell>`. Cells are pure data carriers that implement only `compareTo()` for domain-specific comparison logic. All sorting metadata (algotype, sort direction, ideal position) is managed externally via `CellMetadata`, achieving true domain-agnostic sorting where cells contain zero engine-specific state.

#### Execution Engines
Single-trial execution uses `SynchronousExecutionEngine`. Batch-level parallelism is provided by the `ExperimentRunner` in the experiment package:
- **SynchronousExecutionEngine**: Sequential cell evaluation for deterministic, step-by-step execution
- **ExperimentRunner (batch parallelism)**: Coordinates batch execution across trials via `runBatchExperiments()`, replacing the removed per-cell `ParallelExecutionEngine` and `LockBasedExecutionEngine` threading modes in v2.0.

The execution engine coordinates cell swap decisions, applies convergence detection, and records execution trajectories via the Probe interface.

#### CellMetadata
External metadata provider system that associates sorting metadata with cell positions without modifying cell implementations. Each metadata entry specifies:
- **Algotype**: Sorting algorithm (BUBBLE, SELECTION, or INSERTION)
- **SortDirection**: Ordering preference (ASCENDING or DESCENDING)
- **Ideal Position**: Target position for convergence detection

This architecture enables chimeric populations where different cells follow different algorithms and sort in opposite directions.

#### Metrics and Analysis
The metrics package implements quantitative characterization methods from the Levin paper:
- **MonotonicityError**: Disorder remaining at each step (inversion count)
- **SortednessValue**: Progress toward sorted state
- **DelayedGratificationCalculator**: Temporary error increases that lead to long-term gains
- **AggregationValue**: Clustering behavior in chimeric populations

The analysis package provides trajectory visualization and statistical analysis of emergent behaviors.

#### Probe System
Trajectory recording infrastructure that captures:
- Step-by-step snapshots of array state
- Swap counts and comparison operations
- Convergence metrics and timing data
- Domain-specific metadata for post-hoc analysis

Enables detailed examination of emergent problem-solving behaviors without impacting execution performance.

### Key Features

- **Domain Agnostic**: Minimal cell interface requires only `compareTo()` - all domain logic is encapsulated
- **Pure Comparison**: Cells interact only through ordering relationships
- **Emergent Behavior**: Solutions arise from collective dynamics, not programmed algorithms
- **Flexible Topology**: Configurable neighborhood structures and iteration strategies
- **Rich Analysis**: Built-in trajectory recording, metrics, and visualization
- **Chimeric Populations**: Mix multiple cell behaviors in single experiments
- **Frozen Constraints**: Progressive crystallization of partial solutions

## Lightweight Cell Architecture

The EDE achieves true domain-agnostic sorting through a **lightweight cell** design where cells are pure `Comparable` data carriers with zero engine-specific state. All sorting metadata (algorithm type, sort direction, ideal position) is managed externally by execution engines via metadata providers.

### Visual Overview: Separation of Concerns

```
┌─────────────────────────────────────────────────────────────┐
│                    EDE Architecture                          │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────┐              ┌──────────────────────┐   │
│  │ Your Domain    │              │ Execution Engine     │   │
│  │                │              │                      │   │
│  │  Cell          │              │  Metadata Provider   │   │
│  │  ✅ compareTo() │ ◄────────►  │  ✅ Algotype         │   │
│  │  ✅ getValue()  │              │  ✅ SortDirection    │   │
│  │                │              │  ✅ IdealPosition    │   │
│  │  [Pure domain] │              │  [Engine concerns]   │   │
│  └────────────────┘              └──────────────────────┘   │
│         │                                    │               │
│         │                                    │               │
│         ▼                                    ▼               │
│  Domain-specific                    IntFunction<CellMetadata>│
│  comparison logic                   index → metadata         │
│                                                               │
└─────────────────────────────────────────────────────────────┘

Key Benefit: Any Comparable type can be sorted emergently without
             coupling to the EDE framework!
```

### Pure Domain Cells

Cells only implement `compareTo()` - they contain no knowledge of sorting algorithms:

```java
public class MyDomainCell implements Cell<MyDomainCell> {
    private final MyDomainValue value;
    
    @Override
    public int compareTo(MyDomainCell other) {
        return this.value.compareTo(other.value);
    }
}
```

### External Metadata Management

All sorting metadata is provided to the engine via a metadata provider function:

```java
// Define metadata for each cell position
IntFunction<CellMetadata> metadataProvider = index -> 
    new CellMetadata(
        Algotype.BUBBLE,              // sorting algorithm
        SortDirection.ASCENDING       // sort direction
    );

// Create engine with metadata provider
SynchronousExecutionEngine<MyDomainCell> engine = 
    new SynchronousExecutionEngine<>(
        cells, 
        swapEngine, 
        probe, 
        convergenceDetector, 
        metadataProvider  // externally managed metadata
    );

// For parallel execution, run multiple trials concurrently:
// ExperimentRunner#runBatchExperiments(config)
```

### Chimeric Populations

For experiments mixing different algorithms, combine an algotype provider with metadata configuration:

```java
// Create algotype provider (50% BUBBLE, 50% SELECTION)
AlgotypeProvider algotypeProvider = new PercentageAlgotypeProvider(
    Map.of(Algotype.BUBBLE, 0.5, Algotype.SELECTION, 0.5),
    arraySize,
    seed
);

// Build configuration with metadata provider factory
ChimericExperimentConfig config = ChimericExperimentConfig.builder()
    .arraySize(100)
    .maxSteps(5000)
    .algotypeMix(Map.of(Algotype.BUBBLE, 0.5, Algotype.SELECTION, 0.5))
    .sortDirection(SortDirection.ASCENDING)
    .build();

// Create metadata provider from configuration
IntFunction<CellMetadata> metadataProvider = 
    config.createMetadataProvider(algotypeProvider);
```

This architecture achieves **true generality** - any `Comparable` object can be sorted without implementing engine-specific interfaces or carrying sorting metadata.

## Design Principles

1. **Minimal Cell Contract**: Cells only need to be comparable - the engine remains blind to domain semantics
2. **Local Interactions**: Cells swap based on local comparisons, enabling emergence
3. **Topology-Driven**: Neighborhood structure shapes the emergent behavior
4. **Observable Dynamics**: Complete trajectory recording for post-hoc analysis

## Architecture

```
com.emergent.doom
├── cell/               # Cell interface and implementations
├── topology/           # Neighborhood and iteration strategies
├── swap/               # Swap mechanics and frozen cell management
├── probe/              # Execution trajectory recording
├── execution/          # Main engine and convergence detection
├── metrics/            # Quality measures and analysis
├── experiment/         # Multi-trial experiment framework
├── chimeric/           # Mixed-algotype populations
├── analysis/           # Trajectory visualization and analysis
└── examples/           # Example implementations
```

## Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6+

### Build

```bash
mvn clean compile
```

### Run Example

```bash
mvn package
java -jar target/emergent-doom-engine-0.3.0-alpha.jar
```

## Usage Example

```java
// Define target number to factor
BigInteger target = new BigInteger("143"); // 11 × 13
int arraySize = 20;

// Configure experiment
ExperimentConfig config = new ExperimentConfig(
    arraySize,      // number of cells
    1000,           // max steps
    3,              // stable steps for convergence
    true            // record trajectory
);

// Create experiment runner
ExperimentRunner<RemainderCell> runner = new ExperimentRunner<>(
    () -> createCellArray(target, arraySize),    // cell factory
    () -> new LinearNeighborhood<>(1)             // topology factory
);

// Add metrics
runner.addMetric("Monotonicity", new MonotonicityError<>());

// Run multiple trials
ExperimentResults<RemainderCell> results = runner.runExperiment(config, 5);

// Analyze results
System.out.println(results.getSummaryReport());
```

## Core Components

### Cell Interface

The minimal contract that all cells must implement:

```java
public interface Cell<T extends Cell<T>> extends Comparable<T> {
    // Only compareTo() required - inherited from Comparable
}
```

### Topology

Defines neighborhood relationships and iteration order:

```java
public interface Topology<T extends Cell<T>> {
    List<Integer> getNeighbors(int position, int arraySize);
    List<Integer> getIterationOrder(int arraySize);
}
```

### Execution Engine

Orchestrates the cell dynamics using metadata providers:

```java
// Create metadata provider
IntFunction<CellMetadata> metadataProvider = index -> 
    new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);

// Modern execution engines with metadata support
SynchronousExecutionEngine<T> engine = new SynchronousExecutionEngine<>(
    cells,                  // initial cell array
    swapEngine,             // swap mechanics
    probe,                  // trajectory recorder
    convergenceDetector,    // termination criterion
    metadataProvider        // external metadata
);

engine.runUntilConvergence(maxSteps);
```

## Factorization Domain Integration

The included factorization example demonstrates how to apply EDE to number theory:

- **Cell Implementation**: `RemainderCell` stores N mod position
- **Sorting Behavior**: Cells with smaller remainders are "better"
- **Emergent Factorization**: Perfect factors (remainder = 0) naturally migrate to front
- **No Explicit Search**: Factors emerge from comparison-driven swapping

## Metrics

Built-in metrics for analysis:

- **MonotonicityError**: Counts inversions (disorder) in the array
- **DelayedGratificationIndex**: Measures position-weighted quality distribution
- **AggregationValue**: Custom aggregation over cell values

## Frozen Cell Mechanics

Cells can be frozen to stabilize partial solutions:

- **NONE**: Fully mobile
- **MOVABLE**: Can move but cannot be displaced
- **IMMOVABLE**: Completely frozen

## Extending EDE

### Create a Custom Cell Type

```java
public class MyCell implements Cell<MyCell> {
    private final MyDomainData data;
    
    @Override
    public int compareTo(MyCell other) {
        // Domain-specific comparison logic
        return this.quality - other.quality;
    }
}
```

### Create a Custom Topology

```java
public class MyTopology<T extends Cell<T>> implements Topology<T> {
    @Override
    public List<Integer> getNeighbors(int position, int arraySize) {
        // Define custom neighborhood structure
    }
    
    @Override
    public List<Integer> getIterationOrder(int arraySize) {
        // Define custom iteration strategy
    }
}
```

### Create a Custom Metric

```java
public class MyMetric<T extends Cell<T>> implements Metric<T> {
    @Override
    public double compute(T[] cells) {
        // Compute custom quality measure
    }
    
    @Override
    public String getName() {
        return "My Custom Metric";
    }
}
```

## Testing

```bash
mvn test
```

## Documentation

### Project Documentation

Comprehensive documentation is available in the `/docs` directory:

- **[Documentation Index](docs/README.md)** - Complete documentation structure
- **[Theory](docs/theory/2401.05375v1.md)** - Levin et al. paper on sorting as morphogenesis
- **[Findings](docs/findings/README.md)** - Experimental results and analyses
- **[Requirements](docs/requirements/REQUIREMENTS.md)** - Technical specifications

### API Documentation

Generate Javadoc:

```bash
mvn javadoc:javadoc
```

Documentation will be in `target/site/apidocs/`.

## Future Extensions

Potential areas for expansion:

- **Batch Parallel Execution**: Parallelize across trials via `runBatchExperiments()` [IMPLEMENTED]
- **Adaptive Topologies**: Dynamic neighborhood restructuring
- **Hybrid Algotypes**: Automatic mixing of cell strategies
- **Visualization Tools**: Real-time trajectory visualization
- **Domain Libraries**: Pre-built cells for common problems


## References

[1] Zhang, T., Goldstein, A., Levin, M. (2024). "Classical Sorting Algorithms as a Model of Morphogenesis: self-sorting arrays reveal unexpected competencies in a minimal model of basal intelligence." Available at: https://github.com/zfifteen/emergent-doom-engine/blob/main/docs/theory/2401.05375v1.pdf

[2] Emergent Doom Engine - Implementation Repository. https://github.com/zfifteen/emergent-doom-engine
