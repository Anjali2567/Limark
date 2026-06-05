import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { QuestionnaireForm } from '@/components/form/QuestionnaireForm';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Form } from '@/components/ui/form';
import { useGetIndustryList } from '@/hooks/useIndustry';
import { useGetQuestionnaireIndustry } from '@/hooks/useQuestionnaire';
import { arrayToRecord, recordToArray } from '@/lib/utils/questionnaire';
import { AnswerRequest, Vendor } from '@/types/vendor.types';

import { ArrowLeft, Check, CheckCircle2 } from 'lucide-react';

const vendorSchema = z.object({
  questionnaire: z
    .record(z.string(), z.union([z.string(), z.boolean(), z.array(z.string())]).optional())
    .optional(),
});

type FormValues = z.infer<typeof vendorSchema>;

type ChecklistTabSubmitData = {
  questionnaire: AnswerRequest[];
};

type ChecklistTabProps = {
  onSubmit: (data: ChecklistTabSubmitData) => void;
  handleBack: () => void;
  vendor?: Vendor;
  isSubmitting?: boolean;
};

const ChecklistTab = ({ onSubmit, vendor, isSubmitting, handleBack }: ChecklistTabProps) => {
  const industryList = useGetIndustryList();

  const { data: sections = [], isLoading } = useGetQuestionnaireIndustry({
    industryIds: vendor?.industryIds?.map(String) || [],
  });

  const industryLabel = useMemo(() => {
    if (!vendor?.industryIds?.length || !industryList?.length) return null;
    const labels = industryList
      .filter((industry) => vendor.industryIds?.includes(industry.value as unknown as number))
      .map((industry) => industry.label);
    return labels.length > 0 ? labels.join(' & ') : null;
  }, [industryList, vendor]);

  const initialQuestionnaire = useMemo(() => {
    return arrayToRecord(vendor?.questionnaire, sections);
  }, [vendor?.questionnaire, sections]);

  const form = useForm<FormValues>({
    resolver: zodResolver(vendorSchema),
    defaultValues: {
      questionnaire: initialQuestionnaire,
    },
  });

  useEffect(() => {
    if (sections.length > 0) {
      form.reset({
        questionnaire: initialQuestionnaire,
      });
    }
  }, [initialQuestionnaire, sections.length, form]);

  const handleFormSubmit = (data: FormValues) => {
    const questionnaireArray = recordToArray(data.questionnaire);
    onSubmit({ questionnaire: questionnaireArray });
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleFormSubmit)} className="space-y-6">
        <Card className="border-border shadow-lg">
          <CardHeader className="border-border border-b">
            <div className="mb-2 flex items-center gap-2">
              <CheckCircle2 className="h-5 w-5 text-sky-500" />
              <CardTitle>{industryLabel || 'General'} Services Checklist</CardTitle>
            </div>
            <CardDescription>
              Answer these questions to help us understand your capabilities better. This checklist
              is specifically designed for {industryLabel || 'your'} service providers.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <div className="text-muted-foreground">Loading questionnaire...</div>
              </div>
            ) : sections && sections.length > 0 ? (
              <QuestionnaireForm form={form} sections={sections} />
            ) : (
              <div className="flex items-center justify-center py-8">
                <div className="text-muted-foreground">
                  No questionnaire available for this industry yet.
                </div>
              </div>
            )}
          </CardContent>
        </Card>
        <div className="flex items-center justify-between">
          <Button type="button" variant="outline" onClick={handleBack} disabled={isSubmitting}>
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
          <Button type="submit" className="bg-primary hover:bg-primary/90" disabled={isSubmitting}>
            {isSubmitting ? (
              'Saving...'
            ) : (
              <>
                Complete Setup
                <Check className="ml-2 h-4 w-4" />
              </>
            )}
          </Button>
        </div>
      </form>
    </Form>
  );
};

export { ChecklistTab };
