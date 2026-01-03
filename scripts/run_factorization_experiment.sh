#!/bin/bash
# shellcheck disable=SC2164

# This shell script tests 'FactorizationExperiment' with each target contained in 'scripts/rsa_targets.txt'
# It logs results to docs/findings/batch_factorization_results.log

PROJECT_ROOT="/Users/velocityworks/IdeaProjects/emergent-doom-engine"
JAR_PATH="$PROJECT_ROOT/target/emergent-doom-engine-0.2.0-alpha.jar"
LOG_FILE="$PROJECT_ROOT/scripts/run_factorization_experiment.log"

cd "$PROJECT_ROOT"

# Ensure log directory exists
mkdir -p "$(dirname "$LOG_FILE")"
echo "Batch Factorization Experiment Started: $(date)" > "$LOG_FILE"

# Build the project
echo "Building project..."
mvn clean package -DskipTests >> "$LOG_FILE" 2>&1

# Refactored run_exp: Now leans on Java-side defaults for trials/scaling
run_exp() {
    local target=$1
    echo "Testing target: $target (Auto-scaling trials...)" | tee -a "$LOG_FILE"
    
    # Run with just the target to trigger auto-scaling and range partitioning
    java -cp "$JAR_PATH" com.emergent.doom.examples.FactorizationExperiment "$target" >> "$LOG_FILE" 2>&1
    
    echo "------------------------------------------------------------" >> "$LOG_FILE"
}

# New Loop: Process rsa_targets.txt automatically
while read -r line; do
    # Skip comments and empty lines
    [[ "$line" =~ ^#.*$ ]] || [[ -z "$line" ]] && continue
    run_exp "$line"
done < "scripts/rsa_targets.txt"

echo "Batch experiment complete. Results logged to $LOG_FILE"
