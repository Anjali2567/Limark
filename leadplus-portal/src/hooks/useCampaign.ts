import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import {
  addContactsToCampaign,
  createCampaignForSearchResult,
  getAgentReply,
  getAllCampaigns,
  getCampaignAnalytics,
  getCampaignBasicDetails,
  getCampaignById,
  getWorkspaceCampaignAnalytics,
  launchCampaign,
  pauseCampaign,
  resumeCampaign,
  updateCampaignDetails,
  updateCampaignRecipients,
  updateCampaignTargets,
  updateSendingMailboxForCampaign,
} from '@/lib/api/campaign.api';
import {
  AddContactsToCampaignRequest,
  AssignMailboxRequest,
  BasicCampaign,
  Campaign,
  CampaignAnalytics,
  CampaignChatResponse,
  CampaignListResponse,
  CampaignRecipientsRequest,
  CampaignRequest,
  CampaignSearchParams,
  ChatRequest,
  CreateCampaignForSearchRequest,
  UpdateCampaignDetailsRequest,
  UpdateCampaignTargetRequest,
} from '@/types/campaign.types';
import { PaginatedResponse } from '@/types/paginated-params';
import { BasicParams } from '@/types/user.types';

export const useLaunchCampaign = (): UseMutationResult<void, AxiosError, CampaignRequest> => {
  const queryClient = useQueryClient();
  return useMutation<void, AxiosError, CampaignRequest>({
    mutationFn: async (campaignRequest: CampaignRequest) => launchCampaign(campaignRequest),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const usePauseCampaign = (): UseMutationResult<void, AxiosError, CampaignRequest> => {
  const queryClient = useQueryClient();
  return useMutation<void, AxiosError, CampaignRequest>({
    mutationFn: async (campaignRequest: CampaignRequest) => pauseCampaign(campaignRequest),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useResumeCampaign = (): UseMutationResult<void, AxiosError, CampaignRequest> => {
  const queryClient = useQueryClient();
  return useMutation<void, AxiosError, CampaignRequest>({
    mutationFn: async (params) => resumeCampaign(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useGetChatAgentReply = (): UseMutationResult<
  CampaignChatResponse,
  AxiosError,
  ChatRequest
> => {
  const queryClient = useQueryClient();
  return useMutation<CampaignChatResponse, AxiosError, ChatRequest>({
    mutationFn: async (chatRequest: ChatRequest) => getAgentReply(chatRequest),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useGetCampaignById = ({
  tenantId,
  workspaceId,
  campaignId,
}: CampaignRequest): UseQueryResult<Campaign, AxiosError> => {
  return useQuery({
    enabled: !!tenantId && !!workspaceId && !!campaignId,
    refetchInterval: 60000,
    queryKey: queryKeys.campaigns.byId({
      tenantId,
      workspaceId,
      campaignId,
    }),
    queryFn: async () => {
      const data = await getCampaignById({ tenantId, workspaceId, campaignId });

      let participatingContactsCount = 0;

      const participatingCompanies = (data.campaignCompanies ?? []).filter((company) => {
        const count = (company.campaignContacts ?? []).filter((c) => c.participating).length;
        if (count > 0) {
          participatingContactsCount += count;
          return true;
        }
        return false;
      });

      return {
        ...data,
        participatingCompanies,
        participatingCompaniesCount: participatingCompanies.length,
        participatingContactsCount,
      };
    },
  });
};

export const useGetAllCampaigns = (
  { tenantId, workspaceId, params }: CampaignSearchParams,
  options?: { enabled?: boolean }
): UseQueryResult<PaginatedResponse<CampaignListResponse>, AxiosError> => {
  return useQuery({
    enabled: (options?.enabled ?? true) && !!tenantId && !!workspaceId,
    queryKey: queryKeys.campaigns.search({
      tenantId,
      workspaceId,
      query: params,
    }),
    queryFn: async () => {
      return getAllCampaigns({ tenantId, workspaceId, params });
    },
  });
};

export const useAddContactsToCampaign = (): UseMutationResult<
  void,
  AxiosError,
  AddContactsToCampaignRequest
> => {
  const queryClient = useQueryClient();
  return useMutation<void, AxiosError, AddContactsToCampaignRequest>({
    mutationFn: addContactsToCampaign,
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.byId(variables),
      });
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useUpdateCampaignTargets = (): UseMutationResult<
  void,
  AxiosError,
  UpdateCampaignTargetRequest
> => {
  const queryClient = useQueryClient();
  return useMutation<void, AxiosError, UpdateCampaignTargetRequest>({
    mutationFn: async (updateCampaignTargetRequest: UpdateCampaignTargetRequest) =>
      updateCampaignTargets(updateCampaignTargetRequest),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.byId(variables),
      });
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useGetCampaignAnalytics = ({
  tenantId,
  workspaceId,
  campaignId,
}: CampaignRequest): UseQueryResult<CampaignAnalytics, AxiosError> => {
  return useQuery({
    enabled: !!workspaceId && !!campaignId,
    refetchInterval: 60000,
    queryKey: queryKeys.campaigns.analyticsById({
      tenantId,
      workspaceId,
      campaignId,
    }),
    queryFn: () => getCampaignAnalytics({ tenantId, workspaceId, campaignId }),
  });
};

export const useGetWorkspaceCampaignAnalytics = ({
  tenantId,
  workspaceId,
}: BasicParams): UseQueryResult<CampaignAnalytics, AxiosError> => {
  return useQuery({
    enabled: !!tenantId && !!workspaceId,
    queryKey: queryKeys.campaigns.analytics({ workspaceId, tenantId }),
    queryFn: () => getWorkspaceCampaignAnalytics({ workspaceId, tenantId }),
  });
};

export const useUpdateCampaignDetails = (): UseMutationResult<
  BasicCampaign,
  AxiosError,
  UpdateCampaignDetailsRequest
> => {
  const queryClient = useQueryClient();
  return useMutation<BasicCampaign, AxiosError, UpdateCampaignDetailsRequest>({
    mutationFn: async (updateCampaignDetailsRequest: UpdateCampaignDetailsRequest) =>
      updateCampaignDetails(updateCampaignDetailsRequest),
    onSuccess: (data, variables) => {
      queryClient.setQueryData<Campaign>(
        queryKeys.campaigns.byId(variables),
        (oldData: Campaign | undefined) => {
          if (!oldData) return oldData;
          return {
            ...oldData,
            ...data,
          };
        }
      );
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useGetCampaignBasicDetails = (
  params: CampaignRequest,
  isEnabled: boolean = true
): UseQueryResult<BasicCampaign, AxiosError> => {
  return useQuery({
    enabled: !!params.tenantId && !!params.workspaceId && !!params.campaignId && isEnabled,
    queryKey: queryKeys.campaigns.basicDetails(params),
    queryFn: () => getCampaignBasicDetails(params),
  });
};

export const useUpdateCampaignRecipients = (): UseMutationResult<
  BasicCampaign,
  AxiosError,
  CampaignRecipientsRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (updateCampaignRecipientsRequest: CampaignRecipientsRequest) =>
      updateCampaignRecipients(updateCampaignRecipientsRequest),
    onSuccess: (data, variables) => {
      queryClient.setQueryData<BasicCampaign>(
        queryKeys.campaigns.basicDetails(variables),
        (oldData: BasicCampaign | undefined) => {
          if (!oldData) return oldData;
          return {
            ...oldData,
            ...data,
          };
        }
      );
    },
  });
};

export const useAssignCampaignMailbox = (): UseMutationResult<
  BasicCampaign,
  AxiosError,
  AssignMailboxRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateSendingMailboxForCampaign,
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useCreateCampaignForSearchResult = (): UseMutationResult<
  BasicCampaign,
  AxiosError,
  CreateCampaignForSearchRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: createCampaignForSearchResult,
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.campaigns.all(variables),
        refetchType: 'all',
      });
    },
  });
};
