import { ReactNode } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { Button } from '@/components/ui/button';

import { ListFilter, Mail, Plus } from 'lucide-react';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';

type SearchResultsHeaderProps = {
  onCollapse?: () => void;
  selectedCount?: number;
  proceedButtonText?: string;
  isProceeding?: boolean;
  onProceed?: () => void;
  onAddContacts?: () => void;
  onSendEmail?: () => void;
  resultsSearch?: string;
  setResultsSearch?: (searchQuery: string) => void;
  searchPlaceholder?: string;
  addToListSlot?: ReactNode;
};

const SearchResultsHeader = ({
  onCollapse,
  selectedCount = 0,
  proceedButtonText = 'Proceed',
  isProceeding,
  onProceed,
  onSendEmail,
  onAddContacts,
  searchPlaceholder,
  resultsSearch,
  setResultsSearch,
  addToListSlot,
}: SearchResultsHeaderProps) => {
  return (
    <div className="border-border bg-card h-16 border-b px-6 py-4">
      <div className="flex items-center justify-between gap-4">
        <div className="flex max-w-2xl flex-1 items-center gap-3">
          <Button
            variant="ghost"
            size="icon"
            aria-label="Toggle filter panel"
            onClick={onCollapse}
            className="shrink-0"
          >
            <ListFilter className="h-4 w-4" aria-hidden="true" />
          </Button>
          <div className="relative flex-1">
            <SearchBar
              value={resultsSearch}
              onChange={setResultsSearch}
              placeholder={searchPlaceholder}
            />
          </div>
        </div>
        <div className="flex items-center gap-4">
          {onAddContacts && (
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="outline"
                  aria-label={
                    selectedCount > 0
                      ? `Add ${selectedCount} selected to campaign`
                      : 'Add contacts to campaign'
                  }
                  disabled={selectedCount === 0 || isProceeding}
                  className="shrink-0"
                  onClick={onAddContacts}
                >
                  <Plus className="h-4 w-4" aria-hidden="true" />
                  {selectedCount > 0 && (
                    <span
                      className="ml-2 rounded-full bg-white/20 px-2 py-0.5 text-xs"
                      aria-hidden="true"
                    >
                      {selectedCount}
                    </span>
                  )}
                </Button>
              </TooltipTrigger>
              <TooltipContent>Add Contacts to Campaign</TooltipContent>
            </Tooltip>
          )}
          {addToListSlot}
          {onSendEmail && (
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="outline"
                  aria-label={
                    selectedCount > 0 ? `Send email to ${selectedCount} selected` : 'Send email'
                  }
                  disabled={selectedCount === 0 || isProceeding}
                  className="shrink-0"
                  onClick={onSendEmail}
                >
                  <Mail className="h-4 w-4" aria-hidden="true" />
                  {selectedCount > 0 && (
                    <span
                      className="ml-2 rounded-full bg-white/20 px-2 py-0.5 text-xs"
                      aria-hidden="true"
                    >
                      {selectedCount}
                    </span>
                  )}
                </Button>
              </TooltipTrigger>
              <TooltipContent>Send Email</TooltipContent>
            </Tooltip>
          )}
          <Button
            disabled={selectedCount === 0 || isProceeding}
            className="shrink-0"
            onClick={onProceed}
          >
            <Plus className="mr-2 h-4 w-4" />
            {proceedButtonText}
            {selectedCount > 0 && (
              <span className="ml-2 rounded-full bg-white/20 px-2 py-0.5 text-xs">
                {selectedCount}
              </span>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};

export { SearchResultsHeader };
