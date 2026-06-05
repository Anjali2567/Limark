import { Linkedin, Mail, MapPin, Phone } from 'lucide-react';

import { LeadContactData } from '@/types/leadSearch.types';

type Props = {
  contact: LeadContactData;
  contactLocation: string;
};

export const ContactInfoCard = ({ contact, contactLocation }: Props) => {
  const isInformationAvailable =
    contact.email || contact.phoneE164 || contact.linkedinUrl || contactLocation;

  return (
    <div className="border-border bg-card rounded-lg border p-4">
      <h2 className="text-foreground mb-4 text-sm font-medium">Contact Information</h2>
      {isInformationAvailable ? (
        <div className="space-y-4">
          {contact.email && (
            <div>
              <div className="text-muted-foreground mb-1 flex items-center gap-2 text-xs">
                <Mail className="h-3.5 w-3.5" />
                <span>Email</span>
              </div>
              <a
                href={`mailto:${contact.email}`}
                className="text-primary block pl-5 text-sm hover:underline"
              >
                {contact.email}
              </a>
            </div>
          )}
          {contact.phoneE164 && (
            <div>
              <div className="text-muted-foreground mb-1 flex items-center gap-2 text-xs">
                <Phone className="h-3.5 w-3.5" />
                <span>Phone</span>
              </div>
              <p className="text-foreground pl-5 text-sm">{contact.phoneE164}</p>
            </div>
          )}
          {contact.linkedinUrl && (
            <div>
              <div className="text-muted-foreground mb-1 flex items-center gap-2 text-xs">
                <Linkedin className="h-3.5 w-3.5" />
                <span>LinkedIn</span>
              </div>
              <a
                href={contact.linkedinUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary block pl-5 text-sm hover:underline"
              >
                View Profile
              </a>
            </div>
          )}
          {contactLocation && (
            <div>
              <div className="text-muted-foreground mb-1 flex items-center gap-2 text-xs">
                <MapPin className="h-3.5 w-3.5" />
                <span>Location</span>
              </div>
              <p className="text-foreground pl-5 text-sm">{contactLocation}</p>
            </div>
          )}
        </div>
      ) : (
        <div className="text-muted-foreground text-sm">No information available</div>
      )}
    </div>
  );
};
