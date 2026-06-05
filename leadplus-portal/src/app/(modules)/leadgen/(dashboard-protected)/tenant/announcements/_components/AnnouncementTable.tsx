'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { Badge } from '@/components/ui/badge';
import { useAuth } from '@/context/AuthContext';
import { useGetAllTenantAnnouncements } from '@/hooks/useTenant';
import { timeAgo } from '@/lib/utils/timeConversion';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { TenantAnnouncementStatus } from '@/constants/announcement.constants';
import { TenantAnnouncement } from '@/types/tenant.types';
import { AnnouncementTableActions } from './AnnouncementTableActions';

const statusConfig: Record<TenantAnnouncementStatus, { label: string; className: string }> = {
  [TenantAnnouncementStatus.COMPLETED]: {
    label: 'Completed',
    className: 'bg-green-50 border-green-200 text-green-700',
  },
  [TenantAnnouncementStatus.IN_PROGRESS]: {
    label: 'In Progress',
    className: 'bg-blue-50 border-blue-200 text-blue-700',
  },
  [TenantAnnouncementStatus.DRAFT]: {
    label: 'Draft',
    className: 'bg-gray-50 border-gray-200 text-gray-700',
  },
  [TenantAnnouncementStatus.FAILED]: {
    label: 'Failed',
    className: 'bg-red-50 border-red-200 text-red-700',
  },
};

const AnnouncementTable = () => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const searchParams = useSearchParams();
  const page = searchParams.get('page') || '0';

  const [searchQuery, setSearchQuery] = useState<string>('');

  const { data, isLoading } = useGetAllTenantAnnouncements({
    tenantId,
    params: {
      ...defaultPaginationParams,
      page,
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

  const columns = useMemo<ColumnType<TenantAnnouncement>[]>(
    () => [
      {
        id: 'name',
        header: 'Name',
        width: '18%',
        renderCell: (row) => <Cell value={row.name} />,
      },
      {
        id: 'subject',
        header: 'Subject',
        width: '25%',
        renderCell: (row) => <Cell value={row.subject} />,
      },
      {
        id: 'recipientCount',
        header: 'Recipients',
        width: '10%',
        renderCell: (row) => <Cell value={row.recipientCount.toLocaleString()} />,
      },
      {
        id: 'status',
        header: 'Status',
        width: '13%',
        renderCell: (row) => {
          const config = statusConfig[row.status];
          return (
            <Badge variant="outline" className={config.className}>
              {config.label}
            </Badge>
          );
        },
      },
      {
        id: 'launchedAt',
        header: 'Launched At',
        width: '15%',
        renderCell: (row) => (
          <Cell value={row.launchedAt ? timeAgo(new Date(row.launchedAt)) : ''} />
        ),
      },
      {
        id: 'createdAt',
        header: 'Created',
        width: '14%',
        renderCell: (row) => <Cell value={timeAgo(new Date(row.createdAt))} />,
      },
      {
        id: 'actions',
        header: '',
        width: '5%',
        align: 'center',
        renderCell: (row) => {
          if (row.status !== TenantAnnouncementStatus.DRAFT) return null;
          return <AnnouncementTableActions announcement={row} />;
        },
      },
    ],
    []
  );

  return (
    <>
      <SearchBar
        className="max-w-sm"
        placeholder="Search announcements..."
        value={searchQuery}
        onChange={handleSearch}
      />
      <DataTable
        rowClassName="h-12"
        columns={columns}
        data={data?.content || []}
        isLoading={isLoading}
        defaultMessage="No announcements found"
        pagination={data?.page}
        initialPage={Number(page)}
        onPageChange={handlePageChange}
      />
    </>
  );
};

export { AnnouncementTable };
