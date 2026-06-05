'use client';

import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Mail, Save } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { FormCard } from '@/components/FormCard';
import { EmailRecipientForm } from '@/components/emailRecipients/EmailRecipientForm';

export type EmailListFormValues = {
  ccList: string[];
  bccList: string[];
};

export const emailRecipientsSchema = z.object({
  ccList: z.array(z.email('Invalid email address')),
  bccList: z.array(z.email('Invalid email address')),
});

type EmailRecipientsFormProps = {
  initialValues: EmailListFormValues;
  defaultValues?: EmailListFormValues;
  onSubmit: (values: EmailListFormValues) => Promise<void> | void;
  isSubmitting?: boolean;
};

export const EmailRecipientsForm = ({
  initialValues,
  defaultValues,
  onSubmit,
  isSubmitting,
}: EmailRecipientsFormProps) => {
  const { control, handleSubmit } = useForm<EmailListFormValues>({
    resolver: zodResolver(emailRecipientsSchema),
    values: initialValues,
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <Controller
        name="ccList"
        control={control}
        render={({ field }) => (
          <FormCard
            icon={<Mail className="h-5 w-5 text-sky-500" />}
            title="CC (Carbon Copy)"
            description="Visible to all recipients. Use for team members who should be openly copied."
          >
            <EmailRecipientForm
              label="Current CC Recipients"
              name="ccEmail"
              placeholder="manager@company.com"
              list={field.value || []}
              defaultList={defaultValues?.ccList || []}
              onAdd={(email) => field.onChange([...field.value, email])}
              onRemove={(email) => field.onChange(field.value.filter((e) => e !== email))}
              addButtonText="Add CC"
            />
          </FormCard>
        )}
      />

      <Controller
        name="bccList"
        control={control}
        render={({ field }) => (
          <FormCard
            icon={<Mail className="h-5 w-5 text-sky-500" />}
            title="BCC (Blind Carbon Copy)"
            description="Hidden from recipients. Use for CRM logging or compliance tracking."
          >
            <EmailRecipientForm
              label="Current BCC Recipients"
              name="bccEmail"
              placeholder="compliance@company.com"
              list={field.value || []}
              defaultList={defaultValues?.bccList || []}
              onAdd={(email) => field.onChange([...field.value, email])}
              onRemove={(email) => field.onChange(field.value.filter((e) => e !== email))}
              badgeClass="bg-purple-50 border-purple-200"
              addButtonText="Add BCC"
            />
          </FormCard>
        )}
      />

      <div className="border-border flex justify-end border-t pt-4">
        <Button
          type="submit"
          className="bg-sky-500 text-white hover:bg-sky-600"
          disabled={isSubmitting}
        >
          <Save className="mr-2 h-4 w-4" />
          Save Changes
        </Button>
      </div>
    </form>
  );
};
