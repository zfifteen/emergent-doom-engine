#!/bin/bash
# Script to convert relative documentation links to GitHub permalinks
# This ensures links remain stable and version-specific

set -e

# Base commit SHA for permalinks (commit before this refactoring work)
BASE_SHA="7e77864a5553f144acb55bff7115b22c3d2919cb"
REPO_URL="https://github.com/zfifteen/emergent-doom-engine/blob"

echo "Converting documentation links to GitHub permalinks..."
echo "Base SHA: $BASE_SHA"
echo ""

# Function to convert links in a single file
convert_file() {
    local file="$1"
    echo "Processing: $file"
    
    # Backup original file
    cp "$file" "${file}.bak"
    
    # Convert production code links (../../../../../../main/java/...)
    # Pattern: ](../../../../../../main/java/com/emergent/doom/PATH)
    # Replace: ](https://github.com/zfifteen/emergent-doom-engine/blob/SHA/src/main/java/com/emergent/doom/PATH)
    sed -i 's|](../../../../../../main/java/com/emergent/doom/|]('"${REPO_URL}/${BASE_SHA}"'/src/main/java/com/emergent/doom/|g' "$file"
    
    # Show what changed
    if ! diff -q "$file" "${file}.bak" > /dev/null 2>&1; then
        echo "  ✓ Updated production code links"
    else
        echo "  - No production code links found"
    fi
    
    # Clean up backup
    rm "${file}.bak"
}

# Convert all test suite documentation
echo "=== Converting Test Suite Documentation ==="
find src/test/java/com/emergent/doom -name "*.md" -type f | while read file; do
    convert_file "$file"
done

echo ""
echo "=== Converting Docs Directory ==="
# Convert docs directory if any relative links exist
if [ -f "docs/requirements/factorization_ui_requirements.md" ]; then
    if grep -q "](\.\./" "docs/requirements/factorization_ui_requirements.md"; then
        convert_file "docs/requirements/factorization_ui_requirements.md"
    fi
fi

echo ""
echo "=== Conversion Complete ==="
echo ""
echo "Next steps:"
echo "1. Review changes with: git diff"
echo "2. Verify links work (use verify_permalinks.sh)"
echo "3. Commit changes if satisfied"
