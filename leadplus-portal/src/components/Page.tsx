import { PropsWithChildren } from 'react';

import { cn } from '@/lib/utils/helpers';

interface PageProps {
  className?: string;
  disableAnimation?: boolean;
}

export const Page = ({
  children,
  className,
  disableAnimation = false,
}: PropsWithChildren<PageProps>) => {
  return (
    <div
      className={cn(
        'bg-background flex h-[calc(100vh-4rem)]',
        !disableAnimation && 'animate-in fade-in zoom-in-95 duration-500',
        className
      )}
    >
      {children}
    </div>
  );
};
