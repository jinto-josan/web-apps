#!/bin/bash

# Script to fix IntelliJ IDEA module structure for common-domain
# This ensures proper Maven module recognition

echo "Fixing IntelliJ IDEA module structure for common-domain..."

cd "$(dirname "$0")"

# Remove any existing .iml files in common-domain to force regeneration
find common-domain -name "*.iml" -type f -delete 2>/dev/null

echo "✅ Removed existing .iml files"

# Verify Maven structure
echo "Verifying Maven project structure..."
if [ -f "pom.xml" ] && [ -f "common-domain/pom.xml" ]; then
    echo "✅ Maven project structure is correct"
else
    echo "❌ Maven project structure issue detected"
    exit 1
fi

echo ""
echo "Next steps in IntelliJ IDEA:"
echo "1. File → Invalidate Caches / Restart → Invalidate and Restart"
echo "2. Right-click on root pom.xml → Maven → Reload Project"
echo "3. If modules still show errors:"
echo "   - File → Project Structure → Modules"
echo "   - Remove 'common-domain' if it has source roots"
echo "   - Ensure each child module (shared-models, infrastructure, etc.) exists separately"
echo ""
echo "Expected module structure:"
echo "  - common-domain (parent, POM only, no source roots)"
echo "  - common-domain-shared-models (has src/main/java)"
echo "  - common-domain-infrastructure (has src/main/java)"
echo "  - common-domain-error (has src/main/java)"
echo "  - common-domain-utilities (has src/main/java)"
echo "  - common-domain-event-contracts (has src/main/java)"

