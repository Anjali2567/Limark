'use client';

import { ElementType, ReactNode } from 'react';

import { Breadcrumbs } from '@/components/Breadcrumbs';
import { GridSkeleton, Skeleton } from '@/components/Skeleton';
import { appRoutes } from '@/config/routes';
import { useGetAdminTenantAnalytics, useGetAdminTenantActivityByTenantId } from '@/hooks/useAdmin';
import { formatDate } from '@/lib/utils/formatter';

import { Activity, BarChart2, Clock, Info, Mail, Send, TrendingUp, Users } from 'lucide-react';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { TenantAnalytics } from '@/types/tenant.types';
import { TenantUsersTable } from './TenantUsersTable';

const StatCard = ({
  title,
  value,
  icon: Icon,
  iconClass,
  toolTip,
}: {
  title: ReactNode;
  value: string | number;
  icon: ElementType;
  iconClass: string;
  toolTip?: string;
}) => (
  <div className="bg-card border-border rounded-lg border p-4">
    <div className="mb-2 flex items-center justify-between">
      <span className="flex items-center gap-1 text-sm">
        {title}
        {toolTip && (
          <Tooltip>
            <TooltipTrigger asChild>
              <Info className="text-muted-foreground h-3 w-3 cursor-pointer" />
            </TooltipTrigger>
            <TooltipContent>
              <p>{toolTip}</p>
            </TooltipContent>
          </Tooltip>
        )}
      </span>
      <Icon className={`h-5 w-5 ${iconClass}`} />
    </div>
    <p className="text-foreground text-2xl font-semibold">{value}</p>
  </div>
);

const stats = (data: TenantAnalytics, lastActivity: string) => [
  {
    title: 'Total Campaigns',
    value: data?.totalCampaigns ?? 0,
    icon: BarChart2,
    iconClass: 'text-blue-500',
  },
  {
    title: 'Active Campaigns',
    value: data?.activeCampaigns ?? 0,
    icon: TrendingUp,
    iconClass: 'text-green-500',
  },
  {
    title: 'Campaign Emails Sent',
    value: data?.emailsSent ?? 0,
    icon: Send,
    iconClass: 'text-yellow-500',
  },
  {
    title: 'Individual Emails',
    value: data?.individualEmails ?? 0,
    icon: Mail,
    iconClass: 'text-orange-500',
  },
  {
    title: 'Users',
    value: data?.workspaceUsers ?? 0,
    icon: Users,
    iconClass: 'text-teal-500',
  },
  { title: 'Last Activity', value: lastActivity, icon: Activity, iconClass: 'text-purple-500' },
];

const TenantDetailView = ({ tenantId }: { tenantId: string }) => {
  const { data, isLoading } = useGetAdminTenantAnalytics(tenantId);
  const { data: tenantActivity, isLoading: isActivityLoading } =
    useGetAdminTenantActivityByTenantId(tenantId);

  const lastActivityValue = formatDate(data?.lastActivity) ?? '-';

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 w-full space-y-6 p-8 duration-500">
      <Breadcrumbs
        items={[
          { label: 'Tenants', href: appRoutes.admin.tenants.root },
          { label: data?.tenantName || '' },
        ]}
      />

      <div className="flex flex-col">
        {isLoading ? (
          <Skeleton className="h-9 w-48" />
        ) : (
          <>
            <h1 className="text-foreground text-3xl font-bold">{data?.tenantName}</h1>
            <p className="text-muted-foreground">Tenant analytics overview</p>
          </>
        )}
      </div>

      <div className="grid w-full grid-cols-1 gap-4 md:grid-cols-3 lg:grid-cols-7">
        {isLoading || isActivityLoading
          ? Array.from({ length: 7 }).map((_, i) => <GridSkeleton key={i} />)
          : data && (
              <>
                {stats(data, lastActivityValue).map(({ title, value, icon, iconClass }) => (
                  <StatCard
                    key={title}
                    title={title}
                    value={value}
                    icon={icon}
                    iconClass={iconClass}
                  />
                ))}
                <StatCard
                  title="Active Hours"
                  toolTip="Based on the last 30 days"
                  value={tenantActivity?.activeHours ?? 0}
                  icon={Clock}
                  iconClass="text-indigo-500"
                />
              </>
            )}
      </div>
      <TenantUsersTable tenantId={tenantId} />
    </div>
  );
};

export { TenantDetailView };
