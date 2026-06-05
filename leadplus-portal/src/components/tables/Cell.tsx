import { cn } from '@/lib/utils/helpers';

type CellProps = {
  value?: string | number | null;
  isTag?: boolean;
  className?: string;
  truncate?: boolean;
  type?: 'email' | 'phoneNumber';
};

const Cell = ({ value, isTag, className, truncate = false, type }: CellProps) => {
  const displayValue = value ?? 'N/A';
  const stringValue = displayValue.toString();

  if (value === null || value === undefined || value.toString().trim() === '') {
    return (
      <span className={cn('text-muted-foreground', className)} title={stringValue}>
        N/A
      </span>
    );
  }

  if (type === 'email' || type === 'phoneNumber') {
    const href = type === 'email' ? `mailto:${stringValue}` : `tel:${stringValue}`;
    return (
      <a
        href={href}
        className={cn('cursor-pointer truncate text-sky-500 hover:underline', className)}
        title={stringValue}
      >
        {stringValue}
      </a>
    );
  }

  if (isTag) {
    return (
      <span
        className={cn(
          'bg-secondary text-foreground inline-block max-w-full rounded px-2 py-1 text-xs font-semibold',
          className
        )}
        title={stringValue}
      >
        <span className={cn('block', truncate && 'truncate')}>{stringValue}</span>
      </span>
    );
  }

  if (!truncate) {
    return (
      <span className={cn('text-foreground', className)} title={stringValue}>
        {stringValue}
      </span>
    );
  }

  const splitIndex = Math.floor(stringValue.length * 0.7);
  const startPart = stringValue.substring(0, splitIndex);
  const endPart = stringValue.substring(splitIndex);

  return (
    <div
      className={cn('text-foreground flex w-full min-w-0 items-center', className)}
      title={stringValue}
    >
      <span className="min-w-0 shrink truncate">{startPart}</span>
      <span className="shrink-0 whitespace-nowrap">{endPart}</span>
    </div>
  );
};

export { Cell };
