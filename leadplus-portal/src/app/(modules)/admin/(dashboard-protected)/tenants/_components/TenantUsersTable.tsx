'use client';

import { useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import { useGetAdminTenantUsers } from '@/hooks/useAdmin';
import { formatDate } from '@/lib/utils/formatter';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { TenantUserResponse } from '@/types/tenant.types';

const TenantUsersTable = ({ tenantId }: { tenantId: string }) => {
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState<SortConfig | null>(null);

  const { data, isLoading } = useGetAdminTenantUsers(tenantId, {
    ...defaultPaginationParams,
    sort: sortConfig ? `${sortConfig.key},${sortConfig.direction}` : 'updatedAt,desc',
    page,
    size: 5,
    query: searchQuery,
  });

  const handlePageChange = useCallback((newPage: number) => {
    setPage(newPage);
  }, []);

  const handleSearch = useCallback((query: string) => {
    setPage(0);
    setSearchQuery(query);
  }, []);

  const handleSortChange = useCallback((sort: SortConfig | null) => {
    setPage(0);
    setSortConfig(sort);
  }, []);

  const columns = useMemo<ColumnType<TenantUserResponse>[]>(
    () => [
      {
        id: 'name',
        header: 'Name',
        width: '30%',
        renderCell: (row) => <Cell value={row.name} className="font-medium" truncate />,
        sortable: true,
      },
      {
        id: 'email',
        header: 'Email',
        width: '35%',
        renderCell: (row) => <Cell value={row.email} />,
      },
      {
        id: 'workspacesCount',
        header: 'Workspaces',
        width: '15%',
        renderCell: (row) => <Cell value={row.workspacesCount} />,
      },
      {
        id: 'createdAt',
        header: 'Joined',
        width: '20%',
        renderCell: (row) => <Cell value={formatDate(new Date(row.createdAt))} />,
      },
    ],
    []
  );

  return (
    <div className="space-y-4 pb-4">
      <div className="flex items-center justify-between">
        <h2 className="text-foreground text-xl font-semibold">Users</h2>
        <SearchBar
          className="max-w-sm"
          placeholder="Search users..."
          value={searchQuery}
          onChange={handleSearch}
        />
      </div>
      <DataTable
        rowClassName="h-12"
        columns={columns}
        data={data?.content || []}
        isLoading={isLoading}
        defaultMessage="No users found"
        pagination={data?.page}
        initialPage={page}
        onPageChange={handlePageChange}
        sortConfig={sortConfig}
        onSortChange={handleSortChange}
      />
    </div>
  );
};

export { TenantUsersTable };
