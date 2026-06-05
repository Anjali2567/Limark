'use client';

import { useMemo, useState } from 'react';

import { StatusBadge } from '@/components/StatusBadge';
import { CampaignContactResponse } from '@/types/campaign.types';
import { getCampaignStage } from './ActivityTimelineTab';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { ScrollArea } from '@/components/ui/scroll-area';
import { cn } from '@/lib/utils/helpers';
import {
  CampaignContactStatus as CampaignContactStatusEnum,
  EmailDeliveryStatus,
} from '@/constants/Campaign';
import { SimpleHTML } from '@/components/chat/SimpleHTML';

type CampaignContactStatusProps = {
  data: CampaignContactResponse;
};

const TERMINAL_STATUSES: Record<string, { value: string; status: string }> = {
  [CampaignContactStatusEnum.UNSUBSCRIBED]: { value: 'Unsubscribed', status: 'UNSUBSCRIBED' },
  [CampaignContactStatusEnum.BOUNCED]: { value: 'Bounced', status: 'BOUNCED' },
};

const CampaignContactStatus = ({ data }: CampaignContactStatusProps) => {
  const { currentStep, emailData, fullName, email, status } = data;

  const terminalStatus = TERMINAL_STATUSES[status];
  const stageDetails = terminalStatus ?? getCampaignStage(currentStep, emailData);
  const isReplied =
    !terminalStatus &&
    emailData.some((email) => email.emailDeliveryStatus === EmailDeliveryStatus.REPLIED);

  const [emailThreadModalOpen, setEmailThreadModalOpen] = useState(false);

  const messages = useMemo(
    () =>
      emailData?.find((emailItem) => emailItem.emailDeliveryStatus === EmailDeliveryStatus.REPLIED)
        ?.messages ?? [],
    [emailData]
  );

  const handleOpen = () => {
    if (!isReplied) return;
    setEmailThreadModalOpen(true);
  };

  return (
    <div>
      <button
        type="button"
        onClick={handleOpen}
        disabled={!isReplied}
        className={cn(!isReplied && 'cursor-default')}
        aria-label={
          isReplied
            ? `View email thread with ${fullName} (Status: ${stageDetails.value})`
            : `Status: ${stageDetails.value}`
        }
      >
        <StatusBadge value={stageDetails.value} status={stageDetails.status} />
      </button>

      {isReplied && (
        <Dialog open={emailThreadModalOpen} onOpenChange={setEmailThreadModalOpen}>
          <DialogContent className="max-h-[85vh] w-[95dvw] max-w-357.5!">
            <DialogHeader>
              <DialogTitle>Email Thread</DialogTitle>
              <DialogDescription>Conversation with {fullName}</DialogDescription>
            </DialogHeader>

            <ScrollArea className="max-h-[calc(85vh-120px)] pr-4">
              <div className="space-y-3 pr-2">
                {messages.map((message, index) => {
                  const isIncoming = message.fromAddress === email;

                  return (
                    <div
                      key={index}
                      className={cn(
                        'overflow-hidden rounded-lg border',
                        isIncoming ? 'border-green-500/30' : 'border-border'
                      )}
                    >
                      <div className="hover:bg-secondary/30 p-4 transition-colors">
                        <div className="flex items-start gap-3">
                          <div
                            className={cn(
                              'flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-semibold text-white',
                              isIncoming ? 'bg-green-600' : 'bg-sky-500'
                            )}
                          >
                            {message.fromAddress?.charAt(0)?.toUpperCase()}
                          </div>

                          <div className="min-w-0 flex-1">
                            <div className="mb-1 flex items-baseline gap-2">
                              <span className="text-foreground font-semibold">
                                {message.fromAddress}
                              </span>
                            </div>

                            <div className="text-muted-foreground mb-3 text-xs">
                              {isIncoming
                                ? 'to me'
                                : `to ${
                                    message.toAddresses?.length && message.toAddresses.length > 0
                                      ? message.toAddresses.join(', ')
                                      : 'unknown'
                                  }`}
                            </div>

                            <div className="text-foreground space-y-3 text-sm">
                              {message.subject && (
                                <p className="font-medium">Subject: {message.subject}</p>
                              )}
                              <div className="[&_a]:pointer-events-none [&_a]:text-inherit [&_a]:no-underline">
                                <SimpleHTML text={message.body} stripReplies={true} />
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  );
                })}

                {messages.length === 0 && (
                  <div className="text-muted-foreground text-sm">No email messages available.</div>
                )}
              </div>
            </ScrollArea>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
};

export { CampaignContactStatus };
