'use client';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { LeadSearchFilterSave } from '@/types/leadSearch.types';

import { Bookmark, Clock, Users } from 'lucide-react';

type SavedSearchesDrawerProps = {
  isOpen: boolean;
  onClose: () => void;
  savedSearches: LeadSearchFilterSave[];
  onLoadSearch: (search: LeadSearchFilterSave) => void;
  isLoading?: boolean;
};

const formatFilterLabel = (key: string): string => {
  const labels: Record<string, string> = {
    contactNames: 'Names',
    companyNames: 'Companies',
    cities: 'Cities',
    states: 'States',
    countries: 'Countries',
    companyCities: 'Company Cities',
    companyStates: 'Company States',
    companyCountries: 'Company Countries',
    regions: 'Regions',
    keywords: 'Keywords',
    industries: 'Industries',
    employeeRanges: 'Employee Ranges',
    revenueRanges: 'Revenue Ranges',
    technologies: 'Technologies',
    toolsServices: 'Tools & Services',
    titles: 'Titles',
    seniority: 'Seniority',
    departments: 'Departments',
    postalCodes: 'Postal Codes',
    sicCodes: 'SIC Codes',
    naicsCodes: 'NAICS Codes',
  };
  return labels[key] || key;
};

const formatDate = (date: Date): string => {
  const month = date.toLocaleDateString('en-US', { month: 'short' });
  const day = date.getDate();
  const year = date.getFullYear();
  return `${month} ${day}, ${year}`;
};

export const SavedSearchesDrawer = ({
  isOpen,
  onClose,
  savedSearches,
  onLoadSearch,
  isLoading = false,
}: SavedSearchesDrawerProps) => {
  return (
    <Sheet open={isOpen} onOpenChange={onClose}>
      <SheetContent className="bg-card border-border flex w-96 flex-col gap-0 p-0 sm:max-w-md">
        <SheetHeader className="border-border shrink-0 border-b px-6 pt-6 pb-4">
          <SheetTitle className="text-foreground text-xl font-semibold">Saved Searches</SheetTitle>
          <SheetDescription className="text-muted-foreground text-sm">
            {savedSearches.length} saved search{savedSearches.length !== 1 ? 'es' : ''}
          </SheetDescription>
        </SheetHeader>
        <div className="flex-1 space-y-3 overflow-y-auto px-6 py-6">
          {isLoading ? (
            <div className="py-12 text-center">
              <p className="text-muted-foreground text-sm">Loading saved searches...</p>
            </div>
          ) : savedSearches.length === 0 ? (
            <div className="py-12 text-center">
              <Bookmark className="text-muted-foreground/30 mx-auto mb-3 h-12 w-12" />
              <p className="text-muted-foreground text-sm">No saved searches yet</p>
              <p className="text-muted-foreground mt-1 text-xs">
                Save a search to quickly access it later
              </p>
            </div>
          ) : (
            savedSearches.map((search) => (
              <div
                key={search.id}
                className="border-border group rounded-lg border p-4 transition-colors"
              >
                <div className="mb-2 flex items-start justify-between">
                  <h3 className="text-foreground line-clamp-2 text-sm font-medium">
                    {search.title || 'Untitled Search'}
                  </h3>
                </div>
                <div className="text-muted-foreground mb-3 flex items-center gap-3 text-xs">
                  <span className="flex items-center gap-1">
                    <Clock className="h-3 w-3" />
                    {formatDate(new Date(search.createdAt))}
                  </span>
                  {search.resultCount !== undefined && search.resultCount !== null && (
                    <span className="flex items-center gap-1">
                      <Users className="h-3 w-3" />
                      {search.resultCount.toLocaleString()} results
                    </span>
                  )}
                </div>
                <div className="mb-3 flex flex-wrap gap-1">
                  {Object.entries(search)
                    .filter(
                      ([key, values]) =>
                        Array.isArray(values) &&
                        values.length > 0 &&
                        key !== 'title' &&
                        key !== 'resultCount' &&
                        key !== 'id' &&
                        key !== 'userId' &&
                        key !== 'type' &&
                        key !== 'createdAt'
                    )
                    .map(([key, values]) => (
                      <Badge key={key} variant="secondary" className="text-xs">
                        {formatFilterLabel(key)}: {(values as unknown[]).length}
                      </Badge>
                    ))}
                </div>
                <Button
                  size="sm"
                  variant="secondary"
                  className="mt-3 h-8 w-full text-xs"
                  onClick={() => onLoadSearch(search)}
                >
                  Load Search
                </Button>
              </div>
            ))
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
};
