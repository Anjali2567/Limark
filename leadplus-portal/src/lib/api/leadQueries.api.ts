import { apiEndpoints } from '@/config/endpoints';
import apiClient from './client';
import { LeadQuery, LeadQueryRequest } from '@/types/leadQueries.types';
import { PaginatedResponse } from '@/types/paginated-params';
import { handleAxiosError } from '../utils/apiErrorHandler';

export const getLeadQueries = async (
  params: LeadQueryRequest
): Promise<PaginatedResponse<LeadQuery>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<LeadQuery>>(
      apiEndpoints.leads.queries(),
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
