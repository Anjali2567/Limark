import { AxiosError } from 'axios';

import {
  resetPassword,
  login,
  LoginRequest,
  refreshToken,
  requestForgotPassword,
  userSignup,
  googleLogin,
  resendEmail,
  verifyEmail,
  getAuthenticatedVendor,
} from '@/lib/api/auth.api';
import { GoogleAuthRequest, TokenResponse } from '@/types/auth.types';
import { ForgotPasswordRequest, ResetPasswordRequest, SignupPayload } from '@/types/user.types';
import { useMutation, UseMutationResult, useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/config/queryKeys';
import { vendorSignup } from '@/lib/api/vendorAuth.api';

export const useLogin = (): UseMutationResult<TokenResponse, AxiosError, LoginRequest> => {
  return useMutation({ mutationFn: login });
};

export const useGetAccessToken = (): UseMutationResult<TokenResponse, Error, string> => {
  return useMutation({
    mutationFn: (token: string) => refreshToken(token),
  });
};

export const useSignup = (): UseMutationResult<TokenResponse, AxiosError, SignupPayload> => {
  return useMutation({
    mutationFn: userSignup,
  });
};

export const useForgotPasswordRequest = (): UseMutationResult<
  void,
  AxiosError,
  ForgotPasswordRequest
> => {
  return useMutation({
    mutationFn: requestForgotPassword,
  });
};

export const useResetPassword = (): UseMutationResult<
  TokenResponse,
  AxiosError,
  ResetPasswordRequest
> => {
  return useMutation({
    mutationFn: resetPassword,
  });
};

export const useGoogleAuth = (): UseMutationResult<
  TokenResponse,
  AxiosError,
  GoogleAuthRequest
> => {
  return useMutation({
    mutationFn: googleLogin,
  });
};

export const useVerifyEmail = (): UseMutationResult<void, AxiosError, { token: string }> => {
  return useMutation({
    mutationFn: verifyEmail,
  });
};

export const useResendEmail = (): UseMutationResult<void, AxiosError> => {
  return useMutation({
    mutationFn: resendEmail,
  });
};

export const useGetAuthenticatedVendor = ({ enabled = true }: { enabled?: boolean } = {}) => {
  return useQuery({
    queryKey: queryKeys.user.authenticatedVendor,
    queryFn: getAuthenticatedVendor,
    retry: false,
    enabled,
  });
};

export const useVendorSignup = (): UseMutationResult<TokenResponse, AxiosError, SignupPayload> => {
  return useMutation({
    mutationFn: vendorSignup,
  });
};
