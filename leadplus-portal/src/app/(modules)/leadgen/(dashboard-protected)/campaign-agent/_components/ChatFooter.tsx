import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Send } from 'lucide-react';

type ChatFooterProps = {
  handleSendMessage: (message: string) => void;
  isPending: boolean;
};

const ChatFooter = ({ handleSendMessage, isPending }: ChatFooterProps) => {
  const [inputMessage, setInputMessage] = useState('');

  const handleKeyPress = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleSend = () => {
    if (inputMessage.trim()) {
      handleSendMessage(inputMessage);
      setInputMessage('');
    }
  };

  return (
    <div className="flex items-end gap-2">
      <div className="flex-1">
        <Textarea
          placeholder="Ask me anything about your campaigns, leads..."
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          className="bg-input-background max-h-[120px] min-h-[40px] resize-none"
          rows={1}
          disabled={isPending}
        />
      </div>
      <Button
        onClick={handleSend}
        disabled={!inputMessage.trim() || isPending}
        className="h-10 w-10 shrink-0 bg-sky-500 p-0 text-white hover:bg-sky-600"
      >
        <Send className="h-5 w-5" />
      </Button>
    </div>
  );
};

export { ChatFooter };
