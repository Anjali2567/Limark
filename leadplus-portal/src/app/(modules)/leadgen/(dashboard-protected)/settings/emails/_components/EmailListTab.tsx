'use client';

import { AxiosError } from 'axios';
import { useEffect, useMemo } from 'react';
import { toast } from 'sonner';
import { z } from 'zod';

import { InfoCard } from '@/components/InfoCard';
import { EmailRecipientsForm } from '@/components/emailRecipients/EmailRecipientsForm';
import { useAuth } from '@/context/AuthContext';
import { useGetWorkspaceDetails, useUpdateWorkspaceDetails } from '@/hooks/useWorkspace';
import { useGetTenantRecipients } from '@/hooks/useTenant';

export type EmailListFormValues = {
  ccList: string[];
  bccList: string[];
};

type EmailListTabProps = {
  setIsBusy?: (value: boolean) => void;
};

export const emailRecipientsSchema = z.object({
  ccList: z.array(z.email('Invalid email address')),
  bccList: z.array(z.email('Invalid email address')),
});

export const EmailListTab = ({ setIsBusy }: EmailListTabProps) => {
  const { authenticatedUserDetails } = useAuth();

  const { data: tenantRecipients } = useGetTenantRecipients({
    tenantId: authenticatedUserDetails?.tenantId || '',
  });

  const { data: workspace, isLoading } = useGetWorkspaceDetails({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
  });

  const { mutate: updateWorkspace, isPending } = useUpdateWorkspaceDetails();

  const initialValues: EmailListFormValues = useMemo(
    () => ({
      ccList: workspace?.ccRecipients?.map((r) => r.email) || [],
      bccList: workspace?.bccRecipients?.map((r) => r.email) || [],
    }),
    [workspace]
  );

  const defaultValues: EmailListFormValues = useMemo(
    () => ({
      ccList: tenantRecipients?.ccRecipients?.map((r) => r.email) || [],
      bccList: tenantRecipients?.bccRecipients?.map((r) => r.email) || [],
    }),
    [tenantRecipients]
  );

  const onSubmit = async (values: EmailListFormValues) => {
    if (!workspace) return;

    updateWorkspace(
      {
        tenantId: workspace.tenantId,
        workspaceId: workspace.id,
        payload: {
          name: workspace.name,
          dailySendLimit: workspace.dailySendLimit,
          ccRecipients: values.ccList.map((email) => ({ email })),
          bccRecipients: values.bccList.map((email) => ({ email })),
        },
      },
      {
        onSuccess: () => {
          toast.success('Email recipients updated successfully');
        },
        onError: (error: AxiosError) => {
          toast.error(error.message || 'Failed to update email recipients');
        },
      }
    );
  };

  useEffect(() => {
    setIsBusy?.(isLoading || isPending);
  }, [isLoading, isPending, setIsBusy]);

  if (isLoading || !workspace) return <p>Loading...</p>;

  return (
    <div className="space-y-6">
      <InfoCard
        title="Workspace CC/BCC"
        description="Automatically added to every campaign email. Override per campaign if needed."
      />
      <EmailRecipientsForm
        initialValues={initialValues}
        defaultValues={defaultValues}
        onSubmit={onSubmit}
        isSubmitting={isPending}
      />
    </div>
  );
};
