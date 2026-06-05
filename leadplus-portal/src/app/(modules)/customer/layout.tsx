import { ReactNode } from 'react';
import { ModuleProtectedRoute } from '@/components/ModuleProtectedRoute';

interface CustomerLayoutProps {
  children: ReactNode;
}

export default function CustomerLayout({ children }: CustomerLayoutProps) {
  return (
    <ModuleProtectedRoute>
      <div className="flex min-h-screen flex-col bg-slate-50">{children}</div>
    </ModuleProtectedRoute>
  );
}
