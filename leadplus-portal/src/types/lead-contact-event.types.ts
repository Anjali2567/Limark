import {
  LeadContactEventCategory,
  LeadContactEventSourceType,
  LeadContactEventType,
} from '@/constants/lead-contact-event.constant';
import { PaginationParams } from './paginated-params';

export type LeadContactEvent = {
  id: string;
  tenantId: string;
  workspaceId: string;
  contactId: string;
  category: LeadContactEventCategory;
  type: LeadContactEventType;
  description: string;
  sourceId: string;
  sourceType: LeadContactEventSourceType;
  userName: string;
  eventBy: string;
  eventAt: Date;
  createdAt: Date;
};

export type LeadContactEventCount = {
  category: LeadContactEventCategory;
  count: number;
};

export type LeadContactEventSearchParams = PaginationParams & {
  category?: LeadContactEventCategory;
};

export type LeadContactEventSearchRequest = {
  tenantId: string;
  contactId: string;
  params: LeadContactEventSearchParams;
};

export type LeadContactEventCountsParams = {
  tenantId: string;
  contactId: string;
};
