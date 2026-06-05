import { SourceType } from '@/constants/attachment.constant';

export type VendorShowcaseAttachment = {
  id: string;
  sourceId: string;
  sourceType: SourceType;
  fileName: string;
  fileType: string;
  fileUrl: string;
  sizeBytes: number;
  createdBy: string;
  createdAt: Date;
};

export type VendorShowcase = {
  id: string;
  vendorId: string;
  tenantId: string;
  projectName: string;
  clientName: string;
  description: string;
  serviceIds: string[];
  duration: string;
  resultsAndOutcomes: string;
  createdAt: Date;
  createdBy: string;
  updatedBy: string;
  updatedAt: Date;
  attachments: VendorShowcaseAttachment[];
};

export type VendorShowcasePayload = {
  projectName: string;
  clientName: string;
  description: string;
  serviceIds: string[];
  duration: string;
  resultsAndOutcomes: string;
};

export type VendorShowcaseWithAttachment = {
  id?: string;
  vendorId: string;
  tenantId: string;
  file: File | string;
  payload: VendorShowcasePayload;
};

export type VendorShowcaseBasicDetails = {
  vendorId: string;
  tenantId: string;
};

export type VendorShowcaseRequest = {
  vendorId: string;
  tenantId: string;
  payload: VendorShowcasePayload;
};

export type VendorShowcaseUpdateRequest = {
  id: string;
  vendorId: string;
  tenantId: string;
  payload: VendorShowcasePayload;
};

export type VendorShowcaseDetails = {
  id: string;
  vendorId: string;
  tenantId: string;
};

export type VendorShowcaseAttachmentRequest = {
  id: string;
  vendorId: string;
  tenantId: string;
  file: File;
};

export type VendorShowcaseAttachmentDetails = {
  id: string;
  vendorId: string;
  tenantId: string;
  attachmentId: string;
};
