import { SourceAttachment } from './attachment.types';

export type RequestForQuote = {
  id: string;
  title: string;
  serviceIds: string[];
  vendorIds: string[];
  description: string;
  deadline: Date;
  budget: string;
  attachments: SourceAttachment[];
  createdAt: Date;
  updatedAt: Date;
};

export type RequestForQuoteWithQuoteCount = RequestForQuote & {
  services: {
    id: string;
    industryId: string;
    name: string;
  }[];
  numberOfQuotationsForRfq: number;
  numberOfVendorsForRfq: number;
};

export type RequestForQuotePayload = {
  title: string;
  serviceIds: string[];
  vendorIds: string[];
  description: string;
  deadline: Date;
  budget: string;
};

export type RequestForQuoteRequest = {
  id?: string;
  files?: File[];
  payload: RequestForQuotePayload;
};

export type RequestForQuoteFile = {
  requestForQuoteId: string;
  attachmentId: string;
};
