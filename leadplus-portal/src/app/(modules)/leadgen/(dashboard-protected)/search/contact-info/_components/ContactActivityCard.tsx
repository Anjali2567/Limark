import { Badge } from '@/components/ui/badge';
import { CurrentCampaignData } from '@/types/leadSearch.types';

type Props = {
  campaigns: CurrentCampaignData[];
};

export const ContactActivityCard = ({ campaigns }: Props) => {
  if (!campaigns || campaigns.length === 0) return null;

  return (
    <div className="border-border bg-card rounded-lg border p-4">
      <h2 className="text-foreground mb-4 text-sm font-medium">Activity</h2>
      <div>
        <p className="text-muted-foreground mb-2 text-xs">Active Campaigns</p>
        <div className="flex flex-wrap gap-2">
          {campaigns.map((campaign) => (
            <Badge key={campaign.id} variant="secondary" className="text-xs">
              {campaign.name}
            </Badge>
          ))}
        </div>
      </div>
    </div>
  );
};
