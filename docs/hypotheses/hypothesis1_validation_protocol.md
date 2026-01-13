# Hypothesis 1 Validation Protocol

This protocol is a plug-and-play harness to validate "clustering as free compute" using a non-sorting locality proxy.

## Mapping template

- Cells = items (queries / candidates / packets).
- Values = scalar fitness surrogate (priority, score, cost).
- Algotypes = strategy identities (e.g., access strategies).
- Goal = increase locality such that similar values become adjacent more often (hit uplift).

## Recommended parameters

- N: 100 for fast iteration; 1000 for scale confirmation.
- Algotypes: 50/50 chimeric split of `HASH_JUMP` and `SEQUENTIAL_SCAN`.
- Trials: 100.
- max_steps: 500 (or scaled).
- window_size: 10 (also sweep 5, 20).
- Random seed fixed for reproducibility.

## Metrics

- Baseline hits: hits measured before any steps.
- Post-run hits: hits at termination or max_steps.
- Hit improvement: post  baseline.
- Peak aggregation: maximum aggregation observed.
- Peak timing: progress fraction at which peak aggregation occurs.
- G (DG proxy): hits / steps; optional AUCH = mean hits over time.
- Boundary stability: stddev of inferred cluster boundary indices across trials.

## Controls

1. Label shuffle (negative control): shuffle algotype labels post-init (behaviors fixed).
2. Random-fitness ablation: randomize priorities/values; keep algotype assignment.
3. Window sweep: window_size  {5, 10, 20}.

## Success / failure

Promote H1 to "primitive-ready" only if:

- Hit improvement ~1520% absolute.
- Peak aggregation ~685%.
- Peak timing ~358% progress.
- G > 0.
- Boundary variance < 10%.

If any control collapses the utility signal (hits) while leaving aggregation unchanged, the clustering signal is likely not encoding useful structure for this mapping and should be treated as fragile.
