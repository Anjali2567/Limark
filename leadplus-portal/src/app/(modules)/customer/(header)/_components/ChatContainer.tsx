import { MessageRender } from '@/components/chat/MessageRender';
import { ChatLoadingIndicator } from '@/components/LoadingIndicator';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import { MessageType } from '@/constants/campaign-agent.constant';
import { useChat } from '@/hooks/useChat';
import { Send } from 'lucide-react';
import { useCallback, useEffect, useRef, useState } from 'react';

type ChatMessage = {
  message: string;
  sender: MessageType;
  date: Date;
};

const ChatContainer = () => {
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [chats, setChats] = useState<ChatMessage[]>([]);
  const [inputValue, setInputValue] = useState<string>('');

  const { mutate, isPending } = useChat();

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(scrollToBottom, [chats.length, isPending]);

  const handleSendMessage = useCallback(() => {
    const trimmedMessage = inputValue.trim();
    if (!trimmedMessage) return;
    setInputValue('');

    const userMessage = {
      message: trimmedMessage,
      sender: MessageType.CUSTOMER,
      date: new Date(),
    };
    setChats((prev) => [...prev, userMessage]);

    mutate(
      {
        request: trimmedMessage,
        conversationId: conversationId || undefined,
      },
      {
        onSuccess: (data) => {
          const botMessage = {
            message: data.response,
            sender: MessageType.ASSISTANT,
            date: new Date(),
          };
          setChats((prev) => [...prev, botMessage]);
          setConversationId(data.conversationId);
        },
        onError: (error) => {
          const errorMessage = {
            message: 'Sorry, something went wrong. Please try again.',
            sender: MessageType.ASSISTANT,
            date: new Date(),
          };
          setChats((prev) => [...prev, errorMessage]);
          console.error('Chat error:', error);
        },
      }
    );
  }, [conversationId, inputValue, mutate]);

  return (
    <div className="flex h-full flex-col">
      <ScrollArea className="flex h-full flex-col gap-4 overflow-y-auto p-4">
        {chats.map((chat, index) => (
          <MessageRender key={index} message={chat.message} user={chat.sender} time={chat.date} />
        ))}
        {isPending && <ChatLoadingIndicator />}
        <div ref={messagesEndRef} />
      </ScrollArea>
      <div className="border-border bg-card flex shrink-0 items-center gap-2 border-t p-4">
        <Input
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Type your question..."
          aria-label="Type your question"
          className="flex-1"
          onKeyDown={(e) => {
            if (e.key === 'Enter') handleSendMessage();
          }}
        />
        <Button
          onClick={handleSendMessage}
          size="icon"
          disabled={!inputValue.trim()}
          aria-label="Send message"
        >
          <Send className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
};

export { ChatContainer };
