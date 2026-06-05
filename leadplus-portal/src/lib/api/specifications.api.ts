import { apiEndpoints } from '@/config/endpoints';
import apiClient from './client';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { Specification, SpecificationParams } from '@/types/specifications.types';

export const getAllSpecifications = async (
  params: SpecificationParams
): Promise<Specification[]> => {
  try {
    const response = await apiClient.get(apiEndpoints.specifications.collection, { params });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
