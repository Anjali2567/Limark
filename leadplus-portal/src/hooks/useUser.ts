import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import { approveUser, getAllUsers, getUserById, rejectUser } from '@/lib/api/user.api';
import { PaginatedResponse } from '@/types/paginated-params';
import { User, UserListingParams } from '@/types/user.types';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';

export const useGetAllUsers = (
  params: UserListingParams
): UseQueryResult<PaginatedResponse<User>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.admin.users.search(params),
    queryFn: () => getAllUsers(params),
  });
};
export const useGetUserById = (userId: string): UseQueryResult<User, AxiosError> => {
  return useQuery({
    enabled: !!userId,
    queryKey: queryKeys.admin.users.byId(userId),
    queryFn: () => getUserById(userId),
  });
};

export const useApproveUser = (): UseMutationResult<User, AxiosError, { userId: string }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ userId }) => approveUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.users.all });
    },
  });
};

export const useRejectUser = (): UseMutationResult<User, AxiosError, { userId: string }> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ userId }) => rejectUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.users.all });
    },
  });
};
