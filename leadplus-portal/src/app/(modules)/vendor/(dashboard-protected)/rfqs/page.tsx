'use client';

import { Page } from '@/components/Page';
import { RFQTable } from './_components/RFQTable';

const VendorRfqsPage = () => {
  return (
    <Page className="flex-col space-y-6 p-8">
      <div>
        <h1 className="text-foreground text-2xl font-bold">Request for Quotes (RFQs)</h1>
        <p className="text-muted-foreground mt-1 text-sm">
          Review and respond to software development RFQs from potential clients
        </p>
      </div>
      <RFQTable />
    </Page>
  );
};

export default VendorRfqsPage;
