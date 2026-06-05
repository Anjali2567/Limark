'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { DynamicForm } from '@/components/form/DynamicForm';
import { ImageUploadField } from '@/components/form/elements/ImageUploadField';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { useAuth } from '@/context/AuthContext';
import { useGetAuthenticatedVendor } from '@/hooks/useAuthentication';
import { useGetAllServices } from '@/hooks/useService';
import {
  useGetVendorShowCaseById,
  useUpsertVendorShowcase,
  useDeleteVendorShowcaseAttachment,
} from '@/hooks/useVendorShowcase';
import { FormElementType } from '@/types/form.types';
import { zodResolver } from '@hookform/resolvers/zod';

const showcaseFormSchema = z.object({
  projectName: z.string().trim().min(1, 'Project name is required'),
  clientName: z.string().optional(),
  description: z.string().trim().min(10, 'Description must be at least 10 characters'),
  serviceIds: z.array(z.string()).min(1, 'Select at least one service'),
  duration: z.string().trim().min(1, 'Duration is required'),
  resultsAndOutcomes: z.string().optional(),
  imageFile: z.union([z.instanceof(File), z.string()]).optional(),
});

type ShowcaseFormValues = z.infer<typeof showcaseFormSchema>;

const ShowcaseFormDrawer = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { authenticatedUserDetails } = useAuth();
  const { data: vendor } = useGetAuthenticatedVendor();

  const showcaseId = searchParams.get('id') || '';
  const action = searchParams.get('action') || '';

  const isEditMode = !!showcaseId;

  const isDrawerOpen = useMemo(() => {
    return !!(showcaseId || action === 'add');
  }, [showcaseId, action]);

  const { data: showcaseData } = useGetVendorShowCaseById({
    id: showcaseId,
    tenantId: authenticatedUserDetails?.tenantId || '',
    vendorId: vendor?.id || '',
  });

  const { data: serviceList = [] } = useGetAllServices({});

  const onProjectCreated = useCallback(
    (id: string) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('id', id);
      params.delete('action');
      router.push(`?${params.toString()}`, { scroll: false });
    },
    [router, searchParams]
  );

  const { mutate: upsertShowcase, isPending } = useUpsertVendorShowcase(onProjectCreated);
  const { mutate: deleteAttachment } = useDeleteVendorShowcaseAttachment();

  const form = useForm<ShowcaseFormValues>({
    resolver: zodResolver(showcaseFormSchema),
    values: {
      projectName: showcaseData?.projectName || '',
      clientName: showcaseData?.clientName || '',
      description: showcaseData?.description || '',
      serviceIds: showcaseData?.serviceIds || [],
      duration: showcaseData?.duration || '',
      resultsAndOutcomes: showcaseData?.resultsAndOutcomes || '',
      imageFile: showcaseData?.attachments?.[0]?.fileUrl || '',
    },
  });

  const handleCloseDrawer = useCallback(() => {
    const params = new URLSearchParams(searchParams.toString());
    params.delete('id');
    params.delete('action');
    router.push(`?${params.toString()}`, { scroll: false });
    form.reset();
  }, [router, searchParams, form]);

  const handleDeleteAttachment = useCallback(() => {
    if (!isEditMode || !authenticatedUserDetails?.tenantId || !vendor?.id || !showcaseId) {
      return;
    }

    deleteAttachment(
      {
        id: showcaseId,
        vendorId: vendor.id,
        tenantId: authenticatedUserDetails.tenantId,
        attachmentId: showcaseData?.attachments[0]?.id || '',
      },
      {
        onSuccess: () => {
          toast.success('Image removed successfully');
        },
        onError: () => {
          toast.error('Failed to remove image');
        },
      }
    );
  }, [
    isEditMode,
    authenticatedUserDetails,
    vendor,
    showcaseId,
    deleteAttachment,
    showcaseData?.attachments,
  ]);

  const onSubmit = useCallback(
    (payload: ShowcaseFormValues) => {
      if (!authenticatedUserDetails?.tenantId || !vendor?.id) return;
      const { imageFile, ...formData } = payload;
      upsertShowcase(
        {
          id: showcaseId || undefined,
          vendorId: vendor.id,
          tenantId: authenticatedUserDetails.tenantId,
          file: imageFile instanceof File ? imageFile : '',
          payload: {
            ...formData,
            clientName: formData.clientName || '',
            resultsAndOutcomes: formData.resultsAndOutcomes || '',
          },
        },
        {
          onSuccess: () => {
            toast.success(`Project ${isEditMode ? 'updated' : 'created'} successfully`);
            handleCloseDrawer();
          },
          onError: () => {
            toast.error(`Failed to ${isEditMode ? 'update' : 'create'} project.`);
          },
        }
      );
    },
    [authenticatedUserDetails, vendor, showcaseId, isEditMode, upsertShowcase, handleCloseDrawer]
  );

  const formConfig = useMemo(
    () => [
      [
        {
          type: FormElementType.TEXT,
          name: 'projectName' as const,
          placeholder: 'e.g., E-commerce Platform Redesign',
          label: 'Project Name *',
          size: 6,
        },
        {
          type: FormElementType.TEXT,
          name: 'clientName' as const,
          placeholder: 'e.g., Acme Corp',
          label: 'Client Name (Optional)',
          size: 6,
        },
      ],
      [
        {
          type: FormElementType.TEXTAREA,
          name: 'description' as const,
          placeholder: 'Brief description of the project, challenges, and your approach...',
          label: 'Project Description *',
          size: 12,
        },
      ],
      [
        {
          type: FormElementType.MULTI_SELECT,
          name: 'serviceIds' as const,
          placeholder: 'Select services',
          label: 'Services Provided *',
          size: 6,
          hideSearch: false,
          options: serviceList.map((service) => ({
            label: service.name,
            value: String(service.id),
          })),
        },
        {
          type: FormElementType.TEXT,
          name: 'duration' as const,
          placeholder: 'e.g., 6 months, 2023',
          label: 'Duration/Year *',
          size: 6,
        },
      ],
      [
        {
          type: FormElementType.TEXTAREA,
          name: 'resultsAndOutcomes' as const,
          placeholder: 'Key achievements, metrics, and impact...',
          label: 'Results & Outcomes (Optional)',
          size: 12,
        },
      ],
    ],
    [serviceList]
  );

  return (
    <Sheet open={isDrawerOpen} onOpenChange={handleCloseDrawer}>
      <SheetContent className="flex h-full w-full flex-col gap-0 sm:max-w-3xl" side="right">
        <SheetHeader className="bg-card border-border shrink-0 border-b p-6">
          <SheetTitle className="text-lg">{isEditMode ? 'Edit project' : 'Add project'}</SheetTitle>
          <SheetDescription className="sr-only">
            {isEditMode ? 'Edit your project details' : 'Add details about your previous project'}
          </SheetDescription>
        </SheetHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="flex min-h-0 flex-1 flex-col">
            <div className="flex-1 space-y-4 overflow-y-auto px-6 py-6">
              <DynamicForm form={form} formConfig={formConfig} />
              <Controller
                control={form.control}
                name="imageFile"
                render={({ field, fieldState }) => {
                  const handleImageChange = (file: File | string | undefined | null) => {
                    if (file instanceof File) {
                      field.onChange(file);
                    } else if (typeof file === 'string') {
                      field.onChange(file);
                    } else {
                      // User removed the image
                      field.onChange('');
                      handleDeleteAttachment();
                    }
                  };
                  return (
                    <div>
                      <ImageUploadField
                        label="Project Image (Optional)"
                        variant="card"
                        size={256}
                        value={
                          typeof field.value === 'string'
                            ? field.value
                              ? `${process.env.NEXT_PUBLIC_BASE_URL}${field.value}`
                              : undefined
                            : undefined
                        }
                        onChange={handleImageChange}
                      />
                      {fieldState.error && (
                        <p className="text-destructive mt-2 text-sm">{fieldState.error.message}</p>
                      )}
                    </div>
                  );
                }}
              />
            </div>
            <div className="border-border grid shrink-0 grid-cols-2 gap-3 border-t bg-gray-100 p-4">
              <Button type="button" variant="outline" onClick={handleCloseDrawer}>
                Cancel
              </Button>
              <Button
                type="submit"
                className="bg-sky-500 text-white hover:bg-sky-600"
                disabled={isPending}
              >
                {isPending ? 'Saving...' : isEditMode ? 'Update project' : 'Add project'}
              </Button>
            </div>
          </form>
        </Form>
      </SheetContent>
    </Sheet>
  );
};

export { ShowcaseFormDrawer };
