'use client';

import { AxiosError } from 'axios';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useDebounce } from '@/hooks/useDebounce';
import { useForm, useWatch } from 'react-hook-form';
import { toast } from 'sonner';

import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { RichTextEditor } from '@/components/ui/rich-text-editor';
import { RichTextEditorToolbar } from '@/components/ui/rich-text-editor-toolbar';
import { Editor } from '@tiptap/react';
import { CampaignEmailStatus, CampaignStatus } from '@/constants/Campaign';
import { useAuth } from '@/context/AuthContext';
import { useGetAllAttachments } from '@/hooks/useAttachments';
import { useDeleteCampaignEmail, useUpdateCampaignEmail } from '@/hooks/useCampaignEmail';
import { useModal } from '@/hooks/useModal';
import useToggle from '@/hooks/useToggle';
import { plainTextEmailToHtml } from '@/lib/utils/email.utils';
import { cn } from '@/lib/utils/helpers';
import { CampaignEmail, GenerateCampaignEmailResponse } from '@/types/campaign-email.types';
import { CampaignCompanyResponse } from '@/types/campaign.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { CampaignRecipientsDrawer } from './CampaignRecipientsDrawer';
import { EmailSequenceAttachments } from './EmailSequenceAttachments';
import { EmailSequenceHeader } from './EmailSequenceHeader';
import { useGetAuthorizedMailboxes } from '@/hooks/useMailbox';
import { FormSelect } from '@/components/ui/form-select';
import { useAssignCampaignMailbox } from '@/hooks/useCampaign';
import { useRichTextImageUpload } from '@/hooks/useEmailImage';
import { EmailImageSourceType } from '@/constants/email-image.constant';
import { getEmailPlaceholders } from '@/lib/email/placeholders';

import { Building2, Clock, Users } from 'lucide-react';

type EmailSequenceProps = {
  template: CampaignEmail;
  generatedTemplate?: GenerateCampaignEmailResponse;
  onTemplateGenerate?: () => void;
  onConsume?: (stepNumber: number) => void;
  campaignId: string;
  mailboxId?: string;
  onMailboxChange?: (mailboxId: string) => void;
  initialExpanded?: boolean;
  totalContacts?: number;
  totalCompanies?: number;
  campaignCompanies?: CampaignCompanyResponse[];
  isReadOnly?: boolean;
  canDelete?: boolean;
  campaignStatus?: CampaignStatus;
};

type EmailFormData = {
  subject: string;
  bodyTemplate: string;
  delayDays: number;
  stepNumber: number;
  attachmentIds: string[];
};

const EmailSequence = ({
  template,
  generatedTemplate,
  onTemplateGenerate,
  onConsume,
  campaignId,
  mailboxId,
  onMailboxChange,
  initialExpanded = false,
  totalContacts,
  totalCompanies,
  campaignCompanies,
  isReadOnly = false,
  canDelete = false,
  campaignStatus,
}: EmailSequenceProps) => {
  const { renderModal } = useModal();
  const { value: isToggled, toggle } = useToggle(initialExpanded);
  const { value: isAttachmentOpen, toggle: attachmentToggle } = useToggle(false);
  const [isRecipientsOpen, setIsRecipientsOpen] = useState(false);

  const { authenticatedUserDetails } = useAuth();
  const { mutate: assignCampaignMailbox } = useAssignCampaignMailbox();

  const { data: authorizedMailboxes } = useGetAuthorizedMailboxes({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
  });

  const { mutate: updateCampaignEmail } = useUpdateCampaignEmail();
  const { mutate: deleteCampaignEmail } = useDeleteCampaignEmail();
  const { onImageUpload } = useRichTextImageUpload({
    sourceId: template.id,
    sourceType: EmailImageSourceType.CAMPAIGN,
  });

  const { data: systemDocuments } = useGetAllAttachments({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    params: {
      ...defaultPaginationParams,
      size: 100,
    },
  });

  const isDisabled = useMemo(() => {
    if (isReadOnly) {
      return true;
    }
    return (
      template.status === CampaignEmailStatus.RUNNING ||
      template.status === CampaignEmailStatus.PAUSED ||
      template.status === CampaignEmailStatus.COMPLETED
    );
  }, [isReadOnly, template.status]);

  const isMailboxExists = useMemo(() => {
    return authorizedMailboxes?.some((mailbox) => mailbox.id === mailboxId);
  }, [authorizedMailboxes, mailboxId]);

  const values = useMemo(() => {
    return {
      subject: template.subject,
      bodyTemplate: template.bodyTemplate,
      delayDays: template.delayDays,
      stepNumber: template.stepNumber,
      attachmentIds: template.attachmentIds || [],
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [template.id]);

  const handleMailboxChange = useCallback(
    (mailboxId: string) => {
      if (!authenticatedUserDetails) return;
      assignCampaignMailbox({
        tenantId: authenticatedUserDetails?.tenantId || '',
        workspaceId: authenticatedUserDetails?.workspaceId || '',
        campaignId: campaignId,
        mailboxId,
      });
      onMailboxChange?.(mailboxId);
    },
    [assignCampaignMailbox, authenticatedUserDetails, onMailboxChange, campaignId]
  );

  const {
    register,
    setValue,
    control,
    reset,
    handleSubmit,
    formState: { isDirty },
  } = useForm<EmailFormData>({ values });

  const delayDays = useWatch({ control, name: 'delayDays' });
  const attachmentIds = useWatch({ control, name: 'attachmentIds' });

  const editorRef = useRef<Editor | null>(null);
  const [toolbarEditor, setToolbarEditor] = useState<Editor | null>(null);
  const showToastRef = useRef(false);

  useEffect(() => {
    if (!isToggled) {
      setToolbarEditor(null);
    }
  }, [isToggled]);

  useEffect(() => {
    if (generatedTemplate && !isDisabled) {
      setValue('subject', generatedTemplate.subject, { shouldDirty: true });
      const html = plainTextEmailToHtml(generatedTemplate.body);
      setValue('bodyTemplate', html, { shouldDirty: true });
      onConsume?.(generatedTemplate.stepNumber);
    }
  }, [generatedTemplate, isDisabled, setValue, onConsume]);

  const watchedValues = useWatch({ control });

  const handleSave = useCallback(
    (debouncedValues: EmailFormData) => {
      if (!authenticatedUserDetails) return;

      updateCampaignEmail(
        {
          tenantId: authenticatedUserDetails.tenantId,
          workspaceId: authenticatedUserDetails.workspaceId,
          campaignId: campaignId,
          emailId: template.id,
          requestBody: { ...debouncedValues },
        },
        {
          onSuccess: (data) => {
            if (showToastRef.current) {
              toast.success('Email Template updated successfully');
              showToastRef.current = false;
            }
            reset(
              {
                subject: data.subject,
                bodyTemplate: data.bodyTemplate,
                delayDays: data.delayDays,
                stepNumber: data.stepNumber,
                attachmentIds: data.attachmentIds || [],
              },
              { keepValues: true }
            );
          },
        }
      );
    },
    [authenticatedUserDetails, updateCampaignEmail, campaignId, template.id, reset]
  );

  useDebounce(
    `${watchedValues.subject ?? ''}||${watchedValues.bodyTemplate ?? ''}||${watchedValues.delayDays ?? ''}||${(watchedValues.attachmentIds ?? []).join(',')}`,
    1500,
    () => {
      if (!isDirty || isDisabled) return;
      handleSubmit(handleSave)();
    }
  );

  const insertTagIntoBody = useCallback(
    (tag: string) => {
      if (editorRef.current) {
        editorRef.current.chain().focus().insertContent(` ${tag} `).run();
      } else {
        const currentBody = watchedValues.bodyTemplate || '';
        const newBody = currentBody + ` ${tag} `;
        setValue('bodyTemplate', newBody, { shouldDirty: true });
      }
    },
    [setValue, watchedValues.bodyTemplate]
  );

  const sequenceHeader =
    template.stepNumber === 1 ? 'Initial Outreach' : `Follow-up ${template.stepNumber - 1}`;

  const placeholders = getEmailPlaceholders();
  const isLiveCampaign =
    campaignStatus === CampaignStatus.RUNNING || campaignStatus === CampaignStatus.PAUSED;

  const handleDeleteCampaignEmail = useCallback(() => {
    renderModal({
      type: 'error',
      title: isLiveCampaign ? 'Delete Pending Step' : `Delete ${sequenceHeader} Email`,
      message: isLiveCampaign
        ? 'Are you sure you want to delete this pending step? Later steps will move up automatically and active contacts will be updated.'
        : `Are you sure you want to delete ${sequenceHeader} from this campaign? Later steps will move up automatically.`,
      onConfirm: () => {
        deleteCampaignEmail(
          {
            tenantId: authenticatedUserDetails?.tenantId ?? '',
            workspaceId: authenticatedUserDetails?.workspaceId ?? '',
            campaignId: campaignId,
            emailId: template.id,
          },
          {
            onSuccess: () => {
              toast.success(
                isLiveCampaign
                  ? 'Pending step deleted and sequence updated.'
                  : 'Email sequence updated successfully'
              );
            },
            onError: (error: AxiosError) => {
              toast.error(error?.message || 'Failed to delete email');
            },
          }
        );
      },
    });
  }, [
    renderModal,
    sequenceHeader,
    deleteCampaignEmail,
    authenticatedUserDetails?.tenantId,
    authenticatedUserDetails?.workspaceId,
    campaignId,
    template.id,
    isLiveCampaign,
  ]);

  return (
    <form
      className="space-y-4"
      onSubmit={(e) => {
        showToastRef.current = true;
        handleSubmit(handleSave)(e);
      }}
    >
      {template.stepNumber > 1 && (
        <div className="relative flex items-center justify-center py-2">
          <div className="bg-border absolute top-0 bottom-0 left-1/2 -z-10 w-px -translate-x-1/2" />
          <div className="bg-card border-border text-muted-foreground z-10 flex items-center gap-2 rounded-full border px-3 py-1.5 text-sm shadow-sm">
            <Clock className="h-4 w-4" />
            <span>Wait</span>
            <Input
              type="number"
              min={0}
              className="w-16"
              disabled={isDisabled}
              {...register('delayDays', {
                valueAsNumber: true,
                required: 'Delay days is required',
                min: {
                  value: 0,
                  message: 'Delay days cannot be negative',
                },
              })}
            />
            <span>days</span>
          </div>
        </div>
      )}

      <Card className="border-border relative gap-0 overflow-visible shadow-sm">
        <div
          className={cn(
            'absolute top-0 bottom-0 left-0 w-1 rounded-l-full bg-sky-500 transition-opacity',
            isToggled ? 'opacity-100' : 'opacity-50'
          )}
        />

        <EmailSequenceHeader
          isExpanded={isToggled}
          toggleTemplate={toggle}
          templateStepNumber={template.stepNumber}
          templateDelayDays={delayDays ?? template.delayDays}
          onTemplateGenerate={onTemplateGenerate}
          sequenceHeader={sequenceHeader}
          isDisabled={isDisabled}
        />

        {isToggled && (
          <CardContent className="border-border animate-in slide-in-from-top-2 p-0">
            <div className="bg-card overflow-hidden rounded-b-lg">
              {(totalContacts !== undefined || totalCompanies !== undefined) && (
                <div className="border-border flex items-center gap-2 border-b px-4 py-2.5">
                  <label className="text-muted-foreground min-w-20 text-sm">To</label>
                  <button
                    type="button"
                    className="flex h-8 items-center gap-2 rounded-md px-2 -ml-2 transition-colors hover:bg-muted/50"
                    onClick={() => setIsRecipientsOpen(true)}
                  >
                    <Users className="text-muted-foreground h-3.5 w-3.5 shrink-0" />
                    <span className="text-sm font-medium">
                      {totalContacts} {totalContacts === 1 ? 'Contact' : 'Contacts'}
                    </span>
                    <span className="text-muted-foreground text-sm">·</span>
                    <Building2 className="text-muted-foreground h-3.5 w-3.5 shrink-0" />
                    <span className="text-sm font-medium">
                      {totalCompanies} {totalCompanies === 1 ? 'Company' : 'Companies'}
                    </span>
                  </button>
                </div>
              )}
              <div className="border-border flex items-center gap-2 border-b px-4 py-2.5">
                <label className="text-muted-foreground min-w-20 text-sm">From</label>
                <FormSelect
                  placeholder={
                    authorizedMailboxes && authorizedMailboxes?.length > 0
                      ? 'Select mailbox'
                      : 'No authorized mailboxes'
                  }
                  disabled={isDisabled || template.stepNumber !== 1}
                  value={isMailboxExists ? mailboxId : undefined}
                  onValueChange={handleMailboxChange}
                  options={
                    authorizedMailboxes?.map((mailbox) => ({
                      label: mailbox.emailAddress,
                      value: mailbox.id,
                    })) || []
                  }
                  emptyMessage="No authorized mailboxes found"
                  className="h-8 border-0 bg-transparent px-0 shadow-none focus:ring-0"
                />
              </div>
              <div className="border-border flex items-center gap-2 border-b px-4 py-2.5">
                <label className="text-muted-foreground min-w-20 text-sm">Subject</label>
                <Input
                  disabled={isDisabled}
                  className="h-8 border-0 bg-transparent px-0 shadow-none focus-visible:ring-0"
                  placeholder="Subject"
                  {...register('subject', {
                    required: 'Subject line is required',
                  })}
                />
              </div>
              <div className="px-4 py-4">
                <RichTextEditor
                  value={watchedValues.bodyTemplate || ''}
                  onChange={(value) => setValue('bodyTemplate', value, { shouldDirty: true })}
                  disabled={isDisabled}
                  showToolbar={false}
                  editorRef={editorRef}
                  onEditorReady={setToolbarEditor}
                />
                <div className="mt-3 mb-3 flex items-center gap-2 text-xs">
                  <span className="text-muted-foreground">Variables:</span>
                  {placeholders.map((tag) => (
                    <button
                      key={tag.value}
                      type="button"
                      disabled={isDisabled}
                      onClick={() => insertTagIntoBody(tag.value)}
                      className="rounded border border-sky-200 bg-sky-100 px-2 py-0.5 font-medium text-sky-700 hover:bg-sky-200 disabled:opacity-50"
                    >
                      {tag.label}
                    </button>
                  ))}
                </div>
                <EmailSequenceAttachments
                  isOpen={isAttachmentOpen}
                  toggle={attachmentToggle}
                  systemDocuments={systemDocuments?.content || []}
                  attachmentIds={attachmentIds ?? []}
                  isDisabled={isDisabled}
                  onAttachmentsChange={(ids) =>
                    setValue('attachmentIds', ids, {
                      shouldDirty: true,
                      shouldTouch: true,
                    })
                  }
                />
              </div>
              <RichTextEditorToolbar
                editor={toolbarEditor}
                disabled={isDisabled}
                deleteDisabled={!canDelete}
                onAttachClick={attachmentToggle}
                onImageUpload={onImageUpload}
                onDelete={handleDeleteCampaignEmail}
              />
            </div>
          </CardContent>
        )}
      </Card>
      {campaignCompanies && (
        <CampaignRecipientsDrawer
          open={isRecipientsOpen}
          onOpenChange={setIsRecipientsOpen}
          campaignId={campaignId}
          campaignCompanies={campaignCompanies}
          totalContacts={totalContacts ?? 0}
          totalCompanies={totalCompanies ?? 0}
        />
      )}
    </form>
  );
};

export { EmailSequence };
