'use client';

import { UseFormReturn, FieldValues, Path } from 'react-hook-form';
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Switch } from '@/components/ui/switch';
import { Checkbox } from '@/components/ui/checkbox';
import { QuestionType } from '@/constants/questions.constants';
import { Question, Section } from '@/types/questionnaire.types';
import { cn, sortByPosition } from '@/lib/utils/helpers';

type QuestionnaireFormProps<T extends FieldValues> = {
  form: UseFormReturn<T>;
  sections: Section[];
  className?: string;
};

export function QuestionnaireForm<T extends FieldValues>({
  form,
  sections,
  className,
}: QuestionnaireFormProps<T>) {
  const renderQuestion = (question: Question, index: number) => {
    const fieldName = `questionnaire.${question.id}` as Path<T>;

    switch (question.type) {
      case QuestionType.TEXT:
        return (
          <FormField
            control={form.control}
            name={fieldName}
            render={({ field }) => (
              <FormItem>
                <div className="flex gap-3">
                  <span className="bg-primary/10 text-primary flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-sm font-semibold">
                    {index + 1}
                  </span>
                  <div className="flex-1 space-y-3">
                    <FormLabel className="text-base font-medium">{question.label}</FormLabel>
                    <FormControl>
                      <Input
                        type="text"
                        placeholder="Enter your answer"
                        className="bg-input-background"
                        {...field}
                        value={field.value || ''}
                      />
                    </FormControl>
                    <FormMessage />
                  </div>
                </div>
              </FormItem>
            )}
          />
        );

      case QuestionType.TEXTAREA:
        return (
          <FormField
            control={form.control}
            name={fieldName}
            render={({ field }) => (
              <FormItem>
                <div className="flex gap-3">
                  <span className="bg-primary/10 text-primary flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-sm font-semibold">
                    {index + 1}
                  </span>
                  <div className="flex-1 space-y-3">
                    <FormLabel className="text-base font-medium">{question.label}</FormLabel>
                    <FormControl>
                      <Textarea
                        rows={4}
                        placeholder="Enter your detailed answer"
                        className="bg-input-background max-h-40 min-h-20 resize-none overflow-y-auto"
                        {...field}
                        value={field.value || ''}
                      />
                    </FormControl>
                    <FormMessage />
                  </div>
                </div>
              </FormItem>
            )}
          />
        );

      case QuestionType.BOOLEAN:
        return (
          <FormField
            control={form.control}
            name={fieldName}
            render={({ field }) => (
              <FormItem>
                <div className="flex gap-3">
                  <span className="bg-primary/10 text-primary flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-sm font-semibold">
                    {index + 1}
                  </span>
                  <div className="flex-1 space-y-3">
                    <FormLabel className="text-base font-medium">{question.label}</FormLabel>
                    <div className="flex items-center gap-3">
                      <span className="text-muted-foreground text-sm font-medium">No</span>
                      <FormControl>
                        <Switch checked={field.value === true} onCheckedChange={field.onChange} />
                      </FormControl>
                      <span className="text-muted-foreground text-sm font-medium">Yes</span>
                    </div>
                    <FormMessage />
                  </div>
                </div>
              </FormItem>
            )}
          />
        );

      case QuestionType.MULTISELECT:
        return (
          <FormField
            control={form.control}
            name={fieldName}
            render={({ field }) => {
              const selectedValues = Array.isArray(field.value) ? (field.value as string[]) : [];
              return (
                <FormItem>
                  <div className="flex gap-3">
                    <span className="bg-primary/10 text-primary flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-sm font-semibold">
                      {index + 1}
                    </span>
                    <div className="flex-1 space-y-3">
                      <FormLabel className="text-base font-medium">{question.label}</FormLabel>
                      <FormControl>
                        <div className="flex flex-wrap gap-2">
                          {question.options?.map((option) => {
                            const isSelected = selectedValues.includes(option);
                            return (
                              <button
                                key={option}
                                type="button"
                                onClick={() => {
                                  const newValue = isSelected
                                    ? selectedValues.filter((v: string) => v !== option)
                                    : [...selectedValues, option];
                                  field.onChange(newValue);
                                }}
                                className={cn(
                                  'rounded-full px-4 py-2 text-sm font-medium transition-colors',
                                  isSelected
                                    ? 'bg-primary text-primary-foreground'
                                    : 'border-border bg-background text-foreground hover:bg-accent border'
                                )}
                              >
                                {option}
                              </button>
                            );
                          })}
                        </div>
                      </FormControl>
                      <FormMessage />
                    </div>
                  </div>
                </FormItem>
              );
            }}
          />
        );

      case QuestionType.RADIO:
        return (
          <FormField
            control={form.control}
            name={fieldName}
            render={({ field }) => (
              <FormItem>
                <div className="flex gap-3">
                  <span className="bg-primary/10 text-primary flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-sm font-semibold">
                    {index + 1}
                  </span>
                  <div className="flex-1 space-y-3">
                    <FormLabel className="text-base font-medium">{question.label}</FormLabel>
                    <FormControl>
                      <div className="space-y-2">
                        {question.options?.map((option) => (
                          <div key={option} className="flex items-center space-x-2">
                            <Checkbox
                              id={`${question.id}-${option}`}
                              checked={field.value === option}
                              onCheckedChange={(checked) => {
                                if (checked) {
                                  field.onChange(option);
                                }
                              }}
                            />
                            <label
                              htmlFor={`${question.id}-${option}`}
                              className="cursor-pointer text-sm leading-none font-normal peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                            >
                              {option}
                            </label>
                          </div>
                        ))}
                      </div>
                    </FormControl>
                    <FormMessage />
                  </div>
                </div>
              </FormItem>
            )}
          />
        );

      default:
        return null;
    }
  };

  const sortedSections = sortByPosition<Section>(sections || []);

  return (
    <div className={cn('space-y-6', className)}>
      {sortedSections?.map((section) => {
        const sortedQuestions = sortByPosition<Question>(section.questions || []);
        return (
          <div key={section.sectionId} className="space-y-4">
            {section.sectionName && (
              <h3 className="text-foreground text-md font-semibold">{section.sectionName}</h3>
            )}
            <div className="space-y-6">
              {sortedQuestions?.map((question, index) => (
                <div
                  key={question.id}
                  className="border-border bg-card space-y-3 rounded-lg border p-4"
                >
                  {renderQuestion(question, index)}
                </div>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
