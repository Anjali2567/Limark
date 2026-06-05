import { ChatRequest, ChatResponse } from '@/types/chat.types';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';

export const sendChatMessage = async (payload: ChatRequest) => {
  try {
    const response = await apiClient.post<ChatResponse>(apiEndpoints.customer.chat, payload);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
