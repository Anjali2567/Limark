'use client';

import { useState } from 'react';

import { AnimatePresence, motion } from 'motion/react';

import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import useToggle from '@/hooks/useToggle';
import { cn } from '@/lib/utils/helpers';
import { CampaignCompanyResponse } from '@/types/campaign.types';
import { HoverTooltip } from '@/components/HoverTooltip';
import { formatName } from '@/lib/utils/formatter';

import {
  Briefcase,
  Building,
  ChevronDown,
  ChevronUp,
  Database,
  Globe,
  Mail,
  MapPin,
  Phone,
  User,
} from 'lucide-react';
import Image from 'next/image';

type CampaignTargetResultCompanyCardProps = {
  company: CampaignCompanyResponse;
  onToggleCompany: (companyId: string) => void;
  onToggleContact: (companyId: string, contactId: string) => void;
  isViewMode?: boolean;
};

const CampaignTargetResultCompanyCard = ({
  company,
  onToggleCompany,
  onToggleContact,
  isViewMode = false,
}: CampaignTargetResultCompanyCardProps) => {
  const CONTACT_BATCH_SIZE = 25;
  const { value: isExpanded, toggle } = useToggle(false);
  const [visibleContactCount, setVisibleContactCount] = useState(CONTACT_BATCH_SIZE);

  const contacts = company.campaignContacts ?? [];
  const allSelected =
    contacts.length > 0 &&
    contacts.every((contact) => contact.participating);
  const someSelected =
    !allSelected && contacts.some((contact) => contact.participating);

  const cardClassName = cn(
    'transition-all',
    isViewMode || allSelected || someSelected
      ? 'border-border shadow-sm'
      : 'opacity-60 border-border bg-secondary/50'
  );

  const metaItems = [
    { icon: Globe, text: company.domain, key: 'domain', link: true },
    { icon: Building, text: company.industry, key: 'industry' },
    { icon: MapPin, text: company.hqCountry, key: 'location' },
  ];

  return (
    <Card className={cardClassName}>
      <div className="p-4">
        <div className="flex items-start gap-3">
          {!isViewMode && (
            <div className="pt-1">
              <Checkbox
                checked={allSelected ? true : someSelected ? 'indeterminate' : false}
                onCheckedChange={() => onToggleCompany(company.id)}
              />
            </div>
          )}
          <div className="min-w-0 flex-1 space-y-1">
            <div className="flex items-start justify-between gap-2">
              <h3 className="text-foreground font-bold wrap-break-word">{company.name}</h3>
              <Button
                variant="ghost"
                size="sm"
                className="h-6 w-6 shrink-0 p-0"
                onClick={() => {
                  if (isExpanded) {
                    setVisibleContactCount(CONTACT_BATCH_SIZE);
                  }
                  toggle();
                }}
              >
                {isExpanded ? (
                  <ChevronUp className="h-4 w-4" />
                ) : (
                  <ChevronDown className="h-4 w-4" />
                )}
              </Button>
            </div>
            <div className="text-muted-foreground mt-2 flex flex-wrap gap-x-4 gap-y-2 text-xs">
              {metaItems.map(({ icon: Icon, text, key, link }) =>
                text && link ? (
                  <a
                    key={key}
                    href={`https://${text}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex min-w-0 items-center gap-1 text-sky-500 underline"
                  >
                    <Icon className="h-3 w-3 shrink-0" />
                    <span className="truncate">{text}</span>
                  </a>
                ) : (
                  <div key={key} className="flex min-w-0 items-center gap-1">
                    <Icon className="h-3 w-3 shrink-0" />
                    <span className="truncate">{text}</span>
                  </div>
                )
              )}
            </div>
            <AnimatePresence>
              {isExpanded && (
                <motion.div
                  initial={{ height: 0, opacity: 0 }}
                  animate={{ height: 'auto', opacity: 1 }}
                  exit={{ height: 0, opacity: 0 }}
                  transition={{ duration: 0.2 }}
                  className="overflow-hidden"
                >
                  <div className="border-border mt-4 space-y-3 border-t pt-4">
                    {contacts.slice(0, visibleContactCount).map((contact, index) => (
                      <div
                        key={contact.id ?? index}
                        className="bg-secondary/50 flex gap-3 rounded-md p-3 text-sm"
                      >
                        {!isViewMode && (
                          <div className="shrink-0 pt-1">
                            <Checkbox
                              checked={contact.participating}
                              onCheckedChange={() => onToggleContact(company.id, contact.id)}
                            />
                          </div>
                        )}

                        <div className="flex min-w-0 flex-1 flex-col gap-1">
                          <div className="text-foreground flex items-center gap-2 font-medium">
                            <User className="text-muted-foreground h-4 w-4 shrink-0" />
                            <span className="wrap-break-word">
                              {formatName({
                                firstName: contact.firstName,
                                lastName: contact.lastName,
                              })}
                            </span>
                            {contact.zohoExisting && (
                              <div
                                className="bg-warning/10 text-warning border-warning/20 inline-flex items-center gap-1 rounded-md border px-2 py-1"
                                title="Already in Zoho CRM"
                              >
                                <Database className="h-3 w-3" />
                                <span className="text-xs font-medium">In CRM</span>
                              </div>
                            )}
                          </div>

                          <div className="text-muted-foreground flex items-center gap-2 pl-6">
                            <Briefcase className="h-3 w-3 shrink-0" />
                            <span className="wrap-break-word">{contact.title}</span>
                          </div>

                          <div className="mt-1 flex flex-wrap gap-2 pl-6 text-xs text-sky-500">
                            {contact.email && (
                              <HoverTooltip content={contact.email}>
                                <a
                                  href={`mailto:${contact.email}`}
                                  className="flex items-center gap-2 hover:underline"
                                >
                                  <Mail className="h-3 w-3 shrink-0" />
                                  <span className="truncate">{contact.email}</span>
                                </a>
                              </HoverTooltip>
                            )}
                            {contact.linkedinUrl && (
                              <HoverTooltip content={contact.linkedinUrl}>
                                <a
                                  href={contact.linkedinUrl}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className="flex items-center gap-1 hover:underline"
                                >
                                  <Image
                                    src="/linkedin-outline.svg"
                                    alt="linkedin icon"
                                    width={12}
                                    height={12}
                                    className="object-contain"
                                  />
                                  <span className="truncate">LinkedIn</span>
                                </a>
                              </HoverTooltip>
                            )}
                            {contact.phoneE164 && (
                              <HoverTooltip content={contact.phoneE164}>
                                <a
                                  href={`tel:${contact.phoneE164}`}
                                  className="flex items-center gap-1 hover:underline"
                                >
                                  <Phone className="h-3 w-3 shrink-0" />
                                </a>
                              </HoverTooltip>
                            )}
                          </div>
                        </div>
                      </div>
                    ))}
                    {contacts.length > visibleContactCount && (
                      <div className="flex justify-center pt-1">
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="text-sky-600 hover:text-sky-700"
                          onClick={() =>
                            setVisibleContactCount((prev) => prev + CONTACT_BATCH_SIZE)
                          }
                        >
                          Load more contacts
                        </Button>
                      </div>
                    )}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </Card>
  );
};

export { CampaignTargetResultCompanyCard };
