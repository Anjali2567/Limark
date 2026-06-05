'use client';

import { Briefcase, ChevronLeft, MapPin } from 'lucide-react';

import { AddToListPopover } from '@/app/(modules)/leadgen/(dashboard-protected)/lists/_components/AddToListPopover';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { LeadContactData } from '@/types/leadSearch.types';
import { getInitials } from '@/lib/utils/helpers';

type Props = {
  contactName: string;
  contact: LeadContactData;
  contactLocation: string;
  onBack: () => void;
};

export const ContactInfoHeader = ({ contactName, contact, contactLocation, onBack }: Props) => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';
  const workspaceId = authenticatedUserDetails?.workspaceId ?? '';

  return (
    <div className="sticky top-0 z-30">
      <div className="border-border bg-card flex items-center border-b px-6 py-3">
        <Button variant="compressed" onClick={onBack}>
          <ChevronLeft className="h-4 w-4" />
          <span>Back to Search</span>
        </Button>
      </div>
      <div className="border-border bg-card border-b px-6 py-6">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-4">
            <Avatar className="h-12 w-12">
              <AvatarFallback className="bg-primary/10 text-primary text-sm font-semibold">
                {getInitials(contactName)}
              </AvatarFallback>
            </Avatar>
            <div>
              <h1 className="text-foreground mb-2 text-2xl font-bold">{contactName}</h1>
              <div className="text-muted-foreground flex items-center gap-3 text-sm">
                {contact.title && (
                  <div className="flex items-center gap-1.5">
                    <Briefcase className="h-4 w-4" />
                    <span>{contact.title}</span>
                  </div>
                )}
                {contact.title && contactLocation && <span>•</span>}
                {contactLocation && (
                  <div className="flex items-center gap-1.5">
                    <MapPin className="h-4 w-4" />
                    <span>{contactLocation}</span>
                  </div>
                )}
              </div>
            </div>
          </div>
          <AddToListPopover
            tenantId={tenantId}
            workspaceId={workspaceId}
            type={LeadType.LEAD_CONTACT}
            sourceId={contact.id}
            trigger={<Button>Add to list</Button>}
          />
        </div>
      </div>
    </div>
  );
};
