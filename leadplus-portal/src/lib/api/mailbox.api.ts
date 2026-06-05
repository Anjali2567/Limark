import {
  Mailbox,
  MailboxDisconnectParams,
  OutlookConnectParams,
  GmailConnectParams,
  SesConnectParams,
  SmtpConnectParams,
} from '@/types/mailbox.types';
import apiClient from './client';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { apiEndpoints } from '@/config/endpoints';
import { BasicParams } from '@/types/user.types';

export const addOutlookAuthCode = async ({
  tenantId,
  workspaceId,
  code,
  redirectUri,
}: OutlookConnectParams): Promise<void> => {
  try {
    await apiClient.post(apiEndpoints.mailboxes.microsoft(tenantId, workspaceId), {
      code,
      redirectUri,
    });
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const addGmailAuthCode = async ({
  tenantId,
  workspaceId,
  code,
  redirectUri,
}: GmailConnectParams): Promise<void> => {
  try {
    await apiClient.post(apiEndpoints.mailboxes.gmail(tenantId, workspaceId), {
      code,
      redirectUri,
    });
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const connectSmtp = async ({
  tenantId,
  workspaceId,
  email,
  appPassword,
}: SmtpConnectParams): Promise<void> => {
  try {
    await apiClient.post(apiEndpoints.mailboxes.smtp(tenantId, workspaceId), {
      email,
      appPassword,
    });
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const connectAwsSes = async ({
  tenantId,
  workspaceId,
  email,
}: SesConnectParams): Promise<Mailbox> => {
  try {
    const response = await apiClient.post(apiEndpoints.mailboxes.awsSes(tenantId, workspaceId), {
      email,
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getConnectedMailboxes = async ({
  tenantId,
  workspaceId,
}: BasicParams): Promise<Mailbox[]> => {
  try {
    const response = await apiClient.get(apiEndpoints.mailboxes.collection(tenantId, workspaceId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const disconnectMailbox = async ({
  tenantId,
  workspaceId,
  mailboxId,
}: MailboxDisconnectParams): Promise<void> => {
  try {
    await apiClient.delete(apiEndpoints.mailboxes.item(tenantId, workspaceId, mailboxId));
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAuthorizedMailbox = async ({
  tenantId,
  workspaceId,
}: BasicParams): Promise<Mailbox[]> => {
  try {
    const response = await apiClient.get(apiEndpoints.mailboxes.authorized(tenantId, workspaceId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
