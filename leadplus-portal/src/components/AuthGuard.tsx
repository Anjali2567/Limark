'use client';

import { PropsWithChildren, useEffect } from 'react';
import { usePathname, useRouter } from 'next/navigation';

import { useAuth } from '@/context/AuthContext';
import { appRoutes } from '@/config/routes';
import { FEATURES } from '@/constants/features.constants';
import { UserRoles } from '@/constants/user.constants';

type GenericProtectedRouteProps = {
  defaultRoute?: string;
  allowedRoles?: UserRoles[];
};

const AuthGuard = ({
  children,
  defaultRoute = appRoutes.leadgen.auth.login,
  allowedRoles,
}: PropsWithChildren<GenericProtectedRouteProps>) => {
  const { loggedInUser, isLoading } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  const isEmailVerified = loggedInUser?.verified;

  useEffect(() => {
    if (isLoading) return;

    if (!loggedInUser) {
      const moduleMatch = pathname.match(/^\/([^/]+)/);
      const source = moduleMatch ? moduleMatch[1] : '';
      const params = new URLSearchParams();
      if (source) params.set('source', source);
      if (!FEATURES.LANDING_PAGES_ENABLED) params.set('type', 'vendor');
      const loginUrl = `${appRoutes.auth.login}${params.toString() ? `?${params.toString()}` : ''}`;
      router.replace(loginUrl);
      return;
    }

    if (loggedInUser && !isEmailVerified) {
      router.replace(appRoutes.emailVerification.root);
    } else if (
      allowedRoles &&
      !loggedInUser?.roles?.some((role) => allowedRoles.includes(role as UserRoles))
    ) {
      router.replace(defaultRoute);
    }
  }, [isLoading, loggedInUser, isEmailVerified, defaultRoute, pathname, allowedRoles, router]);

  if (isLoading || !loggedInUser) {
    return (
      <div className="flex h-screen items-center justify-center bg-white">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-500 border-t-transparent" />
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};

export default AuthGuard;
