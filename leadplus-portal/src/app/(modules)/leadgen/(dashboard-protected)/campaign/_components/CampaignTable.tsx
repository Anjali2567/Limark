'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo } from 'react';

import { ProgressBar } from '@/components/ProgressBar';
import { StatusBadge } from '@/components/StatusBadge';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useGetAllCampaigns } from '@/hooks/useCampaign';
import { CampaignListResponse } from '@/types/campaign.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { CampaignTableActions } from './CampaignTableActions';

const CampaignTable = () => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();

  const workspaceId = authenticatedUserDetails?.workspaceId || '';
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const searchParams = useSearchParams();
  const page = searchParams.get('page') || '0';

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('page', newPage.toString());
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const { data: campaigns, isLoading } = useGetAllCampaigns({
    tenantId,
    workspaceId,
    params: {
      ...defaultPaginationParams,
      page: Number(page),
    },
  });

  const handleRowClick = (rowId: string) => {
    router.push(appRoutes.leadgen.campaigns.view(rowId));
  };

  const columns = useMemo<ColumnType<CampaignListResponse>[]>(
    () => [
      {
        id: 'name',
        header: 'Campaign Name',
        width: '220px',
        renderCell: (row) => <Cell value={row.name ? row.name : row.id} />,
      },
      {
        id: 'industry',
        header: 'Industry',
        width: '160px',
        renderCell: (row) => <Cell value={row.industry} />,
      },
      {
        id: 'totalContacts',
        header: 'Total Contacts',
        width: '160px',
        renderCell: (row) => <Cell value={row.totalContacts} />,
      },
      {
        id: 'sentEmails',
        header: 'Sent',
        width: '100px',
        renderCell: (row) => <Cell value={row.sentEmails} />,
      },
      {
        id: 'openedEmails',
        header: 'Opened',
        width: '140px',
        renderCell: (row) => <Cell value={row.openedEmails} />,
      },
      {
        id: 'progress',
        header: 'Progress',
        width: '150px',
        renderCell: (row) => <ProgressBar progress={row.progress} />,
      },
      {
        id: 'status',
        header: 'Status',
        width: '130px',
        renderCell: (row) => <StatusBadge status={row.status} />,
      },
      {
        id: 'actions',
        header: 'Actions',
        width: '80px',
        renderCell: (row) => <CampaignTableActions campaign={row} />,
        align: 'right',
      },
    ],
    []
  );

  const defaultColumns = useMemo<ColumnType<CampaignListResponse>[]>(() => {
    return columns.slice(0, 5).map((column) => ({
      ...column,
      width: '20%',
    }));
  }, [columns]);

  return (
    <>
      <div>
        <h2 className="text-foreground mb-4 text-xl font-semibold">Pipeline</h2>
      </div>
      {isLoading ? (
        <DataTable
          rowClassName="hover:bg-sky-50"
          data={[]}
          columns={defaultColumns}
          isLoading={isLoading}
          defaultMessage="No Campaigns Found"
          onPageChange={handlePageChange}
          initialPage={Number(page)}
        />
      ) : (
        <DataTable
          rowClassName="hover:bg-sky-50"
          columns={columns}
          data={campaigns?.content || []}
          isLoading={isLoading}
          defaultMessage="No Campaigns Found"
          pagination={campaigns?.page}
          initialPage={Number(page)}
          onPageChange={handlePageChange}
          onRowClick={handleRowClick}
        />
      )}
    </>
  );
};

export { CampaignTable };
