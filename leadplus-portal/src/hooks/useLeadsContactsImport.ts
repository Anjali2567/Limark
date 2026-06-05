import { TargetEntity } from '@/constants/import.constants';
import { uploadContactsImport } from '@/lib/api/leadImport.api';
import { ExcelColumnConfig } from '@/types/import.types';
import { useExcelImport } from './useExcelImport';

export const CONTACTS_IMPORT_COLUMNS: ExcelColumnConfig[] = [
  {
    header: 'CONTACT_EMAIL',
    label: 'Email',
    fieldKey: 'email',
    required: true,
    sampleValue: 'jane.doe@acmecorp.com',
    showInPreview: true,
  },
  {
    header: 'COMPANY_DOMAIN',
    label: 'Company Domain',
    fieldKey: 'companyDomain',
    required: true,
    sampleValue: 'acmecorp.com',
    showInPreview: true,
  },
  {
    header: 'CONTACT_FIRST_NAME',
    label: 'First Name',
    fieldKey: 'firstName',
    required: true,
    sampleValue: 'Jane',
    showInPreview: true,
  },
  {
    header: 'CONTACT_LAST_NAME',
    label: 'Last Name',
    fieldKey: 'lastName',
    required: true,
    sampleValue: 'Doe',
    showInPreview: true,
  },
  {
    header: 'CONTACT_TITLE',
    label: 'Title',
    fieldKey: 'title',
    sampleValue: 'VP of Sales',
    showInPreview: true,
  },
  {
    header: 'CONTACT_SENIORITY',
    label: 'Seniority',
    fieldKey: 'seniority',
    sampleValue: 'VP',
    showInPreview: true,
  },
  {
    header: 'CONTACT_DEPARTMENT',
    label: 'Department',
    fieldKey: 'department',
    sampleValue: 'Sales',
    showInPreview: true,
  },
  {
    header: 'CONTACT_PHONE_E164',
    label: 'Phone',
    fieldKey: 'phoneE164',
    sampleValue: '+14155550100',
    showInPreview: true,
  },
  {
    header: 'CONTACT_LINKEDIN_URL',
    label: 'LinkedIn',
    fieldKey: 'linkedinUrl',
    sampleValue: 'https://linkedin.com/in/janedoe',
    showInPreview: true,
  },
  {
    header: 'CONTACT_LOCATION_CITY',
    label: 'City',
    fieldKey: 'locationCity',
    sampleValue: 'San Francisco',
    showInPreview: true,
  },
  {
    header: 'CONTACT_LOCATION_STATE',
    label: 'State',
    fieldKey: 'locationState',
    sampleValue: 'CA',
    showInPreview: true,
  },
  {
    header: 'CONTACT_LOCATION_COUNTRY',
    label: 'Country',
    fieldKey: 'locationCountry',
    sampleValue: 'United States',
    showInPreview: true,
  },
  {
    header: 'CONTACT_LOCATION_ZIP',
    label: 'Zip Code',
    fieldKey: 'locationZip',
    sampleValue: '94105',
    showInPreview: true,
  },
  {
    header: 'CONTACT_PERSONA_MATCH',
    label: 'Persona Match',
    fieldKey: 'personaMatch',
    sampleValue: 'Champion',
    showInPreview: true,
  },
  {
    header: 'CONTACT_NOTES',
    label: 'Notes',
    fieldKey: 'notes',
    sampleValue: 'Key decision maker for enterprise deals',
    showInPreview: true,
  },
];

export const useLeadsContactsImport = () => {
  return useExcelImport({
    columns: CONTACTS_IMPORT_COLUMNS,
    templateFilename: 'leadplus_contacts_import.xlsx',
    uploadFn: uploadContactsImport,
    targetEntity: TargetEntity.LEAD_CONTACT,
  });
};
