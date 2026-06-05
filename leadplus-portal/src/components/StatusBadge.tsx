import { JSX } from 'react';

import { Badge } from '@/components/ui/badge';
import { CampaignStatus } from '@/constants/Campaign';
import { UserStatus } from '@/constants/user.constants';
import { cn } from '@/lib/utils/helpers';
import { formatString } from '@/lib/utils/formatter';

const DEFAULT_COLOR = 'bg-slate-100 text-slate-700 hover:bg-slate-200 border-slate-200';

const MAX_STATUS_LEVEL = 3;

const getColorByStatus = (status?: CampaignStatus | UserStatus | string, number?: number) => {
  if (status !== undefined) {
    switch (status) {
      case CampaignStatus.RUNNING:
      case CampaignStatus.COMPLETED:
        return 'bg-green-100 text-green-700 hover:bg-green-200 border-green-200';
      case CampaignStatus.APPROVED:
      case CampaignStatus.DRAFT:
      case 'INFO':
        return 'bg-blue-100 text-blue-700 hover:bg-blue-200 border-blue-200';
      case 'PENDING':
      case CampaignStatus.PAUSED:
        return 'bg-amber-100 text-amber-700 hover:bg-amber-200 border-amber-200';
      case 'ACTIVE':
        return 'bg-green-500/10 text-green-700 border-green-200';
      case 'UNSUBSCRIBED':
        return 'bg-red-100 text-red-700 hover:bg-red-200 border-red-200';
      case 'BOUNCED':
        return 'bg-orange-100 text-orange-700 hover:bg-orange-200 border-orange-200';
      case UserStatus.PENDING:
        return 'bg-warning/10 text-warning border-warning/20';
      default:
        return DEFAULT_COLOR;
    }
  }

  if (number !== undefined) {
    const clamped = Math.min(number, MAX_STATUS_LEVEL);

    switch (clamped) {
      case 1:
        return 'bg-blue-100 text-blue-700 hover:bg-blue-200 border-blue-200';
      case 2:
        return 'bg-amber-100 text-amber-700 hover:bg-amber-200 border-amber-200';
      case 3:
        return 'bg-purple-100 text-purple-700 hover:bg-purple-200 border-purple-200';
      default:
        return DEFAULT_COLOR;
    }
  }

  return DEFAULT_COLOR;
};

const StatusBadge = ({
  value,
  status,
  number,
  icon,
  className,
}: {
  value?: string;
  status?: CampaignStatus | UserStatus | string;
  number?: number;
  icon?: JSX.Element;
  className?: string;
}) => {
  if (!value && !status) return <span className="text-muted-foreground text-sm">N/A</span>;

  return (
    <Badge
      className={cn(
        getColorByStatus(status, number),
        className,
        'rounded-full border px-3 py-1.5 font-medium'
      )}
    >
      {icon && <span className="mr-1 inline h-3 w-3">{icon}</span>}
      {formatString(value || status)}
    </Badge>
  );
};

export { StatusBadge };
