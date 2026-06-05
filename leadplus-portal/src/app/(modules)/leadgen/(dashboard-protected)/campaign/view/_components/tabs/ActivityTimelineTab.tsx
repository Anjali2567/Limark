import { useSearchParams } from 'next/navigation';
import { useMemo, useRef, useState } from 'react';

import { SearchBar } from '@/components/SearchBar';
import { StatusBadge } from '@/components/StatusBadge';
import { Cell } from '@/components/tables/Cell';
import DataTable from '@/components/tables/DataTable';
import { useAuth } from '@/context/AuthContext';
import { useGetCampaignById } from '@/hooks/useCampaign';
import { useScrollToTop } from '@/hooks/useScrollToTop';
import { getFormattedLocation } from '@/lib/utils/campaign-formatter';
import { formatDate, formatEmployeeRange, formatName, formatTime } from '@/lib/utils/formatter';
import {
  CampaignCompanyResponse,
  CampaignContactResponse,
  EmailDataResponse,
} from '@/types/campaign.types';
import { ColumnType } from '@/types/table.types';
import {
  CampaignContactStatus as CampaignContactStatusEnum,
  EmailDeliveryStatus,
} from '@/constants/Campaign';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { CampaignContactStatus } from './CampaignContactStatus';

const PAGE_SIZE = 10;

export const getCampaignStage = (currentStep: number, emailData?: EmailDataResponse[]) => {
  if (
    emailData &&
    emailData.some((email) => email.emailDeliveryStatus === EmailDeliveryStatus.REPLIED)
  ) {
    return {
      value: 'Replied',
      status: 'ACTIVE',
    };
  }
  if (currentStep === 0) return { value: 'Not Initiated' };
  if (currentStep === 1) return { value: 'Initial Email' };
  return { value: `Follow-up ${currentStep - 1}` };
};

export const getContactStageLabel = (contact: CampaignContactResponse): string => {
  if (contact.status === CampaignContactStatusEnum.BOUNCED) return 'Bounced';
  if (contact.status === CampaignContactStatusEnum.UNSUBSCRIBED) return 'Unsubscribed';
  return getCampaignStage(contact.currentStep, contact.emailData).value;
};

const columns: ColumnType<CampaignCompanyResponse>[] = [
  {
    id: 'name',
    header: 'Company Name',
    width: '20%',
    renderCell: (row) => <Cell value={row.name ? row.name : row.id} className="font-bold" />,
  },
  {
    id: 'industry',
    header: 'Industry',
    width: '15%',
    renderCell: (row) => <Cell value={row.industry} className="text-muted-foreground" />,
  },
  {
    id: 'location',
    header: 'Location',
    width: '12%',
    renderCell: (row) => (
      <Cell
        value={getFormattedLocation({
          hqState: row.hqState,
          hqCountry: row.hqCountry,
        })}
        className="text-muted-foreground"
      />
    ),
  },
  {
    id: 'employees',
    header: 'Employees',
    width: '12%',
    align: 'right',
    renderCell: (row) => (
      <Cell
        value={formatEmployeeRange(row.employeeCount)}
        className="text-muted-foreground justify-end"
      />
    ),
  },
  {
    id: 'contacts',
    header: 'Contacts',
    width: '12%',
    align: 'center',
    renderCell: (row) => (
      <Cell value={(row.campaignContacts ?? []).length} className="justify-center" />
    ),
  },
  {
    id: 'stage',
    header: 'Campaign Stage',
    width: '10%',
    renderCell: (row) => {
      const contacts = row.campaignContacts ?? [];
      const lowestStep = Math.max(...contacts.map((c) => c.currentStep));
      const stageDetails = getCampaignStage(lowestStep);
      return contacts.length > 0 ? (
        <StatusBadge value={stageDetails.value} status={stageDetails.status} />
      ) : (
        <Cell />
      );
    },
  },
];

const subColumns: ColumnType<CampaignContactResponse>[] = [
  {
    id: 'name',
    header: 'Contact Name',
    width: '15%',
    renderCell: (row) => (
      <Cell value={formatName({ firstName: row.firstName, lastName: row.lastName })} />
    ),
  },
  {
    id: 'role',
    header: 'Role',
    width: '15%',
    renderCell: (row) => <Cell value={row.title} />,
  },
  {
    id: 'email',
    header: 'Email',
    width: '12%',
    renderCell: (row) => <Cell value={row.email} type="email" />,
  },
  {
    id: 'phone',
    header: 'Phone',
    width: '12%',
    renderCell: (row) => <Cell value={row.phoneE164} type="phoneNumber" />,
  },
  {
    id: 'status',
    header: 'Status',
    width: '12%',
    renderCell: (row) => <CampaignContactStatus data={row} />,
  },
  {
    id: 'nextEmailAt',
    header: 'Next Email At',
    width: '16%',
    renderCell: (row) => {
      const dateLabel = formatDate(row.nextSendAt);
      const timeLabel = formatTime(row.nextSendAt);

      return <Cell value={dateLabel && timeLabel ? `${dateLabel} at ${timeLabel}` : null} />;
    },
  },
];

const ActivityTimelineTab = () => {
  const searchParams = useSearchParams();
  const { authenticatedUserDetails } = useAuth();

  const campaignId = searchParams.get('id') || '';

  const topRef = useRef<HTMLDivElement>(null);

  const [pageNumber, setPageNumber] = useState<number>(0);
  const [searchValue, setSearchValue] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const { data: campaign, isLoading } = useGetCampaignById({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    campaignId,
  });

  const { scrollToTop } = useScrollToTop(topRef);

  const availableStatuses = useMemo(() => {
    return [
      ...new Set(
        campaign?.participatingCompanies?.flatMap(
          (company) => company.campaignContacts?.map(getContactStageLabel) ?? []
        ) ?? []
      ),
    ];
  }, [campaign?.participatingCompanies]);

  const filteredResults = useMemo(() => {
    const query = searchValue.trim().toLowerCase();
    const hasStatusFilter = statusFilter !== 'ALL';

    const matchesQuery = (value?: string | null) => value?.toLowerCase().includes(query) ?? false;

    const matchesCompany = (company: CampaignCompanyResponse) =>
      matchesQuery(company.name) ||
      matchesQuery(company.segment) ||
      matchesQuery(company.hqState) ||
      matchesQuery(company.hqCity);

    const matchesContact = (contact: CampaignContactResponse) =>
      [contact.fullName, contact.email, contact.title].some(matchesQuery);

    return (campaign?.participatingCompanies ?? []).reduce<CampaignCompanyResponse[]>(
      (results, company) => {
        const contacts = company.campaignContacts ?? [];

        const statusFilteredContacts = hasStatusFilter
          ? contacts.filter((contact) => getContactStageLabel(contact) === statusFilter)
          : contacts;

        if (hasStatusFilter && statusFilteredContacts.length === 0) {
          return results;
        }

        if (!query || matchesCompany(company)) {
          results.push({
            ...company,
            campaignContacts: statusFilteredContacts,
          });
          return results;
        }

        const matchingContacts = statusFilteredContacts.filter(matchesContact);

        if (matchingContacts.length > 0) {
          results.push({
            ...company,
            campaignContacts: matchingContacts,
          });
        }

        return results;
      },
      []
    );
  }, [campaign?.participatingCompanies, searchValue, statusFilter]);

  const page = useMemo(() => {
    const totalElements = filteredResults.length;
    const totalPages = Math.ceil(totalElements / PAGE_SIZE);

    return {
      size: PAGE_SIZE,
      number: pageNumber,
      totalElements,
      totalPages,
    };
  }, [filteredResults, pageNumber]);

  const paginatedResults = useMemo(() => {
    const start = pageNumber * PAGE_SIZE;
    const end = start + PAGE_SIZE;
    return filteredResults.slice(start, end);
  }, [filteredResults, pageNumber]);

  return (
    <div className="space-y-3" ref={topRef}>
      <div className="flex items-center justify-between gap-3">
        <SearchBar
          className="max-w-lg flex-1"
          placeholder="Search companies, contacts, industries, locations..."
          value={searchValue}
          onChange={(value) => {
            setSearchValue(value);
            setPageNumber(0);
          }}
        />
        <Select
          value={statusFilter}
          onValueChange={(value) => {
            setStatusFilter(value);
            setPageNumber(0);
          }}
        >
          <SelectTrigger className="w-44">
            <SelectValue placeholder="All Statuses" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All Statuses</SelectItem>
            {availableStatuses.map((status) => (
              <SelectItem key={status} value={status}>
                {status}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <DataTable<CampaignCompanyResponse, CampaignContactResponse>
        rowClassName="h-12"
        isLoading={isLoading}
        columns={columns}
        data={paginatedResults || []}
        subRowConfig={{
          columns: subColumns,
          getData: (company) => company.campaignContacts ?? [],
        }}
        pagination={page}
        onPageChange={(nextPage: number) => {
          setPageNumber(nextPage);
          scrollToTop();
        }}
      />
    </div>
  );
};

export { ActivityTimelineTab };
