import { apiEndpoints } from '@/config/endpoints';
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
import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';

export const getWorkspaceDetails = async (params: BasicParams) => {
  try {
    const response = await apiClient.get<Workspace>(
      apiEndpoints.workspace.item(params.tenantId, params.workspaceId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createWorkspace = async (params: WorkspaceCreateRequest) => {
  try {
    const response = await apiClient.post<Workspace>(
      apiEndpoints.workspace.create(params.tenantId),
      params.payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateWorkspaceDetails = async (params: WorkspaceRequest) => {
  try {
    const response = await apiClient.put<Workspace>(
      apiEndpoints.workspace.item(params.tenantId, params.workspaceId),
      params.payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllWorkspaceUsers = async (
  params: BasicParams,
  page: PaginationParams
): Promise<PaginatedResponse<WorkspaceUsersResponse>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<WorkspaceUsersResponse>>(
      apiEndpoints.workspace.users(params.tenantId, params.workspaceId),
      { params: page }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const inviteUserToWorkspace = async (params: InviteUserRequest): Promise<UserWorkspace> => {
  try {
    const response = await apiClient.post<UserWorkspace>(
      apiEndpoints.workspace.inviteUser(params.tenantId, params.workspaceId),
      params.payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const acceptWorkspaceInvitation = async (token: string): Promise<void> => {
  try {
    await apiClient.post<void>(apiEndpoints.workspace.acceptInvitation, {}, { params: { token } });
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const revokeWorkspaceInvitation = async (params: UserBasicParams): Promise<void> => {
  try {
    await apiClient.delete<void>(
      apiEndpoints.workspace.revokeInvitation(params.tenantId, params.workspaceId, params.userId)
    );
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const transferWorkspaceOwnership = async (
  params: UserBasicParams
): Promise<UserWorkspace> => {
  try {
    const response = await apiClient.put<UserWorkspace>(
      apiEndpoints.workspace.transferOwnership(params.tenantId, params.workspaceId, params.userId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateWorkspaceUserRole = async (
  params: WorkspaceUserRoleUpdateRequest
): Promise<UserWorkspace> => {
  try {
    const response = await apiClient.put<UserWorkspace>(
      apiEndpoints.workspace.updateUserRole(params.tenantId, params.workspaceId, params.userId),
      params.payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
