import { Badge } from '@/components/ui/badge';
import { formatEmployeeRange, formatRevenueRange } from '@/lib/utils/formatter';
import { LeadContactData } from '@/types/leadSearch.types';

import { Globe, Users } from 'lucide-react';

type Props = {
  contact: LeadContactData;
  companyLocation: string;
  websiteUrl: string;
};

export const ContactCompanyDetails = ({ contact, companyLocation, websiteUrl }: Props) => {
  return (
    <div className="border-border bg-card rounded-lg border">
      <div className="border-border border-b px-4 py-3">
        <h2 className="text-foreground text-sm font-medium">Company Details</h2>
      </div>
      <div className="grid grid-cols-2 gap-6 p-4">
        <div className="space-y-4">
          <div>
            <p className="text-muted-foreground mb-1 text-xs">Revenue</p>
            <p className="text-foreground text-sm">
              {formatRevenueRange(contact.companyRevenueUsd)}
            </p>
          </div>
          {(contact.companySegment || contact.companyIndustry) && (
            <div>
              <p className="text-muted-foreground mb-1 text-xs">Industry</p>
              <Badge variant="secondary" className="text-xs">
                {contact.companySegment || contact.companyIndustry}
              </Badge>
            </div>
          )}
          {contact.companySegment && contact.companyIndustry && (
            <div>
              <p className="text-muted-foreground mb-1 text-xs">Sub-Industry</p>
              <Badge variant="secondary" className="text-xs">
                {contact.companyIndustry}
              </Badge>
            </div>
          )}
        </div>
        <div className="space-y-4">
          {companyLocation && (
            <div>
              <p className="text-muted-foreground mb-1 text-xs">HQ Location</p>
              <p className="text-foreground text-sm">{companyLocation}</p>
            </div>
          )}
          {contact.companyEmployeeCount && (
            <div>
              <p className="text-muted-foreground mb-1 text-xs">Number of Employees</p>
              <div className="flex items-center gap-2">
                <Users className="text-muted-foreground h-4 w-4" />
                <p className="text-foreground text-sm">
                  {formatEmployeeRange(contact.companyEmployeeCount)}
                </p>
              </div>
            </div>
          )}
          {contact.companyLinkedinUrl && (
            <div>
              <p className="text-muted-foreground mb-1 text-xs">LinkedIn</p>
              <a
                href={contact.companyLinkedinUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary text-sm hover:underline"
              >
                View Profile
              </a>
            </div>
          )}
          {contact.companyTwitterUrl && (
            <div>
              <p className="text-muted-foreground mb-1 text-xs">Twitter</p>
              <a
                href={contact.companyTwitterUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary text-sm hover:underline"
              >
                View Profile
              </a>
            </div>
          )}
        </div>
      </div>
      {websiteUrl && (
        <div className="border-border mt-6 border-t p-4">
          <div className="flex items-center gap-2">
            <Globe className="text-muted-foreground h-4 w-4" />
            <a
              href={websiteUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-primary text-sm hover:underline"
            >
              {contact.companyDomain || websiteUrl}
            </a>
          </div>
        </div>
      )}
    </div>
  );
};
