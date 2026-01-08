# Documentation Index

This directory contains the comprehensive documentation for the Emergent Doom Engine (EDE), including research findings, implementation details, experimental lab notes, and theoretical background.

## Table of Contents

- [Core Documentation](#core-documentation)
- [Directory Overview](#directory-overview)
  - [archive/](archive/) - Historical reviews and fix summaries
  - [findings/](findings/) - Experimental results and scaling analysis
  - [implementation/](implementation/) - Technical debt and gap analysis
  - [lab/](lab/) - Active research and exploratory scripts
  - [requirements/](requirements/) - System specifications and test requirements
  - [theory/](theory/) - Academic foundation and reference papers

---

## Core Documentation

The following documents provide high-level context for the current state of the project:

- **[MIGRATION_CELL_ARCHITECTURE.md](MIGRATION_CELL_ARCHITECTURE.md)**: **NEW** - Comprehensive guide for migrating from position-based to cell-based algotype architecture (PR #113). Essential reading for understanding the current codebase.
- **[MIGRATION_v2.0.md](MIGRATION_v2.0.md)**: Guidance for upgrading from per-cell threading to batch-level parallelism.
- **[REQUIREMENTS_GAP_ANALYSIS.md](requirements/REQUIREMENTS_GAP_ANALYSIS.md)**: An audit of the current Java implementation against the core requirements, identifying blockers for upcoming features.
- **[CLUSTERING_PRIMITIVE_SPEC.md](requirements/CLUSTERING_PRIMITIVE_SPEC.md)**: Specification for using clustering as a computational primitive in non-sorting applications.
- **[METRIC_DASHBOARD_BASELINE.md](findings/METRIC_DASHBOARD_BASELINE.md)**: Baseline definitions for performance and emergence metrics.
- **[factorization_ui_requirements.md](requirements/factorization_ui_requirements.md)**: Requirements for the experimental factorization visualization interface.

## Directory Overview

### [archive/](archive/)
Contains historical documentation, pull request reviews, and summaries of past critical fixes.
- **CODE_REVIEW_PR50.md**: Detailed review of the threading model refactor.
- **CRITICAL_FIXES_SUMMARY.md**: Overview of essential bug fixes applied during early development.

### [findings/](findings/)
The primary repository for experimental data and analysis reports.
- **[README.md](findings/README.md)**: Main index of all conducted experiments.
- **factorization-exp-001/ to factorization-exp-004/**: Individual folders containing artifacts for each major factorization experiment.

### [implementation/](implementation/)
Technical implementation details and roadmap tracking.
- **GAPS-CLAUDE.md**: Tracking of implementation gaps identified during AI-assisted development sessions.

### [lab/](lab/)
Exploratory research, draft algorithms, and "work-in-progress" experiments that haven't yet been formalized into the findings directory.
- **distributed_euclidean_remaindercell.md**: Early drafts of the distributed factorization algorithm.
- **experiment-095/**: Wave-CRISPR-Signal PAM detection experiment applying emergent sorting to wavelet-leader features for bioinformatics domain validation.

### [requirements/](requirements/)
Formal specifications that define how the engine should behave.
- **[REQUIREMENTS.md](requirements/REQUIREMENTS.md)**: The "ground truth" specification for the Emergent Doom Engine.
- **SwapEngineTestSpec.md**: Detailed requirements for verifying the swap mechanics.

### [theory/](theory/)
The academic and theoretical foundation for the project.
- **[2401.05375v1.md](theory/2401.05375v1.md)**: Markdown summary of the core research paper "Sorting as a Model of Morphogenesis".
- **2401.05375v1.pdf**: The original research paper by Zhang, Goldstein, and Levin.

## Recent Changes

### Cell Architecture Refactor (January 2026)
PR [#113](https://github.com/zfifteen/emergent-doom-engine/pull/113) implemented a clean break refactoring from position-based to cell-based algotype binding. This fundamental architectural change achieves Levin-aligned morphogenetic clustering semantics. See [MIGRATION_CELL_ARCHITECTURE.md](MIGRATION_CELL_ARCHITECTURE.md) for migration guidance.

**Key Changes:**
- Algotypes are now intrinsic cell properties that travel WITH cells during swaps
- New cell hierarchy: `AbstractCell` → `AbstractSortingCell` → concrete implementations
- Replaced `PercentageAlgotypeProvider` with `SortingCellFactory`
- Replaced `SynchronousExecutionEngine` with `CellBasedExecutionEngine`
- Produces characteristic 18.30% aggregation variance signature matching Levin research

### Snapshot Metadata Guardrails (January 2026)
- Skip invalid algotype labels when computing snapshot type distributions to avoid export-time crashes when metadata is unavailable.

---
*Last updated: January 6, 2026*
