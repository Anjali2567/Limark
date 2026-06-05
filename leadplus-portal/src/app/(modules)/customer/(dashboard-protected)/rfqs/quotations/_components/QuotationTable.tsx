'use client';

import Image from 'next/image';
import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import { useGetCustomerQuotationsByRfqId } from '@/hooks/useCustomerQuotations';
import { formatLocation } from '@/lib/utils/formatter';
import { convertDate } from '@/lib/utils/timeConversion';
import { CustomerQuotation } from '@/types/customer-quotation.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { QuotationDrawer } from './QuotationDrawer';
import { QuotationTableActions } from './QuotationTableActions';

import { Building2, Clock, MapPin, Star } from 'lucide-react';
import { getCachedImageUrl } from '@/lib/utils/helpers';

const QuotationTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const rfqId = searchParams.get('rfqId') || '';
  const page = searchParams.get('page') || '0';

  const [selectedDetailQuote, setSelectedDetailQuote] = useState<CustomerQuotation | null>(null);
  const [showDrawer, setShowDrawer] = useState(false);
  const [sortConfig, setSortConfig] = useState<SortConfig | null>(null);

  const sortParam = useMemo(() => {
    if (!sortConfig) return defaultPaginationParams.sort;
    return `${sortConfig.key},${sortConfig.direction}`;
  }, [sortConfig]);

  const { data: quotationData, isLoading } = useGetCustomerQuotationsByRfqId(rfqId, {
    ...defaultPaginationParams,
    page: Number(page),
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

  const handleSortChange = useCallback((sort: SortConfig | null) => {
    setSortConfig(sort);
  }, []);

  const handleRowClick = useCallback(
    (rowId: string) => {
      const quote = quotationData?.content.find((q) => q.id === rowId);
      if (quote) {
        setSelectedDetailQuote(quote);
        setShowDrawer(true);
      }
    },
    [quotationData?.content]
  );

  const columns = useMemo<ColumnType<CustomerQuotation>[]>(
    () => [
      {
        id: 'vendorName',
        header: 'Vendor',
        width: '25%',
        renderCell: (row) => (
          <div className="flex items-center gap-3">
            {row.vendorLogo ? (
              <Image
                src={getCachedImageUrl(row.vendorLogo, row.updatedAt) || ''}
                alt={row.vendorCompanyName ?? 'Vendor'}
                width={40}
                height={40}
                unoptimized
                className="border-border h-10 w-10 shrink-0 rounded border object-cover"
              />
            ) : (
              <div className="bg-muted border-border flex h-10 w-10 shrink-0 items-center justify-center rounded border">
                <Building2 className="text-muted-foreground h-5 w-5" />
              </div>
            )}
            <div className="min-w-0">
              <div className="text-foreground font-medium" style={{ fontSize: '14px' }}>
                {row.vendorCompanyName ?? 'N/A'}
              </div>
              <div
                className="text-muted-foreground flex items-center gap-1"
                style={{ fontSize: '12px' }}
              >
                <MapPin className="h-3 w-3 shrink-0" />
                <span className="truncate">
                  {formatLocation({
                    state: row.vendorAddress?.state,
                    country: row.vendorAddress?.country,
                  }) ?? 'N/A'}
                </span>
              </div>
            </div>
          </div>
        ),
      },
      {
        id: 'totalCost',
        header: 'Total Cost',
        width: '13%',
        sortable: true,
        renderCell: (row) => {
          if (!row.items || row.items.length === 0) return <Cell value={null} />;
          const totalCost = row.items.reduce((sum, i) => sum + i.price, 0);
          return (
            <span className="text-primary font-semibold" style={{ fontSize: '16px' }}>
              ${totalCost.toLocaleString()}
            </span>
          );
        },
      },
      {
        id: 'timeline',
        header: 'Timeline',
        width: '13%',
        sortable: true,
        renderCell: (row) =>
          row.deadline ? (
            <div className="text-foreground flex items-center gap-1" style={{ fontSize: '14px' }}>
              <Clock className="text-muted-foreground h-3.5 w-3.5 shrink-0" />
              {convertDate(new Date(row.deadline)) ?? 'N/A'}
            </div>
          ) : (
            <Cell value={null} />
          ),
      },
      {
        id: 'vendorRating',
        header: 'Rating',
        width: '12%',
        sortable: true,
        renderCell: (row) =>
          row.vendorRating != null ? (
            <div className="flex items-center gap-1">
              <Star className="h-3.5 w-3.5 fill-orange-500 text-orange-500" />
              <span className="text-foreground font-medium" style={{ fontSize: '14px' }}>
                {row.vendorRating}
              </span>
              {row.vendorReviews != null && (
                <span className="text-muted-foreground" style={{ fontSize: '12px' }}>
                  ({row.vendorReviews})
                </span>
              )}
            </div>
          ) : (
            <Cell value={null} />
          ),
      },
      {
        id: 'teamSize',
        header: 'Team Size',
        width: '10%',
        renderCell: (row) => (
          <Cell value={row.vendorCompanySize != null ? `${row.vendorCompanySize} members` : null} />
        ),
      },
      {
        id: 'createdAt',
        header: 'Submitted',
        width: '12%',
        renderCell: (row) => (
          <Cell value={row.createdAt ? new Date(row.createdAt).toLocaleDateString() : null} />
        ),
      },
      {
        id: 'actions',
        header: '',
        width: '15%',
        align: 'right',
        renderCell: (row) => <QuotationTableActions data={row} />,
      },
    ],
    []
  );

  return (
    <>
      <DataTable
        columns={columns}
        data={quotationData?.content || []}
        pagination={quotationData?.page}
        isLoading={isLoading}
        defaultMessage="No quotations received yet."
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        onRowClick={handleRowClick}
        sortConfig={sortConfig}
        onSortChange={handleSortChange}
      />
      <QuotationDrawer open={showDrawer} onOpenChange={setShowDrawer} quote={selectedDetailQuote} />
    </>
  );
};

export { QuotationTable };
