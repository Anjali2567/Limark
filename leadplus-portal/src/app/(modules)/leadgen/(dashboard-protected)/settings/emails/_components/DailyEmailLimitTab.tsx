'use client';

import { AxiosError } from 'axios';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { FormCard } from '@/components/FormCard';
import { InfoCard } from '@/components/InfoCard';
import { Button } from '@/components/ui/button';
import { FormInput } from '@/components/ui/form-input';
import { useAuth } from '@/context/AuthContext';
import { useGetWorkspaceDetails, useUpdateWorkspaceDetails } from '@/hooks/useWorkspace';
import { numberValidation } from '@/lib/utils/validations';
import { zodResolver } from '@hookform/resolvers/zod';

import { Mail, Save } from 'lucide-react';

export const dailyLimitSchema = z.object({
  dailyLimit: numberValidation({
    min: 1,
    fieldName: 'Daily limit',
    max: 50,
  }),
});

type DailyEmailLimitFormValues = z.infer<typeof dailyLimitSchema>;

type DailyEmailLimitTabProps = {
  setIsBusy?: (value: boolean) => void;
};

const DailyEmailLimitTab = ({ setIsBusy }: DailyEmailLimitTabProps) => {
  const { authenticatedUserDetails } = useAuth();

  const { mutate, isPending } = useUpdateWorkspaceDetails();
  const { data, isLoading } = useGetWorkspaceDetails({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
  });

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<DailyEmailLimitFormValues>({
    resolver: zodResolver(dailyLimitSchema),
    values: {
      dailyLimit: data?.dailySendLimit ?? 30,
    },
  });

  useEffect(() => {
    setIsBusy?.(isLoading || isPending);
  }, [isLoading, isPending, setIsBusy]);

  const onSubmit = (values: DailyEmailLimitFormValues) => {
    if (!authenticatedUserDetails?.tenantId || !authenticatedUserDetails?.workspaceId || !data)
      return;

    mutate(
      {
        tenantId: authenticatedUserDetails?.tenantId,
        workspaceId: authenticatedUserDetails?.workspaceId,
        payload: {
          name: data.name,
          ccRecipients: data.ccRecipients ?? [],
          bccRecipients: data.bccRecipients ?? [],
          dailySendLimit: values.dailyLimit ?? 30,
        },
      },
      {
        onSuccess: () => {
          toast.success('Daily limit updated successfully');
        },
        onError: (error: AxiosError) => {
          toast.error(error.message || 'Failed to update daily limit');
        },
      }
    );
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <InfoCard
        title="Daily Email Limit"
        description="Set the maximum number of emails you can send in a day to avoid hitting spam filters."
      />

      <FormCard
        icon={<Mail className="h-5 w-5 text-sky-500" />}
        title="Daily Limit"
        description="Enter the maximum number of emails you can send in a day."
      >
        <div className="flex gap-3">
          <div className="flex-1 space-y-1">
            <FormInput
              label=""
              type="number"
              placeholder="Enter daily limit (e.g., 500)"
              disabled={isPending}
              error={!!errors.dailyLimit}
              errorMessage={errors.dailyLimit?.message}
              {...register('dailyLimit', { valueAsNumber: true })}
            />
          </div>

          <Button
            type="submit"
            disabled={isPending}
            className="bg-sky-500 text-white hover:bg-sky-600"
          >
            <Save className="mr-2 h-4 w-4" />
            Save Limit
          </Button>
        </div>
      </FormCard>
    </form>
  );
};

export { DailyEmailLimitTab };
