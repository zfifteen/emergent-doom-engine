#!/bin/bash
# shellcheck disable=SC2164

# This shell script tests 'FactorizationExperiment' with a small selection of RSA numbers.
# It is designed to be faster than the full experiment by only testing 3 targets.

PROJECT_ROOT="/Users/velocityworks/IdeaProjects/emergent-doom-engine"
JAR_PATH="$PROJECT_ROOT/target/emergent-doom-engine-0.2.1-alpha.jar"
LOG_FILE="$PROJECT_ROOT/scripts/run_rsa_subset.log"
RSA_DATA="$PROJECT_ROOT/data/rsa_numbers.txt"

cd "$PROJECT_ROOT"

# Ensure log directory exists
mkdir -p "$(dirname "$LOG_FILE")"
echo "RSA Subset Factorization Experiment Started: $(date)" > "$LOG_FILE"

# Build the project if JAR doesn't exist or if forced
if [ ! -f "$JAR_PATH" ]; then
    echo "Building project..."
    mvn clean package -DskipTests >> "$LOG_FILE" 2>&1
fi

run_exp() {
    local name=$1
    local target=$2
    echo "Testing $name: $target (Running 100 trials)" | tee -a "$LOG_FILE"
    
    # Run with target and explicit 100 trials to keep it fast
    # Syntax: FactorizationExperiment <target> <numTrials> <arraySize>
    java -cp "$JAR_PATH" com.emergent.doom.examples.FactorizationExperiment "$target" 100 1000 >> "$LOG_FILE" 2>&1
    
    echo "------------------------------------------------------------" >> "$LOG_FILE"
}

# Select RSA-100, RSA-150, and RSA-200 for a representative subset
grep -E "RSA-(100|150|200):" "$RSA_DATA" | while read -r line; do
    NAME=$(echo "$line" | cut -d':' -f1)
    TARGET=$(echo "$line" | cut -d':' -f2 | xargs)
    run_exp "$NAME" "$TARGET"
done

echo "RSA subset experiment complete. Results logged to $LOG_FILE"
