# Chapter 5: Integration Validation

Integration validation tests provide **system-level verification** that all components work correctly together. They validate correctness, performance scaling, and emergent phenomena across the full EDE stack.

## Purpose

Tests in this package validate:
- End-to-end sorting workflows from cell creation to convergence
- Linear time scaling hypothesis for factorization
- Integration between execution engines, probes, metrics, and convergence detectors
- Performance characteristics at various scales (n = 1K to 1M)
- Robustness under stress (large arrays, long runs)

## Concepts Covered

### Linear Scaling Hypothesis

The **critical research question**: Does emergent factorization exhibit O(n) time complexity?

- **Hypothesis**: Convergence steps remain constant as array size grows
- **Validation**: Run experiments on progressively harder semiprimes (10⁶ to 10¹⁸)
- **Metric**: B = ∂steps/∂array_size (should ≈ 0 for linear scaling)

From [LINEAR_SCALING_ANALYSIS.md](../../../../../../../docs/findings/LINEAR_SCALING_ANALYSIS.md):
> "Rigorous testing across array sizes from 1000 to 4000 elements revealed unexpected linear time complexity O(n)."

### Scaling Stages

Validation proceeds through difficulty stages:

1. **Stage 1 (10⁶)**: Easy semiprimes, baseline behavior
2. **Stage 2 (10¹²)**: Moderate difficulty, scaling verification
3. **Stage 3 (10¹⁵)**: Hard semiprimes, stress testing
4. **Stage 4 (10¹⁸)**: Cryptographically relevant, ultimate test

### System Integration Points

Tests verify integration across:
- Cell ↔ Metadata (lightweight architecture)
- Engine ↔ SwapEngine (swap execution)
- Engine ↔ Probe (trajectory recording)
- Engine ↔ ConvergenceDetector (termination logic)
- Metrics ↔ Probe (snapshot analysis)

## Prerequisites

**Required:**
- [Chapter 3: Execution Engines](../execution/README.md) - Full engine setup
- [Chapter 2: Metrics](../metrics/README.md) - Sortedness and convergence
- [Chapter 1: Cell Foundations](../cell/README.md) - Cell implementations

**Helpful:**
- Understanding of linear time complexity
- [LINEAR_SCALING_ANALYSIS.md](../../../../../../../docs/findings/LINEAR_SCALING_ANALYSIS.md) - Scaling research findings
- Prime factorization domain knowledge

## Test Files

### LinearScalingValidatorTest.java

Tests the experimental infrastructure for validating linear scaling hypothesis.

**Test Categories:**

1. **ScalingStage Configuration**
   - Stage 1 (10⁶) magnitude and array sizes
   - Stage 4 (10¹⁸) higher step limits
   - Progression of difficulty

2. **Target Generation**
   - Prime number generation (`nextPrime`)
   - Semiprime construction
   - Magnitude verification

3. **Scaling Metric Calculation**
   - B coefficient estimation
   - Linear regression on (array_size, steps) data
   - Statistical significance testing

**Key Tests:**
- Stage 1 targets 10⁶ magnitude semiprimes
- Array sizes span 2 orders of magnitude [10⁴, 10⁵, 10⁶]
- Harder stages allow more convergence steps
- Prime generation produces valid primes

**Link to source:** [LinearScalingValidator.java](../../../../../../src/main/java/com/emergent/doom/validation/LinearScalingValidator.java)

## Usage Examples

### Running Linear Scaling Validation

Test the O(n) hypothesis:

```java
// Configure Stage 1 (baseline)
ScalingStage stage = ScalingStage.STAGE_1_E6;
int[] arraySizes = stage.getArraySizes(); // [10000, 100000, 1000000]
int maxSteps = stage.getMaxSteps();

// Generate target semiprime
BigInteger target = generateSemiprime(stage.getTargetMagnitude());
System.out.println("Target semiprime: " + target);

// Run experiments at each array size
Map<Integer, Integer> results = new HashMap<>();

for (int n : arraySizes) {
    IntCell[] cells = createFactorizationCells(target, n);
    SynchronousExecutionEngine<IntCell> engine = createEngine(cells);
    
    int steps = engine.runUntilConvergence(maxSteps);
    results.put(n, steps);
    
    System.out.printf("n=%d: converged in %d steps%n", n, steps);
}

// Calculate B coefficient (slope of regression line)
double B = calculateScalingCoefficient(results);
System.out.printf("Scaling coefficient B = %.6f%n", B);

if (Math.abs(B) < 0.0001) {
    System.out.println("✅ Linear scaling confirmed (B ≈ 0)");
} else {
    System.out.println("⚠️  Non-linear behavior detected");
}
```

**Expected output (for easy semiprimes):**
```
n=10000: converged in 135 steps
n=100000: converged in 138 steps
n=1000000: converged in 140 steps
Scaling coefficient B = 0.000005
✅ Linear scaling confirmed (B ≈ 0)
```

### Progressive Difficulty Testing

Validate scaling across stages:

```java
for (ScalingStage stage : ScalingStage.values()) {
    System.out.println("Testing " + stage + " (10^" + 
                       stage.getTargetMagnitude() + ")");
    
    BigInteger target = generateSemiprime(stage.getTargetMagnitude());
    Map<Integer, Integer> results = runScalingExperiments(stage, target);
    
    double B = calculateScalingCoefficient(results);
    System.out.printf("  B = %.6f%n", B);
    
    if (Math.abs(B) > 0.01) {
        System.out.println("  ⚠️  Scaling degraded - may need more steps");
    }
}
```

### End-to-End Integration Test

Validate full pipeline:

```java
// 1. Create cells
IntCell[] cells = createRandomCells(100);

// 2. Create infrastructure
FrozenCellStatus frozenStatus = new FrozenCellStatus();
SwapEngine<IntCell> swapEngine = new SwapEngine<>(frozenStatus);
Probe<IntCell> probe = new Probe<>();
probe.setRecordingEnabled(true);
ConvergenceDetector<IntCell> convergence = new NoSwapConvergence<>(10);
IntFunction<CellMetadata> metadata = index -> 
    new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);

// 3. Create and run engine
SynchronousExecutionEngine<IntCell> engine = 
    new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                     convergence, metadata);
int steps = engine.runUntilConvergence(5000);

// 4. Validate results
assertTrue(engine.hasConverged(), "Engine should converge");
assertTrue(isSorted(cells), "Array should be sorted");

// 5. Analyze trajectory
TrajectoryAnalyzer<IntCell> analyzer = new TrajectoryAnalyzer<>(probe);
List<Double> sortedness = analyzer.computeSortednessTrajectory();
assertEquals(100.0, sortedness.get(sortedness.size() - 1), 0.01,
             "Final sortedness should be 100%");

// 6. Verify metrics
assertTrue(probe.getCompareAndSwapCount() > 0, "Should have performed swaps");
assertTrue(probe.getSnapshotCount() > 0, "Should have recorded snapshots");

System.out.println("✅ Full integration test passed");
```

### Stress Testing Large Arrays

Validate robustness at scale:

```java
int[] sizes = {1000, 5000, 10000, 50000, 100000};

for (int n : sizes) {
    IntCell[] cells = createRandomCells(n);
    
    long startTime = System.currentTimeMillis();
    SynchronousExecutionEngine<IntCell> engine = createEngine(cells);
    int steps = engine.runUntilConvergence(n * 10); // Generous limit
    long elapsed = System.currentTimeMillis() - startTime;
    
    boolean sorted = isSorted(cells);
    
    System.out.printf("n=%d: %d steps, %dms, sorted=%b%n",
                      n, steps, elapsed, sorted);
    
    assertTrue(sorted, "Should sort correctly at n=" + n);
}
```

## Architecture Insights

### Scaling Stages Design

Stages represent increasing factorization difficulty:

```java
public enum ScalingStage {
    STAGE_1_E6(6, new int[]{10000, 100000, 1000000}, 10000),
    STAGE_2_E12(12, new int[]{10000, 100000, 1000000}, 50000),
    STAGE_3_E15(15, new int[]{10000, 100000, 1000000}, 100000),
    STAGE_4_E18(18, new int[]{10000, 100000, 1000000}, 500000);
    
    private final int targetMagnitude;    // 10^N
    private final int[] arraySizes;       // Search space sizes
    private final int maxSteps;           // Convergence timeout
}
```

**Why this matters:**
- Harder targets may require more steps (higher maxSteps)
- Array sizes remain constant to isolate scaling behavior
- Progressive difficulty tests hypothesis robustness

### Linear Scaling Metric (B Coefficient)

B quantifies scaling behavior via linear regression:

```
steps = A + B × array_size

Where:
- A = intercept (base complexity)
- B = slope (scaling coefficient)
- B ≈ 0 → linear time (steps independent of array size)
- B > 0 → superlinear time (steps grow with array size)
```

**Implementation:**
```java
double calculateScalingCoefficient(Map<Integer, Integer> sizeToSteps) {
    // Extract x (array sizes) and y (steps)
    double[] x = sizeToSteps.keySet().stream().mapToDouble(i -> i).toArray();
    double[] y = sizeToSteps.values().stream().mapToDouble(i -> i).toArray();
    
    // Linear regression: y = A + Bx
    double meanX = Arrays.stream(x).average().orElse(0);
    double meanY = Arrays.stream(y).average().orElse(0);
    
    double numerator = 0, denominator = 0;
    for (int i = 0; i < x.length; i++) {
        numerator += (x[i] - meanX) * (y[i] - meanY);
        denominator += (x[i] - meanX) * (x[i] - meanX);
    }
    
    return numerator / denominator; // Slope B
}
```

### Integration Test Philosophy

Integration tests focus on **realistic workflows**, not isolated units:

- ✅ Create cells, setup engine, run to convergence, validate sorted
- ❌ Mock components or test individual methods

**Why:** Catches issues at component boundaries that unit tests miss.

## Common Patterns

### Convergence Timeout Calculation

Choose appropriate max steps:

```java
// Rule of thumb: maxSteps = arraySize × factor
// - Easy targets: factor = 5-10
// - Hard targets: factor = 50-100

int conservativeMaxSteps = arraySize * 10;
int aggressiveMaxSteps = arraySize * 50;

// Prefer conservative for fast feedback
int steps = engine.runUntilConvergence(conservativeMaxSteps);

if (!engine.hasConverged()) {
    // Retry with aggressive limit
    engine.reset();
    steps = engine.runUntilConvergence(aggressiveMaxSteps);
}
```

### Statistical Significance Testing

Determine if B ≈ 0 is statistically significant:

```java
// Run multiple trials to get distribution of B
List<Double> bCoefficients = new ArrayList<>();

for (int trial = 0; trial < 100; trial++) {
    Map<Integer, Integer> results = runScalingExperiments(stage, target);
    double B = calculateScalingCoefficient(results);
    bCoefficients.add(B);
}

// Compute statistics
double meanB = bCoefficients.stream().mapToDouble(d -> d).average().orElse(0);
double stdDevB = computeStdDev(bCoefficients);

System.out.printf("B = %.6f ± %.6f%n", meanB, stdDevB);

// 95% confidence interval
double margin = 1.96 * stdDevB;
if (Math.abs(meanB) < margin) {
    System.out.println("✅ Linear scaling confirmed with 95% confidence");
}
```

## Troubleshooting

### "Validation fails on hard semiprimes"

**Problem:** Stage 3/4 don't converge within max steps.

**Solutions:**
1. Increase `maxSteps` for harder stages
2. Verify target semiprime is actually factorable
3. Check if it's a convergence issue or implementation bug

**Debug:**
```java
// Monitor progress
for (int step = 0; step < maxSteps; step += 100) {
    for (int i = 0; i < 100; i++) engine.step();
    double sortedness = new SortednessValue<>().compute(cells);
    System.out.printf("Step %d: %.1f%% sorted%n", step, sortedness);
}
```

### "B coefficient is negative"

**Problem:** Steps decrease as array size increases (unexpected).

**Possible causes:**
1. Insufficient trials (statistical noise)
2. Convergence timeout hit on smaller arrays
3. Bug in scaling experiment setup

**Verify:** Ensure all array sizes actually converged.

### "Integration test times out"

**Problem:** Test takes too long or hangs.

**Check:**
1. Is array size reasonable? (Use n ≤ 1000 for fast tests)
2. Is max steps set appropriately?
3. Are cells actually sortable?

**Mitigation:** Use smaller arrays and shorter timeouts for unit tests.

## Next Steps

Now that you understand integration validation, proceed to:

**[Chapter 5: Visualization Tools](../visualization/README.md)** - Learn how to render and display trajectory data.

**Also see:**
- [LINEAR_SCALING_ANALYSIS.md](../../../../../../../docs/findings/LINEAR_SCALING_ANALYSIS.md) - Complete scaling analysis
- [Chapter 4: Experiment Framework](../experiment/README.md) - Multi-trial experiment design

---

**[← Back: Traditional Algorithms](../traditional/README.md)** | **[↑ Test Suite Home](../README.md)** | **[Next: Visualization Tools →](../visualization/README.md)**
