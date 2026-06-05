'use client';

import { usePathname, useSearchParams } from 'next/navigation';
import { useState } from 'react';

import { ChatMessage } from '@/types/campaign-agent.types';
import { ChatHistory } from './_components/ChatHistory';
import { ChatWidget } from './_components/ChatWidget';

const LeadPlusAgent = () => {
  const searchParams = useSearchParams();
  const pathname = usePathname();

  const [chatMessage, setChatMessage] = useState<ChatMessage[]>([]);
  const [conversationId, setConversationId] = useState<string | null>(
    searchParams.get('conversationId')
  );

  const onSelectSession = (id: string | null, shouldClear = true) => {
    if (id) {
      window.history.replaceState(null, '', pathname + '?conversationId=' + id);
    } else {
      window.history.replaceState(null, '', pathname);
    }
    if (shouldClear) {
      setChatMessage([]);
    }
    setConversationId(id);
  };

  return (
    <div className="flex h-[calc(100vh-4rem)] flex-col p-8">
      <header className="mb-4">
        <h1 className="text-foreground text-3xl font-bold">LeadPlus Agent</h1>
        <p className="text-muted-foreground">Your AI-powered assistant for lead management</p>
      </header>
      <div className="grid min-h-0 flex-1 grid-cols-1 gap-6 lg:grid-cols-3">
        <ChatHistory conversationId={conversationId} onSelectSession={onSelectSession} />
        <div className="order-1 min-h-0 lg:order-2 lg:col-span-2">
          <ChatWidget
            conversationId={conversationId}
            onSelectSession={onSelectSession}
            chatMessage={chatMessage}
            setChatMessage={setChatMessage}
          />
        </div>
      </div>
    </div>
  );
};

export default LeadPlusAgent;
