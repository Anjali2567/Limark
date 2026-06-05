'use client';

import { CheckCircle2, Circle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { VendorAgreement } from '@/types/vendor-agreements.types';

type VendorAgreementsListProps = {
  userEmail: string;
  agreements: VendorAgreement[];
  onDocumentClick: (agreement: VendorAgreement) => void;
  onComplete: () => void;
};

const VendorAgreementsList = ({
  userEmail,
  agreements,
  onDocumentClick,
  onComplete,
}: VendorAgreementsListProps) => {
  const allSigned = agreements.every((agreement) => agreement.signed);

  const getAgreementTitle = (agreementType: string) => {
    const titleMap: Record<string, string> = {
      PRIVACY_POLICY: 'Privacy Policy',
      TERMS_OF_SERVICE: 'Terms of Service',
    };
    return titleMap[agreementType] || agreementType;
  };

  return (
    <div className="mx-auto w-full max-w-2xl">
      <div className="mb-8 text-center">
        <h1 className="text-foreground mb-2 text-3xl font-bold">Congratulations!</h1>
        <h2 className="text-2xl font-semibold text-green-600">Your Account Has Been Approved</h2>
      </div>
      <div className="bg-card border-border flex flex-col items-center justify-center space-y-6 rounded-lg border p-8 shadow-lg">
        <div className="flex h-20 w-20 items-center justify-center rounded-full bg-green-100">
          <CheckCircle2 className="h-10 w-10 text-green-600" />
        </div>

        <div className="space-y-2 text-center">
          <p className="text-foreground">
            Your vendor account <span className="font-semibold text-sky-500">{userEmail}</span> has
            been approved!
          </p>
          <p className="text-muted-foreground text-sm">
            Sign the documents below to access the LeadPlus Vendor Portal
          </p>
        </div>

        <div className="w-[75%] space-y-3 pt-4">
          {agreements.map((agreement) => (
            <button
              key={agreement.agreementType}
              onClick={() => onDocumentClick(agreement)}
              disabled={agreement.signed}
              className="bg-background border-border group flex w-full items-center justify-between rounded-lg border p-4 transition-all hover:border-sky-500 hover:shadow-sm"
            >
              <span className="text-foreground text-sm font-medium transition-colors group-hover:text-sky-500">
                {getAgreementTitle(agreement.agreementType)}
              </span>
              {agreement.signed ? (
                <CheckCircle2 className="h-5 w-5 shrink-0 text-green-600" />
              ) : (
                <Circle className="text-border h-5 w-5 shrink-0 transition-colors group-hover:text-sky-500" />
              )}
            </button>
          ))}
        </div>

        <div className="flex justify-center pt-4">
          <Button
            disabled={!allSigned}
            className="bg-sky-500 text-white hover:bg-sky-600 disabled:cursor-not-allowed disabled:opacity-50"
            onClick={onComplete}
          >
            Complete and Access Portal
          </Button>
        </div>
      </div>
    </div>
  );
};

export { VendorAgreementsList };
