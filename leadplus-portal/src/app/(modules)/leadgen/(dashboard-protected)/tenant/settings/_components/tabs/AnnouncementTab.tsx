'use client';

import { AxiosError } from 'axios';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import * as z from 'zod';

import { DynamicForm } from '@/components/form/DynamicForm';
import { InfoCard } from '@/components/InfoCard';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { MailboxConnectionStatus } from '@/constants/mailbox.constants';
import { useAuth } from '@/context/AuthContext';
import { useConnectAnnouncementSmtp, useGetTenantDetails } from '@/hooks/useTenant';
import { DynamicFormConfig, FormElementType } from '@/types/form.types';
import { zodResolver } from '@hookform/resolvers/zod';

import { MailIcon, Save } from 'lucide-react';

const schema = z.object({
  fromEmail: z.string().email('Invalid email address'),
  senderName: z.string().min(1, 'Sender name is required'),
  appPassword: z.string().min(1, 'App password is required'),
});

type FormValues = z.infer<typeof schema>;

const formConfig: DynamicFormConfig<FormValues> = [
  [
    {
      name: 'fromEmail',
      label: 'From Email',
      type: FormElementType.TEXT,
      inputType: 'email',
      placeholder: 'noreply@yourcompany.com',
      required: true,
      size: 12,
    },
  ],
  [
    {
      name: 'senderName',
      label: 'Sender Name',
      type: FormElementType.TEXT,
      placeholder: 'Your Company',
      size: 12,
      required: true,
    },
  ],
  [
    {
      name: 'appPassword',
      label: 'App Password',
      type: FormElementType.TEXT,
      inputType: 'password',
      placeholder: 'Gmail app password',
      size: 12,
      required: true,
    },
  ],
];

export const AnnouncementTab = () => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const { data: tenant } = useGetTenantDetails({ tenantId });
  const { mutate: connectSmtp, isPending } = useConnectAnnouncementSmtp();

  const isConnected = tenant?.announcementStatus === MailboxConnectionStatus.VERIFIED;

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: {
      fromEmail: tenant?.announcementFromEmail ?? '',
      senderName: tenant?.announcementSenderName ?? '',
      appPassword: tenant?.announcementFromEmail ? '********' : '',
    },
  });

  const handleSubmit = (values: FormValues) => {
    connectSmtp(
      { tenantId, payload: values },
      {
        onSuccess: () => {
          form.resetField('appPassword');
          toast.success('Announcement email connected successfully');
        },
        onError: (error: AxiosError) =>
          toast.error(error?.message || 'Failed to connect announcement email'),
      }
    );
  };

  return (
    <div className="space-y-6">
      <InfoCard
        title="Announcement Sender Address"
        description="Configure the email address used to send announcements to your contacts. A test email will be sent to verify the connection."
      />

      {isConnected && (
        <div className="flex items-center gap-3 rounded-lg border border-green-200 bg-green-50 px-4 py-3">
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-green-100">
            <MailIcon className="h-4 w-4 text-green-600" />
          </div>
          <div>
            <p className="text-sm font-medium text-green-800">{tenant?.announcementFromEmail}</p>
            <p className="text-xs text-green-600">{tenant?.announcementSenderName}</p>
          </div>
          <span className="ml-auto rounded-full bg-green-100 px-3 py-1 text-xs font-medium text-green-700">
            Connected
          </span>
        </div>
      )}

      <Form {...form}>
        <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
          <DynamicForm form={form} formConfig={formConfig} />
          <div className="flex justify-end border-t pt-4">
            <Button type="submit" disabled={isPending}>
              <Save className="mr-2 h-4 w-4" />
              {isPending ? 'Connecting...' : isConnected ? 'Reconnect' : 'Connect'}
            </Button>
          </div>
        </form>
      </Form>
    </div>
  );
};
