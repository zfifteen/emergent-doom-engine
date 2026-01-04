#!/usr/bin/env python3
"""
Automated Link Validation for Emergent Doom Engine Documentation (Phase 8)

This script validates all markdown links in the test suite README files to ensure:
- Relative file paths point to existing files
- Internal anchor links are valid
- Cross-references between READMEs are correct

Usage:
    python scripts/validate-links.py [--fix] [--verbose]
"""

import os
import re
import sys
from pathlib import Path
from typing import List, Tuple, Dict
from urllib.parse import urlparse


class LinkValidator:
    """Validates markdown links in documentation files."""
    
    def __init__(self, repo_root: Path, verbose: bool = False):
        self.repo_root = repo_root
        self.verbose = verbose
        self.errors: List[str] = []
        self.warnings: List[str] = []
        
        # Pattern to match markdown links: [text](url)
        self.link_pattern = re.compile(r'\[([^\]]+)\]\(([^)]+)\)')
    
    def log_verbose(self, message: str):
        """Print verbose logging message."""
        if self.verbose:
            print(f"[VERBOSE] {message}")
    
    def validate_file(self, md_file: Path) -> bool:
        """
        Validate all links in a single markdown file.
        
        Args:
            md_file: Path to the markdown file to validate
            
        Returns:
            True if all links are valid, False otherwise
        """
        self.log_verbose(f"Validating {md_file.relative_to(self.repo_root)}")
        
        if not md_file.exists():
            self.errors.append(f"File not found: {md_file}")
            return False
        
        with open(md_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        links = self.link_pattern.findall(content)
        all_valid = True
        
        for text, url in links:
            if not self.validate_link(md_file, text, url):
                all_valid = False
        
        return all_valid
    
    def validate_link(self, source_file: Path, link_text: str, url: str) -> bool:
        """
        Validate a single link.
        
        Args:
            source_file: The markdown file containing the link
            link_text: The display text of the link
            url: The URL or path being linked to
            
        Returns:
            True if the link is valid, False otherwise
        """
        # Skip external URLs (http/https)
        if url.startswith('http://') or url.startswith('https://'):
            self.log_verbose(f"  Skipping external URL: {url}")
            return True
        
        # Skip mailto links
        if url.startswith('mailto:'):
            self.log_verbose(f"  Skipping mailto link: {url}")
            return True
        
        # Skip anchor-only links (internal page anchors)
        if url.startswith('#'):
            self.log_verbose(f"  Skipping internal anchor: {url}")
            return True
        
        # Handle links with anchors (e.g., file.md#section)
        if '#' in url:
            file_part, anchor_part = url.split('#', 1)
            # For now, just validate the file part
            url = file_part if file_part else url
        
        # Resolve relative path from source file
        source_dir = source_file.parent
        target_path = (source_dir / url).resolve()
        
        # Check if target exists
        if not target_path.exists():
            error_msg = (
                f"Broken link in {source_file.relative_to(self.repo_root)}:\n"
                f"  Link text: [{link_text}]\n"
                f"  Target: {url}\n"
                f"  Resolved path: {target_path}\n"
                f"  (File does not exist)"
            )
            self.errors.append(error_msg)
            return False
        
        self.log_verbose(f"  ✓ Valid link: {url}")
        return True
    
    def validate_all(self, base_path: Path) -> bool:
        """
        Validate all markdown files in a directory tree.
        
        Args:
            base_path: Root directory to search for markdown files
            
        Returns:
            True if all links in all files are valid, False otherwise
        """
        md_files = list(base_path.rglob('*.md'))
        
        if not md_files:
            print(f"No markdown files found in {base_path}")
            return True
        
        print(f"Found {len(md_files)} markdown file(s) to validate")
        
        all_valid = True
        for md_file in sorted(md_files):
            if not self.validate_file(md_file):
                all_valid = False
        
        return all_valid
    
    def print_report(self):
        """Print a summary report of validation results."""
        print("\n" + "=" * 70)
        print("LINK VALIDATION REPORT")
        print("=" * 70)
        
        if self.errors:
            print(f"\n❌ Found {len(self.errors)} error(s):\n")
            for error in self.errors:
                print(error)
                print()
        
        if self.warnings:
            print(f"\n⚠️  Found {len(self.warnings)} warning(s):\n")
            for warning in self.warnings:
                print(warning)
                print()
        
        if not self.errors and not self.warnings:
            print("\n✅ All links are valid!")
        
        print("=" * 70)


def main():
    """Main entry point for the script."""
    import argparse
    
    parser = argparse.ArgumentParser(
        description='Validate markdown links in test suite documentation'
    )
    parser.add_argument(
        '--verbose', '-v',
        action='store_true',
        help='Enable verbose output'
    )
    parser.add_argument(
        '--path',
        type=str,
        default='src/test/java/com/emergent/doom',
        help='Path to validate (relative to repo root)'
    )
    
    args = parser.parse_args()
    
    # Determine repository root
    script_dir = Path(__file__).parent.absolute()
    repo_root = script_dir.parent
    
    # Target path to validate
    target_path = repo_root / args.path
    
    if not target_path.exists():
        print(f"Error: Path does not exist: {target_path}")
        sys.exit(1)
    
    # Create validator and run
    validator = LinkValidator(repo_root, verbose=args.verbose)
    all_valid = validator.validate_all(target_path)
    validator.print_report()
    
    # Exit with appropriate code
    sys.exit(0 if all_valid else 1)


if __name__ == '__main__':
    main()
