import { useCallback, useMemo } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { UserStatus } from '@/constants/user.constants';
import { useApproveUser, useRejectUser } from '@/hooks/useUser';
import { User } from '@/types/user.types';
import { CheckCircle2, MoreHorizontal, UserCheck, UserX, XCircle } from 'lucide-react';
import { useModal } from '@/hooks/useModal';

type UserTableActionsProps = {
  user: User;
};

const UserTableActions = ({ user }: UserTableActionsProps) => {
  const { renderModal } = useModal();
  const { mutate: deactivateUser } = useRejectUser();
  const { mutate: activateUser } = useApproveUser();

  const handleActivate = useCallback(() => {
    activateUser(
      { userId: user.id },
      {
        onSuccess: () => toast.success('User status updated successfully'),
        onError: () => toast.error('Failed to update user status'),
      }
    );
  }, [activateUser, user.id]);

  const handleDeactivate = useCallback(() => {
    deactivateUser(
      { userId: user.id },
      {
        onSuccess: () => toast.success('User status updated successfully'),
        onError: () => toast.error('Failed to update user status'),
      }
    );
  }, [deactivateUser, user.id]);

  const openModal = useCallback(
    (
      type: 'success' | 'error',
      title: string,
      message: string,
      onConfirm: () => void,
      buttonText: string
    ) => {
      renderModal({ type, title, message, onConfirm, submitButtonText: buttonText });
    },
    [renderModal]
  );

  const handleApproveUser = useCallback(() => {
    openModal(
      'success',
      'Approve User',
      `Are you sure you want to approve ${user.name}? They will gain access to the platform immediately.`,
      handleActivate,
      'Approve'
    );
  }, [user.name, handleActivate, openModal]);

  const handleRejectUser = useCallback(() => {
    openModal(
      'error',
      'Reject User Request',
      `Are you sure you want to reject ${user.name}'s request? They will not be able to access the platform.`,
      handleDeactivate,
      'Reject'
    );
  }, [user.name, handleDeactivate, openModal]);

  const handleDeactivateUser = useCallback(() => {
    openModal(
      'error',
      'Deactivate User',
      `Are you sure you want to deactivate ${user.name}? They will not be able to access the platform.`,
      handleDeactivate,
      'Deactivate'
    );
  }, [user.name, handleDeactivate, openModal]);

  const handleActivateUser = useCallback(() => {
    openModal(
      'success',
      'Activate User',
      `Are you sure you want to activate ${user.name}? They will gain access to the platform immediately.`,
      handleActivate,
      'Activate'
    );
  }, [user.name, handleActivate, openModal]);

  const dropdownItems = useMemo(() => {
    if (user.status === UserStatus.PENDING) {
      return [
        {
          label: 'Approve',
          icon: <UserCheck className="mr-2 h-4 w-4 text-green-600" />,
          action: handleApproveUser,
        },
        {
          label: 'Reject',
          icon: <UserX className="mr-2 h-4 w-4" />,
          action: handleRejectUser,
          className: 'text-destructive focus:text-destructive',
        },
      ];
    }
    if (user.status === UserStatus.APPROVED) {
      return [
        {
          label: 'Deactivate',
          icon: <XCircle className="mr-2 h-4 w-4" />,
          action: handleDeactivateUser,
          className: 'text-destructive focus:text-destructive',
        },
      ];
    }
    if (user.status === UserStatus.REJECTED) {
      return [
        {
          label: 'Activate',
          icon: <CheckCircle2 className="mr-2 h-4 w-4 text-green-600" />,
          action: handleActivateUser,
        },
      ];
    }
    return [];
  }, [user.status, handleApproveUser, handleRejectUser, handleDeactivateUser, handleActivateUser]);

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="text-foreground hover:text-foreground h-8 w-8 p-0">
          <span className="sr-only">Open menu</span>
          <MoreHorizontal className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        {dropdownItems.map((item) => (
          <DropdownMenuItem key={item.label} className={item.className} onClick={item.action}>
            {item.icon} {item.label}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export { UserTableActions };
