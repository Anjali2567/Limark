import {
  LeadNote,
  LeadNoteParams,
  LeadNotePayload,
  LeadNoteSourceParams,
} from '@/types/lead-notes.types';
import { BasicParams } from '@/types/user.types';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { PaginatedResponse } from '@/types/paginated-params';

export const getAllNotesForLead = async (
  params: LeadNoteSourceParams
): Promise<PaginatedResponse<LeadNote>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<LeadNote>>(
      apiEndpoints.leads.notes.bySource(params.tenantId, params.workspaceId, params.sourceId),
      {
        params: params.params,
      }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadNoteById = async (params: LeadNoteParams): Promise<LeadNote> => {
  try {
    const response = await apiClient.get<LeadNote>(
      apiEndpoints.leads.notes.item(params.tenantId, params.workspaceId, params.leadNoteId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createLeadNote = async (
  params: BasicParams,
  payload: LeadNotePayload
): Promise<LeadNote> => {
  try {
    const response = await apiClient.post<LeadNote>(
      apiEndpoints.leads.notes.collection(params.tenantId, params.workspaceId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateLeadNote = async (params: LeadNoteParams, note: string): Promise<LeadNote> => {
  try {
    const response = await apiClient.put<LeadNote>(
      apiEndpoints.leads.notes.item(params.tenantId, params.workspaceId, params.leadNoteId),
      { note }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteLeadNote = async (params: LeadNoteParams): Promise<void> => {
  try {
    await apiClient.delete(
      apiEndpoints.leads.notes.item(params.tenantId, params.workspaceId, params.leadNoteId)
    );
  } catch (error) {
    throw handleAxiosError(error);
  }
};
