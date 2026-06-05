'use client';

import { createContext, FC, ReactNode, useCallback, useContext, useEffect, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';

import { AuthToken, CURRENT_WORKSPACE } from '@/constants/Auth';
import { setAccessTokenInMemory } from '@/lib/auth/tokenManager';
import { getRefreshToken, removeRefreshToken, setRefreshToken } from '@/lib/auth/tokenStorage';
import { LoggedInUser, TokenResponse } from '@/types/auth.types';
import { jwtDecoder } from '@/lib/utils/jwtDecode';
import { useGetAccessToken } from '@/hooks/useAuthentication';
import { CurrentWorkspace, User, UserWorkspace } from '@/types/user.types';
import { appRoutes } from '@/config/routes';
import { FEATURES } from '@/constants/features.constants';
import { useGetAuthUserData } from '@/hooks/useGetAuthUser';
import ReactGA from 'react-ga4';

type AuthContextType = {
  login: (token: TokenResponse) => void;
  logout: () => void;
  clearSession: () => void;
  refreshSession: () => Promise<void>;
  isLoading: boolean;
  workspaces?: UserWorkspace[];
  authenticatedUserDetails: CurrentWorkspace | null;
  changeWorkspace: (workspaceId: string) => void;
  loggedInUser?: LoggedInUser;
  authenticatedUser?: User;
};

type AuthProviderProps = {
  children: ReactNode;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: FC<AuthProviderProps> = ({ children }) => {
  const router = useRouter();
  const pathname = usePathname();

  const [isLoading, setIsLoading] = useState(true);
  const [loggedInUser, setLoggedInUser] = useState<LoggedInUser>();
  const [refreshToken, setRefreshTokenState] = useState<string | null>(null);
  const [isTokenChecked, setIsTokenChecked] = useState(false);

  const { mutate, isPending } = useGetAccessToken();
  const queryClient = useQueryClient();

useEffect(() => {
  if (loggedInUser?.userId && loggedInUser?.tenantId) {
    
    ReactGA.set({
      user_id: loggedInUser.userId,
    });

    ReactGA.gtag("set", "user_properties", {
      tenant_id: loggedInUser.tenantId,
    });

    ReactGA.event("user_context_initialized", {
      tenant_id: loggedInUser.tenantId,
      user_id: loggedInUser.userId,
    });

    ReactGA.send({
      hitType: "pageview",
      page: window.location.pathname,
    });
  }
}, [loggedInUser]);

  const {
    user: authenticatedUser,
    workspaces,
    isLoading: isAuthenticatedUserLoading,
    initialSelectedWorkspace,
    refetchAuthUserData,
  } = useGetAuthUserData({ isLoggedIn: !!loggedInUser });

  const [selectedWorkspace, setSelectedWorkspace] = useState<CurrentWorkspace | null>(null);

  useEffect(() => {
    const token = getRefreshToken(AuthToken.REFRESH_TOKEN);
    setRefreshTokenState(token);
    setIsTokenChecked(true);
  }, []);

  const login = (tokenResponse: TokenResponse) => {
    if (!tokenResponse.accessToken || !tokenResponse.refreshToken) return;
    setRefreshToken(tokenResponse.refreshToken, AuthToken.REFRESH_TOKEN);
    saveToken(tokenResponse.accessToken);
  };

  const clearSession = useCallback(() => {
    setLoggedInUser(undefined);
    setSelectedWorkspace(null);
    setRefreshTokenState(null);
    setAccessTokenInMemory(null);
    removeRefreshToken(AuthToken.REFRESH_TOKEN);
    setIsLoading(false);
    queryClient.clear();
    if (typeof window !== 'undefined') {
      sessionStorage.clear();
    }
  }, [queryClient]);

  const logout = useCallback(() => {
    clearSession();
    if (typeof window !== 'undefined') {
      window.location.href = FEATURES.LANDING_PAGES_ENABLED
        ? appRoutes.auth.login
        : `${appRoutes.auth.login}?type=vendor`;
    }
  }, [clearSession]);

  const saveUserDetails = useCallback(
    (token: string) => {
      const decoded = jwtDecoder(token);
      if (!decoded) return logout();
      setLoggedInUser(decoded);
      setIsLoading(false);
    },
    [logout]
  );

  const saveToken = useCallback(
    (accessToken: string) => {
      setAccessTokenInMemory(accessToken);
      saveUserDetails(accessToken);
    },
    [saveUserDetails]
  );

  const refreshSession = useCallback((): Promise<void> => {
    return new Promise((resolve) => {
      const storedToken = getRefreshToken(AuthToken.REFRESH_TOKEN);
      if (!storedToken) {
        resolve();
        return;
      }
      mutate(storedToken, {
        onSuccess: async (data) => {
          if (data.accessToken) saveToken(data.accessToken);
          if (data.refreshToken) setRefreshToken(data.refreshToken, AuthToken.REFRESH_TOKEN);
          await refetchAuthUserData();
          resolve();
        },
        onError: () => resolve(),
      });
    });
  }, [mutate, saveToken, refetchAuthUserData]);

  const refreshAccessToken = useCallback(async () => {
    if (!isTokenChecked) return;

    const token = refreshToken;
    if (token) {
      mutate(token, {
        onSuccess: (data) => {
          if (!data.accessToken) return logout();
          const decoded = jwtDecoder(data.accessToken!);

          if (!decoded) return logout();
          saveToken(data.accessToken);
          const decodedValue = jwtDecoder(data.accessToken!);
          if (typeof window !== 'undefined') {
            const isOnAuthPage =
              pathname.startsWith('/login') ||
              pathname.startsWith('/signup') ||
              pathname.startsWith('/forgot-password') ||
              pathname.startsWith('/password-reset');
            if (decodedValue?.verified && isOnAuthPage) {
              router.push(appRoutes.dashboard.root);
            }
          }
        },
        onSettled: () => {
          setIsLoading(false);
        },
      });
    } else {
      setAccessTokenInMemory(null);
      setIsLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mutate, refreshToken, saveUserDetails, isTokenChecked]);

  useEffect(() => {
    refreshAccessToken();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [refreshToken, isTokenChecked]);

  useEffect(() => {
    if (initialSelectedWorkspace && selectedWorkspace === null) {
      setSelectedWorkspace(initialSelectedWorkspace);
    }
  }, [initialSelectedWorkspace, selectedWorkspace]);

  const changeWorkspace = useCallback(
    (workspaceId: string) => {
      if (!authenticatedUser || !workspaces) return;
      const exists = workspaces.some((w: UserWorkspace) => w.workspaceId === workspaceId);
      if (!exists) return;

      const newSelected = {
        tenantId: authenticatedUser.tenantId,
        workspaceId,
      };

      setSelectedWorkspace(newSelected);
      localStorage.setItem(CURRENT_WORKSPACE, workspaceId);
    },
    [authenticatedUser, workspaces]
  );

  return (
    <AuthContext.Provider
      value={{
        login,
        logout,
        clearSession,
        refreshSession,
        isLoading: isLoading || isPending || isAuthenticatedUserLoading,
        loggedInUser,
        authenticatedUser,
        workspaces,
        authenticatedUserDetails: selectedWorkspace,
        changeWorkspace,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
