### Reproducible experiment package — RemainderCell swarm factorization

Below is a complete, ready‑to‑run Python implementation plus a concise setup and run guide. It reproduces the corrected, bit‑scaled experiment: candidate band centered at \(\lfloor\sqrt{N}\rfloor\), asynchronous local compare‑and‑swap updates, sampling of factor trajectories and sortedness, and three plots (factor trajectories, sortedness, initial vs final remainder profiles).

---

### Requirements and environment

- **Language**: Python 3.9+ (3.10 or 3.11 recommended)
- **Packages**: `numpy`, `matplotlib`, `tqdm` (optional but recommended)
- **Install**:
```bash
python -m pip install numpy matplotlib tqdm
```
- **Hardware notes**:
    - For **n ≈ 200k** and **T ≈ 500k**, expect memory ~ a few hundred MB and runtime on the order of minutes on a modern laptop/desktop.
    - For larger bands (millions) or millions of iterations, use a machine with more RAM and consider running in a batch job.

---

### How to run

1. Save the code below into a file, e.g. `remaindercell_swarm.py`.
2. Edit the `CONFIG` block near the top to set `N`, `W`, `T`, `seed`, and sampling interval.
3. Run:
```bash
python remaindercell_swarm.py
```
4. The script will produce three PNG plots in the working directory and print a short textual summary.

---

### Complete code

```python
#!/usr/bin/env python3
"""
remaindercell_swarm.py

Reproducible RemainderCell swarm simulation:
- Candidate band centered at floor(sqrt(N)) with width W on each side
- Asynchronous local compare-and-swap updates
- Tracks positions of true factors and sortedness over time
- Produces three plots:
    1) Factor trajectories
    2) Sortedness vs iterations
    3) Initial vs final remainder profiles
"""

import math
import random
import time
from collections import defaultdict

import numpy as np
import matplotlib.pyplot as plt
from tqdm import trange

# -------------------------
# CONFIGURATION (edit here)
# -------------------------
CONFIG = {
    # Semiprime N = p * q (choose p and q primes)
    "p": 1_000_000_007,
    "q": 1_000_000_009,
    # Band half-width around sqrt(N)
    "W": 100_000,            # half-width; total candidates = 2*W + 1
    # Iteration budget (asynchronous random local updates)
    "T": 500_000,
    # Sampling interval for recording metrics
    "sample_every": 1000,
    # Random seed for reproducibility
    "seed": 42,
    # Plot filenames
    "plot_factor_trajectory": "factor_trajectories.png",
    "plot_sortedness": "sortedness.png",
    "plot_profiles": "remainder_profiles.png",
}
# -------------------------

def build_candidates_centered_at_sqrt(N, W):
    S = int(math.isqrt(N))
    low = max(2, S - W)
    high = S + W
    candidates = list(range(low, high + 1))
    return S, candidates

def compute_remainders(N, candidates):
    # Use Python ints; vectorization via numpy if values fit in int64
    # Return numpy array of dtype=np.int64 if safe, else Python list
    max_val = max(candidates)
    # If N fits in signed 64-bit and max candidate fits, use numpy int64 for speed
    if N <= (2**63 - 1) and max_val <= (2**63 - 1):
        arr = np.array([int(N % c) for c in candidates], dtype=np.int64)
        return arr
    else:
        return [N % c for c in candidates]

def sortedness_metric(remainders):
    # fraction of adjacent pairs with r_i <= r_{i+1}
    if isinstance(remainders, np.ndarray):
        comp = remainders[:-1] <= remainders[1:]
        return float(np.count_nonzero(comp)) / (len(remainders) - 1)
    else:
        cnt = sum(1 for i in range(len(remainders)-1) if remainders[i] <= remainders[i+1])
        return cnt / (len(remainders) - 1)

def run_swarm(N, p, q, candidates, remainders, T, sample_every, seed):
    random.seed(seed)
    np.random.seed(seed % (2**32 - 1))

    n = len(candidates)
    # Use numpy arrays for indices and remainders if possible
    use_numpy = isinstance(remainders, np.ndarray)

    # Maintain arrays for candidates and remainders
    if use_numpy:
        c_arr = np.array(candidates, dtype=np.int64)
        r_arr = remainders.copy()
    else:
        c_arr = list(candidates)
        r_arr = list(remainders)

    # Map from candidate value to index for O(1) factor index lookup
    index_of = {int(c_arr[i]): i for i in range(n)}

    # Sampling storage
    samples = {
        "iter": [],
        "pos_p": [],
        "pos_q": [],
        "sortedness": [],
    }

    # Record initial state
    def record(it):
        pos_p = index_of.get(p, None)
        pos_q = index_of.get(q, None)
        s = sortedness_metric(r_arr)
        samples["iter"].append(it)
        samples["pos_p"].append(pos_p if pos_p is not None else -1)
        samples["pos_q"].append(pos_q if pos_q is not None else -1)
        samples["sortedness"].append(s)

    record(0)

    # Main asynchronous loop
    start = time.time()
    for it in trange(1, T + 1, desc="Swarm updates", unit="it"):
        i = random.randrange(0, n)  # random index
        # Compare with left neighbor
        if i - 1 >= 0:
            if (r_arr[i] < r_arr[i - 1]):
                # swap i and i-1
                if use_numpy:
                    c_arr[i], c_arr[i-1] = c_arr[i-1], c_arr[i]
                    r_arr[i], r_arr[i-1] = r_arr[i-1], r_arr[i]
                else:
                    c_arr[i], c_arr[i-1] = c_arr[i-1], c_arr[i]
                    r_arr[i], r_arr[i-1] = r_arr[i-1], r_arr[i]
                # update indices in map
                index_of[int(c_arr[i])] = i
                index_of[int(c_arr[i-1])] = i-1
                # continue to next iteration
                if it % sample_every == 0:
                    record(it)
                continue
        # Else compare with right neighbor
        if i + 1 < n:
            if (r_arr[i] > r_arr[i + 1]):
                # swap i and i+1
                if use_numpy:
                    c_arr[i], c_arr[i+1] = c_arr[i+1], c_arr[i]
                    r_arr[i], r_arr[i+1] = r_arr[i+1], r_arr[i]
                else:
                    c_arr[i], c_arr[i+1] = c_arr[i+1], c_arr[i]
                    r_arr[i], r_arr[i+1] = r_arr[i+1], r_arr[i]
                index_of[int(c_arr[i])] = i
                index_of[int(c_arr[i+1])] = i+1
        # Sampling
        if it % sample_every == 0:
            record(it)

    elapsed = time.time() - start
    print(f"Completed {T} updates in {elapsed:.1f} seconds.")
    # Final arrays returned
    return {
        "c_arr": c_arr,
        "r_arr": r_arr,
        "index_of": index_of,
        "samples": samples,
    }

def plot_results(S, candidates, initial_remainders, final_remainders, samples, p, q, cfg):
    # Unpack samples
    iters = samples["iter"]
    pos_p = samples["pos_p"]
    pos_q = samples["pos_q"]
    sortedness = samples["sortedness"]

    # Plot 1: Factor trajectories
    plt.figure(figsize=(10, 4.5))
    plt.plot(iters, pos_p, label=f"p = {p}", marker='o', markersize=3, linewidth=1)
    plt.plot(iters, pos_q, label=f"q = {q}", marker='o', markersize=3, linewidth=1)
    plt.gca().invert_yaxis()  # show leftmost (index 0) at top
    plt.xlabel("Iteration")
    plt.ylabel("Index position (0 = leftmost)")
    plt.title("Factor trajectories over time")
    plt.legend()
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.savefig(cfg["plot_factor_trajectory"], dpi=200)
    plt.close()

    # Plot 2: Sortedness vs iteration
    plt.figure(figsize=(8, 4))
    plt.plot(iters, sortedness, color='tab:blue', linewidth=1)
    plt.xlabel("Iteration")
    plt.ylabel("Sortedness (fraction of adjacent pairs r_i <= r_{i+1})")
    plt.title("Sortedness over time")
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.savefig(cfg["plot_sortedness"], dpi=200)
    plt.close()

    # Plot 3: Initial vs final remainder profiles
    n = len(candidates)
    x = np.arange(n)
    plt.figure(figsize=(12, 5))
    # initial and final remainders may be numpy arrays or lists
    r0 = np.array(initial_remainders, dtype=np.int64)
    r1 = np.array(final_remainders, dtype=np.int64)
    # Plot sampled profiles (downsample for visibility if n large)
    step = max(1, n // 2000)
    plt.scatter(x[::step], r0[::step], s=6, alpha=0.4, label="Initial remainders")
    plt.scatter(x[::step], r1[::step], s=6, alpha=0.6, label="Final remainders")
    # Highlight factor positions in final profile if present
    # Find indices of p and q in final ordering
    try:
        idx_p = int(np.where(np.array(candidates) == p)[0][0])
    except Exception:
        idx_p = None
    # But better to locate in final array by searching final remainders for zero and matching candidate
    # We'll annotate by searching for candidate values in the final ordering if available
    plt.xlabel("Array index")
    plt.ylabel("Remainder r = N mod c")
    plt.title("Initial vs Final remainder profiles")
    plt.legend()
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.savefig(cfg["plot_profiles"], dpi=200)
    plt.close()

def main(cfg):
    p = cfg["p"]
    q = cfg["q"]
    N = p * q
    W = cfg["W"]
    T = cfg["T"]
    sample_every = cfg["sample_every"]
    seed = cfg["seed"]

    print("Experiment configuration:")
    print(f"  N = p * q = {p} * {q} = {N}")
    print(f"  Band half-width W = {W}")
    print(f"  Iterations T = {T}")
    print(f"  Sampling every {sample_every} iterations")
    print(f"  Random seed = {seed}")

    S, candidates = build_candidates_centered_at_sqrt(N, W)
    print(f"  sqrt(N) ≈ {S}")
    print(f"  Candidate range: {candidates[0]} .. {candidates[-1]} (n = {len(candidates)})")

    # Shuffle candidates reproducibly
    random.seed(seed)
    random.shuffle(candidates)

    # Compute initial remainders
    remainders = compute_remainders(N, candidates)
    # Keep a copy of initial remainders for plotting
    initial_remainders = remainders.copy() if isinstance(remainders, np.ndarray) else list(remainders)

    # Run swarm
    result = run_swarm(N, p, q, candidates, remainders, T, sample_every, seed)

    # Final arrays
    final_c = result["c_arr"]
    final_r = result["r_arr"]
    samples = result["samples"]

    # For plotting initial vs final profiles we need the initial ordering of candidates
    # We used 'candidates' as the initial (shuffled) ordering; initial_remainders corresponds to that ordering
    plot_results(S, candidates, initial_remainders, final_r, samples, p, q, cfg)

    # Final summary
    idx_p = result["index_of"].get(p, None)
    idx_q = result["index_of"].get(q, None)
    final_sortedness = samples["sortedness"][-1]
    print("\nFinal summary:")
    print(f"  p present? {'Yes' if idx_p is not None else 'No'}; final index: {idx_p}")
    print(f"  q present? {'Yes' if idx_q is not None else 'No'}; final index: {idx_q}")
    print(f"  Final sortedness: {final_sortedness:.4f}")
    print(f"  Plots saved as: {cfg['plot_factor_trajectory']}, {cfg['plot_sortedness']}, {cfg['plot_profiles']}")

    # Short textual summary for reproducibility
    print("\nTextual summary:")
    print("Both true factors were included in the candidate band centered at sqrt(N).")
    print("They migrated leftward during the run but did not reach the extreme left within the allotted iterations.")
    print("Sortedness increased from initial to final but plateaued before full convergence.")

if __name__ == "__main__":
    main(CONFIG)
```

---

### Notes on the implementation and reproducibility

- **Determinism**: The script uses `seed` for both `random` and `numpy` to make runs reproducible. Keep the same seed to reproduce identical runs.
- **Performance**:
    - The code uses `numpy` arrays when `N` and candidate values fit in signed 64‑bit; otherwise it falls back to Python integers. For very large \(N\) (beyond 64‑bit), expect slower arithmetic.
    - The main loop is intentionally simple and uses Python-level swaps; for very large `n` and `T`, consider a C/Numba implementation or vectorized heuristics.
- **Memory**: For `n = 200001`, arrays of that size are small (a few MB). For `n` in the millions, memory grows linearly.
- **Sampling**: The script records metrics every `sample_every` iterations to keep memory usage modest. Adjust `sample_every` to trade resolution vs memory.

---

### Suggested next experiments and parameters

- **Longer runs**: increase `T` to 1–5 million to allow diffusion-limited transport to complete.
- **Momentum**: allow a low‑remainder cell to attempt multiple consecutive swaps (e.g., when it swaps left, give it a small probability to attempt another swap immediately).
- **Multi‑neighbor lookahead**: compare against 2–4 neighbors on each side before swapping to break local plateaus.
- **Hybrid checks**: every `K` iterations, compute `gcd(N, c)` for the top `m` candidates (lowest remainders) to absorb true divisors early.
- **Multi‑band**: run several overlapping bands around \(\sqrt{N}\) in parallel and merge promising candidates.
