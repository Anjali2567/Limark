import { queryKeys } from '@/config/queryKeys';
import {
  addVendorShowcaseAttachment,
  createVendorShowcase,
  deleteVendorShowcase,
  deleteVendorShowcaseAttachment,
  getAllVendorShowcases,
  getAllVendorShowcasesByVendorId,
  getVendorShowCaseById,
  updateVendorShowcase,
} from '@/lib/api/vendor-showcase.api';
import {
  VendorShowcase,
  VendorShowcaseAttachmentDetails,
  VendorShowcaseBasicDetails,
  VendorShowcaseDetails,
  VendorShowcaseWithAttachment,
} from '@/types/vendor-showcase.types';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useGetAllVendorShowcases = (
  params: VendorShowcaseBasicDetails
): UseQueryResult<VendorShowcase[], AxiosError> => {
  return useQuery({
    enabled: !!params.tenantId && !!params.vendorId,
    queryKey: queryKeys.vendor.showcase.all(params),
    queryFn: () => getAllVendorShowcases(params),
  });
};

export const useGetVendorShowCaseById = (
  params: VendorShowcaseDetails
): UseQueryResult<VendorShowcase, AxiosError> => {
  return useQuery({
    enabled: !!params.tenantId && !!params.vendorId && !!params.id,
    queryKey: queryKeys.vendor.showcase.byId(params),
    queryFn: () => getVendorShowCaseById(params),
  });
};

export const useDeleteVendorShowcase = (): UseMutationResult<
  void,
  AxiosError,
  VendorShowcaseDetails
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params: VendorShowcaseDetails) => deleteVendorShowcase(params),
    onSuccess: (_, params) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.vendor.showcase.all(params),
      });
    },
  });
};

export const useDeleteVendorShowcaseAttachment = (): UseMutationResult<
  void,
  AxiosError,
  VendorShowcaseAttachmentDetails
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params: VendorShowcaseAttachmentDetails) => deleteVendorShowcaseAttachment(params),
    onSuccess: (_, params) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.vendor.showcase.all(params),
      });
    },
  });
};

export const useUpsertVendorShowcase = (
  onProjectCreated?: (id: string) => void
): UseMutationResult<VendorShowcase, AxiosError, VendorShowcaseWithAttachment> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, file, ...params }: VendorShowcaseWithAttachment) => {
      let showcase;
      if (id) {
        showcase = await updateVendorShowcase({ id, ...params });
      } else {
        showcase = await createVendorShowcase(params);
        onProjectCreated?.(showcase.id);
      }
      if (file instanceof File) {
        showcase = await addVendorShowcaseAttachment({ id: showcase.id, file, ...params });
      }
      return showcase;
    },
    onSuccess: (_, { tenantId, vendorId }) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.vendor.showcase.all({ tenantId, vendorId }),
      });
    },
  });
};

export const useGetAllVendorShowcasesByVendorId = (params: {
  vendorId: string;
}): UseQueryResult<VendorShowcase[], AxiosError> => {
  return useQuery({
    enabled: !!params.vendorId,
    queryKey: queryKeys.admin.vendors.showcase(params.vendorId),

    queryFn: () => getAllVendorShowcasesByVendorId(params),
  });
};
