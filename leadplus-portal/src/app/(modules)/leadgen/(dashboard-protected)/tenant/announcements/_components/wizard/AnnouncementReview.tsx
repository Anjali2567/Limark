'use client';

import { ReactNode, useMemo } from 'react';

import { SimpleHTML } from '@/components/chat/SimpleHTML';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useListAnnouncementContacts } from '@/hooks/useTenant';
import { TenantAnnouncement } from '@/types/tenant.types';

import { CheckCircle2, FileText, Users } from 'lucide-react';
import { useGetAllAttachments } from '@/hooks/useAttachments';
import { defaultPaginationParams } from '@/types/paginated-params';
import { useAuth } from '@/context/AuthContext';
import { formatFileSize } from '@/lib/utils/file';

type AnnouncementReviewProps = {
  tenantId: string;
  announcement: TenantAnnouncement;
  fromEmail?: string;
  fromName?: string;
};

const KeyValueRow = ({ label, value }: { label: string; value?: string | number }) => (
  <div className="grid grid-cols-[80px_1fr] gap-2 text-sm">
    <span className="text-muted-foreground font-medium">{label}:</span>
    <span className="text-foreground">{value || '—'}</span>
  </div>
);

const SectionCard = ({ children, className }: { children: ReactNode; className?: string }) => (
  <Card className={className}>
    <CardContent className="p-6">{children}</CardContent>
  </Card>
);

const AnnouncementReview = ({
  tenantId,
  announcement,
  fromEmail,
  fromName,
}: AnnouncementReviewProps) => {
  const { authenticatedUserDetails } = useAuth();
  const { data: contactsData } = useListAnnouncementContacts({
    tenantId,
    announcementId: announcement.id,
  });

  const { data: systemDocuments } = useGetAllAttachments({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    params: {
      ...defaultPaginationParams,
      size: 100,
    },
  });

  const contacts = contactsData?.content ?? [];
  const recipientCount = contactsData?.page.totalElements ?? announcement.recipientCount;
  const fromDisplay = useMemo(
    () => (fromName ? `${fromName} <${fromEmail}>` : fromEmail),
    [fromName, fromEmail]
  );

  const recipientFields = [
    { label: 'CC', value: announcement.ccRecipients?.map((r) => r.email).join(', ') },
    { label: 'BCC', value: announcement.bccRecipients?.map((r) => r.email).join(', ') },
  ];

  const attachments = useMemo(() => {
    if (!announcement.attachmentIds || announcement.attachmentIds.length === 0) return [];
    const attachmentSet = new Set(announcement.attachmentIds);
    return systemDocuments?.content.filter((doc) => attachmentSet.has(doc.id)) ?? [];
  }, [announcement.attachmentIds, systemDocuments]);

  return (
    <div className="space-y-4 pb-4">
      <SectionCard className="border-amber-200 bg-linear-to-br from-amber-50 to-orange-50">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-amber-100">
            <CheckCircle2 className="h-5 w-5 text-amber-600" />
          </div>
          <div>
            <p className="text-foreground text-sm font-medium">
              Ready to send to {recipientCount.toLocaleString()}{' '}
              {recipientCount === 1 ? 'contact' : 'contacts'}
            </p>
            <p className="text-muted-foreground mt-1 text-xs">
              Every hour, only 20 emails will be sent to avoid spam filters. Depending on the number
              of recipients, it may take some time for all emails to be sent.
            </p>
          </div>
        </div>
      </SectionCard>
      <SectionCard className="border-primary/20 border-2">
        <div className="border-border flex items-center gap-2 border-b pb-3">
          <FileText className="text-primary h-5 w-5" />
          <h3 className="text-foreground text-base font-semibold">Email Preview</h3>
        </div>
        <div className="mt-3 space-y-3">
          <KeyValueRow label="From" value={fromDisplay} />
          <KeyValueRow
            label="To"
            value={`${recipientCount} ${recipientCount === 1 ? 'contact' : 'contacts'}`}
          />
          {recipientFields.map(
            ({ label, value }) => value && <KeyValueRow key={label} label={label} value={value} />
          )}
          <KeyValueRow label="Subject" value={announcement.subject} />
          {announcement.body && (
            <div className="border-border border-t pt-3">
              <p className="text-muted-foreground mb-2 text-sm font-medium">Message:</p>
              <div className="bg-secondary max-h-50 overflow-y-auto rounded-md p-4">
                <SimpleHTML
                  text={announcement.body.replace(/\n/g, '<br />')}
                  className="prose prose-sm text-foreground text-sm"
                />
              </div>
            </div>
          )}
        </div>
        {attachments.length > 0 && (
          <div className="border-border border-t pt-3">
            <p className="text-muted-foreground mb-2 text-sm font-medium">Attachments:</p>
            <div className="flex flex-wrap gap-2">
              {attachments.map((attachment) => (
                <div
                  key={attachment.id}
                  className="bg-secondary flex max-w-xs items-center gap-3 rounded-md px-3 py-2"
                >
                  <div className="rounded border bg-white p-1.5 shadow-sm">
                    <FileText className="h-4 w-4 text-sky-500" />
                  </div>
                  <div className="min-w-0">
                    <p
                      className="text-foreground truncate text-sm font-medium"
                      title={attachment.filename}
                    >
                      {attachment.filename}
                    </p>
                    {attachment?.sizeBytes && (
                      <p className="text-muted-foreground text-xs">
                        {formatFileSize(attachment.sizeBytes)}
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </SectionCard>
      <SectionCard>
        <div className="border-border mb-4 flex items-center gap-2 border-b pb-3">
          <Users className="text-primary h-5 w-5" />
          <h3 className="text-foreground text-base font-semibold">Recipients ({recipientCount})</h3>
        </div>
        <ScrollArea className="h-50">
          <div className="space-y-2">
            {contacts.length === 0 ? (
              <p className="text-muted-foreground py-4 text-center text-sm">
                No recipients added yet
              </p>
            ) : (
              contacts.map((contact) => (
                <div
                  key={contact.id}
                  className="bg-secondary flex items-center justify-between rounded-md px-3 py-2"
                >
                  <div className="flex flex-col">
                    <span className="text-foreground text-sm font-medium">{contact.firstName}</span>
                    <span className="text-muted-foreground text-xs">{contact.email}</span>
                  </div>
                  <Badge variant="secondary" className="text-xs">
                    {contact.sourceType}
                  </Badge>
                </div>
              ))
            )}
          </div>
        </ScrollArea>
      </SectionCard>
    </div>
  );
};

export { AnnouncementReview };
