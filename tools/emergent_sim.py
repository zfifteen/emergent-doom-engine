import random
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
        if cell.algotype == "BUBBLE":
            return len(neighbors) > 0
        elif cell.algotype == "INSERTION":
            left = [n for n, p in neighbors if p < cell.position]
            if not left:
                return False
            left.sort(key=lambda c: c.position)
            return all(left[j].value <= left[j + 1].value for j in range(len(left) - 1))
        elif cell.algotype == "SELECTION":
            ideal_pos = self._get_ideal(cell)
            return cell.position != ideal_pos
        return False

    def _calculate_target(
        self, cell: SimCell, neighbors: List[Tuple[SimCell, int]]
    ) -> Optional[int]:
        if not self._should_move(cell, neighbors):
            return None
        if cell.algotype == "BUBBLE":
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
        elif cell.algotype == "INSERTION":
            left_pos = [p for _, p in neighbors if p < cell.position]
            if left_pos:
                immediate_left = max(left_pos)
                left_cell = next((n for n, p in neighbors if p == immediate_left), None)
                if left_cell and left_cell.value > cell.value:
                    return immediate_left
        elif cell.algotype == "SELECTION":
            ideal_pos = self._get_ideal(cell)
            for n, p in neighbors:
                if p == ideal_pos and n.value > cell.value:
                    return p
            self._increment_ideal(cell)
            return None
        return None

    def _get_ideal(self, cell: SimCell) -> int:
        # Ideal position: rank in current sorted values (ascending)
        current_values = [c.value for c in self.array]
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

    def simulate(self, max_steps: int = 1000, samples: int = 1) -> dict:
        results = []
        last_final_values = None
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
                        # Update positions to new indices
                        current_array[i].position = i
                        current_array[j].position = j
                        swaps += 1
                        swapped = True
                steps += 1
                if not swapped:
                    break
            results.append({"steps": steps, "swaps": swaps})
            if sample == 0:  # Track first sample for final output
                last_final_values = [c.value for c in current_array]
        avg_steps = sum(r["steps"] for r in results) / samples
        mono = self._monotonicity(last_final_values)
        return {
            "avg_steps": avg_steps,
            "final_order": last_final_values,
            "monotonicity": mono,
            "avg_swaps": sum(r["swaps"] for r in results) / samples,
        }

    def _is_converged_for(self, current_array: List[SimCell]) -> bool:
        values = [c.value for c in current_array]
        return all(values[i] <= values[i + 1] for i in range(len(values) - 1))

    def _monotonicity(self, values: List[int]) -> float:
        if len(values) <= 1:
            return 100.0
        inc = sum(1 for i in range(1, len(values)) if values[i] >= values[i - 1])
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
    args = parser.parse_args()

    array = eval(args.array)
    algos = args.algotypes.split()
    if len(algos) != len(array):
        raise ValueError("Algotypes must match array length")

    sim = EmergentSimulator(array, algos)
    result = sim.simulate(args.max_steps, args.samples)
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()
