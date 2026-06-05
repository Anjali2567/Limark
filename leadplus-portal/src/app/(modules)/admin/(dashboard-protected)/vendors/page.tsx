'use client';

import { Page } from '@/components/Page';
import { VendorTable } from './_components/VendorTable';
import { useSearchParams } from 'next/navigation';
import { VendorProfile } from './_components/VendorProfile';

const VendorsPage = () => {
  const searchParams = useSearchParams();
  const id = searchParams.get('id') || '';

  if (id) {
    return <VendorProfile />;
  }
  return (
    <Page className="flex-col space-y-6 p-6">
      <div className="flex flex-col justify-between">
        <h1 className="text-foreground text-3xl font-bold">Vendors</h1>
        <p className="text-muted-foreground">Manage vendor accounts and approval requests</p>
      </div>
      <VendorTable />
    </Page>
  );
};

export default VendorsPage;
