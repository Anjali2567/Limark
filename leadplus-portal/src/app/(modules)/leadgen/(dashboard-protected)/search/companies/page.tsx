'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo } from 'react';

import { Page } from '@/components/Page';
import { appRoutes } from '@/config/routes';
import { LeadSearchType, LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { useFetchCompanyIdsWithDomains, useLeadCompaniesSearch } from '@/hooks/useSearchLeads';
import useToggle from '@/hooks/useToggle';
import { LeadCompanyData } from '@/types/leadSearch.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { useCompanySearchColumns } from '../../_hooks/useCompanySearchColumns';
import { FilterPanel } from '../_components/FilterPanel';
import { SearchResults } from '../_components/SearchResults';
import { useFilterContext } from '../_context/FilterContext';
import { FilterPanelLayout } from '../_layouts/FilterPanelLayout';

const CompaniesSearchPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const { value: isFilterPanelOpen, toggle } = useToggle(true);
  const { debouncedFilters, updateFilters, selectedCompanyRows, setSelectedCompanyRows } =
    useFilterContext();
  const { authenticatedUserDetails } = useAuth();
  const { tenantId, workspaceId } = authenticatedUserDetails || { tenantId: '', workspaceId: '' };
  const { mutateAsync: fetchCompanyIdsWithDomains } = useFetchCompanyIdsWithDomains();

  const disableAnimation = useMemo(() => searchParams.get('tabSwitch') === 'true', [searchParams]);

  const request = useMemo(() => {
    if (!debouncedFilters) return null;
    const {
      companyCities,
      companyStates,
      companyCountries,
      regions,
      postalCodes,
      keywords,
      industries,
      employeeRanges,
      revenueRanges,
      technologies,
      toolsServices,
      sicCodes,
      naicsCodes,
      companyNames,
    } = debouncedFilters;

    return {
      payload: {
        companyCities,
        companyStates,
        companyCountries,
        regions,
        postalCodes,
        keywords,
        industries,
        employeeRanges,
        revenueRanges,
        technologies,
        toolsServices,
        sicCodes,
        naicsCodes,
        companyNames,
        aggregateTechSearch: true,
      },
      params: {
        ...defaultPaginationParams,
        page: debouncedFilters.page ?? 0,
        query: debouncedFilters.resultsSearch,
        sort: debouncedFilters.sort ?? defaultPaginationParams.sort,
      },
    };
  }, [debouncedFilters]);

  const { data, isLoading } = useLeadCompaniesSearch(
    authenticatedUserDetails?.tenantId ?? '',
    request
  );

  const onFetchAllIds = useCallback(async () => {
    if (!request || !tenantId) return [];
    const results = await fetchCompanyIdsWithDomains({ tenantId, request });
    setSelectedCompanyRows(
      results.map((entry) => ({ id: entry.id, domain: entry.domain }) as LeadCompanyData)
    );
    return results.map((entry) => entry.id);
  }, [fetchCompanyIdsWithDomains, request, tenantId, setSelectedCompanyRows]);

  const onContactSearch = useCallback(
    (rows: LeadCompanyData[]) => {
      const companyNames = rows
        .map((row) => row.domain ?? row.name)
        .filter(Boolean);

      if (companyNames.length > 0) {
        updateFilters((prev) => ({ ...(prev || {}), page: '0' }));
        router.push(`${appRoutes.leadgen.search.contactSearch}?tabSwitch=true`);
      }
    },
    [router, updateFilters]
  );

  const onClickCompanyName = useCallback(
    (id: string) => {
      router.push(appRoutes.leadgen.search.companyInfo(id));
    },
    [router]
  );

  const onClickContacts = useCallback(
    (row: LeadCompanyData) => {
      setSelectedCompanyRows([row]);
      updateFilters((prev) => ({ ...(prev || {}), page: '0', companyIds: [row.id] }));
      router.push(`${appRoutes.leadgen.search.contactSearch}?tabSwitch=true`);
    },
    [router, setSelectedCompanyRows, updateFilters]
  );

  const columns = useCompanySearchColumns(onClickCompanyName, onClickContacts);

  return (
    <Page disableAnimation={disableAnimation}>
      <FilterPanelLayout
        isFilterPanelOpen={isFilterPanelOpen}
        disableAnimation={disableAnimation}
        searchType={LeadSearchType.COMPANIES}
        filterPanel={
          <FilterPanel
            searchType={LeadSearchType.COMPANIES}
            filterType={LeadType.LEAD_COMPANY}
            resultCount={data?.page.totalElements}
          />
        }
        content={
          <SearchResults
            onCollapse={toggle}
            columns={columns}
            data={data}
            isLoading={isLoading}
            proceedButtonText="Find Contacts"
            onProceed={onContactSearch}
            searchPlaceholder="Search companies..."
            tenantId={tenantId}
            workspaceId={workspaceId}
            leadType={LeadType.LEAD_COMPANY}
            selectedRows={selectedCompanyRows}
            setSelectedRows={setSelectedCompanyRows}
            onFetchAllIds={onFetchAllIds}
          />
        }
      />
    </Page>
  );
};

export default CompaniesSearchPage;
