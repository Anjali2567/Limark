import { apiEndpoints } from '@/config/endpoints';
import {
  TenantLeadFilterCreateRequest,
  TenantLeadFilterResponse,
  TenantLeadFilterUpdateRequest,
} from '@/types/tenant-lead-filter.types';
import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';

export const getTenantLeadFilters = async (
  tenantId: string
): Promise<TenantLeadFilterResponse[]> => {
  try {
    const response = await apiClient.get<TenantLeadFilterResponse[]>(
      apiEndpoints.tenants.leadFilters.collection(tenantId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createTenantLeadFilter = async (
  request: TenantLeadFilterCreateRequest
): Promise<TenantLeadFilterResponse> => {
  try {
    const response = await apiClient.post<TenantLeadFilterResponse>(
      apiEndpoints.tenants.leadFilters.collection(request.tenantId),
      request.payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateTenantLeadFilter = async (
  request: TenantLeadFilterUpdateRequest
): Promise<TenantLeadFilterResponse> => {
  try {
    const response = await apiClient.put<TenantLeadFilterResponse>(
      apiEndpoints.tenants.leadFilters.item(request.tenantId, request.id),
      request.payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
