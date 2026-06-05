import {
  LeadContactEvent,
  LeadContactEventCount,
  LeadContactEventCountsParams,
  LeadContactEventSearchRequest,
} from '@/types/lead-contact-event.types';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { PaginatedResponse } from '@/types/paginated-params';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';

export const getEventsForLeadContact = async (
  req: LeadContactEventSearchRequest
): Promise<PaginatedResponse<LeadContactEvent>> => {
  const { tenantId, contactId, params } = req;
  try {
    const response = await apiClient.get<PaginatedResponse<LeadContactEvent>>(
      apiEndpoints.leads.contacts.events.collection(tenantId, contactId),
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getEventCountsForLeadContact = async (
  params: LeadContactEventCountsParams
): Promise<LeadContactEventCount[]> => {
  const { tenantId, contactId } = params;
  try {
    const response = await apiClient.get<LeadContactEventCount[]>(
      apiEndpoints.leads.contacts.events.counts(tenantId, contactId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
