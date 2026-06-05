import { PropsWithChildren } from 'react';
import { ModuleProtectedRoute } from '@/components/ModuleProtectedRoute';

const AdminLayout = ({ children }: PropsWithChildren) => {
  return <ModuleProtectedRoute>{children}</ModuleProtectedRoute>;
};
export default AdminLayout;
