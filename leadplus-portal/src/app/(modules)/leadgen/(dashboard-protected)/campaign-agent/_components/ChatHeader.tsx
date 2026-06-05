import { CardTitle } from '@/components/ui/card';
import { Bot } from 'lucide-react';

const ChatHeader = () => {
  return (
    <div className="flex items-center gap-3">
      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-linear-to-br from-sky-500 to-blue-600">
        <Bot className="h-5 w-5 text-white" />
      </div>

      <div className="flex flex-col">
        <CardTitle className="text-base">AI Assistant</CardTitle>
        <div className="text-muted-foreground flex items-center gap-2 text-xs">
          <span className="h-2 w-2 animate-pulse rounded-full bg-green-500" />
          Online
        </div>
      </div>
    </div>
  );
};

export { ChatHeader };
