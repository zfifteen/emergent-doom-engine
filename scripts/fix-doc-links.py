#!/usr/bin/env python3
"""
Fix broken links in test suite documentation.

This script corrects the relative paths to source files in the README files.
The issue: links use ../../../../../main/ instead of ../../../../../../src/main/
"""

from pathlib import Path


def fix_readme_links(readme_path: Path) -> int:
    """
    Fix broken links in a README file.
    
    Args:
        readme_path: Path to the README file
        
    Returns:
        Number of links fixed
    """
    with open(readme_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    fixes = 0
    
    # Pattern 1: Fix ../../../../../main/ to ../../../../../../main/ (add one more ../)
    # Note: This fixes the path to go up 7 levels instead of 6
    # Skip links that are already correct to avoid breaking them
    
    # Only fix if it's the incorrect pattern (6 levels) but NOT if it's already the correct pattern (7 levels)
    if '(../../../../../main/' in content and '(../../../../../../main/' not in content:
        pattern_from = '(../../../../../main/'
        count_from = content.count(pattern_from)
        if count_from:
            fixes += count_from
            content = content.replace(pattern_from, '(../../../../../../main/')
    
    # Pattern 2: Fix root README links (only in the root test README)
    # Fix 8 ../ to 7 ../ for README.md and REQUIREMENTS.md
    pattern_readme_from = '(../../../../../../../README.md)'
    count_readme_from = content.count(pattern_readme_from)
    if count_readme_from:
        fixes += count_readme_from
        content = content.replace(pattern_readme_from, '(../../../../../../README.md)')
    
    pattern_requirements_from = '(../../../../../../../REQUIREMENTS.md)'
    count_requirements_from = content.count(pattern_requirements_from)
    if count_requirements_from:
        fixes += count_requirements_from
        content = content.replace(pattern_requirements_from, '(../../../../../../docs/requirements/REQUIREMENTS.md)')
    
    # Write back if changed
    if content != original_content:
        with open(readme_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {fixes} link(s) in {readme_path.name}")
        return fixes
    
    return 0


def main():
    """Main entry point."""
    script_dir = Path(__file__).parent.absolute()
    repo_root = script_dir.parent
    
    # Find all README files in test suite
    test_docs = repo_root / 'src' / 'test' / 'java' / 'com' / 'emergent' / 'doom'
    readme_files = list(test_docs.rglob('*.md'))
    
    total_fixes = 0
    for readme in readme_files:
        total_fixes += fix_readme_links(readme)
    
    print(f"\nTotal links fixed: {total_fixes}")
    print("Run 'python scripts/validate-links.py' to verify the fixes.")


if __name__ == '__main__':
    main()
