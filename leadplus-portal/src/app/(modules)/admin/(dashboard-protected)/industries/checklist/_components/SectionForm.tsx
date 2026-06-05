'use client';

import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { zodResolver } from '@hookform/resolvers/zod';
import { CheckListSection } from '@/types/question.types';
import { FormElementType } from '@/types/form.types';
import { DynamicForm } from '@/components/form/DynamicForm';
import { Button } from '@/components/ui/button';
import { useUpsertCheckListSection } from '@/hooks/useQuestion';

import { PlusIcon } from 'lucide-react';
import { Form } from '@/components/ui/form';

type SectionFormProps = {
  selectedSection?: CheckListSection | null;
  setSelectedSection?: (section: CheckListSection | null) => void;
};

const sectionFormConfig = [
  [
    {
      name: 'name' as const,
      type: FormElementType.TEXT,
      label: 'Section Name*',
      placeholder: 'Enter section name',
      size: 12,
    },
  ],
];

const sectionSchema = z.object({
  name: z.string().min(1, 'Section Name is required').trim(),
});

type SectionFormValues = z.infer<typeof sectionSchema>;

const SectionForm = ({ selectedSection, setSelectedSection }: SectionFormProps) => {
  const { mutate } = useUpsertCheckListSection();

  const isEditing = Boolean(selectedSection?.id);
  const buttonLabel = isEditing ? 'Update Section' : 'Add Section';

  const form = useForm<SectionFormValues>({
    resolver: zodResolver(sectionSchema),
    defaultValues: {
      name: '',
    },
  });

  const onSubmit = (values: SectionFormValues) => {
    mutate(
      { sectionId: selectedSection?.id, payload: values },
      {
        onSuccess: () => {
          form.reset({ name: '' });
          setSelectedSection?.(null);
        },
      }
    );
  };

  return (
    <div className="border-muted rounded-lg border p-6">
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          className="flex h-full w-full justify-between gap-10"
        >
          <DynamicForm formConfig={sectionFormConfig} form={form} className="flex-1" />
          <Button title={buttonLabel} className="mt-5.5 shrink-0 gap-3">
            {!isEditing && <PlusIcon className="h-5 w-5" />}
            {buttonLabel}
          </Button>
        </form>
      </Form>
    </div>
  );
};

export default SectionForm;
