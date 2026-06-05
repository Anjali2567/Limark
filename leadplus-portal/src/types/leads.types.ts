import { DataSource, LeadCompanyStatus } from '@/constants/lead.constants';
import { PaginatedResponse, PaginationParams } from './paginated-params';

export type Leads = {
  id: string;
  name: string;
  domain: string;
  websiteUrl: string;
  linkedinUrl: string;
  industry: string;
  revenueUsd: string;
  employeeCount: string;
  hqCity: string;
  hqState: string;
  hqCountry: string;
  territory: string;
  icpTag: string;
  score: number;
  accountSummary: string;
  salespersonId: string;
  salespersonName: string;
  source: string;
  segment: string;
  leadCompanyStatus: LeadCompanyStatus;
  createdAt: string;
  updatedAt: string;
  contactCount: number;
  targetAccount: boolean;
};

export type LeadCounts = {
  totalCompanies: number;
  totalContacts: number;
};

export type LeadWithCounts = {
  companies: PaginatedResponse<Leads>;
} & LeadCounts;

export type LeadSearchParams = PaginationParams & {
  searchText: string;
  segments?: string[];
};

export type LeadContacts = {
  id: string;
  leadCompanyId: string;
  firstName: string;
  lastName: string;
  firstNameNormalized: string;
  lastNameNormalized: string;
  fullName: string;
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
  personaMatch: string;
  personaScore: number;
  ownerId: string;
  consentStatus: string;
  doNotContact: boolean;
  dataSource: DataSource;
  source: string;
  notes: string;
  createdAt: string;
  updatedAt: string;
};

export type LeadsListResponse = {
  id: string;
  firstName: string;
  lastName: string;
  title: string;
  email: string;
  phoneE164: string;
  companyName: string;
};

export type LeadCompanyJobs = {
  id: string;
  leadCompanyId: string;
  type: string;
  title: string;
  skills: string[];
  jobUrl: string;
  applyUrl: string;
  benefits: string[];
  location: string;
  department: string;
  postedDate: string;
  description: string;
  requirements: string;
  createdAt: Date;
  updatedAt: Date;
};
