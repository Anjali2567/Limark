import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '@/lib/utils/apiErrorHandler';
import { GoogleAuthRequest, TokenResponse } from '@/types/auth.types';
import {
  ForgotPasswordRequest,
  ResetPasswordRequest,
  SignupPayload,
  UpdateUserProfileRequest,
  User,
  UserWorkspace,
} from '@/types/user.types';
import apiClient from './client';
import { Vendor } from '@/types/vendor.types';

export interface LoginRequest {
  email: string;
  password: string;
}

export const login = async (credentials: LoginRequest): Promise<TokenResponse> => {
  try {
    const response = await apiClient.post<TokenResponse>(apiEndpoints.auth.login, credentials);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const refreshToken = async (token: string): Promise<TokenResponse> => {
  try {
    const response = await apiClient.post<TokenResponse>(
      apiEndpoints.auth.refresh,
      {},
      { params: { refreshToken: token } }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAuthenticatedUser = async (): Promise<User> => {
  try {
    const response = await apiClient.get<User>(apiEndpoints.auth.me);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateAuthenticatedUser = async (
  payload: UpdateUserProfileRequest
): Promise<User> => {
  try {
    const response = await apiClient.put<User>(apiEndpoints.auth.me, payload);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAuthenticatedVendor = async (): Promise<Vendor> => {
  try {
    const response = await apiClient.get<Vendor>(apiEndpoints.auth.vendor);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const userSignup = async (payload: SignupPayload): Promise<TokenResponse> => {
  try {
    const response = await apiClient.post<TokenResponse>(apiEndpoints.auth.signup, payload);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const requestForgotPassword = async (payload: ForgotPasswordRequest) => {
  try {
    await apiClient.post(apiEndpoints.auth.forgotPasswordRequest, payload);
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const resetPassword = async (payload: ResetPasswordRequest): Promise<TokenResponse> => {
  try {
    const response = await apiClient.post(apiEndpoints.auth.forgotPassword, payload);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAuthenticatedUserWorkspaces = async (): Promise<UserWorkspace[]> => {
  try {
    const response = await apiClient.get<UserWorkspace[]>(apiEndpoints.auth.workspaces);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const googleLogin = async (payload: GoogleAuthRequest): Promise<TokenResponse> => {
  try {
    const response = await apiClient.post<TokenResponse>(apiEndpoints.auth.google, payload);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const verifyEmail = async ({ token }: { token: string }): Promise<void> => {
  try {
    await apiClient.post<void>(apiEndpoints.auth.verifyEmail, { token });
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const resendEmail = async (): Promise<void> => {
  try {
    await apiClient.put<void>(apiEndpoints.auth.resendEmail);
  } catch (error) {
    throw handleAxiosError(error);
  }
};
