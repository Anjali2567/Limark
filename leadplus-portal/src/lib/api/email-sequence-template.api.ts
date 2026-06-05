import apiClient from './client';

import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '@/lib/utils/apiErrorHandler';
import {
  CreateEmailSequenceTemplateParams,
  EmailSequenceTemplate,
  GetEmailSequenceTemplateParams,
  EmailSequenceTemplateParams,
} from '@/types/email-sequence-template.types';

export const listEmailSequenceTemplates = async ({ tenantId }: EmailSequenceTemplateParams) => {
  try {
    const response = await apiClient.get<EmailSequenceTemplate[]>(
      apiEndpoints.tenants.emailSequenceTemplates.collection(tenantId)
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const getEmailSequenceTemplate = async ({
  tenantId,
  templateId,
}: GetEmailSequenceTemplateParams) => {
  try {
    const response = await apiClient.get<EmailSequenceTemplate>(
      apiEndpoints.tenants.emailSequenceTemplates.item(tenantId, templateId)
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const createEmailSequenceTemplate = async ({
  tenantId,
  requestBody,
}: CreateEmailSequenceTemplateParams) => {
  try {
    const response = await apiClient.post<EmailSequenceTemplate>(
      apiEndpoints.tenants.emailSequenceTemplates.collection(tenantId),
      requestBody
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};
