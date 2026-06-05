'use client';

import { useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';
import { toast } from 'sonner';

import { AddToCampaignModal } from '@/app/(modules)/leadgen/(dashboard-protected)/search/_components/AddToCampaignModal';
import { EmailComposeSheet } from '@/app/(modules)/leadgen/(dashboard-protected)/search/_components/EmailComposeSheet';
import { useCampaignCreateModal } from '@/app/(modules)/leadgen/(dashboard-protected)/_hooks/useCampaignCreateModal';
import { Breadcrumbs } from '@/components/Breadcrumbs';
import { DataError } from '@/components/data/DataError';
import { DataLoader } from '@/components/data/DataLoader';
import { Page } from '@/components/Page';
import DataTable from '@/components/tables/DataTable';
import { Button } from '@/components/ui/button';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useCreateCampaignForSearchResult } from '@/hooks/useCampaign';
import { useBulkSendLeadEmails } from '@/hooks/useLeadEmail';
import { useGetLeadContactsInList, useGetLeadListById } from '@/hooks/useLeadList';
import useToggle from '@/hooks/useToggle';
import { formatName } from '@/lib/utils/formatter';
import { LeadEmailPayload } from '@/types/lead-email.types';
import { LeadContactData } from '@/types/leadSearch.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { Recipient } from '@/types/workspace.types';
import { ScrollArea } from '@radix-ui/react-scroll-area';
import { useRouter } from 'next/navigation';
import { useContactSearchColumns } from '../../_hooks/useContactSearchColumns';

import { MailIcon, Plus, Sparkles, Users } from 'lucide-react';

const ContactLeadListPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const listId = searchParams.get('listId') || '';
  const page = searchParams.get('page');

  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId;
  const workspaceId = authenticatedUserDetails?.workspaceId;

  const [selectedContacts, setSelectedContacts] = useState<LeadContactData[]>([]);
  const [isAddToCampaignOpen, setIsAddToCampaignOpen] = useState(false);
  const [emailRecipients, setEmailRecipients] = useState<Recipient[]>([]);
  const [selectedEmailContacts, setSelectedEmailContacts] = useState<LeadContactData[]>([]);
  const { value: isComposeEmailOpen, toggle: toggleComposeEmail } = useToggle();

  const selectedIds = useMemo(() => selectedContacts.map((c) => c.id), [selectedContacts]);

  const { sendBulkEmails, isSending: isSendingEmail } = useBulkSendLeadEmails();

  const { mutate: createCampaign, isPending } = useCreateCampaignForSearchResult();
  const { data, isLoading, isError } = useGetLeadListById({
    tenantId: tenantId || '',
    workspaceId: workspaceId || '',
    listId: listId || '',
  });

  const onClickCompanyName = useCallback(
    (id: string) => {
      router.push(appRoutes.leadgen.search.companyInfo(id));
    },
    [router]
  );

  const onClickContactName = useCallback(
    (id: string) => {
      router.push(appRoutes.leadgen.search.contactInfo(id));
    },
    [router]
  );

  const onProceedToEmail = useCallback(
    (rows: LeadContactData[]) => {
      const validContacts = rows.filter((row) => row.email);
      if (validContacts.length === 0) {
        toast.warning('No valid email addresses found for the selected contacts');
        return;
      }

      const recipients: Recipient[] = validContacts.map((row) => ({
        name: formatName({ firstName: row.firstName, lastName: row.lastName }),
        email: row.email!,
      }));

      setEmailRecipients(recipients);
      setSelectedEmailContacts(validContacts);
      toggleComposeEmail();
    },
    [toggleComposeEmail]
  );

  const onSendEmail = useCallback(
    async (payload: LeadEmailPayload) => {
      if (!tenantId || !workspaceId) return;
      await sendBulkEmails(tenantId, workspaceId, selectedEmailContacts, payload);
      toggleComposeEmail();
      setSelectedContacts([]);
    },
    [selectedEmailContacts, sendBulkEmails, tenantId, toggleComposeEmail, workspaceId]
  );

  const columns = useContactSearchColumns(tenantId || '', onClickContactName, onClickCompanyName, onProceedToEmail);

  const defaultColumns = useMemo<ColumnType<LeadContactData>[]>(() => {
    return columns.slice(0, 5).map((column) => ({
      ...column,
      width: '20%',
    }));
  }, [columns]);

  const { data: contactsData, isLoading: contactsLoading } = useGetLeadContactsInList(
    {
      tenantId: tenantId || '',
      workspaceId: workspaceId || '',
      listId: listId || '',
    },
    { ...defaultPaginationParams, page: Number(page) }
  );

  const handleSelectionChange = useCallback(
    (newIds: string[]) => {
      setSelectedContacts((prev) => {
        const newIdSet = new Set(newIds);
        const kept = prev.filter((c) => newIdSet.has(c.id));
        const keptIdSet = new Set(kept.map((c) => c.id));
        const added = (contactsData?.content ?? []).filter(
          (c) => newIdSet.has(c.id) && !keptIdSet.has(c.id)
        );
        return [...kept, ...added];
      });
    },
    [contactsData?.content]
  );

  const onBulkSend = useCallback(() => {
    const validContacts = selectedContacts.filter((c) => !!c.email);

    if (validContacts.length === 0) {
      toast.warning('No valid email addresses found for the selected contacts');
      return;
    }

    const recipients: Recipient[] = validContacts.map((row) => ({
      name: formatName({ firstName: row.firstName, lastName: row.lastName }),
      email: row.email!,
    }));

    setEmailRecipients(recipients);
    setSelectedEmailContacts(validContacts);
    toggleComposeEmail();
  }, [selectedContacts, toggleComposeEmail]);

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('page', newPage.toString());
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const onCampaignCreate = useCallback(() => {
    if (!authenticatedUserDetails || selectedIds.length === 0) return;
    createCampaign(
      {
        tenantId: authenticatedUserDetails.tenantId,
        workspaceId: authenticatedUserDetails.workspaceId,
        requestBody: {
          contactIds: selectedIds,
        },
      },
      {
        onSuccess: (data) => {
          toast.success('Campaign created successfully');
          router.push(appRoutes.leadgen.campaigns.sequence(data.id));
        },
      }
    );
  }, [authenticatedUserDetails, createCampaign, router, selectedIds]);

  const { handleCampaignCreate } = useCampaignCreateModal(selectedContacts, onCampaignCreate);

  if (isLoading || contactsLoading) return <DataLoader />;
  if (isError || !data) return <DataError />;

  return (
    <Page className="flex-col">
      <div className="border-border bg-card shrink-0 border-b px-6 py-4">
        <Breadcrumbs
          items={[
            { label: 'Lists', href: appRoutes.leadgen.leadList.root },
            { label: data?.name || 'Contacts List' },
          ]}
        />
        <div className="mt-4 flex items-center justify-between">
          <div className="flex flex-col gap-2">
            <div className="flex items-center gap-2">
              <h1 className="text-foreground text-lg font-semibold">{data?.name}</h1>
              <span className="text-primary inline-flex items-center gap-1.5 rounded-md bg-sky-500/10 px-2 py-0.5 text-xs">
                <Users className="h-3 w-3" />
                People
              </span>
            </div>
            <div className="text-muted-foreground text-sm">
              {contactsData?.page?.totalElements}{' '}
              {contactsData?.page?.totalElements === 1 ? 'record' : 'records'}
            </div>
          </div>
          <div className="space-x-2">
            <Button onClick={onBulkSend} disabled={isPending || selectedIds.length === 0}>
              <MailIcon className="mr-2 h-4 w-4" />
              Send Email ({selectedIds.length})
            </Button>
            <Button
              onClick={() => setIsAddToCampaignOpen(true)}
              disabled={selectedIds.length === 0}
            >
              <Plus className="mr-2 h-4 w-4" />
              Add to Campaign ({selectedIds.length})
            </Button>
            <Button onClick={handleCampaignCreate} disabled={isPending || selectedIds.length === 0}>
              <Sparkles className="mr-2 h-4 w-4" />
              Start Campaign ({selectedIds.length})
            </Button>
          </div>
        </div>
      </div>
      <main className="flex flex-1 flex-col overflow-hidden">
        <div className="flex min-h-0 flex-1 flex-col">
          {contactsLoading || (contactsData && contactsData.content.length === 0) ? (
            <div className="flex min-h-0 flex-1 flex-col overflow-hidden px-6 py-4">
              <DataTable
                rowClassName="h-12"
                data={contactsData?.content ?? []}
                pagination={contactsData?.page}
                columns={defaultColumns}
                isLoading={contactsLoading}
                onPageChange={handlePageChange}
                initialPage={Number(page || '0')}
                selectable
                selectedRowIds={selectedIds}
                onSelectionChange={handleSelectionChange}
              />
            </div>
          ) : (
            <ScrollArea className="flex-1 overflow-y-auto">
              <div className="flex min-h-0 flex-1 flex-col overflow-hidden px-6 py-4">
                <DataTable
                  rowClassName="h-12"
                  style="grid"
                  data={contactsData?.content ?? []}
                  pagination={contactsData?.page}
                  columns={columns}
                  isLoading={contactsLoading}
                  onPageChange={handlePageChange}
                  initialPage={Number(page || '0')}
                  selectable
                  scrollHorizontal
                  freezeColumnsCount={1}
                  selectedRowIds={selectedIds}
                  onSelectionChange={handleSelectionChange}
                />
              </div>
            </ScrollArea>
          )}
        </div>
      </main>

      <EmailComposeSheet
        open={isComposeEmailOpen}
        onOpenChange={toggleComposeEmail}
        recipients={emailRecipients}
        onSend={onSendEmail}
        isSending={isSendingEmail}
      />
      <AddToCampaignModal
        open={isAddToCampaignOpen}
        onOpenChange={setIsAddToCampaignOpen}
        contactIds={selectedIds}
        tenantId={tenantId || ''}
        workspaceId={workspaceId || ''}
      />
    </Page>
  );
};

export default ContactLeadListPage;
