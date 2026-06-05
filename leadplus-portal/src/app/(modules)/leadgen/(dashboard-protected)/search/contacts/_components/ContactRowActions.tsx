'use client';

import { showComingSoonToast } from '@/components/CustomToast';
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
import { formatName } from '@/lib/utils/formatter';
import { LeadContactData } from '@/types/leadSearch.types';
import ContactNotesDrawer from '../../_components/ContactNotesDrawer';
import { ContactViewTab } from '../../contact-info/_components/ContactInsightsTabs';

import { BookMarked, Calendar, Mail, MoreVertical, Phone } from 'lucide-react';

type ContactRowActionsProps = {
  data: LeadContactData;
  onSendEmail?: (data: LeadContactData) => void;
};

const ContactRowActions = ({ data, onSendEmail }: ContactRowActionsProps) => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';
  const workspaceId = authenticatedUserDetails?.workspaceId ?? '';

  const entityName = formatName({ firstName: data.firstName, lastName: data.lastName });
  const baseUrl = appRoutes.leadgen.search.contactInfo(data.id);

  const handlePhoneCall = () => {
    showComingSoonToast(
      'The Phone Call feature is currently in development and will be available in an upcoming release.'
    );
  };

  const handleSchedule = () => {
    showComingSoonToast(
      'The Schedule feature is currently in development and will be available in an upcoming release.'
    );
  };

  return (
    <div className="flex items-center justify-center gap-0.5">
      <Tooltip>
        <TooltipTrigger asChild>
          <Button
            variant="ghost"
            size="icon"
            aria-label={`Send email to ${entityName}`}
            className="text-muted-foreground hover:text-foreground h-7 w-7"
            onClick={(e) => {
              e.stopPropagation();
              onSendEmail?.(data);
            }}
          >
            <Mail className="h-3.5 w-3.5" aria-hidden="true" />
          </Button>
        </TooltipTrigger>
        <TooltipContent side="top">Send Email</TooltipContent>
      </Tooltip>
      <Tooltip>
        <TooltipTrigger asChild>
          <Button
            variant="ghost"
            size="icon"
            aria-label={`Phone call with ${entityName}`}
            className="text-muted-foreground hover:text-foreground h-7 w-7"
            onClick={(e) => {
              e.stopPropagation();
              handlePhoneCall();
            }}
          >
            <Phone className="h-3.5 w-3.5" aria-hidden="true" />
          </Button>
        </TooltipTrigger>
        <TooltipContent side="top">Phone Call</TooltipContent>
      </Tooltip>
      <Tooltip>
        <TooltipTrigger asChild>
          <Button
            variant="ghost"
            size="icon"
            aria-label={`Schedule meeting with ${entityName}`}
            className="text-muted-foreground hover:text-foreground h-7 w-7"
            onClick={(e) => {
              e.stopPropagation();
              handleSchedule();
            }}
          >
            <Calendar className="h-3.5 w-3.5" aria-hidden="true" />
          </Button>
        </TooltipTrigger>
        <TooltipContent side="top">Schedule</TooltipContent>
      </Tooltip>
      <div onClick={(e) => e.stopPropagation()}>
        <ContactNotesDrawer
          entityId={data.id}
          entityName={entityName}
          tenantId={tenantId}
          workspaceId={workspaceId}
          leadType={LeadType.LEAD_CONTACT}
        />
      </div>
      <Tooltip>
        <AddToListPopover
          tenantId={tenantId}
          workspaceId={workspaceId}
          type={LeadType.LEAD_CONTACT}
          sourceId={data.id}
          trigger={
            <TooltipTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                aria-label={`Add ${entityName} to list`}
                className="text-muted-foreground hover:text-foreground h-7 w-7"
                onClick={(e) => e.stopPropagation()}
              >
                <BookMarked className="h-3.5 w-3.5" aria-hidden="true" />
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
              onClick={() => router.push(`${baseUrl}&tab=${ContactViewTab.History}`)}
            >
              History
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>
  );
};

export { ContactRowActions };
