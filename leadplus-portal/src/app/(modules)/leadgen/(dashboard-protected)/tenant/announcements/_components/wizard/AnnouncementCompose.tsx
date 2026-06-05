'use client';

import { forwardRef, useCallback, useImperativeHandle, useMemo, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';

import { EmailSequenceAttachments } from '@/app/(modules)/leadgen/(dashboard-protected)/campaign/configure-sequence/_components/EmailSequenceAttachments';
import { DynamicForm } from '@/components/form/DynamicForm';
import { Form, FormMessage } from '@/components/ui/form';
import { Label } from '@/components/ui/label';
import { RichTextEditor } from '@/components/ui/rich-text-editor';
import { RichTextEditorToolbar } from '@/components/ui/rich-text-editor-toolbar';
import { EmailImageSourceType } from '@/constants/email-image.constant';
import { useAuth } from '@/context/AuthContext';
import { useGetAllAttachments } from '@/hooks/useAttachments';
import { useRichTextImageUpload } from '@/hooks/useEmailImage';
import useToggle from '@/hooks/useToggle';
import { FormElementType } from '@/types/form.types';
import { bulkItemPaginationParams } from '@/types/paginated-params';
import { TenantAnnouncement } from '@/types/tenant.types';
import { zodResolver } from '@hookform/resolvers/zod';
import { Editor } from '@tiptap/react';
import { ComposeFormData, composeSchema } from './types';

const VARIABLES = ['{firstName}', '{companyName}'];

export type AnnouncementComposeRef = {
  submit: () => Promise<void>;
};

type AnnouncementComposeProps = {
  fromEmail?: string;
  fromName?: string;
  editAnnouncement?: TenantAnnouncement;
  onSubmit: (data: ComposeFormData) => Promise<void>;
};

const AnnouncementCompose = forwardRef<AnnouncementComposeRef, AnnouncementComposeProps>(
  ({ fromEmail, fromName, editAnnouncement, onSubmit }, ref) => {
    const { authenticatedUserDetails } = useAuth();
    const { value: showAttachmentModal, setValue: setShowAttachmentModal } = useToggle();

    const { onImageUpload } = useRichTextImageUpload({
      sourceId: editAnnouncement?.id ?? '',
      sourceType: EmailImageSourceType.ANNOUNCEMENT,
    });

    const { data: attachmentsData } = useGetAllAttachments({
      tenantId: authenticatedUserDetails?.tenantId || '',
      workspaceId: authenticatedUserDetails?.workspaceId || '',
      params: bulkItemPaginationParams,
    });
    const systemDocuments = attachmentsData?.content || [];

    const [editorInstance, setEditorInstance] = useState<Editor | null>(null);
    const editorRef = useMemo<{ current: Editor | null }>(() => {
      let _current: Editor | null = null;
      return {
        get current() {
          return _current;
        },
        set current(value: Editor | null) {
          _current = value;
          setEditorInstance(value);
        },
      };
    }, []);

    const form = useForm<ComposeFormData>({
      resolver: zodResolver(composeSchema),
      values: {
        name: editAnnouncement?.name ?? '',
        subject: editAnnouncement?.subject ?? '',
        body: editAnnouncement?.body ?? '',
        cc: editAnnouncement?.ccRecipients?.map((r) => r.email).join(', ') ?? '',
        bcc: editAnnouncement?.bccRecipients?.map((r) => r.email).join(', ') ?? '',
        attachmentIds: (editAnnouncement?.attachmentIds ?? []).map(String),
      },
    });

    useImperativeHandle(ref, () => ({
      submit: () => form.handleSubmit(onSubmit)(),
    }));

    const handleAttachmentsChange = useCallback(
      (ids: string[]) => {
        form.setValue('attachmentIds', ids.map(String), { shouldDirty: true });
      },
      [form]
    );

    const formConfig = [
      [
        {
          name: 'name' as const,
          label: 'Announcement Name',
          type: FormElementType.TEXT,
          placeholder: 'e.g., Q2 Product Launch Announcement',
          required: true,
          size: 12,
        },
      ],
      [
        {
          name: 'subject' as const,
          label: 'Subject',
          type: FormElementType.TEXT,
          placeholder: 'Enter email subject...',
          required: true,
          size: 12,
        },
      ],
      [
        {
          name: 'cc' as const,
          label: 'CC',
          type: FormElementType.TEXT,
          placeholder: 'e.g., manager@company.com, team@company.com',
          tooltip: 'Comma-separated email addresses',
          size: 6,
        },
        {
          name: 'bcc' as const,
          label: 'BCC',
          type: FormElementType.TEXT,
          placeholder: 'e.g., manager@company.com, team@company.com',
          tooltip: 'Comma-separated email addresses',
          size: 6,
        },
      ],
    ];

    return (
      <Form {...form}>
        <form className="space-y-4 pb-4">
          <div className="space-y-2">
            <Label>
              From <span className="text-destructive">*</span>
            </Label>
            {fromEmail ? (
              <div className="bg-muted text-muted-foreground flex h-9 items-center rounded-md border px-3 text-sm">
                {fromName ? `${fromName} <${fromEmail}>` : fromEmail}
              </div>
            ) : (
              <div className="bg-muted/50 text-muted-foreground rounded-md border border-dashed px-3 py-2 text-sm">
                No announcement email configured.{' '}
                <a href="../settings" className="text-primary hover:underline">
                  Configure in settings
                </a>
              </div>
            )}
          </div>
          <DynamicForm className="gap-4" form={form} formConfig={formConfig} />
          <div className="space-y-2">
            <Label>
              Email Body <span className="text-destructive">*</span>
            </Label>
            <div className="flex flex-wrap items-center gap-2 text-xs">
              <span className="text-muted-foreground">Variables:</span>
              {VARIABLES.map((v) => (
                <span
                  key={v}
                  className="cursor-pointer rounded border border-sky-200 bg-sky-100 px-2 py-0.5 font-medium text-sky-700 hover:bg-sky-200"
                  onClick={() =>
                    editorRef.current
                      ? editorRef.current.chain().focus().insertContent(v).run()
                      : form.setValue('body', (form.getValues('body') || '') + v)
                  }
                >
                  {v}
                </span>
              ))}
            </div>
            <RichTextEditorToolbar
              editor={editorInstance}
              hideSaveButton
              onAttachClick={() => setShowAttachmentModal(true)}
              onImageUpload={onImageUpload}
            />
            <div className="border-border rounded-lg border">
              <Controller
                control={form.control}
                name="body"
                render={({ field: { onChange, onBlur, value } }) => (
                  <RichTextEditor
                    showToolbar={false}
                    value={value}
                    onChange={onChange}
                    onBlur={onBlur}
                    editorRef={editorRef}
                    placeholder="Compose your announcement message..."
                    className="min-h-75"
                  />
                )}
              />
            </div>
            {form.formState.errors.body && (
              <FormMessage>{form.formState.errors.body.message}</FormMessage>
            )}
          </div>
          <div className="space-y-2">
            <Label>Attachments</Label>
            <div>
              <EmailSequenceAttachments
                isOpen={showAttachmentModal}
                toggle={() => setShowAttachmentModal(false)}
                attachmentIds={form.watch('attachmentIds') || []}
                systemDocuments={systemDocuments}
                onAttachmentsChange={handleAttachmentsChange}
              />
            </div>
          </div>
        </form>
      </Form>
    );
  }
);

AnnouncementCompose.displayName = 'AnnouncementCompose';
export { AnnouncementCompose };
