import { useMemo } from 'react';
import { Building2, Mail, Phone, Users } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import { useGetLeadStatistics } from '@/hooks/useLeads';
import { formatCompactNumber } from '@/lib/utils/formatter';

const LeadStats = () => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const { data: statistics } = useGetLeadStatistics(tenantId);

  const stats = useMemo(
    () => [
      {
        label: 'Contacts',
        value: formatCompactNumber(statistics?.totalContacts),
        icon: Users,
      },
      {
        label: 'Companies',
        value: formatCompactNumber(statistics?.totalCompanies),
        icon: Building2,
      },
      {
        label: 'Emails',
        value: formatCompactNumber(statistics?.totalEmails),
        icon: Mail,
      },
      {
        label: 'Phones',
        value: formatCompactNumber(statistics?.totalPhoneNumbers),
        icon: Phone,
      },
    ],
    [statistics]
  );

  return (
    <div className="w-full overflow-auto px-6">
      <div className="mx-auto max-w-4xl space-y-12">
        <div className="space-y-1 text-center">
          <h1 className="text-foreground text-xl font-bold">Welcome to LeadPlus</h1>
          <p className="text-muted-foreground text-sm">
            Over{' '}
            <span className="text-foreground font-semibold">
              {formatCompactNumber(statistics?.totalContacts)}
            </span>{' '}
            B2B Contacts and{' '}
            <span className="text-foreground font-semibold">
              {formatCompactNumber(statistics?.totalCompanies)}
            </span>{' '}
            Companies researched in real-time with the power of AI.
          </p>
        </div>

        <div className="grid grid-cols-1 gap-3 md:grid-cols-4">
          {stats.map(({ label, value, icon: Icon }) => (
            <div
              key={label}
              className="bg-card border-border flex flex-col items-center space-y-1.5 rounded-lg border p-3"
            >
              <div className="bg-primary/10 flex h-8 w-8 items-center justify-center rounded-full">
                <Icon className="text-primary h-4 w-4" />
              </div>

              <div className="text-center">
                <div className="text-foreground text-lg font-bold">{value}</div>
                <div className="text-muted-foreground text-xs">{label}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export { LeadStats };
