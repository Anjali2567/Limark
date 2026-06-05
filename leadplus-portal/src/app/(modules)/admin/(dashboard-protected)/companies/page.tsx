'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo, useState } from 'react';

import { ExcelImportControls } from '@/components/import/ExcelImportControls';
import { SearchBar } from '@/components/SearchBar';
import { NumberSkeleton } from '@/components/Skeleton';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { Button } from '@/components/ui/button';
import { useCompaniesImport } from '@/hooks/useCompaniesImport';
import { useGetAllIndustriesInLeads, useGetAllLeadsWithCounts } from '@/hooks/useLeads';
import useToggle from '@/hooks/useToggle';
import { formatRevenueRange } from '@/lib/utils/formatter';
import { Leads } from '@/types/leads.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { ColumnType, FilterOption } from '@/types/table.types';
import { ContactListDrawer } from './_components/ContactListDrawer';

import { Building, Users } from 'lucide-react';

const CompaniesPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const page = searchParams.get('page') || '0';

  const companiesImport = useCompaniesImport();

  const { value: isCompanyContactsDrawerOpen, toggle: toggleCompanyContactsDrawer } = useToggle();
  const [selectedCompany, setSelectedCompany] = useState<Leads | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [filtersMap, setFiltersMap] = useState<Record<string, FilterOption | null>>({});

  const { data: industriesData } = useGetAllIndustriesInLeads();
  const { data, isLoading } = useGetAllLeadsWithCounts({
    ...defaultPaginationParams,
    page: Number(page),
    searchText: searchQuery,
    segments: [filtersMap['industry']?.value as string],
  });

  const handleCloseDrawer = useCallback(() => {
    setSelectedCompany(null);
    toggleCompanyContactsDrawer();
  }, [toggleCompanyContactsDrawer]);

  const handleContactClick = useCallback(
    (company: Leads) => {
      setSelectedCompany(company);
      toggleCompanyContactsDrawer();
    },
    [toggleCompanyContactsDrawer]
  );

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('page', newPage.toString());
      router.push(`?${params.toString()}`);
    },
    [router, searchParams]
  );

  const handleFiltersChange = useCallback(
    (key: string, filter: FilterOption | null) => {
      setFiltersMap((prev) => ({ ...prev, [key]: filter }));
      handlePageChange(0);
    },
    [handlePageChange]
  );

  const handleSearch = useCallback(
    (query: string) => {
      setSearchQuery(query);
      handlePageChange(0);
    },
    [handlePageChange]
  );

  const stats = useMemo(
    () => [
      {
        icon: Building,
        label: 'Total Companies:',
        value: data?.totalCompanies ?? 0,
      },
      {
        icon: Users,
        label: 'Total Contacts:',
        value: data?.totalContacts ?? 0,
      },
    ],
    [data]
  );

  const columns: ColumnType<Leads>[] = useMemo(
    () => [
      {
        id: 'name',
        header: 'Company Name',
        width: '30%',
        renderCell: (row) => (
          <div>
            <p className="text-foreground font-semibold">{row.name}</p>
            <p className="text-muted-foreground text-xs">{row.domain}</p>
          </div>
        ),
      },
      {
        id: 'industry',
        header: 'Industry',
        width: '20%',
        filterable: true,
        filterOptions: [
          { label: 'All', value: null },
          ...(industriesData ?? []).map((industry) => ({
            label: industry,
            value: industry,
          })),
        ],
        renderCell: (row) => <Cell value={row.segment} />,
      },
      {
        id: 'location',
        header: 'Location',
        width: '15%',
        renderCell: (row) => {
          const state = row.hqState || '';
          const country = row.hqCountry || '';
          const location = [state, country].filter(Boolean).join(', ');
          return <Cell value={location} />;
        },
      },
      {
        id: 'employees',
        header: 'Employees',
        width: '15%',
        align: 'right',
        renderCell: (row) => {
          let value = row.employeeCount;
          if (typeof value === 'string') {
            const parts = value.split(' ');
            if (parts.length > 1) {
              parts.pop();
              value = parts.join(',');
            }
          }
          return <Cell value={value} className="justify-end" />;
        },
      },
      {
        id: 'contacts',
        header: 'Contacts',
        width: '10%',
        align: 'center',
        renderCell: (row) =>
          row.contactCount ? (
            <Button
              variant="none"
              onClick={() => handleContactClick(row)}
              className="w-full cursor-pointer p-0 text-sm font-medium text-sky-500 underline hover:text-sky-600"
            >
              {row.contactCount}
            </Button>
          ) : (
            <Cell value={row.contactCount} className="justify-center" />
          ),
      },
      {
        id: 'revenue',
        header: 'Revenue',
        width: '10%',
        renderCell: (row) => <Cell value={formatRevenueRange(row.revenueUsd)} />,
      },
    ],
    [handleContactClick, industriesData]
  );

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 space-y-6 p-8 duration-500">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-foreground text-3xl font-bold">Companies</h1>
          <p className="text-muted-foreground mt-1">Manage and organize your company database</p>
        </div>
        <div className="flex items-center gap-2">
          <ExcelImportControls entityLabel="Companies" importHook={companiesImport} />
        </div>
      </div>
      <div className="flex flex-col items-center justify-between gap-4 lg:flex-row">
        <SearchBar
          value={searchQuery}
          className="w-lg"
          onChange={handleSearch}
          placeholder="Search companies..."
        />

        <div className="flex items-center gap-6 text-sm">
          {stats.map((stat) => (
            <div className="flex items-center gap-2" key={stat.label}>
              <stat.icon className="text-muted-foreground h-4 w-4" />
              <span className="text-muted-foreground text-nowrap">{stat.label}</span>
              {isLoading ? (
                <NumberSkeleton />
              ) : (
                <span className="text-foreground font-semibold">{stat.value}</span>
              )}
            </div>
          ))}
        </div>
      </div>
      <DataTable
        columns={columns}
        data={data?.companies?.content || []}
        isLoading={isLoading}
        defaultMessage="No Companies Found"
        pagination={data?.companies?.page}
        initialPage={Number(page)}
        onPageChange={handlePageChange}
        filtersMap={filtersMap}
        onFiltersChange={handleFiltersChange}
      />
      <ContactListDrawer
        isCompanyContactsDrawerOpen={isCompanyContactsDrawerOpen}
        setIsCompanyContactsDrawerOpen={handleCloseDrawer}
        selectedCompany={selectedCompany}
      />
    </div>
  );
};

export default CompaniesPage;
