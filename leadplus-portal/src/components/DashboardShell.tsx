'use client';

import { ReactNode } from 'react';
import { AppSidebar } from '@/components/AppSidebar';
import { Header } from '@/components/Header';
import { useGetAllNavigation } from '@/app/(dashboard)/_hooks/useGetAllNavigation';
import { appRoutes } from '@/config/routes';
import { CustomerMarketplaceButton } from '@/components/CustomerMarketplaceButton';

interface DashboardShellProps {
  children: ReactNode;
}

export const DashboardShell = ({ children }: DashboardShellProps) => {
  const navList = useGetAllNavigation();

  return (
    <div className="flex h-screen overflow-hidden bg-slate-50">
      <AppSidebar pages={navList} />
      <div className="flex min-w-0 flex-1 flex-col overflow-hidden">
        <Header
          hideLogo
          classname="max-w-full"
          headerClassname="bg-white border-b border-slate-200"
          otherOptions={<CustomerMarketplaceButton href={appRoutes.customer.root} />}
        />
        <div className="flex-1 overflow-y-auto">{children}</div>
      </div>
    </div>
  );
};
