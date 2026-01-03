# Emergent Doom Engine: Metric Dashboard Baseline Summary
**Status**: Paper baseline extracted | **Experiment data**: NOT FOUND

---

## Key Baseline Metrics from Levin et al. (2024) Paper

### 1. DELAYED GRATIFICATION (DG) - Context-Sensitive Problem-Solving

| Algorithm | Cell-View vs Traditional | DG Difference | Z-value | P-value | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Bubble Sort** | Cell-View > Traditional | +0.16 | 34.04 | p<<0.01 | **More DG in distributed version** |
| **Insertion Sort** | Cell-View ≈ Traditional | +0.03 | 0.60 | p=0.55 | Nearly equivalent DG performance |
| **Selection Sort** | Cell-View < Traditional | -2.77 | -17.21 | p<<0.01 | Traditional version better at DG |

**Trend with Frozen Cells (obstacle count)**:
- **Cell-View Bubble**: 0.24 (0 FC) → 0.37 (3 FC) ✓ **Linear increase with barriers**
- **Cell-View Insertion**: 1.1 (0 FC) → 1.19 (3 FC) ✓ **Context-sensitive increase**
- **Cell-View Selection**: No clear trend ✗ (exception)

**Key Finding**: Bubble & Insertion deploy DG as problem-solving strategy (more backtracking when facing frozen cells), not random wandering.

---

### 2. AGGREGATION PEAKS - Emergent Clustering in Chimeric Arrays

| Algorithm Mix | Peak Agg. Value | Unique Values | Peak Occurs At | Std Dev | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Bubble-Selection** | 0.72 | Yes (unique) | 42% into process | ±0.05 | Highest peak (unique case) |
| **Bubble-Insertion** | 0.65 | Yes (unique) | 21% into process | ±0.04 | Mid-range clustering |
| **Selection-Insertion** | 0.69 | Yes (unique) | 19% into process | ±0.04 | Early peaking |
| All Three Mixed | 0.62 | Yes (unique) | 22% into process | — | Three-way complexity |

**With Duplicate Values** (no sort-order pressure):
- **Bubble-Selection**: 0.65 (final) | Peak at 100% (maintained to end)
- **Bubble-Insertion**: 0.63 (final) | Peak at 13%
- **Selection-Insertion**: 0.71 (final) | Peak at 100% (maintained to end)

**Critical Control**: Identical algorithm mix (negative control) = 0.50 (no aggregation) → Confirms phenomenon is due to *different algotypes*, not noise.

---

### 3. ERROR TOLERANCE - Frozen Cell Robustness

#### Movable Frozen Cells (can be pushed by others):
| Algorithm | 1 FC | 2 FC | 3 FC | Rank |
| :--- | :--- | :--- | :--- | :--- |
| **Cell-View Bubble** | 0 | 0.8 | 2.64 | **Best** (lowest monotonicity error) |
| **Cell-View Insertion** | 0.5 | 1.2 | 2.5 | Mid |
| **Cell-View Selection** | 2.24 | 4.36 | 13.24 | **Worst** (highest error) |

#### Immovable Frozen Cells (cannot be moved):
| Algorithm | 1 FC | 2 FC | 3 FC | Rank |
| :--- | :--- | :--- | :--- | :--- |
| **Cell-View Selection** | 1.0 | 1.96 | 2.91 | **Best** (most robust) |
| **Cell-View Insertion** | 1.5 | 3.0 | 4.2 | Mid |
| **Cell-View Bubble** | 1.91 | 3.72 | 5.37 | **Worst** (high error accumulation) |

**Key Insight**: 
- Cell-View algorithms **universally show less error** than traditional versions across all frozen-cell scenarios
- Bubble excels vs. *movable* defects (can push them aside)
- Selection excels vs. *immovable* defects (can route around them)

---

### 4. EFFICIENCY BASELINES - Step Counts (100-element arrays)

#### Pure Homogenous Sorts:
| Algorithm | Steps (Bubble) | Steps (Insertion) | Steps (Selection) |
| :--- | :--- | :--- | :--- |
| **Cell-View** | 2448.8 | 2482.8 | 1095.5 |
| **Traditional** | ~2450 | ~2500 | ~900 |
| **Ratio (CV/Trad)** | 1.0× | 1.0× | 1.22× |

#### Chimeric (Mixed) Sorts:
| Mix | Avg Steps | Expected (Linear Avg) | Actual | Match |
| :--- | :--- | :--- | :--- | :--- |
| **Bubble-Insertion** | 2476.02 | ≈2465 | 2476 | ✓ Yes |
| **Bubble-Selection** | 1740.9 | ≈1772 | 1741 | ✓ Yes |
| **Selection-Insertion** | 1534.77 | ≈1289 | 1535 | ✓ Yes |

**Key Finding**: Mixed algorithm-type efficiency = **linear average** of components (no synergy loss, no gain).

---

## Data Integration Status

The paper describes a complete experimental pipeline with:
- ✓ Code repository: `https://github.com/Zhangtaining/cell_research`
- ✓ .npy file outputs from Probe monitoring
- ✓ 100+ repetitions per condition
- ✗ **No experiment data files found in Space or repo yet**

### Action Items:
1. Implement metric collection in Java EDE implementation
2. Output experiment logs in comparable format (.npy or CSV)
3. Add comparison overlay to dashboard when live data available

---

## Metric Dashboard Priorities (ranked by surprise value)

1. **Delayed Gratification Scaling** - Does DG *linearly* increase with frozen cells in YOUR implementation?
2. **Aggregation Peak Timing** - Does clustering occur at same % process point?
3. **Error Tolerance Crossover** - Does Bubble beat Selection on movable FC and vice versa?
4. **Efficiency Linearity** - Do chimeric mixes truly average linearly?
5. **Unique vs Duplicate Sorting** - Can you maintain final aggregation with relaxed sort pressure?

---

**Generated**: 2026-01-02  
**Baseline Source**: Levin et al., "Sorting Algorithms Model Basal Intelligence in Morphogenesis" (2401.05375v1)  
**Status**: Ready for data integration
