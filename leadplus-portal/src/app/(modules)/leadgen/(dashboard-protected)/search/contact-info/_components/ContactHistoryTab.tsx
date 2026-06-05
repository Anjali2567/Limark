'use client';

import { ReactNode, useEffect, useState } from 'react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  LeadContactEventCategory,
  LeadContactEventType,
} from '@/constants/lead-contact-event.constant';
import { useAuth } from '@/context/AuthContext';
import {
  useGetLeadContactEventCounts,
  useGetLeadContactEventsInfinite,
} from '@/hooks/useLeadContactEvents';
import { formatDate, formatTime } from '@/lib/utils/formatter';
import { cn } from '@/lib/utils/helpers';
import { LeadContactData } from '@/types/leadSearch.types';
import { defaultPaginationParams } from '@/types/paginated-params';

import { History, Loader2, Mail, Megaphone } from 'lucide-react';

type CategoryConfig = {
  label: string;
  icon: ReactNode;
  badgeClass: string;
  iconClass: string;
};

const CATEGORY_CONFIG: Record<LeadContactEventCategory, CategoryConfig> = {
  [LeadContactEventCategory.EMAIL]: {
    label: 'Email',
    icon: <Mail className="h-3.5 w-3.5" />,
    badgeClass: 'bg-blue-500/10 text-blue-600 border-blue-200',
    iconClass: 'bg-blue-100 text-blue-600',
  },
  [LeadContactEventCategory.CAMPAIGN]: {
    label: 'Campaign',
    icon: <Megaphone className="h-3.5 w-3.5" />,
    badgeClass: 'bg-purple-500/10 text-purple-600 border-purple-200',
    iconClass: 'bg-purple-100 text-purple-600',
  },
  [LeadContactEventCategory.NOTE]: {
    label: 'Note',
    icon: <History className="h-3.5 w-3.5" />,
    badgeClass: 'bg-green-500/10 text-green-600 border-green-200',
    iconClass: 'bg-green-100 text-green-600',
  },
};

const EVENT_TYPE_LABEL: Record<LeadContactEventType, string> = {
  [LeadContactEventType.CAMPAIGN_INITIATED]: 'Campaign Initiated',
  [LeadContactEventType.CAMPAIGN_EMAIL_SENT]: 'Campaign Email Sent',
  [LeadContactEventType.CAMPAIGN_EMAIL_REPLIED]: 'Campaign Email Replied',
  [LeadContactEventType.EMAIL_SENT]: 'Email Sent',
  [LeadContactEventType.EMAIL_OPENED]: 'Email Opened',
  [LeadContactEventType.NOTE_ADDED]: 'Note Added',
  [LeadContactEventType.NOTE_UPDATED]: 'Note Updated',
  [LeadContactEventType.NOTE_DELETED]: 'Note Deleted',
};

type FilterId = 'ALL' | LeadContactEventCategory;

type Props = {
  contact: LeadContactData;
};

export const ContactHistoryTab = ({ contact }: Props) => {
  const [activeFilter, setActiveFilter] = useState<FilterId>('ALL');

  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';

  const { data: counts, refetch: refetchCounts } = useGetLeadContactEventCounts({
    tenantId,
    contactId: contact.id,
  });

  const {
    data,
    isLoading,
    isFetchingNextPage,
    hasNextPage,
    fetchNextPage,
    refetch: refetchEvents,
  } = useGetLeadContactEventsInfinite({
    tenantId,
    contactId: contact.id,
    params: {
      ...defaultPaginationParams,
      ...(activeFilter !== 'ALL' && { category: activeFilter }),
      sort: 'createdAt,desc',
    },
  });

  useEffect(() => {
    refetchEvents();
    refetchCounts();
  }, [refetchCounts, refetchEvents]);

  const events = data?.pages.flatMap((p) => p.content) ?? [];

  const totalCount = counts?.reduce((sum, c) => sum + c.count, 0) ?? 0;

  const filters: { id: FilterId; label: string; count: number }[] = [
    { id: 'ALL', label: 'All', count: totalCount },
    ...Object.values(LeadContactEventCategory).map((cat) => ({
      id: cat as FilterId,
      label: CATEGORY_CONFIG[cat].label,
      count: counts?.find((c) => c.category === cat)?.count ?? 0,
    })),
  ];

  return (
    <div className="h-full space-y-4">
      <div className="flex flex-wrap gap-2">
        {filters.map(({ id, label, count }) => {
          const isActive = activeFilter === id;
          return (
            <Button
              key={id}
              variant="none"
              onClick={() => setActiveFilter(id)}
              className={cn(
                'inline-flex h-fit items-center gap-1.5 rounded-full border px-3 py-1 text-xs transition-colors',
                isActive
                  ? 'border-primary bg-primary/10 text-primary'
                  : 'border-border text-muted-foreground hover:border-foreground/30 hover:text-foreground'
              )}
            >
              {label}
              <span
                className={cn(
                  'inline-flex h-4 min-w-4 items-center justify-center rounded-full px-1 text-xs',
                  isActive ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground'
                )}
              >
                {count}
              </span>
            </Button>
          );
        })}
      </div>
      {isLoading ? (
        <div className="flex h-full w-full flex-1 items-center justify-center space-y-3 py-8 text-center">
          <Loader2 className="text-muted-foreground animate-spin" />
        </div>
      ) : events.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-8 text-center">
          <History className="text-muted-foreground mb-3 h-8 w-8" />
          <p className="text-muted-foreground text-sm">No interactions found</p>
        </div>
      ) : (
        <div className="relative">
          <div className="bg-border absolute top-2 bottom-2 left-3.75 w-px" />
          <div className="space-y-4">
            {events.map((event) => {
              const config = CATEGORY_CONFIG[event.category] ?? null;
              const typeLabel = EVENT_TYPE_LABEL[event.type] ?? event.type ?? '—';
              const dateStr = formatDate(event.eventAt);
              const timeStr = formatTime(event.eventAt);
              return (
                <div key={event.id} className="relative flex items-start gap-3">
                  <div
                    className={cn(
                      'relative z-10 flex h-8 w-8 shrink-0 items-center justify-center rounded-full',
                      config?.iconClass ?? 'bg-muted text-muted-foreground'
                    )}
                  >
                    {config?.icon ?? <History className="h-3.5 w-3.5" />}
                  </div>
                  <div className="min-w-0 flex-1 pt-0.5">
                    <p className="text-foreground mb-0.5 text-sm font-medium">{typeLabel}</p>
                    {event.description && (
                      <p className="text-muted-foreground mb-1.5 text-sm">{event.description}</p>
                    )}
                    <div className="text-muted-foreground flex flex-wrap items-center gap-2 text-xs">
                      {event.userName && (
                        <>
                          <span className="font-medium">{event.userName}</span>
                          <span>·</span>
                        </>
                      )}
                      {dateStr && (
                        <>
                          <span>{timeStr ? `${dateStr} at ${timeStr}` : dateStr}</span>
                          <span>·</span>
                        </>
                      )}
                      {config && (
                        <Badge
                          variant="secondary"
                          className={cn('px-1.5 py-0 text-xs', config.badgeClass)}
                        >
                          {config.label}
                        </Badge>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
          {hasNextPage && (
            <div className="mt-4 flex justify-center">
              <Button
                variant="ghost"
                size="sm"
                className="text-xs"
                disabled={isFetchingNextPage}
                onClick={() => fetchNextPage()}
              >
                {isFetchingNextPage ? 'Loading...' : 'Load more'}
              </Button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
