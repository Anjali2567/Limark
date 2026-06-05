'use client';

import { AxiosError } from 'axios';
import { useMemo } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { EmailRecipientForm } from '@/components/emailRecipients/EmailRecipientForm';
import { MultiSelectAutoComplete } from '@/components/form/elements/MultiSelectAutocomplete';
import { FormCard } from '@/components/FormCard';
import { InfoCard } from '@/components/InfoCard';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { DAYS_OF_WEEK_OPTIONS } from '@/constants/date.constants';
import { useAuth } from '@/context/AuthContext';
import {
  useGetCampaignBasicDetails,
  useUpdateCampaignDetails,
  useUpdateCampaignRecipients,
} from '@/hooks/useCampaign';
import { useGetTenantRecipients } from '@/hooks/useTenant';
import { useGetWorkspaceDetails } from '@/hooks/useWorkspace';
import { CampaignListResponse } from '@/types/campaign.types';
import { zodResolver } from '@hookform/resolvers/zod';

import { Clock, Loader2, Mail, Save } from 'lucide-react';

const campaignSettingsSchema = z
  .object({
    ccList: z.array(z.email('Invalid email address')),
    bccList: z.array(z.email('Invalid email address')),
    enabled: z.boolean(),
    windowStart: z.string().optional(),
    windowEnd: z.string().optional(),
    sendingDays: z.array(z.string()).optional(),
  })
  .refine((d) => !d.enabled || !!d.windowStart, {
    message: 'Start time is required',
    path: ['windowStart'],
  })
  .refine((d) => !d.enabled || !!d.windowEnd, {
    message: 'End time is required',
    path: ['windowEnd'],
  })
  .refine((d) => !d.enabled || !d.windowStart || !d.windowEnd || d.windowEnd > d.windowStart, {
    message: 'End time must be after start time',
    path: ['windowEnd'],
  })
  .refine((d) => !d.enabled || (d.sendingDays && d.sendingDays.length > 0), {
    message: 'Select at least one day',
    path: ['sendingDays'],
  });

type CampaignSettingsFormValues = z.infer<typeof campaignSettingsSchema>;

type CampaignSettingsProps = {
  campaign: CampaignListResponse;
  isOpen: boolean;
  onClose: (open: boolean) => void;
};

const CampaignSettings = ({ campaign, isOpen, onClose }: CampaignSettingsProps) => {
  const { authenticatedUserDetails } = useAuth();

  const { data: campaignBasicDetails, isLoading } = useGetCampaignBasicDetails(
    {
      tenantId: authenticatedUserDetails?.tenantId || '',
      workspaceId: authenticatedUserDetails?.workspaceId || '',
      campaignId: campaign?.id || '',
    },
    isOpen
  );

  const { data: workspace } = useGetWorkspaceDetails({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
  });
  const { data: tenantRecipients } = useGetTenantRecipients({
    tenantId: authenticatedUserDetails?.tenantId || '',
  });

  const { mutateAsync: updateRecipientsAsync, isPending: isSavingRecipients } =
    useUpdateCampaignRecipients();
  const { mutateAsync: updateDetailsAsync, isPending: isSavingWindow } = useUpdateCampaignDetails();

  const existingWindow = campaignBasicDetails?.sendingWindow;

  const formValues: CampaignSettingsFormValues = useMemo(
    () => ({
      ccList:
        (campaignBasicDetails?.ccRecipients === null
          ? workspace?.ccRecipients?.map((r) => r.email)
          : campaignBasicDetails?.ccRecipients?.map((r) => r.email)) || [],
      bccList:
        (campaignBasicDetails?.bccRecipients === null
          ? workspace?.bccRecipients?.map((r) => r.email)
          : campaignBasicDetails?.bccRecipients?.map((r) => r.email)) || [],
      enabled: !!existingWindow,
      windowStart: existingWindow?.windowStart ?? '',
      windowEnd: existingWindow?.windowEnd ?? '',
      sendingDays: existingWindow?.sendingDays ?? [],
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [campaign?.id, workspace, existingWindow]
  );

  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<CampaignSettingsFormValues>({
    resolver: zodResolver(campaignSettingsSchema),
    values: formValues,
  });

  const windowEnabled = useWatch({ control, name: 'enabled' });

  const tenantDefaults = useMemo(
    () => ({
      ccList: tenantRecipients?.ccRecipients?.map((r) => r.email) || [],
      bccList: tenantRecipients?.bccRecipients?.map((r) => r.email) || [],
    }),
    [tenantRecipients]
  );

  const onSubmit = async (values: CampaignSettingsFormValues) => {
    try {
      await updateRecipientsAsync({
        tenantId: authenticatedUserDetails?.tenantId || '',
        workspaceId: authenticatedUserDetails?.workspaceId || '',
        campaignId: campaign?.id || '',
        payload: {
          ccRecipients: values.ccList.map((email) => ({ email })),
          bccRecipients: values.bccList.map((email) => ({ email })),
        },
      });
      await updateDetailsAsync({
        tenantId: authenticatedUserDetails?.tenantId || '',
        workspaceId: authenticatedUserDetails?.workspaceId || '',
        campaignId: campaign?.id || '',
        requestBody: {
          name: campaignBasicDetails?.name || campaign.name,
          sendingWindow: values.enabled
            ? {
                windowStart: values.windowStart!,
                windowEnd: values.windowEnd!,
                sendingDays: values.sendingDays!,
              }
            : null,
        },
      });
      toast.success('Campaign settings saved successfully');
      onClose(false);
    } catch (error) {
      toast.error((error as AxiosError)?.message || 'Failed to save campaign settings');
    }
  };

  const isSaving = isSavingRecipients || isSavingWindow;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="flex max-h-[90vh] w-5xl max-w-[95vw]! flex-col gap-0 overflow-hidden px-0">
        {isLoading && (
          <div className="bg-background/70 absolute inset-0 z-50 flex items-center justify-center backdrop-blur-sm">
            <div className="flex flex-col items-center gap-3">
              <Loader2 className="text-muted-foreground h-6 w-6 animate-spin" />
              <span className="text-muted-foreground text-sm">Loading campaign settings…</span>
            </div>
          </div>
        )}
        <DialogHeader className="bg-background sticky top-0 px-6 pb-4">
          <DialogTitle>Campaign Settings</DialogTitle>
          <DialogDescription>
            Configure settings for{' '}
            <span className="text-foreground font-semibold">{campaign?.name}</span>.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-1 flex-col overflow-hidden">
          <div className="flex-1 space-y-6 overflow-y-auto px-6 py-4">
            <InfoCard
              title="Campaign Override"
              description="Applied to emails sent from this campaign only. These settings override workspace defaults."
            />
            <Controller
              name="ccList"
              control={control}
              render={({ field }) => (
                <FormCard
                  icon={<Mail className="h-5 w-5 text-sky-500" />}
                  title="CC (Carbon Copy)"
                  description="Visible to all recipients. Use for team members who should be openly copied."
                >
                  <EmailRecipientForm
                    label="Current CC Recipients"
                    name="ccEmail"
                    placeholder="manager@company.com"
                    list={field.value || []}
                    defaultList={tenantDefaults.ccList}
                    onAdd={(email) => field.onChange([...field.value, email])}
                    onRemove={(email) =>
                      field.onChange(field.value.filter((e: string) => e !== email))
                    }
                    addButtonText="Add CC"
                  />
                  {errors.ccList && (
                    <p className="text-destructive text-xs">{errors.ccList.message as string}</p>
                  )}
                </FormCard>
              )}
            />
            <Controller
              name="bccList"
              control={control}
              render={({ field }) => (
                <FormCard
                  icon={<Mail className="h-5 w-5 text-sky-500" />}
                  title="BCC (Blind Carbon Copy)"
                  description="Hidden from recipients. Use for CRM logging or compliance tracking."
                >
                  <EmailRecipientForm
                    label="Current BCC Recipients"
                    name="bccEmail"
                    placeholder="compliance@company.com"
                    list={field.value || []}
                    defaultList={tenantDefaults.bccList}
                    onAdd={(email) => field.onChange([...field.value, email])}
                    onRemove={(email) =>
                      field.onChange(field.value.filter((e: string) => e !== email))
                    }
                    badgeClass="bg-purple-50 border-purple-200"
                    addButtonText="Add BCC"
                  />
                  {errors.bccList && (
                    <p className="text-destructive text-xs">{errors.bccList.message as string}</p>
                  )}
                </FormCard>
              )}
            />

            <InfoCard
              title="Timezone-Aware Scheduling"
              description="Emails are delivered during each contact's local business hours. Unknown locations send immediately."
            />

            <FormCard icon={<Clock className="h-5 w-5 text-sky-500" />} title="Sending Window">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="space-y-1">
                    <Label>Enable sending window</Label>
                    <p className="text-muted-foreground text-sm">
                      {windowEnabled
                        ? 'Emails only send during the configured window.'
                        : 'No sending window — emails send at any time.'}
                    </p>
                  </div>
                  <Controller
                    control={control}
                    name="enabled"
                    render={({ field }) => (
                      <Switch checked={field.value} onCheckedChange={field.onChange} />
                    )}
                  />
                </div>

                {windowEnabled && (
                  <>
                    <div className="flex gap-4">
                      <div className="flex-1 space-y-1">
                        <Label htmlFor="windowStart">From</Label>
                        <Controller
                          control={control}
                          name="windowStart"
                          render={({ field }) => (
                            <input
                              id="windowStart"
                              type="time"
                              className="border-input bg-background focus-visible:ring-ring w-full rounded-md border px-3 py-2 text-sm focus-visible:ring-2 focus-visible:outline-none"
                              {...field}
                            />
                          )}
                        />
                        {errors.windowStart && (
                          <p className="text-destructive text-xs">
                            {errors.windowStart.message as string}
                          </p>
                        )}
                      </div>
                      <div className="flex-1 space-y-1">
                        <Label htmlFor="windowEnd">To</Label>
                        <Controller
                          control={control}
                          name="windowEnd"
                          render={({ field }) => (
                            <input
                              id="windowEnd"
                              type="time"
                              className="border-input bg-background focus-visible:ring-ring w-full rounded-md border px-3 py-2 text-sm focus-visible:ring-2 focus-visible:outline-none"
                              {...field}
                            />
                          )}
                        />
                        {errors.windowEnd && (
                          <p className="text-destructive text-xs">
                            {errors.windowEnd.message as string}
                          </p>
                        )}
                      </div>
                    </div>

                    <div className="space-y-1">
                      <Label>Send on days</Label>
                      <Controller
                        control={control}
                        name="sendingDays"
                        render={({ field }) => (
                          <MultiSelectAutoComplete
                            items={DAYS_OF_WEEK_OPTIONS}
                            selectedItems={field.value ?? []}
                            onChange={(selected) => field.onChange(selected.map((s) => s.value))}
                            placeholder="Select days…"
                            hideSearch
                          />
                        )}
                      />
                      {errors.sendingDays && (
                        <p className="text-destructive text-xs">
                          {errors.sendingDays.message as string}
                        </p>
                      )}
                    </div>
                  </>
                )}
              </div>
            </FormCard>
          </div>

          <div className="bg-background border-border sticky bottom-0 flex justify-end border-t px-6 pt-6 pb-0">
            <Button type="submit" disabled={isSaving}>
              {isSaving ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Saving…
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  Save Changes
                </>
              )}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default CampaignSettings;
