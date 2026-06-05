'use client';

import { ReactNode } from 'react';
import { Filter } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { ScrollArea } from '@/components/ui/scroll-area';

type MultiselectFilterHeaderProps = {
  label: ReactNode;
  selectedValues: string[];
  options: string[];
  onSelectionChange: (values: string[]) => void;
};

const MultiselectFilterHeader = ({
  label,
  selectedValues,
  options,
  onSelectionChange,
}: MultiselectFilterHeaderProps) => {
  const handleToggle = (value: string) => {
    const newSelection = selectedValues.includes(value)
      ? selectedValues.filter((v) => v !== value)
      : [...selectedValues, value];
    onSelectionChange(newSelection);
  };

  const handleClear = () => {
    onSelectionChange([]);
  };

  return (
    <div className="flex items-center gap-2">
      {label}
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="ghost"
            size="icon"
            className="text-muted-foreground hover:text-foreground relative h-6 w-6 p-0 hover:bg-transparent"
          >
            <Filter className="h-4 w-4" fill="currentColor" />
            <span className="sr-only">Filter {String(label)}</span>
            {selectedValues.length > 0 && (
              <div className="absolute top-0 right-0 h-2 w-2 rounded-full bg-sky-500" />
            )}
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="start" onCloseAutoFocus={(e) => e.preventDefault()}>
          <div className="space-y-1 p-2">
            <div className="mb-1 flex items-center justify-between px-2 py-1.5">
              <span className="text-muted-foreground mr-4 text-xs font-semibold">
                Filter by {String(label)}
              </span>
              {selectedValues.length > 0 && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-auto p-0 text-xs text-sky-500 hover:bg-transparent hover:text-sky-600"
                  onClick={handleClear}
                >
                  Clear
                </Button>
              )}
            </div>
            {options.length === 0 ? (
              <div className="px-2 py-6 text-center">
                <span className="text-muted-foreground text-sm">No options available</span>
              </div>
            ) : options.length > 10 ? (
              <ScrollArea className="h-[300px]">
                <div className="space-y-1 pr-3">
                  {options.map((option) => (
                    <div
                      key={option}
                      className="hover:bg-secondary flex cursor-pointer items-center gap-2 rounded px-2 py-1.5"
                      onClick={(e) => {
                        e.preventDefault();
                        handleToggle(option);
                      }}
                    >
                      <Checkbox
                        checked={selectedValues.includes(option)}
                        onCheckedChange={() => handleToggle(option)}
                      />
                      <span className="text-sm">{option}</span>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            ) : (
              <div className="space-y-1">
                {options.map((option) => (
                  <div
                    key={option}
                    className="hover:bg-secondary flex cursor-pointer items-center gap-2 rounded px-2 py-1.5"
                    onClick={(e) => {
                      e.preventDefault();
                      handleToggle(option);
                    }}
                  >
                    <Checkbox
                      checked={selectedValues.includes(option)}
                      onCheckedChange={() => handleToggle(option)}
                    />
                    <span className="text-sm">{option}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
};

export { MultiselectFilterHeader };
