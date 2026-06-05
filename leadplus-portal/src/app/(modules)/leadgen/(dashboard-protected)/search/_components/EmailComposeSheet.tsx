'use client';

import { useCallback, useRef } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';

import { EmailSequenceAttachments } from '@/app/(modules)/leadgen/(dashboard-protected)/campaign/configure-sequence/_components/EmailSequenceAttachments';
import { Button } from '@/components/ui/button';
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
import { FormSelect } from '@/components/ui/form-select';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { Input } from '@/components/ui/input';
import { RichTextEditor } from '@/components/ui/rich-text-editor';
import { RichTextEditorToolbar } from '@/components/ui/rich-text-editor-toolbar';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { EmailImageSourceType } from '@/constants/email-image.constant';
import { useAuth } from '@/context/AuthContext';
import { useGetAllAttachments } from '@/hooks/useAttachments';
import { useRichTextImageUpload } from '@/hooks/useEmailImage';
import { useGetAuthorizedMailboxes } from '@/hooks/useMailbox';
import useToggle from '@/hooks/useToggle';
import { plainTextEmailToHtml } from '@/lib/utils/email.utils';
import { getEmailPlaceholders } from '@/lib/email/placeholders';
import { stringValidation, validateEmailList } from '@/lib/utils/validations';
import { LeadEmailPayload } from '@/types/lead-email.types';
import { bulkItemPaginationParams } from '@/types/paginated-params';
import { Recipient } from '@/types/workspace.types';
import { zodResolver } from '@hookform/resolvers/zod';
import { Editor } from '@tiptap/react';
import { AIEmailComposeDialog } from './AIEmailComposeDialog';

import { Info, Paperclip, Send, Sparkles } from 'lucide-react';

const emailComposeSchema = z.object({
  mailboxId: z.string().min(1, { message: 'Please select a sender email' }),
  toRecipients: z
    .string()
    .min(1, { message: 'At least one recipient is required' })
    .refine((val) => validateEmailList(val, true), {
      message: 'Please enter valid email addresses separated by commas',
    }),
  subject: stringValidation({ fieldName: 'Subject', min: 1 }),
  body: z
    .string()
    .min(1, { message: 'Message is required' })
    .refine((val) => val.replace(/<[^>]*>/g, '').trim().length > 0, {
      message: 'Message cannot be empty',
    }),
  ccRecipients: z
    .string()
    .optional()
    .refine((val) => validateEmailList(val, false), {
      message: 'Emails must be separated by a comma',
    }),
  attachmentIds: z.array(z.string()).optional(),
});

type EmailComposeFormValues = z.infer<typeof emailComposeSchema>;

type EmailComposeSheetProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  recipients: Recipient[];
  onSend?: (payload: LeadEmailPayload) => Promise<void>;
  isSending?: boolean;
};

export const EmailComposeSheet = ({
  open,
  onOpenChange,
  recipients,
  onSend,
  isSending = false,
}: EmailComposeSheetProps) => {
  const { authenticatedUserDetails } = useAuth();

  const { onImageUpload } = useRichTextImageUpload({
    sourceId: '',
    sourceType: EmailImageSourceType.DIRECT,
  });

  const { value: showCloseConfirmation, setValue: setShowCloseConfirmation } = useToggle();
  const { value: showAttachmentModal, setValue: setShowAttachmentModal } = useToggle();
  const { value: showAIDialog, setValue: setShowAIDialog } = useToggle();

  const editorRef = useRef<Editor | null>(null);

  const { data: attachmentsData } = useGetAllAttachments({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    params: bulkItemPaginationParams,
  });

  const { data: authorizedMailboxes } = useGetAuthorizedMailboxes({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
  });

  const systemDocuments = attachmentsData?.content || [];

  const form = useForm<EmailComposeFormValues>({
    resolver: zodResolver(emailComposeSchema),
    mode: 'onChange',
    values: {
      mailboxId: '',
      toRecipients: recipients.map((r) => r.email).join(', '),
      subject: '',
      body: '',
      ccRecipients: '',
      attachmentIds: [],
    },
  });

  const { handleSubmit, control, formState, setValue, reset, getValues } = form;
  const { isValid } = formState;

  const hasUnsavedChanges = useCallback(() => {
    const values = getValues();
    const bodyText = values.body.replace(/<[^>]*>/g, '').trim();
    return (
      values.mailboxId !== '' ||
      values.subject !== '' ||
      bodyText.length > 0 ||
      (values.ccRecipients ?? '') !== '' ||
      (values.attachmentIds?.length ?? 0) > 0
    );
  }, [getValues]);

  const handleSheetClose = useCallback(
    (shouldOpen: boolean) => {
      if (!shouldOpen && hasUnsavedChanges()) {
        setShowCloseConfirmation(true);
      } else {
        onOpenChange(shouldOpen);
        if (!shouldOpen) {
          reset();
        }
      }
    },
    [hasUnsavedChanges, onOpenChange, reset, setShowCloseConfirmation]
  );

  const handleConfirmClose = useCallback(() => {
    setShowCloseConfirmation(false);
    reset();
    onOpenChange(false);
  }, [onOpenChange, reset, setShowCloseConfirmation]);

  const onSubmit = useCallback(
    async (values: EmailComposeFormValues) => {
      if (!authenticatedUserDetails?.tenantId || !authenticatedUserDetails?.workspaceId) return;

      const toEmails = values.toRecipients
        .split(',')
        .map((e) => e.trim())
        .filter((e) => e.length > 0);

      const ccEmails = values.ccRecipients
        ? values.ccRecipients
            .split(',')
            .map((e) => e.trim())
            .filter((e) => e.length > 0)
        : [];

      const toRecipientsWithNames = toEmails.map((email) => {
        const recipientWithName = recipients.find((r) => r.email === email);
        return recipientWithName || { email };
      });

      const ccRecipientsWithNames = ccEmails.map((email) => {
        const recipientWithName = recipients.find((r) => r.email === email);
        return recipientWithName || { email };
      });

      await onSend?.({
        mailboxId: values.mailboxId,
        toRecipients: toRecipientsWithNames,
        ccRecipients: ccRecipientsWithNames,
        bccRecipients: [],
        subject: values.subject,
        body: values.body,
        attachmentIds: values.attachmentIds ?? [],
      });
      reset();
    },
    [authenticatedUserDetails, onSend, recipients, reset]
  );

  const handleAttachmentsChange = useCallback(
    (ids: string[]) => {
      setValue('attachmentIds', ids, { shouldDirty: true });
    },
    [setValue]
  );

  const placeholders = getEmailPlaceholders();

  const insertTagIntoBody = useCallback(
    (tag: string) => {
      if (editorRef.current) {
        editorRef.current.chain().focus().insertContent(` ${tag} `).run();
      } else {
        const currentBody = form.getValues('body') || '';
        setValue('body', currentBody + ` ${tag} `, { shouldDirty: true });
      }
    },
    [form, setValue]
  );

  return (
    <>
      <Sheet open={open} onOpenChange={handleSheetClose}>
        <SheetContent
          className="bg-card border-border flex w-full flex-col gap-0 p-0 sm:max-w-4xl"
          onInteractOutside={(e) => {
            e.preventDefault();
          }}
        >
          <SheetHeader className="border-border bg-muted/30 shrink-0 border-b px-8 pt-6 pb-4">
            <div className="flex flex-col items-start justify-between">
              <SheetTitle className="text-foreground mb-2 text-xl font-bold">
                Compose Email
              </SheetTitle>
              <SheetDescription className="text-muted-foreground text-sm">
                Send email to {recipients.length} selected contact
                {recipients.length !== 1 ? 's' : ''}
              </SheetDescription>
            </div>
          </SheetHeader>
          <div className="flex-1 overflow-y-auto px-8 py-6">
            <Form {...form}>
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <FormField
                  control={control}
                  name="mailboxId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-foreground text-sm font-medium">
                        From <span className="text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <FormSelect
                          placeholder={
                            authorizedMailboxes?.length
                              ? 'Select sender email'
                              : 'No authorized mailboxes'
                          }
                          value={field.value}
                          onValueChange={field.onChange}
                          options={
                            authorizedMailboxes?.map((mailbox) => ({
                              label: mailbox.emailAddress,
                              value: mailbox.id,
                            })) || []
                          }
                          emptyMessage="No authorized mailboxes found"
                          className="border-border bg-background"
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={control}
                  name="toRecipients"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-foreground flex items-center gap-1 text-sm font-medium">
                        To <span className="text-destructive">*</span>
                        <TooltipProvider>
                          <Tooltip>
                            <TooltipTrigger asChild>
                              <Info className="text-muted-foreground/60 hover:text-muted-foreground h-3.5 w-3.5 cursor-help transition-colors" />
                            </TooltipTrigger>
                            <TooltipContent side="right">
                              Each recipient will receive a separate email.
                            </TooltipContent>
                          </Tooltip>
                        </TooltipProvider>
                      </FormLabel>
                      <Tooltip>
                        <TooltipTrigger asChild>
                          <div className="w-full">
                            <FormControl>
                              <Input {...field} disabled className="border-border cursor-default" />
                            </FormControl>
                          </div>
                        </TooltipTrigger>
                        <TooltipContent
                          side="top"
                          className="pointer-events-auto max-h-48 w-lg overflow-y-auto overscroll-contain p-3"
                        >
                          <div className="space-y-1 text-sm break-all">
                            {field.value
                              .split(',')
                              .map((email) => email.trim())
                              .filter(Boolean)
                              .map((email) => (
                                <div key={email}>{email}</div>
                              ))}
                          </div>
                        </TooltipContent>
                      </Tooltip>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={control}
                  name="ccRecipients"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-foreground flex items-center gap-1 text-sm font-medium">
                        CC (Optional)
                        <TooltipProvider>
                          <Tooltip>
                            <TooltipTrigger asChild>
                              <Info className="text-muted-foreground/60 hover:text-muted-foreground h-3.5 w-3.5 cursor-help transition-colors" />
                            </TooltipTrigger>
                            <TooltipContent side="right">
                              Tenant-level CC emails will also be appended.
                            </TooltipContent>
                          </Tooltip>
                        </TooltipProvider>
                      </FormLabel>
                      <FormControl>
                        <Input
                          {...field}
                          placeholder="Enter email addresses (comma separated)"
                          className="border-border bg-background"
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={control}
                  name="subject"
                  render={({ field }) => (
                    <FormItem>
                      <div className="flex items-center justify-between">
                        <FormLabel className="text-foreground text-sm font-medium">
                          Subject <span className="text-destructive">*</span>
                        </FormLabel>
                        <Button
                          type="button"
                          variant="outline"
                          onClick={() => setShowAIDialog(true)}
                          className="border-primary text-primary hover:bg-primary hover:text-primary-foreground h-8"
                        >
                          <Sparkles className="h-3.5 w-3.5" />
                          Generate with AI
                        </Button>
                      </div>
                      <FormControl>
                        <Input
                          {...field}
                          placeholder="Enter email subject"
                          className="border-border bg-background"
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={control}
                  name="body"
                  render={({ fieldState }) => (
                    <FormItem>
                      <FormLabel className="text-foreground text-sm font-medium">
                        Message <span className="text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <div className="space-y-2">
                          <div className="flex flex-col gap-2 text-xs">
                            <span className="text-muted-foreground text-wrap">Placeholders:</span>
                            <div className="flex flex-wrap gap-2">
                              {placeholders.map((tag) => (
                                <button
                                  key={tag.value}
                                  type="button"
                                  onClick={() => insertTagIntoBody(tag.value)}
                                  className="rounded border border-sky-200 bg-sky-100 px-2 py-0.5 font-medium text-sky-700 hover:bg-sky-200"
                                >
                                  {tag.label}
                                </button>
                              ))}
                            </div>
                          </div>
                          <RichTextEditorToolbar
                            editor={editorRef.current}
                            hideSaveButton
                            onImageUpload={onImageUpload}
                          />
                          <div className="border-border rounded-lg border">
                            <Controller
                              control={control}
                              name="body"
                              render={({ field: { onChange, onBlur, value } }) => (
                                <RichTextEditor
                                  showToolbar={false}
                                  value={value}
                                  onChange={(html) => {
                                    onChange(html);
                                  }}
                                  onBlur={onBlur}
                                  editorRef={editorRef}
                                  placeholder="Compose your message..."
                                  className="min-h-75"
                                />
                              )}
                            />
                          </div>
                        </div>
                      </FormControl>
                      {fieldState.isTouched && <FormMessage />}
                    </FormItem>
                  )}
                />
                <FormField
                  control={control}
                  name="attachmentIds"
                  render={() => (
                    <FormItem>
                      <FormLabel className="text-foreground text-sm font-medium">
                        Attachments
                      </FormLabel>
                      <FormControl>
                        <div className="space-y-3">
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={() => setShowAttachmentModal(true)}
                            className="border-border"
                          >
                            <Paperclip className="mr-2 h-4 w-4" />
                            Add Attachment
                          </Button>

                          <EmailSequenceAttachments
                            isOpen={showAttachmentModal}
                            toggle={() => setShowAttachmentModal(false)}
                            attachmentIds={getValues('attachmentIds') || []}
                            systemDocuments={systemDocuments}
                            onAttachmentsChange={handleAttachmentsChange}
                          />
                        </div>
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </form>
            </Form>
          </div>
          <div className="border-border shrink-0 border-t bg-white px-8 py-4 shadow-[0_-2px_8px_rgba(0,0,0,0.08)]">
            <div className="flex w-full items-center justify-end gap-3">
              <Button
                variant="outline"
                onClick={() => handleSheetClose(false)}
                disabled={isSending}
                className="border-border text-foreground hover:bg-accent"
              >
                Cancel
              </Button>
              <Button
                onClick={handleSubmit(onSubmit)}
                disabled={!isValid || isSending}
                className="bg-primary hover:bg-primary/90 text-primary-foreground"
              >
                <Send className="mr-2 h-4 w-4" />
                {isSending ? 'Sending...' : 'Send Email'}
              </Button>
            </div>
          </div>
        </SheetContent>
      </Sheet>

      <AIEmailComposeDialog
        open={showAIDialog}
        onOpenChange={setShowAIDialog}
        onGenerated={({ subject, body }) => {
          const html = plainTextEmailToHtml(body);
          setValue('subject', subject, { shouldDirty: true, shouldValidate: true });
          setValue('body', html, { shouldDirty: true, shouldValidate: true });
          editorRef.current?.commands.setContent(html);
        }}
      />

      <Dialog open={showCloseConfirmation} onOpenChange={setShowCloseConfirmation}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Discard Changes?</DialogTitle>
            <DialogDescription>
              You have unsaved changes. Are you sure you want to discard them?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowCloseConfirmation(false)}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleConfirmClose}>
              Discard
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
};
