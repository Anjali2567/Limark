import { LeadCompanyData } from '@/types/leadSearch.types';

type Props = {
  company: LeadCompanyData;
  location: string;
};

export const CompanyOverviewTab = ({ company }: Props) => {
  return (
    <div className="space-y-4">
      {company.accountSummary ? (
        <p className="text-foreground text-sm font-medium">{company.accountSummary}</p>
      ) : (
        <p className="text-muted-foreground text-xs">No Company Description Available</p>
      )}
    </div>
  );
};
