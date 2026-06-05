import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import { useAuth } from '@/context/AuthContext';
import { useGetAllTenantWorkspaces } from '@/hooks/useTenant';
import { convertDate } from '@/lib/utils/timeConversion';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { TenantWorkspaceResponse } from '@/types/tenant.types';

const WorkspaceTable = () => {
  const router = useRouter();

  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const searchParams = useSearchParams();
  const page = searchParams.get('page') || '0';

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [sortConfig, setSortConfig] = useState<SortConfig | null>(null);

  const { data, isLoading } = useGetAllTenantWorkspaces({
    tenantId,
    params: {
      ...defaultPaginationParams,
      page,
      sort: sortConfig ? `${sortConfig.key},${sortConfig.direction}` : 'updatedAt,desc',
      query: searchQuery,
    },
  });

  const handleRowClick = useCallback(
    (rowId: string) => {
      router.push(`?workspaceId=${rowId}`);
    },
    [router]
  );

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('page', newPage.toString());
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const handleSearch = useCallback(
    (query: string) => {
      handlePageChange(0);
      setSearchQuery(query);
    },
    [handlePageChange]
  );

  const handleSortChange = useCallback(
    (sort: { key: string; direction: 'asc' | 'desc' } | null) => {
      handlePageChange(0);
      setSortConfig(sort);
    },
    [handlePageChange]
  );

  const columns = useMemo<ColumnType<TenantWorkspaceResponse>[]>(
    () => [
      {
        id: 'name',
        header: 'Workspace Name',
        width: '40%',
        renderCell: (row) => <Cell value={row.workspaceName} truncate />,
        sortable: true,
      },
      {
        id: 'owner',
        header: 'Owner',
        width: '20%',
        renderCell: (row) => <Cell value={row.ownerName} />,
      },
      {
        id: 'members',
        header: 'Members',
        width: '20%',
        renderCell: (row) => <Cell value={row.totalMembers} />,
      },
      {
        id: 'createdAt',
        header: 'Created',
        width: '20%',
        renderCell: (row) => <Cell value={convertDate(new Date(row.createdAt))} />,
      },
    ],
    []
  );

  return (
    <>
      <SearchBar
        className="max-w-sm"
        placeholder="Search workspaces..."
        value={searchQuery}
        onChange={handleSearch}
      />
      <DataTable
        rowClassName="h-12"
        columns={columns}
        data={data?.content || []}
        isLoading={isLoading}
        defaultMessage="No Workspaces Found"
        pagination={data?.page}
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        sortConfig={sortConfig}
        onSortChange={handleSortChange}
        onRowClick={handleRowClick}
      />
    </>
  );
};

export { WorkspaceTable };
