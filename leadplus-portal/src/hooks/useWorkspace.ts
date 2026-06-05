import {
  acceptWorkspaceInvitation,
  createWorkspace,
  getAllWorkspaceUsers,
  getWorkspaceDetails,
  inviteUserToWorkspace,
  revokeWorkspaceInvitation,
  transferWorkspaceOwnership,
  updateWorkspaceUserRole,
  updateWorkspaceDetails,
} from '@/lib/api/workspace.api';
import {
  BasicParams,
  InviteUserRequest,
  UserBasicParams,
  UserWorkspace,
  WorkspaceUsersResponse,
} from '@/types/user.types';
import {
  Workspace,
  WorkspaceCreateRequest,
  WorkspaceRequest,
  WorkspaceUserRoleUpdateRequest,
} from '@/types/workspace.types';
import {
  useMutation,
  UseMutationResult,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { useQuery } from '@tanstack/react-query';
import { queryKeys } from '@/config/queryKeys';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';

export const useGetWorkspaceDetails = (
  params: BasicParams
): UseQueryResult<Workspace, AxiosError> => {
  return useQuery({
    enabled: !!params.workspaceId && !!params.tenantId,
    queryKey: queryKeys.workspace.byId(params),
    queryFn: () => getWorkspaceDetails(params),
  });
};

export const useCreateWorkspace = (): UseMutationResult<
  Workspace,
  AxiosError,
  WorkspaceCreateRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => createWorkspace(params),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.user.authenticatedUserWorkspaces });
    },
  });
};

export const useUpdateWorkspaceDetails = (): UseMutationResult<
  Workspace,
  AxiosError,
  WorkspaceRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => updateWorkspaceDetails(params),
    onSuccess: (data, variables) => {
      queryClient.setQueryData(queryKeys.workspace.byId(variables), data);
      queryClient.invalidateQueries({ queryKey: queryKeys.user.authenticatedUserWorkspaces });
    },
  });
};

export const useGetAllWorkspaceUsers = (
  params: BasicParams,
  page: PaginationParams
): UseQueryResult<PaginatedResponse<WorkspaceUsersResponse>, AxiosError> => {
  return useQuery({
    enabled: !!params.workspaceId && !!params.tenantId,
    queryKey: queryKeys.workspace.search({ ...params, paginationParams: page }),
    queryFn: () => getAllWorkspaceUsers(params, page),
  });
};

export const useInviteUserToWorkspace = (): UseMutationResult<
  UserWorkspace,
  AxiosError,
  InviteUserRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => inviteUserToWorkspace(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.workspace.users(variables) });
    },
  });
};

export const useAcceptWorkspaceInvitation = (): UseMutationResult<void, AxiosError, string> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: acceptWorkspaceInvitation,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.user.authenticatedUserWorkspaces });
    },
  });
};

export const useRevokeWorkspaceAccess = (): UseMutationResult<
  void,
  AxiosError,
  UserBasicParams
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => revokeWorkspaceInvitation(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.workspace.users(variables) });
    },
  });
};

export const useTransferWorkspaceOwnership = (): UseMutationResult<
  UserWorkspace,
  AxiosError,
  UserBasicParams
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => transferWorkspaceOwnership(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.workspace.users(variables) });
      queryClient.invalidateQueries({ queryKey: queryKeys.user.authenticatedUserWorkspaces });
    },
  });
};

export const useUpdateWorkspaceUserRole = (): UseMutationResult<
  UserWorkspace,
  AxiosError,
  WorkspaceUserRoleUpdateRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => updateWorkspaceUserRole(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.workspace.users(variables) });
      queryClient.invalidateQueries({ queryKey: queryKeys.user.authenticatedUserWorkspaces });
    },
  });
};
