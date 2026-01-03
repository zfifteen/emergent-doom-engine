### Technical Implementation Plan: Lightweight Cell Refactoring

#### 0. Rationale
The primary motivation for this refactoring is to achieve **true generality** in the `Cell` interface. By stripping the `Cell` of all engine-specific state and metadata, we transform it into a pure data carrier that only needs to implement the `Comparable` contract. This allows domain integrators to plug in any object—regardless of its internal complexity—without needing to implement sorting-specific interfaces. Moving the "intelligence" and state management into the `ParallelExecutionEngine` ensures a clean separation of concerns, simplifies the addition of complex algorithms like Quicksort and Merge Sort, and makes the system significantly more extensible for future domain-specific applications.

#### 1. Objective
Refactor the `Cell` interface to a minimal `Comparable` contract, moving all sorting-specific state and behavioral metadata (algotype, direction, ideal position, group boundaries) into the `ParallelExecutionEngine`. This achieves "true generality," allowing any `Comparable` object to be sorted without engine-specific boilerplate.

#### 2. Phase 1: Engine State Centralization
Instead of cells carrying their own metadata, the `ParallelExecutionEngine` will manage a "shadow" state.

- **Create `CellMetadata` Record:**
  ```java
  public record CellMetadata(
      Algotype algotype,
      SortDirection direction,
      AtomicInteger idealPos,
      // Optional: group boundaries if needed by engine
      int leftBoundary,
      int rightBoundary
  ) {
      public CellMetadata copy() {
          return new CellMetadata(algotype, direction, new AtomicInteger(idealPos.get()), leftBoundary, rightBoundary);
      }
  }
  ```

- **Update `ParallelExecutionEngine` Storage:**
  - Add `private CellMetadata[] metadata;`
  - Initialize metadata during engine setup (e.g., in constructor or a new `initializeMetadata(AlgotypeProvider provider)` method).

#### 3. Phase 2: Refactoring Engine Logic
Update the engine to query its internal `metadata` array rather than the cell objects.

- **Swap Logic:**
  - Update `shouldSwapWithDirection(int i, int j, ...)` to use `metadata[i]` and `metadata[j]`.
  - Update `executeSwaps`: When a swap occurs between `cells[i]` and `cells[j]`, the corresponding `metadata[i]` and `metadata[j]` must also be swapped to ensure the state stays attached to the logical "agent" (the cell).

- **Selection Sort State:**
  - `getIdealPosition(int index)` will now return `metadata[index].idealPos().get()`.
  - `incrementIdealPosition(int index)` will call `metadata[index].idealPos().incrementAndGet()`.

- **Neighbor Discovery:**
  - `getNeighborsForAlgotype(int i, ...)` will use `metadata[i].algotype()`.

#### 4. Phase 3: Stripping the Cell Interface
Clean up the `Cell` contract and its supporting interfaces.

- **`Cell.java`:**
  ```java
  public interface Cell<T extends Cell<T>> extends Comparable<T> {
      // Purely Comparable. No other methods required.
  }
  ```
- **Remove/Deprecate Internal Interfaces:** `HasAlgotype`, `HasIdealPosition`, `HasSortDirection`, `HasStatus`, `HasGroup`, `HasValue`.

#### 5. Phase 4: Simplifying Cell Implementations
`GenericCell` and others become lightweight wrappers.

- **`GenericCell.java`:**
  - Remove `algotype`, `sortDirection`, `idealPos`, `group`, `status`, `leftBoundary`, `rightBoundary`.
  - Retain only the domain `value` (e.g., `int` or `T`).
  - Implement `compareTo` using the value.

#### 6. Phase 5: Verification & Integration
- **Compatibility:** Ensure `ChimericExperimentConfig` and `AlgotypeProvider` work with the new engine-centric metadata initialization.
- **Regression Testing:** Run `ParallelExecutionEngineTest` and `FactorizationExperiment` to verify that emergent behaviors (Bubble, Insertion, Selection) remain identical despite the structural move.
- **Generality Test:** Create a new test case using a standard `Integer` wrapper that implements `Cell` to prove that no extra state is needed.

#### 7. Benefits
- **Integrator Ease:** Users only need to implement `compareTo`.
- **Engine Power:** The engine can now implement complex algorithms like Quicksort (which needs partition state) without polluting the Cell data structure.
- **Performance:** Potential for better cache locality by separating data (Cells) from control state (Metadata).
