import { LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { useGetTenantLeadFilters } from '@/hooks/useTenantLeadFilter';
import { useCallback } from 'react';

export const useTenantFilters = (filterType: LeadType) => {
  const { authenticatedUserDetails } = useAuth();
  const { data: tenantFilters } = useGetTenantLeadFilters(authenticatedUserDetails?.tenantId ?? '');
  const tenantFilter = tenantFilters?.find((f) => f.type === filterType);

  const getTenantDefaultValues = useCallback(
    (sectionId: string): string[] => {
      if (!tenantFilter) return [];
      const isCompanySearch = filterType === LeadType.LEAD_COMPANY;
      switch (sectionId) {
        case 'companyNames':
          return tenantFilter.companyNames ?? [];
        case 'locations':
          return [
            ...(isCompanySearch ? (tenantFilter.companyCities ?? []) : (tenantFilter.cities ?? [])),
            ...(isCompanySearch ? (tenantFilter.companyStates ?? []) : (tenantFilter.states ?? [])),
            ...(isCompanySearch
              ? (tenantFilter.companyCountries ?? [])
              : (tenantFilter.countries ?? [])),
          ];
        case 'industries':
          return tenantFilter.industries ?? [];
        case 'keywords':
          return tenantFilter.keywords ?? [];
        case 'employeeRanges':
          return tenantFilter.employeeRanges ?? [];
        case 'revenueRanges':
          return tenantFilter.revenueRanges ?? [];
        case 'technologies':
          return tenantFilter.technologies ?? [];
        case 'titles':
          return tenantFilter.titles ?? [];
        case 'seniority':
          return tenantFilter.seniority ?? [];
        case 'departments':
          return tenantFilter.departments ?? [];
        case 'postalCodes':
          return tenantFilter.postalCodes ?? [];
        case 'sicCodes':
          return tenantFilter.sicCodes ?? [];
        case 'naicsCodes':
          return tenantFilter.naicsCodes ?? [];
        case 'regions':
          return tenantFilter.regions ?? [];
        default:
          return [];
      }
    },
    [tenantFilter, filterType]
  );

  return { tenantFilter, getTenantDefaultValues };
};
