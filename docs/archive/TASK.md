# Phase 1: Semantic Realignment for Clustering vs Fitness Experiment v2

## Overview

This PR addresses critical issues identified in the clustering vs fitness experiment (PR #166) by realigning the experimental semantics with Levin's framework and avoiding task-specific overfitting.

## Reference

For detailed audit findings and complete technical analysis, see:

**[experiments/clustering_vs_fitness_experiment_2026_01_10/EXPERIMENT_SETUP_AUDIT.md](https://github.com/zfifteen/emergent-doom-engine/blob/main/experiments/clustering_vs_fitness_experiment_2026_01_10/EXPERIMENT_SETUP_AUDIT.md)**

## Critical Issues from v1 Experiment

### 1. Circular Reasoning
- **Problem**: Aggregation metric measures "% cells with same-strategy neighbor" while experiment manipulates strategy grouping
- **Fix**: Define clustering as **fitness-similarity adjacency**, not strategy labels

### 2. Missing Factors Bug  
- **Problem**: Factor 13 frequently absent from candidate pools
- **Fix**: Guarantee both factors (11, 13) present in ALL runs (C1-C3)

### 3. Conceptual Confusion
- **Problem**: C5 has 100% aggregation by definition, not by spatial clustering
- **Fix**: Redesign C5 to test fitness-based clustering while maintaining strategy diversity

### 4. Statistical Interpretation Errors
- **Problem**: Results marked as "supported" actually falsify hypothesis
- **Fix**: Correct statistical annotations to match findings

### 5. Convergence Criterion Mismatch
- **Problem**: Tests "front positioning" but hypothesis discusses "localization"
- **Fix**: Clarify positions ([0,3] vs [0,4]), add stagnation detection

## Tasks

### Phase 1: Semantic Realignment (This PR)

- [ ] Align terminology with Levin paper
  - Define **localization** as concentration of high-fitness configurations in morphospace
  - Define **clustering** as spatial aggregation of similar fitness levels
- [ ] Implement new fitness-field clustering metrics
  - Moran's I or fitness-similarity adjacency metric
  - Localization index for factors (inter-factor distance, neighborhood fitness)
- [ ] Rename old metric to `strategy_aggregation_v1` for comparison
- [ ] Update documentation to use Levin-consistent terminology

### Phase 2: Correctness Fixes (Follow-up PR)

- [ ] Fix candidate generation to guarantee factors present
- [ ] Clarify convergence positions and create shared constant
- [ ] Add stagnation detection (zero swaps for X steps)
- [ ] Extend step limit or prove current limit sufficient
- [ ] Fix statistical annotation errors

### Phase 3: v2 Experiment (Follow-up PR)

- [ ] Design v2 conditions in terms of **fitness structure**
- [ ] Keep task narrow (factorization) but concepts general
- [ ] Re-run with corrected metrics and semantics
- [ ] Compare v1 vs v2 results

## Why This Matters

Per Levin's framework, pattern formation and localization should be substrate-independent. Hard-wiring task-specific definitions risks:

1. **Overfitting**: Future experiments need new bespoke definitions
2. **Theory fragmentation**: Can't interpret experiments as part of coherent theory  
3. **Artifact risk**: "Discoveries" may be boundary condition artifacts

Keeping definitions at the **fitness-field level** enables multi-CP experiments with unified semantics.
