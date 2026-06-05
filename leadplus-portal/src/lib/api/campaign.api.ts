import {
  AddContactsToCampaignRequest,
  AssignMailboxRequest,
  BasicCampaign,
  Campaign,
  CampaignAnalytics,
  CampaignChatResponse,
  CampaignListResponse,
  CampaignRecipientsRequest,
  CampaignRequest,
  ChatReply,
  CreateCampaignForSearchRequest,
  UpdateCampaignDetailsRequest,
  UpdateCampaignTargetRequest,
} from '@/types/campaign.types';
import apiClient from './client';
import { handleAxiosError } from '@/lib/utils/apiErrorHandler';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import { apiEndpoints } from '@/config/endpoints';
import { BasicParams } from '@/types/user.types';

export const getAgentReply = async ({
  tenantId,
  workspaceId,
  requestBody,
}: ChatReply): Promise<CampaignChatResponse> => {
  try {
    const response = await apiClient.post<CampaignChatResponse>(
      apiEndpoints.campaigns.generator(tenantId, workspaceId),
      requestBody
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const getCampaignById = async ({ tenantId, workspaceId, campaignId }: CampaignRequest) => {
  try {
    const response = await apiClient.get<Campaign>(
      apiEndpoints.campaigns.item(tenantId, workspaceId, campaignId)
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const getAllCampaigns = async ({
  tenantId,
  workspaceId,
  params,
}: {
  params: PaginationParams;
} & BasicParams) => {
  try {
    const response = await apiClient.get<PaginatedResponse<CampaignListResponse>>(
      apiEndpoints.campaigns.collection(tenantId, workspaceId),
      { params }
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const updateCampaignTargets = async ({
  tenantId,
  campaignId,
  workspaceId,
  requestBody,
}: UpdateCampaignTargetRequest) => {
  try {
    const response = await apiClient.put<void>(
      apiEndpoints.campaigns.updateCampaignTargets(tenantId, workspaceId, campaignId),
      requestBody
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const launchCampaign = async ({ tenantId, campaignId, workspaceId }: CampaignRequest) => {
  try {
    await apiClient.post<void>(
      apiEndpoints.campaigns.controls.launch(tenantId, workspaceId, campaignId)
    );
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const pauseCampaign = async ({ tenantId, campaignId, workspaceId }: CampaignRequest) => {
  try {
    await apiClient.put<void>(
      apiEndpoints.campaigns.controls.pause(tenantId, workspaceId, campaignId)
    );
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const resumeCampaign = async ({ tenantId, campaignId, workspaceId }: CampaignRequest) => {
  try {
    await apiClient.put<void>(
      apiEndpoints.campaigns.controls.resume(tenantId, workspaceId, campaignId)
    );
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const getCampaignAnalytics = async ({
  tenantId,
  workspaceId,
  campaignId,
}: CampaignRequest) => {
  try {
    const response = await apiClient.get<CampaignAnalytics>(
      apiEndpoints.campaigns.analytics.campaign(tenantId, workspaceId, campaignId)
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const getWorkspaceCampaignAnalytics = async ({ tenantId, workspaceId }: BasicParams) => {
  try {
    const response = await apiClient.get<CampaignAnalytics>(
      apiEndpoints.campaigns.analytics.workspace(tenantId, workspaceId)
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const updateCampaignDetails = async ({
  tenantId,
  workspaceId,
  campaignId,
  requestBody,
}: UpdateCampaignDetailsRequest) => {
  try {
    const response = await apiClient.put<BasicCampaign>(
      apiEndpoints.campaigns.item(tenantId, workspaceId, campaignId),
      requestBody
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const getCampaignBasicDetails = async ({
  tenantId,
  workspaceId,
  campaignId,
}: CampaignRequest): Promise<BasicCampaign> => {
  try {
    const response = await apiClient.get<BasicCampaign>(
      apiEndpoints.campaigns.basicDetails(tenantId, workspaceId, campaignId)
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const updateCampaignRecipients = async ({
  tenantId,
  workspaceId,
  campaignId,
  payload,
}: CampaignRecipientsRequest): Promise<BasicCampaign> => {
  try {
    const response = await apiClient.put<BasicCampaign>(
      apiEndpoints.campaigns.recipients(tenantId, workspaceId, campaignId),
      payload
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const updateSendingMailboxForCampaign = async ({
  tenantId,
  workspaceId,
  campaignId,
  mailboxId,
}: AssignMailboxRequest): Promise<BasicCampaign> => {
  try {
    const response = await apiClient.put<BasicCampaign>(
      apiEndpoints.campaigns.assignMailbox(tenantId, workspaceId, campaignId, mailboxId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const addContactsToCampaign = async ({
  tenantId,
  workspaceId,
  campaignId,
  contactIds,
}: AddContactsToCampaignRequest): Promise<void> => {
  try {
    await apiClient.post<void>(
      apiEndpoints.campaigns.addContacts(tenantId, workspaceId, campaignId),
      { contactIds }
    );
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const createCampaignForSearchResult = async ({
  tenantId,
  workspaceId,
  requestBody,
}: CreateCampaignForSearchRequest): Promise<BasicCampaign> => {
  try {
    const response = await apiClient.post<BasicCampaign>(
      apiEndpoints.campaigns.createForSearch(tenantId, workspaceId),
      requestBody
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

