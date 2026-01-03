# Labs Directory

This directory serves as a playground and experimental sandbox for the Emergent Doom Engine project. It contains specialized experiments, proof-of-concept implementations, and research-oriented modules that extend the core engine's capabilities into new domains.

## Overview

Experiments in this directory typically follow a rigorous scientific protocol and are often organized into self-contained subdirectories. Each lab experiment aims to validate specific hypotheses regarding emergent behavior, sorting dynamics, or domain-specific applications.

## Directory Structure

- **`experiment-095/`**: Wave-CRISPR-Signal Experiment.
  - Investigates emergent PAM (Protospacer Adjacent Motif) detection using wavelet-leader signatures.
  - Implements a multi-phase validation protocol combining unsupervised emergent sorting with supervised learning (MLP).
  - Includes its own dedicated documentation, source code, and benchmarking tools.

## Experimental Workflow

Most experiments in the `lab/` folder follow a structured development pattern:

1.  **Scaffold**: Creation of the basic directory and package structure.
2.  **Protocol Definition**: Detailed documentation of the experimental methodology (e.g., `wave-crispr-signal.md`).
3.  **Core Implementation**: Development of experiment-specific cells, features, and runners.
4.  **Validation**: Execution of trials and comparison against success criteria.

## Usage

Each experiment directory contains its own `README.md` with specific instructions on how to build and run that particular experiment. Generally, experiments are designed to be executed from the project root or their own subfolder.

Example (Experiment-095):
```bash
# Compile and run according to lab/experiment-095/README.md
mvn clean package -DskipTests
java -cp target/emergent-doom-engine-0.1.0-alpha.jar lab.experiment095.WaveCrisprSignalExperiment
```

## Contributing to Labs

When adding new experiments to this folder:
- Create a dedicated subfolder (e.g., `experiment-XXX`).
- Include a comprehensive `README.md` and any necessary protocol documentation.
- Maintain the modular and self-contained nature of the experiment.
- Follow the project's coding standards and documentation patterns.
