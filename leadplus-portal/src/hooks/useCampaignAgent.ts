import { AxiosError } from 'axios';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useInfiniteQuery,
  useQueryClient,
  UseQueryResult,
  UseInfiniteQueryResult,
  InfiniteData,
} from '@tanstack/react-query';

import {
  getCampaignDetails,
  getConversationById,
  getConversations,
  proceedCampaign,
  sendMessage,
  updateTargetCriteria,
} from '@/lib/api/campaign-agent.api';
import {
  ChatMemoryDetails,
  ConversationsParams,
  LeadAgentConversationRequest,
  LeadAgentConversations,
  Message,
  MessageRequest,
  UpdateTargetCriteriaRequest,
} from '@/types/campaign-agent.types';
import { BasicCampaign, Campaign, TargetingCriteriaResponse } from '@/types/campaign.types';
import { queryKeys } from '@/config/queryKeys';
import { PaginatedResponse } from '@/types/paginated-params';

export const useSendMessage = (): UseMutationResult<Message, AxiosError, MessageRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload) => sendMessage(payload),
    onSuccess: (data, variables) => {
      if (!variables.conversationId) {
        queryClient.invalidateQueries({
          queryKey: queryKeys.campaigns.agent.conversations({
            tenantId: variables.tenantId,
            workspaceId: variables.workspaceId,
          }),
        });
      }

      queryClient.setQueryData(
        queryKeys.campaigns.agent.conversationsById({
          tenantId: variables.tenantId,
          workspaceId: variables.workspaceId,
          conversationId: data.conversationId,
        }),
        (old: LeadAgentConversations[] = []) => [
          ...old,
          {
            request: variables.request,
            response: data.response,
            createdAt: new Date().toISOString(),
            targetingCriteriaCount: data.campaignState,
            searchPerformed: data.searchPerformed,
          },
        ]
      );

      if (data.searchPerformed && data.campaignId) {
        queryClient.invalidateQueries({
          queryKey: queryKeys.campaigns.agent.details({
            tenantId: variables.tenantId,
            workspaceId: variables.workspaceId,
            chatMemoryId: String(data.campaignId),
          }),
          refetchType: 'all',
        });
      }
    },
  });
};

export const useGetCampaignDetails = (
  params: ChatMemoryDetails,
  options?: { enabled?: boolean }
): UseQueryResult<Campaign, AxiosError> => {
  return useQuery({
    enabled:
      (options?.enabled ?? true) &&
      !!params.tenantId &&
      !!params.workspaceId &&
      !!params.chatMemoryId,
    queryKey: queryKeys.campaigns.agent.details(params),
    queryFn: () => getCampaignDetails(params),
  });
};

export const useProceedCampaign = (): UseMutationResult<
  BasicCampaign,
  AxiosError,
  ChatMemoryDetails
> => {
  return useMutation({
    mutationFn: (payload) => proceedCampaign(payload),
  });
};

export const useUpdateTargetCriteria = (): UseMutationResult<
  TargetingCriteriaResponse,
  AxiosError,
  UpdateTargetCriteriaRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload) => updateTargetCriteria(payload),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.agent.details({
          tenantId: variables.tenantId,
          workspaceId: variables.workspaceId,
          chatMemoryId: variables.chatMemoryId,
        }),
      });
    },
  });
};

export const useGetConversations = (
  params: ConversationsParams
): UseInfiniteQueryResult<InfiniteData<PaginatedResponse<LeadAgentConversations>>, AxiosError> => {
  return useInfiniteQuery({
    queryKey: queryKeys.campaigns.agent.conversations(params),
    queryFn: ({ pageParam = 0 }) =>
      getConversations({
        ...params,
        page: pageParam,
      }),
    getNextPageParam: (lastPage) => {
      const { number, totalPages } = lastPage.page;
      return number + 1 < totalPages ? number + 1 : undefined;
    },
    initialPageParam: 0,
    enabled: !!params.tenantId && !!params.workspaceId,
  });
};

export const useGetConversationsById = (
  params: LeadAgentConversationRequest,
  options?: { enabled?: boolean }
): UseQueryResult<LeadAgentConversations[], AxiosError> => {
  return useQuery({
    enabled:
      (options?.enabled !== undefined ? options.enabled : true) &&
      !!params.tenantId &&
      !!params.workspaceId &&
      !!params.conversationId,
    queryKey: queryKeys.campaigns.agent.conversationsById(params),
    queryFn: () => getConversationById(params),
  });
};
