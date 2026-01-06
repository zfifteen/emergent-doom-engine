# Experiment Opportunity Analysis
**Date:** 2026-01-06  
**Analyst:** DataGenAgent  
**Purpose:** Identify highest-value experimental data generation opportunity from EDE test suite

---

## Test Suite Categorization

### Category A: Already Data-Rich (Augment with CSV Export)

#### 1. **ChimericPopulationTest** - Chimeric Aggregation & Segregation
**Current State:** Tests verify population creation and algotype distribution  
**Experimental Potential:** HIGH ⭐⭐⭐⭐⭐
- **What it could measure:**
  - Aggregation value over time (% of cells with same-algotype neighbors)
  - Clustering emergence patterns in mixed populations
  - Segregation dynamics across different algotype ratios
  - Comparison: homogeneous (100% single algotype) vs chimeric (50/50, 33/33/33)
- **Why valuable:**
  - **Novel emergent phenomenon:** Spontaneous self-organization in multi-strategy populations
  - **Direct mapping to Levin et al. (2024):** Cellular collective intelligence and organization
  - **Rich time-series data:** Can track aggregation from random initial state to clustered equilibrium
  - **Multiple parameter dimensions:** Array size, algotype mix ratios, frozen cell interference
- **Computational feasibility:** EXCELLENT (current tests run quickly, ~100 array size is reasonable)
- **Scientific gap:** NO existing CSV data on chimeric aggregation trajectories in docs/findings

#### 2. **DelayedGratificationCalculatorTest** - Temporary Setback Analysis
**Current State:** Unit tests verify DG calculation correctness  
**Experimental Potential:** MEDIUM ⭐⭐⭐
- **What it could measure:**
  - DG events per sortedness trajectory
  - Recovery ratios (ΔS_increasing / ΔS_decreasing)
  - Correlation between DG magnitude and final convergence time
- **Why valuable:**
  - Core EDE principle: progress through temporary disorder
  - Already has comprehensive test coverage of edge cases
- **Limitation:** Requires full sorting experiments to generate meaningful trajectories (dependent on ExperimentRunner)

#### 3. **TrajectoryAnalyzerTest** - Post-Hoc Analysis
**Current State:** Tests trajectory computation and convergence detection  
**Experimental Potential:** LOW ⭐⭐
- **What it could measure:** Infrastructure for analyzing experiments, not a source of new data
- **Limitation:** Support tool, not primary experiment driver

#### 4. **ExperimentRunnerBatchTest** - Multi-Trial Orchestration
**Current State:** Tests batch execution with 100+ trials  
**Experimental Potential:** MEDIUM ⭐⭐⭐
- **What it could measure:**
  - Statistical distribution of convergence times
  - Variance in sortedness trajectories across trials
- **Limitation:** Infrastructure tests, lacks specific scientific focus

### Category B: Unit Tests (Extend to Parametric Experiments)

#### 5. **SelectionCellTest** - Selection Sort Behavior
**Current State:** Basic cell functionality tests  
**Experimental Potential:** LOW ⭐
- Already covered by homogeneous population tests in existing experiments

#### 6. **MonotonicityTest / SortednessValueTest** - Metric Validation
**Current State:** Unit tests for metric correctness  
**Experimental Potential:** LOW ⭐
- Metrics are means to an end, not experiments themselves

### Category C: Infrastructure (Skip)
- CellMetadataTest, CellInterfaceTest, LinearScalingValidatorTest - No experimental value

---

## High-Value Opportunity Rankings

### Rank 1: **Chimeric Population Aggregation Experiment** ⭐⭐⭐⭐⭐

**Scientific Value Score: 95/100**

**Rationale:**
1. **Emergent Phenomenon:** Self-organization and clustering in chimeric populations is a CORE EDE principle not yet quantified with time-series data
2. **Direct Framework Relevance:** Maps to Levin et al. (2024) concept of collective problem-solving with diverse strategies
3. **Data Richness:** Can capture multiple metrics simultaneously:
   - Aggregation percentage (same-algotype adjacency)
   - Sortedness convergence rate
   - Clustering position stability
   - Comparative performance vs homogeneous populations
4. **Novel Discovery Potential:** Expected to reveal:
   - Do chimeric populations converge faster or slower than homogeneous?
   - Does aggregation increase monotonically or fluctuate?
   - Optimal algotype mix ratios for fastest convergence?
   - Role of frozen cells in disrupting/enhancing clustering?
5. **No Existing Data:** docs/findings has factorization experiments but NO chimeric aggregation time-series
6. **Computational Tractability:** Array sizes 30-100 will execute in reasonable time
7. **Visualization Ready:** Time-series CSV will support compelling plots of emergent organization

**Experiment Design:**
- **Independent Variables:**
  - Array size: {30, 50, 100}
  - Algotype mix: {100% Bubble (control), 50/50 Bubble/Selection, 33/33/33 Bubble/Selection/Insertion}
  - Frozen cells: {0, 1, 3}
  - Random seeds: {42, 123, 789} for reproducibility
- **Dependent Variables (per step):**
  - Aggregation percentage
  - Sortedness percentage
  - Monotonicity percentage
  - Swap count
  - Step number
- **Expected Output:** 
  - Primary CSV: `chimeric_aggregation_timeseries.csv` (~5000 rows per config × 3 sizes × 3 mixes × 3 frozen × 3 seeds = ~405,000 rows)
  - Metadata JSON: `chimeric_aggregation_metadata.json`

### Rank 2: **Cross-Algotype Performance Comparison** ⭐⭐⭐⭐

**Scientific Value Score: 75/100**

**Rationale:** 
- Compare pure Bubble vs pure Selection vs pure Insertion on identical shuffled arrays
- Measure convergence time, swap efficiency, delayed gratification
- **Limitation:** Less novel than chimeric aggregation (more straightforward comparative study)

### Rank 3: **Delayed Gratification Trajectory Sweep** ⭐⭐⭐

**Scientific Value Score: 65/100**

**Rationale:**
- Systematic study of DG events across array sizes and algotypes
- **Limitation:** Dependent on running full experiments first, less direct emergent phenomenon

---

## FINAL RECOMMENDATION

**Selected Experiment: Chimeric Population Aggregation & Convergence Dynamics**

**Justification:**
1. Addresses fundamental EDE question: "How do mixed-strategy populations self-organize?"
2. No existing quantitative data in repository
3. High discovery potential for unexpected emergent patterns
4. Computationally feasible for immediate execution
5. Produces visualization-ready time-series data
6. Directly tests Levin et al. (2024) collective intelligence hypothesis

**Next Steps:**
1. Create experiment directory: `docs/findings/chimeric-aggregation-exp-001/`
2. Implement `ChimericAggregationDataGenTest.java`
3. Execute experiment and capture CSV time-series
4. Document findings and emergent patterns discovered

---

**Confidence Level:** HIGH  
**Expected Execution Time:** 30-60 minutes (including test implementation and runs)  
**Expected Data Volume:** ~50 MB CSV + metadata JSON  
**Expected Discovery:** Quantitative evidence of spontaneous algotype clustering and its impact on convergence
