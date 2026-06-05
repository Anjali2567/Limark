import { AxiosError } from 'axios';
import { useCallback, useState } from 'react';
import { toast } from 'sonner';

import { generateLeadEmail, sendLeadEmail } from '@/lib/api/lead-email.api';
import { LeadEmailGenerateRequest, LeadEmailGenerateResponse, LeadEmailPayload, LeadEmailRequest } from '@/types/lead-email.types';
import { LeadContactData } from '@/types/leadSearch.types';
import { Recipient } from '@/types/workspace.types';
import { useMutation, UseMutationResult } from '@tanstack/react-query';

export const useSendLeadEmail = (): UseMutationResult<void, AxiosError, LeadEmailRequest> => {
  return useMutation({
    mutationFn: async (request: LeadEmailRequest) => sendLeadEmail(request),
  });
};

type UseBulkSendLeadEmailsReturn = {
  sendBulkEmails: (
    tenantId: string,
    workspaceId: string,
    contacts: LeadContactData[],
    payload: LeadEmailPayload,
    toRecipients?: Recipient[]
  ) => Promise<void>;
  isSending: boolean;
};

export const useBulkSendLeadEmails = (): UseBulkSendLeadEmailsReturn => {
  const { mutateAsync: sendLeadEmail } = useSendLeadEmail();
  const [isSending, setIsSending] = useState(false);
  const sendBulkEmails = useCallback(
    async (
      tenantId: string,
      workspaceId: string,
      contacts: LeadContactData[],
      payload: LeadEmailPayload
    ) => {
      setIsSending(true);
      const recipients = payload.toRecipients;
      for (let i = 0; i < recipients.length; i++) {
        const recipient = recipients[i];
        const contactId = (contacts[i] ?? contacts[0])?.id;
        if (!contactId) continue;
        await sendLeadEmail({
          tenantId,
          workspaceId,
          contactId,
          payload: { ...payload, toRecipients: [recipient] },
        });
      }
      toast.success(
        `Email sent to ${recipients.length} contact${recipients.length > 1 ? 's' : ''}`
      );
      setIsSending(false);
    },
    [sendLeadEmail]
  );

  return {
    sendBulkEmails,
    isSending,
  };
};


export const useGenerateLeadEmail = (): UseMutationResult<LeadEmailGenerateResponse, AxiosError, LeadEmailGenerateRequest> => {
  return useMutation({
    mutationFn: async (request: LeadEmailGenerateRequest) => generateLeadEmail(request),
  });
};