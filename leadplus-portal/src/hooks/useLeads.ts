import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import {
  getAllIndustriesInLeads,
  getAllLeadContacts,
  getAllLeadsWithCounts,
  getContactsByCompanyId,
  getLeadCompanyAllJobs,
  getLeadStatistics,
} from '@/lib/api/leads.api';
import {
  LeadCompanyJobs,
  LeadContacts,
  LeadSearchParams,
  LeadsListResponse,
  LeadWithCounts,
} from '@/types/leads.types';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import {
  InfiniteData,
  useInfiniteQuery,
  UseInfiniteQueryResult,
  useQuery,
  UseQueryResult,
} from '@tanstack/react-query';
import { LeadStatistics } from '@/types/leadSearch.types';

export const useGetAllLeadsWithCounts = (
  params: LeadSearchParams
): UseQueryResult<LeadWithCounts, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.all(params),
    queryFn: () => getAllLeadsWithCounts(params),
  });
};

export const useGetAllIndustriesInLeads = (): UseQueryResult<string[], AxiosError> => {
  return useQuery<string[], AxiosError>({
    queryKey: queryKeys.leads.industries,
    queryFn: () => getAllIndustriesInLeads(),
  });
};

export const useGetLeadContactsByCompanyId = (
  companyId: string
): UseQueryResult<LeadContacts[], AxiosError> => {
  return useQuery({
    enabled: !!companyId,
    queryKey: queryKeys.leads.contactsByCompanyId(companyId),
    queryFn: () => getContactsByCompanyId(companyId),
  });
};

export const useGetAllLeadContacts = (
  params: PaginationParams
): UseQueryResult<PaginatedResponse<LeadsListResponse>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.leads.contacts(params),
    queryFn: () => getAllLeadContacts(params),
  });
};

export const useGetLeadStatistics = (
  tenantId: string
): UseQueryResult<LeadStatistics, AxiosError> => {
  return useQuery({
    enabled: !!tenantId,
    queryKey: queryKeys.leads.statistics(tenantId),
    queryFn: () => getLeadStatistics(tenantId),
  });
};

export const useGetLeadCompanyJobsInfinite = (
  companyId: string
): UseInfiniteQueryResult<InfiniteData<PaginatedResponse<LeadCompanyJobs>>, AxiosError> => {
  return useInfiniteQuery({
    enabled: !!companyId,
    queryKey: queryKeys.leads.companyJobs(companyId),
    queryFn: ({ pageParam = 0 }) =>
      getLeadCompanyAllJobs(companyId, { page: pageParam, size: 10, sort: 'id,desc' }),
    getNextPageParam: (lastPage) => {
      const { number, totalPages } = lastPage.page;
      return number + 1 < totalPages ? number + 1 : undefined;
    },
    initialPageParam: 0,
  });
};
