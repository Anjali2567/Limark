import { PropsWithChildren } from 'react';
import { ModuleProtectedRoute } from '@/components/ModuleProtectedRoute';

const LeadGenLayout = ({ children }: PropsWithChildren) => {
  return <ModuleProtectedRoute>{children}</ModuleProtectedRoute>;
};
export default LeadGenLayout;
