import { useRouter, useSearchParams } from 'next/navigation';
import { JSX, useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { StatusBadge } from '@/components/StatusBadge';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import {
  VendorVerificationStatus,
  vendorVerificationStatusOptions,
} from '@/constants/vendor.constant';
import { useGetAllVendors } from '@/hooks/useVendor';
import { useGetIndustryList } from '@/hooks/useIndustry';
import { convertDateISO } from '@/lib/utils/timeConversion';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType, FilterOption } from '@/types/table.types';
import { Vendor } from '@/types/vendor.types';
import { VendorTableActions } from './VendorTableActions';
import { ListItemCell } from '@/components/tables/ListItemCell';

import { CheckCircle2, Clock, XCircle } from 'lucide-react';

type Status = {
  ui: 'active' | 'pending' | 'inactive';
  label: string;
  status: string;
  icon: JSX.Element;
};

const STATUS_CONFIG: Record<VendorVerificationStatus, Status> = {
  [VendorVerificationStatus.APPROVED]: {
    ui: 'active',
    label: 'Active',
    status: 'ACTIVE',
    icon: <CheckCircle2 className="h-3 w-3" />,
  },
  [VendorVerificationStatus.PENDING]: {
    ui: 'pending',
    label: 'Pending',
    status: 'PENDING',
    icon: <Clock className="h-3 w-3" />,
  },
  [VendorVerificationStatus.REJECTED]: {
    ui: 'inactive',
    label: 'Rejected',
    status: 'REJECTED',
    icon: <XCircle className="h-3 w-3" />,
  },
  [VendorVerificationStatus.INCOMPLETE]: {
    ui: 'inactive',
    label: 'Incomplete',
    status: 'INCOMPLETE',
    icon: <XCircle className="h-3 w-3" />,
  },
};

const VendorTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [filtersMap, setFiltersMap] = useState<Record<string, FilterOption | null>>({});
  const [sortConfig, setSortConfig] = useState<SortConfig | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>('');

  const page = searchParams.get('page') || '0';

  const industryList = useGetIndustryList();

  const { data, isLoading } = useGetAllVendors({
    ...defaultPaginationParams,
    page: Number(page),
    query: searchQuery,
    sort: sortConfig ? `${sortConfig.key},${sortConfig.direction}` : 'updatedAt,desc',
    ...(filtersMap?.status?.value != null
      ? {
          vendorVerificationStatusList: [filtersMap.status!.value as VendorVerificationStatus],
        }
      : {}),
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

  const handleFiltersChange = useCallback(
    (key: string, filter: FilterOption | null) => {
      setFiltersMap((prev) => ({ ...prev, [key]: filter }));
      handlePageChange(0);
    },
    [setFiltersMap, handlePageChange]
  );

  const handleSortChange = useCallback(
    (sort: { key: string; direction: 'asc' | 'desc' } | null) => {
      handlePageChange(0);
      setSortConfig(sort);
    },
    [handlePageChange]
  );

  const handleRowClick = useCallback(
    (rowId: string) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('id', rowId);
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const columns = useMemo<ColumnType<Vendor>[]>(
    () => [
      {
        id: 'name',
        header: 'Vendor Name',
        width: '25%',
        renderCell: (row) => <Cell value={row.companyName} />,
        sortable: true,
      },
      {
        id: 'createdAt',
        header: 'Joined Date',
        width: '15%',
        renderCell: (row) => (
          <Cell value={row.createdAt ? convertDateISO(new Date(row.createdAt)) : ''} />
        ),
        sortable: true,
      },
      {
        id: 'industries',
        header: 'Industries',
        width: '35%',
        renderCell: (row) => {
          const labels = (row.industryIds || [])
            .map((id) => industryList.find((i) => (i.value as unknown as number) === id)?.label)
            .filter(Boolean) as string[];
          return <ListItemCell items={labels} />;
        },
      },
      {
        id: 'status',
        header: 'Status',
        width: '15%',
        filterable: true,
        filterOptions: [{ label: 'All', value: null }, ...vendorVerificationStatusOptions],
        renderCell: (row) => {
          const { label, icon, status } = STATUS_CONFIG[row.vendorVerificationStatus];
          return <StatusBadge value={label} icon={icon} status={status} />;
        },
      },
      {
        id: 'actions',
        header: '',
        width: '10%',
        renderCell: (row) => <VendorTableActions vendor={row} />,
        align: 'right',
      },
    ],
    [industryList]
  );

  return (
    <>
      <SearchBar
        className="max-w-sm"
        placeholder="Search Vendors..."
        value={searchQuery}
        onChange={handleSearch}
      />
      <DataTable
        rowClassName="h-12"
        columns={columns}
        data={data?.content || []}
        pagination={data?.page}
        isLoading={isLoading}
        defaultMessage="No Vendors Found"
        sortConfig={sortConfig}
        onSortChange={handleSortChange}
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        filtersMap={filtersMap}
        onFiltersChange={handleFiltersChange}
        onRowClick={handleRowClick}
      />
    </>
  );
};

export { VendorTable };
