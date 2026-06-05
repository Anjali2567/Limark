'use client';

import { ChevronLeft, Link2 } from 'lucide-react';

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';

import { AddToListPopover } from '@/app/(modules)/leadgen/(dashboard-protected)/lists/_components/AddToListPopover';
import { Button } from '@/components/ui/button';
import { LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { LeadCompanyData } from '@/types/leadSearch.types';
import { getInitials } from '@/lib/utils/helpers';

type Props = {
  company: LeadCompanyData;
  websiteUrl: string;
  industryLabel: string;
  location: string;
  onBack: () => void;
};

export const CompanyInfoHeader = ({
  company,
  websiteUrl,
  industryLabel,
  location,
  onBack,
}: Props) => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';
  const workspaceId = authenticatedUserDetails?.workspaceId ?? '';

  return (
    <div className="sticky top-0 z-20">
      <div className="border-border bg-card border-b px-6 py-3">
        <Button variant="compressed" onClick={onBack}>
          <ChevronLeft className="h-4 w-4" />
          <span>Back to Search</span>
        </Button>
      </div>
      <div className="border-border bg-card border-b">
        <div className="px-6 py-5">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-4">
              <Avatar className="h-12 w-12 rounded-lg">
                <AvatarImage src={company.logoUrl} alt={company.name} className="object-contain" />
                <AvatarFallback className="bg-primary/10 text-primary rounded-lg text-sm font-semibold">
                  {getInitials(company.name)}
                </AvatarFallback>
              </Avatar>
              <div>
                <div className="flex items-center gap-3">
                  <h1 className="text-foreground text-2xl font-bold">{company.name}</h1>
                  {websiteUrl && (
                    <a
                      href={websiteUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-muted-foreground hover:text-primary"
                      aria-label={`Visit ${company.name} website (opens in new tab)`}
                    >
                      <Link2 className="h-4 w-4" />
                    </a>
                  )}
                </div>
                {(industryLabel || location) && (
                  <div className="mt-1 flex items-center gap-2">
                    {industryLabel && (
                      <span className="text-muted-foreground text-sm">{industryLabel}</span>
                    )}
                    {industryLabel && location && <span className="text-muted-foreground">·</span>}
                    {location && <span className="text-muted-foreground text-sm">{location}</span>}
                  </div>
                )}
              </div>
            </div>
            <AddToListPopover
              tenantId={tenantId}
              workspaceId={workspaceId}
              type={LeadType.LEAD_COMPANY}
              sourceId={company.id}
              trigger={<Button>Add to list</Button>}
            />
          </div>
        </div>
      </div>
    </div>
  );
};
