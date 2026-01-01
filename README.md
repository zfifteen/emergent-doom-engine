# Emergent Doom Engine (EDE)

The Emergent Doom Engine (EDE) is a general-purpose, domain-agnostic API for simulating emergent phenomena. It provides a clean, extensible framework that is not tied to any specific application domain, including prime factorization or game development. The engine is designed around sortable Cell primitives and configurable sorting dynamics to enable modular composition of complex emergent systems. [1]

## Theoretical Background

The EDE concept is inspired by research on sorting algorithms as models of emergent behavior and decentralized intelligence. Zhang, Goldstein, and Levin (2024) demonstrated that classical sorting algorithms can serve as minimal models of basal cognition and morphogenesis, revealing unexpected competencies in self-organizing systems.

This work extends those ideas into a general-purpose engine framework, where sorting dynamics become the fundamental computational primitive for simulating emergent phenomena across diverse application domains.

## Key Concepts from the Levin et al. Research

### Decentralized Intelligence and Bottom-Up Control
The Levin paper breaks the traditional assumption of top-down control in sorting algorithms. Instead of treating sorting as a centralized process controlled by an external executor, the research reconceptualizes each array element as an autonomous agent with minimal agency. Each element implements sorting policies from the bottom up through local interactions with neighbors, demonstrating that complex collective behaviors can emerge from simple, decentralized rules without centralized coordination.

### Robustness Through Autonomous Element Agency
A key finding is that arrays of autonomous elements sort themselves more reliably and robustly than traditional implementations, particularly in the presence of errors or "damaged" elements. This robustness emerges from the distributed nature of the sorting process—when individual elements possess agency, the system can adapt to failures without catastrophic breakdown. This demonstrates that basal forms of intelligence can provide unexpected resilience in computational systems.

### Delayed Gratification and Problem-Space Navigation
The research quantitatively characterizes sorting activity as traversal through a problem space, revealing that autonomous sorting systems exhibit delayed gratification behavior. Elements can temporarily increase disorder (reduce progress toward sorted state) to navigate around defects or obstacles, then resume progress toward the goal. This capacity to accept temporary setbacks for long-term gain represents a form of minimal goal-directed behavior that emerges from the sorting dynamics themselves.

### Emergent Clustering in Chimeric Systems
When arrays contain elements following different sorting algorithms ("chimeric arrays"), unexpected clustering behavior emerges. Elements sort themselves not only by value but also spontaneously organize by algorithm type, revealing that the sorting process encodes information about both the target state and the method being used. This emergent pattern formation demonstrates that simple systems can exhibit multiple simultaneous organizational principles.

### Basal Cognition Without Explicit Encoding
The most significant insight is that problem-solving capacities emerge in simple, familiar algorithms without being explicitly encoded in their underlying mechanics. The sorting algorithms, when viewed through the lens of autonomous elements navigating problem spaces, reveal memory-like persistence, decision-making at interaction points, and adaptive responses to perturbations. This demonstrates that basal forms of intelligence can exist in minimal computational substrates, providing a new perspective on the field of Diverse Intelligence and suggesting that cognitive-like competencies may be far more widespread in simple systems than previously recognized.

## Design Concept

The EDE is implemented in Java and built around the standard java.lang.Comparable interface. Users define domain-specific Cells that implement Comparable, allowing the engine to order and process them using well-understood Java sorting contracts.

The engine provides three built-in sorting algorithm implementations, corresponding to the three algotypes studied in the Levin et al. (2024) research:

- Selection Sort (SELECTION algotype)
- Bubble Sort (BUBBLE algotype)
- Insertion Sort (INSERTION algotype)

The Engine API allows users to configure which sorting algorithm is applied to their Cells, making the sorting strategy a tunable parameter of the emergent system.

## User-Facing Components

The EDE exposes two major user-facing components:

### Engine API
The Engine API allows implementors to customize engine parameters and behavior, including selection of the sorting algorithm (Selection Sort, Bubble Sort, or Insertion Sort) to be applied during Cell processing. This provides flexibility to adapt the engine to specific emergent phenomena and experimental setups.

### Implementation API
The Implementation API facilitates the creation of domain-specific Cells that implement the java.lang.Comparable interface. This ensures modularity, interoperability, and the ability to compose complex systems from well-defined, sortable components.

For more information and to contribute, visit the project repository at https://github.com/zfifteen/emergent-doom-engine.

## Java Architecture

The EDE translates the theoretical concepts from the Levin et al. research into a practical Java implementation built around autonomous, sortable Cells and configurable sorting dynamics. The architecture is designed to enable emergent behaviors through decentralized bottom-up interactions while providing clean APIs for extensibility.

### Core Components

#### Cell Interface
The foundation of the EDE is the Cell interface, which extends java.lang.Comparable<Cell>. Each Cell represents an autonomous agent with minimal agency, analogous to the array elements in the Levin paper. Cells must implement compareTo() to define their natural ordering, enabling them to participate in sorting operations. The Cell interface also defines methods for local state management, neighbor interaction, and damage/error simulation, allowing implementations to model various forms of basal intelligence and adaptive behavior.

#### SortingStrategy
This is an abstract strategy pattern implementation that encapsulates the three core sorting algorithms: SelectionSort, BubbleSort, and InsertionSort. Each strategy interprets Cells as autonomous agents rather than passive data, treating comparisons and swaps as local interactions between neighboring elements. The SortingStrategy interface provides hooks for observing sorting progress, detecting temporary disorder increases (delayed gratification), and tracking problem-space traversal metrics like those defined in the Levin research.

#### CellArray
The CellArray class manages collections of Cells and serves as the primary data structure for emergent phenomena. Unlike traditional arrays, CellArray tracks not only element positions but also interaction histories, clustering patterns, and adaptive responses to perturbations. It provides methods for introducing "damaged" cells, creating chimeric arrays (mixing cells following different strategies), and monitoring emergent organizational patterns. The CellArray automatically instruments sorting operations to collect the problem-space navigation metrics described in the Levin paper.

#### EmergentEngine
This is the top-level orchestration component that ties together the Cell, SortingStrategy, and CellArray abstractions. The EmergentEngine provides the public API for configuring experiments, selecting sorting algorithms, defining initial conditions, and collecting emergent behavior metrics. It implements the pattern of treating each sorting execution as a traversal through problem space, automatically measuring robustness, delayed gratification, clustering behavior, and other competencies identified in the Levin research. The engine supports both single-run simulations and batch experiments for statistical analysis.

#### ProblemSpaceAnalyzer
This component implements the quantitative characterization methods from the Levin paper, measuring metrics such as Monotonicity Error (disorder remaining at each step), Sortedness (progress toward goal state), and Delayed Gratification (temporary error increases that lead to long-term gains). The analyzer treats the sorting process as goal-directed navigation through a problem space, automatically detecting cognitive-like behaviors such as obstacle avoidance, adaptive route-finding, and memory-like state persistence. These measurements provide the empirical grounding for claims about emergent intelligence in minimal substrates.

### Key Features

- **Domain Agnostic**: Minimal cell interface requires only `compareTo()` - all domain logic is encapsulated
- **Pure Comparison**: Cells interact only through ordering relationships
- **Emergent Behavior**: Solutions arise from collective dynamics, not programmed algorithms
- **Flexible Topology**: Configurable neighborhood structures and iteration strategies
- **Rich Analysis**: Built-in trajectory recording, metrics, and visualization
- **Chimeric Populations**: Mix multiple cell behaviors in single experiments
- **Frozen Constraints**: Progressive crystallization of partial solutions

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
java -jar target/emergent-doom-engine-1.0.0-SNAPSHOT.jar
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

Orchestrates the cell dynamics:

```java
ExecutionEngine<T> engine = new ExecutionEngine<>(
    cells,                  // initial cell array
    topology,               // neighborhood strategy
    swapEngine,             // swap mechanics
    probe,                  // trajectory recorder
    convergenceDetector     // termination criterion
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

Generate Javadoc:

```bash
mvn javadoc:javadoc
```

Documentation will be in `target/site/apidocs/`.

## Future Extensions

Potential areas for expansion:

- **Parallel Execution**: Multi-threaded or distributed execution [IMPLEMENTED]
- **Adaptive Topologies**: Dynamic neighborhood restructuring
- **Hybrid Algotypes**: Automatic mixing of cell strategies
- **Visualization Tools**: Real-time trajectory visualization
- **Domain Libraries**: Pre-built cells for common problems


## References

[1] Zhang, T., Goldstein, A., Levin, M. (2024). "Classical Sorting Algorithms as a Model of Morphogenesis: self-sorting arrays reveal unexpected competencies in a minimal model of basal intelligence." Available at: https://github.com/zfifteen/emergent-doom-engine/blob/main/docs/theory/2401.05375v1.pdf

[2] Emergent Doom Engine - Implementation Repository. https://github.com/zfifteen/emergent-doom-engine
