import { User, UserListingParams } from '@/types/user.types';
import apiClient from './client';
import { PaginatedResponse } from '@/types/paginated-params';
import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';

export const getAllUsers = async (params: UserListingParams) => {
  try {
    const response = await apiClient.get<PaginatedResponse<User>>(
      apiEndpoints.admin.users.collection,
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getUserById = async (userId: string) => {
  try {
    const response = await apiClient.get<User>(apiEndpoints.admin.users.item(userId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const approveUser = async (userId: string) => {
  try {
    const response = await apiClient.put(apiEndpoints.admin.users.approve(userId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const rejectUser = async (userId: string) => {
  try {
    const response = await apiClient.put(apiEndpoints.admin.users.reject(userId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
