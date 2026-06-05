import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { toast } from 'sonner';

import { zodResolver } from '@hookform/resolvers/zod';
import { FEEDBACK_REPLY_TEMPLATES } from '@/constants/user-feedback.constant';
import { useSendFeedbackReply } from '@/hooks/useFeedback';
import { Feedback } from '@/types/user-feedback.types';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';

import { Send } from 'lucide-react';

const replySchema = z.object({
  message: z.string().min(1, 'Please enter a reply message.'),
});

type ReplyFormValues = z.infer<typeof replySchema>;

type FeedbackReplyDialogProps = {
  feedback: Feedback;
  open: boolean;
  onOpenChange: (open: boolean) => void;
};

const FeedbackReplyDialog = ({ feedback, open, onOpenChange }: FeedbackReplyDialogProps) => {
  const { mutate: sendReply, isPending } = useSendFeedbackReply();

  const form = useForm<ReplyFormValues>({
    resolver: zodResolver(replySchema),
    defaultValues: { message: '' },
  });

  const onSubmit = (values: ReplyFormValues) => {
    sendReply(
      { feedbackId: feedback.id, payload: { message: values.message } },
      {
        onSuccess: () => {
          toast.success('Reply sent successfully.');
          onOpenChange(false);
          form.reset();
        },
        onError: () => toast.error('Failed to send reply.'),
      }
    );
  };

  const handleOpenChange = (next: boolean) => {
    onOpenChange(next);
    if (!next) setTimeout(() => form.reset(), 200);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="max-w-md p-0">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)}>
            <DialogHeader className="border-border bg-secondary border-b px-8 py-4">
              <DialogTitle className="text-foreground text-lg font-semibold">
                Reply to Feedback
              </DialogTitle>
              <DialogDescription className="text-muted-foreground text-sm">
                Replying to{' '}
                <span className="text-foreground font-medium">{feedback?.username}</span>
              </DialogDescription>
            </DialogHeader>

            <div className="px-8 py-5">
              <div className="bg-secondary mb-4 rounded-md p-3 text-sm">
                <p className="text-muted-foreground mb-1 text-xs font-medium uppercase">
                  Original feedback
                </p>
                <p className="text-foreground line-clamp-3">{feedback?.message}</p>
              </div>

              <FormField
                control={form.control}
                name="message"
                render={({ field }) => {
                  return (
                    <FormItem>
                      <FormLabel>Your Reply</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder="Type your reply..."
                          className="border-border bg-card text-foreground placeholder:text-muted-foreground min-h-[120px] resize-none"
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                      <div className="mt-3">
                        <p className="text-muted-foreground mb-2 text-xs font-medium">
                          Quick Templates
                        </p>
                        <div className="flex flex-wrap gap-2">
                          {FEEDBACK_REPLY_TEMPLATES.map((template) => (
                            <button
                              key={template.label}
                              type="button"
                              onClick={() => field.onChange(template?.getMessage())}
                              className="border-border text-muted-foreground hover:border-primary/40 hover:text-primary bg-card rounded-full border px-3 py-1.5 text-xs transition-colors"
                            >
                              {template.label}
                            </button>
                          ))}
                        </div>
                      </div>
                    </FormItem>
                  );
                }}
              />
            </div>
            <DialogFooter className="border-border bg-secondary border-t px-8 py-4">
              <div className="flex w-full items-center justify-end gap-3">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => handleOpenChange(false)}
                  disabled={isPending}
                >
                  Cancel
                </Button>
                <Button type="submit" size="sm" disabled={isPending}>
                  <Send className="mr-2 h-4 w-4" />
                  {isPending ? 'Sending...' : 'Send Reply'}
                </Button>
              </div>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
};

export { FeedbackReplyDialog };
