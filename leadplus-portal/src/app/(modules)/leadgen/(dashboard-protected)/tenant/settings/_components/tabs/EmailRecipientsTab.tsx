'use client';

import { useMemo } from 'react';
import { toast } from 'sonner';

import { EmailRecipientsForm } from '@/components/emailRecipients/EmailRecipientsForm';
import { InfoCard } from '@/components/InfoCard';
import { useAuth } from '@/context/AuthContext';
import { useGetTenantDetails, useUpdateTenantRecipients } from '@/hooks/useTenant';

type EmailListFormValues = {
  ccList: string[];
  bccList: string[];
};

export const EmailRecipientsTab = () => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const { data: tenant } = useGetTenantDetails({ tenantId });
  const { mutate, isPending } = useUpdateTenantRecipients();

  const initialValues: EmailListFormValues = useMemo(
    () => ({
      ccList: tenant?.ccRecipients?.map((r) => r.email) || [],
      bccList: tenant?.bccRecipients?.map((r) => r.email) || [],
    }),
    [tenant]
  );

  const onSubmit = (values: EmailListFormValues) => {
    mutate(
      {
        tenantId,
        payload: {
          ccRecipients: values.ccList.map((email) => ({ email })),
          bccRecipients: values.bccList.map((email) => ({ email })),
        },
      },
      {
        onSuccess: () => toast.success('Tenant recipients updated successfully'),
        onError: () => toast.error('Failed to update tenant recipients'),
      }
    );
  };

  return (
    <div className="space-y-6">
      <InfoCard
        title="Tenant-Wide CC/BCC List"
        description="These email addresses will be automatically added as CC/BCC recipients to all campaign emails across all workspaces in your organization."
      />
      <EmailRecipientsForm
        initialValues={initialValues}
        onSubmit={onSubmit}
        isSubmitting={isPending}
      />
    </div>
  );
};
