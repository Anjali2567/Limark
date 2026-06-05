'use client';

import { FC, ReactNode, useEffect } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useModule } from '@/context/ModuleContext';
import { Module, moduleDefaultRoute, moduleRouteMap } from '@/constants/modules.constants';
import { appRoutes } from '@/config/routes';

interface ModuleProtectedRouteProps {
  children: ReactNode;
}

const exclusionRoutes = [
  appRoutes.vendor.root,
  appRoutes.vendor.onboarding.root,
  appRoutes.vendor.profileOverview.root,
  appRoutes.vendor.agreements.root,
  appRoutes.leadgen.profileOverview.root,
];

export const ModuleProtectedRoute: FC<ModuleProtectedRouteProps> = ({ children }) => {
  const { isLoading, enabledModules } = useModule();
  const router = useRouter();
  const pathname = usePathname();

  const matchedModules = Object.entries(moduleRouteMap)
    .filter(([, route]) => pathname.startsWith(route))
    .map(([module]) => module as Module);

  const isModuleEnabled =
    exclusionRoutes.includes(pathname) ||
    matchedModules.length === 0 ||
    matchedModules.some((module) => enabledModules.includes(module));

  useEffect(() => {
    if (isLoading) return;

    if (!isModuleEnabled) {
      const firstEnabledRoute =
        enabledModules.length > 0
          ? moduleRouteMap[enabledModules[0]] || moduleDefaultRoute
          : moduleDefaultRoute;

      router.replace(firstEnabledRoute);
    }
  }, [isLoading, isModuleEnabled, enabledModules, router]);

  if (isLoading || !isModuleEnabled) {
    return (
      <div className="flex h-screen items-center justify-center bg-white">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-500 border-t-transparent" />
          <p className="mt-4 text-gray-600">Checking module access...</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};
