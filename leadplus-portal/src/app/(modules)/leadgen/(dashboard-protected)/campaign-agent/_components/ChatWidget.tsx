import { useRouter } from 'next/navigation';
import { Dispatch, SetStateAction, useEffect, useRef, useState } from 'react';

import { MessageRender } from '@/components/chat/MessageRender';
import { ChatLoadingIndicator } from '@/components/LoadingIndicator';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { appRoutes } from '@/config/routes';
import { MessageType } from '@/constants/campaign-agent.constant';
import { useAuth } from '@/context/AuthContext';
import {
  useGetConversationsById,
  useProceedCampaign,
  useSendMessage,
} from '@/hooks/useCampaignAgent';
import { ChatMessage } from '@/types/campaign-agent.types';
import { ChatFooter } from './ChatFooter';
import { ChatHeader } from './ChatHeader';

import { Loader2 } from 'lucide-react';

type ChatWidgetProps = {
  conversationId: string | null;
  onSelectSession: (id: string | null, shouldClear?: boolean) => void;
  chatMessage: ChatMessage[];
  setChatMessage: Dispatch<SetStateAction<ChatMessage[]>>;
};

const ChatWidget = ({
  conversationId,
  onSelectSession,
  chatMessage,
  setChatMessage,
}: ChatWidgetProps) => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();

  const tenantId = authenticatedUserDetails?.tenantId ?? '';
  const workspaceId = authenticatedUserDetails?.workspaceId ?? '';

  const [campaignId, setCampaignId] = useState<string | null>(null);

  const { mutate: proceedCampaign, isPending: isCampaignLaunching } = useProceedCampaign();
  const { mutate: sendMessage, isPending } = useSendMessage();
  const { data, isLoading } = useGetConversationsById(
    { tenantId, workspaceId, conversationId: conversationId ?? '' },
    { enabled: chatMessage.length === 0 }
  );

  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (data && chatMessage.length === 0) {
      const messages: ChatMessage[] = data.flatMap((item) => [
        {
          message: item.request,
          owner: MessageType.USER,
          createdAt: item.createdAt,
        },
        {
          message: item.response,
          owner: MessageType.BOT,
          createdAt: item.createdAt,
          campaignState: item.targetingCriteriaCount,
          searchPerformed: item.searchPerformed,
        },
      ]);

      setChatMessage(messages);
    }
  }, [data, conversationId, setChatMessage, chatMessage.length]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessage.length, isPending]);

  const handleSendMessage = (message: string) => {
    if (!message.trim()) return;

    setChatMessage((prev) => [...prev, { message, owner: MessageType.USER }]);

    sendMessage(
      { tenantId, workspaceId, request: message, conversationId },
      {
        onError: () => {
          setChatMessage((prev) => [
            ...prev,
            { message: 'Something went wrong. Please try again later.', owner: MessageType.BOT },
          ]);
        },
        onSuccess: (data) => {
          const cid = data.campaignId != null ? String(data.campaignId) : null;
          setCampaignId(cid);
          onSelectSession(data.conversationId, false);
          setChatMessage((prev) => [
            ...prev,
            {
              message: data.response,
              campaignId: cid ?? undefined,
              campaignState: data.campaignState
                ? {
                    ...data.campaignState,
                    noOfCompanies: data.campaignState.totalCompanies ?? 0,
                    noOfContacts: data.campaignState.totalContacts ?? 0,
                  }
                : undefined,
              searchPerformed: data.searchPerformed,
              owner: MessageType.BOT,
            },
          ]);
        },
      }
    );
  };

  const handleStartCampaign = () => {
    if (!tenantId || !workspaceId || !campaignId) return;

    proceedCampaign(
      { tenantId, workspaceId, chatMemoryId: campaignId },
      {
        onSuccess: (data) => {
          router.push(appRoutes.leadgen.campaigns.summary(String(data.id)));
        },
      }
    );
  };

  return (
    <Card className="flex h-full min-h-0 flex-col overflow-hidden lg:min-h-0">
      <CardHeader className="border-border border-b p-4!">
        <ChatHeader />
      </CardHeader>

      <div className="flex min-h-0 flex-1 flex-col">
        <ScrollArea className="min-h-0 flex-1">
          {isLoading ? (
            <div className="flex h-full items-center justify-center">
              <Loader2 className="h-5 w-5 animate-spin text-sky-500" />
            </div>
          ) : (
            <div className="flex flex-col gap-4 p-4">
              {chatMessage.map((message, index) => (
                <MessageRender
                  key={index}
                  user={message.owner}
                  message={message.message}
                  time={message.createdAt}
                  campaignId={message.campaignId}
                  campaignState={message.campaignState}
                  searchPerformed={message.searchPerformed}
                  isLast={index === chatMessage.length - 1}
                  onStartCampaign={handleStartCampaign}
                  isCampaignLaunching={isCampaignLaunching}
                />
              ))}
              {isPending && <ChatLoadingIndicator />}
              <div ref={messagesEndRef} />
            </div>
          )}
        </ScrollArea>
      </div>

      <CardContent className="border-border border-t p-4!">
        <ChatFooter handleSendMessage={handleSendMessage} isPending={isPending} />
      </CardContent>
    </Card>
  );
};

export { ChatWidget };
