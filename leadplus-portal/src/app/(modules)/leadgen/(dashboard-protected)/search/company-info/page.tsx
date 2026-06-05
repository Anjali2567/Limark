'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useMemo, useState } from 'react';

import { DataError } from '@/components/data/DataError';
import { DataLoader } from '@/components/data/DataLoader';
import { Cell } from '@/components/tables/Cell';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useGetLeadCompanyContacts, useGetLeadCompanyInfo } from '@/hooks/useSearchLeads';
import { formatLocation, formatName, formatString } from '@/lib/utils/formatter';
import { LeadContactData } from '@/types/leadSearch.types';
import { ColumnType } from '@/types/table.types';

import { CompanyContactsCard } from './_components/CompanyContactsCard';
import { CompanyDetailsCard } from './_components/CompanyDetailsCard';
import { CompanyInfoHeader } from './_components/CompanyInfoHeader';
import { CompanyInsightsCard, CompanyViewTab } from './_components/CompanyInsightsCard';
import { defaultPaginationParams } from '@/types/paginated-params';

const CompanyInfoPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const companyId = searchParams.get('company') ?? '';

  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';

  const tabParam = searchParams.get('tab');
  const insightTab = Object.values(CompanyViewTab).includes(tabParam as CompanyViewTab)
    ? (tabParam as CompanyViewTab)
    : CompanyViewTab.Overview;
  const onTabChange = (tab: CompanyViewTab) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('tab', tab);
    router.replace(`?${params.toString()}`);
  };

  const [selectedContacts, setSelectedContacts] = useState<string[]>([]);
  const [contactPage, setContactPage] = useState(0);

  const { data: company, isLoading: isCompanyLoading } = useGetLeadCompanyInfo(tenantId, companyId);
  const { data: contacts, isLoading: isContactsLoading } = useGetLeadCompanyContacts(
    tenantId,
    companyId,
    { ...defaultPaginationParams, page: contactPage.toString(), size: 5 }
  );

  const onBack = () => router.back();

  const contactColumns = useMemo<ColumnType<LeadContactData>[]>(
    () => [
      {
        id: 'firstName',
        header: 'Name',
        width: '20%',
        renderCell: (row) => {
          const name = formatName({ firstName: row.firstName, lastName: row.lastName });
          return (
            <div>
              <button
                className="text-primary text-sm font-medium hover:underline"
                onClick={() => router.push(appRoutes.leadgen.search.contactInfo(row.id))}
              >
                {name}
              </button>
              <p
                className="text-muted-foreground max-w-50 cursor-default truncate text-[12px]"
                title={row.title}
              >
                {row.title}
              </p>
            </div>
          );
        },
      },
      {
        id: 'email',
        header: 'Email',
        width: '20%',
        renderCell: (row) => <Cell value={row.email} type="email" />,
      },
      {
        id: 'locationCity',
        header: 'Location',
        width: '35%',
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
        id: 'department',
        header: 'Department',
        width: '25%',
        renderCell: (row) => <Cell value={formatString(row.department)} />,
      },
    ],
    [router]
  );

  if (isCompanyLoading) return <DataLoader />;
  if (!company) return <DataError />;

  const location = formatLocation({
    city: company.hqCity,
    state: company.hqState,
    country: company.hqCountry,
  });

  const websiteUrl = company.websiteUrl || (company.domain ? `https://${company.domain}` : '');
  const industryLabel = [company.segment, company.industry].filter(Boolean).join(' · ');

  return (
    <div className="bg-secondary flex flex-1 flex-col">
      <CompanyInfoHeader
        company={company}
        websiteUrl={websiteUrl}
        industryLabel={industryLabel}
        location={location}
        onBack={onBack}
      />
      <div className="p-6">
        <div className="grid grid-cols-[320px_1fr] gap-6">
          <div className="space-y-4">
            <CompanyDetailsCard company={company} location={location} websiteUrl={websiteUrl} />
          </div>
          <div className="space-y-6">
            <CompanyInsightsCard
              company={company}
              location={location}
              insightTab={insightTab}
              onTabChange={onTabChange}
            />
            <CompanyContactsCard
              contacts={contacts}
              isLoading={isContactsLoading}
              columns={contactColumns}
              selectedContacts={selectedContacts}
              onSelectionChange={setSelectedContacts}
              page={contactPage}
              onPageChange={setContactPage}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default CompanyInfoPage;
