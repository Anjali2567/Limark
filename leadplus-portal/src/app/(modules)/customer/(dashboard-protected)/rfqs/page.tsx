'use client';

import { Page } from '@/components/Page';
import { RFQTable } from './_components/RFQTable';

const CustomerRfqsPage = () => {
  return (
    <Page className="flex-col space-y-6 p-8">
      <h1 className="text-foreground text-3xl font-bold">Request for Quotes (RFQs)</h1>
      <RFQTable />
    </Page>
  );
};

export default CustomerRfqsPage;
