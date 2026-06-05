import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function sortByPosition<T>(items: T[], positionKey?: keyof T): T[] {
  const key = (positionKey || 'position') as keyof T;
  return [...items].sort((a, b) => {
    const aPos = a[key];
    const bPos = b[key];

    if (aPos === undefined && bPos === undefined) return 0;
    if (aPos === undefined) return 1;
    if (bPos === undefined) return -1;

    return Number(aPos) - Number(bPos);
  });
}

export const getCachedImageUrl = (
  imagePath?: string | null,
  updatedAt?: Date | string | number
): string | undefined => {
  if (!imagePath) return undefined;

  const baseUrl = `${process.env.NEXT_PUBLIC_BASE_URL}${imagePath}`;

  if (!updatedAt) return baseUrl;

  const timestamp = typeof updatedAt === 'number' ? updatedAt : new Date(updatedAt).getTime();

  if (isNaN(timestamp)) return baseUrl;
  return `${baseUrl}?t=${timestamp}`;
};

export const getInitials = (name: string): string => {
  return name
    .split(' ')
    .map((part) => part.charAt(0))
    .join('')
    .toUpperCase()
    .slice(0, 2);
};

export const normalizeUrl = (url: string | null | undefined): string | null => {
  if (!url?.trim()) return null;
  const trimmed = url.trim();

  if (/^[a-z][a-z0-9+\-.]*:/i.test(trimmed) && !/^https?:/i.test(trimmed)) {
    return null;
  }

  const withScheme = /^https?:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
  try {
    const { protocol, href } = new URL(withScheme);
    return protocol === 'http:' || protocol === 'https:' ? href : null;
  } catch {
    return null;
  }
};

export const getAvatarColorClasses = (name: string): string => {
  const colors = [
    'bg-primary text-primary-foreground',
    'bg-accent text-accent-foreground',
    'bg-green-500 text-white',
    'bg-orange-400 text-white',
    'bg-yellow-400 text-foreground',
  ];
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colors[Math.abs(hash) % colors.length];
};
