'use client';

import { ReactNode } from 'react';

import ProtectedRoute from '@/components/ProtectedRoute';
import VendorProtectedRoute from '@/components/VendorProtectedRoute';
import { appRoutes } from '@/config/routes';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { Header } from '@/components/Header';
import { publicNavLinks } from '@/app/_config/navlinks';
import { CustomerMarketplaceButton } from '@/components/CustomerMarketplaceButton';
import { Module } from '@/constants/modules.constants';

interface HeaderLayoutProps {
  children: ReactNode;
}

const HeaderLayout = ({ children }: HeaderLayoutProps) => {
  return (
    <ProtectedRoute defaultRoute={appRoutes.vendor.auth.login}>
      <VendorProtectedRoute
        allowedStatuses={[
          VendorVerificationStatus.INCOMPLETE,
          VendorVerificationStatus.PENDING,
          VendorVerificationStatus.REJECTED,
          VendorVerificationStatus.APPROVED,
        ]}
      >
        <div className="flex min-w-0 flex-1 flex-col overflow-hidden">
          <Header
            navLinks={publicNavLinks}
            userType={Module.VENDOR}
            otherOptions={<CustomerMarketplaceButton />}
          />
          <div className="flex-1 overflow-y-auto">{children}</div>
        </div>
      </VendorProtectedRoute>
    </ProtectedRoute>
  );
};

export default HeaderLayout;
