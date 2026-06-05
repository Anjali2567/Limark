import { queryKeys } from '@/config/queryKeys';
import {
  acceptRFQQuotation,
  getAllVendorRFQQuotations,
  getQuotationById,
  rejectRFQQuotation,
  updateRFQQuotation,
} from '@/lib/api/quotations.api';
import { PaginatedResponse } from '@/types/paginated-params';
import {
  CustomerQuotationResponse,
  Quotation,
  QuotationIdentity,
  RFQQuotationRequest,
  VendorQuotationsRequest,
} from '@/types/quotation.types';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';

export const useGetAllVendorRFQQuotations = (
  params: VendorQuotationsRequest
): UseQueryResult<PaginatedResponse<CustomerQuotationResponse>, Error> => {
  return useQuery({
    enabled: !!params.tenantId && !!params.workspaceId,
    queryKey: queryKeys.vendor.rfq.quotations(params),
    queryFn: () => getAllVendorRFQQuotations(params),
  });
};

export const useGetRFQQuotationById = (
  tenantId: string,
  workspaceId: string,
  quotationId: string
): UseQueryResult<CustomerQuotationResponse, Error> => {
  return useQuery({
    enabled: !!tenantId && !!workspaceId && !!quotationId,
    queryKey: queryKeys.vendor.rfq.quotationById(quotationId),
    queryFn: () => getQuotationById(tenantId, workspaceId, quotationId),
  });
};

export const useAcceptRFQQuotation = (): UseMutationResult<Quotation, Error, QuotationIdentity> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (identity: QuotationIdentity) => acceptRFQQuotation(identity),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.vendor.rfq.all });
    },
  });
};

export const useRejectRFQQuotation = (): UseMutationResult<Quotation, Error, QuotationIdentity> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (identity: QuotationIdentity) => rejectRFQQuotation(identity),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.vendor.rfq.all });
    },
  });
};

export const useUpdateRFQQuotation = (): UseMutationResult<
  Quotation,
  Error,
  RFQQuotationRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (req: RFQQuotationRequest) => updateRFQQuotation(req),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.vendor.rfq.all });
    },
  });
};
