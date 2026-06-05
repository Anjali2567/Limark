import { useRouter, useSearchParams } from 'next/navigation';
import { JSX, useCallback, useMemo } from 'react';

import { StatusBadge } from '@/components/StatusBadge';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { WorkspaceUserRole, WorkspaceUserStatus } from '@/constants/user.constants';
import { useAuth } from '@/context/AuthContext';
import { useGetAllWorkspaceUsers } from '@/hooks/useWorkspace';
import { convertDateISO } from '@/lib/utils/timeConversion';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { WorkspaceUsersResponse } from '@/types/user.types';
import { WorkspaceUserTableActions } from './WorkspaceUserTableActions';

import { Crown, Mail, Users } from 'lucide-react';

type Status = {
  ui?: 'active' | 'pending' | 'inactive';
  label?: string;
  status?: string;
  icon?: JSX.Element;
};

const STATUS_CONFIG: Partial<Record<WorkspaceUserRole | WorkspaceUserStatus, Status>> = {
  [WorkspaceUserRole.MEMBER]: {
    ui: 'inactive',
    label: 'Member',
  },
  [WorkspaceUserRole.OWNER]: {
    label: 'Owner',
    status: 'INFO',
    icon: <Crown className="h-3 w-3" />,
  },
  [WorkspaceUserRole.WORKSPACE_ADMIN]: {
    label: 'Workspace Admin',
    status: 'INFO',
    icon: <Crown className="h-3 w-3" />,
  },
  [WorkspaceUserStatus.INVITED]: {
    label: 'Invited',
    status: 'PENDING',
    icon: <Mail className="h-3 w-3" />,
  },
};

type WorkspaceUserTableProps = {
  canManageUsers: boolean;
  workspaceId?: string;
};

const WorkspaceUserTable = ({ canManageUsers, workspaceId }: WorkspaceUserTableProps) => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();

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

  const { data, isLoading } = useGetAllWorkspaceUsers(
    {
      tenantId: authenticatedUserDetails?.tenantId || '',
      workspaceId: workspaceId || authenticatedUserDetails?.workspaceId || '',
    },
    {
      ...defaultPaginationParams,
      page: Number(page),
    }
  );

  const isUserInvitePending = useCallback((user: WorkspaceUsersResponse) => {
    return user.workspaceUserStatus && user.workspaceUserStatus === WorkspaceUserStatus.INVITED;
  }, []);

  const columns = useMemo<ColumnType<WorkspaceUsersResponse>[]>(
    () => [
      {
        id: 'name',
        header: 'Name',
        width: '30%',
        renderCell: (row) => <Cell value={row.name} />,
      },
      {
        id: 'email',
        header: 'Email',
        width: '30%',
        renderCell: (row) => <Cell value={row.email} />,
      },
      {
        id: 'role',
        header: 'Role',
        width: '15%',
        renderCell: (row) => {
          const ref = isUserInvitePending(row) ? row.workspaceUserStatus : row.workspaceUserRole;
          const { label, icon, status } = STATUS_CONFIG[ref] || {};
          return <StatusBadge value={label} icon={icon} status={status} className="rounded-lg!" />;
        },
      },
      {
        id: 'joined',
        header: 'Joined',
        width: '15%',
        renderCell: (row) => {
          const date = isUserInvitePending(row) ? 'N/A' : convertDateISO(new Date(row.updatedAt));
          return <Cell value={date} />;
        },
      },
      {
        id: 'actions',
        header: '',
        width: '10%',
        align: 'right',
        renderCell: (row: WorkspaceUsersResponse) => {
          return <WorkspaceUserTableActions user={row} canManageUsers={canManageUsers} />;
        },
      },
    ],
    [isUserInvitePending, canManageUsers]
  );
  return (
    <>
      <div className="mb-4 flex items-center gap-2">
        <Users className="text-muted-foreground h-5 w-5" />
        <h2 className="text-md">Members</h2>
        <span className="text-muted-foreground">{`(${data?.page?.totalElements ?? 0})`}</span>
      </div>
      <DataTable
        rowClassName="h-12"
        defaultMessage="No Users found"
        columns={columns}
        data={data?.content ?? []}
        isLoading={isLoading}
        pagination={data?.page}
        initialPage={Number(page)}
        onPageChange={handlePageChange}
      />
    </>
  );
};

export { WorkspaceUserTable };
