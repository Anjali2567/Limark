'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';
import { toast } from 'sonner';

import { AddToCampaignModal } from '@/app/(modules)/leadgen/(dashboard-protected)/search/_components/AddToCampaignModal';
import { EmailComposeSheet } from '@/app/(modules)/leadgen/(dashboard-protected)/search/_components/EmailComposeSheet';
import { useCampaignCreateModal } from '@/app/(modules)/leadgen/(dashboard-protected)/_hooks/useCampaignCreateModal';
import { Page } from '@/components/Page';
import { appRoutes } from '@/config/routes';
import { LeadSearchType, LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { useCreateCampaignForSearchResult } from '@/hooks/useCampaign';
import { useBulkSendLeadEmails } from '@/hooks/useLeadEmail';
import { useFetchContactIds, useLeadContactsSearch } from '@/hooks/useSearchLeads';
import useToggle from '@/hooks/useToggle';
import { formatName } from '@/lib/utils/formatter';
import { LeadEmailPayload } from '@/types/lead-email.types';
import {
  LeadContactData,
  LeadCompanyData,
  LeadFilterCriteria,
  LeadSearchPayload,
} from '@/types/leadSearch.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { Recipient } from '@/types/workspace.types';
import { PEOPLE_SEARCH_DIRECT_CRITERIA_KEYS } from '@/app/(modules)/leadgen/(dashboard-protected)/_constants/leadFilterCriteria.constants';
import { useContactSearchColumns } from '../../_hooks/useContactSearchColumns';
import { FilterPanel } from '../_components/FilterPanel';
import { SearchResults } from '../_components/SearchResults';
import { useFilterContext } from '../_context/FilterContext';
import { FilterPanelLayout } from '../_layouts/FilterPanelLayout';

const buildLeadFilterCriteria = (
  filters: LeadSearchPayload | null | undefined,
  selectedCompanyRows: LeadCompanyData[]
): LeadFilterCriteria | undefined => {
  if (!filters) return undefined;

  // companyNames is computed from selectedCompanyRows for readable display in the UI.
  // All other fields are derived automatically from PEOPLE_SEARCH_DIRECT_CRITERIA_KEYS
  // (defined in leadFilterCriteria.constants.ts). Add new filter fields there.
  const companyNames = selectedCompanyRows.length > 0
    ? selectedCompanyRows.map((row) => row.name || row.domain).filter(Boolean) as string[]
    : undefined;

  const directFields = Object.fromEntries(
    PEOPLE_SEARCH_DIRECT_CRITERIA_KEYS.map((k) => [k, filters[k as keyof LeadSearchPayload]])
  ) as Partial<LeadFilterCriteria>;

  const criteria: LeadFilterCriteria = {
    companyNames: companyNames?.length ? companyNames : undefined,
    ...directFields,
  };

  const hasAtLeastOneCriteria = Object.values(criteria).some(
    (value) => (Array.isArray(value) && value.length > 0) || value === true
  );

  return hasAtLeastOneCriteria ? criteria : undefined;
};

const ContactsSearchPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [emailRecipients, setEmailRecipients] = useState<Recipient[]>([]);
  const [selectedContacts, setSelectedContacts] = useState<LeadContactData[]>([]);
  const [selectedRows, setSelectedRows] = useState<LeadContactData[]>([]);
  const [addToCampaignIds, setAddToCampaignIds] = useState<string[]>([]);
  const [isAddToCampaignOpen, setIsAddToCampaignOpen] = useState(false);

  const { value: isComposeEmailOpen, toggle: toggleComposeEmail } = useToggle();
  const { value: isFilterPanelOpen, toggle } = useToggle(true);

  const { debouncedFilters, selectedCompanyRows, setSelectedCompanyRows } = useFilterContext();
  const { authenticatedUserDetails } = useAuth();
  const { tenantId, workspaceId } = authenticatedUserDetails || { tenantId: '', workspaceId: '' };

  const searchFilterKey = useMemo(() => {
    if (!debouncedFilters) return null;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { page, sort, resultsSearch, ...searchFilters } = debouncedFilters;
    return JSON.stringify(searchFilters);
  }, [debouncedFilters]);

  const [prevSearchFilterKey, setPrevSearchFilterKey] = useState(searchFilterKey);

  if (searchFilterKey !== prevSearchFilterKey) {
    setPrevSearchFilterKey(searchFilterKey);
    if (prevSearchFilterKey !== null) {
      setSelectedRows([]);
    }
  }

  const { sendBulkEmails, isSending: isSendingEmail } = useBulkSendLeadEmails();
  const { mutate: createCampaign, isPending } = useCreateCampaignForSearchResult();
  const { mutateAsync: fetchAllContactIds, isPending: isFetchingContactIds } = useFetchContactIds();

  const disableAnimation = useMemo(() => searchParams.get('tabSwitch') === 'true', [searchParams]);

  const request = useMemo(() => {
    if (!debouncedFilters) return null;

    const {
      contactNames,
      cities,
      states,
      countries,
      companyCities,
      companyStates,
      companyCountries,
      regions,
      employeeRanges,
      seniority,
      titles,
      departments,
      keywords,
      industries,
      technologies,
      toolsServices,
      bdNames,
      isrNames,
      priorities,
      titleCategories,
      campaignEligibleOnly,
    } = debouncedFilters;

    // selectedCompanyRows is the single source of truth for company filtering in people search.
    // The dropdown syncs selections into selectedCompanyRows, so companyIds from filters is not used.
    const finalCompanyIds =
      selectedCompanyRows.length > 0
        ? selectedCompanyRows.map((row) => Number(row.id)).filter(Boolean)
        : [];

    return {
      payload: {
        companyIds: finalCompanyIds,
        contactNames,
        companyNames: [],
        cities,
        states,
        countries,
        companyCities,
        companyStates,
        companyCountries,
        regions,
        employeeRanges,
        seniority,
        titles,
        departments,
        keywords,
        industries,
        technologies,
        toolsServices,
        bdNames,
        isrNames,
        priorities,
        titleCategories,
        campaignEligibleOnly: campaignEligibleOnly ?? false,
        aggregateTechSearch: true,
      },
      params: {
        ...defaultPaginationParams,
        page: debouncedFilters.page ?? 0,
        query: debouncedFilters.resultsSearch,
        sort: debouncedFilters.sort ?? defaultPaginationParams.sort,
      },
    };
  }, [debouncedFilters, selectedCompanyRows]);

  const { data, isLoading } = useLeadContactsSearch(
    authenticatedUserDetails?.tenantId ?? '',
    request
  );

  const onFetchAllIds = useCallback(async () => {
    if (!request || !tenantId) return [];
    const ids = await fetchAllContactIds({ tenantId, request });
    setSelectedRows(ids.map((id) => ({ id }) as LeadContactData));
    return ids;
  }, [fetchAllContactIds, request, tenantId]);

  const onAddContacts = useCallback((rows: LeadContactData[]) => {
    setAddToCampaignIds(rows.map((r) => r.id));
    setIsAddToCampaignOpen(true);
  }, []);

  const onCampaignCreate = useCallback(
    (rows: LeadContactData[]) => {
      if (!authenticatedUserDetails) return;
      const contactIds = rows.map((row) => row.id);
      const leadFilterCriteria = buildLeadFilterCriteria(debouncedFilters, selectedCompanyRows);
      createCampaign(
        {
          tenantId: authenticatedUserDetails.tenantId,
          workspaceId: authenticatedUserDetails.workspaceId,
          requestBody: {
            contactIds,
            leadFilterCriteria,
          },
        },
        {
          onSuccess: (data) => {
            toast.success('Campaign created successfully');
            router.push(appRoutes.leadgen.campaigns.sequence(data.id));
          },
        }
      );
    },
    [authenticatedUserDetails, createCampaign, debouncedFilters, router, selectedCompanyRows]
  );

  const { handleCampaignCreate } = useCampaignCreateModal(selectedRows, () =>
    onCampaignCreate(selectedRows)
  );

  const onProceed = useCallback(() => {
    handleCampaignCreate();
  }, [handleCampaignCreate]);

  const onProceedToEmail = useCallback(
    (rows: LeadContactData[]) => {
      const validContacts = rows.filter((row) => row.email);
      if (validContacts.length === 0) return;

      const recipients: Recipient[] = validContacts.map((row) => ({
        name: formatName({ firstName: row.firstName, lastName: row.lastName }),
        email: row.email!,
      }));

      setEmailRecipients(recipients);
      setSelectedContacts(validContacts);
      toggleComposeEmail();
    },
    [toggleComposeEmail]
  );

  const onSendEmail = useCallback(
    async (payload: LeadEmailPayload) => {
      await sendBulkEmails(tenantId, workspaceId, selectedContacts, payload);
      toggleComposeEmail();
      setSelectedContacts([]);
      setSelectedRows([]);
    },
    [selectedContacts, sendBulkEmails, tenantId, toggleComposeEmail, workspaceId]
  );

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

  const columns = useContactSearchColumns(
    tenantId,
    onClickContactName,
    onClickCompanyName,
    onProceedToEmail
  );

  return (
    <>
      <Page disableAnimation={disableAnimation}>
        <FilterPanelLayout
          searchType={LeadSearchType.PEOPLE}
          isFilterPanelOpen={isFilterPanelOpen}
          filterPanel={
            <FilterPanel
              searchType={LeadSearchType.PEOPLE}
              filterType={LeadType.LEAD_CONTACT}
              resultCount={data?.page.totalElements}
              selectedCompanyRows={selectedCompanyRows}
              onClearFilters={() => {
                setSelectedRows([]);
                setSelectedCompanyRows([]);
              }}
            />
          }
          content={
            <SearchResults
              onCollapse={toggle}
              columns={columns}
              data={data}
              isLoading={isLoading}
              proceedButtonText="Create Campaign"
              onProceed={onProceed}
              onSendEmail={onProceedToEmail}
              onAddContacts={onAddContacts}
              isProceeding={isPending}
              searchPlaceholder="Search contacts..."
              tenantId={tenantId}
              workspaceId={workspaceId}
              selectedRows={selectedRows}
              setSelectedRows={setSelectedRows}
              leadType={LeadType.LEAD_CONTACT}
              onFetchAllIds={onFetchAllIds}
              isFetchingIds={isFetchingContactIds}
            />
          }
        />
      </Page>
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
        contactIds={addToCampaignIds}
        tenantId={tenantId}
        workspaceId={workspaceId}
      />
    </>
  );
};

export default ContactsSearchPage;
