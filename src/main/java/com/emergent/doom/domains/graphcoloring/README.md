# H1 Validation: Graph 3-Coloring Experiment

## Overview

This package implements a minimal, falsifiable test of hypothesis H1: **"Boundary interfaces between algotypes are computational primitives that carry exploitable structure."**

## Experiment Design

### Domain
- **Problem**: 3-coloring on Erdős-Rényi random graphs
- **Graph Size**: n=20 vertices (primary), n=15 (easier), n=25 (harder)
- **Edge Probability**: p ≈ 0.22-0.28 (creates wide fitness plateaus)

### Fitness Landscape
- **Violations**: Count of edges (u,v) where color[u] == color[v]
- **Plateau Engineering**: Bucketed violations (bucket size = 2)
  - Cells compared first by bucket, then by violations within bucket
  - Creates broad plateaus where many states have similar fitness

### Algotypes (4 Strategies)

1. **GREEDY_REPAIR**: Always fix highest-conflict vertex to best color
2. **MIN_CONFLICT**: Pick random conflicted vertex, recolor to min-conflict
3. **RANDOM_WALK**: Random vertex, random recolor (high mobility)
4. **BACKTRACK_LIGHT**: Small-depth lookahead (3-5 steps)

### Plateau Detection

**Operational Definition**: A tick t is "on plateau" if:
- `bestViol(t) == bestViol(t-W)` where W = 75 ticks
- Optional: `medianViol(t..t-W)` unchanged (±1)

### Boundary Analysis

**Observables**:
- **Aggregation**: % of adjacent pairs sharing same algotype
- **Boundaries**: Indices where `algotype[i] != algotype[i+1]`
- **Mobility Gradient**: `|meanMob(left) - meanMob(right)|` at boundary
  - Mobility = average position change over last 10 ticks

### Offline Recombination

**Boundary-Guided Approach**:
1. Select top boundary (max mobility gradient, low fitness gradient)
2. Pick `parentL` = best among 10 cells left of boundary
3. Pick `parentR` = best among 10 cells right of boundary
4. Crossover: Take top k vertices (by degree) from better parent
5. Apply MIN_CONFLICT repair (2 rounds)

**Controls** (must-have):
1. Baseline: Chimeric population, no recombination
2. Negative: Uniform algotype (labels shuffled, behavior identical)
3. Random cut: Random position instead of boundary
4. Random boundary: Pick boundary at random, not max mobility

## Success Criteria

H1 passes **only if** boundary-guided recombination dominates **all** controls on:
- Mean Δviol per plateau sample
- Solve-rate / improvement-rate (≥1.5× random recombination, ≥20% higher solve rate)

Otherwise, **H1 is falsified** under this domain/mechanism.

## Classes

### Core Domain
- `GraphInstance`: Erdős-Rényi graph generator with adjacency list
- `ColoringState`: Candidate coloring with violations tracking
- `ColoringAlgotype`: Enum of 4 behavioral strategies
- `GraphColoringCell`: EDE cell implementation
- `GraphColoringCellFactory`: Chimeric population factory

### Analysis
- `PlateauDetector`: Operational plateau detection (W=75 ticks)
- `BoundaryAnalyzer`: Mobility/fitness gradient extraction
- `BoundaryGuidedRecombination`: Offline crossover operator

### Experiment
- `H1ValidationDemo`: Demonstration of all components

## Running the Demo

```bash
# Build the project
mvn package -DskipTests

# Run the demo with seed 42
java -cp target/classes:target/dependency/* \
  com.emergent.doom.domains.graphcoloring.H1ValidationDemo 42
```

## Key Design Decisions

1. **Minimal Changes**: All code in isolated `domains/graphcoloring` package
2. **No Engine Modifications**: Reuses existing `AbstractCell` architecture
3. **Offline Recombination**: Cells never see recombination results (keeps EDE substrate pure)
4. **Deterministic**: Seed-controlled for reproducibility
5. **Falsifiable**: Clear success criteria; H1 must dominate controls

## Testing

Run tests:
```bash
mvn test -Dtest="GraphInstanceTest,ColoringStateTest,PlateauDetectorTest"
```

All tests validate:
- Graph generation (determinism, degree calculation)
- Violation counting (valid vs invalid colorings)
- Plateau detection (operational definition)

## References

This implementation follows the experimental design outlined in the H1 validation issue, which proposes testing boundary interfaces as computational primitives through:
- Fitness plateaus (bucketed violations)
- Chimeric populations (mixed algotypes)
- Offline boundary-guided recombination
- Rigorous control experiments

## Next Steps for Full H1 Validation

To run a complete H1 validation experiment:

1. **Multiple Trials**: Run 100+ trials per configuration
2. **Statistical Testing**: Compare boundary-guided vs controls with significance tests
3. **Graph Variety**: Test on n=15, 20, 25 with different edge probabilities
4. **Telemetry Export**: Use TrajectoryBuilder to export per-tick metrics
5. **Analysis Pipeline**: Process exported data to compute improvement rates
6. **Report Results**: Clear verdict on whether H1 is supported or falsified

The current implementation provides all necessary components for this full validation.
