import { apiEndpoints } from '@/config/endpoints';
import {
  LeadCompanyJobs,
  LeadContacts,
  LeadSearchParams,
  LeadsListResponse,
  LeadWithCounts,
} from '@/types/leads.types';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';
import { LeadStatistics } from '@/types/leadSearch.types';

export const getAllLeadsWithCounts = async (params: LeadSearchParams): Promise<LeadWithCounts> => {
  try {
    const response = await apiClient.get<LeadWithCounts>(apiEndpoints.leads.search(), {
      params,
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllIndustriesInLeads = async (): Promise<string[]> => {
  try {
    const response = await apiClient.get<string[]>(apiEndpoints.leads.industries());
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getContactsByCompanyId = async (companyId: string): Promise<LeadContacts[]> => {
  try {
    const response = await apiClient.get<LeadContacts[]>(
      apiEndpoints.leads.contactsByCompanyId(companyId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllLeadContacts = async (
  params: PaginationParams
): Promise<PaginatedResponse<LeadsListResponse>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<LeadsListResponse>>(
      apiEndpoints.leads.contacts.root,
      {
        params,
      }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadStatistics = async (tenantId: string): Promise<LeadStatistics> => {
  try {
    const response = await apiClient.get<LeadStatistics>(apiEndpoints.leads.statistics(tenantId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadCompanyAllJobs = async (
  companyId: string,
  params: PaginationParams
): Promise<PaginatedResponse<LeadCompanyJobs>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<LeadCompanyJobs>>(
      apiEndpoints.leads.companyJobs(companyId),
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
