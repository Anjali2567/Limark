'use client';

import { useSearchParams } from 'next/navigation';

import { Breadcrumbs } from '@/components/Breadcrumbs';
import { Page } from '@/components/Page';
import { appRoutes } from '@/config/routes';
import { useGetRequestForQuoteById } from '@/hooks/useRequestForQuote';
import { QuotationTable } from './_components/QuotationTable';

import { Loader2 } from 'lucide-react';

const CustomerRFQQuotationsPage = () => {
  const searchParams = useSearchParams();

  const rfqId = searchParams.get('rfqId');
  const { data: rfqDetails, isLoading } = useGetRequestForQuoteById(rfqId || '');

  if (isLoading) {
    return (
      <div className="flex min-h-[90dvh] items-center justify-center">
        <Loader2 className="text-primary h-8 w-8 animate-spin" />
      </div>
    );
  }

  return (
    <Page className="flex-col space-y-4 p-8">
      <Breadcrumbs
        items={[
          { label: 'RFQs', href: appRoutes.customer.rfq.root },
          {
            label:
              `RFQ-${rfqDetails?.id.slice(rfqDetails.id.length - 5, rfqDetails.id.length).toUpperCase()}` ||
              'Quotations',
          },
        ]}
      />
      <div>
        <h1
          className="text-foreground font-semibold"
          style={{ fontSize: '24px', lineHeight: '32px' }}
        >
          Quotes for: {rfqDetails?.title || 'RFQ Details Not Found'}
        </h1>
        <p className="text-muted-foreground mt-1" style={{ fontSize: '14px' }}>
          {`${rfqDetails?.numberOfQuotationsForRfq || 0}  quotes received`}
        </p>
      </div>
      <QuotationTable />
    </Page>
  );
};

export default CustomerRFQQuotationsPage;
