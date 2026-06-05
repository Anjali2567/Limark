import {
  Tenant,
  IdentityAuthRequest,
  TenantAnnouncement,
  TenantAnnouncementContact,
  TenantAnnouncementSearch,
  TenantAnnouncementSmtpRequest,
  TenantContact,
  TenantWorkspaceSearch,
  TenantWorkspaceResponse,
  TenantUserSearch,
  TenantUserResponse,
  TenantRecipientRequest,
  TenantRecipients,
  TenantModules,
  CreateAnnouncementPayload,
  UpdateAnnouncementPayload,
} from '@/types/tenant.types';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { apiEndpoints } from '@/config/endpoints';
import apiClient from './client';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';

export const getTenantDetails = async (tenantId: string) => {
  try {
    const response = await apiClient.get<Tenant>(apiEndpoints.tenants.item(tenantId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const connectZohoCRM = async (tenantId: string, payload: IdentityAuthRequest) => {
  try {
    const response = await apiClient.post<Tenant>(
      apiEndpoints.tenants.connectZohoCRM(tenantId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const disconnectZohoCRM = async (tenantId: string) => {
  try {
    const response = await apiClient.delete<Tenant>(
      apiEndpoints.tenants.disconnectZohoCRM(tenantId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllTenantWorkspaces = async (
  payload: TenantWorkspaceSearch
): Promise<PaginatedResponse<TenantWorkspaceResponse>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<TenantWorkspaceResponse>>(
      apiEndpoints.tenants.workspaces(payload.tenantId),
      { params: payload.params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllTenantUsers = async (
  payload: TenantUserSearch
): Promise<PaginatedResponse<TenantUserResponse>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<TenantUserResponse>>(
      apiEndpoints.tenants.users(payload.tenantId),
      { params: payload.params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const connectHubSpotCRM = async (tenantId: string, payload: IdentityAuthRequest) => {
  try {
    const response = await apiClient.post<Tenant>(
      apiEndpoints.tenants.connectHubSpotCRM(tenantId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const disconnectHubSpotCRM = async (tenantId: string) => {
  try {
    const response = await apiClient.delete<Tenant>(
      apiEndpoints.tenants.disconnectHubSpotCRM(tenantId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const syncHubSpotRecords = async ({ tenantId }: { tenantId: string }): Promise<void> => {
  try {
    await apiClient.post<void>(apiEndpoints.tenants.syncHubSpotRecords(tenantId));
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const syncZohoRecords = async ({ tenantId }: { tenantId: string }): Promise<void> => {
  try {
    await apiClient.post<void>(apiEndpoints.tenants.syncZohoRecords(tenantId));
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateTenantRecipients = async (payload: TenantRecipientRequest) => {
  try {
    const response = await apiClient.put<Tenant>(
      apiEndpoints.tenants.recipients(payload.tenantId),
      payload.payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getTenantRecipients = async (tenantId: string) => {
  try {
    const response = await apiClient.get<TenantRecipients>(
      apiEndpoints.tenants.recipients(tenantId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const connectAnnouncementSmtp = async (payload: TenantAnnouncementSmtpRequest) => {
  try {
    const response = await apiClient.post<Tenant>(
      apiEndpoints.tenants.announcementSmtp(payload.tenantId),
      payload.payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllTenantAnnouncements = async (
  payload: TenantAnnouncementSearch
): Promise<PaginatedResponse<TenantAnnouncement>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<TenantAnnouncement>>(
      apiEndpoints.tenants.announcements.collection(payload.tenantId),
      { params: payload.params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createAnnouncement = async (
  tenantId: string,
  payload: CreateAnnouncementPayload
): Promise<TenantAnnouncement> => {
  try {
    const response = await apiClient.post<TenantAnnouncement>(
      apiEndpoints.tenants.announcements.collection(tenantId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateAnnouncement = async (
  tenantId: string,
  announcementId: string,
  payload: UpdateAnnouncementPayload
): Promise<TenantAnnouncement> => {
  try {
    const response = await apiClient.put<TenantAnnouncement>(
      apiEndpoints.tenants.announcements.item(tenantId, announcementId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteAnnouncement = async (
  tenantId: string,
  announcementId: string
): Promise<void> => {
  try {
    await apiClient.delete(apiEndpoints.tenants.announcements.item(tenantId, announcementId));
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAnnouncement = async (
  tenantId: string,
  announcementId: string
): Promise<TenantAnnouncement> => {
  try {
    const response = await apiClient.get<TenantAnnouncement>(
      apiEndpoints.tenants.announcements.item(tenantId, announcementId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const launchAnnouncement = async (
  tenantId: string,
  announcementId: string
): Promise<TenantAnnouncement> => {
  try {
    const response = await apiClient.post<TenantAnnouncement>(
      apiEndpoints.tenants.announcements.launch(tenantId, announcementId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const listAnnouncementContacts = async (
  tenantId: string,
  announcementId: string,
  params: PaginationParams = { page: 0, size: 1000, sort: 'createdAt,asc' }
): Promise<PaginatedResponse<TenantAnnouncementContact>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<TenantAnnouncementContact>>(
      apiEndpoints.tenants.announcementContacts.collection(tenantId, announcementId),
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const importLeadContacts = async (
  tenantId: string,
  announcementId: string,
  contactIds: string[]
): Promise<void> => {
  try {
    await apiClient.post(
      apiEndpoints.tenants.announcementContacts.importLeads(tenantId, announcementId),
      { leadContactIds: contactIds }
    );
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const importCrmContacts = async (
  tenantId: string,
  announcementId: string,
  contactIds: string[]
): Promise<void> => {
  try {
    await apiClient.post(
      apiEndpoints.tenants.announcementContacts.importCrm(tenantId, announcementId),
      { tenantContactIds: contactIds }
    );
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const removeAnnouncementContact = async (
  tenantId: string,
  announcementId: string,
  contactId: string
): Promise<void> => {
  try {
    await apiClient.delete(
      apiEndpoints.tenants.announcementContacts.item(tenantId, announcementId, contactId)
    );
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const listTenantContacts = async (
  tenantId: string,
  params: PaginationParams
): Promise<PaginatedResponse<TenantContact>> => {
  try {
    const response = await apiClient.get<PaginatedResponse<TenantContact>>(
      apiEndpoints.tenants.contacts(tenantId),
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getTenantModules = async (tenantId?: string) => {
  try {
    const response = await apiClient.get<TenantModules>(apiEndpoints.tenants.modules, {
      params: { tenantId },
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
