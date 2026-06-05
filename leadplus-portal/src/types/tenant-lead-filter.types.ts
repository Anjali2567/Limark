import { LeadType } from '@/constants/leadSearch.constants';

export type TenantLeadFilterResponse = {
  id: string;
  tenantId: string;
  type: LeadType;
  cities?: string[];
  states?: string[];
  countries?: string[];
  companyNames?: string[];
  companyCities?: string[];
  companyStates?: string[];
  companyCountries?: string[];
  regions?: string[];
  keywords?: string[];
  industries?: string[];
  employeeRanges?: string[];
  revenueRanges?: string[];
  technologies?: string[];
  titles?: string[];
  seniority?: string[];
  departments?: string[];
  postalCodes?: string[];
  sicCodes?: string[];
  naicsCodes?: string[];
  active: boolean;
  createdAt: string;
};

export type TenantLeadFilterPayload = {
  type: LeadType;
  cities?: string[];
  states?: string[];
  countries?: string[];
  companyNames?: string[];
  companyCities?: string[];
  companyStates?: string[];
  companyCountries?: string[];
  regions?: string[];
  keywords?: string[];
  industries?: string[];
  employeeRanges?: string[];
  revenueRanges?: string[];
  technologies?: string[];
  titles?: string[];
  seniority?: string[];
  departments?: string[];
  postalCodes?: string[];
  sicCodes?: string[];
  naicsCodes?: string[];
};

export type TenantLeadFilterCreateRequest = {
  tenantId: string;
  payload: TenantLeadFilterPayload;
};

export type TenantLeadFilterUpdateRequest = {
  tenantId: string;
  id: string;
  payload: TenantLeadFilterPayload;
};
