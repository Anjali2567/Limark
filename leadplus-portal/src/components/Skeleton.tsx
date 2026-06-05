import { cn } from '@/lib/utils/helpers';

export const GridSkeleton = () => (
  <div className="border-border bg-muted/30 animate-pulse rounded-lg border p-4">
    <div className="flex items-center justify-between">
      <div className="space-y-2">
        <div className="bg-muted h-4 w-24 rounded" />
        <div className="bg-muted h-7 w-16 rounded" />
      </div>
      <div className="bg-muted h-6 w-6 rounded-full" />
    </div>
  </div>
);

export const TabSkeleton = () => (
  <div className="border-border bg-muted/30 w-full animate-pulse rounded-lg border p-4">
    <div className="space-y-2">
      <div className="bg-muted h-4 w-full rounded" />
      <div className="bg-muted h-7 w-full rounded" />
    </div>
  </div>
);

export const LaneSkeleton = () => (
  <div className="border-border bg-muted/30 h-48 w-full animate-pulse rounded-lg border p-4">
    <div className="space-y-2">
      <div className="bg-muted h-4 w-32 rounded" />
      <div className="bg-muted h-7 w-40 rounded" />
    </div>
  </div>
);

export const NumberSkeleton = () => <div className="bg-muted h-6 w-6 animate-pulse rounded" />;

export const Skeleton = ({ className }: { className: string }) => (
  <div className={cn('bg-muted animate-pulse rounded', className)} />
);
