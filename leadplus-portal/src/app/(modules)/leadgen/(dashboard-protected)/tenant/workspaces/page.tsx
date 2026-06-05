'use client';

import { usePathname, useSearchParams, useRouter } from 'next/navigation';
import { useCallback } from 'react';

import { WorkspaceTable } from './_components/WorkspaceTable';
import { WorkspaceView } from '../../workspace/_components/WorkspaceView';

const TenantWorkspacesPage = () => {
  const router = useRouter();
  const pathname = usePathname();

  const searchParams = useSearchParams();

  const workspaceId = searchParams.get('workspaceId');

  const onBack = useCallback(() => {
    router.replace(pathname);
  }, [router, pathname]);

  if (workspaceId) {
    return <WorkspaceView workspaceId={workspaceId} onBack={onBack} />;
  }

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 space-y-6 p-8 duration-500">
      <div className="flex flex-col justify-between">
        <h1 className="text-foreground text-3xl font-bold">Workspaces</h1>
        <p className="text-muted-foreground">Manage your workspaces and team members.</p>
      </div>
      <WorkspaceTable />
    </div>
  );
};

export default TenantWorkspacesPage;
