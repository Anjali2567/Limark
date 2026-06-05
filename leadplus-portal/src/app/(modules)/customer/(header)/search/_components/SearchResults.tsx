import { useState, useMemo } from 'react';

import { Button } from '@/components/ui/button';
import { FormSelect } from '@/components/ui/form-select';
import { Pagination } from '@/components/tables/Pagination';
import { RequestForQuoteFormDrawer } from './RequestForQuoteFormDrawer';
import { PaginatedResponse } from '@/types/paginated-params';
import { VendorCardDetails, VendorDetailResponse } from '@/types/vendor.types';
import useToggle from '@/hooks/useToggle';
import { VendorCard } from './VendorCard';
import { useAuth } from '@/context/AuthContext';
import { useAuthModal } from '@/context/AuthModalContext';

import { toast } from 'sonner';
import { MapPin } from 'lucide-react';

const SORT_OPTIONS = [
  { label: 'Sort by: Rating', value: 'rating' },
  { label: 'Sort by: Relevance', value: 'relevance' },
];

type SearchResultsProps = {
  data?: PaginatedResponse<VendorDetailResponse>;
  isLoading: boolean;
  onPageChange: (page: number) => void;
};

const SearchResults = ({ data, isLoading, onPageChange }: SearchResultsProps) => {
  const [selectedVendors, setSelectedVendors] = useState<string[]>([]);
  const [sortBy, setSortBy] = useState<string>('rating');

  const { value: showRequestQuote, toggle: toggleRequestQuote } = useToggle(false);
  const { loggedInUser } = useAuth();
  const { showAuthModal } = useAuthModal();

  const transformedData: VendorCardDetails[] = useMemo(() => {
    const vendors = (data?.content || []).map((vendor) => {
      const year = parseInt(vendor.yearEstablished ?? '', 10);
      const established = !Number.isNaN(year) ? year : new Date().getFullYear();
      return {
        ...vendor,
        established,
        rating: 4,
        reviews: 4,
        responseTime: '24H',
        projects: 0,
      };
    });

    return vendors.sort((a, b) => {
      if (sortBy === 'rating') {
        return b.rating - a.rating;
      }
      return 0;
    });
  }, [data?.content, sortBy]);

  const toggleVendorSelection = (id: string) => {
    setSelectedVendors((prev) =>
      prev.includes(id) ? prev.filter((v) => v !== id) : [...prev, id]
    );
  };

  const handleRequestQuote = (id?: string) => {
    if (!loggedInUser) {
      showAuthModal();
      return;
    }
    if (!loggedInUser.verified) {
      toast.warning('Please verify your email address before submitting a quote request.');
      return;
    }
    if (id && !selectedVendors.includes(id)) {
      setSelectedVendors([id]);
    }
    toggleRequestQuote();
  };

  const handleClearSelection = () => {
    setSelectedVendors([]);
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex h-14 flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          {isLoading ? (
            <div className="space-y-2">
              <div className="h-8 w-48 animate-pulse rounded bg-gray-200" />
              <div className="h-4 w-32 animate-pulse rounded bg-gray-200" />
            </div>
          ) : (
            <>
              <h2 className="text-2xl font-bold tracking-tight text-gray-900">
                {data?.page.totalElements || 0} vendor{data?.page.totalElements !== 1 ? 's' : ''}{' '}
                found
              </h2>
              {selectedVendors.length > 0 && (
                <p className="text-muted-foreground mt-1 text-sm">
                  {selectedVendors.length} vendor{selectedVendors.length > 1 ? 's' : ''} selected
                </p>
              )}
            </>
          )}
        </div>
        <div className="flex items-center gap-3">
          <div className="w-48">
            <FormSelect
              options={SORT_OPTIONS}
              value={sortBy}
              onValueChange={setSortBy}
              placeholder="Sort by"
              className="h-10 bg-white"
            />
          </div>
          <Button
            onClick={() => handleRequestQuote()}
            disabled={selectedVendors.length === 0}
            className="bg-primary hover:bg-primary/90 text-primary-foreground hidden sm:flex"
          >
            Request Quote ({selectedVendors.length})
          </Button>
        </div>
      </div>

      <div className="grid gap-6">
        {isLoading ? (
          Array.from({ length: 5 }).map((_, idx) => (
            <div key={idx} className="h-64 w-full animate-pulse rounded-lg bg-gray-200" />
          ))
        ) : transformedData.length > 0 ? (
          transformedData.map((item) => (
            <VendorCard
              key={item.id}
              item={item}
              isSelected={selectedVendors.includes(item.id)}
              onToggleSelection={toggleVendorSelection}
              onRequestQuote={handleRequestQuote}
            />
          ))
        ) : (
          <div className="bg-card border-border flex min-h-100 flex-col items-center justify-center rounded-lg border p-12 text-center">
            <div className="bg-muted/50 mb-4 rounded-full p-6">
              <MapPin className="text-muted-foreground h-12 w-12" />
            </div>
            <h3 className="text-foreground mb-2 text-xl font-semibold">No vendors found</h3>
            <p className="text-muted-foreground max-w-md text-sm">
              Try adjusting your filters or search criteria to find more results.
            </p>
          </div>
        )}
      </div>
      {!isLoading && data?.page && transformedData.length > 0 && (
        <div className="mt-6">
          <Pagination
            params={data.page}
            initialPage={data.page.number}
            onPageChange={onPageChange}
          />
        </div>
      )}
      <RequestForQuoteFormDrawer
        open={showRequestQuote}
        onOpenChange={toggleRequestQuote}
        selectedVendorIds={selectedVendors}
        setSelectedVendors={setSelectedVendors}
        vendors={transformedData}
        onClearSelection={handleClearSelection}
      />
    </div>
  );
};

export { SearchResults };
