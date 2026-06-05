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
  street: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  postalCode: z.string().optional(),
  country: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

type Props = { vendor?: Vendor; formId: string };

export const AddressSection = ({ vendor, formId }: Props) => {
  const { mutate } = useUpdateVendorDetails();

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: {
      street: vendor?.address?.street || '',
      city: vendor?.address?.city || '',
      state: vendor?.address?.state || '',
      postalCode: vendor?.address?.postalCode || '',
      country: vendor?.address?.country || '',
    },
  });

  const onSubmit = (data: FormValues) => {
    mutate(
      buildVendorPayload(vendor, {
        address: {
          street: data.street || '',
          city: data.city || '',
          state: data.state || '',
          postalCode: data.postalCode || '',
          country: data.country || '',
        },
      }),
      {
        onSuccess: () => toast.success('Address saved'),
        onError: () => toast.error('Failed to save'),
      }
    );
  };

  const formConfig = [
    [
      {
        type: FormElementType.TEXT,
        name: 'street' as const,
        label: 'Street Address',
        placeholder: '123 Main St',
        size: 12,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'city' as const,
        label: 'City',
        placeholder: 'New York',
        size: 6,
      },
      {
        type: FormElementType.TEXT,
        name: 'state' as const,
        label: 'State / Province',
        placeholder: 'NY',
        size: 6,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'postalCode' as const,
        label: 'Postal Code',
        placeholder: '10001',
        size: 6,
      },
      {
        type: FormElementType.TEXT,
        name: 'country' as const,
        label: 'Country',
        placeholder: 'United States',
        size: 6,
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
