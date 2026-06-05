import { Vendor, VendorPayloadWithLogo } from '@/types/vendor.types';

const emptyAddress = { street: '', city: '', state: '', postalCode: '', country: '' };

export const buildVendorPayload = (
  vendor: Vendor | undefined,
  updates: Partial<VendorPayloadWithLogo>
): VendorPayloadWithLogo => ({
  companyName: vendor?.companyName || '',
  address: vendor?.address || emptyAddress,
  description: vendor?.description,
  companySize: vendor?.companySize,
  phoneNumber: vendor?.phoneNumber,
  faxNumber: vendor?.faxNumber,
  salesEmail: vendor?.salesEmail,
  schedulingLink: vendor?.schedulingLink,
  website: vendor?.website,
  videoLink: vendor?.videoLink,
  annualRevenue: vendor?.annualRevenue,
  tagline: vendor?.tagline,
  businessHours: vendor?.businessHours,
  yearEstablished: vendor?.yearEstablished,
  languagesSpoken: vendor?.languagesSpoken,
  teamDescription: vendor?.teamDescription,
  teamPhoto: vendor?.teamPhoto,
  socialMedia: vendor?.socialMedia,
  industryIds: vendor?.industryIds,
  serviceIds: vendor?.serviceIds,
  specificationIds: vendor?.specificationIds,
  regionsCovered: vendor?.regionsCovered,
  certifications: vendor?.certifications,
  minProjectSize: vendor?.minProjectSize,
  avgHourlyRate: vendor?.avgHourlyRate,
  clientBudgets: vendor?.clientBudgets,
  clientSizes: vendor?.clientSizes,
  clientEmployeeRanges: vendor?.clientEmployeeRanges,
  clientLocations: vendor?.clientLocations,
  openToAnyLocation: vendor?.openToAnyLocation,
  clientIndustries: vendor?.clientIndustries,
  openToAnyIndustry: vendor?.openToAnyIndustry,
  questionnaire: vendor?.questionnaire,
  ...updates,
});
