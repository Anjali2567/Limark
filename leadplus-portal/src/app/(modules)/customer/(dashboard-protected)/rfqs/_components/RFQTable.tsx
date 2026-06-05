import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import { Badge } from '@/components/ui/badge';
import { BUDGETS } from '@/constants/request-for-quote.constants';
import { useGetAllRequestForQuotes } from '@/hooks/useRequestForQuote';
import { convertDate } from '@/lib/utils/timeConversion';
import { defaultPaginationParams } from '@/types/paginated-params';
import { RequestForQuoteWithQuoteCount } from '@/types/request-for-quote.types';
import { ColumnType } from '@/types/table.types';
import { RFQTableActions } from './RFQTableActions';
import { RFQViewDrawer } from './RFQViewDrawer';

const RFQTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [sortConfig, setSortConfig] = useState<SortConfig | null>({
    key: 'updatedAt',
    direction: 'desc',
  });

  const page = searchParams.get('page') || '0';
  const rfqId = searchParams.get('rfqId');

  const sortParam = useMemo(() => {
    if (!sortConfig) return defaultPaginationParams.sort;
    return `${sortConfig.key},${sortConfig.direction}`;
  }, [sortConfig]);

  const { data, isLoading } = useGetAllRequestForQuotes({
    ...defaultPaginationParams,
    page: Number(page),
    query: searchQuery,
    sort: sortParam,
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

  const handleSortChange = useCallback((sort: SortConfig | null) => {
    setSortConfig(sort);
  }, []);

  const handleViewRFQ = useCallback(
    (rfq: RequestForQuoteWithQuoteCount) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('rfqId', rfq.id);
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const handleCloseDrawer = useCallback(() => {
    const params = new URLSearchParams(searchParams.toString());
    params.delete('rfqId');
    router.push(`?${params.toString()}`);
  }, [router, searchParams]);

  const handleRowClick = useCallback(
    (rowId: string) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('rfqId', rowId);
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const columns = useMemo<ColumnType<RequestForQuoteWithQuoteCount>[]>(
    () => [
      {
        id: 'createdAt',
        header: 'RFQ ID',
        width: '15%',
        sortable: true,
        renderCell: (row) => (
          <Cell value={`RFQ-${row.id.slice(row.id.length - 5, row.id.length).toUpperCase()}`} />
        ),
      },
      {
        id: 'title',
        header: 'Title',
        width: '25%',
        renderCell: (row) => <Cell value={row.title} />,
        sortable: true,
      },
      {
        id: 'budget',
        header: 'Budget',
        width: '15%',
        renderCell: (row) => {
          const budget = BUDGETS.find((b) => b.value === row.budget);
          return <Cell value={budget?.label || row.budget} />;
        },
      },
      {
        id: 'deadline',
        header: 'Deadline',
        width: '15%',
        renderCell: (row) => (
          <Cell
            value={row.deadline ? convertDate(new Date(row.deadline)) : 'N/A'}
            className="text-sm"
          />
        ),
        sortable: true,
      },
      {
        id: 'vendorCount',
        header: 'Vendors',
        width: '10%',
        renderCell: (row) => (
          <Badge variant="secondary" className="border-border">
            {row.numberOfVendorsForRfq || 0}
          </Badge>
        ),
      },
      {
        id: 'quoteCount',
        header: 'Quotes',
        width: '10%',
        renderCell: (row) => (
          <Badge variant="outline" className="border-green-200 bg-green-50 text-green-700">
            {row.numberOfQuotationsForRfq || 0}
          </Badge>
        ),
      },
      {
        id: 'actions',
        header: '',
        width: '10%',
        renderCell: (row) => <RFQTableActions data={row} onView={handleViewRFQ} />,
        align: 'right',
      },
    ],
    [handleViewRFQ]
  );

  return (
    <>
      <SearchBar
        className="max-w-sm"
        placeholder="Search RFQs..."
        value={searchQuery}
        onChange={handleSearch}
      />
      <DataTable
        columns={columns}
        data={data?.content || []}
        pagination={data?.page}
        isLoading={isLoading}
        defaultMessage="No RFQs Found"
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        onRowClick={(rowId) => handleRowClick(rowId)}
        sortConfig={sortConfig}
        onSortChange={handleSortChange}
      />
      <RFQViewDrawer open={!!rfqId} onOpenChange={handleCloseDrawer} />
    </>
  );
};

export { RFQTable };
