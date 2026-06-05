import { CampaignStatus } from '@/constants/Campaign';
import { PaginationParams } from './paginated-params';
import { LeadType } from '@/constants/leadSearch.constants';

export type CurrentCampaignData = {
  id: string;
  name: string;
  status: CampaignStatus;
};

export type LeadCompanyData = {
  id: string;
  name: string;
  domain: string;
  accountSummary: string;
  logoUrl: string;
  websiteUrl: string;
  linkedinUrl: string;
  twitterUrl: string;
  facebookUrl: string;
  segment: string;
  industry: string;
  revenueUsd: string;
  employeeCount: string;
  hqCity: string;
  hqState: string;
  hqCountry: string;
  hqPostalCode: string;
  sicCodes: string[];
  naicsCodes: string[];
  keywords: string[];
  technologies: string[];
  tools: string[];
  services: string[];
  phoneNumber: string;
  contactCount: number;
};

export type TenantContactMetadataDto = {
  bdName?: string;
  bdEmail?: string;
  bdPhone?: string;
  isrName?: string;
  isrEmail?: string;
  isrPhone?: string;
  priority?: string;
  titleCategory?: string;
};

export type LeadContactData = {
  id: string;
  leadCompanyId: string;
  firstName: string;
  lastName: string;
  title: string;
  seniority: string;
  department: string;
  email: string;
  emailStatus: string;
  phoneE164: string;
  linkedinUrl: string;
  locationCity: string;
  locationState: string;
  locationCountry: string;
  locationZip: string;
  zohoExisting: boolean;
  hubspotExisting: boolean;
  companyName: string;
  companyDomain: string;
  companyDescription: string;
  companyLogoUrl: string;
  companyWebsiteUrl: string;
  companyLinkedinUrl: string;
  companyTwitterUrl: string;
  companyFacebookUrl: string;
  companySegment: string;
  companyIndustry: string;
  companyRevenueUsd: string;
  companyEmployeeCount: string;
  companyHqCity: string;
  companyHqState: string;
  companyHqCountry: string;
  companyPostalCode: string;
  companySicCodes: string[];
  companyNaicsCodes: string[];
  companyKeywords: string[];
  companyTechnologies: string[];
  companyTools: string[];
  companyServices: string[];
  source: string;
  currentCampaigns?: CurrentCampaignData[];
  tenantMetadata?: TenantContactMetadataDto;
};

export type LeadCompanySearchPayload = {
  companyNames?: string[];
  companyCities?: string[];
  companyStates?: string[];
  companyCountries?: string[];
  regions?: string[];
  postalCodes?: string[];
  keywords?: string[];
  industries?: string[];
  employeeRanges?: string[];
  revenueRanges?: string[];
  technologies?: string[];
  toolsServices?: string[];
  sicCodes?: string[];
  naicsCodes?: string[];
};

export type LeadContactSearchPayload = {
  contactNames?: string[];
  companyNames?: string[];
  cities?: string[];
  states?: string[];
  countries?: string[];
  companyCities?: string[];
  companyStates?: string[];
  companyCountries?: string[];
  regions?: string[];
  employeeRanges?: string[];
  seniority?: string[];
  titles?: string[];
  departments?: string[];
  keywords?: string[];
  industries?: string[];
  technologies?: string[];
  toolsServices?: string[];
  bdNames?: string[];
  isrNames?: string[];
  priorities?: string[];
  titleCategories?: string[];
  campaignEligibleOnly?: boolean;
  aggregateTechSearch?: boolean;
};

export type LeadFilterCriteria = {
  companyIds?: string[] | number[];
  contactNames?: string[];
  companyNames?: string[];
  cities?: string[];
  states?: string[];
  countries?: string[];
  companyCities?: string[];
  companyStates?: string[];
  companyCountries?: string[];
  regions?: string[];
  postalCodes?: string[];
  keywords?: string[];
  industries?: string[];
  employeeRanges?: string[];
  revenueRanges?: string[];
  technologies?: string[];
  toolsServices?: string[];
  sicCodes?: string[];
  naicsCodes?: string[];
  titles?: string[];
  seniority?: string[];
  departments?: string[];
  bdNames?: string[];
  isrNames?: string[];
  priorities?: string[];
  titleCategories?: string[];
  campaignEligibleOnly?: boolean;
  aggregateTechSearch?: boolean;
};

export type LeadChatResponse = {
  messageId?: number;
  conversationId?: number;
  request?: string;
  response?: string;
  criteria?: LeadFilterCriteria;
};

export type LeadChatMessage = {
  messageId?: number;
  conversationId?: number;
  request?: string;
  response?: string;
  createdAt?: string;
};

export type LeadSearchPayload = {
  companyIds?: string[];
  contactNames?: string[];
  companyNames?: string[];
  locations?: { value: string; identifier?: string }[];
  companyLocations?: { value: string; identifier?: string }[];
  companyCities?: string[];
  companyStates?: string[];
  companyCountries?: string[];
  cities?: string[];
  states?: string[];
  countries?: string[];
  keywords?: string[];
  industries?: string[];
  regions?: string[];
  employeeRanges?: string[];
  revenueRanges?: string[];
  technologies?: string[];
  toolsServices?: string[];
  titles?: string[];
  seniority?: string[];
  departments?: string[];
  postalCodes?: string[];
  sicCodes?: string[];
  naicsCodes?: string[];
  bdNames?: string[];
  isrNames?: string[];
  priorities?: string[];
  titleCategories?: string[];
  resultsSearch?: string;
  page?: string;
  sort?: string;
  campaignEligibleOnly?: boolean;
  aggregateTechSearch?: boolean;
};

export type LeadCompanySearchRequest = {
  payload: LeadCompanySearchPayload;
  params: PaginationParams;
};

export type LeadContactSearchRequest = {
  payload: LeadContactSearchPayload;
  params: PaginationParams;
};

export type LeadStatistics = {
  totalCompanies: number;
  totalContacts: number;
  totalEmails: number;
  totalPhoneNumbers: number;
};

export type LeadSearchFilterPayload = {
  title?: string;
  resultCount?: number;
  contactNames?: string[];
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
  toolsServices?: string[];
  titles?: string[];
  seniority?: string[];
  departments?: string[];
  postalCodes?: string[];
  sicCodes?: string[];
  naicsCodes?: string[];
};

export type LeadSearchFilterSaveRequest = {
  tenantId: string;
  type: LeadType;
  payload: LeadSearchFilterPayload;
};

export type LeadSearchFilterParams = {
  tenantId: string;
  type: LeadType;
};

export type LeadSearchFilterSave = {
  id: string;
  userId: string;
  type: LeadType;
  createdAt: Date;
} & LeadSearchFilterPayload;
