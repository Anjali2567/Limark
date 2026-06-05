'use client';

import { AxiosError } from 'axios';
import { useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { useGenerateCampaignEmail } from '@/hooks/useCampaignEmail';
import useToggle from '@/hooks/useToggle';
import { cn } from '@/lib/utils/helpers';
import { GenerateCampaignEmailResponse } from '@/types/campaign-email.types';

import { Bot, ChevronDown, ChevronUp } from 'lucide-react';

type EmailSequenceGeneratorProps = {
  tenantId: string;
  workspaceId: string;
  campaignId: string;
  onTemplateGenerate: (templates: GenerateCampaignEmailResponse[]) => void;
  disabled?: boolean;
};

type FormValues = {
  aiPrompt: string;
};

const DEFAULT_PROMPT =
  'Generate a sequence of campaign emails to introduce your product or service and follow up with selected leads.';

const EmailSequenceGenerator = ({
  onTemplateGenerate,
  tenantId,
  workspaceId,
  campaignId,
  disabled = false,
}: EmailSequenceGeneratorProps) => {
  const { value: isAiSectionExpanded, toggle: toggleAiSectionExpanded } = useToggle(false);
  const { mutate, isPending } = useGenerateCampaignEmail();

  const {
    register,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm<FormValues>({
    defaultValues: {
      aiPrompt: DEFAULT_PROMPT,
    },
  });

  const onSubmit = useCallback(
    (data: FormValues) => {
      mutate(
        {
          tenantId,
          workspaceId,
          campaignId,
          requestBody: {
            userPrompt: data.aiPrompt,
          },
        },
        {
          onSuccess: (responseData) => {
            onTemplateGenerate(responseData);
            toggleAiSectionExpanded();
          },
          onError: (error: AxiosError) => {
            toast.error(
              error.message || 'An error occurred while generating the emails. Please try again.'
            );
          },
        }
      );
    },
    [mutate, tenantId, workspaceId, campaignId, onTemplateGenerate, toggleAiSectionExpanded]
  );

  return (
    <Card className="border-sky-500 bg-linear-to-br from-sky-50 to-blue-50 shadow-md">
      <CardHeader
        className={cn(
          'cursor-pointer rounded-xl transition-colors hover:bg-sky-100/70',
          !isAiSectionExpanded && 'pb-6'
        )}
        onClick={toggleAiSectionExpanded}
      >
        <div className="flex items-center justify-between">
          <div className="space-y-1.5">
            <CardTitle className="flex items-center gap-2 text-xl">
              <Bot className="h-6 w-6 text-sky-500" />
              Generate Email Sequence with AI
            </CardTitle>
            <CardDescription>Use AI to create a complete email sequence.</CardDescription>
          </div>
          <Button type="button" variant="ghost" size="sm" className="text-muted-foreground">
            {isAiSectionExpanded ? (
              <ChevronUp className="h-5 w-5" />
            ) : (
              <ChevronDown className="h-5 w-5" />
            )}
          </Button>
        </div>
      </CardHeader>
      {isAiSectionExpanded && (
        <CardContent className="animate-in slide-in-from-top-2 space-y-4 pt-0">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="aiPrompt" className="text-sm font-medium">
                AI Prompt
              </Label>
              <Textarea
                id="aiPrompt"
                placeholder="Generate a sequence with 4 emails with 4 day gaps."
                className="bg-card min-h-24"
                {...register('aiPrompt', { required: true })}
                disabled={isSubmitting || isPending || disabled}
              />
            </div>

            <Button
              type="submit"
              disabled={isSubmitting || isPending || disabled}
              className="bg-sky-500 text-white hover:bg-sky-600"
            >
              <Bot className="mr-2 h-4 w-4" />
              {isSubmitting || isPending ? 'Generating...' : 'Generate Sequence'}
            </Button>
          </form>
        </CardContent>
      )}
    </Card>
  );
};

export { EmailSequenceGenerator };
