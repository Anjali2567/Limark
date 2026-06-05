'use client';

import { useState, useRef, useCallback, useMemo, Fragment, MouseEvent } from 'react';
import { ChevronDown, Plus } from 'lucide-react';
import { cn } from '@/lib/utils/helpers';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { ChipView } from './views/ChipView';
import { SelectableListItem } from './views/SelectableListItem';
import { Command, CommandList } from '@/components/ui/command';

export type MultiSelectAutoCompleteItem = {
  value: string;
  label: string;
  isNew?: boolean;
  toolTip?: string;
  children?: MultiSelectAutoCompleteItem[];
};

export type MultiSelectAutoCompleteProps = {
  id?: string;
  label?: string;
  placeholder?: string;
  items: MultiSelectAutoCompleteItem[];
  selectedItems: string[];
  onChange: (newSelected: MultiSelectAutoCompleteItem[]) => void;
  className?: string;
  disabled?: boolean;
  isError?: boolean;
  errorText?: string;
  allowNew?: boolean;
  hideSearch?: boolean;
  dataTestId?: string;
};

const flattenItems = (
  items: MultiSelectAutoCompleteItem[] | MultiSelectAutoCompleteItem
): MultiSelectAutoCompleteItem[] => {
  const itemsArray = Array.isArray(items) ? items : [items];
  return itemsArray.reduce<MultiSelectAutoCompleteItem[]>((acc, item) => {
    const { children, ...rest } = item;
    acc.push(rest);
    if (children) {
      acc.push(...flattenItems(children));
    }
    return acc;
  }, []);
};

const filterItems = (
  items: MultiSelectAutoCompleteItem[],
  query: string
): MultiSelectAutoCompleteItem[] => {
  const q = query.toLowerCase();
  return items.filter((item) => item.label.toLowerCase().includes(q));
};

export function MultiSelectAutoComplete({
  id,
  label,
  placeholder = 'Select or type...',
  items = [],
  selectedItems = [],
  onChange,
  className = '',
  disabled = false,
  isError = false,
  errorText = '',
  allowNew = false,
  hideSearch = true,
  dataTestId,
}: MultiSelectAutoCompleteProps) {
  const [query, setQuery] = useState('');
  const [open, setOpen] = useState(false);
  const [visibleItems, setVisibleItems] = useState(50);
  const scrollRef = useRef<HTMLDivElement>(null);

  const [flattenedBaseItems, existingIds] = useMemo(() => {
    const flattened = flattenItems(items);
    const ids = new Set(flattened.map((item) => item.value));
    return [flattened, ids];
  }, [items]);

  const allItems = useMemo(() => {
    const combined: MultiSelectAutoCompleteItem[] = [];
    selectedItems.forEach((item) => {
      if (!existingIds.has(item)) {
        combined.push({ value: item, label: item, isNew: true });
      }
    });
    return [...combined, ...items];
  }, [items, selectedItems, existingIds]);

  const flattenedItems = useMemo(() => {
    if (selectedItems.every((item) => existingIds.has(item))) {
      return flattenedBaseItems;
    }
    return flattenItems(allItems);
  }, [allItems, flattenedBaseItems, selectedItems, existingIds]);

  const itemsSelected = useMemo(() => {
    return selectedItems.map((item) => {
      const existingItem = flattenedBaseItems.find(
        (flattenedItem) => flattenedItem.value === item || flattenedItem.label === item
      );
      if (existingItem) {
        return { value: existingItem.value, label: existingItem.label };
      }
      const newItem = allItems.find((itemObj) => itemObj.isNew && itemObj.label === item);
      return newItem
        ? { value: newItem.value, label: newItem.label }
        : { value: item, label: item };
    });
  }, [selectedItems, flattenedBaseItems, allItems]);

  const filteredItems = useMemo(() => {
    return query.trim() ? filterItems(flattenedItems, query) : allItems;
  }, [query, allItems, flattenedItems]);

  const handleAddNew = useCallback(() => {
    if (!query.trim()) return;

    const normalizedQuery = query.toLowerCase();
    const existingItem = items.find((item) => item.label.toLowerCase() === normalizedQuery);

    const newSelectedItems = existingItem
      ? [...itemsSelected, existingItem]
      : [
          ...itemsSelected,
          {
            value: query.trim(),
            label: query.trim(),
            isNew: true,
          },
        ];
    onChange(newSelectedItems);
    setQuery('');
  }, [query, items, itemsSelected, onChange]);

  const removeChip = useCallback(
    (item: MultiSelectAutoCompleteItem, e: MouseEvent) => {
      e.stopPropagation();
      onChange(itemsSelected.filter((s) => s.value !== item.value));
    },
    [itemsSelected, onChange]
  );

  const findParent = useCallback(
    (childValue: string): MultiSelectAutoCompleteItem | undefined => {
      return items.find((parent) => parent.children?.some((child) => child.value === childValue));
    },
    [items]
  );

  const areAllChildrenSelected = useCallback(
    (parent: MultiSelectAutoCompleteItem, selected: MultiSelectAutoCompleteItem[]) => {
      if (!parent.children?.length) return false;
      return parent.children.every((child) => selected.some((sel) => sel.value === child.value));
    },
    []
  );

  const handleChange = useCallback(
    (isChecked: boolean, value: MultiSelectAutoCompleteItem) => {
      let updatedSelected = [...itemsSelected];

      if (isChecked) {
        updatedSelected.push(value);
      } else {
        updatedSelected = updatedSelected.filter((s) => s.value !== value.value);
      }

      const parent = findParent(value.value);
      if (parent) {
        const allChildrenSelected = areAllChildrenSelected(parent, updatedSelected);

        if (allChildrenSelected) {
          if (!updatedSelected.some((s) => s.value === parent.value)) {
            updatedSelected.push({ value: parent.value, label: parent.label });
          }
        } else {
          updatedSelected = updatedSelected.filter((s) => s.value !== parent.value);
        }
      }

      onChange(updatedSelected);
    },
    [itemsSelected, findParent, areAllChildrenSelected, onChange]
  );

  const handleSectionSelection = useCallback(
    (isChecked: boolean, item: MultiSelectAutoCompleteItem) => {
      if (!item.children) {
        handleChange(isChecked, item);
        return;
      }

      const sectionItems = flattenItems(item);
      let updatedSelected: MultiSelectAutoCompleteItem[];

      if (isChecked) {
        const sectionValues = new Set(sectionItems.map((i) => i.value));
        const filtered = itemsSelected.filter((i) => !sectionValues.has(i.value));
        updatedSelected = [...filtered, ...sectionItems, item];
      } else {
        const sectionValues = new Set(sectionItems.map((i) => i.value));
        updatedSelected = itemsSelected.filter(
          (i) => !sectionValues.has(i.value) && i.value !== item.value
        );
      }

      onChange(updatedSelected);
    },
    [itemsSelected, onChange, handleChange]
  );

  const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
    const { scrollTop, clientHeight, scrollHeight } = e.currentTarget;
    if (scrollHeight - (scrollTop + clientHeight) < 25) {
      setVisibleItems((prev) => prev + 25);
    }
  };

  return (
    <div id={id} className={cn('relative w-full', className)}>
      {label && <label className="mb-1 flex items-center text-sm font-medium">{label}</label>}

      <Popover open={open} onOpenChange={setOpen} modal={true}>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            role="combobox"
            aria-expanded={open}
            disabled={disabled}
            className={cn(
              'bg-input-background w-full justify-between px-3 py-2',
              isError && 'border-destructive'
            )}
            data-testid={dataTestId}
          >
            <ChipView
              itemsSelected={itemsSelected}
              placeholder={placeholder}
              removeChip={removeChip}
              disabled={disabled}
            />
            {!disabled && <ChevronDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />}
          </Button>
        </PopoverTrigger>

        <PopoverContent
          className="w-full p-0"
          align="start"
          style={{ width: 'var(--radix-popover-trigger-width)' }}
        >
          <Command className="max-h-80" shouldFilter={false}>
            {!hideSearch && (
              <div className="flex items-center gap-2 border-b p-2">
                <div className="focus-within:ring-ring flex w-full items-center rounded-md border focus-within:ring-2">
                  <Input
                    placeholder={placeholder}
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter' && allowNew && query.trim()) {
                        e.preventDefault();
                        handleAddNew();
                      }
                    }}
                    className="border-0 focus-visible:ring-0 focus-visible:ring-offset-0"
                  />
                  {allowNew && (
                    <Button
                      type="button"
                      size="sm"
                      onClick={handleAddNew}
                      disabled={!query.trim()}
                      className="mr-2 h-6 w-6 rounded-full p-0"
                      title="Add new"
                    >
                      <Plus className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              </div>
            )}

            {filteredItems.length === 0 ? (
              <div className="text-muted-foreground p-2 text-sm">No matching items</div>
            ) : (
              <CommandList
                ref={scrollRef}
                onScroll={handleScroll}
                className="max-h-64 overflow-x-hidden overflow-y-auto"
              >
                <div className="max-h-none! overflow-visible!">
                  {filteredItems.slice(0, visibleItems).map((item) => (
                    <Fragment key={item.value}>
                      <SelectableListItem
                        item={item}
                        selected={itemsSelected.some((s) => s.value === item.value)}
                        onChange={(isChecked) => handleSectionSelection(isChecked, item)}
                        dataTestId={dataTestId ? `${dataTestId}_${item.label}` : undefined}
                      />
                      {item.children && item.children.length > 0 && (
                        <div>
                          {item.children.map((child) => (
                            <SelectableListItem
                              key={child.value}
                              item={child}
                              selected={itemsSelected.some((s) => s.value === child.value)}
                              onChange={(isChecked) => handleChange(isChecked, child)}
                              isChild
                              dataTestId={dataTestId ? `${dataTestId}_${child.label}` : undefined}
                            />
                          ))}
                        </div>
                      )}
                    </Fragment>
                  ))}
                </div>
              </CommandList>
            )}
          </Command>
        </PopoverContent>
      </Popover>

      {isError && errorText && <p className="text-destructive mt-1 text-sm">{errorText}</p>}
    </div>
  );
}
