'use client';

import { ChangeEvent, KeyboardEvent, useRef, useState } from 'react';

import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import { Input } from '@/components/ui/input';
import { Popover, PopoverAnchor, PopoverContent } from '@/components/ui/popover';
import { cn } from '@/lib/utils/helpers';
import { CommandLoading } from 'cmdk';

import { Check, ChevronDown, Plus } from 'lucide-react';

type SelectedItem = string | { value: string; identifier?: string };

interface SearchableMultiSelectProps {
  options: { label: string; value: string; identifier?: string }[];
  selected: SelectedItem[];
  onChange: (value: SelectedItem[]) => void;
  onSearchChange?: (value: string) => void;
  placeholder?: string;
  isLoading?: boolean;
  creatable?: boolean;
}

export function SearchableMultiSelect({
  options,
  selected,
  onChange,
  onSearchChange,
  isLoading,
  placeholder = 'Search...',
  creatable = false,
}: SearchableMultiSelectProps) {
  const [open, setOpen] = useState<boolean>(false);
  const [inputValue, setInputValue] = useState<string>('');
  const [popoverWidth, setPopoverWidth] = useState<number>(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const anchorRef = useRef<HTMLDivElement>(null);

  const handleSelect = (item: SelectedItem) => {
    const itemValue = typeof item === 'object' ? item.value : item;
    const itemIdentifier = typeof item === 'object' ? item.identifier : undefined;

    const matches = (s: SelectedItem) => {
      const sValue = typeof s === 'object' ? s.value : s;
      const sIdentifier = typeof s === 'object' ? s.identifier : undefined;
      const valueMatch = sValue.toLowerCase() === itemValue.toLowerCase();
      if (!sIdentifier || !itemIdentifier) return valueMatch;
      return valueMatch && sIdentifier === itemIdentifier;
    };

    if (selected.some(matches)) {
      onChange(selected.filter((s) => !matches(s)));
    } else {
      onChange([...selected, item]);
    }

    setInputValue('');
    setTimeout(() => inputRef.current?.focus(), 0);
  };

  const handleCreateValue = () => {
    const trimmed = inputValue.trim();
    if (!trimmed) return;

    const alreadySelected = selected.some(
      (s) => (typeof s === 'object' ? s.value : s).toLowerCase() === trimmed.toLowerCase()
    );
    if (!alreadySelected) {
      const hasIdentifiers =
        selected.some((s) => typeof s === 'object') || options.some((o) => o.identifier);
      const newItem: SelectedItem = hasIdentifiers ? { value: trimmed } : trimmed;
      onChange([...selected, newItem]);
    }

    setInputValue('');
    onSearchChange?.('');
    setTimeout(() => inputRef.current?.focus(), 0);
  };

  const handleInputChange = (e: ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setInputValue(val);
    onSearchChange?.(val);
    if (!open) setOpen(true);
  };

  const handleInputKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (creatable && e.key === 'Enter' && inputValue.trim()) {
      e.preventDefault();
      handleCreateValue();
    }
  };

  const handleInputFocus = () => {
    if (anchorRef.current) setPopoverWidth(anchorRef.current.offsetWidth);
    setOpen(true);
  };

  const showCreateOption =
    creatable &&
    inputValue.trim() &&
    !options.some((o) => o.value.toLowerCase() === inputValue.trim().toLowerCase()) &&
    !selected.some(
      (s) => (typeof s === 'object' ? s.value : s).toLowerCase() === inputValue.trim().toLowerCase()
    );

  return (
    <Popover open={open}>
      <PopoverAnchor asChild>
        <div ref={anchorRef} className="relative w-full">
          <Input
            ref={inputRef}
            value={inputValue}
            onChange={handleInputChange}
            onKeyDown={handleInputKeyDown}
            onFocus={handleInputFocus}
            placeholder={isLoading ? 'Loading...' : placeholder}
            autoComplete="off"
            className="w-full pr-8"
          />
          <ChevronDown
            className={cn(
              'text-muted-foreground absolute top-1/2 right-2.5 h-4 w-4 -translate-y-1/2 cursor-pointer transition-transform',
              open && 'rotate-180'
            )}
            onMouseDown={(e) => {
              e.preventDefault();
              setOpen((prev) => !prev);
            }}
          />
        </div>
      </PopoverAnchor>
      <PopoverContent
        className="w-auto p-0"
        align="start"
        style={popoverWidth ? { width: popoverWidth } : undefined}
        onOpenAutoFocus={(e) => e.preventDefault()}
        onInteractOutside={(e) => {
          if (anchorRef.current?.contains(e.target as Node)) return;
          setOpen(false);
        }}
      >
        <Command shouldFilter={!onSearchChange}>
          <CommandList>
            {isLoading ? (
              <CommandLoading>
                <CommandEmpty>Loading...</CommandEmpty>
              </CommandLoading>
            ) : (
              inputValue && !showCreateOption && <CommandEmpty>No results found.</CommandEmpty>
            )}
            <CommandGroup>
              {showCreateOption && (
                <CommandItem
                  key={`__create__${inputValue.trim()}`}
                  value={`__create__${inputValue.trim()}`}
                  onSelect={handleCreateValue}
                >
                  <Plus className="mr-2 h-4 w-4" />
                  <span>Add &quot;{inputValue.trim()}&quot;</span>
                </CommandItem>
              )}
              {options.map((option) => {
                const isSelected = selected.some((s) => {
                  const sValue = typeof s === 'object' ? s.value : s;
                  const sIdentifier = typeof s === 'object' ? s.identifier : undefined;
                  const valueMatch = sValue.toLowerCase() === option.value.toLowerCase();
                  if (!sIdentifier) return valueMatch;
                  return valueMatch && sIdentifier === option.identifier;
                });
                return (
                  <CommandItem
                    key={option.identifier ? `${option.identifier}:${option.value}` : option.value}
                    value={option.value}
                    onSelect={() =>
                      handleSelect(
                        option.identifier
                          ? { value: option.value, identifier: option.identifier }
                          : option.value
                      )
                    }
                  >
                    <div
                      className={cn(
                        'border-primary mr-2 flex h-4 w-4 items-center justify-center rounded-sm border',
                        isSelected
                          ? 'bg-primary text-primary-foreground'
                          : 'opacity-50 [&_svg]:invisible'
                      )}
                    >
                      <Check className="h-4 w-4" />
                    </div>
                    <span>{option.label}</span>
                  </CommandItem>
                );
              })}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  );
}
