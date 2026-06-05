import { useCallback, useEffect, useLayoutEffect, useMemo, useRef } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

import { LeadQueryType } from '@/constants/leadQueries.constant';
import { LeadType } from '@/constants/leadSearch.constants';
import { splitLocations } from '@/lib/utils/leadSearchTransform';
import { LeadSearchPayload } from '@/types/leadSearch.types';

import { useFilterContext } from '../_context/FilterContext';
import { useFilterForm } from './useFilterForm';
import { useTenantFilters } from './useTenantFilters';

type SelectedCompanyRow = {
  id: string;
  domain?: string;
  name?: string;
};

const EMPTY_FORM_VALUES: LeadSearchPayload = {
  contactNames: [],
  companyNames: [],
  locations: [],
  companyLocations: [],
  keywords: [],
  regions: [],
  industries: [],
  employeeRanges: [],
  revenueRanges: [],
  technologies: [],
  toolsServices: [],
  titles: [],
  seniority: [],
  departments: [],
  postalCodes: [],
  sicCodes: [],
  naicsCodes: [],
  bdNames: [],
  isrNames: [],
  priorities: [],
  titleCategories: [],
};

const EMPTY_ARRAY_FIELDS: (keyof LeadSearchPayload)[] = [
  'contactNames',
  'keywords',
  'regions',
  'industries',
  'employeeRanges',
  'revenueRanges',
  'technologies',
  'toolsServices',
  'titles',
  'seniority',
  'departments',
  'postalCodes',
  'sicCodes',
  'naicsCodes',
  'bdNames',
  'isrNames',
  'priorities',
  'titleCategories',
];

const getArrayValue = <T>(value?: T[]) => value ?? [];

const buildLocationValues = (
  source: LeadSearchPayload | null | undefined,
  prefix: 'company' | 'contact'
) => {
  const isCompany = prefix === 'company';

  const cities = isCompany ? source?.companyCities : source?.cities;
  const states = isCompany ? source?.companyStates : source?.states;
  const countries = isCompany ? source?.companyCountries : source?.countries;

  return [
    ...(cities?.map((value) => ({ value, identifier: LeadQueryType.COMPANY_CITY })) ?? []),
    ...(states?.map((value) => ({ value, identifier: LeadQueryType.COMPANY_STATE })) ?? []),
    ...(countries?.map((value) => ({ value, identifier: LeadQueryType.COMPANY_COUNTRY })) ?? []),
  ];
};

const getSelectedCompanyIds = (selectedCompanyRows?: SelectedCompanyRow[]) =>
  selectedCompanyRows?.map((row) => String(row.id)).filter(Boolean) ?? [];

const buildTenantFilterPayload = (
  tenantFilter: LeadSearchPayload,
  isCompanySearch: boolean
): LeadSearchPayload => ({
  companyNames: tenantFilter.companyNames,
  keywords: tenantFilter.keywords,
  regions: tenantFilter.regions,
  industries: tenantFilter.industries,
  employeeRanges: tenantFilter.employeeRanges,
  revenueRanges: tenantFilter.revenueRanges,
  technologies: tenantFilter.technologies,
  titles: tenantFilter.titles,
  seniority: tenantFilter.seniority,
  departments: tenantFilter.departments,
  postalCodes: tenantFilter.postalCodes,
  sicCodes: tenantFilter.sicCodes,
  naicsCodes: tenantFilter.naicsCodes,

  ...(isCompanySearch
    ? {
        companyCities: tenantFilter.companyCities,
        companyStates: tenantFilter.companyStates,
        companyCountries: tenantFilter.companyCountries,
      }
    : {
        cities: tenantFilter.cities,
        states: tenantFilter.states,
        countries: tenantFilter.countries,
        companyCities: tenantFilter.companyCities,
        companyStates: tenantFilter.companyStates,
        companyCountries: tenantFilter.companyCountries,
      }),
});

export const useFilterFormValues = (
  filterType: LeadType,
  selectedCompanyRows?: SelectedCompanyRow[]
) => {
  const { schema } = useFilterForm();
  const { initialFilters, updateFilters, clearFilters, resetFiltersToDefault } = useFilterContext();
  const { tenantFilter, getTenantDefaultValues } = useTenantFilters(filterType);

  const isCompanySearch = filterType === LeadType.LEAD_COMPANY;

  const isInitialMountRef = useRef(true);
  const tenantDefaultsAppliedRef = useRef(false);
  const lastSyncedFiltersRef = useRef('');
  const isDirtyRef = useRef(false);

  const selectedCompanyIds = useMemo(
    () => getSelectedCompanyIds(selectedCompanyRows),
    [selectedCompanyRows]
  );

  const toFormValues = useCallback(
    (source: LeadSearchPayload | null | undefined): LeadSearchPayload => {
      const arrayValues = EMPTY_ARRAY_FIELDS.reduce(
        (acc, field) => ({
          ...acc,
          [field]: getArrayValue(source?.[field] as string[] | undefined),
        }),
        {} as LeadSearchPayload
      );

      return {
        ...EMPTY_FORM_VALUES,
        ...arrayValues,
        companyNames: isCompanySearch ? (source?.companyNames ?? []) : selectedCompanyIds,
        locations: buildLocationValues(source, isCompanySearch ? 'company' : 'contact'),
        companyLocations: isCompanySearch ? [] : buildLocationValues(source, 'company'),
      };
    },
    [isCompanySearch, selectedCompanyIds]
  );

  const {
    control,
    reset,
    setValue,
    formState: { isDirty },
  } = useForm<LeadSearchPayload>({
    resolver: zodResolver(schema),
    defaultValues: toFormValues(initialFilters ?? tenantFilter),
  });

  useLayoutEffect(() => {
    isDirtyRef.current = isDirty;
  });

  const formValues = useWatch({ control });

  useEffect(() => {
    lastSyncedFiltersRef.current = '';
  }, [isCompanySearch]);

  useEffect(() => {
    if (isInitialMountRef.current) {
      isInitialMountRef.current = false;
      return;
    }

    if (!isDirtyRef.current) {
      reset(toFormValues(initialFilters));
    }
  }, [initialFilters, reset, toFormValues]);

  useEffect(() => {
    if (tenantDefaultsAppliedRef.current) return;

    if (initialFilters !== null) {
      tenantDefaultsAppliedRef.current = true;
      return;
    }

    if (!tenantFilter) return;

    tenantDefaultsAppliedRef.current = true;
    updateFilters(buildTenantFilterPayload(tenantFilter, isCompanySearch));
  }, [tenantFilter, initialFilters, isCompanySearch, updateFilters]);

  useEffect(() => {
    if (isInitialMountRef.current) return;

    const nextFilters = isCompanySearch
      ? (() => {
          const { locations, ...rest } = formValues;
          return {
            ...rest,
            ...splitLocations(locations, true),
          };
        })()
      : (() => {
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          const { locations, companyLocations, companyNames: _companyNames, ...rest } = formValues;
          return {
            ...rest,
            ...splitLocations(locations, false),
            ...splitLocations(companyLocations, true),
          };
        })();

    const nextSignature = JSON.stringify({
      ...nextFilters,
      page: '0',
    });

    if (nextSignature === lastSyncedFiltersRef.current) return;

    lastSyncedFiltersRef.current = nextSignature;

    updateFilters((prev) => ({
      ...prev,
      ...nextFilters,
      page: '0',
    }));
  }, [formValues, isCompanySearch, updateFilters]);

  const clearFormFilters = useCallback(() => {
    reset(EMPTY_FORM_VALUES);
    clearFilters();
  }, [clearFilters, reset]);

  const resetFormFilters = useCallback(() => {
    reset(EMPTY_FORM_VALUES);
    resetFiltersToDefault();
  }, [reset, resetFiltersToDefault]);

  return {
    control,
    setValue,
    formValues,
    isDirty,
    reset,
    updateFilters,
    clearFormFilters,
    resetFormFilters,
    getTenantDefaultValues,
  };
};
