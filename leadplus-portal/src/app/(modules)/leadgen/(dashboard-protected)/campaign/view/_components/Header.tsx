'use client';

import { AxiosError } from 'axios';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

import { EditableField } from '@/components/EditableField';
import { StatusBadge } from '@/components/StatusBadge';
import { Button } from '@/components/ui/button';
import { appRoutes } from '@/config/routes';
import { CampaignStatus, editableCampaignStatuses } from '@/constants/Campaign';
import { usePauseCampaign, useResumeCampaign, useUpdateCampaignDetails } from '@/hooks/useCampaign';
import { convertDateISO } from '@/lib/utils/timeConversion';
import { Campaign } from '@/types/campaign.types';

import { ArrowLeft, Building, Calendar, Pause, Pencil, Play, Users } from 'lucide-react';

type HeaderProps = {
  campaign: Campaign;
};

export const Header = ({ campaign }: HeaderProps) => {
  const router = useRouter();

  const onBack = () => {
    router.push(appRoutes.leadgen.campaigns.root);
  };

  const { mutate: pauseCampaignMutate } = usePauseCampaign();
  const { mutate: resumeCampaignMutate } = useResumeCampaign();
  const { mutate: updateCampaignDetails } = useUpdateCampaignDetails();

  const handlePauseCampaign = () => {
    pauseCampaignMutate(
      {
        tenantId: campaign.tenantId,
        workspaceId: campaign.workspaceId,
        campaignId: campaign.id,
      },
      {
        onSuccess: () => {
          toast.success('Campaign paused successfully');
        },
        onError: (error: AxiosError) => {
          toast.error(error?.message || 'Failed to pause campaign');
        },
      }
    );
  };

  const handleResumeCampaign = () => {
    resumeCampaignMutate(
      {
        tenantId: campaign.tenantId,
        workspaceId: campaign.workspaceId,
        campaignId: campaign.id,
      },
      {
        onSuccess: () => {
          toast.success('Campaign resumed successfully');
        },
        onError: (error: AxiosError) => {
          toast.error(error?.message || 'Failed to resume campaign');
        },
      }
    );
  };

  const handleCampaignNameChange = (newName: string) => {
    updateCampaignDetails(
      {
        tenantId: campaign.tenantId,
        workspaceId: campaign.workspaceId,
        campaignId: campaign.id,
        requestBody: { name: newName },
      },
      {
        onSuccess: () => {
          toast.success('Campaign details updated successfully');
        },
        onError: (error: AxiosError) => {
          toast.error(error?.message || 'Failed to update campaign details');
        },
      }
    );
  };

  return (
    <div className="flex flex-col gap-4">
      <Button
        variant="ghost"
        className="text-muted-foreground hover:text-foreground -ml-3 w-fit"
        onClick={onBack}
      >
        <ArrowLeft className="mr-2 h-4 w-4" />
        Back to Campaigns
      </Button>

      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
        <div>
          <div className="flex items-center gap-3">
            <EditableField
              value={campaign.name ? campaign.name : campaign.id}
              onChange={handleCampaignNameChange}
              tooltip="Edit the campaign name"
            />
            <StatusBadge status={campaign.status} />
          </div>
          <div className="text-muted-foreground mt-2 flex items-center gap-4 text-sm">
            {campaign?.launchedAt && (
              <div className="flex items-center gap-1">
                <Calendar className="h-3.5 w-3.5" />
                Started {convertDateISO(new Date(campaign.launchedAt))}
              </div>
            )}
            {campaign.industry && (
              <div className="flex items-center gap-1">
                <Building className="h-3.5 w-3.5" />
                {campaign.industry}
              </div>
            )}
            <div className="flex items-center gap-1">
              <Users className="h-3.5 w-3.5" />
              {campaign.participatingContactsCount} Contacts
            </div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          {editableCampaignStatuses.includes(campaign.status) && (
            <Button
              variant="outline"
              onClick={() => router.push(appRoutes.leadgen.campaigns.summary(campaign.id))}
            >
              <Pencil className="mr-2 h-4 w-4" />
              Edit Campaign
            </Button>
          )}
          {campaign.status === CampaignStatus.RUNNING ? (
            <Button variant="outline" onClick={handlePauseCampaign}>
              <Pause className="mr-2 h-4 w-4" />
              Pause Campaign
            </Button>
          ) : campaign.status === CampaignStatus.PAUSED ? (
            <Button
              className="bg-green-600 text-white hover:bg-green-700"
              onClick={handleResumeCampaign}
            >
              <Play className="mr-2 h-4 w-4" />
              Resume Campaign
            </Button>
          ) : null}
        </div>
      </div>
    </div>
  );
};
