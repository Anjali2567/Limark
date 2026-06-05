import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import useToggle from '@/hooks/useToggle';
import { cn } from '@/lib/utils/helpers';
import { ChatContainer } from './ChatContainer';

import { Bot, MessageCircle, X } from 'lucide-react';

const ChatAssistant = () => {
  const { value: isOpen, toggle } = useToggle(false);
  return (
    <>
      <Button
        size="icon"
        onClick={toggle}
        aria-label={isOpen ? 'Close chat' : 'Open chat'}
        className={cn(
          'fixed right-6 bottom-6 z-50 h-14 w-14 rounded-full shadow-xl transition-all duration-300',
          'hover:scale-110',
          isOpen && 'rotate-90'
        )}
      >
        {isOpen ? (
          <X className="h-6 w-6" />
        ) : (
          <>
            <MessageCircle className="h-6 w-6" />
            <span className="bg-destructive absolute -top-1 -right-1 h-3 w-3 animate-pulse rounded-full" />
          </>
        )}
      </Button>
      <div
        className={cn(
          'fixed right-6 bottom-24 z-50 transition-all duration-300 ease-in-out',
          isOpen
            ? 'translate-y-0 scale-100 opacity-100'
            : 'pointer-events-none translate-y-4 scale-95 opacity-0'
        )}
      >
        <Card className="flex h-130 w-100 flex-col gap-0 rounded-lg border-0 shadow-2xl">
          <CardHeader className="bg-primary text-primary-foreground flex flex-row items-center gap-4 space-y-0 rounded-t-lg border-none px-4 py-3">
            <div className="bg-primary-foreground/20 flex h-9 w-9 items-center justify-center rounded-full">
              <Bot className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-sm font-semibold">LeadPlus Assistant</h3>
              <div className="flex items-center gap-2">
                <span className="h-2 w-2 rounded-full bg-green-400" />
                <span className="text-xs opacity-90">Online</span>
              </div>
            </div>
          </CardHeader>
          <CardContent className="flex-1 overflow-y-auto p-0!">
            <ChatContainer />
          </CardContent>
        </Card>
      </div>
    </>
  );
};

export { ChatAssistant };
