#!/usr/bin/env python3
import pandas as pd
import numpy as np
from scipy import stats
import sys

csv_file = sys.argv[1]
# Robust reading: handle potential type inference issues
df = pd.read_csv(csv_file, dtype={'magnitude': str})

print("=== SCALING VERIFICATION ANALYSIS ===\n")

# Filter out non-converged rows for regression
converged_df = df[df['convergenceRate'] > 0].copy()

if converged_df.empty:
    print("No converged trials found in CSV.")
    sys.exit(0)

for magnitude in converged_df['magnitude'].unique():
    subset = converged_df[converged_df['magnitude'] == magnitude]

    print(f"Magnitude {magnitude}:")
    print(f"  Target: {subset['target'].iloc[0]}")

    # Linear regression: steps ~ a*n + b
    if len(subset) >= 2:
        slope, intercept, r_value, p_value, std_err = stats.linregress(
            subset['arraySize'], subset['meanSteps']
        )

        print(f"\n  Linear fit: steps = {slope:.4f}*n + {intercept:.2f}")
        print(f"  R² = {r_value**2:.6f} (1.0 = perfect linear)")
        print(f"  Slope std error: ±{std_err:.4f}")

        # Steps/n ratio stability
        mean_ratio = subset['stepsPerElement'].mean()
        std_ratio = subset['stepsPerElement'].std()
        cv_ratio = (std_ratio / mean_ratio * 100) if mean_ratio > 0 else 0

        print(f"\n  Steps/n ratio: {mean_ratio:.4f} ± {std_ratio:.4f}")
        print(f"  Coefficient of variation: {cv_ratio:.2f}%")

        if cv_ratio < 10:
            print("  ✓ STRONG O(n) evidence (ratio stable)")
        elif cv_ratio < 20:
            print("  ~ MODERATE O(n) evidence (some variation)")
        else:
            print("  ✗ WEAK O(n) evidence (high variation)")
    else:
        print("  Insufficient data points for regression.")

    print("\n" + "="*60 + "\n")

# Cross-magnitude comparison
print("CROSS-MAGNITUDE ANALYSIS:")
pivot = df.groupby(['magnitude', 'arraySize'])['convergenceRate'].mean().unstack()
print(pivot)
