import { ReactNode } from 'react';

import { DashboardShell } from '@/components/DashboardShell';
import ProtectedRoute from '@/components/ProtectedRoute';
import VendorProtectedRoute from '@/components/VendorProtectedRoute';
import { appRoutes } from '@/config/routes';
import { UserRoles } from '@/constants/user.constants';
import { VendorVerificationStatus } from '@/constants/vendor.constant';

interface DashboardLayoutProps {
  children: ReactNode;
}

const DashboardLayout = ({ children }: DashboardLayoutProps) => {
  return (
    <ProtectedRoute allowedRoles={[UserRoles.VENDOR]} defaultRoute={appRoutes.vendor.root}>
      <VendorProtectedRoute allowedStatuses={[VendorVerificationStatus.APPROVED]}>
        <DashboardShell>{children}</DashboardShell>
      </VendorProtectedRoute>
    </ProtectedRoute>
  );
};

export default DashboardLayout;
