import { ReactNode, Suspense } from 'react';

import { AuthLayoutContent } from '@/components/AuthLayoutContent';
import { appRoutes } from '@/config/routes';

const AuthLayout = ({ children }: { children: ReactNode }) => {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <AuthLayoutContent loginPath={appRoutes.auth.login} signupPath={appRoutes.auth.signup}>
        {children}
      </AuthLayoutContent>
    </Suspense>
  );
};

export default AuthLayout;
