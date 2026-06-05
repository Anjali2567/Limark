import { forwardRef, ReactNode } from 'react';

import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { cn } from '@/lib/utils/helpers';

interface FormSelectOption {
  label: string;
  value: string;
}

interface FormSelectProps {
  label?: string;
  error?: boolean;
  errorMessage?: string;
  rightElement?: ReactNode;
  placeholder?: string;
  options: FormSelectOption[];
  value?: string;
  defaultValue?: string;
  disabled?: boolean;
  onValueChange?: (value: string) => void;
  className?: string;
  id?: string;
  emptyMessage?: string;
}

export const FormSelect = forwardRef<HTMLButtonElement, FormSelectProps>(
  (
    {
      label,
      error,
      errorMessage,
      rightElement,
      placeholder = 'Select an option',
      options,
      value,
      defaultValue,
      disabled,
      onValueChange,
      className,
      id,
      emptyMessage = 'No options available',
    },
    ref
  ) => {
    return (
      <div className="space-y-2">
        {(label || rightElement) && (
          <div className="flex items-center justify-between">
            {label && <Label htmlFor={id}>{label}</Label>}
            {rightElement}
          </div>
        )}
        <div>
          <Select
            value={value}
            defaultValue={defaultValue}
            onValueChange={onValueChange}
            disabled={disabled}
          >
            <SelectTrigger
              id={id}
              ref={ref}
              className={cn(
                'focus:border-primary focus:ring-primary w-full border-slate-200',
                error && 'border-red-500 focus:border-red-600 focus:ring-red-600',
                className
              )}
            >
              <SelectValue placeholder={placeholder} />
            </SelectTrigger>
            <SelectContent>
              {options.length > 0 ? (
                options.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))
              ) : (
                <div className="text-muted-foreground px-2 py-1.5 text-sm">{emptyMessage}</div>
              )}
            </SelectContent>
          </Select>
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

FormSelect.displayName = 'FormSelect';
