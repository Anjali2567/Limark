import {
  LeadCompanyData,
  LeadCompanySearchRequest,
  LeadContactData,
  LeadContactSearchRequest,
  LeadChatResponse,
  LeadChatMessage,
  LeadFilterCriteria,
  LeadSearchFilterParams,
  LeadSearchFilterSave,
  LeadSearchFilterSaveRequest,
} from '@/types/leadSearch.types';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';

export const searchLeadContacts = async (
  tenantId: string,
  request: LeadContactSearchRequest
): Promise<PaginatedResponse<LeadContactData>> => {
  try {
    const response = await apiClient.post<PaginatedResponse<LeadContactData>>(
      apiEndpoints.leads.contactLeadSearch(tenantId),
      request.payload,
      { params: request.params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const searchLeadCompanies = async (
  tenantId: string,
  request: LeadCompanySearchRequest
): Promise<PaginatedResponse<LeadCompanyData>> => {
  try {
    const response = await apiClient.post<PaginatedResponse<LeadCompanyData>>(
      apiEndpoints.leads.companyLeadSearch(tenantId),
      request.payload,
      { params: request.params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const fetchContactIds = async (
  tenantId: string,
  request: LeadContactSearchRequest
): Promise<string[]> => {
  try {
    const response = await apiClient.post(
      apiEndpoints.leads.contactLeadIds(tenantId),
      request.payload,
      { params: request.params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export interface CompanyIdWithDomain {
  id: string;
  domain: string;
}

export const fetchCompanyIdsWithDomains = async (
  tenantId: string,
  request: LeadCompanySearchRequest
): Promise<CompanyIdWithDomain[]> => {
  try {
    const response = await apiClient.post(
      apiEndpoints.leads.companyLeadIds(tenantId),
      request.payload,
      { params: request.params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export interface CompanyLookupItem {
  id: string;
  name: string;
  domain: string;
}

export const lookupCompanies = async (
  tenantId: string,
  query: string
): Promise<CompanyLookupItem[]> => {
  try {
    const response = await apiClient.get<CompanyLookupItem[]>(
      apiEndpoints.leads.companyLookup(tenantId),
      { params: { query } }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const parseQuery = async (
  tenantId: string,
  query: string,
  conversationId?: number,
  criteria?: LeadFilterCriteria
): Promise<LeadChatResponse> => {
  try {
    const response = await apiClient.post<LeadChatResponse>(apiEndpoints.leads.parse(tenantId), {
      request: query,
      conversationId,
      criteria,
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadChatMessages = async (
  tenantId: string,
  conversationId: number
): Promise<LeadChatMessage[]> => {
  try {
    const response = await apiClient.get<LeadChatMessage[]>(
      `${apiEndpoints.leads.parse(tenantId)}/${conversationId}/messages`
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const saveLeadSearchFilters = async (
  request: LeadSearchFilterSaveRequest
): Promise<LeadSearchFilterSave> => {
  try {
    const response = await apiClient.post<LeadSearchFilterSave>(
      apiEndpoints.leads.searchFilters(request.tenantId),
      request.payload,
      { params: { type: request.type } }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadSearchFilters = async (
  params: LeadSearchFilterParams
): Promise<LeadSearchFilterSave[]> => {
  try {
    const response = await apiClient.get<LeadSearchFilterSave[]>(
      apiEndpoints.leads.searchFilters(params.tenantId),
      { params: { type: params.type } }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getMetadataFilterValues = async (
  tenantId: string
): Promise<Record<string, string[]>> => {
  try {
    const response = await apiClient.get<Record<string, string[]>>(
      apiEndpoints.leads.metadataFilters(tenantId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadContactInformation = async (
  tenantId: string,
  contactId: string
): Promise<LeadContactData> => {
  try {
    const response = await apiClient.get<LeadContactData>(
      apiEndpoints.leads.contactInfo(tenantId, contactId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadCompanyInformation = async (
  tenantId: string,
  companyId: string
): Promise<LeadCompanyData> => {
  try {
    const response = await apiClient.get<LeadCompanyData>(
      apiEndpoints.leads.companyInfo(tenantId, companyId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getLeadCompanyContacts = async (
  tenantId: string,
  companyId: string,
  params: PaginationParams
): Promise<PaginatedResponse<LeadContactData>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<LeadContactData>>(
      apiEndpoints.leads.companyContacts(tenantId, companyId),
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
