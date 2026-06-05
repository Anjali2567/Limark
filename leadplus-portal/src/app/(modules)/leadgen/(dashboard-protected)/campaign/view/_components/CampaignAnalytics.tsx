import { GridSkeleton } from "@/components/Skeleton";
import { useGetCampaignAnalytics } from "@/hooks/useCampaign";
import { formatDecimal } from "@/lib/utils/formatter";

import { Calendar, Eye, Mail, MessageSquare } from "lucide-react";

type CampaignAnalyticsProps = {
  tenantId: string;
  campaignId: string;
  workspaceId: string;
};

const CampaignAnalytics = ({
  tenantId,
  campaignId,
  workspaceId,
}: CampaignAnalyticsProps) => {
  const { data: campaign, isLoading } = useGetCampaignAnalytics({
    tenantId,
    campaignId,
    workspaceId,
  });

  const stats = [
    {
      label: "Sent",
      value: campaign?.sentEmails || 0,
      icon: Mail,
      iconClass: "text-yellow-500",
    },
    {
      label: "Opened",
      value: campaign?.openedEmails || 0,
      rate: campaign?.emailsOpenRate,
      icon: Eye,
      iconClass: "text-teal-500",
    },
    {
      label: "Replied",
      value: campaign?.repliedEmails || 0,
      rate: campaign?.emailsReplyRate,
      icon: MessageSquare,
      iconClass: "text-orange-500",
    },
    {
      label: "Meetings",
      value: 0,
      icon: Calendar,
      iconClass: "text-green-500",
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4">
      {isLoading ? (
        <>
          {Array.from({ length: 4 }).map((_, i) => (
            <GridSkeleton key={i} />
          ))}
        </>
      ) : (
        stats.map(({ label, value, rate, icon: Icon, iconClass }) => (
          <div
            key={label}
            className="bg-card border border-border rounded-lg p-4"
          >
            <div className="flex items-center justify-between mb-2">
              <p className="text-sm text-muted-foreground">{label}</p>
              <Icon className={`h-5 w-5 ${iconClass}`} />
            </div>

            {rate !== undefined ? (
              <div className="flex items-baseline gap-2">
                <p className="text-2xl font-semibold text-foreground">
                  {value}
                </p>
                <p className="text-sm text-muted-foreground">
                  {formatDecimal(rate)}%
                </p>
              </div>
            ) : (
              <p className="text-2xl font-semibold text-foreground">{value}</p>
            )}
          </div>
        ))
      )}
    </div>
  );
};

export { CampaignAnalytics };
