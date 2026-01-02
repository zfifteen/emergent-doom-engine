#!/bin/bash
set -euo pipefail

# Scaling Verification Experiment for Emergent Factorization
# Goal: Rigorously test O(n) hypothesis and determine array size scaling laws

# Always resolve to repo root regardless of where the script is invoked
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR%/scripts}"

# Define paths relative to PROJECT_ROOT
JAR_PATH="$PROJECT_ROOT/target/emergent-doom-engine-0.1.0-alpha.jar"
LOG_FILE="$PROJECT_ROOT/scripts/scaling_verification.log"
CSV_FILE="$PROJECT_ROOT/scripts/scaling_results.csv"
ANALYSIS_FILE="$PROJECT_ROOT/scripts/scaling_analysis.txt"

# Change to project root
cd "$PROJECT_ROOT"

# Ensure output directories exist
mkdir -p "$(dirname "$LOG_FILE")"

# Experimental parameters
FIXED_THREADS=4
FIXED_TRIALS=20

# Test targets (magnitude value smallest_factor)
TARGETS=(
    "1e4 1022117 1009"
    "1e5 100160063 10007"
    "1e6 10002200057 100003"
)

# Array size progression - fine-grained to catch nonlinearity
ARRAY_SIZES=(50 100 150 200 250 300 400 500 600 800 1000 1200 1500 2000 2500 3000)

echo "Scaling Verification Experiment Started: $(date)" > "$LOG_FILE"
echo "magnitude,target,smallestFactor,arraySize,trials,threads,meanSteps,stepsPerElement,convergenceRate,wallTimeSec,sortednessMean,monotonicityMean" > "$CSV_FILE"

# Build the project
echo "Building project..."
mvn clean package -DskipTests >> "$LOG_FILE" 2>&1

echo ""
echo "====================================================================="
echo "  SCALING VERIFICATION EXPERIMENT"
echo "====================================================================="
echo ""
echo "Testing ${#TARGETS[@]} targets across ${#ARRAY_SIZES[@]} array sizes"
echo "Trials per configuration: $FIXED_TRIALS"
echo "Threads: $FIXED_THREADS"
echo ""

# Run systematic sweep
for target_spec in "${TARGETS[@]}"; do
    read -r magnitude target smallest_factor <<< "$target_spec"
    
    echo "=== Testing magnitude $magnitude (target=$target, smallest_factor=$smallest_factor) ==="
    echo "" | tee -a "$LOG_FILE"
    
    for n in "${ARRAY_SIZES[@]}"; do
        echo "  Array size n=$n..." | tee -a "$LOG_FILE"
        
        # Time the experiment (use seconds, avoid millisecond overflow)
        start_time=$(date +%s)
        
        # Run experiment with increased heap size
        output=$(java -Xmx6g -cp "$JAR_PATH" \
            com.emergent.doom.examples.FactorizationExperiment \
            "$target" "$FIXED_TRIALS" "$n" "$FIXED_THREADS" 2>&1)
        
        end_time=$(date +%s)
        wall_time=$((end_time - start_time))
        
        # Save full output to log
        echo "$output" >> "$LOG_FILE"
        echo "------------------------------------------------------------" >> "$LOG_FILE"
        
        # Extract metrics using grep and sed (portable)
        mean_steps=$(echo "$output" | grep -E 'Mean Steps:' | sed -E 's/.*Mean Steps:[[:space:]]*([0-9.]+).*/\1/' | head -1)
        sortedness=$(echo "$output" | grep -E 'Sortedness.*mean=' | sed -E 's/.*mean=([0-9.]+).*/\1/' | head -1)
        monotonicity=$(echo "$output" | grep -E 'Monotonicity.*mean=' | sed -E 's/.*mean=([0-9.]+).*/\1/' | head -1)
        convergence=$(echo "$output" | grep -E 'Convergence Rate:' | sed -E 's/.*Convergence Rate:[[:space:]]*([0-9.]+).*/\1/' | head -1)
        
        # Handle empty values
        mean_steps=${mean_steps:-0}
        sortedness=${sortedness:-0}
        monotonicity=${monotonicity:-0}
        convergence=${convergence:-0}
        
        # Calculate derived metric (steps per array element)
        if [ "$n" -gt 0 ] && [ "${mean_steps%.*}" -gt 0 ]; then
            steps_per_element=$(awk "BEGIN {printf \"%.4f\", $mean_steps / $n}")
        else
            steps_per_element="0.0000"
        fi
        
        # Log to CSV
        echo "$magnitude,$target,$smallest_factor,$n,$FIXED_TRIALS,$FIXED_THREADS,$mean_steps,$steps_per_element,$convergence,$wall_time,$sortedness,$monotonicity" >> "$CSV_FILE"
        
        # Real-time analysis display
        echo "    → Steps: $mean_steps, Steps/n: $steps_per_element, Convergence: $convergence%, Time: ${wall_time}s"
    done
    
    echo ""
done

echo ""
echo "====================================================================="
echo "  Generating Statistical Analysis..."
echo "====================================================================="
echo ""

# Post-experiment analysis
if command -v python3 &> /dev/null; then
    echo "Running Python analysis..."
    python3 "$PROJECT_ROOT/scripts/analyze_scaling.py" "$CSV_FILE" > "$ANALYSIS_FILE" 2>&1 || {
        echo "Python analysis failed, using shell fallback"
        echo "=== SCALING ANALYSIS (Shell Fallback) ===" > "$ANALYSIS_FILE"
        echo "" >> "$ANALYSIS_FILE"
        echo "Full analysis requires Python with pandas and scipy." >> "$ANALYSIS_FILE"
        echo "Install with: pip install pandas scipy" >> "$ANALYSIS_FILE"
    }
else
    # Basic shell analysis fallback
    echo "=== SCALING ANALYSIS (Basic) ===" > "$ANALYSIS_FILE"
    echo "" >> "$ANALYSIS_FILE"
    echo "Python not available for statistical analysis." >> "$ANALYSIS_FILE"
    echo "Install Python 3 with pandas and scipy for detailed analysis." >> "$ANALYSIS_FILE"
    echo "" >> "$ANALYSIS_FILE"
    
    for target_spec in "${TARGETS[@]}"; do
        read -r magnitude target smallest_factor <<< "$target_spec"
        
        echo "Magnitude $magnitude (target=$target):" >> "$ANALYSIS_FILE"
        
        # Extract steps/n ratios for this target
        ratios=$(grep "^$magnitude," "$CSV_FILE" | cut -d',' -f8 | grep -v "0.0000")
        
        if [ -n "$ratios" ]; then
            # Calculate mean
            mean_ratio=$(echo "$ratios" | awk '{sum+=$1; n++} END {if(n>0) print sum/n; else print "N/A"}')
            echo "  Mean steps/n ratio: $mean_ratio" >> "$ANALYSIS_FILE"
            
            # Count data points
            count=$(echo "$ratios" | wc -l | tr -d ' ')
            echo "  Valid data points: $count" >> "$ANALYSIS_FILE"
        else
            echo "  No valid data" >> "$ANALYSIS_FILE"
        fi
        echo "" >> "$ANALYSIS_FILE"
    done
fi

echo ""
echo "====================================================================="
echo "  Experiment Complete!"
echo "====================================================================="
echo ""
echo "Results saved to:"
echo "  CSV:      $CSV_FILE"
echo "  Log:      $LOG_FILE"
echo "  Analysis: $ANALYSIS_FILE"
echo ""
echo "Next steps:"
echo "  1. Review $ANALYSIS_FILE for O(n) verification"
echo "  2. Plot steps vs array_size to visualize linearity"
echo "  3. Check if steps/n ratio is constant across magnitudes"
echo ""
