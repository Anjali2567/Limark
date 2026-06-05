import { LeadQueryType } from '@/constants/leadQueries.constant';
import { LeadType } from '@/constants/leadSearch.constants';
import { LeadSearchFilterPayload, LeadSearchPayload } from '@/types/leadSearch.types';

type LocationItem = { value?: string; identifier?: string };
type FlatLocationSource = {
  cities?: string[];
  states?: string[];
  countries?: string[];
  companyCities?: string[];
  companyStates?: string[];
  companyCountries?: string[];
};

/**
 * Splits a `locations` array (with LeadQueryType identifiers) into separate
 * city/state/country URL fields, choosing company-prefixed keys when isCompanySearch.
 */
export const splitLocations = (locations: LocationItem[] | undefined, isCompanySearch: boolean) => {
  const items = (locations ?? []).map((l) => (typeof l === 'string' ? { value: l } : l));
  const cities = items
    .filter((l) => l.identifier === LeadQueryType.COMPANY_CITY || !l.identifier)
    .map((l) => l.value ?? '');
  const states = items
    .filter((l) => l.identifier === LeadQueryType.COMPANY_STATE)
    .map((l) => l.value ?? '');
  const countries = items
    .filter((l) => l.identifier === LeadQueryType.COMPANY_COUNTRY)
    .map((l) => l.value ?? '');

  return isCompanySearch
    ? { companyCities: cities, companyStates: states, companyCountries: countries }
    : { cities, states, countries };
};

/**
 * Picks the correct set of flat location fields from a source object based on
 * whether this is a company search. Use when the source already has separate
 * city/state/country arrays (e.g. saved searches, tenant filter responses).
 */
export const pickLocationFields = (source: FlatLocationSource, isCompanySearch: boolean) =>
  isCompanySearch
    ? {
        companyCities: source.companyCities,
        companyStates: source.companyStates,
        companyCountries: source.companyCountries,
      }
    : {
        cities: source.cities,
        states: source.states,
        countries: source.countries,
      };

export const transformLeadSearchPayloadToSaveRequest = (
  formValues: LeadSearchPayload,
  filterType: LeadType
): Omit<LeadSearchFilterPayload, 'title' | 'resultCount'> => {
  const { locations, companyLocations, ...restFormValues } = formValues;
  const payload = { ...restFormValues } as Omit<LeadSearchFilterPayload, 'title' | 'resultCount'>;

  if (locations && locations.length > 0) {
    const isCompanySearch = filterType === LeadType.LEAD_COMPANY;
    const flat = splitLocations(locations, isCompanySearch);
    Object.entries(flat).forEach(([key, val]) => {
      if (val.length > 0) (payload as Record<string, unknown>)[key] = val;
    });
  }

  if (companyLocations && companyLocations.length > 0) {
    const flat = splitLocations(companyLocations, true);
    Object.entries(flat).forEach(([key, val]) => {
      if (val.length > 0) (payload as Record<string, unknown>)[key] = val;
    });
  }

  return payload;
};
