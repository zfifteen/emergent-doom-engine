#!/usr/bin/env python3
"""
H1 Plateau Boundary Study - Results Analysis

This script provides example analysis for H1 experiment results.
Loads data from all configurations and performs statistical comparisons.
"""

import pandas as pd
import numpy as np
from pathlib import Path
from scipy import stats
import sys

# Experiment configurations
CONFIGS = [
    'BASELINE_CHIMERIC_NO_RECOMB',
    'NEG_CONTROL_LABEL_ONLY',
    'CONTROL_RANDOM_CUT_RECOMB',
    'CONTROL_RANDOM_BOUNDARY_RECOMB',
    'TEST_BOUNDARY_GUIDED_RECOMB'
]

def load_results(results_dir):
    """Load trial summaries for all configurations."""
    summaries = {}
    for config in CONFIGS:
        csv_path = Path(results_dir) / config / 'trial_summary.csv'
        if csv_path.exists():
            summaries[config] = pd.read_csv(csv_path)
        else:
            print(f"Warning: {csv_path} not found", file=sys.stderr)
    return summaries

def print_summary_stats(summaries):
    """Print summary statistics for each configuration."""
    print("=== Summary Statistics ===\n")
    
    for config, df in summaries.items():
        print(f"{config}:")
        print(f"  Trials: {len(df)}")
        print(f"  Solved: {df['solved'].sum()} ({df['solved'].mean()*100:.1f}%)")
        print(f"  Mean final best: {df['final_best'].mean():.2f} ± {df['final_best'].std():.2f}")
        print(f"  Mean final median: {df['final_median'].mean():.2f} ± {df['final_median'].std():.2f}")
        print(f"  Mean max aggregation: {df['max_aggregation'].mean():.3f} ± {df['max_aggregation'].std():.3f}")
        if 'total_recomb_events' in df.columns:
            print(f"  Mean recomb events: {df['total_recomb_events'].mean():.1f}")
        print()

def compare_configurations(summaries, metric='final_best'):
    """Compare TEST configuration against all controls."""
    print(f"=== Statistical Comparisons ({metric}) ===\n")
    
    if 'TEST_BOUNDARY_GUIDED_RECOMB' not in summaries:
        print("Error: TEST_BOUNDARY_GUIDED_RECOMB not found")
        return
    
    test_data = summaries['TEST_BOUNDARY_GUIDED_RECOMB'][metric]
    
    for config in CONFIGS:
        if config == 'TEST_BOUNDARY_GUIDED_RECOMB':
            continue
            
        if config not in summaries:
            continue
            
        control_data = summaries[config][metric]
        
        # Two-sample t-test
        t_stat, p_value = stats.ttest_ind(test_data, control_data, alternative='less')
        
        # Effect size (Cohen's d)
        mean_diff = control_data.mean() - test_data.mean()
        pooled_std = np.sqrt((control_data.std()**2 + test_data.std()**2) / 2)
        cohens_d = mean_diff / pooled_std if pooled_std > 0 else 0
        
        # Interpretation
        if p_value < 0.05 and cohens_d > 0.5:
            result = "✓ SIGNIFICANT"
        elif p_value < 0.05:
            result = "⚠ SIG, SMALL EFFECT"
        else:
            result = "✗ NOT SIGNIFICANT"
        
        print(f"TEST vs {config}:")
        print(f"  Mean difference: {mean_diff:.3f}")
        print(f"  t-statistic: {t_stat:.3f}")
        print(f"  p-value: {p_value:.4f}")
        print(f"  Cohen's d: {cohens_d:.3f}")
        print(f"  Result: {result}")
        print()

def h1_verdict(summaries):
    """Determine if H1 is validated or falsified."""
    print("=== H1 Hypothesis Verdict ===\n")
    
    if 'TEST_BOUNDARY_GUIDED_RECOMB' not in summaries:
        print("Error: Cannot evaluate H1 - TEST configuration missing")
        return
    
    test_data = summaries['TEST_BOUNDARY_GUIDED_RECOMB']['final_best']
    
    # Check against each control
    controls_to_check = [c for c in CONFIGS if c != 'TEST_BOUNDARY_GUIDED_RECOMB']
    
    passes = 0
    for config in controls_to_check:
        if config not in summaries:
            continue
            
        control_data = summaries[config]['final_best']
        t_stat, p_value = stats.ttest_ind(test_data, control_data, alternative='less')
        
        mean_diff = control_data.mean() - test_data.mean()
        pooled_std = np.sqrt((control_data.std()**2 + test_data.std()**2) / 2)
        cohens_d = mean_diff / pooled_std if pooled_std > 0 else 0
        
        if p_value < 0.05 and cohens_d > 0.5:
            passes += 1
            status = "✓"
        else:
            status = "✗"
        
        print(f"{status} vs {config}: p={p_value:.4f}, d={cohens_d:.3f}")
    
    print()
    if passes == len(controls_to_check):
        print("✓✓✓ H1 VALIDATED ✓✓✓")
        print("Boundary-guided recombination dominates all controls.")
    elif passes > 0:
        print("⚠⚠⚠ H1 PARTIALLY SUPPORTED ⚠⚠⚠")
        print(f"Passes {passes}/{len(controls_to_check)} comparisons.")
        print("Consider increasing sample size or adjusting parameters.")
    else:
        print("✗✗✗ H1 FALSIFIED ✗✗✗")
        print("Boundary-guided recombination does not dominate controls.")
        print("Hypothesis: Boundaries do NOT carry exploitable structure.")

def main():
    if len(sys.argv) < 2:
        print("Usage: python analyze_h1_results.py <results_dir>")
        print()
        print("Example:")
        print("  python analyze_h1_results.py experiments/h1_full_20260125_120000")
        sys.exit(1)
    
    results_dir = sys.argv[1]
    
    print(f"Loading results from: {results_dir}\n")
    summaries = load_results(results_dir)
    
    if not summaries:
        print("Error: No results found")
        sys.exit(1)
    
    print_summary_stats(summaries)
    compare_configurations(summaries, metric='final_best')
    h1_verdict(summaries)
    
    print("\n=== Analysis Complete ===")

if __name__ == '__main__':
    main()
