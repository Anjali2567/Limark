import { ChangeEvent, forwardRef } from 'react';

import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { cn } from '@/lib/utils/helpers';
import { dateToInputValue, inputValueToDate } from '@/lib/utils/date.utils';

type DateInputProps = {
  label?: string;
  error?: boolean;
  errorMessage?: string;
  rightElement?: React.ReactNode;
  className?: string;
  id?: string;
  value?: Date | null;
  onChange?: (date: Date | null) => void;
  disabled?: boolean;
  placeholder?: string;
  min?: string;
  max?: string;
};

export const DateInput = forwardRef<HTMLInputElement, DateInputProps>(
  (
    {
      label,
      error,
      errorMessage,
      rightElement,
      className,
      id,
      value,
      onChange,
      disabled,
      min,
      max,
      ...props
    },
    ref
  ) => {
    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
      const newDate = inputValueToDate(e.target.value);
      onChange?.(newDate);
    };

    return (
      <div className="space-y-2">
        {(label || rightElement) && (
          <div className="flex items-center justify-between">
            <Label htmlFor={id}>{label}</Label>
            {rightElement}
          </div>
        )}
        <div>
          <Input
            id={id}
            ref={ref}
            type="date"
            value={dateToInputValue(value)}
            onChange={handleChange}
            disabled={disabled}
            className={cn(
              'focus:border-primary focus:ring-primary border-slate-200',
              error && 'border-red-500 focus:border-red-600 focus:ring-red-600',
              className
            )}
            min={min}
            max={max}
            {...props}
          />
          {error && errorMessage && (
            <div className="animate-in fade-in slide-in-from-top-1 mt-1 flex items-center gap-2 text-sm text-red-500 duration-200">
              <span>{errorMessage}</span>
            </div>
          )}
        </div>
      </div>
    );
  }
);

DateInput.displayName = 'DateInput';
