'use client';

import { useState } from 'react';
import { useRouter, usePathname, useSearchParams } from 'next/navigation';

import { ScrollArea } from '@/components/ui/scroll-area';
import { FilterPanel } from './_components/FilterPanel';
import { SearchResults } from './_components/SearchResults';
import { useSearchVendors } from '@/hooks/useVendor';
import { defaultPaginationParams } from '@/types/paginated-params';
import { useAuth } from '@/context/AuthContext';

export type FilterValues = {
  specifications: number[];
  services: number[];
};

const VendorSearchPage = () => {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const { loggedInUser } = useAuth();
  const industryIds = searchParams.get('industryIds')?.split(',').filter(Boolean).map(Number) ?? [];
  const serviceIds = searchParams.get('serviceIds')?.split(',').filter(Boolean).map(Number) ?? [];
  const specificationIds =
    searchParams.get('specificationIds')?.split(',').filter(Boolean).map(Number) ?? [];

  const [currentPage, setCurrentPage] = useState(0);
  const [filters, setFilters] = useState<FilterValues>({
    specifications: specificationIds,
    services: serviceIds,
  });

  const { data, isLoading } = useSearchVendors({
    pageParams: {
      ...defaultPaginationParams,
      page: currentPage,
    },
    industryIds,
    serviceIds: filters.services,
    specificationIds: filters.specifications,
    certifications: [],
    isLoggedIn: !!loggedInUser,
  });

  const onSubmit = (data: FilterValues) => {
    setFilters(data);
    setCurrentPage(0);
    const params = new URLSearchParams();
    if (industryIds.length) params.set('industryIds', industryIds.join(','));
    if (data.services.length) params.set('serviceIds', data.services.join(','));
    if (data.specifications.length) params.set('specificationIds', data.specifications.join(','));
    router.replace(`${pathname}?${params.toString()}`);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  return (
    <main className="animate-in fade-in zoom-in-95 bg-secondary flex min-h-[calc(100vh-4rem)] px-4 duration-500">
      <div className="mx-auto flex w-full max-w-7xl gap-6 py-6">
        <div className="sticky top-22 flex h-[calc(100vh-7rem)] w-2xs shrink-0 flex-col">
          <ScrollArea className="h-full">
            <FilterPanel
              onSubmit={onSubmit}
              initialServiceIds={serviceIds}
              initialSpecificationIds={specificationIds}
            />
          </ScrollArea>
        </div>
        <div className="w-full min-w-3xl">
          <SearchResults data={data} isLoading={isLoading} onPageChange={handlePageChange} />
        </div>
      </div>
    </main>
  );
};

export default VendorSearchPage;
