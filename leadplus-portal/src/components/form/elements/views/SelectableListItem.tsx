import { CommandItem } from '@/components/ui/command';
import { MultiSelectAutoCompleteItem } from '../MultiSelectAutocomplete';
import { cn } from '@/lib/utils/helpers';
import { Checkbox } from '@/components/ui/checkbox';
import { Info } from 'lucide-react';

export const SelectableListItem = ({
  item,
  selected,
  onChange,
  isChild = false,
  dataTestId,
}: {
  item: MultiSelectAutoCompleteItem;
  selected: boolean;
  onChange: (isChecked: boolean) => void;
  isChild?: boolean;
  dataTestId?: string;
}) => {
  return (
    <CommandItem
      value={item.value}
      onSelect={() => onChange(!selected)}
      className={cn(
        'flex cursor-pointer items-center gap-2 border-b px-4 py-2 text-sm',
        isChild && 'pl-8'
      )}
      data-testid={dataTestId}
    >
      <Checkbox
        checked={selected}
        onCheckedChange={onChange}
        onClick={(e) => e.stopPropagation()}
        className="text-white!"
      />
      <div className="flex w-full items-center justify-between">
        <span className={cn('truncate', item.isNew && 'font-medium italic')}>{item.label}</span>
        {item.toolTip && <Info className="text-muted-foreground h-4 w-4" />}
      </div>
    </CommandItem>
  );
};
