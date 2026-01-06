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
Map<SortingAlgotype, Double> distribution = Map.of(
    SortingAlgotype.BUBBLE, 0.5,
    SortingAlgotype.INSERTION, 0.5
);

SortingCellFactory factory = new SortingCellFactory(42L);
List<AbstractSortingCell> cells = factory.createRandomCells(distribution, arraySize, maxValue);

CellBasedExecutionEngine engine = new CellBasedExecutionEngine();
engine.executeSorting(cells, 10000);

// Result: Cells cluster by algotype WITHOUT explicit clustering code!
// Bubble cells migrate to one region, insertion cells to another.
// Algotypes travel WITH cells during swaps!
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

The EDE translates theoretical principles into a practical Java framework built around cell-based autonomy with Levin-aligned semantics where algotypes are intrinsic cell properties that travel with cells during swaps.

### Foundation: Cell-Based Architecture

**Key Principle:** Algotypes are bound to cell objects, not to array positions. When cells swap, entire objects relocate (value + algotype together), enabling genuine morphogenetic clustering.

#### Core Abstractions

**`AbstractCell<V, A>`** - Domain-agnostic base class parameterized by value type and algotype enum:

```java
public abstract class AbstractCell<V extends Comparable<V>, A extends Enum<A>> 
    implements Comparable<AbstractCell<V, A>> {
    
    // Intrinsic immutable properties (travel with cell)
    public abstract A readAlgotype();
    public abstract V readValue();
    
    // Mutable positional state (updated during swaps)
    public abstract int readCurrentPosition();
    public abstract void updatePositionTo(int newPosition);
    public abstract CellStatus readStatus();
    public abstract void updateStatusTo(CellStatus newStatus);
    
    // Behavioral policy (algotype-specific)
    public abstract boolean shouldMoveGiven(NeighborhoodView<V, A> neighbors);
    public abstract Optional<Integer> calculateTargetPositionGiven(NeighborhoodView<V, A> neighbors);
}
```

**`NeighborhoodView<V, A>`** - Encapsulates neighbor visibility, hiding array access from cells:

```java
// Provides cells with "what they can see" based on algotype rules
NeighborhoodView<Integer, SortingAlgotype> view = 
    new NeighborhoodView<>(cell, position, arraySize, visibleNeighbors, positions);

// Cells query neighbors without knowing array structure
Optional<AbstractCell<Integer, SortingAlgotype>> left = view.getLeftNeighbor();
Optional<AbstractCell<Integer, SortingAlgotype>> right = view.getRightNeighbor();
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

#### Sorting Domain Implementation

**`AbstractSortingCell`** - Main entry point fixing `V=Integer`, `A=SortingAlgotype`:

```java
public abstract class AbstractSortingCell 
    extends AbstractCell<Integer, SortingAlgotype> {
    
    protected final int value;              // Immutable sort key
    protected final SortingAlgotype algotype;  // Immutable behavioral policy
    protected int currentPosition;          // Mutable (updated during swaps)
    protected CellStatus status;            // Mutable (ACTIVE, FREEZE, etc.)
}
```

**Concrete Cell Implementations:**
- **`BubbleSortingCell`** - Random bidirectional movement (50/50 left/right)
- **`SelectionSortingCell`** - Ideal position targeting, increments on denial
- **`InsertionSortingCell`** - Conservative left-only, waits for sorted prefix

### Cell Creation with Embedded Algotypes

**`SortingCellFactory`** - Creates cells with algotypes as intrinsic properties:

```java
SortingCellFactory factory = new SortingCellFactory(42L); // Seeded for reproducibility

// 40% BUBBLE, 30% SELECTION, 30% INSERTION
Map<SortingAlgotype, Double> distribution = Map.of(
    SortingAlgotype.BUBBLE, 0.4,
    SortingAlgotype.SELECTION, 0.3,
    SortingAlgotype.INSERTION, 0.3
);

List<AbstractSortingCell> cells = factory.createRandomCells(distribution, 100, 1000);
// Cells created with algotypes embedded, NOT position-based metadata
```

### Execution Engine: Cell-Based Swapping

**`CellBasedExecutionEngine`** - Simple execution engine (~200 LOC) working with cell objects:

```java
CellBasedExecutionEngine engine = new CellBasedExecutionEngine();
int steps = engine.executeSorting(cells, maxSteps);

// CRITICAL: When cells swap, entire objects relocate
// Swap positions i and j:
AbstractSortingCell temp = cells.get(i);
cells.set(i, cells.get(j));  // Entire object (value + algotype)
cells.set(j, temp);           // moves to new position
cells.get(i).updatePositionTo(i);
cells.get(j).updatePositionTo(j);
```

**Execution Flow:**
1. **Query**: Cell reads its algotype to determine behavior
2. **View**: Engine builds `NeighborhoodView` based on algotype visibility rules
3. **Decide**: Cell evaluates `shouldMoveGiven()` and `calculateTargetPositionGiven()`
4. **Swap**: Engine swaps entire cell objects (algotypes travel with cells!)
5. **Update**: Cells update their position tracking

No cell knows global state. Algotypes relocate WITH cells. Order emerges from local pairwise comparisons with cell-bound behavioral policies.

### Observability: Trajectory Recording

Complete trajectory recording enables analysis of emergent dynamics (future integration with probe system planned):

```java
// Current cell-based execution provides step count and final state
CellBasedExecutionEngine engine = new CellBasedExecutionEngine();
int steps = engine.executeSorting(cells, maxSteps);

// Cells can be inspected at any point
for (AbstractSortingCell cell : cells) {
    System.out.printf("Position %d: value=%d, algotype=%s, status=%s%n",
        cell.readCurrentPosition(),
        cell.readValue(),
        cell.readAlgotype(),
        cell.readStatus());
}
```

Future integration with probe system will enable complete trajectory analysis revealing emergent problem-solving strategies.

## Quickstart: Your First Emergent Sort

This minimal example demonstrates the new cell-based architecture with Levin-aligned semantics:

```java
import com.emergent.doom.cell.*;
import com.emergent.doom.factory.SortingCellFactory;
import java.util.*;

public class QuickStart {
    public static void main(String[] args) {
        // 1. Create cell factory with seeded random for reproducibility
        SortingCellFactory factory = new SortingCellFactory(42L);
        
        // 2. Define algotype distribution (all BUBBLE for simplicity)
        Map<SortingAlgotype, Double> distribution = Map.of(
            SortingAlgotype.BUBBLE, 1.0
        );
        
        // 3. Create cells with embedded algotypes
        // Algotypes are intrinsic properties that travel WITH cells during swaps
        List<AbstractSortingCell> cells = factory.createRandomCells(
            distribution, 
            10,      // array size
            100      // max value
        );
        
        System.out.println("Initial state:");
        printCells(cells);
        
        // 4. Create execution engine and run
        CellBasedExecutionEngine engine = new CellBasedExecutionEngine();
        int steps = engine.executeSorting(cells, 1000);
        
        // 5. Emergent sorting complete!
        System.out.println("\nSorted in " + steps + " steps:");
        printCells(cells);
    }
    
    private static void printCells(List<AbstractSortingCell> cells) {
        System.out.print("[");
        for (int i = 0; i < cells.size(); i++) {
            System.out.print(cells.get(i).readValue());
            if (i < cells.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
    }
}
```

**Expected Output:**
```
Initial state:
[73, 29, 91, 45, 12, 67, 38, 54, 81, 26]

Sorted in 42 steps:
[12, 26, 29, 38, 45, 54, 67, 73, 81, 91]
```

Cells discovered sorted order through local pairwise comparisons. Each cell carries its algotype as an intrinsic property—when cells swap, algotypes relocate WITH them, enabling Levin-style morphogenetic clustering.

### Exploring Further

**Demo application**: Run `NewCellArchitectureDemo` to see algotypes traveling with cells during swaps:
```bash
mvn compile
java -cp target/classes com.emergent.doom.examples.NewCellArchitectureDemo
```

**Test suite**: [Test Suite Documentation](src/test/java/com/emergent/doom/README.md) demonstrates cell contract validation and behavioral testing.

**Different algotypes**: Change distribution to mix `SortingAlgotype.BUBBLE`, `SELECTION`, and `INSERTION` to observe emergent clustering.

**Chimeric populations**: Mix multiple algotypes in one population and observe spontaneous segregation by behavioral strategy.

## Glossary

### Core Concepts

**Doom**: Inevitable convergence toward a target state through repeated local interactions. The term emphasizes inexorable progress, not catastrophe.

**Cell**: Autonomous agent carrying intrinsic properties (value, algotype) that travel together during swaps. Implements `AbstractCell<V, A>` with domain-specific value type and algotype enum.

**Algotype**: Behavioral policy determining swap decisions—BUBBLE (bidirectional adjacent), INSERTION (left-migrating), SELECTION (ideal-position seeking), FIBONACCI (logarithmic viewing). Embedded as immutable property within each cell.

**Neighborhood View**: Encapsulation of visible neighbors based on algotype rules. Hides array access mechanics, preserves "local knowledge only" principle.

**Cell Factory**: Component creating cells with embedded algotypes. `SortingCellFactory` distributes algotypes across population with specified percentages.

**Execution Engine**: `CellBasedExecutionEngine` coordinates cell swaps where entire cell objects (value + algotype) relocate together.

**Convergence**: Equilibrium state where no beneficial swaps remain. Detected when cells reach sorted configuration.

### Advanced Concepts

**Chimeric Population**: Mixed-algotype array enabling emergent clustering studies. Cells spontaneously segregate by behavioral strategy as algotypes travel with cells during sorting.

**Cell Status**: Mutable state controlling swap eligibility—ACTIVE (can initiate/accept), FREEZE (accept only), SLEEP/INACTIVE (no participation).

**Levin-Aligned Semantics**: Architecture where algotypes are intrinsic cell properties that relocate WITH cells during swaps, producing characteristic 18.30% aggregation variance signature observed in morphogenetic experiments.

**Position Tracking**: Mutable state updated after swaps to maintain cell awareness of current array location. Separate from immutable intrinsic properties (value, algotype).

**Swap Eligibility**: Cell capability to participate in swaps based on status. ACTIVE cells initiate and accept; FREEZE cells only accept; others cannot participate.

## Technical Details

### Package Architecture

```
com.emergent.doom
├── cell/               # Cell architecture (AbstractCell, concrete implementations)
│   ├── AbstractCell.java
│   ├── AbstractSortingCell.java
│   ├── BubbleSortingCell.java
│   ├── SelectionSortingCell.java
│   ├── InsertionSortingCell.java
│   ├── SortingAlgotype.java
│   └── NeighborhoodView.java
├── factory/            # Cell creation with embedded algotypes
│   └── SortingCellFactory.java
├── execution/          # Cell-based execution engine
│   └── CellBasedExecutionEngine.java
├── group/              # Cell status management
│   └── CellStatus.java
├── metrics/            # Quality measures and analysis (future integration)
├── experiment/         # Multi-trial experiment framework (future integration)
└── analysis/           # Trajectory visualization (future integration)
```

**Note**: Some packages from previous architecture versions remain for legacy code marked with `.old` extension. Active development uses the cell-based architecture in `cell/`, `factory/`, and updated `execution/` components.

### Execution Modes

**CellBasedExecutionEngine**: Levin-aligned execution where cells carry algotypes as intrinsic properties. Simple ~200 LOC implementation that swaps entire cell objects (value + algotype together).

**Future Integration**: Batch-level parallelism for multiple trials and probe system integration for complete trajectory recording planned in upcoming releases.

### Metrics and Analysis

Quantitative characterization methods from the Levin paper (future integration with cell-based architecture planned):

- **Sortedness tracking**: Monitor progress toward sorted state
- **Clustering analysis**: Measure spatial aggregation in chimeric populations  
- **Delayed gratification detection**: Identify temporary disorder increases enabling long-term gains
- **Convergence metrics**: Track steps to equilibrium across different algotype mixes

Current cell-based architecture provides foundational support for these analyses. Full integration with trajectory recording and statistical summaries planned for future releases.

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
