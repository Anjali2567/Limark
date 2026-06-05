import { useMemo } from 'react';

import { Button } from '@/components/ui/button';
import { UserRoles, WorkspaceUserRole } from '@/constants/user.constants';
import { useAuth } from '@/context/AuthContext';
import { useGetWorkspaceDetails } from '@/hooks/useWorkspace';
import { InviteUserModal } from './InviteUserModal';
import { WorkspaceRenameModal } from './WorkspaceRenameModal';
import { WorkspaceUserTable } from './WorkspaceUserTable';

import { ArrowLeft, Loader2 } from 'lucide-react';

type WorkspaceViewProps = {
  workspaceId?: string;
  onBack?: () => void;
};

const WorkspaceView = ({ workspaceId, onBack }: WorkspaceViewProps) => {
  const { authenticatedUserDetails, workspaces = [], loggedInUser } = useAuth();

  const { data: workspaceDetails, isLoading } = useGetWorkspaceDetails({
    workspaceId: workspaceId ?? authenticatedUserDetails?.workspaceId ?? '',
    tenantId: authenticatedUserDetails?.tenantId ?? '',
  });

  const currentWorkspace = useMemo(() => {
    if (!authenticatedUserDetails || !workspaces) return null;
    return workspaces.find(
      (w) => w.workspaceId === (workspaceId || authenticatedUserDetails.workspaceId)
    );
  }, [authenticatedUserDetails, workspaces, workspaceId]);

  const canManageUsers = useMemo(() => {
    const isTenantOwner = loggedInUser?.roles?.includes(UserRoles.TENANT_OWNER);
    return isTenantOwner || currentWorkspace?.role === WorkspaceUserRole.OWNER;
  }, [currentWorkspace, loggedInUser?.roles]);

  if (workspaces.length === 0) {
    return (
      <div className="animate-in fade-in slide-in-from-right-4 space-y-6 p-8 duration-500">
        <h1 className="text-foreground text-3xl font-bold">No Workspaces Available</h1>
        <p className="text-muted-foreground">
          Please contact your administrator to be added to a workspace.
        </p>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="animate-in fade-in slide-in-from-right-4 flex h-[calc(100vh-4rem)] items-center justify-center p-8 duration-500">
        <Loader2 className="mb-3 h-12 w-12 animate-spin text-sky-500" />
      </div>
    );
  }

  return (
    <div className="animate-in fade-in slide-in-from-right-4 space-y-6 p-8 duration-500">
      {onBack && (
        <Button
          variant="ghost"
          onClick={onBack}
          className="text-muted-foreground hover:text-foreground mb-4 -ml-2"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Workspaces
        </Button>
      )}
      <div className="flex items-center justify-between">
        <div className="group flex items-center gap-2">
          {currentWorkspace && authenticatedUserDetails ? (
            <WorkspaceRenameModal
              currentWorkspace={currentWorkspace}
              userDetails={{
                ...authenticatedUserDetails,
                workspaceId: workspaceId || authenticatedUserDetails.workspaceId,
              }}
            />
          ) : (
            <h1 className="text-foreground text-3xl font-bold">{workspaceDetails?.name}</h1>
          )}
        </div>
        {canManageUsers && (
          <InviteUserModal workspaceId={workspaceId || authenticatedUserDetails?.workspaceId} />
        )}
      </div>
      <WorkspaceUserTable canManageUsers={canManageUsers} workspaceId={workspaceId} />
    </div>
  );
};

export { WorkspaceView };
