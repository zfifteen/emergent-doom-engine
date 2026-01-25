#!/bin/bash
#
# H1 Batch Runner - Helper Script
#
# Provides convenient shortcuts for running H1 experiments.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
RUNNER_CLASS="com.emergent.doom.domains.graphcoloring.H1BatchRunner"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_usage() {
    echo "H1 Batch Runner - Helper Script"
    echo ""
    echo "Usage: $0 <preset> [--outDir <dir>]"
    echo ""
    echo "Presets:"
    echo "  test       Quick test (2 trials, 20 cells, 100 steps, ~10 sec)"
    echo "  small      Small batch (10 trials, 50 cells, 1000 steps, ~2 min)"
    echo "  medium     Medium batch (50 trials, 50 cells, 2500 steps, ~15 min)"
    echo "  full       Full run (100 trials, 50+100 cells, 5000 steps, ~60 min)"
    echo "  custom     Custom run (specify all args manually)"
    echo ""
    echo "Options:"
    echo "  --outDir <dir>    Output directory (default: experiments/h1_<preset>_<timestamp>)"
    echo ""
    echo "Examples:"
    echo "  $0 test"
    echo "  $0 small --outDir my_results"
    echo "  $0 custom --trials 20 --popSizes 30,60 --maxSteps 1000"
    echo ""
}

# Parse preset
PRESET="${1:-}"
shift || true

if [ -z "$PRESET" ]; then
    print_usage
    exit 1
fi

# Default output directory
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DEFAULT_OUTDIR="experiments/h1_${PRESET}_${TIMESTAMP}"
OUTDIR="$DEFAULT_OUTDIR"

# Parse output directory option
while [[ $# -gt 0 ]]; do
    case $1 in
        --outDir)
            OUTDIR="$2"
            shift 2
            ;;
        *)
            break
            ;;
    esac
done

# Build Maven command base
MVN_CMD="mvn -f $PROJECT_DIR/pom.xml exec:java -Dexec.mainClass=$RUNNER_CLASS"

# Set preset parameters
case $PRESET in
    test)
        ARGS="--outDir $OUTDIR --trials 2 --popSizes 20 --maxSteps 100 --masterSeed 42"
        DESCRIPTION="Quick test (2 trials, 20 cells, 100 steps)"
        ;;
    small)
        ARGS="--outDir $OUTDIR --trials 10 --popSizes 50 --maxSteps 1000 --masterSeed 42"
        DESCRIPTION="Small batch (10 trials, 50 cells, 1000 steps)"
        ;;
    medium)
        ARGS="--outDir $OUTDIR --trials 50 --popSizes 50 --maxSteps 2500 --masterSeed 42"
        DESCRIPTION="Medium batch (50 trials, 50 cells, 2500 steps)"
        ;;
    full)
        ARGS="--outDir $OUTDIR --trials 100 --popSizes 50,100 --maxSteps 5000 --masterSeed 42"
        DESCRIPTION="Full run (100 trials, 50+100 cells, 5000 steps)"
        ;;
    custom)
        ARGS="--outDir $OUTDIR $@"
        DESCRIPTION="Custom run"
        ;;
    *)
        echo -e "${RED}Error: Unknown preset '$PRESET'${NC}"
        echo ""
        print_usage
        exit 1
        ;;
esac

# Print configuration
echo -e "${GREEN}=== H1 Batch Runner ===${NC}"
echo "Preset: $PRESET"
echo "Description: $DESCRIPTION"
echo "Output directory: $OUTDIR"
echo ""

# Confirm for long runs
if [ "$PRESET" = "full" ] || [ "$PRESET" = "medium" ]; then
    echo -e "${YELLOW}This will take significant time. Continue? (y/N)${NC}"
    read -r CONFIRM
    if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
        echo "Cancelled."
        exit 0
    fi
fi

# Create output directory
mkdir -p "$OUTDIR"

# Run experiment
echo ""
echo "Running experiment..."
echo ""

# Execute with Maven
$MVN_CMD -Dexec.args="$ARGS" -q

# Print results summary
echo ""
echo -e "${GREEN}=== Experiment Complete ===${NC}"
echo "Results written to: $OUTDIR"
echo ""
echo "Output files per configuration:"
echo "  - manifest.json      (experiment metadata)"
echo "  - trajectories.csv   (step-by-step metrics)"
echo "  - trial_summary.csv  (per-trial aggregates)"
echo ""
echo "View summary:"
for config in BASELINE_CHIMERIC_NO_RECOMB NEG_CONTROL_LABEL_ONLY CONTROL_RANDOM_CUT_RECOMB CONTROL_RANDOM_BOUNDARY_RECOMB TEST_BOUNDARY_GUIDED_RECOMB; do
    SUMMARY_FILE="$OUTDIR/$config/trial_summary.csv"
    if [ -f "$SUMMARY_FILE" ]; then
        SOLVED=$(awk -F, 'NR>1 && $7=="true" {count++} END {print count+0}' "$SUMMARY_FILE")
        TOTAL=$(awk 'END {print NR-1}' "$SUMMARY_FILE")
        echo "  $config: $SOLVED/$TOTAL solved"
    fi
done
echo ""
echo "For detailed analysis, see: docs/H1_BATCH_RUNNER.md"
