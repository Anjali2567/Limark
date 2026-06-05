'use client';

import { useBrandTheme, BrandTheme } from '@/context/BrandThemeContext';
import { cn } from '@/lib/utils/helpers';

interface ThemeSwitcherProps {
  /** Visual variant – use 'dark' when the switcher sits on a dark background */
  variant?: 'dark' | 'light';
}

export function ThemeSwitcher({ variant = 'dark' }: ThemeSwitcherProps) {
  const { brandTheme, setBrandTheme } = useBrandTheme();

  const themes: { value: BrandTheme; label: string; colorClass: string }[] = [
    { value: 'blue', label: 'Blue', colorClass: 'bg-sky-500' },
    { value: 'gold', label: 'Gold', colorClass: 'bg-[#c8a255]' },
  ];

  return (
    <div
      className={cn(
        'flex gap-0.5 rounded-lg border p-0.5',
        variant === 'dark' ? 'border-white/6 bg-white/8' : 'border-slate-900/8 bg-slate-900/5'
      )}
    >
      {themes.map(({ value, label, colorClass }) => {
        const isActive = brandTheme === value;
        return (
          <button
            key={value}
            onClick={() => setBrandTheme(value)}
            title={`Switch to ${label} theme`}
            className={cn(
              'font-family-display flex cursor-pointer items-center gap-1.5 rounded-md border-none px-2.5 py-1 text-[11px] font-semibold tracking-wide transition-all duration-200',
              isActive
                ? variant === 'dark'
                  ? 'bg-white/12 shadow-sm'
                  : 'bg-white shadow-md'
                : 'bg-transparent',
              isActive && value === 'blue' && 'text-sky-500',
              isActive && value === 'gold' && 'text-[#c8a255]'
            )}
          >
            <span
              className={cn(
                'h-1.5 w-1.5 shrink-0 rounded-full',
                colorClass,
                !isActive && 'opacity-40'
              )}
            />
            {label}
          </button>
        );
      })}
    </div>
  );
}
