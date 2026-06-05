import { KeyboardEvent, ReactNode, useCallback, useMemo, useState } from 'react';
import { Control, Controller, ControllerRenderProps, FieldValues, Path } from 'react-hook-form';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { SearchableMultiSelect } from '@/components/ui/searchable-multi-select';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { InputType } from '@/constants/InputType';
import useToggle from '@/hooks/useToggle';
import { cn } from '@/lib/utils/helpers';
import { LeadSearchPayload } from '@/types/leadSearch.types';

import { ChevronDown, ChevronUp, Info, Plus, X, XCircle } from 'lucide-react';

type FilterValue = string | { value: string; identifier?: string };

type FilterOptionType<T extends FieldValues = LeadSearchPayload> = {
  id: Path<T>;
  label: string;
  type: InputType;
  placeholder?: string;
  options?: { label: string; value: string }[];
  isLoading?: boolean;
  defaultValues?: string[];
  creatable?: boolean;
};

type FilterOptionProps<T extends FieldValues = LeadSearchPayload> = {
  section: FilterOptionType<T>;
  control: Control<T>;
  onSearchChange?: (value: string) => void;
  isLoading?: boolean;
  searchValue?: string;
  flat?: boolean;
  showAddButton?: boolean;
};

const toValueArray = (value: unknown): FilterValue[] => {
  if (Array.isArray(value)) return value as FilterValue[];
  if (!value || typeof value === 'boolean') return [];
  return [value as FilterValue];
};

const getRawValue = (value: FilterValue) => (typeof value === 'object' ? value.value : value);

const getKey = (value: FilterValue) =>
  typeof value === 'object' ? `${value.identifier ?? 'item'}:${value.value}` : value;

const FilterOption = <T extends FieldValues = LeadSearchPayload>({
  section,
  control,
  onSearchChange,
  isLoading,
  searchValue,
  flat = false,
  showAddButton = false,
}: FilterOptionProps<T>) => {
  const { value: isExpanded, toggle } = useToggle();
  const [inputValue, setInputValue] = useState(searchValue ?? '');

  const displayIsLoading = isLoading ?? section.isLoading;
  const ToggleIcon = isExpanded ? ChevronUp : ChevronDown;
  const contentId = `filter-content-${section.id}`;

  const optionsLabelMap = useMemo(
    () => new Map(section.options?.map(({ value, label }) => [value, label]) ?? []),
    [section.options]
  );

  const defaultValueSet = useMemo(
    () => new Set(section.defaultValues ?? []),
    [section.defaultValues]
  );

  const addValue = useCallback(
    (currentValues: FilterValue[], onChange: (value: FilterValue[]) => void) => {
      const trimmedValue = inputValue.trim();
      if (!trimmedValue) return;

      onChange([...currentValues, trimmedValue]);
      setInputValue('');
    },
    [inputValue]
  );

  const handleAutoCompleteKeyDown = useCallback(
    (
      e: KeyboardEvent<HTMLInputElement>,
      currentValues: FilterValue[],
      onChange: (value: FilterValue[]) => void
    ) => {
      if (e.key !== 'Enter') return;

      e.preventDefault();
      addValue(currentValues, onChange);
    },
    [addValue]
  );

  const renderSelectedCount = useCallback(
    (fieldValue: ReactNode | object, onClear: () => void) => {
      const valueArray = toValueArray(fieldValue);

      if (!valueArray.length) return null;

      return (
        <Tooltip>
          <TooltipTrigger asChild>
            <Button
              variant="none"
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onClear();
              }}
              aria-label={`Clear all ${section.label} filters`}
              className="group h-auto p-0"
            >
              <Badge
                variant="secondary"
                className="bg-primary text-primary-foreground pointer-events-none flex items-center rounded-full border-0 px-1.5 py-0.5 text-[10px] font-medium shadow-none"
              >
                {valueArray.length}
                <XCircle className="hidden h-5 w-5 transition-all duration-300 group-hover:flex" />
              </Badge>
            </Button>
          </TooltipTrigger>
          <TooltipContent side="top">Clear All {section.label}</TooltipContent>
        </Tooltip>
      );
    },
    [section.label]
  );

  const renderTags = useCallback(
    (tags: FilterValue[], onChange: (value: FilterValue[]) => void) => {
      if (!tags.length) return null;

      return (
        <div className="mt-2 flex flex-wrap gap-1 overflow-hidden">
          {tags.map((tag) => {
            const rawValue = getRawValue(tag);
            const displayTag = optionsLabelMap.get(rawValue) ?? rawValue;
            const isDefault = defaultValueSet.has(rawValue);

            return (
              <Badge
                key={getKey(tag)}
                className={cn(
                  'max-w-full gap-1 rounded-md px-2.5 py-1 text-xs font-medium whitespace-normal',
                  isDefault
                    ? 'border border-gray-200 bg-gray-100 text-gray-600 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-300'
                    : 'bg-primary/10 text-primary'
                )}
                title={displayTag}
              >
                <span className="min-w-0 wrap-break-word">{displayTag}</span>

                <Button
                  variant="none"
                  type="button"
                  onClick={() => onChange(tags.filter((item) => item !== tag))}
                  className="hover:bg-primary/20 h-fit! shrink-0 rounded-full p-0.5! transition-colors"
                  aria-label={`Remove ${displayTag}`}
                >
                  <X className="h-3 w-3" />
                </Button>
              </Badge>
            );
          })}
        </div>
      );
    },
    [defaultValueSet, optionsLabelMap]
  );

  const renderInputByType = useCallback(
    (field: ControllerRenderProps<T, Path<T>>) => {
      const valueArray = toValueArray(field.value);

      if (section.type === InputType.DROPDOWN) {
        return (
          <SearchableMultiSelect
            selected={valueArray}
            options={section.options ?? []}
            onChange={field.onChange}
            onSearchChange={onSearchChange}
            isLoading={displayIsLoading}
            creatable={section.creatable !== false}
            placeholder={
              displayIsLoading
                ? 'Loading...'
                : (section.placeholder ?? `Search ${section.label.toLowerCase()}...`)
            }
          />
        );
      }

      if (section.type === InputType.AUTOCOMPLETE) {
        return (
          <div className="flex gap-2">
            <Input
              className="flex-1"
              placeholder={section.placeholder ?? `Add ${section.label.toLowerCase()}...`}
              value={inputValue}
              onChange={(e) => {
                setInputValue(e.target.value);
                onSearchChange?.(e.target.value);
              }}
              onKeyDown={(e) => handleAutoCompleteKeyDown(e, valueArray, field.onChange)}
            />

            {showAddButton && (
              <Button
                type="button"
                variant="outline"
                size="icon"
                onClick={() => addValue(valueArray, field.onChange)}
                disabled={!inputValue.trim()}
                aria-label={`Add ${section.label}`}
                className="shrink-0"
              >
                <Plus className="h-4 w-4" />
              </Button>
            )}
          </div>
        );
      }

      return null;
    },
    [
      section,
      displayIsLoading,
      inputValue,
      onSearchChange,
      showAddButton,
      handleAutoCompleteKeyDown,
      addValue,
    ]
  );

  const renderFieldContent = useCallback(
    (field: ControllerRenderProps<T, Path<T>>) => {
      const valueArray = toValueArray(field.value);

      return (
        <>
          {renderInputByType(field)}
          {renderTags(valueArray, field.onChange)}
        </>
      );
    },
    [renderInputByType, renderTags]
  );

  if (flat) {
    return (
      <div className="space-y-2">
        <label className="text-foreground text-sm font-semibold">{section.label}</label>
        <Controller
          control={control}
          name={section.id}
          render={({ field }) => renderFieldContent(field)}
        />
      </div>
    );
  }

  return (
    <div className={cn('border-border border-b', isExpanded && 'border-t')}>
      <div
        role="button"
        tabIndex={0}
        onClick={toggle}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            toggle();
          }
        }}
        aria-expanded={isExpanded}
        aria-controls={contentId}
        className="hover:bg-muted/20 flex h-full w-full cursor-pointer items-center justify-between px-8 py-3 transition-colors"
      >
        <div className="flex min-w-0 flex-1 items-center gap-1">
          <span className="text-foreground text-sm font-semibold">{section.label}</span>

          <Tooltip>
            <TooltipTrigger asChild>
              <button
                type="button"
                aria-label={`More info about ${section.label}`}
                onClick={(e) => e.stopPropagation()}
                className="focus-visible:ring-ring rounded-sm focus-visible:ring-2 focus-visible:outline-none"
              >
                <Info
                  aria-hidden="true"
                  className="text-muted-foreground/60 hover:text-muted-foreground h-3.5 w-3.5 cursor-help transition-colors"
                />
              </button>
            </TooltipTrigger>
            <TooltipContent side="right">
              <p>Filter by {section.label.toLowerCase()}</p>
            </TooltipContent>
          </Tooltip>
        </div>

        <div className="flex items-center gap-2">
          <Controller
            control={control}
            name={section.id}
            render={({ field }) => (
              <>{renderSelectedCount(field.value, () => field.onChange([]))}</>
            )}
          />
          <ToggleIcon className="text-muted-foreground h-5 w-5 shrink-0" />
        </div>
      </div>

      {isExpanded && (
        <div id={contentId} className="bg-muted/10 px-8 pb-4">
          <Controller
            control={control}
            name={section.id}
            render={({ field }) => renderFieldContent(field)}
          />
        </div>
      )}
    </div>
  );
};

export { FilterOption };
export type { FilterOptionType };
