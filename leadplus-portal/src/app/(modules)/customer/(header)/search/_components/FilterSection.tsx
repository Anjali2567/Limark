'use client';

import { ReactNode } from 'react';

import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils/helpers';
import { Button } from '@/components/ui/button';

import { ChevronDown } from 'lucide-react';

type FilterSectionProps = {
  title: string;
  isOpen: boolean;
  onToggle: () => void;
  children: ReactNode;
  count?: number;
  className?: string;
};

export const FilterSection = ({
  title,
  isOpen,
  onToggle,
  children,
  count,
  className,
}: FilterSectionProps) => {
  return (
    <div className={cn('px-4', className)}>
      <Button
        type="button"
        variant="none"
        onClick={onToggle}
        className="flex h-fit! w-full items-center justify-between rounded-md p-0! text-sm font-semibold text-slate-900 transition-colors"
      >
        <div className="flex items-center gap-2">
          <span>{title}</span>
          {count !== undefined && count > 0 && (
            <Badge variant="default" className="rounded-full px-1.5 py-0.5 text-xs">
              {count}
            </Badge>
          )}
        </div>
        <ChevronDown
          className={cn('h-4 w-4 transition-transform duration-200', !isOpen && '-rotate-90')}
        />
      </Button>
      {isOpen && (
        <div className="animate-in fade-in slide-in-from-top-1 mt-2 duration-200">{children}</div>
      )}
    </div>
  );
};
