import { queryKeys } from '@/config/queryKeys';
import { AgreementType } from '@/constants/agreement.constant';
import {
  getVendorAgreementByType,
  sendVendorAgreementOtp,
  verifyVendorAgreementOtp,
} from '@/lib/api/vendor-agreements.api';
import {
  VendorAgreement,
  VendorAgreementParams,
  VendorAgreementVerifyOtpRequest,
} from '@/types/vendor-agreements.types';
import {
  useMutation,
  UseMutationResult,
  useQueries,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useGetAllVendorAgreements = (vendor: string) => {
  return useQueries({
    queries: Object.values(AgreementType).map((agreementType) => ({
      enabled: !!vendor,
      queryKey: queryKeys.vendor.agreement.byType({ vendorId: vendor, agreementType }),
      queryFn: () => getVendorAgreementByType({ vendorId: vendor, agreementType }),
    })),
  });
};

export const useGetVendorAgreementByType = (
  params: VendorAgreementParams
): UseQueryResult<VendorAgreement, AxiosError> => {
  return useQuery({
    enabled: !!params.vendorId && !!params.agreementType,
    queryKey: queryKeys.vendor.agreement.byType(params),
    queryFn: () => getVendorAgreementByType(params),
  });
};

export const useRequestOtpForVendorAgreement = (): UseMutationResult<
  void,
  AxiosError,
  VendorAgreementParams
> => {
  return useMutation({
    mutationFn: (params: VendorAgreementParams) => sendVendorAgreementOtp(params),
  });
};

export const useVerifyVendorAgreementOtp = (): UseMutationResult<
  void,
  AxiosError,
  VendorAgreementVerifyOtpRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: VendorAgreementVerifyOtpRequest) => verifyVendorAgreementOtp(payload),
    onSuccess: (_, { vendorId, agreementType }) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.vendor.agreement.byType({ vendorId, agreementType }),
      });
    },
  });
};
