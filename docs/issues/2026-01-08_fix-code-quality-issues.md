# Code Quality Fixes Plan - 2026-01-08_fix-code-quality-issues

## Summary
This branch addresses 7 code quality issues identified in `src/main/java`:
- High: Null safety in `DelayedGratificationIndex.java`, type safety casts in `FactorCell.java`.
- Medium: Inconsistent logging (System.out in demos), wildcard imports in 6 files.
- Low: Inconsistent null handling in `DelayedGratificationIndex.java`.

Goal: Fix issues, verify with `mvn clean test`, create PR if tests pass.

## Detailed Plan

### 1. Null Safety (High - DelayedGratificationIndex.java:49)
- Read file.
- Add null check: `if (cells[i] != null) { double value = cells[i].getValue(); ... }` else skip or throw descriptive exception.
- Update loop to handle nulls gracefully.

### 2. Type Safety (High - FactorCell.java:400,425)
- Read file.
- In `hasGreaterValueThan` and `compareTo`: Add `if (!(other instanceof FactorCell)) return false;` before cast.

### 3. Logging Improvements (Medium - Demos)
- Introduce SLF4J: Add `import org.slf4j.Logger; import org.slf4j.LoggerFactory;` to each demo file.
- Replace `System.out.println` with `logger.info(...)`.
- Files: FactorizationDemo.java (lines 26-112), NewCellArchitectureDemo.java (35-176), StatisticalAnalysisDemo.java (18-103).
- Add logger initialization: `private static final Logger logger = LoggerFactory.getLogger(ClassName.class);`.

### 4. Unused Imports (Medium - 6 Files)
- Replace `import java.util.*;` with explicit: `import java.util.List; import java.util.ArrayList; import java.util.Map;` (adjust per file usage).
- Files: FactorizationExperiment.java:9, FactorCellFactory.java:3, SortingCellFactory.java:5, CellBasedExecutionEngine.java:5, NewCellArchitectureDemo.java:6, Probe.java:8.

### 5. Inconsistent Null Handling (Low - DelayedGratificationIndex.java:46-47,78)
- Align with main loop: Add null checks in snapshot and average methods.

## Verification
- After each fix batch, run `mvn clean test`.
- If all pass, commit changes, push branch, create PR with this plan as body.
- If fails, revert and diagnose.

## Next Steps
Implement fixes incrementally, testing after each section.