import { ReactNode } from 'react';
import { ModuleProtectedRoute } from '@/components/ModuleProtectedRoute';

interface ModulesLayoutProps {
  children: ReactNode;
}

export default function ModulesLayout({ children }: ModulesLayoutProps) {
  return <ModuleProtectedRoute>{children}</ModuleProtectedRoute>;
}
