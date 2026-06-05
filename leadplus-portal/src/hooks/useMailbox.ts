'use client';

import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { useEffect, useState } from 'react';

import { queryKeys } from '@/config/queryKeys';
import { MailboxConnectionStatus, MailboxType } from '@/constants/mailbox.constants';
import {
  addOutlookAuthCode,
  addGmailAuthCode,
  connectSmtp,
  connectAwsSes,
  disconnectMailbox,
  getConnectedMailboxes,
  getAuthorizedMailbox,
} from '@/lib/api/mailbox.api';
import {
  Mailbox,
  MailboxDisconnectParams,
  SmtpConnectParams,
  OutlookConnectParams,
  GmailConnectParams,
} from '@/types/mailbox.types';
import { BasicParams } from '@/types/user.types';

export const useAddOutlookAuthCode = (): UseMutationResult<
  void,
  AxiosError,
  OutlookConnectParams
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => addOutlookAuthCode(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.mailboxes.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useAddGmailAuthCode = (): UseMutationResult<void, AxiosError, GmailConnectParams> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => addGmailAuthCode(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.mailboxes.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useConnectSmtp = (): UseMutationResult<void, AxiosError, SmtpConnectParams> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => connectSmtp(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.mailboxes.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useConnectAwsSes = (params: {
  tenantId: string;
  workspaceId: string;
}): UseMutationResult<Mailbox, AxiosError, string> => {
  const queryClient = useQueryClient();
  const [shouldPoll, setShouldPoll] = useState(false);
  const [email, setEmail] = useState<string | null>(null);

  const mutation = useMutation<Mailbox, AxiosError, string>({
    mutationFn: (email: string) => {
      setEmail(email);
      return connectAwsSes({ ...params, email });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.mailboxes.all(params),
        exact: true,
      });
      setShouldPoll(true);
    },
  });

  const verificationQuery = useQuery({
    queryKey: [...queryKeys.mailboxes.sesCheck(params), email],
    queryFn: () => connectAwsSes({ ...params, email: email! }),
    enabled: shouldPoll && !!email,
    refetchInterval: (query) => {
      const data = query.state.data;
      const verified =
        data?.type === MailboxType.AMAZON_SES && data?.status === MailboxConnectionStatus.VERIFIED;

      return verified ? false : 10_000;
    },
    refetchIntervalInBackground: false,
    refetchOnWindowFocus: false,
  });

  useEffect(() => {
    if (
      verificationQuery.data?.type === MailboxType.AMAZON_SES &&
      verificationQuery.data?.status === MailboxConnectionStatus.VERIFIED
    ) {
      const handler = setTimeout(() => {
        setShouldPoll(false);
        setEmail(null);
        queryClient.invalidateQueries({
          queryKey: queryKeys.mailboxes.all(params),
          exact: true,
        });
      }, 0);

      return () => clearTimeout(handler);
    }
  }, [verificationQuery.data, queryClient, params]);

  return mutation;
};

export const useGetConnectedMailboxes = ({
  tenantId,
  workspaceId,
}: BasicParams): UseQueryResult<Mailbox[], AxiosError> => {
  return useQuery({
    enabled: !!tenantId && !!workspaceId,
    queryKey: queryKeys.mailboxes.all({ tenantId, workspaceId }),
    queryFn: () => getConnectedMailboxes({ tenantId, workspaceId }),
  });
};

export const useDisconnectMailbox = (): UseMutationResult<
  void,
  AxiosError,
  MailboxDisconnectParams
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => disconnectMailbox(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.mailboxes.all(variables),
        refetchType: 'all',
      });
      queryClient.removeQueries({
        queryKey: queryKeys.mailboxes.sesCheck(variables),
      });
    },
  });
};

export const useGetAuthorizedMailboxes = ({
  tenantId,
  workspaceId,
}: BasicParams): UseQueryResult<Mailbox[], AxiosError> => {
  return useQuery({
    enabled: !!tenantId && !!workspaceId,
    queryKey: queryKeys.mailboxes.authorized({ tenantId, workspaceId }),
    queryFn: () => getAuthorizedMailbox({ tenantId, workspaceId }),
  });
};
