import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import {
  getAdminTenantActivityByTenantId,
  getAdminTenantAnalytics,
  getAdminTenantUsers,
  getAllTenantsForAdmin,
} from '@/lib/api/admin-tenants.api';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import { TenantActivityReport, TenantAnalytics, TenantListing, TenantUserResponse } from '@/types/tenant.types';

export const useGetAdminTenants = (
  params: PaginationParams
): UseQueryResult<PaginatedResponse<TenantListing>, AxiosError> => {
  return useQuery<PaginatedResponse<TenantListing>, AxiosError>({
    queryKey: queryKeys.admin.tenants.search(params),
    queryFn: () => getAllTenantsForAdmin(params),
  });
};

export const useGetAdminTenantUsers = (
  tenantId: string,
  params: PaginationParams
): UseQueryResult<PaginatedResponse<TenantUserResponse>, AxiosError> => {
  return useQuery({
    enabled: !!tenantId,
    queryKey: queryKeys.admin.tenants.users(tenantId, params),
    queryFn: () => getAdminTenantUsers(tenantId, params),
  });
};

export const useGetAdminTenantAnalytics = (
  tenantId: string
): UseQueryResult<TenantAnalytics, AxiosError> => {
  return useQuery({
    enabled: !!tenantId,
    queryKey: queryKeys.admin.tenants.analytics(tenantId),
    queryFn: () => getAdminTenantAnalytics(tenantId),
  });
};

export const useGetAdminTenantActivityByTenantId = (
  tenantId: string
): UseQueryResult<TenantActivityReport | null, AxiosError> => {
  return useQuery({
    enabled: !!tenantId,
    queryKey: queryKeys.admin.tenants.tenantActivityByTenantId(tenantId),
    queryFn: () => getAdminTenantActivityByTenantId(tenantId),
  });
};
