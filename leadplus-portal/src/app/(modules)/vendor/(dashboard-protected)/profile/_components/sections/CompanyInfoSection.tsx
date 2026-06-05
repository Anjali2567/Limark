'use client';

import Image from 'next/image';
import { useRef } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { VENDOR_COMPANY_SIZE_OPTIONS } from '@/app/(modules)/vendor/_constants/vendorOptions';
import { DynamicForm } from '@/components/form/DynamicForm';
import { Button } from '@/components/ui/button';
import { Form, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { useUpdateVendorDetails } from '@/hooks/useVendor';
import { FormElementType } from '@/types/form.types';
import { Vendor } from '@/types/vendor.types';
import { zodResolver } from '@hookform/resolvers/zod';
import { TagInput } from '../TagInput';
import { buildVendorPayload } from '../utils/buildVendorPayload';

import { Camera, Upload, X } from 'lucide-react';

const schema = z.object({
  logo: z.any().optional(),
  companyName: z.string().min(1, 'Company name is required'),
  website: z.string().optional(),
  salesEmail: z.string().optional(),
  schedulingLink: z.string().optional(),
  phoneNumber: z.string().optional(),
  yearEstablished: z.string().optional(),
  companySize: z.string().optional(),
  videoLink: z.string().optional(),
  tagline: z.string().min(1, 'Tagline is required').max(100, 'Max 100 characters'),
  description: z.string().min(1, 'Description is required').max(2000, 'Max 2000 characters'),
  languagesSpoken: z.array(z.string()).optional(),
});

type FormValues = z.infer<typeof schema>;

type Props = { vendor?: Vendor; formId: string };

export const CompanyInfoSection = ({ vendor, formId }: Props) => {
  const { mutate } = useUpdateVendorDetails();
  const logoInputRef = useRef<HTMLInputElement>(null);

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: {
      logo: vendor?.logo || '',
      companyName: vendor?.companyName || '',
      website: vendor?.website || '',
      salesEmail: vendor?.salesEmail || '',
      schedulingLink: vendor?.schedulingLink || '',
      phoneNumber: vendor?.phoneNumber || '',
      yearEstablished: vendor?.yearEstablished || '',
      companySize: vendor?.companySize || undefined,
      videoLink: vendor?.videoLink || '',
      tagline: vendor?.tagline || '',
      description: vendor?.description || '',
      languagesSpoken: vendor?.languagesSpoken || [],
    },
    resetOptions: {
      keepDirtyValues: true,
    },
  });

  const onSubmit = (data: FormValues) => {
    const { logo, languagesSpoken, ...rest } = data;
    mutate(buildVendorPayload(vendor, { ...rest, languagesSpoken, logo }), {
      onSuccess: () => toast.success('Company info saved'),
      onError: () => toast.error('Failed to save'),
    });
  };

  const formConfig = [
    [
      {
        type: FormElementType.TEXT,
        name: 'companyName' as const,
        label: 'Company Name',
        required: true,
        placeholder: 'Acme Corp',
        size: 6,
      },
      {
        type: FormElementType.TEXT,
        name: 'website' as const,
        label: 'Company Website',
        placeholder: 'https://yourcompany.com',
        size: 6,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'salesEmail' as const,
        label: 'Sales Email',
        placeholder: 'sales@yourcompany.com',
        size: 6,
      },
      {
        type: FormElementType.TEXT,
        name: 'schedulingLink' as const,
        label: 'Scheduling Link',
        placeholder: 'https://calendly.com/yourname',
        size: 6,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'phoneNumber' as const,
        label: 'Admin Contact Phone',
        placeholder: '+1 (555) 000-0000',
        size: 6,
      },
      {
        type: FormElementType.TEXT,
        name: 'yearEstablished' as const,
        label: 'Founding Year',
        placeholder: '2010',
        size: 6,
      },
    ],
    [
      {
        type: FormElementType.SELECT,
        name: 'companySize' as const,
        label: 'Total Employees',
        placeholder: 'Select range',
        size: 6,
        options: VENDOR_COMPANY_SIZE_OPTIONS,
      },
      {
        type: FormElementType.TEXT,
        name: 'videoLink' as const,
        label: 'Company Video Link',
        placeholder: 'https://youtube.com/watch?v=...',
        size: 6,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'tagline' as const,
        label: 'Tagline',
        required: true,
        placeholder: 'Innovative solutions for modern businesses',
        size: 12,
        maxLength: 100,
      },
    ],
    [
      {
        type: FormElementType.TEXTAREA,
        name: 'description' as const,
        label: 'Description',
        required: true,
        placeholder: 'Describe your company, expertise, and what sets you apart…',
        size: 12,
        rows: 6,
        maxLength: 2000,
      },
    ],
  ];

  return (
    <Form {...form}>
      <form id={formId} onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
        <DynamicForm formConfig={formConfig} form={form} />
        <FormField
          control={form.control}
          name="languagesSpoken"
          render={({ field, fieldState }) => (
            <FormItem>
              <FormLabel>Languages Spoken</FormLabel>
              <TagInput
                value={field.value || []}
                onChange={field.onChange}
                placeholder="e.g. French (press Enter)"
              />
              <FormMessage>{fieldState.error?.message}</FormMessage>
            </FormItem>
          )}
        />
        <div className="border-border space-y-4 border-t pt-6">
          <div>
            <p className="text-foreground text-sm font-medium">Company Logo</p>
            <p className="text-muted-foreground mt-0.5 text-xs">
              Upload a PNG, JPEG, SVG, or WEBP file (max 10 MB). A square logo with a transparent
              background works best.
            </p>
          </div>
          <input
            ref={logoInputRef}
            type="file"
            accept="image/*"
            className="hidden"
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) form.setValue('logo', file);
              e.target.value = '';
            }}
          />
          <Controller
            control={form.control}
            name="logo"
            render={({ field }) => {
              const previewUrl =
                field.value instanceof File
                  ? URL.createObjectURL(field.value)
                  : typeof field.value === 'string' && field.value
                    ? `${process.env.NEXT_PUBLIC_BASE_URL}${field.value}`
                    : null;

              return (
                <div className="flex items-center gap-5">
                  {previewUrl ? (
                    <div className="relative shrink-0">
                      <div className="border-border bg-secondary relative flex h-24 w-24 items-center justify-center overflow-hidden rounded-xl border-2 border-dashed">
                        <Image
                          src={previewUrl}
                          alt="Company logo"
                          fill
                          className="object-contain"
                        />
                      </div>
                      <button
                        type="button"
                        onClick={() => field.onChange('')}
                        className="bg-destructive hover:bg-destructive/90 absolute -top-2 -right-2 rounded-full p-1 text-white shadow transition-colors"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </div>
                  ) : (
                    <button
                      type="button"
                      onClick={() => logoInputRef.current?.click()}
                      className="border-border bg-secondary hover:bg-muted/60 text-muted-foreground flex h-24 w-24 shrink-0 flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed transition-colors"
                    >
                      <Upload className="h-6 w-6" />
                      <span className="px-1 text-center text-xs leading-tight">Upload logo</span>
                    </button>
                  )}

                  <div className="flex gap-3">
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => logoInputRef.current?.click()}
                    >
                      <Camera className="mr-2 h-4 w-4" />
                      {previewUrl ? 'Replace Logo' : 'Upload Logo'}
                    </Button>
                    {previewUrl && (
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="text-destructive hover:text-destructive"
                        onClick={() => field.onChange('')}
                      >
                        <X className="mr-2 h-4 w-4" />
                        Remove
                      </Button>
                    )}
                  </div>
                </div>
              );
            }}
          />
        </div>
      </form>
    </Form>
  );
};
