'use client';

import { useSearchParams } from 'next/navigation';

import { Page } from '@/components/Page';
import { TenantsTable } from './_components/TenantsTable';
import { TenantDetailView } from './_components/TenantDetailView';

const AdminTenantsPage = () => {
  const searchParams = useSearchParams();
  const tenantId = searchParams.get('id');

  if (tenantId) {
    return <TenantDetailView tenantId={tenantId} />;
  }

  return (
    <Page className="flex-col space-y-6 p-8">
      <div className="flex flex-col">
        <h1 className="text-foreground text-3xl font-bold">Tenants</h1>
        <p className="text-muted-foreground">Manage and view all tenants across the platform</p>
      </div>
      <TenantsTable />
    </Page>
  );
};

export default AdminTenantsPage;
