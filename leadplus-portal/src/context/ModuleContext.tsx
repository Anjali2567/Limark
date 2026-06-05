'use client';

import { createContext, FC, ReactNode, useContext, useMemo } from 'react';

import { useGetTenantModules } from '@/hooks/useTenant';
import { Module, moduleDisplayOrder } from '@/constants/modules.constants';
import { FEATURES } from '@/constants/features.constants';
import { TenantModules } from '@/types/tenant.types';
import { useAuth } from './AuthContext';

interface ModuleContextType {
  enabledModules: Module[];
  tenantModules?: TenantModules;
  isLoading: boolean;
}

const ModuleContext = createContext<ModuleContextType | undefined>(undefined);

interface ModuleProviderProps {
  children: ReactNode;
}

export const ModuleProvider: FC<ModuleProviderProps> = ({ children }) => {
  const { authenticatedUserDetails, isLoading, loggedInUser } = useAuth();
  const { tenantId } = authenticatedUserDetails || {};
  const { data: tenantModules, isLoading: isTenantModulesLoading } = useGetTenantModules(tenantId);

  const enabledModules = useMemo(() => {
    if (!tenantModules?.modules) return [];
    const modules = [...tenantModules.modules].filter(
      (m) => FEATURES.CUSTOMER_MODULE_ENABLED || m !== Module.CUSTOMER
    );
    return modules.sort((a, b) => moduleDisplayOrder.indexOf(a) - moduleDisplayOrder.indexOf(b));
  }, [tenantModules]);

  const isStillLoading = isLoading || isTenantModulesLoading || (!!loggedInUser && !tenantId);

  return (
    <ModuleContext.Provider
      value={{
        enabledModules,
        isLoading: isStillLoading,
        tenantModules,
      }}
    >
      {children}
    </ModuleContext.Provider>
  );
};

export const useModule = () => {
  const context = useContext(ModuleContext);
  if (!context) {
    throw new Error('useModule must be used within a ModuleProvider');
  }
  return context;
};
