import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { Address } from './common.types';
import { PaginationParams } from './paginated-params';

export type Identity = {
  id: number;
  industryId: number;
  name: string;
  icon: string;
};

export type AnswerRequest = {
  questionId: number;
  answer: string;
};

export type SocialMedia = {
  linkedin?: string;
  twitter?: string;
  facebook?: string;
  instagram?: string;
  youtube?: string;
};

export type Vendor = {
  id: string;
  userId: string;
  tenantId: string;
  companyName: string;
  description: string;
  logo: string;
  companySize: string;
  phoneNumber: string;
  faxNumber: string;
  salesEmail?: string;
  schedulingLink?: string;
  website: string;
  videoLink?: string;
  annualRevenue: string;
  tagline: string;
  businessHours: string;
  yearEstablished: string;
  languagesSpoken?: string[];
  teamDescription?: string;
  teamPhoto?: string;
  address: Address;
  socialMedia?: SocialMedia;
  industryIds: number[];
  serviceIds: number[];
  specificationIds: number[];
  regionsCovered: string[];
  certifications: string[];
  minProjectSize?: string;
  avgHourlyRate?: string;
  clientBudgets?: string[];
  clientSizes?: string[];
  clientEmployeeRanges?: string[];
  clientLocations?: string[];
  openToAnyLocation?: boolean;
  clientIndustries?: string[];
  openToAnyIndustry?: boolean;
  vendorVerificationStatus: VendorVerificationStatus;
  questionnaire: AnswerRequest[];
  reviewComment: string;
  createdAt: Date;
  updatedAt: Date;
};

export type VendorDetailResponse = Omit<Vendor, 'serviceIds' | 'specificationIds'> & {
  serviceList: Identity[];
  specificationList: Identity[];
};

export type VendorCardDetails = VendorDetailResponse & {
  rating: number;
  reviews: number;
  responseTime: string;
  projects: number;
};

export type VendorPayload = {
  companyName: string;
  industryIds?: number[];
  description?: string;
  companySize?: string;
  phoneNumber?: string;
  faxNumber?: string;
  salesEmail?: string;
  schedulingLink?: string;
  website?: string;
  videoLink?: string;
  annualRevenue?: string;
  tagline?: string;
  businessHours?: string;
  yearEstablished?: string;
  languagesSpoken?: string[];
  teamDescription?: string;
  teamPhoto?: string;
  address: Address;
  socialMedia?: SocialMedia;
  serviceIds?: number[];
  specificationIds?: number[];
  regionsCovered?: string[];
  certifications?: string[];
  minProjectSize?: string;
  avgHourlyRate?: string;
  clientBudgets?: string[];
  clientSizes?: string[];
  clientEmployeeRanges?: string[];
  clientLocations?: string[];
  openToAnyLocation?: boolean;
  clientIndustries?: string[];
  openToAnyIndustry?: boolean;
  questionnaire?: AnswerRequest[];
};

export type VendorPayloadWithLogo = VendorPayload & {
  logo?: File | string;
};

export type VendorFormValues = {
  logo?: File | string;
  companyName?: string;
  industryIds?: number[];
  tagline?: string;
  description?: string;
  yearEstablished?: string;
  companySize?: string;
  annualRevenue?: string;
  website?: string;
  name?: string;
  email?: string;
  phoneNumber?: string;
  faxNumber?: string;
  businessHours?: string;
  street?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  serviceIds?: number[];
  specificationIds?: number[];
  regionsCovered?: string[];
  certifications?: string[];
  questionnaire?: AnswerRequest[];
};

export type VendorListingParams = {
  vendorVerificationStatusList?: VendorVerificationStatus[];
} & PaginationParams;

export type VendorSearchParams = {
  industryIds?: number[];
  serviceIds?: number[];
  specificationIds?: number[];
  certifications?: string[];
  isLoggedIn?: boolean;
  pageParams?: PaginationParams;
};

export type VendorSearchParseResult = {
  serviceIds: number[];
  specificationIds: number[];
};
