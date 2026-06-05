import {
  LeadList,
  LeadListParams,
  LeadListPayload,
  LeadListSearch,
  LeadListSearchParams,
} from '@/types/lead-list.types';
import { BasicParams } from '@/types/user.types';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import { LeadCompanyData, LeadContactData } from '@/types/leadSearch.types';

export const createLeadList = async (
  params: BasicParams,
  payload: LeadListPayload
): Promise<LeadList> => {
  try {
    const response = await apiClient.post<LeadList>(
      apiEndpoints.leads.list.collection(params.tenantId, params.workspaceId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadListById = async (params: LeadListParams): Promise<LeadList> => {
  try {
    const response = await apiClient.get<LeadList>(
      apiEndpoints.leads.list.item(params.tenantId, params.workspaceId, params.listId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadLists = async (
  req: LeadListSearchParams
): Promise<PaginatedResponse<LeadListSearch>> => {
  const { page, params, type } = req;
  try {
    const response = await apiClient.get<PaginatedResponse<LeadListSearch>>(
      apiEndpoints.leads.list.collection(params.tenantId, params.workspaceId),
      { params: { ...page, type } }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteLeadList = async (params: LeadListParams): Promise<void> => {
  try {
    await apiClient.delete(
      apiEndpoints.leads.list.item(params.tenantId, params.workspaceId, params.listId)
    );
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateLeadList = async (
  params: LeadListParams,
  payload: LeadListPayload
): Promise<LeadList> => {
  try {
    const response = await apiClient.put<LeadList>(
      apiEndpoints.leads.list.item(params.tenantId, params.workspaceId, params.listId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadContactsInList = async (
  params: LeadListParams,
  pageParams: PaginationParams
): Promise<PaginatedResponse<LeadContactData>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<LeadContactData>>(
      apiEndpoints.leads.list.contacts(params.tenantId, params.workspaceId, params.listId),
      { params: pageParams }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadCompaniesInList = async (
  params: LeadListParams,
  pageParams: PaginationParams
): Promise<PaginatedResponse<LeadCompanyData>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<LeadCompanyData>>(
      apiEndpoints.leads.list.companies(params.tenantId, params.workspaceId, params.listId),
      { params: pageParams }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
