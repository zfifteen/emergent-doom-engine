# Emergent Doom Engine: Deviations from Levin et al. (2024) GAPS Framework

This document outlines deviations between the Java implementation in the Emergent Doom Engine (EDE) and the methods described in \"Classical Sorting Algorithms as a Model of Morphogenesis\" by Zhang, Goldstein, and Levin (arXiv:2401.05375v1). The EDE aims to faithfully implement the core cell-view sorting concepts but introduces adaptations for Java, extensibility, and focus on domain-agnostic design. Deviations are organized by paper section, with references to paper lines/pages where applicable.

## 1. Implementation Platform and Execution Model (Methods, Lines 70-118)
- **Paper**: Python 3.0 implementation using multi-threading (one thread per cell) for parallel, distributed execution. Main thread activates cell threads; each cell independently evaluates and initiates swaps based on local view.
- **EDE Deviation**: Single-threaded Java implementation using sequential iteration over cells in each step (ExecutionEngine.java:73-88). No true parallelism; cells are processed in a fixed order (iterationOrder from topology, default sequential).
- **Rationale/Impact**: Simplifies debugging and resource management in Java; loses emergent behaviors from true concurrency (e.g., race conditions in swaps). Suitable for small arrays but may not capture all biological-like asynchrony. Location: src/main/java/com/emergent/doom/execution/ExecutionEngine.java.

## 2. Cell Structure and Algotype Assignment (Definitions, Lines 52-66; Methods, Lines 118-134)
- **Paper**: Cells have fixed integer values (1-N, unique) and algotype (BUBBLE, INSERTION, SELECTION). In chimeric experiments, algotypes randomly assigned at creation (Lines 191-195).
- **EDE Deviation**: Abstract base classes per algotype (BubbleCell.java, etc.) with baked-in getAlgotype() returning fixed enum. compareTo() abstract for domain-specific values (not fixed int). ChimericPopulation.createPopulation() stubbed; uses AlgotypeProvider interface for assignment (not yet implemented for random mixing).
- **Rationale/Impact**: Enables domain extensibility (e.g., factorization beyond integers); fixed algotype per class simplifies but requires subclassing for chimeras. No runtime algotype changes. Location: src/main/java/com/emergent/doom/cell/*.java; src/main/java/com/emergent/doom/chimeric/ChimericPopulation.java.

## 3. Frozen Cells (Methods, Lines 42-47, 179-184)
- **Paper**: Two types: \"Movable\" Frozen (does not initiate moves but can be moved by others); \"Immovable\" (neither initiates nor participates).
- **EDE Deviation**: FrozenType enum: NONE, MOVABLE (can initiate moves but cannot be displaced), IMMOVABLE (neither). Semantics reversed for MOVABLE: code allows initiation but blocks displacement, opposite to paper's \"lack of initiative\" but \"movable\".
- **Rationale/Impact**: Possible misinterpretation; affects error tolerance experiments (e.g., Bubble sort performance). Easy to fix by swapping canMove/canBeDisplaced logic. Location: src/main/java/com/emergent/doom/swap/FrozenCellStatus.java:78-88; SwapEngine.java:48,80.

## 4. Swap Mechanics and Neighbor Views (Methods, Lines 118-134)
- **Paper**: Decentralized: Each cell thread views neighbors (Bubble: left/right; Insertion: all left; Selection: ideal position) and initiates swaps locally.
- **EDE Deviation**: Centralized in ExecutionEngine: For each cell, get neighbors via Topology classes, check shouldSwapForAlgotype (local rules), then attemptSwap via SwapEngine (frozen check only). No cell-initiated actions; engine decides.
- **Rationale/Impact**: Ensures determinism and avoids concurrency issues; but loses distributed agency. Matches rules (e.g., Bubble swaps if smaller left/larger right; Selection increments idealPos on denial, ExecutionEngine.java:163-173). Location: src/main/java/com/emergent/doom/execution/ExecutionEngine.java:117-178; src/main/java/com/emergent/doom/topology/*.java.

## 5. Probe and Data Recording (Methods, Lines 76-82)
- **Paper**: Probe object records each sorting step; data saved as .npy files for evaluation.
- **EDE Deviation**: In-memory List<StepSnapshot> (step, cells array copy, swaps); no file export. Snapshots include full array state per step.
- **Rationale/Impact**: Simplifies for in-JVM analysis; loses persistent .npy format for NumPy tools. Enables immediate metric computation. Location: src/main/java/com/emergent/doom/probe/Probe.java; StepSnapshot.java.

## 6. Evaluation Metrics (Methods, Lines 134-164)
- **Paper**: Total steps (swaps/comparisons); Monotonicity Error (inversions); Sortedness Value (% monotonic pairs); Delayed Gratification (ΔS after decrease); Aggregation Value (% same-algotype neighbors).
- **EDE Deviation**:
  - MonotonicityError: Implemented as inversion count (matches).
  - DelayedGratificationIndex: Stubbed (0.0); paper requires tracking sortedness peaks/valleys.
  - AggregationValue: Stubbed; assumes extractor but no algotype neighbor % computation.
  - No explicit Sortedness Value or total steps (swaps counted but not comparisons).
- **Rationale/Impact**: Core inversion metric ready; others incomplete, limiting full analysis (e.g., no DG context-sensitivity with frozen cells). Location: src/main/java/com/emergent/doom/metrics/*.java.

## 7. Chimeric Arrays and Aggregation (Results, Lines 191-214)
- **Paper**: Random algotype mixing; emergent aggregation (same-type clustering during sort, peaks ~0.6-0.7); tested with/without duplicates; cross-purposes (increasing vs decreasing).
- **EDE Deviation**: ChimericPopulation stubbed; no random mixing or aggregation metric. No duplicate values or opposite-direction experiments. Assumes unique increasing sort.
- **Rationale/Impact**: Core chimeric support via algotype classes, but no experiments implemented. Misses key emergent behavior validation. Location: src/main/java/com/emergent/doom/chimeric/ChimericPopulation.java; no dedicated cross-purpose config.

## 8. Convergence Detection (Implicit in Methods)
- **Paper**: Stops when Sortedness stable for several time steps.
- **EDE Deviation**: NoSwapConvergence (no swaps for N stable steps); alternative detectors possible via interface.
- **Rationale/Impact**: Proxy for stability; may converge faster/slower than sortedness-based. Location: src/main/java/com/emergent/doom/execution/NoSwapConvergence.java; ConvergenceDetector.java.

## 9. Experiment Evaluation (Methods, Lines 82-92; Results)
- **Paper**: Sorting process evaluation subsystem loads .npy files; 100 repeats; Z/T-tests for significance; efficiency/error tolerance/DG comparisons.
- **EDE Deviation**: ExperimentRunner aggregates means/stddev over trials (no tests); in-memory results, no file loading. No traditional sort baselines.
- **Rationale/Impact**: Basic stats ready; lacks hypothesis testing and traditional comparisons. Focus on cell-view only. Location: src/main/java/com/emergent/doom/experiment/ExperimentRunner.java; ExperimentResults.java.

## 10. Additional EDE Features (Not in Paper)
- Domain extensibility: Abstract compareTo allows non-integer sorts (e.g., factorization in examples/FactorizationExperiment.java).
- Topology abstraction: Separate classes for neighbor logic, easy extension.
- Stubs: Several classes (e.g., DelayedGratificationIndex) marked for implementation.

## Summary of Gaps and Future Work
The EDE captures the core distributed sorting and frozen mechanics but deviates in execution model (sequential vs parallel), frozen semantics (minor reversal), and incomplete metrics/chimeric experiments. These are implementation choices for Java robustness and extensibility, not fundamental changes to Levin concepts. Priority gaps: Implement DG/Aggregation metrics; fix MOVABLE semantics; add parallel execution option; complete chimeric population generation; add traditional baselines and statistical tests.

For alignment with paper, see IMPLEMENTATION_SUMMARY.md.