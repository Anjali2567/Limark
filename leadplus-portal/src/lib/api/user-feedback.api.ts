import { apiEndpoints } from '@/config/endpoints';
import apiClient from './client';
import {
  Feedback,
  FeedbackReplyPayload,
  FeedbackRequest,
  FeedbackSearchParams,
} from '@/types/user-feedback.types';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { PaginatedResponse } from '@/types/paginated-params';

export const createFeedback = async (request: FeedbackRequest): Promise<Feedback> => {
  const { params, payload } = request;
  try {
    const response = await apiClient.post(
      apiEndpoints.feedbacks.collection(params.tenantId, params.workspaceId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllFeedbacks = async (
  params: FeedbackSearchParams
): Promise<PaginatedResponse<Feedback>> => {
  try {
    const response = await apiClient.get(apiEndpoints.admin.feedbacks.collection, {
      params,
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getFeedbackById = async (feedbackId: string): Promise<Feedback> => {
  try {
    const response = await apiClient.get(apiEndpoints.admin.feedbacks.item(feedbackId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateFeedbackStatusToReviewed = async (feedbackId: string): Promise<Feedback> => {
  try {
    const response = await apiClient.put(apiEndpoints.admin.feedbacks.reviewed(feedbackId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const sendFeedbackReply = async (
  feedbackId: string,
  payload: FeedbackReplyPayload
): Promise<Feedback> => {
  try {
    const response = await apiClient.post(apiEndpoints.admin.feedbacks.reply(feedbackId), payload);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
