import { Building } from 'lucide-react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';
import { toast } from 'sonner';

import { SearchBar } from '@/components/SearchBar';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useGetAllVendorRFQQuotations } from '@/hooks/useQuotation';
import { convertDate } from '@/lib/utils/timeConversion';
import { defaultPaginationParams } from '@/types/paginated-params';
import { CustomerQuotationResponse } from '@/types/quotation.types';
import { ColumnType } from '@/types/table.types';
import { RFQTableActions } from './RFQTableActions';
import { BUDGETS } from '@/constants/request-for-quote.constants';

const RFQTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const { authenticatedUserDetails } = useAuth();

  const page = searchParams.get('page') || '0';

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [sortConfig, setSortConfig] = useState<SortConfig | null>({
    key: 'receivedDate',
    direction: 'desc',
  });

  const { data, isLoading } = useGetAllVendorRFQQuotations({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    params: {
      ...defaultPaginationParams,
      page: Number(page),
      query: searchQuery,
    },
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

  const handleRowClick = useCallback(
    (rowId: string) => {
      router.push(appRoutes.vendor.rfq.summary(rowId));
    },
    [router]
  );

  const handleAcceptRFQ = useCallback((id: string) => {
    toast.success(`RFQ "${id}" has been accepted`);
  }, []);

  const handleRejectRFQ = useCallback((id: string) => {
    toast.error(`RFQ "${id}" has been rejected`);
  }, []);

  const columns = useMemo<ColumnType<CustomerQuotationResponse>[]>(
    () => [
      {
        id: 'id',
        header: 'RFQ ID',
        width: '12%',
        sortable: true,
        renderCell: (row) => (
          <Cell value={`RFQ-${row.id.slice(row.id.length - 5, row.id.length)}`} />
        ),
      },
      {
        id: 'projectTitle',
        header: 'Project Title',
        width: '22%',
        renderCell: (row) => <Cell value={row.title} truncate />,
        sortable: true,
      },
      {
        id: 'customerName',
        header: 'Customer',
        width: '15%',
        renderCell: (row) => <Cell value={row.customerName} />,
      },
      {
        id: 'companyName',
        header: 'Company',
        width: '15%',
        renderCell: (row) => (
          <div className="flex items-center gap-2">
            <Building className="text-muted-foreground h-4 w-4" />
            <Cell value={row.customerCompanyName} truncate />
          </div>
        ),
      },
      {
        id: 'budget',
        header: 'Budget',
        width: '13%',
        renderCell: (row) => (
          <Cell value={BUDGETS.find((b) => b.value === row.budget)?.label ?? 'N/A'} />
        ),
      },
      {
        id: 'deadline',
        header: 'Deadline',
        width: '13%',
        renderCell: (row) => {
          return <Cell value={row.deadline ? convertDate(new Date(row.deadline)) : 'N/A'} />;
        },
        sortable: true,
      },
      {
        id: 'actions',
        header: '',
        width: '10%',
        renderCell: (row) => (
          <RFQTableActions
            data={row}
            onView={() => handleRowClick(row.id)}
            onAccept={handleAcceptRFQ}
            onReject={handleRejectRFQ}
          />
        ),
        align: 'right',
      },
    ],
    [handleRowClick, handleAcceptRFQ, handleRejectRFQ]
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
        isLoading={isLoading}
        columns={columns}
        data={data?.content || []}
        pagination={data?.page}
        defaultMessage="No RFQs Found"
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        onRowClick={(rowId) => handleRowClick(rowId)}
        sortConfig={sortConfig}
        onSortChange={handleSortChange}
      />
    </>
  );
};

export { RFQTable };
