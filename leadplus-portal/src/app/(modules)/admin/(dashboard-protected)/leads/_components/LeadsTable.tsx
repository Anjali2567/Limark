import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { NumberSkeleton } from '@/components/Skeleton';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { formatName } from '@/lib/utils/formatter';
import { LeadsListResponse } from '@/types/leads.types';
import { ColumnType } from '@/types/table.types';

import { User } from 'lucide-react';
import { useGetAllLeadContacts } from '@/hooks/useLeads';
import { defaultPaginationParams } from '@/types/paginated-params';

const LeadsTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const page = searchParams.get('page') || '0';

  const [searchQuery, setSearchQuery] = useState<string>('');

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('page', newPage.toString());
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const handleSearch = useCallback(
    (query: string) => {
      setSearchQuery(query);
      handlePageChange(0);
    },
    [handlePageChange]
  );

  const { data, isLoading } = useGetAllLeadContacts({
    ...defaultPaginationParams,
    page: Number(page),
    query: searchQuery,
  });

  const columns: ColumnType<LeadsListResponse>[] = useMemo(() => {
    return [
      {
        id: 'name',
        header: 'Name',
        width: '20%',
        renderCell: (row) => (
          <Cell
            value={formatName({
              firstName: row.firstName,
              lastName: row.lastName,
            })}
          />
        ),
      },
      {
        id: 'designation',
        header: 'Designation',
        width: '20%',
        renderCell: (row) => <Cell value={row.title} />,
      },
      {
        id: 'email',
        header: 'Email',
        width: '25%',
        renderCell: (row) => <Cell value={row.email} type="email" />,
      },
      {
        id: 'phoneNumber',
        header: 'Phone Number',
        width: '15%',
        renderCell: (row) => <Cell value={row.phoneE164} type="phoneNumber" />,
      },
      {
        id: 'company',
        header: 'Company',
        width: '20%',
        renderCell: (row) => <Cell value={row.companyName} />,
      },
    ];
  }, []);

  return (
    <>
      <div className="flex flex-col items-center justify-between gap-4 lg:flex-row">
        <SearchBar
          value={searchQuery}
          className="w-lg"
          onChange={handleSearch}
          placeholder="Search by leads name, company, email..."
        />

        <div className="flex items-center gap-6 text-sm">
          <div className="flex items-center gap-2">
            <User className="text-muted-foreground h-4 w-4" />
            <span className="text-muted-foreground text-nowrap">Total Leads:</span>
            {isLoading ? (
              <NumberSkeleton />
            ) : (
              <span className="text-foreground font-semibold">{data?.page?.totalElements}</span>
            )}
          </div>
        </div>
      </div>
      <DataTable
        rowClassName="h-12"
        columns={columns}
        data={data?.content || []}
        isLoading={isLoading}
        defaultMessage="No Leads Found"
        pagination={data?.page}
        initialPage={Number(page)}
        onPageChange={handlePageChange}
      />
    </>
  );
};

export { LeadsTable };
