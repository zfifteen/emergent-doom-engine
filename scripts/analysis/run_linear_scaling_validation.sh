#!/bin/bash
#
# Progressive Linear Scaling Validation Experiment Runner
#
# This script runs the experimental ladder defined in the issue:
# Stage 1 (10^6) → Stage 2 (10^9) → Stage 3 (10^12) → Stage 4 (10^18)
#
# Early termination occurs automatically if B > 0.5 (failure boundary detected)
#

set -e  # Exit on error

# Configuration
JAR_FILE="target/emergent-doom-engine-0.1.0-alpha.jar"
MAIN_CLASS="com.emergent.doom.validation.LinearScalingValidator"
OUTPUT_DIR="validation_results"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "========================================================================"
echo "  Linear Scaling Validation: Progressive Experimental Ladder"
echo "========================================================================"
echo ""

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}ERROR: JAR file not found at $JAR_FILE${NC}"
    echo "Please build the project first: mvn clean package -DskipTests"
    exit 1
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

echo -e "${GREEN}Running experimental validation system...${NC}"
echo ""
echo "Configuration:"
echo "  JAR: $JAR_FILE"
echo "  Output directory: $OUTPUT_DIR"
echo ""
echo "The system will automatically:"
echo "  1. Generate balanced semiprimes for each stage"
echo "  2. Run 30 trials across multiple array sizes"
echo "  3. Compute B coefficient (∂steps/∂array_size)"
echo "  4. Export results to CSV"
echo "  5. Terminate early if B > 0.5 (failure boundary detected)"
echo ""
echo -e "${YELLOW}NOTE: Command-line arguments not yet supported. Using default configuration.${NC}"
echo "========================================================================"
echo ""

# Run the experiment
# Note: The main() method handles all stage progression and early termination
java -Xmx8g -cp "$JAR_FILE" "$MAIN_CLASS" 2>&1 | tee "${OUTPUT_DIR}/experiment_log.txt"

# Check exit status
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================================================"
    echo -e "${GREEN}EXPERIMENT COMPLETED SUCCESSFULLY${NC}"
    echo "========================================================================"
    echo ""
    echo "Results saved to: $OUTPUT_DIR/"
    echo ""
    echo "To analyze results:"
    echo "  - Review console output: ${OUTPUT_DIR}/experiment_log.txt"
    echo "  - Examine CSV data: ${OUTPUT_DIR}/*.csv"
    echo "  - Run Python analysis: python3 scripts/analysis/analyze_scaling.py <csv_file>"
    echo ""
else
    echo ""
    echo "========================================================================"
    echo -e "${RED}EXPERIMENT FAILED${NC}"
    echo "========================================================================"
    echo ""
    echo "Check the log for errors: ${OUTPUT_DIR}/experiment_log.txt"
    echo ""
    exit 1
fi
