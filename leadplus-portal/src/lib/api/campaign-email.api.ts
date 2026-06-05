import {
  CampaignEmail,
  CampaignEmailParams,
  DeleteCampaignEmailParams,
  GenerateCampaignEmailParams,
  GenerateCampaignEmailResponse,
  UpdateCampaignEmailParams,
} from "@/types/campaign-email.types";

import { handleAxiosError } from "@/lib/utils/apiErrorHandler";
import apiClient from "./client";
import { apiEndpoints } from "@/config/endpoints";

export const configureCampaignEmails = async ({
  tenantId,
  workspaceId,
  campaignId,
}: CampaignEmailParams) => {
  try {
    const response = await apiClient.post<void>(
      apiEndpoints.campaigns.emails.configure(tenantId, workspaceId, campaignId)
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const updateCampaignEmail = async ({
  tenantId,
  workspaceId,
  campaignId,
  emailId,
  requestBody,
}: UpdateCampaignEmailParams) => {
  try {
    const response = await apiClient.put<CampaignEmail>(
      apiEndpoints.campaigns.emails.item(
        tenantId,
        workspaceId,
        campaignId,
        emailId
      ),
      requestBody
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const getCampaignEmails = async ({
  tenantId,
  workspaceId,
  campaignId,
}: CampaignEmailParams) => {
  try {
    const response = await apiClient.get<CampaignEmail[]>(
      apiEndpoints.campaigns.emails.collection(
        tenantId,
        workspaceId,
        campaignId
      )
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const addCampaignEmail = async ({
  tenantId,
  workspaceId,
  campaignId,
}: CampaignEmailParams) => {
  try {
    const response = await apiClient.post<CampaignEmail>(
      apiEndpoints.campaigns.emails.collection(
        tenantId,
        workspaceId,
        campaignId
      )
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const deleteCampaignEmail = async ({
  tenantId,
  workspaceId,
  campaignId,
  emailId,
}: DeleteCampaignEmailParams) => {
  try {
    await apiClient.delete<CampaignEmail>(
      apiEndpoints.campaigns.emails.item(
        tenantId,
        workspaceId,
        campaignId,
        emailId
      )
    );
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const generateCampaignEmail = async ({
  tenantId,
  workspaceId,
  campaignId,
  requestBody,
}: GenerateCampaignEmailParams) => {
  try {
    const response = await apiClient.post<GenerateCampaignEmailResponse[]>(
      apiEndpoints.campaigns.emails.generate(tenantId, workspaceId, campaignId),
      requestBody
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};
