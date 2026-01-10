#!/usr/bin/env python3
"""
Generate visualizations for Clustering vs Fitness Experiment.

This script loads CSV results from all experimental conditions and generates
5 publication-quality visualizations to test whether factor localization is
caused by clustering or fitness-driven sorting.

Usage:
    python3 generate_visualizations.py results/

Output:
    5 PNG files in visualizations/ directory
    Statistical analysis in analysis/statistical_tests.txt
"""

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path
from scipy import stats
import sys
import glob

# Configure plotting style
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (12, 8)
plt.rcParams['font.size'] = 12

def load_all_results(results_dir):
    """
    Load all CSV files and combine into single DataFrame.
    
    Args:
        results_dir: Path to results directory
        
    Returns:
        DataFrame with all experimental data
    """
    all_data = []
    
    for csv_file in glob.glob(f"{results_dir}/*.csv"):
        # Extract condition and rep from filename
        # Format: C1_baseline_rep_001.csv
        filename = Path(csv_file).stem
        parts = filename.split('_rep_')
        condition = parts[0]
        rep = int(parts[1])
        
        # Load CSV
        df = pd.read_csv(csv_file)
        df['condition'] = condition
        df['rep'] = rep
        
        all_data.append(df)
    
    return pd.concat(all_data, ignore_index=True)


def compute_convergence_time(group):
    """
    Compute step when both factors reach front (positions 0-4).
    
    Args:
        group: DataFrame group for single run
        
    Returns:
        Step number of convergence, or 100 if never converged
    """
    for _, row in group.iterrows():
        # Check if both factors in top 5 positions
        # Note: factor_13_pos can be -1 if not present (C4 condition)
        if row['factor_11_pos'] >= 0 and row['factor_11_pos'] <= 4:
            if row['factor_13_pos'] == -1 or row['factor_13_pos'] <= 4:
                return row['step']
    return 100  # Never converged


def plot_v1_factor_migration(data, output_dir):
    """
    V1: Factor Migration Trajectories by Condition
    
    Multi-line plot showing mean factor position over time for each condition.
    """
    plt.figure(figsize=(12, 8))
    
    conditions = ['C1_baseline', 'C2_high_aggregation', 'C3_zero_aggregation', 
                  'C4_fitness_control', 'C5_homogeneous']
    colors = ['blue', 'green', 'red', 'orange', 'purple']
    labels = ['C1: Baseline', 'C2: High Aggregation', 'C3: Zero Aggregation',
              'C4: Fitness Control', 'C5: Homogeneous']
    
    for condition, color, label in zip(conditions, colors, labels):
        cond_data = data[data['condition'] == condition]
        
        # Compute mean factor position per step
        mean_pos = cond_data.groupby('step')['mean_factor_dist'].mean()
        std_pos = cond_data.groupby('step')['mean_factor_dist'].std()
        
        steps = mean_pos.index
        plt.plot(steps, mean_pos, label=label, color=color, linewidth=2)
        plt.fill_between(steps, mean_pos - std_pos, mean_pos + std_pos, 
                         alpha=0.2, color=color)
    
    plt.xlabel('Step Number', fontsize=14)
    plt.ylabel('Mean Factor Position (0=front)', fontsize=14)
    plt.title('V1: Factor Migration Trajectories by Condition', fontsize=16, fontweight='bold')
    plt.legend(fontsize=10, loc='upper right')
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(f'{output_dir}/V1_factor_migration.png', dpi=300)
    plt.close()
    print("✓ Generated V1: Factor Migration Trajectories")


def plot_v2_dual_axis_baseline(data, output_dir):
    """
    V2: Dual-axis Aggregation vs Factor Position (Baseline only)
    
    Shows correlation between aggregation changes and factor movement.
    """
    baseline = data[data['condition'] == 'C1_baseline']
    
    # Compute mean across reps
    mean_data = baseline.groupby('step').agg({
        'aggregation': 'mean',
        'mean_factor_dist': 'mean'
    })
    
    fig, ax1 = plt.subplots(figsize=(12, 8))
    
    color1 = 'tab:blue'
    ax1.set_xlabel('Step Number', fontsize=14)
    ax1.set_ylabel('Aggregation Value (%)', color=color1, fontsize=14)
    ax1.plot(mean_data.index, mean_data['aggregation'], color=color1, linewidth=2, label='Aggregation')
    ax1.tick_params(axis='y', labelcolor=color1)
    ax1.grid(True, alpha=0.3)
    
    ax2 = ax1.twinx()
    color2 = 'tab:red'
    ax2.set_ylabel('Mean Factor Position', color=color2, fontsize=14)
    ax2.plot(mean_data.index, mean_data['mean_factor_dist'], color=color2, linewidth=2, label='Factor Position')
    ax2.tick_params(axis='y', labelcolor=color2)
    
    plt.title('V2: Aggregation vs Factor Position Over Time (Baseline)', 
              fontsize=16, fontweight='bold')
    fig.tight_layout()
    plt.savefig(f'{output_dir}/V2_aggregation_vs_position.png', dpi=300)
    plt.close()
    print("✓ Generated V2: Dual-axis Aggregation vs Position")


def plot_v3_fitness_gradient_scatter(data, output_dir):
    """
    V3: Fitness Gradient vs Localization Speed
    
    Scatter plot with regression line testing fitness gradient as predictor.
    """
    # Compute mean fitness gradient over steps 0-20 for each run
    early_data = data[data['step'] <= 20]
    mean_gradient = early_data.groupby(['condition', 'rep'])['fitness_grad_mean'].mean()
    
    # Compute convergence time for each run
    convergence = data.groupby(['condition', 'rep']).apply(compute_convergence_time)
    
    # Combine
    scatter_data = pd.DataFrame({
        'fitness_gradient': mean_gradient,
        'convergence_time': convergence
    }).reset_index()
    
    plt.figure(figsize=(12, 8))
    
    conditions = scatter_data['condition'].unique()
    colors_map = {'C1_baseline': 'blue', 'C2_high_aggregation': 'green', 
              'C3_zero_aggregation': 'red', 'C4_fitness_control': 'orange',
              'C5_homogeneous': 'purple'}
    labels_map = {'C1_baseline': 'C1: Baseline', 'C2_high_aggregation': 'C2: High Agg',
                  'C3_zero_aggregation': 'C3: Zero Agg', 'C4_fitness_control': 'C4: No Factors',
                  'C5_homogeneous': 'C5: Homogeneous'}
    
    for condition in conditions:
        cond_data = scatter_data[scatter_data['condition'] == condition]
        plt.scatter(cond_data['fitness_gradient'], cond_data['convergence_time'],
                   label=labels_map.get(condition, condition), alpha=0.6, s=100, 
                   color=colors_map.get(condition, 'gray'))
    
    # Regression line (excluding C4 which has flat fitness)
    non_c4 = scatter_data[scatter_data['condition'] != 'C4_fitness_control']
    x = non_c4['fitness_gradient']
    y = non_c4['convergence_time']
    slope, intercept, r_value, p_value, std_err = stats.linregress(x, y)
    line_x = np.linspace(x.min(), x.max(), 100)
    line_y = slope * line_x + intercept
    plt.plot(line_x, line_y, 'k--', linewidth=2, 
             label=f'Regression (r={r_value:.3f}, p={p_value:.4f})')
    
    plt.xlabel('Mean Fitness Gradient (steps 0-20)', fontsize=14)
    plt.ylabel('Steps to Localization', fontsize=14)
    plt.title('V3: Fitness Gradient vs Localization Speed', fontsize=16, fontweight='bold')
    plt.legend(fontsize=10)
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(f'{output_dir}/V3_fitness_gradient_scatter.png', dpi=300)
    plt.close()
    print(f"✓ Generated V3: Fitness Gradient Scatter (r={r_value:.3f}, p={p_value:.4f})")


def plot_v5_convergence_boxplot(data, output_dir):
    """
    V5: Convergence Time by Initial Aggregation
    
    Box plot showing distribution of convergence times across conditions.
    """
    # Compute convergence time for each run
    convergence = data.groupby(['condition', 'rep']).apply(compute_convergence_time).reset_index()
    convergence.columns = ['condition', 'rep', 'convergence_time']
    
    plt.figure(figsize=(12, 8))
    
    conditions = ['C1_baseline', 'C2_high_aggregation', 'C3_zero_aggregation', 
                  'C4_fitness_control', 'C5_homogeneous']
    labels = ['C1:\nBaseline', 'C2:\nHigh Agg', 'C3:\nZero Agg', 
              'C4:\nNo Factors', 'C5:\nHomogeneous']
    
    # Create box plot
    box_data = [convergence[convergence['condition'] == c]['convergence_time'].values 
                for c in conditions]
    
    bp = plt.boxplot(box_data, labels=labels, patch_artist=True)
    
    # Color boxes
    colors = ['lightblue', 'lightgreen', 'lightcoral', 'lightsalmon', 'plum']
    for patch, color in zip(bp['boxes'], colors):
        patch.set_facecolor(color)
    
    plt.ylabel('Steps to Factor Localization', fontsize=14)
    plt.title('V5: Convergence Time by Initial Aggregation', fontsize=16, fontweight='bold')
    plt.grid(True, alpha=0.3, axis='y')
    plt.tight_layout()
    plt.savefig(f'{output_dir}/V5_convergence_boxplot.png', dpi=300)
    plt.close()
    print("✓ Generated V5: Convergence Time Box Plot")


def plot_v8_correlation_matrix(data, output_dir):
    """
    V8: Correlation Matrix of Key Metrics
    
    Heatmap showing correlations between metrics to identify predictors.
    """
    # Compute metrics per run
    initial_agg = data[data['step'] == 0].groupby(['condition', 'rep'])['aggregation'].mean()
    agg_20 = data[data['step'] <= 20].groupby(['condition', 'rep'])['aggregation'].mean()
    fitness_grad_20 = data[data['step'] <= 20].groupby(['condition', 'rep'])['fitness_grad_mean'].mean()
    convergence = data.groupby(['condition', 'rep']).apply(compute_convergence_time)
    final_pos = data.groupby(['condition', 'rep'])['mean_factor_dist'].last()
    
    # Combine into DataFrame
    metrics_df = pd.DataFrame({
        'Initial Aggregation': initial_agg,
        'Aggregation (avg 0-20)': agg_20,
        'Fitness Gradient (avg 0-20)': fitness_grad_20,
        'Convergence Time': convergence,
        'Final Factor Position': final_pos
    })
    
    # Compute correlation matrix
    corr_matrix = metrics_df.corr()
    
    plt.figure(figsize=(10, 8))
    sns.heatmap(corr_matrix, annot=True, fmt='.3f', cmap='coolwarm', 
                center=0, vmin=-1, vmax=1, square=True, linewidths=1, cbar_kws={"shrink": 0.8})
    plt.title('V8: Metric Correlations (All Conditions Combined)', 
              fontsize=16, fontweight='bold')
    plt.tight_layout()
    plt.savefig(f'{output_dir}/V8_correlation_matrix.png', dpi=300)
    plt.close()
    print("✓ Generated V8: Correlation Matrix")


def perform_statistical_tests(data, output_dir):
    """
    Perform statistical tests and save to text file.
    """
    # Compute convergence times
    convergence = data.groupby(['condition', 'rep']).apply(compute_convergence_time).reset_index()
    convergence.columns = ['condition', 'rep', 'convergence_time']
    
    with open(f'{output_dir}/../analysis/statistical_tests.txt', 'w') as f:
        f.write("=== Statistical Tests: Clustering vs Fitness Experiment ===\n")
        f.write("Date: " + str(pd.Timestamp.now()) + "\n\n")
        
        # Test 1: C2 vs C3 (high agg vs zero agg)
        c2_times = convergence[convergence['condition'] == 'C2_high_aggregation']['convergence_time']
        c3_times = convergence[convergence['condition'] == 'C3_zero_aggregation']['convergence_time']
        
        t_stat, p_val = stats.ttest_ind(c2_times, c3_times)
        f.write("1. C2 (High Aggregation) vs C3 (Zero Aggregation)\n")
        f.write(f"   C2 mean convergence time: {c2_times.mean():.2f} ± {c2_times.std():.2f}\n")
        f.write(f"   C3 mean convergence time: {c3_times.mean():.2f} ± {c3_times.std():.2f}\n")
        f.write(f"   t-test: t={t_stat:.3f}, p={p_val:.4f}\n")
        if p_val > 0.05:
            f.write("   ✓ NO significant difference (clustering hypothesis FALSIFIED)\n\n")
        else:
            f.write("   ✗ Significant difference (clustering hypothesis supported)\n\n")
        
        # Test 2: Correlation aggregation vs convergence
        initial_agg = data[data['step'] == 0].groupby(['condition', 'rep'])['aggregation'].mean()
        conv_times = convergence.set_index(['condition', 'rep'])['convergence_time']
        
        # Align indices
        common_idx = initial_agg.index.intersection(conv_times.index)
        corr, p = stats.pearsonr(initial_agg[common_idx], conv_times[common_idx])
        f.write("2. Correlation: Initial Aggregation vs Convergence Time\n")
        f.write(f"   Pearson r = {corr:.3f}, p = {p:.4f}\n")
        if abs(corr) < 0.3:
            f.write("   ✓ Weak correlation (clustering hypothesis FALSIFIED)\n\n")
        else:
            f.write("   ✗ Strong correlation (clustering hypothesis supported)\n\n")
        
        # Test 3: C4 localization check
        c4_final_pos = data[(data['condition'] == 'C4_fitness_control') & 
                            (data['step'] == data.groupby(['condition', 'rep'])['step'].transform('max'))]
        c4_mean_pos = c4_final_pos['mean_factor_dist'].mean()
        
        f.write("3. C4 (Fitness Control - No True Factors) Localization\n")
        f.write(f"   Mean final factor position: {c4_mean_pos:.2f}\n")
        if c4_mean_pos > 20:
            f.write("   ✓ NO localization (fitness hypothesis SUPPORTED)\n\n")
        else:
            f.write("   ✗ Localization occurred (unexpected)\n\n")
        
        # Test 4: ANOVA across all conditions
        groups = [convergence[convergence['condition'] == c]['convergence_time'].values 
                  for c in convergence['condition'].unique()]
        f_stat, p_anova = stats.f_oneway(*groups)
        
        f.write("4. ANOVA: All Conditions\n")
        f.write(f"   F-statistic = {f_stat:.3f}, p = {p_anova:.4f}\n")
        if p_anova > 0.05:
            f.write("   ✓ No significant difference between conditions\n\n")
        else:
            f.write("   ✗ Significant difference between conditions\n\n")
        
        # Summary statistics
        f.write("\n=== Summary Statistics by Condition ===\n\n")
        for condition in ['C1_baseline', 'C2_high_aggregation', 'C3_zero_aggregation', 
                          'C4_fitness_control', 'C5_homogeneous']:
            cond_conv = convergence[convergence['condition'] == condition]['convergence_time']
            f.write(f"{condition}:\n")
            f.write(f"  Mean: {cond_conv.mean():.2f}\n")
            f.write(f"  Median: {cond_conv.median():.2f}\n")
            f.write(f"  Std: {cond_conv.std():.2f}\n")
            f.write(f"  Min: {cond_conv.min()}\n")
            f.write(f"  Max: {cond_conv.max()}\n\n")
    
    print("✓ Generated statistical tests")


def main():
    """Main execution function."""
    if len(sys.argv) < 2:
        print("Usage: python3 generate_visualizations.py results/")
        sys.exit(1)
    
    results_dir = sys.argv[1]
    output_dir = "visualizations"
    
    # Create output directory
    Path(output_dir).mkdir(exist_ok=True)
    Path("analysis").mkdir(exist_ok=True)
    
    print("Loading experimental results...")
    data = load_all_results(results_dir)
    
    print(f"Loaded {len(data)} rows from {data['condition'].nunique()} conditions")
    print(f"Repetitions per condition: {data.groupby('condition')['rep'].nunique().to_dict()}")
    print()
    
    # Generate visualizations
    print("Generating visualizations...")
    plot_v1_factor_migration(data, output_dir)
    plot_v2_dual_axis_baseline(data, output_dir)
    plot_v3_fitness_gradient_scatter(data, output_dir)
    plot_v5_convergence_boxplot(data, output_dir)
    plot_v8_correlation_matrix(data, output_dir)
    
    # Statistical tests
    print("\nPerforming statistical tests...")
    perform_statistical_tests(data, output_dir)
    
    print("\n=== Analysis Complete ===")
    print(f"Visualizations saved to: {output_dir}/")
    print(f"Statistical tests saved to: analysis/statistical_tests.txt")


if __name__ == '__main__':
    main()
