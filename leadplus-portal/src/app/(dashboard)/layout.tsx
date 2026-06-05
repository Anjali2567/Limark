import { ReactNode } from 'react';

import ProtectedRoute from '@/components/ProtectedRoute';
import { appRoutes } from '@/config/routes';
import { DashboardShell } from '@/components/DashboardShell';

type DashboardLayoutProps = {
  children: ReactNode;
};

const DashboardLayout = ({ children }: DashboardLayoutProps) => {
  return (
    <ProtectedRoute defaultRoute={appRoutes.auth.login}>
      <DashboardShell>{children}</DashboardShell>
    </ProtectedRoute>
  );
};

export default DashboardLayout;
