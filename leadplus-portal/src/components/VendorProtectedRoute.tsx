'use client';

import { PropsWithChildren, useEffect, useMemo } from 'react';
import { useRouter, usePathname } from 'next/navigation';

import { appRoutes } from '@/config/routes';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { useGetAuthenticatedVendor } from '@/hooks/useAuthentication';
import { useGetAllVendorAgreements } from '@/hooks/useVendorAgreement';
import { useAuth } from '@/context/AuthContext';
import { UserStatus } from '@/constants/user.constants';
import { FEATURES } from '@/constants/features.constants';

type VendorProtectedRouteProps = {
  allowedStatuses?: VendorVerificationStatus[];
};

const VendorProtectedRoute = ({
  children,
  allowedStatuses,
}: PropsWithChildren<VendorProtectedRouteProps>) => {
  const router = useRouter();
  const pathname = usePathname();

  const { authenticatedUser } = useAuth();
  const { data: vendor, isLoading } = useGetAuthenticatedVendor();
  const allVendorAgreementsQueries = useGetAllVendorAgreements(
    FEATURES.AGREEMENT_CHECK_ENABLED ? vendor?.id || '' : ''
  );

  const isVendorAllowed =
    vendor?.vendorVerificationStatus && allowedStatuses?.includes(vendor.vendorVerificationStatus);

  const isVendorAgreementsLoading =
    FEATURES.AGREEMENT_CHECK_ENABLED && allVendorAgreementsQueries.some((query) => query.isLoading);

  const agreementsCount = useMemo(
    () =>
      FEATURES.AGREEMENT_CHECK_ENABLED
        ? allVendorAgreementsQueries.filter((query) => query.data !== undefined).length
        : 0,
    [allVendorAgreementsQueries]
  );

  const hasUnsignedAgreement = useMemo(
    () =>
      FEATURES.AGREEMENT_CHECK_ENABLED &&
      allVendorAgreementsQueries.some((query) => query.data !== undefined && !query.data.signed),
    [allVendorAgreementsQueries]
  );

  const shouldRedirect = useMemo(() => {
    if (isLoading || isVendorAgreementsLoading || !vendor) return false;
    if (authenticatedUser && authenticatedUser.status !== UserStatus.APPROVED) {
      return !pathname.startsWith(appRoutes.leadgen.profileOverview.root);
    }
    if (vendor.vendorVerificationStatus === VendorVerificationStatus.INCOMPLETE) {
      return !pathname.startsWith(appRoutes.vendor.onboarding.root);
    }
    if (!isVendorAllowed) {
      return !pathname.startsWith(appRoutes.vendor.profileOverview.root);
    }
    if (
      FEATURES.AGREEMENT_CHECK_ENABLED &&
      vendor.vendorVerificationStatus === VendorVerificationStatus.APPROVED &&
      agreementsCount > 0 &&
      hasUnsignedAgreement
    ) {
      return !pathname.startsWith(appRoutes.vendor.agreements.root);
    }
    return false;
  }, [
    authenticatedUser,
    pathname,
    isLoading,
    isVendorAgreementsLoading,
    isVendorAllowed,
    vendor,
    agreementsCount,
    hasUnsignedAgreement,
  ]);

  useEffect(() => {
    if (isLoading || isVendorAgreementsLoading) return;

    if (authenticatedUser && authenticatedUser.status !== UserStatus.APPROVED) {
      router.replace(appRoutes.leadgen.profileOverview.root);
    } else if (!vendor) {
      router.replace(appRoutes.vendor.root);
    } else if (vendor && vendor.vendorVerificationStatus === VendorVerificationStatus.INCOMPLETE) {
      router.replace(appRoutes.vendor.onboarding.root);
    } else if (vendor && !isVendorAllowed) {
      router.replace(appRoutes.vendor.profileOverview.root);
    } else if (
      FEATURES.AGREEMENT_CHECK_ENABLED &&
      vendor &&
      vendor.vendorVerificationStatus === VendorVerificationStatus.APPROVED &&
      agreementsCount > 0 &&
      hasUnsignedAgreement
    ) {
      router.replace(appRoutes.vendor.agreements.root);
    }
  }, [
    authenticatedUser,
    isLoading,
    isVendorAgreementsLoading,
    isVendorAllowed,
    vendor,
    agreementsCount,
    hasUnsignedAgreement,
    router,
  ]);

  if (isLoading || isVendorAgreementsLoading || !vendor || shouldRedirect) {
    return (
      <div className="flex h-screen items-center justify-center bg-white">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-500 border-t-transparent" />
          <p className="mt-4 text-gray-600">Verifying access...</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};

export default VendorProtectedRoute;
