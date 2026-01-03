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
- **[LINEAR_SCALING_ANALYSIS.md](findings/LINEAR_SCALING_ANALYSIS.md)**: Verification of the O(n) time complexity discovery.
- **factorization-exp-001/ to factorization-exp-004/**: Individual folders containing artifacts for each major factorization experiment.

### [implementation/](implementation/)
Technical implementation details and roadmap tracking.
- **GAPS-CLAUDE.md**: Tracking of implementation gaps identified during AI-assisted development sessions.

### [lab/](lab/)
Exploratory research, draft algorithms, and "work-in-progress" experiments that haven't yet been formalized into the findings directory.
- **distributed_euclidean_remaindercell.md**: Early drafts of the distributed factorization algorithm.
- **experiment-005/**: Active research into wave-based signaling between cells.

### [requirements/](requirements/)
Formal specifications that define how the engine should behave.
- **[REQUIREMENTS.md](requirements/REQUIREMENTS.md)**: The "ground truth" specification for the Emergent Doom Engine.
- **SwapEngineTestSpec.md**: Detailed requirements for verifying the swap mechanics.

### [theory/](theory/)
The academic and theoretical foundation for the project.
- **[2401.05375v1.md](theory/2401.05375v1.md)**: Markdown summary of the core research paper "Sorting as a Model of Morphogenesis".
- **2401.05375v1.pdf**: The original research paper by Zhang, Goldstein, and Levin.

---
*Last updated: January 2, 2026*
