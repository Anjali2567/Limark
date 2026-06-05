import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import { Badge } from '@/components/ui/badge';
import { useGetAdminAllIndustries } from '@/hooks/useIndustry';
import { Industry } from '@/types/industry.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { IndustryTableActions } from './IndustryTableActions';

import { CheckCircle, XCircle } from 'lucide-react';

const IndustryTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [sortConfig, setSortConfig] = useState<SortConfig | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>('');

  const page = searchParams.get('page') || '0';

  const { data, isLoading } = useGetAdminAllIndustries({
    ...defaultPaginationParams,
    query: searchQuery,
    sort: sortConfig ? `${sortConfig.key},${sortConfig.direction}` : 'updatedAt,desc',
    page: Number(page),
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

  const handleSortChange = useCallback(
    (sort: { key: string; direction: 'asc' | 'desc' } | null) => {
      handlePageChange(0);
      setSortConfig(sort);
    },
    [handlePageChange]
  );

  const columns = useMemo<ColumnType<Industry>[]>(
    () => [
      {
        id: 'name',
        header: 'Industry Name',
        width: '20%',
        renderCell: (row) => <Cell value={row.name ? row.name : row.id} truncate />,
        sortable: true,
      },
      {
        id: 'description',
        header: 'Description',
        width: '35%',
        renderCell: (row) => <Cell value={row.description} />,
      },
      {
        id: 'vendors',
        header: 'Vendors',
        width: '10%',
        renderCell: () => <Cell />,
      },
      {
        id: 'leads',
        header: 'Leads',
        width: '10%',
        renderCell: () => <Cell />,
      },
      {
        id: 'growthRate',
        header: 'Growth Rate',
        width: '10%',
        renderCell: () => <Cell />,
      },
      {
        id: 'status',
        header: 'Status',
        width: '10%',
        renderCell: (row) => {
          return (
            <>
              {!row.disabled ? (
                <Badge className="border-green-200 bg-green-100 text-green-800 hover:bg-green-100">
                  <CheckCircle className="mr-1 h-3 w-3" />
                  Active
                </Badge>
              ) : (
                <Badge
                  variant="secondary"
                  className="border-gray-200 bg-gray-100 text-gray-600 hover:bg-gray-100"
                >
                  <XCircle className="mr-1 h-3 w-3" />
                  Inactive
                </Badge>
              )}
            </>
          );
        },
      },
      {
        id: 'actions',
        header: '',
        width: '5%',
        renderCell: (row) => <IndustryTableActions data={row} />,
        align: 'right',
      },
    ],
    []
  );

  return (
    <>
      <SearchBar
        className="max-w-sm"
        placeholder="Search Industries..."
        value={searchQuery}
        onChange={handleSearch}
      />
      <DataTable
        columns={columns}
        data={data?.content || []}
        pagination={data?.page}
        isLoading={isLoading}
        defaultMessage="No Industries Found"
        sortConfig={sortConfig}
        onSortChange={handleSortChange}
        initialPage={Number(page)}
        onPageChange={handlePageChange}
      />
    </>
  );
};

export { IndustryTable };
