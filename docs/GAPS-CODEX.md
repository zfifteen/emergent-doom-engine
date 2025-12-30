# GAPS-COPILOT: Levin Paper vs. emergent-doom-engine

Paper: Zhang, T., Goldstein, A., & Levin, M. (2024) "Classical Sorting Algorithms as a Model of Morphogenesis"
Repo: /Users/velocityworks/IdeaProjects/emergent-doom-engine
Analysis date: 2025-12-30

This document lists **all observed deviations** between the Levin paper's methods and the current Java implementation.

Legend:
- MISSING: described in the paper, not implemented
- STUB: code exists but returns placeholder values
- PARTIAL: present but incomplete
- DEVIATION: implemented differently than the paper

**Execution Model**
1. DEVIATION: Cell-view policies are executed by a centralized controller, not by per-cell threads; cells are passive objects with no per-cell execution. Evidence: `src/main/java/com/emergent/doom/execution/ExecutionEngine.java:72`.
2. DEVIATION: Updates are sequential and in-place, not parallel "all cells move each step"; swaps occur during the sweep and immediately mutate the array. Evidence: `src/main/java/com/emergent/doom/execution/ExecutionEngine.java:80`.
3. DEVIATION: A fixed left-to-right iteration order is used; no parallel step or randomized scheduling is implemented. Evidence: `src/main/java/com/emergent/doom/topology/BubbleTopology.java:19`.
4. DEVIATION: Each step is a full sweep of the array, while the paper defines a step as each comparison or swap. Evidence: `src/main/java/com/emergent/doom/execution/ExecutionEngine.java:72`.
5. DEVIATION: Comparisons are not counted at all; only swaps are tracked, so the paper’s “comparison + swap cost” cannot be reproduced. Evidence: `src/main/java/com/emergent/doom/probe/StepSnapshot.java:18`.

**Algorithm Semantics**
6. PARTIAL: Cell-view Insertion Sort does not model “view all cells to the left”; it only exposes the immediate left neighbor and relies on a global sorted-prefix check. Evidence: `src/main/java/com/emergent/doom/topology/InsertionTopology.java:11` and `src/main/java/com/emergent/doom/execution/ExecutionEngine.java:105`.
7. DEVIATION: Cell-view Selection Sort increments `idealPos` only when comparison fails; it does not adjust for swaps blocked by frozen-cell constraints, which can stall behind frozen cells. Evidence: `src/main/java/com/emergent/doom/execution/ExecutionEngine.java:162` and `src/main/java/com/emergent/doom/swap/SwapEngine.java:46`.
8. DEVIATION: A cell can be swapped multiple times during a single step because neighbors are processed sequentially after in-place swaps. This differs from the paper’s parallel step model. Evidence: `src/main/java/com/emergent/doom/execution/ExecutionEngine.java:80`.

**Frozen Cell Model**
9. DEVIATION: “Movable Frozen Cell” semantics are reversed. The paper: cannot initiate moves but can be displaced. Code: MOVABLE can move but cannot be displaced. Evidence: `src/main/java/com/emergent/doom/swap/FrozenCellStatus.java:10` and `src/main/java/com/emergent/doom/swap/FrozenCellStatus.java:78`.
10. DEVIATION: Frozen status is tied to positions, not cells; when cells swap, the frozen state stays at the index. Paper treats frozen cells as damaged elements. Evidence: `src/main/java/com/emergent/doom/swap/FrozenCellStatus.java:41`.
11. MISSING: No experiment configuration or random assignment of frozen cells per trial; experiments always run with no frozen cells unless manually set. Evidence: `src/main/java/com/emergent/doom/experiment/ExperimentRunner.java:60`.

**Metrics and Definitions**
12. MISSING: Sortedness Value metric is not implemented. Evidence: no `SortednessValue` in `src/main/java/com/emergent/doom/metrics/`.
13. DEVIATION: Monotonicity Error is implemented as inversion count, not the paper’s per-cell monotonicity error definition. Evidence: `src/main/java/com/emergent/doom/metrics/MonotonicityError.java:8`.
14. STUB + DEVIATION: Delayed Gratification is stubbed and its docstring defines a different, position-weighted metric rather than the paper’s trajectory-based ΔS_increasing/ΔS_decreasing ratio. Evidence: `src/main/java/com/emergent/doom/metrics/DelayedGratificationIndex.java:8` and `src/main/java/com/emergent/doom/metrics/DelayedGratificationIndex.java:28`.
15. STUB + DEVIATION: Aggregation Value is stubbed and is defined as a generic value aggregate, not “adjacent same-Algotype clustering” used in the paper. Evidence: `src/main/java/com/emergent/doom/metrics/AggregationValue.java:6` and `src/main/java/com/emergent/doom/metrics/AggregationValue.java:46`.
16. MISSING: Metrics are only computed on the final state; there is no Sortedness or Monotonicity trajectory computation used in the paper’s figures. Evidence: `src/main/java/com/emergent/doom/experiment/ExperimentRunner.java:79` and `src/main/java/com/emergent/doom/analysis/TrajectoryAnalyzer.java:28`.

**Trajectory and Output**
17. STUB: TrajectoryAnalyzer does not compute metric trajectories, swap timelines, or convergence step. Evidence: `src/main/java/com/emergent/doom/analysis/TrajectoryAnalyzer.java:28`.
18. MISSING: Probe data are not exported to .npy (or any file format) as in the paper’s evaluation pipeline. Evidence: no export code under `src/main/java/com/emergent/doom/probe/`.

**Experiments and Statistics**
19. MISSING: Traditional (top-down) Bubble/Insertion/Selection sorts are not implemented for baseline comparisons. Evidence: no traditional sort engine in `src/main/java/com/emergent/doom/`.
20. MISSING: Z-test and T-test statistical analysis are not implemented. Evidence: no statistical utilities in `src/main/java/com/emergent/doom/`.
21. DEVIATION: Convergence is defined as “no swaps for N steps,” while the paper’s chimeric experiments stop when Sortedness stops changing. Evidence: `src/main/java/com/emergent/doom/execution/NoSwapConvergence.java:10`.
22. DEVIATION: Experiment sizes and replicates are not aligned with the paper (e.g., 100 elements, N=100 trials); the framework does not enforce these defaults. Evidence: `src/main/java/com/emergent/doom/examples/BubbleSortTest.java:29` and `src/main/java/com/emergent/doom/examples/FactorizationExperiment.java:40`.

**Chimeric and Mixed-Algotype Experiments**
23. STUB: Chimeric population creation and algotype counting are unimplemented. Evidence: `src/main/java/com/emergent/doom/chimeric/ChimericPopulation.java:44` and `src/main/java/com/emergent/doom/chimeric/ChimericPopulation.java:64`.
24. MISSING: No support for mixed-goal experiments (increasing vs decreasing order per algotype); there is no per-cell sort-direction attribute. Evidence: `src/main/java/com/emergent/doom/cell/Cell.java:29`.
25. MISSING: Experiments for duplicate-value arrays used to study sustained aggregation are not implemented in examples or experiment configs. Evidence: no such experiment code under `src/main/java/com/emergent/doom/examples/`.
26. MISSING: Aggregation-over-time analysis of mixed algotypes (Figure 8) is not implemented because AggregationValue and trajectory analysis are missing. Evidence: `src/main/java/com/emergent/doom/metrics/AggregationValue.java:46` and `src/main/java/com/emergent/doom/analysis/TrajectoryAnalyzer.java:28`.

**Problem Setup Differences**
27. DEVIATION: The codebase is domain-agnostic and includes a factorization demo instead of the paper’s randomized integer-array sorting experiments. Evidence: `src/main/java/com/emergent/doom/examples/FactorizationExperiment.java:20`.
28. MISSING: No built-in generator for randomized integer arrays with unique or duplicated values as used in the paper’s experiments. Evidence: no generator in `src/main/java/com/emergent/doom/`.

**Notes**
- This list is based on the paper sections: Definitions, Methods (execution + evaluation), Metrics, Results (comparative experiments), and Chimeric experiments.
- Some deviations are architectural (central controller, sequential updates) and may materially change the emergent behaviors reported in the paper.
