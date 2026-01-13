# Clustering Primitive Spec (EDE)

This document extends the Space-level clustering primitive spec with a concrete, reproducible, **non-sorting proxy** validation harness ("locality hits").

## Non-sorting proxy: locality hits

### Problem mapping

- Cells: queries / items competing for placement.
- Values: priorities (or other scalar fitness surrogate).
- Algotypes: access / probe strategies (e.g., `HASH_JUMP` vs `SEQUENTIAL_SCAN`).
- Readout: emergent clustering yields strategy-dominant regions that improve locality ("hits") without explicit global coordination.

### Hypothesis (H1)

In a chimeric population with 50% `HASH_JUMP` and 50% `SEQUENTIAL_SCAN`, the system will:

- Increase locality hits by ~15–20% absolute over a random baseline.
- Exhibit a transient peak aggregation near ~35% progress.
- Show positive delayed-gratification-like behavior when measured as hits / step (net gain after temporary rearrangement).

### Metrics

- **Hits**: percent of positions whose neighbor(s) fall within a similarity band in value/priority (e.g., |diff| < 5) under a fixed window size.
- **Aggregation**: percent of cells that have at least one same-algotype neighbor (or, for windowed aggregation, fraction of windows dominated by one algotype).
- **G (proxy DG)**: hits / steps (optionally AUCH = mean hits over time).
- **Peak timing**: progress fraction when aggregation is maximal.
- **Boundary stability**: variance of inferred cluster boundaries across runs.

### Controls (falsifiability)

- **Negative control (label shuffle)**: shuffle algotype labels post-init while keeping behaviors fixed. Expect aggregation signal and hit improvement to collapse toward baseline.
- **Random-fitness ablation**: randomize priorities/values (decouple from behavior). Expect aggregation to persist (if driven by movement policy) but hit improvement to collapse.
- **Window-size sweep**: run window_size  {5, 10, 20} and verify a smooth tradeoff (no single magic window).

### Success criteria

A run set (e.g., 100 trials) supports the primitive if:

- Hit improvement is in the predicted band (~15–20% absolute).
- Peak aggregation is within the expected envelope (~685%) and peak timing is near ~358% progress.
- G > 0.
- Boundary stability variance < 10% across runs.

## Integration note

This proxy is intended to be a reusable validation harness for any domain where clustering is claimed to provide *computational value* as an intermediate partition (e.g., factorization candidate partitioning).
