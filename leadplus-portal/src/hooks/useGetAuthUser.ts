'use client';

import { useQueries } from '@tanstack/react-query';
import { queryKeys } from '@/config/queryKeys';
import { getAuthenticatedUser, getAuthenticatedUserWorkspaces } from '@/lib/api/auth.api';
import { CURRENT_WORKSPACE } from '@/constants/Auth';
import { CurrentWorkspace, User, UserWorkspace } from '@/types/user.types';

type AuthUserDataHookReturn = {
  user: User | undefined;
  workspaces: UserWorkspace[] | undefined;
  isLoading: boolean;
  initialSelectedWorkspace: CurrentWorkspace | null;
  refetchAuthUserData: () => Promise<void>;
};

type Params = {
  isLoggedIn: boolean;
};

export const useGetAuthUserData = ({ isLoggedIn }: Params): AuthUserDataHookReturn => {
  const results = useQueries({
    queries: [
      {
        queryKey: queryKeys.user.authenticatedUser,
        queryFn: getAuthenticatedUser,
        enabled: isLoggedIn,
      },
      {
        queryKey: queryKeys.user.authenticatedUserWorkspaces,
        queryFn: getAuthenticatedUserWorkspaces,
        enabled: isLoggedIn,
      },
    ],
  });

  const [userQuery, workspacesQuery] = results;

  const user = userQuery.data;
  const workspaces = workspacesQuery.data ?? [];
  const isLoading = userQuery.isLoading || workspacesQuery.isLoading;

  const refetchAuthUserData = async () => {
    await Promise.all([userQuery.refetch(), workspacesQuery.refetch()]);
  };

  let initialSelectedWorkspace = null;
  if (isLoading || !user) {
    initialSelectedWorkspace = null;
    return { user, workspaces, isLoading, initialSelectedWorkspace, refetchAuthUserData };
  }

  if (workspaces.length > 0) {
    const storedWorkspaceId =
      typeof window !== 'undefined' ? localStorage.getItem(CURRENT_WORKSPACE) : null;
    const workspaceId =
      storedWorkspaceId && workspaces.some((w) => w.workspaceId === storedWorkspaceId)
        ? storedWorkspaceId
        : workspaces[0].workspaceId;

    if (
      typeof window !== 'undefined' &&
      (!storedWorkspaceId || storedWorkspaceId !== workspaceId)
    ) {
      localStorage.setItem(CURRENT_WORKSPACE, workspaceId);
    }

    initialSelectedWorkspace = {
      tenantId: user.tenantId || '',
      workspaceId: workspaceId || '',
    };
  } else {
    initialSelectedWorkspace = {
      tenantId: user.tenantId,
      workspaceId: user.workspaceId,
    };
  }

  return { user, workspaces, isLoading, initialSelectedWorkspace, refetchAuthUserData };
};
