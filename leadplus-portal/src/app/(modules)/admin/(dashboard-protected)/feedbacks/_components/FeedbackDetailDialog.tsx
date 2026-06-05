import { StarRating } from '@/components/StarRating';
import { ImageCell } from '@/components/tables/ImageCell';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { FeedbackStatus } from '@/constants/user-feedback.constant';
import { getInitials } from '@/lib/utils/helpers';
import { convertDateISO } from '@/lib/utils/timeConversion';
import { Feedback } from '@/types/user-feedback.types';
import { CategoryBadge, StatusBadge } from './FeedbackBadges';

import { Reply } from 'lucide-react';

type FeedbackDetailDialogProps = {
  feedback: Feedback | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onReply: () => void;
};

const FeedbackDetailDialog = ({
  feedback,
  open,
  onOpenChange,
  onReply,
}: FeedbackDetailDialogProps) => {
  if (!feedback) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg p-0">
        <DialogHeader className="border-border bg-secondary rounded-t-2xl border-b px-8 py-4">
          <DialogTitle className="text-foreground text-lg font-semibold">
            Feedback Details
          </DialogTitle>
          <DialogDescription className="text-muted-foreground text-sm">
            View feedback details and respond to the user
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-5 px-8">
          <ImageCell
            text={feedback.username}
            subText={feedback.companyName}
            initials={getInitials(feedback.username)}
            size={44}
          />
          <div className="flex flex-wrap items-center gap-2.5">
            <CategoryBadge type={feedback.type} />
            <StatusBadge status={feedback.status} />
            <StarRating rating={feedback.rating} />
          </div>
          <div className="bg-secondary rounded-md p-4">
            <p className="text-foreground text-sm leading-relaxed">{feedback.message}</p>
            {feedback.feedbackDate && (
              <p className="text-muted-foreground mt-3 text-xs">
                Submitted {convertDateISO(new Date(feedback.feedbackDate))}
              </p>
            )}
          </div>
        </div>

        <DialogFooter className="border-border bg-secondary border-t px-8 py-4">
          <div className="flex w-full items-center justify-end gap-3">
            <Button variant="outline" size="sm" onClick={() => onOpenChange(false)}>
              Close
            </Button>
            {feedback.status !== FeedbackStatus.RESPONDED && (
              <Button size="sm" onClick={onReply}>
                <Reply className="mr-2 h-4 w-4" />
                Reply
              </Button>
            )}
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export { FeedbackDetailDialog };
