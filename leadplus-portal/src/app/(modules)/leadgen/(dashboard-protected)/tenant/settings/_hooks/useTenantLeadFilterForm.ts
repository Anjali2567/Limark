import { useMemo, useEffect, useRef } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import {
  COMPANY_SIZE_OPTIONS,
  REVENUE_RANGE_OPTIONS,
} from '@/app/(modules)/leadgen/(dashboard-protected)/campaign-agent/_constants/filterOptions';
import { EmployeeRange } from '@/constants/Campaign';
import { InputType } from '@/constants/InputType';
import { LeadQueryType } from '@/constants/leadQueries.constant';
import { LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { useGetLeadQueries } from '@/hooks/useGetLeadQueries';
import { useFilterSearch } from '@/app/(modules)/leadgen/(dashboard-protected)/search/_hooks/useFilterSearch';
import {
  useCreateTenantLeadFilter,
  useGetTenantLeadFilters,
  useUpdateTenantLeadFilter,
} from '@/hooks/useTenantLeadFilter';
import { defaultPaginationParams } from '@/types/paginated-params';
import { TenantLeadFilterResponse } from '@/types/tenant-lead-filter.types';

export type LocationItem = { value: string; identifier?: string };

export type FilterSectionConfig = {
  id: keyof FilterFormValues;
  label: string;
  type: InputType;
  placeholder?: string;
  options?: { value: string; label: string; identifier?: string }[];
  isLoading?: boolean;
};

export type FilterFormValues = {
  locations: LocationItem[];
  companyNames: string[];
  regions: string[];
  keywords: string[];
  industries: string[];
  employeeRanges: EmployeeRange[];
  revenueRanges: string[];
  technologies: string[];
  titles: string[];
  seniority: string[];
  departments: string[];
  postalCodes: string[];
  sicCodes: string[];
  naicsCodes: string[];
};

const toLocationItems = (
  cities?: string[] | null,
  states?: string[] | null,
  countries?: string[] | null
): LocationItem[] => [
  ...(cities ?? []).map((v) => ({ value: v, identifier: LeadQueryType.COMPANY_CITY })),
  ...(states ?? []).map((v) => ({ value: v, identifier: LeadQueryType.COMPANY_STATE })),
  ...(countries ?? []).map((v) => ({ value: v, identifier: LeadQueryType.COMPANY_COUNTRY })),
];

const splitLocations = (items: LocationItem[]) => ({
  cities: items.filter((l) => l.identifier === LeadQueryType.COMPANY_CITY).map((l) => l.value),
  states: items.filter((l) => l.identifier === LeadQueryType.COMPANY_STATE).map((l) => l.value),
  countries: items
    .filter((l) => l.identifier === LeadQueryType.COMPANY_COUNTRY)
    .map((l) => l.value),
});

const toFormValues = (
  filter: Partial<TenantLeadFilterResponse>,
  tab: LeadType
): FilterFormValues => ({
  locations:
    tab === LeadType.LEAD_CONTACT
      ? toLocationItems(filter.cities, filter.states, filter.countries)
      : toLocationItems(filter.companyCities, filter.companyStates, filter.companyCountries),
  companyNames: filter.companyNames ?? [],
  regions: filter.regions ?? [],
  keywords: filter.keywords ?? [],
  industries: filter.industries ?? [],
  employeeRanges: (filter.employeeRanges as EmployeeRange[]) ?? [],
  revenueRanges: filter.revenueRanges ?? [],
  technologies: filter.technologies ?? [],
  titles: filter.titles ?? [],
  seniority: filter.seniority ?? [],
  departments: filter.departments ?? [],
  postalCodes: filter.postalCodes ?? [],
  sicCodes: filter.sicCodes ?? [],
  naicsCodes: filter.naicsCodes ?? [],
});

const queryParams = { ...defaultPaginationParams, sort: 'value,asc' };

export const useTenantLeadFilterForm = (activeTab: LeadType) => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';

  const { activeSearchState, updateSearchField } = useFilterSearch({ delay: 500 });

  const { data: industriesData, isLoading: isLoadingIndustries } = useGetLeadQueries({
    types: [LeadQueryType.COMPANY_INDUSTRY],
    query: activeSearchState.industry,
    ...queryParams,
    sort: activeSearchState.industry ? 'value,asc' : 'createdAt,asc',
  });
  const { data: titlesData, isLoading: isLoadingTitles } = useGetLeadQueries({
    types: [LeadQueryType.CONTACT_TITLE],
    query: activeSearchState.title,
    ...queryParams,
    sort: activeSearchState.title ? 'value,asc' : 'createdAt,asc',
  });
  const { data: seniorityData, isLoading: isLoadingSeniority } = useGetLeadQueries({
    types: [LeadQueryType.CONTACT_SENIORITY],
    query: activeSearchState.seniority,
    ...queryParams,
  });
  const { data: locationsData, isLoading: isLoadingLocations } = useGetLeadQueries({
    types: [LeadQueryType.COMPANY_CITY, LeadQueryType.COMPANY_STATE, LeadQueryType.COMPANY_COUNTRY],
    query: activeSearchState.location,
    ...queryParams,
  });

  const handleSearchChange = (sectionId: string, value: string) => {
    const fieldMap: Record<string, Parameters<typeof updateSearchField>[0]> = {
      locations: 'location',
      industries: 'industry',
      seniority: 'seniority',
      titles: 'title',
    };
    const field = fieldMap[sectionId];
    if (field) updateSearchField(field, value);
  };

  const industryOptions = useMemo(
    () => industriesData?.content.map((i) => ({ label: i.value, value: i.value })) ?? [],
    [industriesData]
  );
  const titleOptions = useMemo(
    () => titlesData?.content.map((t) => ({ label: t.value, value: t.value })) ?? [],
    [titlesData]
  );
  const seniorityOptions = useMemo(
    () => seniorityData?.content.map((s) => ({ label: s.value, value: s.value })) ?? [],
    [seniorityData]
  );
  const locationOptions = useMemo(
    () =>
      locationsData?.content.map((l) => ({
        label: l.value,
        value: l.value,
        identifier: l.type,
      })) ?? [],
    [locationsData]
  );

  const peopleSearchFilters: FilterSectionConfig[] = useMemo(
    () => [
      {
        id: 'companyNames',
        label: 'Company',
        type: InputType.AUTOCOMPLETE,
        placeholder: 'e.g. Apple, Microsoft',
      },
      {
        id: 'locations',
        label: 'City / State / Country',
        type: InputType.DROPDOWN,
        placeholder: 'Search city, state or country...',
        options: locationOptions,
        isLoading: isLoadingLocations,
      },
      {
        id: 'industries',
        label: 'Industries',
        type: InputType.DROPDOWN,
        placeholder: 'Search industries...',
        options: industryOptions,
        isLoading: isLoadingIndustries,
      },
      {
        id: 'seniority',
        label: 'Seniority',
        type: InputType.DROPDOWN,
        placeholder: 'Search seniority levels...',
        options: seniorityOptions,
        isLoading: isLoadingSeniority,
      },
      {
        id: 'titles',
        label: 'Titles',
        type: InputType.DROPDOWN,
        placeholder: 'Search job titles...',
        options: titleOptions,
        isLoading: isLoadingTitles,
      },
      {
        id: 'departments',
        label: 'Departments',
        type: InputType.AUTOCOMPLETE,
        placeholder: 'e.g. Engineering, Sales',
      },
      {
        id: 'keywords',
        label: 'Keywords',
        type: InputType.AUTOCOMPLETE,
        placeholder: 'e.g. SaaS, fintech',
      },
      {
        id: 'employeeRanges',
        label: 'Employee Ranges',
        type: InputType.DROPDOWN,
        placeholder: 'Select employee ranges...',
        options: COMPANY_SIZE_OPTIONS,
      },
    ],
    [
      locationOptions,
      industryOptions,
      seniorityOptions,
      titleOptions,
      isLoadingLocations,
      isLoadingIndustries,
      isLoadingSeniority,
      isLoadingTitles,
    ]
  );

  const companySearchFilters: FilterSectionConfig[] = useMemo(
    () => [
      {
        id: 'companyNames',
        label: 'Company',
        type: InputType.AUTOCOMPLETE,
        placeholder: 'e.g. Apple, Microsoft',
      },
      {
        id: 'locations',
        label: 'City / State / Country',
        type: InputType.DROPDOWN,
        placeholder: 'Search city, state or country...',
        options: locationOptions,
        isLoading: isLoadingLocations,
      },
      {
        id: 'keywords',
        label: 'Keywords',
        type: InputType.AUTOCOMPLETE,
        placeholder: 'e.g. SaaS, fintech',
      },
      {
        id: 'industries',
        label: 'Industries',
        type: InputType.DROPDOWN,
        placeholder: 'Search industries...',
        options: industryOptions,
        isLoading: isLoadingIndustries,
      },
      {
        id: 'employeeRanges',
        label: 'Employee Ranges',
        type: InputType.DROPDOWN,
        placeholder: 'Select employee ranges...',
        options: COMPANY_SIZE_OPTIONS,
      },
      {
        id: 'revenueRanges',
        label: 'Revenue',
        type: InputType.DROPDOWN,
        placeholder: 'Select revenue ranges...',
        options: REVENUE_RANGE_OPTIONS,
      },
      {
        id: 'postalCodes',
        label: 'Postal Codes',
        type: InputType.AUTOCOMPLETE,
        placeholder: 'e.g. 90210, 10001',
      },
      {
        id: 'sicCodes',
        label: 'SIC Codes',
        type: InputType.AUTOCOMPLETE,
        placeholder: 'e.g. 7372',
      },
      {
        id: 'naicsCodes',
        label: 'NAICS Codes',
        type: InputType.AUTOCOMPLETE,
        placeholder: 'e.g. 541511',
      },
    ],
    [locationOptions, industryOptions, isLoadingLocations, isLoadingIndustries]
  );

  const { data: filters } = useGetTenantLeadFilters(tenantId);
  const { mutateAsync: createFilter, isPending: isCreating } = useCreateTenantLeadFilter();
  const { mutateAsync: updateFilter, isPending: isUpdating } = useUpdateTenantLeadFilter();

  const activeFilters = useMemo(() => {
    const people = filters?.find((f) => f.type === LeadType.LEAD_CONTACT) ?? {
      id: '',
      type: LeadType.LEAD_CONTACT,
    };
    const company = filters?.find((f) => f.type === LeadType.LEAD_COMPANY) ?? {
      id: '',
      type: LeadType.LEAD_COMPANY,
    };
    return activeTab === LeadType.LEAD_CONTACT ? people : company;
  }, [filters, activeTab]);

  const activeSections =
    activeTab === LeadType.LEAD_CONTACT ? peopleSearchFilters : companySearchFilters;

  const {
    control,
    handleSubmit,
    reset,
    formState: { isDirty },
  } = useForm<FilterFormValues>({
    defaultValues: toFormValues(activeFilters, activeTab),
  });

  const prevTabRef = useRef(activeTab);
  useEffect(() => {
    const tabChanged = prevTabRef.current !== activeTab;
    prevTabRef.current = activeTab;
    if (tabChanged || !isDirty) {
      reset(toFormValues(activeFilters, activeTab));
    }
  }, [activeFilters, activeTab, isDirty, reset]);

  const onSubmit = async (values: FilterFormValues) => {
    const { id } = activeFilters;
    const { locations, ...rest } = values;
    const locationFields =
      activeTab === LeadType.LEAD_CONTACT
        ? splitLocations(locations)
        : {
            companyCities: locations
              .filter((l) => l.identifier === LeadQueryType.COMPANY_CITY)
              .map((l) => l.value),
            companyStates: locations
              .filter((l) => l.identifier === LeadQueryType.COMPANY_STATE)
              .map((l) => l.value),
            companyCountries: locations
              .filter((l) => l.identifier === LeadQueryType.COMPANY_COUNTRY)
              .map((l) => l.value),
          };
    const payload = { type: activeTab, ...rest, ...locationFields };
    if (id) {
      await updateFilter({ id, tenantId, payload });
    } else {
      await createFilter({ tenantId, payload });
    }
    reset(values);
    toast.success('Default filters saved.');
  };

  return {
    activeTab,
    activeSections,
    control,
    handleSubmit,
    handleSearchChange,
    onSubmit,
    isSaving: isCreating || isUpdating,
    isDirty,
  };
};
