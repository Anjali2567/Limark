'use client';
import { useEffect, useMemo, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { QuickTemplates } from './QuickTemplates';

import { Wand2, X } from 'lucide-react';
import { Textarea } from '@/components/ui/textarea';
import { useGetChatAgentReply } from '@/hooks/useCampaign';
import { useAuth } from '@/context/AuthContext';
import { useRouter, useSearchParams } from 'next/navigation';
import { ErrorCode } from '@/constants/ErrorCode';
import { appRoutes } from '@/config/routes';
import { useChat } from '@/context/ChatContext';

const formatCampaignErrorMessage = (message: string) => {
  const lines = message
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);

  const summary = lines[0] || 'No matching leads found.';
  const searchedWith = lines.find((line) => line.startsWith('We searched with:'));
  const context = lines.find((line) => line.startsWith('Results are limited'));
  const guidance = lines.find((line) => line.startsWith('Try '));
  const reason = lines.find(
    (line) =>
      line !== summary && line !== searchedWith && line !== context && line !== guidance
  );

  return { summary, reason, searchedWith, context, guidance };
};

const CampaignGenerator = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const campaignId = searchParams.get('id');

  const { authenticatedUserDetails } = useAuth();
  const {
    generatorPrompt: inputMessage,
    setGeneratorPrompt: setInputMessage,
    generatorConversationId: conversationId,
    setGeneratorConversationId: setConversationId,
    resetGenerator: clear,
  } = useChat();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const { mutate, isPending } = useGetChatAgentReply();

  const isConversation = useMemo(() => {
    return conversationId && campaignId;
  }, [conversationId, campaignId]);
  const formattedError = useMemo(
    () => (errorMessage ? formatCampaignErrorMessage(errorMessage) : null),
    [errorMessage]
  );

  useEffect(() => {
    if (conversationId && !campaignId) {
      clear();
    }
    if (campaignId && !conversationId) {
      router.replace(appRoutes.leadgen.campaignGenerator.root);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onCardSelect = (templatePrompt?: string) => {
    if (!templatePrompt || isPending) return;
    setInputMessage(templatePrompt);
    handleGenerate(templatePrompt);
  };

  const handleGenerate = async (templatePrompt?: string) => {
    const prompt = templatePrompt || inputMessage;
    if (
      !prompt.trim() ||
      !authenticatedUserDetails?.workspaceId ||
      !authenticatedUserDetails?.tenantId
    )
      return;
    setErrorMessage(null);
    mutate(
      {
        tenantId: authenticatedUserDetails?.tenantId,
        workspaceId: authenticatedUserDetails?.workspaceId,
        requestBody: {
          conversationId,
          request: prompt,
        },
      },
      {
        onSuccess: (data) => {
          setConversationId(data.conversationId);
          window.history.replaceState(null, '', `?id=${data.campaignId}`);
          router.push(appRoutes.leadgen.campaigns.summary(data.campaignId));
        },
        onError: (error) => {
          if (error.code === ErrorCode.BAD_REQUEST) {
            setErrorMessage(error.message || 'An error occurred. Please try again.');
          }
        },
      }
    );
  };

  return (
    <div className="animate-in fade-in zoom-in-95 mx-auto flex min-h-[70vh] w-full max-w-5xl flex-col items-center justify-center gap-8 p-8 duration-500">
      <div className="max-w-2xl space-y-4 text-center">
        <h2 className="text-foreground text-3xl font-bold tracking-tight">
          What kind of leads are you looking for?
        </h2>
        <p className="text-muted-foreground text-base">
          Describe your ideal customer profile and campaign goals, and I&apos;ll find the best
          matches.
        </p>
      </div>

      <div className="w-full max-w-3xl space-y-6">
        <div>
          <Card className="border-border gap-0 overflow-hidden shadow-sm">
            <Textarea
              placeholder="E.g., Find engineers in US-based companies with 500–1,000 employees...."
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              className="bg-secondary/30 min-h-40 resize-none border-none p-6 text-base focus-visible:ring-0"
            />
            <div className="bg-secondary/50 border-border text-muted-foreground flex items-center justify-between border-t px-6 py-3 text-xs">
              <span>Try: &quot;Software engineers in Boston who work with AI/ML&quot;</span>
              {inputMessage && isConversation && (
                <Button
                  variant="ghost"
                  size="sm"
                  className="-mr-2 h-7 text-red-500 hover:bg-red-50 hover:text-red-600"
                  onClick={() => setInputMessage('')}
                >
                  <X className="mr-1 h-3 w-3" />
                  Clear
                </Button>
              )}
            </div>
          </Card>
          {formattedError && (
            <div className="mt-2 space-y-2 rounded-md border border-red-200 bg-red-50 p-3 text-xs">
              <p className="font-semibold text-red-700">{formattedError.summary}</p>
              {formattedError.reason && (
                <p className="text-slate-700">{formattedError.reason}</p>
              )}
              {formattedError.searchedWith && (
                <p className="text-slate-700">{formattedError.searchedWith}</p>
              )}
              {formattedError.context && (
                <p className="text-muted-foreground">{formattedError.context}</p>
              )}
              {formattedError.guidance && (
                <p className="font-medium text-slate-800">{formattedError.guidance}</p>
              )}
            </div>
          )}
        </div>
        <Button
          size="lg"
          className="mx-auto block h-12 w-full bg-sky-500 text-base font-semibold shadow-md transition-all hover:scale-105 hover:bg-sky-600 md:w-auto md:px-12"
          onClick={() => handleGenerate(inputMessage)}
          disabled={!inputMessage.trim() || isPending}
        >
          {isPending ? (
            <span className="flex items-center gap-2">
              <Wand2 className="h-4 w-4 animate-spin" />
              Generating...
            </span>
          ) : (
            <span className="flex items-center gap-2">
              <Wand2 className="h-4 w-4" />
              {isConversation ? 'Regenerate' : 'Generate'} Campaign
            </span>
          )}
        </Button>
      </div>
      <QuickTemplates handleGenerate={onCardSelect} />
    </div>
  );
};

export default CampaignGenerator;
