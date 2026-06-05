import { queryKeys } from '@/config/queryKeys';
import { getLeadQueries } from '@/lib/api/leadQueries.api';
import { LeadQueryRequest } from '@/types/leadQueries.types';
import { useQuery } from '@tanstack/react-query';

export const useGetLeadQueries = (params: LeadQueryRequest, options?: { enabled?: boolean }) => {
  return useQuery({
    queryKey: queryKeys.leads.queries(params),
    queryFn: () => getLeadQueries(params),
    enabled: options?.enabled ?? true,
  });
};
