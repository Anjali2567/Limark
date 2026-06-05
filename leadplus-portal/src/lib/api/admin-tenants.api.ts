import { apiEndpoints } from '@/config/endpoints';
import apiClient from './client';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { TenantActivityReport, TenantAnalytics, TenantListing, TenantUserResponse } from '@/types/tenant.types';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';

export const getAllTenantsForAdmin = async (
  params: PaginationParams
): Promise<PaginatedResponse<TenantListing>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<TenantListing>>(
      apiEndpoints.admin.tenants.collection,
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAdminTenantUsers = async (
  tenantId: string,
  params: PaginationParams
): Promise<PaginatedResponse<TenantUserResponse>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<TenantUserResponse>>(
      apiEndpoints.admin.tenants.users(tenantId),
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAdminTenantAnalytics = async (tenantId: string) => {
  try {
    const response = await apiClient.get<TenantAnalytics>(
      apiEndpoints.admin.tenants.analytics(tenantId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAdminTenantActivityByTenantId = async (tenantId: string): Promise<TenantActivityReport | null> => {
  try {
    const response = await apiClient.get<TenantActivityReport[]>(
      apiEndpoints.admin.analytics.tenantActivityByTenantId(tenantId)
    );
    return response.data?.length ? response.data[0] : null;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
