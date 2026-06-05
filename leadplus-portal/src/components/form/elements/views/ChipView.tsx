import { MouseEvent, useCallback, useEffect, useRef, useState } from 'react';

import { MultiSelectAutoCompleteItem } from '../MultiSelectAutocomplete';

import { X } from 'lucide-react';

export const ChipView = ({
  itemsSelected,
  placeholder,
  removeChip,
  disabled,
}: {
  itemsSelected: MultiSelectAutoCompleteItem[];
  placeholder: string;
  removeChip: (item: MultiSelectAutoCompleteItem, e: MouseEvent) => void;
  disabled?: boolean;
}) => {
  const chipContainerRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLDivElement>(null);
  const [visibleCount, setVisibleCount] = useState<number>(itemsSelected.length);

  useEffect(() => {
    setVisibleCount(itemsSelected.length);
  }, [itemsSelected]);

  const updateVisibleChips = useCallback(() => {
    if (!buttonRef.current || !chipContainerRef.current) return;
    const availableWidth = buttonRef.current.offsetWidth - 40;
    if (availableWidth <= 0) {
      setVisibleCount(itemsSelected.length > 0 ? 1 : 0);
      return;
    }

    let totalWidth = 0;
    let count = 0;
    const chips = chipContainerRef.current.querySelectorAll('.chip');
    const hiddenIndicatorWidth = 150;

    for (let i = 0; i < chips.length; i++) {
      const chip = chips[i] as HTMLElement;
      const chipWidth = chip.offsetWidth;
      if (totalWidth + chipWidth + hiddenIndicatorWidth > availableWidth) {
        break;
      }
      totalWidth += chipWidth;
      count++;
    }
    setVisibleCount(Math.max(count, itemsSelected.length > 0 ? 1 : 0));
  }, [itemsSelected.length]);

  useEffect(() => {
    const resizeObserver = new ResizeObserver(() => updateVisibleChips());
    if (buttonRef.current) {
      resizeObserver.observe(buttonRef.current);
    }
    return () => {
      resizeObserver.disconnect();
    };
  }, [updateVisibleChips]);

  const hiddenCount = Math.max(itemsSelected.length - visibleCount, 0);

  return (
    <div
      ref={buttonRef}
      className="flex w-full flex-1 items-center gap-1 overflow-hidden font-normal"
    >
      <div ref={chipContainerRef} className="flex items-center gap-1">
        {itemsSelected.length === 0 ? (
          <span className="text-muted-foreground">{placeholder}</span>
        ) : (
          <>
            {itemsSelected.slice(0, visibleCount).map((item) => (
              <div
                key={item.value}
                className="chip bg-primary inline-flex shrink-0 items-center gap-1 rounded-md px-2 py-1 text-xs text-white"
              >
                <span>{item.label}</span>
                {!disabled && (
                  <span
                    role="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      removeChip(item, e);
                    }}
                    className="hover:bg-muted cursor-pointer rounded-full p-0.5"
                  >
                    <X className="h-3 w-3" />
                  </span>
                )}
              </div>
            ))}
            {hiddenCount > 0 && (
              <span className="text-muted-foreground shrink-0 text-xs">+{hiddenCount} more</span>
            )}
          </>
        )}
      </div>
    </div>
  );
};
