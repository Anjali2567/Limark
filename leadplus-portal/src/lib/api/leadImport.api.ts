import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { TargetEntity } from '@/constants/import.constants';
import {
  BatchResponse,
  PreviewResponse,
  RecordResponse,
  UploadResponse,
} from '@/types/import.types';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import apiClient from './client';

export const uploadContactsImport = async (file: File): Promise<UploadResponse> => {
  try {
    const form = new FormData();
    form.append('file', file);
    const response = await apiClient.post<UploadResponse>(
      apiEndpoints.admin.leadImports.uploadContacts,
      form,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const uploadCompaniesImport = async (file: File): Promise<UploadResponse> => {
  try {
    const form = new FormData();
    form.append('file', file);
    const response = await apiClient.post<UploadResponse>(
      apiEndpoints.admin.leadImports.uploadCompanies,
      form,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const previewImport = async (id: number): Promise<PreviewResponse> => {
  try {
    const response = await apiClient.post<PreviewResponse>(
      apiEndpoints.admin.leadImports.preview(id)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const confirmImport = async (id: number): Promise<BatchResponse> => {
  try {
    const response = await apiClient.post<BatchResponse>(
      apiEndpoints.admin.leadImports.confirm(id)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const rollbackImport = async (id: number): Promise<BatchResponse> => {
  try {
    const response = await apiClient.post<BatchResponse>(
      apiEndpoints.admin.leadImports.rollback(id)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const listImportBatches = async (
  params: PaginationParams & { targetEntity?: TargetEntity }
): Promise<PaginatedResponse<BatchResponse>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<BatchResponse>>(
      apiEndpoints.admin.leadImports.collection,
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getImportRecords = async (
  id: number,
  params: PaginationParams
): Promise<PaginatedResponse<RecordResponse>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<RecordResponse>>(
      apiEndpoints.admin.leadImports.records(id),
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
