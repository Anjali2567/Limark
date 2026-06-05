import {
  LeadEmailGenerateRequest,
  LeadEmailGenerateResponse,
  LeadEmailRequest,
} from '@/types/lead-email.types';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';

export const sendLeadEmail = async (request: LeadEmailRequest) => {
  try {
    const response = await apiClient.post<void>(
      apiEndpoints.leads.sendEmail(request.tenantId, request.workspaceId, request.contactId),
      request.payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const generateLeadEmail = async (
  request: LeadEmailGenerateRequest
): Promise<LeadEmailGenerateResponse> => {
  const { params, payload } = request;
  try {
    const response = await apiClient.post<LeadEmailGenerateResponse>(
      apiEndpoints.leads.generateEmail(params.tenantId, params.workspaceId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
