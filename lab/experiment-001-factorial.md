# Experiment 001: 2×2 Factorial for Hypothesis 1 Clustering Primitive

## Overview
Validation of H1 under protocol (N=100 random priorities seed 42, 50 samples/trial, max_steps=500, track_progress=true). Tests diversity (2 vs 4 algos) x window_size (10 vs 20). Baseline hits ~11.8%. Controls passed (label shuffle +2.1%, ablation +4.8%, boundary var 6.9%).

## Results Table

| Factor | Hits Improvement (%) | Peak Agg (%) | Peak Progress (%) | ΔG | Avg Steps |
|--------|----------------------|--------------|-------------------|-----|-----------|
| 2 Algos (HASH/SEQ), WS=10 | 18.1 ± 3.2 | 70.5 ± 4.1 | 35.2 ± 6.8 | 0.51 | 138 ± 25 |
| 2 Algos, WS=20 | 19.4 ± 3.5 | 68.9 ± 4.3 | 36.8 ± 7.2 | 0.53 | 142 ± 27 |
| 4 Algos (+ASSOC/BLOOM 25% ea), WS=10 | 16.7 ± 3.8 | 65.2 ± 5.4 | 38.1 ± 8.5 | 0.44 | 152 ± 31 |
| 4 Algos, WS=20 | 18.5 ± 4.1 | 63.8 ± 5.7 | 39.4 ± 9.0 | 0.47 | 158 ± 34 |

## Analysis
- Diversity dilutes agg/hits slightly (4 algos -1.4% vs 2), but >15% threshold holds; larger WS boosts utility (+1.3%).
- Peak timing stable ~35-39%; ΔG>0 confirms "delayed" value.
- Supports H1 robustness for primitive; ready for factorization mapping (probes as algos, divisors as values).

## Raw Data Snippet (Trial 1, 2 Algos WS=10)
Progress Data: [{"progress": 20.0, "aggregation": 45.5}, ..., {"progress": 100, "aggregation": 70.5}]

Sim Invocation: `python tools/emergent_sim.py --array "[846,596,...]" --algotypes "HASH_JUMP SEQUENTIAL_SCAN ..." --samples 50 --track_progress --window_size 10`

**Status**: Validated; next: Factorization experiment under protocol.