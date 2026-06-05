import { useMemo } from 'react';

import { Cell } from '@/components/tables/Cell';
import { ImageCell } from '@/components/tables/ImageCell';
import { ListItemCell } from '@/components/tables/ListItemCell';
import { SocialCell } from '@/components/tables/SocialCell';
import { Button } from '@/components/ui/button';
import { useMetadataFilterValues } from '@/hooks/useSearchLeads';
import { formatLocation, formatName } from '@/lib/utils/formatter';
import { LeadContactData } from '@/types/leadSearch.types';
import { ColumnType } from '@/types/table.types';
import { ContactRowActions } from '../search/contacts/_components/ContactRowActions';

const METADATA_COLUMN_IDS = new Set(['bdName', 'isrName', 'priority', 'titleCategory', 'dataSource']);

export const useContactSearchColumns = (
  tenantId: string,
  onClickContactName: (contactId: string) => void,
  onClickCompanyName: (companyDomain: string) => void,
  onProceedToEmail?: (contacts: LeadContactData[]) => void
) => {
  const { data: metadataFilterValues } = useMetadataFilterValues(tenantId);

  const hasMetadata = useMemo(() => {
    if (!metadataFilterValues) return false;
    return Object.values(metadataFilterValues).some((values) => values.length > 0);
  }, [metadataFilterValues]);

  const columns = useMemo<ColumnType<LeadContactData>[]>(() => {
    const getCrmBadge = (row: LeadContactData) => {
      if (row.zohoExisting)
        return {
          src: '/assets/zoho-icon.png',
          tooltip: 'This contact is already a contact in Zoho CRM',
        };
      if (row.hubspotExisting)
        return {
          src: '/assets/hubspot-icon.png',
          tooltip: 'This contact is already a contact in HubSpot CRM',
        };
      return { src: '', tooltip: '' };
    };

    const allColumns: ColumnType<LeadContactData>[] = [
      {
        id: 'contactName',
        header: 'Contact Name',
        width: '180px',
        renderCell: (row) => {
          const { src, tooltip } = getCrmBadge(row);
          return (
            <Button variant="compressed" onClick={() => onClickContactName(row.id)}>
              <ImageCell
                className="flex cursor-pointer flex-row-reverse items-start justify-end text-sky-500 hover:underline"
                text={formatName({
                  firstName: row.firstName,
                  lastName: row.lastName,
                })}
                src={src}
                hideInitials={true}
                tooltip={tooltip}
              />
            </Button>
          );
        },
      },
      {
        id: 'socials',
        header: 'Socials',
        width: '80px',
        align: 'center',
        renderCell: (row) =>
          row.linkedinUrl ? <SocialCell linkedInUrl={row.linkedinUrl} /> : <Cell />,
      },
      {
        id: 'title',
        header: 'Title',
        width: '180px',
        renderCell: (row) => <Cell value={row.title} />,
      },
      {
        id: 'companyName',
        header: 'Company Name',
        width: '180px',
        renderCell: (row) => (
          <Button variant="compressed" onClick={() => onClickCompanyName(row.companyDomain)}>
            <ImageCell
              className="flex cursor-pointer items-start justify-end text-sky-500 hover:underline"
              src={row.companyLogoUrl}
              text={row.companyName}
              alt="Company Logo"
            />
          </Button>
        ),
      },
      {
        id: 'email',
        header: 'Email',
        width: '250px',
        renderCell: (row) => <Cell value={row.email} type="email" />,
      },
      {
        id: 'phoneNumber',
        header: 'Phone Number',
        width: '150px',
        renderCell: (row) => <Cell value={row.phoneE164} />,
      },
      {
        id: 'location',
        header: 'Location',
        width: '300px',
        renderCell: (row) => (
          <Cell
            value={formatLocation({
              city: row.locationCity,
              state: row.locationState,
              country: row.locationCountry,
            })}
          />
        ),
      },
      {
        id: 'industry',
        header: 'Industry',
        width: '250px',
        renderCell: (row) => <Cell value={row.companySegment || row.companyIndustry} />,
      },
      {
        id: 'links',
        header: 'Company Links',
        width: '150px',
        renderCell: (row) => {
          const websiteUrl = row.companyWebsiteUrl || `https://${row.companyDomain}`;

          return (
            <SocialCell
              websiteUrl={websiteUrl}
              linkedInUrl={row.companyLinkedinUrl}
              facebookUrl={row.companyFacebookUrl}
              twitterUrl={row.companyTwitterUrl}
            />
          );
        },
      },
      {
        id: 'bdName',
        header: 'BD Name',
        width: '150px',
        renderCell: (row) => <Cell value={row.tenantMetadata?.bdName} />,
      },
      {
        id: 'isrName',
        header: 'ISR Name',
        width: '150px',
        renderCell: (row) => <Cell value={row.tenantMetadata?.isrName} />,
      },
      {
        id: 'priority',
        header: 'Priority',
        width: '100px',
        renderCell: (row) => <Cell value={row.tenantMetadata?.priority} />,
      },
      {
        id: 'titleCategory',
        header: 'Title Category',
        width: '180px',
        renderCell: (row) => <Cell value={row.tenantMetadata?.titleCategory} />,
      },
      {
        id: 'dataSource',
        header: 'Data Source',
        width: '150px',
        renderCell: (row) => <Cell value={row.source} />,
      },
      {
        id: 'currentCampaigns',
        header: 'Current Campaigns',
        width: '250px',
        renderCell: (row) => (
          <ListItemCell items={row.currentCampaigns?.map((campaign) => campaign.name) || []} />
        ),
      },
      {
        id: 'actions',
        header: 'Engage',
        width: '200px',
        align: 'center',
        fixedRight: true,
        cellClassName: 'bg-secondary',
        renderCell: (row) => (
          <ContactRowActions data={row} onSendEmail={(contact) => onProceedToEmail?.([contact])} />
        ),
      },
    ];

    return allColumns.filter((col) => hasMetadata || !METADATA_COLUMN_IDS.has(col.id));
  }, [hasMetadata, onClickCompanyName, onClickContactName, onProceedToEmail]);

  return columns;
};
