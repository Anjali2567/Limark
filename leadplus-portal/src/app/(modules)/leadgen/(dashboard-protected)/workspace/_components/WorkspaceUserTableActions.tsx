import { useCallback } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { WorkspaceUserRole, WorkspaceUserStatus } from '@/constants/user.constants';
import { useAuth } from '@/context/AuthContext';
import { useModal } from '@/hooks/useModal';
import { useRevokeWorkspaceAccess, useUpdateWorkspaceUserRole } from '@/hooks/useWorkspace';
import { WorkspaceUsersResponse } from '@/types/user.types';

import { MoreHorizontal, RefreshCw, Trash2, X } from 'lucide-react';

type WorkspaceUserTableActionsProps = {
  user: WorkspaceUsersResponse;
  canManageUsers: boolean;
};

const WorkspaceUserTableActions = ({ user, canManageUsers }: WorkspaceUserTableActionsProps) => {
  const { authenticatedUserDetails } = useAuth();
  const { renderModal } = useModal();
  const isInvitation = user.workspaceUserStatus === WorkspaceUserStatus.INVITED;
  const isTargetOwner = user.workspaceUserRole === WorkspaceUserRole.OWNER;
  const isWorkspaceAdmin = user.workspaceUserRole === WorkspaceUserRole.WORKSPACE_ADMIN;

  const { mutate: revokeInvitation } = useRevokeWorkspaceAccess();
  const { mutate: updateWorkspaceUserRole } = useUpdateWorkspaceUserRole();

  const shouldShowMenu = canManageUsers && !isTargetOwner;

  const onCancelInvitation = useCallback(() => {
    renderModal({
      type: 'error',
      title: 'Cancel Invitation',
      message: `Are you sure you want to cancel the invitation for ${user.email}?`,
      cancelButtonText: 'Keep Invitation',
      submitButtonText: 'Cancel Invitation',
      onConfirm: () => {
        revokeInvitation(
          {
            tenantId: authenticatedUserDetails?.tenantId || '',
            workspaceId: authenticatedUserDetails?.workspaceId || '',
            userId: user.id,
          },
          {
            onSuccess: () => {
              toast.success('Invitation cancelled successfully.');
            },
            onError: (error) => {
              toast.error(`Failed to cancel invitation. ${error.message}`);
            },
          }
        );
      },
    });
  }, [renderModal, revokeInvitation, user, authenticatedUserDetails]);

  const onRevokeAccess = useCallback(() => {
    renderModal({
      type: 'error',
      title: 'Revoke Member Access',
      message: `Are you sure you want to revoke access for ${user.name}? They will no longer be able to access this workspace or send campaigns on your behalf.`,
      submitButtonText: 'Revoke Access',
      onConfirm: () => {
        revokeInvitation(
          {
            tenantId: authenticatedUserDetails?.tenantId || '',
            workspaceId: authenticatedUserDetails?.workspaceId || '',
            userId: user.id,
          },
          {
            onSuccess: () => {
              toast.success('Access revoked successfully.');
            },
            onError: (error) => {
              toast.error(`Failed to revoke access. ${error.message}`);
            },
          }
        );
      },
    });
  }, [renderModal, user, authenticatedUserDetails, revokeInvitation]);

  const onToggleRole = useCallback(() => {
    const nextRole =
      user.workspaceUserRole === WorkspaceUserRole.WORKSPACE_ADMIN
        ? WorkspaceUserRole.MEMBER
        : WorkspaceUserRole.WORKSPACE_ADMIN;

    updateWorkspaceUserRole(
      {
        tenantId: authenticatedUserDetails?.tenantId || '',
        workspaceId: authenticatedUserDetails?.workspaceId || '',
        userId: user.id,
        payload: { role: nextRole },
      },
      {
        onSuccess: () => {
          toast.success(
            nextRole === WorkspaceUserRole.WORKSPACE_ADMIN
              ? 'Workspace admin access granted successfully.'
              : 'Workspace admin access removed successfully.'
          );
        },
        onError: (error) => {
          toast.error(`Failed to update role. ${error.message}`);
        },
      }
    );
  }, [authenticatedUserDetails, updateWorkspaceUserRole, user]);

  if (!shouldShowMenu) return null;

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="text-foreground hover:text-foreground h-8 w-8 p-0">
          <span className="sr-only">Open menu</span>
          <MoreHorizontal className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end">
        {isInvitation && (
          <DropdownMenuItem className="cursor-pointer" onClick={onCancelInvitation}>
            <X className="mr-2 h-4 w-4" />
            Cancel Invitation
          </DropdownMenuItem>
        )}

        {!isInvitation && (
          <>
            <DropdownMenuItem className="cursor-pointer" onClick={onToggleRole}>
              <RefreshCw className="mr-2 h-4 w-4" />
              {isWorkspaceAdmin ? 'Make Member' : 'Make Workspace Admin'}
            </DropdownMenuItem>
            <DropdownMenuItem
              className="text-destructive focus:text-destructive cursor-pointer"
              onClick={onRevokeAccess}
            >
              <Trash2 className="mr-2 h-4 w-4" />
              Revoke Access
            </DropdownMenuItem>
          </>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export { WorkspaceUserTableActions };
