import { queryKeys } from '@/config/queryKeys';
import { LeadType } from '@/constants/leadSearch.constants';
import {
  createLeadList,
  deleteLeadList,
  getLeadCompaniesInList,
  getLeadContactsInList,
  getLeadListById,
  getLeadLists,
  updateLeadList,
} from '@/lib/api/lead-list.api';
import {
  LeadList,
  LeadListParams,
  LeadListRequest,
  LeadListSearch,
  LeadListSearchParams,
} from '@/types/lead-list.types';
import { LeadCompanyData, LeadContactData } from '@/types/leadSearch.types';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useGetLeadLists = (
  search: LeadListSearchParams,
  options?: { enabled?: boolean; staleTime?: number }
): UseQueryResult<PaginatedResponse<LeadListSearch>, AxiosError> => {
  return useQuery({
    enabled: (options?.enabled ?? true) && !!search.params.tenantId && !!search.params.workspaceId,
    staleTime: options?.staleTime,
    queryKey: queryKeys.leads.leadList.search(
      search.params.tenantId,
      search.params.workspaceId,
      search.type,
      search.page
    ),
    queryFn: () => getLeadLists(search),
  });
};

export const useGetLeadListById = (
  params: LeadListParams
): UseQueryResult<LeadList, AxiosError> => {
  return useQuery({
    enabled: !!params.tenantId && !!params.workspaceId && !!params.listId,
    queryKey: queryKeys.leads.leadList.byId(params.tenantId, params.workspaceId, params.listId),
    queryFn: () => getLeadListById(params),
  });
};

export const useCreateLeadList = (): UseMutationResult<LeadList, AxiosError, LeadListRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (request: LeadListRequest) => createLeadList(request.params, request.payload),
    onSuccess: (_, request) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.leads.leadList.allByType(
          request.params.tenantId,
          request.params.workspaceId,
          request.payload.type
        ),
        refetchType: 'all',
      });
    },
  });
};

export const useUpdateLeadList = (): UseMutationResult<LeadList, AxiosError, LeadListRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (request: LeadListRequest) => updateLeadList(request.params, request.payload),
    onSuccess: (_, request) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.leads.leadList.allByType(
          request.params.tenantId,
          request.params.workspaceId,
          request.payload.type
        ),
        refetchType: 'all',
      });
    },
  });
};

export const useDeleteLeadList = (
  type: LeadType
): UseMutationResult<void, AxiosError, LeadListParams> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params: LeadListParams) => deleteLeadList(params),
    onSuccess: (_, params) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.leads.leadList.allByType(params.tenantId, params.workspaceId, type),
        refetchType: 'all',
      });
    },
  });
};

export const useGetLeadContactsInList = (
  params: LeadListParams,
  page: PaginationParams
): UseQueryResult<PaginatedResponse<LeadContactData>, AxiosError> => {
  return useQuery({
    enabled: !!params.tenantId && !!params.workspaceId && !!params.listId,
    queryKey: queryKeys.leads.leadList.contacts(
      params.tenantId,
      params.workspaceId,
      params.listId,
      page
    ),
    queryFn: () => getLeadContactsInList(params, page),
  });
};

export const useGetLeadCompaniesInList = (
  params: LeadListParams,
  page: PaginationParams
): UseQueryResult<PaginatedResponse<LeadCompanyData>, AxiosError> => {
  return useQuery({
    enabled: !!params.tenantId && !!params.workspaceId && !!params.listId,
    queryKey: queryKeys.leads.leadList.companies(
      params.tenantId,
      params.workspaceId,
      params.listId,
      page
    ),
    queryFn: () => getLeadCompaniesInList(params, page),
  });
};
