# Scripts Directory

This directory contains various scripts used for experiments, analysis, and data management in the Emergent Doom Engine project.

## Directory Structure

- **`analysis/`**: Python and shell scripts for data analysis and validation.
  - `analyze_scaling.py`: Python script for statistical analysis of scaling results.
  - `run_linear_scaling_validation.sh`: Entry point for the progressive linear scaling validation ladder.
  - `run_scaling_verification.sh`: Memory-optimized scaling verification experiment.
- **`experiments/`**: Shell scripts for running different types of factorization experiments.
  - `run_rsa_subset.sh`: Rapid testing on a representative subset of RSA numbers.
  - `run_factorization_experiment.sh`: Batch factorization experiment over a target dataset.
  - `run_deep_probe_experiment.sh`: Systematic parameter sweeps and characterization of algorithm dynamics.
- **`data/`**: Input data files and target lists for scripts.
  - `rsa_targets.txt`: Dataset of RSA numbers and targets for factorization.
  - `scaling_results.csv`: Raw data from scaling experiments.
- **`logs/`**: Log files generated during script execution.
  - `run_rsa_subset.log`: Output from the RSA subset experiment.
  - `run_factorization_experiment.log`: Output from the batch experiment.
  - `run_deep_probe_experiment.log`: Detailed logs from the parameter sweep experiment.

## Usage

Most scripts are designed to be run from the project root:

```bash
./scripts/experiments/run_rsa_subset.sh
```

Ensure the project is built before running experiments that depend on the JAR file:

```bash
mvn clean package -DskipTests
```
