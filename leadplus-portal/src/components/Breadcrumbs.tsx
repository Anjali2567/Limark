'use client';

import Link from 'next/link';
import { ChevronRight } from 'lucide-react';

interface BreadcrumbItem {
  label: string;
  href?: string;
}

interface BreadcrumbsProps {
  items: BreadcrumbItem[];
  className?: string;
}

export const Breadcrumbs = ({ items, className }: BreadcrumbsProps) => {
  return (
    <nav aria-label="Breadcrumb" className={`mb-6 ${className ?? ''}`}>
      <ol className="flex items-center text-sm">
        {items.map((item, index) => {
          const isLast = index === items.length - 1;
          return (
            <li key={item.label} className="flex items-center">
              {index > 0 && <ChevronRight className="text-muted-foreground mx-2 h-4 w-4" />}

              {item.href && !isLast ? (
                <Link
                  href={item.href}
                  className="font-medium text-sky-500 transition-colors hover:text-sky-600"
                >
                  {item.label}
                </Link>
              ) : (
                <span className="text-foreground font-medium">{item.label}</span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
};
