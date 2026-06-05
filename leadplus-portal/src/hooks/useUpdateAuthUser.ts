'use client';

import { useMutation, useQueryClient, UseMutationResult } from '@tanstack/react-query';
import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import { updateAuthenticatedUser } from '@/lib/api/auth.api';
import { UpdateUserProfileRequest, User } from '@/types/user.types';

export const useUpdateAuthenticatedUser = (): UseMutationResult<
  User,
  AxiosError,
  UpdateUserProfileRequest
> => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateAuthenticatedUser,
    onSuccess: (data) => {
      queryClient.setQueryData(queryKeys.user.authenticatedUser, data);
    },
  });
};
