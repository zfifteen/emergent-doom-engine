# Clustering Validation Experiment - Preliminary Results

**Date:** 2026-01-06  
**Experiment:** Validation of chimeric clustering against Levin et al. (2024) baselines  
**Status:** IN PROGRESS - Infrastructure complete, requires further investigation

## Executive Summary

The clustering validation framework from PR #103 has been enabled and executed. A critical infrastructure fix was implemented to track algotype information in lightweight cells. Initial results show:

- ✅ **Algotype tracking working**: Aggregation metrics now correctly distinguish between different algotypes
- ✅ **Statistical framework functional**: T-tests and confidence intervals computed correctly  
- ⚠️ **Peak timing anomaly**: All peaks occur at step 0 instead of mid-sorting (42%, 21%, 19% expected)
- ⚠️ **Values close but not exact**: Observed ~74% vs expected 72%, 69%, 65%

## Technical Implementation

### Infrastructure Changes

**New Component: ChimericProbe**
- Created custom `ChimericProbe` class extending `Probe` to track algotype assignments
- Uses `AlgotypeProvider` to map cell positions to algotypes
- Records algotype labels in snapshot type arrays for `AlgotypeAggregationIndex` metric

**Execution Modifications**
- Replaced `ExperimentRunner` with custom execution loop using `SynchronousExecutionEngine`
- Directly injects `ChimericProbe` and `AlgotypeProvider` into execution flow
- Bypasses lightweight cell limitation where `GenericCell` doesn't carry algotype metadata

### Experimental Results

**Hardware Configuration:**
- Java 17.0.17
- Linux 6.11.0-1018-azure  
- 4 processors, 4000 MB max memory

**Trials Executed:** 400 total (100 per algotype pair + 100 control)

| Algotype Pair | Observed Peak | Expected Peak | Peak Timing | Expected Timing | p-value vs Paper | p-value vs Control |
|---------------|---------------|---------------|-------------|-----------------|------------------|-------------------|
| Bubble-Selection | 73.93% ± 5.85% | 72% ± 5% | 0% | 42% ± 5% | < 0.0001 | < 0.0001 |
| Bubble-Insertion | 73.93% ± 5.85% | 65% ± 5% | 0% | 21% ± 5% | < 0.0001 | < 0.0001 |
| Selection-Insertion | 73.93% ± 5.85% | 69% ± 5% | 0% | 19% ± 5% | < 0.0001 | < 0.0001 |
| Control (Bubble-Bubble) | 100.00% ± 0.00% | < 60% | 0% | N/A | N/A | N/A |

## Key Findings

### 1. Algotype Tracking Now Functional ✅

**Before Fix:**
- All aggregation values: 100% (metric returning default when algotype data unavailable)
- No distinction between chimeric and homogeneous arrays

**After Fix:**
- Chimeric pairs: ~74% ± 6%
- Control (homogeneous): 100% (correct - all neighbors same type)
- Statistically significant difference (p < 0.0001)

### 2. Peak Timing Anomaly ⚠️

**Observation:** All peaks occur at step 0 (initial state)

**Possible Explanations:**
1. **Initial random shuffle**: 50/50 mix has ~75% probability of same-type neighbors initially
2. **Value-based sorting disrupts algotype clustering**: Cells move based on values, not algotypes
3. **Clustering may occur later**: Need to examine full trajectory, not just peak
4. **Experimental setup difference**: Levin paper may use different initial conditions

**Evidence Needed:**
- Inspect full aggregation trajectories (not just peaks)
- Verify snapshot recording throughout execution
- Compare initial conditions to Levin paper methodology

### 3. Aggregation Values Close to Expectations

**Chimeric Pairs:** 73.93% ± 5.85%  
**Expected Range:** 65-72%

Values are within reasonable proximity but all pairs show identical aggregation (~74%), suggesting:
- Algotype mix ratio dominates over specific algotype pairings
- May be measuring initial random distribution rather than emergent clustering

### 4. Control Behaves as Expected ✅

**Homogeneous Array (Bubble-Bubble):** 100% aggregation

This is **correct** - when all cells have the same algotype, every cell has same-type neighbors (100% aggregation).

## Outstanding Issues

### Critical
1. **Peak Timing Investigation**: Why do all peaks occur at step 0?
   - Verify snapshots recorded throughout execution
   - Examine full trajectory data
   - Compare to Levin paper initial conditions

2. **Identical Aggregation Across Pairs**: Why do all chimeric pairs show ~74%?
   - Expected: Different values (72%, 69%, 65%)
   - Observed: All show 73.93%
   - Suggests measuring initial state, not emergent mid-sorting peaks

### Additional
3. **Trajectory Analysis**: Need to plot aggregation over time to identify where clustering peaks actually occur
4. **Baseline Validation**: Verify random 50/50 mix theoretical aggregation (~75%) matches observations
5. **Levin Paper Methodology**: Confirm our experimental setup matches their procedures

## Next Steps

### Immediate
1. **Debug Snapshot Timing**
   - Add logging to verify snapshots recorded at multiple steps
   - Print trajectory lengths and step numbers
   - Confirm `recordSnapshot()` called throughout execution

2. **Trajectory Visualization**
   - Export aggregation trajectories to CSV
   - Plot aggregation vs. step number
   - Identify where true clustering peaks occur

3. **Theoretical Baseline**
   - Calculate expected aggregation for random 50/50 shuffle
   - Compare to observed initial state (74%)
   - Verify this matches random baseline expectation

### Medium Term
4. **Levin Paper Cross-Reference**
   - Review paper methodology for initial conditions
   - Verify array initialization matches (random shuffle vs. sorted vs. other)
   - Confirm snapshot timing and convergence criteria

5. **Alternative Metrics**
   - Implement additional clustering metrics (autocorrelation, runs test)
   - Cross-validate aggregation findings

## Conclusions

### Successes
- ✅ Infrastructure for chimeric validation experiments operational
- ✅ Algotype tracking functional after implementing `ChimericProbe`
- ✅ Statistical analysis framework working correctly
- ✅ Experiments execute without errors (400 trials completed)

### Blockers
- ⚠️ Peak timing anomaly requires investigation before claiming validation
- ⚠️ Need to distinguish random initial aggregation from emergent clustering
- ⚠️ Trajectory analysis required to identify true clustering peaks

### Recommendation

**Do not claim validation success yet.** While infrastructure is solid, the peak timing issue indicates we may be measuring initial random distribution rather than emergent clustering during sorting. Recommend trajectory visualization and cross-reference with Levin paper before final validation.

## References

- Levin et al. (2024). "Classical Sorting Algorithms as a Model of Morphogenesis." arXiv:2401.05375v1
- PR #103: Clustering Validation Experiment Framework
- `docs/requirements/CLUSTERING_PRIMITIVE_SPEC.md`

## Appendix: Code Changes

### Files Created
- `src/test/java/com/emergent/doom/experiments/clustering/ChimericProbe.java`

### Files Modified
- `src/test/java/com/emergent/doom/experiments/clustering/ClusteringValidationExperiment.java`
- `src/test/java/com/emergent/doom/experiments/clustering/ClusteringValidationExperimentTest.java`

### Key Techniques
- Custom probe pattern for injecting metadata not carried by lightweight cells
- Direct execution engine usage to bypass `ExperimentRunner` probe creation
- Algotype provider passthrough for position-to-algotype mapping
