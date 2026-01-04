# Chapter 1: Cell Foundations

The Cell interface is the foundation of the Emergent Doom Engine. It defines the minimal contract for domain-agnostic sorting, enabling emergent computation across diverse problem domains without specialized infrastructure.

## Purpose

Tests in this package validate that the Cell interface maintains its **lightweight, domain-agnostic design**. Cells are pure `Comparable<T>` data carriers with zero engine-specific state, ensuring maximum flexibility and reusability.

## Concepts Covered

### Minimal Interface Contract
- Cells extend only `Comparable<T>` - no engine dependencies
- Single required method: `compareTo(T other)`
- Optional `getValue()` for metrics (throws exception if unsupported)

### Pure Data Carriers
- Zero engine-specific metadata (no algotype, sort direction, position fields)
- All sorting metadata managed externally via `CellMetadata` providers
- Implementations decide comparison logic based on domain needs

### Domain Agnostic Design
- Framework provides no domain-specific functionality
- Factorization is one example application, not the primary purpose
- Same interface works for sorting numbers, strings, custom objects, etc.

## Prerequisites

**None** - this is the starting point for understanding the EDE framework.

Recommended background:
- Basic Java generics (`<T extends Cell<T>>`)
- `Comparable<T>` interface contract
- Value vs reference semantics

## Test Files

### CellInterfaceTest.java

Validates the Cell interface contract itself (not implementations).

**Key Tests:**
- `testInterfaceExtendsOnlyComparable()` - Ensures Cell has no extra superinterfaces
- `testMinimalImplementation()` - Verifies only `compareTo()` is required
- `testComparableContract()` - Confirms Cell instances can be ordered
- `testNoAdditionalMethods()` - Checks for no abstract methods beyond `Comparable`

**Why it matters:** Prevents interface bloat and maintains domain-agnostic purity.

**Link to source:** [Cell.java](../../../../../main/java/com/emergent/doom/cell/Cell.java)

### GenericCellTest.java

Tests the `GenericCell<T>` implementation, a general-purpose cell wrapper.

**Key Tests:**
- Value wrapping and extraction
- Comparison delegation to wrapped type
- Null handling
- Generic type safety

**Why it matters:** Demonstrates how to wrap existing `Comparable` types as cells.

**Link to source:** [GenericCell.java](../../../../../main/java/com/emergent/doom/cell/GenericCell.java)

### SelectionCellTest.java

Tests selection sort-specific cell behavior.

**Key Tests:**
- Minimum finding logic
- Position tracking during selection
- Swap semantics for selection algotype

**Why it matters:** Shows how cells can encode algorithm-specific behavior while maintaining the common interface.

**Link to source:** [SelectionCell.java](../../../../../main/java/com/emergent/doom/cell/SelectionCell.java)

## Usage Examples

### Implementing a Minimal Cell

The simplest possible cell wraps an integer value:

```java
public class IntCell implements Cell<IntCell> {
    private final int value;

    public IntCell(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(IntCell other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public int getValue() {
        return value;
    }
}
```

**Key points:**
- No algotype, sort direction, or position fields
- Pure domain logic (integer comparison)
- Optional `getValue()` for metrics

### Implementing a Domain-Specific Cell

For a custom domain (e.g., sorting people by age):

```java
public class PersonCell implements Cell<PersonCell> {
    private final String name;
    private final int age;

    public PersonCell(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public int compareTo(PersonCell other) {
        // Domain-specific comparison: sort by age, then name
        int ageComparison = Integer.compare(this.age, other.age);
        if (ageComparison != 0) {
            return ageComparison;
        }
        return this.name.compareTo(other.name);
    }

    // getValue() intentionally not implemented - no single "value" concept
}
```

**Key points:**
- Cell encapsulates domain logic (age-then-name sorting)
- No need to implement `getValue()` if it doesn't make sense
- Comparison logic is entirely application-defined

### Using GenericCell for Quick Prototyping

Wrap existing `Comparable` types without creating new classes:

```java
// Wrap integers
GenericCell<Integer> cell1 = new GenericCell<>(42);
GenericCell<Integer> cell2 = new GenericCell<>(100);
System.out.println(cell1.compareTo(cell2)); // -1 (42 < 100)

// Wrap strings
GenericCell<String> cellA = new GenericCell<>("apple");
GenericCell<String> cellB = new GenericCell<>("banana");
System.out.println(cellA.compareTo(cellB)); // -1 ("apple" < "banana")
```

**Key points:**
- No boilerplate for simple cases
- Delegates to wrapped type's `compareTo()`
- Type-safe via generics

## Architecture Insights

### External Metadata Management

The Cell interface deliberately **excludes** engine-specific metadata. This was a key architectural decision to achieve true domain-agnostic design.

**Before (heavyweight cells):**
```java
interface Cell<T> {
    Algotype getAlgotype();       // ❌ Engine-specific
    SortDirection getDirection(); // ❌ Engine-specific
    int getIdealPosition();       // ❌ Engine-specific
    int compareTo(T other);
}
```

**After (lightweight cells):**
```java
interface Cell<T> extends Comparable<T> {
    // ✅ Only domain-agnostic comparison
    // All metadata managed externally via CellMetadata[]
}
```

Execution engines use `IntFunction<CellMetadata>` providers to manage metadata separately:

```java
IntFunction<CellMetadata> metadataProvider = index -> 
    new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);
    
SynchronousExecutionEngine<IntCell> engine = 
    new SynchronousExecutionEngine<>(cells, swapEngine, probe, 
                                     convergenceDetector, metadataProvider);
```

**Why this matters:**
- Cells can be reused across different engines with different algotypes
- Domain objects don't need to "know" about the sorting framework
- Framework remains truly domain-agnostic

### Comparable Contract Requirements

Implementations **must** satisfy the `Comparable<T>` contract:

1. **Consistency with equals**: If `a.equals(b)`, then `a.compareTo(b) == 0`
2. **Transitivity**: If `a < b` and `b < c`, then `a < c`
3. **Reflexivity**: `a.compareTo(a) == 0`
4. **Symmetry**: `sgn(a.compareTo(b)) == -sgn(b.compareTo(a))`

Violating these guarantees can lead to undefined behavior during sorting.

## Common Patterns

### Immutable Cells (Recommended)

Make cell fields `final` for thread safety and predictability:

```java
public class ImmutableCell implements Cell<ImmutableCell> {
    private final int value; // ✅ Final - cannot change after construction

    public ImmutableCell(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(ImmutableCell other) {
        return Integer.compare(this.value, other.value);
    }
}
```

### Mutable Cells (Use with Caution)

Some algorithms may require mutable state (e.g., selection sort position tracking):

```java
public class MutableCell implements Cell<MutableCell> {
    private final int value;
    private int position; // ⚠️ Mutable state

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int compareTo(MutableCell other) {
        return Integer.compare(this.value, other.value);
    }
}
```

**Caution:** Mutable cells require careful synchronization in parallel execution.

## Troubleshooting

### "Cell doesn't have getValue()"

If you see `UnsupportedOperationException` when using metrics:

**Problem:** Your cell doesn't implement `getValue()` and metrics are trying to extract values.

**Solution 1:** Implement `getValue()` if your cell has a natural value representation:
```java
@Override
public int getValue() {
    return this.myDomainSpecificValue;
}
```

**Solution 2:** Use metrics that don't require `getValue()` (Spearman Distance works with any `Comparable`).

### "ClassCastException during compareTo()"

If you see `ClassCastException` at runtime:

**Problem:** Type parameter mismatch - comparing incompatible cell types.

**Solution:** Ensure `Cell<T>` where `T` is the concrete cell class itself:
```java
// ✅ Correct
class MyCell implements Cell<MyCell> { ... }

// ❌ Wrong
class MyCell implements Cell<SomeOtherCell> { ... }
```

## Next Steps

Now that you understand cells as pure data carriers, proceed to:

**[Chapter 2: Swap Mechanics](../swap/README.md)** - Learn how cells interact locally through swaps, enabling decentralized computation.

**Also see:**
- [Chapter 3: Execution Engines](../execution/README.md) - How metadata providers work with lightweight cells
- [Production Cell Examples](../../../../../main/java/com/emergent/doom/examples/) - Real-world cell implementations

---

**[← Back to Test Suite Home](README.md)** | **[Next: Swap Mechanics →](../swap/README.md)**
