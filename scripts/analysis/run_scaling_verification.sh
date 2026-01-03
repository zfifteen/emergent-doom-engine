#!/bin/bash
set -euo pipefail

# Scaling Verification Experiment (Memory Optimized)
# Goal: Rigorously test O(n) hypothesis for emergent factorization up to n=4000

PROJECT_ROOT="/Users/velocityworks/IdeaProjects/emergent-doom-engine"
JAR_PATH="$PROJECT_ROOT/target/emergent-doom-engine-0.1.0-alpha.jar"
CSV_FILE="$PROJECT_ROOT/scripts/data/scaling_results.csv"
ANALYSIS_FILE="$PROJECT_ROOT/scripts/logs/scaling_analysis.txt"

# Experimental parameters
FIXED_THREADS=4
FIXED_TRIALS=20

# Test targets (magnitude, value, smallest_factor)
TARGETS=(
    "1e4 1022117 1009"
    "1e5 100160063 10007"
    "1e6 10002200057 100003"
    "1e9 1000036000099 1000003"
)

# Array size progression
ARRAY_SIZES=(50 100 200 500 1000 1500 2000 2500 3000 3500 4000)

# Portable millisecond timer
get_time_ms() {
    python3 -c 'import time; print(int(time.time() * 1000))'
}

echo "=== Scaling Verification Experiment (Memory Optimized) ==="
echo "CSV: $CSV_FILE"

# Ensure JAR exists
if [ ! -f "$JAR_PATH" ]; then
    echo "Error: JAR not found at $JAR_PATH. Please run 'mvn package' first."
    exit 1
fi

# CSV header
echo "magnitude,target,smallestFactor,arraySize,trials,threads,meanSteps,stepsPerElement,convergenceRate,wallTimeMs" > "$CSV_FILE"

for target_spec in "${TARGETS[@]}"; do
    read -r magnitude target smallest_factor <<< "$target_spec"
    echo "=== Testing magnitude $magnitude (target=$target) ==="

    for n in "${ARRAY_SIZES[@]}"; do
        echo "  Array size n=$n..."
        start_time=$(get_time_ms)

        # Run with 8GB heap and trajectory recording DISABLED in FactorizationExperiment.java
        output=$(java -Xmx8g -cp "$JAR_PATH" \
            com.emergent.doom.examples.FactorizationExperiment \
            "$target" "$FIXED_TRIALS" "$n" "$FIXED_THREADS" 2>&1) || {
                echo "    FAILED: Java execution error"
                echo "$output" | tail -n 5
                continue
            }

        end_time=$(get_time_ms)
        wall_time=$((end_time - start_time))

        # Extract metrics
        mean_steps=$(echo "$output" | grep -E 'Mean Steps:' | sed -E 's/.*Mean Steps:[[:space:]]*([0-9.]+).*/\1/' | head -1 || echo "0")
        convergence=$(echo "$output" | grep -E 'Convergence Rate:' | sed -E 's/.*Convergence Rate:[[:space:]]*([0-9.]+).*/\1/' | head -1 || echo "0")

        # Handle potential empty/zero values
        if [[ -z "$mean_steps" ]]; then mean_steps=0; fi
        if [[ -z "$convergence" ]]; then convergence=0; fi

        if [[ "$n" -gt 0 ]]; then
            steps_per_element=$(echo "scale=4; $mean_steps / $n" | bc)
        else
            steps_per_element=0
        fi

        echo "$magnitude,$target,$smallest_factor,$n,$FIXED_TRIALS,$FIXED_THREADS,$mean_steps,$steps_per_element,$convergence,$wall_time" >> "$CSV_FILE"
        echo "    → Steps: $mean_steps, Steps/n: $steps_per_element, Convergence: $convergence%"
    done
done

echo "Generating analysis report..."
if [ -s "$CSV_FILE" ]; then
    python3 "$PROJECT_ROOT/scripts/analysis/analyze_scaling.py" "$CSV_FILE" > "$ANALYSIS_FILE" || echo "Analysis script failed."
else
    echo "CSV file is empty. Skipping analysis."
fi

echo "Experiment complete!"
