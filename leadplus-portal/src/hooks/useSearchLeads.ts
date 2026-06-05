import { AxiosError } from 'axios';

import {
  CompanyIdWithDomain,
  CompanyLookupItem,
  fetchCompanyIdsWithDomains,
  fetchContactIds,
  getLeadChatMessages,
  getLeadCompanyContacts,
  getLeadCompanyInformation,
  getLeadContactInformation,
  getLeadSearchFilters,
  getMetadataFilterValues,
  lookupCompanies,
  parseQuery,
  saveLeadSearchFilters,
  searchLeadCompanies,
  searchLeadContacts,
} from '@/lib/api/leadSearch.api';
import {
  LeadCompanyData,
  LeadCompanySearchRequest,
  LeadContactData,
  LeadContactSearchRequest,
  LeadChatResponse,
  LeadChatMessage,
  LeadFilterCriteria,
  LeadSearchFilterParams,
  LeadSearchFilterSave,
  LeadSearchFilterSaveRequest,
} from '@/types/leadSearch.types';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { queryKeys } from '@/config/queryKeys';

export const useLeadContactsSearch = (
  tenantId: string,
  request: LeadContactSearchRequest | null
): UseQueryResult<PaginatedResponse<LeadContactData>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.searchContactLeads(request),
    queryFn: () => searchLeadContacts(tenantId, request!),
    enabled: !!request && Object.keys(request.payload).length > 0 && !!tenantId,
  });
};

export const useLeadCompaniesSearch = (
  tenantId: string,
  request: LeadCompanySearchRequest | null
): UseQueryResult<PaginatedResponse<LeadCompanyData>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.searchCompanyLeads(request),
    queryFn: () => searchLeadCompanies(tenantId, request!),
    enabled: !!request && Object.keys(request.payload).length > 0 && !!tenantId,
  });
};

export const useCompanyLookup = (
  tenantId: string,
  query: string,
  enabled = true
): UseQueryResult<CompanyLookupItem[], AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.companyLookup(tenantId, query),
    queryFn: () => lookupCompanies(tenantId, query),
    enabled: !!tenantId && enabled,
  });
};

export const useFetchContactIds = (): UseMutationResult<
  string[],
  AxiosError,
  { tenantId: string; request: LeadContactSearchRequest }
> => {
  return useMutation({
    mutationFn: ({ tenantId, request }: { tenantId: string; request: LeadContactSearchRequest }) =>
      fetchContactIds(tenantId, request),
  });
};

export const useFetchCompanyIdsWithDomains = (): UseMutationResult<
  CompanyIdWithDomain[],
  AxiosError,
  { tenantId: string; request: LeadCompanySearchRequest }
> => {
  return useMutation({
    mutationFn: ({ tenantId, request }: { tenantId: string; request: LeadCompanySearchRequest }) =>
      fetchCompanyIdsWithDomains(tenantId, request),
  });
};

export const useParseQuery = (): UseMutationResult<
  LeadChatResponse,
  AxiosError,
  { tenantId: string; query: string; conversationId?: number; criteria?: LeadFilterCriteria }
> => {
  return useMutation({
    mutationFn: ({ tenantId, query, conversationId, criteria }: { tenantId: string; query: string; conversationId?: number; criteria?: LeadFilterCriteria }) =>
      parseQuery(tenantId, query, conversationId, criteria),
  });
};

export const useGetLeadChatMessages = (
  tenantId: string,
  conversationId?: number
): UseQueryResult<LeadChatMessage[], AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.chatMessages(tenantId, conversationId),
    queryFn: () => getLeadChatMessages(tenantId, conversationId!),
    enabled: !!tenantId && !!conversationId,
  });
};

export const useGetLeadSavedFilters = (
  params: LeadSearchFilterParams
): UseQueryResult<LeadSearchFilterSave[], AxiosError> => {
  return useQuery({
    enabled: !!params.tenantId && !!params.type,
    queryKey: queryKeys.leads.savedSearchFilters(params),
    queryFn: () => getLeadSearchFilters(params),
  });
};

export const useSaveLeadSearchFilters = (): UseMutationResult<
  LeadSearchFilterSave,
  AxiosError,
  LeadSearchFilterSaveRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (searchData: LeadSearchFilterSaveRequest) => saveLeadSearchFilters(searchData),
    onSuccess: (_, { tenantId, type }) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.leads.savedSearchFilters({ tenantId, type }),
      });
    },
  });
};

export const useMetadataFilterValues = (
  tenantId: string
): UseQueryResult<Record<string, string[]>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.metadataFilters(tenantId),
    queryFn: () => getMetadataFilterValues(tenantId),
    enabled: !!tenantId,
  });
};

export const useGetLeadContactInfo = (
  tenantId: string,
  contactId: string
): UseQueryResult<LeadContactData, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.contactInfo(tenantId, contactId),
    queryFn: () => getLeadContactInformation(tenantId, contactId),
    enabled: !!tenantId && !!contactId,
  });
};

export const useGetLeadCompanyInfo = (
  tenantId: string,
  companyId: string
): UseQueryResult<LeadCompanyData, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.companyInfo(tenantId, companyId),
    queryFn: () => getLeadCompanyInformation(tenantId, companyId),
    enabled: !!tenantId && !!companyId,
  });
};

export const useGetLeadCompanyContacts = (
  tenantId: string,
  companyId: string,
  params: PaginationParams
): UseQueryResult<PaginatedResponse<LeadContactData>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.companyContacts(tenantId, companyId, params),
    queryFn: () => getLeadCompanyContacts(tenantId, companyId, params),
    enabled: !!tenantId && !!companyId,
  });
};
