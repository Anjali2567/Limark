import { cn } from '@/lib/utils/helpers';

import { Building, MapPin, Users } from 'lucide-react';

type CampaignStatsCardsProps = {
  companies?: number | null;
  contacts?: number | null;
  locations?: number | null;
  isLoading?: boolean;
  onCardClick?: () => void;
};

const STATS = [
  { key: 'companies', label: 'Companies', icon: Building, color: 'blue' },
  { key: 'contacts', label: 'No. of Contacts', icon: Users, color: 'purple' },
  { key: 'locations', label: 'Locations', icon: MapPin, color: 'orange' },
] as const;

const CampaignStatsCards = ({
  companies,
  contacts,
  locations,
  isLoading,
  onCardClick,
}: CampaignStatsCardsProps) => {
  const values = { companies, contacts, locations };

  return (
    <div className="grid grid-cols-3 gap-3">
      {STATS.map(({ key, label, icon: Icon, color }) => (
        <div
          key={key}
          className={cn(
            'border-border rounded-lg border p-3 text-center text-nowrap',
            `bg-${color}-50/70`,
            onCardClick && 'cursor-pointer transition hover:shadow-md'
          )}
          onClick={onCardClick}
        >
          {isLoading ? (
            <div className="space-y-2">
              <div className="bg-muted mx-auto h-4 w-16 animate-pulse rounded" />
              <div className="bg-muted mx-auto h-6 w-12 animate-pulse rounded" />
            </div>
          ) : (
            <>
              <div className="mb-1 flex items-center justify-center gap-1">
                <Icon className={`h-4 w-4 text-${color}-600`} />
                <span className="text-muted-foreground text-xs">{label}</span>
              </div>
              <p className={`text-2xl font-semibold text-${color}-600`}>{values[key]}</p>
            </>
          )}
        </div>
      ))}
    </div>
  );
};

export { CampaignStatsCards };
