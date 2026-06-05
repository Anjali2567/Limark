import { Control, useWatch } from 'react-hook-form';

import { Badge } from '@/components/ui/badge';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import useToggle from '@/hooks/useToggle';
import { cn } from '@/lib/utils/helpers';
import { LeadSearchPayload } from '@/types/leadSearch.types';
import { FilterOption, FilterOptionType } from './FilterOption';
import { Button } from '@/components/ui/button';

import { ChevronDown, ChevronUp, Info, XCircle } from 'lucide-react';

type FilterGroupType = {
  id: string;
  label: string;
  filters: FilterOptionType[];
};

type FilterGroupProps = {
  group: FilterGroupType;
  control: Control<LeadSearchPayload>;
  getFilterOptions: (sectionId: string) => {
    options: { label: string; value: string; identifier?: string }[];
    isLoading?: boolean;
    searchValue?: string;
  };
  handleSearchChange: (sectionId: string, value: string) => void;
  onClearGroup?: () => void;
  getDefaultValues?: (sectionId: string) => string[];
};

const FilterGroup = ({
  group,
  control,
  getFilterOptions,
  handleSearchChange,
  onClearGroup,
  getDefaultValues,
}: FilterGroupProps) => {
  const { value: isExpanded, toggle } = useToggle();

  const fieldValues = useWatch({ control, name: group.filters.map((f) => f.id) });

  const totalSelected = (Array.isArray(fieldValues) ? fieldValues : [fieldValues]).reduce(
    (sum, val) => sum + (Array.isArray(val) ? val.length : val ? 1 : 0),
    0
  );

  const ToggleIcon = isExpanded ? ChevronUp : ChevronDown;
  const contentId = `filter-group-${group.id}`;

  return (
    <div className={cn('border-border border-b', isExpanded && 'border-t border-b')}>
      <div
        role="button"
        onClick={toggle}
        aria-expanded={isExpanded}
        aria-controls={contentId}
        className="hover:bg-muted/20 flex h-full w-full cursor-pointer items-center justify-between px-8 py-3 transition-colors"
      >
        <div className="flex min-w-0 flex-1 items-center gap-1">
          <span className="text-foreground text-sm font-semibold">{group.label}</span>
          <Tooltip>
            <TooltipTrigger asChild onClick={(e) => e.stopPropagation()}>
              <Info className="text-muted-foreground/60 hover:text-muted-foreground h-3.5 w-3.5 cursor-help transition-colors" />
            </TooltipTrigger>
            <TooltipContent side="right">
              <p>Filter by {group.label.toLowerCase()}</p>
            </TooltipContent>
          </Tooltip>
        </div>
        <div className="flex items-center gap-2">
          {totalSelected > 0 && (
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="none"
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    onClearGroup?.();
                  }}
                  aria-label={`Clear all ${group.label} filters`}
                  className="group h-auto p-0"
                >
                  <Badge
                    variant="secondary"
                    className="bg-primary text-primary-foreground pointer-events-none flex items-center rounded-full border-0 px-1.5 py-0.5 text-[10px] font-medium shadow-none"
                  >
                    {totalSelected}
                    <XCircle className="hidden h-5 w-5 transition-all duration-1000 group-hover:flex" />
                  </Badge>
                </Button>
              </TooltipTrigger>
              <TooltipContent side="top">Clear All {group.label}</TooltipContent>
            </Tooltip>
          )}
          <ToggleIcon className="text-muted-foreground h-5 w-5 shrink-0" />
        </div>
      </div>

      {isExpanded && (
        <div className="bg-muted/10 space-y-4 px-8 pb-4">
          {group.filters.map((filter) => {
            const { isLoading, searchValue, options } = getFilterOptions(filter.id);
            return (
              <FilterOption
                key={filter.id}
                section={{ ...filter, options, defaultValues: getDefaultValues?.(filter.id) }}
                control={control}
                searchValue={searchValue}
                onSearchChange={(value) => handleSearchChange(filter.id, value)}
                isLoading={isLoading}
                flat
              />
            );
          })}
        </div>
      )}
    </div>
  );
};

export { FilterGroup, type FilterGroupType };
