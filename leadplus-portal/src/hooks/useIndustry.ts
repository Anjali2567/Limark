import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import {
  createIndustry,
  disableIndustry,
  enableIndustry,
  getAdminAllIndustries,
  getAllIndustries,
  getIndustryById,
  updateIndustry,
  uploadIndustryImage,
} from '@/lib/api/industry.api';
import { Industry, IndustryRequestWithImage } from '@/types/industry.types';
import {
  bulkItemPaginationParams,
  PaginatedResponse,
  PaginationParams,
} from '@/types/paginated-params';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';

export const useGetAllIndustries = (
  params: PaginationParams
): UseQueryResult<PaginatedResponse<Industry>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.industries.search(params),
    queryFn: () => getAllIndustries(params),
  });
};

export const useGetAdminAllIndustries = (
  params: PaginationParams
): UseQueryResult<PaginatedResponse<Industry>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.industries.search(params),
    queryFn: () => getAdminAllIndustries(params),
  });
};

export const useGetIndustryList = () => {
  const { data: industryList } = useGetAllIndustries(bulkItemPaginationParams);
  return (
    industryList?.content.map((industry) => ({ label: industry.name, value: industry.id })) || []
  );
};

export const useGetIndustryById = (industryId: string): UseQueryResult<Industry, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.industries.byId(industryId),
    queryFn: () => getIndustryById(industryId),
    enabled: !!industryId,
  });
};

export const useUpsertIndustry = (): UseMutationResult<
  Industry,
  AxiosError,
  IndustryRequestWithImage
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, imageFile, ...payload }) => {
      let industry;
      if (id) {
        industry = await updateIndustry(id, payload);
      } else {
        industry = await createIndustry(payload);
      }
      if (imageFile instanceof File) {
        await uploadIndustryImage(industry.id, imageFile);
      }
      return industry;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.industries.all });
    },
  });
};

export const useToggleIndustryStatus = (): UseMutationResult<
  Industry,
  AxiosError,
  { id: string; active: boolean }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, active }) => (active ? enableIndustry(id) : disableIndustry(id)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.industries.all });
    },
  });
};
