'use client';

import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { DynamicForm } from '@/components/form/DynamicForm';
import { Form } from '@/components/ui/form';
import { useUpdateVendorDetails } from '@/hooks/useVendor';
import { FormElementType } from '@/types/form.types';
import { Vendor } from '@/types/vendor.types';
import { zodResolver } from '@hookform/resolvers/zod';
import { buildVendorPayload } from '../utils/buildVendorPayload';

const schema = z.object({
  teamDescription: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

type Props = { vendor?: Vendor; formId: string };

export const AboutTeamSection = ({ vendor, formId }: Props) => {
  const { mutate } = useUpdateVendorDetails();

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: {
      teamDescription: vendor?.teamDescription || '',
    },
  });

  const onSubmit = (data: FormValues) => {
    mutate(buildVendorPayload(vendor, data), {
      onSuccess: () => toast.success('Team info saved'),
      onError: () => toast.error('Failed to save'),
    });
  };

  const formConfig = [
    [
      {
        type: FormElementType.TEXTAREA,
        name: 'teamDescription' as const,
        label: 'Team Overview',
        placeholder: 'Tell potential clients about your team — culture, expertise, and approach',
        size: 12,
      },
    ],
  ];

  return (
    <Form {...form}>
      <form id={formId} onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <DynamicForm formConfig={formConfig} form={form} />
      </form>
    </Form>
  );
};
