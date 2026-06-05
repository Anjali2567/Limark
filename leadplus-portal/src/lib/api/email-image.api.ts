import { EmailImageRequest, EmailImageResponse } from '@/types/email-image.types';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';

export const createEmailImage = async (payload: EmailImageRequest): Promise<EmailImageResponse> => {
  try {
    const formData = new FormData();
    formData.append('file', payload.file);
    formData.append('sourceId', payload.sourceId);
    formData.append('sourceType', payload.sourceType);
    const response = await apiClient.post(apiEndpoints.emailImage.collection, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
