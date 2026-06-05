import apiClient from './client';

import { handleAxiosError } from '../utils/apiErrorHandler';
import {
  ChatMemoryDetails,
  LeadAgentConversationRequest,
  LeadAgentConversations,
  ConversationsParams,
  Message,
  MessageRequest,
  UpdateTargetCriteriaRequest,
} from '@/types/campaign-agent.types';
import { apiEndpoints } from '@/config/endpoints';
import { BasicCampaign, Campaign, TargetingCriteriaResponse } from '@/types/campaign.types';
import { PaginatedResponse } from '@/types/paginated-params';

export const sendMessage = async ({
  tenantId,
  workspaceId,
  conversationId,
  request,
}: MessageRequest): Promise<Message> => {
  try {
    const response = await apiClient.post(
      apiEndpoints.campaigns.agent.collection(tenantId, workspaceId),
      { conversationId, request }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getCampaignDetails = async ({
  tenantId,
  workspaceId,
  chatMemoryId,
}: ChatMemoryDetails): Promise<Campaign> => {
  try {
    const response = await apiClient.get(
      apiEndpoints.campaigns.agent.details(tenantId, workspaceId, chatMemoryId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateTargetCriteria = async ({
  tenantId,
  workspaceId,
  chatMemoryId,
  payload,
}: UpdateTargetCriteriaRequest): Promise<TargetingCriteriaResponse> => {
  try {
    const response = await apiClient.put(
      apiEndpoints.campaigns.agent.updateTargetCriteria(tenantId, workspaceId, chatMemoryId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const proceedCampaign = async ({
  tenantId,
  workspaceId,
  chatMemoryId,
}: ChatMemoryDetails): Promise<BasicCampaign> => {
  try {
    const response = await apiClient.post(
      apiEndpoints.campaigns.agent.proceedCampaign(tenantId, workspaceId, chatMemoryId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getConversations = async ({
  tenantId,
  workspaceId,
  ...params
}: ConversationsParams): Promise<PaginatedResponse<LeadAgentConversations>> => {
  try {
    const response = await apiClient.get(
      apiEndpoints.campaigns.agent.conversations.collection(tenantId, workspaceId),
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getConversationById = async ({
  tenantId,
  workspaceId,
  conversationId,
}: LeadAgentConversationRequest): Promise<LeadAgentConversations[]> => {
  try {
    const response = await apiClient.get(
      apiEndpoints.campaigns.agent.conversations.item(tenantId, workspaceId, conversationId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
