'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo } from 'react';
import { toast } from 'sonner';

import { DataError } from '@/components/data/DataError';
import { DataLoader } from '@/components/data/DataLoader';
import { Button } from '@/components/ui/button';
import { appRoutes } from '@/config/routes';
import { QuotationStatus } from '@/constants/quotation.constant';
import { useAuth } from '@/context/AuthContext';
import {
  useAcceptRFQQuotation,
  useGetRFQQuotationById,
  useRejectRFQQuotation,
} from '@/hooks/useQuotation';
import { RFQQuotationDetails } from './_components/RFQQuotationDetails';

import { ArrowLeft } from 'lucide-react';
import { Page } from '@/components/Page';

const VendorRFQSummaryPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const rfqId = searchParams.get('id');
  const { authenticatedUserDetails } = useAuth();

  const { mutate: acceptRFQQuotation } = useAcceptRFQQuotation();
  const { mutate: rejectRFQQuotation } = useRejectRFQQuotation();

  const {
    data: quotationData,
    isLoading,
    isError,
  } = useGetRFQQuotationById(
    authenticatedUserDetails?.tenantId || '',
    authenticatedUserDetails?.workspaceId || '',
    rfqId || ''
  );

  const onBack = useCallback(() => {
    router.push(appRoutes.vendor.rfq.root);
  }, [router]);

  const onAccept = useCallback(() => {
    if (!rfqId || !authenticatedUserDetails) return;
    acceptRFQQuotation(
      {
        tenantId: authenticatedUserDetails.tenantId,
        workspaceId: authenticatedUserDetails.workspaceId,
        quotationId: rfqId,
      },
      {
        onSuccess: () => {
          toast.success('RFQ accepted successfully');
        },
      }
    );
  }, [acceptRFQQuotation, rfqId, authenticatedUserDetails]);

  const onReject = useCallback(() => {
    if (!rfqId || !authenticatedUserDetails) return;
    rejectRFQQuotation(
      {
        tenantId: authenticatedUserDetails.tenantId,
        workspaceId: authenticatedUserDetails.workspaceId,
        quotationId: rfqId,
      },
      {
        onSuccess: () => {
          toast.success('RFQ rejected successfully');
        },
      }
    );
  }, [rejectRFQQuotation, rfqId, authenticatedUserDetails]);

  const title = useMemo(() => {
    if (quotationData?.status === QuotationStatus.ACCEPTED) return 'Submit Quotation';
    if (quotationData?.status === QuotationStatus.QUOTED) return 'View Quotation';
    return 'View RFQ';
  }, [quotationData]);

  if (isLoading) {
    return <DataLoader />;
  }

  if (isError) {
    return <DataError message="Error loading RFQ details" />;
  }

  return (
    <Page className="bg-muted/30 flex-col">
      <div className="border-border bg-card sticky top-0 z-10 flex items-center justify-between border-b px-6 py-4">
        <div className="flex items-start gap-4">
          <Button
            variant="ghost"
            size="icon"
            onClick={onBack}
            className="text-muted-foreground hover:bg-secondary hover:text-foreground h-8 w-8"
          >
            <ArrowLeft className="h-5 w-5" />
            <span className="sr-only">Back</span>
          </Button>

          <div className="flex flex-col gap-1">
            <h1 className="text-foreground text-2xl font-bold">{title}</h1>
            <div className="flex items-center gap-3">
              <span className="text-muted-foreground text-sm font-medium">{`RFQ-${rfqId?.slice(rfqId.length - 5, rfqId.length)?.toUpperCase()}`}</span>
            </div>
          </div>
        </div>
        {quotationData?.status === QuotationStatus.PENDING && (
          <div className="flex gap-3">
            <Button
              variant="outline"
              onClick={onReject}
              className="border-destructive text-destructive hover:bg-destructive disabled:hover:text-destructive transition-colors hover:text-white disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:bg-transparent"
            >
              Reject
            </Button>
            <Button
              onClick={onAccept}
              className="bg-primary hover:bg-primary/90 text-primary-foreground disabled:hover:bg-primary disabled:cursor-not-allowed disabled:opacity-50"
            >
              Accept
            </Button>
          </div>
        )}
      </div>
      {quotationData && <RFQQuotationDetails quotationData={quotationData} />}
    </Page>
  );
};

export default VendorRFQSummaryPage;
