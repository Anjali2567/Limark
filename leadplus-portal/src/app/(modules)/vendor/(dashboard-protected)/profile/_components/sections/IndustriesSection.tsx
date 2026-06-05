'use client';

import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { ChipOption, ChipSelect } from '@/components/form/elements/ChipSelect';
import { Form, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { useGetIndustryList } from '@/hooks/useIndustry';
import { useUpdateVendorDetails } from '@/hooks/useVendor';
import { Vendor } from '@/types/vendor.types';
import { zodResolver } from '@hookform/resolvers/zod';
import { buildVendorPayload } from '../utils/buildVendorPayload';

const schema = z.object({
  industryIds: z.array(z.number()).min(1, 'Please select at least one industry'),
});

type FormValues = z.infer<typeof schema>;

type Props = { vendor?: Vendor; formId: string };

export const IndustriesSection = ({ vendor, formId }: Props) => {
  const { mutate } = useUpdateVendorDetails();
  const industryList = useGetIndustryList();

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: {
      industryIds: vendor?.industryIds || [],
    },
  });

  const onSubmit = (data: FormValues) => {
    mutate(buildVendorPayload(vendor, data), {
      onSuccess: () => toast.success('Industries saved'),
      onError: () => toast.error('Failed to save'),
    });
  };

  return (
    <Form {...form}>
      <form id={formId} onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <FormField
          control={form.control}
          name="industryIds"
          render={({ field }) => {
            const chipValue: ChipOption[] = (field.value || []).map(
              (v: number) =>
                industryList.find((o) => o.value === String(v)) || { label: String(v), value: v }
            );
            return (
              <FormItem>
                <FormLabel>Industries</FormLabel>
                <ChipSelect
                  value={chipValue}
                  onChange={(newValues: ChipOption[]) =>
                    field.onChange(newValues.map((v) => v.value as number))
                  }
                  options={industryList}
                />
                <FormMessage />
              </FormItem>
            );
          }}
        />
      </form>
    </Form>
  );
};
