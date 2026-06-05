'use client';

import { useMemo } from 'react';

import { UserStatus } from '@/constants/user.constants';
import { useGetAllUsers } from '@/hooks/useUser';
import { singleItemPaginationParams } from '@/types/paginated-params';
import { UserTable } from './_components/UserTable';

import { Clock } from 'lucide-react';

const UsersPage = () => {
  const { data } = useGetAllUsers({
    ...singleItemPaginationParams,
    userStatuses: [UserStatus.PENDING],
  });

  const pendingCount = useMemo(() => {
    return data?.page?.totalElements || 0;
  }, [data?.page?.totalElements]);

  return (
    <div className="animate-in fade-in slide-in-from-right-4 space-y-6 p-8 duration-500">
      <div className="flex flex-col items-start">
        <h1 className="text-foreground text-3xl font-bold">Users</h1>
        <p className="text-muted-foreground">
          Manage user accounts and approval requests
          {pendingCount > 0 && (
            <span className="bg-warning/10 text-warning border-warning/20 ml-2 inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-xs font-semibold">
              <Clock className="h-3 w-3" />
              {pendingCount} pending {pendingCount === 1 ? 'request' : 'requests'}
            </span>
          )}
        </p>
      </div>
      <UserTable />
    </div>
  );
};

export default UsersPage;
