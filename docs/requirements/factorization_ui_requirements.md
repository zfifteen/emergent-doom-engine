# Factorization Experiment UI Requirements

**Document Version:** 1.0  
**Created:** 2026-01-02  
**Status:** Approved  
**Goal this supports:** Visualize emergent factorization dynamics; enable real-time and post-hoc experiment analysis

---

## 1. Overview

This document captures requirements for a mock UI for the `FactorizationExperiment.java` component of the Emergent Doom Engine. The UI enables visualization of RemainderCell agent dynamics during integer factorization experiments.

### 1.1 Source Reference

- **Implementation:** [`src/main/java/com/emergent/doom/examples/FactorizationExperiment.java`](../../src/main/java/com/emergent/doom/examples/FactorizationExperiment.java)
- **Core entities:** `RemainderCell`, `ExperimentRunner`, `MonotonicityError`, `SortednessValue`

---

## 2. Requirements Elicitation

### 2.1 Initial Request

> "Create a mock UI for my Factorization Experiment"

### 2.2 Clarifying Questions & Answers

| Question | User Response |
| :--- | :--- |
| **Primary user story:** Real-time trial progress, post-experiment results analysis, or both? | **Both** |
| **Which metrics/data should be visible?** (Mean steps, convergence rate, cell state, trajectories) | **Cell state is the most important**, followed by trajectories, then mean steps |
| **UI interaction priorities:** Input targets, adjust config, step-by-step debugging? | **Input target semiprimes with sensible defaults pre-populated**, allowing modification before run |

### 2.3 Agreed Design Direction

> Interactive dashboard with input controls → trial runner → live results + summary stats

---

## 3. Functional Requirements

### 3.1 Configuration Panel

| ID | Requirement | Priority | Default Value |
| :--- | :--- | :--- | :--- |
| FR-CFG-01 | User can input target semiprime number | **Must** | `100099` |
| FR-CFG-02 | User can modify array size | **Must** | `1000` |
| FR-CFG-03 | User can set number of trials | **Must** | `5` |
| FR-CFG-04 | User can set max steps | **Should** | `50000` |
| FR-CFG-05 | User can set stable steps (convergence threshold) | **Should** | `3` |
| FR-CFG-06 | All inputs pre-populated with sensible defaults | **Must** | See above |
| FR-CFG-07 | Inline hints/tooltips explain each parameter | **Should** | — |

### 3.2 Cell State Visualization (Primary)

| ID | Requirement | Priority |
| :--- | :--- | :--- |
| FR-VIZ-01 | Display grid of RemainderCell final states | **Must** |
| FR-VIZ-02 | Color-code cells by remainder value (red = 0/factor found, gradient for others) | **Must** |
| FR-VIZ-03 | Hover tooltip shows cell position and exact remainder value | **Must** |
| FR-VIZ-04 | Visual indicator when factor is discovered (remainder = 0, position > 1) | **Must** |
| FR-VIZ-05 | Support array sizes up to 2000+ cells | **Should** |

### 3.3 Trial Trajectories

| ID | Requirement | Priority |
| :--- | :--- | :--- |
| FR-TRJ-01 | Display trajectory of cell remainder values across steps | **Must** |
| FR-TRJ-02 | Show per-trial results with step counts | **Must** |
| FR-TRJ-03 | Visual success indicator per trial (factor found vs not) | **Must** |
| FR-TRJ-04 | Support step-by-step scrubbing/slider for frame-by-frame inspection | **Should** |

### 3.4 Summary Metrics

| ID | Requirement | Priority |
| :--- | :--- | :--- |
| FR-MET-01 | Display factor found (yes/no) | **Must** |
| FR-MET-02 | Display mean steps to convergence | **Must** |
| FR-MET-03 | Display convergence rate (%) | **Should** |
| FR-MET-04 | Display total trials run | **Should** |

### 3.5 Experiment Control

| ID | Requirement | Priority |
| :--- | :--- | :--- |
| FR-CTL-01 | "Run Experiment" button initiates trials with current config | **Must** |
| FR-CTL-02 | Real-time progress updates during experiment execution | **Should** |
| FR-CTL-03 | Ability to load/view completed experiment results | **Should** |

---

## 4. Non-Functional Requirements

| ID | Requirement | Priority |
| :--- | :--- | :--- |
| NFR-01 | Responsive layout (desktop and tablet) | **Should** |
| NFR-02 | Keyboard accessible (focus rings, proper input semantics) | **Should** |
| NFR-03 | Use EDE project color palette | **Could** |
| NFR-04 | Ready to wire to Java backend via REST or WebSocket | **Must** |

---

## 5. Data Model

### 5.1 RemainderCell State

```java
class RemainderCell {
    BigInteger target;      // The semiprime being factored
    int position;           // Divisor candidate (1 to arraySize)
    BigInteger remainder;   // target % position
}
```

### 5.2 Trial Result

```java
class TrialResult<T> {
    int steps;              // Steps to convergence
    boolean converged;      // Did experiment stabilize?
    T[] finalCells;         // Final cell states
    // trajectory data if recordTrajectory=true
}
```

### 5.3 Factor Found Condition

A factor is discovered when:
```
cell.position > 1 AND cell.remainder == 0
```

---

## 6. UI Component Hierarchy

```
FactorizationDashboard
├── ConfigurationPanel
│   ├── SemiprimeInput (default: 100099)
│   ├── ArraySizeInput (default: 1000)
│   ├── TrialsInput (default: 5)
│   ├── MaxStepsInput (default: 50000)
│   ├── StableStepsInput (default: 3)
│   └── RunButton
├── CellStateGrid [PRIMARY]
│   └── CellTile[] (color-coded, hoverable)
├── TrajectoryPanel
│   ├── TrajectoryDisplay (ASCII or chart)
│   └── StepSlider (optional)
├── TrialResultsList
│   └── TrialCard[] (step count, success indicator)
└── SummaryMetrics
    ├── FactorFoundCard
    ├── MeanStepsCard
    ├── ConvergenceRateCard
    └── TrialsRunCard
```

---

## 7. Color Scheme

| Remainder Value | Color | Meaning |
|-----------------|-------|---------|
| `0` | Red (`#e74c3c`) | **Factor found** |
| Low (1-10% of target) | Orange (`#f39c12`) | Close to factor |
| Medium | Blue (`#3498db`) | Moderate remainder |
| High | Purple (`#9b59b6`) | Far from factor |

---

## 8. Next Steps

| Step | Description | Goal Supported |
|------|-------------|----------------|
| 1 | Wire mock UI to backend API | Move from prototype to live data pipeline |
| 2 | Add step-by-step trajectory slider | Enable morphogenesis-as-computation inspection |
| 3 | Export trial data as JSON | Build feedback loop for experiment refinement |

---

## 9. Traceability

| Requirement Area | EDE Principle |
|------------------|---------------|
| Cell state visualization | Bottom-up, emergent mindset (observe local agent states) |
| Trajectory display | Delayed gratification analysis |
| Factor-found detection | "Doom" as inevitability toward target state |
| Configurable parameters | Experimental requirements for parameter sweeps |

---

## Appendix A: Original FactorizationExperiment.java Defaults

```java
private static final int DEFAULT_SEMIPRIME_COUNT = 1;
private static final int DEFAULT_MIN = 99_000;
private static final int DEFAULT_MAX = 101_000;
private static final long DEFAULT_SEED = 12345L;
private static final int DEFAULT_ARRAY_SIZE = 1000;
```

---

*This document is the canonical reference for Factorization UI requirements. Updates should be versioned and traced to specific goal refinements.*
