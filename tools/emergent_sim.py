import random
import math
from typing import List, Optional, Tuple
from dataclasses import dataclass
import argparse
import json


@dataclass
class SimCell:
    value: int
    algotype: str
    position: int
    status: str = "ACTIVE"


class EmergentSimulator:
    def __init__(self, array: List[int], algotypes: List[str]):
        self.n = len(array)
        self.initial_values = array[:]
        self.algotypes = algotypes[:]
        # Array represents current order, positions implicit 0 to n-1
        self.array = [
            SimCell(val, algo, i) for i, (val, algo) in enumerate(zip(array, algotypes))
        ]

    def _neighborhood_view(self, i: int, radius: int = 1) -> List[Tuple[SimCell, int]]:
        """Neighbors for cell at array index i (position i)."""
        left = self.array[max(0, i - radius) : i]
        right = self.array[i + 1 : i + 1 + radius]
        neighbors = [(cell, cell.position) for cell in left + right]
        return neighbors

    def _should_move(self, cell: SimCell, neighbors: List[Tuple[SimCell, int]]) -> bool:
        if cell.algotype in ["BUBBLE", "SEQUENTIAL_SCAN"]:
            return len(neighbors) > 0
        elif cell.algotype in ["INSERTION", "ASSOCIATIVE_CHAIN"]:
            left = [n for n, p in neighbors if p < cell.position]
            if not left:
                return False
            left.sort(key=lambda c: c.position)
            return all(left[j].value <= left[j + 1].value for j in range(len(left) - 1))
        elif cell.algotype in ["SELECTION", "HASH_JUMP"]:
            ideal_pos = self._get_ideal(cell)
            return cell.position != ideal_pos
        elif cell.algotype == "BLOOM_FILTER":
            return len(neighbors) > 0 and random.random() < 0.8  # Probabilistic
        return False

    def _calculate_target(
        self, cell: SimCell, neighbors: List[Tuple[SimCell, int]]
    ) -> Optional[int]:
        if not self._should_move(cell, neighbors):
            return None
        if cell.algotype in ["BUBBLE", "SEQUENTIAL_SCAN"]:
            left_cands = [
                p for n, p in neighbors if p < cell.position and cell.value < n.value
            ]
            right_cands = [
                p for n, p in neighbors if p > cell.position and cell.value > n.value
            ]
            if left_cands or right_cands:
                if left_cands and right_cands:
                    return random.choice(left_cands + right_cands)
                return min(left_cands) if left_cands else min(right_cands)
        elif cell.algotype in ["INSERTION", "ASSOCIATIVE_CHAIN"]:
            left_pos = [p for _, p in neighbors if p < cell.position]
            if left_pos:
                immediate_left = max(left_pos)
                left_cell = next((n for n, p in neighbors if p == immediate_left), None)
                if left_cell and left_cell.value > cell.value:
                    return immediate_left
        elif cell.algotype in ["SELECTION", "HASH_JUMP"]:
            ideal_pos = self._get_ideal(cell)
            for n, p in neighbors:
                if p == ideal_pos and n.value > cell.value:
                    return p
            self._increment_ideal(cell)
            return None
        elif cell.algotype == "BLOOM_FILTER":
            similar = [p for n, p in neighbors if abs(n.value - cell.value) < 10]
            if similar:
                return random.choice(similar)
        return None

    def _get_ideal(self, cell: SimCell, descending: bool = False) -> int:
        # Rank based on value: smaller values lower for ascending, reverse for descending fitness
        current_values = [c.value for c in self.array]
        if descending:
            sorted_indices = sorted(
                range(len(current_values)),
                key=lambda k: current_values[k],
                reverse=True,
            )
        else:
            sorted_indices = sorted(
                range(len(current_values)), key=lambda k: current_values[k]
            )
        for rank, idx in enumerate(sorted_indices):
            if self.array[idx].value == cell.value:
                return rank
        return 0

    def _increment_ideal(self, cell: SimCell):
        pass  # Simplified; in full, track per-cell ideal offset

    def _is_converged(self) -> bool:
        values = [c.value for c in self.array]
        return all(values[i] <= values[i + 1] for i in range(len(values) - 1))

    def simulate(
        self,
        max_steps: int = 1000,
        samples: int = 1,
        track_progress: bool = False,
        window_size: int = 5,
    ) -> dict:
        results = []
        progress_data = []
        for sample in range(samples):
            # Reset array for sample
            current_array = [
                SimCell(val, algo, i)
                for i, (val, algo) in enumerate(
                    zip(self.initial_values, self.algotypes)
                )
            ]
            steps = 0
            swaps = 0
            total_estimated_steps = self.n * math.log2(self.n) if self.n > 1 else 1
            while steps < max_steps and not self._is_converged_for(current_array):
                # Random proposal order
                proposal_order = list(range(self.n))
                random.shuffle(proposal_order)
                swapped = False
                for i in proposal_order:
                    cell = current_array[i]
                    if cell.status != "ACTIVE":
                        continue
                    neighbors = self._neighborhood_view(i)
                    target_pos = self._calculate_target(cell, neighbors)
                    if (
                        target_pos is not None
                        and 0 <= target_pos < self.n
                        and target_pos != i
                    ):
                        # Swap in array
                        j = target_pos
                        current_array[i], current_array[j] = (
                            current_array[j],
                            current_array[i],
                        )
                        # Update positions
                        current_array[i].position = i
                        current_array[j].position = j
                        swaps += 1
                        swapped = True
                steps += 1
                if track_progress and steps % 10 == 0:
                    progress = min((steps / total_estimated_steps) * 100, 100)
                    aggregation = self._calculate_aggregation(
                        current_array, window_size
                    )
                    progress_data.append(
                        {"progress": progress, "aggregation": aggregation}
                    )
                if not swapped:
                    break
            results.append({"steps": steps, "swaps": swaps})
            if track_progress and sample == 0:
                progress_data.append(
                    {
                        "progress": 100,
                        "aggregation": self._calculate_aggregation(
                            current_array, window_size
                        ),
                    }
                )
        avg_steps = sum(r["steps"] for r in results) / samples
        final_values = [c.value for c in current_array]  # From last sample
        mono = self._monotonicity(final_values)
        cache_hits_random = self._simulate_cache_hits(self.initial_values, window_size)
        cache_hits_clustered = self._simulate_cache_hits(final_values, window_size)
        improvement_pct = (
            ((cache_hits_clustered - cache_hits_random) / cache_hits_random * 100)
            if cache_hits_random > 0
            else 0
        )
        delta_g = improvement_pct / avg_steps if avg_steps > 0 else 0
        peak_agg = (
            max([d["aggregation"] for d in progress_data]) if progress_data else 0
        )
        peak_prog = (
            next(
                (d["progress"] for d in progress_data if d["aggregation"] == peak_agg),
                0,
            )
            if progress_data
            else 0
        )
        return {
            "avg_steps": avg_steps,
            "final_order": final_values,
            "monotonicity": mono,
            "avg_swaps": sum(r["swaps"] for r in results) / samples,
            "cache_hits_improvement_pct": improvement_pct,
            "delta_g": delta_g,
            "peak_aggregation": peak_agg,
            "peak_progress": peak_prog,
            "progress_data": progress_data,
        }

    def _calculate_aggregation(
        self, current_array: List[SimCell], window_size: int
    ) -> float:
        """% of windows with same algotype majority >70%."""
        n_windows = len(current_array) - window_size + 1
        if n_windows <= 0:
            return 0.0
        clustered = 0
        for start in range(n_windows):
            window_algos = [
                current_array[start + k].algotype for k in range(window_size)
            ]
            majority_count = max(window_algos.count(algo) for algo in set(window_algos))
            if majority_count / window_size > 0.7:
                clustered += 1
        return (clustered / n_windows) * 100

    def _simulate_cache_hits(self, values: List[int], window_size: int) -> float:
        """% of positions with at least one 'hit' (similar value in window)."""
        hits = 0
        total = len(values)
        for i in range(total):
            start = max(0, i - window_size // 2)
            end = min(total, i + window_size // 2 + 1)
            window = values[start:end]
            # Hit if any neighbor similar (diff < threshold)
            similar = any(abs(v - values[i]) < 5 and v != values[i] for v in window)
            if similar:
                hits += 1
        return (hits / total) * 100 if total > 0 else 0

    def _is_converged_for(
        self, current_array: List[SimCell], descending: bool = False
    ) -> bool:
        values = [c.value for c in current_array]
        if descending:
            return all(values[i] >= values[i + 1] for i in range(len(values) - 1))
        return all(values[i] <= values[i + 1] for i in range(len(values) - 1))

    def _monotonicity(self, values: List[int], descending: bool = False) -> float:
        if len(values) <= 1:
            return 100.0
        if descending:
            inc = sum(1 for i in range(1, len(values)) if values[i] >= values[i - 1])
        else:
            inc = sum(1 for i in range(1, len(values)) if values[i] <= values[i - 1])
        return (inc / (len(values) - 1)) * 100


def main():
    parser = argparse.ArgumentParser(description="EDE Lightweight Emergent Simulator")
    parser.add_argument(
        "--array", type=str, required=True, help="Input array as '[1,2,3]'"
    )
    parser.add_argument(
        "--algotypes", type=str, required=True, help="Algotypes as 'BUBBLE INSERTION'"
    )
    parser.add_argument("--max_steps", type=int, default=1000)
    parser.add_argument("--samples", type=int, default=1)
    parser.add_argument(
        "--track_progress", action="store_true", help="Track aggregation over progress"
    )
    parser.add_argument(
        "--window_size", type=int, default=5, help="Window for aggregation/cache sim"
    )
    args = parser.parse_args()

    array = eval(args.array)
    algos = args.algotypes.split()
    if len(algos) != len(array):
        raise ValueError("Algotypes must match array length")

    sim = EmergentSimulator(array, algos)
    result = sim.simulate(
        args.max_steps, args.samples, args.track_progress, args.window_size
    )
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
