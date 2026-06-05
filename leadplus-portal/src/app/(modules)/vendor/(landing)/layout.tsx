'use client';

import { PropsWithChildren } from 'react';
import { useRouter } from 'next/navigation';

import { Module } from '@/constants/modules.constants';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useCreateVendor } from '@/hooks/useVendor';
import { Button } from '@/components/ui/button';
import PublicHeaderLayout from '@/components/PublicHeaderLayout';

const VendorHeaderLayout = ({ children }: PropsWithChildren) => {
  const router = useRouter();
  const { loggedInUser, refreshSession } = useAuth();
  const { mutate: createVendor, isPending } = useCreateVendor();

  const onboardAsVendor = () => {
    createVendor(undefined, {
      onSuccess: async () => {
        await refreshSession();
        router.replace(appRoutes.vendor.onboarding.root);
      },
    });
  };

  return (
    <PublicHeaderLayout
      userType={Module.VENDOR}
      otherOptions={
        loggedInUser && (
          <Button onClick={onboardAsVendor} disabled={isPending}>
            Become a Vendor
          </Button>
        )
      }
    >
      {children}
    </PublicHeaderLayout>
  );
};

export default VendorHeaderLayout;
