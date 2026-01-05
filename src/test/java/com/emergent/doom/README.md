# Emergent Doom Engine Test Suite

---
**Documentation Version:** Commit [7e77864](https://github.com/zfifteen/emergent-doom-engine/commit/7e77864a5553f144acb55bff7115b22c3d2919cb)  
**Last Updated:** 2026-01-05
---

The EDE test suite is an **executable instruction manual** that teaches the framework through progressively complex, well-documented test cases. Tests are organized into five chapters that build from foundational concepts to advanced features, providing a complete learning path for understanding emergent computation.

## How to Navigate This Test Suite

This documentation follows a **book-like structure** where each chapter builds on previous concepts. Start at Chapter 1 and progress sequentially for the best learning experience.

Each package README includes:
- **Purpose**: What the package tests and why it matters
- **Concepts Covered**: Key EDE principles demonstrated
- **Prerequisites**: What to understand before reading
- **Test Files**: Annotated catalog of all tests
- **Usage Examples**: Working code snippets
- **Next Steps**: Link to the next logical chapter

## Table of Contents

### Chapter 1: Foundations

**Start here** if you're new to the EDE framework.

1. **[cell/](cell/README.md)** - The Cell Interface and Lightweight Cells
   - Minimal contract for domain-agnostic sorting
   - Pure `Comparable` data carriers
   - Zero engine-specific state

### Chapter 2: Core Components

Build understanding of the engine's building blocks.

2. **[swap/](swap/README.md)** - Swap Mechanics and Frozen Cells
   - Local agent interactions
   - Unreliable substrate simulation
   - Frozen cell constraints

3. **[probe/](probe/README.md)** - Execution Trajectory Recording
   - Observability infrastructure
   - Snapshot capture and replay
   - Emergent dynamics tracking

4. **[metrics/](metrics/README.md)** - Quality Measures
   - Monotonicity (disorder remaining)
   - Sortedness (progress toward goal)
   - Delayed Gratification (temporary setbacks)
   - Spearman Distance (rank correlation)

### Chapter 3: Execution Engines

Learn how the engine orchestrates emergent computation.

5. **[execution/](execution/README.md)** - Execution Architecture
   - Synchronous vs parallel execution
   - External metadata management
   - Convergence detection
   - Doom (inevitability toward target state)

### Chapter 4: Advanced Features

Explore complex emergent behaviors.

6. **[chimeric/](chimeric/README.md)** - Mixed-Algotype Populations
   - Clustering and segregation
   - Multi-strategy systems
   - Emergent organization patterns

7. **[experiment/](experiment/README.md)** - Multi-Trial Experiments
   - Batch execution framework
   - Statistical validation
   - Experimental design patterns

8. **[analysis/](analysis/README.md)** - Trajectory Analysis
   - Post-hoc visualization
   - Emergent pattern detection
   - Metrics over time

### Chapter 5: Validation & Tooling

Verify correctness and compare with traditional approaches.

9. **[traditional/](traditional/README.md)** - Classical Algorithm Comparison
   - Functional equivalence validation
   - Performance benchmarking
   - Determinism verification

10. **[validation/](validation/README.md)** - Integration Testing
    - End-to-end workflows
    - System-level validation
    - Scaling behavior verification

11. **[visualization/](visualization/README.md)** - Visualization Tools
    - Debugging utilities
    - Presentation tools
    - Trajectory rendering

## Reading Test Code

All test methods in this suite follow a **user-story format** for clarity:

```java
/**
 * PURPOSE: As a [role] I want to [action] so that I can [outcome].
 *
 * INPUTS: [What data/objects are provided to the test]
 * EXPECTED OUTPUT: [What behavior/result is expected]
 * TEST DATA: [Specific values used in the test]
 * REPRODUCTION: [How to manually reproduce this scenario]
 */
@Test
@DisplayName("Human-readable test description")
void testMethodName() {
    // Test implementation
}
```

This format makes tests self-documenting and reveals the **why** behind each validation.

## Learning Strategy

### For Framework Users

1. Start with [cell/](cell/README.md) to understand the minimal interface
2. Read [swap/](swap/README.md) to see local interactions
3. Progress through [execution/](execution/README.md) to compose a complete system
4. Explore advanced features based on your use case

### For Framework Contributors

1. Follow the user path through Chapters 1-3
2. Study [chimeric/](chimeric/README.md) for emergent phenomena research
3. Review [validation/](validation/README.md) for correctness guarantees
4. Consult [metrics/](metrics/README.md) for quantifying emergent behavior

### For Researchers

1. Understand the basics (Chapters 1-2)
2. Focus on [metrics/](metrics/README.md) for problem-space navigation measures
3. Explore [chimeric/](chimeric/README.md) for clustering and organization
4. Use [analysis/](analysis/README.md) for trajectory-level insights

## Framework Principles Demonstrated

Every test in this suite illustrates one or more EDE core principles:

- **Emergent Computation**: Solutions arise from collective dynamics, not programmed algorithms
- **Decentralized Control**: Cells interact locally through comparisons, no global orchestration
- **Domain Agnostic**: Tests work across problem domains (factorization is just one example)
- **Robust on Unreliable Substrates**: Frozen cells and error tolerance
- **Delayed Gratification**: Temporary disorder increases for long-term progress
- **Clustering in Chimeric Systems**: Spontaneous organization by strategy type

## Reference Materials

- **[Production Code](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/src/main/java/com/emergent/doom/)** - Implementations being tested
- **[Main README](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/README.md)** - Project overview and quick start
- **[Levin et al. (2024)](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/docs/theory/)** - Theoretical foundation
- **[REQUIREMENTS.md](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/docs/requirements/REQUIREMENTS.md)** - Technical specifications

## Quick Start: Running Tests

```bash
# Run all tests
mvn test

# Run a specific package
mvn test -Dtest="com.emergent.doom.cell.*"

# Run a specific test class
mvn test -Dtest="CellInterfaceTest"

# Run with verbose output
mvn test -X
```

## Contributing Test Documentation

When adding new tests:
1. Follow the user-story JavaDoc format
2. Update the relevant package README
3. Add cross-references to related tests
4. Include working code examples
5. Link to production code being tested

---

**Ready to start?** Begin your journey with [Chapter 1: Cell Foundations](cell/README.md).

## Maintaining Documentation Links

This test suite uses GitHub permalinks for all production code references to ensure version stability. When making significant code changes:

```bash
# Update all permalinks to a new commit SHA
./scripts/update_permalink_base.sh <new_commit_sha>

# Verify all links are accessible
./scripts/verify_permalinks.sh
```

**Cross-documentation links** (between test suite READMEs) remain as relative paths for easier local browsing.
