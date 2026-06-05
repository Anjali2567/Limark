'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowLeft, ArrowRight } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { ChipOption, ChipSelect } from '@/components/form/elements/ChipSelect';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { useGetIndustryList } from '@/hooks/useIndustry';
import { useGetAllServiceCategories, useGetAllServices } from '@/hooks/useService';
import { Vendor, VendorFormValues } from '@/types/vendor.types';
import { ServiceAccordionList } from './ServiceAccordionList';

const vendorSchema = z.object({
  serviceIds: z.array(z.number()).min(1, 'Select at least one service'),
  industryIds: z.array(z.number()).min(1, 'Select at least one industry'),
});

type FormValues = z.infer<typeof vendorSchema>;

type ServiceTabProps = {
  onSubmit: (data: VendorFormValues) => void;
  handleBack: () => void;
  vendor?: Vendor;
  isSubmitting?: boolean;
};

const ServicesTab = ({ onSubmit, handleBack, vendor, isSubmitting }: ServiceTabProps) => {
  const { data: services = [] } = useGetAllServices({});
  const { data: categories = [] } = useGetAllServiceCategories();
  const industryList = useGetIndustryList();

  const form = useForm<FormValues>({
    resolver: zodResolver(vendorSchema),
    values: {
      serviceIds: vendor?.serviceIds || [],
      industryIds: vendor?.industryIds || [],
    },
  });

  return (
    <div className="space-y-6">
      <Form {...form}>
        <form className="space-y-6">
          <Card className="border-border shadow-lg">
            <CardHeader className="border-border border-b">
              <CardTitle>Services</CardTitle>
              <CardDescription>Select the services your company offers.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <FormField
                control={form.control}
                name="serviceIds"
                render={({ field }) => (
                  <FormItem>
                    <ServiceAccordionList
                      categories={categories}
                      services={services}
                      selectedServiceIds={field.value}
                      onChange={field.onChange}
                    />
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className="border-border border-t pt-6">
                <FormField
                  control={form.control}
                  name="industryIds"
                  render={({ field }) => {
                    const chipValue: ChipOption[] = Array.isArray(field.value)
                      ? field.value.map(
                          (v: number) =>
                            industryList.find((o) => o.value === String(v)) || {
                              label: String(v),
                              value: v,
                            }
                        )
                      : [];
                    return (
                      <FormItem>
                        <FormLabel>Industries *</FormLabel>
                        <ChipSelect
                          value={chipValue}
                          onChange={(newValues: ChipOption[]) => {
                            field.onChange(newValues.map((v) => v.value as number));
                          }}
                          options={industryList}
                        />
                        <FormMessage />
                      </FormItem>
                    );
                  }}
                />
              </div>
            </CardContent>
          </Card>
        </form>
      </Form>
      <div className="flex items-center justify-between">
        <Button type="button" variant="outline" onClick={handleBack} disabled={isSubmitting}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <Button
          type="button"
          onClick={form.handleSubmit(onSubmit)}
          className="bg-primary hover:bg-primary/90"
          disabled={isSubmitting}
        >
          {isSubmitting ? (
            'Saving...'
          ) : (
            <>
              Next Step
              <ArrowRight className="ml-2 h-4 w-4" />
            </>
          )}
        </Button>
      </div>
    </div>
  );
};

export { ServicesTab };
