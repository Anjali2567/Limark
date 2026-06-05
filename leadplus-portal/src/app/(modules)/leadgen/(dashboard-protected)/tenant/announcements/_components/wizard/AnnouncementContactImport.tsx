'use client';

import { forwardRef, useCallback, useEffect, useImperativeHandle, useMemo, useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';

import { zodResolver } from '@hookform/resolvers/zod';
import { TenantAnnouncementContactSource } from '@/constants/announcement.constants';
import { useDebounce } from '@/hooks/useDebounce';
import { useLeadContactsSearch } from '@/hooks/useSearchLeads';
import {
  useListAnnouncementContacts,
  useListTenantContacts,
  useUpsertContactsToAnnouncement,
} from '@/hooks/useTenant';
import {
  LeadContactData,
  LeadContactSearchPayload,
  LeadContactSearchRequest,
} from '@/types/leadSearch.types';
import { TenantContact } from '@/types/tenant.types';
import {
  ContactImportFilters,
  ContactFilterValues,
  contactFilterDefaults,
  contactFilterSchema,
} from './ContactImportFilters';
import { ContactImportTable, ImportContactRow } from './ContactImportTable';
import { defaultPaginationParams } from '@/types/paginated-params';
import { PaginatedResponse } from '@/types/paginated-params';

export type AnnouncementContactImportRef = {
  save: () => Promise<void>;
};

type AnnouncementContactImportProps = {
  tenantId: string;
  announcementId: string;
  onHasSelectionChange?: (hasSelection: boolean) => void;
};

const collect = (rows: { id: string }[], deselected: Set<string>, existingIds: Set<string>) => {
  const ids = new Set(rows.map((r) => r.id));
  for (const id of existingIds) {
    if (!deselected.has(id)) ids.add(id);
  }
  return ids;
};

const AnnouncementContactImport = forwardRef<
  AnnouncementContactImportRef,
  AnnouncementContactImportProps
>(({ tenantId, announcementId, onHasSelectionChange }, ref) => {
  const { control, setValue } = useForm<ContactFilterValues>({
    resolver: zodResolver(contactFilterSchema),
    defaultValues: contactFilterDefaults,
  });

  const contactType = useWatch({ control, name: 'contactType' });
  const company = useWatch({ control, name: 'company' });
  const location = useWatch({ control, name: 'location' });
  const keywords = useWatch({ control, name: 'keywords' });

  const [page, setPage] = useState<number>(0);

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [debouncedSearch, setDebouncedSearch] = useState<string>('');
  useDebounce(searchQuery, 400, setDebouncedSearch);

  const [leadSelectedRows, setLeadSelectedRows] = useState<LeadContactData[]>([]);
  const [leadDeselectedIds, setLeadDeselectedIds] = useState<Set<string>>(new Set());

  const [crmSelectedRows, setCrmSelectedRows] = useState<TenantContact[]>([]);
  const [crmDeselectedIds, setCrmDeselectedIds] = useState<Set<string>>(new Set());

  const { data: existingData } = useListAnnouncementContacts({ tenantId, announcementId });

  const [prevContactType, setPrevContactType] = useState(contactType);
  if (prevContactType !== contactType) {
    setPrevContactType(contactType);
    setPage(0);
    setSearchQuery('');
  }

  const isLeadContacts = useMemo(
    () => contactType === TenantAnnouncementContactSource.LEAD,
    [contactType]
  );

  const { leadIds: originalExistingLeadIds, crmIds: originalExistingCrmIds } = useMemo(() => {
    const leadIds = new Set<string>();
    const crmIds = new Set<string>();
    existingData?.content?.forEach((c) => {
      if (c.sourceType === TenantAnnouncementContactSource.LEAD) {
        leadIds.add(c.sourceId);
      } else if (c.sourceType === TenantAnnouncementContactSource.CRM) {
        crmIds.add(c.sourceId);
      }
    });
    return { leadIds, crmIds };
  }, [existingData]);

  const searchParams = useMemo(() => {
    return {
      ...defaultPaginationParams,
      page,
      query: debouncedSearch,
    };
  }, [page, debouncedSearch]);

  const searchRequest = useMemo<LeadContactSearchRequest | null>(() => {
    const payload: LeadContactSearchPayload = {};
    payload.aggregateTechSearch = true;
    payload.companyCities = location.map((l) => l.value);
    payload.companyNames = company;
    payload.keywords = keywords;
    return {
      payload,
      params: searchParams,
    };
  }, [company, keywords, location, searchParams]);

  const { data: leadsData, isLoading: isLeadsLoading } = useLeadContactsSearch(
    tenantId,
    searchRequest
  );

  const { data: crmData, isLoading: isCrmLoading } = useListTenantContacts(
    tenantId,
    searchParams,
    contactType === TenantAnnouncementContactSource.CRM
  );

  const { mutateAsync: upsertContacts } = useUpsertContactsToAnnouncement();

  const tableData = useMemo<PaginatedResponse<ImportContactRow> | undefined>(() => {
    if (isLeadContacts) {
      return (
        leadsData && {
          ...leadsData,
          content: leadsData.content.map((r) => ({
            id: r.id,
            firstName: r.firstName,
            lastName: r.lastName,
            email: r.email,
            companyName: r.companyName,
          })),
        }
      );
    }
    return (
      crmData && {
        ...crmData,
        content: crmData.content.map((r) => ({
          id: r.id,
          firstName: r.firstName,
          lastName: r.lastName,
          email: r.email,
          companyName: r.companyName,
        })),
      }
    );
  }, [isLeadContacts, crmData, leadsData]);

  const selectedRowIds = useMemo<string[]>(() => {
    const leadIds = collect(leadSelectedRows, leadDeselectedIds, originalExistingLeadIds);
    const crmIds = collect(crmSelectedRows, crmDeselectedIds, originalExistingCrmIds);
    return Array.from(new Set([...leadIds, ...crmIds]));
  }, [
    leadSelectedRows,
    crmSelectedRows,
    leadDeselectedIds,
    crmDeselectedIds,
    originalExistingLeadIds,
    originalExistingCrmIds,
  ]);

  useEffect(() => {
    onHasSelectionChange?.(selectedRowIds.length > 0);
  }, [selectedRowIds.length, onHasSelectionChange]);

  const handleSelectionChange = useCallback(
    (newIds: string[]) => {
      const newIdsSet = new Set(newIds);

      const data = isLeadContacts ? leadsData?.content : crmData?.content;
      if (!data) return;

      const setDeselectedIds = isLeadContacts ? setLeadDeselectedIds : setCrmDeselectedIds;
      const originalExistingIds = isLeadContacts ? originalExistingLeadIds : originalExistingCrmIds;

      const currentPageIdSet = new Set(data.map((r) => r.id));

      if (isLeadContacts) {
        setLeadSelectedRows((prev) => {
          const otherPages = prev.filter((r) => !currentPageIdSet.has(r.id));
          const currentPageSelected = data.filter((r) => newIdsSet.has(r.id)) as LeadContactData[];
          return [...otherPages, ...currentPageSelected];
        });
      } else {
        setCrmSelectedRows((prev) => {
          const otherPages = prev.filter((r) => !currentPageIdSet.has(r.id));
          const currentPageSelected = data.filter((r) => newIdsSet.has(r.id)) as TenantContact[];
          return [...otherPages, ...currentPageSelected];
        });
      }

      setDeselectedIds((prev: Set<string>) => {
        const next = new Set(prev);
        for (const id of currentPageIdSet) {
          if (newIdsSet.has(id)) {
            next.delete(id);
          } else if (originalExistingIds.has(id)) {
            next.add(id);
          }
        }
        return next;
      });
    },
    [
      isLeadContacts,
      leadsData?.content,
      crmData?.content,
      originalExistingLeadIds,
      originalExistingCrmIds,
    ]
  );

  useImperativeHandle(ref, () => ({
    save: async () => {
      const content = existingData?.content ?? [];

      const leadIdsToAdd = leadSelectedRows
        .filter((r) => !originalExistingLeadIds.has(r.id))
        .map((r) => r.id);

      const crmContactIdsToAdd = crmSelectedRows
        .filter((r) => !originalExistingCrmIds.has(r.id))
        .map((r) => r.id);

      const contactIdsToRemove = content
        .filter(
          (c) =>
            (c.sourceType === TenantAnnouncementContactSource.LEAD &&
              leadDeselectedIds.has(c.sourceId)) ||
            (c.sourceType === TenantAnnouncementContactSource.CRM &&
              crmDeselectedIds.has(c.sourceId))
        )
        .map((c) => c.id);

      await upsertContacts({
        tenantId,
        announcementId,
        leadIdsToAdd,
        crmContactIdsToAdd,
        contactIdsToRemove,
      });
    },
  }));

  return (
    <div className="grid grid-cols-[220px_1fr] gap-6 pb-4">
      <aside className="border-border flex flex-col gap-4 border-r pr-4">
        <p className="text-foreground text-sm font-semibold">Filters</p>
        <ContactImportFilters control={control} setValue={setValue} />
      </aside>
      <div className="flex flex-col">
        <ContactImportTable
          data={tableData}
          isLoading={isLeadContacts ? isLeadsLoading : isCrmLoading}
          selectedRowIds={selectedRowIds}
          page={page}
          onPageChange={setPage}
          onSelectionChange={handleSelectionChange}
          searchQuery={searchQuery}
          onSearchQueryChange={setSearchQuery}
        />
      </div>
    </div>
  );
});

AnnouncementContactImport.displayName = 'AnnouncementContactImport';
export { AnnouncementContactImport };
