# SwapEngine Test Specification

## 1. Component Overview

The `SwapEngine` is the core decision-maker in the Emergent Doom Engine. It determines whether two cells should swap positions based on:

1. **Frozen cell constraints** — whether cells are allowed to move/be displaced
2. **Comparison outcome** — whether swapping would improve order (`cells[i] > cells[j]`)

This component is critical because the entire emergent computation model depends on swaps occurring **only** when both constraints are satisfied. Any bug here breaks the fundamental premise of the engine.

## 2. Test Scope

### Methods Under Test

| Method | Signature | Purpose |
|--------|-----------|---------|
| `attemptSwap` | `boolean attemptSwap(T[] cells, int i, int j)` | Attempt swap, mutate array if successful |
| `wouldSwap` | `boolean wouldSwap(T[] cells, int i, int j)` | Check if swap would occur (no mutation) |
| `getSwapCount` | `int getSwapCount()` | Return total swaps performed |
| `resetSwapCount` | `void resetSwapCount()` | Reset counter to zero |

### Primary Focus

`attemptSwap` is the critical method. It must:
- Respect frozen constraints before comparing
- Only swap when `compareTo()` returns positive
- Correctly mutate the array when swapping
- Increment swap count only on successful swaps

## 3. Frozen State Definitions

| State | `canMove()` | `canBeDisplaced()` | Behavior |
|-------|-------------|-------------------|----------|
| `NONE` | true | true | Fully mobile — can initiate and receive swaps |
| `MOVABLE` | true | false | Can initiate swaps, cannot be displaced |
| `IMMOVABLE` | false | false | Completely frozen — no swap participation |

## 4. Test Matrix

### 4.1 Frozen State Combinations

All combinations of frozen states for cells at positions `i` and `j`:

| ID | Cell i State | Cell j State | Frozen Check Result | Proceeds to Compare? |
|----|--------------|--------------|---------------------|----------------------|
| F1 | NONE | NONE | Pass | Yes |
| F2 | MOVABLE | NONE | Pass | Yes |
| F3 | NONE | MOVABLE | Fail (j not displaceable) | No |
| F4 | MOVABLE | MOVABLE | Fail (j not displaceable) | No |
| F5 | IMMOVABLE | NONE | Fail (i cannot move) | No |
| F6 | IMMOVABLE | MOVABLE | Fail (i cannot move) | No |
| F7 | IMMOVABLE | IMMOVABLE | Fail (i cannot move) | No |
| F8 | NONE | IMMOVABLE | Fail (j not displaceable) | No |
| F9 | MOVABLE | IMMOVABLE | Fail (j not displaceable) | No |

### 4.2 Comparison Outcomes

For cases where frozen check passes (F1, F2), test all comparison outcomes:

| ID | Comparison Result | `compareTo()` Returns | Swap Occurs? |
|----|-------------------|----------------------|--------------|
| C1 | i > j | Positive (e.g., 1) | Yes |
| C2 | i < j | Negative (e.g., -1) | No |
| C3 | i == j | Zero | No |

### 4.3 Combined Test Cases

Full matrix combining frozen states with comparison outcomes:

| Test ID | Cell i State | Cell j State | i vs j | Expected Result | Swap Occurs? |
|---------|--------------|--------------|--------|-----------------|--------------|
| T01 | NONE | NONE | i > j | true | Yes |
| T02 | NONE | NONE | i < j | false | No |
| T03 | NONE | NONE | i == j | false | No |
| T04 | MOVABLE | NONE | i > j | true | Yes |
| T05 | MOVABLE | NONE | i < j | false | No |
| T06 | MOVABLE | NONE | i == j | false | No |
| T07 | NONE | MOVABLE | i > j | false | No |
| T08 | NONE | MOVABLE | i < j | false | No |
| T09 | MOVABLE | MOVABLE | i > j | false | No |
| T10 | IMMOVABLE | NONE | i > j | false | No |
| T11 | NONE | IMMOVABLE | i > j | false | No |
| T12 | IMMOVABLE | IMMOVABLE | i > j | false | No |

## 5. Detailed Test Cases

### T01: NONE/NONE with i > j — Swap Occurs

**Preconditions:**
- Cell at position 0: value = 10, frozen = NONE
- Cell at position 1: value = 5, frozen = NONE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `true`
- cells[0] now has value 5
- cells[1] now has value 10
- swapEngine.getSwapCount() == 1

---

### T02: NONE/NONE with i < j — No Swap

**Preconditions:**
- Cell at position 0: value = 5, frozen = NONE
- Cell at position 1: value = 10, frozen = NONE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 5
- cells[1] still has value 10
- swapEngine.getSwapCount() == 0

---

### T03: NONE/NONE with i == j — No Swap

**Preconditions:**
- Cell at position 0: value = 7, frozen = NONE
- Cell at position 1: value = 7, frozen = NONE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 7
- cells[1] still has value 7
- swapEngine.getSwapCount() == 0

---

### T04: MOVABLE/NONE with i > j — Swap Occurs

**Preconditions:**
- Cell at position 0: value = 10, frozen = MOVABLE
- Cell at position 1: value = 5, frozen = NONE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `true`
- cells[0] now has value 5
- cells[1] now has value 10
- swapEngine.getSwapCount() == 1

---

### T05: MOVABLE/NONE with i < j — No Swap

**Preconditions:**
- Cell at position 0: value = 5, frozen = MOVABLE
- Cell at position 1: value = 10, frozen = NONE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 5
- cells[1] still has value 10
- swapEngine.getSwapCount() == 0

---

### T06: MOVABLE/NONE with i == j — No Swap

**Preconditions:**
- Cell at position 0: value = 7, frozen = MOVABLE
- Cell at position 1: value = 7, frozen = NONE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 7
- cells[1] still has value 7
- swapEngine.getSwapCount() == 0

---

### T07: NONE/MOVABLE with i > j — Blocked by Frozen

**Preconditions:**
- Cell at position 0: value = 10, frozen = NONE
- Cell at position 1: value = 5, frozen = MOVABLE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 10 (no swap despite i > j)
- cells[1] still has value 5
- swapEngine.getSwapCount() == 0

---

### T08: NONE/MOVABLE with i < j — Blocked by Frozen

**Preconditions:**
- Cell at position 0: value = 5, frozen = NONE
- Cell at position 1: value = 10, frozen = MOVABLE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 5
- cells[1] still has value 10
- swapEngine.getSwapCount() == 0

---

### T09: MOVABLE/MOVABLE — Blocked by Frozen

**Preconditions:**
- Cell at position 0: value = 10, frozen = MOVABLE
- Cell at position 1: value = 5, frozen = MOVABLE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 10
- cells[1] still has value 5
- swapEngine.getSwapCount() == 0

---

### T10: IMMOVABLE/NONE — Blocked by Frozen

**Preconditions:**
- Cell at position 0: value = 10, frozen = IMMOVABLE
- Cell at position 1: value = 5, frozen = NONE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 10
- cells[1] still has value 5
- swapEngine.getSwapCount() == 0

---

### T11: NONE/IMMOVABLE — Blocked by Frozen

**Preconditions:**
- Cell at position 0: value = 10, frozen = NONE
- Cell at position 1: value = 5, frozen = IMMOVABLE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 10
- cells[1] still has value 5
- swapEngine.getSwapCount() == 0

---

### T12: IMMOVABLE/IMMOVABLE — Blocked by Frozen

**Preconditions:**
- Cell at position 0: value = 10, frozen = IMMOVABLE
- Cell at position 1: value = 5, frozen = IMMOVABLE
- SwapEngine swap count = 0

**Action:**
```java
boolean result = swapEngine.attemptSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells[0] still has value 10
- cells[1] still has value 5
- swapEngine.getSwapCount() == 0

---

## 6. Additional Test Cases

### T13: wouldSwap matches attemptSwap (positive case)

**Purpose:** Verify `wouldSwap` returns same result as `attemptSwap` would, without mutation.

**Preconditions:**
- Cell at position 0: value = 10, frozen = NONE
- Cell at position 1: value = 5, frozen = NONE

**Action:**
```java
boolean wouldResult = swapEngine.wouldSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `true`
- cells[0] still has value 10 (no mutation)
- cells[1] still has value 5 (no mutation)
- swapEngine.getSwapCount() == 0 (no increment)

---

### T14: wouldSwap matches attemptSwap (negative case)

**Purpose:** Verify `wouldSwap` returns false when frozen blocks swap.

**Preconditions:**
- Cell at position 0: value = 10, frozen = NONE
- Cell at position 1: value = 5, frozen = IMMOVABLE

**Action:**
```java
boolean wouldResult = swapEngine.wouldSwap(cells, 0, 1);
```

**Expected Outcomes:**
- Return value: `false`
- cells unchanged
- swapEngine.getSwapCount() == 0

---

### T15: Swap count accumulates correctly

**Purpose:** Verify swap count increments correctly across multiple swaps.

**Preconditions:**
- 3 cells: values [10, 5, 3], all frozen = NONE
- SwapEngine swap count = 0

**Actions:**
```java
swapEngine.attemptSwap(cells, 0, 1);  // 10 > 5, swaps
swapEngine.attemptSwap(cells, 1, 2);  // 10 > 3, swaps
swapEngine.attemptSwap(cells, 0, 1);  // 5 > 3, swaps
```

**Expected Outcomes:**
- swapEngine.getSwapCount() == 3
- Final array: [3, 5, 10]

---

### T16: resetSwapCount clears counter

**Purpose:** Verify reset functionality.

**Preconditions:**
- Perform a successful swap (count = 1)

**Action:**
```java
swapEngine.resetSwapCount();
```

**Expected Outcomes:**
- swapEngine.getSwapCount() == 0

---

## 7. Test Implementation Notes

### Test Cell Implementation

Tests require a simple `Cell` implementation for testing:

```java
public class IntCell implements Cell<IntCell> {
    private final int value;

    public IntCell(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int compareTo(IntCell other) {
        return Integer.compare(this.value, other.value);
    }
}
```

### Test Structure

- Use JUnit 5 `@ParameterizedTest` for the matrix tests (T01-T12)
- Use standard `@Test` for auxiliary tests (T13-T16)
- Use `@DisplayName` with test IDs for traceability
- Use `@BeforeEach` to reset state between tests

### Verification Checklist

For each swap test, verify:
1. Return value matches expected
2. Array elements are in expected positions
3. Swap count is correct
4. Original cell values are preserved (just moved)
