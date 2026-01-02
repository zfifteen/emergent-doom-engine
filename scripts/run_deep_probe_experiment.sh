#!/bin/bash
# shellcheck disable=SC2164

# Deep Probe Experiment for Emergent Factorization Algorithm
# This script performs systematic parameter sweeps and harder test cases
# to characterize the emergent algorithm's behavior, scaling, and limits.
#
# Key differences from run_factorization_experiment.sh:
# 1. Sweeps array sizes (500, 1000, 2000, 5000) for selected targets
# 2. Varies trial counts (5, 10, 20, 50) to probe distribution properties
# 3. Includes harder composites where smallest factor may be >1000
# 4. Tests true semi-primes where both factors are large primes
# 5. Generates structured CSV output for statistical analysis

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}").." && pwd)"
JAR_PATH="$PROJECT_ROOT/target/emergent-doom-engine-0.1.0-alpha.jar"
LOG_FILE="$PROJECT_ROOT/scripts/run_deep_probe_experiment.log"
CSV_FILE="$PROJECT_ROOT/scripts/deep_probe_results.csv"

cd "$PROJECT_ROOT"

# Ensure output directories exist
mkdir -p "$(dirname "$LOG_FILE")"

echo "Deep Probe Experiment Started: $(date)" > "$LOG_FILE"
echo "target,trials,arraySize,threads,meanSteps,sortednessMean,monotonicityMean,convergenceRate" > "$CSV_FILE"

# Build the project
echo "Building project..."
mvn clean package -DskipTests >> "$LOG_FILE" 2>&1

# Helper function to run a single experiment and extract key metrics
# Usage: run_probe <target> <trials> <arraySize> <threads>
run_probe() {
    local target=$1
    local trials=$2
    local arraySize=$3
    local threads=$4
    
    echo "Testing target: $target (Trials: $trials, ArraySize: $arraySize, Threads: $threads)" | tee -a "$LOG_FILE"
    
    # Run experiment and capture output
    local output=$(java -cp "$JAR_PATH" com.emergent.doom.examples.FactorizationExperiment "$target" "$trials" "$arraySize" "$threads" 2>&1)
    echo "$output" >> "$LOG_FILE"
    echo "------------------------------------------------------------" >> "$LOG_FILE"
    
    # Extract metrics using grep and awk
    local meanSteps=$(echo "$output" | grep -oP 'Mean Steps: \K[0-9.]+' | head -1)
    local sortedness=$(echo "$output" | grep -oP 'Sortedness.*?mean=\K[0-9.]+' | head -1)
    local monotonicity=$(echo "$output" | grep -oP 'Monotonicity.*?mean=\K[0-9.]+' | head -1)
    local convergence=$(echo "$output" | grep -oP 'Convergence Rate: \K[0-9.]+' | head -1)
    
    # Append to CSV
    echo "$target,$trials,$arraySize,$threads,$meanSteps,$sortedness,$monotonicity,$convergence" >> "$CSV_FILE"
}

#############################################################################
# EXPERIMENT 1: Array Size Sweep on Representative Targets
# Goal: Measure how dynamics scale with search-space size
#############################################################################

echo ""
echo "=== EXPERIMENT 1: Array Size Sweep ==="
echo ""

# Small magnitude (1e5)
for size in 500 1000 2000 5000; do
    run_probe 100697 10 $size 4
done

# Medium magnitude (1e9)
for size in 500 1000 2000 5000; do
    run_probe 999997979 10 $size 4
done

# Large magnitude (1e12)
for size in 500 1000 2000 5000; do
    run_probe 1000000003349 10 $size 4
done

#############################################################################
# EXPERIMENT 2: Trial Count Sweep to Probe Distributions
# Goal: Assess variance and multi-modality in convergence behavior
#############################################################################

echo ""
echo "=== EXPERIMENT 2: Trial Count Sweep ==="
echo ""

# Fixed array size, varying trials
for trials in 5 10 20 50; do
    run_probe 100697 $trials 1000 4
    run_probe 999997979 $trials 1000 4
    run_probe 1000000003349 $trials 1000 4
done

#############################################################################
# EXPERIMENT 3: Hard Composites (smallest factor possibly >1000)
# Goal: Test behavior when targets have no small factors in range
#############################################################################

echo ""
echo "=== EXPERIMENT 3: Hard Composites ==="
echo ""

# Products of two primes both near 1000
# 1009 × 1013 = 1022117
run_probe 1022117 20 1000 4
run_probe 1022117 20 2000 4

# 1009 × 10007 = 10097063
run_probe 10097063 20 1000 4
run_probe 10097063 20 2000 4

# 10007 × 10009 = 100160063
run_probe 100160063 20 1000 4
run_probe 100160063 20 5000 4

# 10007 × 100003 = 1000100021
run_probe 1000100021 20 1000 4
run_probe 1000100021 20 5000 4

#############################################################################
# EXPERIMENT 4: True Large Semi-Primes
# Goal: Probe absolute limits - both factors are large primes
#############################################################################

echo ""
echo "=== EXPERIMENT 4: Large Semi-Primes ==="
echo ""

# 9973 × 10007 = 99800011 (both factors >1000)
run_probe 99800011 20 2000 4
run_probe 99800011 20 5000 4

# 99991 × 100003 = 9999600073 (both factors >>1000)
run_probe 9999600073 20 2000 4
run_probe 9999600073 20 10000 4

#############################################################################
# EXPERIMENT 5: Thread Count Sweep
# Goal: Assess parallelism benefit vs overhead
#############################################################################

echo ""
echo "=== EXPERIMENT 5: Thread Count Sweep ==="
echo ""

for threads in 1 2 4 8; do
    run_probe 999997979 10 1000 $threads
    run_probe 1000000003349 10 1000 $threads
done

#############################################################################
# EXPERIMENT 6: Edge Cases and Special Numbers
# Goal: Test algorithm robustness on degenerate inputs
#############################################################################

echo ""
echo "=== EXPERIMENT 6: Edge Cases ==="
echo ""

# Perfect square of large prime: 997^2 = 994009
run_probe 994009 20 1000 4
run_probe 994009 20 2000 4

# Product of small and large: 2 × 500000003 = 1000000006
run_probe 1000000006 20 1000 4

# Highly composite (not semi-prime): 2^3 × 3^2 × 5 × 7 = 2520
run_probe 2520 20 1000 4

echo ""
echo "Deep probe complete. Results logged to:"
echo "  Log: $LOG_FILE"
echo "  CSV: $CSV_FILE"
echo ""
echo "To analyze results:"
echo "  - Import $CSV_FILE into your data analysis tool"
echo "  - Plot meanSteps vs arraySize for scaling analysis"
echo "  - Examine convergenceRate for hard vs easy targets"
echo "  - Check metric distributions across trials"
