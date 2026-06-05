'use client';

import Link from 'next/link';
import { useEffect } from 'react';

import { appRoutes } from '@/config/routes';
import { AuthToken } from '@/constants/Auth';
import { UserStatus } from '@/constants/user.constants';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { useAuth } from '@/context/AuthContext';
import { useGetAuthenticatedVendor } from '@/hooks/useAuthentication';
import { removeRefreshToken } from '@/lib/auth/tokenStorage';
import { useRouter } from 'next/navigation';
import { AccessStatus } from './_components/AccessStatus';

import { Ban, Clock } from 'lucide-react';

const ProfileOverview = () => {
  const router = useRouter();
  const { authenticatedUser: authenticatedUserDetails, isLoading } = useAuth();
  const { data: vendor, isLoading: isVendorLoading } = useGetAuthenticatedVendor();

  useEffect(() => {
    if (authenticatedUserDetails?.status === UserStatus.APPROVED) {
      router.push(appRoutes.leadgen.search.companySearch);
    }
  }, [authenticatedUserDetails, vendor, router]);

  if (!authenticatedUserDetails || isLoading || isVendorLoading) {
    return (
      <div className="flex h-[calc(100vh-64px)] items-center justify-center bg-white">
        <div className="text-center">
          <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-500 border-t-transparent" />
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  const { status } = authenticatedUserDetails;

  if (status === UserStatus.APPROVED && vendor) {
    if (vendor.vendorVerificationStatus === VendorVerificationStatus.PENDING) {
      return null;
    }

    if (vendor.vendorVerificationStatus === VendorVerificationStatus.REJECTED) {
      return (
        <div className="flex flex-col items-center justify-center p-4 text-center">
          <AccessStatus
            type="rejected"
            title="Welcome to LeadPlus"
            subtitle="Vendor Profile Not Approved"
            icon={Ban}
            iconBgColor="bg-destructive/10"
            iconColor="text-destructive"
            description={
              <>
                <p className="text-foreground text-sm">
                  Your vendor profile for{' '}
                  <span className="font-semibold text-sky-500">{vendor.companyName}</span> was
                  reviewed and unfortunately was not approved.
                </p>
                {vendor.reviewComment && (
                  <p className="text-muted-foreground mt-2 text-sm">
                    <span className="font-semibold">Reason:</span> {vendor.reviewComment}
                  </p>
                )}
                <p className="text-muted-foreground text-sm">
                  This may be due to eligibility requirements or administrative policies.
                </p>
              </>
            }
            nextSteps={{
              title: 'What can you do?',
              items: [
                'Review your vendor profile details for accuracy',
                'Contact support if you believe this is a mistake',
                'You may resubmit your profile if eligibility changes',
              ],
              borderColor: 'border-destructive',
              bgColor: 'bg-destructive/5',
            }}
          />
        </div>
      );
    }
  }

  if (status === UserStatus.PENDING) {
    return (
      <div className="flex flex-col items-center justify-center p-4 text-center">
        <AccessStatus
          type="pending"
          title="Welcome to LeadPlus"
          subtitle="Account Pending Approval"
          icon={Clock}
          iconBgColor="bg-warning/10"
          iconColor="text-warning"
          description={
            <>
              <p className="text-foreground text-sm">
                Thank you for signing up! Your account{' '}
                <span className="font-semibold text-sky-500">
                  {authenticatedUserDetails?.email}
                </span>{' '}
                is currently pending approval from an administrator.
              </p>
              <p className="text-muted-foreground text-sm">
                You&apos;ll receive an email notification once your account has been approved and
                you can access the platform.
              </p>
            </>
          }
          nextSteps={{
            title: 'What happens next?',
            items: [
              'An administrator will review your request',
              "You'll be notified via email once approved",
              'This usually takes 1-2 business days',
            ],
            borderColor: 'border-sky-500',
            bgColor: 'bg-sky-500/5',
          }}
        />
      </div>
    );
  }

  if (status === UserStatus.REJECTED) {
    return (
      <div className="flex flex-col items-center justify-center p-4 text-center">
        <AccessStatus
          type="rejected"
          title="Welcome to LeadPlus"
          subtitle="Access Not Approved"
          icon={Ban}
          iconBgColor="bg-destructive/10"
          iconColor="text-destructive"
          description={
            <>
              <p className="text-foreground text-sm">
                The account{' '}
                <span className="font-semibold text-sky-500">
                  {authenticatedUserDetails?.email}
                </span>{' '}
                was reviewed and unfortunately was not approved for access.
              </p>
              <p className="text-muted-foreground text-sm">
                This may be due to eligibility requirements or administrative policies.
              </p>
            </>
          }
          nextSteps={{
            title: 'What can you do?',
            items: [
              'Review your registration details for accuracy',
              'Contact support if you believe this is a mistake',
              'You may reapply if your eligibility changes',
            ],
            borderColor: 'border-destructive',
            bgColor: 'bg-destructive/5',
          }}
          actions={
            <Link
              href="/?signup=true"
              onClick={() => removeRefreshToken(AuthToken.REFRESH_TOKEN)}
              className="text-center text-xs text-sky-500 hover:text-sky-600"
            >
              Go back to Sign Up
            </Link>
          }
        />
      </div>
    );
  }

  return null;
};

export default ProfileOverview;
