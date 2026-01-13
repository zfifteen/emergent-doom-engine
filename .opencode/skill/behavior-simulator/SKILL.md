---
name: behavior-simulator
description: >
   This skill enables real-time, lightweight simulation 
   of emergent sorting behaviors in the Emergent 
   Doom Engine (EDE)
license: MIT
compatibility: opencode/skills
metadata:
  domain: "lab-tools"
  style: "ede-chop-shop"
  languages: "java,python,typescript"
  emphasis: "readability-over-cleverness"
---

# Emergent Behavior Simulator Skill

## Description
This skill enables real-time, lightweight simulation of emergent sorting behaviors in the Emergent Doom Engine (EDE), predicting how local cell swaps evolve into global patterns without full Java execution. Focused on general sorting (any comparable array), it models cell interactions, swap proposals, and convergence trajectories, using probabilistic shortcuts for efficiency. Ideal for rapid prototyping, debugging algotype mixes, or forecasting iterations (e.g., "Simulate 100 Bubble cells on [3,1,4,1,5]—predict steps to sort?").

Key capabilities:
- Input: Array of comparables, algotype assignments (e.g., all Bubble, mixed), initial positions/statuses.
- Output: Simulated trajectory (swaps per step), convergence metric (e.g., monotonicity), final order, stats (steps, swaps attempted/successful).
- Lightweight: Python-based, <1s for N=1000; Monte Carlo for randomness (e.g., 100 samples avg. Bubble outcomes).
- Derived from codebase: Mirrors AbstractCell contract (immutable value/algotype, mutable position/status), SwapEngine eligibility, execution loops.

From training: Local rules (e.g., Bubble's random dir on disorder) create diffusion; Insertion prefixes build left-to-right; Selection increments ideals for min-finding. Skill abstracts to Markov-like steps, predicting without full Probe/Engine.

## Training Insights (Code Patterns)
Analyzed full relevant codebase (~40 files) for emergent fidelity:

- **Cell Layer** (cell/*.java): Pure carriers—compareTo(value-only), shouldMoveGiven(NeighborhoodView: checks left/right disorder), calculateTarget(algotype-specific: Bubble bidirectional random, Insertion if left sorted && value < left, Selection if not at ideal && value < target).
- **Swap Layer** (swap/*.java): Proposals via canInitiate(ACTIVE), canAccept(FREEZE/others), hasGreaterValueThan for order. Emergent: Local fixes propagate (e.g., one swap ripples via status updates).
- **Execution Layer** (execution/*.java): Loops until convergence (NoSwapDetector); Probe records trajectories. Pattern: O(N^2) worst, but clustering (from experiments) reduces to O(N log N).
- **Metrics Layer** (metrics/*.java): Validate predictions—monotonicity (% increasing subsequences), DG (delayed rewards via erf approx). Shortcuts: If monotonicity >95%, halt sim.
- **Experiments/Tests** (experiments/*, test/*): Validate: Mixed algos converge faster (ClusteringVsFitness); e.g., Bubble+Selection hybrids sort [unsorted array] in ~150 steps.

Probabilistic model: Bubble nextBoolean() → sample 100 paths; deterministic for Insertion/Selection. General sorting: No domain (e.g., factorization)—pure comparables.

## Usage
Invoke via `/simulate-sorting` command or tool integration:
- Params: `array=[3,1,4]`, `algotypes=['BUBBLE','INSERTION','SELECTION']`, `steps=1000`, `samples=100` (for random algos).
- Output JSON: `{ "steps_to_converge": 25, "final_order": [1,3,4], "swaps": 12, "monotonicity": 100.0, "trajectory": [[initial], [after_swap1], ...] }`.

Example CLI:
```
$ python tools/emergent_sim.py --array "[5,2,8,1]" --algotypes "BUBBLE BUBBLE INSERTION SELECTION" --max_steps 500
```
Returns: Simulated path, e.g., 18 steps to sort, 95% monotonicity at end.

In opencode sessions: "Simulate Bubble on [10,3,7]—what's convergence time?" → Runs Python, summarizes.

## Implementation Blueprint (Python Logic)
Standalone `tools/emergent_sim.py` (std lib only).

[Code as above, fixed version]
