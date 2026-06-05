import { useEffect, useMemo } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Clock, Loader2, MessageSquare, Plus } from 'lucide-react';
import { useInView } from 'react-intersection-observer';

import { useAuth } from '@/context/AuthContext';
import { useGetConversations } from '@/hooks/useCampaignAgent';
import { defaultPaginationParams } from '@/types/paginated-params';
import { convertDate } from '@/lib/utils/timeConversion';
import { cn } from '@/lib/utils/helpers';

type ChatHistoryProps = {
  conversationId: string | null;
  onSelectSession: (id: string | null) => void;
};

const ChatHistory = ({ conversationId, onSelectSession }: ChatHistoryProps) => {
  const { authenticatedUserDetails } = useAuth();
  const { ref, inView } = useInView();

  const {
    data: conversationsData,
    isLoading,
    isFetchingNextPage,
    hasNextPage,
    fetchNextPage,
  } = useGetConversations({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    ...defaultPaginationParams,
    sort: 'createdAt,desc',
  });

  useEffect(() => {
    if (inView && hasNextPage && !isFetchingNextPage) {
      fetchNextPage();
    }
  }, [inView, hasNextPage, isFetchingNextPage, fetchNextPage]);

  const conversations = useMemo(
    () => conversationsData?.pages?.flatMap((page) => page.content) || [],
    [conversationsData]
  );

  return (
    <Card className="flex min-h-0 flex-1 flex-col">
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2 text-lg">
            <MessageSquare className="h-5 w-5 text-sky-500" />
            Chat History
          </CardTitle>
          <Button
            variant="ghost"
            size="sm"
            className="h-8 gap-1 text-sky-500 hover:bg-sky-50 hover:text-sky-600"
            onClick={() => onSelectSession?.(null)}
          >
            <Plus className="h-4 w-4" />
            <span className="text-xs">New</span>
          </Button>
        </div>
        <CardDescription className="truncate text-sm">
          View and continue previous conversations
        </CardDescription>
      </CardHeader>
      <CardContent className="flex min-h-0 flex-1 flex-col overflow-hidden border-t p-0!">
        <ScrollArea className="min-h-0 flex-1">
          {isLoading ? (
            <div className="flex h-full w-full items-center justify-center">
              <Loader2 className="h-6 w-6 animate-spin text-sky-500" />
            </div>
          ) : conversations.length === 0 ? (
            <div className="flex h-full flex-col items-center justify-center gap-3 py-10 text-center">
              <div className="rounded-full bg-sky-50 p-4">
                <MessageSquare className="h-8 w-8 text-sky-500" />
              </div>
              <div className="space-y-1">
                <p className="text-sm font-medium">No conversations found</p>
                <p className="text-muted-foreground text-xs">Start a new chat to begin</p>
              </div>
            </div>
          ) : (
            <>
              {conversations.map((chat) => (
                <button
                  key={chat.id}
                  onClick={() => onSelectSession?.(chat.conversationId)}
                  className={cn(
                    'group relative w-full border-b p-3 text-left transition-all',
                    conversationId === chat.conversationId
                      ? 'border-sky-100 bg-sky-50/50 shadow-sm'
                      : 'bg-background border-border hover:bg-secondary/50 hover:border-border'
                  )}
                >
                  {conversationId === chat.conversationId && (
                    <div className="absolute top-0 bottom-0 left-0 w-1 rounded-r-full bg-sky-500" />
                  )}
                  <div className="flex items-start gap-3">
                    <div
                      className={cn(
                        'mt-0.5 rounded-lg p-1.5 transition-colors',
                        conversationId === chat.conversationId
                          ? 'bg-sky-100 text-sky-600'
                          : 'bg-slate-100 text-slate-500 group-hover:bg-slate-200'
                      )}
                    >
                      <MessageSquare className="h-4 w-4 shrink-0" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <h4
                        className={cn(
                          'mb-1 line-clamp-1 text-sm font-medium',
                          conversationId === chat.conversationId ? 'text-sky-900' : 'text-slate-700'
                        )}
                      >
                        {chat.request || 'New Conversation'}
                      </h4>
                      <div className="flex items-center gap-1.5">
                        <Clock className="h-3 w-3 text-slate-400" />
                        <span className="text-[11px] font-medium text-slate-500">
                          {chat.createdAt ? convertDate(new Date(chat.createdAt)) : 'Recently'}
                        </span>
                      </div>
                    </div>
                  </div>
                </button>
              ))}
              {(hasNextPage || isFetchingNextPage) && (
                <div ref={ref} className="flex justify-center p-4">
                  <Loader2 className="h-6 w-6 animate-spin text-sky-500" />
                </div>
              )}
            </>
          )}
        </ScrollArea>
      </CardContent>
    </Card>
  );
};

export { ChatHistory };
