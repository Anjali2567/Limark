'use client';

import { useForm, useWatch } from 'react-hook-form';
import { z } from 'zod';
import { toast } from 'sonner';

import { zodResolver } from '@hookform/resolvers/zod';
import { Form, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Vendor } from '@/types/vendor.types';
import { useUpdateVendorDetails } from '@/hooks/useVendor';
import { buildVendorPayload } from '../utils/buildVendorPayload';
import { TagInput } from '../TagInput';

const COMMON_CERTIFICATIONS = ['ISO 9001', 'ISO 27001', 'SOC 2', 'PCI DSS', 'GDPR', 'HIPAA'];

const schema = z.object({
  certifications: z.array(z.string()).min(1, 'Please add at least one certification'),
});

type FormValues = z.infer<typeof schema>;

type Props = { vendor?: Vendor; formId: string };

export const CertificationsSection = ({ vendor, formId }: Props) => {
  const { mutate } = useUpdateVendorDetails();

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: {
      certifications: vendor?.certifications || [],
    },
  });

  const onSubmit = (data: FormValues) => {
    mutate(buildVendorPayload(vendor, data), {
      onSuccess: () => toast.success('Certifications saved'),
      onError: () => toast.error('Failed to save'),
    });
  };

  const current = useWatch({ control: form.control, name: 'certifications' }) || [];

  const addCommon = (cert: string) => {
    if (!current.includes(cert)) {
      form.setValue('certifications', [...current, cert]);
    }
  };

  return (
    <Form {...form}>
      <form id={formId} onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <FormField
          control={form.control}
          name="certifications"
          render={({ field, fieldState }) => (
            <FormItem>
              <FormLabel>Certifications</FormLabel>
              <TagInput
                value={field.value || []}
                onChange={field.onChange}
                placeholder="e.g. ISO 9001, SOC 2"
              />
              <FormMessage>{fieldState.error?.message}</FormMessage>
            </FormItem>
          )}
        />

        <div className="space-y-2">
          <p className="text-muted-foreground text-sm">Common certifications — click to add:</p>
          <div className="flex flex-wrap gap-2">
            {COMMON_CERTIFICATIONS.filter((c) => !current.includes(c)).map((cert) => (
              <button
                key={cert}
                type="button"
                onClick={() => addCommon(cert)}
                className="border-border hover:bg-muted rounded-full border px-3 py-1 text-xs transition-colors"
              >
                + {cert}
              </button>
            ))}
          </div>
        </div>
      </form>
    </Form>
  );
};
