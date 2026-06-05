'use client';

import { ReactNode } from 'react';

import { Header } from '@/components/Header';
import ProtectedRoute from '@/components/ProtectedRoute';
import VendorProtectedRoute from '@/components/VendorProtectedRoute';
import { appRoutes } from '@/config/routes';
import { Module } from '@/constants/modules.constants';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { publicNavLinks } from '@/app/_config/navlinks';
import { useAuth } from '@/context/AuthContext';
import { CustomerMarketplaceButton } from '@/components/CustomerMarketplaceButton';

interface HeaderLayoutProps {
  children: ReactNode;
}

const HeaderLayout = ({ children }: HeaderLayoutProps) => {
  const { loggedInUser } = useAuth();

  return (
    <ProtectedRoute defaultRoute={appRoutes.leadgen.auth.login}>
      <VendorProtectedRoute
        allowedStatuses={[
          VendorVerificationStatus.INCOMPLETE,
          VendorVerificationStatus.PENDING,
          VendorVerificationStatus.REJECTED,
          VendorVerificationStatus.APPROVED,
        ]}
      >
        <div className="flex h-screen overflow-hidden bg-slate-50">
          <div className="flex min-w-0 flex-1 flex-col overflow-hidden">
            <Header
              nonAuthState={!loggedInUser}
              navLinks={publicNavLinks}
              userType={Module.VENDOR}
              otherOptions={<CustomerMarketplaceButton />}
            />
            <main className="flex-1 overflow-y-auto">{children}</main>
          </div>
        </div>
      </VendorProtectedRoute>
    </ProtectedRoute>
  );
};

export default HeaderLayout;
