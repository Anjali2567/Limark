import { DashboardShell } from '@/components/DashboardShell';
import ProtectedRoute from '@/components/ProtectedRoute';
import { ReactNode } from 'react';

interface AdminDashboardLayoutProps {
  children: ReactNode;
}

const AdminDashboardLayout = ({ children }: AdminDashboardLayoutProps) => {
  return (
    <ProtectedRoute>
      <DashboardShell>{children}</DashboardShell>
    </ProtectedRoute>
  );
};

export default AdminDashboardLayout;
