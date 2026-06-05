'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { toast } from 'sonner';

import { DocumentSigningDrawer } from '@/app/(modules)/vendor/(header)/agreements/_components/DocumentSigningDrawer';
import { VendorAgreementsList } from '@/app/(modules)/vendor/(header)/agreements/_components/VendorAgreementsList';
import { Spinner } from '@/components/ui/spinner';
import { appRoutes } from '@/config/routes';
import { AgreementType } from '@/constants/agreement.constant';
import { useAuth } from '@/context/AuthContext';
import { useGetAuthenticatedVendor } from '@/hooks/useAuthentication';
import {
  useGetAllVendorAgreements,
  useRequestOtpForVendorAgreement,
  useVerifyVendorAgreementOtp,
} from '@/hooks/useVendorAgreement';
import { VendorAgreement } from '@/types/vendor-agreements.types';

const VendorAgreementPage = () => {
  const router = useRouter();
  const [selectedAgreement, setSelectedAgreement] = useState<VendorAgreement | null>(null);

  const { authenticatedUser } = useAuth();
  const { data: vendor, isLoading: isVendorLoading } = useGetAuthenticatedVendor();
  const allVendorAgreementsQueries = useGetAllVendorAgreements(vendor?.id || '');
  const { mutate: requestOtpMutation } = useRequestOtpForVendorAgreement();
  const { mutate: verifyOtpMutation } = useVerifyVendorAgreementOtp();

  const isAgreementsLoading = allVendorAgreementsQueries.some((query) => query.isLoading);
  const agreements = allVendorAgreementsQueries
    .map((query) => query.data)
    .filter((data): data is VendorAgreement => data !== undefined);

  const handleRequestOtp = (agreementType: AgreementType) => {
    if (!vendor?.id) return;

    requestOtpMutation(
      {
        vendorId: vendor.id,
        agreementType: agreementType,
      },
      {
        onSuccess: () => {
          toast.success('OTP sent to your email!');
        },
        onError: () => {
          toast.error('Failed to send OTP. Please try again.');
        },
      }
    );
  };

  const handleSign = (data: { signedBy: string; name: string; title: string; otp: string }) => {
    if (!vendor?.id || !selectedAgreement) return;
    verifyOtpMutation(
      {
        vendorId: vendor.id,
        agreementType: selectedAgreement.agreementType,
        signedBy: data.signedBy,
        name: data.name,
        title: data.title,
        otp: data.otp,
      },
      {
        onSuccess: () => {
          toast.success('Document signed successfully!');
          setSelectedAgreement(null);
        },
      }
    );
  };

  const handleComplete = () => {
    router.push(appRoutes.leadgen.search.companySearch);
  };

  if (isVendorLoading || isAgreementsLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Spinner className="h-8 w-8" />
      </div>
    );
  }

  if (!vendor) return null;

  return (
    <div className="flex flex-1 items-center justify-center p-8">
      <VendorAgreementsList
        userEmail={authenticatedUser?.email || ''}
        agreements={agreements}
        onDocumentClick={setSelectedAgreement}
        onComplete={handleComplete}
      />
      <DocumentSigningDrawer
        agreement={selectedAgreement}
        vendorCompanyName={vendor.companyName}
        onClose={() => setSelectedAgreement(null)}
        onSign={handleSign}
        onRequestOtp={handleRequestOtp}
      />
    </div>
  );
};

export default VendorAgreementPage;
