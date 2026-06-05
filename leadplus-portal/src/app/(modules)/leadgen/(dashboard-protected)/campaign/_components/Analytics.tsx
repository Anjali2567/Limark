"use client";

import { GridSkeleton } from "@/components/Skeleton";
import { useAuth } from "@/context/AuthContext";
import { useGetWorkspaceCampaignAnalytics } from "@/hooks/useCampaign";
import { formatDecimal } from "@/lib/utils/formatter";
import {
  Calendar,
  Eye,
  Mail,
  MessageSquare,
  type LucideIcon,
} from "lucide-react";

type MetricKey =
  | "sentEmails"
  | "emailsOpenRate"
  | "emailsReplyRate"
  | "bookedMeetings";

type MetricColor = "yellow" | "teal" | "orange" | "green";

const metricColorClasses: Record<
  MetricColor,
  {
    bg: string;
    icon: string;
  }
> = {
  yellow: {
    bg: "bg-yellow-100/70",
    icon: "text-yellow-500",
  },
  teal: {
    bg: "bg-teal-100/70",
    icon: "text-teal-500",
  },
  orange: {
    bg: "bg-orange-100/70",
    icon: "text-orange-500",
  },
  green: {
    bg: "bg-green-100/70",
    icon: "text-green-500",
  },
};

type MetricConfig<K extends MetricKey> = {
  key: K;
  name: string;
  icon: LucideIcon;
  color: MetricColor;
  type?: "Percentage" | "Number";
};

const metrics: MetricConfig<MetricKey>[] = [
  { key: "sentEmails", name: "Emails Sent", icon: Mail, color: "yellow" },
  {
    key: "emailsOpenRate",
    name: "Open Rate",
    icon: Eye,
    color: "teal",
    type: "Percentage",
  },
  {
    key: "emailsReplyRate",
    name: "Reply Rate",
    icon: MessageSquare,
    color: "orange",
    type: "Percentage",
  },
  {
    key: "bookedMeetings",
    name: "Meetings Booked",
    icon: Calendar,
    color: "green",
  },
];

const Analytics = () => {
  const { authenticatedUserDetails } = useAuth();

  const { data, isLoading } = useGetWorkspaceCampaignAnalytics({
    tenantId: authenticatedUserDetails?.tenantId || "",
    workspaceId: authenticatedUserDetails?.workspaceId || "",
  });

  const metricData: Partial<Record<MetricKey, number>> = data ?? {};

  const stats = metrics.map((metric) => {
    const rawValue = metricData[metric.key];

    return {
      ...metric,
      value: typeof rawValue === "number" ? formatDecimal(rawValue) : 0,
    };
  });

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      {isLoading ? (
        <>
          {Array.from({ length: 4 }).map((_, i) => (
            <GridSkeleton key={i} />
          ))}
        </>
      ) : (
        stats.map((metric) => {
          const colorClasses = metricColorClasses[metric.color];
          return (
            <div
              key={metric.key}
              className={`${colorClasses.bg} border border-border rounded-lg p-4`}
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">{metric.name}</p>
                  <p className="text-2xl font-semibold text-foreground mt-1">
                    {metric.type === "Percentage"
                      ? `${formatDecimal(metric.value)}%`
                      : metric.value}
                  </p>
                </div>
                <metric.icon className={`h-6 w-6 ${colorClasses.icon}`} />
              </div>
            </div>
          );
        })
      )}
    </div>
  );
};

export { Analytics };
