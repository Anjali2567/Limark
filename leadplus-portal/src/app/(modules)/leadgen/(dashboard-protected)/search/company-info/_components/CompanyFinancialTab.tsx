import { Badge } from '@/components/ui/badge';
import { formatRevenueRange } from '@/lib/utils/formatter';
import { LeadCompanyData } from '@/types/leadSearch.types';

type Props = {
  company: LeadCompanyData;
};

export const CompanyFinancialTab = ({ company }: Props) => {
  return (
    <div className="space-y-3">
      <div>
        <p className="text-muted-foreground mb-1 text-xs">ANNUAL REVENUE</p>
        <p className="text-foreground text-sm font-medium">
          {formatRevenueRange(company.revenueUsd)}
        </p>
      </div>
      {company.sicCodes && company.sicCodes.length > 0 && (
        <div>
          <p className="text-muted-foreground mb-2 text-xs">SIC CODES</p>
          <div className="flex flex-wrap gap-1.5">
            {company.sicCodes.map((code, i) => (
              <Badge key={i} variant="outline" className="text-xs">
                {code}
              </Badge>
            ))}
          </div>
        </div>
      )}
      {company.naicsCodes && company.naicsCodes.length > 0 && (
        <div>
          <p className="text-muted-foreground mb-2 text-xs">NAICS CODES</p>
          <div className="flex flex-wrap gap-1.5">
            {company.naicsCodes.map((code, i) => (
              <Badge key={i} variant="outline" className="text-xs">
                {code}
              </Badge>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
