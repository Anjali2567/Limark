import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import { getCustomerQuotationsByRfqId } from '@/lib/api/customer-quotation.api';
import { CustomerQuotation } from '@/types/customer-quotation.types';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import { useQuery, UseQueryResult } from '@tanstack/react-query';

export const useGetCustomerQuotationsByRfqId = (
  rfqId: string,
  params: PaginationParams
): UseQueryResult<PaginatedResponse<CustomerQuotation>, AxiosError> => {
  return useQuery({
    enabled: !!rfqId,
    queryKey: queryKeys.customer.quotations.byRfqId(rfqId, params),
    queryFn: async () => getCustomerQuotationsByRfqId(rfqId, params),
  });
};
