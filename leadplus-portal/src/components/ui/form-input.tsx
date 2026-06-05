import { forwardRef, InputHTMLAttributes, ReactNode } from 'react';

import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import useToggle from '@/hooks/useToggle';
import { cn } from '@/lib/utils/helpers';
import { Button } from './button';

import { Eye, EyeOff, LucideIcon } from 'lucide-react';

interface FormInputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  icon?: LucideIcon;
  error?: boolean;
  errorMessage?: string;
  rightElement?: ReactNode;
}

export const FormInput = forwardRef<HTMLInputElement, FormInputProps>(
  (
    { label, icon: Icon, error, errorMessage, rightElement, className, id, type, ...props },
    ref
  ) => {
    const { value: showPassword, toggle: toggleShowPassword } = useToggle();
    const isPasswordField = type === 'password';
    const inputType = isPasswordField && showPassword ? 'text' : type;

    return (
      <div className="space-y-2">
        {(label || rightElement) && (
          <div className="flex items-center justify-between">
            <Label htmlFor={id}>{label}</Label>
            {rightElement}
          </div>
        )}
        <div>
          <div className="group relative">
            {Icon && (
              <Icon
                className={cn(
                  'group-focus-within:text-primary absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 text-slate-400 transition-colors',
                  error && 'text-red-500 group-focus-within:text-red-500'
                )}
              />
            )}
            <Input
              id={id}
              ref={ref}
              type={inputType}
              className={cn(
                'focus:border-primary focus:ring-primary border-slate-200',
                error && 'border-red-500 focus:border-red-600 focus:ring-red-600',
                Icon && 'pl-10',
                isPasswordField && 'pr-10',
                className
              )}
              {...props}
            />
            {isPasswordField && (
              <Button
                type="button"
                onClick={toggleShowPassword}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                className="absolute top-1/2 right-3 -translate-y-1/2 p-0! text-slate-400 transition-colors hover:text-slate-600 focus:outline-none"
                variant="none"
              >
                {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </Button>
            )}
          </div>
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

FormInput.displayName = 'FormInput';
