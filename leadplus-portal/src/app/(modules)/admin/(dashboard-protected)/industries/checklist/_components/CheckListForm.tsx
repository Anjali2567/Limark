'use client';

import { FC, useCallback, useEffect, useMemo } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { z } from 'zod';

import { zodResolver } from '@hookform/resolvers/zod';
import { QuestionType, questionTypeOptions } from '@/constants/questions.constants';
import { CheckListQuestion, CheckListSection } from '@/types/question.types';
import { CheckListFormValues } from '../page';
import { useGetIndustryList } from '@/hooks/useIndustry';
import { Button } from '@/components/ui/button';
import { PlusIcon } from 'lucide-react';
import { DynamicForm } from '@/components/form/DynamicForm';
import { FormElementType } from '@/types/form.types';
import { Form } from '@/components/ui/form';

type CheckListFormProps = {
  selectedQuestion: CheckListQuestion | null;
  sectionList: CheckListSection[];
  resetForm: () => void;
  handleSubmit: (values: CheckListFormValues) => void;
};

const baseSchema = z.object({
  questionSectionId: z.string().min(1, 'Section is required'),
  label: z.string().min(1, 'Check List Item is required'),
  type: z.nativeEnum(QuestionType),
  industryIds: z.array(z.string()).min(1, 'Please select at least one industry.'),
  options: z.array(z.string()),
  position: z.number().nullable(),
});

const schema = baseSchema.superRefine((data, ctx) => {
  if (
    [QuestionType.MULTISELECT, QuestionType.RADIO].includes(data.type) &&
    data.options.length < 2
  ) {
    ctx.addIssue({
      path: ['options'],
      code: z.ZodIssueCode.custom,
      message: 'Please add two or more options.',
    });
  }
});

const CheckListForm: FC<CheckListFormProps> = ({
  selectedQuestion,
  sectionList,
  resetForm,
  handleSubmit,
}) => {
  const industryList = useGetIndustryList();

  const form = useForm<CheckListFormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      label: '',
      type: QuestionType.TEXT,
      questionSectionId: '',
      options: [],
      industryIds: [],
      position: null,
    },
  });

  const { handleSubmit: rhfSubmit, reset, control } = form;

  const selectedType = useWatch({
    control,
    name: 'type',
  });

  const isMultiInputType = useMemo(
    () => selectedType && [QuestionType.MULTISELECT, QuestionType.RADIO].includes(selectedType),
    [selectedType]
  );

  const resetCheckList = useCallback(() => {
    reset({
      label: '',
      type: QuestionType.TEXT,
      questionSectionId: '',
      options: [],
      industryIds: [],
      position: null,
    });
  }, [reset]);

  useEffect(() => {
    if (selectedQuestion) {
      reset({
        label: selectedQuestion.label ?? '',
        type: selectedQuestion.type ?? QuestionType.TEXT,
        questionSectionId: selectedQuestion.questionSectionId ?? '',
        options: selectedQuestion.options ?? [],
        industryIds: selectedQuestion.industryIds ?? [],
        position: selectedQuestion.position ?? null,
      });
    } else {
      resetCheckList();
    }
  }, [selectedQuestion, reset, resetCheckList]);

  const onSubmit = (values: CheckListFormValues) => {
    handleSubmit({
      id: selectedQuestion?.id || '',
      questionSectionId: values.questionSectionId,
      industryIds: values.industryIds,
      label: values.label,
      type: values.type,
      options: values.options,
      position: selectedQuestion?.position ?? null,
    });
    resetCheckList();
    resetForm();
  };

  const formConfig = useMemo(
    () => [
      [
        {
          name: 'questionSectionId' as const,
          type: FormElementType.SELECT,
          label: 'Select Section*',
          placeholder: 'Select a section',
          options: sectionList.map((q) => ({
            label: q.name,
            value: q.id,
          })),
          size: 6,
        },
        {
          name: 'industryIds' as const,
          type: FormElementType.MULTI_SELECT,
          label: 'Applies for*',
          options: [
            {
              label: 'All',
              value: 'all',
              children: industryList.map((industry) => ({
                label: industry.label,
                value: industry.value,
              })),
            },
          ],
          placeholder: 'Select industries',
          size: 6,
        },
      ],
      [
        {
          name: 'label' as const,
          type: FormElementType.TEXT,
          label: 'Check List Item*',
          placeholder: 'Enter checklist item',
          size: 6,
        },
        {
          name: 'type' as const,
          type: FormElementType.SELECT,
          label: 'Field Type*',
          placeholder: 'Select field type',
          size: 6,
          options: questionTypeOptions,
        },
        ...(isMultiInputType
          ? [
              {
                name: 'options' as const,
                type: FormElementType.AUTOCOMPLETE_SELECT,
                label: 'Option(s) Input',
                placeholder: 'Type here',
                size: 6,
              },
            ]
          : []),
      ],
    ],
    [industryList, isMultiInputType, sectionList]
  );

  return (
    <div className="border-muted flex items-start gap-10 rounded-lg border p-6">
      <Form {...form}>
        <form onSubmit={rhfSubmit(onSubmit)} className="flex h-full min-h-0 w-full flex-col gap-6">
          <DynamicForm formConfig={formConfig} form={form} />
          <div className="mt-4 flex justify-end">
            <Button title="Add Item" className="gap-3">
              <PlusIcon className="h-5 w-5" />
              Add Item
            </Button>
          </div>
        </form>
      </Form>
    </div>
  );
};

export { CheckListForm };
