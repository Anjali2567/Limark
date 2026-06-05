import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { FeedbackStatus } from '@/constants/user-feedback.constant';
import { useModal } from '@/hooks/useModal';
import { useUpdateFeedbackStatusToReviewed } from '@/hooks/useFeedback';
import useToggle from '@/hooks/useToggle';
import { Feedback } from '@/types/user-feedback.types';
import { FeedbackReplyDialog } from './FeedbackReplyDialog';

import { CheckCircle2, MoreVertical, Reply } from 'lucide-react';

type FeedbackTableActionsProps = {
  feedback: Feedback;
};

const FeedbackTableActions = ({ feedback }: FeedbackTableActionsProps) => {
  const { value: replyOpen, setValue: setReplyOpen } = useToggle();
  const { mutate: markAsReviewed } = useUpdateFeedbackStatusToReviewed(feedback.id);
  const { renderModal } = useModal();

  const handleMarkReviewed = () => {
    renderModal({
      type: 'success',
      title: 'Mark as Reviewed',
      message: 'Are you sure you want to mark this feedback as reviewed?',
      submitButtonText: 'Mark as Reviewed',
      onConfirm: () => {
        markAsReviewed(undefined, {
          onSuccess: () => toast.success('Feedback marked as reviewed.'),
          onError: () => toast.error('Failed to update feedback status.'),
        });
      },
    });
  };

  if (feedback.status === FeedbackStatus.RESPONDED) return <></>;

  return (
    <div onClick={(e) => e.stopPropagation()}>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" className="text-foreground hover:text-foreground h-8 w-8 p-0">
            <span className="sr-only">Open menu</span>
            <MoreVertical className="h-4 w-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          {feedback.status === FeedbackStatus.NEW && (
            <DropdownMenuItem onClick={handleMarkReviewed}>
              <CheckCircle2 className="mr-2 h-4 w-4 text-green-600" />
              Mark as Reviewed
            </DropdownMenuItem>
          )}
          <DropdownMenuItem onClick={() => setReplyOpen(true)}>
            <Reply className="mr-2 h-4 w-4" />
            Reply
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
      {feedback && (
        <FeedbackReplyDialog feedback={feedback} open={replyOpen} onOpenChange={setReplyOpen} />
      )}
    </div>
  );
};

export { FeedbackTableActions };
