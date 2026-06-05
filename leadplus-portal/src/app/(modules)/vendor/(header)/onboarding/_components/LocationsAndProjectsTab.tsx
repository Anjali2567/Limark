'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { ArrowLeft, ArrowRight } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { DynamicForm } from '@/components/form/DynamicForm';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Form } from '@/components/ui/form';
import { FormElementType } from '@/types/form.types';
import { Vendor } from '@/types/vendor.types';
import { CERTIFICATION_OPTIONS, REGION_OPTIONS } from './data';
import { ProjectShowcase } from './ProjectShowcase';
import { ShowcaseFormDrawer } from './ShowcaseFormDrawer';

const locationsSchema = z.object({
  regionsCovered: z.array(z.string()).min(1, 'Select at least one region'),
  certifications: z.array(z.string()).optional(),
});

type FormValues = z.infer<typeof locationsSchema>;

type LocationsAndProjectsTabProps = {
  onSubmit: (data: FormValues) => void;
  handleBack: () => void;
  vendor?: Vendor;
  isSubmitting?: boolean;
};

const LocationsAndProjectsTab = ({
  onSubmit,
  handleBack,
  vendor,
  isSubmitting,
}: LocationsAndProjectsTabProps) => {
  const form = useForm<FormValues>({
    resolver: zodResolver(locationsSchema),
    values: {
      regionsCovered: vendor?.regionsCovered || [],
      certifications: vendor?.certifications || [],
    },
  });

  const formConfig = [
    [
      {
        type: FormElementType.AUTOCOMPLETE_SELECT,
        name: 'regionsCovered' as const,
        label: 'Regions Covered *',
        labelSupportingText: 'Type to search or add custom regions',
        placeholder: 'e.g., North America, Europe, Asia Pacific',
        size: 12,
        options: REGION_OPTIONS,
      },
    ],
    [
      {
        type: FormElementType.AUTOCOMPLETE_SELECT,
        name: 'certifications' as const,
        label: 'Certifications',
        labelSupportingText: 'Add any relevant certifications (optional)',
        placeholder: 'e.g., AWS Certified, Azure, ISO/IEC 27001',
        size: 12,
        options: CERTIFICATION_OPTIONS,
      },
    ],
  ];

  return (
    <div className="space-y-6">
      <Form {...form}>
        <form className="space-y-6">
          <Card className="border-border shadow-lg">
            <CardHeader className="border-border border-b">
              <CardTitle>Locations & Projects</CardTitle>
              <CardDescription>Where do you operate and what have you built?</CardDescription>
            </CardHeader>
            <CardContent>
              <DynamicForm formConfig={formConfig} form={form} />
            </CardContent>
          </Card>
        </form>
      </Form>
      <ProjectShowcase />
      <ShowcaseFormDrawer />
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

export { LocationsAndProjectsTab };
