import { useMemo } from 'react';

import { Cell } from '@/components/tables/Cell';
import { ImageCell } from '@/components/tables/ImageCell';
import { SocialCell } from '@/components/tables/SocialCell';
import { Button } from '@/components/ui/button';
import { formatEmployeeRange, formatLocation, formatRevenueRange } from '@/lib/utils/formatter';
import { LeadCompanyData } from '@/types/leadSearch.types';
import { ColumnType } from '@/types/table.types';
import { CompanyRowActions } from '../search/companies/_components/CompanyRowActions';

export const useCompanySearchColumns = (
  onClickCompanyName: (companyId: string) => void,
  onClickContacts: (row: LeadCompanyData) => void
) => {
  const columns = useMemo<ColumnType<LeadCompanyData>[]>(
    () => [
      {
        id: 'name',
        header: 'Name',
        width: '250px',
        renderCell: (row) => (
          <Button variant="compressed" onClick={() => onClickCompanyName(row.domain)}>
            <ImageCell
              className="flex cursor-pointer items-start justify-end text-sky-500 hover:underline"
              src={row.logoUrl}
              text={row.name}
              alt="Logo"
            />
          </Button>
        ),
      },
      {
        id: 'links',
        header: 'Socials',
        width: '150px',
        renderCell: (row) => {
          const websiteUrl = row.websiteUrl || `https://${row.domain}`;
          return (
            <SocialCell
              websiteUrl={websiteUrl}
              linkedInUrl={row.linkedinUrl}
              facebookUrl={row.facebookUrl}
              twitterUrl={row.twitterUrl}
            />
          );
        },
      },
      {
        id: 'contactCount',
        header: 'Contacts',
        width: '100px',
        renderCell: (row) => (
          <Button
            variant="compressed"
            onClick={() => onClickContacts(row)}
            className="w-full min-w-full justify-center"
          >
            <Cell value={row.contactCount} className="text-sky-500 hover:underline" />
          </Button>
        ),
      },
      {
        id: 'location',
        header: 'Location',
        width: '300px',
        renderCell: (row) => (
          <Cell
            value={formatLocation({
              city: row.hqCity,
              state: row.hqState,
              country: row.hqCountry,
            })}
          />
        ),
      },
      {
        id: 'employeeCount',
        header: 'Employee Range',
        width: '150px',
        renderCell: (row) => <Cell value={formatEmployeeRange(row.employeeCount)} />,
      },
      {
        id: 'revenue',
        header: 'Revenue',
        width: '150px',
        renderCell: (row) => <Cell value={formatRevenueRange(row.revenueUsd)} />,
      },
      {
        id: 'industry',
        header: 'Industry',
        width: '250px',
        renderCell: (row) => <Cell value={row.segment || row.industry} />,
      },
      {
        id: 'actions',
        header: 'Engage',
        width: '120px',
        align: 'center',
        fixedRight: true,
        cellClassName: 'bg-secondary',
        renderCell: (row) => <CompanyRowActions data={row} />,
      },
    ],
    [onClickCompanyName, onClickContacts]
  );

  return columns;
};
