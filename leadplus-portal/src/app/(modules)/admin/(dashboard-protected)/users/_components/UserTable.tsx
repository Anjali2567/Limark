import { useRouter, useSearchParams } from 'next/navigation';
import { JSX, useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { StatusBadge } from '@/components/StatusBadge';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import { UserStatus, userStatusList } from '@/constants/user.constants';
import { useGetAllUsers } from '@/hooks/useUser';
import { formatString } from '@/lib/utils/formatter';
import { convertDateISO } from '@/lib/utils/timeConversion';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType, FilterOption } from '@/types/table.types';
import { User, UserListingParams } from '@/types/user.types';
import { UserTableActions } from './UserTableActions';

import { CheckCircle2, Clock, XCircle } from 'lucide-react';

type Status = {
  ui: 'active' | 'pending' | 'inactive';
  label: string;
  status: string;
  icon: JSX.Element;
};

const STATUS_CONFIG: Record<UserStatus, Status> = {
  [UserStatus.APPROVED]: {
    ui: 'active',
    label: 'Active',
    status: 'ACTIVE',
    icon: <CheckCircle2 className="h-3 w-3" />,
  },
  [UserStatus.PENDING]: {
    ui: 'pending',
    label: 'Pending',
    status: 'PENDING',
    icon: <Clock className="h-3 w-3" />,
  },
  [UserStatus.REJECTED]: {
    ui: 'inactive',
    label: 'Inactive',
    status: 'REJECTED',
    icon: <XCircle className="h-3 w-3" />,
  },
};

const UserTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const page = searchParams.get('page') || '0';

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [sortConfig, setSortConfig] = useState<SortConfig | null>({
    key: 'createdAt',
    direction: 'desc',
  });
  const [filtersMap, setFiltersMap] = useState<Record<string, FilterOption | null>>({});

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('page', newPage.toString());
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const handleSortChange = useCallback(
    (newSortConfig: SortConfig | null) => {
      setSortConfig(newSortConfig);
      handlePageChange(0);
    },
    [setSortConfig, handlePageChange]
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

  const queryParams = useMemo(() => {
    const params: UserListingParams = {
      ...defaultPaginationParams,
      page,
      query: searchQuery,
    };

    if (sortConfig) {
      params.sort = `${sortConfig.key},${sortConfig.direction}`;
    }

    if (filtersMap.status?.value) {
      params.userStatuses = [filtersMap.status.value as UserStatus];
    }

    return params;
  }, [page, sortConfig, filtersMap, searchQuery]);

  const { data, isLoading } = useGetAllUsers(queryParams);
  const columns: ColumnType<User>[] = useMemo(
    () => [
      {
        id: 'name',
        header: 'Name',
        width: '15%',
        sortable: true,
        renderCell: (row) => <Cell value={row.name} />,
      },
      {
        id: 'email',
        header: 'Email',
        width: '25%',
        sortable: true,
        renderCell: (row) => <Cell value={row.email} />,
      },
      {
        id: 'status',
        header: 'Status',
        width: '15%',
        filterable: true,
        filterOptions: [{ label: 'All', value: null }, ...userStatusList],
        renderCell: (row) => {
          const { label, icon, status } = STATUS_CONFIG[row.status];
          return <StatusBadge value={label} icon={icon} status={status} />;
        },
      },
      {
        id: 'role',
        header: 'Role',
        width: '20%',
        renderCell: (row) => {
          const roles = row?.roles?.map((role) => formatString(role)) || [];
          return <Cell value={roles.join(', ')} />;
        },
      },
      {
        id: 'createdAt',
        header: 'Joined Date',
        width: '10%',
        sortable: true,
        renderCell: (row) => (
          <Cell value={row.createdAt ? convertDateISO(new Date(row.createdAt)) : ''} />
        ),
      },
      {
        id: 'actions',
        header: '',
        align: 'right',
        width: '15%',
        renderCell: (row) => <UserTableActions user={row} />,
      },
    ],
    []
  );
  return (
    <div className="space-y-6">
      <SearchBar
        className="max-w-md"
        placeholder="Search users..."
        value={searchQuery}
        onChange={handleSearch}
      />
      <DataTable
        rowClassName="h-10"
        columns={columns}
        data={data?.content || []}
        pagination={data?.page}
        isLoading={isLoading}
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        sortConfig={sortConfig}
        onSortChange={handleSortChange}
        filtersMap={filtersMap}
        onFiltersChange={handleFiltersChange}
      />
    </div>
  );
};

export { UserTable };
