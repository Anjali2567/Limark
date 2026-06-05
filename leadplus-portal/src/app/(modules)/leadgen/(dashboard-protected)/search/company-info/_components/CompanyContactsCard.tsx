import DataTable from '@/components/tables/DataTable';
import { LeadContactData } from '@/types/leadSearch.types';
import { PaginatedResponse } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';

type Props = {
  contacts: PaginatedResponse<LeadContactData> | undefined;
  isLoading: boolean;
  columns: ColumnType<LeadContactData>[];
  selectedContacts: string[];
  onSelectionChange: (ids: string[]) => void;
  page: number;
  onPageChange: (page: number) => void;
};

export const CompanyContactsCard = ({
  contacts,
  isLoading,
  columns,
  selectedContacts,
  onSelectionChange,
  page,
  onPageChange,
}: Props) => {
  return (
    <div className="border-border bg-card space-y-4 rounded-lg border px-5 py-4">
      <div className="flex items-center justify-between">
        <h2 className="text-foreground text-base font-semibold">
          Contacts
          {contacts?.page?.totalElements && contacts.page.totalElements > 0 && (
            <span className="text-muted-foreground ml-2 text-sm font-normal">
              ({contacts.page.totalElements})
            </span>
          )}
        </h2>
      </div>
      <DataTable
        columns={columns}
        data={contacts?.content || []}
        pagination={contacts?.page}
        isLoading={isLoading}
        defaultMessage="No contacts found."
        selectedRowIds={selectedContacts}
        onSelectionChange={onSelectionChange}
        initialPage={page}
        onPageChange={onPageChange}
      />
    </div>
  );
};
