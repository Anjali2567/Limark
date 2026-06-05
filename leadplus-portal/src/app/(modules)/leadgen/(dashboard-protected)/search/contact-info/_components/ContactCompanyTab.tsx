import { Building2 } from 'lucide-react';
import Image from 'next/image';

import { LeadContactData } from '@/types/leadSearch.types';

type Props = {
  contact: LeadContactData;
};

export const ContactCompanyTab = ({ contact }: Props) => {
  return (
    <div className="space-y-4">
      <div>
        <div className="mb-3 flex items-center gap-2">
          {contact.companyLogoUrl ? (
            <Image
              src={contact.companyLogoUrl}
              alt={contact.companyName}
              width={20}
              height={20}
              className="rounded object-contain"
            />
          ) : (
            <Building2 className="text-muted-foreground h-4 w-4" />
          )}
          <h3 className="text-foreground text-md font-semibold">{contact.companyName}</h3>
        </div>
        {contact.companyDescription && (
          <p className="mb-4 text-sm leading-relaxed whitespace-pre-wrap">
            {contact.companyDescription}
          </p>
        )}
      </div>
    </div>
  );
};
