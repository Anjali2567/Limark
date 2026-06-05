import { Badge } from '@/components/ui/badge';
import { LeadCompanyData } from '@/types/leadSearch.types';
import { formatEmployeeRange, formatRevenueRange } from '@/lib/utils/formatter';

import { CircleDollarSign, Facebook, Link2, Linkedin, Phone, Twitter, Users } from 'lucide-react';

type Props = {
  company: LeadCompanyData;
  location: string;
  websiteUrl: string;
};

export const CompanyDetailsCard = ({ company, location, websiteUrl }: Props) => {
  return (
    <div className="border-border bg-card rounded-lg border p-5">
      <h2 className="text-foreground mb-4 text-sm font-medium">Company details</h2>
      <div className="space-y-4">
        {(company.segment || company.industry) && (
          <div>
            <p className="text-muted-foreground mb-2 text-[12px] font-medium tracking-wider">
              Industry
            </p>
            <Badge variant="secondary" className="rounded-full text-xs whitespace-normal">
              {company.segment || company.industry}
            </Badge>
          </div>
        )}
        {company.segment && company.industry && (
          <div>
            <p className="text-muted-foreground mb-2 text-[12px] font-medium tracking-wider">
              Sub-Industry
            </p>
            <Badge variant="secondary" className="rounded-full text-xs whitespace-normal">
              {company.industry}
            </Badge>
          </div>
        )}
        {company.phoneNumber && (
          <div>
            <p className="text-muted-foreground mb-2 text-[12px] font-medium tracking-wider">
              Phone Number
            </p>
            <div className="flex items-center gap-2">
              <Phone className="text-muted-foreground h-4 w-4" />
              <span className="text-foreground text-sm">{company.phoneNumber}</span>
            </div>
          </div>
        )}
        {company.employeeCount && (
          <div>
            <p className="text-muted-foreground mb-1 text-[12px] font-medium tracking-wider">
              Number of Employees
            </p>
            <div className="flex items-center gap-2">
              <Users className="text-muted-foreground h-4 w-4" />
              <span className="text-foreground text-sm">
                {formatEmployeeRange(company.employeeCount)}
              </span>
            </div>
          </div>
        )}
        {company.revenueUsd && (
          <div>
            <p className="text-muted-foreground mb-1 text-[12px] font-medium tracking-wider">
              Annual Revenue
            </p>
            <div className="flex items-center gap-2">
              <CircleDollarSign className="text-muted-foreground h-4 w-4" />
              <span className="text-foreground text-sm">
                {formatRevenueRange(company.revenueUsd)}
              </span>
            </div>
          </div>
        )}
        {company.naicsCodes && company.naicsCodes.length > 0 && (
          <div>
            <p className="text-muted-foreground mb-2 text-[12px] font-medium tracking-wider">
              NAICS Codes
            </p>
            <div className="flex flex-wrap gap-1.5">
              {company.naicsCodes.map((code, i) => (
                <Badge key={i} variant="outline" className="text-xs">
                  {code}
                </Badge>
              ))}
            </div>
          </div>
        )}
        {company.sicCodes && company.sicCodes.length > 0 && (
          <div>
            <p className="text-muted-foreground mb-2 text-[12px] font-medium tracking-wider">
              SIC Codes
            </p>
            <div className="flex flex-wrap gap-1.5">
              {company.sicCodes.map((code, i) => (
                <Badge key={i} variant="outline" className="text-xs">
                  {code}
                </Badge>
              ))}
            </div>
          </div>
        )}
        {location && (
          <div>
            <p className="text-muted-foreground mb-1 text-[12px] font-medium tracking-wider">
              HQ Location
            </p>
            <span className="text-foreground text-sm">{location}</span>
          </div>
        )}
        <div>
          <p className="text-muted-foreground mb-2 text-[12px] font-medium tracking-wider">Links</p>
          <div className="flex items-center gap-3">
            {websiteUrl && (
              <a
                href={websiteUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-muted-foreground hover:text-primary transition-colors"
              >
                <Link2 className="h-4 w-4" />
              </a>
            )}
            {company.linkedinUrl && (
              <a
                href={company.linkedinUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-muted-foreground hover:text-primary transition-colors"
              >
                <Linkedin className="h-4 w-4" />
              </a>
            )}
            {company.twitterUrl && (
              <a
                href={company.twitterUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-muted-foreground hover:text-primary transition-colors"
              >
                <Twitter className="h-4 w-4" />
              </a>
            )}
            {company.facebookUrl && (
              <a
                href={company.facebookUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-muted-foreground hover:text-primary transition-colors"
              >
                <Facebook className="h-4 w-4" />
              </a>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
