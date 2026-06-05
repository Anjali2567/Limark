'use client';

import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';

import { DynamicForm } from '@/components/form/DynamicForm';
import { VENDOR_COMPANY_SIZE_OPTIONS } from '@/app/(modules)/vendor/_constants/vendorOptions';
import { ImageUploadField } from '@/components/form/elements/ImageUploadField';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { FormElementType } from '@/types/form.types';
import { Form } from '@/components/ui/form';
import { ArrowRight, Building2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Vendor } from '@/types/vendor.types';
import { User } from '@/types/user.types';

const vendorSchema = z.object({
  logo: z.any().optional(),
  companyName: z.string().min(1, 'Company name is required'),
  tagline: z.string().optional(),
  description: z.string().min(1, 'Company description is required'),
  yearEstablished: z.string().optional(),
  companySize: z.string().min(1, 'Company size is required'),
  annualRevenue: z.string().optional(),
  website: z.string().optional(),
  name: z.string().min(1, 'Name is required'),
  email: z.email('Enter a valid email'),
  phoneNumber: z.string().min(1, 'Phone number is required'),
  businessHours: z.string().optional(),
  street: z.string().min(1, 'Street is required'),
  city: z.string().min(1, 'City is required'),
  state: z.string().min(1, 'State is required'),
  postalCode: z.string().min(1, 'Postal code is required'),
  country: z.string().min(1, 'Country is required'),
});

type FormValues = z.infer<typeof vendorSchema>;

type VendorInformationTabProps = {
  onSubmit: (data: FormValues) => void;
  vendor?: Vendor;
  user?: User;
  isSubmitting?: boolean;
};

const VendorInformationTab = ({
  onSubmit,
  vendor,
  user,
  isSubmitting,
}: VendorInformationTabProps) => {
  const form = useForm<FormValues>({
    resolver: zodResolver(vendorSchema),
    values: {
      logo: vendor?.logo || '',
      companyName: vendor?.companyName || user?.company || '',
      tagline: vendor?.tagline || '',
      description: vendor?.description || '',
      companySize: vendor?.companySize || '',
      yearEstablished: vendor?.yearEstablished || '',
      annualRevenue: vendor?.annualRevenue || '',
      website: vendor?.website || '',
      name: user?.name || '',
      email: user?.email || '',
      phoneNumber: vendor?.phoneNumber || user?.phoneNumber || '',
      businessHours: vendor?.businessHours || '',
      street: vendor?.address?.street || '',
      city: vendor?.address?.city || '',
      state: vendor?.address?.state || '',
      postalCode: vendor?.address?.postalCode || '',
      country: vendor?.address?.country || '',
    },
  });

  const formConfig = [
    [
      {
        type: FormElementType.TEXT,
        name: 'companyName' as const,
        placeholder: 'Enter your company name',
        label: 'Company Name *',
        size: 12,
      },
    ],

    [
      {
        type: FormElementType.TEXTAREA,
        name: 'description' as const,
        placeholder: 'Describe your company, its mission, and what makes it unique',
        label: 'Company Description *',
        size: 12,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'tagline' as const,
        placeholder: 'A short tagline for your company',
        label: 'Tagline',
        size: 12,
      },
    ],
    [
      {
        type: FormElementType.SELECT,
        name: 'companySize' as const,
        placeholder: 'Select company size',
        label: 'Company Size *',
        size: 6,
        options: VENDOR_COMPANY_SIZE_OPTIONS,
      },
      {
        type: FormElementType.SELECT,
        name: 'annualRevenue' as const,
        placeholder: 'Select revenue range',
        label: 'Annual Revenue',
        size: 6,
        options: [
          { label: '$0 - $1M', value: '0-1M' },
          { label: '$1M - $10M', value: '1M-10M' },
          { label: '$10M - $50M', value: '10M-50M' },
          { label: '$50M - $100M', value: '50M-100M' },
          { label: '$100M+', value: '100M+' },
        ],
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'yearEstablished' as const,
        placeholder: 'Enter year established',
        label: 'Year Established',
        size: 6,
      },
      {
        type: FormElementType.TEXT,
        name: 'website' as const,
        placeholder: 'https://www.company.com',
        label: 'Website',
        size: 6,
      },
    ],
  ];

  const contactFormConfig = [
    [
      {
        type: FormElementType.TEXT,
        name: 'name' as const,
        placeholder: 'Enter your name',
        label: 'Name *',
        size: 6,
        disabled: true,
      },
      {
        type: FormElementType.TEXT,
        name: 'email' as const,
        placeholder: 'Enter your email',
        label: 'Email *',
        size: 6,
        disabled: true,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'phoneNumber' as const,
        placeholder: 'Enter your phone number',
        label: 'Phone Number *',
        size: 6,
      },
      {
        type: FormElementType.TEXT,
        name: 'businessHours' as const,
        placeholder: 'Monday - Friday, 8:00 AM - 6:00 PM EST',
        label: 'Business Hours',
        size: 6,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'street' as const,
        placeholder: 'Enter your street address',
        label: 'Street Address *',
        size: 12,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'city' as const,
        placeholder: 'Enter your city',
        label: 'City *',
        size: 6,
      },
      {
        type: FormElementType.TEXT,
        name: 'state' as const,
        placeholder: 'Enter your state',
        label: 'State/Province *',
        size: 6,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'postalCode' as const,
        placeholder: 'Enter your postal code',
        label: 'Postal Code *',
        size: 6,
      },
      {
        type: FormElementType.TEXT,
        name: 'country' as const,
        placeholder: 'Enter your country',
        label: 'Country *',
        size: 6,
      },
    ],
  ];

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <Card className="border-border shadow-lg">
          <CardHeader className="border-border border-b">
            <CardTitle>Vendor Information</CardTitle>
            <CardDescription>Tell us about your company and how to reach you</CardDescription>
          </CardHeader>

          <CardContent className="space-y-4">
            <div className="border-border flex items-center gap-6 border-b pb-6">
              <Controller
                control={form.control}
                name="logo"
                render={({ field, fieldState }) => (
                  <div>
                    <ImageUploadField
                      label="Industry Image"
                      variant="avatar"
                      required
                      size={96}
                      value={
                        typeof field.value === 'string'
                          ? field.value
                            ? `${process.env.NEXT_PUBLIC_BASE_URL}${field.value}`
                            : undefined
                          : undefined
                      }
                      fallbackIcon={<Building2 className="text-muted-foreground h-10 w-10" />}
                      onChange={(file) => {
                        field.onChange(file ?? undefined);
                      }}
                    />
                    {fieldState.error && (
                      <p className="text-destructive mt-2 flex justify-center text-sm">
                        {fieldState.error.message}
                      </p>
                    )}
                  </div>
                )}
              />
              <div className="flex-1">
                <h3 className="text-foreground mb-1 font-semibold">Company Logo</h3>
                <p className="text-muted-foreground text-sm">Upload your company logo (optional)</p>
              </div>
            </div>
            <div className="space-y-4">
              <p className="text-foreground text-[16px] font-semibold">Company Details</p>
              <DynamicForm formConfig={formConfig} form={form} />
            </div>
            <div className="border-border space-y-4 border-t pt-6">
              <p className="text-foreground text-[16px] font-semibold">Contact Information</p>
              <DynamicForm formConfig={contactFormConfig} form={form} />
            </div>
          </CardContent>
        </Card>
        <div className="flex justify-end">
          <Button type="submit" className="bg-primary hover:bg-primary/90" disabled={isSubmitting}>
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
      </form>
    </Form>
  );
};

export { VendorInformationTab };
