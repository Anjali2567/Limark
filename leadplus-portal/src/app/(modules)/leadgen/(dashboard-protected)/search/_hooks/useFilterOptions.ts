import { useMemo } from 'react';

import { InputType } from '@/constants/InputType';
import { LeadQueryType } from '@/constants/leadQueries.constant';
import { LeadSearchType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { useGetLeadQueries } from '@/hooks/useGetLeadQueries';
import { useCompanyLookup, useMetadataFilterValues } from '@/hooks/useSearchLeads';
import { defaultPaginationParams } from '@/types/paginated-params';
import {
  COMPANY_SIZE_OPTIONS,
  REVENUE_RANGE_OPTIONS,
} from '../../campaign-agent/_constants/filterOptions';
import { FilterOptionType } from '../_components/FilterOption';
import { FilterGroupType } from '../_components/FilterGroup';
import { useFilterSearch } from '../_hooks/useFilterSearch';

const params = {
  ...defaultPaginationParams,
  sort: 'value,asc',
};

const FILTER_GROUPS_CONFIG: Record<LeadSearchType, FilterGroupType[]> = {
  [LeadSearchType.COMPANIES]: [
    {
      id: 'locationGroup',
      label: 'Locations',
      filters: [
        {
          id: 'locations',
          label: 'City/State/Country',
          type: InputType.DROPDOWN,
        },
        {
          id: 'postalCodes',
          label: 'Postal Codes',
          type: InputType.AUTOCOMPLETE,
        },
        {
          id: 'regions',
          label: 'Regions',
          type: InputType.DROPDOWN,
        },
      ],
    },
    {
      id: 'codeGroup',
      label: 'NAICS/SIC Codes',
      filters: [
        {
          id: 'sicCodes',
          label: 'SIC Codes',
          type: InputType.AUTOCOMPLETE,
        },
        {
          id: 'naicsCodes',
          label: 'NAICS Codes',
          type: InputType.AUTOCOMPLETE,
        },
      ],
    },
  ],
  [LeadSearchType.PEOPLE]: [
    {
      id: 'locationGroup',
      label: 'Locations',
      filters: [
        {
          id: 'locations',
          label: 'People Locations',
          type: InputType.DROPDOWN,
        },
        {
          id: 'companyLocations',
          label: 'Company Locations',
          type: InputType.DROPDOWN,
        },
      ],
    },
    {
      id: 'metadataGroup',
      label: 'Assignments',
      filters: [
        {
          id: 'bdNames',
          label: 'BD Name',
          type: InputType.DROPDOWN,
          creatable: false,
        },
        {
          id: 'isrNames',
          label: 'ISR Name',
          type: InputType.DROPDOWN,
          creatable: false,
        },
        {
          id: 'priorities',
          label: 'Priority',
          type: InputType.DROPDOWN,
          creatable: false,
        },
        {
          id: 'titleCategories',
          label: 'Title Category',
          type: InputType.DROPDOWN,
          creatable: false,
        },
      ],
    },
  ],
};

const FILTER_ORDER_CONFIG: Record<LeadSearchType, string[]> = {
  [LeadSearchType.COMPANIES]: [
    'companyNames',
    'locationGroup',
    'keywords',
    'industries',
    'employeeRanges',
    'revenueRanges',
    'codeGroup',
  ],
  [LeadSearchType.PEOPLE]: [
    'contactNames',
    'companyNames',
    'locationGroup',
    'industries',
    'seniority',
    'titles',
    'departments',
    'keywords',
    'employeeRanges',
    'metadataGroup',
  ],
};

const getAllSections = (searchType: LeadSearchType): FilterOptionType[] => [
  {
    id: 'contactNames',
    label: 'Name',
    type: InputType.AUTOCOMPLETE,
  },
  {
    id: 'companyNames',
    label: 'Company',
    type: searchType === LeadSearchType.PEOPLE ? InputType.DROPDOWN : InputType.AUTOCOMPLETE,
    creatable: searchType === LeadSearchType.PEOPLE ? false : undefined,
  },
  {
    id: 'locations',
    label: 'Locations',
    type: InputType.DROPDOWN,
  },
  {
    id: 'companyLocations',
    label: 'Company Locations',
    type: InputType.DROPDOWN,
  },
  {
    id: 'postalCodes',
    label: 'Postal Codes',
    type: InputType.AUTOCOMPLETE,
  },
  {
    id: 'regions',
    label: 'Regions',
    type: InputType.DROPDOWN,
  },
  {
    id: 'seniority',
    label: 'Seniority',
    type: InputType.DROPDOWN,
  },
  {
    id: 'titles',
    label: 'Titles',
    type: InputType.DROPDOWN,
  },
  {
    id: 'industries',
    label: 'Industries',
    type: InputType.DROPDOWN,
  },
  {
    id: 'employeeRanges',
    label: 'Employee Ranges',
    type: InputType.DROPDOWN,
    creatable: false,
  },
  {
    id: 'revenueRanges',
    label: 'Revenue',
    type: InputType.DROPDOWN,
    creatable: false,
  },

  {
    id: 'departments',
    label: 'Departments',
    type: InputType.AUTOCOMPLETE,
  },
  {
    id: 'keywords',
    label: 'Keywords',
    type: InputType.AUTOCOMPLETE,
  },
  {
    id: 'sicCodes',
    label: 'SIC Codes',
    type: InputType.AUTOCOMPLETE,
  },
  {
    id: 'naicsCodes',
    label: 'NAICS Codes',
    type: InputType.AUTOCOMPLETE,
  },
];

type SelectedCompanyRow = {
  id: string;
  domain?: string;
  name?: string;
};

export const useFilterOptions = (
  searchType: LeadSearchType,
  selectedCompanyRows?: SelectedCompanyRow[]
) => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';

  const { searchState, activeSearchState, updateSearchField, resetSearch } = useFilterSearch({
    delay: 500,
  });

  const { data: industries, isLoading: isLoadingIndustries } = useGetLeadQueries({
    types: [LeadQueryType.COMPANY_INDUSTRY],
    query: activeSearchState.industry,
    ...params,
    sort: activeSearchState.title ? 'value,asc' : 'createdAt,asc',
  });

  const { data: regions, isLoading: isLoadingRegions } = useGetLeadQueries({
    types: [LeadQueryType.COMPANY_REGION],
    query: activeSearchState.region,
    ...params,
    sort: 'value,asc',
  });

  const { data: titles, isLoading: isLoadingTitles } = useGetLeadQueries({
    types: [LeadQueryType.CONTACT_TITLE],
    query: activeSearchState.title,
    ...params,
    sort: activeSearchState.title ? 'value,asc' : 'createdAt,asc',
  });

  const { data: seniority, isLoading: isLoadingSeniority } = useGetLeadQueries({
    types: [LeadQueryType.CONTACT_SENIORITY],
    query: activeSearchState.seniority,
    ...params,
    sort: 'value,asc',
  });

  const { data: locations, isLoading: isLoadingLocations } = useGetLeadQueries({
    types: [LeadQueryType.COMPANY_CITY, LeadQueryType.COMPANY_STATE, LeadQueryType.COMPANY_COUNTRY],
    query: activeSearchState.location,
    ...params,
    sort: 'value,asc',
  });

  const { data: companyLocations, isLoading: isLoadingCompanyLocations } = useGetLeadQueries({
    types: [LeadQueryType.COMPANY_CITY, LeadQueryType.COMPANY_STATE, LeadQueryType.COMPANY_COUNTRY],
    query: activeSearchState.companyLocation,
    ...params,
    sort: 'value,asc',
  });

  const { data: metadataFilterValues } = useMetadataFilterValues(tenantId);
  const hasMetadataValues = useMemo(() => {
    if (!metadataFilterValues) return false;
    return Object.values(metadataFilterValues).some((values) => values.length > 0);
  }, [metadataFilterValues]);

  const isPeopleSearch = searchType === LeadSearchType.PEOPLE;
  const companyLookupQuery = isPeopleSearch ? activeSearchState.companyName : '';

  const { data: companyLookupResults, isLoading: isLoadingCompanyLookup } =
    useCompanyLookup(tenantId, companyLookupQuery, isPeopleSearch);

  const companyOptions = useMemo(() => {
    const formatLabel = (name?: string, domain?: string) => {
      if (name && domain) return `${name} (${domain})`;
      return name || domain || '';
    };

    const searchOptions =
      companyLookupResults?.map((c) => ({
        label: formatLabel(c.name, c.domain),
        value: String(c.id),
      })) ?? [];

    // Merge selected companies so chips can always resolve their label
    const searchIds = new Set(searchOptions.map((o) => o.value));
    const selectedOptions =
      selectedCompanyRows
        ?.filter((r) => !searchIds.has(String(r.id)))
        .map((r) => ({
          label: formatLabel(r.name, r.domain),
          value: String(r.id),
        })) ?? [];

    return [...searchOptions, ...selectedOptions];
  }, [companyLookupResults, selectedCompanyRows]);

  const groups = useMemo<FilterGroupType[]>(() => {
    const configuredGroups = FILTER_GROUPS_CONFIG[searchType] || [];

    if (searchType !== LeadSearchType.PEOPLE || hasMetadataValues) {
      return configuredGroups;
    }

    return configuredGroups.filter((group) => group.id !== 'metadataGroup');
  }, [hasMetadataValues, searchType]);

  const groupedFilterIds = useMemo(() => {
    return new Set(groups.flatMap((group) => group.filters.map((filter) => filter.id)));
  }, [groups]);

  const sections = useMemo<FilterOptionType[]>(() => {
    const orderConfig = FILTER_ORDER_CONFIG[searchType];
    const groupIds = new Set(groups.map((g) => g.id));

    const availableFilters = getAllSections(searchType)
      .filter((section) => !groupedFilterIds.has(section.id))
      .filter((section) => {
        return orderConfig.includes(section.id) && !groupIds.has(section.id);
      });

    return availableFilters.sort((a, b) => {
      const indexA = orderConfig.indexOf(a.id);
      const indexB = orderConfig.indexOf(b.id);
      return indexA - indexB;
    });
  }, [searchType, groups, groupedFilterIds]);

  const getFilterOptions = (sectionId: string) => {
    switch (sectionId) {
      case 'industries':
        return {
          options: industries?.content.map((i) => ({ label: i.value, value: i.value })) ?? [],
          isLoading: isLoadingIndustries,
          searchValue: searchState.industry,
        };
      case 'regions':
        return {
          options: regions?.content.map((r) => ({ label: r.value, value: r.value })) ?? [],
          isLoading: isLoadingRegions,
          searchValue: searchState.region,
        };
      case 'seniority':
        return {
          options: seniority?.content.map((s) => ({ label: s.value, value: s.value })) ?? [],
          isLoading: isLoadingSeniority,
          searchValue: searchState.seniority,
        };
      case 'titles':
        return {
          options: titles?.content.map((t) => ({ label: t.value, value: t.value })) ?? [],
          isLoading: isLoadingTitles,
          searchValue: searchState.title,
        };
      case 'locations':
        return {
          options:
            locations?.content.map((l) => ({
              label: l.value,
              value: l.value,
              identifier: l.type,
            })) ?? [],
          isLoading: isLoadingLocations,
          searchValue: searchState.location,
        };
      case 'companyLocations':
        return {
          options:
            companyLocations?.content.map((l) => ({
              label: l.value,
              value: l.value,
              identifier: l.type,
            })) ?? [],
          isLoading: isLoadingCompanyLocations,
          searchValue: searchState.companyLocation,
        };
      case 'companyNames':
        return {
          options: companyOptions,
          isLoading: isLoadingCompanyLookup,
          searchValue: searchState.companyName,
        };
      case 'employeeRanges':
        return {
          options: COMPANY_SIZE_OPTIONS,
        };
      case 'revenueRanges':
        return {
          options: REVENUE_RANGE_OPTIONS,
        };
      case 'bdNames':
        return {
          options: metadataFilterValues?.bdName?.map((v) => ({ label: v, value: v })) ?? [],
        };
      case 'isrNames':
        return {
          options: metadataFilterValues?.isrName?.map((v) => ({ label: v, value: v })) ?? [],
        };
      case 'priorities':
        return {
          options: metadataFilterValues?.priority?.map((v) => ({ label: v, value: v })) ?? [],
        };
      case 'titleCategories':
        return {
          options: metadataFilterValues?.titleCategory?.map((v) => ({ label: v, value: v })) ?? [],
        };
      default:
        return { options: [], isLoading: false, searchValue: '' };
    }
  };

  const handleSearchChange = (sectionId: string, value: string) => {
    const fieldMap: Record<string, keyof typeof searchState> = {
      industries: 'industry',
      regions: 'region',
      seniority: 'seniority',
      locations: 'location',
      companyLocations: 'companyLocation',
      titles: 'title',
      companyNames: 'companyName',
    };
    const field = fieldMap[sectionId];
    if (field) updateSearchField(field, value);
  };

  return {
    sections,
    groups,
    groupedFilterIds,
    filterOrder: FILTER_ORDER_CONFIG[searchType],
    getFilterOptions,
    handleSearchChange,
    resetSearch,
    companyLookupResults,
  };
};
