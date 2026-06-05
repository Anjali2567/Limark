import { apiEndpoints } from '@/config/endpoints';
import { TokenResponse } from '@/types/auth.types';
import { SignupPayload } from '@/types/user.types';
import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';

export const vendorSignup = async (payload: SignupPayload): Promise<TokenResponse> => {
  try {
    const response = await apiClient.post<TokenResponse>(apiEndpoints.vendor.auth.signup, payload);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
