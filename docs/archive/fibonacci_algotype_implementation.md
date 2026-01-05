# Implement Fibonacci Jump Sort AlgoType

## Summary

Add a novel distributed cell-view sorting algotype that uses **Fibonacci-distance viewing** for local swap decisions. Experimental validation demonstrates **22-24% efficiency improvement** over canonical Bubble and Insertion sorts with very large effect sizes (Cohen's d > 19, p < 0.001).

## Background

### What is Fibonacci Jump Sort?

A distributed sorting algorithm where individual cells (sortable elements) make autonomous swap decisions by viewing neighbors at **Fibonacci-sequence distances** {1, 2, 3, 5, 8, 13, 21, ...} rather than constant (Bubble: ±1) or linear (Insertion: all-left) patterns.

**Key Properties:**
- **Architecture:** Bottom-up emergent (no central controller)
- **Viewing:** Logarithmically-spaced Fibonacci distances
- **Execution:** Parallel synchronous with conflict resolution
- **Performance:** ~470 swaps for N=50 vs ~607 (Bubble) vs ~600 (Insertion)

### Relationship to Prior Art

**Mathematical principle** (log-spaced viewing = efficiency) has prior art:
- Shellsort with Fibonacci gaps [orlp.net 2025]
- Sedgewick gap sequences [1986]

**Mechanism is novel:**
- First distributed/cell-view implementation with Fibonacci viewing
- Different architecture from centralized gap-sorting algorithms
- Unique application to EDE emergent dynamics framework

## Specification

**Full formal specification:** See `docs/algotypes/fibonacci_jump_sort_spec.md`

### Core Algorithm

```python
def evaluate_move(cell_position, cell_value, array, frozen_set):
    """Evaluate Fibonacci-distance neighbors for beneficial swaps."""
    fib_distances = [1, 2, 3, 5, 8, 13, 21, ...]  # up to array length
    
    # Check left neighbors at Fibonacci distances
    for dist in fib_distances:
        left_pos = cell_position - dist
        if left_pos >= 0 and array[left_pos].value > cell_value:
            return ('LEFT', left_pos)  # Found larger value to my left
    
    # Check right neighbors at Fibonacci distances
    for dist in fib_distances:
        right_pos = cell_position + dist
        if right_pos < len(array) and array[right_pos].value < cell_value:
            return ('RIGHT', right_pos)  # Found smaller value to my right
    
    return None  # No beneficial swap
```

### Conflict Resolution

When multiple cells propose swaps to overlapping positions:
1. Sort proposals by source position (deterministic ordering)
2. Execute non-conflicting swaps only
3. Skip swaps involving already-committed positions

## Implementation Requirements

### Code Changes

**1. Add Algotype Enum**
```python
class AlgotypeEnum(Enum):
    BUBBLE = "BUBBLE"
    INSERTION = "INSERTION"
    SELECTION = "SELECTION"
    FIBONACCI = "FIBONACCI"  # NEW
```

**2. Implement FibonacciJumpSort Class**
```python
class FibonacciJumpSort(CellViewSorter):
    """Fibonacci-distance viewing sorter."""
    
    def __init__(self):
        self.name = "Fibonacci Jump Sort"
    
    def generate_fibonacci_distances(self, max_distance: int) -> List[int]:
        """Generate Fibonacci sequence up to max_distance."""
        # Implementation
    
    def evaluate_move(self, cell: Cell, array: List[Cell]) -> Optional[Tuple[str, int]]:
        """Evaluate Fibonacci viewing window for swaps."""
        # Implementation (see spec)
```

**3. Update Sorter Factory**
```python
def get_sorter(algotype: AlgotypeEnum):
    if algotype == AlgotypeEnum.FIBONACCI:
        return FibonacciJumpSort()
    # ... existing cases
```

**4. Add Utility Functions**
```python
def generate_fibonacci_up_to(max_value: int) -> List[int]:
    """Generate Fibonacci numbers up to max_value."""
    if max_value < 1:
        return []
    fib = [1, 2]
    while True:
        next_fib = fib[-1] + fib[-2]
        if next_fib >= max_value:
            break
        fib.append(next_fib)
    return fib
```

### Testing Requirements

**Unit Tests:**
- [ ] Fibonacci generation: `test_fibonacci_generation()`
  - Edge cases: N=0, N=1, N=5, N=100
  - Verify sequence correctness
- [ ] Swap evaluation: `test_fibonacci_evaluate_move()`
  - Cell in middle of array
  - Cell at boundaries (edge cases)
  - No beneficial swap available
  - Multiple Fibonacci neighbors
- [ ] Frozen cell handling: `test_fibonacci_with_frozen()`
  - Skip frozen positions in viewing
  - Don't propose swaps to frozen cells

**Integration Tests:**
- [ ] Small arrays: `test_fibonacci_sort_small()`
  - N=5: [3,1,4,2,5] -> [1,2,3,4,5]
  - N=10: random permutation -> sorted
- [ ] Validation: `test_fibonacci_sort_validation()`
  - N=50, 10 trials
  - Mean swaps: 469.9 ± 10 (tolerance)
  - All arrays reach 100% sortedness
- [ ] Frozen cell robustness: `test_fibonacci_frozen_validation()`
  - N=50, 2 frozen, 10 trials
  - Mean swaps: 499.1 ± 15 (tolerance)
  - Degradation: ~6% (expected)

**Performance Benchmarks:**
- [ ] Compare to canonical algotypes (N=50)
  - Verify 22-24% advantage vs Bubble/Insertion
  - Verify 61% advantage vs Selection
- [ ] Scaling test (N=25, 50, 100)
  - Document swap counts at each size
  - Check if advantage holds

**Chimeric Tests (Future):**
- [ ] Mixed Bubble-Fibonacci arrays
  - 25% Fibonacci, 75% Bubble
  - Measure clustering/segregation

### Documentation Requirements

- [ ] Add to `README.md` algotype comparison table
- [ ] Document in `docs/algotypes/fibonacci.md`
- [ ] Add example usage to tutorials
- [ ] Include performance comparison chart
- [ ] Document viewing distance visualization

## Expected Performance

### Validated Results (N=50)

| Algotype | Mean Swaps | Std Dev | vs Fibonacci |
|----------|-----------|---------|---------------|
| Fibonacci | **469.9** | 4.9 | — |
| Insertion | 600.4 | 5.1 | +21.7% |
| Bubble | 606.6 | 5.8 | +22.5% |
| Selection | 1224.0 | 0.0 | +61.6% |

**Statistical significance:** p < 0.001 (all comparisons), Cohen's d > 19

### Robustness (2 Frozen Cells)

| Algotype | Degradation |
|----------|-------------|
| Selection | +5.0% (best) |
| **Fibonacci** | **+6.2%** |
| Insertion | +7.8% |
| Bubble | +8.0% (worst) |

## Acceptance Criteria

### Must Have
- [x] Formal specification document created
- [ ] `FibonacciJumpSort` class implemented
- [ ] All unit tests passing (100% coverage for new code)
- [ ] Integration tests validate experimental results (±10% tolerance)
- [ ] Frozen cell handling correct
- [ ] Performance benchmarks vs canonical algotypes documented
- [ ] Code review completed
- [ ] Documentation updated (README + algotype docs)

### Should Have
- [ ] Visualization of Fibonacci viewing windows
- [ ] Performance scaling analysis (N=25, 50, 100, 200)
- [ ] Chimeric array support (mixed algotypes)
- [ ] Async execution mode (non-blocking)

### Could Have
- [ ] Adaptive Fibonacci scaling (dynamic distance adjustment)
- [ ] Golden Ratio continuous positioning mode
- [ ] Powers-of-2 alternative implementation for comparison

## Implementation Plan

### Phase 1: Core Implementation (3-4 hours)
1. Add `FIBONACCI` to `AlgotypeEnum`
2. Implement `generate_fibonacci_up_to()` utility
3. Implement `FibonacciJumpSort` class
4. Add to sorter factory
5. Basic unit tests

### Phase 2: Validation (2-3 hours)
6. Integration tests (small arrays)
7. Validation tests (N=50, match experimental results)
8. Frozen cell tests
9. Performance benchmarks

### Phase 3: Documentation (1-2 hours)
10. Update README with comparison table
11. Create `docs/algotypes/fibonacci.md`
12. Add usage examples
13. Document novelty and prior art

### Phase 4: Enhancement (Optional, 2-4 hours)
14. Visualization implementation
15. Chimeric array support
16. Scaling analysis experiments

**Total Estimated Effort:** 6-9 hours (core + validation + docs)

## Success Metrics

✅ **All unit tests pass** (100% coverage)  
✅ **Validation tests match experimental results** (within ±10%)  
✅ **22-24% efficiency advantage confirmed** vs Bubble/Insertion  
✅ **Documentation complete** and reviewed  
✅ **Code review approved** by maintainer  
✅ **No regressions** in existing algotype tests  

## Dependencies

- Existing `Cell` and `CellViewSorter` base classes
- `AlgotypeEnum` enumeration
- Experimental validation data: `experiments/data/validated_results.csv`
- Metrics: `calculate_sortedness()`, `calculate_monotonicity_error()`

## References

- **Full Specification:** `docs/algotypes/fibonacci_jump_sort_spec.md`
- **Experimental Validation:** `experiments/data/validated_results.csv` (2026-01-04)
- **Prior Art:** [orlp.net Fibonacci Shellsort](https://orlp.net/blog/fibonacci-sort/) (mechanism distinct)
- **EDE Framework:** Morphogenesis-inspired distributed computation

## Labels

`enhancement`, `algotype`, `validated`, `high-priority`, `fibonacci`

## Milestone

v0.2.0 - Extended AlgoType Library

---

**Issue Type:** Feature Implementation  
**Priority:** High (validated novel algorithm with significant gains)  
**Complexity:** Medium (well-specified, clear acceptance criteria)  
**Status:** Ready for implementation
