import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import {
  approveVendor,
  createVendor,
  deleteVendorLogo,
  getAllVendors,
  getPublicVendorById,
  getVendorById,
  parseVendorQuery,
  rejectVendor,
  searchVendors,
  submitVendor,
  updateVendorDetails,
  uploadVendorLogo,
} from '@/lib/api/vendor.api';
import { PaginatedResponse } from '@/types/paginated-params';
import {
  Vendor,
  VendorDetailResponse,
  VendorListingParams,
  VendorPayloadWithLogo,
  VendorSearchParams,
  VendorSearchParseResult,
} from '@/types/vendor.types';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';

export const useCreateVendor = (): UseMutationResult<Vendor, AxiosError, void> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => createVendor(),
    onSuccess: (data) => {
      queryClient.setQueryData(queryKeys.user.authenticatedVendor, data);
    },
  });
};

export const useUpdateVendorDetails = (): UseMutationResult<
  Vendor,
  AxiosError,
  VendorPayloadWithLogo
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ logo, ...params }) => {
      if (logo instanceof File) {
        await uploadVendorLogo(logo);
      } else if (logo === '') {
        await deleteVendorLogo();
      }
      return await updateVendorDetails(params);
    },
    onSuccess: (data) => {
      queryClient.setQueryData(queryKeys.user.authenticatedVendor, data);
    },
  });
};

export const useGetAllVendors = (
  params: VendorListingParams
): UseQueryResult<PaginatedResponse<Vendor>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.admin.vendors.search(params),
    queryFn: () => getAllVendors(params),
  });
};

export const useGetVendorById = (vendorId: string): UseQueryResult<Vendor, AxiosError> => {
  return useQuery({
    enabled: !!vendorId,
    queryKey: queryKeys.admin.vendors.byId(vendorId),
    queryFn: () => getVendorById(vendorId),
  });
};

export const useApproveVendor = (): UseMutationResult<Vendor, AxiosError, { vendorId: string }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ vendorId }) => approveVendor(vendorId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.vendors.all });
    },
  });
};

export const useRejectVendor = (): UseMutationResult<Vendor, AxiosError, { vendorId: string }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ vendorId }) => rejectVendor(vendorId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.vendors.all });
    },
  });
};

export const useGetPublicVendorById = (
  vendorId: string
): UseQueryResult<VendorDetailResponse, AxiosError> => {
  return useQuery({
    enabled: !!vendorId,
    queryKey: queryKeys.vendors.byId(vendorId),
    queryFn: () => getPublicVendorById(vendorId),
  });
};

export const useSearchVendors = (
  params: VendorSearchParams
): UseQueryResult<PaginatedResponse<VendorDetailResponse>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.vendors.search(params),
    queryFn: () => searchVendors(params),
  });
};

export const useParseVendorQuery = (): UseMutationResult<
  VendorSearchParseResult,
  AxiosError,
  string
> => {
  return useMutation({
    mutationFn: (userInput: string) => parseVendorQuery(userInput),
  });
};

export const useSubmitVendor = (): UseMutationResult<Vendor, AxiosError, void> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => submitVendor(),
    onSuccess: (data) => {
      queryClient.setQueryData(queryKeys.user.authenticatedVendor, data);
    },
  });
};
