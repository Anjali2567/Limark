'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'sonner';

import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import { appRoutes } from '@/config/routes';
import { LeadType } from '@/constants/leadSearch.constants';
import { useDeleteLeadList, useGetLeadLists } from '@/hooks/useLeadList';
import { LeadList } from '@/types/lead-list.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { useLeadListColumns } from '../_hooks/useLeadListColumns';

type PeopleListTabProps = {
  tenantId: string;
  workspaceId: string;
  searchQuery: string;
  selectedIds: string[];
  onSelectionChange: (ids: string[]) => void;
  onCountChange: (count: number) => void;
};

const PeopleListTab = ({
  tenantId,
  workspaceId,
  searchQuery,
  selectedIds,
  onSelectionChange,
  onCountChange,
}: PeopleListTabProps) => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const page = searchParams.get('page') || '0';

  const [sortConfig, setSortConfig] = useState<SortConfig | null>(null);

  const { mutate: deleteList } = useDeleteLeadList(LeadType.LEAD_CONTACT);
  const { data, isLoading } = useGetLeadLists({
    params: { tenantId, workspaceId },
    type: LeadType.LEAD_CONTACT,
    page: {
      ...defaultPaginationParams,
      page: Number(page),
      sort: sortConfig ? `${sortConfig.key},${sortConfig.direction}` : 'updatedAt,desc',
      query: searchQuery,
    },
  });

  useEffect(() => {
    if (data?.page?.totalElements !== undefined) {
      onCountChange(data.page.totalElements);
    }
  }, [data?.page?.totalElements, onCountChange]);

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('page', newPage.toString());
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const handleSortChange = useCallback(
    (sort: { key: string; direction: 'asc' | 'desc' } | null) => {
      handlePageChange(0);
      setSortConfig(sort);
    },
    [handlePageChange]
  );

  const handleDelete = useCallback(
    (list: LeadList) => {
      deleteList(
        { tenantId, workspaceId, listId: list.id },
        {
          onSuccess: () => toast.success(`"${list.name}" deleted`),
          onError: () => toast.error('Failed to delete list'),
        }
      );
    },
    [deleteList, tenantId, workspaceId]
  );

  const handleView = useCallback(
    (id: string) => {
      router.push(appRoutes.leadgen.leadList.contact(id));
    },
    [router]
  );

  const columns = useLeadListColumns(handleDelete, handleView);

  return (
    <div className="flex-1 overflow-auto p-6">
      <DataTable
        columns={columns}
        data={data?.content ?? []}
        pagination={data?.page}
        isLoading={isLoading}
        selectable
        selectedRowIds={selectedIds}
        onSelectionChange={onSelectionChange}
        defaultMessage="No people lists found. Create a list to get started."
        onPageChange={handlePageChange}
        sortConfig={sortConfig}
        onSortChange={handleSortChange}
        onRowClick={(rowId) => handleView(rowId)}
      />
    </div>
  );
};

export { PeopleListTab };
