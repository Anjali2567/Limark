'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useMemo } from 'react';

import { Drawer } from '@/components/Drawer';
import { InfoItem } from '@/components/InfoItem';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Separator } from '@/components/ui/separator';
import { BUDGETS } from '@/constants/request-for-quote.constants';
import { useGetRequestForQuoteById } from '@/hooks/useRequestForQuote';
import { convertDate } from '@/lib/utils/timeConversion';

import {
  Building2,
  CalendarClock,
  DollarSign,
  Loader2,
  MessageSquareQuote,
  Paperclip,
} from 'lucide-react';
import { appRoutes } from '@/config/routes';

type RFQViewDrawerProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
};

const RFQViewDrawer = ({ open, onOpenChange }: RFQViewDrawerProps) => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const rfqId = searchParams.get('rfqId');
  const { data: rfqDetails, isLoading } = useGetRequestForQuoteById(rfqId || '');

  const budget = rfqDetails ? BUDGETS.find((b) => b.value === rfqDetails.budget) : null;
  const isDeadlinePassed = rfqDetails?.deadline && new Date(rfqDetails.deadline) < new Date();

  const details = useMemo(() => {
    if (!rfqDetails) return { title: '', description: '' };
    return {
      title: rfqDetails.title,
      description: `RFQ-${rfqDetails.id.slice(rfqDetails.id.length - 5, rfqDetails.id.length)} • Created ${convertDate(
        new Date(rfqDetails.createdAt)
      )}`,
    };
  }, [rfqDetails]);

  const onViewFile = (fileUrl: string) => {
    window.open(`${process.env.NEXT_PUBLIC_BASE_URL}${fileUrl}`, '_blank', 'noopener,noreferrer');
  };

  const onViewQuotations = () => {
    if (!rfqId) return;
    router.push(appRoutes.customer.rfq.quotations(rfqId));
  };

  return (
    <Drawer
      open={open}
      onOpenChange={onOpenChange}
      title={details.title}
      description={details.description}
      maxWidth="3xl"
    >
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="text-primary h-8 w-8 animate-spin" />
        </div>
      ) : !rfqDetails ? (
        <div className="flex items-center justify-center py-12">
          <p className="text-muted-foreground">RFQ not found</p>
        </div>
      ) : (
        <div className="space-y-6">
          <div>
            <h3 className="text-foreground mb-4 text-base font-semibold">Project Details</h3>
            <div className="border-border space-y-6 rounded-lg border bg-gray-200/30 p-6">
              <div>
                <Label className="text-muted-foreground mb-2 block text-sm">Description</Label>
                <p className="text-foreground text-sm leading-5 whitespace-pre-wrap">
                  {rfqDetails.description}
                </p>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <InfoItem
                  label="Budget"
                  icon={DollarSign}
                  value={budget?.label || rfqDetails.budget}
                />
                <InfoItem
                  label="Deadline"
                  icon={CalendarClock}
                  value={convertDate(new Date(rfqDetails.deadline))}
                  iconClassName={isDeadlinePassed ? 'text-red-600' : ''}
                  valueClassName={isDeadlinePassed ? 'text-red-600' : ''}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <InfoItem
                  label="Vendors Invited"
                  icon={Building2}
                  value={rfqDetails.numberOfVendorsForRfq || 0}
                />
                <InfoItem
                  label="Quotes Received"
                  icon={MessageSquareQuote}
                  value={rfqDetails.numberOfQuotationsForRfq || 0}
                />
              </div>
              <Separator />
              <div>
                <Label className="text-muted-foreground mb-3 block text-sm">Attachments</Label>
                <div className="space-y-2">
                  {rfqDetails.attachments?.map((attachment, index) => (
                    <div
                      key={index}
                      className="border-border flex items-center gap-3 rounded-lg border bg-white/50 p-3 transition-colors hover:bg-white/70"
                    >
                      <Paperclip className="text-muted-foreground h-4 w-4" />
                      <span className="text-foreground flex-1 text-sm">{attachment.fileName}</span>
                      <Button
                        variant="ghost"
                        size="sm"
                        className="text-primary hover:text-primary hover:bg-primary/10 h-8 px-3 text-sm"
                        onClick={() => onViewFile(attachment.fileUrl)}
                      >
                        View File
                      </Button>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          <div>
            <h3 className="text-foreground mb-4 text-base font-semibold">Quotes Summary</h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="border-border rounded-lg border bg-gray-200/30 p-6">
                <p className="text-muted-foreground mb-1 text-sm">Vendors Contacted</p>
                <p className="text-foreground text-2xl font-semibold">
                  {rfqDetails.numberOfVendorsForRfq || 0}
                </p>
              </div>
              <div className="border-border rounded-lg border bg-gray-200/30 p-6">
                <p className="text-muted-foreground mb-1 text-sm">Quotes Received</p>
                <p className="text-foreground text-2xl font-semibold">
                  {rfqDetails.numberOfQuotationsForRfq || 0}
                </p>
              </div>
            </div>
          </div>

          <Button
            className="bg-primary hover:bg-primary/90 text-primary-foreground w-full"
            disabled={(rfqDetails.numberOfQuotationsForRfq || 0) === 0}
            onClick={onViewQuotations}
          >
            View Quotes {`(${rfqDetails.numberOfQuotationsForRfq || 0})`}
          </Button>
        </div>
      )}
    </Drawer>
  );
};

export { RFQViewDrawer };
