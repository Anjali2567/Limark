'use client';

import * as React from 'react';
import { Check, ChevronsUpDown } from 'lucide-react';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { cn } from '@/lib/utils/helpers';
import { Button } from '@/components/ui/button';
import { CommandLoading } from 'cmdk';

type SelectedItem = string | { value: string; identifier?: string };

interface MultiSelectProps {
  options: { label: string; value: string; identifier?: string }[];
  selected: SelectedItem[];
  onChange: (value: SelectedItem[]) => void;
  onSearchChange?: (value: string) => void;
  placeholder?: string;
  className?: string;
  isLoading?: boolean;
}

export function MultiSelect({
  options,
  selected,
  onChange,
  onSearchChange,
  isLoading,
  placeholder = 'Select options...',
  className,
}: MultiSelectProps) {
  const [open, setOpen] = React.useState(false);

  const handleSelect = (item: SelectedItem) => {
    const itemValue = typeof item === 'object' ? item.value : item;
    const isSelected = selected.some((s) => (typeof s === 'object' ? s.value : s) === itemValue);

    if (isSelected) {
      onChange(selected.filter((s) => (typeof s === 'object' ? s.value : s) !== itemValue));
    } else {
      onChange([...selected, item]);
    }
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          variant="outline"
          role="combobox"
          aria-expanded={open}
          className={cn(
            'bg-input-background border-border hover:bg-input-background h-auto min-h-10 w-full justify-between px-3 py-2',
            className
          )}
        >
          <div className="flex items-center gap-1 overflow-hidden">
            {selected.length > 0 ? (
              <span className="truncate">
                {options.find((opt) => {
                  const s = selected[0];
                  const val = typeof s === 'object' ? s.value : s;
                  const id = typeof s === 'object' ? s.identifier : undefined;
                  return opt.value === val && opt.identifier === id;
                })?.label ?? (typeof selected[0] === 'object' ? selected[0].value : selected[0])}
                {selected.length > 1 && ` + ${selected.length - 1} more`}
              </span>
            ) : (
              <span className="text-muted-foreground truncate">{placeholder}</span>
            )}
          </div>
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-full p-0" align="start">
        <Command shouldFilter={!onSearchChange}>
          <CommandInput placeholder="Search..." onValueChange={(v) => onSearchChange?.(v)} />
          <CommandList>
            {isLoading ? (
              <CommandLoading>
                <CommandEmpty>Loading...</CommandEmpty>
              </CommandLoading>
            ) : (
              <CommandEmpty>No results found.</CommandEmpty>
            )}
            <CommandGroup>
              {options.map((option) => (
                <CommandItem
                  key={option.identifier ? `${option.identifier}:${option.value}` : option.value}
                  onSelect={() => {
                    handleSelect(
                      option.identifier
                        ? {
                            value: option.value,
                            identifier: option.identifier,
                          }
                        : option.value
                    );
                  }}
                >
                  <div
                    className={cn(
                      'border-primary mr-2 flex h-4 w-4 items-center justify-center rounded-sm border',
                      selected.some(
                        (s) =>
                          (typeof s === 'object' ? s.value : s) === option.value &&
                          (typeof s === 'object' ? s.identifier : undefined) === option.identifier
                      )
                        ? 'bg-primary text-primary-foreground'
                        : 'opacity-50 [&_svg]:invisible'
                    )}
                  >
                    <Check className={cn('h-4 w-4')} />
                  </div>
                  <span>{option.label}</span>
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
