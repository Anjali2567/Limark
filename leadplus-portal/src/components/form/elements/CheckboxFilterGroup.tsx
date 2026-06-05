'use client';

import { useState } from 'react';

import { Checkbox } from '@/components/ui/checkbox';
import { ScrollArea } from '@/components/ui/scroll-area';
import { cn } from '@/lib/utils/helpers';

import { Search } from 'lucide-react';

type Option = {
  label: string;
  value: string | number;
};

type CheckboxFilterGroupProps = {
  options: Option[];
  selectedValues: (string | number)[];
  onChange: (values: (string | number)[]) => void;
  groupId: string;
  maxHeight?: number;
  className?: string;
};

export const CheckboxFilterGroup = ({
  options,
  selectedValues,
  onChange,
  groupId,
  maxHeight = 200,
  className,
}: CheckboxFilterGroupProps) => {
  const [search, setSearch] = useState('');

  const filteredOptions = search.trim()
    ? options.filter((opt) => opt.label.toLowerCase().includes(search.toLowerCase()))
    : options;

  const isAllSelected =
    filteredOptions.length > 0 &&
    filteredOptions.every((opt) => selectedValues.includes(opt.value));
  const isSomeSelected =
    filteredOptions.some((opt) => selectedValues.includes(opt.value)) && !isAllSelected;

  const handleSelectAll = (checked: boolean | 'indeterminate') => {
    if (checked === true) {
      const filteredValues = filteredOptions.map((opt) => opt.value);
      onChange([...new Set([...selectedValues, ...filteredValues])]);
    } else {
      const filteredSet = new Set(filteredOptions.map((opt) => opt.value));
      onChange(selectedValues.filter((v) => !filteredSet.has(v)));
    }
  };

  const handleToggleOption = (value: string | number, checked: boolean | 'indeterminate') => {
    if (checked === true) {
      onChange([...selectedValues, value]);
    } else {
      onChange(selectedValues.filter((v) => v !== value));
    }
  };

  const optimizedMaxHeight = maxHeight + 40;

  return (
    <div className={cn('flex flex-col gap-3', className)}>
      <div className="relative">
        <Search className="absolute top-1/2 left-3 h-3.5 w-3.5 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search..."
          className="w-full rounded-md border border-slate-200 bg-white py-1.5 pr-3 pl-8 text-sm outline-none placeholder:text-slate-400 focus:border-slate-400"
        />
      </div>

      <div className="flex items-center justify-between px-1">
        <div className="flex items-center space-x-2">
          <Checkbox
            id={`${groupId}-select-all`}
            checked={isAllSelected ? true : isSomeSelected ? 'indeterminate' : false}
            onCheckedChange={handleSelectAll}
          />
          <label
            htmlFor={`${groupId}-select-all`}
            className="cursor-pointer text-sm font-medium text-slate-600 transition-colors hover:text-slate-900"
          >
            Select All
          </label>
        </div>
        <span className="text-xs text-slate-400">
          {selectedValues.length} / {options.length}
        </span>
      </div>

      <ScrollArea className="px-1" style={{ maxHeight: `${optimizedMaxHeight}px` }}>
        <div className="flex flex-col gap-3 pb-2">
          {filteredOptions.length === 0 ? (
            <p className="py-2 text-center text-sm text-slate-400">No results</p>
          ) : (
            filteredOptions.map((option) => (
              <div key={option.value} className="flex items-center space-x-2">
                <Checkbox
                  id={`${groupId}-${option.value}`}
                  checked={selectedValues.includes(option.value)}
                  onCheckedChange={(checked) => handleToggleOption(option.value, checked)}
                />
                <label
                  htmlFor={`${groupId}-${option.value}`}
                  className="cursor-pointer text-sm font-medium text-slate-600 transition-colors hover:text-slate-900"
                >
                  {option.label}
                </label>
              </div>
            ))
          )}
        </div>
      </ScrollArea>
    </div>
  );
};
