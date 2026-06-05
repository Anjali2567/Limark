import { useMemo } from 'react';

import { Module, moduleHierarchy, moduleRequiredRoles, moduleRouteMap } from '@/constants/modules.constants';
import { UserRoles } from '@/constants/user.constants';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { useAuth } from '@/context/AuthContext';
import { useGetAuthenticatedVendor } from './useAuthentication';
import { useGetTenantModules } from './useTenant';
import { useGetAllVendorAgreements } from './useVendorAgreement';
import { appRoutes } from '@/config/routes';
import { VendorAgreement } from '@/types/vendor-agreements.types';
import { FEATURES } from '@/constants/features.constants';

type PostLoginRedirectResult = {
  isLoading: boolean;
  shouldRedirect: boolean;
  redirectPath: string | null;
  hasNoModules: boolean;
};

export const usePostLoginRedirect = (): PostLoginRedirectResult => {
  const { isLoading: authLoading, authenticatedUserDetails, loggedInUser } = useAuth();

  const { data, isLoading: moduleLoading } = useGetTenantModules(
    authenticatedUserDetails?.tenantId
  );

  const modules = useMemo(() => data?.modules ?? [], [data]);
  const hasVendorModule = modules.includes(Module.VENDOR);
  const hasVendorRole = loggedInUser?.roles?.includes(UserRoles.VENDOR) ?? false;
  const hasVendorAccess = hasVendorModule && hasVendorRole;

  const { data: vendor, isLoading: vendorLoading } = useGetAuthenticatedVendor({
    enabled: hasVendorAccess,
  });

  const isVendorApproved = vendor?.vendorVerificationStatus === VendorVerificationStatus.APPROVED;

  const allVendorAgreementsQueries = useGetAllVendorAgreements(
    FEATURES.AGREEMENT_CHECK_ENABLED && hasVendorAccess && isVendorApproved ? (vendor?.id ?? '') : ''
  );
  const isAgreementsLoading = FEATURES.AGREEMENT_CHECK_ENABLED && allVendorAgreementsQueries.some((q) => q.isLoading);

  return useMemo<PostLoginRedirectResult>(() => {
    if (authLoading || moduleLoading || !authenticatedUserDetails?.tenantId) {
      return { isLoading: true, shouldRedirect: false, redirectPath: null, hasNoModules: false };
    }

    if (modules.length === 0) {
      return { isLoading: false, shouldRedirect: false, redirectPath: null, hasNoModules: true };
    }

    if (hasVendorAccess && (vendorLoading || isAgreementsLoading)) {
      return { isLoading: true, shouldRedirect: false, redirectPath: null, hasNoModules: false };
    }

    const firstModule = [...modules]
      .sort((a, b) => moduleHierarchy.indexOf(a) - moduleHierarchy.indexOf(b))
      .find((module) => {
        const requiredRoles = moduleRequiredRoles[module];
        if (!requiredRoles) return true;
        return loggedInUser?.roles?.some((role) => requiredRoles.includes(role as UserRoles));
      });

    if (!firstModule) {
      return { isLoading: false, shouldRedirect: false, redirectPath: null, hasNoModules: false };
    }

    if (FEATURES.AGREEMENT_CHECK_ENABLED && firstModule === Module.VENDOR) {
      const agreements = allVendorAgreementsQueries
        .map((query) => query.data)
        .filter((data): data is VendorAgreement => data !== undefined);
      const hasUnsignedAgreement = agreements.some((agreement) => !agreement.signed);

      if (isVendorApproved && hasUnsignedAgreement) {
        return {
          isLoading: false,
          shouldRedirect: true,
          redirectPath: appRoutes.vendor.agreements.root,
          hasNoModules: false,
        };
      }
    }

    const redirectPath = moduleRouteMap[firstModule] ?? null;
    return {
      isLoading: false,
      shouldRedirect: Boolean(redirectPath),
      redirectPath,
      hasNoModules: false,
    };
  }, [
    authLoading,
    moduleLoading,
    authenticatedUserDetails?.tenantId,
    loggedInUser,
    modules,
    hasVendorAccess,
    vendorLoading,
    isAgreementsLoading,
    allVendorAgreementsQueries,
    isVendorApproved,
  ]);
};
