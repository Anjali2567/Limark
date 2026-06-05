#!/bin/bash

# Development Tools Setup Script
# Run this script to complete the installation of Husky and lint-staged

set -e  # Exit on error

echo "🚀 Setting up development tools..."
echo ""

# Step 1: Install dependencies
echo "📦 Installing Husky and lint-staged..."
pnpm install

# Step 2: Initialize Husky
echo "🔧 Initializing Husky..."
pnpm exec husky init

# Step 3: Make pre-push hook executable
echo "🔐 Making pre-push hook executable..."
chmod +x .husky/pre-push

# Verify installation
echo ""
echo "✅ Setup complete!"
echo ""
echo "📋 Verifying installation..."

if [ -d ".husky/_" ]; then
  echo "  ✅ Husky initialized"
else
  echo "  ❌ Husky not initialized properly"
fi

if [ -x ".husky/pre-push" ]; then
  echo "  ✅ Pre-push hook is executable"
else
  echo "  ❌ Pre-push hook is not executable"
fi

if command -v husky &> /dev/null; then
  echo "  ✅ Husky command available"
else
  echo "  ⚠️  Husky command not in PATH (this is OK)"
fi

echo ""
echo "🎉 All done! Your development tools are ready."
echo ""
echo "Next steps:"
echo "  1. Test the pre-push hook: git push"
echo "  2. It will run: typecheck → lint → unit tests"
echo ""
