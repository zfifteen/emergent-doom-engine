#!/usr/bin/env python3
"""
Trajectory CSV Validation Script

This script validates exported trajectory CSV files and demonstrates
compatibility with Python/R data analysis workflows.

Validates Levin et al. (2024) metrics:
- Delayed Gratification events
- Monotonicity Error progression
- Sortedness progression
- Final state validation

Usage:
    python3 scripts/validate_trajectory.py experiments/data/bubble_test_trajectory.csv
"""

import sys
import pandas as pd


def validate_trajectory(csv_path):
    """Validate trajectory CSV file and compute metrics."""
    
    print(f"\n{'='*80}")
    print(f"Trajectory Validation: {csv_path}")
    print(f"{'='*80}\n")
    
    # Load trajectory (skip metadata comment lines)
    try:
        # First, find where the actual data starts (after metadata and headers)
        with open(csv_path, 'r') as f:
            lines = f.readlines()
        
        # Find the line with column headers (starts with "step_number")
        header_line_idx = None
        for i, line in enumerate(lines):
            if line.startswith('step_number'):
                header_line_idx = i
                break
        
        if header_line_idx is None:
            print("ERROR: Could not find column header line")
            return False
        
        # Read CSV starting from the header line
        df = pd.read_csv(csv_path, skiprows=header_line_idx)
        
    except FileNotFoundError:
        print(f"ERROR: File not found: {csv_path}")
        return False
    except Exception as e:
        print(f"ERROR: Failed to read CSV: {e}")
        return False
    
    # Validate required columns
    required_columns = ['step_number', 'sortedness', 'monotonicity_error', 
                       'cumulative_swaps', 'cumulative_comparisons']
    
    missing_cols = [col for col in required_columns if col not in df.columns]
    if missing_cols:
        print(f"ERROR: Missing required columns: {missing_cols}")
        return False
    
    print("✓ CSV format valid")
    print(f"✓ Rows: {len(df)}")
    print(f"✓ Columns: {list(df.columns)}")
    
    # Validate Delayed Gratification
    # Check for temporary sortedness decreases followed by gains
    print("\n--- Delayed Gratification Analysis ---")
    sortedness = df['sortedness'].values
    
    dg_events = 0
    for i in range(1, len(sortedness) - 1):
        if sortedness[i] < sortedness[i-1] and sortedness[i+1] > sortedness[i]:
            dg_events += 1
            print(f"  DG event at step {df['step_number'].iloc[i]}: "
                  f"{sortedness[i-1]:.1f}% → {sortedness[i]:.1f}% → {sortedness[i+1]:.1f}%")
    
    print(f"\nDelayed Gratification events detected: {dg_events}")
    
    # Validate Monotonicity Error trend
    print("\n--- Monotonicity Error Analysis ---")
    error_values = df['monotonicity_error'].values
    error_diff = df['monotonicity_error'].diff().dropna()
    
    decreasing_steps = (error_diff <= 0).sum()
    total_steps = len(error_diff)
    decreasing_pct = (decreasing_steps / total_steps * 100) if total_steps > 0 else 0
    
    print(f"Monotonicity error trend:")
    print(f"  Initial: {error_values[0]}")
    print(f"  Final: {error_values[-1]}")
    print(f"  Decreasing steps: {decreasing_pct:.1f}% ({decreasing_steps}/{total_steps})")
    
    # Validate Sortedness progression
    print("\n--- Sortedness Progression Analysis ---")
    sortedness_diff = df['sortedness'].diff().dropna()
    
    increasing_steps = (sortedness_diff >= 0).sum()
    total_steps = len(sortedness_diff)
    increasing_pct = (increasing_steps / total_steps * 100) if total_steps > 0 else 0
    
    print(f"Sortedness progression:")
    print(f"  Initial: {sortedness[0]:.1f}%")
    print(f"  Final: {sortedness[-1]:.1f}%")
    print(f"  Overall gain: {sortedness[-1] - sortedness[0]:.1f}%")
    print(f"  Increasing steps: {increasing_pct:.1f}% ({increasing_steps}/{total_steps})")
    
    # Validate final state
    print("\n--- Final State Validation ---")
    final_step = df.iloc[-1]
    
    print(f"Final step {int(final_step['step_number'])}:")
    print(f"  Sortedness: {final_step['sortedness']:.1f}%")
    print(f"  Monotonicity error: {int(final_step['monotonicity_error'])}")
    print(f"  Cumulative swaps: {int(final_step['cumulative_swaps'])}")
    print(f"  Cumulative comparisons: {int(final_step['cumulative_comparisons'])}")
    
    # Overall validation
    print("\n--- Overall Validation ---")
    
    is_sorted = final_step['sortedness'] == 100.0
    error_decreased = error_values[-1] < error_values[0]
    sortedness_increased = sortedness[-1] > sortedness[0]
    
    print(f"✓ Final state sorted (100%): {is_sorted}")
    print(f"✓ Monotonicity error decreased: {error_decreased}")
    print(f"✓ Sortedness increased: {sortedness_increased}")
    
    all_valid = is_sorted and error_decreased and sortedness_increased
    
    print(f"\n{'='*80}")
    if all_valid:
        print("VALIDATION: ✓ PASSED")
    else:
        print("VALIDATION: ✗ FAILED (see details above)")
    print(f"{'='*80}\n")
    
    return all_valid


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python3 validate_trajectory.py <trajectory_csv_file>")
        print("Example: python3 validate_trajectory.py experiments/data/bubble_test_trajectory.csv")
        sys.exit(1)
    
    csv_path = sys.argv[1]
    success = validate_trajectory(csv_path)
    sys.exit(0 if success else 1)
