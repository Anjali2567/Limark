'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { Badge } from '@/components/ui/badge';
import { useGetAdminTenants } from '@/hooks/useAdmin';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { TenantListing } from '@/types/tenant.types';
import { formatDate, formatString } from '@/lib/utils/formatter';
import { MODULE_COLORS } from '@/constants/modules.constants';

const TenantsTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const page = searchParams.get('page') || '0';

  const [searchQuery, setSearchQuery] = useState<string>('');

  const { data, isLoading } = useGetAdminTenants({
    ...defaultPaginationParams,
    page,
    query: searchQuery,
  });

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

  const handleRowClick = useCallback(
    (tenantId: string) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('id', tenantId);
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const columns: ColumnType<TenantListing>[] = useMemo(
    () => [
      {
        id: 'name',
        header: 'Name',
        width: '20%',
        renderCell: (row) => <Cell value={row.name} className="font-medium" />,
      },
      {
        id: 'domain',
        header: 'Domain',
        width: '20%',
        renderCell: (row) => <Cell value={row.domain} />,
      },
      {
        id: 'modules',
        header: 'Modules',
        width: '30%',
        renderCell: (row) =>
          row.modules?.length > 0 ? (
            <div className="flex flex-wrap gap-1">
              {row.modules.map((module) => (
                <Badge key={module} className={`${MODULE_COLORS[module]} text-xs`}>
                  {formatString(module)}
                </Badge>
              ))}
            </div>
          ) : (
            <Cell />
          ),
      },
      {
        id: 'ownerName',
        header: 'Owner',
        width: '15%',
        renderCell: (row) => <Cell value={row.ownerName ?? undefined} />,
      },
      {
        id: 'createdAt',
        header: 'Created At',
        width: '15%',
        renderCell: (row) => <Cell value={formatDate(new Date(row.createdAt))} />,
      },
    ],
    []
  );

  return (
    <div className="space-y-6 pb-8">
      <SearchBar
        className="max-w-md"
        placeholder="Search by name or domain..."
        value={searchQuery}
        onChange={handleSearch}
      />
      <DataTable
        rowClassName="h-12"
        columns={columns}
        data={data?.content || []}
        pagination={data?.page}
        isLoading={isLoading}
        defaultMessage="No tenants found"
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        onRowClick={handleRowClick}
      />
    </div>
  );
};

export { TenantsTable };
