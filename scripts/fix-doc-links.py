#!/usr/bin/env python3
"""
Fix broken links in test suite documentation.

This script corrects the relative paths to source files in the README files.
The issue: links use ../../../../../main/ instead of ../../../../../../src/main/
"""

import re
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
    content_before = content
    content = content.replace('(../../../../../../src/main/', '(../../../../../../main/')
    content = content.replace('(../../../../../main/', '(../../../../../../main/')
    
    # Pattern 2: Fix root README links (only in the root test README)
    # Fix 8 ../ to 7 ../ for README.md and REQUIREMENTS.md
    content = content.replace('(../../../../../../../README.md)', '(../../../../../../README.md)')
    content = content.replace('(../../../../../../../REQUIREMENTS.md)', '(../../../../../../docs/requirements/REQUIREMENTS.md)')
    
    # Count how many replacements were made
    fixes = content.count('(../../../../../../main/') - content_before.count('(../../../../../../main/')
    
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
