import { apiEndpoints } from '@/config/endpoints';
import {
  Vendor,
  VendorDetailResponse,
  VendorListingParams,
  VendorPayload,
  VendorSearchParams,
  VendorSearchParseResult,
} from '@/types/vendor.types';
import apiClient from './client';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { PaginatedResponse } from '@/types/paginated-params';

export const createVendor = async (): Promise<Vendor> => {
  try {
    const response = await apiClient.post<Vendor>(apiEndpoints.auth.vendor);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateVendorDetails = async (request: VendorPayload): Promise<Vendor> => {
  try {
    const response = await apiClient.put<Vendor>(apiEndpoints.auth.vendor, request);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const uploadVendorLogo = async (imageFile: File) => {
  try {
    const formData = new FormData();
    formData.append('file', imageFile);
    const response = await apiClient.put<Vendor>(apiEndpoints.auth.vendorLogo, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteVendorLogo = async (): Promise<Vendor> => {
  try {
    const response = await apiClient.delete<Vendor>(apiEndpoints.auth.vendorLogo);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllVendors = async (params: VendorListingParams) => {
  try {
    const response = await apiClient.get<PaginatedResponse<Vendor>>(
      apiEndpoints.admin.vendor.collection,
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getVendorById = async (vendorId: string) => {
  try {
    const response = await apiClient.get<Vendor>(apiEndpoints.admin.vendor.item(vendorId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const approveVendor = async (vendorId: string) => {
  try {
    const response = await apiClient.put(apiEndpoints.admin.vendor.approve(vendorId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const rejectVendor = async (vendorId: string) => {
  try {
    const response = await apiClient.put(apiEndpoints.admin.vendor.reject(vendorId), {});
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const searchVendors = async (params: VendorSearchParams) => {
  try {
    const { industryIds, serviceIds, specificationIds, certifications, pageParams } = params;
    const response = await apiClient.post<PaginatedResponse<VendorDetailResponse>>(
      apiEndpoints.vendor.search,
      { industryIds, serviceIds, specificationIds, certifications },
      { params: pageParams }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const parseVendorQuery = async (userInput: string): Promise<VendorSearchParseResult> => {
  try {
    const response = await apiClient.post<VendorSearchParseResult>(
      apiEndpoints.vendor.searchParse,
      { prompt: userInput }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getPublicVendorById = async (vendorId: string): Promise<VendorDetailResponse> => {
  try {
    const response = await apiClient.get<VendorDetailResponse>(
      apiEndpoints.vendor.searchVendor(vendorId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const submitVendor = async (): Promise<Vendor> => {
  try {
    const response = await apiClient.put<Vendor>(apiEndpoints.auth.vendorSubmit);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
