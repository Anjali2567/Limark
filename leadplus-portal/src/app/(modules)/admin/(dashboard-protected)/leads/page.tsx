'use client';

import { ExcelImportControls } from '@/components/import/ExcelImportControls';
import { useLeadsContactsImport } from '@/hooks/useLeadsContactsImport';

import { LeadsTable } from './_components/LeadsTable';

const LeadsPage = () => {
  const leadsImport = useLeadsContactsImport();

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 space-y-6 p-8 duration-500">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-foreground text-3xl font-bold">Leads</h1>
          <p className="text-muted-foreground mt-1">Manage and track individual leads</p>
        </div>
        <div className="flex items-center gap-2">
          <ExcelImportControls entityLabel="Leads" importHook={leadsImport} />
        </div>
      </div>
      <LeadsTable />
    </div>
  );
};

export default LeadsPage;
