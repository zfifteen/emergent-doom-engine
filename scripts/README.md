# Emergent Doom Engine Scripts

This directory contains utility scripts for the Emergent Doom Engine project.

## Documentation Link Validation (Phase 8)

Automated validation of markdown links in test suite documentation.

### validate-links.py

Validates all markdown links in the test suite documentation to ensure they point to valid files.

**Usage:**
```bash
# Validate all README files in test suite (default)
python scripts/validate-links.py

# Enable verbose output
python scripts/validate-links.py --verbose

# Validate a specific path
python scripts/validate-links.py --path src/test/java/com/emergent/doom/cell
```

**Features:**
- Validates relative file paths
- Skips external URLs (http/https)
- Skips mailto links
- Skips internal anchors (#section)
- Provides detailed error messages with file locations
- Returns exit code 0 if all links valid, 1 if errors found

**Output:**
- Success: "✅ All links are valid!"
- Failure: Detailed report of broken links with:
  - Source file location
  - Link text
  - Target path
  - Resolved absolute path
  - Reason for failure

### fix-doc-links.py

One-time script used to fix broken links discovered during Phase 8 implementation.

**Usage:**
```bash
# Fix broken links in all README files
python scripts/fix-doc-links.py
```

**What it fixes:**
- Corrects relative paths from test suite READMEs to production source files
- Updates path depth (from 6 `../` to 7 `../` where needed)
- Fixes references to project root files (README.md, REQUIREMENTS.md)

**Note:** This script was used to perform one-time fixes and may not be needed for ongoing maintenance. Use `validate-links.py` to check for new issues.

## GitHub Actions Integration

Link validation runs automatically on:
- Pull requests that modify markdown files
- Pushes to main branch
- Manual workflow dispatch

See `.github/workflows/validate-docs-links.yml` for configuration.

## Contributing

When adding new documentation:
1. Add markdown files with proper relative links
2. Run `python scripts/validate-links.py` locally before committing
3. Fix any broken links reported
4. Commit changes - CI will validate on PR

## Troubleshooting

### "Broken link" error for valid file

Check that:
- The target file actually exists at the specified path
- The relative path is correct (count `../` carefully)
- You're using forward slashes `/` not backslashes `\`

### "File not found" when running script

Ensure you're running from the repository root:
```bash
cd /path/to/emergent-doom-engine
python scripts/validate-links.py
```

### Links work in GitHub but fail validation

GitHub may be more lenient with link resolution. Ensure your links work with standard file system path resolution.
