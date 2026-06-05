import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';

import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Industry } from '@/types/industry.types';
import { zodResolver } from '@hookform/resolvers/zod';
import { FormElementType } from '@/types/form.types';
import { DynamicForm } from '@/components/form/DynamicForm';
import { Form } from '@/components/ui/form';
import { ImageUploadField } from '@/components/form/elements/ImageUploadField';
import { getCachedImageUrl } from '@/lib/utils/helpers';

type IndustryFormValues = {
  name: string;
  slug: string;
  description: string;
  imageFile: File | string;
};

type IndustryFormProps = {
  industryData?: Industry;
  onSubmit: (payload: IndustryFormValues) => void;
  isEditMode?: boolean;
  isSubmitting?: boolean;
  onCancel?: () => void;
};

const industryFormSchema = z.object({
  name: z.string().trim().min(1, 'Industry name is required'),
  slug: z.string().trim().min(1, 'Slug is required'),
  description: z.string().trim().min(1, 'Description is required'),
  imageFile: z.union([z.instanceof(File), z.string().min(1, 'Industry image is required')]),
});

const IndustryForm = ({
  industryData,
  onSubmit,
  onCancel,
  isEditMode = false,
  isSubmitting = false,
}: IndustryFormProps) => {
  const form = useForm<IndustryFormValues>({
    resolver: zodResolver(industryFormSchema),
    values: {
      name: industryData?.name || '',
      slug: industryData?.slug || '',
      description: industryData?.description || '',
      imageFile: industryData?.image || '',
    },
  });

  const formConfig = [
    [
      {
        type: FormElementType.TEXT,
        name: 'name' as const,
        placeholder: 'e.g. Technology, Healthcare, Finance',
        label: 'Industry Name *',
        size: 12,
      },
    ],
    [
      {
        type: FormElementType.TEXT,
        name: 'slug' as const,
        placeholder: 'e.g. technology, healthcare, finance',
        label: 'Slug *',
        size: 12,
      },
    ],
    [
      {
        type: FormElementType.TEXTAREA,
        name: 'description' as const,
        placeholder: 'Brief description of this industry...',
        label: 'Description *',
        size: 12,
      },
    ],
  ];

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="flex h-full min-h-0 flex-col">
        <ScrollArea className="min-h-0 flex-1 overflow-y-auto px-4 pb-4">
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
                  field.onChange('');
                }
              };

              return (
                <div>
                  <ImageUploadField
                    label="Industry Image"
                    required
                    value={getCachedImageUrl(
                      typeof field.value === 'string' ? field.value : undefined,
                      industryData?.updatedAt
                    )}
                    onChange={handleImageChange}
                  />

                  {fieldState.error && (
                    <p className="text-destructive mt-2 flex justify-center text-sm">
                      {fieldState.error.message}
                    </p>
                  )}
                </div>
              );
            }}
          />

          <DynamicForm form={form} formConfig={formConfig} />
        </ScrollArea>

        <div className="border-border grid shrink-0 grid-cols-2 gap-3 border-t bg-gray-100 p-4">
          <Button type="button" variant="outline" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button
            type="submit"
            className="bg-sky-500 text-white hover:bg-sky-600"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Saving...' : <>{isEditMode ? 'Update Industry' : 'Add Industry'}</>}
          </Button>
        </div>
      </form>
    </Form>
  );
};

export { IndustryForm, type IndustryFormValues };
