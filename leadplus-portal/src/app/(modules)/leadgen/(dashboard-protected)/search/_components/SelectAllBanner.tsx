import { Button } from '@/components/ui/button';
import { Loader2 } from 'lucide-react';

type SelectAllBannerProps = {
  pageCount: number;
  totalCount: number;
  selectedCount: number;
  isCurrentPageAllSelected: boolean;
  onSelectAll: () => void;
  onClearSelection: () => void;
  isLoading?: boolean;
  entityLabel?: string;
};

const SelectAllBanner = ({
  pageCount,
  totalCount,
  selectedCount,
  isCurrentPageAllSelected,
  onSelectAll,
  onClearSelection,
  isLoading = false,
  entityLabel = 'results',
}: SelectAllBannerProps) => {
  return (
    <div className="bg-primary/5 border-primary/20 border-b px-6 py-2 text-center text-sm">
      {isCurrentPageAllSelected && selectedCount < totalCount ? (
        <span>
          All {pageCount} on this page selected.{' '}
          <Button
            variant="link"
            className="h-auto p-0 text-sm font-semibold"
            onClick={onSelectAll}
            disabled={isLoading}
          >
            {isLoading && <Loader2 className="mr-1 inline h-3 w-3 animate-spin" />}
            Select all {totalCount} {entityLabel} matching this search
          </Button>
        </span>
      ) : (
        <span>
          {selectedCount} {entityLabel} selected.{' '}
          <Button
            variant="link"
            className="h-auto p-0 text-sm font-semibold"
            onClick={onClearSelection}
          >
            Clear selection
          </Button>
        </span>
      )}
    </div>
  );
};

export { SelectAllBanner };
