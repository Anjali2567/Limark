import { Address } from './common.types';

export type CustomerQuotation = {
  id: string;
  sourceId: string;
  tenantId: string;
  workspaceId: string;
  title: string;
  description: string;
  budget: string;
  deadline: Date;
  items: {
    serviceName: string;
    description: string;
    duration: string;
    price: number;
  }[];
  paymentTerms: string;
  deliverables: string[];
  attachments?: string[];
  status: string;
  sourceType: string;
  vendorId: string;
  vendorCompanyName: string | null;
  vendorLogo: string | null;
  vendorAddress: Address | null;
  vendorEmail: string | null;
  vendorPhoneNumber: string | null;
  vendorWebsite: string | null;
  vendorRating: number | null;
  vendorReviews: number | null;
  vendorCompanySize: number | null;
  createdBy: string;
  createdAt: Date;
  updatedBy: string;
  updatedAt: Date;
};
