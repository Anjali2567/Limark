import { PropsWithChildren } from 'react';
import { ModuleProtectedRoute } from '@/components/ModuleProtectedRoute';

const VendorLayout = ({ children }: PropsWithChildren) => {
  return <ModuleProtectedRoute>{children}</ModuleProtectedRoute>;
};

export default VendorLayout;
