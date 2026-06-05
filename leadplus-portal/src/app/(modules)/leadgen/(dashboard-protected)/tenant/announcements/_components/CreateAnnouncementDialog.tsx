'use client';

import { useCallback, useRef, useState } from 'react';
import { toast } from 'sonner';

import { useAuth } from '@/context/AuthContext';
import {
  useGetTenantDetails,
  useLaunchAnnouncement,
  useUpsertAnnouncement,
} from '@/hooks/useTenant';
import { TenantAnnouncement } from '@/types/tenant.types';
import { Modal } from '@/components/Modal';
import { AnnouncementCompose, AnnouncementComposeRef } from './wizard/AnnouncementCompose';
import {
  AnnouncementContactImport,
  AnnouncementContactImportRef,
} from './wizard/AnnouncementContactImport';
import { AnnouncementReview } from './wizard/AnnouncementReview';
import { Footer } from './wizard/Footer';
import { StepIndicator } from './wizard/StepIndicator';
import { ComposeFormData, WizardStep } from './wizard/types';

type CreateAnnouncementDialogProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  editAnnouncement?: TenantAnnouncement;
};

const CreateAnnouncementDialog = ({
  open,
  onOpenChange,
  editAnnouncement,
}: CreateAnnouncementDialogProps) => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const [currentStep, setCurrentStep] = useState<WizardStep>(WizardStep.COMPOSE);
  const [announcementId, setAnnouncementId] = useState<string | null>(editAnnouncement?.id ?? null);
  const [savedAnnouncement, setSavedAnnouncement] = useState<TenantAnnouncement | null>(
    editAnnouncement ?? null
  );
  const [hasContactSelection, setHasContactSelection] = useState(false);
  const [isImportingContacts, setIsImportingContacts] = useState(false);

  const composeRef = useRef<AnnouncementComposeRef>(null);
  const contactImportRef = useRef<AnnouncementContactImportRef>(null);

  const { mutateAsync: upsertAnnouncement, isPending: isSaving } = useUpsertAnnouncement();
  const { mutateAsync: launchAnnouncement, isPending: isLaunching } = useLaunchAnnouncement();
  const { data: tenant } = useGetTenantDetails({ tenantId });

  const handleClose = useCallback(() => {
    onOpenChange(false);
    setTimeout(() => {
      setCurrentStep(WizardStep.COMPOSE);
      setAnnouncementId(editAnnouncement?.id ?? null);
      setSavedAnnouncement(editAnnouncement ?? null);
    }, 300);
  }, [onOpenChange, editAnnouncement]);

  const handleComposeSubmit = useCallback(
    async (data: ComposeFormData) => {
      const ccEmails = data.cc
        ? data.cc
            .split(',')
            .map((e) => e.trim())
            .filter((e) => e.length > 0)
            .map((email) => ({ email }))
        : [];

      const bccEmails = data.bcc
        ? data.bcc
            .split(',')
            .map((e) => e.trim())
            .filter((e) => e.length > 0)
            .map((email) => ({ email }))
        : [];

      await upsertAnnouncement(
        {
          tenantId,
          announcementId,
          payload: {
            name: data.name,
            subject: data.subject,
            body: data.body,
            ccRecipients: ccEmails,
            bccRecipients: bccEmails,
            attachmentIds: data.attachmentIds ?? [],
          },
        },
        {
          onSuccess: (result) => {
            setAnnouncementId(result.id);
            setSavedAnnouncement(result);
            setCurrentStep(WizardStep.CONTACTS);
          },
          onError: () => {
            toast.error('Failed to save announcement. Please try again.');
          },
        }
      );
    },
    [tenantId, announcementId, upsertAnnouncement]
  );

  const handleNext = useCallback(async () => {
    if (currentStep === WizardStep.COMPOSE) {
      await composeRef.current?.submit();
    } else if (currentStep === WizardStep.CONTACTS) {
      setIsImportingContacts(true);
      try {
        await contactImportRef.current?.save();
        setCurrentStep(WizardStep.REVIEW);
      } finally {
        setIsImportingContacts(false);
      }
    } else if (currentStep === WizardStep.REVIEW) {
      if (!announcementId) return;
      await launchAnnouncement(
        { tenantId, announcementId },
        {
          onSuccess: () => {
            toast.success('Announcement launched!');
            handleClose();
          },
          onError: () => {
            toast.error('Failed to launch announcement. Please try again.');
          },
        }
      );
    }
  }, [currentStep, announcementId, tenantId, launchAnnouncement, handleClose]);

  const bodyContent = (
    <>
      {currentStep === WizardStep.COMPOSE && (
        <AnnouncementCompose
          ref={composeRef}
          fromEmail={tenant?.announcementFromEmail}
          fromName={tenant?.announcementSenderName}
          editAnnouncement={savedAnnouncement ?? editAnnouncement}
          onSubmit={handleComposeSubmit}
        />
      )}
      {announcementId && currentStep !== WizardStep.COMPOSE && (
        <div className={currentStep !== WizardStep.CONTACTS ? 'hidden' : undefined}>
          <AnnouncementContactImport
            ref={contactImportRef}
            tenantId={tenantId}
            announcementId={announcementId}
            onHasSelectionChange={setHasContactSelection}
          />
        </div>
      )}
      {currentStep === WizardStep.REVIEW && savedAnnouncement && (
        <AnnouncementReview
          tenantId={tenantId}
          announcement={savedAnnouncement}
          fromEmail={tenant?.announcementFromEmail}
          fromName={tenant?.announcementSenderName}
        />
      )}
    </>
  );

  return (
    <Modal
      isOpen={open}
      toggleOpen={handleClose}
      contentClassName="flex max-h-[90vh]! h-full w-[80vw]! max-w-350! flex-col overflow-hidden p-0 gap-0"
      headerClassName="p-6"
      bodyClassName="px-6"
      disableOutsideClose
      title={editAnnouncement ? 'Edit Announcement' : 'New Announcement'}
      content={
        <StepIndicator
          current={currentStep}
          onChange={setCurrentStep}
          announcementId={announcementId}
          hasContactSelection={hasContactSelection}
        />
      }
      body={bodyContent}
      footer={
        <Footer
          currentStep={currentStep}
          onChange={setCurrentStep}
          onNext={handleNext}
          handleClose={handleClose}
          isLoading={isSaving || isLaunching || isImportingContacts}
          disabled={currentStep === WizardStep.CONTACTS && !hasContactSelection}
        />
      }
    />
  );
};

export { CreateAnnouncementDialog };
