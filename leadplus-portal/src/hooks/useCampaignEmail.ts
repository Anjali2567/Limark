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
  addCampaignEmail,
  configureCampaignEmails,
  deleteCampaignEmail,
  generateCampaignEmail,
  getCampaignEmails,
  updateCampaignEmail,
} from '@/lib/api/campaign-email.api';
import {
  CampaignEmail,
  CampaignEmailParams,
  DeleteCampaignEmailParams,
  GenerateCampaignEmailParams,
  GenerateCampaignEmailResponse,
  UpdateCampaignEmailParams,
} from '@/types/campaign-email.types';

const emailsQueryKey = (params: { tenantId: string; workspaceId: string; campaignId: string }) =>
  queryKeys.campaigns.emailsById({
    tenantId: String(params.tenantId),
    workspaceId: String(params.workspaceId),
    campaignId: String(params.campaignId),
  });

export const useGetCampaignEmails = ({
  tenantId,
  workspaceId,
  campaignId,
}: CampaignEmailParams): UseQueryResult<CampaignEmail[], AxiosError> => {
  return useQuery<CampaignEmail[], AxiosError>({
    enabled: Boolean(tenantId && workspaceId && campaignId),
    queryKey: emailsQueryKey({ tenantId, workspaceId, campaignId }),
    queryFn: () => getCampaignEmails({ tenantId, workspaceId, campaignId }),
  });
};

export const useConfigureCampaignEmails = (): UseMutationResult<
  void,
  AxiosError,
  CampaignEmailParams
> => {
  return useMutation<void, AxiosError, CampaignEmailParams>({
    mutationFn: (params) => configureCampaignEmails(params),
  });
};

export const useUpdateCampaignEmail = (): UseMutationResult<
  CampaignEmail,
  AxiosError,
  UpdateCampaignEmailParams
> => {
  const queryClient = useQueryClient();
  return useMutation<CampaignEmail, AxiosError, UpdateCampaignEmailParams>({
    mutationFn: (params) => updateCampaignEmail(params),
    onSuccess: (data, variables) => {
      queryClient.setQueryData(
        emailsQueryKey(variables),
        (prevData: CampaignEmail[] | undefined) => {
          if (!prevData) return prevData;
          return prevData.map((email) => (String(email.id) === String(data.id) ? data : email));
        }
      );
    },
  });
};

export const useAddCampaignEmail = (): UseMutationResult<
  CampaignEmail,
  AxiosError,
  CampaignEmailParams
> => {
  const queryClient = useQueryClient();
  return useMutation<CampaignEmail, AxiosError, CampaignEmailParams>({
    mutationFn: (params) => addCampaignEmail(params),
    onSuccess: (data, variables) => {
      queryClient.setQueryData(
        emailsQueryKey(variables),
        (prevData: CampaignEmail[] | undefined) => {
          if (!prevData) return prevData;
          return [...prevData, data];
        }
      );
    },
  });
};

export const useDeleteCampaignEmail = (): UseMutationResult<
  void,
  AxiosError,
  DeleteCampaignEmailParams
> => {
  const queryClient = useQueryClient();
  return useMutation<void, AxiosError, DeleteCampaignEmailParams>({
    mutationFn: (params) => deleteCampaignEmail(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: emailsQueryKey(variables),
      });
    },
  });
};

export const useGenerateCampaignEmail = (): UseMutationResult<
  GenerateCampaignEmailResponse[],
  AxiosError,
  GenerateCampaignEmailParams
> => {
  return useMutation<GenerateCampaignEmailResponse[], AxiosError, GenerateCampaignEmailParams>({
    mutationFn: (params) => generateCampaignEmail(params),
  });
};
