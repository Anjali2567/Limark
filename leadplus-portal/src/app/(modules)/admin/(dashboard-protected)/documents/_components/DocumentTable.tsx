import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { useGetAllAgreements } from '@/hooks/useAgreement';
import { convertDate } from '@/lib/utils/timeConversion';
import { Agreement } from '@/types/agreements.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType, FilterOption } from '@/types/table.types';
import { DocumentTableActions } from './DocumentTableActions';
import { formatString } from '@/lib/utils/formatter';
import { DocumentDrawer } from './DocumentDrawer';
import useToggle from '@/hooks/useToggle';
import { AgreementType } from '@/constants/agreement.constant';

const DocumentTable = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const page = searchParams.get('page') || '0';

  const { value: isOpen, toggle, setTrue } = useToggle();
  const [selectedDocument, setSelectedDocument] = useState<Agreement | undefined>(undefined);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [filtersMap, setFiltersMap] = useState<Record<string, FilterOption | null>>({});

  const { data, isLoading } = useGetAllAgreements({
    ...defaultPaginationParams,
    query: searchQuery,
    sort: 'version,desc',
    page: Number(page),
    agreementType: filtersMap['title']?.value as AgreementType,
  });

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
      handlePageChange(0);
      setSearchQuery(query);
    },
    [handlePageChange]
  );

  const handleFiltersChange = useCallback(
    (key: string, filter: FilterOption | null) => {
      handlePageChange(0);
      setFiltersMap((prev) => ({
        ...prev,
        [key]: filter,
      }));
    },
    [handlePageChange]
  );

  const handleRowClick = useCallback(
    (rowId: string) => {
      const row = data?.content.find((item) => item.id === rowId);
      if (!row) return;
      setSelectedDocument(row);
      setTrue();
    },
    [setTrue, data]
  );

  const columns = useMemo<ColumnType<Agreement>[]>(
    () => [
      {
        id: 'title',
        header: 'Document Title',
        width: '35%',
        filterable: true,
        filterOptions: [
          {
            label: 'All',
            value: null,
          },
          ...Object.values(AgreementType).map((type) => ({
            label: formatString(type),
            value: type,
          })),
        ],
        renderCell: (row) => <Cell value={formatString(row.agreementType)} />,
      },
      {
        id: 'version',
        header: 'Version',
        width: '25%',
        renderCell: (row) => <Cell value={row.version} />,
      },
      {
        id: 'createdAt',
        header: 'Effective Date',
        width: '25%',
        renderCell: (row) => <Cell value={convertDate(new Date(row.createdAt))} />,
      },
      {
        id: 'actions',
        header: '',
        width: '15%',
        renderCell: (row) => <DocumentTableActions data={row} />,
        align: 'right',
      },
    ],
    []
  );

  return (
    <>
      <SearchBar
        className="max-w-sm"
        placeholder="Search documents..."
        value={searchQuery}
        onChange={handleSearch}
      />
      <DataTable
        columns={columns}
        data={data?.content || []}
        pagination={data?.page}
        isLoading={isLoading}
        defaultMessage="No Documents Found"
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        onRowClick={handleRowClick}
        filtersMap={filtersMap}
        onFiltersChange={handleFiltersChange}
      />
      <DocumentDrawer isOpen={isOpen} setIsOpen={toggle} document={selectedDocument} />
    </>
  );
};

export { DocumentTable };
