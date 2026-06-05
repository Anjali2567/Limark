import { AxiosError } from 'axios';
import { useMutation, useQuery, useQueryClient, UseQueryResult } from '@tanstack/react-query';

import { queryKeys } from '@/config/queryKeys';
import {
  createTenantLeadFilter,
  getTenantLeadFilters,
  updateTenantLeadFilter,
} from '@/lib/api/tenant-lead-filter.api';
import {
  TenantLeadFilterCreateRequest,
  TenantLeadFilterResponse,
  TenantLeadFilterUpdateRequest,
} from '@/types/tenant-lead-filter.types';

export const useGetTenantLeadFilters = (
  tenantId: string
): UseQueryResult<TenantLeadFilterResponse[], AxiosError> => {
  return useQuery({
    queryKey: queryKeys.tenant.leadFilters.all(tenantId),
    queryFn: () => getTenantLeadFilters(tenantId),
    enabled: !!tenantId,
  });
};

export const useCreateTenantLeadFilter = () => {
  const queryClient = useQueryClient();
  return useMutation<TenantLeadFilterResponse, AxiosError, TenantLeadFilterCreateRequest>({
    mutationFn: createTenantLeadFilter,
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.tenant.leadFilters.all(variables.tenantId),
      });
    },
  });
};

export const useUpdateTenantLeadFilter = () => {
  const queryClient = useQueryClient();
  return useMutation<TenantLeadFilterResponse, AxiosError, TenantLeadFilterUpdateRequest>({
    mutationFn: updateTenantLeadFilter,
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.tenant.leadFilters.all(variables.tenantId),
      });
    },
  });
};
