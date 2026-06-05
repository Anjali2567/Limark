'use client';

import { useSearchParams } from 'next/navigation';
import { DocumentTable } from './_components/DocumentTable';
import { DocumentEditor } from './_components/DocumentEditor';

const DocumentsPage = () => {
  const searchParams = useSearchParams();
  const id = searchParams.get('id') || '';

  if (id) {
    return <DocumentEditor />;
  }

  return (
    <div className="animate-in fade-in slide-in-from-right-4 space-y-6 p-8 duration-500">
      <div className="flex flex-col">
        <h1 className="text-foreground text-3xl font-bold">Documents</h1>
        <p className="text-muted-foreground">
          Manage legal agreement documents and version history
        </p>
      </div>
      <DocumentTable />
    </div>
  );
};

export default DocumentsPage;
