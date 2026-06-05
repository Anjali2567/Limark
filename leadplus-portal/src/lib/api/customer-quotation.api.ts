import { CustomerQuotation } from '@/types/customer-quotation.types';
import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';

export const getCustomerQuotationsByRfqId = async (
  rfqId: string,
  params: PaginationParams
): Promise<PaginatedResponse<CustomerQuotation>> => {
  try {
    const response = await apiClient.get(apiEndpoints.customer.quotations.byRfqId(rfqId), {
      params,
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
