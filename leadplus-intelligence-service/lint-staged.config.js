/**
 * Lint-staged configuration
 * Runs on staged files during pre-commit
 */
export default {
  // TypeScript files
  '*.ts': [
    // Type check (runs on all files, not just staged)
    () => 'pnpm run typecheck',

    // Lint with auto-fix
    'eslint --fix --quiet',

    // Format
    'prettier --write',
  ],

  // JSON, Markdown, etc.
  '*.{json,md}': ['prettier --write'],
};
