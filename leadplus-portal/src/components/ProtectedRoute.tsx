'use client';

import { PropsWithChildren, useEffect } from 'react';
import { usePathname, useRouter } from 'next/navigation';

import { useAuth } from '@/context/AuthContext';
import { appRoutes } from '@/config/routes';
import { UserRoles, UserStatus } from '@/constants/user.constants';

type ProtectedRouteProps = {
  defaultRoute?: string;
  allowedRoles?: UserRoles[];
};

const NON_VERIFIED_USER_STATUS = [UserStatus.PENDING, UserStatus.REJECTED];
const NON_VERIFIED_ALLOWED_ROUTES = [appRoutes.leadgen.profileOverview.root];

const ProtectedRoute = ({
  children,
  defaultRoute = appRoutes.leadgen.auth.login,
  allowedRoles,
}: PropsWithChildren<ProtectedRouteProps>) => {
  const { loggedInUser, isLoading, authenticatedUser } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  const isNonVerifiedUser =
    authenticatedUser && NON_VERIFIED_USER_STATUS.includes(authenticatedUser.status);

  const isRouteAllowedForNonVerifiedUser = NON_VERIFIED_ALLOWED_ROUTES.some((route) =>
    pathname.startsWith(route)
  );

  const isEmailVerified = loggedInUser?.verified;
  const shouldRedirectNonVerifiedUser = isNonVerifiedUser && !isRouteAllowedForNonVerifiedUser;

  useEffect(() => {
    if (isLoading) return;

    if (!loggedInUser) {
      const moduleMatch = pathname.match(/^\/([^/]+)/);
      const source = moduleMatch ? moduleMatch[1] : '';
      const loginUrl = source ? `${appRoutes.auth.login}?source=${source}` : appRoutes.auth.login;
      router.replace(loginUrl);
      return;
    }

    if (loggedInUser && !isEmailVerified) {
      router.replace(appRoutes.emailVerification.root);
    } else if (shouldRedirectNonVerifiedUser) {
      router.replace(appRoutes.leadgen.profileOverview.root);
    } else if (
      allowedRoles &&
      !loggedInUser?.roles?.some((role) => allowedRoles.includes(role as UserRoles))
    ) {
      router.replace(defaultRoute);
    }
  }, [
    isLoading,
    loggedInUser,
    shouldRedirectNonVerifiedUser,
    router,
    isEmailVerified,
    defaultRoute,
    pathname,
    allowedRoles,
  ]);

  if (isLoading || !loggedInUser || !authenticatedUser || shouldRedirectNonVerifiedUser) {
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

export default ProtectedRoute;
