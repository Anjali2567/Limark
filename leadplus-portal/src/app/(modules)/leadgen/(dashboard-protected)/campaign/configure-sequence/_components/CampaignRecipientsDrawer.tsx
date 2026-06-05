import { useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { Badge } from '@/components/ui/badge';
import { Card } from '@/components/ui/card';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { useAuth } from '@/context/AuthContext';
import { useGetCampaignBasicDetails } from '@/hooks/useCampaign';
import { useGetTenantRecipients } from '@/hooks/useTenant';
import { useGetWorkspaceDetails } from '@/hooks/useWorkspace';
import { CampaignCompanyResponse } from '@/types/campaign.types';
import { Recipient } from '@/types/workspace.types';

import { Building2, ChevronDown, Loader2, Mail, Phone, UserCheck } from 'lucide-react';

type CampaignRecipientsDrawerProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  campaignId: string;
  campaignCompanies: CampaignCompanyResponse[];
  totalContacts: number;
  totalCompanies: number;
};

type RecipientsSourceGroup = {
  label: string;
  cc: Recipient[];
  bcc: Recipient[];
};

const RecipientBadges = ({ recipients }: { recipients: Recipient[] }) => (
  <div className="mt-1 flex flex-wrap gap-1.5">
    {recipients.map((r) => (
      <Badge key={r.email} variant="secondary" className="text-xs font-normal">
        {r.name ? `${r.name} <${r.email}>` : r.email}
      </Badge>
    ))}
  </div>
);

const CampaignRecipientsDrawer = ({
  open,
  onOpenChange,
  campaignId,
  campaignCompanies,
  totalContacts,
  totalCompanies,
}: CampaignRecipientsDrawerProps) => {
  const [search, setSearch] = useState('');
  const [isCcBccExpanded, setIsCcBccExpanded] = useState(false);

  const { authenticatedUserDetails } = useAuth();

  const { data: basicDetails, isLoading: isBasicDetailsLoading } = useGetCampaignBasicDetails(
    {
      tenantId: authenticatedUserDetails?.tenantId || '',
      workspaceId: authenticatedUserDetails?.workspaceId || '',
      campaignId,
    },
    open
  );

  const { data: workspace, isLoading: isWorkspaceLoading } = useGetWorkspaceDetails({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
  });

  const { data: tenantRecipients, isLoading: isTenantLoading } = useGetTenantRecipients({
    tenantId: authenticatedUserDetails?.tenantId || '',
  });

  const isRecipientsLoading = isBasicDetailsLoading || isWorkspaceLoading || isTenantLoading;

  const recipientSources = useMemo<RecipientsSourceGroup[]>(() => {
    const sources: RecipientsSourceGroup[] = [];

    const tenantCc = tenantRecipients?.ccRecipients ?? [];
    const tenantBcc = tenantRecipients?.bccRecipients ?? [];
    if (tenantCc.length > 0 || tenantBcc.length > 0) {
      sources.push({ label: 'Tenant', cc: tenantCc, bcc: tenantBcc });
    }

    const workspaceCc = workspace?.ccRecipients ?? [];
    const workspaceBcc = workspace?.bccRecipients ?? [];
    if (workspaceCc.length > 0 || workspaceBcc.length > 0) {
      sources.push({ label: 'Workspace', cc: workspaceCc, bcc: workspaceBcc });
    }

    // Campaign-level only shows when explicitly set (not null)
    if (basicDetails?.ccRecipients !== null || basicDetails?.bccRecipients !== null) {
      const campaignCc = basicDetails?.ccRecipients ?? [];
      const campaignBcc = basicDetails?.bccRecipients ?? [];
      if (campaignCc.length > 0 || campaignBcc.length > 0) {
        sources.push({ label: 'Campaign', cc: campaignCc, bcc: campaignBcc });
      }
    }

    return sources;
  }, [tenantRecipients, workspace, basicDetails]);

  const hasCcBcc = recipientSources.length > 0;

  const filteredCompanies = useMemo(() => {
    const trimmed = search.trim().toLowerCase();
    if (!trimmed) return campaignCompanies;

    return campaignCompanies
      .map((company) => {
        const matchingContacts = (company.campaignContacts ?? []).filter((contact) => {
          const fullName = (contact.fullName ?? '').toLowerCase();
          const email = (contact.email ?? '').toLowerCase();
          return fullName.includes(trimmed) || email.includes(trimmed);
        });

        if (matchingContacts.length > 0) {
          return { ...company, campaignContacts: matchingContacts };
        }

        if (company.name.toLowerCase().includes(trimmed)) {
          return company;
        }

        return null;
      })
      .filter(Boolean) as CampaignCompanyResponse[];
  }, [campaignCompanies, search]);

  const filteredContactCount = useMemo(() => {
    return filteredCompanies.reduce(
      (sum, company) => sum + (company.campaignContacts?.length ?? 0),
      0
    );
  }, [filteredCompanies]);

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="flex w-full flex-col gap-0 px-6 sm:max-w-2xl" side="right">
        <SheetHeader className="shrink-0 px-0">
          <div className="flex items-center justify-between">
            <div>
              <SheetTitle className="text-2xl">Campaign Recipients</SheetTitle>
              <SheetDescription className="mt-1">
                All contacts being emailed in this campaign
              </SheetDescription>
            </div>
            <Badge variant="outline" className="text-sm whitespace-nowrap">
              {totalContacts} {totalContacts === 1 ? 'contact' : 'contacts'} &middot;{' '}
              {totalCompanies} {totalCompanies === 1 ? 'company' : 'companies'}
            </Badge>
          </div>
        </SheetHeader>

        <div className="relative mb-4">
          <SearchBar onChange={setSearch} placeholder="Search by name or email..." />
        </div>

        {(hasCcBcc || isRecipientsLoading) && (
          <div className="mb-4 shrink-0">
            <button
              type="button"
              className="text-muted-foreground hover:text-foreground flex w-full items-center gap-2 text-sm font-medium transition-colors"
              onClick={() => setIsCcBccExpanded((prev) => !prev)}
            >
              <ChevronDown
                className={`h-4 w-4 transition-transform ${isCcBccExpanded ? 'rotate-0' : '-rotate-90'}`}
              />
              Tenant & Workspace CC / BCC Recipients
            </button>
            {isCcBccExpanded && (
              <div className="mt-3 space-y-3 pl-6">
                {isRecipientsLoading ? (
                  <div className="text-muted-foreground flex items-center gap-2 py-2 text-sm">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Loading...
                  </div>
                ) : recipientSources.length === 0 ? (
                  <p className="text-muted-foreground text-xs">No CC/BCC recipients configured</p>
                ) : (
                  recipientSources.map((source) => (
                    <div key={source.label} className="border-border rounded-md border px-3 py-2.5">
                      <p className="text-foreground mb-2 text-xs font-semibold">{source.label}</p>
                      <div className="space-y-2">
                        {source.cc.length > 0 && (
                          <div>
                            <span className="text-muted-foreground text-xs font-medium tracking-wider uppercase">
                              CC
                            </span>
                            <RecipientBadges recipients={source.cc} />
                          </div>
                        )}
                        {source.bcc.length > 0 && (
                          <div>
                            <span className="text-muted-foreground text-xs font-medium tracking-wider uppercase">
                              BCC
                            </span>
                            <RecipientBadges recipients={source.bcc} />
                          </div>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        )}

        <div className="-mx-6 mb-4 flex-1 overflow-y-auto px-6">
          {filteredCompanies.length === 0 ? (
            <div className="text-muted-foreground py-8 text-center">No contacts found</div>
          ) : (
            <div className="space-y-4">
              {search.trim() && (
                <p className="text-muted-foreground text-xs">
                  {filteredContactCount} {filteredContactCount === 1 ? 'contact' : 'contacts'} in{' '}
                  {filteredCompanies.length}{' '}
                  {filteredCompanies.length === 1 ? 'company' : 'companies'}
                </p>
              )}
              {filteredCompanies.map((company) => (
                <div key={company.id}>
                  <div className="mb-2 flex items-center gap-2">
                    <Building2 className="text-muted-foreground h-4 w-4 shrink-0" />
                    <span className="text-foreground text-sm font-semibold">{company.name}</span>
                    <Badge variant="outline" className="text-xs">
                      {company.campaignContacts?.length ?? 0}
                    </Badge>
                  </div>
                  <div className="space-y-2 pl-6">
                    {(company.campaignContacts ?? []).map((contact) => (
                      <Card key={contact.id} className="p-3">
                        <div>
                          <div className="mb-1.5">
                            <p className="text-foreground text-sm font-semibold">
                              {contact.fullName ||
                                [contact.firstName, contact.lastName].filter(Boolean).join(' ') ||
                                'Unknown'}
                            </p>
                            {contact.title && (
                              <p className="text-muted-foreground text-xs">{contact.title}</p>
                            )}
                          </div>
                          <div className="space-y-1">
                            {contact.email && (
                              <div className="text-muted-foreground flex items-center gap-2 text-xs">
                                <Mail className="h-3 w-3 shrink-0" />
                                <span className="truncate">{contact.email}</span>
                              </div>
                            )}
                            {contact.phoneE164 && (
                              <div className="text-muted-foreground flex items-center gap-2 text-xs">
                                <Phone className="h-3 w-3 shrink-0" />
                                <span>{contact.phoneE164}</span>
                              </div>
                            )}
                            {contact.bdEmail && (
                              <div className="flex items-center gap-2 text-xs text-sky-600">
                                <UserCheck className="h-3 w-3 shrink-0" />
                                <span className="truncate">
                                  CC:{' '}
                                  {contact.bdName
                                    ? `${contact.bdName} (${contact.bdEmail})`
                                    : contact.bdEmail}
                                </span>
                              </div>
                            )}
                          </div>
                        </div>
                      </Card>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
};

export { CampaignRecipientsDrawer };
