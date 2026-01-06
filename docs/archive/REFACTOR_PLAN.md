# Clean Break Refactoring Plan

## Objective
Complete integration of new cell architecture with NO backwards compatibility, NO adapters, clean removal of old architecture.

## Files to DELETE
1. `src/main/java/com/emergent/doom/chimeric/PercentageAlgotypeProvider.java` - Position-based algotype binding (OLD)
2. `src/main/java/com/emergent/doom/chimeric/AlgotypeProvider.java` - Interface for old approach (OLD)
3. `src/main/java/com/emergent/doom/execution/CellMetadata.java` - Parallel metadata arrays (OLD)
4. `src/test/java/com/emergent/doom/execution/CellMetadataTest.java` - Tests for old approach (OBSOLETE)

## Files to CREATE
1. `src/main/java/com/emergent/doom/factory/SortingCellFactory.java` - Creates cells with embedded algotypes
2. `src/main/java/com/emergent/doom/execution/CellBasedExecutionEngine.java` - Engine using new cell architecture

## Files to MODIFY
1. `src/main/java/com/emergent/doom/examples/ChimericClusteringExperiment.java` - Update to use new cell factory
2. `src/main/java/com/emergent/doom/examples/NewCellArchitectureDemo.java` - Update to use cell factory
3. Various test files - Update or remove as needed

## Implementation Steps

### Step 1: Create Cell Factory
- Create `SortingCellFactory` that generates cells with algotypes embedded
- Support percentage-based distribution (but algotypes bound to cells, not positions)

### Step 2: Create New Engine
- `CellBasedExecutionEngine` that works with `AbstractSortingCell` directly
- Builds `NeighborhoodView` for each cell based on algotype
- No metadata arrays - reads algotype from cell directly

### Step 3: Delete Old Files
- Remove `PercentageAlgotypeProvider`
- Remove `AlgotypeProvider` interface
- Remove `CellMetadata`
- Remove `CellMetadataTest`

### Step 4: Update Examples
- Update `ChimericClusteringExperiment` to use new architecture
- Update `NewCellArchitectureDemo` to use factory

### Step 5: Update/Remove Obsolete Tests
- Remove tests that depend on old architecture
- Update tests to use new cell-based approach

### Step 6: Validate
- Run all tests
- Ensure clean build
- Verify demo applications work

## Success Criteria
- ✅ No `CellMetadata` references
- ✅ No `PercentageAlgotypeProvider` references
- ✅ All tests pass
- ✅ Demo applications work
- ✅ Clean, simple architecture with cells carrying algotypes
