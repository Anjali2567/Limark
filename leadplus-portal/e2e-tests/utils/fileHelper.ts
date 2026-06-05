import fs from 'fs';
import path from 'path';

export function getAllAssetFiles(): string[] {
  const folder = path.resolve(__dirname, '../assets');

  const allowed = ['.pdf', '.png', '.jpg', '.pptx'];

  const files = fs.readdirSync(folder)
    .filter(f => allowed.includes(path.extname(f).toLowerCase()))
    .map(f => path.join(folder, f));

  if (files.length === 0) {
    throw new Error('No valid asset files found in e2e-tests/assets');
  }

  return files;
}