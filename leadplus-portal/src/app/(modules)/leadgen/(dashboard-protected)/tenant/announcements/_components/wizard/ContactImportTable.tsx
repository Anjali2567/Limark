'use client';

import { useMemo } from 'react';
import { Search } from 'lucide-react';

import DataTable from '@/components/tables/DataTable';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { PaginatedResponse } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { ImageCell } from '@/components/tables/ImageCell';
import { formatName } from '@/lib/utils/formatter';
import { Cell } from '@/components/tables/Cell';

export type ImportContactRow = {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  companyName?: string;
};

type ContactImportTableProps = {
  data: PaginatedResponse<ImportContactRow> | undefined;
  isLoading: boolean;
  selectedRowIds: string[];
  page: number;
  onPageChange: (page: number) => void;
  onSelectionChange: (ids: string[]) => void;
  searchQuery: string;
  onSearchQueryChange: (q: string) => void;
};

const ContactImportTable = ({
  data,
  isLoading,
  selectedRowIds,
  page,
  onPageChange,
  onSelectionChange,
  searchQuery,
  onSearchQueryChange,
}: ContactImportTableProps) => {
  const columns = useMemo<ColumnType<ImportContactRow>[]>(
    () => [
      {
        id: 'firstName',
        header: 'Name',
        width: '40%',
        renderCell: (row) => {
          return (
            <ImageCell
              text={formatName({ firstName: row.firstName, lastName: row.lastName })}
              subText={row.email}
            />
          );
        },
      },
      {
        id: 'companyName',
        header: 'Company',
        width: '40%',
        renderCell: (row) => <Cell value={row.companyName} />,
      },
      {
        id: 'status',
        header: 'Status',
        width: '20%',
        align: 'center',
        renderCell: (row) => {
          const isAdded = selectedRowIds.includes(row.id);
          return (
            <span className="text-sm">
              {isAdded ? (
                <Badge className="bg-primary/10 text-primary shrink-0 rounded-sm border-0 px-1.5 py-0 text-sm shadow-none">
                  Added
                </Badge>
              ) : (
                '-'
              )}
            </span>
          );
        },
      },
    ],
    [selectedRowIds]
  );

  return (
    <div className="flex flex-col gap-3">
      <span className="text-muted-foreground text-right text-xs">
        {selectedRowIds.length} Contacts Selected
      </span>
      <div className="relative">
        <Search className="text-muted-foreground absolute top-1/2 left-2.5 h-3.5 w-3.5 -translate-y-1/2" />
        <Input
          className="h-8 pl-8 text-xs"
          placeholder="Search contacts..."
          value={searchQuery}
          onChange={(e) => onSearchQueryChange(e.target.value)}
        />
      </div>
      <DataTable
        data={data?.content ?? []}
        columns={columns}
        isLoading={isLoading}
        pagination={data?.page}
        initialPage={page}
        onPageChange={onPageChange}
        selectable
        selectedRowIds={selectedRowIds}
        onSelectionChange={onSelectionChange}
        defaultMessage="No contacts found"
      />
    </div>
  );
};

export { ContactImportTable };
