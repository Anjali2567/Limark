import { QuotationSourceType } from '@/constants/attachment.constant';
import { PaginationParams } from './paginated-params';
import { QuotationStatus } from '@/constants/quotation.constant';

export type QuotationItem = {
  serviceName: string;
  description: string;
  duration: string;
  price: number;
};

export type Quotation = {
  id: string;
  sourceId: string;
  tenantId: string;
  workspaceId: string;
  title: string;
  description: string;
  budget: string;
  deadline: Date;
  items: QuotationItem[];
  paymentTerms: string;
  deliverables: string[];
  status: QuotationStatus;
  sourceType: QuotationSourceType;
  vendorId: string;
  createdBy: string;
  createdAt: Date;
  updatedBy: string;
  updatedAt: Date;
};

export type QuotationIdentity = {
  tenantId: string;
  workspaceId: string;
  quotationId: string;
};

export type CustomerQuotationResponse = Quotation & {
  customerName: string;
  customerCompanyName: string;
  customerEmail: string;
  customerPhoneNumber: string;
};

export type VendorQuotationsRequest = {
  tenantId: string;
  workspaceId: string;
  params: PaginationParams;
};

export type RFQQuotationRequest = {
  params: QuotationIdentity;
  payload: {
    sourceId: string;
    items: QuotationItem[];
    paymentTerms: string;
    deliverables: string[];
  };
};
