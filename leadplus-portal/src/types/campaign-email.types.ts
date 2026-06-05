import { CampaignEmailStatus } from "@/constants/Campaign";
import { BasicParams } from "./user.types";

export type CampaignEmail = {
  id: string;
  campaignId: string;
  stepNumber: number;
  subject: string;
  bodyTemplate: string;
  delayDays: number;
  attachmentIds: string[];
  status: CampaignEmailStatus;
};

export type CampaignEmailParams = {
  campaignId: string;
} & BasicParams;

export type UpdateCampaignEmailRequestBody = {
  subject: string;
  bodyTemplate: string;
  delayDays: number;
  attachmentIds?: string[];
};

export type UpdateCampaignEmailParams = CampaignEmailParams & {
  emailId: string;
  requestBody: UpdateCampaignEmailRequestBody;
};

export type DeleteCampaignEmailParams = CampaignEmailParams & {
  emailId: string;
};

export type GenerateCampaignEmailParams = CampaignEmailParams & {
  requestBody: {
    userPrompt: string;
  };
};

export type GenerateCampaignEmailResponse = {
  stepNumber: number;
  subject: string;
  body: string;
};
