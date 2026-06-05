'use client';

import { useRouter } from 'next/navigation';

import { AddToListPopover } from '@/app/(modules)/leadgen/(dashboard-protected)/lists/_components/AddToListPopover';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { appRoutes } from '@/config/routes';
import { LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { LeadCompanyData } from '@/types/leadSearch.types';
import ContactNotesDrawer from '../../_components/ContactNotesDrawer';
import { CompanyViewTab } from '../../company-info/_components/CompanyInsightsCard';

import { BookMarked, MoreVertical } from 'lucide-react';

type CompanyRowActionsProps = {
  data: LeadCompanyData;
};

const CompanyRowActions = ({ data }: CompanyRowActionsProps) => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';
  const workspaceId = authenticatedUserDetails?.workspaceId ?? '';

  const baseUrl = appRoutes.leadgen.search.companyInfo(data.id);

  return (
    <div className="flex items-center justify-center gap-0.5">
      <div onClick={(e) => e.stopPropagation()}>
        <ContactNotesDrawer
          entityId={data.id}
          entityName={data.name}
          tenantId={tenantId}
          workspaceId={workspaceId}
          leadType={LeadType.LEAD_COMPANY}
        />
      </div>
      <Tooltip>
        <AddToListPopover
          tenantId={tenantId}
          workspaceId={workspaceId}
          type={LeadType.LEAD_COMPANY}
          sourceId={data.id}
          trigger={
            <TooltipTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="text-muted-foreground hover:text-foreground h-7 w-7"
                onClick={(e) => e.stopPropagation()}
              >
                <BookMarked className="h-3.5 w-3.5" />
              </Button>
            </TooltipTrigger>
          }
        />
        <TooltipContent side="top">Lists</TooltipContent>
      </Tooltip>
      <div onClick={(e) => e.stopPropagation()}>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              size="icon"
              className="text-muted-foreground hover:text-foreground h-7 w-7"
              aria-label="Open menu"
            >
              <MoreVertical className="h-3.5 w-3.5" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onClick={() => router.push(baseUrl)}>View Details</DropdownMenuItem>
            <DropdownMenuItem
              onClick={() => router.push(`${baseUrl}&tab=${CompanyViewTab.JobPostings}`)}
            >
              Job Postings
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  );
};

export { CompanyRowActions };
