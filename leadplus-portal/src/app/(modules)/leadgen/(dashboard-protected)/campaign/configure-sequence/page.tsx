'use client';

import { AxiosError } from 'axios';
import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';
import { toast } from 'sonner';

import { TooltipButton } from '@/components/TooltipButton';
import { Button } from '@/components/ui/button';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { appRoutes } from '@/config/routes';
import { CampaignEmailStatus, CampaignStatus } from '@/constants/Campaign';
import { useAuth } from '@/context/AuthContext';
import { useChat } from '@/context/ChatContext';
import { useGetCampaignById, useLaunchCampaign, useResumeCampaign } from '@/hooks/useCampaign';
import { useAddCampaignEmail, useGetCampaignEmails } from '@/hooks/useCampaignEmail';
import { GenerateCampaignEmailResponse } from '@/types/campaign-email.types';
import { CampaignCompanyResponse, CampaignListResponse } from '@/types/campaign.types';
import { cn } from '@/lib/utils/helpers';
import CampaignSettings from '../_components/CampaignSettings';
import { CampaignLaunchSuccess } from './_components/CampaignLaunchSuccess';
import { EmailSequence } from './_components/EmailSequence';
import { EmailSequenceGenerator } from './_components/EmailSequenceGenerator';
import { LoadTemplateDialog } from './_components/LoadTemplateDialog';
import { SaveTemplateDialog } from './_components/SaveTemplateDialog';

import {
  ArrowLeft,
  Download,
  Info,
  Megaphone,
  Play,
  PlusCircle,
  Save,
  Settings,
} from 'lucide-react';

import { CampaignSearchCriteria } from '../summary/CampaignSearchCriteria';

const SENDING_SCHEDULE_TIPS = [
  { label: 'Daily limit', text: 'By default 30 emails per sender account is set' },
  { label: 'Rollover', text: 'Undelivered emails roll over to the next business day' },
  { label: 'Best practice', text: 'Space emails 2-3 days apart for optimal engagement' },
  { label: 'Timing tip', text: "Send at 8-10 AM or 1-3 PM in the recipient's timezone" },
];

type LaunchCampaignButtonProps = {
  campaignStatus?: CampaignStatus;
  onLaunchCampaign?: () => void;
  onResumeCampaign?: () => void;
  onSaveChanges?: () => void;
  onSaveDraft?: () => void;
  onSettingsClick?: () => void;
  onSaveTemplate?: () => void;
  onLoadTemplate?: () => void;
  isSaveTemplateDisabled?: boolean;
  isLoadTemplateDisabled?: boolean;
};

const LaunchCampaignButton = ({
  campaignStatus,
  onLaunchCampaign,
  onResumeCampaign,
  onSaveChanges,
  onSaveDraft,
  onSettingsClick,
  onSaveTemplate,
  onLoadTemplate,
  isSaveTemplateDisabled,
  isLoadTemplateDisabled,
}: LaunchCampaignButtonProps) => {
  const primaryAction =
    campaignStatus === CampaignStatus.DRAFT
      ? {
          label: 'Launch Campaign',
          onClick: onLaunchCampaign,
          icon: <Megaphone className="mr-2 h-4 w-4" />,
          className: 'bg-sky-500 hover:bg-sky-600 text-white',
          title: 'Launch Campaign',
        }
      : campaignStatus === CampaignStatus.PAUSED
        ? {
            label: 'Resume Campaign',
            onClick: onResumeCampaign,
            icon: <Play className="mr-2 h-4 w-4" />,
            className: 'bg-green-600 hover:bg-green-700 text-white',
            title: 'Resume the campaign',
          }
        : campaignStatus === CampaignStatus.RUNNING
          ? {
              label: 'Done',
              onClick: onSaveChanges,
              icon: <Save className="mr-2 h-4 w-4" />,
              className: 'bg-sky-500 hover:bg-sky-600 text-white',
              title: 'Return to campaign view',
            }
          : null;
  const pausedSaveChangesAction =
    campaignStatus === CampaignStatus.PAUSED && onSaveChanges
      ? {
          label: 'Done',
          onClick: onSaveChanges,
          icon: <Save className="mr-2 h-4 w-4" />,
          className: 'border-slate-300 text-slate-700 hover:bg-slate-50',
          title: 'Return to campaign view',
        }
      : null;
  const draftSaveAction =
    campaignStatus === CampaignStatus.DRAFT && onSaveDraft
      ? {
          label: 'Save as Draft',
          onClick: onSaveDraft,
          icon: <Save className="mr-2 h-4 w-4" />,
          className: 'border-slate-300 text-slate-700 hover:bg-slate-50',
          title: 'Save campaign as draft',
        }
      : null;

  return (
    <div className="flex items-center gap-3">
      {onSettingsClick && (
        <TooltipButton
          variant="outline"
          icon={<Settings className="h-4 w-4" />}
          tooltip="Campaign Settings"
          className="text-black/90"
          onClick={onSettingsClick}
        />
      )}
      {onLoadTemplate && (
        <TooltipButton
          variant="outline"
          icon={<Download className="h-4 w-4" />}
          tooltip={
            isLoadTemplateDisabled
              ? 'Cannot modify templates while campaign is active'
              : 'Import Template'
          }
          className="text-black/90"
          onClick={onLoadTemplate}
          disabled={isLoadTemplateDisabled}
        />
      )}
      {onSaveTemplate && (
        <TooltipButton
          variant="outline"
          icon={<Save className="h-4 w-4" />}
          tooltip={
            isSaveTemplateDisabled
              ? 'Cannot modify templates while campaign is active'
              : 'Save as Template'
          }
          className="text-black/90"
          onClick={onSaveTemplate}
          disabled={isSaveTemplateDisabled}
        />
      )}
      {draftSaveAction && (
        <Button
          variant="outline"
          className={cn('px-8', draftSaveAction.className)}
          onClick={draftSaveAction.onClick}
          title={draftSaveAction.title}
        >
          {draftSaveAction.icon}
          {draftSaveAction.label}
        </Button>
      )}
      {pausedSaveChangesAction && (
        <Button
          variant="outline"
          className={cn('px-8', pausedSaveChangesAction.className)}
          onClick={pausedSaveChangesAction.onClick}
          title={pausedSaveChangesAction.title}
        >
          {pausedSaveChangesAction.icon}
          {pausedSaveChangesAction.label}
        </Button>
      )}
      {primaryAction && (
        <Button
          className={cn('px-8', primaryAction.className)}
          onClick={primaryAction.onClick}
          disabled={!primaryAction.onClick}
          title={primaryAction.title}
        >
          {primaryAction.icon}
          {primaryAction.label}
        </Button>
      )}
    </div>
  );
};

const ConfigureSequence = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const id = searchParams.get('id');

  const [isCampaignLaunched, setIsCampaignLaunched] = useState<boolean>(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState<boolean>(false);
  const [isSaveTemplateOpen, setIsSaveTemplateOpen] = useState<boolean>(false);
  const [isLoadTemplateOpen, setIsLoadTemplateOpen] = useState<boolean>(false);
  const [generatedTemplates, setGeneratedTemplates] = useState<GenerateCampaignEmailResponse[]>([]);
  const [currentMailboxId, setCurrentMailboxId] = useState<string>('');
  const [importKey, setImportKey] = useState(0);

  const { authenticatedUserDetails } = useAuth();
  const { resetGenerator } = useChat();
  const { mutate: launchCampaign } = useLaunchCampaign();
  const { mutate: resumeCampaign } = useResumeCampaign();
  const { mutate: addCampaignEmail } = useAddCampaignEmail();

  const {
    data = [],
    isLoading,
    refetch: refetchCampaignEmails,
  } = useGetCampaignEmails({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    campaignId: id || '',
  });

  const {
    data: campaignData,
    isLoading: campaignLoading,
    isFetching: campaignIsFetching,
  } = useGetCampaignById({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    campaignId: id || '',
  });

  const handleBack = useCallback(() => {
    window.history.back();
  }, []);

  const sortedCampaigns = useMemo(() => {
    return [...data].sort((a, b) => a.stepNumber - b.stepNumber);
  }, [data]);

  const pendingStepNumbers = useMemo(() => {
    if (
      campaignData?.status !== CampaignStatus.RUNNING &&
      campaignData?.status !== CampaignStatus.PAUSED
    ) {
      return new Set<number>();
    }

    return new Set(
      sortedCampaigns
        .filter((email) => email.status === CampaignEmailStatus.PENDING)
        .map((email) => email.stepNumber)
    );
  }, [campaignData?.status, sortedCampaigns]);

  const handleLaunchCampaign = useCallback(() => {
    const incompleteEmail = sortedCampaigns.find(
      (email) => !email.subject?.trim() || !email.bodyTemplate?.trim()
    );
    if (incompleteEmail) {
      toast.error(
        `Step ${incompleteEmail.stepNumber} is missing content. Please fill in all email subject and body fields before launching.`
      );
      return;
    }

    launchCampaign(
      {
        tenantId: authenticatedUserDetails?.tenantId || '',
        workspaceId: authenticatedUserDetails?.workspaceId || '',
        campaignId: id || '',
      },
      {
        onSuccess: () => {
          resetGenerator();
          setIsCampaignLaunched(true);
        },
        onError: (error: AxiosError) => {
          toast.error(error?.message || 'Failed to launch campaign');
        },
      }
    );
  }, [
    launchCampaign,
    resetGenerator,
    sortedCampaigns,
    authenticatedUserDetails?.tenantId,
    authenticatedUserDetails?.workspaceId,
    id,
  ]);

  const handleAddCampaignEmail = useCallback(() => {
    addCampaignEmail(
      {
        tenantId: authenticatedUserDetails?.tenantId || '',
        workspaceId: authenticatedUserDetails?.workspaceId || '',
        campaignId: id || '',
      },
      {
        onSuccess: () => {
          toast.success('Email added successfully');
        },
        onError: (error: AxiosError) => {
          toast.error(error?.message || 'Failed to add email');
        },
      }
    );
  }, [
    addCampaignEmail,
    authenticatedUserDetails?.tenantId,
    authenticatedUserDetails?.workspaceId,
    id,
  ]);

  const handleResumeCampaign = useCallback(() => {
    if (!id || !authenticatedUserDetails) return;

    resumeCampaign(
      {
        tenantId: authenticatedUserDetails.tenantId,
        workspaceId: authenticatedUserDetails.workspaceId,
        campaignId: id,
      },
      {
        onSuccess: () => {
          toast.success('Campaign resumed successfully');
          router.push(appRoutes.leadgen.campaigns.view(id));
        },
        onError: (error: AxiosError) => {
          toast.error(error?.message || 'Failed to resume campaign');
        },
      }
    );
  }, [authenticatedUserDetails, id, resumeCampaign, router]);

  const handleSaveChanges = useCallback(() => {
    if (!id) return;

    router.push(appRoutes.leadgen.campaigns.view(id));
  }, [id, router]);

  const handleSaveDraft = useCallback(() => {
    if (!id) return;

    toast.success('Campaign saved as draft');
    router.push(appRoutes.leadgen.campaigns.view(id));
  }, [id, router]);

  const handleTemplatesGenerated = useCallback(
    (templates: GenerateCampaignEmailResponse[]) => {
      const editableStepNumbers = new Set(
        sortedCampaigns
          .filter((email) => email.status === CampaignEmailStatus.PENDING)
          .map((email) => email.stepNumber)
      );

      setGeneratedTemplates(
        templates.filter((template) => editableStepNumbers.has(template.stepNumber))
      );
    },
    [sortedCampaigns]
  );

  const handleConsumeTemplate = useCallback((stepNumber: number) => {
    setGeneratedTemplates((prev) => prev.filter((t) => t.stepNumber !== stepNumber));
  }, []);

  const handleMailboxChange = useCallback((mailboxId: string) => {
    setCurrentMailboxId(mailboxId);
  }, []);

  const handleImportSuccess = useCallback(() => {
    refetchCampaignEmails().then(() => {
      setImportKey((k) => k + 1);
    });
  }, [refetchCampaignEmails]);

  const activeMailboxId = useMemo(() => {
    return currentMailboxId || campaignData?.sendingMailboxId || '';
  }, [currentMailboxId, campaignData?.sendingMailboxId]);

  const participatingData = useMemo(() => {
    const companies: CampaignCompanyResponse[] = [];

    for (const company of campaignData?.campaignCompanies ?? []) {
      const contacts = (company.campaignContacts ?? []).filter((c) => c.participating);
      if (contacts.length > 0) {
        companies.push({ ...company, campaignContacts: contacts });
      }
    }

    const totalContacts = companies.reduce((sum, c) => sum + (c.campaignContacts?.length ?? 0), 0);

    return { companies, totalContacts, totalCompanies: companies.length };
  }, [campaignData?.campaignCompanies]);

  if (isCampaignLaunched && campaignData) {
    return (
      <CampaignLaunchSuccess
        campaign={campaignData}
        campaignEmails={data}
        isLoading={campaignLoading || campaignIsFetching}
      />
    );
  }

  if (isLoading || campaignLoading) {
    return (
      <div className="flex min-h-[calc(100vh-200px)] items-center justify-center">
        <div className="relative h-16 w-16">
          <div className="border-primary absolute inset-0 animate-spin rounded-full border-4 border-t-transparent"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto w-full max-w-7xl p-4 md:p-8">
      <div className="animate-in fade-in slide-in-from-right-4 space-y-6 duration-500">
        <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
          <div>
            <div className="flex items-center gap-3">
              <h2 className="text-foreground text-3xl font-bold">Configure Sequence</h2>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Info className="text-muted-foreground h-5 w-5 cursor-pointer" />
                </TooltipTrigger>
                <TooltipContent side="right" className="max-w-lg text-start">
                  <div className="space-y-2">
                    <p className="text-sm font-semibold text-white">Sending Schedule & Limits</p>
                    <ul className="space-y-1.5 text-xs text-white">
                      {SENDING_SCHEDULE_TIPS.map(({ label, text }) => (
                        <li key={label}>
                          • <strong className="text-white">{label}:</strong> {text}
                        </li>
                      ))}
                    </ul>
                  </div>
                </TooltipContent>
              </Tooltip>
            </div>
            <p className="text-muted-foreground text-lg">
              Review and customize the emails for your leads.
            </p>
          </div>
          <LaunchCampaignButton
            campaignStatus={campaignData?.status}
            onLaunchCampaign={handleLaunchCampaign}
            onResumeCampaign={handleResumeCampaign}
            onSaveChanges={handleSaveChanges}
            onSaveDraft={handleSaveDraft}
            onSettingsClick={() => setIsSettingsOpen(true)}
            onSaveTemplate={() => setIsSaveTemplateOpen(true)}
            onLoadTemplate={() => setIsLoadTemplateOpen(true)}
            isSaveTemplateDisabled={campaignData?.status === CampaignStatus.RUNNING}
            isLoadTemplateDisabled={campaignData?.status !== CampaignStatus.DRAFT}
          />
        </div>
        {campaignData && <CampaignSearchCriteria leadFilter={campaignData.leadFilter} />}
        <EmailSequenceGenerator
          campaignId={id || ''}
          disabled={campaignData?.status === CampaignStatus.RUNNING}
          tenantId={authenticatedUserDetails?.tenantId || ''}
          workspaceId={authenticatedUserDetails?.workspaceId || ''}
          onTemplateGenerate={handleTemplatesGenerated}
        />
        {sortedCampaigns.map((template) => (
          <EmailSequence
            key={`${template.id}-${importKey}`}
            template={template}
            generatedTemplate={generatedTemplates.find((t) => t.stepNumber === template.stepNumber)}
            onConsume={handleConsumeTemplate}
            campaignId={id || ''}
            mailboxId={activeMailboxId}
            onMailboxChange={handleMailboxChange}
            initialExpanded={template.stepNumber === 1}
            totalContacts={participatingData.totalContacts}
            totalCompanies={participatingData.totalCompanies}
            campaignCompanies={participatingData.companies}
            campaignStatus={campaignData?.status}
            canDelete={
              campaignData?.status === CampaignStatus.RUNNING ||
              campaignData?.status === CampaignStatus.PAUSED
                ? pendingStepNumbers.has(template.stepNumber)
                : true
            }
          />
        ))}
        <div className="flex w-full items-center">
          <Button variant="outline" className="mx-auto px-8" onClick={handleAddCampaignEmail}>
            <PlusCircle className="mr-2 h-4 w-4" />
            Add Email
          </Button>
        </div>
        <div className="border-border mt-6 flex items-center justify-between border-t pt-4">
          <Button variant="outline" onClick={handleBack} className="px-8">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back
          </Button>
          <LaunchCampaignButton
            campaignStatus={campaignData?.status}
            onLaunchCampaign={handleLaunchCampaign}
            onResumeCampaign={handleResumeCampaign}
            onSaveChanges={handleSaveChanges}
            onSaveDraft={handleSaveDraft}
            isSaveTemplateDisabled={campaignData?.status === CampaignStatus.RUNNING}
            isLoadTemplateDisabled={campaignData?.status !== CampaignStatus.DRAFT}
          />
        </div>
      </div>
      {campaignData && (
        <CampaignSettings
          campaign={campaignData as unknown as CampaignListResponse}
          isOpen={isSettingsOpen}
          onClose={setIsSettingsOpen}
        />
      )}
      <SaveTemplateDialog
        open={isSaveTemplateOpen}
        onOpenChange={setIsSaveTemplateOpen}
        tenantId={authenticatedUserDetails?.tenantId || ''}
        campaignEmails={sortedCampaigns}
      />
      <LoadTemplateDialog
        open={isLoadTemplateOpen}
        onOpenChange={setIsLoadTemplateOpen}
        tenantId={authenticatedUserDetails?.tenantId || ''}
        workspaceId={authenticatedUserDetails?.workspaceId || ''}
        campaignId={id || ''}
        onImportSuccess={handleImportSuccess}
      />
    </div>
  );
};

export default ConfigureSequence;
