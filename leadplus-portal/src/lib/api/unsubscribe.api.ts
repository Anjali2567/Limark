import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';

export const unsubscribeContact = async (token: string): Promise<string> => {
  try {
    await apiClient.get<void>(apiEndpoints.unsubscribe, { params: { token } });
    return 'success';
  } catch (error) {
    throw handleAxiosError(error);
  }
};
