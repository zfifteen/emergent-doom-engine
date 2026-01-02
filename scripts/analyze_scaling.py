#!/usr/bin/env python3
"""
Scaling Analysis Script for Emergent Factorization Experiment
Performs statistical analysis on scaling_results.csv to verify O(n) hypothesis
"""

import sys
import os

# Check if required packages are available
try:
    import pandas as pd
    import numpy as np
    from scipy import stats
except ImportError as e:
    print("ERROR: Required Python packages not installed")
    print(f"Missing package: {e}")
    print("\nInstall with: pip install pandas numpy scipy")
    sys.exit(1)

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 analyze_scaling.py <csv_file>")
        sys.exit(1)
    
    csv_file = sys.argv[1]
    
    # Check if file exists
    if not os.path.exists(csv_file):
        print(f"ERROR: File not found: {csv_file}")
        sys.exit(1)
    
    # Read CSV with error handling
    try:
        df = pd.read_csv(csv_file)
    except Exception as e:
        print(f"ERROR: Failed to read CSV file: {e}")
        sys.exit(1)
    
    # Validate required columns
    required_cols = ['magnitude', 'target', 'smallestFactor', 'arraySize', 
                     'meanSteps', 'stepsPerElement', 'convergenceRate']
    missing_cols = [col for col in required_cols if col not in df.columns]
    if missing_cols:
        print(f"ERROR: Missing required columns: {missing_cols}")
        sys.exit(1)
    
    print("="*70)
    print(" SCALING VERIFICATION ANALYSIS")
    print("="*70)
    print()
    
    # Filter out failed experiments (meanSteps = 0)
    df_valid = df[df['meanSteps'] > 0].copy()
    
    if len(df_valid) == 0:
        print("ERROR: No valid data points found (all meanSteps = 0)")
        print("This means all experiments failed or didn't converge.")
        sys.exit(1)
    
    print(f"Total experiments: {len(df)}")
    print(f"Valid data points: {len(df_valid)} ({100*len(df_valid)/len(df):.1f}%)")
    print()
    
    # Analyze each magnitude separately
    for magnitude in sorted(df_valid['magnitude'].unique()):
        subset = df_valid[df_valid['magnitude'] == magnitude].copy()
        
        if len(subset) < 3:
            print(f"Magnitude {magnitude}: Insufficient data ({len(subset)} points)")
            print()
            continue
        
        print(f"Magnitude {magnitude}:")
        print(f"  Target: {subset['target'].iloc[0]:,}")
        print(f"  Smallest factor: {subset['smallestFactor'].iloc[0]:,}")
        print(f"  Data points: {len(subset)}")
        
        # Linear regression: meanSteps = slope * arraySize + intercept
        x = subset['arraySize'].values
        y = subset['meanSteps'].values
        
        try:
            slope, intercept, r_value, p_value, std_err = stats.linregress(x, y)
            
            print()
            print(f"  LINEAR FIT: steps = {slope:.4f} × n + {intercept:.2f}")
            print(f"  R² = {r_value**2:.6f}  (1.0 = perfect linear)")
            print(f"  Slope std error: ±{std_err:.4f}")
            print(f"  p-value: {p_value:.2e}")
            
            # Interpret R²
            r_squared = r_value**2
            if r_squared > 0.98:
                print("  ✓ EXCELLENT linear fit")
            elif r_squared > 0.95:
                print("  ✓ GOOD linear fit")
            elif r_squared > 0.90:
                print("  ~ MODERATE linear fit")
            else:
                print("  ✗ POOR linear fit (non-linear behavior)")
        
        except Exception as e:
            print(f"  ERROR in regression: {e}")
        
        # Steps/n ratio analysis
        print()
        ratios = subset['stepsPerElement'].values
        mean_ratio = np.mean(ratios)
        std_ratio = np.std(ratios, ddof=1) if len(ratios) > 1 else 0
        cv_ratio = (std_ratio / mean_ratio * 100) if mean_ratio > 0 else float('inf')
        
        print(f"  STEPS/N RATIO: {mean_ratio:.4f} ± {std_ratio:.4f}")
        print(f"  Coefficient of variation: {cv_ratio:.2f}%")
        
        # Interpret stability
        if cv_ratio < 10:
            print("  ✓ STRONG O(n) evidence (ratio very stable)")
        elif cv_ratio < 20:
            print("  ~ MODERATE O(n) evidence (some variation)")
        else:
            print("  ✗ WEAK O(n) evidence (high variation)")
        
        # Check for systematic trend in residuals
        print()
        try:
            residuals = y - (slope * x + intercept)
            corr, trend_p = stats.spearmanr(x, residuals)
            
            if trend_p > 0.05:
                print("  ✓ No systematic deviation from linearity")
            else:
                print(f"  ⚠ Systematic residual trend detected (p={trend_p:.4f})")
                if corr > 0:
                    print("     → Steps grow faster than linear (super-linear)")
                else:
                    print("     → Steps grow slower than linear (sub-linear)")
        except Exception as e:
            print(f"  WARNING: Could not test residuals: {e}")
        
        # Convergence rate summary
        conv_rate = subset['convergenceRate'].mean()
        print()
        print(f"  Average convergence rate: {conv_rate:.1f}%")
        
        print()
        print("-"*70)
        print()
    
    # Cross-magnitude comparison
    print()
    print("CROSS-MAGNITUDE COMPARISON:")
    print("Does array size requirement scale with magnitude?")
    print()
    
    # Create pivot table: convergence rate by (magnitude, arraySize)
    try:
        pivot = df_valid.pivot_table(
            values='convergenceRate', 
            index='arraySize', 
            columns='magnitude',
            aggfunc='mean'
        )
        
        print(pivot.to_string())
        print()
        print("Interpretation:")
        print("- If rows are similar → array size independent of magnitude (GOOD)")
        print("- If diagonal pattern → array size scales with magnitude")
    except Exception as e:
        print(f"Could not create pivot table: {e}")
    
    print()
    print("="*70)
    print(" SUMMARY")
    print("="*70)
    print()
    
    # Overall assessment
    all_r_squared = []
    all_cv = []
    
    for magnitude in sorted(df_valid['magnitude'].unique()):
        subset = df_valid[df_valid['magnitude'] == magnitude]
        if len(subset) >= 3:
            x = subset['arraySize'].values
            y = subset['meanSteps'].values
            try:
                _, _, r_value, _, _ = stats.linregress(x, y)
                all_r_squared.append(r_value**2)
                
                ratios = subset['stepsPerElement'].values
                cv = (np.std(ratios, ddof=1) / np.mean(ratios) * 100) if len(ratios) > 1 else 0
                all_cv.append(cv)
            except:
                pass
    
    if all_r_squared:
        avg_r_squared = np.mean(all_r_squared)
        avg_cv = np.mean(all_cv)
        
        print(f"Average R² across magnitudes: {avg_r_squared:.4f}")
        print(f"Average coefficient of variation: {avg_cv:.2f}%")
        print()
        
        # Final verdict
        if avg_r_squared > 0.95 and avg_cv < 15:
            print("CONCLUSION: ✓ Strong evidence for O(n) scaling")
            print("The algorithm demonstrates linear scaling with array size.")
        elif avg_r_squared > 0.90 and avg_cv < 25:
            print("CONCLUSION: ~ Moderate evidence for O(n) scaling")
            print("The algorithm shows mostly linear behavior with some variance.")
        else:
            print("CONCLUSION: ✗ Insufficient evidence for strict O(n) scaling")
            print("The relationship may be non-linear or highly variable.")
    
    print()
    print("="*70)

if __name__ == "__main__":
    main()
