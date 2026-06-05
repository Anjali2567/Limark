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
  deleteCampaignEmail,
  getCampaignEmails,
  updateCampaignEmail,
} from '@/lib/api/campaign-email.api';
import {
  createEmailSequenceTemplate,
  getEmailSequenceTemplate,
  listEmailSequenceTemplates,
} from '@/lib/api/email-sequence-template.api';
import {
  CreateEmailSequenceTemplateParams,
  EmailSequenceTemplate,
  EmailSequenceTemplateParams,
  GetEmailSequenceTemplateParams,
  ImportEmailSequenceTemplateParams,
} from '@/types/email-sequence-template.types';

export const useListEmailSequenceTemplates = (
  params: EmailSequenceTemplateParams,
  enabled = true
): UseQueryResult<EmailSequenceTemplate[], AxiosError> => {
  return useQuery<EmailSequenceTemplate[], AxiosError>({
    enabled: Boolean(params.tenantId) && enabled,
    queryKey: queryKeys.emailSequenceTemplates.all(params.tenantId),
    queryFn: () => listEmailSequenceTemplates(params),
  });
};

export const useGetEmailSequenceTemplate = (
  params: GetEmailSequenceTemplateParams,
  enabled = false
): UseQueryResult<EmailSequenceTemplate, AxiosError> => {
  return useQuery<EmailSequenceTemplate, AxiosError>({
    enabled: Boolean(params.tenantId && params.templateId) && enabled,
    queryKey: queryKeys.emailSequenceTemplates.byId(params.tenantId, params.templateId),
    queryFn: () => getEmailSequenceTemplate(params),
  });
};

export const useImportEmailSequenceTemplate = (): UseMutationResult<
  void,
  AxiosError,
  ImportEmailSequenceTemplateParams
> => {
  return useMutation<void, AxiosError, ImportEmailSequenceTemplateParams>({
    mutationFn: async ({ tenantId, workspaceId, campaignId, templateId }) => {
      const template = await getEmailSequenceTemplate({ tenantId, templateId });
      if (!template.steps?.length) return;

      const templateSteps = [...template.steps].sort((a, b) => a.stepNumber - b.stepNumber);

      const currentEmails = await getCampaignEmails({ tenantId, workspaceId, campaignId });
      const sortedEmails = [...currentEmails].sort((a, b) => a.stepNumber - b.stepNumber);

      const templateCount = templateSteps.length;
      const currentCount = sortedEmails.length;

      if (currentCount > templateCount) {
        const emailsToDelete = sortedEmails.slice(templateCount).reverse();
        for (const email of emailsToDelete) {
          await deleteCampaignEmail({ tenantId, workspaceId, campaignId, emailId: email.id });
        }
      }

      const updateCount = Math.min(currentCount, templateCount);
      for (let i = 0; i < updateCount; i++) {
        await updateCampaignEmail({
          tenantId,
          workspaceId,
          campaignId,
          emailId: sortedEmails[i].id,
          requestBody: {
            subject: templateSteps[i].subject,
            bodyTemplate: templateSteps[i].bodyTemplate,
            delayDays: templateSteps[i].delayDays,
          },
        });
      }

      if (templateCount > currentCount) {
        for (let i = currentCount; i < templateCount; i++) {
          const newEmail = await addCampaignEmail({ tenantId, workspaceId, campaignId });
          await updateCampaignEmail({
            tenantId,
            workspaceId,
            campaignId,
            emailId: newEmail.id,
            requestBody: {
              subject: templateSteps[i].subject,
              bodyTemplate: templateSteps[i].bodyTemplate,
              delayDays: templateSteps[i].delayDays,
            },
          });
        }
      }
    },
  });
};

export const useCreateEmailSequenceTemplate = (): UseMutationResult<
  EmailSequenceTemplate,
  AxiosError,
  CreateEmailSequenceTemplateParams
> => {
  const queryClient = useQueryClient();
  return useMutation<EmailSequenceTemplate, AxiosError, CreateEmailSequenceTemplateParams>({
    mutationFn: (params) => createEmailSequenceTemplate(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.emailSequenceTemplates.all(variables.tenantId),
      });
    },
  });
};
