# Emergent Doom Engine (EDE)

A Java-based domain-agnostic substrate for exploring emergent problem-solving through collective cell dynamics.

## Overview

The Emergent Doom Engine (EDE) is a framework that enables complex problem-solving to emerge from simple local interactions between computational entities called "cells". Rather than encoding explicit solution strategies, EDE allows cells to self-organize through comparison-based swapping dynamics.

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
cd experiments/emergent_doom_engine
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

## Integration with Python Factorization Engine

This Java EDE implementation complements the existing Python emergent factorization engine in this repository. Key integration points:

1. **Comparative Studies**: Run identical experiments in both languages
2. **Performance Analysis**: Compare Java vs Python execution characteristics
3. **Cross-Validation**: Verify emergence patterns across implementations
4. **Hybrid Workflows**: Use Java for compute-intensive trials, Python for analysis

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

- **Parallel Execution**: Multi-threaded or distributed execution
- **Adaptive Topologies**: Dynamic neighborhood restructuring
- **Hybrid Algotypes**: Automatic mixing of cell strategies
- **Visualization Tools**: Real-time trajectory visualization
- **Domain Libraries**: Pre-built cells for common problems

## License

See repository root for license information.

## References

This implementation is based on research into emergent computation and collective problem-solving dynamics. See the main repository documentation for theoretical background and related work.

## Contact

See repository root for contribution guidelines and contact information.
