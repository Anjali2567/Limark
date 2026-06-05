import { AxiosError } from 'axios';

import {
  createFeedback,
  getAllFeedbacks,
  getFeedbackById,
  sendFeedbackReply,
  updateFeedbackStatusToReviewed,
} from '@/lib/api/user-feedback.api';
import {
  Feedback,
  FeedbackReplyRequest,
  FeedbackRequest,
  FeedbackSearchParams,
} from '@/types/user-feedback.types';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { PaginatedResponse } from '@/types/paginated-params';
import { queryKeys } from '@/config/queryKeys';

export const useCreateFeedback = (): UseMutationResult<Feedback, AxiosError, FeedbackRequest> => {
  return useMutation({
    mutationFn: createFeedback,
  });
};

export const useGetAllFeedbacks = (
  params: FeedbackSearchParams
): UseQueryResult<PaginatedResponse<Feedback>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.feedbacks.search(params),
    queryFn: () => getAllFeedbacks(params),
  });
};

export const useGetFeedbackById = (feedbackId: string): UseQueryResult<Feedback, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.feedbacks.byId(feedbackId),
    queryFn: () => getFeedbackById(feedbackId),
  });
};

export const useUpdateFeedbackStatusToReviewed = (
  feedbackId: string
): UseMutationResult<Feedback, AxiosError> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => updateFeedbackStatusToReviewed(feedbackId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.feedbacks.all,
      });
    },
  });
};

export const useSendFeedbackReply = (): UseMutationResult<
  Feedback,
  AxiosError,
  FeedbackReplyRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ feedbackId, payload }) => sendFeedbackReply(feedbackId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.feedbacks.all,
      });
    },
  });
};
