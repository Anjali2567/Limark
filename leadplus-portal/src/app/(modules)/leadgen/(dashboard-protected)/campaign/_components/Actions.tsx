import {
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu';
import { CampaignStatus, editableCampaignStatuses } from '@/constants/Campaign';

import { Pause, Play, Settings } from 'lucide-react';

type ActionsProps = {
  campaignStatus: CampaignStatus;
  handleViewDetails?: () => void;
  handleEditCampaign?: () => void;
  handlePauseCampaign?: () => void;
  handleResumeCampaign?: () => void;
  handleEmailRecipients?: () => void;
};

const Actions = ({
  campaignStatus,
  handleViewDetails,
  handleEditCampaign,
  handlePauseCampaign,
  handleResumeCampaign,
  handleEmailRecipients,
}: ActionsProps) => {
  return (
    <DropdownMenuContent align="end">
      <DropdownMenuLabel>Actions</DropdownMenuLabel>
      <DropdownMenuItem
        onClick={(e) => {
          e.stopPropagation();
          handleViewDetails?.();
        }}
      >
        View Details
      </DropdownMenuItem>
      {editableCampaignStatuses.includes(campaignStatus) && (
        <DropdownMenuItem
          onClick={(e) => {
            e.stopPropagation();
            handleEditCampaign?.();
          }}
        >
          Edit Campaign
        </DropdownMenuItem>
      )}
      <DropdownMenuSeparator />
      <DropdownMenuItem
        onClick={(e) => {
          e.stopPropagation();
          handleEmailRecipients?.();
        }}
      >
        <Settings className="mr-2 h-4 w-4" />
        Settings
      </DropdownMenuItem>
      {campaignStatus === CampaignStatus.RUNNING ? (
        <>
          <DropdownMenuSeparator />
          <DropdownMenuItem
            className="text-amber-600"
            onClick={(e) => {
              e.stopPropagation();
              handlePauseCampaign?.();
            }}
          >
            <Pause className="mr-2 h-4 w-4" />
            Pause Campaign
          </DropdownMenuItem>
        </>
      ) : campaignStatus === CampaignStatus.PAUSED ? (
        <>
          <DropdownMenuSeparator />
          <DropdownMenuItem
            className="text-green-600"
            onClick={(e) => {
              e.stopPropagation();
              handleResumeCampaign?.();
            }}
          >
            <Play className="mr-2 h-4 w-4" />
            Resume Campaign
          </DropdownMenuItem>
        </>
      ) : null}
    </DropdownMenuContent>
  );
};

export { Actions };
