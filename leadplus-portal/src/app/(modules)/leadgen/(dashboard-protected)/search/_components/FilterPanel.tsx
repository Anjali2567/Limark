import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useWatch } from 'react-hook-form';
import { toast } from 'sonner';

import { SavedSearchesDrawer } from '@/app/(modules)/leadgen/(dashboard-protected)/search/_components/SavedSearchesDrawer';
import { SaveSearchModal } from '@/components/SaveSearchModal';
import { TooltipButton } from '@/components/TooltipButton';
import { ScrollArea } from '@/components/ui/scroll-area';
import { LeadSearchType, LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { useGetLeadSavedFilters, useSaveLeadSearchFilters } from '@/hooks/useSearchLeads';
import useToggle from '@/hooks/useToggle';
import {
  pickLocationFields,
  transformLeadSearchPayloadToSaveRequest,
} from '@/lib/utils/leadSearchTransform';
import { LeadCompanyData, LeadSearchFilterSave, LeadSearchPayload } from '@/types/leadSearch.types';
import { useFilterFormValues } from '../_hooks/useFilterFormValues';
import { useFilterOptions } from '../_hooks/useFilterOptions';
import { useFilterContext } from '../_context/FilterContext';
import { FilterGroup } from './FilterGroup';
import { FilterOption } from './FilterOption';

import { Bookmark, FunnelX, History, MessageSquare } from 'lucide-react';

type FilterPanelProps = {
  searchType: LeadSearchType;
  filterType: LeadType;
  resultCount?: number;
  selectedCompanyRows?: Array<{ id: string; domain?: string; name?: string }>;
  onClearFilters?: () => void;
};

const FilterPanel = ({
  searchType,
  filterType,
  resultCount,
  selectedCompanyRows,
  onClearFilters,
}: FilterPanelProps) => {
  const { authenticatedUserDetails } = useAuth();
  const { setChatMode, setSelectedCompanyRows, selectedCompanyRows: contextCompanyRows } =
    useFilterContext();

  const {
    control,
    setValue,
    formValues,
    reset,
    updateFilters,
    resetFormFilters,
    getTenantDefaultValues,
  } = useFilterFormValues(filterType, selectedCompanyRows);

  const {
    sections,
    groups,
    filterOrder,
    getFilterOptions,
    handleSearchChange,
    resetSearch,
    companyLookupResults,
  } = useFilterOptions(searchType, contextCompanyRows);

  const isPeopleSearch = searchType === LeadSearchType.PEOPLE;

  // Watch company name IDs from the form dropdown and sync with selectedCompanyRows
  const companyNameValues = useWatch({ control, name: 'companyNames' });
  const companyLookupResultsRef = useRef(companyLookupResults);
  companyLookupResultsRef.current = companyLookupResults;
  const contextCompanyRowsRef = useRef(contextCompanyRows);
  contextCompanyRowsRef.current = contextCompanyRows;

  useEffect(() => {
    if (!isPeopleSearch) return;

    const selectedIds = ((companyNameValues as string[] | undefined) ?? []).map(String);
    const selectedIdSet = new Set(selectedIds);
    const currentRows = contextCompanyRowsRef.current;

    if (selectedIdSet.size === 0) {
      if (currentRows.length > 0) {
        setSelectedCompanyRows([]);
      }
      return;
    }

    // Check if already in sync
    const currentIds = new Set(currentRows.map((r) => String(r.id)));
    if (
      selectedIdSet.size === currentIds.size &&
      [...selectedIdSet].every((id) => currentIds.has(id))
    ) {
      return;
    }

    // Build updated rows: keep existing rows that are still selected, add new ones from search results
    const existingById = new Map(currentRows.map((r) => [String(r.id), r]));
    const searchById = new Map(
      (companyLookupResultsRef.current ?? []).map((r) => [String(r.id), r])
    );

    const nextRows = selectedIds.map(
      (id) =>
        existingById.get(id) ??
        searchById.get(id) ?? { id, name: '', domain: '' }
    );

    setSelectedCompanyRows(nextRows as LeadCompanyData[]);
  }, [companyNameValues, isPeopleSearch, setSelectedCompanyRows]);

  const { mutate: saveSearch, isPending: isSearchSaving } = useSaveLeadSearchFilters();
  const { data: savedSearches = [], isLoading: isLoadingSavedSearches } = useGetLeadSavedFilters({
    tenantId: authenticatedUserDetails?.tenantId || '',
    type: filterType,
  });

  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const { value: isModalOpen, toggle: toggleModal } = useToggle();

  const handleClearFilters = useCallback(() => {
    resetFormFilters();
    resetSearch();
    onClearFilters?.();
  }, [resetFormFilters, resetSearch, onClearFilters]);

  const handleGoBackToChat = useCallback(() => {
    setChatMode(true);
    resetSearch();
  }, [resetSearch, setChatMode]);

  const handleSaveSearch = useCallback(
    (searchName: string) => {
      const cleanedFormValues = {
        ...formValues,
        locations: formValues.locations?.filter((l) => l.value) as
          | { value: string; identifier?: string }[]
          | undefined,
        companyLocations: formValues.companyLocations?.filter((l) => l.value) as
          | { value: string; identifier?: string }[]
          | undefined,
      };
      const payload = {
        ...transformLeadSearchPayloadToSaveRequest(cleanedFormValues, filterType),
        title: searchName,
        resultCount,
      };
      saveSearch(
        { tenantId: authenticatedUserDetails?.tenantId || '', type: filterType, payload },
        {
          onSuccess: () => {
            toast.success('Search saved successfully!');
            toggleModal();
          },
          onError: (error) => {
            toast.error(`Failed to save search: ${error.message}`);
          },
        }
      );
    },
    [
      formValues,
      filterType,
      resultCount,
      saveSearch,
      authenticatedUserDetails?.tenantId,
      toggleModal,
    ]
  );

  const handleLoadSavedSearch = useCallback(
    (search: LeadSearchFilterSave) => {
      const isCompanySearch = filterType === LeadType.LEAD_COMPANY;

      const formPayload: LeadSearchPayload = {
        contactNames: search.contactNames ?? undefined,
        companyNames: search.companyNames ?? undefined,
        locations: [
          ...(search.cities ?? []).map((value) => ({ value, identifier: 'COMPANY_CITY' })),
          ...(search.states ?? []).map((value) => ({ value, identifier: 'COMPANY_STATE' })),
          ...(search.countries ?? []).map((value) => ({ value, identifier: 'COMPANY_COUNTRY' })),
        ],
        companyLocations: [
          ...(search.companyCities ?? []).map((value) => ({ value, identifier: 'COMPANY_CITY' })),
          ...(search.companyStates ?? []).map((value) => ({ value, identifier: 'COMPANY_STATE' })),
          ...(search.companyCountries ?? []).map((value) => ({
            value,
            identifier: 'COMPANY_COUNTRY',
          })),
        ],
        keywords: search.keywords ?? undefined,
        regions: search.regions ?? undefined,
        industries: search.industries ?? undefined,
        employeeRanges: search.employeeRanges?.map(String) ?? undefined,
        revenueRanges: search.revenueRanges ?? undefined,
        technologies: search.technologies ?? undefined,
        toolsServices: search.toolsServices ?? undefined,
        titles: search.titles ?? undefined,
        seniority: search.seniority ?? undefined,
        departments: search.departments ?? undefined,
        postalCodes: search.postalCodes ?? undefined,
        sicCodes: search.sicCodes ?? undefined,
        naicsCodes: search.naicsCodes ?? undefined,
      };

      reset(formPayload);
      updateFilters({
        ...formPayload,
        locations: undefined,
        ...pickLocationFields(search, isCompanySearch),
        companyCities: search.companyCities,
        companyStates: search.companyStates,
        companyCountries: search.companyCountries,
      });
      setIsDrawerOpen(false);
    },
    [filterType, reset, updateFilters]
  );

  const hasFilters = useMemo(() => {
    const { locations, companyLocations, ...rest } = formValues;
    return (
      Object.values(rest).some((v) => Array.isArray(v) && v.length > 0) ||
      (locations ?? []).length > 0 ||
      (companyLocations ?? []).length > 0
    );
  }, [formValues]);

  const actions = useMemo(
    () => [
      {
        icon: <FunnelX className="h-4 w-4" />,
        tooltip: 'Clear Filters',
        onClick: handleClearFilters,
      },
      {
        icon: <MessageSquare className="h-4 w-4" />,
        tooltip: 'Go Back to Chat',
        onClick: handleGoBackToChat,
      },
      {
        icon: <History className="h-4 w-4" />,
        tooltip: 'View Saved Searches',
        onClick: () => setIsDrawerOpen(true),
      },
      {
        icon: <Bookmark className="h-4 w-4" />,
        tooltip: 'Save Search',
        disabled: !hasFilters,
        onClick: toggleModal,
      },
    ],
    [handleClearFilters, handleGoBackToChat, toggleModal, hasFilters]
  );

  return (
    <>
      <div className="flex h-full w-full flex-col">
        <div className="border-border flex h-16 flex-row items-center justify-between border-b p-4">
          <h2 className="text-foreground font-semibold">Filters</h2>
          <div className="flex items-center gap-1">
            {actions.map((action, index) => (
              <TooltipButton
                key={index}
                icon={action.icon}
                disabled={action.disabled}
                tooltip={action.tooltip}
                onClick={action.onClick}
              />
            ))}
          </div>
        </div>
        <ScrollArea className="flex-1 overflow-y-auto">
          {filterOrder.map((itemId) => {
            const group = groups.find((g) => g.id === itemId);
            if (group) {
              return (
                <FilterGroup
                  key={group.id}
                  group={group}
                  control={control}
                  getFilterOptions={getFilterOptions}
                  handleSearchChange={handleSearchChange}
                  getDefaultValues={getTenantDefaultValues}
                  onClearGroup={() =>
                    group.filters.forEach((f) => {
                      setValue(f.id, []);
                      handleSearchChange(f.id, '');
                    })
                  }
                />
              );
            }
            const section = sections.find((s) => s.id === itemId);
            if (!section) return null;
            const { isLoading, searchValue, options } = getFilterOptions(section.id);
            return (
              <FilterOption
                key={section.id}
                section={{
                  ...section,
                  options,
                  defaultValues: getTenantDefaultValues(section.id),
                }}
                control={control}
                searchValue={searchValue}
                onSearchChange={(value) => handleSearchChange(section.id, value)}
                isLoading={isLoading}
              />
            );
          })}
        </ScrollArea>
      </div>

      <SaveSearchModal
        isOpen={isModalOpen}
        onClose={toggleModal}
        onSave={handleSaveSearch}
        isPending={isSearchSaving}
        resultCount={resultCount}
      />
      <SavedSearchesDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        savedSearches={savedSearches}
        onLoadSearch={handleLoadSavedSearch}
        isLoading={isLoadingSavedSearches}
      />
    </>
  );
};

export { FilterPanel };
