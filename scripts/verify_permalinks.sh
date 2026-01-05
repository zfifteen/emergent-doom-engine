#!/bin/bash
# Script to verify that all GitHub permalinks are accessible
# Returns non-zero exit code if any links are broken

set -e

echo "Verifying GitHub permalinks..."
echo ""

broken_links=0
total_links=0

# Extract all GitHub permalink URLs from markdown files
while IFS= read -r url; do
    ((total_links++))
    
    # Check HTTP status
    http_code=$(curl -o /dev/null -s -w "%{http_code}" "$url" || echo "000")
    
    if [ "$http_code" = "200" ]; then
        echo "✓ $url"
    else
        echo "✗ $url (HTTP $http_code)"
        ((broken_links++))
    fi
done < <(grep -rho "https://github.com/zfifteen/emergent-doom-engine/blob/[^)]*" \
    src/test/java/com/emergent/doom docs --include="*.md" 2>/dev/null | sort -u)

echo ""
echo "=== Verification Summary ==="
echo "Total permalinks checked: $total_links"
echo "Broken links: $broken_links"

if [ $broken_links -eq 0 ]; then
    echo "✓ All permalinks are accessible!"
    exit 0
else
    echo "✗ Some permalinks are broken. Please review."
    exit 1
fi
