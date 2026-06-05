import { InfiniteData, useInfiniteQuery, UseInfiniteQueryResult, useQuery } from '@tanstack/react-query';
import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import {
  getEventCountsForLeadContact,
  getEventsForLeadContact,
} from '@/lib/api/lead-contact-event.api';
import { PaginatedResponse } from '@/types/paginated-params';
import {
  LeadContactEvent,
  LeadContactEventCountsParams,
  LeadContactEventSearchRequest,
} from '@/types/lead-contact-event.types';

export const useGetLeadContactEventsInfinite = (
  req: LeadContactEventSearchRequest
): UseInfiniteQueryResult<InfiniteData<PaginatedResponse<LeadContactEvent>>, AxiosError> => {
  return useInfiniteQuery({
    queryKey: queryKeys.leads.contactEvents.collection(
      req.tenantId,
      req.contactId,
      req.params
    ),
    queryFn: ({ pageParam = 0 }) =>
      getEventsForLeadContact({ ...req, params: { ...req.params, page: pageParam } }),
    getNextPageParam: (lastPage) => {
      const { number, totalPages } = lastPage.page;
      return number + 1 < totalPages ? number + 1 : undefined;
    },
    initialPageParam: 0,
    enabled: !!req.tenantId && !!req.contactId,
  });
};

export const useGetLeadContactEventCounts = (params: LeadContactEventCountsParams) => {
  return useQuery({
    queryKey: queryKeys.leads.contactEvents.counts(
      params.tenantId,
      params.contactId
    ),
    queryFn: () => getEventCountsForLeadContact(params),
    enabled: !!params.tenantId && !!params.contactId,
  });
};
