#!/bin/bash
# Script to update the permalink base SHA in all documentation
# Usage: ./update_permalink_base.sh <new_commit_sha>

set -e

if [ -z "$1" ]; then
    echo "Error: No commit SHA provided"
    echo "Usage: $0 <new_commit_sha>"
    echo ""
    echo "Example: $0 abc123def456"
    exit 1
fi

NEW_SHA="$1"

# Validate SHA format (7-40 hex characters)
if ! [[ "$NEW_SHA" =~ ^[0-9a-f]{7,40}$ ]]; then
    echo "Error: Invalid commit SHA format"
    echo "Expected 7-40 hexadecimal characters, got: $NEW_SHA"
    exit 1
fi

# Verify commit exists
if ! git rev-parse "$NEW_SHA" > /dev/null 2>&1; then
    echo "Error: Commit $NEW_SHA not found in repository"
    exit 1
fi

FULL_SHA=$(git rev-parse "$NEW_SHA")
echo "Updating permalink base to commit: $FULL_SHA"
echo ""

# Extract current base SHA from existing permalinks
CURRENT_SHA=$(grep -m1 "github.com/zfifteen/emergent-doom-engine/blob/[a-f0-9]\{7,40\}" \
    src/test/java/com/emergent/doom/README.md 2>/dev/null | \
    sed -n 's|.*blob/\([a-f0-9]\{7,40\}\)/.*|\1|p' || echo "unknown")

if [ "$CURRENT_SHA" = "unknown" ]; then
    echo "Warning: Could not detect current permalink base SHA"
else
    echo "Current base SHA: $CURRENT_SHA"
fi

echo "New base SHA: $FULL_SHA"
echo ""

# Update all markdown files with permalinks
find_cmd="find src/test/java/com/emergent/doom docs -name '*.md' -type f 2>/dev/null"

files_updated=0

eval "$find_cmd" | while read file; do
    if grep -q "github.com/zfifteen/emergent-doom-engine/blob/" "$file"; then
        # Create backup
        cp "$file" "${file}.bak"
        
        # Replace all commit SHAs in GitHub permalinks
        sed -i "s|github.com/zfifteen/emergent-doom-engine/blob/[a-f0-9]\{7,40\}/|github.com/zfifteen/emergent-doom-engine/blob/${FULL_SHA}/|g" "$file"
        
        if ! diff -q "$file" "${file}.bak" > /dev/null 2>&1; then
            echo "Updated: $file"
            ((files_updated++)) || true
        fi
        
        rm "${file}.bak"
    fi
done

echo ""
echo "=== Update Complete ==="
echo "Files updated: $files_updated"
echo ""
echo "Next steps:"
echo "1. Review changes: git diff"
echo "2. Verify links: ./scripts/verify_permalinks.sh"
echo "3. Commit changes if satisfied"
