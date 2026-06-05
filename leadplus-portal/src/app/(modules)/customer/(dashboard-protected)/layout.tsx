'use client';

import { ReactNode } from 'react';

import { DashboardShell } from '@/components/DashboardShell';
import AuthGuard from '@/components/AuthGuard';
import { UserRoles } from '@/constants/user.constants';

interface DashboardLayoutProps {
  children: ReactNode;
}

const DashboardLayout = ({ children }: DashboardLayoutProps) => {
  return (
    <AuthGuard allowedRoles={[UserRoles.CUSTOMER, UserRoles.USER, UserRoles.GUEST]}>
      <DashboardShell>{children}</DashboardShell>
    </AuthGuard>
  );
};

export default DashboardLayout;
