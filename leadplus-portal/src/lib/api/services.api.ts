import { apiEndpoints } from '@/config/endpoints';
import apiClient from './client';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { Service, ServiceCategory, ServiceParams } from '@/types/services.types';

export const getAllServices = async (params: ServiceParams): Promise<Service[]> => {
  try {
    const response = await apiClient.get(apiEndpoints.services.collection, { params });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllServiceCategories = async (): Promise<ServiceCategory[]> => {
  try {
    const response = await apiClient.get(apiEndpoints.serviceCategories.collection);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
