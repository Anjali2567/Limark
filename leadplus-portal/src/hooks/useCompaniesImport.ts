import { TargetEntity } from '@/constants/import.constants';
import { uploadCompaniesImport } from '@/lib/api/leadImport.api';
import { ExcelColumnConfig } from '@/types/import.types';
import { useExcelImport } from './useExcelImport';

export const COMPANIES_IMPORT_COLUMNS: ExcelColumnConfig[] = [
  {
    header: 'COMPANY_DOMAIN',
    label: 'Domain',
    fieldKey: 'domain',
    required: true,
    sampleValue: 'acmecorp.com',
    showInPreview: true,
  },
  {
    header: 'COMPANY_NAME',
    label: 'Company Name',
    fieldKey: 'name',
    required: true,
    sampleValue: 'Acme Corp',
    showInPreview: true,
  },
  {
    header: 'COMPANY_WEBSITE_URL',
    label: 'Website',
    fieldKey: 'websiteUrl',
    sampleValue: 'https://acmecorp.com',
    showInPreview: true,
  },
  {
    header: 'COMPANY_LINKEDIN_URL',
    label: 'LinkedIn',
    fieldKey: 'linkedinUrl',
    sampleValue: 'https://linkedin.com/company/acme',
    showInPreview: true,
  },
  {
    header: 'COMPANY_TWITTER_URL',
    label: 'Twitter',
    fieldKey: 'twitterUrl',
    sampleValue: '@acmecorp',
    showInPreview: true,
  },
  {
    header: 'COMPANY_PHONE',
    label: 'Phone',
    fieldKey: 'phoneNumber',
    sampleValue: '+12125550100',
    showInPreview: true,
  },
  {
    header: 'COMPANY_INDUSTRY',
    label: 'Industry',
    fieldKey: 'industry',
    sampleValue: 'Technology',
    showInPreview: true,
  },
  {
    header: 'COMPANY_EMPLOYEE_COUNT',
    label: 'Employees',
    fieldKey: 'employeeCount',
    sampleValue: '201-500',
    showInPreview: true,
  },
  {
    header: 'COMPANY_HQ_CITY',
    label: 'HQ City',
    fieldKey: 'hqCity',
    sampleValue: 'San Francisco',
    showInPreview: true,
  },
  {
    header: 'COMPANY_HQ_STATE',
    label: 'HQ State',
    fieldKey: 'hqState',
    sampleValue: 'CA',
    showInPreview: true,
  },
  {
    header: 'COMPANY_HQ_COUNTRY',
    label: 'HQ Country',
    fieldKey: 'hqCountry',
    sampleValue: 'United States',
    showInPreview: true,
  },
  {
    header: 'COMPANY_POSTAL_CODE',
    label: 'Postal Code',
    fieldKey: 'postalCode',
    sampleValue: '94105',
    showInPreview: true,
  },
  {
    header: 'COMPANY_TERRITORY',
    label: 'Territory',
    fieldKey: 'territory',
    sampleValue: 'West Coast',
    showInPreview: true,
  },
  {
    header: 'COMPANY_REGION',
    label: 'Region',
    fieldKey: 'region',
    sampleValue: 'WestCoast',
    showInPreview: true,
  },
  {
    header: 'COMPANY_ICP_TAG',
    label: 'ICP Tag',
    fieldKey: 'icpTag',
    sampleValue: 'Enterprise',
    showInPreview: true,
  },
  {
    header: 'COMPANY_SEGMENT',
    label: 'Segment',
    fieldKey: 'segment',
    sampleValue: 'SaaS',
    showInPreview: true,
  },
  {
    header: 'COMPANY_ACCOUNT_SUMMARY',
    label: 'Account Summary',
    fieldKey: 'accountSummary',
    sampleValue: 'Cloud-based CRM platform for SMBs',
    showInPreview: true,
  },
];

export const useCompaniesImport = () => {
  return useExcelImport({
    columns: COMPANIES_IMPORT_COLUMNS,
    templateFilename: 'leadplus_companies_import.xlsx',
    uploadFn: uploadCompaniesImport,
    targetEntity: TargetEntity.LEAD_COMPANY,
  });
};
