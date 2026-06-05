'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useMemo } from 'react';

import { moduleDefaultRoute, moduleRouteMap } from '@/constants/modules.constants';
import { FEATURES } from '@/constants/features.constants';
import { useModule } from '@/context/ModuleContext';
import { useAuth } from '@/context/AuthContext';
import { appRoutes } from '@/config/routes';

const App = () => {
  const router = useRouter();
  const { authenticatedUser } = useAuth();
  const { enabledModules, isLoading } = useModule();

  const isWhiteListedDomain = useMemo(() => {
    if (typeof window === 'undefined') return false;
    const domain = window.location.hostname;
    return domain.includes('localhost') || domain.includes('leadplus.ai');
  }, []);

  useEffect(() => {
    if (isLoading) return;

    if (!authenticatedUser && isWhiteListedDomain) {
      router.replace(
        FEATURES.LANDING_PAGES_ENABLED ? appRoutes.customer.root : `${appRoutes.auth.login}?type=vendor`
      );
      return;
    }

    if (enabledModules?.length > 0) {
      const firstEnabledRoute = moduleRouteMap[enabledModules[0]] ?? moduleDefaultRoute;
      router.replace(firstEnabledRoute);
      return;
    }

    router.replace(moduleDefaultRoute);
  }, [authenticatedUser, enabledModules, isLoading, isWhiteListedDomain, router]);

  return null;
};

export default App;
