'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback } from 'react';

import { Page } from '@/components/Page';
import { Button } from '@/components/ui/button';
import { IndustryTable } from './_components/IndustryTable';
import { IndustryDrawer } from './_components/IndustryDrawer';

import { Plus } from 'lucide-react';

const IndustriesPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const handleAddClick = useCallback(() => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('action', 'add');
    router.push(`?${params.toString()}`);
  }, [router, searchParams]);

  return (
    <Page className="flex-col space-y-6 p-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-foreground text-3xl font-bold">Industries</h1>
          <p className="text-muted-foreground">Manage industry categories and track performance</p>
        </div>
        <Button onClick={handleAddClick} className="bg-sky-500 text-white hover:bg-sky-600">
          <Plus className="mr-2 h-4 w-4" />
          Add Industry
        </Button>
      </div>
      <IndustryTable />
      <IndustryDrawer />
    </Page>
  );
};

export default IndustriesPage;
