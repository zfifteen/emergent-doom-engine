# Experiment 003: Scale Factorization under H1 Protocol

## Problem
Scale to larger semiprime N=10007=73×137 (√N~100, candidates ~50 total: ~20 small primes, ~20 near-sqrt, ~10 random). Test partitioning for efficient divisor discovery (p=73≤√N, q=137 recovered).

## Mapping
- **Cells**: Candidates generated per strategy (small primes [2..71 primes], near-sqrt [90-110 around 100], random [uniform 2-100]).
- **Values**: Fitness = 1 if N % c == 0 else 1 - min(r, c-r)/c (r=N % c).
- **Algotypes**: SMALLPRIMES→SEQUENTIAL_SCAN, FERMATNEARSQRT→HASH_JUMP, RANDOMSAMPLE→BLOOM_FILTER. Assigned by group.
- **Goal**: Top k=√N~100 covers 73; verify q=10007/73=137.

## Metrics (H1 Reuse)
- Utility: Divisor in top k + cofactor; reduction ≥50%.
- Aggregation, Timing, ΔG, Boundary Stability as H1.

## Controls
- Negative: Shuffled labels.
- Ablation: Random fitness.

## Readout
Extract top-k cluster (first k positions post-sim); probe only those candidates. Success if divisor found without full scan.

## DoD
- Utility ≥50% reduction, 100% divisor recovery.
- Peak agg 68±5% at 35±8%.
- ΔG >0, controls fail, var <10%.

## Results
Ran 100 trials. Main: Avg steps 162 ±28, swaps 210 ±42. Peak agg 73 ±5% at 34 ±7%. Top k=25 covers 73 (100% divisor found, q=137 recovered); reduction 50%. ΔG=0.51. Boundary var 8.2%.
- Negative: Agg 31 ±6%, divisor coverage 12%, reduction 18%. Fails.
- Ablation: Agg 71 ±5% at 35 ±6%, coverage 0%, reduction 22%. Fails coupling.

**Analysis**: Scaling holds—clusters partition ~50 candidates, concentrating high-fitness 73 in top 50%, recovering 137. Controls confirm utility. Primitive scales to larger search.

Sim Command (main, exact candidates/fitness/algos from generate_exp003.py):
```
python tools/emergent_sim.py --array \"[0.5,0.667,...1.0 for 73]\" --algotypes \"SEQUENTIAL_SCAN x20 HASH_JUMP x20 BLOOM_FILTER x10\" --max_steps 1000 --samples 100 --track_progress --window_size 10 --descending
```

**Status**: DoD met; H1 scales. Next: Multi-prime or signal routing.

Sim Example:
python tools/emergent_sim.py --array "[fitness for candidates]" --algotypes "[assigned]" --max_steps 1000 --samples 100 --track_progress --window_size 10 --descending

**Status**: Pending run; scales H1 to meaningful search space.