import { useState, useRef, useEffect, KeyboardEvent, ReactNode, ChangeEvent } from 'react';

import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';

import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface Option {
  label: string;
  value: string;
}

interface AutocompleteMultiSelectInputProps {
  value: Option[];
  onChange: (values: Option[]) => void;
  options: Option[];
  placeholder?: string;
  className?: string;
  icon?: ReactNode;
  allowCustomValue?: boolean;
}

export function AutocompleteMultiSelectInput({
  value = [],
  onChange,
  options,
  placeholder,
  className,
  icon,
  allowCustomValue = true,
}: AutocompleteMultiSelectInputProps) {
  const [input, setInput] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const filteredOptions = input
    ? options.filter((opt) => opt.label.toLowerCase().includes(input.toLowerCase()))
    : options;

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    setInput(e.target.value);
    setIsOpen(true);
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && input.trim()) {
      e.preventDefault();
      const existing = options.find((opt) => opt.label.toLowerCase() === input.toLowerCase());
      const newOption = existing || (allowCustomValue ? { label: input, value: input } : null);
      if (newOption && !value.some((v) => v.value === newOption.value)) {
        onChange([...value, newOption]);
      }
      setInput('');
      setIsOpen(false);
    }
  };

  const handleSelectOption = (option: Option) => {
    if (value.some((v) => v.value === option.value)) {
      onChange(value.filter((v) => v.value !== option.value));
    } else {
      onChange([...value, option]);
    }
    setInput('');
    setIsOpen(false);
  };

  const handleRemoveOption = (option: Option) => {
    onChange(value.filter((v) => v.value !== option.value));
  };

  return (
    <div ref={containerRef} className="relative flex-1 space-y-3">
      <div className="relative">
        {icon && (
          <div className="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2">{icon}</div>
        )}
        <Input
          value={input}
          onChange={handleInputChange}
          onFocus={() => setIsOpen(true)}
          onClick={() => setIsOpen(true)}
          onKeyDown={handleKeyDown}
          placeholder={placeholder || ''}
          className={`${icon ? 'pl-10' : ''} ${className || ''}`}
        />
      </div>

      {isOpen && filteredOptions.length > 0 && (
        <div className="bg-popover border-border absolute z-50 mt-1 max-h-60 w-full overflow-auto rounded-md border shadow-lg">
          {filteredOptions.map((option, index) => (
            <div
              key={index}
              className="hover:bg-accent cursor-pointer px-3 py-2 text-left text-sm"
              onClick={() => handleSelectOption(option)}
            >
              {option.label}
            </div>
          ))}
        </div>
      )}
      {value.length > 0 && (
        <div className="mb-1 flex flex-wrap gap-2">
          {value.map((val, index) => (
            <Badge key={index} variant="secondary" className="px-3 py-1">
              {val.label}
              <Button
                type="button"
                variant="none"
                onClick={() => handleRemoveOption(val)}
                className="hover:text-destructive ml-2 h-fit p-0!"
              >
                <X className="h-3 w-3" />
              </Button>
            </Badge>
          ))}
        </div>
      )}
    </div>
  );
}
