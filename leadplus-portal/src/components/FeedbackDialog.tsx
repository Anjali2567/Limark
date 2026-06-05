'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { toast } from 'sonner';

import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '@/context/AuthContext';
import { useCreateFeedback } from '@/hooks/useFeedback';
import { FEEDBACK_CATEGORY_CONFIG, FeedbackType } from '@/constants/user-feedback.constant';
import { Button } from './ui/button';
import { Textarea } from './ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from './ui/dialog';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from './ui/form';
import { StarRating } from '@/components/StarRating';

import { CheckCircle, Send } from 'lucide-react';

const feedbackSchema = z.object({
  type: z.nativeEnum(FeedbackType),
  rating: z.number().min(1, 'Please select a rating.').max(5),
  message: z.string().min(1, 'Please enter your feedback message.').max(500),
});

type FeedbackFormValues = z.infer<typeof feedbackSchema>;

type FeedbackDialogProps = {
  open: boolean;
  toggleOpen: () => void;
};

const FeedbackDialog = ({ open, toggleOpen }: FeedbackDialogProps) => {
  const { authenticatedUserDetails } = useAuth();
  const { mutate: createFeedback, isPending } = useCreateFeedback();

  const [isSubmitted, setIsSubmitted] = useState(false);

  const form = useForm<FeedbackFormValues>({
    resolver: zodResolver(feedbackSchema),
    defaultValues: {
      type: FeedbackType.GENERAL,
      rating: 0,
      message: '',
    },
  });

  const onSubmit = (values: FeedbackFormValues) => {
    if (!authenticatedUserDetails) {
      toast.error('Unable to submit feedback. Please try again.');
      return;
    }

    createFeedback(
      {
        params: {
          tenantId: authenticatedUserDetails.tenantId,
          workspaceId: authenticatedUserDetails.workspaceId,
        },
        payload: values,
      },
      {
        onSuccess: () => {
          setIsSubmitted(true);
          form.reset();
        },
        onError: () => {
          toast.error('Failed to submit feedback. Please try again.');
        },
      }
    );
  };

  const handleOpenChange = () => {
    toggleOpen();
    setTimeout(() => {
      form.reset();
      setIsSubmitted(false);
    }, 200);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="max-w-md p-0">
        {isSubmitted ? (
          <div className="px-8 py-10 text-center">
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-green-500/10">
              <CheckCircle className="h-7 w-7 text-green-500" />
            </div>
            <h3 className="text-foreground text-lg font-semibold">Thank you!</h3>
            <p className="text-muted-foreground mt-2 text-sm">
              Your feedback has been submitted successfully. We appreciate your input!
            </p>
          </div>
        ) : (
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)}>
              <DialogHeader className="border-border bg-secondary border-b px-8 py-4">
                <DialogTitle className="text-foreground text-lg font-semibold">
                  User Feedback
                </DialogTitle>
                <DialogDescription className="text-muted-foreground text-sm">
                  Share your thoughts to help us improve LeadPlus
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-5 px-8 py-5">
                <FormField
                  control={form.control}
                  name="type"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Category</FormLabel>
                      <FormControl>
                        <div className="grid grid-cols-2 gap-2">
                          {(
                            Object.entries(FEEDBACK_CATEGORY_CONFIG) as [
                              FeedbackType,
                              (typeof FEEDBACK_CATEGORY_CONFIG)[FeedbackType],
                            ][]
                          ).map(([value, cat]) => (
                            <button
                              key={value}
                              type="button"
                              onClick={() => field.onChange(value)}
                              className={`flex items-center gap-2 rounded-md border px-3 py-2 text-sm transition-colors ${
                                field.value === value
                                  ? 'border-primary bg-primary/5 text-primary font-medium'
                                  : 'border-border bg-card text-foreground hover:border-primary/40'
                              }`}
                            >
                              <span>{cat.emoji}</span>
                              <span>{cat.label}</span>
                            </button>
                          ))}
                        </div>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="rating"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>How would you rate your experience?</FormLabel>
                      <FormControl>
                        <StarRating
                          rating={field.value}
                          size="md"
                          showLabel
                          onRate={field.onChange}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="message"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Your Feedback</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder="Tell us what's on your mind..."
                          className="border-border bg-card text-foreground placeholder:text-muted-foreground min-h-[100px] resize-none"
                          maxLength={500}
                          {...field}
                        />
                      </FormControl>
                      <div className="flex items-center justify-between">
                        <FormMessage />
                        <span className="text-muted-foreground ml-auto text-xs">
                          {field.value.length}/500
                        </span>
                      </div>
                    </FormItem>
                  )}
                />
              </div>
              <DialogFooter className="border-border bg-secondary border-t px-8 py-4">
                <div className="flex w-full items-center justify-end gap-3">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={handleOpenChange}
                    className="border-border text-foreground"
                    disabled={isPending}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    size="sm"
                    className="bg-primary text-primary-foreground hover:bg-primary/90"
                    disabled={isPending}
                  >
                    <Send className="mr-2 h-4 w-4" />
                    {isPending ? 'Submitting...' : 'Submit'}
                  </Button>
                </div>
              </DialogFooter>
            </form>
          </Form>
        )}
      </DialogContent>
    </Dialog>
  );
};

export { FeedbackDialog };
