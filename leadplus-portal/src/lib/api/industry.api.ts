import { apiEndpoints } from '@/config/endpoints';
import { Industry, IndustryPayload } from '@/types/industry.types';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';

export const getAllIndustries = async (params: PaginationParams) => {
  try {
    const response = await apiClient.get<PaginatedResponse<Industry>>(
      apiEndpoints.industries.collection,
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAdminAllIndustries = async (params: PaginationParams) => {
  try {
    const response = await apiClient.get<PaginatedResponse<Industry>>(
      apiEndpoints.admin.industries.collection,
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getIndustryById = async (industryId: string) => {
  try {
    const response = await apiClient.get<Industry>(apiEndpoints.admin.industries.item(industryId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createIndustry = async (payload: IndustryPayload) => {
  try {
    const response = await apiClient.post<Industry>(
      apiEndpoints.admin.industries.collection,
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateIndustry = async (industryId: string, payload: IndustryPayload) => {
  try {
    const response = await apiClient.put<Industry>(
      apiEndpoints.admin.industries.item(industryId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const uploadIndustryImage = async (industryId: string, imageFile: File) => {
  try {
    const formData = new FormData();
    formData.append('file', imageFile);
    const response = await apiClient.put<Industry>(
      apiEndpoints.admin.industries.imageUpload(industryId),
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const disableIndustry = async (industryId: string) => {
  try {
    const response = await apiClient.put<Industry>(
      apiEndpoints.admin.industries.toggleDisable(industryId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const enableIndustry = async (industryId: string) => {
  try {
    const response = await apiClient.delete<Industry>(
      apiEndpoints.admin.industries.toggleDisable(industryId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
