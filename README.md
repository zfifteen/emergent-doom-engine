[![Java CI with Maven](https://github.com/zfifteen/emergent-doom-engine/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/zfifteen/emergent-doom-engine/actions/workflows/maven.yml)  [![Documentation Link Validation](https://github.com/zfifteen/emergent-doom-engine/actions/workflows/validate-docs-links.yml/badge.svg)](https://github.com/zfifteen/emergent-doom-engine/actions/workflows/validate-docs-links.yml)

# Emergent Doom Engine (EDE)

A general-purpose computational framework for emergent phenomena inspired by biological morphogenesis. The engine demonstrates how complex, goal-directed behaviors arise from simple local interactions without centralized control.

## Abstract

The Emergent Doom Engine implements principles from recent research on sorting algorithms as models of basal intelligence and morphogenesis [1]. Traditional sorting algorithms, when reconceptualized with decentralized autonomous elements operating on unreliable substrates, reveal unexpected competencies including error tolerance, delayed gratification, and emergent clustering. EDE extends these discoveries into a domain-agnostic Java framework where any `Comparable` type can exhibit emergent problem-solving behaviors through local pairwise interactions.

## Theoretical Foundation

### Morphogenesis and Basal Intelligence

Biological development demonstrates remarkable problem-solving capacities through decentralized coordination. Embryonic cells organize into organs without blueprints. Damaged tissues regenerate correct anatomical structures despite starting from scrambled configurations. These systems exhibit **basal cognition**—minimal forms of goal-directed behavior, memory, and adaptive responses—without explicit programming or centralized control.

Zhang, Goldstein, and Levin (2024) demonstrated that classical sorting algorithms, when viewed through the lens of autonomous elements navigating problem spaces, reveal these same cognitive-like competencies [1]. The research breaks two fundamental assumptions:

**Top-down control** → **Bottom-up agency**: Each array element becomes an autonomous agent implementing sorting policies through local interactions rather than following centralized commands.

**Reliable substrate** → **Unreliable substrate**: Elements may be "damaged" and fail to execute moves, simulating biological constraints and testing robustness.

### Key Findings from Levin et al.

The research by Zhang, Goldstein, and Levin quantitatively characterizes sorting as traversal through problem space, revealing unexpected emergent properties:

**Robustness through autonomy**: Arrays of autonomous elements sort themselves more reliably than traditional implementations in the presence of errors. Distributed agency provides unexpected resilience.

**Delayed gratification**: Autonomous sorting systems temporarily increase disorder to navigate around defects, then resume progress toward goals. Elements accept short-term setbacks for long-term gains without explicit programming of this strategy.

**Emergent clustering**: When arrays contain elements following different sorting algorithms ("chimeric arrays"), spontaneous organization by algorithm type emerges. Elements sort not only by value but also cluster by behavioral strategy.

**Basal cognition without encoding**: Problem-solving capacities—memory-like persistence, decision-making at interaction points, adaptive responses to perturbations—emerge in simple algorithms without being explicitly encoded in their mechanics.

### From Biology to Computation

Morphogenesis provides a template for robust, adaptive computation. The Emergent Doom Engine translates these biological principles into software:

- **Cells as autonomous agents**: Minimal `Comparable` data carriers with local decision-making
- **Unreliable substrates**: Frozen cells and stochastic failures simulate biological constraints  
- **Emergent goal-seeking**: Global order arises from local pairwise comparisons without centralized planning
- **Observable dynamics**: Complete trajectory recording reveals problem-solving strategies as they emerge

The term **"doom"** emphasizes inevitability—the system inexorably converges toward target states through repeated local interactions. This is the engine's purpose, not a failure mode.

## Emergent Computation Model

### Decentralized Intelligence

The engine implements computation through autonomous cell interactions rather than centralized algorithms. Each cell:

- Implements only `compareTo()` for domain-specific comparison logic
- Makes local swap decisions based on comparisons with neighbors  
- Operates without knowledge of global array state
- Follows simple behavioral policies (algotypes) externally assigned

Global order emerges from collective local dynamics. No centralized controller orchestrates the solution—cells discover it through repeated pairwise interactions.

### Computational Primitives

Four fundamental algotypes implement sorting policies from the Levin research:

**BUBBLE**: Bidirectional movement based on immediate neighbors (i±1). Cells swap with adjacent positions when locally disordered.

**INSERTION**: Left-only swaps requiring sorted left side. Cells migrate leftward until finding correct position relative to all left neighbors.

**SELECTION**: Target ideal position with incremental convergence. Cells identify and gradually approach their final destination in the sorted array.

**FIBONACCI**: Logarithmic neighbor coverage via Fibonacci-distance viewing. Novel algotype extending traditional algorithms with non-local awareness.

These policies combine local topology (which neighbors are visible) with swap logic (when to propose exchanges). Emergent behaviors arise from algotype interactions in heterogeneous populations.

### Why Use Emergence?

**Trading efficiency for capabilities**: If you just need to sort an array of integers, use `Arrays.sort()`. It's faster, simpler, and battle-tested. The Emergent Doom Engine trades runtime efficiency for unique properties that traditional algorithms cannot provide.

### Emergent Properties

The engine enables four key capabilities discovered in the Levin research:

**1. Robustness on Unreliable Substrates**

Traditional sorting fails when array positions are locked or corrupted. EDE continues working around defects:

```java
// Freeze 30% of array positions (simulate hardware defects)
FrozenCellStatus frozenStatus = new FrozenCellStatus();
frozenStatus.setFrozen(10, FrozenType.IMMOVABLE);
frozenStatus.setFrozen(25, FrozenType.IMMOVABLE);

// Sorting still converges around frozen cells!
SwapEngine<GenericCell> swapEngine = new SwapEngine<>(frozenStatus);
engine.runUntilConvergence(5000);
```

**Applications**: Fault-tolerant distributed systems, self-repairing data structures, computation on degraded hardware.

**2. Emergent Clustering and Organization**

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

**Applications**: Agent-based modeling, swarm intelligence research, self-organizing systems.

**3. Observable Delayed Gratification**

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

**Applications**: Understanding biological development, multi-objective optimization, strategy evolution research.

**4. Domain-Agnostic Framework**

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

**Applications**: Prototyping emergent algorithms for new domains, cross-domain research (biology ↔ computation), educational tool for emergence concepts.

## Implementation Architecture

The EDE translates theoretical principles into a practical Java framework built around three foundational concepts: lightweight cells, external metadata, and emergent execution.

### Foundation: The Cell Interface

Cells are pure `Comparable` data carriers with zero engine-specific state. The minimal contract requires only domain-specific comparison logic:

```java
public interface Cell<T extends Cell<T>> extends Comparable<T> {
    // Only compareTo() required - inherited from Comparable
}
```

Domain implementations encapsulate their own ordering semantics:

```java
// Generic integer cells for basic sorting
public class GenericCell implements Cell<GenericCell> {
    private final int value;
    
    @Override
    public int compareTo(GenericCell other) {
        return Integer.compare(this.value, other.value);
    }
}

// Domain-specific cells for factorization
public class FactorCell implements Cell<FactorCell> {
    private final int remainder;  // N mod position
    
    @Override
    public int compareTo(FactorCell other) {
        return Integer.compare(this.remainder, other.remainder);
    }
}
```

Cells contain no algotype, no sort direction, no ideal position—only domain data and comparison logic. This separation enables true domain-agnostic sorting.

### External Metadata Management

All sorting metadata lives outside cells, provided by external metadata functions. This architecture achieves complete decoupling between domain logic and engine mechanics:

```java
// Metadata specifies: algotype, sort direction, ideal position (optional)
IntFunction<CellMetadata> metadataProvider = index -> 
    new CellMetadata(
        Algotype.BUBBLE,              // behavioral policy
        SortDirection.ASCENDING       // ordering preference
    );
```

For chimeric populations mixing multiple algotypes:

```java
// 50% BUBBLE, 50% INSERTION cells
AlgotypeProvider algotypeProvider = new PercentageAlgotypeProvider(
    Map.of(Algotype.BUBBLE, 0.5, Algotype.INSERTION, 0.5),
    arraySize,
    seed
);

IntFunction<CellMetadata> chimericMetadata = index -> {
    Algotype algotype = Algotype.valueOf(
        algotypeProvider.getAlgotype(index, arraySize)
    );
    return new CellMetadata(algotype, SortDirection.ASCENDING);
};
```

This external metadata pattern enables:
- Same cells usable in different sorting contexts
- Runtime algotype assignment without cell modification  
- Chimeric experiments mixing behavioral strategies
- Complete independence of domain logic from engine infrastructure

### Execution Engine: Orchestrating Emergence

The `SynchronousExecutionEngine` coordinates cell dynamics through a simple evaluation loop:

1. **Evaluate**: Each cell compares itself with neighbors visible from its algotype's topology
2. **Propose**: Cells following their behavioral policy suggest beneficial swaps
3. **Execute**: `SwapEngine` applies proposed swaps, respecting frozen cell constraints
4. **Record**: `Probe` captures array state, swap counts, and metrics at each step
5. **Converge**: `ConvergenceDetector` signals termination when stable state reached

```java
// Create execution engine with external metadata
SynchronousExecutionEngine<GenericCell> engine = 
    new SynchronousExecutionEngine<>(
        cells,                  // cell array
        swapEngine,             // swap mechanics
        probe,                  // trajectory recorder
        convergenceDetector,    // termination criterion
        metadataProvider        // external metadata
    );

// Run until convergence or max steps
int steps = engine.runUntilConvergence(maxSteps);
```

No cell knows the global state. No centralized controller directs the solution. Order emerges from repeated local pairwise comparisons.

### Observability: The Probe System

Complete trajectory recording enables post-hoc analysis of emergent dynamics:

```java
Probe<GenericCell> probe = new Probe<>();

// ... run experiment with probe ...

// Access recorded trajectory
List<GenericCell[]> snapshots = probe.getSnapshots();
long swapCount = probe.getSwapCount();
long comparisonCount = probe.getComparisonCount();

// Analyze delayed gratification
MonotonicityError<GenericCell> metric = new MonotonicityError<>();
for (GenericCell[] snapshot : snapshots) {
    double disorder = metric.compute(snapshot);
    // Track disorder over time - may increase temporarily!
}
```

Probes capture execution without affecting runtime performance. Analysis happens after convergence, revealing emergent problem-solving strategies as they unfold.

## Quickstart: Your First Emergent Sort

This minimal example demonstrates emergent sorting through local cell interactions:

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

Cells discovered the sorted order through 47 steps of local pairwise comparisons. No centralized algorithm directed the solution—global order emerged from bottom-up dynamics.

### Exploring Further

**Test suite**: [Test Suite Documentation](src/test/java/com/emergent/doom/README.md) demonstrates chimeric populations, delayed gratification, clustering, and frozen cell robustness.

**Different algotypes**: Replace `Algotype.BUBBLE` with `SELECTION`, `INSERTION`, or `FIBONACCI` to observe different convergence behaviors.

**Custom domains**: Implement `Cell<YourType>` with domain-specific `compareTo()` logic. Same engine, different problem space.

## Glossary

### Core Concepts

**Doom**: Inevitable convergence toward a target state through repeated local interactions. The term emphasizes inexorable progress, not catastrophe.

**Cell**: Lightweight `Comparable<T>` data carrier implementing only domain-specific comparison logic. Contains zero engine-specific metadata.

**Algotype**: Behavioral policy determining swap decisions—BUBBLE (bidirectional adjacent), INSERTION (left-migrating), SELECTION (ideal-position seeking), FIBONACCI (logarithmic viewing). Assigned externally via metadata providers.

**Metadata Provider**: Function (`IntFunction<CellMetadata>`) supplying algotype, sort direction, and ideal position for each cell position. Enables domain-agnostic cell design.

**Probe**: Trajectory recording infrastructure capturing snapshots, swap counts, and metrics at each step for post-hoc analysis.

**Convergence**: Equilibrium state where no beneficial swaps remain. Detected via heuristics (consecutive zero-swap steps or sortedness thresholds).

**Frozen Cell**: Constrained cell simulating substrate unreliability. IMMOVABLE (cannot move), MOVABLE (passive only), NONE (fully mobile).

### Advanced Concepts

**Chimeric Population**: Mixed-algotype array enabling emergent clustering studies. Cells spontaneously segregate by behavioral strategy.

**Delayed Gratification**: Temporary disorder increases enabling long-term progress. Emerges from sorting dynamics without explicit encoding.

**Substrate**: Computational environment with potential unreliability (frozen cells, stochastic failures) modeling biological constraints.

**Topology**: Neighborhood structure defining cell interactions. Shapes emergent behavior through visibility constraints.

**Monotonicity Error**: Inversion count measuring disorder. Tracks problem-space navigation including temporary setbacks.

## Technical Details

### Package Architecture

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
└── analysis/           # Trajectory visualization and analysis
```

### Execution Modes

**SynchronousExecutionEngine**: Sequential cell evaluation for deterministic, step-by-step execution with complete trajectory visibility.

**ExperimentRunner**: Batch-level parallelism coordinating multiple trials concurrently via `runBatchExperiments()`. Replaces removed per-cell threading modes for 5-10× speedup through eliminated synchronization overhead.

### Metrics and Analysis

Quantitative characterization methods from the Levin paper:

- **MonotonicityError**: Inversion count tracking disorder at each step
- **SortednessValue**: Progress toward sorted state measurement
- **DelayedGratificationCalculator**: Identifies temporary error increases enabling long-term gains
- **AggregationValue**: Clustering behavior analysis in chimeric populations

Analysis package provides trajectory visualization and statistical summaries of emergent behaviors.

## Build and Run

### Prerequisites

- Java 11 or higher
- Maven 3.6+

### Build

```bash
mvn clean compile
```

### Run Tests

```bash
mvn test
```

### Package

```bash
mvn package
java -jar target/emergent-doom-engine-0.3.0-alpha.jar
```

## Example Application: Integer Factorization

The included factorization domain demonstrates emergent problem-solving beyond sorting:

```java
// FactorCell: Remainder-based comparison for integer factorization
public class RemainderCell implements Cell<RemainderCell> {
    private final int remainder;  // N mod position
    
    @Override
    public int compareTo(RemainderCell other) {
        return Integer.compare(this.remainder, other.remainder);
    }
}

// Configure factorization experiment
BigInteger target = new BigInteger("143"); // 11 × 13
ExperimentRunner<RemainderCell> runner = new ExperimentRunner<>(
    () -> createCellArray(target, arraySize),
    () -> new LinearNeighborhood<>(1)
);

// Run experiment
ExperimentResults<RemainderCell> results = runner.runExperiment(config, trials);

// Perfect factors (remainder = 0) emerge at array front through local comparisons
```

Factors emerge from comparison-driven swapping without explicit search. Same engine, different problem domain.

## Extending EDE

### Custom Cell Types

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

### Custom Topologies

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

### Custom Metrics

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

## Documentation

Comprehensive documentation available in `/docs`:

- **[Documentation Index](docs/README.md)** - Complete structure
- **[Theory](docs/theory/2401.05375v1.md)** - Levin et al. paper summary
- **[Findings](docs/findings/README.md)** - Experimental results
- **[Requirements](docs/requirements/REQUIREMENTS.md)** - Technical specifications
- **[Test Suite](src/test/java/com/emergent/doom/README.md)** - Detailed examples

Generate API documentation:

```bash
mvn javadoc:javadoc  # Output: target/site/apidocs/
```

## When to Use EDE

**Use EDE for:**
- Fault tolerance on unreliable substrates
- Emergent pattern detection (clustering, segregation)
- Observable dynamics (delayed gratification, trajectories)
- Cross-domain algorithm research
- Educational demonstrations of emergence

**Use traditional algorithms for:**
- Maximum runtime performance
- Simple, one-off sorting tasks
- Guaranteed O(n log n) complexity
- Production-critical code without experimental tolerance

## Contributing

Visit the project repository: [https://github.com/zfifteen/emergent-doom-engine](https://github.com/zfifteen/emergent-doom-engine)

## References

[1] Zhang, T., Goldstein, A., Levin, M. (2024). "Classical Sorting Algorithms as a Model of Morphogenesis: self-sorting arrays reveal unexpected competencies in a minimal model of basal intelligence." [PDF](https://github.com/zfifteen/emergent-doom-engine/blob/main/docs/theory/2401.05375v1.pdf)
