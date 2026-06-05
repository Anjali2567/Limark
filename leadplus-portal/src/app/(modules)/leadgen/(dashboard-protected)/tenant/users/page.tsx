'use client';

import { UserTable } from './_components/UserTable';

const TenantUsersPage = () => {
  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 space-y-6 p-8 duration-500">
      <div className="flex flex-col justify-between">
        <h1 className="text-foreground text-3xl font-bold">Users</h1>
        <p className="text-muted-foreground">Manage your users and team members.</p>
      </div>
      <UserTable />
    </div>
  );
};

export default TenantUsersPage;
