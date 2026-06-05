import { AxiosError } from 'axios';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

import { ActionMenu } from '@/components/tables/ActionMenu';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { usePauseCampaign, useResumeCampaign } from '@/hooks/useCampaign';
import { CampaignListResponse } from '@/types/campaign.types';
import { Actions } from './Actions';
import useToggle from '@/hooks/useToggle';
import CampaignSettings from './CampaignSettings';
import { useCallback } from 'react';

type CampaignTableActionsProps = {
  campaign: CampaignListResponse;
};

const CampaignTableActions = ({ campaign }: CampaignTableActionsProps) => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();

  const workspaceId = authenticatedUserDetails?.workspaceId || '';
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const { value: isSettingsOpen, toggle: handleToggleSettings } = useToggle();

  const { mutate: pauseCampaignMutate } = usePauseCampaign();
  const { mutate: resumeCampaignMutate } = useResumeCampaign();

  const handleEditCampaign = useCallback(() => {
    router.push(appRoutes.leadgen.campaigns.summary(campaign.id));
  }, [router, campaign.id]);

  const handleRowClick = useCallback(() => {
    router.push(appRoutes.leadgen.campaigns.view(campaign.id));
  }, [router, campaign.id]);

  const handlePauseCampaign = useCallback(() => {
    pauseCampaignMutate(
      {
        tenantId,
        workspaceId,
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
  }, [pauseCampaignMutate, tenantId, workspaceId, campaign.id]);

  const handleResumeCampaign = useCallback(() => {
    resumeCampaignMutate(
      {
        tenantId,
        workspaceId,
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
  }, [resumeCampaignMutate, tenantId, workspaceId, campaign.id]);

  return (
    <div onClick={(e) => e.stopPropagation()}>
      <ActionMenu
        actions={
          <Actions
            campaignStatus={campaign.status}
            handleEditCampaign={handleEditCampaign}
            handleViewDetails={handleRowClick}
            handlePauseCampaign={handlePauseCampaign}
            handleResumeCampaign={handleResumeCampaign}
            handleEmailRecipients={handleToggleSettings}
          />
        }
      />
      <CampaignSettings
        campaign={campaign}
        isOpen={isSettingsOpen}
        onClose={handleToggleSettings}
      />
    </div>
  );
};

export { CampaignTableActions };
