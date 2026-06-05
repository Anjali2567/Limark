import { queryKeys } from '@/config/queryKeys';
import { getAgreementById, getAllAgreements, updateAgreement } from '@/lib/api/agreements.api';
import { Agreement, AgreementParams, AgreementRequest } from '@/types/agreements.types';
import { PaginatedResponse } from '@/types/paginated-params';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useGetAllAgreements = (
  params: AgreementParams
): UseQueryResult<PaginatedResponse<Agreement>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.agreements.search(params),
    queryFn: () => getAllAgreements(params),
  });
};

export const useGetAgreementById = (agreementId: string): UseQueryResult<Agreement, AxiosError> => {
  return useQuery({
    enabled: !!agreementId,
    queryKey: queryKeys.agreements.byId(agreementId),
    queryFn: () => getAgreementById(agreementId),
  });
};

export const useUpdateAgreement = (): UseMutationResult<
  Agreement,
  AxiosError,
  AgreementRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ agreementType, ...payload }) => updateAgreement(agreementType, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.agreements.all });
    },
  });
};
