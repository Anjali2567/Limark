'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { Breadcrumbs } from '@/components/Breadcrumbs';
import { DataError } from '@/components/data/DataError';
import { DataLoader } from '@/components/data/DataLoader';
import { Page } from '@/components/Page';
import DataTable from '@/components/tables/DataTable';
import { Button } from '@/components/ui/button';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useGetLeadCompaniesInList, useGetLeadListById } from '@/hooks/useLeadList';
import { LeadCompanyData } from '@/types/leadSearch.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { ScrollArea } from '@radix-ui/react-scroll-area';
import { useCompanySearchColumns } from '../../_hooks/useCompanySearchColumns';

import { Building2, ExternalLink } from 'lucide-react';

const CompanyLeadListPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const listId = searchParams.get('listId');
  const page = searchParams.get('page');

  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId;
  const workspaceId = authenticatedUserDetails?.workspaceId;

  const [selectedIds, setSelectedIds] = useState<string[]>([]);

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

  const onClickContacts = useCallback(
    (row: LeadCompanyData) => {
      const companyParam = encodeURIComponent(row.name);
      router.push(`${appRoutes.leadgen.search.contactSearch}?companyNames=${companyParam}`);
    },
    [router]
  );

  const columns = useCompanySearchColumns(onClickCompanyName, onClickContacts);

  const defaultColumns = useMemo<ColumnType<LeadCompanyData>[]>(() => {
    return columns.slice(0, 5).map((column) => ({
      ...column,
      width: '20%',
    }));
  }, [columns]);

  const { data: contactsData, isLoading: contactsLoading } = useGetLeadCompaniesInList(
    {
      tenantId: tenantId || '',
      workspaceId: workspaceId || '',
      listId: listId || '',
    },
    { ...defaultPaginationParams, page: Number(page) }
  );

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('page', newPage.toString());
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const onContactSearch = useCallback(() => {
    if (!contactsData?.content?.length) return;
    const idToNameMap = new Map(contactsData.content.map(({ id, name }) => [id, name]));
    const names = selectedIds.map((id) => idToNameMap.get(id)).filter(Boolean);

    if (!names.length) return;

    const params = new URLSearchParams();
    params.set('companyNames', names.join('|'));

    router.push(`${appRoutes.leadgen.search.contactSearch}?${params.toString()}`);
  }, [router, selectedIds, contactsData]);

  if (isLoading || contactsLoading) return <DataLoader />;
  if (isError || !data) return <DataError />;

  return (
    <Page className="flex-col">
      <div className="border-border bg-card shrink-0 border-b px-6 py-4">
        <Breadcrumbs
          items={[
            { label: 'Lists', href: appRoutes.leadgen.leadList.root },
            { label: data?.name || 'Companies List' },
          ]}
        />
        <div className="mt-4 flex items-center justify-between">
          <div className="flex flex-col gap-2">
            <div className="flex items-center gap-2">
              <h1 className="text-foreground text-lg font-semibold">{data?.name}</h1>
              <span className="inline-flex items-center gap-1.5 rounded-md bg-purple-500/10 px-2 py-0.5 text-xs text-purple-500">
                <Building2 className="h-3 w-3" />
                Companies
              </span>
            </div>
            <div className="text-muted-foreground text-sm">
              {contactsData?.page?.totalElements}{' '}
              {contactsData?.page?.totalElements === 1 ? 'record' : 'records'}
            </div>
          </div>
          <Button onClick={onContactSearch} disabled={selectedIds.length === 0}>
            <ExternalLink className="mr-2 h-4 w-4" />
            Search Contacts ({selectedIds.length})
          </Button>
        </div>
      </div>
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
              onSelectionChange={setSelectedIds}
            />
          </div>
        </ScrollArea>
      )}
    </Page>
  );
};

export default CompanyLeadListPage;
